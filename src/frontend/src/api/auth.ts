import request from './request'

// 登录
export function login(data: { username: string; password: string }) {
  return request.post('/auth/login', data)
}

// 注册
export function register(data: any) {
  return request.post('/auth/register', data)
}

// 获取当前用户
export function getCurrentUser() {
  return request.get('/auth/current')
}

// 刷新Token
export function refreshToken(refreshToken: string) {
  return request.post('/auth/refresh', null, {
    headers: { 'Refresh-Token': refreshToken }
  })
}

// 用户列表
export function getUserList(params?: any) {
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
