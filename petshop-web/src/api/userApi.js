// src/api/userApi.js
import instance from "@/utils/axios.js";

// 正确导出方式一：命名导出   函数声明式导出 提升（Hoisting）：函数声明会被提升，可以在声明前调用
export async function getUserProfile() {
    try {
        const response = await instance.get('/users/profile');
        return response.data;
    } catch (error) {
        throw new Error(error.response?.data?.message || '获取用户信息失败');
    }
}

export async function updateUserProfile(formData) {
    try {
        const response = await instance.put('/users/profile',
            formData,  // 添加请求体数据
        );
        return response.data;
    } catch (error) {
        throw new Error(
            error.response?.data?.message ||
            `更新失败 (${error.response?.status || '无响应'})`
        );
    }
}


// 头像上传接口
export async function uploadUserAvatar(file) {
    const formData = new FormData();
    formData.append('avatar', file); // 字段名必须与后端 @RequestPart("avatar") 一致

    try {
        const response = await instance.post('/users/upload-avatar', formData, {
            headers: {
                'Content-Type': 'multipart/form-data' // 必须指定
            }
        });
        return response.data;
    } catch (error) {
        throw new Error(
            error.response?.data?.message ||
            `头像上传失败 (${error.response?.status || '无响应'})`
        );
    }
}
export const updatePassword = (data) => {
    return instance({
        url: '/users/password', // 确保与后端接口路径一致
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json' // 明确指定JSON格式
        },
        data: JSON.stringify(data) // 确保序列化
    })
}


