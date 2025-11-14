<template>
  <n-config-provider
    :theme="themeStore.isDark ? darkTheme : null"
    :theme-overrides="themeStore.isDark ? darkThemeOverrides : lightThemeOverrides"
  >
    <n-message-provider>
      <router-view />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { useThemeStore } from './store/theme'
import { darkTheme, NConfigProvider, NMessageProvider } from 'naive-ui'
import { lightThemeOverrides, darkThemeOverrides } from './utils/themeOverrides'

const themeStore = useThemeStore()
themeStore.toggleDark(themeStore.isDark)
</script>