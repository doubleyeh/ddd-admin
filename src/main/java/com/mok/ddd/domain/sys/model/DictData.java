package com.mok.ddd.domain.sys.model;

import com.mok.ddd.application.sys.dto.dict.DictDataSaveDTO;
import com.mok.ddd.domain.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "sys_dict_data", indexes = {
        @Index(name = "idx_dict_type", columnList = "type_code")
})
public class DictData extends BaseEntity {

    /**
     * 字典类型编码
     */
    @Column(name = "type_code", nullable = false)
    private String typeCode;

    /**
     * 字典标签 (如: 男)
     */
    @Column(nullable = false)
    private String label;

    /**
     * 字典键值 (如: 0)
     */
    @Column(nullable = false)
    private String value;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 样式属性 (如: primary, success, danger)
     */
    private String cssClass;

    /**
     * 表格回显样式 (如: default, primary)
     */
    private String listClass;

    /**
* 是否默认
     */
    @Column(name = "is_default")
    private Boolean isDefault;

    /**
     * 备注
     */
    private String remark;

    public static DictData create(@NonNull DictDataSaveDTO dto) {
        DictData dictData = new DictData();
        dictData.typeCode = dto.getTypeCode();
        dictData.label = dto.getLabel();
        dictData.value = dto.getValue();
        dictData.sort = dto.getSort();
        dictData.cssClass = dto.getCssClass();
        dictData.listClass = dto.getListClass();
        dictData.isDefault = dto.getIsDefault();
        dictData.remark = dto.getRemark();
        return dictData;
    }

    public void updateInfo(@NonNull DictDataSaveDTO dto) {
        this.label = dto.getLabel();
        this.value = dto.getValue();
        this.sort = dto.getSort();
        this.cssClass = dto.getCssClass();
        this.listClass = dto.getListClass();
        this.isDefault = dto.getIsDefault();
        this.remark = dto.getRemark();
    }
}
