import http from './http'

export async function loginRequest () {
  const resp = await http.get('/api/user/login_request')
  return resp.data
}

export async function loginCheck ({ username, encrypted }) {
  const resp = await http.post('/api/user/login_check', { username, encrypted })
  return resp.data
}

export async function sendEmail (username) {
  const resp = await http.get(`/api/email/send_email/${encodeURIComponent(username)}`)
  return resp.data
}

export async function submitSignup (obj) {
  const resp = await http.get('/api/user/submit_signup', { params: { obj: JSON.stringify(obj) } })
  return resp.data
}
