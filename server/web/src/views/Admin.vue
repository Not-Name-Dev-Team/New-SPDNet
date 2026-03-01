<template>
  <div class="admin-page">
    <el-result
      v-if="!isAdmin"
      icon="warning"
      title="权限不足"
      sub-title="您没有权限访问此页面"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>

    <template v-else>
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :xs="12" :sm="8" :md="4" v-for="(stat, index) in statsList" :key="index">
          <el-card class="stat-card" shadow="hover" :body-style="{ padding: '20px' }">
            <div class="stat-content">
              <el-icon :size="32" :color="stat.color"><component :is="stat.icon" /></el-icon>
              <div class="stat-info">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 管理标签页 -->
      <el-card class="admin-tabs-card" shadow="hover">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- 在线玩家 -->
          <el-tab-pane label="在线玩家" name="online">
            <div class="tab-header">
              <h3>在线玩家 ({{ onlinePlayers.length }})</h3>
              <el-button type="primary" :icon="Refresh" @click="loadOnlinePlayers" :loading="onlineLoading">
                刷新
              </el-button>
            </div>
            <el-table :data="onlinePlayers" v-loading="onlineLoading" stripe>
              <el-table-column prop="name" label="用户名" min-width="120">
                <template #default="{ row }">
                  <router-link :to="`/player/${row.name}`" class="player-link">{{ row.name }}</router-link>
                </template>
              </el-table-column>
              <el-table-column label="身份" width="100">
                <template #default="{ row }">
                  <el-tag :type="getRoleType(row.role)" size="small">{{ row.role }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <span>{{ getStatusText(row.status) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="深度" width="80" align="center">
                <template #default="{ row }">
                  <span v-if="row.status?.depth">{{ row.status.depth }}层</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="挑战" width="80" align="center">
                <template #default="{ row }">
                  <span v-if="countChallenges(row.status?.challenges) > 0">
                    {{ countChallenges(row.status?.challenges) }}
                  </span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row }">
                  <el-button type="danger" size="small" @click="kickPlayer(row)">踢出</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 玩家管理 -->
          <el-tab-pane label="玩家管理" name="players">
            <div class="tab-header">
              <h3>玩家列表</h3>
              <div class="filter-bar">
                <el-input v-model="playerSearch" placeholder="搜索玩家" clearable style="width: 200px" />
                <el-select v-model="playerRoleFilter" placeholder="角色筛选" clearable style="width: 120px">
                  <el-option label="全部" value="" />
                  <el-option label="玩家" value="USER" />
                  <el-option label="管理员" value="ADMIN" />
                  <el-option label="封禁" value="BANNED" />
                </el-select>
                <el-button type="primary" :icon="Search" @click="loadPlayers">搜索</el-button>
              </div>
            </div>
            <el-table :data="players" v-loading="playersLoading" stripe>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="用户名" min-width="120" />
              <el-table-column label="身份" width="100">
                <template #default="{ row }">
                  <el-tag :type="getRoleType(row.role)" size="small">{{ row.role }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="注册时间" width="160">
                <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
              </el-table-column>
              <el-table-column label="最后登录" width="160">
                <template #default="{ row }">{{ formatDate(row.lastLoginAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="180" align="center">
                <template #default="{ row }">
                  <el-select v-model="row.role" size="small" style="width: 90px" @change="changeRole(row)">
                    <el-option label="玩家" value="USER" />
                    <el-option label="管理员" value="ADMIN" />
                    <el-option label="封禁" value="BANNED" />
                  </el-select>
                  <el-button type="danger" size="small" :icon="Delete" @click="deletePlayer(row)" style="margin-left: 8px" />
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-model:current-page="playerPage"
              :page-size="20"
              :total="playerTotalPages * 20"
              layout="prev, pager, next"
              @current-change="loadPlayers"
              class="pagination"
            />
          </el-tab-pane>

          <!-- 记录管理 -->
          <el-tab-pane label="记录管理" name="records">
            <div class="tab-header">
              <h3>游戏记录</h3>
              <div class="filter-bar">
                <el-input v-model="recordPlayerFilter" placeholder="玩家名称" clearable style="width: 150px" />
                <el-select v-model="recordWinFilter" placeholder="结果" clearable style="width: 100px">
                  <el-option label="全部" value="" />
                  <el-option label="胜利" value="true" />
                  <el-option label="失败" value="false" />
                </el-select>
                <el-button type="primary" :icon="Search" @click="loadRecords">搜索</el-button>
              </div>
            </div>
            <el-table :data="records" v-loading="recordsLoading" stripe>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="player_name" label="玩家" min-width="120" />
              <el-table-column prop="score" label="分数" width="100" />
              <el-table-column prop="maxDepth" label="深度" width="80" />
              <el-table-column label="结果" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.win ? 'success' : 'danger'" size="small">{{ row.win ? '胜利' : '失败' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row }">
                  <el-button type="danger" size="small" :icon="Delete" @click="deleteRecord(row)" />
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-model:current-page="recordPage"
              :page-size="20"
              :total="recordTotalPages * 20"
              layout="prev, pager, next"
              @current-change="loadRecords"
              class="pagination"
            />
          </el-tab-pane>

          <!-- 广播通知 -->
          <el-tab-pane label="广播通知" name="broadcast">
            <el-card class="broadcast-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <span>发送广播消息</span>
                  <el-tag type="info">当前在线: {{ stats?.onlineCount || 0 }} 人</el-tag>
                </div>
              </template>
              <el-alert
                v-if="broadcastMessage"
                :title="broadcastMessage"
                :type="broadcastSuccess ? 'success' : 'error'"
                show-icon
                :closable="false"
                style="margin-bottom: 16px"
              />
              <el-input
                v-model="broadcastText"
                type="textarea"
                :rows="4"
                placeholder="请输入要广播的消息内容..."
                maxlength="500"
                show-word-limit
              />
              <div class="broadcast-actions">
                <el-button type="primary" size="large" @click="sendBroadcast" :loading="broadcastLoading">
                  <el-icon><Promotion /></el-icon>
                  发送广播
                </el-button>
              </div>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Delete, Promotion } from '@element-plus/icons-vue'
import { adminApi } from '../api'
import { authStore } from '../store/auth'

const isAdmin = computed(() => authStore.user?.role === '管理员')

const stats = ref(null)
const activeTab = ref('online')

const players = ref([])
const playersLoading = ref(false)
const playerPage = ref(1)
const playerTotalPages = ref(0)
const playerSearch = ref('')
const playerRoleFilter = ref('')

const records = ref([])
const recordsLoading = ref(false)
const recordPage = ref(1)
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

const statsList = computed(() => [
  { label: '注册玩家', value: stats.value?.totalPlayers || 0, icon: 'User', color: '#409EFF' },
  { label: '游戏记录', value: stats.value?.totalRecords || 0, icon: 'Document', color: '#67C23A' },
  { label: '当前在线', value: stats.value?.onlineCount || 0, icon: 'CircleCheck', color: '#E6A23C' },
  { label: '胜利次数', value: stats.value?.winCount || 0, icon: 'Trophy', color: '#F56C6C' },
  { label: '封禁账号', value: stats.value?.bannedCount || 0, icon: 'CircleClose', color: '#909399' },
  { label: '管理员', value: stats.value?.adminCount || 0, icon: 'UserFilled', color: '#9B59B6' }
])

const countChallenges = (challenges) => {
  if (!challenges) return 0
  let count = 0
  for (const mask of CHALLENGE_MASKS) {
    if ((challenges & mask) !== 0) count++
  }
  return count
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getStatusText = (status) => {
  if (!status) return '大厅'
  if (status.depth > 0) return `地牢${status.depth}层`
  return '大厅'
}

const getRoleType = (role) => {
  const types = { 'ADMIN': 'danger', 'BANNED': 'info', 'USER': 'success' }
  return types[role] || 'info'
}

const loadStats = async () => {
  try {
    const res = await adminApi.getStats()
    if (res.data.success) stats.value = res.data.data
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const loadOnlinePlayers = async () => {
  onlineLoading.value = true
  try {
    const res = await adminApi.getOnline()
    if (res.data.success) onlinePlayers.value = res.data.data || []
  } catch (error) {
    console.error('加载在线玩家失败:', error)
  } finally {
    onlineLoading.value = false
  }
}

const loadPlayers = async () => {
  playersLoading.value = true
  try {
    const res = await adminApi.getPlayers(playerPage.value - 1, 20, playerRoleFilter.value || null, playerSearch.value || null)
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

const loadRecords = async () => {
  recordsLoading.value = true
  try {
    const win = recordWinFilter.value === '' ? null : recordWinFilter.value === 'true'
    const res = await adminApi.getRecords(recordPage.value - 1, 20, win, recordPlayerFilter.value || null)
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

const changeRole = async (player) => {
  try {
    await ElMessageBox.confirm(`确定要将 ${player.name} 的角色改为 ${player.role} 吗？`, '确认')
    const res = await adminApi.setPlayerRole(player.id, player.role)
    if (res.data.success) {
      ElMessage.success('修改成功')
      loadStats()
    } else {
      ElMessage.error(res.data.message)
      loadPlayers()
    }
  } catch {
    loadPlayers()
  }
}

const deletePlayer = async (player) => {
  try {
    await ElMessageBox.confirm(`确定要删除玩家 ${player.name} 吗？此操作不可恢复！`, '警告', { type: 'warning' })
    const res = await adminApi.deletePlayer(player.id)
    if (res.data.success) {
      ElMessage.success('删除成功')
      loadPlayers()
      loadStats()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const deleteRecord = async (record) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？此操作不可恢复！', '警告', { type: 'warning' })
    const res = await adminApi.deleteRecord(record.id)
    if (res.data.success) {
      ElMessage.success('删除成功')
      loadRecords()
      loadStats()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const kickPlayer = async (player) => {
  try {
    await ElMessageBox.confirm(`确定要踢出玩家 ${player.name} 吗？`, '确认')
    const res = await adminApi.kick(player.name)
    if (res.data.success) {
      ElMessage.success('踢出成功')
      loadOnlinePlayers()
      loadStats()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('踢出失败')
  }
}

const sendBroadcast = async () => {
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
.admin-page {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.admin-tabs-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

:deep(.admin-tabs-card .el-tabs__header) {
  background: var(--bg-hover);
  border-bottom: 1px solid var(--border-color);
}

:deep(.admin-tabs-card .el-tabs__item) {
  color: var(--text-secondary);
}

:deep(.admin-tabs-card .el-tabs__item.is-active) {
  color: var(--primary-color);
  background: var(--bg-card);
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.tab-header h3 {
  margin: 0;
  font-size: 16px;
}

.filter-bar {
  display: flex;
  gap: 8px;
}

.player-link {
  color: var(--primary-color);
  text-decoration: none;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.broadcast-card {
  max-width: 600px;
  margin: 0 auto;
  background: var(--bg-hover);
  border: 1px solid var(--border-color);
}

.broadcast-actions {
  margin-top: 16px;
  text-align: center;
}

:deep(.el-table) {
  background: transparent;
}

:deep(.el-table th) {
  background: var(--bg-hover);
}

:deep(.el-table tr) {
  background: transparent;
}

:deep(.el-table td) {
  background: transparent;
}
</style>
