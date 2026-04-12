import http from './http'

export async function startInfo (user) {
  const resp = await http.get(`/api/start/start_info/${encodeURIComponent(user)}`)
  return resp.data
}
