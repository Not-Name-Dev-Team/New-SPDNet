// SPDNet: 提供随时间自动推进的"当前时间"引用。
//
// 背景：formatTimeAgo 在模板里被直接调用（如 {{ formatTimeAgo(game.endTime) }}），
// 它是纯函数、对时间不具响应性。页面打开后不再重渲染，于是"刚刚"会一直停在"刚刚"、
// "3 分钟前"也永远不涨——在玩家页这种会被长期挂着的页面上尤其明显。
//
// 用法：在组件中 const now = useNow()，并在调用处把它作为依赖传入，
// 例如 formatTimeAgo(t, now.value)，这样每分钟自动重新求值。

import { ref, onMounted, onUnmounted } from 'vue'

// SPDNet: 一分钟一次即可——formatTimeAgo 的最小单位就是分钟，
// 更高频率只会白白触发重渲染。
const TICK_MS = 60 * 1000

/**
 * 返回一个每分钟自增的响应式时间戳（毫秒）。
 * 同一组件内多处使用共享同一个定时器。
 *
 * 页面转入后台时暂停计时，回到前台立即校准一次：
 * 后台期间 setInterval 在部分浏览器会被节流甚至冻结，
 * 恢复时直接取真实时间，避免显示一个落后的"现在"。
 */
export function useNow() {
  const now = ref(Date.now())
  let timer = null

  const stop = () => {
    if (timer !== null) {
      clearInterval(timer)
      timer = null
    }
  }

  const start = () => {
    if (timer !== null) return
    timer = setInterval(() => {
      now.value = Date.now()
    }, TICK_MS)
  }

  const handleVisibilityChange = () => {
    if (document.hidden) {
      stop()
    } else {
      // 校准到真实时间，再恢复计时
      now.value = Date.now()
      start()
    }
  }

  onMounted(() => {
    // 标签页初始即处于后台时不必空转
    if (typeof document === 'undefined' || !document.hidden) start()
    if (typeof document !== 'undefined') {
      document.addEventListener('visibilitychange', handleVisibilityChange)
    }
  })

  onUnmounted(() => {
    stop()
    if (typeof document !== 'undefined') {
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    }
  })

  return now
}
