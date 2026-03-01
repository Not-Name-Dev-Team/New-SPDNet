<template>
  <div class="chat-page">
    <div class="chat-wrapper">
      <!-- Sidebar - Online Users -->
      <aside class="chat-sidebar">
        <div class="sidebar-header">
          <div class="header-title">
            <el-icon><UserFilled /></el-icon>
            <span>在线玩家</span>
          </div>
          <el-tag type="success" effect="dark" round size="small">
            {{ onlineUsers.length }}
          </el-tag>
        </div>

        <div class="users-list" v-if="onlineUsers.length > 0">
          <router-link
            v-for="user in onlineUsers"
            :key="user.name"
            :to="`/player/${user.name}`"
            class="user-item"
          >
            <div class="user-avatar-wrapper">
              <el-avatar :size="36" :icon="UserFilled" class="user-avatar" />
              <span class="online-dot"></span>
            </div>
            <div class="user-info">
              <span class="user-name">{{ user.name }}</span>
              <el-tag :type="getRoleType(user.role)" size="small" effect="dark" round>
                {{ user.role }}
              </el-tag>
            </div>
          </router-link>
        </div>

        <div v-else class="empty-users">
          <el-icon :size="32"><User /></el-icon>
          <span>暂无在线玩家</span>
        </div>
      </aside>

      <!-- Main Chat Area -->
      <main class="chat-main">
        <!-- Header -->
        <header class="chat-header">
          <div class="header-info">
            <div class="header-icon-wrapper">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </div>
            <div class="header-text">
              <h2>公共聊天室</h2>
              <p>与在线玩家实时交流</p>
            </div>
          </div>
          <div class="header-actions">
            <el-button
              type="primary"
              text
              :icon="Refresh"
              @click="loadOnlineUsers"
              :loading="loading"
            >
              刷新
            </el-button>
          </div>
        </header>

        <!-- Messages Area - 使用独立滚动区域 -->
        <div class="messages-wrapper" ref="messagesWrapper">
          <div v-if="messages.length === 0" class="empty-messages">
            <div class="empty-icon">
              <el-icon :size="48"><ChatDotRound /></el-icon>
            </div>
            <p>暂无消息</p>
            <span>发送第一条消息开始聊天吧！</span>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-item"
            :class="{ 'self': isSelfMessage(msg) }"
          >
            <router-link :to="`/player/${msg.name}`" class="message-avatar">
              <el-avatar :size="40" :icon="UserFilled" />
            </router-link>
            <div class="message-content">
              <div class="message-header">
                <router-link :to="`/player/${msg.name}`" class="message-author">
                  {{ msg.name }}
                </router-link>
                <span class="message-time">{{ formatTime(msg.time) }}</span>
              </div>
              <div class="message-bubble">
                <p>{{ msg.message }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Input Area - 固定在底部 -->
        <footer class="chat-footer">
          <div class="input-wrapper">
            <el-input
              v-model="messageText"
              type="textarea"
              :rows="2"
              placeholder="输入消息..."
              resize="none"
              @keyup.enter.prevent="sendMessage"
              :disabled="!authStore.isLoggedIn"
            />
            <el-button
              type="primary"
              :icon="Promotion"
              :disabled="!canSend"
              :loading="sending"
              @click="sendMessage"
              class="send-btn"
            >
              发送
            </el-button>
          </div>
          <div v-if="!authStore.isLoggedIn" class="login-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>请先 <router-link to="/login">登录</router-link> 后发送消息</span>
          </div>
        </footer>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  UserFilled, User, ChatDotRound, Refresh, Promotion, InfoFilled
} from '@element-plus/icons-vue'
import { playerApi, chatApi } from '../api'
import { authStore } from '../store/auth'

const onlineUsers = ref([])
const messages = ref([])
const messageText = ref('')
const loading = ref(false)
const sending = ref(false)
const messagesWrapper = ref(null)
let refreshInterval = null

const canSend = computed(() => {
  return authStore.isLoggedIn && messageText.value.trim() && !sending.value
})

const getRoleType = (role) => {
  const types = {
    '管理员': 'danger',
    '玩家': 'primary'
  }
  return types[role] || 'primary'
}

const isSelfMessage = (msg) => {
  return msg.name === authStore.user?.name
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  if (isNaN(date.getTime())) {
    return typeof time === 'string' ? time.split('T')[1]?.substring(0, 5) || '' : ''
  }
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesWrapper.value) {
      messagesWrapper.value.scrollTop = messagesWrapper.value.scrollHeight
    }
  })
}

const loadOnlineUsers = async () => {
  loading.value = true
  try {
    const res = await playerApi.getOnline()
    onlineUsers.value = res.data.data || []
  } catch (error) {
    console.error('获取在线用户失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMessages = async () => {
  try {
    const res = await chatApi.getMessages(50)
    if (res.data.success) {
      const newMessages = res.data.data || []
      const reversedMessages = [...newMessages].reverse()
      if (JSON.stringify(reversedMessages) !== JSON.stringify(messages.value)) {
        messages.value = reversedMessages
        scrollToBottom()
      }
    }
  } catch (error) {
    console.error('获取聊天记录失败:', error)
  }
}

const sendMessage = async () => {
  if (!canSend.value) return

  const content = messageText.value.trim()
  if (!content) return

  sending.value = true
  try {
    const res = await chatApi.send(authStore.user?.name, content)
    if (res.data.success) {
      messageText.value = ''
      await loadMessages()
    } else {
      ElMessage.error(res.data.message || '发送失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败，请检查网络连接')
  } finally {
    sending.value = false
  }
}

watch(messages, () => {
  scrollToBottom()
}, { deep: true })

onMounted(() => {
  loadOnlineUsers()
  loadMessages()

  refreshInterval = setInterval(() => {
    loadOnlineUsers()
    loadMessages()
  }, 5000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
/* 外层容器 - 正常文档流布局 */
.chat-page {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-6) var(--content-padding);
  min-height: calc(100vh - var(--header-height) - 200px); /* 确保最小高度 */
}

/* 聊天包装器 */
.chat-wrapper {
  display: flex;
  height: calc(100vh - var(--header-height) - var(--space-12) - 200px); /* 视口高度减去header、padding和footer空间 */
  min-height: 500px;
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

/* Sidebar */
.chat-sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: var(--surface-2);
  border-right: 1px solid var(--border-subtle);
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: 600;
  color: var(--text-primary);
}

.users-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-2);
}

.user-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2);
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.user-item:hover {
  background: var(--surface-3);
}

.user-avatar-wrapper {
  position: relative;
}

.user-avatar {
  background: var(--gradient-primary);
}

.online-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  background: var(--accent-emerald);
  border: 2px solid var(--surface-2);
  box-shadow: 0 0 6px var(--accent-emerald);
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  display: block;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-users {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-8);
  color: var(--text-tertiary);
}

/* Main Chat Area */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--surface-1);
}

/* Header */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
  background: var(--surface-1);
}

.header-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.header-icon-wrapper {
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
  color: var(--text-secondary);
  margin: 2px 0 0;
  font-size: 0.8125rem;
}

/* Messages Wrapper - 独立滚动区域 */
.messages-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  min-height: 0;
}

.empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-8);
  color: var(--text-tertiary);
}

.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-xl);
  background: var(--surface-2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-messages p {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.message-item {
  display: flex;
  gap: var(--space-3);
  animation: fadeInUp 0.3s ease-out;
}

.message-item.self {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-avatar :deep(.el-avatar) {
  background: var(--gradient-primary);
}

.message-content {
  max-width: 70%;
}

.message-item.self .message-content {
  text-align: right;
}

.message-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.message-item.self .message-header {
  justify-content: flex-end;
}

.message-author {
  font-weight: 500;
  color: var(--primary-400);
  text-decoration: none;
  font-size: 0.875rem;
}

.message-author:hover {
  text-decoration: underline;
}

.message-time {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.message-bubble {
  display: inline-block;
  padding: var(--space-3) var(--space-4);
  background: var(--surface-2);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-lg);
  border-top-left-radius: 4px;
  text-align: left;
}

.message-item.self .message-bubble {
  background: linear-gradient(135deg, rgba(147, 51, 234, 0.15) 0%, rgba(147, 51, 234, 0.08) 100%);
  border-color: rgba(147, 51, 234, 0.25);
  border-top-left-radius: var(--radius-lg);
  border-top-right-radius: 4px;
}

.message-bubble p {
  margin: 0;
  color: var(--text-primary);
  line-height: 1.5;
  word-break: break-word;
}

/* Footer - 固定在底部 */
.chat-footer {
  padding: var(--space-4);
  border-top: 1px solid var(--border-subtle);
  background: var(--surface-2);
  flex-shrink: 0;
}

.input-wrapper {
  display: flex;
  gap: var(--space-3);
}

.input-wrapper :deep(.el-textarea__inner) {
  background: var(--surface-1);
  border-color: var(--border-default);
  color: var(--text-primary);
  resize: none;
}

.input-wrapper :deep(.el-textarea__inner:focus) {
  border-color: var(--primary-500);
}

.send-btn {
  align-self: flex-end;
  height: 54px;
  padding: 0 var(--space-5);
}

.login-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  margin-top: var(--space-3);
  padding: var(--space-3);
  background: rgba(245, 158, 11, 0.08);
  border: 1px solid rgba(245, 158, 11, 0.15);
  border-radius: var(--radius-md);
  color: var(--accent-amber);
  font-size: 0.875rem;
}

.login-tip a {
  color: var(--primary-400);
  text-decoration: none;
  font-weight: 500;
}

.login-tip a:hover {
  text-decoration: underline;
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
@media (max-width: 900px) {
  .chat-sidebar {
    display: none;
  }
}

@media (max-width: 640px) {
  .chat-page {
    top: 56px;
    padding: var(--space-2);
  }

  .chat-wrapper {
    border-radius: var(--radius-lg);
  }

  .message-content {
    max-width: 85%;
  }

  .input-wrapper {
    flex-direction: column;
  }

  .send-btn {
    width: 100%;
    height: 40px;
  }
}
</style>
