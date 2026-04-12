import axios from 'axios'
import { encryptPasswordToHex } from './src/utils/rsa.js'

const BASE_URL = 'http://localhost:8080'
const USERNAME = 'user03'
const PASSWORD = '123456'

async function main () {
  const client = axios.create({
    baseURL: BASE_URL,
    timeout: 15000,
    validateStatus: () => true
  })

  const req1 = await client.get('/api/user/login_request')
  if (req1.status !== 200 || req1.data?.code !== 200) {
    throw new Error(`login_request failed: status=${req1.status} body=${JSON.stringify(req1.data)}`)
  }

  const setCookie = req1.headers?.['set-cookie']
  const cookieHeader = Array.isArray(setCookie) && setCookie.length > 0 ? setCookie[0].split(';')[0] : ''
  if (!cookieHeader) {
    throw new Error('login_request did not return session cookie (set-cookie)')
  }

  const pubExpHex = req1.data?.data?.pub_exp
  const pubModHex = req1.data?.data?.pub_mod
  if (!pubExpHex || !pubModHex) {
    throw new Error(`login_request missing pub keys: ${JSON.stringify(req1.data)}`)
  }

  const encrypted = encryptPasswordToHex({ password: PASSWORD, pubExpHex, pubModHex })

  const req2 = await client.post(
    '/api/user/login_check',
    { username: USERNAME, encrypted },
    { headers: { Cookie: cookieHeader } }
  )
  if (req2.status !== 200 || req2.data?.code !== 200) {
    throw new Error(`login_check failed: status=${req2.status} body=${JSON.stringify(req2.data)}`)
  }

  const token = req2.data?.data?.token
  if (!token) {
    throw new Error(`login_check missing token: ${JSON.stringify(req2.data)}`)
  }

  const req3 = await client.get(`/api/review/get_daily/${encodeURIComponent(USERNAME)}`, {
    headers: { Authorization: `Bearer ${token}` }
  })

  console.log('GET /api/review/get_daily =>', JSON.stringify(req3.data))

  if (req3.status !== 200 || req3.data?.code !== 200) {
    process.exitCode = 1
    return
  }
  process.exitCode = 0
}

main().catch((e) => {
  console.error(e)
  process.exitCode = 1
})
