// SPDNet: 前端路由中心
// 采用路由级代码分包(懒加载)，并为受保护页面添加登录/管理员守卫
import { createRouter, createWebHistory } from 'vue-router'
import { authStore } from '../store/auth'

// 判断当前用户是否具有管理员权限（兼容中英文角色枚举）
function isAdmin(user) {
  const role = user?.role
  return role === 'ADMIN' || role === '管理员'
}

const routes = [
  { path: '/', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
  { path: '/register', component: () => import('../views/Register.vue'), meta: { title: '注册' } },
  { path: '/login', component: () => import('../views/Login.vue'), meta: { title: '登录' } },
  { path: '/forgot-password', component: () => import('../views/ForgotPassword.vue'), meta: { title: '忘记密码' } },
  { path: '/leaderboard', component: () => import('../views/Leaderboard.vue'), meta: { title: '排行榜' } },
  { path: '/daily-challenge', component: () => import('../views/DailyChallenge.vue'), meta: { title: '每日挑战' } },
  { path: '/player/:name', component: () => import('../views/Player.vue'), meta: { title: '玩家详情' } },
  { path: '/dashboard', component: () => import('../views/Profile.vue'), meta: { title: '个人中心', requiresAuth: true } },
  { path: '/admin', component: () => import('../views/Admin.vue'), meta: { title: '后台管理', requiresAuth: true, requiresAdmin: true } },
  { path: '/chat', component: () => import('../views/Chat.vue'), meta: { title: '聊天室', requiresAuth: true } },
  { path: '/prefix/:id', component: () => import('../views/PrefixDetail.vue'), meta: { title: '前缀详情' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：设置页面标题 + 权限拦截
router.beforeEach((to, _from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - 联机破碎地牢`
  }

  const user = authStore.user

  // 需要登录的页面
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    // 记录来源页，登录后跳回
    return next({ path: '/login', query: to.fullPath === '/' ? {} : { redirect: to.fullPath } })
  }

  // 需要管理员权限的页面
  if (to.meta.requiresAdmin && !isAdmin(user)) {
    return next({ path: '/dashboard' })
  }

  next()
})

export default router