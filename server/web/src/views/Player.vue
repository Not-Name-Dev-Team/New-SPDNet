<template>
  <div class="player">
    <div v-if="loading" class="loading">加载中...</div>

    <template v-else-if="playerInfo">
      <div class="card">
        <h2 class="card-title">
          {{ playerInfo.name }}
          <span :class="['badge', playerInfo.online ? 'badge-online' : 'badge-offline']">
            {{ playerInfo.online ? '在线' : '离线' }}
          </span>
        </h2>

        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-value">{{ playerInfo.totalGames }}</div>
            <div class="stat-label">总游戏次数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ playerInfo.wins }}</div>
            <div class="stat-label">胜利次数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ playerInfo.achievementCount }}</div>
            <div class="stat-label">成就数量</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ playerInfo.role }}</div>
            <div class="stat-label">身份</div>
          </div>
        </div>

        <div class="player-meta">
          <div class="meta-item">
            <span class="meta-label">注册时间:</span>
            <span class="meta-value">{{ formatDate(playerInfo.createdAt) }} ({{ timeAgo(playerInfo.createdAt) }})</span>
          </div>
          <div class="meta-item" v-if="playerInfo.lastLoginAt">
            <span class="meta-label">最后登录:</span>
            <span class="meta-value">{{ formatDate(playerInfo.lastLoginAt) }} ({{ timeAgo(playerInfo.lastLoginAt) }})</span>
          </div>
        </div>
      </div>

      <div class="card">
        <h3 class="card-title">游戏记录</h3>

        <div v-if="recordsLoading" class="loading">加载中...</div>

        <table class="table" v-else-if="records.length > 0">
          <thead>
            <tr>
              <th>分数</th>
              <th>深度</th>
              <th>挑战</th>
              <th>结果</th>
              <th>角色</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in records" :key="record.id">
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

        <div v-else class="loading">暂无游戏记录</div>
      </div>
    </template>

    <div v-else class="card">
      <div class="alert alert-error">玩家不存在</div>
      <router-link to="/" class="btn">返回首页</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { playerApi } from '../api'

const CHALLENGE_MASKS = [128, 256, 1, 2, 4, 8, 16, 32, 64]

function countChallenges(challenges) {
  if (!challenges) return 0
  let count = 0
  for (const mask of CHALLENGE_MASKS) {
    if ((challenges & mask) !== 0) count++
  }
  return count
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

function timeAgo(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(days / 365)

  if (years > 0) return `${years}年前`
  if (months > 0) return `${months}个月前`
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  return '刚刚'
}

const route = useRoute()
const playerInfo = ref(null)
const records = ref([])
const loading = ref(true)
const recordsLoading = ref(true)

async function loadPlayer() {
  loading.value = true
  recordsLoading.value = true

  try {
    const name = route.params.name
    const res = await playerApi.getPlayerInfo(name)
    
    if (res.data.success) {
      playerInfo.value = res.data.data
      
      const recordsRes = await playerApi.getPlayerRecords(name)
      if (recordsRes.data.success) {
        records.value = recordsRes.data.data || []
      }
    }
  } catch (error) {
    console.error('加载玩家信息失败:', error)
    playerInfo.value = null
  } finally {
    loading.value = false
    recordsLoading.value = false
  }
}

onMounted(() => {
  loadPlayer()
})

watch(() => route.params.name, () => {
  loadPlayer()
})
</script>

<style scoped>
.card-title {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.player-meta {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.meta-item {
  display: flex;
  padding: 0.5rem 0;
}

.meta-label {
  width: 80px;
  color: var(--text-secondary);
}

.meta-value {
  flex: 1;
  color: var(--text-color);
}
</style>
