<template>
  <div class="admin">
    <div v-if="!isAdmin" class="card">
      <div class="alert alert-error">您没有权限访问此页面</div>
      <router-link to="/" class="btn">返回首页</router-link>
    </div>

    <template v-else>
      <div class="stats-grid" v-if="stats">
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalPlayers }}</div>
          <div class="stat-label">注册玩家</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.totalRecords }}</div>
          <div class="stat-label">游戏记录</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.onlineCount }}</div>
          <div class="stat-label">当前在线</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.winCount }}</div>
          <div class="stat-label">胜利次数</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.bannedCount }}</div>
          <div class="stat-label">封禁账号</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ stats.adminCount }}</div>
          <div class="stat-label">管理员</div>
        </div>
      </div>

      <div class="tabs">
        <button :class="['tab', activeTab === 'online' ? 'active' : '']" @click="activeTab = 'online'">在线玩家</button>
        <button :class="['tab', activeTab === 'players' ? 'active' : '']" @click="activeTab = 'players'">玩家管理</button>
        <button :class="['tab', activeTab === 'records' ? 'active' : '']" @click="activeTab = 'records'">记录管理</button>
        <button :class="['tab', activeTab === 'broadcast' ? 'active' : '']" @click="activeTab = 'broadcast'">广播通知</button>
      </div>

      <div class="card" v-show="activeTab === 'online'">
        <div class="card-header">
          <h3 class="card-title">在线玩家 ({{ onlinePlayers.length }})</h3>
          <button class="btn btn-sm" @click="loadOnlinePlayers">刷新</button>
        </div>

        <div v-if="onlineLoading" class="loading">加载中...</div>

        <table class="table" v-else-if="onlinePlayers.length > 0">
          <thead>
            <tr>
              <th>用户名</th>
              <th>身份</th>
              <th>状态</th>
              <th>深度</th>
              <th>挑战</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="player in onlinePlayers" :key="player.name">
              <td>
                <router-link :to="`/player/${player.name}`">{{ player.name }}</router-link>
              </td>
              <td>
                <span :class="['badge', getRoleBadgeClass(player.role)]">{{ player.role }}</span>
              </td>
              <td>{{ getStatusText(player.status) }}</td>
              <td>{{ player.status?.depth || '-' }}</td>
              <td>{{ countChallenges(player.status?.challenges) }}</td>
              <td>
                <button class="btn btn-sm btn-danger" @click="kickPlayer(player)">踢出</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-else class="loading">暂无在线玩家</div>
      </div>

      <div class="card" v-show="activeTab === 'broadcast'">
        <h3 class="card-title">发送广播通知</h3>
        <p class="hint">向当前服务器中的所有在线玩家发送消息弹窗</p>
        
        <div v-if="broadcastMessage" :class="['alert', broadcastSuccess ? 'alert-success' : 'alert-error']">
          {{ broadcastMessage }}
        </div>

        <form @submit.prevent="sendBroadcast">
          <div class="form-group">
            <label for="broadcastText">消息内容</label>
            <textarea 
              id="broadcastText" 
              v-model="broadcastText" 
              placeholder="请输入要广播的消息..."
              rows="4"
              required
            ></textarea>
          </div>
          <div class="broadcast-info">
            <span>当前在线玩家: <strong>{{ stats?.onlineCount || 0 }}</strong> 人</span>
          </div>
          <button type="submit" class="btn btn-broadcast" :disabled="broadcastLoading">
            {{ broadcastLoading ? '发送中...' : '发送广播' }}
          </button>
        </form>
      </div>

      <div class="card" v-show="activeTab === 'players'">
        <div class="card-header">
          <h3 class="card-title">玩家列表</h3>
          <div class="filters">
            <input type="text" v-model="playerSearch" placeholder="搜索玩家..." @keyup.enter="searchPlayers" />
            <select v-model="playerRoleFilter" @change="loadPlayers">
              <option value="">全部角色</option>
              <option value="USER">玩家</option>
              <option value="ADMIN">管理员</option>
              <option value="BANNED">封禁</option>
            </select>
          </div>
        </div>

        <div v-if="playersLoading" class="loading">加载中...</div>

        <table class="table" v-else-if="players.length > 0">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>身份</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="player in players" :key="player.id">
              <td>{{ player.id }}</td>
              <td>{{ player.name }}</td>
              <td>
                <span :class="['badge', getRoleBadgeClass(player.role)]">{{ player.role }}</span>
              </td>
              <td class="actions">
                <select v-model="player.role" @change="changeRole(player)" class="role-select">
                  <option value="USER">玩家</option>
                  <option value="ADMIN">管理员</option>
                  <option value="BANNED">封禁</option>
                </select>
                <button class="btn btn-sm btn-danger" @click="deletePlayer(player)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-else class="loading">暂无玩家</div>

        <div class="pagination" v-if="playerTotalPages > 1">
          <button class="btn" :disabled="playerPage === 0" @click="playerPage--; loadPlayers()">上一页</button>
          <span>第 {{ playerPage + 1 }} / {{ playerTotalPages }} 页</span>
          <button class="btn" :disabled="playerPage >= playerTotalPages - 1" @click="playerPage++; loadPlayers()">下一页</button>
        </div>
      </div>

      <div class="card" v-show="activeTab === 'records'">
        <div class="card-header">
          <h3 class="card-title">游戏记录</h3>
          <div class="filters">
            <input type="text" v-model="recordPlayerFilter" placeholder="玩家名称..." @keyup.enter="loadRecords" />
            <select v-model="recordWinFilter" @change="loadRecords">
              <option value="">全部结果</option>
              <option value="true">胜利</option>
              <option value="false">失败</option>
            </select>
          </div>
        </div>

        <div v-if="recordsLoading" class="loading">加载中...</div>

        <table class="table" v-else-if="records.length > 0">
          <thead>
            <tr>
              <th>ID</th>
              <th>玩家</th>
              <th>分数</th>
              <th>深度</th>
              <th>结果</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in records" :key="record.id">
              <td>{{ record.id }}</td>
              <td>{{ record.player_name || '未知' }}</td>
              <td>{{ record.score }}</td>
              <td>{{ record.maxDepth }}</td>
              <td>
                <span :class="['badge', record.win ? 'badge-win' : 'badge-offline']">
                  {{ record.win ? '胜利' : '失败' }}
                </span>
              </td>
              <td>
                <button class="btn btn-sm btn-danger" @click="deleteRecord(record)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-else class="loading">暂无记录</div>

        <div class="pagination" v-if="recordTotalPages > 1">
          <button class="btn" :disabled="recordPage === 0" @click="recordPage--; loadRecords()">上一页</button>
          <span>第 {{ recordPage + 1 }} / {{ recordTotalPages }} 页</span>
          <button class="btn" :disabled="recordPage >= recordTotalPages - 1" @click="recordPage++; loadRecords()">下一页</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { adminApi } from '../api'
import { authStore } from '../store/auth'

const isAdmin = computed(() => authStore.user?.role === '管理员')

const stats = ref(null)
const activeTab = ref('players')

const players = ref([])
const playersLoading = ref(false)
const playerPage = ref(0)
const playerTotalPages = ref(0)
const playerSearch = ref('')
const playerRoleFilter = ref('')

const records = ref([])
const recordsLoading = ref(false)
const recordPage = ref(0)
const recordTotalPages = ref(0)
const recordPlayerFilter = ref('')
const recordWinFilter = ref('')

const broadcastText = ref('')
const broadcastLoading = ref(false)
const broadcastMessage = ref('')
const broadcastSuccess = ref(false)

const onlinePlayers = ref([])
const onlineLoading = ref(false)

const CHALLENGE_MASKS = [128, 256, 1, 2, 4, 8, 16, 32, 64]

function countChallenges(challenges) {
  if (!challenges) return 0
  let count = 0
  for (const mask of CHALLENGE_MASKS) {
    if ((challenges & mask) !== 0) count++
  }
  return count
}

function getStatusText(status) {
  if (!status) return '大厅'
  if (status.depth > 0) return `地牢第${status.depth}层`
  return '大厅'
}

async function loadStats() {
  try {
    const res = await adminApi.getStats()
    if (res.data.success) {
      stats.value = res.data.data
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

async function loadPlayers() {
  playersLoading.value = true
  try {
    const res = await adminApi.getPlayers(playerPage.value, 20, playerRoleFilter.value, playerSearch.value || null)
    if (res.data.success) {
      players.value = res.data.data.players || []
      playerTotalPages.value = res.data.data.totalPages || 0
    }
  } catch (error) {
    console.error('加载玩家失败:', error)
  } finally {
    playersLoading.value = false
  }
}

function searchPlayers() {
  playerPage.value = 0
  loadPlayers()
}

async function changeRole(player) {
  if (!confirm(`确定要将 ${player.name} 的角色改为 ${player.role} 吗？`)) {
    loadPlayers()
    return
  }
  try {
    const res = await adminApi.setPlayerRole(player.id, player.role)
    if (res.data.success) {
      alert('修改成功')
      loadStats()
    } else {
      alert(res.data.message)
      loadPlayers()
    }
  } catch (error) {
    alert(error.response?.data?.message || '修改失败')
    loadPlayers()
  }
}

async function deletePlayer(player) {
  if (!confirm(`确定要删除玩家 ${player.name} 吗？此操作不可恢复！`)) return
  try {
    const res = await adminApi.deletePlayer(player.id)
    if (res.data.success) {
      alert('删除成功')
      loadPlayers()
      loadStats()
    } else {
      alert(res.data.message)
    }
  } catch (error) {
    alert(error.response?.data?.message || '删除失败')
  }
}

async function loadRecords() {
  recordsLoading.value = true
  try {
    const win = recordWinFilter.value === '' ? null : recordWinFilter.value === 'true'
    const res = await adminApi.getRecords(recordPage.value, 20, win, recordPlayerFilter.value || null)
    if (res.data.success) {
      records.value = res.data.data.records || []
      recordTotalPages.value = res.data.data.totalPages || 0
    }
  } catch (error) {
    console.error('加载记录失败:', error)
  } finally {
    recordsLoading.value = false
  }
}

async function deleteRecord(record) {
  if (!confirm(`确定要删除这条记录吗？此操作不可恢复！`)) return
  try {
    const res = await adminApi.deleteRecord(record.id)
    if (res.data.success) {
      alert('删除成功')
      loadRecords()
      loadStats()
    } else {
      alert(res.data.message)
    }
  } catch (error) {
    alert(error.response?.data?.message || '删除失败')
  }
}

function getRoleBadgeClass(role) {
  switch (role) {
    case 'ADMIN': return 'badge-win'
    case 'BANNED': return 'badge-offline'
    default: return 'badge-online'
  }
}

async function loadOnlinePlayers() {
  onlineLoading.value = true
  try {
    const res = await adminApi.getOnline()
    if (res.data.success) {
      onlinePlayers.value = res.data.data || []
    }
  } catch (error) {
    console.error('加载在线玩家失败:', error)
  } finally {
    onlineLoading.value = false
  }
}

async function kickPlayer(player) {
  if (!confirm(`确定要踢出玩家 ${player.name} 吗？`)) return
  try {
    const res = await adminApi.kick(player.name)
    if (res.data.success) {
      alert('踢出成功')
      loadOnlinePlayers()
      loadStats()
    } else {
      alert(res.data.message)
    }
  } catch (error) {
    alert(error.response?.data?.message || '踢出失败')
  }
}

async function sendBroadcast() {
  if (!broadcastText.value.trim()) {
    broadcastSuccess.value = false
    broadcastMessage.value = '请输入消息内容'
    return
  }

  broadcastLoading.value = true
  broadcastMessage.value = ''

  try {
    const res = await adminApi.broadcast(broadcastText.value)
    if (res.data.success) {
      broadcastSuccess.value = true
      broadcastMessage.value = '广播发送成功！'
      broadcastText.value = ''
    } else {
      broadcastSuccess.value = false
      broadcastMessage.value = res.data.message
    }
  } catch (error) {
    broadcastSuccess.value = false
    broadcastMessage.value = error.response?.data?.message || '发送失败'
  } finally {
    broadcastLoading.value = false
  }
}

onMounted(() => {
  if (isAdmin.value) {
    loadStats()
    loadOnlinePlayers()
    loadPlayers()
    loadRecords()
  }
})
</script>

<style scoped>
.admin {
  max-width: 1200px;
  margin: 0 auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tab {
  padding: 0.75rem 1.5rem;
  border: none;
  background-color: var(--bg-color);
  color: var(--text-color);
  cursor: pointer;
  border-radius: 4px 4px 0 0;
  transition: background-color 0.2s;
}

.tab.active {
  background-color: var(--primary-color);
  color: white;
}

.tab:hover:not(.active) {
  background-color: var(--border-color);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.filters {
  display: flex;
  gap: 0.5rem;
}

.filters input,
.filters select {
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-color);
  color: var(--text-color);
}

.actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.role-select {
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-color);
  color: var(--text-color);
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}

.btn-danger {
  background-color: #dc3545;
  border-color: #dc3545;
}

.btn-danger:hover {
  background-color: #c82333;
  border-color: #bd2130;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.hint {
  color: var(--text-secondary);
  margin-bottom: 1rem;
}

textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-color);
  color: var(--text-color);
  font-family: inherit;
  font-size: 1rem;
  resize: vertical;
}

textarea:focus {
  outline: none;
  border-color: var(--primary-color);
}

.broadcast-info {
  margin: 1rem 0;
  padding: 0.75rem;
  background-color: var(--bg-color);
  border-radius: 4px;
  color: var(--text-secondary);
}

.broadcast-info strong {
  color: var(--primary-color);
}

.btn-broadcast {
  background-color: #17a2b8;
  border-color: #17a2b8;
}

.btn-broadcast:hover {
  background-color: #138496;
  border-color: #117a8b;
}
</style>
