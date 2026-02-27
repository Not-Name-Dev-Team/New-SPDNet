<template>
  <div class="login">
    <div class="card">
      <h2 class="card-title">登录账号</h2>
      
      <div v-if="message" :class="['alert', success ? 'alert-success' : 'alert-error']">
        {{ message }}
      </div>

      <div v-if="loginData" class="alert alert-success">
        <p><strong>登录成功！</strong></p>
        <p>用户名: {{ loginData.name }}</p>
        <p>身份: {{ loginData.role }}</p>
      </div>

      <form @submit.prevent="handleLogin" v-if="!loginData">
        <div class="form-group">
          <label for="name">用户名</label>
          <input 
            type="text" 
            id="name" 
            v-model="form.name" 
            placeholder="请输入用户名"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password" 
            v-model="form.password" 
            placeholder="请输入密码"
            required
          />
        </div>

        <button type="submit" class="btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="links" v-if="loginData">
        <router-link to="/" class="btn">返回首页</router-link>
      </div>
      
      <div class="links" v-if="!loginData">
        <router-link to="/register">没有账号？去注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { playerApi } from '../api'

const form = reactive({
  name: '',
  password: ''
})

const loading = ref(false)
const message = ref('')
const success = ref(false)
const loginData = ref(null)

async function handleLogin() {
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
      loginData.value = res.data.data
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
.login {
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
</style>
