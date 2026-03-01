<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <div class="brand-section">
          <el-icon :size="48" class="brand-icon"><Connection /></el-icon>
          <h1 class="brand-title">
            <span class="gradient-text">SPD</span>Net
          </h1>
          <p class="brand-subtitle">联机破碎地牢</p>
        </div>
        <div class="features-list">
          <div class="feature-item" v-for="(item, index) in features" :key="index">
            <el-icon :size="20" color="var(--success-color)"><CircleCheck /></el-icon>
            <span>{{ item }}</span>
          </div>
        </div>
      </div>

      <div class="login-right">
        <el-card class="login-card" shadow="hover">
          <template #header>
            <div class="login-header">
              <h2>欢迎回来</h2>
              <p>登录你的账号继续冒险</p>
            </div>
          </template>

          <el-alert
            v-if="message"
            :title="message"
            :type="success ? 'success' : 'error'"
            show-icon
            :closable="false"
            class="login-alert"
          />

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            @keyup.enter="handleLogin"
            class="login-form"
          >
            <el-form-item prop="name">
              <el-input
                v-model="form.name"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                clearable
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                @click="handleLogin"
                :loading="loading"
                class="login-btn glow-btn"
              >
                <el-icon><Key /></el-icon>
                {{ loading ? '登录中...' : '立即登录' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer">
            <span>还没有账号？</span>
            <router-link to="/register" class="register-link">
              立即注册
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, ArrowRight, Connection, CircleCheck } from '@element-plus/icons-vue'
import { playerApi } from '../api'
import { authStore } from '../store/auth'

const router = useRouter()
const formRef = ref()

const form = reactive({
  name: '',
  password: ''
})

const rules = {
  name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 16, message: '用户名长度在 2 到 16 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度在 6 到 32 个字符', trigger: 'blur' }
  ]
}

const loading = ref(false)
const message = ref('')
const success = ref(false)

const features = [
  '与全球玩家实时联机',
  '查看详细的游戏数据',
  '参与排行榜竞争',
  '实时聊天交流'
]

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  message.value = ''

  try {
    const res = await playerApi.login({
      name: form.name,
      password: form.password
    })

    if (res.data.success) {
      success.value = true
      message.value = res.data.message
      authStore.login(res.data.data)
      ElMessage.success('登录成功')
      setTimeout(() => {
        router.push('/profile')
      }, 500)
    } else {
      success.value = false
      message.value = res.data.message
    }
  } catch (error) {
    success.value = false
    message.value = error.response?.data?.message || '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 64px - 100px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.login-container {
  display: flex;
  max-width: 1000px;
  width: 100%;
  gap: 4rem;
  align-items: center;
}

.login-left {
  flex: 1;
  padding: 2rem;
}

.brand-section {
  margin-bottom: 3rem;
}

.brand-icon {
  color: var(--primary-color);
  margin-bottom: 1rem;
  animation: float 3s ease-in-out infinite;
}

.brand-title {
  font-size: 2.5rem;
  font-weight: 800;
  margin-bottom: 0.5rem;
  letter-spacing: -1px;
}

.brand-subtitle {
  font-size: 1.25rem;
  color: var(--text-secondary);
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: var(--text-secondary);
  font-size: 1rem;
}

.login-right {
  flex: 1;
  max-width: 420px;
}

.login-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
}

:deep(.login-card .el-card__header) {
  border-bottom: 1px solid var(--border-color);
  padding: 2rem 2rem 1.5rem;
}

:deep(.login-card .el-card__body) {
  padding: 2rem;
}

.login-header {
  text-align: center;
}

.login-header h2 {
  font-size: 1.75rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.login-header p {
  color: var(--text-secondary);
}

.login-alert {
  margin-bottom: 1.5rem;
}

.login-form {
  margin-bottom: 1.5rem;
}

:deep(.login-form .el-input__wrapper) {
  background: var(--bg-dark);
  box-shadow: 0 0 0 1px var(--border-color) inset;
  padding: 4px 11px;
}

:deep(.login-form .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

:deep(.login-form .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.login-btn {
  width: 100%;
  font-size: 1rem;
  height: 44px;
}

.login-footer {
  text-align: center;
  color: var(--text-secondary);
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.register-link {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  margin-left: 0.5rem;
  transition: all 0.3s;
}

.register-link:hover {
  color: var(--primary-light);
  transform: translateX(4px);
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    gap: 2rem;
  }
  
  .login-left {
    text-align: center;
    padding: 1rem;
  }
  
  .features-list {
    display: none;
  }
  
  .login-right {
    width: 100%;
  }
}
</style>
