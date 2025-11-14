<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useMenuStore } from '@/store/menu'
import { useThemeStore } from '@/store/theme'
import { useMessage } from 'naive-ui'
import { ChevronBackCircleOutline, ChevronForwardCircleOutline } from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const user = useUserStore()
const menuStore = useMenuStore()
const themeStore = useThemeStore()
const message = useMessage()

const collapsed = ref(false)

const userMenuOptions = [
  { label: '个人信息', key: 'Profile' },
  { label: '修改密码', key: 'ChangePassword' },
  { label: '退出登录', key: 'logout' }
]

function handleUserMenuSelect(key: string) {
  switch (key) {
    case 'Profile': router.push({ name: 'Profile' }); break
    case 'ChangePassword': router.push({ name: 'ChangePassword' }); break
    case 'logout': logout(); break
  }
}

function logout() {
  user.logout()
  router.push('/login')
}

const homeMenuOption = computed(() => ({
  label: menuStore.renderLabel('首页'),
  key: 'Home',
  icon: menuStore.renderIcon('HomeOutline'),
  onClick: async (e?: Event) => {
    if (e && e.stopPropagation) e.stopPropagation()
    try {
      await router.push({ name: 'Home' })
    } catch (error) {
      console.error('首页跳转失败:', error)
      message.error('无法导航到首页')
    }
  }
}))

const allMenuOptions = computed(() => {
  const process = (opts: any[]): any[] => {
    return opts.map(opt => {
      const newOpt = { ...opt }
      if (newOpt.children && newOpt.children.length) {
        newOpt.children = process(newOpt.children)
      } else if (!newOpt.onClick) {
        newOpt.onClick = async (e?: Event) => {
          if (e && e.stopPropagation) e.stopPropagation()
          try {
            let targetPath = newOpt.path || ''
            targetPath = targetPath.startsWith('/') ? targetPath : `/${targetPath}`
            targetPath = targetPath.replace(/\/+/g, '/')
            await router.push(targetPath)
          } catch (error) {
            console.error('路由跳转失败:', error)
            message.error('无法导航到该页面')
          }
        }
      }
      return newOpt
    })
  }
  return [homeMenuOption.value, ...process(menuStore.menuOptions)]
})
</script>

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
            <n-button quaternary>👤</n-button>
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
          :collapsed="collapsed"
          :collapsed-width="64"
          :options="allMenuOptions"
          :value="route.name as string"
        />
      </n-layout-sider>
      
      <n-layout-content content-style="padding: 24px; min-height: 100%;" class="bg-gray-50 dark:bg-gray-900">
        <router-view />
      </n-layout-content>
    </n-layout>
  </div>
</template>