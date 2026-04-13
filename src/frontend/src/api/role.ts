import request from './request'

// 获取角色列表
export function getRoleList() {
  return request.get('/roles')
}

// 获取角色详情
export function getRoleById(id: number) {
  return request.get(`/roles/${id}`)
}

// 创建角色
export function createRole(data: any) {
  return request.post('/roles', data)
}

// 更新角色
export function updateRole(id: number, data: any) {
  return request.put(`/roles/${id}`, data)
}

// 删除角色
export function deleteRole(id: number) {
  return request.delete(`/roles/${id}`)
}
