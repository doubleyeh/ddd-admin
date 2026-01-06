package com.mok.ddd.application.sys.dto.log;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperLogDTO {
    private Long id;
    private String title;
    private Integer businessType;
    private String method;
    private String requestMethod;
    private String operName;
    private String operUrl;
    private String operIp;
    private String operParam;
    private String jsonResult;
    private Integer status;
    private String errorMsg;
    private Long costTime;
    private LocalDateTime createTime;
    private String createBy;
}
