import request from './request'

// 获取评审列表
export function getReviewList(params?: any) {
  return request.get('/reviews', { params })
}

// 获取评审详情
export function getReviewById(id: number) {
  return request.get(`/reviews/${id}`)
}

// 创建评审
export function createReview(data: any) {
  return request.post('/reviews', data)
}

// 更新评审
export function updateReview(id: number, data: any) {
  return request.put(`/reviews/${id}`, data)
}

// 提交评审
export function submitReview(id: number) {
  return request.put(`/reviews/${id}/submit`)
}

// 开始评审
export function startReview(id: number) {
  return request.put(`/reviews/${id}/start`)
}

// 评审决策
export function decideReview(id: number, decision: number, conclusion?: string) {
  return request.put(`/reviews/${id}/decide`, null, { 
    params: { decision, conclusion } 
  })
}

// 撤回评审
export function withdrawReview(id: number) {
  return request.put(`/reviews/${id}/withdraw`)
}

// 获取待我评审
export function getMyPendingReviews(params?: any) {
  return request.get('/reviews/my/pending', { params })
}

// 获取评审统计
export function getReviewStatistics() {
  return request.get('/reviews/statistics')
}
