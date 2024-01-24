import request from '@/utils/request'

export function getZkServices(params) {
    return request({
        url:'/dubbo-postman/result/appNames',
        method:'get',
        params
    })
}

export function upload(params) {
    return request({
        url:'/dubbo-postman/create',
        method:'get',
        params
    })
}

export function uploadJdk(params) {
    return request({
        url:'/dubbo-postman/createJdk',
        method:'get',
        params
    })
}

export function refresh(params) {
    return request({
        url:'/dubbo-postman/refresh',
        method:'get',
        params
    })
}

export function refreshJdk(params) {
    return request({
        url:'/dubbo-postman/refreshJdk',
        method:'get',
        params
    })
}

export function delService(params) {
    return request({
        url:'/dubbo-postman/delService',
        method:'get',
        params
    })
}