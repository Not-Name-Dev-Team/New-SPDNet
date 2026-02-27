<template>
  <div class="register">
    <div class="card">
      <h2 class="card-title">注册账号</h2>
      
      <div v-if="message" :class="['alert', success ? 'alert-success' : 'alert-error']">
        {{ message }}
      </div>

      <div v-if="success && !message.includes('失败')" class="alert alert-success">
        <p><strong>注册成功！</strong></p>
        <p>您可以使用用户名和密码登录游戏</p>
      </div>

      <form @submit.prevent="handleRegister" v-if="!success">
        <div class="form-group">
          <label for="name">用户名</label>
          <input 
            type="text" 
            id="name" 
            v-model="form.name" 
            placeholder="请输入用户名 (2-16字符)"
            required
            minlength="2"
            maxlength="16"
          />
        </div>

        <div class="form-group">
          <label for="email">邮箱</label>
          <input 
            type="email" 
            id="email" 
            v-model="form.email" 
            placeholder="请输入邮箱地址"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password" 
            v-model="form.password" 
            placeholder="请输入密码 (6-32字符)"
            required
            minlength="6"
            maxlength="32"
          />
        </div>

        <div class="form-group">
          <label for="confirmPassword">确认密码</label>
          <input 
            type="password" 
            id="confirmPassword" 
            v-model="form.confirmPassword" 
            placeholder="请再次输入密码"
            required
          />
        </div>

        <button type="submit" class="btn" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="links" v-if="success">
        <router-link to="/" class="btn">返回首页</router-link>
        <router-link to="/login" class="btn" style="margin-left: 1rem;">去登录</router-link>
      </div>
      
      <div class="links" v-if="!success">
        <router-link to="/login">已有账号？去登录</router-link>
      </div>
    </div>

    <div class="card">
      <h3>注册说明</h3>
      <ul>
        <li>用户名一旦注册无法更改</li>
        <li>密码长度需在6-32个字符之间</li>
        <li>每个邮箱只能注册一个账号</li>
        <li>注册后即可使用用户名和密码登录游戏</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { playerApi } from '../api'

const form = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const loading = ref(false)
const message = ref('')
const success = ref(false)

async function handleRegister() {
  if (form.password !== form.confirmPassword) {
    message.value = '两次输入的密码不一致'
    success.value = false
    return
  }

  loading.value = true
  message.value = ''

  try {
    const res = await playerApi.register({
      name: form.name,
      email: form.email,
      password: form.password
    })

    if (res.data.success) {
      success.value = true
      message.value = res.data.message
    } else {
      success.value = false
      message.value = res.data.message
    }
  } catch (error) {
    success.value = false
    message.value = error.response?.data?.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register {
  max-width: 500px;
  margin: 0 auto;
}

.links {
  margin-top: 1rem;
  text-align: center;
}

.links a {
  color: var(--primary-color);
  text-decoration: none;
}

.links a:hover {
  text-decoration: underline;
}

ul {
  list-style: none;
  padding: 0;
}

ul li {
  padding: 0.5rem 0;
  color: var(--text-secondary);
}

ul li::before {
  content: '• ';
  color: var(--primary-color);
}
</style>
