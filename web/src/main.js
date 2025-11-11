import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './store'
import '@/styles/index.css'
import 'nprogress/nprogress.css'
import './permission'
import '@/styles/theme.css'

const app = createApp(App)
app.use(pinia)
app.use(router)
app.mount('#app')
