DELETE FROM sys_role_permission;
DELETE FROM sys_menu;
DELETE FROM sys_permission;

INSERT IGNORE INTO sys_permission (id, name, code, url, method, description, create_time, update_time)
VALUES
(1, '用户查询', 'user:list', NULL, NULL, '获取用户分页列表、详情', NOW(), NOW()),
(2, '用户创建', 'user:create', NULL, NULL, '新增用户', NOW(), NOW()),
(3, '用户修改', 'user:update', NULL, NULL, '修改用户信息、重置密码', NOW(), NOW()),
(4, '用户删除', 'user:delete', NULL, NULL, '删除用户', NOW(), NOW()),
(5, '角色查询', 'role:list', NULL, NULL, '获取角色分页列表、详情及权限菜单', NOW(), NOW()),
(6, '角色创建', 'role:create', NULL, NULL, '新增角色', NOW(), NOW()),
(7, '角色修改', 'role:update', NULL, NULL, '修改角色信息', NOW(), NOW()),
(8, '角色删除', 'role:delete', NULL, NULL, '删除角色', NOW(), NOW()),
(9, '租户查询', 'tenant:list', NULL, NULL, '获取租户分页列表、详情', NOW(), NOW()),
(10, '租户创建', 'tenant:create', NULL, NULL, '新增租户', NOW(), NOW()),
(11, '租户修改', 'tenant:update', NULL, NULL, '修改租户信息', NOW(), NOW()),
(12, '租户删除', 'tenant:delete', NULL, NULL, '删除租户', NOW(), NOW());

INSERT IGNORE INTO sys_menu (id, parent_id, name, path, component, icon, sort, is_hidden, create_time, update_time)
VALUES
(100, NULL, '系统管理', '/system', 'Layout', 'SettingsSharp', 10, FALSE, NOW(), NOW()),
(1001, 100, '租户管理', '/system/tenant', 'system/tenant', 'PeopleOutline', 11, FALSE, NOW(), NOW()),
(1002, 100, '用户管理', '/system/user', 'system/user', 'PersonOutline', 21, FALSE, NOW(), NOW()),
(1003, 100, '角色管理', '/system/role', 'system/role', 'ShieldOutline', 31, FALSE, NOW(), NOW()),

(10001, 1001, '查询/详情', NULL, NULL, NULL, 1, FALSE, NOW(), NOW()),
(10002, 1001, '新增租户', NULL, NULL, NULL, 2, FALSE, NOW(), NOW()),
(10003, 1001, '修改租户', NULL, NULL, NULL, 3, FALSE, NOW(), NOW()),
(10004, 1001, '删除租户', NULL, NULL, NULL, 4, FALSE, NOW(), NOW()),

(10005, 1002, '查询/详情', NULL, NULL, NULL, 1, FALSE, NOW(), NOW()),
(10006, 1002, '新增用户', NULL, NULL, NULL, 2, FALSE, NOW(), NOW()),
(10007, 1002, '修改用户', NULL, NULL, NULL, 3, FALSE, NOW(), NOW()),
(10008, 1002, '重置密码', NULL, NULL, NULL, 4, FALSE, NOW(), NOW()),
(10009, 1002, '删除用户', NULL, NULL, NULL, 5, FALSE, NOW(), NOW()),

(10010, 1003, '查询/详情', NULL, NULL, NULL, 1, FALSE, NOW(), NOW()),
(10011, 1003, '新增角色', NULL, NULL, NULL, 2, FALSE, NOW(), NOW()),
(10012, 1003, '修改角色', NULL, NULL, NULL, 3, FALSE, NOW(), NOW()),
(10013, 1003, '删除角色', NULL, NULL, NULL, 4, FALSE, NOW(), NOW());


INSERT INTO `sys_tenant` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `contact_person`, `contact_phone`, `enabled`, `name`, `tenant_id`) VALUES (1, 'root', '2025-11-29 16:45:25.000000', 'root', '2025-11-29 16:45:30.000000', 'root', '1', b'1', 'root', '000000');
