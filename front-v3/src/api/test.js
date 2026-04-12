import http from './http'

export async function generateTest (username, bookTitle, count) {
  const resp = await http.get('/api/test/generate', {
    params: { username, bookTitle, count }
  })
  return resp.data
}

export async function submitTest (body) {
  const resp = await http.post('/api/test/submit', body)
  return resp.data
}
