<template>
  <div class="send-key">
    <div class="card">
      <h2 class="card-title">查询Key</h2>

      <div v-if="message" :class="['alert', success ? 'alert-success' : 'alert-error']">
        {{ message }}
      </div>

      <form @submit.prevent="handleSendKey">
        <div class="form-group">
          <label for="email">注册邮箱</label>
          <input 
            type="email" 
            id="email" 
            v-model="email" 
            placeholder="请输入注册时使用的邮箱"
            required
          />
        </div>

        <button type="submit" class="btn" :disabled="loading">
          {{ loading ? '发送中...' : '发送Key到邮箱' }}
        </button>
      </form>
    </div>

    <div class="card">
      <h3>说明</h3>
      <ul>
        <li>输入您注册时使用的邮箱地址</li>
        <li>Key将会发送到您的邮箱</li>
        <li>如果邮箱未注册，将无法收到Key</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { playerApi } from '../api'

const email = ref('')
const loading = ref(false)
const message = ref('')
const success = ref(false)

async function handleSendKey() {
  loading.value = true
  message.value = ''

  try {
    const res = await playerApi.sendKey({ email: email.value })

    if (res.data.success) {
      success.value = true
      message.value = res.data.message
    } else {
      success.value = false
      message.value = res.data.message
    }
  } catch (error) {
    success.value = false
    message.value = error.response?.data?.message || '发送失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.send-key {
  max-width: 500px;
  margin: 0 auto;
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
