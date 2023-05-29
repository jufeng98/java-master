import request from '@/utils/request'

export function getAllZk(params) {

    return request({
        url:'/dubbo-postman/all-zk',
        method:'get',
        params
    })
}

export function getEnv(params) {
    return request({
        url:'/dubbo-postman/env',
        method:'get',
        params
    })
}