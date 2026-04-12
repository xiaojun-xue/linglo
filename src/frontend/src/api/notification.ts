import request from './request'

// 获取通知列表
export function getNotificationList(params?: any) {
  return request.get('/notifications', { params })
}

// 获取未读数量
export function getUnreadCount() {
  return request.get('/notifications/unread-count')
}

// 标记已读
export function markAsRead(id: number) {
  return request.put(`/notifications/${id}/read`)
}

// 全部标记已读
export function markAllAsRead() {
  return request.put('/notifications/read-all')
}
