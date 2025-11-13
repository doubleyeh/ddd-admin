import { defineStore } from 'pinia'

export interface LoginResDTO {
  token: string
  username: string
  tenantId: string
}

export interface UserInfoDTO {
    id: string | number
    username: string
    nickname: string
    state: number
    createTime: string
}

export const useUserStore = defineStore('user', {
  state: () => ({ 
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
    tenantId: localStorage.getItem('tenantId') || '',
    nickname: '',
    permissions: [] as string[]
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    login(userInfo: LoginResDTO) {
      this.token = userInfo.token
      this.username = userInfo.username
      this.tenantId = userInfo.tenantId

      localStorage.setItem('token', userInfo.token)
      localStorage.setItem('username', userInfo.username)
      localStorage.setItem('tenantId', userInfo.tenantId)
    },
    
    setAccountInfo(userInfo: UserInfoDTO, permissions: string[]) {
        this.nickname = userInfo.nickname
        this.permissions = permissions
    },
    
    logout() {
      this.token = ''
      this.username = ''
      this.tenantId = ''
      this.nickname = ''
      this.permissions = []
      
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('tenantId')
    }
  }
})