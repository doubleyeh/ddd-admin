<template>
  <div class="h-screen flex flex-col text-gray-800 dark:text-gray-100">
    
    <n-layout-header bordered class="h-14 flex justify-between items-center px-6 shadow-sm">
      <div class="flex items-center">
        <div class="text-lg font-semibold">DDD Admin</div>
        
        <n-button quaternary @click="collapsed = !collapsed" class="mr-4">
          <n-icon :component="collapsed ? ChevronForwardCircleOutline : ChevronBackCircleOutline" />
        </n-button>
      </div>
      <div class="flex items-center gap-4">
        <n-switch :value="themeStore.isDark" @update:value="themeStore.toggleDark" size="small" />
        
        <n-dropdown :options="userMenuOptions" @select="handleUserMenuSelect">
          <div class="cursor-pointer flex items-center gap-2">
            <span>{{ user.username || '用户' }}</span>
            <n-button quaternary>
              👤
            </n-button>
          </div>
        </n-dropdown>
      </div>
    </n-layout-header>
    
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
      >
        <n-menu
          :options="menuOptions"
          v-model:value="active"
          :collapsed="collapsed"
          :collapsed-width="64"
          :collapsed-icon-size="22"
        />
      </n-layout-sider>
      
      <n-layout-content content-style="padding: 24px;" class="flex-1 overflow-auto">
        <router-view />
      </n-layout-content>
    </n-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { useThemeStore } from '../store/theme'
import { NMenu, NSwitch, NButton, NLayout, NLayoutHeader, NLayoutSider, NLayoutContent, NDropdown, NIcon } from 'naive-ui'
import { ChevronBackCircleOutline, ChevronForwardCircleOutline } from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const user = useUserStore()
const themeStore = useThemeStore()
const collapsed = ref(false)

const active = ref(route.name as string || 'Home')

watch(() => route.name, (name) => {
  if (name) {
    active.value = name as string
  }
}, { immediate: true })

const menuOptions = [
  { label: '首页', key: 'Home', icon: () => '🏠', onClick: () => router.push({ name: 'Home' }) }
]

const userMenuOptions = [
  { label: '个人信息', key: 'Profile' },
  { label: '修改密码', key: 'ChangePassword' },
  { type: 'divider', key: 'd1' },
  { label: '退出登录', key: 'logout' }
]

function handleUserMenuSelect(key: string) {
  switch (key) {
    case 'Profile':
      router.push({ name: 'Profile' })
      break
    case 'ChangePassword':
      router.push({ name: 'ChangePassword' })
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