<template>
  <n-config-provider
    :theme="themeStore.isDark ? darkTheme : null"
    :theme-overrides="themeStore.isDark ? darkThemeOverrides : lightThemeOverrides"
  >
    <n-message-provider>
      <router-view v-slot="{ Component }">
        <component :is="isLogin ? Component : MainLayout" />
      </router-view>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import { useRoute } from 'vue-router'
  import MainLayout from './layouts/layout.vue'
  import { darkTheme, NConfigProvider, NMessageProvider } from 'naive-ui'
  import { useThemeStore } from './store/theme'
  import { lightThemeOverrides, darkThemeOverrides } from './utils/themeOverrides'

  const route = useRoute()
  const isLogin = computed(() => route.path === '/login')
  const themeStore = useThemeStore()

  themeStore.toggleDark(themeStore.isDark)
</script>
