<template>
  <div class="h-screen flex items-center justify-center bg-gray-100 dark:bg-gray-800">
    <div class="p-8 bg-white dark:bg-gray-700 rounded-xl shadow w-80">
      <n-input v-model:value="username" placeholder="用户名" class="mb-4" />
      <n-input v-model:value="password" type="password" placeholder="密码" class="mb-4" />
      <n-button type="primary" block @click="onLogin">登录</n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const username = ref('')
const password = ref('')

function onLogin() {
  if (username.value && password.value) {
    userStore.login('token')
    router.push('/')
  }
}
</script>
