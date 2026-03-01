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
