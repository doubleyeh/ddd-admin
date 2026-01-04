package com.mok.ddd.application.sys.dto.tenant;

import com.mok.ddd.common.Const;
import lombok.Data;

import java.util.Objects;

@Data
public class TenantDTO {
    private Long id;
    private String tenantId;
    private String name;
    private String contactPerson;
    private String contactPhone;
    private Integer state;
    private Long packageId;
    private String packageName;

    public boolean isEnabled(){
        return Objects.equals(Const.TenantState.NORMAL, state);
    }
}
