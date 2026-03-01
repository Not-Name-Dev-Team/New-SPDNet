<template>
  <div class="home">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <div class="hero-badge">
          <el-tag type="success" effect="dark" round>在线联机版</el-tag>
        </div>
        <h1 class="hero-title">
          <span class="gradient-text">联机破碎地牢</span>
        </h1>
        <p class="hero-subtitle">Shattered Pixel Dungeon NET</p>
        <p class="hero-description">
          与全球玩家一起探索地牢，挑战极限，创造属于你的传奇！
        </p>
        <div class="hero-actions">
          <template v-if="!authStore.isLoggedIn">
            <el-button 
              type="primary" 
              size="large" 
              @click="$router.push('/register')"
              class="glow-btn"
            >
              <el-icon><User /></el-icon>
              立即注册
            </el-button>
            <el-button 
              size="large" 
              @click="$router.push('/login')"
            >
              <el-icon><Key /></el-icon>
              登录账号
            </el-button>
          </template>
          <template v-else>
            <el-button 
              type="primary" 
              size="large" 
              @click="$router.push('/profile')"
              class="glow-btn"
            >
              <el-icon><User /></el-icon>
              个人中心
            </el-button>
          </template>
          <el-button 
            size="large" 
            @click="$router.push('/leaderboard')"
          >
            <el-icon><Trophy /></el-icon>
            查看排行榜
          </el-button>
        </div>
      </div>
      <div class="hero-decoration">
        <div class="floating-icon">
          <el-icon :size="120" color="var(--primary-color)"><Connection /></el-icon>
        </div>
      </div>
    </div>

    <!-- Stats Section -->
    <el-row :gutter="20" class="stats-section" v-if="serverInfo">
      <el-col :xs="12" :sm="6" v-for="(stat, index) in statsList" :key="index">
        <div class="stat-card" :style="{ animationDelay: `${index * 0.1}s` }">
          <div class="stat-icon" :style="{ background: stat.gradient }">
            <el-icon :size="24" color="white"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value stat-number">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Online Players Section -->
    <el-card class="online-section" shadow="hover" v-loading="loading">
      <template #header>
        <div class="section-header">
          <div class="section-title">
            <el-icon><UserFilled /></el-icon>
            <span>在线玩家</span>
            <el-tag type="success" effect="dark" round size="small" v-if="onlinePlayers.length > 0">
              {{ onlinePlayers.length }} 人在线
            </el-tag>
          </div>
          <el-button 
            type="primary" 
            text 
            :icon="Refresh" 
            @click="loadData"
            :loading="loading"
          >
            刷新
          </el-button>
        </div>
      </template>

      <div v-if="onlinePlayers.length > 0">
        <el-table 
          :data="onlinePlayers" 
          style="width: 100%"
          :header-cell-style="{ background: 'var(--bg-hover)' }"
        >
          <el-table-column label="玩家名" min-width="150">
            <template #default="{ row }">
              <router-link :to="`/player/${row.name}`" class="player-link">
                <el-avatar :size="32" :icon="UserFilled" class="player-avatar" />
                <span class="player-name">{{ row.name }}</span>
              </router-link>
            </template>
          </el-table-column>
          <el-table-column label="身份" width="120">
            <template #default="{ row }">
              <el-tag :type="getRoleType(row.role)" effect="light" round size="small">
                {{ row.role }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default>
              <el-tag type="success" effect="dark" round size="small">在线</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                text 
                size="small"
                @click="$router.push(`/player/${row.name}`)"
              >
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-empty 
        v-else 
        description="暂无在线玩家" 
        :image-size="120"
      >
        <template #image>
          <el-icon :size="60" color="var(--text-muted)"><User /></el-icon>
        </template>
      </el-empty>
    </el-card>

    <!-- Features Section -->
    <el-row :gutter="20" class="features-section">
      <el-col :xs="24" :sm="8" v-for="(feature, index) in features" :key="index">
        <el-card class="feature-card" shadow="hover">
          <div class="feature-icon" :style="{ background: feature.gradient }">
            <el-icon :size="32" color="white"><component :is="feature.icon" /></el-icon>
          </div>
          <h3 class="feature-title">{{ feature.title }}</h3>
          <p class="feature-desc">{{ feature.description }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  User, UserFilled, Trophy, Connection, Key, 
  CircleCheck, Refresh, ChatDotRound, Rank, Medal, CollectionTag
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
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  { 
    label: '注册玩家', 
    value: serverInfo.value?.totalPlayers || 0, 
    icon: 'User',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  { 
    label: '游戏版本', 
    value: serverInfo.value?.version || '-', 
    icon: 'CollectionTag',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  { 
    label: '联机版本', 
    value: serverInfo.value?.netVersion || '-', 
    icon: 'CircleCheck',
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
])

const features = [
  {
    icon: 'Trophy',
    title: '竞技排行',
    description: '挑战高分排行榜，与全球玩家一较高下，展示你的地牢探险实力。',
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  },
  {
    icon: 'ChatDotRound',
    title: '实时聊天',
    description: '与在线玩家实时交流，分享游戏心得，结交志同道合的冒险伙伴。',
    gradient: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)'
  },
  {
    icon: 'Rank',
    title: '数据追踪',
    description: '详细记录你的每一次冒险，分析游戏数据，不断提升自己的技巧。',
    gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
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
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4rem 0;
  gap: 3rem;
}

.hero-content {
  flex: 1;
}

.hero-badge {
  margin-bottom: 1.5rem;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  margin-bottom: 0.5rem;
  letter-spacing: -1px;
}

.hero-subtitle {
  font-size: 1.5rem;
  color: var(--text-secondary);
  margin-bottom: 1rem;
  font-weight: 300;
}

.hero-description {
  font-size: 1.125rem;
  color: var(--text-muted);
  margin-bottom: 2rem;
  max-width: 500px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.hero-decoration {
  flex-shrink: 0;
}

.floating-icon {
  animation: float 3s ease-in-out infinite;
  opacity: 0.3;
}

.stats-section {
  margin-bottom: 2rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  transition: all 0.3s;
  animation: slideUp 0.6s ease-out backwards;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
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
  margin-top: 0.25rem;
}

.online-section {
  margin-bottom: 2rem;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.125rem;
  font-weight: 600;
}

.section-title .el-icon {
  color: var(--primary-color);
}

.player-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  text-decoration: none;
  color: var(--text-primary);
  transition: color 0.3s;
}

.player-link:hover {
  color: var(--primary-color);
}

.player-avatar {
  background: var(--gradient-primary);
}

.player-name {
  font-weight: 500;
}

.features-section {
  margin-top: 2rem;
}

.feature-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  text-align: center;
  padding: 1rem;
  transition: all 0.3s;
  height: 100%;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: var(--text-primary);
}

.feature-desc {
  color: var(--text-secondary);
  line-height: 1.6;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .hero-section {
    flex-direction: column;
    text-align: center;
    padding: 2rem 0;
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
  
  .hero-decoration {
    display: none;
  }
  
  .stat-card {
    margin-bottom: 1rem;
  }
}
</style>
