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
              <!-- SPDNet: 未登录时没有"我的记录"可言；禁用而非静默退化为全服数据 -->
              <el-option
                label="我的记录"
                value="self"
                :disabled="!authStore.isLoggedIn"
              />
            </el-select>
          </div>

          <div class="filter-group" v-if="filters.playerType === 'all'">
            <span class="filter-label">搜索玩家</span>
            <el-input
              v-model="filters.playerName"
              placeholder="输入玩家名"
              clearable
              class="filter-name-input"
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
              <el-option label="铁人模式" value="IRONMAN" />
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
            <!-- SPDNet: 玩家可以选择只查看被ban玩家的记录 -->
            <el-checkbox v-model="filters.bannedOnly">只显示被封禁玩家</el-checkbox>
            <el-button type="primary" :icon="Search" @click="applyFilters">筛选</el-button>
            <!-- SPDNet: 仅在筛选生效时出现，作为空状态的出口（空状态文案会引导用户点它） -->
            <el-button v-if="filtersActive" :icon="RefreshLeft" @click="resetFilters">
              重置筛选
            </el-button>
          </div>
        </div>
      </div>

      <!-- Top 3 Podium -->
      <!-- SPDNet: 前三名展示台显示铁人模式未被ban玩家的成绩，不受筛选条件影响 -->
      <div class="podium-section" v-if="topPlayers.length > 0">
        <div class="podium-title">
          <el-icon><Trophy /></el-icon>
          <span>铁人模式排行榜 TOP3</span>
        </div>
        <div class="podium-cards">
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
            <el-avatar :size="60" :icon="UserFilled" aria-hidden="true" />
          </div>
          <div class="podium-info">
            <router-link :to="`/player/${player.name}`" class="podium-name">
              <PrefixBadge v-if="player.prefix" :prefix="player.prefix" />
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
      </div>

      <!-- Leaderboard Table -->
      <div class="table-section">
        <div class="table-header">
          <div class="table-stats">
            <span>共 {{ totalElements }} 条记录</span>
            <span v-if="filtersActive" class="filter-active-tip">（已应用筛选）</span>
          </div>
        </div>

        <div class="leaderboard-table" v-if="loading && leaderboard.length === 0">
          <div class="table-loading">
            <el-skeleton :rows="5" animated />
          </div>
        </div>

        <div class="leaderboard-table" v-else-if="rankedLeaderboard.length > 0">
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
            v-for="(player, index) in rankedLeaderboard"
            :key="player.id || `${player.name}-${index}`"
            class="table-row"
            :class="{ 'highlight': player.actualRank <= 3 && !filtersActive }"
            :style="{ animationDelay: `${Math.min(index, 12) * 0.03}s` }"
          >
            <div class="col-rank">
              <div class="rank-badge" :class="`rank-${player.actualRank <= 3 ? player.actualRank : 'other'}`">
                <span v-if="player.actualRank <= 3">
                  <el-icon><component :is="getRankIcon(player.actualRank - 1)" /></el-icon>
                </span>
                <span v-else>{{ player.actualRank }}</span>
              </div>
            </div>
            <div class="col-player">
              <el-avatar :size="32" :icon="UserFilled" class="player-avatar" aria-hidden="true" />
              <router-link :to="`/player/${player.name}`" class="player-name">
                <PrefixBadge v-if="player.prefix" :prefix="player.prefix" />
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
              <el-tag :type="getModeTagType(player.gameMode)" size="small" effect="dark">
                {{ getModeLabel(player.gameMode) }}
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
          <p>{{ emptyStateHint.title }}</p>
          <span>{{ emptyStateHint.desc }}</span>
          <!-- SPDNet: 筛选筛空的场景直接给出出口，省得用户自己逐个字段调回去 -->
          <el-button
            v-if="filtersActive"
            class="empty-reset-btn"
            :icon="RefreshLeft"
            @click="resetFilters"
          >
            重置筛选
          </el-button>
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
import { ElMessage } from 'element-plus'
import {
  Trophy, Refresh, RefreshLeft, UserFilled, Medal, Location,
  Search, View, StarFilled
} from '@element-plus/icons-vue'
import { leaderboardApi } from '../api'
import { authStore } from '../store/auth'
import PrefixBadge from '../components/PrefixBadge.vue'

const leaderboard = ref([])
const top3IronmanPlayers = ref([])
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
  winOnly: false,
  bannedOnly: false
})

const rankIcons = [Trophy, Medal, StarFilled]

const getRankIcon = (index) => rankIcons[index] || Medal

// SPDNet: 模式列直接读后端 gameMode（'NORMAL'/'IRONMAN'/'DAILY'）。
// 原先用 player.daily 反推，导致所有铁人模式记录被错标为"标准"。
const MODE_LABELS = {
  NORMAL: '标准',
  IRONMAN: '铁人',
  DAILY: '每日'
}

const MODE_TAG_TYPES = {
  NORMAL: 'info',
  IRONMAN: 'warning',
  DAILY: 'success'
}

const getModeLabel = (gameMode) => MODE_LABELS[gameMode] || gameMode || '标准'

const getModeTagType = (gameMode) => MODE_TAG_TYPES[gameMode] || 'info'

// SPDNet: 把当前生效的筛选条件列成人类可读的短语，供空状态解释"为什么没有结果"。
// 只读 filters 中真正会发到后端的字段，避免显示一个请求里并未使用的条件。
const filterLabels = computed(() => {
  const f = filters.value
  const labels = []

  // SPDNet: playerType==='self' 只在真正带上了 playerName 时才算生效筛选——
  // 否则仅凭下拉选择就显示"已应用筛选"会与实际请求不符（请求会退化成全服数据）。
  if (f.playerType === 'self' && f.playerName) {
    labels.push(`“我的记录”`)
  } else if (f.playerName) {
    labels.push(`玩家名包含“${f.playerName}”`)
  }
  if (f.challengeCount !== null) labels.push(`${f.challengeCount} 挑战`)
  if (f.gameMode) labels.push(`${getModeLabel(f.gameMode)}模式`)
  if (f.winOnly) labels.push('仅胜利')
  if (f.bannedOnly) labels.push('仅被封禁玩家')

  return labels
})

// SPDNet: 是否应用了筛选
// playerType==='self' 只在真正带上了 playerName 时才算生效筛选，
// 否则仅凭下拉选择就显示"已应用筛选"会与实际请求不符。
const filtersActive = computed(() => filterLabels.value.length > 0)

// SPDNet: 空结果的可操作解释。区分"服务端就没有记录"与"筛选条件过滤掉了全部记录"，
// 后者要告诉用户是哪些条件、以及怎么退回。原先两种情况都只说"暂无数据"。
const emptyStateHint = computed(() => {
  if (!filtersActive.value) {
    return { title: '暂无数据', desc: '还没有任何游戏记录上榜' }
  }

  // SPDNet: 不预设任何"条件组合不合理"——被封禁玩家本就留有成绩（作弊刷分才会被封），
  // "仅胜利 + 仅被封禁玩家"是查作弊通关记录的正当组合。
  // 空结果只如实说明是哪些条件筛掉了全部记录，并给出退出的入口。
  const parts = [
    `当前筛选：${filterLabels.value.join('、')}`,
    '可点击“重置筛选”查看全部记录'
  ]

  return { title: '没有符合条件的记录', desc: parts.join('　·　') }
})

// SPDNet: 前三名改为使用铁人模式未被ban玩家的数据
const topPlayers = computed(() => {
  return top3IronmanPlayers.value
})

const getActualRank = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

// SPDNet: 把名次预计算进每行数据，模板里不再每格调用 getActualRank
// （原先每行 5 次调用，20 行即每次渲染 100 次求值）
const rankedLeaderboard = computed(() => {
  const base = (currentPage.value - 1) * pageSize.value
  return leaderboard.value.map((player, index) => ({
    ...player,
    actualRank: base + index + 1
  }))
})

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
    // SPDNet: 理论上入口已禁用，此处再兜底一次，避免未登录时 playerName 为空
    // 导致请求退化成"所有玩家"、UI 却显示"已应用筛选"的错误呈现。
    if (!authStore.isLoggedIn) {
      ElMessage.warning('请先登录后查看个人记录')
      filters.value.playerType = 'all'
      filters.value.playerName = ''
      return
    }
    filters.value.playerName = authStore.user?.name || ''
  } else {
    filters.value.playerName = ''
  }
}

const applyFilters = () => {
  currentPage.value = 1
  loadData()
}

// SPDNet: 一键清空全部筛选条件。
// 需要显式 loadData：playerName 不被 watch 监听，而"只搜了个找不到的玩家名、
// 其余条件本就是默认值"是完全常见的路径——此时点重置，被 watch 的字段一个都没变，
// 不主动重载就会毫无反应、空状态原地不动。
// 其余字段同时变化时会连带触发 watch，两次 loadData 由 loadSeq 序号保证
// 只有最后一次的结果生效，不会出现旧结果覆盖新结果。
const resetFilters = () => {
  filters.value = {
    playerType: 'all',
    playerName: '',
    challengeCount: null,
    gameMode: null,
    sortBy: 'score',
    winOnly: false,
    bannedOnly: false
  }
  currentPage.value = 1
  loadData()
}

// SPDNet: 加载铁人模式前三名（未被ban玩家）
// 该请求由 loadData 统一 await，加载态复用 loading，无需单独的 flag
const loadTop3IronmanPlayers = async () => {
  try {
    const res = await leaderboardApi.getTop3IronmanPlayers()
    if (res.data.success) {
      const records = res.data.data || []
      top3IronmanPlayers.value = records.map(record => {
        const playerName = record.playerName || '未知'
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
    }
  } catch (error) {
    console.error('获取铁人模式前三名失败:', error)
  }
}

// SPDNet: 请求序号，防止快速切换筛选时先发出的慢响应覆盖后发出的新结果
let loadSeq = 0

const loadData = async () => {
  const seq = ++loadSeq
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
    // SPDNet: 玩家可以选择只显示被ban玩家
    if (filters.value.bannedOnly) {
      params.bannedOnly = true
    }

    const res = await leaderboardApi.getLeaderboard(params.page, params.size, params)
    // SPDNet: 已有更新的请求发出，丢弃这次的结果
    if (seq !== loadSeq) return
    if (res.data.success) {
      const data = res.data.data || {}
      const records = data.records || []
      leaderboard.value = records.map(record => {
        const playerName = record.playerName || '未知'
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
    if (seq !== loadSeq) return
    console.error('获取排行榜失败:', error)
    ElMessage.error('获取排行榜失败')
  } finally {
    // 仅最新请求负责结束 loading，避免旧请求提前清掉加载态
    if (seq === loadSeq) loading.value = false
  }
}

// 监听筛选条件变化（除了playerName，因为需要手动点击筛选）
watch([() => filters.value.sortBy], () => {
  currentPage.value = 1
  loadData()
})

// SPDNet: 监听其他筛选条件变化，实时更新排行榜
watch([
  () => filters.value.playerType,
  () => filters.value.challengeCount,
  () => filters.value.gameMode,
  () => filters.value.winOnly,
  () => filters.value.bannedOnly
], () => {
  currentPage.value = 1
  loadData()
})

onMounted(() => {
  loadData()
  loadTop3IronmanPlayers()
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
  background: var(--gradient-amber);
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
  background: var(--gradient-amber);
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
  color: white;
  border-color: var(--primary-400);
  background: rgba(168, 85, 247, 0.2);
}

.refresh-btn:hover {
  color: white;
  border-color: var(--primary-300);
  background: rgba(168, 85, 247, 0.3);
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

/* SPDNet: 900-1400px 之间筛选组会换行，而 margin-left:auto 仍把动作区推到最右，
   与它作用的复选框脱节。此断点让动作区独占一行并靠左。 */
@media (max-width: 1200px) {
  .filter-actions {
    margin-left: 0;
    flex-basis: 100%;
  }
}

/* SPDNet: 用类控制宽度，不要改回内联 style —— 内联样式会压过
   900px 断点的 width:100%，两套机制会互相打架。 */
.filter-name-input {
  flex: 0 1 170px;
  min-width: 120px;
}

.filter-active-tip {
  color: var(--primary-400);
  font-size: 0.875rem;
}

/* Podium Section */
.podium-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.podium-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  padding: var(--space-3);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
}

.podium-title .el-icon {
  color: #f59e0b;
}

.podium-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
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
  background: var(--gradient-amber);
  color: white;
  box-shadow: 0 4px 16px rgba(245, 158, 11, 0.4);
}

.podium-card.rank-2 .podium-rank {
  background: var(--gradient-silver);
  color: white;
  box-shadow: 0 4px 16px rgba(161, 161, 170, 0.4);
}

.podium-card.rank-3 .podium-rank {
  background: var(--gradient-bronze);
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
  font-variant-numeric: tabular-nums;
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
/* SPDNet: 横向必须可滚动。用 overflow:hidden 时，表格一旦溢出，
   列会被静默裁掉且无法触达（窄屏下的"操作"列就是一例）。 */
.table-section {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow-x: auto;
  overflow-y: hidden;
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

/* SPDNet: 首次加载骨架屏，避免数据到达前闪现"暂无数据" */
.table-loading {
  padding: var(--space-5);
}

.table-row {
  display: grid;
  /* SPDNet: 轨道必须与"实际可见的列"匹配，因此用弹性轨道而非手写固定轨道：
     数值列按内容自适应，玩家列占剩余空间，各断点只负责 display:none 掉整列。
     若改成手写固定轨道，务必同步各断点隐藏的列，否则会错位。 */
  grid-template-columns:
    56px
    minmax(120px, 1fr)
    repeat(6, minmax(64px, auto));
  gap: var(--space-2);
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
  background: var(--gradient-amber);
  color: white;
}

.rank-badge.rank-2 {
  background: var(--gradient-silver);
  color: white;
}

.rank-badge.rank-3 {
  background: var(--gradient-bronze);
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

.col-score {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--accent-amber);
}

.score-value {
  font-weight: 600;
  /* SPDNet: 排行榜的核心用途是纵向比较数值，比例数字会让右边缘参差 */
  font-variant-numeric: tabular-nums;
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

/* SPDNet: 限定为直接子级 span——否则会命中 el-button 内部的 <span>，
   把按钮文字染成次要灰并压小字号 */
.empty-state > span {
  color: var(--text-secondary);
  font-size: 0.875rem;
  max-width: 46ch;
  line-height: 1.7;
}

.empty-reset-btn {
  margin-top: var(--space-4);
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
/* SPDNet: 断点只负责"隐藏列"，轨道布局交给上面的 minmax 弹性定义，
   避免再次出现"轨道数与可见列数不一致 → 错位/被裁切"的问题。 */
@media (max-width: 1200px) {
  .podium-cards {
    grid-template-columns: 1fr;
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
    padding: var(--space-2) var(--space-3);
  }

  /* SPDNet: 此处原先隐藏 .col-result，导致 640px 以下的用户完全看不到
     "胜利/失败"——而这是排行榜最核心的语义之一，不该按屏宽丢弃。
     改为保留该列，由 .table-section 已有的 overflow-x: auto 承接横向滚动
     （该容器当初正是为此从不透明的 overflow: hidden 改过来的）。 */
  .col-rank,
  .col-player,
  .col-score,
  .col-result,
  .col-action {
    min-width: 0;
  }

  .player-name {
    font-size: 0.875rem;
  }
}
</style>
