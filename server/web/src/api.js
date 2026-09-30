import axios from 'axios'
import { ElMessage } from 'element-plus'
import { authStore } from './store/auth'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// SPDNet: 请求拦截器——为所有请求附加登录令牌，供后端管理员接口鉴权
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// SPDNet: 登录令牌失效(401)时清除本地登录态并跳转登录页。
//  - 带上 redirect，登录后可回到原页面（原先直接跳 /login，用户当前所在页面被丢弃）
//  - 提示"登录已过期"，避免用户被无声传送、以为页面出错
//  - 已登录态才提示：登录页本身返回 401（密码错误）不应报"过期"
//  - 跳转延迟到提示渲染之后，否则 location.replace 会立刻卸载页面、提示一闪即逝
let redirecting = false

api.interceptors.response.use(
  (resp) => resp,
  (error) => {
    if (error.response?.status === 401 && !redirecting) {
      redirecting = true
      const wasLoggedIn = authStore.isLoggedIn
      const current = window.location.pathname + window.location.search
      authStore.logout()

      if (window.location.pathname !== '/login') {
        if (wasLoggedIn) {
          ElMessage.warning('登录已过期，请重新登录')
        }
        const redirect = current === '/' ? '' : `?redirect=${encodeURIComponent(current)}`
        // 用 replace 而非 href：失效页面不应留在浏览器历史里
        setTimeout(() => {
          window.location.replace(`/login${redirect}`)
        }, wasLoggedIn ? 600 : 0)
      }
    }
    return Promise.reject(error)
  }
)

export const playerApi = {
  register(data) {
    return api.post('/register', data)
  },

  sendCode(data) {
    return api.post('/send-code', data)
  },

  login(data) {
    return api.post('/login', data)
  },

  getOnline() {
    return api.get('/online')
  },

  getPlayerPublicInfo(name) {
    return api.get(`/player/${name}`)
  },

  getPlayerPrivateInfo(name) {
    return api.get(`/player/${name}/private`)
  },

  getPlayerRecords(name) {
    return api.get(`/player/${name}/records`)
  },

  getServerInfo() {
    return api.get('/server/info')
  },

  changeName(data) {
    return api.post('/change-name', data)
  },

  changePassword(data) {
    return api.post('/change-password', data)
  },

  sendForgotPasswordCode(data) {
    return api.post('/forgot-password/send-code', data)
  },

  resetPassword(data) {
    return api.post('/forgot-password/reset', data)
  }
}

export const leaderboardApi = {
  getLeaderboard(page = 0, size = 20, filters = {}) {
    return api.get('/leaderboard', {
      params: { page, size, ...filters }
    })
  },

  // SPDNet: 获取铁人模式前三名（未被ban玩家）
  getTop3IronmanPlayers() {
    return api.get('/leaderboard/top3-ironman')
  },

  // SPDNet: 获取某玩家的全球排名（服务端计算，避免前端拉取全量记录）
  getPlayerRank(name) {
    return api.get(`/leaderboard/rank/${encodeURIComponent(name)}`)
  }
}

export const dailyChallengeApi = {
  getTodaySeeds() {
    return api.get('/daily-challenge/today')
  },

  getDailyChallengeInfo(date = null) {
    const params = date ? { date } : {}
    return api.get('/daily-challenge/info', { params })
  },

  getDailyChallengeInfoByGroup(groupIndex, date = null) {
    const params = date ? { date } : {}
    return api.get(`/daily-challenge/info/${groupIndex}`, { params })
  },

  getRecords(date = null, groupIndex = null) {
    const params = {}
    if (date) params.date = date
    if (groupIndex !== null) params.groupIndex = groupIndex
    return api.get('/daily-challenge/records', { params })
  },

  getPlayerRecords(playerName, date = null) {
    const params = date ? { date } : {}
    return api.get(`/daily-challenge/records/${playerName}`, { params })
  },

  getSeedsByDate(date) {
    return api.get(`/daily-challenge/seeds/${date}`)
  }
}

export const adminApi = {
  getStats() {
    return api.get('/admin/stats')
  },

  getPlayers(page = 0, size = 20, role = null, search = null) {
    return api.get('/admin/players', {
      params: { page, size, role, search }
    })
  },

  setPlayerRole(id, role) {
    return api.post(`/admin/player/${id}/role`, { role })
  },

  deletePlayer(id) {
    return api.delete(`/admin/player/${id}`)
  },

  getRecords(page = 0, size = 20, win = null, playerName = null) {
    return api.get('/admin/records', {
      params: { page, size, win, playerName }
    })
  },

  deleteRecord(id) {
    return api.delete(`/admin/record/${id}`)
  },

  broadcast(message) {
    return api.post('/admin/broadcast', { message })
  },

  getOnline() {
    return api.get('/admin/online')
  },

  kick(name) {
    return api.post(`/admin/kick/${encodeURIComponent(name)}`)
  },

  getPrefixes() {
    return api.get('/admin/prefixes')
  },

  createPrefix(data) {
    return api.post('/admin/prefixes', data)
  },

  updatePrefix(id, data) {
    return api.put(`/admin/prefixes/${id}`, data)
  },

  deletePrefix(id) {
    return api.delete(`/admin/prefixes/${id}`)
  },

  getPlayerPrefixes(playerName) {
    return api.get(`/admin/prefixes/player/${encodeURIComponent(playerName)}`)
  },

  assignPrefixToPlayer(playerName, prefixId) {
    return api.post(`/admin/prefixes/player/${encodeURIComponent(playerName)}/assign`, { prefixId })
  },

  removePrefixFromPlayer(playerName, prefixId) {
    return api.delete(`/admin/prefixes/player/${encodeURIComponent(playerName)}/remove`, { params: { prefixId } })
  }
}

export const prefixApi = {
  getMyPrefixes(playerName) {
    return api.get('/admin/prefixes/my', { params: { playerName } })
  },

  getMyActivePrefix(playerName) {
    return api.get('/admin/prefixes/my/active', { params: { playerName } })
  },

  setMyActivePrefix(playerName, assignmentId) {
    return api.post('/admin/prefixes/my/active', { assignmentId }, { params: { playerName } })
  },

  getPrefixInfo(prefixId) {
    return api.get(`/admin/prefixes/public/${prefixId}`)
  },

  getPrefixOwners(prefixId, params = {}) {
    return api.get(`/admin/prefixes/public/${prefixId}/owners`, { params })
  }
}

export const chatApi = {
  getMessages(count = 50) {
    return api.get('/chat/messages', { params: { count } })
  },

  send(name, message) {
    return api.post('/chat/send', { name, message })
  }
}

export default api
