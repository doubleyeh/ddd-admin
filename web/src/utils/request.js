import axios from 'axios';
import { getToken } from '@/utils/auth';

const message = window.$message;
const dialog = window.$dialog;

const service = axios.create({
    baseURL: '/api', 
    timeout: 5000,
});

service.interceptors.request.use(
    config => {
        const token = getToken(); 
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

service.interceptors.response.use(
    response => {
        const res = response.data;

        if (res.code !== 200) {
            message.error(res.msg || '错误');

            if (res.code === 401) {
                dialog.warning({
                    title: '确认登出',
                    content: '登录状态已过期，请重新登录',
                    positiveText: '重新登录',
                    negativeText: '取消',
                    onPositiveClick: () => {
                        
                    }
                });
            }
            return Promise.reject(new Error(res.msg || 'Error'));
        } else {
            return res; 
        }
    },
    error => {
        message.error(error.message);
        return Promise.reject(error);
    }
);

export default service;