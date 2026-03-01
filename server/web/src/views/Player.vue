<template>
  <div class="player-page">
    <!-- Player Header -->
    <div class="player-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :icon="UserFilled" class="player-avatar" />
            <div class="avatar-glow"></div>
          </div>
          <div class="player-info">
            <h1 class="player-name">
              <span v-if="playerInfo?.prefix" class="player-prefix" :style="getPrefixStyle(playerInfo.prefix)">{{ playerInfo.prefix.displayText }}</span>
              {{ playerInfo?.name || '加载中...' }}
            </h1>
            <div class="player-badges">
              <el-tag :type="getRoleType(playerInfo?.role)" effect="dark" round size="large">
                {{ playerInfo?.role || '玩家' }}
              </el-tag>
              <el-tag
                v-if="playerInfo?.online"
                type="success"
                effect="dark"
                round
                size="large"
              >
                <span class="online-indicator"></span>
                <span>在线</span>
              </el-tag>
            </div>
          </div>
          <div class="player-rank-section" v-if="playerRank">
            <div class="rank-badge">
              <el-icon :size="20"><Medal /></el-icon>
              <span class="rank-text">排名</span>
              <span class="rank-number">#{{ playerRank }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Player Content -->
    <div class="player-content">
      <!-- Stats Cards -->
      <div class="stats-grid">
        <div class="stat-card" v-for="(stat, index) in statsList" :key="index">
          <div class="stat-glow" :style="{ background: stat.glow }"></div>
          <div class="stat-icon-wrapper" :style="{ background: stat.gradient }">
            <el-icon :size="24" color="white"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>

      <!-- Main Grid -->
      <div class="main-grid">
        <!-- Left Column -->
        <div class="left-column">
          <!-- Player Info Card -->
          <div class="info-card">
            <div class="card-header">
              <div class="header-icon-wrapper" style="background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);">
                <el-icon :size="20" color="white"><User /></el-icon>
              </div>
              <h3>玩家信息</h3>
            </div>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">用户名</span>
                <span class="info-value">{{ playerInfo?.name }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">角色</span>
                <span class="info-value">
                  <el-tag :type="getRoleType(playerInfo?.role)" effect="dark" round size="small">
                    {{ playerInfo?.role }}
                  </el-tag>
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">注册时间</span>
                <span class="info-value">{{ formatDateTimeWithAgo(playerInfo?.createdAt) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">当前状态</span>
                <span class="info-value">
                  <el-tag :type="playerInfo?.online ? 'success' : 'info'" effect="dark" round size="small">
                    {{ playerInfo?.online ? '在线' : '离线' }}
                  </el-tag>
                </span>
              </div>
            </div>
          </div>

          <!-- Recent Games -->
          <div class="info-card">
            <div class="card-header">
              <div class="header-icon-wrapper" style="background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);">
                <el-icon :size="20" color="white"><Clock /></el-icon>
              </div>
              <h3>最近游戏</h3>
            </div>
            <div class="games-list" v-if="recentGames.length > 0">
              <div v-for="(game, index) in recentGames" :key="index" class="game-item">
                <div class="game-icon" :class="getResultClass(game.result)">
                  <el-icon><component :is="getResultIcon(game.result)" /></el-icon>
                </div>
                <div class="game-info">
                  <div class="game-header">
                    <span class="game-result" :class="getResultClass(game.result)">
                      {{ game.result }}
                    </span>
                    <span class="game-time">{{ formatTimeAgo(game.endTime) }}</span>
                  </div>
                  <div class="game-stats">
                    <span><el-icon><Trophy /></el-icon> {{ game.score }} 分</span>
                    <span><el-icon><Location /></el-icon> 第 {{ game.floor }} 层</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-games">
              <el-icon :size="32"><Clock /></el-icon>
              <span>暂无游戏记录</span>
            </div>
          </div>
        </div>

        <!-- Right Column -->
        <div class="right-column">
          <!-- Game Progress -->
          <div class="info-card">
            <div class="card-header">
              <div class="header-icon-wrapper" style="background: linear-gradient(135deg, #10b981 0%, #06b6d4 100%);">
                <el-icon :size="20" color="white"><Collection /></el-icon>
              </div>
              <h3>游戏进度</h3>
            </div>
            <div class="progress-list">
              <!-- 成就 -->
              <div class="progress-item" @click="toggleDetail('achievement')">
                <div class="progress-icon" style="background: rgba(245, 158, 11, 0.12); color: #f59e0b;">
                  <el-icon><Trophy /></el-icon>
                </div>
                <div class="progress-info">
                  <span class="progress-name">成就</span>
                  <span class="progress-desc">{{ playerInfo?.achievementCount || 0 }}/{{ playerInfo?.achievementTotal || 94 }}</span>
                </div>
                <div class="progress-right">
                  <div class="progress-bar-mini">
                    <div class="progress-fill-mini" :style="{ width: ((playerInfo?.achievementCount || 0) / (playerInfo?.achievementTotal || 94) * 100) + '%' }"></div>
                  </div>
                  <el-icon class="expand-icon" :class="{ expanded: expandedDetail === 'achievement' }"><ArrowRight /></el-icon>
                </div>
              </div>

              <!-- 图鉴主分类 -->
              <div class="progress-item" @click="toggleDetail('catalog')">
                <div class="progress-icon" style="background: rgba(139, 92, 246, 0.12); color: #8b5cf6;">
                  <el-icon><View /></el-icon>
                </div>
                <div class="progress-info">
                  <span class="progress-name">图鉴</span>
                  <span class="progress-desc">{{ totalCatalogSeen }}/{{ totalCatalogCount }}</span>
                </div>
                <div class="progress-right">
                  <div class="progress-bar-mini">
                    <div class="progress-fill-mini" :style="{ width: (totalCatalogSeen / totalCatalogCount * 100) + '%' }"></div>
                  </div>
                  <el-icon class="expand-icon" :class="{ expanded: expandedDetail === 'catalog' }"><ArrowRight /></el-icon>
                </div>
              </div>

              <!-- 图鉴展开详情 -->
              <div v-if="expandedDetail === 'catalog'" class="progress-detail">
                <!-- 装备 -->
                <div class="detail-section">
                  <div class="detail-header" @click="toggleSubDetail('equipment')">
                    <span class="detail-title">装备</span>
                    <span class="detail-count">{{ playerInfo?.equipmentSeen || 0 }}/{{ playerInfo?.equipmentTotal || 165 }}</span>
                    <el-icon class="expand-icon" :class="{ expanded: expandedSubDetail === 'equipment' }"><ArrowRight /></el-icon>
                  </div>
                  <div v-if="expandedSubDetail === 'equipment'" class="detail-items">
                    <div class="detail-item"><span>近战武器</span><span>{{ getEquipmentDetail('MELEE_WEAPONS') }}</span></div>
                    <div class="detail-item"><span>护甲</span><span>{{ getEquipmentDetail('ARMOR') }}</span></div>
                    <div class="detail-item"><span>附魔与诅咒</span><span>{{ getEquipmentDetail('ENCHANTMENTS') }}</span></div>
                    <div class="detail-item"><span>刻印与诅咒</span><span>{{ getEquipmentDetail('GLYPHS') }}</span></div>
                    <div class="detail-item"><span>投掷武器</span><span>{{ getEquipmentDetail('THROWN_WEAPONS') }}</span></div>
                    <div class="detail-item"><span>法杖</span><span>{{ getEquipmentDetail('WANDS') }}</span></div>
                    <div class="detail-item"><span>戒指</span><span>{{ getEquipmentDetail('RINGS') }}</span></div>
                    <div class="detail-item"><span>神器</span><span>{{ getEquipmentDetail('ARTIFACTS') }}</span></div>
                    <div class="detail-item"><span>饰品</span><span>{{ getEquipmentDetail('TRINKETS') }}</span></div>
                    <div class="detail-item"><span>杂项装备</span><span>{{ getEquipmentDetail('MISC_EQUIPMENT') }}</span></div>
                  </div>
                </div>
                <!-- 消耗品 -->
                <div class="detail-section">
                  <div class="detail-header" @click="toggleSubDetail('consumables')">
                    <span class="detail-title">消耗品</span>
                    <span class="detail-count">{{ playerInfo?.consumablesSeen || 0 }}/{{ playerInfo?.consumablesTotal || 161 }}</span>
                    <el-icon class="expand-icon" :class="{ expanded: expandedSubDetail === 'consumables' }"><ArrowRight /></el-icon>
                  </div>
                  <div v-if="expandedSubDetail === 'consumables'" class="detail-items">
                    <div class="detail-item"><span>药剂</span><span>{{ getConsumableDetail('POTIONS') }}</span></div>
                    <div class="detail-item"><span>卷轴</span><span>{{ getConsumableDetail('SCROLLS') }}</span></div>
                    <div class="detail-item"><span>种子</span><span>{{ getConsumableDetail('SEEDS') }}</span></div>
                    <div class="detail-item"><span>符石</span><span>{{ getConsumableDetail('STONES') }}</span></div>
                    <div class="detail-item"><span>食物</span><span>{{ getConsumableDetail('FOOD') }}</span></div>
                    <div class="detail-item"><span>合剂</span><span>{{ getConsumableDetail('EXOTIC_POTIONS') }}</span></div>
                    <div class="detail-item"><span>秘卷</span><span>{{ getConsumableDetail('EXOTIC_SCROLLS') }}</span></div>
                    <div class="detail-item"><span>炸弹</span><span>{{ getConsumableDetail('BOMBS') }}</span></div>
                    <div class="detail-item"><span>涂药飞镖</span><span>{{ getConsumableDetail('TIPPED_DARTS') }}</span></div>
                    <div class="detail-item"><span>魔药与秘药</span><span>{{ getConsumableDetail('BREWS_ELIXIRS') }}</span></div>
                    <div class="detail-item"><span>法术结晶</span><span>{{ getConsumableDetail('SPELLS') }}</span></div>
                    <div class="detail-item"><span>杂项消耗品</span><span>{{ getConsumableDetail('MISC_CONSUMABLES') }}</span></div>
                  </div>
                </div>
                <!-- 单位图鉴 -->
                <div class="detail-section">
                  <div class="detail-header" @click="toggleSubDetail('bestiary')">
                    <span class="detail-title">单位图鉴</span>
                    <span class="detail-count">{{ playerInfo?.bestiarySeen || 0 }}/{{ playerInfo?.bestiaryTotal || 143 }}</span>
                    <el-icon class="expand-icon" :class="{ expanded: expandedSubDetail === 'bestiary' }"><ArrowRight /></el-icon>
                  </div>
                  <div v-if="expandedSubDetail === 'bestiary'" class="detail-items">
                    <div class="detail-item"><span>区域敌人</span><span>{{ getBestiaryDetail('REGIONAL') }}</span></div>
                    <div class="detail-item"><span>区域Boss</span><span>{{ getBestiaryDetail('BOSSES') }}</span></div>
                    <div class="detail-item"><span>全局敌人</span><span>{{ getBestiaryDetail('UNIVERSAL') }}</span></div>
                    <div class="detail-item"><span>稀有敌人</span><span>{{ getBestiaryDetail('RARE') }}</span></div>
                    <div class="detail-item"><span>任务敌人与Boss</span><span>{{ getBestiaryDetail('QUEST') }}</span></div>
                    <div class="detail-item"><span>中立角色</span><span>{{ getBestiaryDetail('NEUTRAL') }}</span></div>
                    <div class="detail-item"><span>盟友</span><span>{{ getBestiaryDetail('ALLY') }}</span></div>
                    <div class="detail-item"><span>陷阱</span><span>{{ getBestiaryDetail('TRAP') }}</span></div>
                    <div class="detail-item"><span>植物</span><span>{{ getBestiaryDetail('PLANT') }}</span></div>
                  </div>
                </div>
                <!-- 背景故事 -->
                <div class="detail-section">
                  <div class="detail-header" @click="toggleSubDetail('lore')">
                    <span class="detail-title">背景故事</span>
                    <span class="detail-count">{{ playerInfo?.loreFound || 0 }}/{{ playerInfo?.loreTotal || 36 }}</span>
                    <el-icon class="expand-icon" :class="{ expanded: expandedSubDetail === 'lore' }"><ArrowRight /></el-icon>
                  </div>
                  <div v-if="expandedSubDetail === 'lore'" class="detail-items">
                    <div class="detail-item"><span>地牢区域介绍</span><span>{{ getLoreDetail('INTROS') }}</span></div>
                    <div class="detail-item"><span>巡逻队员的信件</span><span>{{ getLoreDetail('SEWERS_GUARD') }}</span></div>
                    <div class="detail-item"><span>监狱长日志</span><span>{{ getLoreDetail('PRISON_WARDEN') }}</span></div>
                    <div class="detail-item"><span>探险者日志</span><span>{{ getLoreDetail('CAVES_EXPLORER') }}</span></div>
                    <div class="detail-item"><span>矮人术士手记</span><span>{{ getLoreDetail('CITY_WARLOCK') }}</span></div>
                    <div class="detail-item"><span>？？？录</span><span>{{ getLoreDetail('HALLS_KING') }}</span></div>
                  </div>
                </div>
              </div>

              <!-- 地牢指南 -->
              <div class="progress-item" @click="toggleDetail('guide')">
                <div class="progress-icon" style="background: rgba(16, 185, 129, 0.12); color: #10b981;">
                  <el-icon><Notebook /></el-icon>
                </div>
                <div class="progress-info">
                  <span class="progress-name">地牢指南</span>
                  <span class="progress-desc">{{ playerInfo?.adventurersGuideFound || 0 }}/{{ playerInfo?.adventurersGuideTotal || 13 }}</span>
                </div>
                <div class="progress-right">
                  <div class="progress-bar-mini">
                    <div class="progress-fill-mini" :style="{ width: ((playerInfo?.adventurersGuideFound || 0) / (playerInfo?.adventurersGuideTotal || 13) * 100) + '%' }"></div>
                  </div>
                  <el-icon class="expand-icon" :class="{ expanded: expandedDetail === 'guide' }"><ArrowRight /></el-icon>
                </div>
              </div>

              <!-- 炼金指南 -->
              <div class="progress-item" @click="toggleDetail('alchemy')">
                <div class="progress-icon" style="background: rgba(6, 182, 212, 0.12); color: #06b6d4;">
                  <el-icon><FirstAidKit /></el-icon>
                </div>
                <div class="progress-info">
                  <span class="progress-name">炼金指南</span>
                  <span class="progress-desc">{{ playerInfo?.alchemyGuideFound || 0 }}/{{ playerInfo?.alchemyGuideTotal || 9 }}</span>
                </div>
                <div class="progress-right">
                  <div class="progress-bar-mini">
                    <div class="progress-fill-mini" :style="{ width: ((playerInfo?.alchemyGuideFound || 0) / (playerInfo?.alchemyGuideTotal || 9) * 100) + '%' }"></div>
                  </div>
                  <el-icon class="expand-icon" :class="{ expanded: expandedDetail === 'alchemy' }"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  UserFilled, User, Trophy, Location, Medal, TrendCharts,
  Clock, StarFilled, CircleCheckFilled, Check, Close, Warning,
  Collection, Goods, Document, Flag, View, Notebook, FirstAidKit, ArrowRight
} from '@element-plus/icons-vue'
import { playerApi, leaderboardApi } from '../api'

const route = useRoute()
const playerInfo = ref(null)
const playerRank = ref(null)
const rankDiff = ref(0)
const recentGames = ref([])
const loading = ref(false)
const expandedDetail = ref(null)
const expandedSubDetail = ref(null)

// 图鉴总数量计算
const totalCatalogCount = computed(() => {
  return (playerInfo.value?.equipmentTotal || 165) +
         (playerInfo.value?.consumablesTotal || 161) +
         (playerInfo.value?.bestiaryTotal || 137) +
         (playerInfo.value?.loreTotal || 37)
})

const totalCatalogSeen = computed(() => {
  return (playerInfo.value?.equipmentSeen || 0) +
         (playerInfo.value?.consumablesSeen || 0) +
         (playerInfo.value?.bestiarySeen || 0) +
         (playerInfo.value?.loreFound || 0)
})

const toggleDetail = (detail) => {
  expandedDetail.value = expandedDetail.value === detail ? null : detail
  expandedSubDetail.value = null
}

const toggleSubDetail = (subDetail) => {
  expandedSubDetail.value = expandedSubDetail.value === subDetail ? null : subDetail
}

const getEquipmentDetail = (type) => {
  const detail = playerInfo.value?.equipmentDetails?.[type]
  if (!detail) return '0/0'
  return `${detail.seen}/${detail.total}`
}

const getConsumableDetail = (type) => {
  const detail = playerInfo.value?.consumablesDetails?.[type]
  if (!detail) return '0/0'
  return `${detail.seen}/${detail.total}`
}

const getBestiaryDetail = (type) => {
  const detail = playerInfo.value?.bestiaryDetails?.[type]
  if (!detail) return '0/0'
  return `${detail.seen}/${detail.total}`
}

const getLoreDetail = (type) => {
  const detail = playerInfo.value?.loreDetails?.[type]
  if (!detail) return '0/0'
  return `${detail.found}/${detail.total}`
}

const statsList = computed(() => [
  {
    label: '最高分数',
    value: playerInfo.value?.maxScore || 0,
    icon: 'Trophy',
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #f97316 100%)',
    glow: 'rgba(245, 158, 11, 0.2)'
  },
  {
    label: '最高通关挑战',
    value: playerInfo.value?.maxChallengeAmount || 0,
    icon: 'Flag',
    gradient: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
    glow: 'rgba(6, 182, 212, 0.2)'
  },
  {
    label: '总分数',
    value: playerInfo.value?.totalScore || 0,
    icon: 'Medal',
    gradient: 'linear-gradient(135deg, #8b5cf6 0%, #a855f7 100%)',
    glow: 'rgba(139, 92, 246, 0.2)'
  },
  {
    label: '胜利次数',
    value: playerInfo.value?.wins || 0,
    icon: 'StarFilled',
    gradient: 'linear-gradient(135deg, #ec4899 0%, #f43f5e 100%)',
    glow: 'rgba(236, 72, 153, 0.2)'
  }
])

const progressPercent = computed(() => {
  if (!playerInfo.value?.maxScore || !rankDiff.value) return 0
  const base = playerInfo.value.maxScore - rankDiff.value
  if (base <= 0) return 100
  return Math.min(100, Math.round((playerInfo.value.maxScore / (base + rankDiff.value)) * 100))
})

const getRoleType = (role) => {
  const types = {
    '管理员': 'danger',
    '玩家': 'primary'
  }
  return types[role] || 'primary'
}

// SPDNet: 前缀系统 - 获取前缀样式
const getPrefixStyle = (prefix) => {
  return {
    color: prefix.color || '#ffffff',
    backgroundColor: prefix.backgroundColor || 'rgba(139, 92, 246, 0.8)',
    padding: '4px 12px',
    borderRadius: '6px',
    fontSize: '14px',
    fontWeight: 'bold',
    marginRight: '8px',
    display: 'inline-block'
  }
}

const getResultClass = (result) => {
  const classes = {
    '胜利': 'success',
    '失败': 'danger',
    '逃跑': 'warning'
  }
  return classes[result] || 'info'
}

const getResultIcon = (result) => {
  const icons = {
    '胜利': Check,
    '失败': Close,
    '逃跑': Warning
  }
  return icons[result] || CircleCheckFilled
}

const formatDateTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 格式化时间为"多久之前"的形式
const formatTimeAgo = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  const months = Math.floor(days / 30)
  const years = Math.floor(days / 365)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days < 30) return `${days} 天前`
  if (months < 12) return `${months} 个月前`
  return `${years} 年前`
}

// 格式化时间，带括号显示多久之前
const formatDateTimeWithAgo = (time) => {
  if (!time) return '-'
  const dateTime = formatDateTime(time)
  const ago = formatTimeAgo(time)
  return `${dateTime} (${ago})`
}

const loadPlayerInfo = async () => {
  const name = route.params.name
  if (!name) return

  loading.value = true
  try {
    const res = await playerApi.getPlayerPublicInfo(name)
    if (res.data.success) {
      playerInfo.value = res.data.data
      recentGames.value = res.data.data?.recentGames || []
    }
  } catch (error) {
    console.error('获取玩家信息失败:', error)
    ElMessage.error('获取玩家信息失败')
  } finally {
    loading.value = false
  }
}

const loadPlayerRank = async () => {
  try {
    const res = await leaderboardApi.getLeaderboard(0, 1000)
    if (res.data.success) {
      const records = res.data.data?.records || []
      const leaderboard = records.map(record => ({
        name: record.player_name || record.playerName || (record.player?.name) || '未知',
        bestScore: record.score || 0,
        bestFloor: record.maxDepth || 0
      }))
      const index = leaderboard.findIndex(p => p.name === playerInfo.value?.name)
      if (index !== -1) {
        playerRank.value = index + 1
        if (index > 0) {
          rankDiff.value = leaderboard[index - 1].bestScore - leaderboard[index].bestScore
        }
      }
    }
  } catch (error) {
    console.error('获取排名失败:', error)
  }
}

watch(() => route.params.name, () => {
  loadPlayerInfo().then(() => {
    loadPlayerRank()
  })
})

onMounted(() => {
  loadPlayerInfo().then(() => {
    loadPlayerRank()
  })
})
</script>

<style scoped>
.player-page {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--content-padding) var(--space-6);
}

/* Player Header */
.player-header {
  position: relative;
  margin: 0 calc(-1 * var(--content-padding)) var(--space-6);
  padding: var(--space-8) var(--content-padding);
  overflow: hidden;
}

.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(147, 51, 234, 0.15) 0%, rgba(6, 182, 212, 0.1) 100%);
}

.header-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 50% 0%, rgba(147, 51, 234, 0.2) 0%, transparent 70%);
}

.header-content {
  position: relative;
  z-index: 1;
  max-width: var(--max-width);
  margin: 0 auto;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: var(--space-5);
}

.avatar-wrapper {
  position: relative;
}

.player-avatar {
  background: var(--gradient-primary);
  font-size: 2.5rem;
  border: 4px solid var(--surface-1);
  box-shadow: var(--shadow-lg);
}

.avatar-glow {
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  background: var(--gradient-primary);
  filter: blur(20px);
  opacity: 0.3;
  z-index: -1;
}

.player-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.player-name {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.player-prefix {
  display: inline-block;
}

.player-badges {
  display: flex;
  gap: var(--space-2);
}

.online-indicator {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent-emerald);
  margin-right: 4px;
  box-shadow: 0 0 6px var(--accent-emerald);
}

/* Player Rank Section */
.player-rank-section {
  margin-left: auto;
  padding-left: var(--space-6);
}

.rank-badge {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.15) 0%, rgba(244, 63, 94, 0.15) 100%);
  border: 1px solid rgba(236, 72, 153, 0.3);
  border-radius: var(--radius-xl);
  color: var(--text-primary);
}

.rank-badge .el-icon {
  color: #ec4899;
}

.rank-text {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.rank-number {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #ec4899 0%, #f43f5e 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Player Content */
.player-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
  transition: all var(--transition-base);
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: var(--border-default);
  box-shadow: var(--shadow-lg);
}

.stat-glow {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity var(--transition-base);
}

.stat-card:hover .stat-glow {
  opacity: 0.08;
}

.stat-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.stat-info {
  flex: 1;
  position: relative;
  z-index: 1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* Main Grid */
.main-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}

.left-column,
.right-column {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

/* Info Card */
.info-card {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-subtle);
}

.header-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

/* Info List */
.info-list {
  padding: var(--space-2);
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.info-item:hover {
  background: var(--surface-2);
}

.info-label {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.info-value {
  color: var(--text-primary);
  font-weight: 500;
}

/* Games List */
.games-list {
  padding: var(--space-2);
}

.game-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.game-item:hover {
  background: var(--surface-2);
}

.game-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.game-icon.success {
  background: rgba(16, 185, 129, 0.12);
  color: var(--accent-emerald);
}

.game-icon.danger {
  background: rgba(244, 63, 94, 0.12);
  color: var(--accent-rose);
}

.game-icon.warning {
  background: rgba(245, 158, 11, 0.12);
  color: var(--accent-amber);
}

.game-info {
  flex: 1;
  min-width: 0;
}

.game-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-1);
}

.game-result {
  font-weight: 600;
  font-size: 0.875rem;
}

.game-result.success {
  color: var(--accent-emerald);
}

.game-result.danger {
  color: var(--accent-rose);
}

.game-result.warning {
  color: var(--accent-amber);
}

.game-time {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.game-stats {
  display: flex;
  gap: var(--space-3);
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.game-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-games {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-8);
  color: var(--text-tertiary);
}

/* Game Stats */
.game-stats {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
  padding: var(--space-4);
}

.game-stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--surface-2);
  border-radius: var(--radius-lg);
}

.stat-circle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-detail {
  display: flex;
  flex-direction: column;
}

.detail-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
}

.detail-label {
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

/* Rank Info */
.rank-info {
  padding: var(--space-4);
}

.rank-display {
  text-align: center;
  margin-bottom: var(--space-4);
}

.rank-number {
  font-size: 3rem;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
}

.rank-label {
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin-top: var(--space-1);
}

.rank-progress {
  background: var(--surface-2);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--space-2);
  font-size: 0.875rem;
}

.progress-header span:first-child {
  color: var(--text-secondary);
}

.progress-diff {
  color: var(--accent-amber);
  font-weight: 600;
}

.progress-bar {
  height: 8px;
  background: var(--surface-3);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  transition: width 0.5s ease;
}

/* Game Progress */
.progress-list {
  padding: var(--space-2);
}

.progress-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.progress-item:hover {
  background: var(--surface-2);
}

.progress-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.progress-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.progress-name {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 0.9375rem;
}

.progress-desc {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin-top: 2px;
}

.progress-bar-mini {
  width: 60px;
  height: 6px;
  background: var(--surface-3);
  border-radius: var(--radius-full);
  overflow: hidden;
  flex-shrink: 0;
}

.progress-fill-mini {
  height: 100%;
  background: linear-gradient(135deg, #10b981 0%, #06b6d4 100%);
  border-radius: var(--radius-full);
  transition: width 0.5s ease;
}

.progress-right {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.expand-icon {
  color: var(--text-tertiary);
  transition: transform 0.3s ease;
  font-size: 14px;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

/* Progress Detail */
.progress-detail {
  margin-left: var(--space-12);
  margin-bottom: var(--space-2);
  border-left: 2px solid var(--border-subtle);
  padding-left: var(--space-3);
}

.detail-section {
  margin-bottom: var(--space-2);
}

.detail-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.detail-header:hover {
  background: var(--surface-2);
}

.detail-title {
  flex: 1;
  font-weight: 500;
  color: var(--text-primary);
  font-size: 0.875rem;
}

.detail-count {
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.detail-items {
  margin-left: var(--space-8);
  padding: var(--space-1) 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: var(--space-1) var(--space-3);
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.detail-item span:first-child {
  color: var(--text-tertiary);
}

.detail-item span:last-child {
  color: var(--text-secondary);
  font-family: monospace;
}

/* Responsive */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .main-grid {
    grid-template-columns: 1fr;
  }

  .player-rank-section {
    margin-left: 0;
    padding-left: 0;
    margin-top: var(--space-3);
  }
}

@media (max-width: 640px) {
  .player-header {
    padding: var(--space-6) var(--content-padding);
  }

  .avatar-section {
    flex-direction: column;
    text-align: center;
  }

  .player-name {
    font-size: 1.5rem;
  }

  .player-badges {
    justify-content: center;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .game-stats {
    grid-template-columns: 1fr;
  }

  .rank-number {
    font-size: 2rem;
  }
}
</style>
