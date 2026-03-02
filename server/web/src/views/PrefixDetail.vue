<template>
  <div class="prefix-detail-page">
    <!-- Header -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon-wrapper">
          <el-icon :size="28"><Medal /></el-icon>
        </div>
        <div class="header-text">
          <h1 class="page-title">前缀详情</h1>
          <p class="page-subtitle">查看称号的详细信息和获得记录</p>
        </div>
      </div>
      <el-button type="primary" plain :icon="ArrowLeft" @click="goBack" class="back-btn">
        返回
      </el-button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- Content -->
    <div v-else-if="prefixInfo" class="content-wrapper">
      <!-- Prefix Info Card -->
      <div class="prefix-info-card">
        <div class="prefix-header">
          <div class="prefix-badge" :style="getPrefixStyle(prefixInfo)">
            {{ prefixInfo.displayText }}
          </div>
          <div class="prefix-meta">
            <h2 class="prefix-name">{{ prefixInfo.name }}</h2>
            <p class="prefix-id">ID: {{ prefixInfo.id }}</p>
          </div>
        </div>
        <div class="prefix-description" v-if="prefixInfo.description">
          <el-icon><InfoFilled /></el-icon>
          <span>{{ prefixInfo.description }}</span>
        </div>
        <div class="prefix-stats">
          <div class="stat-item">
            <el-icon><User /></el-icon>
            <span class="stat-label">拥有者数量</span>
            <span class="stat-value">{{ ownerCount }} 人</span>
          </div>
          <div class="stat-item">
            <el-icon><Clock /></el-icon>
            <span class="stat-label">创建时间</span>
            <span class="stat-value">{{ formatDateTime(prefixInfo.createdAt) }}</span>
          </div>
          <div class="stat-item" v-if="prefixInfo.updatedAt && prefixInfo.updatedAt !== prefixInfo.createdAt">
            <el-icon><Refresh /></el-icon>
            <span class="stat-label">最后更新</span>
            <span class="stat-value">{{ formatDateTime(prefixInfo.updatedAt) }}</span>
          </div>
        </div>
      </div>

      <!-- Owners List -->
      <div class="owners-section">
        <div class="section-header">
          <div class="section-title">
            <el-icon><UserFilled /></el-icon>
            <span>拥有者列表</span>
          </div>
          <el-tag type="info" effect="dark" size="small">
            共 {{ ownerCount }} 人
          </el-tag>
        </div>

        <div v-if="owners.length > 0" class="owners-list">
          <div
            v-for="owner in owners"
            :key="owner.id"
            class="owner-item"
            :class="{ 'is-active': owner.active }"
          >
            <div class="owner-avatar">
              <el-avatar :size="48" :icon="UserFilled" />
              <div v-if="owner.active" class="active-badge" title="当前使用中">
                <el-icon><Check /></el-icon>
              </div>
            </div>
            <div class="owner-info">
              <router-link :to="`/player/${owner.playerName}`" class="owner-name">
                {{ owner.playerName }}
              </router-link>
              <div class="owner-meta">
                <el-tag v-if="owner.active" type="success" size="small" effect="dark">
                  当前使用
                </el-tag>
                <span class="obtain-time">
                  <el-icon><Clock /></el-icon>
                  获得于 {{ formatTimeAgo(owner.assignedAt) }}
                </span>
              </div>
            </div>
            <div class="owner-actions">
              <router-link :to="`/player/${owner.playerName}`">
                <el-button type="primary" text :icon="View">
                  查看玩家
                </el-button>
              </router-link>
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <div class="empty-icon">
            <el-icon :size="48"><User /></el-icon>
          </div>
          <p>暂无拥有者</p>
          <span>还没有玩家获得此前缀</span>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="totalElements"
            layout="total, sizes, prev, pager, next"
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else class="error-state">
      <div class="error-icon">
        <el-icon :size="64"><Warning /></el-icon>
      </div>
      <h2>前缀不存在</h2>
      <p>无法找到指定的前缀信息</p>
      <el-button type="primary" @click="goBack">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Medal, ArrowLeft, User, UserFilled, Clock, Refresh,
  InfoFilled, Check, View, Warning
} from '@element-plus/icons-vue'
import { prefixApi } from '../api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const prefixInfo = ref(null)
const owners = ref([])
const ownerCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const totalElements = ref(0)
const totalPages = ref(0)

// 获取前缀样式
const getPrefixStyle = (prefix) => {
  return {
    color: prefix.color || '#ffffff',
    backgroundColor: prefix.backgroundColor || 'rgba(139, 92, 246, 0.8)',
    padding: '8px 16px',
    borderRadius: '8px',
    fontSize: '1.25rem',
    fontWeight: 'bold',
    display: 'inline-block'
  }
}

// 格式化日期时间
const formatDateTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 格式化时间为"多久之前"
const formatTimeAgo = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  const months = Math.floor(days / 30)
  const years = Math.floor(days / 365)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days < 30) return `${days} 天前`
  if (months < 12) return `${months} 个月前`
  return `${years} 年前`
}

const goBack = () => {
  router.back()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadPrefixOwners()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadPrefixOwners()
}

const loadPrefixInfo = async () => {
  const prefixId = route.params.id
  if (!prefixId) {
    ElMessage.error('前缀ID不能为空')
    return
  }

  loading.value = true
  try {
    const res = await prefixApi.getPrefixInfo(prefixId)
    if (res.data.success) {
      prefixInfo.value = res.data.data
      ownerCount.value = res.data.data?.ownerCount || 0
    } else {
      ElMessage.error(res.data.message || '获取前缀信息失败')
    }
  } catch (error) {
    console.error('获取前缀信息失败:', error)
    ElMessage.error('获取前缀信息失败')
  } finally {
    loading.value = false
  }
}

const loadPrefixOwners = async () => {
  const prefixId = route.params.id
  if (!prefixId) return

  try {
    const res = await prefixApi.getPrefixOwners(prefixId, {
      page: currentPage.value - 1,
      size: pageSize.value
    })
    if (res.data.success) {
      const data = res.data.data || {}
      owners.value = data.owners || []
      totalElements.value = data.totalElements || 0
      totalPages.value = data.totalPages || 1
    }
  } catch (error) {
    console.error('获取前缀拥有者失败:', error)
  }
}

watch(() => route.params.id, () => {
  loadPrefixInfo()
  loadPrefixOwners()
})

onMounted(() => {
  loadPrefixInfo()
  loadPrefixOwners()
})
</script>

<style scoped>
.prefix-detail-page {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: var(--space-6) var(--content-padding);
}

/* Page Header */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.header-content {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.header-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, #8b5cf6 0%, #a855f7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(139, 92, 246, 0.25);
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #8b5cf6 0%, #a855f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  color: var(--text-secondary);
  margin: var(--space-1) 0 0;
  font-size: 0.9375rem;
}

.back-btn {
  font-weight: 500;
  color: white;
  background: rgba(168, 85, 247, 0.3);
  border-color: var(--primary-400);
}

.back-btn:hover {
  color: white;
  background: rgba(168, 85, 247, 0.5);
  border-color: var(--primary-300);
}

.owner-actions .el-button {
  color: white;
  background: rgba(168, 85, 247, 0.3);
}

.owner-actions .el-button:hover {
  color: white;
  background: rgba(168, 85, 247, 0.5);
}

/* Loading State */
.loading-state {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
}

/* Content Wrapper */
.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

/* Prefix Info Card */
.prefix-info-card {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
}

.prefix-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.prefix-badge {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.prefix-name {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.prefix-id {
  font-size: 0.875rem;
  color: var(--text-tertiary);
  margin: var(--space-1) 0 0;
}

.prefix-description {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
  padding: var(--space-4);
  background: var(--surface-2);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-4);
  color: var(--text-secondary);
}

.prefix-description .el-icon {
  color: var(--primary-400);
  margin-top: 2px;
  flex-shrink: 0;
}

.prefix-stats {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: var(--surface-2);
  border-radius: var(--radius-lg);
}

.stat-item .el-icon {
  color: var(--primary-400);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.stat-value {
  font-weight: 600;
  color: var(--text-primary);
  margin-left: var(--space-1);
}

/* Owners Section */
.owners-section {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-subtle);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
}

.section-title .el-icon {
  color: var(--primary-400);
}

/* Owners List */
.owners-list {
  display: flex;
  flex-direction: column;
}

.owner-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-subtle);
  transition: all var(--transition-fast);
}

.owner-item:last-child {
  border-bottom: none;
}

.owner-item:hover {
  background: var(--surface-2);
}

.owner-item.is-active {
  background: linear-gradient(90deg, rgba(16, 185, 129, 0.05) 0%, transparent 100%);
  border-left: 3px solid var(--accent-emerald);
}

.owner-avatar {
  position: relative;
  flex-shrink: 0;
}

.owner-avatar :deep(.el-avatar) {
  background: var(--gradient-primary);
}

.active-badge {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 20px;
  height: 20px;
  border-radius: var(--radius-full);
  background: var(--accent-emerald);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  border: 2px solid var(--surface-1);
}

.owner-info {
  flex: 1;
  min-width: 0;
}

.owner-name {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.owner-name:hover {
  color: var(--primary-400);
}

.owner-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-top: var(--space-1);
}

.obtain-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.obtain-time .el-icon {
  font-size: 14px;
}

.owner-actions {
  flex-shrink: 0;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
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
  margin-bottom: var(--space-4);
}

.empty-state p {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-1);
}

.empty-state span {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: var(--space-4);
  border-top: 1px solid var(--border-subtle);
}

/* Error State */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16);
  text-align: center;
}

.error-icon {
  width: 100px;
  height: 100px;
  border-radius: var(--radius-xl);
  background: var(--surface-2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-rose);
  margin-bottom: var(--space-4);
}

.error-state h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
}

.error-state p {
  color: var(--text-secondary);
  margin: 0 0 var(--space-6);
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .prefix-header {
    flex-direction: column;
    text-align: center;
  }

  .prefix-stats {
    flex-direction: column;
  }

  .owner-item {
    flex-wrap: wrap;
  }

  .owner-actions {
    width: 100%;
    margin-top: var(--space-2);
  }

  .owner-actions :deep(.el-button) {
    width: 100%;
  }
}
</style>
