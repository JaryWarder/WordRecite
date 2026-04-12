import http from './http'

export async function getServerDate () {
  const resp = await http.get('/api/memorize/get_server_date')
  return resp.data
}

export async function memorizeWord (obj) {
  const resp = await http.get('/api/memorize/memorize', {
    params: { obj: JSON.stringify(obj) },
    paramsSerializer: (params) => new URLSearchParams(params).toString()
  })
  return resp.data
}

export async function submitList (obj) {
  const resp = await http.get('/api/memorize/submit_list', {
    params: { obj: JSON.stringify(obj) },
    paramsSerializer: (params) => new URLSearchParams(params).toString()
  })
  return resp.data
}
