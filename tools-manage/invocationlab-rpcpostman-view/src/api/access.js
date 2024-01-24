import request from '@/utils/request'

export function getRegisterService(params) {
    return request({
        url:'/dubbo-postman/result/serviceNames',
        method:'get',
        params
    })
}

export function getDependencyList(params) {
    return request({
        url:'/dubbo-postman/result/serviceNamesJdk',
        method:'get',
        params
    })
}

export function getAllMethods(params) {
    return request({
        url:'/dubbo-postman/result/interface',
        method:'get',
        params
    })
}

export function getArgs(params) {
    return request({
        url:'/dubbo-postman/args',
        method:'get',
        params
    })
}

export function getAllProviders(params) {
    return request({
        url:'/dubbo-postman/result/interfaceNames',
        method:'get',
        params
    })
}

export function getTemplate(params) {
    return request({
        url:'/dubbo-postman/result/interface/method/param',
        method:'get',
        params
    })
}

export function getRemoteHistoryTemplate(params) {
    return request({
        url:'/dubbo-postman/result/template/names',
        method:'get',
        params
    })
}

export function getRemoteAssignedTemplate(params) {
    return request({
        url:'/dubbo-postman/result/template/get',
        method:'get',
        params
    })
}

export function doRequest(params) {
    return request({
        url:'/dubbo',
        method:'post',
        data:params
    })
}

export function saveHisTemplate(params) {
    return request({
        url:'/dubbo-postman/result/template/save',
        method:'get',
        params
    })
}