import http from './http'

export async function getUserInfo (username) {
  const resp = await http.get(`/api/user/get_userInfo/${encodeURIComponent(username)}`)
  return resp.data
}

