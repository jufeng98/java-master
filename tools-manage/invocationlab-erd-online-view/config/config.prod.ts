// https://umijs.org/config/
import {defineConfig} from '@umijs/max';

export default defineConfig({
  publicPath: '/invocationlab-erd-online-view/',
  define: {
    API_URL: 'http://192.168.241.106:8083'
  },
  // 打包时移除 console
  extraBabelPlugins: ['transform-remove-console'],

});
