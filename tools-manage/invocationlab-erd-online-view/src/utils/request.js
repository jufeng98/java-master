/**
 * request 网络请求工具
 * 更详细的api文档: https://bigfish.alipay.com/doc/api#request
 */
import { extend } from 'umi-request';
import { message } from 'antd';
import * as cache from "./cache";
import { CONSTANT } from "@/utils/constant";
import { history } from '@@/exports';
import moment from "moment";

const codeMessage = {
  200: '服务器成功返回请求的数据。',
  201: '新建或修改数据成功。',
  202: '一个请求已经进入后台排队（异步任务）。',
  204: '删除数据成功。',
  400: '发出的请求有错误，服务器没有进行新建或修改数据的操作。',
  401: '用户没有权限（令牌、用户名、密码错误）。',
  403: '当前用户权限不够，无法操作此功能。',
  404: '发出的请求针对的是不存在的记录，服务器没有进行操作。',
  406: '请求的格式不可得。',
  410: '请求的资源被永久删除，且不会再得到的。',
  422: '当创建一个对象时，发生一个验证错误。',
  500: '服务器发生错误，请联系管理员。',
  502: '网关错误。',
  503: '服务不可用，服务器暂时过载或维护。',
  504: '网关超时。',
};

/**
 * 异常处理程序
 */
const errorHandler = error => {
  console.log(34, 'error', error)
  const { response = {} } = error;
  if (!response) {
    if (error) {
      return;
    }
    message.error('请求未响应');
    return;
  }
  const errorText = codeMessage[response.status] || response.statusText;
  const { status, url } = response;

  if (status === 400) {
    message.error(errorText);
    return;
  }
  if (status === 401) {
    history.push("/login");
    return;
  }
  // environment should not be used
  if (status === 403) {
    message.error(errorText);
    return;
  }
  if (status <= 504 && status > 500) {
    message.error(errorText);
    return;
  }
  if (status >= 404 && status < 422) {
    message.error(errorText);
  }
  message.error("" + error);
  Promise.resolve("" + error)
};

export const BASE_URL = window.location.origin.startsWith("http://10") ? window.location.origin : (window._env_.API_URL || API_URL);

/**
 * 配置request请求时的默认参数
 */
const request = extend({
  prefix: BASE_URL,
  errorHandler, // 默认错误处理
  timeout: 30000,
});


request.interceptors.request.use((url, options) => {
  // let params = (new URL(document.location)).searchParams;
  // let projectId = params.get(CONSTANT.PROJECT_ID);
  if (url.indexOf('/oauth/token') < 0) {
    // const authorization = cache.getItem('Authorization');
    const projectId = cache.getItem(CONSTANT.PROJECT_ID);
    // if (authorization) {
    options.headers = {
      ...options.headers,
      projectId: projectId,
      // 'Authorization': `Bearer ${authorization}`
    }
    return (
      {
        options: {
          ...options,
          interceptors: true,
        },
      }
    );
    // }
  } else {
    options.headers = {
      ...options.headers,
      'Authorization': 'Basic Y2xpZW50MjoxMjM0NTY='
    }
    return (
      {
        options: {
          ...options,
          interceptors: true,
        },
      }
    );
  }
});


// clone response in response interceptor
request.interceptors.response.use(async (response, options) => {
  if (response.status !== 200) {
    return response;
  }
  if (options.responseType === 'blob') {
    return response;
  }
  const data = await response.clone().json();
  if (data) {
    const { code, msg } = data;
    if (code && code === 10003) {
      setTimeout(() => {
        let loginUrl = '/invocationlab-erd-online-view/login'
        if (window.location.href.includes(loginUrl)) {
          return
        }
        window.location = loginUrl
      }, 3000);
      message.warning(msg);
      return
    }

    if (code && code === 405) {
      return response;
    }

    if (code && code !== 200) {
      const errorText = msg || codeMessage[code];
      message.error(errorText);
    }
  }
  return response;
});

export const exportSql = (reqDataObj, setLoading, url) => {
  request.post(url, {
    method: 'post',
    data: JSON.stringify(reqDataObj),
    headers: { accept: "*/*", 'Content-Type': 'application/json' },
    responseType: 'blob',
    getResponse: true
  }).then(resData => {
    const disposition = resData.response.headers.get('content-disposition');
    if (!disposition) {
      const reader = new FileReader();
      reader.readAsText(resData.data, 'utf-8');
      reader.onload = () => {
        message.error(JSON.parse(reader.result).msg)
      }
      return
    }
    let fileName = decodeURIComponent(disposition.split(';')[2].split("=")[1]).replaceAll('"', '')

    let blob = resData.data
    let downloadElement = document.createElement('a')
    // 创建下载的链接
    let href = window.URL.createObjectURL(blob);
    downloadElement.href = href;
    // 下载后文件名
    downloadElement.download = fileName;
    document.body.appendChild(downloadElement);
    // 点击下载
    downloadElement.click();
    // 下载完成移除元素
    document.body.removeChild(downloadElement);
    // 释放blob对象
    window.URL.revokeObjectURL(href);
  })
    .finally(() => setLoading(false))
}

export const logout = () => {
  cache.setItem(CONSTANT.PROJECT_ID, "");
  request.post('/auth/oauth/logout')
    .then(res => {
      message.success(res.data);
      cache.clear()
      history.push("/login");
    })
}

export default request;
