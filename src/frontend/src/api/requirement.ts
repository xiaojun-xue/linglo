import request from './request'

// 获取需求列表（支持层级筛选）
export function getRequirementList(params?: any) {
  return request.get('/requirements', { params })
}

// 获取需求树（IR→SR→AR层级结构）
export function getRequirementTree(projectId: number) {
  return request.get('/requirements/tree', { params: { projectId } })
}

// 获取子需求列表
export function getRequirementChildren(id: number) {
  return request.get(`/requirements/${id}/children`)
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

// 推进需求状态
export function advanceRequirementStatus(id: number) {
  return request.put(`/requirements/${id}/advance-status`)
}

// 更新需求状态
export function updateRequirementStatus(id: number, status: string) {
  return request.put(`/requirements/${id}/status`, null, { params: { status } })
}

// 删除需求
export function deleteRequirement(id: number) {
  return request.delete(`/requirements/${id}`)
}

// 导出系统需求分析说明书（基于IR）
export function exportSystemSpec(irIds: number[]) {
  return request.post('/requirements/export/system-spec', irIds)
}

// 导出功能需求分析说明书（基于SR）
export function exportFuncSpec(srIds: number[]) {
  return request.post('/requirements/export/func-spec', srIds)
}

// 按产品线获取需求
export function getRequirementsByProduct(productId: number) {
  return request.get(`/requirements/product/${productId}`)
}

// 按项目获取需求
export function getRequirementsByProject(projectId: number) {
  return request.get(`/requirements/project/${projectId}`)
}
