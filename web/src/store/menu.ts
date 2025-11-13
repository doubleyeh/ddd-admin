import { defineStore } from 'pinia'
import { h } from 'vue'
import type { VNodeChild } from 'vue'
import { RouterLink } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import * as ionicons from '@vicons/ionicons5'
import { NIcon } from 'naive-ui'

interface MenuOption {
    label: string | (() => VNodeChild)
    key: string
    icon?: () => VNodeChild
    children?: MenuOption[]
}

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

export const renderIcon = (iconName: string) => {
    const IconComponent = (ionicons as any)[iconName] || ionicons.HomeOutline
    
    return () => h(NIcon, null, { default: () => h(IconComponent) })
}

export const renderLabel = (label: string, routeName: string | null) => {
    if (routeName) {
        return h(
            RouterLink,
            {
                to: { name: routeName }
            },
            { default: () => () => label }
        )
    }
    return h('span', null, label)
}

const mapMenusToOptions = (menus: MenuDTO[]): MenuOption[] => {
    return menus.map(menu => {
        const routeName = menu.component && menu.component !== 'Layout' ? menu.name : null
        
        const option: MenuOption = {
            label: renderLabel(menu.name, routeName),
            key: menu.name, 
            icon: menu.icon ? renderIcon(menu.icon) : undefined
        }

        if (menu.children && menu.children.length > 0) {
            option.children = mapMenusToOptions(menu.children)
        }
        return option
    })
}

const mapMenusToRoutes = (menus: MenuDTO[]): RouteRecordRaw[] => {
    const routes: RouteRecordRaw[] = []

    menus.forEach(menu => {
        if (menu.component && menu.path && menu.component !== 'Layout') {
            const routePath = menu.path.startsWith('/') ? menu.path : `/${menu.path}`
            
            let componentPath = menu.component
            if (componentPath.startsWith('views/')) {
                componentPath = componentPath.substring(6)
            }
            
            const route: RouteRecordRaw = {
                path: routePath,
                name: menu.name,
                component: () => import(/* @vite-ignore */ `@/views/${componentPath}.vue`),
                meta: { 
                    requiresAuth: true, 
                    title: menu.name,
                    icon: menu.icon
                }
            }
            routes.push(route)
        }
        
        if (menu.children && menu.children.length > 0) {
            const childRoutes = mapMenusToRoutes(menu.children)
            routes.push(...childRoutes)
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