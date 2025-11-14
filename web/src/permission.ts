import { http } from '@/utils/http'
import type { Router } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useMenuStore } from '@/store/menu'

export async function loadAndAddRoutes(router: Router): Promise<boolean> {
  const userStore = useUserStore()
  const menuStore = useMenuStore()
  try {
    const { user: u, permissions, menus } = await http.get<any>('/account/info')
    userStore.setAccountInfo(u, permissions)
    menuStore.setMenus(menus)
    if (menuStore.dynamicRoutes.length > 0) {
      menuStore.dynamicRoutes.forEach(r => {
        let path = r.path || ''
        path = path.startsWith('/') ? path.slice(1) : path
        path = path.replace(/\/+/g, '/')
        const childRoute = { ...r, path }
        router.addRoute('Layout', childRoute)
      })
      menuStore.setRoutesAdded()
      return true
    }
    return false
  } catch (error) {
    userStore.logout()
    return false
  }
}