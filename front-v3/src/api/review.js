import http from './http'

export async function getDaily (user) {
  const resp = await http.get(`/api/review/get_daily/${encodeURIComponent(user)}`)
  return resp.data
}
