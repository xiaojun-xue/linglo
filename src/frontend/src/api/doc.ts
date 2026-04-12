import request from './request'

export function getDocList(params?: any) {
  return request.get('/docs', { params })
}

export function getDocTree(projectId?: number, parentId?: number) {
  return request.get('/docs/tree', { params: { projectId, parentId } })
}

export function getDocById(id: number) {
  return request.get(`/docs/${id}`)
}

export function createDoc(data: any) {
  return request.post('/docs', data)
}

export function createDocFolder(data: any) {
  return request.post('/docs/folder', data)
}

export function updateDoc(id: number, data: any) {
  return request.put(`/docs/${id}`, data)
}

export function deleteDoc(id: number) {
  return request.delete(`/docs/${id}`)
}
