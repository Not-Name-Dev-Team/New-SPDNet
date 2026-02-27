<template>
  <div class="home">
    <div class="hero">
      <h1>联机破碎地牢</h1>
      <p class="subtitle">Shattered Pixel Dungeon NET</p>
    </div>

    <div class="stats-grid" v-if="serverInfo">
      <div class="stat-card">
        <div class="stat-value">{{ serverInfo.onlineCount }}</div>
        <div class="stat-label">当前在线</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ serverInfo.totalPlayers }}</div>
        <div class="stat-label">注册玩家</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ serverInfo.version }}</div>
        <div class="stat-label">游戏版本</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ serverInfo.netVersion }}</div>
        <div class="stat-label">联机版本</div>
      </div>
    </div>

    <div class="card" v-if="onlinePlayers.length > 0">
      <h2 class="card-title">在线玩家</h2>
      <table class="table">
        <thead>
          <tr>
            <th>玩家名</th>
            <th>身份</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="player in onlinePlayers" :key="player.name">
            <td>
              <router-link :to="`/player/${player.name}`">{{ player.name }}</router-link>
            </td>
            <td>{{ player.role }}</td>
            <td><span class="badge badge-online">在线</span></td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="card" v-else-if="!loading">
      <h2 class="card-title">在线玩家</h2>
      <p class="loading">暂无在线玩家</p>
    </div>

    <div class="actions">
      <template v-if="!authStore.isLoggedIn">
        <router-link to="/login" class="btn">登录</router-link>
        <router-link to="/register" class="btn" style="margin-left: 1rem;">注册</router-link>
      </template>
      <template v-else>
        <router-link to="/profile" class="btn">个人中心</router-link>
      </template>
      <router-link to="/leaderboard" class="btn" style="margin-left: 1rem;">排行榜</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { playerApi } from '../api'
import { authStore } from '../store/auth'

const serverInfo = ref(null)
const onlinePlayers = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [serverRes, onlineRes] = await Promise.all([
      playerApi.getServerInfo(),
      playerApi.getOnline()
    ])
    serverInfo.value = serverRes.data.data
    onlinePlayers.value = onlineRes.data.data || []
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home {
  text-align: center;
}

.hero {
  padding: 3rem 0;
}

.hero h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  color: var(--primary-color);
}

.subtitle {
  color: var(--text-secondary);
  font-size: 1.25rem;
}

.actions {
  margin-top: 2rem;
}
</style>
