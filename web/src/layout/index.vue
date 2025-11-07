<template>
    <n-layout has-sider style="height: 100vh">
        <n-layout-sider 
            bordered 
            collapse-mode="width" 
            :collapsed-width="64" 
            :width="240" 
            :collapsed="collapsed" 
            show-trigger="arrow-circle"
            @collapse="collapsed = true"
            @expand="collapsed = false"
        >
            <div style="height: 60px; display: flex; align-items: center; justify-content: center;">
                <n-h2 style="color: #409eff; margin: 0; font-size: 20px;">
                    <span v-if="!collapsed">Admin System</span>
                    <span v-else>AS</span>
                </n-h2>
            </div>
            <n-menu
                :collapsed="collapsed"
                :options="menuOptions"
                :value="currentMenuKey"
                @update:value="handleMenuUpdate"
            />
        </n-layout-sider>
        <n-layout>
            <n-layout-header bordered style="height: 60px; padding: 0 20px; display: flex; justify-content: space-between; align-items: center;">
                <div>
                    </div>
                <n-dropdown trigger="hover" :options="userOptions" @select="handleUserSelect">
                    <n-button quaternary>
                        {{ userStore.username || 'Admin' }}
                        <template #icon><n-icon><ChevronDownOutline /></n-icon></template>
                    </n-button>
                </n-dropdown>
            </n-layout-header>
            <n-layout-content content-style="padding: 24px; background-color: #f7f9fd;" :native-scrollbar="false">
                <router-view />
            </n-layout-content>
        </n-layout>
    </n-layout>
</template>

<script setup>
import { ref, computed, h } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { NLayout, NLayoutHeader, NLayoutSider, NLayoutContent, NMenu, NDropdown, NButton, NIcon, NH2, useMessage } from 'naive-ui';
import { ChevronDownOutline, LogOutOutline, SettingsOutline } from '@vicons/ionicons5';
import { useUserStore } from '@/store/user';
import { asyncRoutes } from '@/router';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const message = useMessage();
const collapsed = ref(false);

const currentMenuKey = computed(() => route.path);

const renderIcon = (icon) => {
    return () => h(NIcon, null, { default: () => h(icon) });
};

const getIconComponent = (iconName) => {
    if (iconName.includes('setting')) return SettingsOutline;
    return SettingsOutline; 
};

const generateMenuOptions = (routes, permissions) => {
    return routes
        .filter(route => !route.meta || !route.meta.hidden)
        .map(route => {
            const requiredRoles = route.meta?.roles || [];
            const hasAccess = requiredRoles.length === 0 || requiredRoles.some(role => permissions.includes(role));
            
            if (hasAccess) {
                const iconComponent = route.meta?.icon ? getIconComponent(route.meta.icon) : null;
                
                const option = {
                    label: route.meta?.title || route.name,
                    key: route.path,
                    icon: iconComponent ? renderIcon(iconComponent) : undefined,
                    children: route.children ? generateMenuOptions(route.children, permissions) : undefined,
                };
                
                if (option.children && option.children.length === 0) {
                    return null;
                }
                return option;
            }
            return null;
        })
        .filter(Boolean);
};

const menuOptions = computed(() => {
    const filteredAsyncRoutes = generateMenuOptions(asyncRoutes, userStore.permissions);
    
    const constantMenu = generateMenuOptions(router.options.routes.filter(r => r.path === '/')[0].children || [], userStore.permissions);
    
    return [
        ...constantMenu,
        ...filteredAsyncRoutes
    ];
});

const handleMenuUpdate = (key, item) => {
    router.push(key);
};

const userOptions = [
    { label: '个人设置', key: 'profile', icon: renderIcon(SettingsOutline) },
    { label: '退出登录', key: 'logout', icon: renderIcon(LogOutOutline) }
];

const handleUserSelect = (key) => {
    if (key === 'logout') {
        userStore.logout();
        router.push('/login');
        message.success('已退出登录');
    } else if (key === 'profile') {
        
    }
};

if (userStore.roles.length === 0 && userStore.token) {
    userStore.getInfo();
}
</script>