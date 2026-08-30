<template>
  <span
    class="prefix-badge"
    :class="{ 'clickable-prefix': clickable }"
    :style="getPrefixStyle(prefix, size)"
    :title="title"
    @click="handleClick"
  >{{ prefix.displayText }}</span>
</template>

<script setup>
// SPDNet: 玩家前缀徽章展示组件
// 收敛各视图中重复的前缀渲染逻辑（样式 + 点击跳转前缀详情）
import { useRouter } from 'vue-router'
import { getPrefixStyle } from '../utils/format'

const props = defineProps({
  // 前缀对象（需含 displayText/color/backgroundColor/id）
  prefix: {
    type: Object,
    required: true
  },
  // 是否可点击跳转到前缀详情
  clickable: {
    type: Boolean,
    default: true
  },
  // 悬停提示文案
  title: {
    type: String,
    default: '点击查看前缀详情'
  },
  // 徽章尺寸: 'md' 常规 / 'xs' 小号（聊天等紧凑场景）
  size: {
    type: String,
    default: 'md'
  }
})

const router = useRouter()

// 点击跳转到前缀详情页；阻止冒泡避免触发外层链接/卡片跳转
const handleClick = (e) => {
  if (!props.clickable || !props.prefix?.id) return
  e.preventDefault()
  e.stopPropagation()
  router.push(`/prefix/${props.prefix.id}`)
}
</script>

<style scoped>
.prefix-badge {
  display: inline-block;
}

.clickable-prefix {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.clickable-prefix:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}
</style>