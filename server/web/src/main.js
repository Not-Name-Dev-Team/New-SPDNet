import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './style.css'

import Home from './views/Home.vue'
import Register from './views/Register.vue'
import Login from './views/Login.vue'
import Leaderboard from './views/Leaderboard.vue'
import Player from './views/Player.vue'
import Profile from './views/Profile.vue'
import Admin from './views/Admin.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/register', component: Register },
  { path: '/login', component: Login },
  { path: '/leaderboard', component: Leaderboard },
  { path: '/player/:name', component: Player },
  { path: '/profile', component: Profile },
  { path: '/admin', component: Admin }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

createApp(App).use(router).mount('#app')
