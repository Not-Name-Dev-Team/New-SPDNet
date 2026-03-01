<template>
  <div class="auth-page">
    <div class="auth-container">
      <!-- Left Side - Branding -->
      <div class="auth-branding">
        <div class="branding-content">
          <div class="brand-logo">
            <div class="logo-icon">
              <el-icon :size="32"><Connection /></el-icon>
            </div>
            <h1 class="brand-title">
              <span class="gradient-text">SPD</span>Net
            </h1>
          </div>
          <p class="brand-tagline">联机破碎地牢</p>
          <p class="brand-description">
            加入全球玩家的冒险之旅，探索无尽地牢，创造属于你的传奇故事。
          </p>

          <div class="feature-list">
            <div class="feature-item" v-for="(item, index) in features" :key="index">
              <div class="feature-check">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <span>{{ item }}</span>
            </div>
          </div>
        </div>

        <div class="branding-visual">
          <div class="visual-glow"></div>
          <div class="visual-card">
            <el-icon :size="64"><Connection /></el-icon>
          </div>
        </div>
      </div>

      <!-- Right Side - Form -->
      <div class="auth-form-wrapper">
        <div class="form-card">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>登录你的账号继续冒险</p>
          </div>

          <el-alert
            v-if="message"
            :title="message"
            :type="success ? 'success' : 'error'"
            show-icon
            :closable="false"
            class="form-alert"
          />

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            @keyup.enter="handleLogin"
            class="auth-form"
          >
            <el-form-item prop="name">
              <div class="input-wrapper">
                <el-icon class="input-icon"><User /></el-icon>
                <el-input
                  v-model="form.name"
                  placeholder="请输入用户名"
                  size="large"
                  clearable
                />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-wrapper">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                  clearable
                />
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                @click="handleLogin"
                :loading="loading"
                class="submit-btn"
              >
                <el-icon><Key /></el-icon>
                <span>{{ loading ? '登录中...' : '立即登录' }}</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <span>还没有账号？</span>
            <router-link to="/register" class="link-primary">
              立即注册
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </div>
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
.auth-page {
  min-height: calc(100vh - var(--header-height));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) var(--content-padding);
}

.auth-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-12);
  max-width: 1200px;
  width: 100%;
  align-items: center;
}

/* Branding Side */
.auth-branding {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
  padding: var(--space-8);
}

.branding-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.logo-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: var(--shadow-glow-sm);
}

.brand-title {
  font-size: 2.5rem;
  font-weight: 800;
  margin: 0;
  letter-spacing: -0.02em;
}

.brand-tagline {
  font-size: 1.25rem;
  color: var(--text-secondary);
  margin: 0;
}

.brand-description {
  font-size: 1rem;
  color: var(--text-tertiary);
  line-height: 1.7;
  max-width: 400px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-top: var(--space-2);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  color: var(--text-secondary);
  font-size: 0.9375rem;
}

.feature-check {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full);
  background: rgba(16, 185, 129, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-emerald);
  flex-shrink: 0;
}

.branding-visual {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
}

.visual-glow {
  position: absolute;
  width: 300px;
  height: 300px;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  filter: blur(80px);
  opacity: 0.2;
  animation: pulse 4s ease-in-out infinite;
}

.visual-card {
  width: 180px;
  height: 180px;
  border-radius: var(--radius-2xl);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-400);
  position: relative;
  animation: float 6s ease-in-out infinite;
}

/* Form Side */
.auth-form-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-card {
  width: 100%;
  max-width: 420px;
  padding: var(--space-8);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
}

.form-header {
  text-align: center;
  margin-bottom: var(--space-6);
}

.form-header h2 {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 var(--space-2);
  color: var(--text-primary);
}

.form-header p {
  color: var(--text-secondary);
  margin: 0;
}

.form-alert {
  margin-bottom: var(--space-5);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
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
  height: 48px;
  font-size: 1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  margin-top: var(--space-6);
  padding-top: var(--space-6);
  border-top: 1px solid var(--border-subtle);
  color: var(--text-secondary);
  font-size: 0.9375rem;
}

.link-primary {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--primary-400);
  text-decoration: none;
  font-weight: 600;
  transition: all var(--transition-fast);
}

.link-primary:hover {
  color: var(--primary-300);
  transform: translateX(2px);
}

/* Animations */
@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.15;
    transform: scale(1);
  }
  50% {
    opacity: 0.25;
    transform: scale(1.1);
  }
}

/* Responsive */
@media (max-width: 1024px) {
  .auth-container {
    grid-template-columns: 1fr;
    gap: var(--space-8);
  }

  .auth-branding {
    text-align: center;
    padding: var(--space-4);
  }

  .brand-logo {
    justify-content: center;
  }

  .brand-description {
    margin-left: auto;
    margin-right: auto;
  }

  .feature-list {
    align-items: center;
  }

  .branding-visual {
    display: none;
  }
}

@media (max-width: 640px) {
  .auth-page {
    padding: var(--space-4);
  }

  .form-card {
    padding: var(--space-6);
  }

  .brand-title {
    font-size: 2rem;
  }
}
</style>
