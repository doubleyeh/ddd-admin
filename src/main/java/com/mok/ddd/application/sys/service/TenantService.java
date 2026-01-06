package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.common.service.BaseServiceImpl;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.sys.dto.tenant.TenantCreateResultDTO;
import com.mok.ddd.application.sys.dto.tenant.TenantDTO;
import com.mok.ddd.application.sys.dto.tenant.TenantOptionDTO;
import com.mok.ddd.application.sys.dto.tenant.TenantSaveDTO;
import com.mok.ddd.application.sys.event.TenantCreatedEvent;
import com.mok.ddd.application.sys.mapper.TenantMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.PasswordGenerator;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.sys.model.QTenant;
import com.mok.ddd.domain.sys.model.QTenantPackage;
import com.mok.ddd.domain.sys.model.Tenant;
import com.mok.ddd.domain.sys.repository.TenantRepository;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.mok.ddd.domain.sys.model.QTenant.tenant;

@Service
@RequiredArgsConstructor
public class TenantService extends BaseServiceImpl<Tenant, Long, TenantDTO> {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @NonNull
    protected CustomRepository<Tenant, Long> getRepository() {
        return tenantRepository;
    }

    @Override
    protected Tenant toEntity(@NonNull TenantDTO dto) {
        throw new UnsupportedOperationException("不支持从DTO创建或更新实体，请从Repository获取实体并使用其业务方法进行更新。");
    }

    @Override
    protected TenantDTO toDto(@NonNull Tenant entity) {
        return tenantMapper.toDto(entity);
    }

    @Override
    protected String getEntityAlias(){
        return QTenant.tenant.getMetadata().getName();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull TenantDTO> findPage(Predicate predicate, Pageable pageable) {
        QTenant tenant = QTenant.tenant;
        QTenantPackage tenantPackage = QTenantPackage.tenantPackage;

        JPAQuery<TenantDTO> query = tenantRepository.getJPAQueryFactory()
                .select(Projections.bean(TenantDTO.class,
                        tenant.id,
                        tenant.tenantId,
                        tenant.name,
                        tenant.contactPerson,
                        tenant.contactPhone,
                        tenant.state,
                        tenant.packageId,
                        tenantPackage.name.as("packageName")
                ))
                .from(tenant)
                .leftJoin(tenantPackage).on(tenant.packageId.eq(tenantPackage.id))
                .where(predicate);

        Pageable qSortPageable = this.convertToQSortPageable(pageable);
        JPQLQuery<TenantDTO> paginatedQuery = tenantRepository.getQuerydsl().applyPagination(qSortPageable, query);
        List<TenantDTO> content = paginatedQuery.fetch();

        long total = Optional.ofNullable(tenantRepository.getJPAQueryFactory()
                .select(tenant.count())
                .from(tenant)
                .where(predicate)
                .fetchOne()).orElse(0L);

        Pageable cleanPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return new PageImpl<>(content, cleanPageable, total);
    }

    @Transactional
    public TenantCreateResultDTO createTenant(@NonNull TenantSaveDTO dto) {
        Tenant tenant = Tenant.create(dto.getName(), dto.getContactPerson(), dto.getContactPhone(), dto.getPackageId(), tenantRepository);
        tenant = tenantRepository.save(tenant);

        String rawPassword = PasswordGenerator.generateRandomPassword();

        eventPublisher.publishEvent(new TenantCreatedEvent(this, tenant, rawPassword));

        TenantCreateResultDTO result = new TenantCreateResultDTO();
        result.setId(tenant.getId());
        result.setTenantId(tenant.getTenantId());
        result.setName(tenant.getName());
        result.setContactPerson(tenant.getContactPerson());
        result.setContactPhone(tenant.getContactPhone());
        result.setState(tenant.getState());
        result.setInitialAdminPassword(rawPassword);

        return result;
    }

    @Transactional
    public TenantDTO updateTenant(@NonNull Long id,@NonNull TenantSaveDTO dto) {
        Tenant existingTenant = tenantRepository.findById(id).orElseThrow(() -> new BizException("租户不存在"));

        if (!existingTenant.getTenantId().equals(dto.getTenantId())) {
            throw new BizException("租户编码不可修改");
        }

        existingTenant.updateInfo(dto.getName(), dto.getContactPerson(), dto.getContactPhone());
        existingTenant.changePackage(dto.getPackageId());

        Tenant savedTenant = tenantRepository.save(existingTenant);
        redisTemplate.delete(Const.CacheKey.TENANT + savedTenant.getTenantId());
        return tenantMapper.toDto(savedTenant);
    }

    @Transactional
    public TenantDTO updateTenantState(@NonNull Long id, @NonNull Integer state) {
        Tenant existingTenant = tenantRepository.findById(id).orElseThrow(() -> new BizException("租户不存在"));

        if (Objects.equals(state, Const.TenantState.NORMAL)) {
            existingTenant.enable();
        } else if (Objects.equals(state, Const.TenantState.DISABLED)) {
            existingTenant.disable();
        } else {
            throw new BizException("无效的状态值: " + state);
        }

        Tenant savedTenant = tenantRepository.save(existingTenant);
        redisTemplate.delete(Const.CacheKey.TENANT + savedTenant.getTenantId());
        return tenantMapper.toDto(savedTenant);
    }

    @Transactional
    public boolean deleteByVerify(@NonNull Long id){
        TenantDTO old = getById(id);
        if(Objects.isNull(old)){
            throw new BizException("租户不存在");
        }
        if (SysUtil.isSuperTenant(old.getTenantId())) {
            throw new BizException("该租户不可删除");
        }

        // TODO其他业务数据判断
        deleteById(id);
        redisTemplate.delete(Const.CacheKey.TENANT + old.getTenantId());
        return true;
    }

    @Transactional(readOnly = true)
    public List<TenantOptionDTO> findOptions(String name){
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(name)) {
            builder.and(tenant.name.containsIgnoreCase(name));
        }
        builder.and(tenant.state.eq(Const.TenantState.NORMAL));
        List<TenantDTO> list = findAll(builder);
        return tenantMapper.dtoToOptionsDto(list);
    }
}
