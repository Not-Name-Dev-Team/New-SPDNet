<template>
  <div class="leaderboard-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="32"><Trophy /></el-icon>
        </div>
        <div class="header-text">
          <h1>排行榜</h1>
          <p>查看所有玩家的冒险成绩</p>
        </div>
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

    <!-- Filters -->
    <div class="filters-card">
      <div class="filters-grid">
        <div class="filter-group">
          <label>玩家筛选</label>
          <el-select v-model="filters.playerType" @change="onPlayerTypeChange" style="width: 140px">
            <el-option label="所有玩家" value="all" />
            <el-option label="我的记录" value="me" />
          </el-select>
        </div>

        <div class="filter-group" v-if="filters.playerType === 'all'">
          <label>搜索玩家</label>
          <el-input
            v-model="filters.playerName"
            placeholder="输入玩家名"
            clearable
            @keyup.enter="loadLeaderboard"
            style="width: 160px"
          />
        </div>

        <div class="filter-group">
          <label>挑战数</label>
          <el-select v-model="filters.challengeCount" @change="loadLeaderboard" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option v-for="i in 10" :key="i-1" :label="`${i-1}挑战`" :value="i-1" />
          </el-select>
        </div>

        <div class="filter-group">
          <label>游戏模式</label>
          <el-select v-model="filters.gameMode" @change="loadLeaderboard" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option label="标准模式" value="STANDARD" />
            <el-option label="每日挑战" value="DAILY" />
          </el-select>
        </div>

        <div class="filter-group">
          <label>排序方式</label>
          <el-select v-model="filters.sortBy" @change="loadLeaderboard" style="width: 140px">
            <el-option label="分数最高" value="score" />
            <el-option label="最近通关" value="id" />
            <el-option label="时间最短" value="duration" />
          </el-select>
        </div>

        <div class="filter-group checkbox-group">
          <el-checkbox v-model="filters.winOnly" @change="loadLeaderboard">
            只显示胜利
          </el-checkbox>
        </div>

        <div class="filter-actions">
          <el-button @click="resetFilters" :icon="Delete">重置</el-button>
          <el-button type="primary" @click="loadLeaderboard" :icon="Search">筛选</el-button>
        </div>
      </div>
    </div>

    <!-- Leaderboard Table -->
    <div class="table-card" v-loading="loading">
      <el-table
        :data="records"
        stripe
        style="width: 100%"
        :header-cell-style="{ background: 'var(--surface-2)' }"
      >
        <el-table-column type="index" label="排名" width="90" align="center">
          <template #default="{ $index }">
            <div class="rank-cell">
              <div v-if="page === 0 && $index < 3" :class="['rank-badge', `rank-${$index + 1}`]">
                <el-icon><Trophy /></el-icon>
                {{ $index + 1 }}
              </div>
              <span v-else class="rank-number">{{ $index + 1 + page * size }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="玩家" min-width="160">
          <template #default="{ row }">
            <router-link :to="`/player/${row.player_name}`" class="player-cell">
              <el-avatar :size="32" :icon="UserFilled" class="player-avatar" />
              <span class="player-name">{{ row.player_name || '未知' }}</span>
            </router-link>
          </template>
        </el-table-column>

        <el-table-column label="分数" width="120" sortable>
          <template #default="{ row }">
            <span class="score-value">{{ row.score.toLocaleString() }}</span>
          </template>
        </el-table-column>

        <el-table-column label="层数" width="90" align="center">
          <template #default="{ row }">
            <span class="depth-value">{{ row.maxDepth }}层</span>
          </template>
        </el-table-column>

        <el-table-column label="等级" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.level" class="level-value">Lv.{{ row.level }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>

        <el-table-column label="挑战" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              v-if="countChallenges(row.challenges) > 0"
              type="warning"
              size="small"
              effect="dark"
              round
            >
              {{ countChallenges(row.challenges) }}
            </el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>

        <el-table-column label="模式" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gameMode === 'DAILY' ? 'success' : 'info'" size="small" effect="light" round>
              {{ getGameModeName(row.gameMode) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="结果" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.win ? 'success' : 'danger'" effect="light" round size="small">
              {{ row.win ? '胜' : '败' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small" effect="light" round>
              {{ getHeroClassName(row.class) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="死亡原因" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="cause-text">{{ formatCause(row.cause) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && records.length === 0" class="empty-state">
        <div class="empty-icon">
          <el-icon :size="48"><Trophy /></el-icon>
        </div>
        <p>暂无记录</p>
        <span>尝试调整筛选条件</span>
      </div>

      <div class="pagination-wrapper" v-if="totalPages > 1">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="totalPages * size"
          layout="total, sizes, prev, pager, next"
          @size-change="loadLeaderboard"
          @current-change="loadLeaderboard"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Trophy, Refresh, Delete, Search,
  UserFilled
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

const filters = reactive({
  playerType: 'all',
  playerName: '',
  challengeCount: null,
  gameMode: null,
  winOnly: false,
  sortBy: 'score'
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
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-8) var(--content-padding);
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
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
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

.refresh-btn {
  box-shadow: var(--shadow-glow-sm);
}

/* Filters */
.filters-card {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  margin-bottom: var(--space-6);
}

.filters-grid {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  align-items: flex-end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.filter-group label {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.filter-group.checkbox-group {
  padding-bottom: var(--space-2);
}

.filter-actions {
  display: flex;
  gap: var(--space-2);
  margin-left: auto;
}

/* Table Card */
.table-card {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

/* Rank Cell */
.rank-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.rank-badge {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-md);
  font-weight: 600;
  font-size: 0.875rem;
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
  color: white;
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
  color: white;
}

.rank-number {
  color: var(--text-secondary);
  font-weight: 600;
}

/* Player Cell */
.player-cell {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  color: var(--text-primary);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.player-cell:hover {
  color: var(--primary-400);
}

.player-avatar {
  background: var(--gradient-primary);
}

.player-name {
  font-weight: 500;
}

/* Score & Values */
.score-value {
  font-weight: 700;
  color: var(--primary-400);
  font-size: 1.0625rem;
}

.depth-value {
  color: var(--text-secondary);
  font-weight: 500;
}

.level-value {
  color: var(--accent-amber);
  font-weight: 600;
}

.cause-text {
  color: var(--text-tertiary);
  font-size: 0.875rem;
}

.text-muted {
  color: var(--text-muted);
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
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
}

.empty-state p {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.empty-state span {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* Pagination */
.pagination-wrapper {
  padding: var(--space-5);
  display: flex;
  justify-content: center;
  border-top: 1px solid var(--border-subtle);
}

/* Responsive */
@media (max-width: 1024px) {
  .filters-grid {
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
    width: 100%;
  }

  .filter-actions .el-button {
    flex: 1;
  }
}

@media (max-width: 640px) {
  .leaderboard-page {
    padding: var(--space-4);
  }

  .page-header {
    flex-direction: column;
    gap: var(--space-4);
    align-items: flex-start;
  }

  .filters-card {
    padding: var(--space-4);
  }
}
</style>
