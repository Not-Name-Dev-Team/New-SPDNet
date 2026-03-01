<template>
  <div class="auth-page">
    <div class="auth-container">
      <!-- Left Side - Branding -->
      <div class="auth-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <div class="logo-glow"></div>
            <el-icon :size="48" class="logo-icon"><Connection /></el-icon>
          </div>
          <h1 class="brand-title">
            <span class="gradient-text">SPD</span>Net
          </h1>
          <p class="brand-subtitle">联机破碎地牢</p>
          <p class="brand-desc">与全球玩家一起探索地牢，挑战极限，创造属于你的传奇！</p>

          <div class="brand-features">
            <div class="feature-item">
              <div class="feature-dot" style="background: var(--accent-emerald);"></div>
              <span>实时联机对战</span>
            </div>
            <div class="feature-item">
              <div class="feature-dot" style="background: var(--accent-cyan);"></div>
              <span>全球排行榜</span>
            </div>
            <div class="feature-item">
              <div class="feature-dot" style="background: var(--accent-pink);"></div>
              <span>社区交流</span>
            </div>
          </div>
        </div>

        <div class="brand-decoration">
          <div class="decoration-ring ring-1"></div>
          <div class="decoration-ring ring-2"></div>
          <div class="decoration-ring ring-3"></div>
        </div>
      </div>

      <!-- Right Side - Form -->
      <div class="auth-form-wrapper">
        <div class="form-container">
          <div class="form-header">
            <h2>创建账号</h2>
            <p>加入 SPDNet 开启你的冒险之旅</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="auth-form"
            @keyup.enter="handleRegister"
          >
            <el-form-item prop="name">
              <div class="input-wrapper">
                <el-icon class="input-icon"><User /></el-icon>
                <el-input
                  v-model="form.name"
                  placeholder="请输入用户名"
                  size="large"
                  class="auth-input"
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
                  class="auth-input"
                />
              </div>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <div class="input-wrapper">
                <el-icon class="input-icon"><Lock /></el-icon>
                <el-input
                  v-model="form.confirmPassword"
                  type="password"
                  placeholder="请确认密码"
                  size="large"
                  show-password
                  class="auth-input"
                />
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="loading"
                @click="handleRegister"
              >
                <el-icon v-if="!loading"><Right /></el-icon>
                <span>注册</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <div class="divider">
              <span>已有账号？</span>
            </div>
            <router-link to="/login" class="login-link">
              <el-icon><ArrowLeft /></el-icon>
              <span>立即登录</span>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User, Lock, Right, ArrowLeft, Connection
} from '@element-plus/icons-vue'
import { playerApi } from '../api'
import { authStore } from '../store/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  name: '',
  password: '',
  confirmPassword: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  name: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await playerApi.register({
          name: form.name,
          password: form.password
        })
        if (res.data.success) {
          ElMessage.success('注册成功')
          router.push('/login')
        } else {
          ElMessage.error(res.data.message || '注册失败')
        }
      } catch (error) {
        console.error('注册失败:', error)
        ElMessage.error('注册失败，请检查网络连接')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  if (authStore.isLoggedIn) {
    router.push('/')
  }
})
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
}

.auth-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  width: 100%;
  max-width: 1000px;
  min-height: 600px;
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
}

/* Brand Side */
.auth-brand {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: var(--space-8);
  background: linear-gradient(135deg, rgba(147, 51, 234, 0.1) 0%, rgba(6, 182, 212, 0.05) 100%);
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
}

.brand-logo {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: var(--radius-xl);
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-5);
}

.logo-glow {
  position: absolute;
  inset: -4px;
  border-radius: var(--radius-xl);
  background: var(--gradient-primary);
  filter: blur(16px);
  opacity: 0.4;
  animation: glow-pulse 2s ease-in-out infinite;
}

.logo-icon {
  color: white;
  position: relative;
  z-index: 1;
}

.brand-title {
  font-size: 2.5rem;
  font-weight: 800;
  margin: 0 0 var(--space-1);
  letter-spacing: -0.02em;
}

.brand-subtitle {
  font-size: 1.125rem;
  color: var(--text-secondary);
  margin: 0 0 var(--space-4);
}

.brand-desc {
  font-size: 0.9375rem;
  color: var(--text-tertiary);
  line-height: 1.7;
  margin: 0 0 var(--space-6);
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  box-shadow: 0 0 8px currentColor;
}

/* Decoration */
.brand-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.decoration-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid var(--border-default);
}

.ring-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
  opacity: 0.3;
  animation: rotate 30s linear infinite;
}

.ring-2 {
  width: 200px;
  height: 200px;
  bottom: 50px;
  left: -50px;
  opacity: 0.2;
  animation: rotate 20s linear infinite reverse;
}

.ring-3 {
  width: 150px;
  height: 150px;
  bottom: -30px;
  right: 50px;
  opacity: 0.15;
  animation: rotate 15s linear infinite;
}

/* Form Side */
.auth-form-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
}

.form-container {
  width: 100%;
  max-width: 360px;
}

.form-header {
  text-align: center;
  margin-bottom: var(--space-6);
}

.form-header h2 {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 var(--space-1);
}

.form-header p {
  color: var(--text-secondary);
  margin: 0;
  font-size: 0.9375rem;
}

/* Form Elements */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  z-index: 1;
  transition: color var(--transition-fast);
}

:deep(.auth-input .el-input__wrapper) {
  padding-left: 40px;
}

:deep(.auth-input .el-input__inner) {
  height: 48px;
}

.input-wrapper:focus-within .input-icon {
  color: var(--primary-400);
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow-sm);
}

/* Form Footer */
.form-footer {
  margin-top: var(--space-6);
}

.divider {
  position: relative;
  text-align: center;
  margin-bottom: var(--space-4);
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: var(--border-subtle);
}

.divider span {
  position: relative;
  background: var(--surface-1);
  padding: 0 var(--space-3);
  color: var(--text-tertiary);
  font-size: 0.8125rem;
}

.login-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3);
  background: var(--surface-2);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-weight: 500;
  text-decoration: none;
  transition: all var(--transition-fast);
}

.login-link:hover {
  background: var(--surface-3);
  border-color: var(--border-strong);
  color: var(--primary-400);
}

/* Animations */
@keyframes glow-pulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.05);
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* Responsive */
@media (max-width: 900px) {
  .auth-container {
    grid-template-columns: 1fr;
    max-width: 480px;
  }

  .auth-brand {
    display: none;
  }

  .auth-form-wrapper {
    padding: var(--space-6);
  }
}

@media (max-width: 480px) {
  .auth-page {
    padding: var(--space-4);
  }

  .auth-container {
    border-radius: var(--radius-lg);
  }

  .auth-form-wrapper {
    padding: var(--space-4);
  }

  .form-header h2 {
    font-size: 1.5rem;
  }
}
</style>
