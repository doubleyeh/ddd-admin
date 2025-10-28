package com.mok.ddd.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QTenantBaseEntity is a Querydsl query type for TenantBaseEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QTenantBaseEntity extends EntityPathBase<TenantBaseEntity> {

    private static final long serialVersionUID = -1883219109L;

    public static final QTenantBaseEntity tenantBaseEntity = new QTenantBaseEntity("tenantBaseEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final StringPath createBy = _super.createBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createTime = _super.createTime;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath tenantId = createString("tenantId");

    //inherited
    public final StringPath updateBy = _super.updateBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updateTime = _super.updateTime;

    public QTenantBaseEntity(String variable) {
        super(TenantBaseEntity.class, forVariable(variable));
    }

    public QTenantBaseEntity(Path<? extends TenantBaseEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTenantBaseEntity(PathMetadata metadata) {
        super(TenantBaseEntity.class, metadata);
    }

}

