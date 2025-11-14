import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useMenuStore } from '@/store/menu'
import { loadAndAddRoutes } from '@/permission'

const Layout = () => import('@/layouts/layout.vue')

const fixedRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Layout',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { requiresAuth: true, title: '首页', icon: '🏠' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile.vue'),
        meta: { requiresAuth: true, title: '个人信息' }
      },
      {
        path: 'change-password',
        name: 'ChangePassword',
        component: () => import('@/views/changePassword.vue'),
        meta: { requiresAuth: true, title: '修改密码' }
      },
      {
        path: ':pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/404.vue'),
        meta: { requiresAuth: false, title: '404' }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: fixedRoutes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const menuStore = useMenuStore()
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + 'DDD Admin'

  if (to.path === '/login') {
    if (userStore.isLoggedIn) {
      next({ name: 'Home' })
    } else {
      next()
    }
    return
  }

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    userStore.logout()
    next({ name: 'Login' })
    return
  }

  if (userStore.isLoggedIn && !menuStore.isRoutesAdded) {
    const success = await loadAndAddRoutes(router)
    if (success) {
      next({ path: to.fullPath, replace: true })
      return
    } else {
      userStore.logout()
      next({ name: 'Login', replace: true })
      return
    }
  }

  next()
})

export default router
