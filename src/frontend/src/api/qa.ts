import request from './request'

// 测试用例
export function getTestCaseList(params?: any) {
  return request.get('/qa/cases', { params })
}

export function getTestCaseById(id: number) {
  return request.get(`/qa/cases/${id}`)
}

export function createTestCase(data: any) {
  return request.post('/qa/cases', data)
}

export function updateTestCase(id: number, data: any) {
  return request.put(`/qa/cases/${id}`, data)
}

export function deleteTestCase(id: number) {
  return request.delete(`/qa/cases/${id}`)
}

// 缺陷
export function getBugList(params?: any) {
  return request.get('/qa/bugs', { params })
}

export function getBugById(id: number) {
  return request.get(`/qa/bugs/${id}`)
}

export function createBug(data: any) {
  return request.post('/qa/bugs', data)
}

export function updateBug(id: number, data: any) {
  return request.put(`/qa/bugs/${id}`, data)
}

export function updateBugStatus(id: number, status: number) {
  return request.put(`/qa/bugs/${id}/status`, null, { params: { status } })
}

export function deleteBug(id: number) {
  return request.delete(`/qa/bugs/${id}`)
}

// 质量统计
export function getQualityStats(projectId?: number) {
  return request.get('/qa/quality/stats', { params: { projectId } })
}
