<template>
  <n-config-provider :theme="isDark ? darkTheme : null">
    <n-message-provider>
      <div class="h-screen flex flex-col bg-gray-50 dark:bg-slate-900 text-gray-800 dark:text-gray-100">
        <header class="h-14 flex justify-between items-center px-6 border-b border-gray-200 dark:border-slate-700 bg-white dark:bg-slate-800 shadow-sm">
          <div class="flex items-center">
            <n-button quaternary @click="collapsed = !collapsed" class="mr-4">
              <n-icon size="20">
                <Navigation24Regular v-if="!collapsed" />
                <NavigationFilled24Regular v-else />
              </n-icon>
            </n-button>
            <div class="text-lg font-semibold">DDD Admin</div>
          </div>
          <div class="flex items-center gap-4">
            <n-switch v-model:value="isDark" size="small" />
            
            <n-dropdown :options="userMenuOptions" @select="handleUserMenuSelect">
              <div class="cursor-pointer flex items-center gap-2">
                <span class="text-gray-700 dark:text-gray-100">{{ user.username || '用户' }}</span>
                <n-button quaternary>
                  <n-icon size="18">
                    <Person24Regular />
                  </n-icon>
                </n-button>
              </div>
            </n-dropdown>
          </div>
        </header>
        
        <n-layout has-sider class="flex-1 min-h-0">
          <n-layout-sider
            bordered
            collapse-mode="width"
            :collapsed-width="64"
            :width="200"
            :collapsed="collapsed"
            show-trigger="bar"
            @collapse="collapsed = true"
            @expand="collapsed = false"
            class="!bg-white dark:!bg-slate-800"
          >
            <n-menu
              :options="menuOptions"
              v-model:value="active"
              :collapsed="collapsed"
              :collapsed-width="64"
              :collapsed-icon-size="22"
            />
          </n-layout-sider>
          
          <n-layout-content content-style="padding: 24px;" class="flex-1 overflow-auto bg-gray-50 dark:bg-slate-900">
            <router-view />
          </n-layout-content>
        </n-layout>
      </div>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { ref, watch, h } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { darkTheme, NConfigProvider, NMessageProvider, NMenu, NSwitch, NButton, NLayout, NLayoutSider, NLayoutContent, NDropdown, NIcon } from 'naive-ui'
import { 
  Person24Regular, 
  Key24Regular, 
  PersonCircle24Regular, 
  ArrowExit20Regular, 
  Home24Regular,
  Navigation24Regular,
  NavigationFilled24Regular
} from '@vicons/fluent'

const router = useRouter()
const user = useUserStore()
const isDark = ref(localStorage.getItem('theme') === 'dark')
const collapsed = ref(false)

watch(isDark, v => {
  localStorage.setItem('theme', v ? 'dark' : 'light')
  if (v) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}, { immediate: true })

const active = ref('home')

const renderIcon = (icon: any) => {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions = [
  { label: '首页', key: 'home', icon: renderIcon(Home24Regular), onClick: () => router.push('/') }
]

const userMenuOptions = [
  { label: '个人信息', key: 'profile', icon: renderIcon(PersonCircle24Regular) },
  { label: '修改密码', key: 'change-password', icon: renderIcon(Key24Regular) },
  { type: 'divider', key: 'd1' },
  { label: '退出登录', key: 'logout', icon: renderIcon(ArrowExit20Regular) }
]

function handleUserMenuSelect(key: string) {
  switch (key) {
    case 'profile':
      router.push('/profile')
      break
    case 'change-password':
      router.push('/change-password')
      break
    case 'logout':
      logout()
      break
  }
}

function logout() {
  user.logout()
  router.push('/login')
}
</script>

<style scoped>
.n-layout {
  background-color: inherit;
}
</style>