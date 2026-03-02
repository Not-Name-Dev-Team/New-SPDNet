<template>
  <div class="daily-challenge-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon-wrapper">
          <el-icon :size="28"><Calendar /></el-icon>
        </div>
        <div class="header-text">
          <h1 class="page-title">每日挑战</h1>
          <p class="page-subtitle">每天都有新的地牢，每天都有新排行榜！</p>
        </div>
      </div>
      <div class="header-actions">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :clearable="false"
          :disabled-date="disabledDate"
          @change="handleDateChange"
          class="date-picker"
        />
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
    </div>

    <div class="daily-content">
      <div class="info-cards-section">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ selectedDate }} 每日挑战信息</span>
          </h2>
          <el-tag v-if="isToday" type="success" effect="dark" size="small">今日</el-tag>
        </div>

        <div class="info-cards" v-if="challengeInfo.length > 0">
          <div
            v-for="info in challengeInfo"
            :key="info.groupIndex"
            class="info-card"
            :class="`group-${info.groupIndex}`"
          >
            <div class="card-header">
              <div class="group-badge" :class="`badge-${info.groupIndex}`">
                {{ getGroupShortName(info.groupIndex) }}
              </div>
              <h3 class="group-name">{{ info.groupName }}</h3>
            </div>

            <div class="card-body">
              <div class="challenge-info">
                <div class="challenge-count">
                  <el-icon><StarFilled /></el-icon>
                  <span>{{ info.challengeCount }}挑战</span>
                </div>
                <div class="challenge-tags" v-if="info.challengeNames && info.challengeNames.length > 0">
                  <el-tag
                    v-for="name in info.challengeNames"
                    :key="name"
                    size="small"
                    effect="plain"
                    class="challenge-tag"
                  >
                    {{ name }}
                  </el-tag>
                </div>
                <div class="no-challenge" v-else>
                  <span>无挑战</span>
                </div>
              </div>

              <div class="stats-grid">
                <div class="stat-item">
                  <div class="stat-value">{{ info.totalParticipants }}</div>
                  <div class="stat-label">参与人数</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ info.completedCount }}</div>
                  <div class="stat-label">完成人数</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ info.winCount }}</div>
                  <div class="stat-label">通关人数</div>
                </div>
              </div>

              <div class="rate-section">
                <div class="rate-item">
                  <span class="rate-label">完成率</span>
                  <el-progress
                    :percentage="info.completionRate || 0"
                    :stroke-width="8"
                    :color="getProgressColor(info.completionRate)"
                  />
                </div>
                <div class="rate-item">
                  <span class="rate-label">通关率</span>
                  <el-progress
                    :percentage="info.winRate || 0"
                    :stroke-width="8"
                    :color="getProgressColor(info.winRate)"
                  />
                </div>
              </div>
            </div>

            <div class="card-footer">
              <el-button
                :type="activeGroup === info.groupIndex ? 'primary' : 'default'"
                size="small"
                @click="selectGroup(info.groupIndex)"
              >
                {{ activeGroup === info.groupIndex ? '当前查看' : '查看排行' }}
              </el-button>
            </div>
          </div>
        </div>

        <div v-else class="empty-info">
          <el-empty description="暂无数据" />
        </div>
      </div>

      <div class="leaderboard-section">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon><Trophy /></el-icon>
            <span>{{ activeGroupName }} 排行榜</span>
          </h2>
          <div class="group-tabs">
            <el-radio-group v-model="activeGroup" size="small">
              <el-radio-button :value="0">新手</el-radio-button>
              <el-radio-button :value="1">高手</el-radio-button>
              <el-radio-button :value="2">大师</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <div class="leaderboard-content" v-loading="recordsLoading">
          <div class="leaderboard-table" v-if="records.length > 0">
            <div class="table-row header">
              <div class="col-rank">排名</div>
              <div class="col-player">玩家</div>
              <div class="col-score">分数</div>
              <div class="col-hero">职业</div>
              <div class="col-depth">层数</div>
              <div class="col-duration">用时</div>
              <div class="col-result">结果</div>
              <div class="col-time">完成时间</div>
            </div>

            <div
              v-for="(record, index) in records"
              :key="record.id || index"
              class="table-row"
              :class="{ 'highlight': index < 3 }"
              :style="{ animationDelay: `${index * 0.03}s` }"
            >
              <div class="col-rank">
                <div class="rank-badge" :class="`rank-${index < 3 ? index + 1 : 'other'}`">
                  <span v-if="index < 3">
                    <el-icon><component :is="getRankIcon(index)" /></el-icon>
                  </span>
                  <span v-else>{{ index + 1 }}</span>
                </div>
              </div>
              <div class="col-player">
                <el-avatar :size="32" :icon="UserFilled" class="player-avatar" />
                <router-link :to="`/player/${record.playerName}`" class="player-name">
                  <span
                    v-if="record.prefix"
                    class="player-prefix clickable-prefix"
                    :style="getPrefixStyle(record.prefix)"
                    @click.prevent.stop="goToPrefix(record.prefix)"
                    title="点击查看前缀详情"
                  >{{ record.prefix.displayText }}</span>
                  {{ record.playerName }}
                </router-link>
              </div>
              <div class="col-score">
                <el-icon><Medal /></el-icon>
                <span class="score-value">{{ record.score || 0 }}</span>
              </div>
              <div class="col-hero">
                <el-tag size="small" effect="plain">{{ getHeroClassName(record.heroClass) }}</el-tag>
              </div>
              <div class="col-depth">
                <el-icon><Location /></el-icon>
                <span>第{{ record.depth || 0 }}层</span>
              </div>
              <div class="col-duration">
                <el-icon><Timer /></el-icon>
                <span>{{ formatDuration(record.duration) }}</span>
              </div>
              <div class="col-result">
                <el-tag :type="record.win ? 'success' : 'danger'" size="small" effect="dark">
                  {{ record.win ? '通关' : '失败' }}
                </el-tag>
              </div>
              <div class="col-time">
                <span>{{ record.completedAt || '-' }}</span>
              </div>
            </div>
          </div>

          <div v-else class="empty-state">
            <div class="empty-icon">
              <el-icon :size="48"><Trophy /></el-icon>
            </div>
            <p>暂无记录</p>
            <span>{{ isToday ? '今天还没有人完成该组别的挑战' : '该日期没有完成记录' }}</span>
          </div>
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
  Calendar, Refresh, InfoFilled, Trophy, StarFilled,
  UserFilled, Medal, Location, Timer
} from '@element-plus/icons-vue'
import { dailyChallengeApi } from '../api'

const router = useRouter()
const loading = ref(false)
const recordsLoading = ref(false)
const selectedDate = ref(new Date().toISOString().split('T')[0])
const challengeInfo = ref([])
const records = ref([])
const activeGroup = ref(0)

const isToday = computed(() => {
  return selectedDate.value === new Date().toISOString().split('T')[0]
})

const activeGroupName = computed(() => {
  const info = challengeInfo.value.find(i => i.groupIndex === activeGroup.value)
  return info ? info.groupName : '未知组别'
})

const disabledDate = (date) => {
  return date > new Date()
}

const getGroupShortName = (index) => {
  const names = ['新', '高', '大']
  return names[index] || '?'
}

const getHeroClassName = (heroClass) => {
  const heroNames = {
    'WARRIOR': '战士',
    'MAGE': '法师',
    'ROGUE': '盗贼',
    'HUNTRESS': '女猎手',
    'DUELIST': '决斗家',
    'CLERIC': '牧师'
  }
  return heroNames[heroClass] || heroClass || '未知'
}

const formatDuration = (duration) => {
  if (!duration) return '-'
  const minutes = Math.floor(duration / 60)
  const seconds = Math.floor(duration % 60)
  return `${minutes}分${seconds}秒`
}

const getProgressColor = (rate) => {
  if (rate >= 50) return '#10b981'
  if (rate >= 25) return '#f59e0b'
  return '#ef4444'
}

const rankIcons = [Trophy, Medal, StarFilled]
const getRankIcon = (index) => rankIcons[index] || Medal

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

const goToPrefix = (prefix) => {
  if (prefix && prefix.id) {
    router.push(`/prefix/${prefix.id}`)
  }
}

const handleDateChange = () => {
  loadData()
}

const selectGroup = (groupIndex) => {
  activeGroup.value = groupIndex
}

const loadChallengeInfo = async () => {
  try {
    const res = await dailyChallengeApi.getDailyChallengeInfo(selectedDate.value)
    if (res.data.success) {
      challengeInfo.value = res.data.data || []
    }
  } catch (error) {
    console.error('获取每日挑战信息失败:', error)
  }
}

const loadRecords = async () => {
  recordsLoading.value = true
  try {
    const res = await dailyChallengeApi.getRecords(selectedDate.value, activeGroup.value)
    if (res.data.success) {
      records.value = res.data.data || []
    }
  } catch (error) {
    console.error('获取排行榜失败:', error)
    ElMessage.error('获取排行榜失败')
  } finally {
    recordsLoading.value = false
  }
}

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([loadChallengeInfo(), loadRecords()])
  } finally {
    loading.value = false
  }
}

watch(activeGroup, () => {
  loadRecords()
})

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.daily-challenge-page {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-6) var(--content-padding);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
  flex-wrap: wrap;
  gap: var(--space-4);
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
  background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(6, 182, 212, 0.25);
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  color: var(--text-secondary);
  margin: var(--space-1) 0 0;
  font-size: 0.9375rem;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.date-picker {
  width: 160px;
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

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.info-cards-section {
  margin-bottom: var(--space-6);
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

.info-card {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all var(--transition-base);
}

.info-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.info-card.group-0 {
  border-color: rgba(16, 185, 129, 0.3);
}

.info-card.group-1 {
  border-color: rgba(245, 158, 11, 0.3);
}

.info-card.group-2 {
  border-color: rgba(239, 68, 68, 0.3);
}

.card-header {
  padding: var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  border-bottom: 1px solid var(--border-subtle);
}

.info-card.group-0 .card-header {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, transparent 100%);
}

.info-card.group-1 .card-header {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, transparent 100%);
}

.info-card.group-2 .card-header {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, transparent 100%);
}

.group-badge {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1rem;
  color: white;
}

.badge-0 {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.badge-1 {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.badge-2 {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.group-name {
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.card-body {
  padding: var(--space-4);
}

.challenge-info {
  margin-bottom: var(--space-4);
}

.challenge-count {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 1rem;
  font-weight: 600;
  color: var(--accent-amber);
  margin-bottom: var(--space-2);
}

.challenge-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-1);
}

.challenge-tag {
  background: rgba(168, 85, 247, 0.1);
  border-color: rgba(168, 85, 247, 0.3);
  color: var(--primary-400);
}

.no-challenge {
  color: var(--text-tertiary);
  font-size: 0.875rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  margin-top: var(--space-1);
}

.rate-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.rate-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.rate-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  width: 50px;
  flex-shrink: 0;
}

.rate-item :deep(.el-progress) {
  flex: 1;
}

.rate-item :deep(.el-progress__text) {
  font-size: 0.75rem !important;
  min-width: 40px;
}

.card-footer {
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid var(--border-subtle);
  text-align: center;
}

.leaderboard-section {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
}

.group-tabs {
  display: flex;
  gap: var(--space-2);
}

.leaderboard-content {
  min-height: 300px;
}

.leaderboard-table {
  display: flex;
  flex-direction: column;
}

.table-row {
  display: grid;
  grid-template-columns: 70px 1fr 100px 80px 80px 100px 80px 140px;
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

.col-hero,
.col-depth,
.col-duration,
.col-result,
.col-time {
  display: flex;
  align-items: center;
  justify-content: center;
}

.col-depth,
.col-duration {
  color: var(--text-secondary);
  gap: var(--space-1);
}

.col-time {
  font-size: 0.8125rem;
  color: var(--text-tertiary);
}

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

.empty-info {
  padding: var(--space-8);
}

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

@media (max-width: 1200px) {
  .info-cards {
    grid-template-columns: 1fr;
  }

  .table-row {
    grid-template-columns: 60px 1fr 80px 70px 80px 80px;
  }

  .col-hero,
  .col-time {
    display: none;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: space-between;
  }

  .table-row {
    grid-template-columns: 50px 1fr 70px 70px;
  }

  .col-duration,
  .col-result {
    display: none;
  }
}
</style>
