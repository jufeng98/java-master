export default {
    addConfig:(params)=>{
        console.log("mock接收addConfig",params);
        return {
            code: 0,
            error:'服务超时',
            data: 'ok'
        }
    },
    deleteZk:(params)=>{
        console.log("mock接收deleteZk",params);
        return {
            code: 0,
            error:'服务超时',
            data: 'ok'
        }
    },
    configs:(params)=>{
        console.log("mock接收configs",params);
        return {
            code: 0,
            error:'服务超时',
            data: ["127.0.0.1:8081,127.0.0.1:8081,127.0.0.1:8081,127.0.0.1:8081","127.0.0.1:8082"]
        }
    },
}
