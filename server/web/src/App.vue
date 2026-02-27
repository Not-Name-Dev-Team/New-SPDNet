<template>
  <div class="app">
    <nav class="navbar">
      <div class="nav-brand">
        <router-link to="/">联机破碎地牢</router-link>
      </div>
      <div class="nav-links">
        <router-link to="/">首页</router-link>
        <router-link to="/register">注册</router-link>
        <router-link to="/leaderboard">排行榜</router-link>
        <router-link to="/send-key">查询Key</router-link>
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
</script>
