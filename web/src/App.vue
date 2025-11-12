<template>
  <router-view v-slot="{ Component }">
    <component :is="isLogin ? Component : MainLayout" />
  </router-view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from './layouts/layout.vue'

const route = useRoute()
const isLogin = computed(() => route.path === '/login')
</script>
