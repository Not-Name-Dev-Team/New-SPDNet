<template>
  <div class="app">
    <nav class="navbar">
      <div class="nav-brand">
        <router-link to="/">联机破碎地牢</router-link>
      </div>
      <div class="nav-links">
        <router-link to="/">首页</router-link>
        <router-link to="/leaderboard">排行榜</router-link>
        <template v-if="!authStore.isLoggedIn">
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
        <template v-else>
          <router-link to="/profile">个人中心</router-link>
          <span class="user-info">
            {{ authStore.user.name }}
            <span class="user-role">({{ authStore.user.role }})</span>
          </span>
        </template>
      </div>
    </nav>
    <main class="main-content">
      <router-view />
    </main>
    <footer class="footer">
      <p>联机破碎地牢 &copy; 2024</p>
    </footer>
  </div>
</template>

<script setup>
import { authStore } from './store/auth'
</script>

<style scoped>
.user-info {
  color: var(--primary-color);
  font-weight: 500;
  margin-left: 0.5rem;
  padding: 0.5rem 1rem;
  background-color: rgba(76, 175, 80, 0.1);
  border-radius: 4px;
}

.user-role {
  color: var(--text-secondary);
  font-size: 0.875rem;
}
</style>
