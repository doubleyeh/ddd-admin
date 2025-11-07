import request from '@/utils/request';

const API_URL = '/users';

export function getPage(params) {
    return request({
        url: API_URL,
        method: 'GET',
        params,
    });
}

export function createUser(data) {
    return request({
        url: API_URL,
        method: 'POST',
        data,
    });
}

export function updateUser(id, data) {
    return request({
        url: `${API_URL}/${id}`,
        method: 'PUT',
        data,
    });
}

export function resetPassword(id) {
    return request({
        url: `${API_URL}/${id}/password`,
        method: 'PUT',
    });
}

export function deleteUser(id) {
    return request({
        url: `${API_URL}/${id}`,
        method: 'DELETE',
    });
}