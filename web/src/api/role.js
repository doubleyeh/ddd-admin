import request from '@/utils/request';

const API_URL = '/roles';

export function getPage(params) {
    return request({
        url: API_URL,
        method: 'GET',
        params,
    });
}

export function createRole(data) {
    return request({
        url: API_URL,
        method: 'POST',
        data,
    });
}

export function updateRole(id, data) {
    return request({
        url: `${API_URL}/${id}`,
        method: 'PUT',
        data,
    });
}

export function deleteRole(id) {
    return request({
        url: `${API_URL}/${id}`,
        method: 'DELETE',
    });
}

export function getRoleMenus(id) {
    return request({
        url: `${API_URL}/${id}/menus`,
        method: 'GET',
    });
}