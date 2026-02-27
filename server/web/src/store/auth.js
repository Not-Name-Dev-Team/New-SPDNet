import { reactive } from 'vue'

const STORAGE_KEY = 'auth_user'

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
    state.user = userData
    localStorage.setItem(STORAGE_KEY, JSON.stringify(userData))
  },

  logout() {
    state.user = null
    localStorage.removeItem(STORAGE_KEY)
  },

  updateUser(userData) {
    state.user = { ...state.user, ...userData }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state.user))
  }
}
