import { reactive } from 'vue'

const STORAGE_KEY = 'auth_user'
const TOKEN_KEY = 'auth_token'

function loadUser() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      return JSON.parse(stored)
    }
  } catch (e) {
    console.error('Failed to load user from localStorage:', e)
  }
  return null
}

const state = reactive({
  user: loadUser()
})

export const authStore = {
  get user() {
    return state.user
  },

  get isLoggedIn() {
    return state.user !== null
  },

  login(userData) {
    // SPDNet: token 单独存储，user 对象不携带 token
    const { token, ...user } = userData || {}
    state.user = user
    if (token) {
      localStorage.setItem(TOKEN_KEY, token)
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
  },

  logout() {
    state.user = null
    localStorage.removeItem(STORAGE_KEY)
    localStorage.removeItem(TOKEN_KEY)
  },

  updateUser(userData) {
    state.user = { ...state.user, ...userData }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state.user))
  }
}
