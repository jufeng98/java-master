/*
  作者:yixuan
  介绍：sql语句（select语句）解析类
  邮箱:yixuan.zzq@taobao.com
*/

var Lexter = require('./lexter.js');
var Tool   = require('./sqlParseTool.js');
var util   = require('util');

/*{{{ divideUnion()*/
/**
 * 根据union划分tokens
 * @param {String} tokens sql解析出的tokens
 * @return {Array} 解析结果数组
 */
function divideUnion(tokens){

  var res = [];
  var lev = 0;
  var pre = 0;

  for(var i = 0;i < tokens.length; i++){
    var txt = tokens[i].text;
    if(txt === "("){lev++;continue;}
    if(txt === ")"){lev--;continue;}
    if(lev === 0 && /^UNION$/i.test(txt) && tokens[i].type === Lexter.types.KEYWORD){
      res.push(Tool.removeParenthese(tokens.slice(pre,i)));
      pre = i + 1;
    }
  }
  res.push(Tool.removeParenthese(tokens.slice(pre)));

  return res;

}
/*}}}*/

/*{{{ divideTokens()*/
/**
 * 将所有token分解为几个组成部分(每部分都是token数组)
 * @param {Array} tokens sql分解为的tokens
 * @return {Object} 分解结果
 */
function divideTokens(tokens){

  var posArr  = [];
  var Map     = {}
  var pre     = 0;
  var lev     = 0;

  for(var i = 0;i < tokens.length; i++){

    var txt = tokens[i].text;
    var type = tokens[i].type;

    if(txt === "("){lev++;continue;}
    if(txt === ")"){lev--;continue;}
    if(lev !== 0 || type !== Lexter.types.KEYWORD){continue;}

    if(/^SELECT$/i.test(txt)){
      if(Map["column"] !== undefined){continue;}
      posArr.push({keyword:"column",pos:i});
      Map["column"] = true;

    }else if(/^FROM$/i.test(txt)){
      if(Map["source"] !== undefined){continue;}
      posArr.push({keyword:"source",pos:i});
      Map["source"] = true;

    }else if(/^(JOIN|INNER|OUTER|LEFT|RIGHT)$/i.test(txt)){
      if(Map["joinmap"] !== undefined){continue;}
      posArr.push({keyword:"joinmap",pos:i});
      Map["joinmap"] = true;

    }else if(/^WHERE$/i.test(txt)){
      if(Map["where"] !== undefined){continue;}
      posArr.push({keyword:"where",pos:i});
      Map["where"] = true;

    }else if(/^GROUP$/i.test(txt)){
      if(Map["groupby"] !== undefined){continue;}
      posArr.push({keyword:"groupby",pos:i});
      Map["groupby"] = true;

    }else if(/^ORDER$/i.test(txt)){
      if(Map["orderby"] !== undefined){continue;}
      posArr.push({keyword:"orderby",pos:i});
      Map["orderby"] = true;

    }else if(/^LIMIT$/i.test(txt)){
      if(Map["limit"] !== undefined){continue;}
      posArr.push({keyword:"limit",pos:i});
      Map["limit"] = true;
    }

  }

  var parts = {};
  for(var i = 1; i < posArr.length; i++){
    parts[posArr[i-1].keyword] = tokens.slice(posArr[i-1].pos,posArr[i].pos);
  }
  parts[posArr[posArr.length - 1].keyword] = tokens.slice(posArr[posArr.length-1].pos);
  return parts;
}
/*}}}*/

/*{{{ parseColumn()*/
/**
 * 将tokens当做sql中column字段分解
 * （介于select和from中间的部分）
 * @param {String} tokens 需要解析的tokens
 * @return {Object} 解析结果 
 */
function parseColumn(tokens){

  var res = {};
  
  var parts = Tool.pickUp(tokens,",");
  parts[0].shift();

  for(var i = 0;i < parts.length;i++){
    var tmp = parts[i];

    /*{{{ 判断是否有distinct */
    var dist = null;
    if(/^(distinct|all|distinctrow)$/i.test(tmp[0].text)){
      tmp[0].text = tmp[0].text.toUpperCase();
      dist        = tmp[0];
      tmp.shift();
    }
    /*}}}*/

    /*{{{ 判断每个column名字并赋值 */
    var colName;
    var tmpVar;
    if(tmp[tmp.length - 1].type === Lexter.types.KEYWORD || tmp[tmp.length - 1].type === Lexter.types.VARIABLE){
      if((tmpVar = tmp[tmp.length - 1].text.indexOf(".")) === -1){
        colName = tmp[tmp.length-1].text;
        if(tmp.length === 1){
          res[colName] = {
            dist : dist,
            expr : tmp
          }
        }else if(/^as$/i.test(tmp[tmp.length-2].text)){
          res[colName] = {
            dist : dist,
            expr : tmp.slice(0,tmp.length-2)
          }
        }else{
          res[colName] = {
            dist : dist,
            expr : tmp.slice(0,tmp.length-1)
          }
        }
      }else{
        colName = tmp[tmp.length - 1].text.substr(tmpVar+1);
        res[colName] = {
          dist : dist,
          expr : tmp
        }
      }
    }else{
      colName = Tool.merge(tmp,"");
      res[colName] = {
        dist : dist,
        expr : tmp
      }
    }
    /*}}}*/

  }

  return res;

}
exports.parseColumn = parseColumn;
/*}}}*/

/*{{{ parseSource()*/
/**
 * 将tokens当做sql中source字段分解
 * （介于from和join中间的部分）
 * @param {String} tokens 需要解析的tokens
 * @return {Object} 解析结果
 */
function parseSource(tokens){

  var res = {};

  var parts = Tool.pickUp(tokens,",");
  parts[0].shift();

  for(var i = 0;i < parts.length;i++){
    var get = Tool.parseOneSource(parts[i]);
    res[get["name"]] = {
      type   : get["type"],
      source : get["source"]
    }
  }

  return res;

}
exports.parseSource = parseSource;
/*}}}*/

/*{{{ parseJoinmap()*/
/**
 * 将tokens当做sql中join字段分解
 * （介于join和where中间的部分）
 * @param {String} tokens 需要解析的tokens
 * @return {Object} 解析结果
 */
function parseJoinmap(tokens){

  var res = {}; 

  var parts   = Tool.pickUp(tokens,"JOIN");
  var forNext = undefined;

  for(var i = 0;i < parts.length; i++){
    var tmp = parts[i];

    if(tmp.length === 1){
      forNext = tmp[0].text.toUpperCase();
      continue;
    }

    var method = (forNext === undefined) ? "INNER JOIN" : forNext + " JOIN";
    var type;
    var source;
    var name;

    //如果一个部分的结尾念着下面字段，则这个字段是属于后面一个JOIN的一部分
    forNext = (/^(LEFT|RIGHT|OUTER|INNER)$/i.test(tmp[tmp.length-1].text)) ? tmp.pop().text : undefined;
    var onPos = undefined;

    /*{{{ 分析源和源类型*/
    for(var j = 0;j < tmp.length; j++){
      if(/^on$/i.test(tmp[j].text)){
        onPos   = j;
        var joinTableTokens = tmp.slice(0,j);
        var get = Tool.parseOneSource(joinTableTokens);
        name    = get["name"];
        type    = get["type"];
        source  = get["source"];
        break;
      }
    }
    /*}}}*/

    /*{{{ 分析ON后面的条件元素*/
    if(onPos === undefined){
      throw new Error("no keyword 'on' in join part");
    }

    var where = [];
    var pre   = onPos+1;
    tmp.push({text:"and",type:1});

    for(var j = pre;j < tmp.length; j++){
      if(/^and$/i.test(tmp[j].text)){
        var ele = tmp.slice(pre,j);
        where.push(Tool.parseOneWhere(ele));
        pre = j + 1;
      }
    }
    /*}}}*/

    res[name] = {
      type   : type,
      source : source,
      method : Tool.JOIN[method.toUpperCase()],
      where  : where
    }
  }

  return res;

}
exports.parseJoinmap = parseJoinmap
/*}}}*/

/*{{{ parseWhere()*/
/**
 * 将tokens当做sql中where字段分解
 * （介于where和group by中间的部分）
 * @param {String} tokens 需要解析的tokens
 * @return {Object} 解析结果
 */
function parseWhere(tokens){

  var res = [];

  var parts = Tool.pickUp(tokens,"and");
  parts[0].shift();

  parts.forEach(function(part){
    res.push(Tool.parseOneWhere(part));
  });

  return res;

}
exports.parseWhere = parseWhere;
/*}}}*/

/*{{{ parseGroupby()*/
/**
 * 将tokens当做sql中group by字段分解
 * （介于group by和order by中间的部分）
 * @param {String} tokens 需要解析的tokens
 * @return {Object} 解析结果
 */
function parseGroupby(tokens){

  var res = [];

  var parts = Tool.pickUp(tokens,",");
  parts[0].shift();
  parts[0].shift();

  parts.forEach(function(part){
    res.push(part);
  });

  return res;

}
exports.parseGroupby = parseGroupby;
/*}}}*/

/*{{{ parseOrderby()*/
/**
 * 将tokens当做sql中order by字段分解
 * （介于order by和limit中间的部分）
 * @param {String} tokens 需要解析的tokens
 * @return {Object} 解析结果
 */
function parseOrderby(tokens){

  var res = [];
  var parts = Tool.pickUp(tokens,",");
  parts[0].shift();
  parts[0].shift();

  for(var i = 0;i < parts.length; i++){
    var tmp  = parts[i];
    var type;
    var last = tmp[tmp.length-1].text.toUpperCase();
    if(/^(ASC|DESC)$/i.test(last)){
      type = Tool.ORDER[last];
      tmp.pop();
    }else{
      type = Tool.ORDER["ASC"];
    }
    if(tmp.length === 0){continue;}
    res.push({
      type : type,
      expr : tmp
    });
  }

  return res;

}
exports.parseOrderby = parseOrderby;
/*}}}*/

/*{{{ parseLimit()*/
/**
 * 将tokens当做sql中limit字段分解
 * （limit部分）
 * @param {String} tokens 需要解析的tokens
 * @return {Object} 解析结果
 */
function parseLimit(tokens){

  var res   = [];
  var parts = Tool.pickUp(tokens,",");
  parts[0].shift();

  for(var i = 0;i < parts.length; i++){
    var tmp = parts[i];

    if(tmp.length >= 2){
      throw new Error("something wrong in 'limit' part");
    }

    res.push(tmp[0]);
  }
  if(res.length === 1){
    res.unshift({text:0,type:2});
  }

  return res;

}
exports.parseLimit = parseLimit;
/*}}}*/

/*{{{ createObj()*/
/**
 * 创建sql对应的sql对象，可以选择解析部分
 * @param {String} sql 需要分析的sql
 * @return {Object} 结果对象
 */
exports.createObj= function(sql){

  var result = [];

  var tokens      = Lexter.create(sql).getAll();
  var tokenGroups = divideUnion(tokens);


  for(var i = 0;i < tokenGroups.length; i++){
    var res = {};
    var parts = divideTokens(tokenGroups[i]);

    var hint = Tool.getHint(parts['column'],1);
    if(hint){res.hint = hint;}
    if(parts["column"]){res.column   = parseColumn(parts["column"]);}
    if(parts["source"]){res.source   = parseSource(parts["source"]);}
    if(parts["joinmap"]){res.joinmap = parseJoinmap(parts["joinmap"]);}
    if(parts["where"]){res.where = parseWhere(parts["where"]);}
    if(parts["groupby"]){res.groupby = parseGroupby(parts["groupby"]);}
    if(parts["orderby"]){res.orderby = parseOrderby(parts["orderby"]);}
    if(parts["limit"]){res.limit = parseLimit(parts["limit"]);}
    result.push(res);
  }

  return result;

}
/*}}}*/

