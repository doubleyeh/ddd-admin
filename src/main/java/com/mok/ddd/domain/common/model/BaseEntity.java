package com.mok.ddd.domain.common.model;

import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import com.mok.ddd.infrastructure.util.SnowFlakeIdGenerator;
import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Id
    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    protected BaseEntity() {
    }

    @PrePersist
    protected void prePersist() {
        if (this.id == null) {
            this.id = SnowFlakeIdGenerator.nextId();
        }
        String username = TenantContextHolder.getUsername();
        if (username != null) {
            if(this.createBy == null) {
                this.createBy = username;
            }
            if(this.updateBy == null) {
                this.updateBy = username;
            }
        }
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        this.updateTime = LocalDateTime.now();
        String username = TenantContextHolder.getUsername();
        if (username != null && this.updateBy == null) {
            this.updateBy = username;
        }
    }

    protected void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    protected void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }
}
