<template>
  <div class="h-screen flex items-center justify-center bg-gradient-to-br from-sky-200 to-blue-400 dark:from-slate-800 dark:to-slate-950">
    <div class="p-8 rounded-2xl bg-white dark:bg-slate-800 shadow-lg w-[360px]" @keyup.enter="loginClick">
      <h2 class="text-center text-2xl font-semibold mb-6 text-gray-700 dark:text-gray-100">登录系统</h2>
      <n-input v-model:value="tenantId" placeholder="租户ID" class="mb-4" />
      <n-input v-model:value="username" placeholder="用户名" class="mb-4" />
      <n-input v-model:value="password" placeholder="密码" type="password" class="mb-6" />
      <n-button type="primary" block :loading="loading" @click="loginClick">登 录</n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { http } from '@/utils/http'
import { NInput, NButton, useMessage } from 'naive-ui' 
import type { LoginResDTO } from '@/store/user'

const username = ref('root') 
const password = ref('123456') 
const tenantId = ref('000000') 
const loading = ref(false)

const router = useRouter()
const store = useUserStore()
const message = useMessage()

async function loginClick() {
  if (loading.value) return

  if (!username.value || !password.value || !tenantId.value) {
    message.warning('请填写完整的登录信息')
    return
  }
  
  loading.value = true
  
  try {
    const loginRequest = {
      username: username.value,
      password: password.value,
      tenantId: tenantId.value
    }
    
    const res = await http.post<LoginResDTO>('/auth/login', loginRequest)
    
    if (res) {
      store.login(res)
      message.success('登录成功！')
      router.push('/')
    }

  } catch (error: any) {
    message.error(error.message || '登录失败，请检查信息')
  } finally {
    loading.value = false
  }
}
</script>