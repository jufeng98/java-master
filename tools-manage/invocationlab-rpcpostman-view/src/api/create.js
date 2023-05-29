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

export function refresh(params) {
    return request({
        url:'/dubbo-postman/refresh',
        method:'get',
        params
    })
}