import { createApp } from 'vue'
import 'element-plus/theme-chalk/dark/css-vars.css'
import App from './App.vue'
import router from './router'
import './style.css'

// SPDNet: 图标按视图显式 import（Element Plus 组件由 vite 按需自动导入），
// 不再全量注册 @element-plus/icons-vue，避免约 1000 个图标进入主包。
const app = createApp(App)

app.use(router)
app.mount('#app')
