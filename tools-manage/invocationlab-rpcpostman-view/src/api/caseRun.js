import request from '@/utils/request'

export function batchCaseRun(data) {

    return request({
        url:'/dubbo-postman/case/scene/run',
        method:'post',
        data
    })
}