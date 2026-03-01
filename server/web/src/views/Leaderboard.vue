<template>
  <div class="leaderboard-page">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <div class="hero-badge">
          <el-icon :size="24"><Trophy /></el-icon>
          <span>英雄榜</span>
        </div>
        <h1 class="hero-title">
          <span class="gradient-text">排行榜</span>
        </h1>
        <p class="hero-subtitle">见证最强冒险者的荣耀时刻，挑战极限，名留青史</p>
      </div>
      <div class="hero-visual">
        <div class="trophy-glow"></div>
        <div class="trophy-ring ring-1"></div>
        <div class="trophy-ring ring-2"></div>
        <div class="trophy-ring ring-3"></div>
        <div class="trophy-core">
          <el-icon :size="56"><Trophy /></el-icon>
        </div>
      </div>
    </div>

    <!-- Stats Overview -->
    <div class="stats-overview" v-if="stats">
      <div class="stat-card">
        <div class="stat-icon gold">
          <el-icon><Trophy /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalRecords?.toLocaleString() || 0 }}</span>
          <span class="stat-label">总记录</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalPlayers?.toLocaleString() || 0 }}</span>
          <span class="stat-label">玩家数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon cyan">
          <el-icon><Medal /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.winners?.toLocaleString() || 0 }}</span>
          <span class="stat-label">通关者</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon amber">
          <el-icon><Star /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.highestScore?.toLocaleString() || 0 }}</span>
          <span class="stat-label">最高分</span>
        </div>
      </div>
    </div>

    <!-- Filters Panel -->
    <div class="filters-panel">
      <div class="panel-header">
        <div class="panel-title">
          <el-icon><Filter /></el-icon>
          <span>筛选条件</span>
        </div>
        <el-button
          type="primary"
          :icon="Refresh"
          @click="loadLeaderboard"
          :loading="loading"
          circle
          class="refresh-btn"
        />
      </div>
      <div class="filters-grid">
        <div class="filter-item">
          <label class="filter-label">
            <el-icon><User /></el-icon>
            玩家筛选
          </label>
          <el-select v-model="filters.playerType" @change="onPlayerTypeChange" class="filter-select">
            <el-option label="所有玩家" value="all">
              <el-icon><UserFilled /></el-icon> 所有玩家
            </el-option>
            <el-option label="我的记录" value="me">
              <el-icon><Avatar /></el-icon> 我的记录
            </el-option>
          </el-select>
        </div>

        <div class="filter-item" v-if="filters.playerType === 'all'">
          <label class="filter-label">
            <el-icon><Search /></el-icon>
            搜索玩家
          </label>
          <el-input
            v-model="filters.playerName"
            placeholder="输入玩家名搜索"
            clearable
            @keyup.enter="loadLeaderboard"
            class="filter-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="filter-item">
          <label class="filter-label">
            <el-icon><Lightning /></el-icon>
            挑战数
          </label>
          <el-select v-model="filters.challengeCount" @change="loadLeaderboard" clearable class="filter-select">
            <el-option label="全部挑战" :value="null" />
            <el-option v-for="i in 10" :key="i-1" :label="`${i-1} 个挑战`" :value="i-1" />
          </el-select>
        </div>

        <div class="filter-item">
          <label class="filter-label">
            <el-icon><Grid /></el-icon>
            游戏模式
          </label>
          <el-select v-model="filters.gameMode" @change="loadLeaderboard" clearable class="filter-select">
            <el-option label="全部模式" :value="null" />
            <el-option label="标准模式" value="STANDARD" />
            <el-option label="每日挑战" value="DAILY" />
          </el-select>
        </div>

        <div class="filter-item">
          <label class="filter-label">
            <el-icon><Sort /></el-icon>
            排序方式
          </label>
          <el-select v-model="filters.sortBy" @change="loadLeaderboard" class="filter-select">
            <el-option label="分数最高" value="score">
              <el-icon><TrendCharts /></el-icon> 分数最高
            </el-option>
            <el-option label="最近通关" value="id">
              <el-icon><Clock /></el-icon> 最近通关
            </el-option>
            <el-option label="时间最短" value="duration">
              <el-icon><Timer /></el-icon> 时间最短
            </el-option>
          </el-select>
        </div>

        <div class="filter-item checkbox-item">
          <el-checkbox v-model="filters.winOnly" @change="loadLeaderboard" class="win-checkbox">
            <span class="checkbox-text">只显示胜利</span>
            <el-tooltip content="仅显示成功通关的记录" placement="top">
              <el-icon class="checkbox-hint"><InfoFilled /></el-icon>
            </el-tooltip>
          </el-checkbox>
        </div>

        <div class="filter-actions">
          <el-button @click="resetFilters" :icon="Delete" class="reset-btn">重置</el-button>
          <el-button type="primary" @click="loadLeaderboard" :icon="Filter" class="filter-btn">筛选</el-button>
        </div>
      </div>
    </div>

    <!-- Top 3 Podium -->
    <div class="podium-section" v-if="!loading && topThree.length > 0 && page === 0">
      <div class="podium-container">
        <!-- 2nd Place -->
        <div class="podium-item second" v-if="topThree[1]">
          <div class="podium-rank">2</div>
          <div class="podium-card">
            <div class="podium-avatar">
              <el-avatar :size="72" :icon="UserFilled" />
              <div class="rank-medal silver">
                <el-icon><Medal /></el-icon>
              </div>
            </div>
            <router-link :to="`/player/${topThree[1].player_name}`" class="podium-name">
              {{ topThree[1].player_name || '未知' }}
            </router-link>
            <div class="podium-score">{{ topThree[1].score.toLocaleString() }}</div>
            <div class="podium-meta">
              <el-tag size="small" effect="dark" round>{{ getHeroClassName(topThree[1].class) }}</el-tag>
              <span class="depth-tag">{{ topThree[1].maxDepth }}层</span>
            </div>
          </div>
          <div class="podium-base second-base"></div>
        </div>

        <!-- 1st Place -->
        <div class="podium-item first" v-if="topThree[0]">
          <div class="podium-rank">1</div>
          <div class="crown-icon">
            <el-icon :size="32"><Trophy /></el-icon>
          </div>
          <div class="podium-card featured">
            <div class="podium-avatar">
              <el-avatar :size="88" :icon="UserFilled" />
              <div class="rank-medal gold">
                <el-icon><Trophy /></el-icon>
              </div>
            </div>
            <router-link :to="`/player/${topThree[0].player_name}`" class="podium-name">
              {{ topThree[0].player_name || '未知' }}
            </router-link>
            <div class="podium-score champion">{{ topThree[0].score.toLocaleString() }}</div>
            <div class="podium-meta">
              <el-tag size="small" effect="dark" type="warning" round>{{ getHeroClassName(topThree[0].class) }}</el-tag>
              <span class="depth-tag">{{ topThree[0].maxDepth }}层</span>
            </div>
          </div>
          <div class="podium-base first-base"></div>
        </div>

        <!-- 3rd Place -->
        <div class="podium-item third" v-if="topThree[2]">
          <div class="podium-rank">3</div>
          <div class="podium-card">
            <div class="podium-avatar">
              <el-avatar :size="72" :icon="UserFilled" />
              <div class="rank-medal bronze">
                <el-icon><Medal /></el-icon>
              </div>
            </div>
            <router-link :to="`/player/${topThree[2].player_name}`" class="podium-name">
              {{ topThree[2].player_name || '未知' }}
            </router-link>
            <div class="podium-score">{{ topThree[2].score.toLocaleString() }}</div>
            <div class="podium-meta">
              <el-tag size="small" effect="dark" round>{{ getHeroClassName(topThree[2].class) }}</el-tag>
              <span class="depth-tag">{{ topThree[2].maxDepth }}层</span>
            </div>
          </div>
          <div class="podium-base third-base"></div>
        </div>
      </div>
    </div>

    <!-- Leaderboard Table -->
    <div class="table-section" v-loading="loading">
      <div class="section-header">
        <div class="section-title">
          <el-icon><List /></el-icon>
          <span>完整榜单</span>
          <el-tag type="info" size="small" effect="light" round class="count-tag">
            {{ totalPages * size }} 条记录
          </el-tag>
        </div>
      </div>

      <div class="table-container">
        <el-table
          :data="displayRecords"
          style="width: 100%"
          :header-cell-style="headerStyle"
          row-class-name="table-row"
        >
          <el-table-column type="index" label="排名" width="100" align="center">
            <template #default="{ $index }">
              <div class="rank-cell">
                <div v-if="getActualRank($index) <= 3" :class="['top-rank', `rank-${getActualRank($index)}`]">
                  <el-icon><Trophy /></el-icon>
                  <span>{{ getActualRank($index) }}</span>
                </div>
                <span v-else class="normal-rank">{{ getActualRank($index) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="玩家" min-width="180">
            <template #default="{ row }">
              <router-link :to="`/player/${row.player_name}`" class="player-cell">
                <div class="player-avatar-wrapper">
                  <el-avatar :size="40" :icon="UserFilled" />
                  <div v-if="row.win" class="win-indicator">
                    <el-icon><CircleCheck /></el-icon>
                  </div>
                </div>
                <div class="player-info">
                  <span class="player-name">{{ row.player_name || '未知' }}</span>
                  <span v-if="row.level" class="player-level">Lv.{{ row.level }}</span>
                </div>
              </router-link>
            </template>
          </el-table-column>

          <el-table-column label="分数" width="140" sortable>
            <template #default="{ row }">
              <div class="score-cell">
                <el-icon><StarFilled /></el-icon>
                <span class="score-value">{{ row.score.toLocaleString() }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="进度" width="120" align="center">
            <template #default="{ row }">
              <div class="progress-cell">
                <el-icon><Location /></el-icon>
                <span>{{ row.maxDepth }}层</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="挑战" width="100" align="center">
            <template #default="{ row }">
              <div class="challenge-cell">
                <el-tag
                  v-if="countChallenges(row.challenges) > 0"
                  type="warning"
                  size="small"
                  effect="dark"
                  round
                  class="challenge-tag"
                >
                  <el-icon><Lightning /></el-icon>
                  {{ countChallenges(row.challenges) }}
                </el-tag>
                <span v-else class="no-challenge">-</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="模式" width="110" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.gameMode === 'DAILY' ? 'success' : 'info'"
                size="small"
                effect="dark"
                round
                class="mode-tag"
              >
                {{ getGameModeName(row.gameMode) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="结果" width="90" align="center">
            <template #default="{ row }">
              <div :class="['result-cell', row.win ? 'win' : 'loss']">
                <el-icon v-if="row.win"><CircleCheckFilled /></el-icon>
                <el-icon v-else><CircleCloseFilled /></el-icon>
                <span>{{ row.win ? '胜利' : '失败' }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="角色" width="100" align="center">
            <template #default="{ row }">
              <el-tag type="primary" size="small" effect="dark" round class="hero-tag">
                {{ getHeroClassName(row.class) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="死亡原因" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="cause-cell">
                <el-icon><Warning /></el-icon>
                <span class="cause-text">{{ formatCause(row.cause) }}</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && records.length === 0" class="empty-state">
        <div class="empty-visual">
          <div class="empty-glow"></div>
          <el-icon :size="64"><Trophy /></el-icon>
        </div>
        <h3>暂无记录</h3>
        <p>尝试调整筛选条件，发现更多精彩内容</p>
        <el-button type="primary" @click="resetFilters" :icon="Refresh">重置筛选</el-button>
      </div>

      <!-- Pagination -->
      <div class="pagination-wrapper" v-if="totalPages > 1">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="totalPages * size"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadLeaderboard"
          @current-change="loadLeaderboard"
          background
          class="custom-pagination"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Trophy, Refresh, Delete, Search, UserFilled, User,
  Medal, Star, StarFilled, Filter, Grid, Lightning,
  Sort, Clock, Timer, TrendCharts, List, InfoFilled,
  Avatar, Location, CircleCheck, CircleCheckFilled,
  CircleCloseFilled, Warning
} from '@element-plus/icons-vue'
import { leaderboardApi } from '../api'
import { authStore } from '../store/auth'

const CHALLENGE_MASKS = [128, 256, 1, 2, 4, 8, 16, 32, 64]

const countChallenges = (challenges) => {
  if (!challenges) return 0
  let count = 0
  for (const mask of CHALLENGE_MASKS) {
    if ((challenges & mask) !== 0) count++
  }
  return count
}

const getGameModeName = (mode) => {
  if (!mode) return '标准'
  const modeMap = {
    'STANDARD': '标准',
    'DAILY': '每日'
  }
  return modeMap[mode] || mode
}

const getHeroClassName = (heroClass) => {
  if (!heroClass) return '-'
  const classMap = {
    'WARRIOR': '战士',
    'MAGE': '法师',
    'ROGUE': '盗贼',
    'HUNTRESS': '女猎手',
    'DUELIST': '决斗家',
    'CLERIC': '牧师'
  }
  return classMap[heroClass] || heroClass
}

const formatCause = (cause) => {
  if (!cause) return '-'
  const simplified = cause
    .replace('com.shatteredpixel.shatteredpixeldungeon.actors.', '')
    .replace('com.shatteredpixel.shatteredpixeldungeon.', '')
    .replace('com.watabou.', '')
  return simplified.length > 30 ? simplified.substring(0, 27) + '...' : simplified
}

const records = ref([])
const loading = ref(true)
const page = ref(0)
const size = ref(20)
const totalPages = ref(0)
const stats = ref({})

const filters = reactive({
  playerType: 'all',
  playerName: '',
  challengeCount: null,
  gameMode: null,
  winOnly: false,
  sortBy: 'score'
})

const topThree = computed(() => records.value.slice(0, 3))
const displayRecords = computed(() => {
  if (page.value === 0) {
    return records.value.slice(3)
  }
  return records.value
})

const getActualRank = (index) => {
  if (page.value === 0) {
    return index + 4
  }
  return index + 1 + page.value * size.value
}

const headerStyle = () => ({
  background: 'rgba(20, 20, 35, 0.8)',
  color: '#a78bfa',
  fontWeight: 600,
  fontSize: '0.875rem',
  borderBottom: '1px solid rgba(139, 92, 246, 0.2)'
})

const onPlayerTypeChange = () => {
  if (filters.playerType === 'me') {
    filters.playerName = authStore.user?.name || ''
  } else {
    filters.playerName = ''
  }
  loadLeaderboard()
}

const resetFilters = () => {
  filters.playerType = 'all'
  filters.playerName = ''
  filters.challengeCount = null
  filters.gameMode = null
  filters.winOnly = false
  filters.sortBy = 'score'
  page.value = 0
  loadLeaderboard()
}

const loadLeaderboard = async () => {
  loading.value = true
  try {
    const apiFilters = {}
    if (filters.playerName?.trim()) apiFilters.playerName = filters.playerName.trim()
    if (filters.challengeCount !== null) apiFilters.challengeCount = filters.challengeCount
    if (filters.gameMode) apiFilters.gameMode = filters.gameMode
    if (filters.winOnly) apiFilters.winOnly = true
    if (filters.sortBy) apiFilters.sortBy = filters.sortBy

    const res = await leaderboardApi.getLeaderboard(page.value, size.value, apiFilters)
    if (res.data.success) {
      records.value = res.data.data.records || []
      totalPages.value = res.data.data.totalPages || 0
      stats.value = res.data.data.stats || {}
    }
  } catch (error) {
    console.error('加载排行榜失败:', error)
    ElMessage.error('加载排行榜失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLeaderboard()
})
</script>

<style scoped>
.leaderboard-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-6) var(--content-padding);
}

/* Hero Section */
.hero-section {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: var(--space-8);
  align-items: center;
  padding: var(--space-10) var(--space-8);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  margin-bottom: var(--space-6);
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.5), transparent);
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2), rgba(251, 191, 36, 0.1));
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: var(--radius-full);
  color: #fbbf24;
  font-size: 0.875rem;
  font-weight: 600;
  margin-bottom: var(--space-4);
}

.hero-title {
  font-size: 3rem;
  font-weight: 800;
  margin: 0 0 var(--space-3);
  letter-spacing: -0.02em;
}

.hero-subtitle {
  font-size: 1.125rem;
  color: var(--text-secondary);
  margin: 0;
  max-width: 400px;
}

/* Hero Visual */
.hero-visual {
  position: relative;
  width: 180px;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.trophy-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.3) 0%, transparent 70%);
  animation: pulse-glow 3s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.trophy-ring {
  position: absolute;
  border: 2px solid rgba(245, 158, 11, 0.2);
  border-radius: 50%;
  animation: rotate-ring 20s linear infinite;
}

.trophy-ring.ring-1 {
  width: 100%;
  height: 100%;
  animation-duration: 20s;
}

.trophy-ring.ring-2 {
  width: 75%;
  height: 75%;
  border-color: rgba(139, 92, 246, 0.2);
  animation-duration: 15s;
  animation-direction: reverse;
}

.trophy-ring.ring-3 {
  width: 50%;
  height: 50%;
  border-color: rgba(6, 182, 212, 0.2);
  animation-duration: 10s;
}

@keyframes rotate-ring {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.trophy-core {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 50%, #f59e0b 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 0 40px rgba(245, 158, 11, 0.5), inset 0 0 20px rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 1;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

/* Stats Overview */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.stat-card {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  display: flex;
  align-items: center;
  gap: var(--space-4);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  border-color: rgba(139, 92, 246, 0.3);
  box-shadow: var(--shadow-glow-sm);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

.stat-icon.gold {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2), rgba(251, 191, 36, 0.1));
  color: #fbbf24;
}

.stat-icon.purple {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(168, 85, 247, 0.1));
  color: #a78bfa;
}

.stat-icon.cyan {
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.2), rgba(34, 211, 238, 0.1));
  color: #22d3ee;
}

.stat-icon.amber {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2), rgba(251, 191, 36, 0.1));
  color: #fbbf24;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

/* Filters Panel */
.filters-panel {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  margin-bottom: var(--space-6);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  background: rgba(139, 92, 246, 0.05);
  border-bottom: 1px solid var(--border-subtle);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-primary);
  font-weight: 600;
}

.panel-title .el-icon {
  color: #a78bfa;
}

.refresh-btn {
  background: rgba(139, 92, 246, 0.2);
  border: 1px solid rgba(139, 92, 246, 0.3);
  color: #a78bfa;
}

.refresh-btn:hover {
  background: rgba(139, 92, 246, 0.3);
  border-color: rgba(139, 92, 246, 0.5);
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: var(--space-4);
  padding: var(--space-5);
  align-items: end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.filter-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 0.8125rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.filter-label .el-icon {
  color: #a78bfa;
}

.filter-select,
.filter-input {
  width: 100%;
}

.filter-select :deep(.el-input__wrapper),
.filter-input :deep(.el-input__wrapper) {
  background: rgba(15, 15, 25, 0.5);
  border: 1px solid rgba(139, 92, 246, 0.2);
  box-shadow: none;
}

.filter-select :deep(.el-input__wrapper:hover),
.filter-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(139, 92, 246, 0.4);
}

.checkbox-item {
  display: flex;
  align-items: flex-end;
  padding-bottom: var(--space-2);
}

.win-checkbox {
  --el-checkbox-text-color: var(--text-secondary);
  --el-checkbox-checked-text-color: var(--text-primary);
  --el-checkbox-checked-bg-color: rgba(139, 92, 246, 0.2);
  --el-checkbox-checked-input-border-color: #8b5cf6;
}

.checkbox-text {
  margin-right: var(--space-1);
}

.checkbox-hint {
  color: var(--text-tertiary);
  cursor: help;
}

.filter-actions {
  display: flex;
  gap: var(--space-2);
  grid-column: -2 / -1;
  justify-content: flex-end;
}

.reset-btn {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
}

.reset-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  color: var(--text-primary);
}

.filter-btn {
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  border: none;
}

.filter-btn:hover {
  background: linear-gradient(135deg, #7c3aed, #9333ea);
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.4);
}

/* Podium Section */
.podium-section {
  margin-bottom: var(--space-6);
}

.podium-container {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: var(--space-4);
  padding: var(--space-8) var(--space-4);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  position: relative;
}

.podium-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(245, 158, 11, 0.3), transparent);
}

.podium-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.podium-item.first {
  order: 2;
  z-index: 3;
}

.podium-item.second {
  order: 1;
  z-index: 2;
}

.podium-item.third {
  order: 3;
  z-index: 1;
}

.podium-rank {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.875rem;
  margin-bottom: var(--space-3);
}

.first .podium-rank {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: white;
  box-shadow: 0 0 20px rgba(245, 158, 11, 0.5);
}

.second .podium-rank {
  background: linear-gradient(135deg, #94a3b8, #64748b);
  color: white;
}

.third .podium-rank {
  background: linear-gradient(135deg, #f87171, #ef4444);
  color: white;
}

.crown-icon {
  position: absolute;
  top: -20px;
  color: #fbbf24;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.podium-card {
  background: rgba(20, 20, 35, 0.8);
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  text-align: center;
  width: 200px;
  transition: all 0.3s ease;
}

.podium-card.featured {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(139, 92, 246, 0.1));
  border-color: rgba(245, 158, 11, 0.3);
  width: 220px;
}

.podium-card:hover {
  transform: translateY(-5px);
  border-color: rgba(139, 92, 246, 0.4);
  box-shadow: var(--shadow-glow-sm);
}

.podium-avatar {
  position: relative;
  display: inline-block;
  margin-bottom: var(--space-3);
}

.podium-avatar :deep(.el-avatar) {
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  border: 3px solid rgba(255, 255, 255, 0.1);
}

.featured .podium-avatar :deep(.el-avatar) {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
  border-color: rgba(245, 158, 11, 0.5);
  box-shadow: 0 0 30px rgba(245, 158, 11, 0.3);
}

.rank-medal {
  position: absolute;
  bottom: -5px;
  right: -5px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
}

.rank-medal.gold {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: white;
}

.rank-medal.silver {
  background: linear-gradient(135deg, #94a3b8, #64748b);
  color: white;
}

.rank-medal.bronze {
  background: linear-gradient(135deg, #f87171, #ef4444);
  color: white;
}

.podium-name {
  display: block;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  text-decoration: none;
  margin-bottom: var(--space-2);
  transition: color 0.2s;
}

.podium-name:hover {
  color: #a78bfa;
}

.podium-score {
  font-size: 1.5rem;
  font-weight: 700;
  color: #a78bfa;
  margin-bottom: var(--space-3);
}

.podium-score.champion {
  font-size: 1.75rem;
  color: #fbbf24;
  text-shadow: 0 0 20px rgba(245, 158, 11, 0.5);
}

.podium-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
}

.depth-tag {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.podium-base {
  width: 100%;
  height: 12px;
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
  margin-top: -6px;
}

.first-base {
  background: linear-gradient(90deg, #f59e0b, #fbbf24, #f59e0b);
  height: 16px;
}

.second-base {
  background: linear-gradient(90deg, #64748b, #94a3b8, #64748b);
}

.third-base {
  background: linear-gradient(90deg, #ef4444, #f87171, #ef4444);
}

/* Table Section */
.table-section {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  background: rgba(139, 92, 246, 0.05);
  border-bottom: 1px solid var(--border-subtle);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: 600;
  color: var(--text-primary);
}

.section-title .el-icon {
  color: #a78bfa;
}

.count-tag {
  margin-left: var(--space-2);
}

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

/* Rank Cell */
.rank-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.top-rank {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-md);
  font-weight: 700;
  font-size: 0.875rem;
}

.top-rank.rank-1 {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: white;
  box-shadow: 0 0 15px rgba(245, 158, 11, 0.4);
}

.top-rank.rank-2 {
  background: linear-gradient(135deg, #94a3b8, #64748b);
  color: white;
}

.top-rank.rank-3 {
  background: linear-gradient(135deg, #f87171, #ef4444);
  color: white;
}

.normal-rank {
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 0.9375rem;
}

/* Player Cell */
.player-cell {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  color: var(--text-primary);
  text-decoration: none;
  transition: all 0.2s;
}

.player-cell:hover {
  color: #a78bfa;
}

.player-cell:hover .player-avatar-wrapper :deep(.el-avatar) {
  transform: scale(1.05);
  box-shadow: 0 0 15px rgba(139, 92, 246, 0.3);
}

.player-avatar-wrapper {
  position: relative;
}

.player-avatar-wrapper :deep(.el-avatar) {
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  border: 2px solid rgba(139, 92, 246, 0.3);
  transition: all 0.2s;
}

.win-indicator {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 18px;
  height: 18px;
  background: #10b981;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.625rem;
  border: 2px solid var(--surface-1);
}

.player-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.player-name {
  font-weight: 600;
  font-size: 0.9375rem;
}

.player-level {
  font-size: 0.75rem;
  color: #fbbf24;
  font-weight: 500;
}

/* Score Cell */
.score-cell {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.score-cell .el-icon {
  color: #fbbf24;
}

.score-value {
  font-weight: 700;
  color: #a78bfa;
  font-size: 1.0625rem;
}

/* Progress Cell */
.progress-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  color: var(--text-secondary);
  font-weight: 500;
}

.progress-cell .el-icon {
  color: #22d3ee;
}

/* Challenge Cell */
.challenge-cell {
  display: flex;
  justify-content: center;
}

.challenge-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
  border: none;
}

.no-challenge {
  color: var(--text-muted);
}

/* Mode Tag */
.mode-tag {
  font-weight: 500;
}

/* Result Cell */
.result-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  font-size: 0.8125rem;
  font-weight: 500;
}

.result-cell.win {
  color: #10b981;
}

.result-cell.loss {
  color: #ef4444;
}

/* Hero Tag */
.hero-tag {
  background: rgba(139, 92, 246, 0.2);
  border: 1px solid rgba(139, 92, 246, 0.3);
  color: #a78bfa;
}

/* Cause Cell */
.cause-cell {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-tertiary);
}

.cause-cell .el-icon {
  color: #ef4444;
  flex-shrink: 0;
}

.cause-text {
  font-size: 0.8125rem;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-16) var(--space-8);
  text-align: center;
}

.empty-visual {
  position: relative;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
}

.empty-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.2) 0%, transparent 70%);
  animation: pulse 2s ease-in-out infinite;
}

.empty-state h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.empty-state p {
  color: var(--text-secondary);
  margin: 0;
}

/* Pagination */
.pagination-wrapper {
  padding: var(--space-5);
  display: flex;
  justify-content: center;
  border-top: 1px solid var(--border-subtle);
}

.custom-pagination :deep(.el-pagination__total) {
  color: var(--text-secondary);
}

.custom-pagination :deep(.el-pagination__jump) {
  color: var(--text-secondary);
}

.custom-pagination :deep(.el-pagination__editor) {
  background: rgba(15, 15, 25, 0.5);
  border: 1px solid rgba(139, 92, 246, 0.2);
  color: var(--text-primary);
}

/* Responsive */
@media (max-width: 1024px) {
  .hero-section {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .hero-visual {
    order: -1;
    margin: 0 auto;
  }

  .hero-subtitle {
    margin: 0 auto;
  }

  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }

  .filters-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .filter-actions {
    grid-column: span 2;
    justify-content: stretch;
  }

  .filter-actions .el-button {
    flex: 1;
  }

  .podium-container {
    flex-direction: column;
    align-items: center;
  }

  .podium-item.first {
    order: 1;
  }

  .podium-item.second {
    order: 2;
  }

  .podium-item.third {
    order: 3;
  }
}

@media (max-width: 640px) {
  .leaderboard-page {
    padding: var(--space-4);
  }

  .hero-section {
    padding: var(--space-6) var(--space-4);
  }

  .hero-title {
    font-size: 2rem;
  }

  .stats-overview {
    grid-template-columns: 1fr;
  }

  .filters-grid {
    grid-template-columns: 1fr;
  }

  .filter-actions {
    grid-column: span 1;
    flex-direction: column;
  }

  .podium-card {
    width: 100%;
    max-width: 280px;
  }

  .podium-card.featured {
    width: 100%;
    max-width: 300px;
  }
}
</style>