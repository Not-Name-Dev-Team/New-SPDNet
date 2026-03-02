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

  // SPDNet: 前缀系统管理接口（管理员）
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

  // 获取玩家的前缀列表
  getPlayerPrefixes(playerName) {
    return api.get(`/admin/prefixes/player/${encodeURIComponent(playerName)}`)
  },

  // 给玩家分配前缀
  assignPrefixToPlayer(playerName, prefixId) {
    return api.post(`/admin/prefixes/player/${encodeURIComponent(playerName)}/assign`, { prefixId })
  },

  // 移除玩家的前缀
  removePrefixFromPlayer(playerName, prefixId) {
    return api.delete(`/admin/prefixes/player/${encodeURIComponent(playerName)}/remove`, { params: { prefixId } })
  }
}

// SPDNet: 前缀系统接口（玩家）
export const prefixApi = {
  // 获取我的所有前缀
  getMyPrefixes(playerName) {
    return api.get('/admin/prefixes/my', { params: { playerName } })
  },

  // 获取当前激活的前缀
  getMyActivePrefix(playerName) {
    return api.get('/admin/prefixes/my/active', { params: { playerName } })
  },

  // 设置激活的前缀
  setMyActivePrefix(playerName, assignmentId) {
    return api.post('/admin/prefixes/my/active', { assignmentId }, { params: { playerName } })
  },

  // SPDNet: 获取前缀详细信息（公开）
  getPrefixInfo(prefixId) {
    return api.get(`/admin/prefixes/public/${prefixId}`)
  },

  // SPDNet: 获取前缀的拥有者列表（公开）
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
