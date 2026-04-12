import http from './http'

export async function getUserInfo (username) {
  const resp = await http.get(`/api/user/get_userInfo/${encodeURIComponent(username)}`)
  return resp.data
}

export async function deleteUser (user) {
  const resp = await http.get(`/api/user/delete_user/${encodeURIComponent(user)}`)
  return resp.data
}
