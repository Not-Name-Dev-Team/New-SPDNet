<template>
  <div class="leaderboard-page">
    <div class="page-header">
      <div class="header-title">
        <el-icon :size="32" color="var(--primary-color)"><Trophy /></el-icon>
        <div>
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
      />
    </div>

    <el-card class="filter-card" shadow="hover">
      <el-form :model="filters" inline class="filter-form">
        <el-form-item label="玩家">
          <el-select v-model="filters.playerType" @change="onPlayerTypeChange" style="width: 120px">
            <el-option label="所有玩家" value="all" />
            <el-option label="我的记录" value="me" />
          </el-select>
        </el-form-item>

        <el-form-item label="搜索" v-if="filters.playerType === 'all'">
          <el-input
            v-model="filters.playerName"
            placeholder="输入玩家名"
            clearable
            @keyup.enter="loadLeaderboard"
            style="width: 150px"
          />
        </el-form-item>

        <el-form-item label="挑战">
          <el-select v-model="filters.challengeCount" @change="loadLeaderboard" clearable style="width: 120px">
            <el-option label="不筛选" :value="null" />
            <el-option v-for="i in 10" :key="i-1" :label="`${i-1}挑战`" :value="i-1" />
          </el-select>
        </el-form-item>

        <el-form-item label="模式">
          <el-select v-model="filters.gameMode" @change="loadLeaderboard" clearable style="width: 120px">
            <el-option label="不筛选" :value="null" />
            <el-option label="标准模式" value="STANDARD" />
            <el-option label="每日挑战" value="DAILY" />
          </el-select>
        </el-form-item>

        <el-form-item label="排序">
          <el-select v-model="filters.sortBy" @change="loadLeaderboard" style="width: 140px">
            <el-option label="分数最高" value="score" />
            <el-option label="最近通关" value="id" />
            <el-option label="时间最短" value="duration" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="filters.winOnly" @change="loadLeaderboard">
            只显示胜利
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button @click="resetFilters" :icon="Delete">重置</el-button>
          <el-button type="primary" @click="loadLeaderboard" :icon="Search">筛选</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="hover" v-loading="loading">
      <el-table
        :data="records"
        stripe
        style="width: 100%"
        :header-cell-style="{ background: 'var(--bg-hover)' }"
      >
        <el-table-column type="index" label="排名" width="80" align="center">
          <template #default="{ $index }">
            <div class="rank-cell">
              <el-tag
                v-if="page === 0 && $index < 3"
                :type="['danger', 'warning', 'success'][$index]"
                effect="dark"
                round
                size="small"
              >
                <el-icon><Trophy /></el-icon>
                {{ $index + 1 }}
              </el-tag>
              <span v-else class="rank-number">{{ $index + 1 + page * size }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="玩家" min-width="140">
          <template #default="{ row }">
            <router-link :to="`/player/${row.player_name}`" class="player-link">
              <el-avatar :size="28" :icon="UserFilled" class="player-avatar" />
              <span>{{ row.player_name || '未知' }}</span>
            </router-link>
          </template>
        </el-table-column>

        <el-table-column label="分数" width="100" sortable>
          <template #default="{ row }">
            <span class="score-value">{{ row.score.toLocaleString() }}</span>
          </template>
        </el-table-column>

        <el-table-column label="层数" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info" effect="plain">{{ row.maxDepth }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="等级" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.level" class="level-value">Lv.{{ row.level }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>

        <el-table-column label="挑战" width="80" align="center">
          <template #default="{ row }">
            <el-tag
              v-if="countChallenges(row.challenges) > 0"
              type="warning"
              size="small"
              effect="dark"
            >
              {{ countChallenges(row.challenges) }}
            </el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>

        <el-table-column label="模式" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.gameMode === 'DAILY' ? 'success' : 'info'" size="small" effect="plain">
              {{ getGameModeName(row.gameMode) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="结果" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.win ? 'success' : 'danger'" effect="dark" round size="small">
              {{ row.win ? '胜' : '败' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small" effect="plain">
              {{ getHeroClassName(row.class) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="死亡原因" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="cause-text">{{ formatCause(row.cause) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && records.length === 0" description="暂无记录" />

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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Trophy, Refresh, Delete, Search,
  UserFilled, CircleCheck, CircleClose
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
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header-title h1 {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.header-title p {
  margin: 0;
  color: var(--text-secondary);
}

.filter-card {
  margin-bottom: 1.5rem;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
}

:deep(.filter-form .el-form-item) {
  margin-bottom: 0;
  margin-right: 0;
}

.table-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.rank-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.rank-number {
  color: var(--text-secondary);
  font-weight: 500;
}

.player-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-primary);
  text-decoration: none;
  transition: color 0.3s;
}

.player-link:hover {
  color: var(--primary-color);
}

.player-avatar {
  background: var(--gradient-primary);
}

.score-value {
  font-weight: 600;
  color: var(--primary-color);
}

.level-value {
  color: var(--warning-color);
  font-weight: 500;
}

.cause-text {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.text-muted {
  color: var(--text-muted);
}

.pagination-wrapper {
  margin-top: 1.5rem;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background: var(--primary-color);
}

@media (max-width: 768px) {
  .filter-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  :deep(.filter-form .el-form-item) {
    width: 100%;
  }
  
  :deep(.filter-form .el-select,
         .filter-form .el-input) {
    width: 100% !important;
  }
}
</style>
