<template>
  <n-config-provider :theme="isDark ? darkTheme : null" :theme-overrides="themeOverrides">
    <n-message-provider>
      <div class="h-screen flex flex-col text-gray-800 dark:text-gray-100">
        
        <n-layout-header bordered class="h-14 flex justify-between items-center px-6 shadow-sm">
          <div class="flex items-center">
            <div class="text-lg font-semibold">DDD Admin</div>
            
            <n-button quaternary @click="collapsed = !collapsed" class="mr-4">
              <n-icon :component="collapsed ? ChevronForwardCircleOutline : ChevronBackCircleOutline" />
            </n-button>
          </div>
          <div class="flex items-center gap-4">
            <n-switch v-model:value="isDark" size="small" />
            
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
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { darkTheme, NConfigProvider, NMessageProvider, NMenu, NSwitch, NButton, NLayout, NLayoutHeader, NLayoutSider, NLayoutContent, NDropdown, NIcon } from 'naive-ui'
import { ChevronBackCircleOutline, ChevronForwardCircleOutline } from '@vicons/ionicons5'

const router = useRouter()
const user = useUserStore()
const isDark = ref(localStorage.getItem('theme') === 'dark')
const collapsed = ref(false)

const themeOverrides = computed(() => {
  if (isDark.value) {
    return {
      Layout: {
        color: 'rgb(24, 24, 28)', 
        headerBorderColor: 'rgb(60, 60, 60)', 
        siderBorderColor: 'rgb(60, 60, 60)',
      },
      Menu: {
        itemColorActive: 'rgba(255, 255, 255, 0.1)',
      }
    };
  }
  return null;
});

watch(isDark, v => {
  localStorage.setItem('theme', v ? 'dark' : 'light')
  if (v) {
    document.documentElement.classList.add('dark') 
  } else {
    document.documentElement.classList.remove('dark')
  }
}, { immediate: true })

const active = ref('home')

const menuOptions = [
  { label: '首页', key: 'home', icon: () => '🏠', onClick: () => router.push('/') }
]

const userMenuOptions = [
  { label: '个人信息', key: 'profile' },
  { label: '修改密码', key: 'change-password' },
  { type: 'divider', key: 'd1' },
  { label: '退出登录', key: 'logout' }
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