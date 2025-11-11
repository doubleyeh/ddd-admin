<template>
  <n-layout has-sider style="height: 100vh">
    <n-layout-sider
      class="layout-sider"
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="240"
      :collapsed="collapsed"
      show-trigger="arrow-circle"
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="sider-header">
        <n-h2 class="sider-title">
          <span v-if="!collapsed">DDD Admin</span>
          <span v-else>DA</span>
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
      <n-layout-header class="layout-header">
        <div class="header-left"></div>
        <div class="header-right">
          <n-dropdown
            trigger="hover"
            :options="themeOptions"
            @select="handleThemeChange"
          >
            <n-button quaternary class="header-action-btn">
              <n-icon size="18"><ColorPaletteOutline /></n-icon>
              <span class="btn-text">主题</span>
            </n-button>
          </n-dropdown>

          <n-dropdown
            trigger="hover"
            :options="userOptions"
            @select="handleUserSelect"
          >
            <n-button quaternary class="header-action-btn">
              <n-icon size="18"><PersonCircleOutline /></n-icon>
              <span class="btn-text">{{ userStore.username || 'Admin' }}</span>
              <n-icon size="16"><ChevronDownOutline /></n-icon>
            </n-button>
          </n-dropdown>
        </div>
      </n-layout-header>

      <n-layout-content class="layout-content" :native-scrollbar="false">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, computed, h, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NLayout, NLayoutHeader, NLayoutSider, NLayoutContent, NMenu,
  NDropdown, NButton, NIcon, NH2, useMessage
} from 'naive-ui'
import {
  ChevronDownOutline,
  LogOutOutline,
  SettingsOutline,
  HomeOutline,
  ColorPaletteOutline,
  PersonCircleOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/store/user'
import { useThemeStore } from '@/store/theme'
import { asyncRoutes } from '@/router'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()
const message = useMessage()
const collapsed = ref(false)

const currentMenuKey = computed(() => route.path)

const renderIcon = (icon) => () => h(NIcon, null, { default: () => h(icon) })

const getIconComponent = (name) => {
  if (name.includes('home')) return HomeOutline
  if (name.includes('setting')) return SettingsOutline
  return SettingsOutline
}

const getConstantRoutesChildren = () => {
  const root = router.options.routes.find(r => r.path === '/')
  return root ? root.children || [] : []
}

const filterAsyncRoutes = (routes, permissions) => {
  const res = []
  routes.forEach(route => {
    const tmp = { ...route }
    const required = tmp.meta?.roles
    if (required && required.length > 0) {
      if (required.some(role => permissions.includes(role))) {
        if (tmp.children) tmp.children = filterAsyncRoutes(tmp.children, permissions)
        res.push(tmp)
      }
    } else {
      if (tmp.children) tmp.children = filterAsyncRoutes(tmp.children, permissions)
      res.push(tmp)
    }
  })
  return res
}

const generateMenuOptions = (routes, permissions) =>
  routes
    .filter(r => !r.meta || !r.meta.hidden)
    .map(r => {
      const allow = !r.meta?.roles?.length || r.meta.roles.some(role => permissions.includes(role))
      if (!allow) return null
      const iconComp = r.meta?.icon ? getIconComponent(r.meta.icon) : null
      return {
        label: r.meta?.title || r.name,
        key: r.path.startsWith('/') ? r.path : `/${r.path}`,
        icon: iconComp ? renderIcon(iconComp) : undefined,
        children: r.children ? generateMenuOptions(r.children, permissions) : undefined
      }
    })
    .filter(Boolean)

const menuOptions = computed(() => {
  const perms = userStore.permissions || []
  const constRoutes = getConstantRoutesChildren()
  const asyncFiltered = filterAsyncRoutes(asyncRoutes, perms)
  const menu = [...constRoutes, ...asyncFiltered]
  return generateMenuOptions(menu, perms)
})

const handleMenuUpdate = (key) => router.push(key)

const userOptions = [
  { label: '个人设置', key: 'profile', icon: renderIcon(SettingsOutline) },
  { label: '退出登录', key: 'logout', icon: renderIcon(LogOutOutline) }
]

const themeOptions = [
  { label: '蓝色主题', key: 'blue' },
  { label: '浅色主题', key: 'light' },
  { label: '深色主题', key: 'dark' }
]

const handleUserSelect = (key) => {
  if (key === 'logout') {
    userStore.logout()
    router.push('/login')
    message.success('已退出登录')
  }
}

const handleThemeChange = (theme) => themeStore.setTheme(theme)

watch(() => themeStore.theme, (theme) => {
  document.documentElement.setAttribute('data-theme', theme)
}, { immediate: true })

if (userStore.token && userStore.roles?.length === 0) userStore.getInfo()
</script>

<style scoped>
.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--layout-bg);
  color: var(--layout-text);
  height: 70px;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--layout-text) !important;
  font-weight: 500;
}

.sider-header {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--layout-bg);
  color: var(--layout-text);
}

.sider-title {
  color: var(--layout-text);
  font-weight: 600;
}

.layout-sider {
  background-color: var(--layout-bg);
  color: var(--layout-text);
}

.layout-content {
  background-color: var(--content-bg);
  padding: 24px;
}

/* 菜单样式 */
:deep(.n-menu) {
  background-color: var(--layout-bg) !important;
}

:deep(.n-menu .n-menu-item-content__header),
:deep(.n-menu .n-menu-item-content__icon) {
  color: var(--layout-text) !important;
}

:deep(.n-menu-item-content:hover) {
  background-color: var(--menu-hover) !important;
}

:deep(.n-menu-item-content.n-menu-item-content--selected) {
  background-color: var(--menu-selected) !important;
}

:deep(.n-submenu-children) {
  background-color: var(--submenu-bg) !important;
}
</style>
