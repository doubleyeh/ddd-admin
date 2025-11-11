import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/', component: () => import('../layouts/MainLayout.vue'), meta: { requiresAuth: true } }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const isLogin = localStorage.getItem('token')
  if (to.meta.requiresAuth && !isLogin) next('/login')
  else next()
})

export default router
