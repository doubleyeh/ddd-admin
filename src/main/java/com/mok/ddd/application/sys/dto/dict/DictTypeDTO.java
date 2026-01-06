package com.mok.ddd.application.sys.dto.dict;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DictTypeDTO {
    private Long id;
    private String name;
    private String code;
    private Integer sort;
    private String remark;
    private Boolean isSystem;
    private LocalDateTime createTime;
}
