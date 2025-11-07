<template>
    <div class="login-container">
        <n-card class="login-card" :bordered="false" title="系统登录">
            <n-form ref="formRef" :model="loginForm" :rules="rules" size="large" @submit.prevent="handleLogin">
                <n-form-item path="tenantId">
                    <n-input v-model:value="loginForm.tenantId" placeholder="请输入租户ID" size="large"
                        @keyup.enter="handleLogin">
                        <template #prefix>
                            <n-icon>
                                <LockClosedOutline />
                            </n-icon>
                        </template>
                    </n-input>
                </n-form-item>

                <n-form-item path="username">
                    <n-input v-model:value="loginForm.username" placeholder="请输入用户名" size="large"
                        @keyup.enter="handleLogin">
                        <template #prefix>
                            <n-icon>
                                <PersonOutline />
                            </n-icon>
                        </template>
                    </n-input>
                </n-form-item>

                <n-form-item path="password">
                    <n-input v-model:value="loginForm.password" placeholder="请输入密码" type="password"
                        show-password-on="click" size="large" @keyup.enter="handleLogin">
                        <template #prefix>
                            <n-icon>
                                <LockClosedOutline />
                            </n-icon>
                        </template>
                    </n-input>
                </n-form-item>

                <n-button type="primary" size="large" :loading="loading" block @click="handleLogin">
                    登录
                </n-button>
            </n-form>
        </n-card>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NCard, NForm, NFormItem, NInput, NButton, NIcon, useMessage } from 'naive-ui'
import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const message = useMessage()

const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
    username: '',
    password: '',
    tenantId: ''
})

const rules = {
    username: {
        required: true,
        message: '请输入用户名',
        trigger: 'blur'
    },
    password: {
        required: true,
        message: '请输入密码',
        trigger: 'blur'
    },
    tenantId: {
        required: true,
        message: '请输入租户ID',
        trigger: 'blur'
    }
}

const handleLogin = (e) => {
    e.preventDefault()
    formRef.value.validate(async (errors) => {
        if (!errors) {
            loading.value = true
            try {
                await userStore.login(loginForm)
                await userStore.getInfo()

                const redirect = route.query.redirect || '/'
                router.push(redirect)

                message.success('登录成功')
            } catch (error) {
                message.error(error.message || '登录失败，请检查用户名、密码或租户ID')
            } finally {
                loading.value = false
            }
        }
    })
}
</script>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    width: 100vw;
    background-color: #f0f2f5;
    background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" preserveAspectRatio="none"><rect width="100" height="100" fill="%23f0f2f5"/><path d="M0,100 L100,0 L100,100 Z" fill="%23409eff" opacity="0.1"/></svg>');
    background-size: 100% 100%;
}

.login-card {
    width: 400px;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>
