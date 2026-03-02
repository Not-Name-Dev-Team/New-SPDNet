<template>
  <div class="leaderboard-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon-wrapper">
          <el-icon :size="28"><Trophy /></el-icon>
        </div>
        <div class="header-text">
          <h1 class="page-title">排行榜</h1>
          <p class="page-subtitle">查看所有玩家的最高记录，挑战极限！</p>
        </div>
      </div>
      <el-button
        type="primary"
        plain
        :icon="Refresh"
        @click="loadData"
        :loading="loading"
        class="refresh-btn"
      >
        刷新
      </el-button>
    </div>

    <div class="leaderboard-content">
      <!-- Filter Section -->
      <div class="filter-section">
        <div class="filter-row">
          <div class="filter-group">
            <span class="filter-label">排行榜类型</span>
            <el-select v-model="filters.playerType" placeholder="选择类型" @change="handlePlayerTypeChange">
              <el-option label="所有玩家" value="all" />
              <el-option label="我的记录" value="self" />
            </el-select>
          </div>

          <div class="filter-group" v-if="filters.playerType === 'all'">
            <span class="filter-label">搜索玩家</span>
            <el-input
              v-model="filters.playerName"
              placeholder="输入玩家名"
              clearable
              style="width: 150px"
            />
          </div>

          <div class="filter-group">
            <span class="filter-label">挑战数量</span>
            <el-select v-model="filters.challengeCount" placeholder="不筛选" clearable>
              <el-option label="不筛选" :value="null" />
              <el-option v-for="i in 10" :key="i-1" :label="`${i-1}挑战`" :value="i-1" />
            </el-select>
          </div>

          <div class="filter-group">
            <span class="filter-label">游戏模式</span>
            <el-select v-model="filters.gameMode" placeholder="不筛选" clearable>
              <el-option label="不筛选" :value="null" />
              <el-option label="标准模式" value="NORMAL" />
              <el-option label="每日挑战" value="DAILY" />
            </el-select>
          </div>

          <div class="filter-group">
            <span class="filter-label">排序方式</span>
            <el-select v-model="filters.sortBy" placeholder="排序方式">
              <el-option label="最近通关" value="id" />
              <el-option label="分数最高" value="score" />
              <el-option label="通关时间最短" value="duration" />
            </el-select>
          </div>

          <div class="filter-actions">
            <el-checkbox v-model="filters.winOnly">只显示胜利</el-checkbox>
            <el-button type="primary" :icon="Search" @click="applyFilters">筛选</el-button>
          </div>
        </div>
      </div>

      <!-- Top 3 Podium -->
      <div class="podium-section" v-if="topPlayers.length > 0 && currentPage === 1 && !filtersActive">
        <div
          v-for="(player, index) in topPlayers"
          :key="player.name"
          class="podium-card"
          :class="`rank-${index + 1}`"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <div class="podium-glow"></div>
          <div class="podium-rank">
            <el-icon :size="24"><component :is="getRankIcon(index)" /></el-icon>
            <span class="rank-number">{{ index + 1 }}</span>
          </div>
          <div class="podium-avatar">
            <el-avatar :size="60" :icon="UserFilled" />
          </div>
          <div class="podium-info">
            <router-link :to="`/player/${player.name}`" class="podium-name">
              <span
                v-if="player.prefix"
                class="player-prefix clickable-prefix"
                :style="getPrefixStyle(player.prefix)"
                @click.prevent.stop="goToPrefix(player.prefix)"
                title="点击查看前缀详情"
              >{{ player.prefix.displayText }}</span>
              {{ player.name }}
            </router-link>
            <div class="podium-score">
              <el-icon><Medal /></el-icon>
              <span>{{ player.bestScore }}</span>
            </div>
          </div>
          <div class="podium-floor">
            <el-icon><Location /></el-icon>
            <span>第 {{ player.bestFloor }} 层</span>
          </div>
        </div>
      </div>

      <!-- Leaderboard Table -->
      <div class="table-section">
        <div class="table-header">
          <div class="table-stats">
            <span>共 {{ totalElements }} 条记录</span>
            <span v-if="filtersActive" class="filter-active-tip">（已应用筛选）</span>
          </div>
        </div>

        <div class="leaderboard-table" v-if="filteredLeaderboard.length > 0">
          <div class="table-row header">
            <div class="col-rank">排名</div>
            <div class="col-player">玩家</div>
            <div class="col-score">最高分数</div>
            <div class="col-floor">最高层数</div>
            <div class="col-challenge">挑战</div>
            <div class="col-mode">模式</div>
            <div class="col-result">结果</div>
            <div class="col-action">操作</div>
          </div>

          <div
            v-for="(player, index) in paginatedLeaderboard"
            :key="player.id || index"
            class="table-row"
            :class="{ 'highlight': getActualRank(index) <= 3 && !filtersActive }"
            :style="{ animationDelay: `${index * 0.03}s` }"
          >
            <div class="col-rank">
              <div class="rank-badge" :class="`rank-${getActualRank(index) <= 3 ? getActualRank(index) : 'other'}`">
                <span v-if="getActualRank(index) <= 3">
                  <el-icon><component :is="getRankIcon(getActualRank(index) - 1)" /></el-icon>
                </span>
                <span v-else>{{ getActualRank(index) }}</span>
              </div>
            </div>
            <div class="col-player">
              <el-avatar :size="32" :icon="UserFilled" class="player-avatar" />
              <router-link :to="`/player/${player.name}`" class="player-name">
                <span
                  v-if="player.prefix"
                  class="player-prefix clickable-prefix"
                  :style="getPrefixStyle(player.prefix)"
                  @click.prevent.stop="goToPrefix(player.prefix)"
                  title="点击查看前缀详情"
                >{{ player.prefix.displayText }}</span>
                {{ player.name }}
              </router-link>
            </div>
            <div class="col-score">
              <el-icon><Medal /></el-icon>
              <span class="score-value">{{ player.bestScore }}</span>
            </div>
            <div class="col-floor">
              <el-icon><Location /></el-icon>
              <span>第 {{ player.bestFloor }} 层</span>
            </div>
            <div class="col-challenge">
              <el-tag v-if="player.challengeAmount > 0" type="warning" size="small" effect="dark">
                {{ player.challengeAmount }}挑战
              </el-tag>
              <span v-else>-</span>
            </div>
            <div class="col-mode">
              <el-tag :type="player.daily ? 'success' : 'info'" size="small" effect="dark">
                {{ player.daily ? '每日' : '标准' }}
              </el-tag>
            </div>
            <div class="col-result">
              <el-tag :type="player.win ? 'success' : 'danger'" size="small" effect="dark">
                {{ player.win ? '胜利' : '失败' }}
              </el-tag>
            </div>
            <div class="col-action">
              <router-link :to="`/player/${player.name}`" class="view-btn">
                <el-icon><View /></el-icon>
                <span>查看</span>
              </router-link>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon">
            <el-icon :size="48"><Trophy /></el-icon>
          </div>
          <p>暂无数据</p>
          <span>没有找到符合条件的记录</span>
        </div>

        <!-- Pagination -->
        <div class="pagination-wrapper" v-if="totalPages > 1">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="totalElements"
            layout="total, sizes, prev, pager, next"
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Trophy, Refresh, UserFilled, Medal, Location,
  Search, View, StarFilled
} from '@element-plus/icons-vue'
import { leaderboardApi } from '../api'
import { authStore } from '../store/auth'

const router = useRouter()

const leaderboard = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const totalElements = ref(0)
const totalPages = ref(0)

// 筛选条件
const filters = ref({
  playerType: 'all',
  playerName: '',
  challengeCount: null,
  gameMode: null,
  sortBy: 'score',
  winOnly: false
})

const rankIcons = [Trophy, Medal, StarFilled]

const getRankIcon = (index) => rankIcons[index] || Medal

// SPDNet: 前缀系统 - 获取前缀样式
const getPrefixStyle = (prefix) => {
  return {
    color: prefix.color || '#ffffff',
    backgroundColor: prefix.backgroundColor || 'rgba(139, 92, 246, 0.8)',
    padding: '2px 8px',
    borderRadius: '4px',
    fontSize: '12px',
    fontWeight: 'bold',
    marginRight: '6px',
    display: 'inline-block'
  }
}

// SPDNet: 跳转到前缀详情页
const goToPrefix = (prefix) => {
  if (prefix && prefix.id) {
    router.push(`/prefix/${prefix.id}`)
  }
}

// 是否应用了筛选
const filtersActive = computed(() => {
  return filters.value.playerType !== 'all' ||
    filters.value.playerName ||
    filters.value.challengeCount !== null ||
    filters.value.gameMode !== null ||
    filters.value.winOnly
})

const filteredLeaderboard = computed(() => {
  return leaderboard.value
})

const paginatedLeaderboard = computed(() => {
  // 使用后端分页
  return leaderboard.value
})

const topPlayers = computed(() => {
  return leaderboard.value.slice(0, 3)
})

const getActualRank = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

const handlePlayerTypeChange = (val) => {
  if (val === 'self') {
    filters.value.playerName = authStore.user?.name || ''
  } else {
    filters.value.playerName = ''
  }
}

const applyFilters = () => {
  currentPage.value = 1
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: filters.value.sortBy
    }

    // 添加筛选参数
    if (filters.value.playerName) {
      params.playerName = filters.value.playerName
    }
    if (filters.value.challengeCount !== null) {
      params.challengeCount = filters.value.challengeCount
    }
    if (filters.value.gameMode) {
      params.gameMode = filters.value.gameMode
    }
    if (filters.value.winOnly) {
      params.winOnly = true
    }

    const res = await leaderboardApi.getLeaderboard(params)
    if (res.data.success) {
      const data = res.data.data || {}
      const records = data.records || []
      leaderboard.value = records.map(record => {
        const playerName = record.player_name || record.playerName || (record.player?.name) || '未知'
        return {
          id: record.id,
          name: playerName,
          bestScore: record.score || 0,
          bestFloor: record.maxDepth || 0,
          challengeAmount: record.challengeAmount || 0,
          daily: record.daily || false,
          win: record.win || false,
          gameMode: record.gameMode || 'NORMAL',
          // SPDNet: 前缀系统 - 添加前缀信息
          prefix: record.prefix || null
        }
      })
      totalElements.value = data.totalElements || records.length
      totalPages.value = data.totalPages || 1
    }
  } catch (error) {
    console.error('获取排行榜失败:', error)
    ElMessage.error('获取排行榜失败')
  } finally {
    loading.value = false
  }
}

// 监听筛选条件变化（除了playerName，因为需要手动点击筛选）
watch([() => filters.value.sortBy], () => {
  currentPage.value = 1
  loadData()
})

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.leaderboard-page {
  max-width: var(--max-width);
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

.header-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(245, 158, 11, 0.25);
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  color: var(--text-secondary);
  margin: var(--space-1) 0 0;
  font-size: 0.9375rem;
}

.refresh-btn {
  font-weight: 500;
}

/* Filter Section */
.filter-section {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-4);
  margin-bottom: var(--space-6);
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-4);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.filter-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  white-space: nowrap;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-left: auto;
}

.filter-active-tip {
  color: var(--primary-400);
  font-size: 0.875rem;
}

/* Podium Section */
.podium-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.podium-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-5);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  text-align: center;
  transition: all var(--transition-base);
  animation: fadeInUp 0.5s ease-out backwards;
  overflow: hidden;
}

.podium-glow {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity var(--transition-base);
}

.podium-card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
}

.podium-card:hover .podium-glow {
  opacity: 1;
}

.podium-card.rank-1 {
  border-color: rgba(245, 158, 11, 0.3);
  background: linear-gradient(180deg, rgba(245, 158, 11, 0.08) 0%, var(--surface-1) 100%);
}

.podium-card.rank-1 .podium-glow {
  background: radial-gradient(circle at 50% 0%, rgba(245, 158, 11, 0.15) 0%, transparent 70%);
}

.podium-card.rank-2 {
  border-color: rgba(161, 161, 170, 0.3);
  background: linear-gradient(180deg, rgba(161, 161, 170, 0.08) 0%, var(--surface-1) 100%);
}

.podium-card.rank-2 .podium-glow {
  background: radial-gradient(circle at 50% 0%, rgba(161, 161, 170, 0.15) 0%, transparent 70%);
}

.podium-card.rank-3 {
  border-color: rgba(249, 115, 22, 0.3);
  background: linear-gradient(180deg, rgba(249, 115, 22, 0.08) 0%, var(--surface-1) 100%);
}

.podium-card.rank-3 .podium-glow {
  background: radial-gradient(circle at 50% 0%, rgba(249, 115, 22, 0.15) 0%, transparent 70%);
}

.podium-rank {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-3);
}

.podium-card.rank-1 .podium-rank {
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(245, 158, 11, 0.4);
}

.podium-card.rank-2 .podium-rank {
  background: linear-gradient(135deg, #a1a1aa 0%, #71717a 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(161, 161, 170, 0.4);
}

.podium-card.rank-3 .podium-rank {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(249, 115, 22, 0.4);
}

.rank-number {
  position: absolute;
  font-size: 0.75rem;
  font-weight: 700;
  bottom: -2px;
  right: -2px;
  width: 18px;
  height: 18px;
  border-radius: var(--radius-full);
  background: var(--surface-1);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid currentColor;
}

.podium-avatar {
  margin-bottom: var(--space-3);
}

.podium-avatar :deep(.el-avatar) {
  background: var(--gradient-primary);
  font-size: 1.5rem;
}

.podium-info {
  position: relative;
  z-index: 1;
}

.podium-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  text-decoration: none;
  margin-bottom: var(--space-2);
  transition: color var(--transition-fast);
}

.podium-name:hover {
  color: var(--primary-400);
}

.podium-score {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--accent-amber);
  margin-bottom: var(--space-1);
}

.podium-floor {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  font-size: 0.875rem;
  color: var(--text-secondary);
}

/* Table Section */
.table-section {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-subtle);
}

.table-stats {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* Leaderboard Table */
.leaderboard-table {
  display: flex;
  flex-direction: column;
}

.table-row {
  display: grid;
  grid-template-columns: 70px 1fr 120px 100px 90px 80px 80px 90px;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--border-subtle);
  transition: background var(--transition-fast);
  animation: fadeInUp 0.3s ease-out backwards;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row:not(.header):hover {
  background: var(--surface-2);
}

.table-row.header {
  background: var(--surface-2);
  font-weight: 600;
  font-size: 0.8125rem;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.table-row.highlight {
  background: linear-gradient(90deg, rgba(245, 158, 11, 0.05) 0%, transparent 100%);
}

.col-rank {
  display: flex;
  align-items: center;
}

.rank-badge {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  color: white;
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #a1a1aa 0%, #71717a 100%);
  color: white;
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  color: white;
}

.rank-badge.rank-other {
  background: var(--surface-2);
  color: var(--text-secondary);
}

.col-player {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.player-avatar {
  background: var(--gradient-primary);
}

.player-name {
  font-weight: 500;
  color: var(--text-primary);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.player-name:hover {
  color: var(--primary-400);
}

.player-prefix {
  display: inline-block;
  vertical-align: middle;
}

.clickable-prefix {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.clickable-prefix:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.col-score {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--accent-amber);
}

.score-value {
  font-weight: 600;
}

.col-floor,
.col-challenge,
.col-mode,
.col-result {
  display: flex;
  align-items: center;
  justify-content: center;
}

.col-floor {
  color: var(--text-secondary);
  gap: var(--space-1);
}

.col-action {
  display: flex;
  align-items: center;
  justify-content: center;
}

.view-btn {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  background: var(--surface-2);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 0.8125rem;
  text-decoration: none;
  transition: all var(--transition-fast);
}

.view-btn:hover {
  background: var(--surface-3);
  border-color: var(--border-strong);
  color: var(--primary-400);
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12);
  text-align: center;
}

.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-xl);
  background: var(--surface-2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
  margin-bottom: var(--space-4);
}

.empty-state p {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-1);
}

.empty-state span {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: var(--space-4);
  border-top: 1px solid var(--border-subtle);
}

/* Animations */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 1200px) {
  .podium-section {
    grid-template-columns: 1fr;
  }

  .table-row {
    grid-template-columns: 60px 1fr 100px 80px 70px 70px 80px;
  }

  .col-challenge {
    display: none;
  }
}

@media (max-width: 900px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-group {
    width: 100%;
  }

  .filter-group :deep(.el-select),
  .filter-group :deep(.el-input) {
    width: 100% !important;
  }

  .filter-actions {
    margin-left: 0;
    justify-content: space-between;
  }

  .table-row {
    grid-template-columns: 50px 1fr 80px 60px 70px;
  }

  .col-floor,
  .col-mode {
    display: none;
  }
}

@media (max-width: 640px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .table-row {
    grid-template-columns: 40px 1fr 70px 60px;
    padding: var(--space-2) var(--space-3);
  }

  .col-result {
    display: none;
  }

  .player-name {
    font-size: 0.875rem;
  }
}
</style>
