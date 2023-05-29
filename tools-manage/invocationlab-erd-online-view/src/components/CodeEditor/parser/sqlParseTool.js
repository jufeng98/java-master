var Lexter = require('./lexter.js');

/*{{{ RELATE*/
var RELATE = {
  "=": 1,
  ">": 2,
  ">=": 3,
  "<": 4,
  "<=": 5,
  "<>": 6,
  "!=": 6,
  "in": 7,
  "not in": 8,
  "like": 9,
  "not like": 10,
  "between": 11,
  "is null": 20,
  "not null": 21,
};
exports.RELATE = RELATE;
/*}}}*/

/*{{{ ORDER*/
var ORDER = {
  'ASC' : 1,
  'DESC' : 2
}
exports.ORDER = ORDER;
/*}}}*/

/*{{{ JOIN*/
var JOIN = {
  'INNER JOIN' : 1,
  'OUTER JOIN' : 2,
  'LEFT JOIN'  : 3,
  'RIGHT JOIN' : 4,
}
exports.JOIN = JOIN;
/*}}}*/

/*{{{ removeParenthese()*/
/**
 * 过滤最外面的括号对
 * @param {Array} tokens
 * @return {Array}
 */
function removeParenthese(tokens){
  if(tokens[0] === undefined || tokens[0].text !== "("){
    return tokens;
  }
  var lev = 0;
  for(var i = 0;i < tokens.length; i++){
    if(tokens[i].text === "("){lev++;}
    if(tokens[i].text === ")"){lev--;}
  }
  if(lev === 0 && tokens[tokens.length-1].text === ")"){
    tokens.pop();
    tokens.shift();
    return removeParenthese(tokens);
  }
  throw new Error("lack parenthese");
}
exports.removeParenthese = removeParenthese;
/*}}}*/

/*{{{ merge()*/
/**
 * 拼合一组token的text字段
 * @param {Array} parts token组成的数组
 * @param {String} sep 连接分隔符
 * @return {String}
 */
function merge(parts,sep){
  sep = (sep === undefined) ? " " : sep;
  var res = "";
  for(var i = 0;i < parts.length; i++){
    res += (parts[i].text + sep);
  }
  return res.substr(0,res.length - sep.length);
}
exports.merge = merge;
/*}}}*/

/*{{{ pickUp()*/
/**
 * 将str中的内容按照分隔符提取成数组，类似Array里的split
 * @param {String} str 待分割的字符串
 * @param {String} sep 分隔符
 * @return {Array}
 */
function pickUp(tokens,sep){
  tokens.push({text:sep});
  var res = [];
  var pre = 0;
  var lev = 0;
  for(var i = 0;i < tokens.length; i++){
    if(tokens[i].text === "("){lev++;}
    if(tokens[i].text === ")"){lev--;}
    if(new RegExp("^"+sep+"$","i").test(tokens[i].text) && lev === 0){
      var part = tokens.slice(pre,i);
      if(part.length === 0){
        pre = i + 1;
        continue;
      }
      res.push(part);
      pre = i + 1;
    }
  }
  return res;
}
exports.pickUp = pickUp;
/*}}}*/

/*{{{ getHint()*/
/**
 * 获得hint信息 
 * @param {array} part 可能包含hint的部分
 * @param {int}   pos  hint出现的位置
 * @return {obj||undefined} 返回信息对象或者Undefined
 */
function getHint(part,pos){
  var hint = undefined;
  if(part && part[pos] && part[pos].type === Lexter.types.COMMENT){
    hint = part[pos];
    var tmp = part.slice(0,pos);
    for(var i = 0;i <= pos; i++){
      part.shift();
    }
    while(tmp.length > 0){
      part.unshift(tmp.pop());
    }
  }
  return hint;
}
exports.getHint = getHint;
/*}}}*/

/*{{{ parseOneSource()*/
/**
 * 解析一个源
 * @param {Object} part 由几个token表示的一个源
 * @return {Object} 这个源的解析结果
 */
function parseOneSource(part){
  var name;
  var type;
  var source;
  var res;
  var idx = part[0].text.indexOf(".");
  //确定源种类
  type = part[0].text.substr(0,idx);

  /*{{{ 确定具体源*/
  //例如from字段跟的是(select * from table)这样的子表
  if(idx === -1){
    if(part[0].text === "("){
      var innerLev = 1;
      for(var j = 1;j < part.length; j++){
        if(part[j].text === "("){innerLev++;}
        if(part[j].text === ")"){innerLev--;}
        if(innerLev === 0){
          source = merge(part.slice(1,j)," ");
          part = part.slice(j+1,part.length);
          break;
        }
      }
    }else{
      source = part[0].text;
      part.shift();
    }
    //例如sql.()这样的源
  }else if(idx === part[0].text.length - 1){
    if(part[1].text !== "("){throw new Error("something wrong in 'source' part");}
    var innerLev = 0;
    var j;
    for(j = 1;j < part.length; j++){
      if(part[j].text === "("){innerLev++;}
      if(part[j].text === ")"){innerLev--;}
      if(innerLev === 0){
        source = merge(part.slice(2,j)," ");
        part = part.slice(j+1,part.length);
        break;
      }
    }
    if(j === part.length){throw new Error("lack parentheses in 'source' part");}
    //列入db.table这样的源
  }else{
    source = part[0].text.substr(idx+1);
    part.shift();
  }
  /*}}}*/

  /*{{{ 确定源名字，并设置返回对象*/
  if(part.length === 0){
    res = {
      name : source,
      type : type,
      source : source
    }
  }else{
    if(part.length >= 3 || (part.length === 2 && !(/^as$/i.test(part[0].text))) || (part[part.length-1].type !== Lexter.types.KEYWORD && part[part.length-1].type !== Lexter.types.VARIABLE)){throw new Error("something wrong in 'source' part");}
    res = {
      name : part[part.length-1].text,
      type : type,
      source : source
    }
  }
  /*}}}*/

  return res;

}
exports.parseOneSource = parseOneSource;
/*}}}*/

/*{{{ parseOneWhere()*/
/**
 * 解析一个条件
 * @param {Object} part 由几个token表示的一个条件
 * @return {Object} 这个条件的解析结果
 */
function parseOneWhere(part){
  var res = {};
  var relate = part[1];
  res["column"] = part[0];
  if(!relate){
    return res;
  }
  //如果关系是操作符
  if(relate.type === Lexter.types.OPERATOR){
    if(RELATE[relate.text] === undefined){
      throw new Error("unrecognized operator");
    }
    res["relate"] = RELATE[relate.text];
    res["values"] = group(part.slice(2),',');
  //如果关系是类似 in , like 这样的关键字
  }else{
    if(relate.text.toLowerCase() === "between"){
      res["relate"] = RELATE[relate.text.toLowerCase()];
      res["values"] = group(part.slice(3,part.length-1),"and");
    }else if(relate.text.toLowerCase() === "in"){
      res["relate"] = RELATE[relate.text.toLowerCase()];
      res["values"] = group(part.slice(3,part.length-1),',');
    }else if(relate.text.toLowerCase() === "like"){
      res["relate"] = RELATE[relate.text.toLowerCase()];
      res["values"] = [[part[2]]];
    }else if(relate.text.toLowerCase() === "is"){
      if(part[2].text.toLowerCase() === "not"){
        res["relate"] = RELATE["not null"];
        res["values"] = null;
      }else if(part[2].text.toLowerCase() === "null"){
        res["relate"] = RELATE["is null"];
        res["values"] = null;
      }else{
        throw new Error("wrong key word after \"is\"");
      }
    }else if(relate.text.toLowerCase() === "not"){
      if(part[2].text.toLowerCase() === "in"){
        res["relate"] = RELATE["not in"];
        res["values"] = group(part.slice(4,part.length-1),',');
      }else if(part[2].text.toLowerCase() === "like"){
        res["relate"] = RELATE["not like"];
        res["values"] = [[part[3]]];
      }else{
        throw new Error("wrong key word after \"not\"");
      }
    }else{
      throw new Error("unrecognized relate");
    }
  }
  return res;
}
exports.parseOneWhere = parseOneWhere;
/*}}}*/

/*{{{ group */
/**
 * token分组
 * @param {array} part token组
 * @param {string} sep 分隔符
 * @param {}
 */
function group(part,sep){
  sep = new RegExp(sep,'i');

  var res = [];
  var pos = 0;
  var tmp = [];
  for(var i in part){
    if(sep.test(part[i].text)){
      res.push(tmp);
      tmp = [];
    }else{
      tmp.push(part[i]);
    }
  }

  if(tmp.length !== 0){
    res.push(tmp);
  }
  return res;
}
/*}}}*/

