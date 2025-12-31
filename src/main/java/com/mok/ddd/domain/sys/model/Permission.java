package com.mok.ddd.domain.sys.model;

import com.mok.ddd.domain.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_permission")
@Getter
@Setter
public class Permission extends BaseEntity {
    private String name;
    private String code;
    /**
     * 接口路径 (URL Pattern)
     * * 说明：对应后端 API 的 RequestMapping 路径。
     * 作用：用于【动态权限拦截】。
     * 示例："/api/users/**" 或 "/api/roles/{id}"。
     * 逻辑：当用户发起请求时，系统会拿当前请求的真实 URL 去数据库比对，看该用户是否有权访问此路径。
     */
    private String url;

    /**
     * HTTP 请求方法 (HTTP Method)
     * * 说明：对应 GET, POST, PUT, DELETE, ALL 等。
     * 作用：精确控制操作权限。
     * 示例：
     * - "GET"：仅允许查询，不能修改。
     * - "POST"：允许新增。
     * - "ALL" 或 null：不限制方法，匹配该路径下的所有操作。
     * 逻辑：配合 url 字段使用，防止用户“钻空子”。例如：用户有 GET 权限查看列表，但如果没有 POST 权限，就无法提交新增。
     */
    private String method;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;
}