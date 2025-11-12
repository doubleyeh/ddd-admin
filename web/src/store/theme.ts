import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    isDark: localStorage.getItem('theme') === 'dark'
  }),
  actions: {
    toggleDark(value?: boolean) {
      const target = value !== undefined ? value : !this.isDark
      this.isDark = target
      localStorage.setItem('theme', target ? 'dark' : 'light')
      
      if (target) {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
    }
  }
})