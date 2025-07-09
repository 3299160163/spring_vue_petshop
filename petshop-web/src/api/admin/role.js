import instance from '@/utils/axios.js'

export const getUserRoles = (userId) => {
    return instance.get(`/user-roles/${userId}/roles`)
}

export const assignRole = (data) => {
    return instance.post('/user-roles/assign', data)
}

export const deleteRole = (data) => {
    return instance.delete('/user-roles/delete', { params: data })
}

export const changeRole = (data) => {
    return instance.put('/user-roles/change-role', data)
}