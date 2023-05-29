function send(req){
    var resultObj = sender.invoke(req);
    var sendOk = resultObj.code == 0;
    if(sendOk){
        var resultData = resultObj.data;
        return resultData;
    }else{
        return resultObj.error;
    }
}