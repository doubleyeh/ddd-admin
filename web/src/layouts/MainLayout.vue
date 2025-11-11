<template>
  <n-config-provider :theme="isDark ? darkTheme : null">
    <n-layout has-sider class="h-screen">
      <n-layout-sider bordered collapse-mode="width" :collapsed-width="64" width="200">
        <div class="p-4 text-center text-lg font-bold">Logo</div>
        <n-menu :options="menuOptions" />
      </n-layout-sider>
      <n-layout>
        <n-layout-header class="flex justify-between items-center p-4 border-b">
          <div>Header</div>
          <n-switch v-model:value="isDark" />
        </n-layout-header>
        <n-layout-content class="p-6 bg-gray-50 dark:bg-gray-900 text-gray-900 dark:text-gray-100">
          <router-view />
        </n-layout-content>
      </n-layout>
    </n-layout>
  </n-config-provider>
</template>

<script setup lang="ts">
import { darkTheme, NConfigProvider, NLayout, NLayoutSider, NLayoutHeader, NLayoutContent, NMenu, NSwitch } from 'naive-ui'
const isDark = ref(false)
const menuOptions = [{ label: '主页', key: 'home' }]
</script>
