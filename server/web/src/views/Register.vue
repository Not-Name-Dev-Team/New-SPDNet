<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-left">
        <div class="brand-section">
          <el-icon :size="48" class="brand-icon"><Connection /></el-icon>
          <h1 class="brand-title">
            <span class="gradient-text">SPD</span>Net
          </h1>
          <p class="brand-subtitle">联机破碎地牢</p>
        </div>
        <div class="info-card">
          <h3><el-icon><InfoFilled /></el-icon> 注册说明</h3>
          <ul>
            <li><el-icon><Check /></el-icon> 用户名一旦注册无法更改</li>
            <li><el-icon><Check /></el-icon> 密码长度需在6-32个字符之间</li>
            <li><el-icon><Check /></el-icon> 每个邮箱只能注册一个账号</li>
            <li><el-icon><Check /></el-icon> 需要通过邮箱验证码验证</li>
            <li><el-icon><Check /></el-icon> 注册后即可使用用户名和密码登录游戏</li>
          </ul>
        </div>
      </div>

      <div class="register-right">
        <el-card class="register-card" shadow="hover">
          <template #header>
            <div class="register-header">
              <h2>创建账号</h2>
              <p>开始你的地牢冒险之旅</p>
            </div>
          </template>

          <el-result
            v-if="success"
            icon="success"
            title="注册成功"
            sub-title="您可以使用用户名和密码登录游戏"
            class="success-result"
          >
            <template #extra>
              <el-button type="primary" @click="$router.push('/login')" size="large">
                去登录
              </el-button>
              <el-button @click="$router.push('/')" size="large">
                返回首页
              </el-button>
            </template>
          </el-result>

          <template v-else>
            <el-alert
              v-if="message"
              :title="message"
              :type="error ? 'error' : 'success'"
              show-icon
              :closable="false"
              class="register-alert"
            />

            <el-steps :active="currentStep" finish-status="success" class="register-steps">
              <el-step title="填写信息" />
              <el-step title="验证邮箱" />
              <el-step title="完成注册" />
            </el-steps>

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              @keyup.enter="handleRegister"
              class="register-form"
            >
              <el-form-item prop="name">
                <el-input
                  v-model="form.name"
                  placeholder="请输入用户名 (2-16字符)"
                  size="large"
                  :prefix-icon="User"
                  maxlength="16"
                  show-word-limit
                  clearable
                />
              </el-form-item>

              <el-form-item prop="email">
                <el-input
                  v-model="form.email"
                  placeholder="请输入邮箱地址"
                  size="large"
                  :prefix-icon="Message"
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
              </el-form-item>

              <el-form-item prop="verificationCode" v-if="codeSent">
                <el-input
                  v-model="form.verificationCode"
                  placeholder="请输入6位验证码"
                  size="large"
                  :prefix-icon="Key"
                  maxlength="6"
                  clearable
                />
                <div class="code-hint">
                  <el-icon><InfoFilled /></el-icon>
                  验证码已发送到 {{ maskedEmail }}，5分钟内有效
                </div>
              </el-form-item>

              <el-form-item prop="password">
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="请输入密码 (6-32字符)"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                  clearable
                />
              </el-form-item>

              <el-form-item prop="confirmPassword">
                <el-input
                  v-model="form.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
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
                  @click="handleRegister"
                  :loading="loading"
                  :disabled="!codeSent"
                  class="register-btn glow-btn"
                >
                  <el-icon><CircleCheck /></el-icon>
                  {{ loading ? '注册中...' : '立即注册' }}
                </el-button>
              </el-form-item>
            </el-form>

            <div class="register-footer">
              <span>已有账号？</span>
              <router-link to="/login" class="login-link">
                立即登录
                <el-icon><ArrowRight /></el-icon>
              </router-link>
            </div>
          </template>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  User, Lock, Key, Message, ArrowRight, 
  Connection, InfoFilled, Check, CircleCheck 
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
.register-page {
  min-height: calc(100vh - 64px - 100px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.register-container {
  display: flex;
  max-width: 1000px;
  width: 100%;
  gap: 4rem;
  align-items: flex-start;
}

.register-left {
  flex: 1;
  padding: 2rem;
  position: sticky;
  top: 100px;
}

.brand-section {
  margin-bottom: 2rem;
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

.info-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 1.5rem;
}

.info-card h3 {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.125rem;
  margin-bottom: 1rem;
  color: var(--text-primary);
}

.info-card h3 .el-icon {
  color: var(--primary-color);
}

.info-card ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.info-card li {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0;
  color: var(--text-secondary);
}

.info-card li .el-icon {
  color: var(--success-color);
}

.register-right {
  flex: 1;
  max-width: 480px;
}

.register-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
}

:deep(.register-card .el-card__header) {
  border-bottom: 1px solid var(--border-color);
  padding: 2rem 2rem 1.5rem;
}

:deep(.register-card .el-card__body) {
  padding: 2rem;
}

.register-header {
  text-align: center;
}

.register-header h2 {
  font-size: 1.75rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.register-header p {
  color: var(--text-secondary);
}

.success-result {
  padding: 2rem 0;
}

.register-alert {
  margin-bottom: 1.5rem;
}

.register-steps {
  margin-bottom: 2rem;
}

:deep(.register-steps .el-step__title) {
  font-size: 0.875rem;
}

.register-form {
  margin-bottom: 1.5rem;
}

:deep(.register-form .el-input__wrapper) {
  background: var(--bg-dark);
  box-shadow: 0 0 0 1px var(--border-color) inset;
  padding: 4px 11px;
}

:deep(.register-form .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

:deep(.register-form .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

.code-btn {
  min-width: 100px;
}

.code-hint {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.code-hint .el-icon {
  color: var(--primary-color);
}

.register-btn {
  width: 100%;
  font-size: 1rem;
  height: 44px;
}

.register-footer {
  text-align: center;
  color: var(--text-secondary);
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.login-link {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  margin-left: 0.5rem;
  transition: all 0.3s;
}

.login-link:hover {
  color: var(--primary-light);
  transform: translateX(4px);
}

@media (max-width: 768px) {
  .register-container {
    flex-direction: column;
    gap: 2rem;
  }
  
  .register-left {
    text-align: center;
    padding: 1rem;
    position: static;
  }
  
  .info-card {
    display: none;
  }
  
  .register-right {
    width: 100%;
  }
  
  :deep(.register-steps) {
    display: none;
  }
}
</style>
