<template>
  <div class="register">
    <div class="card">
      <h2 class="card-title">注册账号</h2>
      
      <div v-if="message" :class="['alert', success ? 'alert-success' : 'alert-error']">
        {{ message }}
      </div>

      <div v-if="registeredKey" class="alert alert-success">
        <p><strong>注册成功！</strong></p>
        <p>您的Key: <code>{{ registeredKey }}</code></p>
        <p>请妥善保管，Key已发送到您的邮箱</p>
      </div>

      <form @submit.prevent="handleRegister" v-if="!registeredKey">
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
          <label for="email">邮箱</label>
          <input 
            type="email" 
            id="email" 
            v-model="form.email" 
            placeholder="请输入邮箱地址"
            required
          />
        </div>

        <button type="submit" class="btn" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="links" v-if="registeredKey">
        <router-link to="/" class="btn">返回首页</router-link>
      </div>
    </div>

    <div class="card">
      <h3>注册说明</h3>
      <ul>
        <li>用户名一旦注册无法更改</li>
        <li>Key是您登录游戏的唯一凭证，请妥善保管</li>
        <li>如果忘记Key，可以通过邮箱找回</li>
        <li>每个邮箱只能注册一个账号</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { playerApi } from '../api'

const form = reactive({
  name: '',
  email: ''
})

const loading = ref(false)
const message = ref('')
const success = ref(false)
const registeredKey = ref('')

async function handleRegister() {
  loading.value = true
  message.value = ''

  try {
    const res = await playerApi.register({
      name: form.name,
      email: form.email
    })

    if (res.data.success) {
      success.value = true
      message.value = res.data.message
      registeredKey.value = res.data.data?.key || ''
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

code {
  background-color: var(--bg-color);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-family: monospace;
}
</style>
