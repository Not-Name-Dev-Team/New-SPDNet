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
              <td>{{ record.challengeAmount }}</td>
              <td>
                <span :class="['badge', record.win ? 'badge-win' : 'badge-offline']">
                  {{ record.win ? '胜利' : '失败' }}
                </span>
              </td>
              <td>{{ record.heroClass }}</td>
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
</style>
