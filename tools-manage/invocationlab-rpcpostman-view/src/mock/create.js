export default {
    upload:(params)=>{
        console.log("mock接收upload",params);
        return {
            code: 0,
            error:'',
            data:{}
        }
    },
    refresh:(params)=>{
        console.log("mock接收refresh",params);
        return {
            code: 0,
            error:'超时',
            data:{}
        }
    },
    getZkServices:(params)=>{
        console.log("mock接收getZkServices",params);
        return {
            code: 0,
            error:'服务超时',
            data:["abc","def","sdb"]
        }
    },
}