<template>
  <div class="profile-page">
    <!-- Profile Header -->
    <div class="profile-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :icon="UserFilled" class="profile-avatar" />
            <div class="avatar-glow"></div>
          </div>
          <div class="user-info">
            <h1 class="user-name">{{ userInfo?.name || '加载中...' }}</h1>
            <div class="user-badges">
              <el-tag :type="getRoleType(userInfo?.role)" effect="dark" round size="large">
                {{ userInfo?.role || '玩家' }}
              </el-tag>
              <el-tag type="info" effect="dark" round size="large">
                <el-icon><Calendar /></el-icon>
                <span>注册于 {{ formatDate(userInfo?.createdAt) }} ({{ formatTimeAgo(userInfo?.createdAt) }})</span>
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Profile Content -->
    <div class="profile-content">
      <!-- Main Grid -->
      <div class="main-grid">
        <!-- Left Column -->
        <div class="left-column">
          <!-- Account Info Card -->
          <div class="info-card">
            <div class="card-header">
              <div class="header-icon-wrapper" style="background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);">
                <el-icon :size="20" color="white"><User /></el-icon>
              </div>
              <h3>账号信息</h3>
            </div>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">用户名</span>
                <span class="info-value">{{ userInfo?.name }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">角色</span>
                <span class="info-value">
                  <el-tag :type="getRoleType(userInfo?.role)" effect="dark" round size="small">
                    {{ userInfo?.role }}
                  </el-tag>
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">注册时间</span>
                <span class="info-value">{{ formatDateTimeWithAgo(userInfo?.createdAt) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">最后登录时间</span>
                <span class="info-value">{{ formatDateTimeWithAgo(userInfo?.lastLoginAt) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">最后登录IP</span>
                <span class="info-value">{{ userInfo?.lastLoginIp || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="info-card">
            <div class="card-header">
              <div class="header-icon-wrapper" style="background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);">
                <el-icon :size="20" color="white"><Tools /></el-icon>
              </div>
              <h3>快捷操作</h3>
            </div>
            <div class="action-list">
              <router-link :to="`/player/${userInfo?.name}`" class="action-item">
                <div class="action-icon" style="background: rgba(147, 51, 234, 0.12); color: var(--primary-400);">
                  <el-icon><View /></el-icon>
                </div>
                <div class="action-info">
                  <span class="action-title">查看主页</span>
                  <span class="action-desc">查看公开资料</span>
                </div>
                <el-icon class="action-arrow"><ArrowRight /></el-icon>
              </router-link>
            </div>
          </div>
        </div>

        <!-- Right Column -->
        <div class="right-column">
          <!-- Account Settings -->
          <div class="info-card">
            <div class="card-header">
              <div class="header-icon-wrapper" style="background: linear-gradient(135deg, #10b981 0%, #06b6d4 100%);">
                <el-icon :size="20" color="white"><Setting /></el-icon>
              </div>
              <h3>账号设置</h3>
            </div>
            <div class="settings-list">
              <div class="setting-item" @click="showChangeName = true">
                <div class="setting-icon" style="background: rgba(139, 92, 246, 0.12); color: #8b5cf6;">
                  <el-icon><Edit /></el-icon>
                </div>
                <div class="setting-info">
                  <span class="setting-title">修改昵称</span>
                  <span class="setting-desc">更改您的用户名</span>
                </div>
                <el-icon class="setting-arrow"><ArrowRight /></el-icon>
              </div>
              <div class="setting-item" @click="showChangePassword = true">
                <div class="setting-icon" style="background: rgba(6, 182, 212, 0.12); color: var(--accent-cyan);">
                  <el-icon><Lock /></el-icon>
                </div>
                <div class="setting-info">
                  <span class="setting-title">修改密码</span>
                  <span class="setting-desc">更新账号密码</span>
                </div>
                <el-icon class="setting-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Change Password Dialog -->
    <el-dialog
      v-model="showChangePassword"
      title="修改密码"
      width="400px"
      class="password-dialog"
      destroy-on-close
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-position="top"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button type="primary" :loading="changingPassword" @click="handleChangePassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>

    <!-- Change Name Dialog -->
    <el-dialog
      v-model="showChangeName"
      title="修改昵称"
      width="400px"
      class="password-dialog"
      destroy-on-close
    >
      <el-form
        ref="nameFormRef"
        :model="nameForm"
        :rules="nameRules"
        label-position="top"
      >
        <el-form-item label="当前密码" prop="password">
          <el-input
            v-model="nameForm.password"
            type="password"
            placeholder="请输入当前密码验证身份"
            show-password
          />
        </el-form-item>
        <el-form-item label="新昵称" prop="newName">
          <el-input
            v-model="nameForm.newName"
            placeholder="请输入新昵称"
            maxlength="16"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangeName = false">取消</el-button>
        <el-button type="primary" :loading="changingName" @click="handleChangeName">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  UserFilled, User, Calendar, Tools, View, Lock, ArrowRight, Edit, Setting
} from '@element-plus/icons-vue'
import { playerApi } from '../api'
import { authStore } from '../store/auth'

const router = useRouter()
const userInfo = ref(null)
const loading = ref(false)
const showChangePassword = ref(false)
const showChangeName = ref(false)
const changingPassword = ref(false)
const changingName = ref(false)
const passwordFormRef = ref()
const nameFormRef = ref()

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const nameForm = ref({
  password: '',
  newName: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const nameRules = {
  password: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newName: [
    { required: true, message: '请输入新昵称', trigger: 'blur' },
    { min: 2, max: 16, message: '长度在 2 到 16 个字符', trigger: 'blur' }
  ]
}

const getRoleType = (role) => {
  const types = {
    '管理员': 'danger',
    '玩家': 'primary'
  }
  return types[role] || 'primary'
}

const formatDate = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN')
}

const formatDateTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 格式化时间为"多久之前"的形式
const formatTimeAgo = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  const months = Math.floor(days / 30)
  const years = Math.floor(days / 365)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days < 30) return `${days} 天前`
  if (months < 12) return `${months} 个月前`
  return `${years} 年前`
}

// 格式化时间，带括号显示多久之前
const formatDateTimeWithAgo = (time) => {
  if (!time) return '-'
  const dateTime = formatDateTime(time)
  const ago = formatTimeAgo(time)
  return `${dateTime} (${ago})`
}

const loadUserInfo = async () => {
  loading.value = true
  try {
    const user = authStore.user
    if (user?.name) {
      // 获取公开信息（包含游戏统计）
      const publicRes = await playerApi.getPlayerPublicInfo(user.name)
      // 获取私密信息（包含IP等）
      const privateRes = await playerApi.getPlayerPrivateInfo(user.name)

      if (publicRes.data.success && privateRes.data.success) {
        userInfo.value = {
          ...publicRes.data.data,
          ...privateRes.data.data
        }
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changingPassword.value = true
      try {
        const res = await playerApi.changePassword({
          name: authStore.user?.name,
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        })
        if (res.data.success) {
          ElMessage.success('密码修改成功')
          showChangePassword.value = false
          passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
        } else {
          ElMessage.error(res.data.message || '密码修改失败')
        }
      } catch (error) {
        console.error('密码修改失败:', error)
        ElMessage.error('密码修改失败')
      } finally {
        changingPassword.value = false
      }
    }
  })
}

const handleChangeName = async () => {
  if (!nameFormRef.value) return

  await nameFormRef.value.validate(async (valid) => {
    if (valid) {
      changingName.value = true
      try {
        const res = await playerApi.changeName({
          currentName: authStore.user?.name,
          password: nameForm.value.password,
          newName: nameForm.value.newName
        })
        if (res.data.success) {
          ElMessage.success('昵称修改成功，请重新登录')
          showChangeName.value = false
          nameForm.value = { password: '', newName: '' }
          // 更新本地存储的用户名
          authStore.user.name = res.data.data.name
          authStore.user.role = res.data.data.role
          // 刷新页面以更新显示
          setTimeout(() => {
            window.location.reload()
          }, 1000)
        } else {
          ElMessage.error(res.data.message || '昵称修改失败')
        }
      } catch (error) {
        console.error('昵称修改失败:', error)
        ElMessage.error('昵称修改失败')
      } finally {
        changingName.value = false
      }
    }
  })
}

onMounted(() => {
  if (!authStore.isLoggedIn) {
    router.push('/login')
    return
  }
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--content-padding) var(--space-6);
}

/* Profile Header */
.profile-header {
  position: relative;
  margin: 0 calc(-1 * var(--content-padding)) var(--space-6);
  padding: var(--space-8) var(--content-padding);
  overflow: hidden;
}

.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(147, 51, 234, 0.15) 0%, rgba(6, 182, 212, 0.1) 100%);
}

.header-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 50% 0%, rgba(147, 51, 234, 0.2) 0%, transparent 70%);
}

.header-content {
  position: relative;
  z-index: 1;
  max-width: var(--max-width);
  margin: 0 auto;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: var(--space-5);
}

.avatar-wrapper {
  position: relative;
}

.profile-avatar {
  background: var(--gradient-primary);
  font-size: 2.5rem;
  border: 4px solid var(--surface-1);
  box-shadow: var(--shadow-lg);
}

.avatar-glow {
  position: absolute;
  inset: -8px;
  border-radius: 50%;
  background: var(--gradient-primary);
  filter: blur(20px);
  opacity: 0.3;
  z-index: -1;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.user-name {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.user-badges {
  display: flex;
  gap: var(--space-2);
}

/* Profile Content */
.profile-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

/* Main Grid */
.main-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}

.left-column,
.right-column {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

/* Info Card */
.info-card {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-subtle);
}

.header-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

/* Info List */
.info-list {
  padding: var(--space-2);
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.info-item:hover {
  background: var(--surface-2);
}

.info-label {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.info-value {
  color: var(--text-primary);
  font-weight: 500;
}

/* Action List */
.action-list {
  padding: var(--space-2);
}

.action-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  cursor: pointer;
  text-decoration: none;
  transition: all var(--transition-fast);
}

.action-item:hover {
  background: var(--surface-2);
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.action-title {
  font-weight: 500;
  color: var(--text-primary);
}

.action-desc {
  font-size: 0.8125rem;
  color: var(--text-tertiary);
}

.action-arrow {
  color: var(--text-tertiary);
  transition: all var(--transition-fast);
}

.action-item:hover .action-arrow {
  color: var(--primary-400);
  transform: translateX(3px);
}

/* Settings List */
.settings-list {
  padding: var(--space-2);
}

.setting-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.setting-item:hover {
  background: var(--surface-2);
}

.setting-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.setting-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.setting-title {
  font-weight: 500;
  color: var(--text-primary);
}

.setting-desc {
  font-size: 0.8125rem;
  color: var(--text-tertiary);
}

.setting-arrow {
  color: var(--text-tertiary);
  transition: all var(--transition-fast);
}

.setting-item:hover .setting-arrow {
  color: var(--primary-400);
  transform: translateX(3px);
}

/* Dialog */
:deep(.password-dialog) {
  background: var(--surface-1);
  border-radius: var(--radius-xl);
}

:deep(.password-dialog .el-dialog__header) {
  border-bottom: 1px solid var(--border-subtle);
  padding: var(--space-4);
  margin: 0;
}

:deep(.password-dialog .el-dialog__title) {
  color: var(--text-primary);
  font-weight: 600;
}

:deep(.password-dialog .el-dialog__body) {
  padding: var(--space-4);
}

:deep(.password-dialog .el-dialog__footer) {
  border-top: 1px solid var(--border-subtle);
  padding: var(--space-3) var(--space-4);
}

:deep(.password-dialog .el-form-item__label) {
  color: var(--text-secondary);
}

/* Responsive */
@media (max-width: 1024px) {
  .main-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .profile-header {
    padding: var(--space-6) var(--content-padding);
  }

  .avatar-section {
    flex-direction: column;
    text-align: center;
  }

  .user-name {
    font-size: 1.5rem;
  }

  .user-badges {
    justify-content: center;
  }
}
</style>
