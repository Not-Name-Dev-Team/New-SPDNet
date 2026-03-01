<template>
  <div class="chat-page">
    <el-card class="chat-card" shadow="hover">
      <template #header>
        <div class="chat-header">
          <div class="header-title">
            <el-icon :size="24" color="var(--primary-color)"><ChatDotRound /></el-icon>
            <span>聊天室</span>
            <el-tag type="success" effect="dark" round size="small" v-if="authStore.isLoggedIn">
              在线
            </el-tag>
          </div>
          <div class="header-actions">
            <el-button type="primary" text :icon="Refresh" @click="loadMessages" :loading="loading">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <div class="chat-container" ref="chatContainer" v-loading="loading">
        <el-empty v-if="!loading && messages.length === 0" description="暂无消息，来发送第一条消息吧！">
          <template #image>
            <el-icon :size="60" color="var(--text-muted)"><ChatLineRound /></el-icon>
          </template>
        </el-empty>

        <div v-else class="messages-list">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-item', { 'message-own': msg.name === authStore.user?.name }]"
          >
            <el-avatar 
              :size="40" 
              :icon="UserFilled"
              class="message-avatar"
              :class="{ 'own-avatar': msg.name === authStore.user?.name }"
            />
            <div class="message-content">
              <div class="message-header">
                <router-link :to="`/player/${msg.name}`" class="message-author">
                  {{ msg.name }}
                </router-link>
                <el-tag v-if="msg.name === authStore.user?.name" size="small" type="success" effect="plain">我</el-tag>
              </div>
              <div class="message-bubble">
                {{ msg.message }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <el-alert
          v-if="!authStore.isLoggedIn"
          title="请先登录后再参与聊天"
          type="warning"
          show-icon
          :closable="false"
          class="login-warning"
        />
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Refresh, ChatLineRound, UserFilled, Promotion } from '@element-plus/icons-vue'
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
  height: calc(100vh - 200px);
  min-height: 500px;
}

.chat-card {
  height: 100%;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}

:deep(.chat-card .el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.125rem;
  font-weight: 600;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
  background: var(--bg-dark);
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.message-item {
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
}

.message-item.message-own {
  flex-direction: row-reverse;
}

.message-avatar {
  background: var(--gradient-primary);
  flex-shrink: 0;
}

.message-avatar.own-avatar {
  background: var(--gradient-success);
}

.message-content {
  max-width: 70%;
}

.message-own .message-content {
  text-align: right;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
}

.message-own .message-header {
  justify-content: flex-end;
}

.message-author {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  font-size: 0.875rem;
}

.message-author:hover {
  text-decoration: underline;
}

.message-bubble {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 0.75rem 1rem;
  color: var(--text-primary);
  word-break: break-word;
  display: inline-block;
  text-align: left;
}

.message-own .message-bubble {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
}

.chat-input-area {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--border-color);
  background: var(--bg-card);
}

.login-warning {
  margin: 0;
}

.input-wrapper {
  display: flex;
  gap: 0.75rem;
  align-items: flex-end;
}

.input-wrapper .el-textarea {
  flex: 1;
}

:deep(.input-wrapper .el-textarea__inner) {
  background: var(--bg-dark);
  border-color: var(--border-color);
  color: var(--text-primary);
}

:deep(.input-wrapper .el-textarea__inner:focus) {
  border-color: var(--primary-color);
}

.send-btn {
  height: 52px;
  padding: 0 1.5rem;
}

@media (max-width: 768px) {
  .chat-page {
    height: calc(100vh - 150px);
  }
  
  .message-content {
    max-width: 80%;
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
