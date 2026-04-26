import request from './request'

// 获取项目列表
export function getProjectList(params?: any) {
  return request.get('/projects', { params })
}

// 获取项目详情
export function getProjectById(id: number) {
  return request.get(`/projects/${id}`)
}

// 创建项目
export function createProject(data: any) {
  return request.post('/projects', data)
}

// 更新项目
export function updateProject(id: number, data: any) {
  return request.put(`/projects/${id}`, data)
}

// 更新项目阶段
export function updateProjectStage(id: number, stage: number) {
  return request.put(`/projects/${id}/stage`, null, { params: { stage } })
}

// 删除项目
export function deleteProject(id: number) {
  return request.delete(`/projects/${id}`)
}

// 获取项目统计
export function getProjectStatistics(id: number) {
  return request.get(`/projects/${id}/statistics`)
}

// 推进项目阶段（IPD评审门控）
export function advanceProjectStage(id: number, force: boolean = false) {
  return request.put(`/projects/${id}/advance-stage`, null, { params: { force } })
}
