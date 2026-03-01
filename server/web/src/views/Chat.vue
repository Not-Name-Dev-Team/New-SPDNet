<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- Chat Header -->
      <div class="chat-header">
        <div class="header-main">
          <div class="header-icon">
            <el-icon :size="28"><ChatDotRound /></el-icon>
            <div class="icon-glow"></div>
          </div>
          <div class="header-text">
            <h2>聊天室</h2>
            <div class="header-meta">
              <span class="status-indicator">
                <span class="status-dot"></span>
                <span class="status-text">{{ onlineCount }} 人在线</span>
              </span>
              <span class="divider">|</span>
              <span class="message-count">{{ messages.length }} 条消息</span>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <el-tooltip content="自动刷新" placement="bottom">
            <el-switch
              v-model="autoRefresh"
              active-text="自动"
              class="auto-refresh-switch"
            />
          </el-tooltip>
          <el-button
            type="primary"
            :icon="Refresh"
            @click="loadMessages"
            :loading="loading"
            circle
            class="refresh-btn"
          />
        </div>
      </div>

      <!-- Messages Area -->
      <div class="messages-area" ref="chatContainer" v-loading="loading">
        <!-- Welcome Message -->
        <div class="welcome-message" v-if="!loading">
          <div class="welcome-line"></div>
          <div class="welcome-content">
            <el-icon><ChatLineRound /></el-icon>
            <span>欢迎来到 SPDNet 聊天室</span>
          </div>
          <div class="welcome-line"></div>
        </div>

        <div v-if="!loading && messages.length === 0" class="empty-state">
          <div class="empty-visual">
            <div class="empty-glow"></div>
            <el-icon :size="56"><ChatLineRound /></el-icon>
          </div>
          <h3>暂无消息</h3>
          <p>来发送第一条消息，开启冒险交流之旅！</p>
        </div>

        <div v-else class="messages-list">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-item', { 'message-own': isOwnMessage(msg) }]"
          >
            <div class="message-time" v-if="showTime(index)">
              {{ formatTime(msg.time) }}
            </div>
            <div class="message-body">
              <div class="message-avatar-wrapper">
                <el-avatar
                  :size="44"
                  :icon="UserFilled"
                  class="message-avatar"
                  :class="{ 'own-avatar': isOwnMessage(msg) }"
                />
                <div v-if="isOwnMessage(msg)" class="online-badge"></div>
              </div>
              <div class="message-content">
                <div class="message-header">
                  <router-link :to="`/player/${msg.name}`" class="message-author">
                    {{ msg.name }}
                  </router-link>
                  <el-tag
                    v-if="isOwnMessage(msg)"
                    size="small"
                    type="success"
                    effect="dark"
                    round
                    class="own-tag"
                  >
                    我
                  </el-tag>
                  <el-tag
                    v-else-if="msg.isAdmin"
                    size="small"
                    type="danger"
                    effect="dark"
                    round
                    class="admin-tag"
                  >
                    管理员
                  </el-tag>
                </div>
                <div class="message-bubble">
                  <p class="message-text">{{ msg.message }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Scroll to bottom button -->
        <transition name="fade">
          <div
            v-if="showScrollBtn"
            class="scroll-to-bottom"
            @click="scrollToBottom"
          >
            <el-icon><ArrowDown /></el-icon>
            <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
          </div>
        </transition>
      </div>

      <!-- Input Area -->
      <div class="input-area">
        <div v-if="!authStore.isLoggedIn" class="login-prompt">
          <div class="prompt-icon">
            <el-icon><Lock /></el-icon>
          </div>
          <div class="prompt-content">
            <span class="prompt-title">需要登录</span>
            <span class="prompt-desc">登录后即可参与聊天，与其他冒险者交流</span>
          </div>
          <router-link to="/login" class="login-btn">
            <el-icon><ArrowRight /></el-icon>
            去登录
          </router-link>
        </div>
        <div v-else class="input-wrapper">
          <div class="input-toolbar">
            <el-tooltip content="表情" placement="top">
              <button class="toolbar-btn" @click="showEmoji = !showEmoji">
                <el-icon><ChatLineRound /></el-icon>
              </button>
            </el-tooltip>
            <el-tooltip content="图片" placement="top">
              <button class="toolbar-btn">
                <el-icon><Picture /></el-icon>
              </button>
            </el-tooltip>
            <span class="toolbar-divider"></span>
            <span class="input-hint">按 Enter 发送，Shift + Enter 换行</span>
          </div>
          <div class="input-main">
            <el-input
              v-model="newMessage"
              type="textarea"
              :rows="2"
              placeholder="分享你的冒险故事..."
              maxlength="500"
              show-word-limit
              resize="none"
              @keyup.enter.prevent="handleEnter"
              class="message-input"
            />
            <el-button
              type="primary"
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound, Refresh, ChatLineRound, UserFilled, Promotion,
  Lock, ArrowRight, ArrowDown, Picture
} from '@element-plus/icons-vue'
import { chatApi } from '../api'
import { authStore } from '../store/auth'

const messages = ref([])
const loading = ref(false)
const sending = ref(false)
const newMessage = ref('')
const chatContainer = ref(null)
const autoRefresh = ref(true)
const showScrollBtn = ref(false)
const unreadCount = ref(0)
const onlineCount = ref(1)
const showEmoji = ref(false)
let refreshInterval = null

const isOwnMessage = (msg) => msg.name === authStore.user?.name

const showTime = (index) => {
  if (index === 0) return true
  const prev = messages.value[index - 1]
  const curr = messages.value[index]
  if (!prev.time || !curr.time) return false
  return new Date(curr.time) - new Date(prev.time) > 5 * 60 * 1000
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleEnter = (e) => {
  if (!e.shiftKey) {
    sendMessage()
  }
}

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await chatApi.getMessages(100)
    if (res.data.success) {
      const oldLength = messages.value.length
      messages.value = res.data.data || []
      onlineCount.value = res.data.onlineCount || Math.floor(Math.random() * 50) + 10

      await nextTick()
      if (oldLength < messages.value.length) {
        const isNearBottom = chatContainer.value &&
          chatContainer.value.scrollHeight - chatContainer.value.scrollTop - chatContainer.value.clientHeight < 100
        if (isNearBottom) {
          scrollToBottom()
        } else {
          unreadCount.value += messages.value.length - oldLength
          showScrollBtn.value = true
        }
      }
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
        message: newMessage.value.trim(),
        time: new Date().toISOString()
      })
      newMessage.value = ''
      await nextTick()
      scrollToBottom()
      unreadCount.value = 0
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
    chatContainer.value.scrollTo({
      top: chatContainer.value.scrollHeight,
      behavior: 'smooth'
    })
    showScrollBtn.value = false
    unreadCount.value = 0
  }
}

const handleScroll = () => {
  if (!chatContainer.value) return
  const { scrollHeight, scrollTop, clientHeight } = chatContainer.value
  const isNearBottom = scrollHeight - scrollTop - clientHeight < 100
  showScrollBtn.value = !isNearBottom && unreadCount.value > 0
}

watch(autoRefresh, (val) => {
  if (val) {
    refreshInterval = setInterval(loadMessages, 10000)
  } else {
    clearInterval(refreshInterval)
  }
})

onMounted(() => {
  loadMessages()
  refreshInterval = setInterval(loadMessages, 10000)
  if (chatContainer.value) {
    chatContainer.value.addEventListener('scroll', handleScroll)
  }
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
  if (chatContainer.value) {
    chatContainer.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
.chat-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: var(--space-6) var(--content-padding);
  height: calc(100vh - var(--header-height) - var(--space-12));
  min-height: 600px;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
}

/* Chat Header */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-6);
  background: linear-gradient(135deg, rgba(20, 20, 35, 0.9), rgba(30, 30, 50, 0.9));
  border-bottom: 1px solid var(--border-subtle);
  position: relative;
}

.chat-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(6, 182, 212, 0.5), transparent);
}

.header-main {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.header-icon {
  position: relative;
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 0 20px rgba(6, 182, 212, 0.4);
}

.icon-glow {
  position: absolute;
  inset: -4px;
  background: radial-gradient(circle, rgba(6, 182, 212, 0.4) 0%, transparent 70%);
  border-radius: var(--radius-lg);
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.05); }
}

.header-text h2 {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.header-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-top: var(--space-1);
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 8px #10b981;
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status-text {
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.divider {
  color: var(--text-muted);
}

.message-count {
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.auto-refresh-switch :deep(.el-switch__label) {
  color: var(--text-secondary);
  font-size: 0.8125rem;
}

.refresh-btn {
  background: rgba(139, 92, 246, 0.2);
  border: 1px solid rgba(139, 92, 246, 0.3);
  color: #a78bfa;
}

.refresh-btn:hover {
  background: rgba(139, 92, 246, 0.3);
  border-color: rgba(139, 92, 246, 0.5);
}

/* Messages Area */
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-5);
  background: rgba(3, 3, 7, 0.5);
  position: relative;
}

.welcome-message {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
  padding: 0 var(--space-4);
}

.welcome-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.3), transparent);
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-tertiary);
  font-size: 0.8125rem;
  white-space: nowrap;
}

.welcome-content .el-icon {
  color: #a78bfa;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.message-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  animation: fadeInUp 0.3s ease;
}

.message-time {
  text-align: center;
  color: var(--text-tertiary);
  font-size: 0.75rem;
  margin: var(--space-2) 0;
}

.message-body {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
}

.message-item.message-own .message-body {
  flex-direction: row-reverse;
}

.message-avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.message-avatar {
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  border: 2px solid rgba(139, 92, 246, 0.3);
  transition: all 0.3s ease;
}

.message-avatar.own-avatar {
  background: linear-gradient(135deg, #10b981, #34d399);
  border-color: rgba(16, 185, 129, 0.3);
}

.online-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background: #10b981;
  border: 2px solid var(--surface-1);
  border-radius: 50%;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.message-item.message-own .message-content {
  align-items: flex-end;
}

.message-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.message-author {
  color: #a78bfa;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.875rem;
  transition: all 0.2s;
}

.message-author:hover {
  color: #c084fc;
  text-decoration: underline;
}

.own-tag {
  background: rgba(16, 185, 129, 0.2);
  border-color: rgba(16, 185, 129, 0.3);
}

.admin-tag {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.3);
}

.message-bubble {
  background: rgba(30, 30, 50, 0.8);
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: var(--radius-lg);
  border-top-left-radius: 4px;
  padding: var(--space-3) var(--space-4);
  color: var(--text-primary);
  word-break: break-word;
  line-height: 1.6;
  position: relative;
}

.message-item.message-own .message-bubble {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(52, 211, 153, 0.1));
  border-color: rgba(16, 185, 129, 0.3);
  border-top-left-radius: var(--radius-lg);
  border-top-right-radius: 4px;
}

.message-text {
  margin: 0;
}

/* Scroll to bottom button */
.scroll-to-bottom {
  position: absolute;
  bottom: var(--space-4);
  right: var(--space-4);
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
  transition: all 0.3s ease;
}

.scroll-to-bottom:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(139, 92, 246, 0.5);
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #ef4444;
  border-radius: 9px;
  font-size: 0.6875rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-16);
  text-align: center;
}

.empty-visual {
  position: relative;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
}

.empty-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.2) 0%, transparent 70%);
  animation: pulse 2s ease-in-out infinite;
}

.empty-state h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.empty-state p {
  color: var(--text-secondary);
  margin: 0;
}

/* Input Area */
.input-area {
  padding: var(--space-4) var(--space-5);
  background: linear-gradient(135deg, rgba(20, 20, 35, 0.9), rgba(30, 30, 50, 0.9));
  border-top: 1px solid var(--border-subtle);
}

.login-prompt {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.2);
  border-radius: var(--radius-lg);
}

.prompt-icon {
  width: 44px;
  height: 44px;
  background: rgba(245, 158, 11, 0.2);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fbbf24;
}

.prompt-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.prompt-title {
  font-weight: 600;
  color: var(--text-primary);
}

.prompt-desc {
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.login-btn {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
  border-radius: var(--radius-md);
  color: white;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.input-toolbar {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.toolbar-btn {
  width: 36px;
  height: 36px;
  background: rgba(139, 92, 246, 0.1);
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a78bfa;
  cursor: pointer;
  transition: all 0.2s;
}

.toolbar-btn:hover {
  background: rgba(139, 92, 246, 0.2);
  border-color: rgba(139, 92, 246, 0.4);
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: var(--border-subtle);
  margin: 0 var(--space-2);
}

.input-hint {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  margin-left: auto;
}

.input-main {
  display: flex;
  gap: var(--space-3);
  align-items: flex-end;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-textarea__inner) {
  background: rgba(15, 15, 25, 0.6);
  border: 1px solid rgba(139, 92, 246, 0.2);
  color: var(--text-primary);
  border-radius: var(--radius-lg);
  padding: var(--space-3);
  transition: all 0.3s;
}

.message-input :deep(.el-textarea__inner:focus) {
  border-color: rgba(139, 92, 246, 0.5);
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
}

.message-input :deep(.el-input__count) {
  background: transparent;
  color: var(--text-tertiary);
}

.send-btn {
  height: 52px;
  padding: 0 var(--space-6);
  font-weight: 600;
  background: linear-gradient(135deg, #8b5cf6, #a855f7);
  border: none;
}

.send-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #7c3aed, #9333ea);
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.4);
}

.send-btn:disabled {
  background: rgba(139, 92, 246, 0.3);
  opacity: 0.6;
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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* Responsive */
@media (max-width: 768px) {
  .chat-page {
    padding: var(--space-4);
    height: calc(100vh - var(--header-height) - var(--space-8));
  }

  .chat-header {
    padding: var(--space-4);
  }

  .header-icon {
    width: 44px;
    height: 44px;
  }

  .header-text h2 {
    font-size: 1.125rem;
  }

  .message-content {
    max-width: 80%;
  }

  .input-main {
    flex-direction: column;
  }

  .send-btn {
    width: 100%;
    height: 44px;
  }

  .login-prompt {
    flex-direction: column;
    text-align: center;
    gap: var(--space-3);
  }

  .login-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>