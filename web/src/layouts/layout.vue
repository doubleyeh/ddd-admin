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
          :options="allMenuOptions"
          v-model:value="active"
          :collapsed="collapsed"
          :collapsed-width="64"
          :collapsed-icon-size="22"
        />
      </n-layout-sider>
      
      <n-layout-content content-style="padding: 24px;" class="flex-1 overflow-auto">
        <n-spin :show="user.isLoggedIn && !menuStore.isRoutesAdded">
             <router-view />
        </n-spin>
      </n-layout-content>
    </n-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { http } from '@/utils/http'
import { useUserStore } from '../store/user'
import { useThemeStore } from '../store/theme'
import { useMenuStore, MenuDTO } from '../store/menu'
import { NMenu, NSwitch, NButton, NLayout, NLayoutHeader, NLayoutSider, NLayoutContent, NDropdown, NIcon, useMessage, NSpin } from 'naive-ui'
import { ChevronBackCircleOutline, ChevronForwardCircleOutline } from '@vicons/ionicons5'

interface UserDTO {
  id: number
  username: string
  nickname: string
  state: number
  createTime: string
}
interface AccountInfoDTO {
    user: UserDTO
    permissions: string[]
    menus: MenuDTO[]
}

const router = useRouter()
const route = useRoute()
const user = useUserStore()
const themeStore = useThemeStore()
const menuStore = useMenuStore()
const userStore = useUserStore()
const message = useMessage()
const collapsed = ref(false)

const active = ref(route.name as string || 'Home')

watch(() => route.name, (name) => {
  if (name) {
    active.value = name as string
  }
}, { immediate: true })

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

const homeMenuOption = computed(() => {
    return { 
        label: menuStore.renderLabel('首页', 'Home'), 
        key: 'Home', 
        icon: menuStore.renderIcon('🏠')
    }
})

const allMenuOptions = computed(() => {
    return [
        homeMenuOption.value,
        ...menuStore.menuOptions
    ]
})

async function fetchAccountInfo() {
    if (menuStore.isRoutesAdded) return 

    try {
        const { user, permissions, menus } = await http.get<any>('/account/info')
        
        userStore.setAccountInfo(user, permissions) 
        menuStore.setMenus(menus)
        
        if (menuStore.dynamicRoutes.length > 0) {
            menuStore.setRoutesAdded()
            router.replace(route.fullPath)
        }

    } catch (error) {
        message.error('加载用户信息失败')
        console.error('Account Info Error:', error)
    }
}

onMounted(() => {
    if (user.isLoggedIn) {
        fetchAccountInfo()
    }
})
</script>