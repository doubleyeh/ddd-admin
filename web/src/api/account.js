import request from '@/utils/request';

const API_URL = '/account';

export function getInfo() {
    return request({
        url: API_URL+'/info',
        method: 'GET',
        params:{},
    });
}