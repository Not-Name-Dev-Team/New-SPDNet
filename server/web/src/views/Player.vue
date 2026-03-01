<template>
  <div class="player-page">
    <!-- Player Header -->
    <div class="player-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="player-avatar-wrapper">
          <el-avatar :size="120" :icon="UserFilled" class="player-avatar" />
          <div class="online-status" v-if="playerInfo.online">
            <span class="status-dot"></span>
            <span>在线</span>
          </div>
        </div>
        <div class="player-info">
          <h1>{{ playerInfo.name }}</h1>
          <div class="player-tags">
            <el-tag :type="playerInfo.role === '管理员' ? 'danger' : 'primary'" effect="light" round size="large">
              {{ playerInfo.role || '玩家' }}
            </el-tag>
            <el-tag type="info" effect="light" round size="large" v-if="playerInfo.online">
              <el-icon><CircleCheck /></el-icon>
              游戏中
            </el-tag>
          </div>
          <div class="player-meta">
            <span><el-icon><Calendar /></el-icon> 注册于 {{ formatDate(playerInfo.createdAt) }}</span>
            <span v-if="playerInfo.lastLoginAt"><el-icon><Timer /></el-icon> 最后登录 {{ formatDate(playerInfo.lastLoginAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Stats Grid -->
    <div class="stats-section">
      <h2 class="section-title">
        <el-icon><TrendCharts /></el-icon>
        游戏统计
      </h2>
      <div class="stats-grid">
        <div class="stat-card" v-for="(stat, index) in gameStats" :key="index">
          <div class="stat-icon" :style="{ background: stat.gradient }">
            <el-icon :size="24" color="white"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Collection Stats -->
    <div class="collection-section">
      <h2 class="section-title">
        <el-icon><Collection /></el-icon>
        收集进度
      </h2>
      <div class="collection-grid">
        <!-- 成就 -->
        <div class="collection-card">
          <div class="collection-header">
            <div class="collection-icon" style="background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%)">
              <el-icon :size="28"><Trophy /></el-icon>
            </div>
            <div class="collection-info">
              <h3>成就</h3>
              <p>{{ playerInfo.achievementCount || 0 }} 个解锁</p>
            </div>
          </div>
          <div class="collection-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: '100%' }"></div>
            </div>
            <span class="progress-text">已获得</span>
          </div>
        </div>

        <!-- 怪物图鉴 -->
        <div class="collection-card">
          <div class="collection-header">
            <div class="collection-icon" style="background: linear-gradient(135deg, #ec4899 0%, #f43f5e 100%)">
              <el-icon :size="28"><View /></el-icon>
            </div>
            <div class="collection-info">
              <h3>怪物图鉴</h3>
              <p>{{ playerInfo.bestiarySeen || 0 }} / {{ playerInfo.bestiaryTotal || 0 }} 已发现</p>
            </div>
          </div>
          <div class="collection-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: bestiaryProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ bestiaryProgress }}%</span>
          </div>
        </div>

        <!-- 物品图鉴 -->
        <div class="collection-card">
          <div class="collection-header">
            <div class="collection-icon" style="background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)">
              <el-icon :size="28"><Goods /></el-icon>
            </div>
            <div class="collection-info">
              <h3>物品图鉴</h3>
              <p>{{ playerInfo.catalogSeen || 0 }} / {{ playerInfo.catalogTotal || 0 }} 已收集</p>
            </div>
          </div>
          <div class="collection-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: catalogProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ catalogProgress }}%</span>
          </div>
        </div>

        <!-- 文档日志 -->
        <div class="collection-card">
          <div class="collection-header">
            <div class="collection-icon" style="background: linear-gradient(135deg, #10b981 0%, #06b6d4 100%)">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="collection-info">
              <h3>文档日志</h3>
              <p>{{ playerInfo.documentFound || 0 }} / {{ playerInfo.documentTotal || 0 }} 已发现</p>
            </div>
          </div>
          <div class="collection-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: documentProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ documentProgress }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Collection Details -->
    <div class="details-section" v-if="hasCollectionData">
      <el-tabs type="border-card" class="collection-tabs">
        <!-- 怪物图鉴详情 -->
        <el-tab-pane label="怪物图鉴" v-if="playerInfo.bestiaryList?.length > 0">
          <div class="detail-list">
            <div v-for="(item, index) in playerInfo.bestiaryList" :key="index" class="detail-item">
              <div class="detail-icon">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="detail-content">
                <span class="detail-name">{{ formatEntityName(item.entity) }}</span>
                <span class="detail-type">{{ item.type }}</span>
              </div>
              <el-tag size="small" type="info">遭遇 {{ item.encountered }} 次</el-tag>
            </div>
          </div>
        </el-tab-pane>

        <!-- 物品图鉴详情 -->
        <el-tab-pane label="物品图鉴" v-if="playerInfo.catalogList?.length > 0">
          <div class="detail-list">
            <div v-for="(item, index) in playerInfo.catalogList" :key="index" class="detail-item">
              <div class="detail-icon">
                <el-icon><Goods /></el-icon>
              </div>
              <div class="detail-content">
                <span class="detail-name">{{ formatItemName(item.item) }}</span>
                <span class="detail-type">{{ item.type }}</span>
              </div>
              <el-tag size="small" type="info">使用 {{ item.useCount }} 次</el-tag>
            </div>
          </div>
        </el-tab-pane>

        <!-- 文档日志详情 -->
        <el-tab-pane label="文档日志" v-if="playerInfo.documentList?.length > 0">
          <div class="detail-list">
            <div v-for="(item, index) in playerInfo.documentList" :key="index" class="detail-item">
              <div class="detail-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="detail-content">
                <span class="detail-name">{{ formatPageName(item.page) }}</span>
                <span class="detail-type">{{ item.type }}</span>
              </div>
              <el-tag size="small" type="success">已发现</el-tag>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Records Section -->
    <div class="records-section">
      <div class="section-header">
        <div class="header-title">
          <el-icon :size="24"><Trophy /></el-icon>
          <h2>游戏记录</h2>
        </div>
        <div class="header-filters">
          <el-select v-model="filters.sortBy" @change="loadRecords" style="width: 140px">
            <el-option label="最新记录" value="id" />
            <el-option label="分数最高" value="score" />
            <el-option label="层数最深" value="depth" />
          </el-select>
          <el-checkbox v-model="filters.winOnly" @change="loadRecords">
            只显示胜利
          </el-checkbox>
        </div>
      </div>

      <div class="records-list" v-loading="loading">
        <div v-if="!loading && records.length === 0" class="empty-state">
          <div class="empty-icon">
            <el-icon :size="48"><Trophy /></el-icon>
          </div>
          <p>暂无游戏记录</p>
          <span>该玩家还没有上传过游戏记录</span>
        </div>

        <div v-else class="record-cards">
          <div v-for="(record, index) in records" :key="index" class="record-card">
            <div class="record-header">
              <div class="record-rank" :class="{ 'top': index < 3 }">
                #{{ index + 1 }}
              </div>
              <el-tag :type="record.win ? 'success' : 'danger'" effect="light" round size="small">
                {{ record.win ? '胜利' : '失败' }}
              </el-tag>
            </div>

            <div class="record-stats">
              <div class="record-stat">
                <span class="stat-name">分数</span>
                <span class="stat-number highlight">{{ record.score.toLocaleString() }}</span>
              </div>
              <div class="record-stat">
                <span class="stat-name">层数</span>
                <span class="stat-number">{{ record.maxDepth }}层</span>
              </div>
              <div class="record-stat">
                <span class="stat-name">等级</span>
                <span class="stat-number">Lv.{{ record.level || 1 }}</span>
              </div>
            </div>

            <div class="record-details">
              <div class="detail-row">
                <span class="detail-label">角色</span>
                <el-tag type="primary" size="small" effect="light" round>
                  {{ getHeroClassName(record.class) }}
                </el-tag>
              </div>
              <div class="detail-row">
                <span class="detail-label">挑战</span>
                <span class="detail-value">{{ countChallenges(record.challenges) }}个</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">模式</span>
                <el-tag :type="record.gameMode === 'DAILY' ? 'success' : 'info'" size="small" effect="light" round>
                  {{ getGameModeName(record.gameMode) }}
                </el-tag>
              </div>
              <div class="detail-row" v-if="record.cause">
                <span class="detail-label">死因</span>
                <span class="detail-value cause">{{ formatCause(record.cause) }}</span>
              </div>
            </div>

            <div class="record-time">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDate(record.createdAt) }}</span>
            </div>
          </div>
        </div>

        <div class="pagination-wrapper" v-if="totalPages > 1">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[10, 20, 50]"
            :total="totalPages * size"
            layout="total, sizes, prev, pager, next"
            @size-change="loadRecords"
            @current-change="loadRecords"
            background
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  UserFilled, CircleCheck, Trophy, Clock, Calendar, Timer,
  TrendCharts, FirstAidKit, OfficeBuilding, Medal, Collection,
  View, Goods, Document, Warning
} from '@element-plus/icons-vue'
import { playerApi, leaderboardApi } from '../api'

const route = useRoute()
const playerName = computed(() => route.params.name)

const playerInfo = ref({})
const records = ref([])
const loading = ref(false)
const page = ref(0)
const size = ref(10)
const totalPages = ref(0)

const filters = reactive({
  sortBy: 'id',
  winOnly: false
})

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

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const formatEntityName = (entityClass) => {
  if (!entityClass) return '未知'
  const parts = entityClass.split('.')
  const name = parts[parts.length - 1]
  return name.replace(/([A-Z])/g, ' $1').trim()
}

const formatItemName = (itemClass) => {
  if (!itemClass) return '未知'
  const parts = itemClass.split('.')
  const name = parts[parts.length - 1]
  return name.replace(/([A-Z])/g, ' $1').trim()
}

const formatPageName = (pageName) => {
  if (!pageName) return '未知'
  return pageName.replace(/_/g, ' ')
}

// 计算进度百分比
const bestiaryProgress = computed(() => {
  const total = playerInfo.value.bestiaryTotal || 0
  const seen = playerInfo.value.bestiarySeen || 0
  return total > 0 ? Math.round((seen / total) * 100) : 0
})

const catalogProgress = computed(() => {
  const total = playerInfo.value.catalogTotal || 0
  const seen = playerInfo.value.catalogSeen || 0
  return total > 0 ? Math.round((seen / total) * 100) : 0
})

const documentProgress = computed(() => {
  const total = playerInfo.value.documentTotal || 0
  const found = playerInfo.value.documentFound || 0
  return total > 0 ? Math.round((found / total) * 100) : 0
})

const hasCollectionData = computed(() => {
  return playerInfo.value.bestiaryList?.length > 0 ||
         playerInfo.value.catalogList?.length > 0 ||
         playerInfo.value.documentList?.length > 0
})

// 游戏统计数据
const gameStats = computed(() => [
  {
    label: '总分数',
    value: (playerInfo.value.totalScore || 0).toLocaleString(),
    icon: 'TrendCharts',
    gradient: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)'
  },
  {
    label: '最高分数',
    value: (playerInfo.value.maxScore || 0).toLocaleString(),
    icon: 'Trophy',
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #f97316 100%)'
  },
  {
    label: '最深到达',
    value: `${playerInfo.value.maxDepth || 0}层`,
    icon: 'OfficeBuilding',
    gradient: 'linear-gradient(135deg, #ec4899 0%, #f43f5e 100%)'
  },
  {
    label: '最高等级',
    value: `Lv.${playerInfo.value.maxLevel || 1}`,
    icon: 'Medal',
    gradient: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)'
  },
  {
    label: '游戏次数',
    value: playerInfo.value.totalGames || 0,
    icon: 'FirstAidKit',
    gradient: 'linear-gradient(135deg, #10b981 0%, #06b6d4 100%)'
  },
  {
    label: '胜利次数',
    value: playerInfo.value.wins || 0,
    icon: 'CircleCheck',
    gradient: 'linear-gradient(135deg, #22c55e 0%, #10b981 100%)'
  }
])

const loadPlayerInfo = async () => {
  try {
    const res = await playerApi.getPlayerInfo(playerName.value)
    if (res.data.success) {
      playerInfo.value = res.data.data
    }
  } catch (error) {
    console.error('获取玩家信息失败:', error)
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const apiFilters = {
      playerName: playerName.value
    }
    if (filters.winOnly) apiFilters.winOnly = true
    if (filters.sortBy) apiFilters.sortBy = filters.sortBy

    const res = await leaderboardApi.getLeaderboard(page.value, size.value, apiFilters)
    if (res.data.success) {
      records.value = res.data.data.records || []
      totalPages.value = res.data.data.totalPages || 0
    }
  } catch (error) {
    console.error('加载记录失败:', error)
    ElMessage.error('加载记录失败')
  } finally {
    loading.value = false
  }
}

watch(() => route.params.name, () => {
  loadPlayerInfo()
  loadRecords()
})

onMounted(() => {
  loadPlayerInfo()
  loadRecords()
})
</script>

<style scoped>
.player-page {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-8) var(--content-padding);
}

/* Header */
.player-header {
  position: relative;
  border-radius: var(--radius-xl);
  overflow: hidden;
  margin-bottom: var(--space-6);
}

.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(168, 85, 247, 0.1) 100%);
  border-radius: var(--radius-xl);
}

.header-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--surface-1);
  opacity: 0.9;
}

.header-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-6);
  padding: var(--space-8);
}

.player-avatar-wrapper {
  position: relative;
}

.player-avatar {
  background: var(--gradient-primary);
  box-shadow: var(--shadow-glow);
}

.online-status {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  background: var(--accent-emerald);
  border-radius: var(--radius-full);
  color: white;
  font-size: 0.75rem;
  font-weight: 600;
  box-shadow: 0 0 12px rgba(16, 185, 129, 0.4);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: white;
  animation: pulse 2s ease-in-out infinite;
}

.player-info {
  flex: 1;
}

.player-info h1 {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 var(--space-3);
  color: var(--text-primary);
}

.player-tags {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.player-meta {
  display: flex;
  gap: var(--space-4);
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.player-meta span {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

/* Section Title */
.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 var(--space-4);
  color: var(--text-primary);
}

/* Stats Section */
.stats-section {
  margin-bottom: var(--space-8);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: var(--border-default);
  box-shadow: var(--shadow-lg);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-top: var(--space-1);
}

/* Collection Section */
.collection-section {
  margin-bottom: var(--space-8);
}

.collection-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}

.collection-card {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  transition: all var(--transition-base);
}

.collection-card:hover {
  border-color: var(--border-default);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.collection-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.collection-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.collection-info h3 {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 var(--space-1);
  color: var(--text-primary);
}

.collection-info p {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin: 0;
}

.collection-progress {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: var(--surface-2);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  transition: width 0.5s ease;
}

.progress-text {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--primary-400);
  min-width: 40px;
  text-align: right;
}

/* Details Section */
.details-section {
  margin-bottom: var(--space-8);
}

:deep(.collection-tabs) {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}

:deep(.collection-tabs .el-tabs__header) {
  background: var(--surface-2);
  border-bottom: 1px solid var(--border-subtle);
}

:deep(.collection-tabs .el-tabs__item) {
  color: var(--text-secondary);
}

:deep(.collection-tabs .el-tabs__item.is-active) {
  color: var(--primary-400);
}

:deep(.collection-tabs .el-tabs__content) {
  padding: var(--space-4);
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  max-height: 400px;
  overflow-y: auto;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  background: var(--surface-2);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
}

.detail-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: var(--surface-3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-400);
}

.detail-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.detail-name {
  font-weight: 500;
  color: var(--text-primary);
}

.detail-type {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

/* Records Section */
.records-section {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.records-section .section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5);
  border-bottom: 1px solid var(--border-subtle);
  background: var(--surface-2);
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.header-title h2 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.header-title .el-icon {
  color: var(--accent-amber);
}

.header-filters {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.records-list {
  padding: var(--space-5);
}

.record-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--space-4);
}

.record-card {
  background: var(--surface-2);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  transition: all var(--transition-base);
}

.record-card:hover {
  border-color: var(--border-default);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.record-rank {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-tertiary);
}

.record-rank.top {
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.record-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-subtle);
}

.record-stat {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.stat-name {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  text-transform: uppercase;
}

.stat-number {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-number.highlight {
  color: var(--primary-400);
}

.record-details {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.detail-value {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 500;
}

.detail-value.cause {
  color: var(--text-tertiary);
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-time {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 0.8125rem;
  color: var(--text-tertiary);
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
  padding-top: var(--space-5);
  display: flex;
  justify-content: center;
  border-top: 1px solid var(--border-subtle);
  margin-top: var(--space-5);
}

/* Animations */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* Responsive */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .collection-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .record-cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .player-page {
    padding: var(--space-4);
  }

  .header-content {
    flex-direction: column;
    text-align: center;
    padding: var(--space-6);
  }

  .player-meta {
    flex-direction: column;
    gap: var(--space-2);
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .collection-grid {
    grid-template-columns: 1fr;
  }

  .records-section .section-header {
    flex-direction: column;
    gap: var(--space-3);
    align-items: flex-start;
  }

  .header-filters {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .record-stats {
    grid-template-columns: 1fr;
  }
}
</style>
