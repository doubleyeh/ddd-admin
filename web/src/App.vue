<template>
  <n-config-provider :theme="isDark ? darkTheme : null">
    <n-message-provider>
      <div class="flex flex-col h-screen bg-gray-50 dark:bg-[#121212] text-gray-800 dark:text-gray-200">
        <header class="flex justify-between items-center h-14 px-6 border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-900 shadow-sm flex-shrink-0">
          <div class="text-xl font-bold">DDD Admin</div>
          <div class="flex items-center gap-4">
            <n-switch v-model:value="isDark" size="small" />
            <n-button quaternary @click="logout">退出</n-button>
          </div>
        </header>
        <div class="flex flex-1 min-h-0 overflow-hidden">
          <aside class="w-60 bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-700 p-4 hidden md:block flex-shrink-0">
            <n-menu :options="menuOptions" v-model:value="activeKey" />
          </aside>
          <main class="flex-1 overflow-auto p-6 bg-gray-50 dark:bg-[#181818] min-h-0">
            <router-view />
          </main>
        </div>
      </div>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { darkTheme, NConfigProvider, NMessageProvider, NMenu, NSwitch, NButton } from 'naive-ui'
import { useRouter } from 'vue-router'
import { useUserStore } from './store/user'

const router = useRouter()
const userStore = useUserStore()
const isDark = ref(false)
const activeKey = ref('home')
const menuOptions = [
  { label: '首页', key: 'home', onClick: () => router.push('/') },
  { label: '关于', key: 'about', onClick: () => router.push('/about') }
]

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>
