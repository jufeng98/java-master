import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

import Layout from '@/views/layout/Layout'
import Create from '@/views/pages/CreateService.vue'
import Access from '@/views/pages/AccessService.vue'
import Config from '@/views/pages/SystemConfig.vue'
import RedisHelper from '@/views/pages/RedisHelper.vue'
import ElImageTouchTest from '@/views/pages/ElImageTouchTest.vue'
import CaseManage from '@/views/pages/CaseManage.vue'

export const constantRouterMap = [
    {
        path: '/redirect',
        component: Layout,
        hidden: true,
        children: [
            {
                path: '/redirect/:path*',
                component: () => import('@/views/redirect/index')
            }
        ]
    },
    {
        path: '/',
        component: Layout,
        redirect: '/access/index',
        name: 'accessService',
        hidden: true,
        children: [{
            path: 'access',
            component: Access
        }]
    },
    {
        path: '/access',
        component: Layout,
        redirect: '/access/index',
        children: [
            {
                path: 'index',
                component: Access,
                meta: { title: '访问服务', icon: 'table' },
                name: 'accessService'
            },
        ]
    },
    {
        path: '/case-manage',
        component: Layout,
        redirect: '/case-manage/index',
        children: [
            {
                path: 'index',
                component: CaseManage,
                meta: { title: '场景测试', icon: 'nested' },
                name: 'sceneManage'
            },
        ]
    },
    {
        path: '/create',
        redirect: '/create/index',
        component: Layout,
        children: [
            {
                path: 'index',
                component: Create,
                meta: { title: '创建服务', icon: 'tab' },
                name: 'createService'
            },
        ]
    },
    {
        path: '/config',
        component: Layout,
        children: [
            {
                path: 'index',
                component: Config,
                meta: { title: '注册中心', icon: 'list' },
                name: 'systemConfig'
            },
        ]
    },
    {
        path: '/redisHelper',
        component: Layout,
        children: [
            {
                path: 'index',
                component: RedisHelper,
                meta: { title: 'Redis工具', icon: 'table' },
                name: 'redisHelper'
            },
        ]
    },
    {
        path: '/elImageTouchTest',
        component: Layout,
        children: [
            {
                path: 'index',
                component: ElImageTouchTest,
                meta: { title: 'elementUI图片组件触控测试', icon: 'table' },
                name: 'elImageTouchTest'
            },
        ]
    },
    {
        path: 'external-link',
        component: Layout,
        children: [
            {
                path: 'https://github.com/everythingbest/dubbo-postman/tree/master',
                meta: { title: '使用帮助', icon: 'guide' }
            }
        ]
    },
    {
        path: '/404',
        meta: { title: '404页面'},
        component: () => import('@/views/error-page/404'),
        hidden: true
    },
    {
        path: '/401',
        meta: { title: '401页面'},
        component: () => import('@/views/error-page/401'),
        hidden: true
    },
    { path: '*', redirect: '/404', hidden: true }
]

export default new Router({
    // mode: 'history', //后端支持可开
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRouterMap
})
