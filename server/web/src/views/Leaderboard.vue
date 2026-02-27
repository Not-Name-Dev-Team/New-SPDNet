<template>
  <div class="leaderboard">
    <div class="card">
      <h2 class="card-title">排行榜</h2>

      <div class="filters">
        <label>
          <input type="checkbox" v-model="winOnly" @change="loadLeaderboard" />
          只显示胜利记录
        </label>
      </div>

      <div v-if="loading" class="loading">加载中...</div>

      <table class="table" v-else-if="records.length > 0">
        <thead>
          <tr>
            <th>排名</th>
            <th>玩家</th>
            <th>分数</th>
            <th>最高层数</th>
            <th>挑战</th>
            <th>结果</th>
            <th>角色</th>
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
            <td>{{ countChallenges(record.challenges) }}</td>
            <td>
              <span :class="['badge', record.win ? 'badge-win' : 'badge-offline']">
                {{ record.win ? '胜利' : '失败' }}
              </span>
            </td>
            <td>{{ record.class }}</td>
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
import { ref, onMounted } from 'vue'
import { leaderboardApi } from '../api'

const CHALLENGE_MASKS = [128, 256, 1, 2, 4, 8, 16, 32, 64]

function countChallenges(challenges) {
  if (!challenges) return 0
  let count = 0
  for (const mask of CHALLENGE_MASKS) {
    if ((challenges & mask) !== 0) count++
  }
  return count
}

const records = ref([])
const loading = ref(true)
const page = ref(0)
const size = ref(20)
const totalPages = ref(0)
const winOnly = ref(false)

async function loadLeaderboard() {
  loading.value = true
  try {
    const res = await leaderboardApi.getLeaderboard(page.value, size.value, winOnly.value)
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
.filters {
  margin-bottom: 1rem;
  padding: 0.5rem;
  background-color: var(--bg-color);
  border-radius: 4px;
}

.filters label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
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
</style>
