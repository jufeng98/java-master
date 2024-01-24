import axios from 'axios'
import { Message, MessageBox } from 'element-ui'
import Vue from "vue";
import AppUtils from "@/utils/AppUtils";

// 创建axios实例
const service = axios.create({
    baseURL: AppUtils.isProEnv() ? window.location.origin : process.env.BASE_API, // api 的 base_url
    timeout: 300000, // 请求超时时间
    headers: { "ajax-type": true }
});

// response 拦截器
service.interceptors.response.use(
    response => {
        console.log(response.headers);
        let session_timeout = response.headers["ajax-header"];
        if (session_timeout) {
            MessageBox.confirm(
                '你已被登出，重新登录',
                '确定登出',
                {
                    confirmButtonText: '重新登录',
                    type: 'warning'
                }
            ).then(() => {
                window.location = "/logout";
            }).catch(() => {
                console.log("catch,session过期留在当前页面")
            })
            return;
        }
        if (response.data.code === 1) {
            Vue.prototype.$message({
                type: "error",
                message: response.data.error,
                duration: 6000,
                showClose: true
            });
        }
        return response
    },
    error => {
        console.log("错误类型:", typeof error);
        console.log('服务错误:' + error);// for debug

        Message({
            message: error.toString(),
            type: 'error',
            duration: 5 * 1000
        });
        return Promise.reject(error)
    }
)

export default service
