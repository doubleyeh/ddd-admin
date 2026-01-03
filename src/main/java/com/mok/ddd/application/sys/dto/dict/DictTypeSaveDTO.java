package com.mok.ddd.application.sys.dto.dict;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictTypeSaveDTO {
    private Long id;

    @NotBlank(message = "字典名称不能为空")
    private String name;

    @NotBlank(message = "字典类型编码不能为空")
    private String code;

    private Integer sort;
    private String remark;
}
