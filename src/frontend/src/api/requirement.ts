import request from './request'

// 获取需求列表
export function getRequirementList(params?: any) {
  return request.get('/requirements', { params })
}

// 获取需求详情
export function getRequirementById(id: number) {
  return request.get(`/requirements/${id}`)
}

// 创建需求
export function createRequirement(data: any) {
  return request.post('/requirements', data)
}

// 更新需求
export function updateRequirement(id: number, data: any) {
  return request.put(`/requirements/${id}`, data)
}

// 更新需求状态
export function updateRequirementStatus(id: number, status: string) {
  return request.put(`/requirements/${id}/status`, null, { params: { status } })
}

// 删除需求
export function deleteRequirement(id: number) {
  return request.delete(`/requirements/${id}`)
}

// 按产品线获取需求
export function getRequirementsByProduct(productId: number) {
  return request.get(`/requirements/product/${productId}`)
}

// 按项目获取需求
export function getRequirementsByProject(projectId: number) {
  return request.get(`/requirements/project/${projectId}`)
}
