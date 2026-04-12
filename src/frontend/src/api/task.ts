import request from './request'

// 获取任务列表
export function getTaskList(params?: any) {
  return request.get('/tasks', { params })
}

// 获取任务详情
export function getTaskById(id: number) {
  return request.get(`/tasks/${id}`)
}

// 创建任务
export function createTask(data: any) {
  return request.post('/tasks', data)
}

// 更新任务
export function updateTask(id: number, data: any) {
  return request.put(`/tasks/${id}`, data)
}

// 更新任务状态
export function updateTaskStatus(id: number, status: string) {
  return request.put(`/tasks/${id}/status`, null, { params: { status } })
}

// 分配任务
export function assignTask(id: number, assigneeId: number) {
  return request.put(`/tasks/${id}/assign`, null, { params: { assigneeId } })
}

// 删除任务
export function deleteTask(id: number) {
  return request.delete(`/tasks/${id}`)
}

// 获取看板视图
export function getTaskBoard(params?: any) {
  return request.get('/tasks/board', { params })
}

// 获取燃尽图数据
export function getBurndownData(sprintId: number) {
  return request.get(`/tasks/${sprintId}/burndown`)
}
