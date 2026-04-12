import http from './http'

export async function getProgress (user) {
  const resp = await http.get(`/api/progress/get_progress/${encodeURIComponent(user)}`)
  return resp.data
}
