<template>
  <div class="not-found-wrapper" :style="{ backgroundColor: overlayColor }">
    <n-result
      status="404"
      title="404 资源未找到"
      description="你访问的页面失踪了"
      class="not-found-card"
      :style="{ backgroundColor: cardColor, color: textColor }"
    >
      <template #footer>
        <n-button type="primary" @click="goHome">({{ count }}秒)立即返回</n-button>
      </template>
    </n-result>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeVars } from 'naive-ui'

const router = useRouter()
const count = ref(10)
let timer: NodeJS.Timeout | null = null
const themeVars = useThemeVars()

const goHome = () => {
  if (timer) clearInterval(timer)
  router.push('/')
}

onMounted(() => {
  timer = setInterval(() => {
    count.value--
    if (count.value <= 0) goHome()
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const cardColor = computed(() => themeVars.cardColor + 'E6')
const textColor = computed(() => themeVars.textColorBase)
const overlayColor = computed(() => themeVars.bodyColor + '99')
</script>

<style scoped>
.not-found-wrapper {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  z-index: 9999;
}

.not-found-card {
  min-width: 360px;
  padding: 32px 48px;
  border-radius: 12px;
  box-shadow: 0 6px 25px rgba(0,0,0,0.3);
  text-align: center;
  transition: all 0.3s ease-in-out;
}
</style>
