// https://umijs.org/config/
import { defineConfig } from '@umijs/max';

export default defineConfig({
  publicPath: '/invocationlab-erd-online-view/',
  define: {
    API_URL: 'http://localhost:8001'
  },
  // Fast Refresh 热更新
  fastRefresh: true,
  title:'ERD Online',
  mfsu: {
    exclude :['@playwright/test']
  },
});
