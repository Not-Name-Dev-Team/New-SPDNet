<template>
  <div class="app-container">
    <el-header class="app-header">
      <div class="header-content">
        <div class="brand">
          <el-icon class="brand-icon" :size="28"><Connection /></el-icon>
          <router-link to="/" class="brand-text">
            <span class="gradient-text">SPD</span>Net
          </router-link>
        </div>
        
        <el-menu
          :default-active="$route.path"
          class="nav-menu"
          mode="horizontal"
          :ellipsis="false"
          router
        >
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/leaderboard">
            <el-icon><Trophy /></el-icon>
            <span>排行榜</span>
          </el-menu-item>
          <el-menu-item index="/chat">
            <el-icon><ChatDotRound /></el-icon>
            <span>聊天室</span>
          </el-menu-item>
        </el-menu>

        <div class="user-section">
          <template v-if="!authStore.isLoggedIn">
            <el-button 
              type="primary" 
              plain 
              @click="$router.push('/login')"
              class="glow-btn"
            >
              <el-icon><User /></el-icon>
              登录
            </el-button>
            <el-button 
              type="primary" 
              @click="$router.push('/register')"
              class="glow-btn"
            >
              <el-icon><Plus /></el-icon>
              注册
            </el-button>
          </template>
          <template v-else>
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-dropdown">
                <el-avatar 
                  :size="36" 
                  :icon="UserFilled"
                  class="user-avatar"
                />
                <span class="username">{{ authStore.user?.name }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="player" v-if="authStore.user?.name">
                    <el-icon><View /></el-icon>
                    我的主页
                  </el-dropdown-item>
                  <el-dropdown-item command="admin" v-if="isAdmin" divided>
                    <el-icon><Setting /></el-icon>
                    后台管理
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </el-header>

    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="slide-up" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

    <el-footer class="app-footer">
      <div class="footer-content">
        <div class="footer-brand">
          <el-icon :size="20"><Connection /></el-icon>
          <span>联机破碎地牢 SPDNet</span>
        </div>
        <div class="footer-links">
          <a href="https://github.com/Not-Name-Dev-Team/New-SPDNet" target="_blank">
            <el-icon><Link /></el-icon>
            GitHub
          </a>
        </div>
        <div class="footer-copy">
          © 2024 SPDNet. All rights reserved.
        </div>
      </div>
    </el-footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authStore } from './store/auth'
import { House, Trophy, ChatDotRound, User, Plus, UserFilled, ArrowDown, View, Setting, SwitchButton, Link, Connection } from '@element-plus/icons-vue'

const router = useRouter()
const isAdmin = computed(() => authStore.user?.role === '管理员')

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'player':
      router.push(`/player/${authStore.user?.name}`)
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      ElMessageBox.confirm(
        '确定要退出登录吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        authStore.logout()
        ElMessage.success('已退出登录')
        router.push('/')
      }).catch(() => {})
      break
  }
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-dark);
}

.app-header {
  height: 64px;
  padding: 0;
  background: rgba(13, 17, 23, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.brand-icon {
  color: var(--primary-color);
  animation: float 3s ease-in-out infinite;
}

.brand-text {
  font-size: 1.5rem;
  font-weight: 700;
  text-decoration: none;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

.nav-menu {
  background: transparent;
  border-bottom: none;
  flex: 1;
  margin: 0 2rem;
  justify-content: center;
}

:deep(.nav-menu .el-menu-item) {
  font-size: 1rem;
  height: 64px;
  line-height: 64px;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

:deep(.nav-menu .el-menu-item:hover),
:deep(.nav-menu .el-menu-item.is-active) {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
  background: rgba(91, 140, 255, 0.1);
}

:deep(.nav-menu .el-icon) {
  margin-right: 4px;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 8px;
  transition: background 0.3s;
}

.user-dropdown:hover {
  background: var(--bg-hover);
}

.user-avatar {
  background: var(--gradient-primary);
}

.username {
  font-weight: 500;
  color: var(--text-primary);
}

.main-content {
  flex: 1;
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.app-footer {
  height: auto;
  padding: 2rem;
  background: var(--bg-card);
  border-top: 1px solid var(--border-color);
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
}

.footer-brand .el-icon {
  color: var(--primary-color);
}

.footer-links a {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: var(--primary-color);
}

.footer-copy {
  color: var(--text-muted);
  font-size: 0.875rem;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 1rem;
  }
  
  .nav-menu {
    display: none;
  }
  
  .main-content {
    padding: 1rem;
  }
}
</style>
