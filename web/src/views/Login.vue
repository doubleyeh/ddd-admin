<template>
  <div class="h-screen flex items-center justify-center bg-gradient-to-br from-sky-200 to-blue-400 dark:from-slate-800 dark:to-slate-950">
    <div class="p-8 rounded-2xl bg-white dark:bg-slate-800 shadow-lg w-80">
      <h2 class="text-center text-xl font-semibold mb-6 text-gray-700 dark:text-gray-100">登录系统</h2>
      <n-input v-model:value="tenantId" placeholder="租户ID" class="mb-4" />
      <n-input v-model:value="username" placeholder="用户名" class="mb-4" />
      <n-input v-model:value="password" placeholder="密码" type="password" class="mb-6" />
      <n-button type="primary" block @click="loginClick">登 录</n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { NInput, NButton } from 'naive-ui'

const username = ref('')
const password = ref('')
const tenantId = ref('')
const router = useRouter()
const store = useUserStore()
function loginClick() {
  if (username.value && password.value && tenantId.value) {
    store.login('fake-token-' + Date.now(), username.value) 
    router.push('/')
  }
}
</script>