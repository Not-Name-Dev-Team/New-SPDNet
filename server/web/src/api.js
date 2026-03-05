import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

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

  getAllPlayers() {
    return api.get('/players')
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
  },

  getChatHistory(count = 50) {
    return api.get('/chat/messages', { params: { count } })
  },

  sendMessage(name, message) {
    return api.post('/chat/send', { name, message })
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

  getAllPlayers() {
    return api.get('/admin/players', {
      params: { page: 0, size: 1000 }
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
