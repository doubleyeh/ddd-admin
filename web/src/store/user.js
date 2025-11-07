import { defineStore } from 'pinia';
import { getToken, setToken, removeToken } from '@/utils/auth';
import { login as apiLogin } from '@/api/auth'; 
import { getInfo as apiGetInfo } from '@/api/account';

export const useUserStore = defineStore('user', {
    state: () => ({
        token: getToken(),
        username: '',
        permissions: [],
        roles: [],
    }),
    actions: {
        async login(userInfo) {
            const { username, password, tenantId } = userInfo;
            const response = await apiLogin({ username: username.trim(), password, tenantId });

            this.token = response.data.token;
            setToken(response.data.token);
        },

        async getInfo() {
            if (!this.token) {
                return;
            }
            const response = await apiGetInfo(); 
            
            const { username, roles, permissions } = response.data;

            this.username = username;
            this.roles = roles;
            this.permissions = permissions;
        },

        async logout() {
            this.token = undefined;
            this.username = '';
            this.permissions = [];
            this.roles = [];
            removeToken();
        },
    },
});