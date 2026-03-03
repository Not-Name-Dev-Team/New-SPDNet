<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-badge animate-fadeInUp">
          <span class="badge-pulse"></span>
          <el-icon><CircleCheck /></el-icon>
          <span>在线联机版</span>
        </div>

        <h1 class="hero-title animate-fadeInUp" style="animation-delay: 0.1s">
          <span class="gradient-text">联机破碎地牢</span>
        </h1>

        <p class="hero-subtitle animate-fadeInUp" style="animation-delay: 0.2s">
          Shattered Pixel Dungeon NET
        </p>

        <p class="hero-description animate-fadeInUp" style="animation-delay: 0.3s">
          与全球玩家一起探索地牢，挑战极限，创造属于你的传奇！
        </p>

        <div class="hero-actions animate-fadeInUp" style="animation-delay: 0.4s">
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/register" class="btn-primary btn-lg">
              <el-icon><User /></el-icon>
              <span>立即注册</span>
            </router-link>
            <router-link to="/login" class="btn-secondary btn-lg">
              <el-icon><Key /></el-icon>
              <span>登录账号</span>
            </router-link>
          </template>
          <template v-else>
            <router-link to="/dashboard" class="btn-primary btn-lg">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </router-link>
          </template>
          <router-link to="/leaderboard" class="btn-secondary btn-lg">
            <el-icon><Trophy /></el-icon>
            <span>查看排行榜</span>
          </router-link>
        </div>
      </div>

      <div class="hero-visual animate-fadeIn" style="animation-delay: 0.5s">
        <div class="visual-container">
          <div class="visual-ring ring-1"></div>
          <div class="visual-ring ring-2"></div>
          <div class="visual-ring ring-3"></div>
          <div class="visual-core">
            <el-icon class="visual-icon" :size="56"><Connection /></el-icon>
          </div>
        </div>
        <div class="floating-icons">
          <div class="float-icon" v-for="(icon, i) in floatingIcons" :key="i" :style="{ '--delay': `${i * 0.4}s`, '--x': icon.x, '--y': icon.y }">
            <el-icon :size="icon.size"><component :is="icon.name" /></el-icon>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="stats-section" v-if="serverInfo">
      <div
        v-for="(stat, index) in statsList"
        :key="index"
        class="stat-card"
        :style="{ animationDelay: `${index * 0.1}s` }"
      >
        <div class="stat-glow" :style="{ background: stat.glow }"></div>
        <div class="stat-icon-wrapper" :style="{ background: stat.gradient }">
          <el-icon :size="22" color="white"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </section>

    <!-- Online Players Section -->
    <section class="online-section">
      <div class="section-header">
        <div class="section-title-wrapper">
          <div class="title-icon-wrapper">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="title-content">
            <h2>在线玩家</h2>
            <p v-if="onlinePlayers.length > 0">{{ onlinePlayers.length }} 位冒险者正在探索</p>
          </div>
        </div>
        <el-button
          type="primary"
          text
          :icon="Refresh"
          @click="loadData"
          :loading="loading"
          class="refresh-btn"
        >
          刷新
        </el-button>
      </div>

      <div class="players-grid" v-if="onlinePlayers.length > 0">
        <router-link
          v-for="(player, index) in onlinePlayers"
          :key="player.name"
          :to="`/player/${player.name}`"
          class="player-card"
          :style="{ animationDelay: `${index * 0.05}s` }"
        >
          <div class="card-glow"></div>
          <div class="player-avatar-wrapper">
            <el-avatar :size="44" :icon="UserFilled" class="player-avatar" />
            <span class="online-indicator"></span>
          </div>
          <div class="player-info">
            <span class="player-name">
              <span
                v-if="player.prefix"
                class="player-prefix clickable-prefix"
                :style="getPrefixStyle(player.prefix)"
                @click.prevent.stop="goToPrefix(player.prefix)"
                title="点击查看前缀详情"
              >{{ player.prefix.displayText }}</span>
              {{ player.name }}
            </span>
            <div class="player-meta">
              <el-tag :type="getRoleType(player.role)" size="small" effect="dark" round>
                {{ player.role }}
              </el-tag>
              <span v-if="player.status" class="player-status">
                <span class="status-separator">·</span>
                <span class="status-item">
                  <el-icon size="12"><Location /></el-icon>
                  {{ player.status.depth }}层
                </span>
                <span class="status-item" v-if="player.status.challenges > 0">
                  <el-icon size="12"><StarFilled /></el-icon>
                  {{ player.status.challenges }}挑
                </span>
                <span class="status-item">
                  <el-icon size="12"><CollectionTag /></el-icon>
                  {{ getGameModeText(player.status.gameMode) }}
                </span>
              </span>
            </div>
          </div>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </router-link>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon-wrapper">
          <el-icon :size="40"><User /></el-icon>
        </div>
        <p>暂无在线玩家</p>
        <span>成为第一个探索地牢的冒险者吧！</span>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <div class="section-header centered">
        <div class="section-badge">特色功能</div>
        <h2 class="section-title-main">探索 SPDNet 带来的全新体验</h2>
        <p class="section-subtitle">与全球玩家一起，开启你的地牢冒险之旅</p>
      </div>

      <div class="features-grid">
        <div
          v-for="(feature, index) in features"
          :key="index"
          class="feature-card"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <div class="feature-glow" :style="{ background: feature.glow }"></div>
          <div class="feature-icon-wrapper" :style="{ background: feature.gradient }">
            <el-icon :size="28" color="white"><component :is="feature.icon" /></el-icon>
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.description }}</p>
          <div class="feature-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User, UserFilled, Trophy, Connection, Key,
  CircleCheck, Refresh, ChatDotRound, Rank, Medal, CollectionTag,
  ArrowRight, StarFilled, View, TrendCharts, FirstAidKit, Location
} from '@element-plus/icons-vue'
import { playerApi } from '../api'
import { authStore } from '../store/auth'

const router = useRouter()

const serverInfo = ref(null)
const onlinePlayers = ref([])
const loading = ref(true)

const floatingIcons = [
  { name: 'StarFilled', size: 18, x: '10%', y: '20%' },
  { name: 'Trophy', size: 22, x: '85%', y: '15%' },
  { name: 'FirstAidKit', size: 20, x: '75%', y: '75%' },
  { name: 'Medal', size: 16, x: '15%', y: '70%' }
]

const statsList = computed(() => [
  {
    label: '当前在线',
    value: serverInfo.value?.onlineCount || 0,
    icon: 'UserFilled',
    gradient: 'linear-gradient(135deg, #9333ea 0%, #a855f7 100%)',
    glow: 'rgba(147, 51, 234, 0.25)'
  },
  {
    label: '注册玩家',
    value: serverInfo.value?.totalPlayers || 0,
    icon: 'User',
    gradient: 'linear-gradient(135deg, #ec4899 0%, #f43f5e 100%)',
    glow: 'rgba(236, 72, 153, 0.25)'
  },
  {
    label: '游戏版本',
    value: serverInfo.value?.version || '-',
    icon: 'CollectionTag',
    gradient: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
    glow: 'rgba(6, 182, 212, 0.25)'
  },
  {
    label: '联机版本',
    value: serverInfo.value?.netVersion || '-',
    icon: 'CircleCheck',
    gradient: 'linear-gradient(135deg, #10b981 0%, #06b6d4 100%)',
    glow: 'rgba(16, 185, 129, 0.25)'
  }
])

const features = [
  {
    icon: 'Trophy',
    title: '竞技排行',
    description: '挑战高分排行榜，与全球玩家一较高下，展示你的地牢探险实力。',
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #f97316 100%)',
    glow: 'rgba(245, 158, 11, 0.15)'
  },
  {
    icon: 'ChatDotRound',
    title: '实时聊天',
    description: '与在线玩家实时交流，分享游戏心得，结交志同道合的冒险伙伴。',
    gradient: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
    glow: 'rgba(6, 182, 212, 0.15)'
  },
  {
    icon: 'TrendCharts',
    title: '数据追踪',
    description: '详细记录你的每一次冒险，分析游戏数据，不断提升自己的技巧。',
    gradient: 'linear-gradient(135deg, #10b981 0%, #06b6d4 100%)',
    glow: 'rgba(16, 185, 129, 0.15)'
  }
]

const getRoleType = (role) => {
  const types = {
    '管理员': 'danger',
    '玩家': 'primary'
  }
  return types[role] || 'primary'
}

// SPDNet: 获取游戏模式文本
// 0=铁人模式, 1=娱乐模式, 2=每日挑战
const getGameModeText = (gameMode) => {
  const modes = {
    0: '铁人',
    1: '娱乐',
    2: '每日'
  }
  return modes[gameMode] || '娱乐'
}

// SPDNet: 前缀系统 - 获取前缀样式
const getPrefixStyle = (prefix) => {
  return {
    color: prefix.color || '#ffffff',
    backgroundColor: prefix.backgroundColor || 'rgba(139, 92, 246, 0.8)',
    padding: '2px 8px',
    borderRadius: '4px',
    fontSize: '12px',
    fontWeight: 'bold',
    marginRight: '4px',
    display: 'inline-block'
  }
}

// SPDNet: 跳转到前缀详情页
const goToPrefix = (prefix) => {
  if (prefix && prefix.id) {
    router.push(`/prefix/${prefix.id}`)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const [serverRes, onlineRes] = await Promise.all([
      playerApi.getServerInfo(),
      playerApi.getOnline()
    ])
    serverInfo.value = serverRes.data.data
    onlinePlayers.value = onlineRes.data.data || []
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.home-page {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-6) var(--content-padding);
}

/* Hero Section */
.hero-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-10);
  align-items: center;
  padding: var(--space-10) 0 var(--space-12);
  min-height: calc(100vh - var(--header-height) - var(--space-12));
}

.hero-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.hero-badge {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-3);
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: var(--radius-full);
  color: var(--accent-emerald);
  font-size: 0.8125rem;
  font-weight: 500;
  width: fit-content;
  overflow: hidden;
}

.badge-pulse {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, rgba(16, 185, 129, 0.2) 0%, transparent 70%);
  animation: pulse 2s ease-in-out infinite;
}

.hero-badge .el-icon,
.hero-badge span {
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.02em;
}

.hero-subtitle {
  font-size: 1.25rem;
  color: var(--text-secondary);
  font-weight: 400;
}

.hero-description {
  font-size: 1rem;
  color: var(--text-tertiary);
  max-width: 440px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
  margin-top: var(--space-2);
}

.btn-lg {
  padding: var(--space-3) var(--space-5);
  font-size: 0.9375rem;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  text-decoration: none;
  border-radius: var(--radius-full);
}

/* Hero Visual */
.hero-visual {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.visual-container {
  position: relative;
  width: 280px;
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.visual-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid var(--border-default);
}

.ring-1 {
  width: 100%;
  height: 100%;
  animation: rotate 25s linear infinite;
  border-style: dashed;
  opacity: 0.5;
}

.ring-2 {
  width: 75%;
  height: 75%;
  animation: rotate 18s linear infinite reverse;
  opacity: 0.4;
}

.ring-3 {
  width: 50%;
  height: 50%;
  animation: rotate 12s linear infinite;
  border-style: dotted;
  opacity: 0.3;
}

.visual-core {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: var(--shadow-glow);
  animation: float 5s ease-in-out infinite;
}

.visual-core::before {
  content: '';
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  background: var(--gradient-primary);
  opacity: 0.25;
  filter: blur(16px);
}

.visual-icon {
  color: white;
  position: relative;
  z-index: 1;
}

.floating-icons {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.float-icon {
  position: absolute;
  left: var(--x);
  top: var(--y);
  color: var(--primary-400);
  opacity: 0.5;
  animation: float 4s ease-in-out infinite;
  animation-delay: var(--delay);
}

/* Stats Section */
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-10);
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
  animation: fadeInUp 0.5s ease-out backwards;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-3px);
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
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
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
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 0.8125rem;
  color: var(--text-secondary);
  margin-top: 2px;
}

/* Online Section */
.online-section {
  margin-bottom: var(--space-10);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
}

.section-header.centered {
  flex-direction: column;
  text-align: center;
  gap: var(--space-2);
}

.section-title-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.title-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  background: rgba(147, 51, 234, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-400);
}

.title-content h2 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.title-content p {
  color: var(--text-secondary);
  margin: 2px 0 0;
  font-size: 0.8125rem;
}

.refresh-btn {
  font-weight: 500;
  color: white;
}

.refresh-btn:hover {
  color: white;
  background: rgba(168, 85, 247, 0.2);
}

.players-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--space-3);
}

.player-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-base);
  animation: fadeInUp 0.4s ease-out backwards;
  overflow: hidden;
}

.card-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 50% 0%, rgba(147, 51, 234, 0.12) 0%, transparent 70%);
  opacity: 0;
  transition: opacity var(--transition-base);
}

.player-card:hover {
  border-color: var(--primary-500);
  transform: translateY(-3px);
  box-shadow: var(--shadow-glow-sm);
}

.player-card:hover .card-glow {
  opacity: 1;
}

.player-avatar-wrapper {
  position: relative;
  z-index: 1;
}

.player-avatar {
  background: var(--gradient-primary);
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  background: var(--accent-emerald);
  border: 2px solid var(--surface-1);
  box-shadow: 0 0 6px var(--accent-emerald);
  z-index: 2;
}

.player-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  position: relative;
  z-index: 1;
}

.player-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.player-status {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  font-size: 0.75rem;
}

.status-separator {
  color: var(--text-tertiary);
}

.status-item {
  display: flex;
  align-items: center;
  gap: 2px;
}

.player-name {
  font-weight: 600;
  font-size: 0.9375rem;
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

.arrow-icon {
  color: var(--text-tertiary);
  transition: all var(--transition-fast);
  position: relative;
  z-index: 1;
}

.player-card:hover .arrow-icon {
  color: var(--primary-400);
  transform: translateX(3px);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-10);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  text-align: center;
}

.empty-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-xl);
  background: var(--surface-2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
}

.empty-state p {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.empty-state span {
  color: var(--text-secondary);
  font-size: 0.8125rem;
}

/* Features Section */
.features-section {
  padding: var(--space-10) 0;
}

.section-badge {
  display: inline-flex;
  padding: var(--space-1) var(--space-3);
  background: rgba(147, 51, 234, 0.1);
  border: 1px solid rgba(147, 51, 234, 0.2);
  border-radius: var(--radius-full);
  color: var(--primary-400);
  font-size: 0.8125rem;
  font-weight: 500;
}

.section-title-main {
  font-size: 1.875rem;
  font-weight: 700;
  margin: 0;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.section-subtitle {
  color: var(--text-secondary);
  margin: var(--space-1) 0 0;
  font-size: 1rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  margin-top: var(--space-6);
}

.feature-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: var(--space-6);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  transition: all var(--transition-base);
  animation: fadeInUp 0.5s ease-out backwards;
  overflow: hidden;
}

.feature-glow {
  position: absolute;
  inset: 0;
  opacity: 0;
  transition: opacity var(--transition-base);
}

.feature-card:hover {
  transform: translateY(-6px);
  border-color: var(--border-default);
  box-shadow: var(--shadow-lg);
}

.feature-card:hover .feature-glow {
  opacity: 0.08;
}

.feature-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-4);
  position: relative;
  z-index: 1;
}

.feature-title {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0 0 var(--space-2);
  color: var(--text-primary);
  position: relative;
  z-index: 1;
}

.feature-desc {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  font-size: 0.875rem;
  position: relative;
  z-index: 1;
}

.feature-arrow {
  margin-top: var(--space-4);
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background: var(--surface-2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
  transition: all var(--transition-fast);
  position: relative;
  z-index: 1;
}

.feature-card:hover .feature-arrow {
  background: var(--gradient-primary);
  color: white;
  transform: translateX(3px);
}

/* Animations */
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

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* Responsive */
@media (max-width: 1024px) {
  .hero-section {
    grid-template-columns: 1fr;
    text-align: center;
    gap: var(--space-8);
    min-height: auto;
    padding: var(--space-8) 0;
  }

  .hero-title {
    font-size: 2.5rem;
  }

  .hero-description {
    margin-left: auto;
    margin-right: auto;
  }

  .hero-actions {
    justify-content: center;
  }

  .hero-visual {
    order: -1;
  }

  .visual-container {
    width: 220px;
    height: 220px;
  }

  .visual-core {
    width: 90px;
    height: 90px;
  }

  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .home-page {
    padding: var(--space-4);
  }

  .hero-section {
    padding: var(--space-6) 0;
  }

  .hero-title {
    font-size: 2rem;
  }

  .hero-subtitle {
    font-size: 1rem;
  }

  .stats-section {
    grid-template-columns: 1fr;
  }

  .players-grid {
    grid-template-columns: 1fr;
  }

  .section-title-main {
    font-size: 1.5rem;
  }
}
</style>
