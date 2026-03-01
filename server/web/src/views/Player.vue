<template>
  <div class="player-page">
    <el-skeleton :rows="6" animated v-if="loading" />

    <template v-else-if="playerInfo">
      <!-- 玩家信息卡片 -->
      <el-card class="player-header-card" shadow="hover">
        <div class="player-header">
          <div class="player-identity">
            <el-avatar :size="80" :icon="UserFilled" class="player-avatar" />
            <div class="player-basic">
              <h1>
                {{ playerInfo.name }}
                <el-tag
                  :type="playerInfo.online ? 'success' : 'info'"
                  effect="dark"
                  round
                  size="small"
                >
                  {{ playerInfo.online ? '在线' : '离线' }}
                </el-tag>
              </h1>
              <p class="player-role">{{ playerInfo.role || '玩家' }}</p>
            </div>
          </div>
          
          <div class="player-stats">
            <div class="stat-item">
              <div class="stat-value">{{ playerInfo.totalGames || 0 }}</div>
              <div class="stat-label">总游戏</div>
            </div>
            <div class="stat-item">
              <div class="stat-value text-success">{{ playerInfo.wins || 0 }}</div>
              <div class="stat-label">胜利</div>
            </div>
            <div class="stat-item">
              <div class="stat-value text-warning">{{ playerInfo.achievementCount || 0 }}</div>
              <div class="stat-label">成就</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ winRate }}%</div>
              <div class="stat-label">胜率</div>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="player-meta">
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>注册于 {{ formatDate(playerInfo.createdAt) }}</span>
            <el-tag size="small" type="info">{{ timeAgo(playerInfo.createdAt) }}</el-tag>
          </div>
          <div class="meta-item" v-if="playerInfo.lastLoginAt">
            <el-icon><Timer /></el-icon>
            <span>最后登录 {{ formatDate(playerInfo.lastLoginAt) }}</span>
            <el-tag size="small" type="info">{{ timeAgo(playerInfo.lastLoginAt) }}</el-tag>
          </div>
        </div>
      </el-card>

      <!-- 游戏记录 -->
      <el-card class="records-card" shadow="hover" v-loading="recordsLoading">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <el-icon><Trophy /></el-icon>
              <span>游戏记录</span>
              <el-tag type="primary" effect="dark" round size="small">
                {{ records.length }}
              </el-tag>
            </div>
          </div>
        </template>

        <el-timeline v-if="records.length > 0">
          <el-timeline-item
            v-for="record in records"
            :key="record.id"
            :type="record.win ? 'success' : 'danger'"
          >
            <div class="record-item">
              <div class="record-header">
                <div class="record-score">
                  <el-icon><Trophy /></el-icon>
                  <span>{{ record.score.toLocaleString() }} 分</span>
                </div>
                <el-tag :type="record.win ? 'success' : 'danger'" effect="dark" round size="small">
                  {{ record.win ? '胜利' : '失败' }}
                </el-tag>
              </div>
              <div class="record-details">
                <el-tag size="small" effect="plain">第{{ record.maxDepth }}层</el-tag>
                <el-tag size="small" type="primary" effect="plain">{{ getHeroClassName(record.class) }}</el-tag>
                <el-tag v-if="countChallenges(record.challenges) > 0" size="small" type="warning" effect="dark">
                  {{ countChallenges(record.challenges) }}挑战
                </el-tag>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>

        <el-empty v-else description="暂无游戏记录" />
      </el-card>
    </template>

    <!-- 玩家不存在 -->
    <el-result
      v-else
      icon="error"
      title="玩家不存在"
      sub-title="该玩家可能不存在或已被删除"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">
          <el-icon><House /></el-icon>
          返回首页
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  UserFilled, CircleCheck, CircleClose, Calendar, Timer,
  Trophy, Location, House
} from '@element-plus/icons-vue'
import { playerApi } from '../api'

const CHALLENGE_MASKS = [128, 256, 1, 2, 4, 8, 16, 32, 64]

const countChallenges = (challenges) => {
  if (!challenges) return 0
  let count = 0
  for (const mask of CHALLENGE_MASKS) {
    if ((challenges & mask) !== 0) count++
  }
  return count
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

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const timeAgo = (dateStr) => {
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

const winRate = computed(() => {
  if (!playerInfo.value?.totalGames) return 0
  return Math.round((playerInfo.value.wins / playerInfo.value.totalGames) * 100)
})

const loadPlayer = async () => {
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
.player-page {
  max-width: 900px;
  margin: 0 auto;
}

.player-header-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  margin-bottom: 1.5rem;
}

.player-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.player-identity {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.player-avatar {
  background: var(--gradient-primary);
}

.player-basic h1 {
  margin: 0 0 0.5rem 0;
  font-size: 1.75rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.player-role {
  margin: 0;
  color: var(--text-secondary);
}

.player-stats {
  display: flex;
  gap: 2rem;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-value.text-success {
  color: var(--success-color);
}

.stat-value.text-warning {
  color: var(--warning-color);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

.player-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
}

.meta-item .el-icon {
  color: var(--primary-color);
}

.records-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 600;
}

.header-title .el-icon {
  color: var(--primary-color);
}

.record-item {
  padding: 0.5rem 0;
}

.record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}

.record-score {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--primary-color);
}

.record-details {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

:deep(.el-timeline-item__node) {
  background-color: var(--bg-card);
  border: 2px solid;
}

:deep(.el-timeline-item__node--success) {
  border-color: var(--success-color);
  color: var(--success-color);
}

:deep(.el-timeline-item__node--danger) {
  border-color: var(--danger-color);
  color: var(--danger-color);
}

@media (max-width: 768px) {
  .player-header {
    flex-direction: column;
    text-align: center;
  }
  
  .player-identity {
    flex-direction: column;
  }
  
  .player-stats {
    width: 100%;
    justify-content: space-around;
  }
  
  .player-meta {
    flex-direction: column;
    gap: 0.75rem;
  }
}
</style>
