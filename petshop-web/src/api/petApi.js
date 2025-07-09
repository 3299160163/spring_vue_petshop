// src/api/petApi.js
import instance from "@/utils/axios.js";

export default {
    async getPets(params) {
        try {
            const response = await instance.get('/pets', {
                params: {
                    page: params.page || 1,    // 默认第一页
                    size: params.size || 10,   // 默认每页10条
                    category: params.category, // 分类参数
                    name: params.name          // 搜索名称
                }
            });

            // 处理图片路径并返回结构化数据
            return {
                records: (response.data.records || []).map(pet =>
                    this.processImageUrl(pet)
                ),
                total: response.data.total || 0,
                pages: response.data.pages || 1,
                current: response.data.current || 1,
                size: response.data.size || 10
            };

        } catch (error) {
            // 增强错误处理
            const errorMessage = error.response?.data?.message
                || error.message
                || '未知错误';
            throw new Error(`获取宠物列表失败: ${errorMessage}`);
        }
    },

    // 图片处理函数（保持原有逻辑）
    processImageUrl(pet) {
        return {
            ...pet,
            imageUrl: pet.imageUrl
                ? `${instance.defaults.baseURL}${pet.imageUrl}`
                : '/images/default-pet.jpg'
        }
    },
    async getMyPets(params) {
        try {
            const response = await instance.get('/pets/my', {
                params: {
                    page: params.page || 1,    // 默认第一页
                    size: params.size || 10,   // 默认每页10条
                    category: params.category, // 分类参数
                    name: params.name          // 搜索名称
                }
            });

            // 处理图片路径并返回结构化数据
            return {
                records: (response.data.records || []).map(pet =>
                    this.processImageUrl(pet)
                ),
                total: response.data.total || 0,
                pages: response.data.pages || 1,
                current: response.data.current || 1,
                size: response.data.size || 10
            };

        } catch (error) {
            // 增强错误处理
            const errorMessage = error.response?.data?.message
                || error.message
                || '未知错误';
            throw new Error(`获取宠物列表失败: ${errorMessage}`);
        }
    },

    // 新增：获取单个宠物详情
    async getPetDetail(id) {
        try {
            const response = await instance.get(`/pets/${id}`, {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                }
            })
            return response.data
        } catch (error) {
            throw new Error(error.response?.data?.message || '获取宠物信息失败')
        }
    },


    // 购买宠物
    async purchasePet(petId) {
        try {
            await instance.post(`/pets/${petId}/purchase`)
            return true
        } catch (error) {
            throw new Error(`购买操作失败: ${error.message}`)
        }
    },

//创建宠物
    async createPet(petData) {
        return instance.post('/pets', petData)
    },
}
