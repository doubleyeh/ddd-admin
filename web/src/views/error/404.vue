<template>
    <div class="not-found-container">
        <n-result status="404" title="404 资源不存在" description="生活总归带点遗憾，但你可以点击按钮返回首页。">
            <template #footer>
                <n-button type="primary" @click="goHome">返回首页</n-button>
            </template>
        </n-result>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { NResult, NButton } from 'naive-ui';

const router = useRouter();

const goHome = () => {
    router.push('/');
};
</script>

<style scoped>
.not-found-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
}
</style>