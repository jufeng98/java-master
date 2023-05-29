import request from '@/utils/request'

export function addConfig(params) {
    return request({
        url:'/dubbo-postman/new/config',
        method:'get',
        params
    })
}

export function deleteZk(params) {
    return request({
        url:'/dubbo-postman/zk/del',
        method:'get',
        params
    })
}

export function configs(params) {
    return request({
        url:'/dubbo-postman/configs',
        method:'get',
        params
    })
}
