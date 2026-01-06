package com.mok.ddd.domain.sys.model;

import com.mok.ddd.application.sys.dto.dict.DictTypeSaveDTO;
import com.mok.ddd.domain.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "sys_dict_type")
public class DictType extends BaseEntity {

    /**
     * 字典名称
     */
    @Column(nullable = false)
    private String name;

    /**
     * 字典类型编码 (如: sys_user_sex)
     */
    @Column(unique = true, nullable = false)
    private String code;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否系统内置 (true: 禁止删除/修改代码)
     */
    @Column(name = "is_system")
    private Boolean isSystem;

    public static DictType create(@NonNull DictTypeSaveDTO dto) {
        DictType dictType = new DictType();
        dictType.name = dto.getName();
        dictType.code = dto.getCode();
        dictType.sort = dto.getSort();
        dictType.remark = dto.getRemark();
        dictType.isSystem = false;
        return dictType;
    }

    public void updateInfo(@NonNull DictTypeSaveDTO dto) {
        this.name = dto.getName();
        this.sort = dto.getSort();
        this.remark = dto.getRemark();
    }
}
