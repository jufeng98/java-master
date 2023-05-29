export default {
    batchCaseRun:(data)=>{
        console.log("mock接收batchCaseRun",data);
        return {
            code: 0,
            error:'',
            data:{
                "test1":true,
                "name":"test",
                "obj":{
                    "name":"obj"
                }
            }
        }
    },
}