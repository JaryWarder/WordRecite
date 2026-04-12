import http from './http'

export async function getProgress (user) {
  const resp = await http.get(`/api/progress/get_progress/${encodeURIComponent(user)}`)
  return resp.data
}

export async function getDailyList (date) {
  const resp = await http.get('/api/progress/daily_list', {
    params: { date }
  })
  return resp.data
}
