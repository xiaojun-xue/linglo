import request from './request'

// 获取用户列表
export function getUserList(params?: { page?: number; size?: number; keyword?: string }) {
  return request.get('/users', { params })
}

// 获取用户详情
export function getUserById(id: number) {
  return request.get(`/users/${id}`)
}

// 创建用户
export function createUser(data: any) {
  return request.post('/users', data)
}

// 更新用户
export function updateUser(id: number, data: any) {
  return request.put(`/users/${id}`, data)
}

// 删除用户
export function deleteUser(id: number) {
  return request.delete(`/users/${id}`)
}
