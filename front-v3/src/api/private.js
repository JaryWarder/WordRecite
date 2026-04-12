import http from './http'

export async function getPrivate (user) {
  const resp = await http.get(`/api/private/get_private/${encodeURIComponent(user)}`)
  return resp.data
}

export async function addPrivate (obj) {
  const resp = await http.get('/api/private/add_private', {
    params: { obj: JSON.stringify(obj) },
    paramsSerializer: (params) => new URLSearchParams(params).toString()
  })
  return resp.data
}

export async function deletePrivate (obj) {
  const resp = await http.get('/api/private/delete_private', {
    params: { obj: JSON.stringify(obj) },
    paramsSerializer: (params) => new URLSearchParams(params).toString()
  })
  return resp.data
}

export async function checkPrivate (username, word) {
  const resp = await http.get('/api/private/check_private', {
    params: { username, word },
    paramsSerializer: (params) => new URLSearchParams(params).toString()
  })
  return resp.data
}
