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
              <span
                v-if="playerInfo?.prefix"
                class="player-prefix clickable-prefix"
                :style="getPrefixStyle(playerInfo.prefix)"
                @click="goToPrefix(playerInfo.prefix)"
                title="点击查看前缀详情"
              >{{ playerInfo.prefix.displayText }}</span>
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

              <!-- 成就展开详情 -->
              <div v-if="expandedDetail === 'achievement'" class="progress-detail">
                <div class="detail-section">
                  <div class="detail-header">
                    <span class="detail-title">已获得成就</span>
                    <span class="detail-count">{{ playerInfo?.achievementCount || 0 }}个</span>
                  </div>
                  <div class="detail-items achievement-items">
                    <div
                      v-for="achievement in (playerInfo?.achievements || [])"
                      :key="achievement"
                      class="detail-item achievement-item"
                    >
                      <el-icon class="achievement-icon"><StarFilled /></el-icon>
                      <span class="achievement-name">{{ formatAchievementName(achievement) }}</span>
                    </div>
                    <div v-if="!playerInfo?.achievements?.length" class="empty-detail">
                      暂无成就
                    </div>
                  </div>
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
                    <div
                      v-for="eqType in ['MELEE_WEAPONS', 'ARMOR', 'ENCHANTMENTS', 'GLYPHS', 'THROWN_WEAPONS', 'WANDS', 'RINGS', 'ARTIFACTS', 'TRINKETS', 'MISC_EQUIPMENT']"
                      :key="eqType"
                      class="detail-item expandable"
                      @click="toggleThirdDetail(eqType)"
                    >
                      <span class="item-label">{{ getEquipmentLabel(eqType) }}</span>
                      <span class="item-count">{{ getEquipmentDetail(eqType) }}</span>
                      <el-icon class="expand-icon-small" :class="{ expanded: expandedThirdDetail === eqType }"><ArrowRight /></el-icon>
                    </div>
                  </div>
                  <!-- 装备第三级展开 -->
                  <div v-if="expandedThirdDetail && expandedSubDetail === 'equipment'" class="detail-third-level">
                    <div
                      v-for="item in getCatalogByType(expandedThirdDetail)"
                      :key="item.item"
                      class="third-level-item"
                    >
                      <el-icon class="item-icon"><Box /></el-icon>
                      <span class="item-name">{{ formatItemName(item) }}</span>
                      <span class="item-meta">使用 {{ item.useCount || 0 }} 次</span>
                    </div>
                    <div v-if="getCatalogByType(expandedThirdDetail).length === 0" class="empty-detail">
                      暂无发现
                    </div>
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
                    <div
                      v-for="cType in ['POTIONS', 'SCROLLS', 'SEEDS', 'STONES', 'FOOD', 'EXOTIC_POTIONS', 'EXOTIC_SCROLLS', 'BOMBS', 'TIPPED_DARTS', 'BREWS_ELIXIRS', 'SPELLS', 'MISC_CONSUMABLES']"
                      :key="cType"
                      class="detail-item expandable"
                      @click="toggleThirdDetail(cType)"
                    >
                      <span class="item-label">{{ getConsumableLabel(cType) }}</span>
                      <span class="item-count">{{ getConsumableDetail(cType) }}</span>
                      <el-icon class="expand-icon-small" :class="{ expanded: expandedThirdDetail === cType }"><ArrowRight /></el-icon>
                    </div>
                  </div>
                  <!-- 消耗品第三级展开 -->
                  <div v-if="expandedThirdDetail && expandedSubDetail === 'consumables'" class="detail-third-level">
                    <div
                      v-for="item in getCatalogByType(expandedThirdDetail)"
                      :key="item.item"
                      class="third-level-item"
                    >
                      <el-icon class="item-icon"><Box /></el-icon>
                      <span class="item-name">{{ formatItemName(item) }}</span>
                      <span class="item-meta">使用 {{ item.useCount || 0 }} 次</span>
                    </div>
                    <div v-if="getCatalogByType(expandedThirdDetail).length === 0" class="empty-detail">
                      暂无发现
                    </div>
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
                    <div
                      v-for="bType in ['REGIONAL', 'BOSSES', 'UNIVERSAL', 'RARE', 'QUEST', 'NEUTRAL', 'ALLY', 'TRAP', 'PLANT']"
                      :key="bType"
                      class="detail-item expandable"
                      @click="toggleThirdDetail(bType)"
                    >
                      <span class="item-label">{{ getBestiaryLabel(bType) }}</span>
                      <span class="item-count">{{ getBestiaryDetail(bType) }}</span>
                      <el-icon class="expand-icon-small" :class="{ expanded: expandedThirdDetail === bType }"><ArrowRight /></el-icon>
                    </div>
                  </div>
                  <!-- 单位图鉴第三级展开 -->
                  <div v-if="expandedThirdDetail && expandedSubDetail === 'bestiary'" class="detail-third-level">
                    <div
                      v-for="item in getBestiaryByType(expandedThirdDetail)"
                      :key="item.entity"
                      class="third-level-item"
                    >
                      <el-icon class="item-icon"><User /></el-icon>
                      <span class="item-name">{{ formatItemName(item) }}</span>
                      <span class="item-meta">遭遇 {{ item.encountered || 0 }} 次</span>
                    </div>
                    <div v-if="getBestiaryByType(expandedThirdDetail).length === 0" class="empty-detail">
                      暂无发现
                    </div>
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

              <!-- 地牢指南展开详情 -->
              <div v-if="expandedDetail === 'guide'" class="progress-detail">
                <div class="detail-section">
                  <div class="detail-header">
                    <span class="detail-title">已发现页面</span>
                    <span class="detail-count">{{ playerInfo?.adventurersGuideFound || 0 }}页</span>
                  </div>
                  <div class="detail-items document-items">
                    <div
                      v-for="doc in getDocumentsByType('ADVENTURERS_GUIDE')"
                      :key="doc.page"
                      class="detail-item document-item"
                    >
                      <el-icon class="document-icon"><Document /></el-icon>
                      <span class="document-name">{{ doc.page }}</span>
                    </div>
                    <div v-if="getDocumentsByType('ADVENTURERS_GUIDE').length === 0" class="empty-detail">
                      暂无发现
                    </div>
                  </div>
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

              <!-- 炼金指南展开详情 -->
              <div v-if="expandedDetail === 'alchemy'" class="progress-detail">
                <div class="detail-section">
                  <div class="detail-header">
                    <span class="detail-title">已发现页面</span>
                    <span class="detail-count">{{ playerInfo?.alchemyGuideFound || 0 }}页</span>
                  </div>
                  <div class="detail-items document-items">
                    <div
                      v-for="doc in getDocumentsByType('ALCHEMY_GUIDE')"
                      :key="doc.page"
                      class="detail-item document-item"
                    >
                      <el-icon class="document-icon"><Document /></el-icon>
                      <span class="document-name">{{ doc.page }}</span>
                    </div>
                    <div v-if="getDocumentsByType('ALCHEMY_GUIDE').length === 0" class="empty-detail">
                      暂无发现
                    </div>
                  </div>
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  UserFilled, User, Trophy, Location, Medal, TrendCharts,
  Clock, StarFilled, CircleCheckFilled, Check, Close, Warning,
  Collection, Goods, Document, Flag, View, Notebook, FirstAidKit, ArrowRight, Box
} from '@element-plus/icons-vue'
import { playerApi, leaderboardApi } from '../api'

const route = useRoute()
const router = useRouter()
const playerInfo = ref(null)
const playerRank = ref(null)
const rankDiff = ref(0)
const recentGames = ref([])
const loading = ref(false)
const expandedDetail = ref(null)
const expandedSubDetail = ref(null)
const expandedThirdDetail = ref(null)

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
  expandedThirdDetail.value = null
}

const toggleSubDetail = (subDetail) => {
  expandedSubDetail.value = expandedSubDetail.value === subDetail ? null : subDetail
  expandedThirdDetail.value = null
}

const toggleThirdDetail = (thirdDetail) => {
  expandedThirdDetail.value = expandedThirdDetail.value === thirdDetail ? null : thirdDetail
}

const getEquipmentLabel = (type) => {
  const labels = {
    'MELEE_WEAPONS': '近战武器',
    'ARMOR': '护甲',
    'ENCHANTMENTS': '附魔与诅咒',
    'GLYPHS': '刻印与诅咒',
    'THROWN_WEAPONS': '投掷武器',
    'WANDS': '法杖',
    'RINGS': '戒指',
    'ARTIFACTS': '神器',
    'TRINKETS': '饰品',
    'MISC_EQUIPMENT': '杂项装备'
  }
  return labels[type] || type
}

const getConsumableLabel = (type) => {
  const labels = {
    'POTIONS': '药剂',
    'SCROLLS': '卷轴',
    'SEEDS': '种子',
    'STONES': '符石',
    'FOOD': '食物',
    'EXOTIC_POTIONS': '合剂',
    'EXOTIC_SCROLLS': '秘卷',
    'BOMBS': '炸弹',
    'TIPPED_DARTS': '涂药飞镖',
    'BREWS_ELIXIRS': '魔药与秘药',
    'SPELLS': '法术结晶',
    'MISC_CONSUMABLES': '杂项消耗品'
  }
  return labels[type] || type
}

const getBestiaryLabel = (type) => {
  const labels = {
    'REGIONAL': '区域敌人',
    'BOSSES': '区域Boss',
    'UNIVERSAL': '全局敌人',
    'RARE': '稀有敌人',
    'QUEST': '任务敌人与Boss',
    'NEUTRAL': '中立角色',
    'ALLY': '盟友',
    'TRAP': '陷阱',
    'PLANT': '植物'
  }
  return labels[type] || type
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

const getDocumentsByType = (type) => {
  const docs = playerInfo.value?.documentList || []
  return docs.filter(d => d.type === type)
}

const getCatalogByType = (type) => {
  const catalog = playerInfo.value?.catalogList || []
  return catalog.filter(c => c.type === type)
}

const getBestiaryByType = (type) => {
  const bestiary = playerInfo.value?.bestiaryList || []
  return bestiary.filter(b => b.type === type)
}

const formatAchievementName = (achievement) => {
  return achievement
}

const formatItemName = (item) => {
  if (!item) return ''
  return item.item || item.entity || '未知'
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

// SPDNet: 跳转到前缀详情页
const goToPrefix = (prefix) => {
  if (prefix && prefix.id) {
    router.push(`/prefix/${prefix.id}`)
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

.clickable-prefix {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.clickable-prefix:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
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

.detail-item.expandable {
  cursor: pointer;
  transition: background var(--transition-fast);
  border-radius: var(--radius-sm);
}

.detail-item.expandable:hover {
  background: var(--surface-2);
}

.detail-item.expandable .item-label {
  flex: 1;
  color: var(--text-primary);
}

.detail-item.expandable .item-count {
  color: var(--text-secondary);
  font-family: monospace;
  margin-right: var(--space-2);
}

.expand-icon-small {
  transition: transform var(--transition-base);
  color: var(--text-tertiary);
  font-size: 12px;
}

.expand-icon-small.expanded {
  transform: rotate(90deg);
}

.detail-third-level {
  margin-left: var(--space-8);
  margin-top: var(--space-2);
  padding: var(--space-2);
  background: var(--surface-2);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
}

.third-level-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  font-size: 0.75rem;
  border-bottom: 1px solid var(--border-subtle);
}

.third-level-item:last-child {
  border-bottom: none;
}

.third-level-item .item-icon {
  color: var(--primary-400);
  font-size: 14px;
}

.third-level-item .item-name {
  flex: 1;
  color: var(--text-primary);
  font-family: monospace;
}

.third-level-item .item-meta {
  color: var(--text-tertiary);
  font-size: 0.6875rem;
}

.achievement-items {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-left: var(--space-4);
}

.achievement-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-2);
  background: rgba(245, 158, 11, 0.08);
  border-radius: var(--radius-sm);
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.achievement-icon {
  color: #f59e0b;
  font-size: 12px;
}

.achievement-name {
  font-size: 0.75rem;
  color: var(--text-primary);
  font-family: monospace;
}

.document-items {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.document-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  background: var(--surface-2);
  border-radius: var(--radius-sm);
}

.document-icon {
  color: var(--primary-400);
  font-size: 14px;
}

.document-name {
  font-size: 0.8125rem;
  color: var(--text-primary);
  font-family: monospace;
}

.empty-detail {
  padding: var(--space-3);
  text-align: center;
  color: var(--text-tertiary);
  font-size: 0.8125rem;
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
