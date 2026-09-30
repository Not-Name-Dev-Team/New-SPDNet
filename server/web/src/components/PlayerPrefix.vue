<template>
  <div class="prefix-management">
    <div class="section-header-bar">
      <div class="header-title">
        <div class="title-icon prefix">
          <el-icon><Medal /></el-icon>
        </div>
        <div class="title-content">
          <h2>前缀管理</h2>
          <span class="section-desc">管理玩家称号前缀</span>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">
          创建前缀
        </el-button>
      </div>
    </div>

    <!-- Prefix List -->
    <div class="prefix-list" v-loading="loading">
      <div v-for="prefix in prefixes" :key="prefix.id" class="prefix-card">
        <div class="prefix-preview">
          <PrefixBadge :prefix="prefix" :clickable="false" />
        </div>
        <div class="prefix-info">
          <div class="prefix-name">{{ prefix.name }}</div>
          <div class="prefix-desc">{{ prefix.description || '无描述' }}</div>
        </div>
        <div class="prefix-actions">
          <el-button type="primary" text :icon="Edit" @click="editPrefix(prefix)">
            编辑
          </el-button>
          <el-button type="danger" text :icon="Delete" @click="deletePrefix(prefix)">
            删除
          </el-button>
        </div>
      </div>
      <el-empty v-if="prefixes.length === 0" description="暂无前缀" />
    </div>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingPrefix ? '编辑前缀' : '创建前缀'"
      width="500px"
      @closed="closeDialog"
    >
      <el-form :model="prefixForm" label-width="100px">
        <el-form-item label="标识" v-if="!editingPrefix">
          <el-input v-model="prefixForm.name" placeholder="唯一标识，如：vip" />
        </el-form-item>
        <el-form-item label="显示文本">
          <el-input v-model="prefixForm.displayText" placeholder="显示的前缀文本，如：[VIP]" />
        </el-form-item>
        <el-form-item label="文字颜色">
          <el-color-picker v-model="prefixForm.color" show-alpha />
        </el-form-item>
        <el-form-item label="背景颜色">
          <el-color-picker v-model="prefixForm.backgroundColor" show-alpha />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="prefixForm.description"
            type="textarea"
            :rows="2"
            placeholder="前缀描述说明"
          />
        </el-form-item>
        <el-form-item label="预览">
          <PrefixBadge
            :prefix="{ ...prefixForm, displayText: prefixForm.displayText || '预览' }"
            :clickable="false"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="savePrefix" :loading="saving">
          {{ editingPrefix ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Assign Prefix to Player -->
    <div class="assign-section">
      <h3>管理玩家前缀</h3>
      <div class="assign-form">
        <!-- SPDNet: 玩家下拉必须走服务端搜索，不要改回 v-for 渲染传入的数组。
             用户量上千时，一次性渲染全部 <el-option> 会拖垮页面；而父组件的
             players 只是当前分页的 20 条，渲染它只会让下拉选不全玩家。
             这里按关键词查询后端（复用 AdminController 的 search 参数）。 -->
        <el-select
          v-model="selectedPlayer"
          placeholder="搜索并选择玩家"
          filterable
          remote
          reserve-keyword
          clearable
          :remote-method="searchPlayers"
          :loading="playerSearchLoading"
          style="width: 240px"
          @change="onPlayerChange"
        >
          <el-option
            v-for="player in playerOptions"
            :key="player.id"
            :label="player.name"
            :value="player.name"
          />
          <template #empty>
            <p class="select-empty-hint">
              {{ playerSearchKeyword ? '未找到匹配的玩家' : '输入玩家名以搜索' }}
            </p>
          </template>
        </el-select>
      </div>

      <!-- Player's Prefixes -->
      <div v-if="selectedPlayer" class="player-prefixes" v-loading="playerPrefixesLoading">
        <h4>{{ selectedPlayer }} 的前缀</h4>
        <div class="assigned-prefixes">
          <div
            v-for="assignment in playerPrefixes"
            :key="assignment.id"
            class="assigned-prefix-item"
            :class="{ active: assignment.active }"
          >
            <PrefixBadge :prefix="assignment.prefix" :clickable="false" />
            <span v-if="assignment.active" class="active-tag">当前使用</span>
            <el-button
              type="danger"
              text
              size="small"
              :icon="Delete"
              @click="removePrefixFromPlayer(assignment.prefix.id)"
            >
              移除
            </el-button>
          </div>
          <el-empty v-if="playerPrefixes.length === 0" description="该玩家暂无前缀" />
        </div>

        <!-- Assign New Prefix -->
        <div class="assign-new-prefix">
          <h4>分配新前缀</h4>
          <div class="assign-form">
            <el-select
              v-model="prefixToAssign"
              placeholder="选择要分配的前缀"
              clearable
              style="width: 250px"
            >
              <el-option
                v-for="prefix in availablePrefixesToAssign"
                :key="prefix.id"
                :label="prefix.displayText"
                :value="prefix.id"
              >
                <PrefixBadge :prefix="prefix" :clickable="false" size="xs" />
              </el-option>
            </el-select>
            <el-button
              type="primary"
              @click="assignPrefixToPlayer"
              :loading="assigning"
              :disabled="!prefixToAssign"
            >
              分配
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Medal } from '@element-plus/icons-vue'
import { adminApi } from '../api'
import PrefixBadge from './PrefixBadge.vue'

// SPDNet: 不接收父组件传入的玩家列表 —— 那是当前分页数据，选不全；
// 玩家下拉在本组件内按关键词向服务端搜索（见 searchPlayers）。
const loading = ref(false)
const saving = ref(false)
const assigning = ref(false)
const playerPrefixesLoading = ref(false)
const prefixes = ref([])
const playerPrefixes = ref([])
const showCreateDialog = ref(false)
const editingPrefix = ref(null)
const selectedPlayer = ref('')
const prefixToAssign = ref(null)

// SPDNet: 玩家下拉的服务端搜索状态
const playerOptions = ref([])
const playerSearchLoading = ref(false)
const playerSearchKeyword = ref('')
let playerSearchTimer = null

// SPDNet: 输入防抖 300ms，避免每敲一个字就打一次接口
const searchPlayers = (keyword) => {
  playerSearchKeyword.value = keyword || ''
  clearTimeout(playerSearchTimer)

  if (!playerSearchKeyword.value.trim()) {
    playerOptions.value = []
    return
  }

  playerSearchTimer = setTimeout(async () => {
    playerSearchLoading.value = true
    try {
      const res = await adminApi.getPlayers(0, 20, null, playerSearchKeyword.value.trim())
      if (res.data.success) {
        playerOptions.value = res.data.data?.players || []
      }
    } catch (error) {
      console.error('搜索玩家失败:', error)
    } finally {
      playerSearchLoading.value = false
    }
  }, 300)
}

const prefixForm = ref({
  name: '',
  displayText: '',
  color: '#ffffff',
  backgroundColor: 'rgba(139, 92, 246, 0.8)',
  description: ''
})

// 计算可分配给当前玩家的前缀（过滤掉已分配的）
const availablePrefixesToAssign = computed(() => {
  const assignedIds = playerPrefixes.value.map(p => p.prefix.id)
  return prefixes.value.filter(p => !assignedIds.includes(p.id))
})

// SPDNet: 前缀渲染统一走 PrefixBadge + utils/format.js 的 getPrefixStyle，
// 不再各处自己维护一份（原先此处与 MyPrefixSelector、format.js 三份数值互相不一致）。
const loadPrefixes = async () => {
  loading.value = true
  try {
    const res = await adminApi.getPrefixes()
    if (res.data.success) {
      prefixes.value = res.data.data || []
    }
  } catch (error) {
    console.error('加载前缀失败:', error)
    ElMessage.error('加载前缀失败')
  } finally {
    loading.value = false
  }
}

const loadPlayerPrefixes = async () => {
  if (!selectedPlayer.value) {
    playerPrefixes.value = []
    return
  }

  playerPrefixesLoading.value = true
  try {
    const res = await adminApi.getPlayerPrefixes(selectedPlayer.value)
    if (res.data.success) {
      playerPrefixes.value = res.data.data || []
    }
  } catch (error) {
    console.error('加载玩家前缀失败:', error)
    ElMessage.error('加载玩家前缀失败')
  } finally {
    playerPrefixesLoading.value = false
  }
}

const onPlayerChange = () => {
  prefixToAssign.value = null
  // SPDNet: 先清空上一个玩家的前缀列表，否则新玩家数据到达前，
  // availablePrefixesToAssign 会基于旧玩家的分配情况计算，导致下拉出现已分配的前缀。
  playerPrefixes.value = []
  loadPlayerPrefixes()
}

const closeDialog = () => {
  showCreateDialog.value = false
  editingPrefix.value = null
  prefixForm.value = {
    name: '',
    displayText: '',
    color: '#ffffff',
    backgroundColor: 'rgba(139, 92, 246, 0.8)',
    description: ''
  }
}

// SPDNet: 供"创建前缀"入口使用，确保不会带着上一次编辑的残留状态打开
const openCreateDialog = () => {
  closeDialog()
  showCreateDialog.value = true
}

const editPrefix = (prefix) => {
  editingPrefix.value = prefix
  prefixForm.value = {
    name: prefix.name,
    displayText: prefix.displayText,
    color: prefix.color || '#ffffff',
    backgroundColor: prefix.backgroundColor || 'rgba(139, 92, 246, 0.8)',
    description: prefix.description || ''
  }
  showCreateDialog.value = true
}

const savePrefix = async () => {
  if (!prefixForm.value.displayText) {
    ElMessage.error('显示文本不能为空')
    return
  }

  saving.value = true
  try {
    if (editingPrefix.value) {
      const res = await adminApi.updatePrefix(editingPrefix.value.id, prefixForm.value)
      if (res.data.success) {
        ElMessage.success('更新成功')
        closeDialog()
        loadPrefixes()
      } else {
        ElMessage.error(res.data.message || '更新失败')
      }
    } else {
      if (!prefixForm.value.name) {
        ElMessage.error('标识不能为空')
        saving.value = false
        return
      }
      const res = await adminApi.createPrefix(prefixForm.value)
      if (res.data.success) {
        ElMessage.success('创建成功')
        closeDialog()
        loadPrefixes()
      } else {
        ElMessage.error(res.data.message || '创建失败')
      }
    }
  } catch (error) {
    console.error('保存前缀失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const deletePrefix = async (prefix) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除前缀 "${prefix.displayText}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await adminApi.deletePrefix(prefix.id)
    if (res.data.success) {
      ElMessage.success('删除成功')
      loadPrefixes()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除前缀失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const assignPrefixToPlayer = async () => {
  if (!selectedPlayer.value || !prefixToAssign.value) {
    ElMessage.error('请选择玩家和前缀')
    return
  }

  assigning.value = true
  try {
    const res = await adminApi.assignPrefixToPlayer(selectedPlayer.value, prefixToAssign.value)
    if (res.data.success) {
      ElMessage.success('分配成功')
      prefixToAssign.value = null
      loadPlayerPrefixes()
    } else {
      ElMessage.error(res.data.message || '分配失败')
    }
  } catch (error) {
    console.error('分配前缀失败:', error)
    ElMessage.error(error.response?.data?.message || '分配失败')
  } finally {
    assigning.value = false
  }
}

const removePrefixFromPlayer = async (prefixId) => {
  if (!selectedPlayer.value) return

  try {
    await ElMessageBox.confirm(
      '确定要移除此前缀吗？',
      '确认移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await adminApi.removePrefixFromPlayer(selectedPlayer.value, prefixId)
    if (res.data.success) {
      ElMessage.success('移除成功')
      loadPlayerPrefixes()
    } else {
      ElMessage.error(res.data.message || '移除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除前缀失败:', error)
      ElMessage.error(error.response?.data?.message || '移除失败')
    }
  }
}

onMounted(() => {
  loadPrefixes()
})
</script>

<style scoped>
.prefix-management {
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
}

.section-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-6);
  background: rgba(20, 20, 35, 0.5);
  border-bottom: 1px solid var(--border-subtle);
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.title-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(168, 85, 247, 0.1));
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a78bfa;
  font-size: 1.25rem;
}

.title-icon.prefix {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.2), rgba(251, 191, 36, 0.1));
  border-color: rgba(245, 158, 11, 0.3);
  color: #fbbf24;
}

.title-content h2 {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0 0 2px;
  color: var(--text-primary);
}

.section-desc {
  font-size: 0.8125rem;
  color: var(--text-secondary);
}

.prefix-list {
  padding: var(--space-4);
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--space-4);
}

.prefix-card {
  background: rgba(20, 20, 35, 0.5);
  border: 1px solid rgba(139, 92, 246, 0.1);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-4);
  transition: all 0.3s ease;
}

.prefix-card:hover {
  border-color: rgba(139, 92, 246, 0.3);
  transform: translateY(-2px);
}

.prefix-preview {
  flex-shrink: 0;
}

.prefix-info {
  flex: 1;
  min-width: 0;
}

.prefix-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.prefix-desc {
  font-size: 0.75rem;
  /* SPDNet: 前缀描述是内容，用 secondary 保证可读 */
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.prefix-actions {
  display: flex;
  gap: var(--space-1);
}

.assign-section {
  padding: var(--space-5) var(--space-6);
  border-top: 1px solid var(--border-subtle);
  background: rgba(20, 20, 35, 0.3);
}

.assign-section h3 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-4);
}

.assign-section h4 {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin: var(--space-4) 0 var(--space-3);
}

.assign-form {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: var(--space-4);
}

/* SPDNet: 远程搜索下拉的空状态提示 */
.select-empty-hint {
  margin: 0;
  padding: var(--space-3);
  text-align: center;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.player-prefixes {
  margin-top: var(--space-4);
  padding: var(--space-4);
  background: rgba(20, 20, 35, 0.5);
  border-radius: var(--radius-lg);
}

.assigned-prefixes {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.assigned-prefix-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-3);
  background: rgba(30, 30, 50, 0.5);
  border-radius: var(--radius-md);
  border: 1px solid transparent;
}

.assigned-prefix-item.active {
  border-color: rgba(34, 197, 94, 0.5);
  background: rgba(34, 197, 94, 0.1);
}

.active-tag {
  font-size: 0.75rem;
  color: #22c55e;
  background: rgba(34, 197, 94, 0.2);
  padding: 2px 8px;
  border-radius: 4px;
  margin-right: auto;
}

.assign-new-prefix {
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-subtle);
}
</style>
