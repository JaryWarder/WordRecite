import axios from 'axios'
import { useUserStore } from '../stores/user'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081',
  withCredentials: true,
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const userStore = useUserStore()
  userStore.restore()
  const token = userStore.token
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (resp) => resp,
  (err) => {
    const status = err?.response?.status
    if (status === 401) {
      const userStore = useUserStore()
      userStore.logout()
    }
    return Promise.reject(err)
  }
)

export default http
