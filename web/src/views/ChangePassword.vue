<template>
  <n-card title="修改密码" class="max-w-xl mx-auto">
    <n-form ref="formRef" :model="model" :rules="rules" :label-width="100" label-placement="left">
      <n-form-item label="旧密码" path="oldPassword">
        <n-input v-model:value="model.oldPassword" type="password" show-password-on="click" placeholder="输入旧密码" />
      </n-form-item>
      <n-form-item label="新密码" path="newPassword">
        <n-input v-model:value="model.newPassword" type="password" show-password-on="click" placeholder="输入新密码" />
      </n-form-item>
      <n-form-item label="确认密码" path="confirmPassword">
        <n-input v-model:value="model.confirmPassword" type="password" show-password-on="click" placeholder="再次输入新密码" />
      </n-form-item>
    </n-form>
    <div class="mt-4 text-center">
      <n-button type="primary" :loading="loading" @click="handleUpdatePassword">确认修改</n-button>
    </div>
  </n-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { http } from '@/utils/http'
import { useUserStore } from '@/store/user'
import { NCard, NForm, NFormItem, NInput, NButton, useMessage } from 'naive-ui'
import type { FormInst, FormRules } from 'naive-ui'

const formRef = ref<FormInst | null>(null)
const message = useMessage()
const loading = ref(false)
const userStore = useUserStore()
const router = useRouter()

const model = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules: FormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: ['input', 'blur'] }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: ['input', 'blur'] },
    { min: 6, message: '密码长度不能少于6位', trigger: ['input', 'blur'] }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: ['input', 'blur'] },
    {
      validator: (rule, value) => value === model.value.newPassword,
      message: '两次密码输入不一致',
      trigger: ['input', 'blur']
    }
  ]
}

async function handleUpdatePassword() {
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      loading.value = true
      try {
        const payload = {
          oldPassword: model.value.oldPassword,
          newPassword: model.value.newPassword
        }
        
        await http.put<boolean>('/account/password', payload)
        
        message.success('密码修改成功，请重新登录')
        
        userStore.logout()
        router.push('/login')
        
      } catch (error: any) {
        message.error(error.message || '密码修改失败')
      } finally {
        loading.value = false
      }
    } else {
      message.error('请检查表单输入')
    }
  })
}
</script>