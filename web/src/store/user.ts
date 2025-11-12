import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({ 
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '' 
  }),
  actions: {
    login(token: string, username: string) {
      this.token = token
      this.username = username
      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
    },
    logout() {
      this.token = ''
      this.username = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
    }
  }
})