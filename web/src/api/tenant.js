import request from '@/utils/request';

const API_URL = '/tenants';

export function getPage(params) {
    return request({
        url: API_URL,
        method: 'GET',
        params,
    });
}

export function createTenant(data) {
    return request({
        url: API_URL,
        method: 'POST',
        data,
    });
}

export function updateTenant(id, data) {
    return request({
        url: `${API_URL}/${id}`,
        method: 'PUT',
        data,
    });
}

export function deleteTenant(id) {
    return request({
        url: `${API_URL}/${id}`,
        method: 'DELETE',
    });
}