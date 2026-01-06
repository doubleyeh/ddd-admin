package com.mok.ddd.domain.sys.model;

import com.mok.ddd.domain.common.model.TenantBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "sys_oper_log")
public class OperLog extends TenantBaseEntity {

    /**
     * 模块标题
     */
    private String title;

    /**
     * 业务类型（0其它 1新增 2修改 3删除）
     */
    private Integer businessType;

    /**
     * 方法名称
     */
    private String method;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 操作人员
     */
    private String operName;

    /**
     * 请求URL
     */
    private String operUrl;

    /**
     * 主机地址
     */
    private String operIp;

    /**
     * 请求参数
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String operParam;

    /**
     * 返回参数
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String jsonResult;

    /**
     * 操作状态（1正常 0异常）
     */
    private Integer status;

    /**
     * 错误消息
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String errorMsg;

    /**
     * 操作耗时(毫秒)
     */
    private Long costTime;

    public static OperLog create(String title, Integer businessType, String method, String requestMethod, String operName, String operUrl, String operIp, String operParam, String jsonResult, Integer status, String errorMsg, Long costTime) {
        OperLog log = new OperLog();
        log.title = title;
        log.businessType = businessType;
        log.method = method;
        log.requestMethod = requestMethod;
        log.operName = operName;
        log.operUrl = operUrl;
        log.operIp = operIp;
        log.operParam = operParam;
        log.jsonResult = jsonResult;
        log.status = status;
        log.errorMsg = errorMsg;
        log.costTime = costTime;
        return log;
    }

    public void assignTenant(String tenantId) {
        if (this.getTenantId() == null) {
            this.setTenantId(tenantId);
        }
    }

    public void assignCreator(String username) {
        if (this.getCreateBy() == null) {
            this.setCreateBy(username);
        }
        if (this.getUpdateBy() == null) {
            this.setUpdateBy(username);
        }
    }
}
