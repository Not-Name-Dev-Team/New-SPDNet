<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-badge animate-fadeInUp">
          <span class="badge-icon">
            <el-icon><CircleCheck /></el-icon>
          </span>
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
            <router-link to="/profile" class="btn-primary btn-lg">
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
        <div class="visual-card">
          <div class="card-glow"></div>
          <el-icon class="visual-icon" :size="80"><Connection /></el-icon>
        </div>
        <div class="floating-elements">
          <div class="float-item" v-for="i in 3" :key="i" :style="{ '--delay': `${i * 0.2}s` }">
            <el-icon :size="24 + i * 8"><StarFilled /></el-icon>
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
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <el-icon :size="24" color="white"><component :is="stat.icon" /></el-icon>
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
        <div class="section-title">
          <div class="title-icon">
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
          v-for="player in onlinePlayers"
          :key="player.name"
          :to="`/player/${player.name}`"
          class="player-card"
        >
          <div class="player-avatar-wrapper">
            <el-avatar :size="48" :icon="UserFilled" class="player-avatar" />
            <span class="online-indicator"></span>
          </div>
          <div class="player-info">
            <span class="player-name">{{ player.name }}</span>
            <el-tag :type="getRoleType(player.role)" size="small" effect="light" round>
              {{ player.role }}
            </el-tag>
          </div>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </router-link>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">
          <el-icon :size="48"><User /></el-icon>
        </div>
        <p>暂无在线玩家</p>
        <span>成为第一个探索地牢的冒险者吧！</span>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <div class="section-header centered">
        <h2 class="section-title-main">特色功能</h2>
        <p class="section-subtitle">探索 SPDNet 带来的全新体验</p>
      </div>

      <div class="features-grid">
        <div
          v-for="(feature, index) in features"
          :key="index"
          class="feature-card"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <div class="feature-icon-wrapper" :style="{ background: feature.gradient }">
            <el-icon :size="32" color="white"><component :is="feature.icon" /></el-icon>
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.description }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User, UserFilled, Trophy, Connection, Key,
  CircleCheck, Refresh, ChatDotRound, Rank, Medal, CollectionTag,
  ArrowRight, StarFilled
} from '@element-plus/icons-vue'
import { playerApi } from '../api'
import { authStore } from '../store/auth'

const serverInfo = ref(null)
const onlinePlayers = ref([])
const loading = ref(true)

const statsList = computed(() => [
  {
    label: '当前在线',
    value: serverInfo.value?.onlineCount || 0,
    icon: 'UserFilled',
    gradient: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)'
  },
  {
    label: '注册玩家',
    value: serverInfo.value?.totalPlayers || 0,
    icon: 'User',
    gradient: 'linear-gradient(135deg, #ec4899 0%, #f43f5e 100%)'
  },
  {
    label: '游戏版本',
    value: serverInfo.value?.version || '-',
    icon: 'CollectionTag',
    gradient: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)'
  },
  {
    label: '联机版本',
    value: serverInfo.value?.netVersion || '-',
    icon: 'CircleCheck',
    gradient: 'linear-gradient(135deg, #10b981 0%, #06b6d4 100%)'
  }
])

const features = [
  {
    icon: 'Trophy',
    title: '竞技排行',
    description: '挑战高分排行榜，与全球玩家一较高下，展示你的地牢探险实力。',
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #f97316 100%)'
  },
  {
    icon: 'ChatDotRound',
    title: '实时聊天',
    description: '与在线玩家实时交流，分享游戏心得，结交志同道合的冒险伙伴。',
    gradient: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)'
  },
  {
    icon: 'Rank',
    title: '数据追踪',
    description: '详细记录你的每一次冒险，分析游戏数据，不断提升自己的技巧。',
    gradient: 'linear-gradient(135deg, #10b981 0%, #06b6d4 100%)'
  }
]

const getRoleType = (role) => {
  const types = {
    '管理员': 'danger',
    '玩家': 'info'
  }
  return types[role] || 'info'
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
  padding: var(--space-8) var(--content-padding);
}

/* Hero Section */
.hero-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-12);
  align-items: center;
  padding: var(--space-12) 0;
  min-height: 70vh;
}

.hero-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: var(--radius-full);
  color: var(--accent-emerald);
  font-size: 0.875rem;
  font-weight: 500;
  width: fit-content;
}

.badge-icon {
  display: flex;
  align-items: center;
}

.hero-title {
  font-size: 4rem;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.02em;
}

.hero-subtitle {
  font-size: 1.5rem;
  color: var(--text-secondary);
  font-weight: 300;
}

.hero-description {
  font-size: 1.125rem;
  color: var(--text-tertiary);
  max-width: 480px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
  margin-top: var(--space-2);
}

.btn-lg {
  padding: var(--space-3) var(--space-6);
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  text-decoration: none;
}

/* Hero Visual */
.hero-visual {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.visual-card {
  width: 280px;
  height: 280px;
  border-radius: var(--radius-2xl);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  animation: float 6s ease-in-out infinite;
}

.card-glow {
  position: absolute;
  inset: -1px;
  border-radius: var(--radius-2xl);
  background: var(--gradient-primary);
  opacity: 0.5;
  filter: blur(20px);
  z-index: -1;
  animation: glow-pulse 3s ease-in-out infinite;
}

.visual-icon {
  color: var(--primary-400);
}

.floating-elements {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.float-item {
  position: absolute;
  color: var(--primary-400);
  opacity: 0.6;
  animation: float 4s ease-in-out infinite;
  animation-delay: var(--delay);
}

.float-item:nth-child(1) {
  top: 10%;
  right: 10%;
}

.float-item:nth-child(2) {
  bottom: 20%;
  left: 5%;
}

.float-item:nth-child(3) {
  top: 50%;
  right: 0;
}

/* Stats Section */
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-5);
  margin-bottom: var(--space-12);
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
  animation: fadeInUp 0.6s ease-out backwards;
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

.stat-info {
  flex: 1;
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
  margin-top: var(--space-1);
}

/* Online Section */
.online-section {
  margin-bottom: var(--space-12);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.section-header.centered {
  flex-direction: column;
  text-align: center;
  gap: var(--space-2);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.title-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: rgba(99, 102, 241, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-400);
}

.title-content h2 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
}

.title-content p {
  color: var(--text-secondary);
  margin: var(--space-1) 0 0;
  font-size: 0.875rem;
}

.refresh-btn {
  font-weight: 500;
}

.players-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-4);
}

.player-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-base);
}

.player-card:hover {
  border-color: var(--primary-500);
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow-sm);
}

.player-avatar-wrapper {
  position: relative;
}

.player-avatar {
  background: var(--gradient-primary);
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: var(--radius-full);
  background: var(--accent-emerald);
  border: 2px solid var(--surface-1);
  box-shadow: 0 0 8px var(--accent-emerald);
}

.player-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.player-name {
  font-weight: 600;
  color: var(--text-primary);
}

.arrow-icon {
  color: var(--text-tertiary);
  transition: all var(--transition-fast);
}

.player-card:hover .arrow-icon {
  color: var(--primary-400);
  transform: translateX(4px);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
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

/* Features Section */
.features-section {
  padding: var(--space-12) 0;
}

.section-title-main {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.section-subtitle {
  color: var(--text-secondary);
  margin: var(--space-2) 0 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-6);
  margin-top: var(--space-8);
}

.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: var(--space-8);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  transition: all var(--transition-base);
  animation: fadeInUp 0.6s ease-out backwards;
}

.feature-card:hover {
  transform: translateY(-8px);
  border-color: var(--border-default);
  box-shadow: var(--shadow-lg);
}

.feature-icon-wrapper {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-5);
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 var(--space-3);
  color: var(--text-primary);
}

.feature-desc {
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

/* Animations */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes glow-pulse {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.6;
  }
}

/* Responsive */
@media (max-width: 1024px) {
  .hero-section {
    grid-template-columns: 1fr;
    text-align: center;
    gap: var(--space-8);
  }

  .hero-title {
    font-size: 3rem;
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

  .visual-card {
    width: 200px;
    height: 200px;
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
    padding: var(--space-8) 0;
    min-height: auto;
  }

  .hero-title {
    font-size: 2.25rem;
  }

  .hero-subtitle {
    font-size: 1.125rem;
  }

  .stats-section {
    grid-template-columns: 1fr;
  }

  .players-grid {
    grid-template-columns: 1fr;
  }
}
</style>
