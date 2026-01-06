package com.mok.ddd.application.sys.dto.dict;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DictDataDTO {
    private Long id;
    private String typeCode;
    private String label;
    private String value;
    private Integer sort;
    private String cssClass;
    private String listClass;
    private Boolean isDefault;
    private String remark;
    private LocalDateTime createTime;
}
