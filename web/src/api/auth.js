import request from '@/utils/request';

export function login(data) {
    return request({
        url: '/auth/login',
        method: 'POST',
        data,
    });
}

export function getInfo() {
    return request({
        url: '/auth/info',
        method: 'GET',
    });
}