/**
 * @author yudong
 * @date 2023/3/3
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
    baseURL: import.meta.env.VITE_BASE_API, 
    timeout: 6000,
    headers: { "ajax-type": true }
});

// response 拦截器
service.interceptors.response.use(
    response => {
        if (response.data.code === 1) {
            ElMessage({
                type: "error",
                message: response.data.error,
                duration: 3000,
                showClose: true
            });
        }
        return response
    },
    error => {
        console.log('服务错误', error);
        ElMessage({
            message: error.toString(),
            type: 'error',
            duration: 3000
        });
        return Promise.reject(error)
    }
)

export default service
