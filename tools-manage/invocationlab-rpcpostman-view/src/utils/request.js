import axios from 'axios'
import { Message, MessageBox } from 'element-ui'
import Vue from "vue";

// 创建axios实例
const service = axios.create({
  baseURL: process.env.BASE_API, // api 的 base_url
  timeout: 1200*10*1000, // 请求超时时间10min
  headers:{"ajax-type":true}
});

// response 拦截器
service.interceptors.response.use(
  response => {
      console.log(response.headers);
      let session_timeout = response.headers["ajax-header"];
      if (session_timeout) {
          MessageBox.confirm(
              '你已被登出，可以取消继续留在该页面，或者重新登录',
              '确定登出',
              {
                  confirmButtonText: '重新登录',
                  cancelButtonText: '取消',
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
      console.log("错误类型:",typeof error);
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
