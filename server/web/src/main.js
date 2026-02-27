import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './style.css'

import Home from './views/Home.vue'
import Register from './views/Register.vue'
import Leaderboard from './views/Leaderboard.vue'
import Player from './views/Player.vue'
import SendKey from './views/SendKey.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/register', component: Register },
  { path: '/leaderboard', component: Leaderboard },
  { path: '/player/:name', component: Player },
  { path: '/send-key', component: SendKey }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

createApp(App).use(router).mount('#app')
