import { createRouter, createWebHashHistory } from 'vue-router';
import Layout from '@/layout/index.vue'; 

export const constantRoutes = [
    {
        path: '/login',
        component: () => import('@/views/login/index.vue'),
        meta: { hidden: true },
    },
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                component: () => import('@/views/dashboard/index.vue'),
                name: 'Dashboard',
                meta: { title: '首页', icon: 'i-ant-design:home-filled' },
            },
        ],
    },
];

export const asyncRoutes = [
    // {
    //     path: '/system',
    //     component: Layout,
    //     redirect: '/system/user',
    //     name: 'System',
    //     meta: { 
    //         title: '系统管理', 
    //         icon: 'i-ant-design:setting-filled',
    //     },
    //     children: [
    //         {
    //             path: 'tenant',
    //             component: () => import('@/views/system/tenant/index.vue'),
    //             name: 'TenantManagement',
    //             meta: { 
    //                 title: '租户管理', 
    //                 roles: ['tenant:list'] 
    //             }
    //         },
    //         {
    //             path: 'user',
    //             component: () => import('@/views/system/user/index.vue'),
    //             name: 'UserManagement',
    //             meta: { 
    //                 title: '用户管理', 
    //                 roles: ['user:list'] 
    //             }
    //         },
    //         {
    //             path: 'role',
    //             component: () => import('@/views/system/role/index.vue'),
    //             name: 'RoleManagement',
    //             meta: { 
    //                 title: '角色管理', 
    //                 roles: ['role:list'] 
    //             }
    //         }
    //     ]
    // },
    { 
        path: '/:pathMatch(.*)*', 
        name: 'NotFound', 
        component: () => import('@/views/error/404.vue'), 
        meta: { hidden: true } 
    }
];


const router = createRouter({
    history: createWebHashHistory(),
    routes: constantRoutes, 
    scrollBehavior: () => ({ left: 0, top: 0 }),
});

export default router;