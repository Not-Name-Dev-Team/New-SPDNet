// SPDNet: 前端共享格式化/显示工具函数
// 该模块收敛各视图重复实现的前缀样式、角色标签、游戏模式文本逻辑
// 说明：其余夹具(角色/游戏模式)映射需与后端保持一致

// 玩家角色 -> Element Plus tag 类型
// 兼容两种取值：中文枚举（前端页）与英文枚举（Admin 管理端）
export function getRoleType(role) {
  const types = {
    // 中文：玩家侧页面
    '管理员': 'danger',
    '玩家': 'primary',
    // 英文：Admin 管理端 (玩家角色为 'ADMIN'/'PLAYER'/'BANNED')
    'ADMIN': 'danger',
    'PLAYER': 'primary',
    'BANNED': 'info'
  }
  return types[role] || 'primary'
}

// 游戏模式数字 -> 文本 (0=铁人模式, 1=娱乐模式, 2=每日挑战)
export function getGameModeText(gameMode) {
  const modes = {
    0: '铁人',
    1: '娱乐',
    2: '每日'
  }
  return modes[gameMode] || '娱乐'
}

// 前缀对象 -> 内联样式（渲染玩家称号前缀徽章）
// size: 'lg' 大号展示(详情页) / 'md' 常规卡片 / 'xs' 小号（如聊天消息）
export function getPrefixStyle(prefix, size = 'md') {
  const sizes = {
    lg: { padding: '8px 16px', borderRadius: '8px', fontSize: '1.25rem', marginRight: '0' },
    xs: { padding: '1px 4px', borderRadius: '3px', fontSize: '10px', marginRight: '3px' },
    md: { padding: '2px 8px', borderRadius: '4px', fontSize: '12px', marginRight: '4px' }
  }
  const s = sizes[size] || sizes.md
  return {
    color: prefix?.color || '#ffffff',
    backgroundColor: prefix?.backgroundColor || 'rgba(139, 92, 246, 0.8)',
    padding: s.padding,
    borderRadius: s.borderRadius,
    fontSize: s.fontSize,
    marginRight: s.marginRight,
    fontWeight: 'bold',
    display: 'inline-block'
  }
}