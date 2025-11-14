import { defineStore } from 'pinia'
import { h } from 'vue'
import type { VNodeChild } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import * as ionicons from '@vicons/ionicons5'
import { NIcon } from 'naive-ui'

const modules = import.meta.glob('../views/**/*.vue')

export interface MenuDTO {
  id: number
  name: string
  path: string | null
  icon: string | null
  component: string | null
  parentId: number | null
  sort: number | null
  isHidden: boolean | null
  children: MenuDTO[] | null
}

interface MenuOption {
  label: () => VNodeChild
  key: string
  path: string | null
  icon?: () => VNodeChild
  children?: MenuOption[]
  onClick?: (e?: Event) => void
}

export const renderIcon = (iconName: string) => {
  const IconComponent = (ionicons as any)[iconName] || ionicons.HomeOutline
  return () => h(NIcon, null, { default: () => h(IconComponent) })
}

export const renderLabel = (label: string) => {
  return () => h('span', null, label)
}

const mapMenusToOptions = (menus: MenuDTO[]): MenuOption[] => {
  return menus.map(menu => {
    const option: MenuOption = {
      label: renderLabel(menu.name),
      key: menu.name,
      path: menu.path,
      icon: menu.icon ? renderIcon(menu.icon) : undefined,
      children: menu.children && menu.children.length > 0 ? mapMenusToOptions(menu.children) : undefined
    }
    return option
  })
}

const mapMenusToRoutes = (menus: MenuDTO[]): RouteRecordRaw[] => {
  const routes: RouteRecordRaw[] = []
  menus.forEach(menu => {
    if (menu.component && menu.path && menu.component !== 'Layout') {
      let routePath = menu.path.startsWith('/') ? menu.path : `/${menu.path}`
      routePath = routePath.replace(/\/+/g, '/')
      let componentPath = menu.component
      if (componentPath.startsWith('views/')) componentPath = componentPath.substring(6)
      const componentKey = `../views/${componentPath}.vue`
      const component = modules[componentKey]
      if (component) {
        routes.push({
          path: routePath,
          name: menu.name,
          component: component,
          meta: { requiresAuth: true, title: menu.name, icon: menu.icon }
        })
      }
    }
    if (menu.children && menu.children.length > 0) {
      routes.push(...mapMenusToRoutes(menu.children))
    }
  })
  return routes
}

export const useMenuStore = defineStore('menu', {
  state: () => ({
    menuOptions: [] as MenuOption[],
    dynamicRoutes: [] as RouteRecordRaw[],
    isRoutesAdded: false
  }),
  actions: {
    setMenus(menus: MenuDTO[]) {
      this.menuOptions = mapMenusToOptions(menus)
      this.dynamicRoutes = mapMenusToRoutes(menus)
    },
    setRoutesAdded() {
      this.isRoutesAdded = true
    },
    renderLabel,
    renderIcon
  }
})