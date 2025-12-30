DELETE FROM sys_role_permission;
DELETE FROM sys_menu;
DELETE FROM sys_permission;

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, sort, is_hidden, create_time, update_time)
VALUES
(100, NULL, '系统管理', '/system', 'Layout', 'SettingsSharp', 10, FALSE, NOW(), NOW()),
(101, 100, '租户管理', '/system/tenant', 'system/tenant', 'PeopleOutline', 11, FALSE, NOW(), NOW()),
(102, 100, '用户管理', '/system/user', 'system/user', 'PersonOutline', 21, FALSE, NOW(), NOW()),
(103, 100, '角色管理', '/system/role', 'system/role', 'ShieldOutline', 31, FALSE, NOW(), NOW()),
(104, 100, '菜单管理', '/system/menu', 'system/menu', 'MenuOutline', 41, FALSE, NOW(), NOW()),
(999, 100, '在线用户', '/system/online-user', 'system/online-user', 'GlobeOutline', 99, FALSE, NOW(), NOW());

INSERT INTO sys_permission (id, menu_id, name, code, url, method, description, create_time, update_time)
VALUES
(1, 102, '用户查询', 'user:list', NULL, NULL, '获取用户分页列表、详情', NOW(), NOW()),
(2, 102, '用户创建', 'user:create', NULL, NULL, '新增用户', NOW(), NOW()),
(3, 102, '用户修改', 'user:update', NULL, NULL, '修改用户信息、重置密码', NOW(), NOW()),
(4, 102, '用户删除', 'user:delete', NULL, NULL, '删除用户', NOW(), NOW()),
(5, 103, '角色查询', 'role:list', NULL, NULL, '获取角色分页列表、详情及权限菜单', NOW(), NOW()),
(6, 103, '角色创建', 'role:create', NULL, NULL, '新增角色', NOW(), NOW()),
(7, 103, '角色修改', 'role:update', NULL, NULL, '修改角色信息', NOW(), NOW()),
(8, 103, '角色删除', 'role:delete', NULL, NULL, '删除角色', NOW(), NOW()),
(9, 101, '租户查询', 'tenant:list', NULL, NULL, '获取租户分页列表、详情', NOW(), NOW()),
(10, 101, '租户创建', 'tenant:create', NULL, NULL, '新增租户', NOW(), NOW()),
(11, 101, '租户修改', 'tenant:update', NULL, NULL, '修改租户信息', NOW(), NOW()),
(12, 101, '租户删除', 'tenant:delete', NULL, NULL, '删除租户', NOW(), NOW()),
(13, 104, '菜单查询', 'menu:list', NULL, NULL, '获取菜单树、详情', NOW(), NOW()),
(14, 104, '菜单创建', 'menu:create', NULL, NULL, '新增菜单', NOW(), NOW()),
(15, 104, '菜单修改', 'menu:update', NULL, NULL, '修改菜单信息', NOW(), NOW()),
(16, 104, '菜单删除', 'menu:delete', NULL, NULL, '删除菜单', NOW(), NOW()),
(998, 999, '在线用户查询', 'admin:online-user', NULL, NULL, '查看在线登录用户列表', NOW(), NOW()),
(999, 999, '在线用户强退', 'admin:online-user:kickout', NULL, NULL, '强制下线指定用户', NOW(), NOW());

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, sort, is_hidden, create_time, update_time)
VALUES (105, 100, '套餐管理', '/system/package', 'system/tenant-package', 'GiftOutline', 51, FALSE, NOW(), NOW());

INSERT INTO sys_permission (id, menu_id, name, code, url, method, description, create_time, update_time)
VALUES
(17, 105, '套餐查询', 'tenantPackage:list', NULL, NULL, '查询套餐分页及详情', NOW(), NOW()),
(18, 105, '套餐创建', 'tenantPackage:create', NULL, NULL, '新增套餐', NOW(), NOW()),
(19, 105, '套餐修改', 'tenantPackage:update', NULL, NULL, '修改套餐信息', NOW(), NOW()),
(20, 105, '套餐删除', 'tenantPackage:delete', NULL, NULL, '删除套餐', NOW(), NOW());

INSERT INTO `sys_tenant` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `contact_person`, `contact_phone`, `enabled`, `name`, `tenant_id`) VALUES (1, 'root', '2025-11-29 16:45:25.000000', 'root', '2025-11-29 16:45:30.000000', 'root', '1', b'1', 'root', '000000');

INSERT INTO `sys_user` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `tenant_id`, `nickname`, `password`, `state`, `username`) VALUES (1, 'root', '2025-11-30 07:52:38.000000', 'root', '2025-11-30 07:52:44.000000', '000000', 'Root', '$2a$10$3UzFSpn1EaTqUhU5tN/n/e3lZQUkXf0zCIAbefLrbuzNIEXKmECUG', 1, 'root');

