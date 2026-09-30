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
    <!-- SPDNet: 区块用标签页承载。四个区块纵向堆叠时，前缀管理随前缀数量持续增行，
         整页会变得极长；标签页一次只渲染一块。 -->
    <div class="admin-content">
      <el-tabs v-model="activeTab" class="admin-tabs">
        <el-tab-pane name="players">
          <template #label>
            <span class="tab-label"><el-icon><UserFilled /></el-icon>玩家管理</span>
          </template>

      <!-- Players Section -->
      <div class="content-section">
        <div class="section-header-bar">
          <div class="header-title">
            <div class="title-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="title-content">
              <h2>玩家管理</h2>
              <span class="player-count">{{ totalPlayers }} 位玩家</span>
            </div>
          </div>
          <div class="header-filters">
            <el-select
              v-model="roleFilter"
              placeholder="全部角色"
              clearable
              style="width: 130px"
              @change="handleRoleFilterChange"
            >
              <el-option label="管理员" value="ADMIN" />
              <!-- SPDNet: 后端 UserRole 枚举常量名是 USER，不是 PLAYER。
                   此前写 'PLAYER' 会让后端 UserRole.valueOf('PLAYER') 抛异常，
                   选「玩家」必然筛选失败并弹「加载数据失败」。 -->
              <el-option label="玩家" value="USER" />
              <el-option label="已封禁" value="BANNED" />
            </el-select>
            <el-input
              v-model="searchQuery"
              placeholder="搜索玩家..."
              clearable
              class="search-input"
              @input="handleSearch"
              @clear="handleSearch"
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
            row-class-name="table-row"
          >
            <el-table-column label="玩家" min-width="180">
              <template #default="{ row }">
                <div class="player-cell">
                  <div class="avatar-wrapper">
                    <el-avatar :size="40" :icon="UserFilled" class="player-avatar" aria-hidden="true" />
                    <div :class="['online-indicator', row.online ? 'online' : 'offline']"></div>
                  </div>
                  <div class="player-info">
                    <span class="player-name">
                      <PrefixBadge v-if="row.prefix" :prefix="row.prefix" />
                      {{ row.name }}
                    </span>
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
                  <el-button
                    type="primary"
                    text
                    class="action-menu-btn"
                    :aria-label="`管理玩家 ${row.name}`"
                  >
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

          <!-- SPDNet: 空状态提示，避免搜索无结果时只剩表头 -->
          <el-empty
            v-if="!loading && players.length === 0"
            :description="searchQuery || roleFilter ? '没有匹配的玩家' : '暂无玩家数据'"
          />

          <!-- SPDNet: 服务端分页 -->
          <div class="pagination-bar" v-if="totalPlayers > 0">
            <el-pagination
              :current-page="currentPage"
              :page-size="pageSize"
              :total="totalPlayers"
              :page-sizes="[20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @current-change="handlePageChange"
              @size-change="handlePageSizeChange"
            />
          </div>
        </div>
      </div>
        </el-tab-pane>

        <el-tab-pane name="broadcast">
          <template #label>
            <span class="tab-label"><el-icon><Bell /></el-icon>广播消息</span>
          </template>

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
        </el-tab-pane>

        <el-tab-pane name="system">
          <template #label>
            <span class="tab-label"><el-icon><Monitor /></el-icon>系统信息</span>
          </template>

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
        </el-tab-pane>

        <el-tab-pane name="prefix">
          <template #label>
            <span class="tab-label"><el-icon><Medal /></el-icon>前缀管理</span>
          </template>

          <!-- Prefix Management Section -->
          <!-- SPDNet: 不传 players。本页的 players 只是当前分页的 20 条，
               传下去会让下拉只能选到当前页玩家；组件内部自行走服务端搜索。 -->
          <PlayerPrefix />
        </el-tab-pane>
      </el-tabs>
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
import PlayerPrefix from '../components/PlayerPrefix.vue'
import PrefixBadge from '../components/PrefixBadge.vue'
import {
  getRoleType, getRoleDisplay, isAdminUser, formatShortDateTime
} from '../utils/format'

const router = useRouter()
const loading = ref(false)
const players = ref([])
const serverInfo = ref({})
const searchQuery = ref('')
const broadcastMessage = ref('')
const broadcasting = ref(false)

// SPDNet: 当前标签页。区块多且长，改为一次只渲染一块，避免页面过长。
const activeTab = ref('players')

// SPDNet: 服务端分页 + 筛选状态
const currentPage = ref(1)
const pageSize = ref(20)
const totalPlayers = ref(0)
const roleFilter = ref(null)
let loadSeq = 0
let searchTimer = null

const stats = computed(() => [
  {
    label: '总玩家数',
    value: serverInfo.value?.totalPlayers || 0,
    icon: UserFilled,
    gradient: 'var(--gradient-violet)'
  },
  {
    label: '在线玩家',
    value: serverInfo.value?.onlineCount || 0,
    icon: TrendCharts,
    gradient: 'var(--gradient-success)'
  },
  {
    label: '管理员数',
    value: serverInfo.value?.adminCount || 0,
    icon: Medal,
    gradient: 'var(--gradient-rose)'
  },
  {
    label: '封禁数',
    value: serverInfo.value?.bannedCount || 0,
    icon: Lock,
    gradient: 'var(--gradient-warning)'
  }
])

// SPDNet: 搜索与角色筛选已改为服务端执行，此处不再本地过滤，
// 否则在分页数据上过滤会得到"当前页内匹配"的错误结果。
const filteredPlayers = computed(() => players.value)

// SPDNet: getRoleDisplay / formatShortDateTime 统一由 utils/format.js 提供，
// 原先 Admin.vue 自己维护了一份角色映射表，导致 BANNED -> '已封禁' 的语义漂移。
const formatDate = (dateStr) => formatShortDateTime(dateStr)

const loadData = async () => {
  loading.value = true
  const seq = ++loadSeq
  try {
    // SPDNet: 改为服务端分页。
    // 原先每次调用 getAllPlayers()（size=1000）全量拉取，且每次增删改后又重新全量拉取。
    const [playersRes, infoRes] = await Promise.all([
      adminApi.getPlayers(currentPage.value - 1, pageSize.value, roleFilter.value, searchQuery.value || null),
      playerApi.getServerInfo()
    ])

    if (seq !== loadSeq) return

    if (playersRes.data.success) {
      // 后端返回的数据结构是 { players: [...], totalElements: ..., totalPages: ... }
      const data = playersRes.data.data || {}
      players.value = data.players || []
      totalPlayers.value = data.totalElements ?? players.value.length
    }
    if (infoRes.data.success) {
      serverInfo.value = infoRes.data.data
    }
  } catch (error) {
    if (seq !== loadSeq) return
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    if (seq === loadSeq) loading.value = false
  }
}

// SPDNet: 搜索走服务端，输入防抖，避免每次按键都打一次全量查询
const handleSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    loadData()
  }, 300)
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

const handleRoleFilterChange = () => {
  currentPage.value = 1
  loadData()
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

// SPDNet: 管理操作的统一收口。
// 原先 6 个分支各自复制约 15 行 confirm + try/catch，且失败时用的是
// res.data.message（后端未给 message 时弹出空白提示），此处统一兜底。
const runPlayerAction = async ({ command, player, message, title, type = 'warning', confirmText = '确定', successText, fallbackError, requireText = null }) => {
  try {
    await ElMessageBox.confirm(message, title, {
      confirmButtonText: confirmText,
      cancelButtonText: '取消',
      type,
      // SPDNet: 不可恢复的操作要求输入确认文本
      ...(requireText
        ? {
            inputPlaceholder: requireText,
            inputValidator: (value) => value === requireText || '输入不一致，操作已取消'
          }
        : {})
    })
  } catch {
    // 用户取消
    return
  }

  try {
    const res = await command()
    if (res.data.success) {
      ElMessage.success(successText)
      loadData()
    } else {
      ElMessage.error(res.data.message || fallbackError)
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || fallbackError)
  }
}

const handlePlayerAction = (command, player) => {
  switch (command) {
    case 'view':
      router.push(`/player/${player.name}`)
      break
    case 'setAdmin':
      runPlayerAction({
        player,
        message: `确定要将 "${player.name}" 设为管理员吗？`,
        title: '确认操作',
        successText: '设置成功',
        fallbackError: '操作失败',
        command: () => adminApi.setPlayerRole(player.id, 'ADMIN')
      })
      break
    case 'setPlayer':
      runPlayerAction({
        player,
        message: `确定要将 "${player.name}" 设为普通玩家吗？`,
        title: '确认操作',
        successText: '设置成功',
        fallbackError: '操作失败',
        command: () => adminApi.setPlayerRole(player.id, 'PLAYER')
      })
      break
    case 'ban':
      runPlayerAction({
        player,
        message: `确定要封禁玩家 "${player.name}" 吗？`,
        title: '确认封禁',
        type: 'danger',
        confirmText: '确定封禁',
        successText: '封禁成功',
        fallbackError: '封禁失败',
        command: () => adminApi.setPlayerRole(player.id, 'BANNED')
      })
      break
    case 'unban':
      runPlayerAction({
        player,
        message: `确定要解封玩家 "${player.name}" 吗？`,
        title: '确认解封',
        confirmText: '确定解封',
        successText: '解封成功',
        fallbackError: '解封失败',
        command: () => adminApi.setPlayerRole(player.id, 'PLAYER')
      })
      break
    case 'kick':
      runPlayerAction({
        player,
        message: `确定要踢出玩家 "${player.name}" 吗？`,
        title: '确认踢出',
        confirmText: '确定踢出',
        successText: '踢出成功',
        fallbackError: '踢出失败',
        command: () => adminApi.kick(player.name)
      })
      break
    case 'delete':
      runPlayerAction({
        player,
        // SPDNet: 删除不可恢复，要求输入玩家名确认，避免误点
        message: `此操作不可恢复！请输入玩家名 "${player.name}" 以确认删除。`,
        title: '危险操作',
        type: 'error',
        confirmText: '确定删除',
        successText: '删除成功',
        fallbackError: '删除失败',
        requireText: player.name,
        command: () => adminApi.deletePlayer(player.id)
      })
      break
  }
}

onMounted(() => {
  // SPDNet: 与路由守卫共用同一判定（原先只比对中文'管理员'，
  // 与 router/index.js 兼容 'ADMIN'||'管理员' 的口径不一致）
  if (!isAdminUser(authStore.user)) {
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
  background: var(--gradient-violet);
  border: none;
  color: white;
}

.refresh-btn:hover {
  background: var(--gradient-primary);
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.4);
  color: white;
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
  /* SPDNet: 数字等宽对齐，避免刷新时位数变化导致宽度抖动 */
  font-variant-numeric: tabular-nums;
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

/* SPDNet: 标签页样式，对齐设计 token */
.admin-tabs :deep(.el-tabs__header) {
  margin-bottom: var(--space-6);
}

.admin-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: var(--border-subtle);
}

.admin-tabs :deep(.el-tabs__item) {
  color: var(--text-secondary);
  font-size: 0.9375rem;
  height: 46px;
}

.admin-tabs :deep(.el-tabs__item.is-active),
.admin-tabs :deep(.el-tabs__item:hover) {
  color: var(--primary-400);
}

.admin-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--primary-500);
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
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
  /* SPDNet: 原无 flex-wrap，480-768px 之间筛选项会溢出区块 */
  flex-wrap: wrap;
}

.search-input {
  width: 240px;
}

/* SPDNet: 分页条 */
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  /* SPDNet: 与 .table-container 的横向内边距对齐，避免分页器比表格右缘多缩进 16px */
  padding: var(--space-4) var(--space-2);
  border-top: 1px solid var(--border-subtle);
  flex-wrap: wrap;
  gap: var(--space-2);
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
  /* SPDNet: 表格各列固定宽合计约 870px，低于此宽度必须允许横向滚动 ——
     容器的 .content-section 是 overflow:hidden，溢出会静默裁掉「操作」列
     （封禁/删除按钮），用户点不到也不会有任何提示。 */
  padding: var(--space-2) var(--space-2) 0;
}

@media (max-width: 1000px) {
  .table-container {
    overflow-x: auto;
  }

  .table-container :deep(.el-table) {
    min-width: 870px;
  }
}

.table-container :deep(.el-table) {
  background: transparent;
  --el-table-border-color: rgba(139, 92, 246, 0.1);
}

.table-container :deep(.el-table__header-wrapper) {
  background: var(--surface-2);
}

/* SPDNet: 表头样式走 scoped CSS + 设计 token。
   不要改回 :header-cell-style 传 JS 对象：内联样式会盖掉 style.css 里
   设计系统自己的 .el-table th 规则，导致表头颜色脱离调色板。 */
.table-container :deep(.el-table th.el-table__cell) {
  background: var(--surface-2);
  color: var(--primary-300);
  font-weight: 600;
  font-size: 0.875rem;
  border-bottom: 1px solid var(--border-strong);
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
  background: var(--gradient-violet);
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
  /* SPDNet: 管理员要照着念的编号，用 secondary 保证足够醒目。
     注：--text-tertiary 已整体抬到达标值，此处是主动选择更强的一档，非缺陷修复。 */
  color: var(--text-secondary);
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
  color: white;
  background: rgba(139, 92, 246, 0.3);
}

.action-menu-btn:hover {
  color: white;
  background: rgba(139, 92, 246, 0.5);
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
  /* SPDNet: 数字等宽对齐，避免轮询刷新时位数变化导致宽度抖动 */
  font-variant-numeric: tabular-nums;
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
