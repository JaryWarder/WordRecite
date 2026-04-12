import http from './http'

export async function getDaily (user) {
  const resp = await http.get(`/api/review/get_daily/${encodeURIComponent(user)}`)
  return resp.data
}

export async function generateReview (origin, count) {
  const resp = await http.get('/api/review/generate', {
    params: { origin, count }
  })
  return resp.data
}

export async function getReviewMeta (origin) {
  const resp = await http.get('/api/review/meta', {
    params: { origin }
  })
  return resp.data
}

export async function getPendingCount () {
  const resp = await http.get('/api/review/pending_count')
  return resp.data
}

export async function submitReview (body) {
  const resp = await http.post('/api/review/submit', body)
  return resp.data
}
