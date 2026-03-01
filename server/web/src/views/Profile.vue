<template>
  <div class="profile-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon :size="32"><User /></el-icon>
        </div>
        <div class="header-text">
          <h1>个人中心</h1>
          <p>管理你的账号信息和安全设置</p>
        </div>
      </div>
    </div>

    <div class="profile-grid">
      <!-- Left Column - User Info -->
      <div class="profile-sidebar">
        <div class="user-card">
          <div class="card-glow"></div>
          <div class="user-header">
            <div class="avatar-wrapper">
              <div class="avatar-ring"></div>
              <el-avatar :size="100" :icon="UserFilled" class="user-avatar" />
              <div class="status-badge">
                <span class="status-dot"></span>
                <span class="status-text">在线</span>
              </div>
            </div>
            <div class="user-info">
              <h1>{{ authStore.user?.name }}</h1>
              <el-tag
                :type="userInfo.role === '管理员' ? 'danger' : 'primary'"
                effect="dark"
                round
                size="large"
                class="role-tag"
              >
                <el-icon v-if="userInfo.role === '管理员'"><StarFilled /></el-icon>
                <el-icon v-else><User /></el-icon>
                {{ userInfo.role || '玩家' }}
              </el-tag>
            </div>
          </div>

          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.gameCount || 0 }}</div>
              <div class="stat-label">游戏场次</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.winCount || 0 }}</div>
              <div class="stat-label">胜利次数</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.winRate || '0%' }}</div>
              <div class="stat-label">胜率</div>
            </div>
          </div>

          <div class="user-details">
            <div class="detail-item">
              <div class="detail-icon">
                <el-icon><Message /></el-icon>
              </div>
              <div class="detail-content">
                <span class="detail-label">邮箱</span>
                <span class="detail-value">{{ userInfo.email || '-' }}</span>
              </div>
            </div>
            <div class="detail-item">
              <div class="detail-icon">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="detail-content">
                <span class="detail-label">注册时间</span>
                <span class="detail-value">{{ formatDate(userInfo.createdAt) }}</span>
              </div>
            </div>
            <div class="detail-item">
              <div class="detail-icon">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="detail-content">
                <span class="detail-label">最后登录</span>
                <span class="detail-value">{{ userInfo.lastLoginAt ? formatDate(userInfo.lastLoginAt) : '从未登录' }}</span>
              </div>
            </div>
            <div class="detail-item">
              <div class="detail-icon">
                <el-icon><Location /></el-icon>
              </div>
              <div class="detail-content">
                <span class="detail-label">登录IP</span>
                <span class="detail-value">{{ userInfo.lastLoginIp || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="user-actions">
            <router-link :to="`/player/${authStore.user?.name}`" class="action-btn primary">
              <el-icon><View /></el-icon>
              <span>查看主页</span>
            </router-link>
            <button @click="handleLogout" class="action-btn danger">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Right Column - Settings -->
      <div class="profile-main">
        <div class="settings-card">
          <div class="card-glow"></div>
          <div class="settings-tabs">
            <div class="tabs-header">
              <button
                v-for="tab in tabs"
                :key="tab.key"
                :class="['tab-btn', { active: activeTab === tab.key }]"
                @click="activeTab = tab.key"
              >
                <div class="tab-icon">
                  <el-icon><component :is="tab.icon" /></el-icon>
                </div>
                <div class="tab-info">
                  <span class="tab-label">{{ tab.label }}</span>
                  <span class="tab-desc">{{ tab.desc }}</span>
                </div>
              </button>
            </div>

            <div class="tab-content">
              <!-- Change Name Tab -->
              <div v-if="activeTab === 'name'" class="settings-panel">
                <div class="panel-header">
                  <div class="panel-icon">
                    <el-icon><Edit /></el-icon>
                  </div>
                  <div class="panel-info">
                    <h3>修改用户名</h3>
                    <p>更改你在游戏中的显示名称，每月限修改一次</p>
                  </div>
                </div>

                <el-alert
                  v-if="nameMessage"
                  :title="nameMessage"
                  :type="nameSuccess ? 'success' : 'error'"
                  show-icon
                  :closable="false"
                  class="panel-alert"
                  :effect="nameSuccess ? 'dark' : 'light'"
                />

                <el-form :model="nameForm" class="settings-form">
                  <el-form-item>
                    <label class="form-label">
                      <el-icon><User /></el-icon>
                      新用户名
                    </label>
                    <div class="input-wrapper">
                      <el-input
                        v-model="nameForm.newName"
                        placeholder="请输入新用户名"
                        clearable
                        class="custom-input"
                      />
                    </div>
                  </el-form-item>
                  <el-form-item>
                    <label class="form-label">
                      <el-icon><Lock /></el-icon>
                      确认密码
                    </label>
                    <div class="input-wrapper">
                      <el-input
                        v-model="nameForm.password"
                        type="password"
                        placeholder="请输入密码确认身份"
                        show-password
                        clearable
                        class="custom-input"
                      />
                    </div>
                  </el-form-item>
                  <el-form-item>
                    <el-button
                      type="primary"
                      @click="handleChangeName"
                      :loading="nameLoading"
                      class="submit-btn"
                    >
                      <el-icon><Check /></el-icon>
                      <span>确认修改</span>
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>

              <!-- Change Password Tab -->
              <div v-if="activeTab === 'password'" class="settings-panel">
                <div class="panel-header">
                  <div class="panel-icon password">
                    <el-icon><Lock /></el-icon>
                  </div>
                  <div class="panel-info">
                    <h3>修改密码</h3>
                    <p>定期更新密码可以保护你的账号安全</p>
                  </div>
                </div>

                <el-alert
                  v-if="pwdMessage"
                  :title="pwdMessage"
                  :type="pwdSuccess ? 'success' : 'error'"
                  show-icon
                  :closable="false"
                  class="panel-alert"
                  :effect="pwdSuccess ? 'dark' : 'light'"
                />

                <el-form :model="pwdForm" class="settings-form">
                  <el-form-item>
                    <label class="form-label">
                      <el-icon><Lock /></el-icon>
                      原密码
                    </label>
                    <div class="input-wrapper">
                      <el-input
                        v-model="pwdForm.oldPassword"
                        type="password"
                        placeholder="请输入当前密码"
                        show-password
                        clearable
                        class="custom-input"
                      />
                    </div>
                  </el-form-item>
                  <el-form-item>
                    <label class="form-label">
                      <el-icon><Key /></el-icon>
                      新密码
                    </label>
                    <div class="input-wrapper">
                      <el-input
                        v-model="pwdForm.newPassword"
                        type="password"
                        placeholder="请输入新密码（至少6位）"
                        show-password
                        clearable
                        class="custom-input"
                      />
                    </div>
                    <div class="password-strength" v-if="pwdForm.newPassword">
                      <div class="strength-bar">
                        <div class="strength-fill" :style="{ width: passwordStrength + '%', background: strengthColor }"></div>
                      </div>
                      <span class="strength-text">{{ strengthText }}</span>
                    </div>
                  </el-form-item>
                  <el-form-item>
                    <label class="form-label">
                      <el-icon><Key /></el-icon>
                      确认新密码
                    </label>
                    <div class="input-wrapper">
                      <el-input
                        v-model="pwdForm.confirmPassword"
                        type="password"
                        placeholder="请再次输入新密码"
                        show-password
                        clearable
                        class="custom-input"
                      />
                    </div>
                  </el-form-item>
                  <el-form-item>
                    <el-button
                      type="primary"
                      @click="handleChangePassword"
                      :loading="pwdLoading"
                      class="submit-btn"
                    >
                      <el-icon><Check /></el-icon>
                      <span>确认修改</span>
                    </el-button>
                  </el-form-item>
                </el-form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UserFilled, Message, Calendar, Timer, Location,
  View, SwitchButton, Edit, Lock, Key, Check, User, StarFilled
} from '@element-plus/icons-vue'
import { playerApi } from '../api'
import { authStore } from '../store/auth'

const router = useRouter()
const activeTab = ref('name')

const tabs = [
  { key: 'name', label: '修改用户名', desc: '更改显示名称', icon: Edit },
  { key: 'password', label: '修改密码', desc: '更新安全密码', icon: Lock }
]

const userInfo = ref({})

const nameForm = reactive({
  newName: '',
  password: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const nameLoading = ref(false)
const nameMessage = ref('')
const nameSuccess = ref(false)

const pwdLoading = ref(false)
const pwdMessage = ref('')
const pwdSuccess = ref(false)

const passwordStrength = computed(() => {
  const pwd = pwdForm.newPassword
  if (!pwd) return 0
  let strength = 0
  if (pwd.length >= 6) strength += 20
  if (pwd.length >= 10) strength += 20
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) strength += 20
  if (/\d/.test(pwd)) strength += 20
  if (/[^a-zA-Z0-9]/.test(pwd)) strength += 20
  return strength
})

const strengthColor = computed(() => {
  const strength = passwordStrength.value
  if (strength <= 20) return '#ef4444'
  if (strength <= 40) return '#f59e0b'
  if (strength <= 60) return '#eab308'
  if (strength <= 80) return '#22c55e'
  return '#10b981'
})

const strengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength <= 20) return '太弱'
  if (strength <= 40) return '较弱'
  if (strength <= 60) return '一般'
  if (strength <= 80) return '较强'
  return '非常强'
})

const loadUserInfo = async () => {
  if (!authStore.user?.name) return
  try {
    const res = await playerApi.getPlayerPrivateInfo(authStore.user.name)
    if (res.data.success) {
      userInfo.value = res.data.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const handleChangeName = async () => {
  if (!nameForm.newName.trim()) {
    nameSuccess.value = false
    nameMessage.value = '请输入新用户名'
    return
  }

  nameLoading.value = true
  nameMessage.value = ''

  try {
    const res = await playerApi.changeName({
      currentName: authStore.user.name,
      password: nameForm.password,
      newName: nameForm.newName
    })

    if (res.data.success) {
      nameSuccess.value = true
      nameMessage.value = res.data.message
      authStore.updateUser(res.data.data)
      nameForm.newName = ''
      nameForm.password = ''
    } else {
      nameSuccess.value = false
      nameMessage.value = res.data.message
    }
  } catch (error) {
    nameSuccess.value = false
    nameMessage.value = error.response?.data?.message || '修改失败'
  } finally {
    nameLoading.value = false
  }
}

const handleChangePassword = async () => {
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    pwdSuccess.value = false
    pwdMessage.value = '两次输入的新密码不一致'
    return
  }

  pwdLoading.value = true
  pwdMessage.value = ''

  try {
    const res = await playerApi.changePassword({
      name: authStore.user.name,
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })

    if (res.data.success) {
      pwdSuccess.value = true
      pwdMessage.value = res.data.message
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } else {
      pwdSuccess.value = false
      pwdMessage.value = res.data.message
    }
  } catch (error) {
    pwdSuccess.value = false
    pwdMessage.value = error.response?.data?.message || '修改失败'
  } finally {
    pwdLoading.value = false
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    customClass: 'custom-message-box'
  }).then(() => {
    authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  }).catch(() => {})
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6) var(--content-padding);
}

/* Page Header */
.page-header {
  margin-bottom: var(--space-6);
}

.header-content {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.header-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, #8b5cf6 0%, #a855f7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.4);
}

.header-text h1 {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.header-text p {
  color: var(--text-secondary);
  margin: var(--space-1) 0 0;
}

/* Profile Grid */
.profile-grid {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: var(--space-6);
}

/* Sidebar */
.profile-sidebar {
  position: sticky;
  top: calc(var(--header-height) + var(--space-6));
  height: fit-content;
}

.user-card {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  position: relative;
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.5), transparent);
}

.user-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-8) var(--space-6);
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1) 0%, rgba(168, 85, 247, 0.05) 100%);
  border-bottom: 1px solid var(--border-subtle);
  position: relative;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: var(--space-4);
}

.avatar-ring {
  position: absolute;
  inset: -8px;
  border: 2px solid rgba(139, 92, 246, 0.3);
  border-radius: 50%;
  animation: rotate 10s linear infinite;
}

.avatar-ring::before {
  content: '';
  position: absolute;
  inset: -4px;
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: 50%;
  animation: rotate 15s linear infinite reverse;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.user-avatar {
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  border: 3px solid rgba(139, 92, 246, 0.3);
  box-shadow: 0 0 30px rgba(139, 92, 246, 0.3);
}

.status-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: rgba(16, 185, 129, 0.2);
  border: 1px solid rgba(16, 185, 129, 0.3);
  border-radius: var(--radius-full);
  font-size: 0.6875rem;
  font-weight: 600;
  color: #10b981;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 6px #10b981;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.user-info {
  text-align: center;
}

.user-info h1 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 var(--space-3);
  color: var(--text-primary);
}

.role-tag {
  font-weight: 600;
}

.role-tag :deep(.el-icon) {
  margin-right: 4px;
}

/* User Stats */
.user-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background: rgba(20, 20, 35, 0.5);
  border-bottom: 1px solid var(--border-subtle);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #a78bfa;
  line-height: 1;
}

.stat-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 30px;
  background: var(--border-subtle);
}

/* User Details */
.user-details {
  padding: var(--space-5);
}

.detail-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-subtle);
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: rgba(139, 92, 246, 0.1);
  border: 1px solid rgba(139, 92, 246, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a78bfa;
  flex-shrink: 0;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.detail-label {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.detail-value {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 500;
  word-break: break-all;
}

/* User Actions */
.user-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-5);
  border-top: 1px solid var(--border-subtle);
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  font-size: 0.9375rem;
}

.action-btn.primary {
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  color: white;
}

.action-btn.primary:hover {
  background: linear-gradient(135deg, #7c3aed, #9333ea);
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.4);
  transform: translateY(-1px);
}

.action-btn.danger {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

.action-btn.danger:hover {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.3);
}

/* Main Content */
.profile-main {
  min-width: 0;
}

.settings-card {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  position: relative;
}

.settings-tabs {
  display: flex;
  flex-direction: column;
  min-height: 500px;
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid var(--border-subtle);
  background: rgba(20, 20, 35, 0.5);
  padding: var(--space-2);
  gap: var(--space-2);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
  flex: 1;
  text-align: left;
}

.tab-btn:hover {
  background: rgba(139, 92, 246, 0.1);
  color: var(--text-primary);
}

.tab-btn.active {
  background: rgba(139, 92, 246, 0.15);
  border-color: rgba(139, 92, 246, 0.3);
  color: #a78bfa;
}

.tab-icon {
  width: 44px;
  height: 44px;
  background: rgba(139, 92, 246, 0.1);
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.tab-btn.active .tab-icon {
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  border-color: transparent;
  color: white;
}

.tab-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.tab-label {
  font-size: 0.9375rem;
  font-weight: 600;
}

.tab-desc {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.tab-content {
  flex: 1;
  padding: var(--space-8);
}

.settings-panel {
  max-width: 480px;
  margin: 0 auto;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-8);
}

.panel-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(168, 85, 247, 0.1));
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a78bfa;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.panel-icon.password {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2), rgba(251, 191, 36, 0.1));
  border-color: rgba(245, 158, 11, 0.3);
  color: #fbbf24;
}

.panel-info h3 {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0 0 var(--space-1);
  color: var(--text-primary);
}

.panel-info p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 0.875rem;
}

.panel-alert {
  margin-bottom: var(--space-6);
  border-radius: var(--radius-lg);
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
}

.form-label .el-icon {
  color: #a78bfa;
}

.input-wrapper {
  position: relative;
}

.custom-input :deep(.el-input__wrapper) {
  background: rgba(15, 15, 25, 0.6);
  border: 1px solid rgba(139, 92, 246, 0.2);
  box-shadow: none;
  padding: var(--space-3) var(--space-4);
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(139, 92, 246, 0.4);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(139, 92, 246, 0.6);
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
}

.password-strength {
  margin-top: var(--space-2);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.strength-text {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  min-width: 50px;
  text-align: right;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-weight: 600;
  font-size: 1rem;
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  border: none;
  border-radius: var(--radius-lg);
  margin-top: var(--space-2);
}

.submit-btn:hover {
  background: linear-gradient(135deg, #7c3aed, #9333ea);
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.4);
}

/* Responsive */
@media (max-width: 1024px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    position: static;
  }

  .user-header {
    flex-direction: row;
    text-align: left;
    gap: var(--space-6);
  }

  .user-info {
    text-align: left;
  }

  .tabs-header {
    flex-direction: column;
  }

  .tab-btn {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .profile-page {
    padding: var(--space-4);
  }

  .header-content {
    flex-direction: column;
    text-align: center;
  }

  .user-header {
    flex-direction: column;
    text-align: center;
  }

  .user-info {
    text-align: center;
  }

  .user-stats {
    flex-direction: column;
    gap: var(--space-4);
  }

  .stat-divider {
    width: 50%;
    height: 1px;
  }

  .tab-content {
    padding: var(--space-5);
  }

  .panel-header {
    flex-direction: column;
    text-align: center;
  }
}
</style>