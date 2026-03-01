<template>
  <div class="admin-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="32"><Setting /></el-icon>
        </div>
        <div class="header-text">
          <h1>管理后台</h1>
          <p>系统管理与监控</p>
        </div>
      </div>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card" v-for="(stat, index) in stats" :key="index">
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <el-icon :size="24" color="white"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="admin-content">
      <!-- Players Section -->
      <div class="content-section">
        <div class="section-header">
          <div class="header-title">
            <el-icon :size="20"><UserFilled /></el-icon>
            <h2>玩家管理</h2>
          </div>
          <div class="header-actions">
            <el-input
              v-model="searchQuery"
              placeholder="搜索玩家..."
              clearable
              style="width: 200px"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" :icon="Refresh" @click="loadData" :loading="loading">
              刷新
            </el-button>
          </div>
        </div>

        <div class="table-wrapper" v-loading="loading">
          <el-table :data="filteredPlayers" stripe style="width: 100%">
            <el-table-column label="玩家" min-width="150">
              <template #default="{ row }">
                <div class="player-cell">
                  <el-avatar :size="32" :icon="UserFilled" class="player-avatar" />
                  <div class="player-info">
                    <span class="player-name">{{ row.name }}</span>
                    <span class="player-email">{{ row.email }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="角色" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.role === '管理员' ? 'danger' : 'info'" effect="light" round size="small">
                  {{ row.role || '玩家' }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <div class="status-cell">
                  <span :class="['status-dot', row.online ? 'online' : 'offline']"></span>
                  <span>{{ row.online ? '在线' : '离线' }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="注册时间" width="160">
              <template #default="{ row }">
                <span class="time-text">{{ formatDate(row.createdAt) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="最后登录" width="160">
              <template #default="{ row }">
                <span class="time-text">{{ row.lastLoginAt ? formatDate(row.lastLoginAt) : '从未' }}</span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-dropdown @command="(cmd) => handlePlayerAction(cmd, row)" trigger="click">
                  <el-button type="primary" text :icon="More" circle />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="view">
                        <el-icon><View /></el-icon>
                        <span>查看主页</span>
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" divided>
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
        <div class="section-header">
          <div class="header-title">
            <el-icon :size="20"><Monitor /></el-icon>
            <h2>系统信息</h2>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-card">
            <div class="info-icon">
              <el-icon><CollectionTag /></el-icon>
            </div>
            <div class="info-content">
              <span class="info-label">游戏版本</span>
              <span class="info-value">{{ serverInfo?.version || '-' }}</span>
            </div>
          </div>

          <div class="info-card">
            <div class="info-icon">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="info-content">
              <span class="info-label">联机版本</span>
              <span class="info-value">{{ serverInfo?.netVersion || '-' }}</span>
            </div>
          </div>

          <div class="info-card">
            <div class="info-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="info-content">
              <span class="info-label">在线玩家</span>
              <span class="info-value">{{ serverInfo?.onlineCount || 0 }}</span>
            </div>
          </div>

          <div class="info-card">
            <div class="info-icon">
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
  Setting, UserFilled, Refresh, Search, More, View, Delete,
  Monitor, CollectionTag, Connection, DataAnalysis,
  TrendCharts, FirstAidKit, OfficeBuilding, Medal
} from '@element-plus/icons-vue'
import { playerApi, adminApi } from '../api'
import { authStore } from '../store/auth'

const router = useRouter()
const loading = ref(false)
const players = ref([])
const serverInfo = ref({})
const searchQuery = ref('')

const stats = computed(() => [
  {
    label: '总玩家数',
    value: serverInfo.value?.totalPlayers || 0,
    icon: 'UserFilled',
    gradient: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)'
  },
  {
    label: '在线玩家',
    value: serverInfo.value?.onlineCount || 0,
    icon: 'TrendCharts',
    gradient: 'linear-gradient(135deg, #10b981 0%, #06b6d4 100%)'
  },
  {
    label: '今日新增',
    value: 0,
    icon: 'FirstAidKit',
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #f97316 100%)'
  },
  {
    label: '管理员数',
    value: players.value.filter(p => p.role === '管理员').length,
    icon: 'Medal',
    gradient: 'linear-gradient(135deg, #ec4899 0%, #f43f5e 100%)'
  }
])

const filteredPlayers = computed(() => {
  if (!searchQuery.value) return players.value
  const query = searchQuery.value.toLowerCase()
  return players.value.filter(p =>
    p.name?.toLowerCase().includes(query) ||
    p.email?.toLowerCase().includes(query)
  )
})

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
      players.value = playersRes.data.data || []
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

const handlePlayerAction = (command, player) => {
  switch (command) {
    case 'view':
      router.push(`/player/${player.name}`)
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
          const res = await adminApi.deletePlayer(player.name)
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
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-8) var(--content-padding);
}

/* Page Header */
.page-header {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-6);
}

.header-content {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.header-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, #f43f5e 0%, #e11d48 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
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

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-8);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: var(--border-default);
  box-shadow: var(--shadow-lg);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
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

/* Admin Content */
.admin-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.content-section {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5);
  border-bottom: 1px solid var(--border-subtle);
  background: var(--surface-2);
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.header-title h2 {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.header-title .el-icon {
  color: var(--primary-400);
}

.header-actions {
  display: flex;
  gap: var(--space-3);
}

/* Table */
.table-wrapper {
  padding: var(--space-5);
}

.player-cell {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.player-avatar {
  background: var(--gradient-primary);
}

.player-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.player-name {
  font-weight: 600;
  color: var(--text-primary);
}

.player-email {
  font-size: 0.8125rem;
  color: var(--text-tertiary);
}

.status-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  font-size: 0.875rem;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
}

.status-dot.online {
  background: var(--accent-emerald);
  box-shadow: 0 0 8px var(--accent-emerald);
}

.status-dot.offline {
  background: var(--text-muted);
}

.time-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

/* Info Grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  padding: var(--space-5);
}

.info-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--surface-2);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}

.info-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  background: var(--surface-3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-400);
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
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
}

/* Responsive */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .section-header {
    flex-direction: column;
    gap: var(--space-3);
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions .el-input {
    flex: 1;
  }
}

@media (max-width: 640px) {
  .admin-page {
    padding: var(--space-4);
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .table-wrapper {
    padding: var(--space-3);
    overflow-x: auto;
  }
}
</style>
