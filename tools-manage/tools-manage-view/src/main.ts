import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import App from './App.vue'
import router from './router'
import request from './common/request'

import 'element-plus/dist/index.css'
import './assets/main.css'

const app = createApp(App)
app.config.errorHandler = (err) => {
    console.log(err);

}
app.config.globalProperties.$request = request

app.use(router)
app.use(ElementPlus)
app.mount('#app')
