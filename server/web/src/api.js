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

  login(data) {
    return api.post('/login', data)
  },

  getOnline() {
    return api.get('/online')
  },

  getAllPlayers() {
    return api.get('/players')
  },

  getPlayerInfo(name) {
    return api.get(`/player/${name}`)
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
  }
}

export const leaderboardApi = {
  getLeaderboard(page = 0, size = 20, winOnly = false) {
    return api.get('/leaderboard', {
      params: { page, size, winOnly }
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
  }
}

export default api
