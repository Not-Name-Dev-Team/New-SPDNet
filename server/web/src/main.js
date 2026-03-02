import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import App from './App.vue'
import './style.css'

import Home from './views/Home.vue'
import Register from './views/Register.vue'
import Login from './views/Login.vue'
import Leaderboard from './views/Leaderboard.vue'
import Player from './views/Player.vue'
import Profile from './views/Profile.vue'
import Admin from './views/Admin.vue'
import Chat from './views/Chat.vue'
import PrefixDetail from './views/PrefixDetail.vue'

const routes = [
  { path: '/', component: Home, meta: { title: '首页' } },
  { path: '/register', component: Register, meta: { title: '注册' } },
  { path: '/login', component: Login, meta: { title: '登录' } },
  { path: '/leaderboard', component: Leaderboard, meta: { title: '排行榜' } },
  { path: '/player/:name', component: Player, meta: { title: '玩家详情' } },
  { path: '/dashboard', component: Profile, meta: { title: '个人中心' } },
  { path: '/admin', component: Admin, meta: { title: '后台管理' } },
  { path: '/chat', component: Chat, meta: { title: '聊天室' } },
  // SPDNet: 前缀详情页面
  { path: '/prefix/:id', component: PrefixDetail, meta: { title: '前缀详情' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - 联机破碎地牢`
  }
  next()
})

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(ElementPlus)
app.mount('#app')
