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
            创建账号，开启你的地牢冒险之旅。与全球玩家一起探索、战斗、成长！
          </p>

          <div class="info-card">
            <div class="info-header">
              <el-icon><InfoFilled /></el-icon>
              <span>注册须知</span>
            </div>
            <ul class="info-list">
              <li v-for="(item, index) in infoItems" :key="index">
                <el-icon><Check /></el-icon>
                <span>{{ item }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- Right Side - Form -->
      <div class="auth-form-wrapper">
        <div class="form-card">
          <!-- Success State -->
          <template v-if="success">
            <div class="success-state">
              <div class="success-icon">
                <el-icon :size="64"><CircleCheck /></el-icon>
              </div>
              <h2>注册成功</h2>
              <p>您可以使用用户名和密码登录游戏了</p>
              <div class="success-actions">
                <router-link to="/login" class="btn-primary">
                  <el-icon><Key /></el-icon>
                  <span>去登录</span>
                </router-link>
                <router-link to="/" class="btn-secondary">
                  <el-icon><House /></el-icon>
                  <span>返回首页</span>
                </router-link>
              </div>
            </div>
          </template>

          <!-- Form State -->
          <template v-else>
            <div class="form-header">
              <h2>创建账号</h2>
              <p>开始你的地牢冒险之旅</p>
            </div>

            <!-- Step Indicator -->
            <div class="step-indicator">
              <div
                v-for="step in 3"
                :key="step"
                :class="['step', {
                  active: currentStep >= step - 1,
                  completed: currentStep > step - 1
                }]"
              >
                <div class="step-number">{{ step }}</div>
                <div class="step-label">{{ ['填写信息', '验证邮箱', '完成注册'][step - 1] }}</div>
              </div>
              <div class="step-line">
                <div class="step-progress" :style="{ width: `${(currentStep / 2) * 100}%` }"></div>
              </div>
            </div>

            <el-alert
              v-if="message"
              :title="message"
              :type="error ? 'error' : 'success'"
              show-icon
              :closable="false"
              class="form-alert"
            />

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              @keyup.enter="handleRegister"
              class="auth-form"
            >
              <el-form-item prop="name">
                <div class="input-wrapper">
                  <el-icon class="input-icon"><User /></el-icon>
                  <el-input
                    v-model="form.name"
                    placeholder="请输入用户名 (2-16字符)"
                    size="large"
                    maxlength="16"
                    show-word-limit
                    clearable
                  />
                </div>
              </el-form-item>

              <el-form-item prop="email">
                <div class="input-wrapper">
                  <el-icon class="input-icon"><Message /></el-icon>
                  <el-input
                    v-model="form.email"
                    placeholder="请输入邮箱地址"
                    size="large"
                    clearable
                  >
                    <template #append>
                      <el-button
                        :disabled="!canSendCode || codeSending"
                        :loading="codeSending"
                        @click="sendCode"
                        class="code-btn"
                      >
                        {{ countdown > 0 ? `${countdown}s` : (codeSent ? '重新获取' : '获取验证码') }}
                      </el-button>
                    </template>
                  </el-input>
                </div>
              </el-form-item>

              <el-form-item prop="verificationCode" v-if="codeSent">
                <div class="input-wrapper">
                  <el-icon class="input-icon"><Key /></el-icon>
                  <el-input
                    v-model="form.verificationCode"
                    placeholder="请输入6位验证码"
                    size="large"
                    maxlength="6"
                    clearable
                  />
                </div>
                <div class="code-hint">
                  <el-icon><InfoFilled /></el-icon>
                  <span>验证码已发送到 {{ maskedEmail }}，5分钟内有效</span>
                </div>
              </el-form-item>

              <el-form-item prop="password">
                <div class="input-wrapper">
                  <el-icon class="input-icon"><Lock /></el-icon>
                  <el-input
                    v-model="form.password"
                    type="password"
                    placeholder="请输入密码 (6-32字符)"
                    size="large"
                    show-password
                    clearable
                  />
                </div>
              </el-form-item>

              <el-form-item prop="confirmPassword">
                <div class="input-wrapper">
                  <el-icon class="input-icon"><Lock /></el-icon>
                  <el-input
                    v-model="form.confirmPassword"
                    type="password"
                    placeholder="请再次输入密码"
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
                  @click="handleRegister"
                  :loading="loading"
                  :disabled="!codeSent"
                  class="submit-btn"
                >
                  <el-icon><CircleCheck /></el-icon>
                  <span>{{ loading ? '注册中...' : '立即注册' }}</span>
                </el-button>
              </el-form-item>
            </el-form>

            <div class="form-footer">
              <span>已有账号？</span>
              <router-link to="/login" class="link-primary">
                立即登录
                <el-icon><ArrowRight /></el-icon>
              </router-link>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User, Lock, Key, Message, ArrowRight,
  Connection, InfoFilled, Check, CircleCheck, House
} from '@element-plus/icons-vue'
import { playerApi } from '../api'

const formRef = ref()

const form = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  verificationCode: ''
})

const validatePass2 = (rule, value, callback) => {
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
    { min: 2, max: 16, message: '用户名长度在 2 到 16 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度在 6 到 32 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

const loading = ref(false)
const message = ref('')
const error = ref(false)
const success = ref(false)
const codeSending = ref(false)
const codeSent = ref(false)
const countdown = ref(0)
const currentStep = ref(0)

const infoItems = [
  '用户名一旦注册无法更改',
  '密码长度需在6-32个字符之间',
  '每个邮箱只能注册一个账号',
  '需要通过邮箱验证码验证',
  '注册后即可使用用户名和密码登录游戏'
]

const canSendCode = computed(() => {
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return emailRegex.test(form.email) && countdown.value === 0
})

const maskedEmail = computed(() => {
  if (!form.email) return ''
  const [local, domain] = form.email.split('@')
  const maskedLocal = local.slice(0, 2) + '***' + local.slice(-1)
  return maskedLocal + '@' + domain
})

let countdownTimer = null

const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 1000)
}

const sendCode = async () => {
  if (!canSendCode.value) return

  codeSending.value = true
  message.value = ''

  try {
    const res = await playerApi.sendCode({ email: form.email })

    if (res.data.success) {
      codeSent.value = true
      currentStep.value = 1
      ElMessage.success('验证码已发送')
      startCountdown()
    } else {
      error.value = true
      message.value = res.data.message
    }
  } catch (err) {
    error.value = true
    message.value = err.response?.data?.message || '验证码发送失败'
  } finally {
    codeSending.value = false
  }
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  message.value = ''

  try {
    const res = await playerApi.register({
      name: form.name,
      email: form.email,
      password: form.password,
      verificationCode: form.verificationCode
    })

    if (res.data.success) {
      success.value = true
      currentStep.value = 2
      ElMessage.success('注册成功')
    } else {
      error.value = true
      message.value = res.data.message
      if (res.data.message?.includes('验证码')) {
        form.verificationCode = ''
      }
    }
  } catch (err) {
    error.value = true
    message.value = err.response?.data?.message || '注册失败'
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
  align-items: start;
}

/* Branding Side */
.auth-branding {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
  padding: var(--space-8);
  position: sticky;
  top: calc(var(--header-height) + var(--space-8));
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
}

.info-card {
  background: var(--surface-2);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
}

.info-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.info-header .el-icon {
  color: var(--primary-400);
}

.info-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.info-list li {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  font-size: 0.9375rem;
}

.info-list li .el-icon {
  color: var(--accent-emerald);
  flex-shrink: 0;
}

/* Form Side */
.auth-form-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-card {
  width: 100%;
  max-width: 480px;
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

/* Step Indicator */
.step-indicator {
  display: flex;
  justify-content: space-between;
  position: relative;
  margin-bottom: var(--space-6);
  padding: 0 var(--space-4);
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  position: relative;
  z-index: 1;
}

.step-number {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background: var(--surface-2);
  border: 2px solid var(--border-default);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: var(--text-tertiary);
  transition: all var(--transition-base);
}

.step.active .step-number {
  background: var(--gradient-primary);
  border-color: transparent;
  color: white;
}

.step.completed .step-number {
  background: var(--accent-emerald);
  border-color: transparent;
  color: white;
}

.step-label {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  font-weight: 500;
}

.step.active .step-label {
  color: var(--primary-400);
}

.step-line {
  position: absolute;
  top: 18px;
  left: 60px;
  right: 60px;
  height: 2px;
  background: var(--border-subtle);
  z-index: 0;
}

.step-progress {
  height: 100%;
  background: var(--gradient-primary);
  transition: width var(--transition-base);
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

.code-btn {
  min-width: 100px;
}

.code-hint {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-top: var(--space-2);
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.code-hint .el-icon {
  color: var(--primary-400);
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

/* Success State */
.success-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: var(--space-8) 0;
}

.success-icon {
  width: 100px;
  height: 100px;
  border-radius: var(--radius-full);
  background: rgba(16, 185, 129, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-emerald);
  margin-bottom: var(--space-6);
  animation: scaleIn 0.5s ease;
}

.success-state h2 {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 var(--space-2);
  color: var(--text-primary);
}

.success-state p {
  color: var(--text-secondary);
  margin: 0 0 var(--space-8);
}

.success-actions {
  display: flex;
  gap: var(--space-3);
}

.success-actions .btn-primary,
.success-actions .btn-secondary {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-6);
  border-radius: var(--radius-md);
  font-weight: 600;
  text-decoration: none;
  transition: all var(--transition-fast);
}

.success-actions .btn-primary {
  background: var(--gradient-primary);
  color: white;
}

.success-actions .btn-secondary {
  background: var(--surface-2);
  border: 1px solid var(--border-default);
  color: var(--text-primary);
}

/* Animations */
@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* Responsive */
@media (max-width: 1024px) {
  .auth-container {
    grid-template-columns: 1fr;
    gap: var(--space-8);
  }

  .auth-branding {
    position: static;
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

  .info-card {
    max-width: 400px;
    margin: 0 auto;
  }

  .info-list {
    text-align: left;
  }
}

@media (max-width: 640px) {
  .auth-page {
    padding: var(--space-4);
  }

  .form-card {
    padding: var(--space-5);
  }

  .brand-title {
    font-size: 2rem;
  }

  .step-label {
    display: none;
  }

  .success-actions {
    flex-direction: column;
    width: 100%;
  }

  .success-actions .btn-primary,
  .success-actions .btn-secondary {
    justify-content: center;
  }
}
</style>
