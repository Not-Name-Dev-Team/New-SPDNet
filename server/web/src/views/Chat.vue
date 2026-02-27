<template>
  <div class="chat">
    <div class="card">
      <div class="chat-header">
        <h2 class="card-title">聊天室</h2>
        <button class="btn btn-sm" @click="loadMessages">刷新</button>
      </div>

      <div v-if="!authStore.isLoggedIn" class="alert alert-error">
        请先登录后再参与聊天
      </div>

      <div class="chat-container" ref="chatContainer">
        <div v-if="loading" class="loading">加载中...</div>
        
        <div v-else-if="messages.length === 0" class="no-messages">
          暂无消息，来发送第一条消息吧！
        </div>

        <div v-else class="messages">
          <div 
            v-for="(msg, index) in reversedMessages" 
            :key="index" 
            :class="['message', { 'own-message': msg.name === authStore.user?.name }]"
          >
            <div class="message-header">
              <router-link :to="`/player/${msg.name}`" class="message-name">
                {{ msg.name }}
              </router-link>
              <span class="message-own" v-if="msg.name === authStore.user?.name">(我)</span>
            </div>
            <div class="message-content">{{ msg.message }}</div>
          </div>
        </div>
      </div>

      <form @submit.prevent="sendMessage" class="chat-input" v-if="authStore.isLoggedIn">
        <input 
          type="text" 
          v-model="newMessage" 
          placeholder="输入消息..." 
          :disabled="sending"
          maxlength="500"
        />
        <button type="submit" class="btn" :disabled="sending || !newMessage.trim()">
          {{ sending ? '发送中...' : '发送' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { chatApi } from '../api'
import { authStore } from '../store/auth'

const messages = ref([])
const loading = ref(false)
const sending = ref(false)
const newMessage = ref('')
const chatContainer = ref(null)

const reversedMessages = computed(() => [...messages.value].reverse())

async function loadMessages() {
  loading.value = true
  try {
    const res = await chatApi.getMessages(100)
    if (res.data.success) {
      messages.value = res.data.data || []
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    loading.value = false
  }
}

async function sendMessage() {
  if (!newMessage.value.trim()) return

  sending.value = true
  try {
    const res = await chatApi.send(authStore.user.name, newMessage.value.trim())
    if (res.data.success) {
      messages.value.unshift({
        name: authStore.user.name,
        message: newMessage.value.trim()
      })
      newMessage.value = ''
      await nextTick()
      scrollToBottom()
    } else {
      alert(res.data.message)
    }
  } catch (error) {
    alert(error.response?.data?.message || '发送失败')
  } finally {
    sending.value = false
  }
}

function scrollToBottom() {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.chat {
  max-width: 800px;
  margin: 0 auto;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.chat-container {
  height: 400px;
  overflow-y: auto;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 1rem;
  margin-bottom: 1rem;
  background-color: var(--bg-color);
}

.messages {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.message {
  padding: 0.75rem;
  border-radius: 8px;
  background-color: var(--card-bg);
  max-width: 80%;
}

.own-message {
  align-self: flex-end;
  background-color: rgba(76, 175, 80, 0.1);
  border: 1px solid rgba(76, 175, 80, 0.3);
}

.message-header {
  margin-bottom: 0.25rem;
}

.message-name {
  font-weight: 600;
  color: var(--primary-color);
  text-decoration: none;
}

.message-name:hover {
  text-decoration: underline;
}

.message-own {
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin-left: 0.25rem;
}

.message-content {
  word-break: break-word;
  color: var(--text-color);
}

.no-messages {
  text-align: center;
  color: var(--text-secondary);
  padding: 2rem;
}

.chat-input {
  display: flex;
  gap: 0.5rem;
}

.chat-input input {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background-color: var(--bg-color);
  color: var(--text-color);
  font-size: 1rem;
}

.chat-input input:focus {
  outline: none;
  border-color: var(--primary-color);
}
</style>
