import { defineStore } from 'pinia'
import { getCookie, setCookie, delCookie } from '../utils/cookie'

const TOKEN_KEY = 'token'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    username: 'Guest'
  }),
  getters: {
    isLogin: (state) => Boolean(state.token)
  },
  actions: {
    restore () {
      if (this.token) return
      const token = localStorage.getItem(TOKEN_KEY)
      const username = getCookie('username')
      if (token) this.token = token
      if (username && username !== 'null') this.username = username
    },
    setAuth ({ token, username }) {
      this.token = token || ''
      this.username = username || 'Guest'
      if (this.token) {
        localStorage.setItem(TOKEN_KEY, this.token)
      } else {
        localStorage.removeItem(TOKEN_KEY)
      }
      if (username) {
        setCookie('username', username, 7)
      }
    },
    logout () {
      this.token = ''
      this.username = 'Guest'
      localStorage.removeItem(TOKEN_KEY)
      delCookie('username', 1)
      setCookie('isLogin', 'false', 1)
    }
  }
})
