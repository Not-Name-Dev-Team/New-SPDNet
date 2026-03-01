<template>
  <div class="leaderboard">
    <div class="card">
      <h2 class="card-title">排行榜</h2>

      <!-- 筛选面板 -->
      <div class="filters-panel">
        <div class="filter-row">
          <div class="filter-group">
            <label>排行榜类型</label>
            <select v-model="filters.playerType" @change="onPlayerTypeChange">
              <option value="all">所有玩家</option>
              <option value="me">我的记录</option>
            </select>
          </div>

          <div class="filter-group" v-if="filters.playerType === 'all'">
            <label>搜索玩家</label>
            <input 
              type="text" 
              v-model="filters.playerName" 
              placeholder="输入玩家名"
              @keyup.enter="loadLeaderboard"
            />
          </div>
        </div>

        <div class="filter-row">
          <div class="filter-group">
            <label>挑战数量</label>
            <select v-model="filters.challengeCount" @change="loadLeaderboard">
              <option :value="null">不筛选</option>
              <option v-for="i in 10" :key="i-1" :value="i-1">{{ i-1 }}挑战</option>
            </select>
          </div>

          <div class="filter-group">
            <label>游戏模式</label>
            <select v-model="filters.gameMode" @change="loadLeaderboard">
              <option :value="null">不筛选</option>
              <option value="STANDARD">标准模式</option>
              <option value="DAILY">每日挑战</option>
            </select>
          </div>
        </div>

        <div class="filter-row">
          <div class="filter-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="filters.winOnly" @change="loadLeaderboard" />
              只显示胜利
            </label>
          </div>

          <div class="filter-group">
            <label>排序方式</label>
            <select v-model="filters.sortBy" @change="loadLeaderboard">
              <option value="score">分数最高</option>
              <option value="id">最近通关</option>
              <option value="duration">通关时间最短</option>
            </select>
          </div>
        </div>

        <div class="filter-actions">
          <button class="btn btn-sm" @click="resetFilters">重置筛选</button>
          <button class="btn btn-primary btn-sm" @click="loadLeaderboard">应用筛选</button>
        </div>
      </div>

      <div v-if="loading" class="loading">加载中...</div>

      <table class="table" v-else-if="records.length > 0">
        <thead>
          <tr>
            <th>排名</th>
            <th>玩家</th>
            <th>分数</th>
            <th>层数</th>
            <th>等级</th>
            <th>挑战</th>
            <th>模式</th>
            <th>结果</th>
            <th>角色</th>
            <th>死亡原因</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(record, index) in records" :key="record.id">
            <td>{{ index + 1 + page * size }}</td>
            <td>
              <router-link :to="`/player/${record.player_name}`">
                {{ record.player_name || '未知' }}
              </router-link>
            </td>
            <td>{{ record.score }}</td>
            <td>{{ record.maxDepth }}</td>
            <td>{{ record.level || '-' }}</td>
            <td>{{ countChallenges(record.challenges) }}</td>
            <td>{{ getGameModeName(record.gameMode) }}</td>
            <td>
              <span :class="['badge', record.win ? 'badge-win' : 'badge-offline']">
                {{ record.win ? '胜利' : '失败' }}
              </span>
            </td>
            <td>{{ getHeroClassName(record.class) }}</td>
            <td :title="record.cause" class="cause-cell">
              {{ formatCause(record.cause) }}
            </td>
          </tr>
        </tbody>
      </table>

      <div v-else class="loading">暂无记录</div>

      <div class="pagination" v-if="totalPages > 1">
        <button 
          class="btn" 
          :disabled="page === 0" 
          @click="page--; loadLeaderboard()"
        >
          上一页
        </button>
        <span>第 {{ page + 1 }} / {{ totalPages }} 页</span>
        <button 
          class="btn" 
          :disabled="page >= totalPages - 1" 
          @click="page++; loadLeaderboard()"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { leaderboardApi } from '../api'
import { authStore } from '../store/auth'

const CHALLENGE_MASKS = [128, 256, 1, 2, 4, 8, 16, 32, 64]

function countChallenges(challenges) {
  if (!challenges) return 0
  let count = 0
  for (const mask of CHALLENGE_MASKS) {
    if ((challenges & mask) !== 0) count++
  }
  return count
}

function getGameModeName(mode) {
  if (!mode) return '标准'
  const modeMap = {
    'STANDARD': '标准',
    'DAILY': '每日'
  }
  return modeMap[mode] || mode
}

function getHeroClassName(heroClass) {
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

function formatCause(cause) {
  if (!cause) return '-'
  // 简化死亡原因显示
  const simplified = cause
    .replace('com.shatteredpixel.shatteredpixeldungeon.actors.', '')
    .replace('com.shatteredpixel.shatteredpixeldungeon.', '')
    .replace('com.watabou.', '')
  // 如果太长则截断
  if (simplified.length > 25) {
    return simplified.substring(0, 22) + '...'
  }
  return simplified
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

function onPlayerTypeChange() {
  if (filters.playerType === 'me') {
    filters.playerName = authStore.user?.name || ''
  } else {
    filters.playerName = ''
  }
  loadLeaderboard()
}

function resetFilters() {
  filters.playerType = 'all'
  filters.playerName = ''
  filters.challengeCount = null
  filters.gameMode = null
  filters.winOnly = false
  filters.sortBy = 'score'
  page.value = 0
  loadLeaderboard()
}

async function loadLeaderboard() {
  loading.value = true
  try {
    const apiFilters = {}
    
    if (filters.playerName && filters.playerName.trim()) {
      apiFilters.playerName = filters.playerName.trim()
    }
    if (filters.challengeCount !== null) {
      apiFilters.challengeCount = filters.challengeCount
    }
    if (filters.gameMode) {
      apiFilters.gameMode = filters.gameMode
    }
    if (filters.winOnly) {
      apiFilters.winOnly = true
    }
    if (filters.sortBy) {
      apiFilters.sortBy = filters.sortBy
    }

    const res = await leaderboardApi.getLeaderboard(page.value, size.value, apiFilters)
    if (res.data.success) {
      records.value = res.data.data.records || []
      totalPages.value = res.data.data.totalPages || 0
    }
  } catch (error) {
    console.error('加载排行榜失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLeaderboard()
})
</script>

<style scoped>
.filters-panel {
  background-color: var(--bg-color);
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
}

.filter-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.75rem;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
  min-width: 150px;
}

.filter-group label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.filter-group input,
.filter-group select {
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--card-bg);
  color: var(--text-color);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding-top: 1.5rem;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
}

.filter-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  margin-top: 0.5rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--border-color);
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
}

.btn-primary {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.cause-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.875rem;
  color: var(--text-secondary);
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

.badge-win {
  background-color: #28a745;
}

.badge-offline {
  background-color: #dc3545;
}
</style>
