// SPDNet: 前端共享格式化/显示工具函数
// 该模块收敛各视图重复实现的前缀样式、角色标签、游戏模式文本逻辑
// 说明：其余夹具(角色/游戏模式)映射需与后端保持一致

// SPDNet: 角色 -> Element Plus tag 类型
// 兼容三种取值：后端枚举名（USER/ADMIN/BANNED）、中文显示名（登录接口）、以及历史遗留的 'PLAYER'
//
// 重要（踩坑记录）：后端 UserRole 枚举的常量名是 USER，不是 PLAYER
//   （UserRole.java: USER("玩家"), ADMIN("管理员"), BANNED("封禁")）
// 而 Admin 接口返回的是 player.getRole().name()，即 'USER'。
// 早期前端误以为是 'PLAYER'，导致 'USER' 落空、角色列原样显示英文。此处以 USER 为准，
// 同时保留 'PLAYER' 键，兼容可能存在的旧缓存/旧数据，避免再次漂移。
export function getRoleType(role) {
  const types = {
    // 后端枚举名（Admin 管理端）
    'ADMIN': 'danger',
    'USER': 'primary',
    'BANNED': 'info',
    // 中文显示名（玩家侧页面 / 登录接口返回 getDisplayName()）
    '管理员': 'danger',
    '玩家': 'primary',
    '已封禁': 'info',
    '封禁': 'info',
    // 历史遗留：曾用 PLAYER 表示普通玩家
    'PLAYER': 'primary'
  }
  return types[role] || 'primary'
}

// SPDNet: 角色 -> 中文显示名（Admin 表格复用，避免各处自建映射表）
// 兼容已经是中文显示名的情况，直接原样返回
export function getRoleDisplay(role) {
  const displays = {
    'ADMIN': '管理员',
    'USER': '玩家',
    'BANNED': '已封禁',
    // 历史遗留：曾用 PLAYER 表示普通玩家
    'PLAYER': '玩家'
  }
  return displays[role] || role
}

// SPDNet: 统一的"是否管理员"判定。
// 后端登录返回的是 role.getDisplayName()（中文'管理员'），但路由守卫同时兼容英文枚举，
// 此处收敛为唯一实现，避免各页面各写一套导致守卫放行、页面又拒绝的不一致。
export function isAdminUser(user) {
  const role = user?.role
  return role === 'ADMIN' || role === '管理员'
}

// SPDNet: 本地时区的 YYYY-MM-DD。
// 不能用 toISOString()——它按 UTC 取日期，在 UTC+8 的 00:00-08:00 会得到"昨天"。
export function toLocalDateString(date = new Date()) {
  const d = date instanceof Date ? date : new Date(date)
  if (isNaN(d.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
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

// ============================================
// 时间格式化（各视图曾各自复制一份，此处收敛为唯一实现）
// ============================================

// 完整本地时间文本
export function formatDateTime(time) {
  if (!time) return '-'
  const date = new Date(time)
  if (isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN')
}

// 相对时间："3 分钟前"
// SPDNet: 可选的 nowMs 参数用于让调用方注入一个响应式时间源（见 utils/useNow.js）。
// 不传时退化为 Date.now()，保持原有的纯函数用法不变。
export function formatTimeAgo(time, nowMs) {
  if (!time) return '-'
  const date = new Date(time)
  if (isNaN(date.getTime())) return '-'

  const diff = (nowMs ?? Date.now()) - date.getTime()
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

// 完整时间 + 括号内相对时间
export function formatDateTimeWithAgo(time, nowMs) {
  if (!time) return '-'
  return `${formatDateTime(time)} (${formatTimeAgo(time, nowMs)})`
}

// 仅日期（月/日 + 时分），用于管理端表格
export function formatShortDateTime(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}