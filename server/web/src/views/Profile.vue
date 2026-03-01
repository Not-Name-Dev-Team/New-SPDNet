<template>
  <div class="profile-page">
    <div class="profile-grid">
      <!-- Left Column - User Info -->
      <div class="profile-sidebar">
        <div class="user-card">
          <div class="user-header">
            <div class="avatar-wrapper">
              <el-avatar :size="100" :icon="UserFilled" class="user-avatar" />
              <span class="status-badge online">在线</span>
            </div>
            <div class="user-info">
              <h1>{{ authStore.user?.name }}</h1>
              <el-tag
                :type="userInfo.role === '管理员' ? 'danger' : 'primary'"
                effect="light"
                round
                size="large"
              >
                {{ userInfo.role || '玩家' }}
              </el-tag>
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
            <router-link :to="`/player/${authStore.user?.name}`" class="btn-primary btn-full">
              <el-icon><View /></el-icon>
              <span>查看主页</span>
            </router-link>
            <button @click="handleLogout" class="btn-danger btn-full">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Right Column - Settings -->
      <div class="profile-main">
        <div class="settings-tabs">
          <div class="tabs-header">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              :class="['tab-btn', { active: activeTab === tab.key }]"
              @click="activeTab = tab.key"
            >
              <el-icon><component :is="tab.icon" /></el-icon>
              <span>{{ tab.label }}</span>
            </button>
          </div>

          <div class="tab-content">
            <!-- Change Name Tab -->
            <div v-if="activeTab === 'name'" class="settings-panel">
              <div class="panel-header">
                <h3>修改用户名</h3>
                <p>更改你在游戏中的显示名称</p>
              </div>

              <el-alert
                v-if="nameMessage"
                :title="nameMessage"
                :type="nameSuccess ? 'success' : 'error'"
                show-icon
                :closable="false"
                class="panel-alert"
              />

              <el-form :model="nameForm" class="settings-form">
                <el-form-item label="新用户名">
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><User /></el-icon>
                    <el-input
                      v-model="nameForm.newName"
                      placeholder="请输入新用户名"
                      clearable
                    />
                  </div>
                </el-form-item>
                <el-form-item label="确认密码">
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Lock /></el-icon>
                    <el-input
                      v-model="nameForm.password"
                      type="password"
                      placeholder="请输入密码确认"
                      show-password
                      clearable
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
                <h3>修改密码</h3>
                <p>更新你的账号密码</p>
              </div>

              <el-alert
                v-if="pwdMessage"
                :title="pwdMessage"
                :type="pwdSuccess ? 'success' : 'error'"
                show-icon
                :closable="false"
                class="panel-alert"
              />

              <el-form :model="pwdForm" class="settings-form">
                <el-form-item label="原密码">
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Lock /></el-icon>
                    <el-input
                      v-model="pwdForm.oldPassword"
                      type="password"
                      placeholder="请输入原密码"
                      show-password
                      clearable
                    />
                  </div>
                </el-form-item>
                <el-form-item label="新密码">
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Key /></el-icon>
                    <el-input
                      v-model="pwdForm.newPassword"
                      type="password"
                      placeholder="请输入新密码"
                      show-password
                      clearable
                    />
                  </div>
                </el-form-item>
                <el-form-item label="确认新密码">
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Key /></el-icon>
                    <el-input
                      v-model="pwdForm.confirmPassword"
                      type="password"
                      placeholder="请再次输入新密码"
                      show-password
                      clearable
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
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UserFilled, Message, Calendar, Timer, Location,
  View, SwitchButton, Edit, Lock, Key, Check, User
} from '@element-plus/icons-vue'
import { playerApi } from '../api'
import { authStore } from '../store/auth'

const router = useRouter()
const activeTab = ref('name')

const tabs = [
  { key: 'name', label: '修改用户名', icon: Edit },
  { key: 'password', label: '修改密码', icon: Lock }
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
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-8) var(--content-padding);
}

.profile-grid {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: var(--space-6);
}

/* Sidebar */
.profile-sidebar {
  position: sticky;
  top: calc(var(--header-height) + var(--space-8));
  height: fit-content;
}

.user-card {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.user-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-8);
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(168, 85, 247, 0.05) 100%);
  border-bottom: 1px solid var(--border-subtle);
}

.avatar-wrapper {
  position: relative;
  margin-bottom: var(--space-4);
}

.user-avatar {
  background: var(--gradient-primary);
  box-shadow: var(--shadow-glow-sm);
}

.status-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-full);
  font-size: 0.6875rem;
  font-weight: 600;
  text-transform: uppercase;
  border: 2px solid var(--surface-1);
}

.status-badge.online {
  background: var(--accent-emerald);
  color: white;
}

.user-info {
  text-align: center;
}

.user-info h1 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0 0 var(--space-2);
  color: var(--text-primary);
}

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
  background: var(--surface-2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-400);
  flex-shrink: 0;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}

.detail-label {
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.detail-value {
  font-size: 0.9375rem;
  color: var(--text-primary);
  font-weight: 500;
  word-break: break-all;
}

.user-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-5);
  border-top: 1px solid var(--border-subtle);
}

.btn-full {
  width: 100%;
  justify-content: center;
}

.btn-danger {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: rgba(244, 63, 94, 0.1);
  border: 1px solid rgba(244, 63, 94, 0.2);
  color: var(--accent-rose);
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-danger:hover {
  background: rgba(244, 63, 94, 0.2);
}

/* Main Content */
.profile-main {
  min-width: 0;
}

.settings-tabs {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid var(--border-subtle);
  background: var(--surface-2);
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-6);
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-size: 0.9375rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}

.tab-btn:hover {
  color: var(--text-primary);
  background: var(--surface-3);
}

.tab-btn.active {
  color: var(--primary-400);
  border-bottom-color: var(--primary-500);
  background: var(--surface-1);
}

.tab-content {
  padding: var(--space-6);
}

.settings-panel {
  max-width: 480px;
}

.panel-header {
  margin-bottom: var(--space-6);
}

.panel-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 0 var(--space-1);
  color: var(--text-primary);
}

.panel-header p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 0.9375rem;
}

.panel-alert {
  margin-bottom: var(--space-5);
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

:deep(.settings-form .el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
  padding-bottom: var(--space-2);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: var(--space-4);
  color: var(--text-tertiary);
  z-index: 1;
}

:deep(.input-wrapper .el-input__wrapper) {
  padding-left: var(--space-10);
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
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
    gap: var(--space-4);
  }

  .user-info {
    text-align: left;
  }
}

@media (max-width: 640px) {
  .profile-page {
    padding: var(--space-4);
  }

  .tab-btn span {
    display: none;
  }

  .tab-btn {
    flex: 1;
    justify-content: center;
  }
}
</style>
