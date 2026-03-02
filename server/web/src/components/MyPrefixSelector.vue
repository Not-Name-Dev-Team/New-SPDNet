<template>
  <div class="my-prefix-selector">
    <div class="section-header">
      <div class="header-title">
        <el-icon><Medal /></el-icon>
        <span>我的称号前缀</span>
      </div>
    </div>

    <div class="prefix-content" v-loading="loading">
      <!-- Current Active Prefix -->
      <div class="current-prefix-section">
        <span class="section-label">当前使用：</span>
        <div v-if="activePrefix" class="active-prefix">
          <span
            class="prefix-badge"
            :style="getPrefixStyle(activePrefix.prefix)"
          >
            {{ activePrefix.prefix.displayText }}
          </span>
        </div>
        <div v-else class="no-active-prefix">
          未选择前缀
        </div>
      </div>

      <!-- My Prefixes List -->
      <div class="my-prefixes-section">
        <h4>我的前缀列表</h4>
        <div v-if="myPrefixes.length > 0" class="prefix-list">
          <div
            v-for="assignment in myPrefixes"
            :key="assignment.id"
            class="prefix-item"
            :class="{ active: assignment.active }"
            @click="selectPrefix(assignment)"
          >
            <span
              class="prefix-badge"
              :style="getPrefixStyle(assignment.prefix)"
            >
              {{ assignment.prefix.displayText }}
            </span>
            <span v-if="assignment.active" class="active-indicator">
              <el-icon><Check /></el-icon>
              使用中
            </span>
            <span v-else class="select-hint">点击选择</span>
          </div>
        </div>
        <el-empty v-else description="暂无称号前缀" />

        <!-- No Prefix Option -->
        <div
          v-if="myPrefixes.length > 0"
          class="prefix-item no-prefix"
          :class="{ active: !activePrefix }"
          @click="selectPrefix(null)"
        >
          <span>不使用前缀</span>
          <span v-if="!activePrefix" class="active-indicator">
            <el-icon><Check /></el-icon>
            使用中
          </span>
          <span v-else class="select-hint">点击选择</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Medal, Check } from '@element-plus/icons-vue'
import { prefixApi } from '../api'
import { authStore } from '../store/auth'

const emit = defineEmits(['prefix-changed'])

const loading = ref(false)
const myPrefixes = ref([])
const activePrefix = ref(null)

// 获取当前登录用户的名称
const playerName = computed(() => {
  return authStore.user?.name || ''
})

const getPrefixStyle = (prefix) => {
  return {
    color: prefix.color || '#ffffff',
    backgroundColor: prefix.backgroundColor || 'rgba(139, 92, 246, 0.8)',
    padding: '4px 12px',
    borderRadius: '6px',
    fontSize: '14px',
    fontWeight: 'bold'
  }
}

const loadMyPrefixes = async () => {
  if (!playerName.value) {
    console.error('未获取到玩家名称')
    return
  }

  loading.value = true
  try {
    const res = await prefixApi.getMyPrefixes(playerName.value)
    if (res.data.success) {
      myPrefixes.value = res.data.data || []
      // 找到当前激活的前缀
      activePrefix.value = myPrefixes.value.find(p => p.active) || null
    }
  } catch (error) {
    console.error('加载前缀失败:', error)
    ElMessage.error('加载前缀失败')
  } finally {
    loading.value = false
  }
}

const selectPrefix = async (assignment) => {
  // 如果点击的是当前已激活的，不做任何操作
  if (assignment && assignment.active) return
  if (!assignment && !activePrefix.value) return

  if (!playerName.value) {
    ElMessage.error('未获取到玩家信息')
    return
  }

  try {
    const assignmentId = assignment ? assignment.id : null
    const res = await prefixApi.setMyActivePrefix(playerName.value, assignmentId)
    if (res.data.success) {
      ElMessage.success(assignmentId ? '前缀设置成功' : '已取消前缀')
      // 更新本地状态
      myPrefixes.value.forEach(p => p.active = false)
      if (assignment) {
        assignment.active = true
        activePrefix.value = assignment
      } else {
        activePrefix.value = null
      }
      // 通知父组件前缀已更改
      emit('prefix-changed')
    } else {
      ElMessage.error(res.data.message || '设置失败')
    }
  } catch (error) {
    console.error('设置前缀失败:', error)
    ElMessage.error(error.response?.data?.message || '设置失败')
  }
}

onMounted(() => {
  // 如果playerName已经存在，立即加载
  if (playerName.value) {
    loadMyPrefixes()
  }
})

// 监听playerName变化，当获取到用户名称后加载前缀
watch(playerName, (newName) => {
  if (newName) {
    loadMyPrefixes()
  }
})
</script>

<style scoped>
.my-prefix-selector {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  margin-bottom: var(--space-5);
}

.section-header {
  padding: var(--space-4) var(--space-5);
  background: rgba(139, 92, 246, 0.1);
  border-bottom: 1px solid var(--border-subtle);
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: 600;
  color: var(--text-primary);
}

.header-title .el-icon {
  color: #a78bfa;
  font-size: 1.25rem;
}

.prefix-content {
  padding: var(--space-5);
}

.current-prefix-section {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
  padding: var(--space-4);
  background: rgba(20, 20, 35, 0.5);
  border-radius: var(--radius-lg);
}

.section-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.active-prefix {
  display: flex;
  align-items: center;
}

.no-active-prefix {
  font-size: 0.875rem;
  color: var(--text-tertiary);
  font-style: italic;
}

.prefix-badge {
  display: inline-block;
}

.my-prefixes-section h4 {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin: 0 0 var(--space-3);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.prefix-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.prefix-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  background: rgba(30, 30, 50, 0.5);
  border: 2px solid transparent;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.2s ease;
}

.prefix-item:hover {
  background: rgba(50, 50, 70, 0.5);
  border-color: rgba(139, 92, 246, 0.3);
}

.prefix-item.active {
  border-color: rgba(34, 197, 94, 0.5);
  background: rgba(34, 197, 94, 0.1);
}

.prefix-item.no-prefix {
  margin-top: var(--space-3);
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.active-indicator {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: 0.75rem;
  color: #22c55e;
  font-weight: 500;
}

.select-hint {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.prefix-item:hover .select-hint {
  opacity: 1;
}
</style>
