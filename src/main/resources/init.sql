DELETE FROM sys_role_permission;
DELETE FROM sys_menu;
DELETE FROM sys_permission;

-- 菜单ID 3位数
INSERT INTO sys_menu (id, parent_id, name, path, component, icon, sort, is_hidden, create_time, update_time)
VALUES
(100, NULL, '系统管理', '/system', 'Layout', 'SettingsSharp', 10, FALSE, NOW(), NOW()),
(101, 100, '租户管理', '/system/tenant', 'system/tenant', 'PeopleOutline', 11, FALSE, NOW(), NOW()),
(102, 100, '用户管理', '/system/user', 'system/user', 'PersonOutline', 21, FALSE, NOW(), NOW()),
(103, 100, '角色管理', '/system/role', 'system/role', 'ShieldOutline', 31, FALSE, NOW(), NOW()),
(104, 100, '菜单管理', '/system/menu', 'system/menu', 'MenuOutline', 41, FALSE, NOW(), NOW()),
(105, 100, '套餐管理', '/system/package', 'system/tenant-package', 'GiftOutline', 51, FALSE, NOW(), NOW()),
(106, 100, '字典管理', '/system/dict', 'system/dict', 'BookOutline', 61, FALSE, NOW(), NOW()),
(200, NULL, '日志管理', '/log', 'Layout', 'ReceiptLongSharp', 60, FALSE, NOW(), NOW()),
(201, 200, '登录日志', '/log/login', 'log/login', 'LogInOutline', 61, FALSE, NOW(), NOW()),
(202, 200, '操作日志', '/log/operlog', 'log/operlog', 'DocumentTextOutline', 62, FALSE, NOW(), NOW()),
(999, 100, '在线用户', '/system/online-user', 'system/online-user', 'GlobeOutline', 99, FALSE, NOW(), NOW());

-- 权限ID 5位数
INSERT INTO sys_permission (id, menu_id, name, code, url, method, description, create_time, update_time)
VALUES
(10201, 102, '用户查询', 'user:list', NULL, NULL, '获取用户分页列表、详情', NOW(), NOW()),
(10202, 102, '用户创建', 'user:create', NULL, NULL, '新增用户', NOW(), NOW()),
(10203, 102, '用户修改', 'user:update', NULL, NULL, '修改用户信息、重置密码', NOW(), NOW()),
(10204, 102, '用户删除', 'user:delete', NULL, NULL, '删除用户', NOW(), NOW()),
(10301, 103, '角色查询', 'role:list', NULL, NULL, '获取角色分页列表、详情及权限菜单', NOW(), NOW()),
(10302, 103, '角色创建', 'role:create', NULL, NULL, '新增角色', NOW(), NOW()),
(10303, 103, '角色修改', 'role:update', NULL, NULL, '修改角色信息', NOW(), NOW()),
(10304, 103, '角色删除', 'role:delete', NULL, NULL, '删除角色', NOW(), NOW()),
(10101, 101, '租户查询', 'tenant:list', NULL, NULL, '获取租户分页列表、详情', NOW(), NOW()),
(10102, 101, '租户创建', 'tenant:create', NULL, NULL, '新增租户', NOW(), NOW()),
(10103, 101, '租户修改', 'tenant:update', NULL, NULL, '修改租户信息', NOW(), NOW()),
(10104, 101, '租户删除', 'tenant:delete', NULL, NULL, '删除租户', NOW(), NOW()),
(10401, 104, '菜单查询', 'menu:list', NULL, NULL, '获取菜单树、详情', NOW(), NOW()),
(10402, 104, '菜单创建', 'menu:create', NULL, NULL, '新增菜单', NOW(), NOW()),
(10403, 104, '菜单修改', 'menu:update', NULL, NULL, '修改菜单信息', NOW(), NOW()),
(10404, 104, '菜单删除', 'menu:delete', NULL, NULL, '删除菜单', NOW(), NOW()),
(10501, 105, '套餐查询', 'tenantPackage:list', NULL, NULL, '查询套餐分页及详情', NOW(), NOW()),
(10502, 105, '套餐创建', 'tenantPackage:create', NULL, NULL, '新增套餐', NOW(), NOW()),
(10503, 105, '套餐修改', 'tenantPackage:update', NULL, NULL, '修改套餐信息', NOW(), NOW()),
(10504, 105, '套餐删除', 'tenantPackage:delete', NULL, NULL, '删除套餐', NOW(), NOW()),
(10601, 106, '字典查询', 'dict:list', NULL, NULL, '查询字典类型及数据', NOW(), NOW()),
(10602, 106, '字典创建', 'dict:create', NULL, NULL, '新增字典类型或数据', NOW(), NOW()),
(10603, 106, '字典修改', 'dict:update', NULL, NULL, '修改字典类型或数据', NOW(), NOW()),
(10604, 106, '字典删除', 'dict:delete', NULL, NULL, '删除字典类型或数据', NOW(), NOW()),
(20101, 201, '登录日志查询', 'log:login:list', NULL, NULL, '查询登录日志分页列表', NOW(), NOW()),
(20201, 202, '操作日志查询', 'log:oper:list', NULL, NULL, '查询操作日志分页列表', NOW(), NOW()),
(99901, 999, '在线用户查询', 'admin:online-user', NULL, NULL, '查看在线登录用户列表', NOW(), NOW()),
(99902, 999, '在线用户强退', 'admin:online-user:kickout', NULL, NULL, '强制下线指定用户', NOW(), NOW());

INSERT INTO sys_dict_type (id, create_by, create_time, update_by, update_time, name, code, sort, remark, is_system)
VALUES (100, 'root', NOW(), 'root', NOW(), '用户状态', 'sys_user_status', 1, '用户账号状态列表', TRUE);
INSERT INTO sys_dict_data (id, create_by, create_time, update_by, update_time, type_code, label, value, sort, list_class, is_default, remark)
VALUES (1001, 'root', NOW(), 'root', NOW(), 'sys_user_status', '正常', '1', 1, 'success', TRUE, '账号正常使用');
INSERT INTO sys_dict_data (id, create_by, create_time, update_by, update_time, type_code, label, value, sort, list_class, is_default, remark)
VALUES (1002, 'root', NOW(), 'root', NOW(), 'sys_user_status', '禁用', '0', 2, 'danger', FALSE, '账号已被封禁');

INSERT INTO sys_dict_type (id, create_by, create_time, update_by, update_time, name, code, sort, remark, is_system)
VALUES (101, 'root', NOW(), 'root', NOW(), '角色状态', 'sys_role_status', 2, '角色状态列表', TRUE);
INSERT INTO sys_dict_data (id, create_by, create_time, update_by, update_time, type_code, label, value, sort, list_class, is_default, remark)
VALUES (1003, 'root', NOW(), 'root', NOW(), 'sys_role_status', '正常', '1', 1, 'success', TRUE, '角色正常使用');
INSERT INTO sys_dict_data (id, create_by, create_time, update_by, update_time, type_code, label, value, sort, list_class, is_default, remark)
VALUES (1004, 'root', NOW(), 'root', NOW(), 'sys_role_status', '禁用', '0', 2, 'danger', FALSE, '角色已被禁用');

INSERT INTO sys_dict_type (id, create_by, create_time, update_by, update_time, name, code, sort, remark, is_system)
VALUES (102, 'root', NOW(), 'root', NOW(), '租户状态', 'sys_tenant_status', 3, '租户状态列表', TRUE);
INSERT INTO sys_dict_data (id, create_by, create_time, update_by, update_time, type_code, label, value, sort, list_class, is_default, remark)
VALUES (1005, 'root', NOW(), 'root', NOW(), 'sys_tenant_status', '正常', '1', 1, 'success', TRUE, '租户正常使用');
INSERT INTO sys_dict_data (id, create_by, create_time, update_by, update_time, type_code, label, value, sort, list_class, is_default, remark)
VALUES (1006, 'root', NOW(), 'root', NOW(), 'sys_tenant_status', '禁用', '0', 2, 'danger', FALSE, '租户已被禁用');

INSERT INTO sys_dict_type (id, create_by, create_time, update_by, update_time, name, code, sort, remark, is_system)
VALUES (103, 'root', NOW(), 'root', NOW(), '套餐状态', 'sys_tenant_package_status', 4, '套餐状态列表', TRUE);
INSERT INTO sys_dict_data (id, create_by, create_time, update_by, update_time, type_code, label, value, sort, list_class, is_default, remark)
VALUES (1007, 'root', NOW(), 'root', NOW(), 'sys_tenant_package_status', '正常', '1', 1, 'success', TRUE, '套餐正常使用');
INSERT INTO sys_dict_data (id, create_by, create_time, update_by, update_time, type_code, label, value, sort, list_class, is_default, remark)
VALUES (1008, 'root', NOW(), 'root', NOW(), 'sys_tenant_package_status', '禁用', '0', 2, 'danger', FALSE, '套餐已被禁用');


INSERT INTO `sys_tenant` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `contact_person`, `contact_phone`, `state`, `name`, `tenant_id`) VALUES (1, 'root', '2025-11-29 16:45:25.000000', 'root', '2025-11-29 16:45:30.000000', 'root', '1', 1, 'root', '000000');

INSERT INTO `sys_user` (`id`, `create_by`, `create_time`, `update_by`, `update_time`, `tenant_id`, `nickname`, `password`, `state`, `username`, `is_tenant_admin`) VALUES (1, 'root', '2025-11-30 07:52:38.000000', 'root', '2025-11-30 07:52:44.000000', '000000', 'Root', '$2a$10$3UzFSpn1EaTqUhU5tN/n/e3lZQUkXf0zCIAbefLrbuzNIEXKmECUG', 1, 'root', 0);
