<template>
  <div class="app-wrapper">
    <!-- Aurora Background -->
    <div class="aurora-bg">
      <div class="aurora aurora-1"></div>
      <div class="aurora aurora-2"></div>
      <div class="aurora aurora-3"></div>
    </div>

    <!-- Particle Background -->
    <ParticleBackground />

    <!-- Header -->
    <header class="app-header" :class="{ 'scrolled': isScrolled }">
      <div class="header-container">
        <!-- Brand -->
        <router-link to="/" class="brand">
          <div class="brand-icon-wrapper">
            <div class="brand-icon-glow"></div>
            <el-icon class="brand-icon" :size="20"><Connection /></el-icon>
          </div>
          <span class="brand-text">
            <span class="gradient-text">SPD</span>Net
          </span>
        </router-link>

        <!-- Navigation -->
        <nav class="main-nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            :class="['nav-link', { active: $route.path === item.path }]"
          >
            <span class="nav-indicator"></span>
            <el-icon :size="16"><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </router-link>
        </nav>

        <!-- User Section -->
        <div class="user-section">
          <template v-if="!authStore.isLoggedIn">
            <router-link to="/login" class="btn-login">
              <el-icon><User /></el-icon>
              <span>登录</span>
            </router-link>
            <router-link to="/register" class="btn-register">
              <el-icon><Plus /></el-icon>
              <span>注册</span>
            </router-link>
          </template>
          <template v-else>
            <el-dropdown @command="handleCommand" trigger="click" popper-class="user-dropdown-menu">
              <div class="user-trigger">
                <div class="user-avatar-wrapper">
                  <el-avatar :size="34" :icon="UserFilled" class="user-avatar" />
                  <span class="status-indicator online"></span>
                </div>
                <span class="user-name">{{ authStore.user?.name }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    <span>个人中心</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="player" v-if="authStore.user?.name">
                    <el-icon><View /></el-icon>
                    <span>我的主页</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="admin" v-if="isAdmin" divided>
                    <el-icon><Setting /></el-icon>
                    <span>后台管理</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- Footer -->
    <footer class="app-footer">
      <div class="footer-container">
        <div class="footer-brand">
          <div class="footer-icon">
            <el-icon :size="18"><Connection /></el-icon>
          </div>
          <span>联机破碎地牢 SPDNet</span>
        </div>
        <div class="footer-links">
          <a href="https://github.com/Not-Name-Dev-Team/New-SPDNet" target="_blank" class="footer-link">
            <el-icon><Link /></el-icon>
            <span>GitHub</span>
          </a>
        </div>
        <div class="footer-copy">
          © 2024 SPDNet. All rights reserved.
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authStore } from './store/auth'
import ParticleBackground from './components/ParticleBackground.vue'
import {
  House, Trophy, ChatDotRound, User, Plus, UserFilled,
  ArrowDown, View, Setting, SwitchButton, Link, Connection
} from '@element-plus/icons-vue'

const router = useRouter()
const isAdmin = computed(() => authStore.user?.role === '管理员')
const isScrolled = ref(false)

const navItems = [
  { path: '/', label: '首页', icon: House },
  { path: '/leaderboard', label: '排行榜', icon: Trophy },
  { path: '/chat', label: '聊天室', icon: ChatDotRound }
]

const handleScroll = () => {
  isScrolled.value = window.scrollY > 20
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/dashboard')
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
          type: 'warning',
          customClass: 'custom-message-box'
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
.app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* Aurora Background */
.aurora-bg {
  position: fixed;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.aurora {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.15;
  animation: aurora 20s ease-in-out infinite;
}

.aurora-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #9333ea, #a855f7);
  top: -200px;
  left: -100px;
  animation-delay: 0s;
}

.aurora-2 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #06b6d4, #3b82f6);
  top: 20%;
  right: -150px;
  animation-delay: -7s;
}

.aurora-3 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #ec4899, #f43f5e);
  bottom: 10%;
  left: 20%;
  animation-delay: -14s;
}

@keyframes aurora {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(50px, -30px) scale(1.1);
  }
  50% {
    transform: translate(-30px, 50px) scale(0.95);
  }
  75% {
    transform: translate(-50px, -20px) scale(1.05);
  }
}

/* Header */
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--header-height);
  background: rgba(10, 10, 15, 0.5);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-subtle);
  transition: all var(--transition-base);
}

.app-header.scrolled {
  background: rgba(10, 10, 15, 0.85);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.3);
}

.header-container {
  max-width: var(--max-width);
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--content-padding);
}

/* Brand */
.brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  text-decoration: none;
  position: relative;
}

.brand-icon-wrapper {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-icon-glow {
  position: absolute;
  inset: -2px;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  filter: blur(8px);
  opacity: 0.4;
  animation: glow-pulse 2s ease-in-out infinite;
}

.brand-icon {
  color: white;
  position: relative;
  z-index: 1;
}

.brand-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

/* Navigation */
.main-nav {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  background: var(--surface-1);
  padding: var(--space-1);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-subtle);
}

.nav-link {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  color: var(--text-secondary);
  text-decoration: none;
  font-weight: 500;
  font-size: 0.875rem;
  transition: all var(--transition-fast);
  overflow: hidden;
}

.nav-indicator {
  position: absolute;
  inset: 0;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  opacity: 0;
  transform: scale(0.8);
  transition: all var(--transition-fast);
}

.nav-link:hover {
  color: var(--text-primary);
}

.nav-link.active {
  color: white;
}

.nav-link.active .nav-indicator {
  opacity: 1;
  transform: scale(1);
}

.nav-link .el-icon,
.nav-link span {
  position: relative;
  z-index: 1;
}

/* User Section */
.user-section {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.btn-login,
.btn-register {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  font-weight: 500;
  font-size: 0.875rem;
  text-decoration: none;
  transition: all var(--transition-fast);
}

.btn-login {
  color: var(--text-secondary);
  background: transparent;
}

.btn-login:hover {
  color: var(--text-primary);
  background: var(--surface-2);
}

.btn-register {
  color: white;
  background: var(--gradient-primary);
  box-shadow: var(--shadow-glow-sm);
}

.btn-register:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-glow);
}

/* User Dropdown */
.user-trigger {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-2) var(--space-1) var(--space-1);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
}

.user-trigger:hover {
  background: var(--surface-2);
  border-color: var(--border-default);
}

.user-avatar-wrapper {
  position: relative;
}

.user-avatar {
  background: var(--gradient-primary);
}

.status-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  border: 2px solid var(--bg-primary);
}

.status-indicator.online {
  background: var(--accent-emerald);
  box-shadow: 0 0 6px var(--accent-emerald);
}

.user-name {
  font-weight: 500;
  font-size: 0.875rem;
  color: var(--text-primary);
}

.dropdown-icon {
  color: var(--text-tertiary);
  transition: transform var(--transition-fast);
}

.user-trigger:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* Main Content */
.app-main {
  flex: 1;
  position: relative;
  z-index: 1;
  padding-top: var(--header-height);
}

/* Footer */
.app-footer {
  position: relative;
  z-index: 1;
  padding: var(--space-6) 0;
  border-top: 1px solid var(--border-subtle);
  background: rgba(10, 10, 15, 0.5);
  backdrop-filter: blur(10px);
}

.footer-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--content-padding);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 0.9375rem;
  font-weight: 600;
  color: var(--text-primary);
}

.footer-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.footer-links {
  display: flex;
  gap: var(--space-4);
}

.footer-link {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 0.875rem;
  transition: color var(--transition-fast);
}

.footer-link:hover {
  color: var(--primary-400);
}

.footer-copy {
  color: var(--text-tertiary);
  font-size: 0.75rem;
}

/* Page Transition */
.page-enter-active,
.page-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* Animations */
@keyframes glow-pulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.05);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .main-nav {
    display: none;
  }

  .brand-text {
    font-size: 1.125rem;
  }

  .user-name {
    display: none;
  }

  .btn-login span,
  .btn-register span {
    display: none;
  }

  .footer-container {
    text-align: center;
  }
}
</style>

<style>
/* Global Dropdown Menu Style */
.user-dropdown-menu {
  background: var(--surface-1) !important;
  border: 1px solid var(--border-default) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-lg) !important;
  padding: var(--space-2) !important;
  backdrop-filter: blur(20px);
}

.user-dropdown-menu .el-dropdown-menu__item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.user-dropdown-menu .el-dropdown-menu__item:hover {
  background: var(--surface-2);
  color: var(--primary-400);
}

.user-dropdown-menu .el-dropdown-menu__item .el-icon {
  font-size: 16px;
}

.user-dropdown-menu .el-dropdown-menu__item--divided {
  border-top: 1px solid var(--border-subtle);
  margin-top: var(--space-2);
  padding-top: var(--space-2);
}

/* Message Box Style */
.custom-message-box {
  background: var(--surface-1) !important;
  border: 1px solid var(--border-default) !important;
  border-radius: var(--radius-lg) !important;
  backdrop-filter: blur(20px);
}

.custom-message-box .el-message-box__title {
  color: var(--text-primary) !important;
}

.custom-message-box .el-message-box__message {
  color: var(--text-secondary) !important;
}
</style>
