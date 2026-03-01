<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 左侧：用户信息 -->
      <el-col :xs="24" :lg="8">
        <el-card class="user-card" shadow="hover">
          <div class="user-header">
            <el-avatar :size="80" :icon="UserFilled" class="user-avatar" />
            <div class="user-info">
              <h2>{{ authStore.user?.name }}</h2>
              <el-tag :type="userInfo.role === '管理员' ? 'danger' : 'primary'" effect="dark" round>
                {{ userInfo.role || '玩家' }}
              </el-tag>
            </div>
          </div>
          
          <el-divider />
          
          <div class="user-details">
            <div class="detail-item">
              <el-icon><Message /></el-icon>
              <span class="label">邮箱</span>
              <span class="value">{{ userInfo.email || '-' }}</span>
            </div>
            <div class="detail-item">
              <el-icon><Calendar /></el-icon>
              <span class="label">注册时间</span>
              <span class="value">{{ formatDate(userInfo.createdAt) }}</span>
            </div>
            <div class="detail-item">
              <el-icon><Timer /></el-icon>
              <span class="label">最后登录</span>
              <span class="value">{{ userInfo.lastLoginAt ? formatDate(userInfo.lastLoginAt) : '从未登录' }}</span>
            </div>
            <div class="detail-item">
              <el-icon><Location /></el-icon>
              <span class="label">登录IP</span>
              <span class="value">{{ userInfo.lastLoginIp || '-' }}</span>
            </div>
          </div>
          
          <el-divider />
          
          <div class="user-actions">
            <el-button type="primary" @click="$router.push(`/player/${authStore.user?.name}`)">
              <el-icon><View /></el-icon>
              查看主页
            </el-button>
            <el-button type="danger" plain @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：设置 -->
      <el-col :xs="24" :lg="16">
        <el-tabs v-model="activeTab" class="settings-tabs">
          <el-tab-pane label="修改用户名" name="name">
            <el-card shadow="hover" class="settings-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Edit /></el-icon>
                  <span>修改用户名</span>
                </div>
              </template>
              
              <el-alert
                v-if="nameMessage"
                :title="nameMessage"
                :type="nameSuccess ? 'success' : 'error'"
                show-icon
                :closable="false"
                class="settings-alert"
              />
              
              <el-form :model="nameForm" label-position="top">
                <el-form-item label="新用户名">
                  <el-input
                    v-model="nameForm.newName"
                    placeholder="请输入新用户名"
                    :prefix-icon="User"
                    clearable
                  />
                </el-form-item>
                <el-form-item label="确认密码">
                  <el-input
                    v-model="nameForm.password"
                    type="password"
                    placeholder="请输入密码确认"
                    :prefix-icon="Lock"
                    show-password
                    clearable
                  />
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    @click="handleChangeName"
                    :loading="nameLoading"
                  >
                    <el-icon><Check /></el-icon>
                    确认修改
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="修改密码" name="password">
            <el-card shadow="hover" class="settings-card">
              <template #header>
                <div class="card-header">
                  <el-icon><Lock /></el-icon>
                  <span>修改密码</span>
                </div>
              </template>
              
              <el-alert
                v-if="pwdMessage"
                :title="pwdMessage"
                :type="pwdSuccess ? 'success' : 'error'"
                show-icon
                :closable="false"
                class="settings-alert"
              />
              
              <el-form :model="pwdForm" label-position="top">
                <el-form-item label="原密码">
                  <el-input
                    v-model="pwdForm.oldPassword"
                    type="password"
                    placeholder="请输入原密码"
                    :prefix-icon="Lock"
                    show-password
                    clearable
                  />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input
                    v-model="pwdForm.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    :prefix-icon="Key"
                    show-password
                    clearable
                  />
                </el-form-item>
                <el-form-item label="确认新密码">
                  <el-input
                    v-model="pwdForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    :prefix-icon="Key"
                    show-password
                    clearable
                  />
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    @click="handleChangePassword"
                    :loading="pwdLoading"
                  >
                    <el-icon><Check /></el-icon>
                    确认修改
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UserFilled, Message, Calendar, Timer, Location,
  View, SwitchButton, Edit, Lock, Key, Check
} from '@element-plus/icons-vue'
import { playerApi } from '../api'
import { authStore } from '../store/auth'

const router = useRouter()
const activeTab = ref('name')

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
    type: 'warning'
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
}

.user-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  margin-bottom: 1rem;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1rem;
}

.user-avatar {
  background: var(--gradient-primary);
}

.user-info h2 {
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
}

.user-details {
  padding: 0.5rem 0;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--border-color);
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item .el-icon {
  color: var(--primary-color);
  font-size: 1.1rem;
}

.detail-item .label {
  color: var(--text-secondary);
  width: 80px;
}

.detail-item .value {
  flex: 1;
  color: var(--text-primary);
}

.user-actions {
  display: flex;
  gap: 0.75rem;
}

.user-actions .el-button {
  flex: 1;
}

.settings-tabs :deep(.el-tabs__header) {
  margin-bottom: 1.5rem;
}

.settings-tabs :deep(.el-tabs__item) {
  font-size: 1rem;
}

.settings-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
}

.settings-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--primary-color);
}

.settings-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
}

.card-header .el-icon {
  color: var(--primary-color);
}

.settings-alert {
  margin-bottom: 1.5rem;
}

:deep(.settings-card .el-input__wrapper) {
  background: var(--bg-dark);
  box-shadow: 0 0 0 1px var(--border-color) inset;
}

:deep(.settings-card .el-input__wrapper:hover),
:deep(.settings-card .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

@media (max-width: 768px) {
  .user-header {
    flex-direction: column;
    text-align: center;
  }
  
  .user-actions {
    flex-direction: column;
  }
}
</style>
