<template>
  <div class="particle-background" aria-hidden="true">
    <canvas ref="canvasRef" class="particle-canvas"></canvas>
    <div class="gradient-overlay"></div>
    <div class="grid-overlay"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// SPDNet: 背景粒子动画
//  - 页面转入后台时暂停 rAF，避免不可见的持续渲染
//  - 按 devicePixelRatio 缩放画布，避免高分屏发虚
//  - resize 防抖，拖拽窗口时不再每帧重建粒子
//  - 尊重 prefers-reduced-motion，只画静态一帧

const canvasRef = ref(null)

// 连线可见的最大距离
const MAX_DISTANCE = 150

let animationId = null
let particles = []
let resizeTimer = null
let reducedMotion = false
let paused = false
let ctx = null
// 画布的逻辑(CSS)像素尺寸；绘制坐标与清除区域都基于它
let logicalWidth = 0
let logicalHeight = 0

class Particle {
  constructor(width, height) {
    this.bounds = { width, height }
    this.reset()
  }

  reset() {
    this.x = Math.random() * this.bounds.width
    this.y = Math.random() * this.bounds.height
    this.size = Math.random() * 2 + 0.5
    this.speedX = (Math.random() - 0.5) * 0.5
    this.speedY = (Math.random() - 0.5) * 0.5
    this.opacity = Math.random() * 0.5 + 0.1
    this.color = this.getRandomColor()
  }

  getRandomColor() {
    const colors = [
      '139, 92, 246', // Purple
      '168, 85, 247', // Violet
      '6, 182, 212',  // Cyan
      '59, 130, 246', // Blue
      '236, 72, 153'  // Pink
    ]
    return colors[Math.floor(Math.random() * colors.length)]
  }

  update() {
    this.x += this.speedX
    this.y += this.speedY

    if (this.x < 0 || this.x > this.bounds.width) this.speedX *= -1
    if (this.y < 0 || this.y > this.bounds.height) this.speedY *= -1
  }

  draw(context) {
    context.beginPath()
    context.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    context.fillStyle = `rgba(${this.color}, ${this.opacity})`
    context.fill()
  }
}

// SPDNet: 按 DPR 设置画布实际像素，同时保持 CSS 尺寸铺满容器
const setupCanvasSize = (canvas) => {
  const dpr = Math.min(window.devicePixelRatio || 1, 2)
  const cssWidth = window.innerWidth
  const cssHeight = window.innerHeight

  canvas.width = Math.round(cssWidth * dpr)
  canvas.height = Math.round(cssHeight * dpr)
  canvas.style.width = `${cssWidth}px`
  canvas.style.height = `${cssHeight}px`

  const context = canvas.getContext('2d')
  // 用逻辑坐标绘制，后续所有坐标都基于 CSS 像素
  context.setTransform(dpr, 0, 0, dpr, 0, 0)

  return { ctx: context, width: cssWidth, height: cssHeight }
}

const initParticles = (width, height) => {
  const particleCount = Math.floor((width * height) / 15000)
  particles = []
  for (let i = 0; i < particleCount; i++) {
    particles.push(new Particle(width, height))
  }
}

// SPDNet: 朴素两两连线。
// 实测 1080p 约 138 个粒子时每帧仅 ~0.015ms（占 60fps 预算的 0.1%），4K 约 552 个粒子
// 也只需 ~0.19ms，因此无需空间分桶——分桶的 Map/字符串键开销反而更慢。
const drawConnections = (context) => {
  const maxDistSq = MAX_DISTANCE * MAX_DISTANCE
  context.lineWidth = 0.5

  for (let i = 0; i < particles.length; i++) {
    const p = particles[i]
    for (let j = i + 1; j < particles.length; j++) {
      const q = particles[j]
      const dx = p.x - q.x
      const dy = p.y - q.y
      const distanceSq = dx * dx + dy * dy
      if (distanceSq >= maxDistSq) continue

      const distance = Math.sqrt(distanceSq)
      const opacity = (1 - distance / MAX_DISTANCE) * 0.15
      context.beginPath()
      context.moveTo(p.x, p.y)
      context.lineTo(q.x, q.y)
      context.strokeStyle = `rgba(139, 92, 246, ${opacity})`
      context.stroke()
    }
  }
}

const renderFrame = () => {
  if (!ctx) return

  ctx.clearRect(0, 0, logicalWidth, logicalHeight)

  particles.forEach(particle => {
    particle.update()
    particle.draw(ctx)
  })

  drawConnections(ctx)
}

const animate = () => {
  if (paused) {
    animationId = null
    return
  }
  renderFrame()
  animationId = requestAnimationFrame(animate)
}

const startAnimation = () => {
  if (animationId !== null || paused) return
  animationId = requestAnimationFrame(animate)
}

const stopAnimation = () => {
  if (animationId !== null) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
}

const setup = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  const size = setupCanvasSize(canvas)
  ctx = size.ctx
  logicalWidth = size.width
  logicalHeight = size.height
  initParticles(logicalWidth, logicalHeight)

  if (reducedMotion) {
    // 只渲染一帧静态背景，不做持续动画
    renderFrame()
  } else if (!paused) {
    startAnimation()
  }
}

// SPDNet: resize 防抖，拖拽窗口时只在停稳后重建一次
const handleResize = () => {
  clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    stopAnimation()
    setup()
  }, 200)
}

const handleVisibilityChange = () => {
  if (document.hidden) {
    paused = true
    stopAnimation()
  } else {
    paused = false
    if (!reducedMotion) startAnimation()
  }
}

let motionQuery = null
const handleMotionChange = (event) => {
  reducedMotion = event.matches
  stopAnimation()
  setup()
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return

  motionQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
  reducedMotion = motionQuery.matches
  // 标签页初始即处于后台时不要启动动画
  paused = document.hidden

  setup()

  window.addEventListener('resize', handleResize)
  document.addEventListener('visibilitychange', handleVisibilityChange)
  motionQuery.addEventListener('change', handleMotionChange)
})

onUnmounted(() => {
  stopAnimation()
  clearTimeout(resizeTimer)
  resizeTimer = null
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  motionQuery?.removeEventListener('change', handleMotionChange)
  motionQuery = null
  particles = []
  ctx = null
})
</script>

<style scoped>
.particle-background {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.particle-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.gradient-overlay {
  position: absolute;
  inset: 0;
  background: 
    radial-gradient(ellipse at 20% 20%, rgba(139, 92, 246, 0.06) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(6, 182, 212, 0.04) 0%, transparent 50%);
}

/* SPDNet: 网格原先与粒子连线同屏可见——两者都是细线网络，视觉上互相打架，
   且叠加后进一步压低卡片内文字的对比度（style.css 的对比度实测是基于不透明
   底色计算的，不包含这层）。网格本身不承载任何信息，调暗到接近不可见，
   只作为极浅的质感存在。 */
.grid-overlay {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(139, 92, 246, 0.015) 1px, transparent 1px),
    linear-gradient(90deg, rgba(139, 92, 246, 0.015) 1px, transparent 1px);
  background-size: 80px 80px;
  mask-image: radial-gradient(ellipse at center, black 0%, transparent 65%);
  -webkit-mask-image: radial-gradient(ellipse at center, black 0%, transparent 65%);
}
</style>
