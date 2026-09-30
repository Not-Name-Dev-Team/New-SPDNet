<template>
  <!-- SPDNet: 可点击时渲染为 <button>，保证键盘(Enter/Space)可聚焦可触发；
       不可点击时保持 <span>，避免在链接/卡片内产生无意义的可聚焦元素 -->
  <component
    :is="clickable ? 'button' : 'span'"
    class="prefix-badge"
    :class="{ 'clickable-prefix': clickable }"
    :style="getPrefixStyle(prefix, size)"
    :type="clickable ? 'button' : undefined"
    :title="title"
    :aria-label="clickable ? `${prefix.displayText}，${title}` : undefined"
    @click="handleClick"
  >{{ prefix.displayText }}</component>
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
  e.stopPropagation()
  router.push(`/prefix/${props.prefix.id}`)
}
</script>

<style scoped>
.prefix-badge {
  display: inline-block;
  /* 渲染为 button 时重置浏览器默认外观，保持与原来的 span 视觉一致 */
  border: none;
  font-family: inherit;
  line-height: inherit;
  text-align: inherit;
  vertical-align: baseline;
}

button.prefix-badge {
  cursor: pointer;
  appearance: none;
  /* 重置 UA 默认外观，确保与 span 版本视觉一致（内联样式仍设置颜色/背景/字号） */
  padding: 0;
  background: none;
  font-size: inherit;
  font-weight: inherit;
  color: inherit;
}

button.prefix-badge:focus-visible {
  outline: 2px solid var(--primary-400);
  outline-offset: 2px;
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