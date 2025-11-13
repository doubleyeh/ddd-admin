<template>
  <div class="flex flex-col items-center justify-center min-h-[50vh]">
    <n-result status="404" title="404 资源未找到" description="你访问的页面失踪了，TM的，快回去！">
      <template #footer>
        <n-button type="primary" @click="goHome">返回首页</n-button>
      </template>
    </n-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { NResult, NButton } from 'naive-ui'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>