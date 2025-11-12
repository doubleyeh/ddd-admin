<template>
  <n-card title="个人信息" class="max-w-xl mx-auto">
    <n-spin :show="loading">
      <n-form :label-width="80" label-placement="left">
        <n-form-item label="用户名">
          <n-input :value="user.username" disabled />
        </n-form-item>
        <n-form-item label="租户ID">
          <n-input :value="user.tenantId" disabled />
        </n-form-item>
        <n-form-item label="昵称">
          <n-input v-model:value="userInfo.nickname" />
        </n-form-item>
        <n-form-item label="状态">
          <n-tag :type="userInfo.state === 1 ? 'success' : 'error'">
            {{ userInfo.state === 1 ? '正常' : '禁用' }}
          </n-tag>
        </n-form-item>
        <n-form-item label="注册时间">
          <n-input :value="formatTime(userInfo.createTime)" disabled />
        </n-form-item>
      </n-form>
      <div class="mt-4 text-center">
        <n-button type="primary" :loading="saving" @click="handleSave">保存</n-button>
      </div>
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { http } from '@/utils/http'
import { useUserStore } from '@/store/user'
import { NCard, NForm, NFormItem, NInput, NButton, NSpin, useMessage, NTag } from 'naive-ui'

interface UserDTO {
  id: number
  username: string
  nickname: string
  state: number
  createTime: string
}

const user = useUserStore()
const message = useMessage()
const loading = ref(false)
const saving = ref(false)

const userInfo = reactive<UserDTO>({
  id: 0,
  username: '',
  nickname: '',
  state: 0,
  createTime: '',
})

function formatTime(time: string) {
  if (!time) return 'N/A'
  return new Date(time).toLocaleString()
}

async function fetchUserInfo() {
  loading.value = true
  try {
    const data = await http.get<UserDTO>('/account/info')
    Object.assign(userInfo, data)
  } catch (error: any) {
    message.error(error.message || '获取信息失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!userInfo.nickname) {
    message.warning('昵称不能为空')
    return
  }
  
  saving.value = true
  try {
    await http.put('/account/nickname', {
      nickname: userInfo.nickname
    })
    message.success('昵称修改成功')
    
  } catch (error: any) {
    message.error(error.message || '修改昵称失败')
    
  } finally {
    saving.value = false
  }
}

onMounted(fetchUserInfo)
</script>