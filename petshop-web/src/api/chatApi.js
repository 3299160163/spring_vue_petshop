import instance from '@/utils/axios'

export const getChatHistory = (params) => {
    return instance.get('/chat/messages', {
        params: {
            sellerId: params.sellerId,
            buyerId: params.buyerId,
            petId: params.petId
        }
    })
}

// 标记消息已读
export const markMessagesRead = (data) => {
    return instance.post('/chat/mark-read', data)
}

export const fetchChatList = async () => {
    return instance.get('/chat/list');
};