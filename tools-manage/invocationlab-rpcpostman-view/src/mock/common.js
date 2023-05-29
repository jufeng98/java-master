export default {
    getAllZk:(params)=>{
        console.log("mock接收getAllZk",params);
        return {
            code: 0,
            error:'服务超时',
            data: ["127.0.0.1:8080","127.0.0.1:8081"]
        }
    },
}