import request from '@/utils/request'

export function saveCase(data) {

    return request({
        url:'/dubbo-postman/case/save',
        method:'post',
        data
    })
}

export function getGroupAndCaseName(params) {
    return request({
        url:'/dubbo-postman/case/group/list',
        method:'get',
        params
    })
}

export function getAllGroupName(params) {
    return request({
        url:'/dubbo-postman/case/group-name/list',
        method:'get',
        params
    })
}

export function queryCaseDetail(params) {
    return request({
        url:'/dubbo-postman/case/detail',
        method:'get',
        params
    })
}

export function deleteDetail(params) {
    return request({
        url:'/dubbo-postman/case/delete',
        method:'get',
        params
    })
}

export function queryAllCaseDetail(params) {
    return request({
        url:'/dubbo-postman/case/group-case-detail/list',
        method:'get',
        params
    })
}