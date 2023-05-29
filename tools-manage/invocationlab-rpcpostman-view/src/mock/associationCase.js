export default {
    saveAssociationCase:(data)=>{
        console.log("mock接收saveAssociationCase",data);
        return {
            code: 0,
            error:'',
            data:true
        }
    },
    getAllAssociationName:(params)=>{
        console.log("mock接收getAllAssociationName",params);
        return {
            code: 0,
            error:'',
            data:["ABC","123"]
        }
    },
    getAssociationCase:(params)=>{
        console.log("mock接收getAssociationCase",params);
        return {
            code: 0,
            error:'',
            data:{
                sceneScript:'var content = reqs[0].body;\n' +
                    'rst.put("content",content);\n' +
                    'var requestObj = JSON.parse(content);\n' +
                    'requestObj.type = 1;\n' +
                    'reqs[0].body = JSON.stringify(requestObj);\n' +
                    'var result = sender.send(reqs[0]);\n' +
                    'var obj = JSON.parse(result);\n' +
                    'var code = obj.data;\n' +
                    'rst.put("result",result);\n' +
                    'rst.put("code",code);',
                caseDtoList:[{
                    caseName:'a',
                    groupName:'test',
                    zkAddress:'10.0.1.1:990',
                    serviceName:'test-service',
                    providerName:'provider-name',
                    className:'provider-name',
                    methodName:'method-name',
                    requestValue:'{"sdgg":242,"nmae":"gwegt"}',
                    testScript:'{"sdgg":242,"nmae":"gwegt"}'
                }]
            }

        }
    },
    deleteAssociationCase:(params)=>{
        console.log("mock接收deleteAssociationCase",params);
        return {
            code: 0,
            error:'',
            data:"ok"
        }
    },
}