// src/utils/websocket.js
import { Client } from '@stomp/stompjs'

class ChatClient {
    constructor() {
        this.client = null
        this.subscription = null
        this.reconnectAttempts = 0 // ✅ 重连计数器
    }

    async connect(token, onMessage) {
        return new Promise((resolve, reject) => {

            // ✅ 协议自适应处理
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
            const wsUrl = `${protocol}//${window.location.hostname}:8080/ws-chat?token=${encodeURIComponent(token)}`;

            this.client = new Client({
                brokerURL: wsUrl,
                beforeConnect: () => {
                    this.client.connectHeaders = {
                        // ✅ 优先使用 Header 传递（需后端支持 CORS）
                        Authorization: `Bearer ${token}`
                    };
                },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,


                // ✅ 连接成功回调
                onConnect: () => {
                    this.reconnectAttempts = 0 // 重置重连计数器
                    this.subscription = this.client.subscribe(
                        `/user/queue/messages`,
                        message => onMessage(JSON.parse(message.body))
                    )
                    resolve()
                },

                // ✅ 错误处理增强
                onStompError: frame => {
                    const errorMsg = frame.headers?.message || 'STOMP协议错误'
                    if (this.reconnectAttempts < 3) {
                        this.reconnectAttempts++
                        log.warn(`尝试第${this.reconnectAttempts}次重连...`)
                    } else {
                        reject(new Error(`连接失败: ${errorMsg}`))
                    }
                },

                // ✅ 断线日志
                onWebSocketClose: (event) => {
                    console.warn('WebSocket连接关闭:', event.reason)
                }
            })

            this.client.activate()
        })
    }

    // ✅ 消息发送增加校验
    sendMessage(message) {
        if (!this.client?.connected) {
            throw new Error('连接未就绪，请先调用connect方法')
        }

        // ✅ 关键：发送时包含必要元数据
        this.client.publish({
            destination: '/app/chat.send',
            body: JSON.stringify({
                ...message,
                tempId: Date.now() // 携带临时ID用于服务端确认
            }),
            headers: { 'content-type': 'application/json' }
        });
    }

    // ✅ 安全断开连接
    async disconnect() {
        return new Promise((resolve) => {
            if (this.subscription) {
                this.subscription.unsubscribe()
                this.subscription = null
            }

            if (this.client?.connected) {
                this.client.deactivate().then(() => resolve())
            } else {
                resolve()
            }
        })
    }
}

export default new ChatClient()