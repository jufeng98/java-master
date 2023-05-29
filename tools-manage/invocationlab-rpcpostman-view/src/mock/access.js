export default {
    getTemplate: (params) => {
        console.log("mock接收getTemplate",params);
        return {
            code: 0,
            error:'服务超时',
            data: {
                "arg0":"127.0.0.1：8080",
                "name":123
            }
        }
    },

    saveHisTemplate:(params) => {
        console.log("mock接收saveHisTemplate",params);
        return {
            code: 0,
            error:'服务超时',
            data: {"code":0,"name":"sgweg"}
        }
    },

    doRequest:(params) => {
        console.log("mock接收doRequest",params);
        return  {
            actualResponse:{"test":12134,"name":"sgweg"},
            testResponse:{"test":12134,"name":"sgweg","用例1":true,"用例2":false}
        }
    },
    getAllMethods:(params) => {
        console.log("mock接收getAllMethods",params);
        return {
            code: 0,
            error:'服务超时',
            data: [
                "method1"
            ]
        }
    },
    getAllProviders:(params)=>{
        console.log("mock接收getAllProviders",params);
        return {
            code: 0,
            error:'服务超时',
            data: {
                'abc':"a/sdg",
                'def':"a/sdg",
                'sgd':"a/sdg"
            }
        }
    },
    getRegisterService:(params)=>{
        console.log("mock接收getRegisterService",params);
        return {
            code: 0,
            error:'服务超时',
            data: ['abc','def','sgd']
        }
    },
}