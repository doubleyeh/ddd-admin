package com.mok.ddd.application.sys.dto.dict;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictDataSaveDTO {
    private Long id;

    @NotBlank(message = "字典类型编码不能为空")
    private String typeCode;

    @NotBlank(message = "字典标签不能为空")
    private String label;

    @NotBlank(message = "字典键值不能为空")
    private String value;

    private Integer sort;
    private String cssClass;
    private String listClass;
    private Boolean isDefault;
    private String remark;
}
