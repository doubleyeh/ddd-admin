<template>
  <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
    <n-card :bordered="false" class="shadow">
      <div class="text-sm text-gray-500 dark:text-slate-400">用户数</div>
      <div class="mt-2 text-2xl font-bold">1,230</div>
    </n-card>
    <n-card :bordered="false" class="shadow">
      <div class="text-sm text-gray-500 dark:text-slate-400">访问量</div>
      <div class="mt-2 text-2xl font-bold">8,921</div>
    </n-card>
    <n-card :bordered="false" class="shadow">
      <div class="text-sm text-gray-500 dark:text-slate-400">收入</div>
      <div class="mt-2 text-2xl font-bold">¥12,340</div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { NCard } from 'naive-ui'
</script>