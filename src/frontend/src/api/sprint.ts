import request from './request'

// 获取项目的Sprint列表
export function getSprintList(projectId: number) {
  return request.get(`/sprints/project/${projectId}`)
}

// 获取Sprint详情
export function getSprintById(id: number) {
  return request.get(`/sprints/${id}`)
}

// 创建Sprint
export function createSprint(data: any) {
  return request.post('/sprints', data)
}

// 更新Sprint
export function updateSprint(id: number, data: any) {
  return request.put(`/sprints/${id}`, data)
}

// 开始Sprint
export function startSprint(id: number) {
  return request.put(`/sprints/${id}/start`)
}

// 完成Sprint
export function completeSprint(id: number) {
  return request.put(`/sprints/${id}/complete`)
}

// 关闭Sprint
export function closeSprint(id: number) {
  return request.put(`/sprints/${id}/close`)
}

// 获取燃尽图数据
export function getBurndownData(sprintId: number) {
  return request.get(`/sprints/${sprintId}/burndown`)
}
