<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- Chat Header -->
      <div class="chat-header">
        <div class="header-info">
          <div class="header-icon">
            <el-icon :size="24"><ChatDotRound /></el-icon>
          </div>
          <div class="header-text">
            <h2>聊天室</h2>
            <p v-if="authStore.isLoggedIn">
              <span class="status-dot online"></span>
              在线
            </p>
          </div>
        </div>
        <el-button
          type="primary"
          text
          :icon="Refresh"
          @click="loadMessages"
          :loading="loading"
          class="refresh-btn"
        >
          刷新
        </el-button>
      </div>

      <!-- Messages Area -->
      <div class="messages-area" ref="chatContainer" v-loading="loading">
        <div v-if="!loading && messages.length === 0" class="empty-state">
          <div class="empty-icon">
            <el-icon :size="48"><ChatLineRound /></el-icon>
          </div>
          <p>暂无消息</p>
          <span>来发送第一条消息吧！</span>
        </div>

        <div v-else class="messages-list">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-item', { 'message-own': msg.name === authStore.user?.name }]"
          >
            <div class="message-avatar-wrapper">
              <el-avatar
                :size="40"
                :icon="UserFilled"
                class="message-avatar"
                :class="{ 'own-avatar': msg.name === authStore.user?.name }"
              />
            </div>
            <div class="message-content">
              <div class="message-header">
                <router-link :to="`/player/${msg.name}`" class="message-author">
                  {{ msg.name }}
                </router-link>
                <el-tag
                  v-if="msg.name === authStore.user?.name"
                  size="small"
                  type="success"
                  effect="light"
                  round
                >
                  我
                </el-tag>
              </div>
              <div class="message-bubble">
                {{ msg.message }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="input-area">
        <div v-if="!authStore.isLoggedIn" class="login-prompt">
          <el-icon><Warning /></el-icon>
          <span>请先登录后再参与聊天</span>
          <router-link to="/login" class="btn-primary btn-sm">去登录</router-link>
        </div>
        <div v-else class="input-wrapper">
          <el-input
            v-model="newMessage"
            type="textarea"
            :rows="2"
            placeholder="输入消息..."
            maxlength="500"
            show-word-limit
            resize="none"
            @keyup.enter.prevent="sendMessage"
            class="message-input"
          />
          <el-button
            type="primary"
            size="large"
            :icon="Promotion"
            :loading="sending"
            :disabled="!newMessage.trim()"
            @click="sendMessage"
            class="send-btn"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Refresh, ChatLineRound, UserFilled, Promotion, Warning } from '@element-plus/icons-vue'
import { chatApi } from '../api'
import { authStore } from '../store/auth'

const messages = ref([])
const loading = ref(false)
const sending = ref(false)
const newMessage = ref('')
const chatContainer = ref(null)

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await chatApi.getMessages(100)
    if (res.data.success) {
      messages.value = res.data.data || []
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim()) return

  sending.value = true
  try {
    const res = await chatApi.send(authStore.user.name, newMessage.value.trim())
    if (res.data.success) {
      messages.value.push({
        name: authStore.user.name,
        message: newMessage.value.trim()
      })
      newMessage.value = ''
      await nextTick()
      scrollToBottom()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发送失败')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.chat-page {
  max-width: 900px;
  margin: 0 auto;
  padding: var(--space-8) var(--content-padding);
  height: calc(100vh - var(--header-height) - var(--space-16));
  min-height: 600px;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

/* Chat Header */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5);
  border-bottom: 1px solid var(--border-subtle);
  background: var(--surface-2);
}

.header-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.header-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.header-text h2 {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.header-text p {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin: var(--space-1) 0 0;
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--accent-emerald);
  box-shadow: 0 0 8px var(--accent-emerald);
}

.refresh-btn {
  font-weight: 500;
}

/* Messages Area */
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-5);
  background: var(--bg-primary);
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.message-item {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
  animation: fadeInUp 0.3s ease;
}

.message-item.message-own {
  flex-direction: row-reverse;
}

.message-avatar-wrapper {
  flex-shrink: 0;
}

.message-avatar {
  background: var(--gradient-primary);
}

.message-avatar.own-avatar {
  background: var(--gradient-success);
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.message-own .message-content {
  align-items: flex-end;
}

.message-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.message-author {
  color: var(--primary-400);
  text-decoration: none;
  font-weight: 500;
  font-size: 0.875rem;
  transition: color var(--transition-fast);
}

.message-author:hover {
  color: var(--primary-300);
  text-decoration: underline;
}

.message-bubble {
  background: var(--surface-2);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: var(--space-3) var(--space-4);
  color: var(--text-primary);
  word-break: break-word;
  line-height: 1.5;
}

.message-own .message-bubble {
  background: var(--gradient-primary);
  border-color: transparent;
  color: white;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12);
  text-align: center;
}

.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-xl);
  background: var(--surface-2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
}

.empty-state p {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.empty-state span {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* Input Area */
.input-area {
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--border-subtle);
  background: var(--surface-2);
}

.login-prompt {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.2);
  border-radius: var(--radius-lg);
  color: var(--accent-amber);
}

.login-prompt .el-icon {
  font-size: 1.25rem;
}

.btn-sm {
  padding: var(--space-2) var(--space-4);
  font-size: 0.875rem;
  text-decoration: none;
  border-radius: var(--radius-md);
}

.input-wrapper {
  display: flex;
  gap: var(--space-3);
  align-items: flex-end;
}

.message-input {
  flex: 1;
}

:deep(.message-input .el-textarea__inner) {
  background: var(--surface-1);
  border-color: var(--border-subtle);
  color: var(--text-primary);
  border-radius: var(--radius-lg);
  padding: var(--space-3);
}

:deep(.message-input .el-textarea__inner:focus) {
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.send-btn {
  height: 52px;
  padding: 0 var(--space-6);
  font-weight: 600;
}

/* Animations */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .chat-page {
    padding: var(--space-4);
    height: calc(100vh - var(--header-height) - var(--space-8));
  }

  .message-content {
    max-width: 80%;
  }

  .input-wrapper {
    flex-direction: column;
  }

  .send-btn {
    width: 100%;
    height: 44px;
  }
}
</style>
