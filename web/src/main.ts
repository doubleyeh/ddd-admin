import { createApp, watch } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './store'
import './assets/tailwind.css'

const app = createApp(App)
app.use(pinia).use(router).mount('#app')
