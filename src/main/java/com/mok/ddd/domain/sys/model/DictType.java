package com.mok.ddd.domain.sys.model;

import com.mok.ddd.domain.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
