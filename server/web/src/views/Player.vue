<template>
  <div class="player-page">
    <!-- Player Header -->
    <div class="player-header">
      <div class="header-bg">
        <div class="bg-pattern"></div>
        <div class="bg-glow"></div>
      </div>
      <div class="header-content">
        <div class="player-avatar-wrapper">
          <div class="avatar-ring ring-1"></div>
          <div class="avatar-ring ring-2"></div>
          <el-avatar :size="120" :icon="UserFilled" class="player-avatar" />
          <div class="online-status" v-if="playerInfo.online">
            <span class="status-dot"></span>
            <span>在线</span>
          </div>
        </div>
        <div class="player-info">
          <h1>{{ playerInfo.name }}</h1>
          <div class="player-tags">
            <el-tag
              :type="playerInfo.role === '管理员' ? 'danger' : 'primary'"
              effect="dark"
              round
              size="large"
              class="role-tag"
            >
              <el-icon v-if="playerInfo.role === '管理员'"><StarFilled /></el-icon>
              <el-icon v-else><User /></el-icon>
              {{ playerInfo.role || '玩家' }}
            </el-tag>
            <el-tag type="success" effect="dark" round size="large" v-if="playerInfo.online">
              <el-icon><CircleCheck /></el-icon>
              游戏中
            </el-tag>
          </div>
          <div class="player-meta">
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>注册于 {{ formatDate(playerInfo.createdAt) }}</span>
            </div>
            <div class="meta-divider"></div>
            <div class="meta-item" v-if="playerInfo.lastLoginAt">
              <el-icon><Timer /></el-icon>
              <span>最后登录 {{ formatDate(playerInfo.lastLoginAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Stats Overview -->
    <div class="stats-section">
      <div class="section-header">
        <div class="section-icon">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <h2 class="section-title">游戏统计</h2>
      </div>
      <div class="stats-grid">
        <div class="stat-card" v-for="(stat, index) in gameStats" :key="index">
          <div class="stat-glow"></div>
          <div class="stat-icon" :style="{ background: stat.gradient }">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-decoration"></div>
        </div>
      </div>
    </div>

    <!-- Collection Progress -->
    <div class="collection-section">
      <div class="section-header">
        <div class="section-icon collection">
          <el-icon><Collection /></el-icon>
        </div>
        <h2 class="section-title">收集进度</h2>
      </div>
      <div class="collection-grid">
        <!-- Achievement -->
        <div class="collection-card">
          <div class="card-glow"></div>
          <div class="collection-header">
            <div class="collection-icon" style="background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%)">
              <el-icon :size="28"><Trophy /></el-icon>
            </div>
            <div class="collection-info">
              <h3>成就</h3>
              <p>{{ playerInfo.achievementCount || 0 }} 个解锁</p>
            </div>
          </div>
          <div class="collection-progress">
            <div class="progress-bar">
              <div class="progress-fill gold" :style="{ width: '100%' }"></div>
            </div>
            <span class="progress-text">已获得</span>
          </div>
        </div>

        <!-- Bestiary -->
        <div class="collection-card">
          <div class="card-glow"></div>
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
              <div class="progress-fill rose" :style="{ width: bestiaryProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ bestiaryProgress }}%</span>
          </div>
        </div>

        <!-- Catalog -->
        <div class="collection-card">
          <div class="card-glow"></div>
          <div class="collection-header">
            <div class="collection-icon" style="background: linear-gradient(135deg, #06b6d4 0%, #22d3ee 100%)">
              <el-icon :size="28"><Goods /></el-icon>
            </div>
            <div class="collection-info">
              <h3>物品图鉴</h3>
              <p>{{ playerInfo.catalogSeen || 0 }} / {{ playerInfo.catalogTotal || 0 }} 已收集</p>
            </div>
          </div>
          <div class="collection-progress">
            <div class="progress-bar">
              <div class="progress-fill cyan" :style="{ width: catalogProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ catalogProgress }}%</span>
          </div>
        </div>

        <!-- Documents -->
        <div class="collection-card">
          <div class="card-glow"></div>
          <div class="collection-header">
            <div class="collection-icon" style="background: linear-gradient(135deg, #10b981 0%, #34d399 100%)">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="collection-info">
              <h3>文档日志</h3>
              <p>{{ playerInfo.documentFound || 0 }} / {{ playerInfo.documentTotal || 0 }} 已发现</p>
            </div>
          </div>
          <div class="collection-progress">
            <div class="progress-bar">
              <div class="progress-fill emerald" :style="{ width: documentProgress + '%' }"></div>
            </div>
            <span class="progress-text">{{ documentProgress }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Collection Details -->
    <div class="details-section" v-if="hasCollectionData">
      <div class="section-header">
        <div class="section-icon details">
          <el-icon><List /></el-icon>
        </div>
        <h2 class="section-title">详细数据</h2>
      </div>
      <div class="details-tabs">
        <div class="tabs-header">
          <button
            v-for="tab in availableTabs"
            :key="tab.key"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            <el-icon><component :is="tab.icon" /></el-icon>
            <span>{{ tab.label }}</span>
            <el-tag size="small" effect="dark" round class="tab-count">{{ tab.count }}</el-tag>
          </button>
        </div>
        <div class="tabs-content">
          <!-- Bestiary Tab -->
          <div v-if="activeTab === 'bestiary'" class="tab-panel">
            <div class="detail-grid">
              <div v-for="(item, index) in playerInfo.bestiaryList" :key="index" class="detail-card">
                <div class="detail-icon">
                  <el-icon><Warning /></el-icon>
                </div>
                <div class="detail-info">
                  <span class="detail-name">{{ formatEntityName(item.entity) }}</span>
                  <span class="detail-type">{{ item.type }}</span>
                </div>
                <div class="detail-badge">
                  <el-icon><View /></el-icon>
                  {{ item.encountered }}
                </div>
              </div>
            </div>
          </div>

          <!-- Catalog Tab -->
          <div v-if="activeTab === 'catalog'" class="tab-panel">
            <div class="detail-grid">
              <div v-for="(item, index) in playerInfo.catalogList" :key="index" class="detail-card">
                <div class="detail-icon cyan">
                  <el-icon><Goods /></el-icon>
                </div>
                <div class="detail-info">
                  <span class="detail-name">{{ formatItemName(item.item) }}</span>
                  <span class="detail-type">{{ item.type }}</span>
                </div>
                <div class="detail-badge cyan">
                  <el-icon><CircleCheck /></el-icon>
                  {{ item.useCount }}
                </div>
              </div>
            </div>
          </div>

          <!-- Documents Tab -->
          <div v-if="activeTab === 'documents'" class="tab-panel">
            <div class="detail-grid">
              <div v-for="(item, index) in playerInfo.documentList" :key="index" class="detail-card">
                <div class="detail-icon emerald">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="detail-info">
                  <span class="detail-name">{{ formatPageName(item.page) }}</span>
                  <span class="detail-type">{{ item.type }}</span>
                </div>
                <div class="detail-badge emerald">
                  <el-icon><CircleCheckFilled /></el-icon>
                  已发现
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Records Section -->
    <div class="records-section">
      <div class="section-header-bar">
        <div class="header-title">
          <div class="title-icon">
            <el-icon><Trophy /></el-icon>
          </div>
          <div class="title-content">
            <h2>游戏记录</h2>
            <span class="record-count">{{ totalPages * size }} 场对局</span>
          </div>
        </div>
        <div class="header-filters">
          <el-select v-model="filters.sortBy" @change="loadRecords" class="filter-select">
            <el-option label="最新记录" value="id">
              <el-icon><Clock /></el-icon> 最新记录
            </el-option>
            <el-option label="分数最高" value="score">
              <el-icon><TrendCharts /></el-icon> 分数最高
            </el-option>
            <el-option label="层数最深" value="depth">
              <el-icon><OfficeBuilding /></el-icon> 层数最深
            </el-option>
          </el-select>
          <el-checkbox v-model="filters.winOnly" @change="loadRecords" class="win-checkbox">
            <span class="checkbox-text">只显示胜利</span>
          </el-checkbox>
        </div>
      </div>

      <div class="records-content" v-loading="loading">
        <div v-if="!loading && records.length === 0" class="empty-state">
          <div class="empty-visual">
            <div class="empty-glow"></div>
            <el-icon :size="64"><Trophy /></el-icon>
          </div>
          <h3>暂无游戏记录</h3>
          <p>该玩家还没有上传过游戏记录</p>
        </div>

        <div v-else class="record-cards">
          <div v-for="(record, index) in records" :key="index" class="record-card">
            <div class="card-shine"></div>
            <div class="record-header">
              <div class="record-rank" :class="{ 'top': index < 3 && page === 0 }">
                <el-icon v-if="index < 3 && page === 0"><Medal /></el-icon>
                <span>#{{ page * size + index + 1 }}</span>
              </div>
              <el-tag
                :type="record.win ? 'success' : 'danger'"
                effect="dark"
                round
                size="small"
                class="result-tag"
              >
                <el-icon v-if="record.win"><CircleCheckFilled /></el-icon>
                <el-icon v-else><CircleCloseFilled /></el-icon>
                {{ record.win ? '胜利' : '失败' }}
              </el-tag>
            </div>

            <div class="record-stats">
              <div class="record-stat highlight">
                <span class="stat-name">分数</span>
                <span class="stat-number">{{ record.score.toLocaleString() }}</span>
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
                <el-tag type="primary" size="small" effect="dark" round class="hero-tag">
                  {{ getHeroClassName(record.class) }}
                </el-tag>
              </div>
              <div class="detail-row">
                <span class="detail-label">挑战</span>
                <span class="detail-value">
                  <el-tag v-if="countChallenges(record.challenges) > 0" type="warning" size="small" effect="dark" round>
                    <el-icon><Lightning /></el-icon>
                    {{ countChallenges(record.challenges) }}
                  </el-tag>
                  <span v-else class="no-challenge">无</span>
                </span>
              </div>
              <div class="detail-row">
                <span class="detail-label">模式</span>
                <el-tag :type="record.gameMode === 'DAILY' ? 'success' : 'info'" size="small" effect="dark" round>
                  {{ getGameModeName(record.gameMode) }}
                </el-tag>
              </div>
              <div class="detail-row" v-if="record.cause">
                <span class="detail-label">死因</span>
                <span class="detail-value cause" :title="formatCause(record.cause)">
                  {{ formatCause(record.cause) }}
                </span>
              </div>
            </div>

            <div class="record-footer">
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
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadRecords"
            @current-change="loadRecords"
            background
            class="custom-pagination"
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
  View, Goods, Document, Warning, List, StarFilled, User,
  CircleCheckFilled, CircleCloseFilled, Lightning, Medal as MedalIcon
} from '@element-plus/icons-vue'
import { playerApi, leaderboardApi } from '../api'

const route = useRoute()
const playerName = computed(() => route.params.name)
const activeTab = ref('bestiary')

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

const availableTabs = computed(() => {
  const tabs = []
  if (playerInfo.value.bestiaryList?.length > 0) {
    tabs.push({ key: 'bestiary', label: '怪物图鉴', icon: Warning, count: playerInfo.value.bestiaryList.length })
  }
  if (playerInfo.value.catalogList?.length > 0) {
    tabs.push({ key: 'catalog', label: '物品图鉴', icon: Goods, count: playerInfo.value.catalogList.length })
  }
  if (playerInfo.value.documentList?.length > 0) {
    tabs.push({ key: 'documents', label: '文档日志', icon: Document, count: playerInfo.value.documentList.length })
  }
  return tabs
})

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

const gameStats = computed(() => [
  {
    label: '总分数',
    value: (playerInfo.value.totalScore || 0).toLocaleString(),
    icon: 'TrendCharts',
    gradient: 'linear-gradient(135deg, #8b5cf6 0%, #a855f7 100%)'
  },
  {
    label: '最高分数',
    value: (playerInfo.value.maxScore || 0).toLocaleString(),
    icon: 'Trophy',
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%)'
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
    gradient: 'linear-gradient(135deg, #06b6d4 0%, #22d3ee 100%)'
  },
  {
    label: '游戏次数',
    value: playerInfo.value.totalGames || 0,
    icon: 'FirstAidKit',
    gradient: 'linear-gradient(135deg, #10b981 0%, #34d399 100%)'
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
      if (availableTabs.value.length > 0) {
        activeTab.value = availableTabs.value[0].key
      }
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
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-6) var(--content-padding);
}

/* Header */
.player-header {
  position: relative;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  margin-bottom: var(--space-6);
}

.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.15) 0%, rgba(168, 85, 247, 0.1) 100%);
  border-radius: var(--radius-2xl);
}

.header-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--surface-1);
  opacity: 0.95;
}

.bg-pattern {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle at 20% 50%, rgba(139, 92, 246, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 50%, rgba(6, 182, 212, 0.1) 0%, transparent 50%);
}

.bg-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.5), transparent);
}

.header-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-6);
  padding: var(--space-10) var(--space-8);
}

.player-avatar-wrapper {
  position: relative;
}

.avatar-ring {
  position: absolute;
  border: 2px solid rgba(139, 92, 246, 0.3);
  border-radius: 50%;
}

.avatar-ring.ring-1 {
  inset: -8px;
  animation: rotate 15s linear infinite;
}

.avatar-ring.ring-2 {
  inset: -16px;
  border-color: rgba(6, 182, 212, 0.2);
  animation: rotate 20s linear infinite reverse;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.player-avatar {
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  border: 3px solid rgba(139, 92, 246, 0.3);
  box-shadow: 0 0 40px rgba(139, 92, 246, 0.3);
}

.online-status {
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  background: rgba(16, 185, 129, 0.2);
  border: 1px solid rgba(16, 185, 129, 0.3);
  border-radius: var(--radius-full);
  color: #10b981;
  font-size: 0.75rem;
  font-weight: 600;
  backdrop-filter: blur(8px);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 6px #10b981;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.player-info {
  flex: 1;
}

.player-info h1 {
  font-size: 2.5rem;
  font-weight: 800;
  margin: 0 0 var(--space-3);
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.player-tags {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.role-tag :deep(.el-icon) {
  margin-right: 4px;
}

.player-meta {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.meta-divider {
  width: 4px;
  height: 4px;
  background: var(--text-muted);
  border-radius: 50%;
}

/* Section Header */
.section-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}

.section-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(168, 85, 247, 0.1));
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a78bfa;
  font-size: 1.25rem;
}

.section-icon.collection {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2), rgba(251, 191, 36, 0.1));
  border-color: rgba(245, 158, 11, 0.3);
  color: #fbbf24;
}

.section-icon.details {
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.2), rgba(34, 211, 238, 0.1));
  border-color: rgba(6, 182, 212, 0.3);
  color: #22d3ee;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0;
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
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: rgba(139, 92, 246, 0.3);
  box-shadow: var(--shadow-glow-sm);
}

.stat-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.5), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.stat-card:hover .stat-glow {
  opacity: 1;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
  position: relative;
  z-index: 1;
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

.stat-decoration {
  position: absolute;
  right: -20px;
  top: -20px;
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.1) 0%, transparent 70%);
  border-radius: 50%;
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
  position: relative;
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  overflow: hidden;
  transition: all 0.3s ease;
}

.collection-card:hover {
  border-color: rgba(139, 92, 246, 0.3);
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow-sm);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.3), transparent);
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
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
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
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: var(--radius-full);
  transition: width 0.5s ease;
}

.progress-fill.gold {
  background: linear-gradient(90deg, #f59e0b, #fbbf24);
}

.progress-fill.rose {
  background: linear-gradient(90deg, #ec4899, #f43f5e);
}

.progress-fill.cyan {
  background: linear-gradient(90deg, #06b6d4, #22d3ee);
}

.progress-fill.emerald {
  background: linear-gradient(90deg, #10b981, #34d399);
}

.progress-text {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  min-width: 40px;
  text-align: right;
}

/* Details Section */
.details-section {
  margin-bottom: var(--space-8);
}

.details-tabs {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.tabs-header {
  display: flex;
  gap: var(--space-1);
  padding: var(--space-2);
  background: rgba(20, 20, 35, 0.5);
  border-bottom: 1px solid var(--border-subtle);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  background: rgba(139, 92, 246, 0.1);
  color: var(--text-primary);
}

.tab-btn.active {
  background: rgba(139, 92, 246, 0.15);
  border-color: rgba(139, 92, 246, 0.3);
  color: #a78bfa;
}

.tab-count {
  margin-left: var(--space-1);
}

.tabs-content {
  padding: var(--space-5);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-3);
}

.detail-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: rgba(20, 20, 35, 0.5);
  border: 1px solid rgba(139, 92, 246, 0.1);
  border-radius: var(--radius-lg);
  transition: all 0.3s ease;
}

.detail-card:hover {
  border-color: rgba(139, 92, 246, 0.3);
  background: rgba(139, 92, 246, 0.05);
}

.detail-icon {
  width: 40px;
  height: 40px;
  background: rgba(139, 92, 246, 0.1);
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a78bfa;
  flex-shrink: 0;
}

.detail-icon.cyan {
  background: rgba(6, 182, 212, 0.1);
  border-color: rgba(6, 182, 212, 0.2);
  color: #22d3ee;
}

.detail-icon.emerald {
  background: rgba(16, 185, 129, 0.1);
  border-color: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

.detail-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.detail-name {
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.detail-type {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.detail-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: rgba(139, 92, 246, 0.1);
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  color: #a78bfa;
  font-weight: 500;
}

.detail-badge.cyan {
  background: rgba(6, 182, 212, 0.1);
  border-color: rgba(6, 182, 212, 0.2);
  color: #22d3ee;
}

.detail-badge.emerald {
  background: rgba(16, 185, 129, 0.1);
  border-color: rgba(16, 185, 129, 0.2);
  color: #10b981;
}

/* Records Section */
.records-section {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
}

.section-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-6);
  background: rgba(20, 20, 35, 0.5);
  border-bottom: 1px solid var(--border-subtle);
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.title-icon {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2), rgba(251, 191, 36, 0.1));
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fbbf24;
  font-size: 1.5rem;
}

.title-content h2 {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0 0 2px;
  color: var(--text-primary);
}

.record-count {
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.header-filters {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.filter-select {
  width: 160px;
}

.filter-select :deep(.el-input__wrapper) {
  background: rgba(15, 15, 25, 0.6);
  border: 1px solid rgba(139, 92, 246, 0.2);
  box-shadow: none;
}

.win-checkbox {
  --el-checkbox-text-color: var(--text-secondary);
  --el-checkbox-checked-text-color: var(--text-primary);
}

.checkbox-text {
  font-size: 0.875rem;
}

.records-content {
  padding: var(--space-6);
}

.record-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--space-4);
}

.record-card {
  position: relative;
  background: rgba(20, 20, 35, 0.5);
  border: 1px solid rgba(139, 92, 246, 0.1);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  overflow: hidden;
  transition: all 0.3s ease;
}

.record-card:hover {
  border-color: rgba(139, 92, 246, 0.3);
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow-sm);
}

.card-shine {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.3), transparent);
}

.record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.record-rank {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--text-tertiary);
}

.record-rank.top {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.result-tag :deep(.el-icon) {
  margin-right: 4px;
}

.record-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid rgba(139, 92, 246, 0.1);
}

.record-stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.record-stat.highlight .stat-number {
  color: #a78bfa;
  font-size: 1.25rem;
}

.stat-name {
  font-size: 0.6875rem;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-number {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
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
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.detail-value {
  font-size: 0.8125rem;
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

.no-challenge {
  color: var(--text-muted);
}

.hero-tag {
  background: rgba(139, 92, 246, 0.2);
  border: 1px solid rgba(139, 92, 246, 0.3);
}

.record-footer {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 0.75rem;
  color: var(--text-tertiary);
  padding-top: var(--space-3);
  border-top: 1px solid rgba(139, 92, 246, 0.1);
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-16);
  text-align: center;
}

.empty-visual {
  position: relative;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
}

.empty-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.2) 0%, transparent 70%);
  animation: pulse 2s ease-in-out infinite;
}

.empty-state h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.empty-state p {
  color: var(--text-secondary);
  margin: 0;
}

/* Pagination */
.pagination-wrapper {
  padding-top: var(--space-6);
  display: flex;
  justify-content: center;
  border-top: 1px solid var(--border-subtle);
  margin-top: var(--space-6);
}

.custom-pagination :deep(.el-pagination__total) {
  color: var(--text-secondary);
}

.custom-pagination :deep(.el-pagination__jump) {
  color: var(--text-secondary);
}

/* Responsive */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .collection-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
    padding: var(--space-8) var(--space-5);
  }

  .player-info h1 {
    font-size: 1.75rem;
  }

  .player-meta {
    flex-direction: column;
    gap: var(--space-2);
  }

  .meta-divider {
    display: none;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .collection-grid {
    grid-template-columns: 1fr;
  }

  .section-header-bar {
    flex-direction: column;
    gap: var(--space-4);
    align-items: flex-start;
  }

  .header-filters {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .filter-select {
    width: 100%;
  }

  .record-cards {
    grid-template-columns: 1fr;
  }

  .record-stats {
    grid-template-columns: 1fr;
  }

  .tabs-header {
    flex-direction: column;
  }

  .tab-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>