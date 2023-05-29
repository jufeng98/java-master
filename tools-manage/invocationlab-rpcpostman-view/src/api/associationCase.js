import request from '@/utils/request'

export function saveAssociationCase(data) {

    return request({
        url:'/dubbo-postman/case/scene/save',
        method:'post',
        data
    })
}

export function deleteAssociationCase(params) {

    return request({
        url:'/dubbo-postman/case/scene/delete',
        method:'get',
        params
    })
}

export function getAllAssociationName(params) {
    return request({
        url:'/dubbo-postman/case/scene-name/list',
        method:'get',
        params
    })
}


export function getAssociationCase(params) {
    return request({
        url:'/dubbo-postman/case/scene/get',
        method:'get',
        params
    })
}