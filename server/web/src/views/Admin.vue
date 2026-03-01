<template>
  <div class="admin-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="32"><Setting /></el-icon>
          <div class="icon-glow"></div>
        </div>
        <div class="header-text">
          <h1>管理后台</h1>
          <p>系统管理与监控中心</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Refresh" @click="loadData" :loading="loading" class="refresh-btn">
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card" v-for="(stat, index) in stats" :key="index">
          <div class="stat-glow"></div>
          <div class="stat-icon" :style="{ background: stat.gradient }">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-decoration"></div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="admin-content">
      <!-- Broadcast Section -->
      <div class="content-section">
        <div class="section-header-bar">
          <div class="header-title">
            <div class="title-icon broadcast">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="title-content">
              <h2>广播消息</h2>
              <span class="section-desc">向所有在线玩家发送消息</span>
            </div>
          </div>
        </div>

        <div class="broadcast-form">
          <el-input
            v-model="broadcastMessage"
            type="textarea"
            :rows="3"
            placeholder="输入广播消息内容..."
            maxlength="200"
            show-word-limit
          />
          <div class="broadcast-actions">
            <el-button
              type="primary"
              :icon="Promotion"
              :loading="broadcasting"
              :disabled="!broadcastMessage.trim()"
              @click="handleBroadcast"
            >
              发送广播
            </el-button>
          </div>
        </div>
      </div>

      <!-- Players Section -->
      <div class="content-section">
        <div class="section-header-bar">
          <div class="header-title">
            <div class="title-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="title-content">
              <h2>玩家管理</h2>
              <span class="player-count">{{ filteredPlayers.length }} 位玩家</span>
            </div>
          </div>
          <div class="header-filters">
            <el-input
              v-model="searchQuery"
              placeholder="搜索玩家..."
              clearable
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>

        <div class="table-container" v-loading="loading">
          <el-table
            :data="filteredPlayers"
            style="width: 100%"
            :header-cell-style="headerStyle"
            row-class-name="table-row"
          >
            <el-table-column label="玩家" min-width="180">
              <template #default="{ row }">
                <div class="player-cell">
                  <div class="avatar-wrapper">
                    <el-avatar :size="40" :icon="UserFilled" class="player-avatar" />
                    <div :class="['online-indicator', row.online ? 'online' : 'offline']"></div>
                  </div>
                  <div class="player-info">
                    <span class="player-name">{{ row.name }}</span>
                    <span class="player-id">ID: {{ row.id }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="角色" width="120" align="center">
              <template #default="{ row }">
                <el-tag
                  :type="getRoleType(row.role)"
                  effect="dark"
                  round
                  size="small"
                  class="role-tag"
                >
                  <el-icon v-if="row.role === 'ADMIN'"><StarFilled /></el-icon>
                  <el-icon v-else><User /></el-icon>
                  {{ getRoleDisplay(row.role) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <div class="status-cell">
                  <span :class="['status-dot', row.online ? 'online' : 'offline']"></span>
                  <span :class="['status-text', row.online ? 'online' : 'offline']">
                    {{ row.online ? '在线' : '离线' }}
                  </span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="注册时间" width="160">
              <template #default="{ row }">
                <div class="time-cell">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ formatDate(row.createdAt) }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="最后登录" width="160">
              <template #default="{ row }">
                <div class="time-cell">
                  <el-icon><Timer /></el-icon>
                  <span>{{ row.lastLoginAt ? formatDate(row.lastLoginAt) : '从未' }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="150" align="center">
              <template #default="{ row }">
                <el-dropdown @command="(cmd) => handlePlayerAction(cmd, row)" trigger="click">
                  <el-button type="primary" text class="action-menu-btn">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu class="custom-dropdown">
                      <el-dropdown-item command="view">
                        <el-icon><View /></el-icon>
                        <span>查看主页</span>
                      </el-dropdown-item>
                      <el-dropdown-item command="setAdmin" v-if="row.role !== 'ADMIN'">
                        <el-icon><StarFilled /></el-icon>
                        <span>设为管理员</span>
                      </el-dropdown-item>
                      <el-dropdown-item command="setPlayer" v-if="row.role === 'ADMIN'">
                        <el-icon><User /></el-icon>
                        <span>设为普通玩家</span>
                      </el-dropdown-item>
                      <el-dropdown-item command="ban" v-if="row.role !== 'BANNED'" divided>
                        <el-icon><Lock /></el-icon>
                        <span>封禁账号</span>
                      </el-dropdown-item>
                      <el-dropdown-item command="unban" v-if="row.role === 'BANNED'" divided>
                        <el-icon><Unlock /></el-icon>
                        <span>解封账号</span>
                      </el-dropdown-item>
                      <el-dropdown-item command="kick" divided>
                        <el-icon><CircleClose /></el-icon>
                        <span>踢出游戏</span>
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" class="danger-item">
                        <el-icon><Delete /></el-icon>
                        <span>删除账号</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- System Info -->
      <div class="content-section">
        <div class="section-header-bar">
          <div class="header-title">
            <div class="title-icon system">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="title-content">
              <h2>系统信息</h2>
              <span class="system-status">服务器运行正常</span>
            </div>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-card">
            <div class="card-shine"></div>
            <div class="info-icon purple">
              <el-icon><CollectionTag /></el-icon>
            </div>
            <div class="info-content">
              <span class="info-label">游戏版本</span>
              <span class="info-value">{{ serverInfo?.version || '-' }}</span>
            </div>
          </div>

          <div class="info-card">
            <div class="card-shine"></div>
            <div class="info-icon cyan">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="info-content">
              <span class="info-label">联机版本</span>
              <span class="info-value">{{ serverInfo?.netVersion || '-' }}</span>
            </div>
          </div>

          <div class="info-card">
            <div class="card-shine"></div>
            <div class="info-icon emerald">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="info-content">
              <span class="info-label">在线玩家</span>
              <span class="info-value highlight">{{ serverInfo?.onlineCount || 0 }}</span>
            </div>
          </div>

          <div class="info-card">
            <div class="card-shine"></div>
            <div class="info-icon amber">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="info-content">
              <span class="info-label">总注册数</span>
              <span class="info-value">{{ serverInfo?.totalPlayers || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, UserFilled, Refresh, Search, MoreFilled, View, Delete,
  Monitor, CollectionTag, Connection, DataAnalysis, StarFilled, User,
  TrendCharts, Medal, Calendar, Timer, Bell, Promotion,
  Lock, Unlock, CircleClose
} from '@element-plus/icons-vue'
import { playerApi, adminApi } from '../api'
import { authStore } from '../store/auth'

const router = useRouter()
const loading = ref(false)
const players = ref([])
const serverInfo = ref({})
const searchQuery = ref('')
const broadcastMessage = ref('')
const broadcasting = ref(false)

const stats = computed(() => [
  {
    label: '总玩家数',
    value: serverInfo.value?.totalPlayers || 0,
    icon: 'UserFilled',
    gradient: 'linear-gradient(135deg, #8b5cf6 0%, #a855f7 100%)'
  },
  {
    label: '在线玩家',
    value: serverInfo.value?.onlineCount || 0,
    icon: 'TrendCharts',
    gradient: 'linear-gradient(135deg, #10b981 0%, #34d399 100%)'
  },
  {
    label: '管理员数',
    value: serverInfo.value?.adminCount || 0,
    icon: 'Medal',
    gradient: 'linear-gradient(135deg, #ec4899 0%, #f43f5e 100%)'
  },
  {
    label: '封禁数',
    value: serverInfo.value?.bannedCount || 0,
    icon: 'Lock',
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #f97316 100%)'
  }
])

const filteredPlayers = computed(() => {
  if (!searchQuery.value) return players.value
  const query = searchQuery.value.toLowerCase()
  return players.value.filter(p =>
    p.name?.toLowerCase().includes(query)
  )
})

const headerStyle = () => ({
  background: 'rgba(20, 20, 35, 0.8)',
  color: '#a78bfa',
  fontWeight: 600,
  fontSize: '0.875rem',
  borderBottom: '1px solid rgba(139, 92, 246, 0.2)'
})

const getRoleType = (role) => {
  const types = {
    'ADMIN': 'danger',
    'PLAYER': 'primary',
    'BANNED': 'info'
  }
  return types[role] || 'primary'
}

const getRoleDisplay = (role) => {
  const displays = {
    'ADMIN': '管理员',
    'PLAYER': '玩家',
    'BANNED': '已封禁'
  }
  return displays[role] || role
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadData = async () => {
  loading.value = true
  try {
    const [playersRes, infoRes] = await Promise.all([
      adminApi.getAllPlayers(),
      playerApi.getServerInfo()
    ])

    if (playersRes.data.success) {
      // 后端返回的数据结构是 { players: [...], totalElements: ..., totalPages: ... }
      players.value = playersRes.data.data?.players || []
    }
    if (infoRes.data.success) {
      serverInfo.value = infoRes.data.data
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleBroadcast = async () => {
  if (!broadcastMessage.value.trim()) return

  broadcasting.value = true
  try {
    const res = await adminApi.broadcast(broadcastMessage.value)
    if (res.data.success) {
      ElMessage.success('广播发送成功')
      broadcastMessage.value = ''
    } else {
      ElMessage.error(res.data.message || '发送失败')
    }
  } catch (error) {
    console.error('广播发送失败:', error)
    ElMessage.error('广播发送失败')
  } finally {
    broadcasting.value = false
  }
}

const handlePlayerAction = (command, player) => {
  switch (command) {
    case 'view':
      router.push(`/player/${player.name}`)
      break
    case 'setAdmin':
      ElMessageBox.confirm(
        `确定要将 "${player.name}" 设为管理员吗？`,
        '确认操作',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          const res = await adminApi.setPlayerRole(player.id, 'ADMIN')
          if (res.data.success) {
            ElMessage.success('设置成功')
            loadData()
          } else {
            ElMessage.error(res.data.message)
          }
        } catch (error) {
          ElMessage.error(error.response?.data?.message || '操作失败')
        }
      }).catch(() => {})
      break
    case 'setPlayer':
      ElMessageBox.confirm(
        `确定要将 "${player.name}" 设为普通玩家吗？`,
        '确认操作',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          const res = await adminApi.setPlayerRole(player.id, 'PLAYER')
          if (res.data.success) {
            ElMessage.success('设置成功')
            loadData()
          } else {
            ElMessage.error(res.data.message)
          }
        } catch (error) {
          ElMessage.error(error.response?.data?.message || '操作失败')
        }
      }).catch(() => {})
      break
    case 'ban':
      ElMessageBox.confirm(
        `确定要封禁玩家 "${player.name}" 吗？`,
        '确认封禁',
        {
          confirmButtonText: '确定封禁',
          cancelButtonText: '取消',
          type: 'danger'
        }
      ).then(async () => {
        try {
          const res = await adminApi.setPlayerRole(player.id, 'BANNED')
          if (res.data.success) {
            ElMessage.success('封禁成功')
            loadData()
          } else {
            ElMessage.error(res.data.message)
          }
        } catch (error) {
          ElMessage.error(error.response?.data?.message || '封禁失败')
        }
      }).catch(() => {})
      break
    case 'unban':
      ElMessageBox.confirm(
        `确定要解封玩家 "${player.name}" 吗？`,
        '确认解封',
        {
          confirmButtonText: '确定解封',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          const res = await adminApi.setPlayerRole(player.id, 'PLAYER')
          if (res.data.success) {
            ElMessage.success('解封成功')
            loadData()
          } else {
            ElMessage.error(res.data.message)
          }
        } catch (error) {
          ElMessage.error(error.response?.data?.message || '解封失败')
        }
      }).catch(() => {})
      break
    case 'kick':
      ElMessageBox.confirm(
        `确定要踢出玩家 "${player.name}" 吗？`,
        '确认踢出',
        {
          confirmButtonText: '确定踢出',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          const res = await adminApi.kick(player.name)
          if (res.data.success) {
            ElMessage.success('踢出成功')
            loadData()
          } else {
            ElMessage.error(res.data.message)
          }
        } catch (error) {
          ElMessage.error(error.response?.data?.message || '踢出失败')
        }
      }).catch(() => {})
      break
    case 'delete':
      ElMessageBox.confirm(
        `确定要删除玩家 "${player.name}" 吗？此操作不可恢复！`,
        '警告',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'danger',
          customClass: 'custom-message-box'
        }
      ).then(async () => {
        try {
          const res = await adminApi.deletePlayer(player.id)
          if (res.data.success) {
            ElMessage.success('删除成功')
            loadData()
          } else {
            ElMessage.error(res.data.message)
          }
        } catch (error) {
          ElMessage.error(error.response?.data?.message || '删除失败')
        }
      }).catch(() => {})
      break
  }
}

onMounted(() => {
  if (authStore.user?.role !== '管理员') {
    ElMessage.error('无权访问')
    router.push('/')
    return
  }
  loadData()
})
</script>

<style scoped>
.admin-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-6) var(--content-padding);
}

/* Page Header */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.header-content {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.header-icon {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.4);
}

.icon-glow {
  position: absolute;
  inset: -4px;
  background: radial-gradient(circle, rgba(239, 68, 68, 0.3) 0%, transparent 70%);
  border-radius: var(--radius-lg);
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.05); }
}

.header-text h1 {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.header-text p {
  color: var(--text-secondary);
  margin: var(--space-1) 0 0;
}

.refresh-btn {
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  border: none;
}

.refresh-btn:hover {
  background: linear-gradient(135deg, #7c3aed, #9333ea);
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.4);
}

/* Stats Section */
.stats-section {
  margin-bottom: var(--space-6);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: rgba(139, 92, 246, 0.3);
  box-shadow: var(--shadow-glow-sm);
}

.stat-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.5), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.stat-card:hover .stat-glow {
  opacity: 1;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
  position: relative;
  z-index: 1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-top: var(--space-1);
}

.stat-decoration {
  position: absolute;
  right: -20px;
  top: -20px;
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}

/* Admin Content */
.admin-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.content-section {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
}

.section-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-6);
  background: rgba(20, 20, 35, 0.5);
  border-bottom: 1px solid var(--border-subtle);
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.title-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(168, 85, 247, 0.1));
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a78bfa;
  font-size: 1.25rem;
}

.title-icon.system {
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.2), rgba(34, 211, 238, 0.1));
  border-color: rgba(6, 182, 212, 0.3);
  color: #22d3ee;
}

.title-icon.broadcast {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2), rgba(251, 191, 36, 0.1));
  border-color: rgba(245, 158, 11, 0.3);
  color: #fbbf24;
}

.title-content h2 {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0 0 2px;
  color: var(--text-primary);
}

.player-count,
.system-status,
.section-desc {
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.header-filters {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.search-input {
  width: 240px;
}

.search-input :deep(.el-input__wrapper) {
  background: rgba(15, 15, 25, 0.6);
  border: 1px solid rgba(139, 92, 246, 0.2);
  box-shadow: none;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(139, 92, 246, 0.4);
}

/* Broadcast Form */
.broadcast-form {
  padding: var(--space-5) var(--space-6);
}

.broadcast-form :deep(.el-textarea__inner) {
  background: rgba(15, 15, 25, 0.6);
  border-color: rgba(139, 92, 246, 0.2);
  color: var(--text-primary);
}

.broadcast-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-4);
}

/* Table */
.table-container {
  padding: var(--space-2);
}

.table-container :deep(.el-table) {
  background: transparent;
  --el-table-border-color: rgba(139, 92, 246, 0.1);
}

.table-container :deep(.el-table__header-wrapper) {
  background: rgba(20, 20, 35, 0.8);
}

.table-container :deep(.el-table__body-wrapper) {
  background: transparent;
}

.table-container :deep(.el-table__row) {
  background: transparent;
  transition: all 0.2s;
}

.table-container :deep(.el-table__row:hover) {
  background: rgba(139, 92, 246, 0.05) !important;
}

.table-container :deep(.el-table__cell) {
  background: transparent;
  border-bottom: 1px solid rgba(139, 92, 246, 0.1);
  padding: var(--space-3) 0;
}

.player-cell {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.avatar-wrapper {
  position: relative;
}

.player-avatar {
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  border: 2px solid rgba(139, 92, 246, 0.3);
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid var(--surface-1);
}

.online-indicator.online {
  background: #10b981;
  box-shadow: 0 0 6px #10b981;
}

.online-indicator.offline {
  background: #6b7280;
}

.player-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.player-name {
  font-weight: 600;
  color: var(--text-primary);
}

.player-id {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.role-tag :deep(.el-icon) {
  margin-right: 4px;
}

.status-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.online {
  background: #10b981;
  box-shadow: 0 0 8px #10b981;
  animation: pulse 2s ease-in-out infinite;
}

.status-dot.offline {
  background: #6b7280;
}

.status-text {
  font-size: 0.875rem;
}

.status-text.online {
  color: #10b981;
}

.status-text.offline {
  color: var(--text-tertiary);
}

.time-cell {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.time-cell .el-icon {
  color: var(--text-tertiary);
}

.action-menu-btn {
  color: #a78bfa;
}

.action-menu-btn:hover {
  color: #c084fc;
  background: rgba(139, 92, 246, 0.1);
}

/* Custom Dropdown */
:global(.custom-dropdown) {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

:global(.custom-dropdown .el-dropdown-menu__item) {
  color: var(--text-primary);
}

:global(.custom-dropdown .el-dropdown-menu__item:hover) {
  background: rgba(139, 92, 246, 0.1);
  color: #a78bfa;
}

:global(.custom-dropdown .danger-item) {
  color: #ef4444;
}

:global(.custom-dropdown .danger-item:hover) {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

/* Info Grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  padding: var(--space-6);
}

.info-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background: rgba(20, 20, 35, 0.5);
  border: 1px solid rgba(139, 92, 246, 0.1);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all 0.3s ease;
}

.info-card:hover {
  border-color: rgba(139, 92, 246, 0.3);
  transform: translateY(-2px);
}

.card-shine {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.3), transparent);
}

.info-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.info-icon.purple {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(168, 85, 247, 0.1));
  border: 1px solid rgba(139, 92, 246, 0.3);
  color: #a78bfa;
}

.info-icon.cyan {
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.2), rgba(34, 211, 238, 0.1));
  border: 1px solid rgba(6, 182, 212, 0.3);
  color: #22d3ee;
}

.info-icon.emerald {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(52, 211, 153, 0.1));
  border: 1px solid rgba(16, 185, 129, 0.3);
  color: #10b981;
}

.info-icon.amber {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2), rgba(251, 191, 36, 0.1));
  border: 1px solid rgba(245, 158, 11, 0.3);
  color: #fbbf24;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.info-label {
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.info-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
}

.info-value.highlight {
  color: #10b981;
}

/* Responsive */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .admin-page {
    padding: var(--space-4);
  }

  .page-header {
    flex-direction: column;
    gap: var(--space-4);
    align-items: flex-start;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .section-header-bar {
    flex-direction: column;
    gap: var(--space-4);
    align-items: flex-start;
  }

  .header-filters {
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .table-container {
    overflow-x: auto;
  }
}
</style>
