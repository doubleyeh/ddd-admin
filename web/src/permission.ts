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
      menuStore.dynamicRoutes.forEach(r => router.addRoute('Home', r))
      menuStore.setRoutesAdded()
      return true
    }
    return false

  } catch (error) {
    userStore.logout()
    return false
  }
}