// src/api/order.js
import instance from '@/utils/axios.js'

export const fetchOrders = (params) => {
    return instance.get('/orders/admin', { params })
}
export const buyerOrders = (params) => {
    return instance.get('/orders/buyer', { params })
}
export const sellerOrders = (params) => {
    return instance.get('/orders/seller', { params })
}

export const getOrderDetail = (orderNo) => {
    return instance.get(`/orders/${orderNo}`)
}

export const createOrder = (orderData) => {
    return instance.post('/orders', orderData,)
}
// 更新订单状态
export const updateOrderStatus = (orderNo, action) => {
    return instance({
        url: `/orders/${orderNo}/status`,
        method: 'PATCH',
        data: { action }
    })
}
// orderApi.js 新增删除接口
export const deleteOrder = (orderNo) =>
    instance.delete(`/orders/delete/${orderNo}`)