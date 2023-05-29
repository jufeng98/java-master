// +------------------------------------------------------------------------+
// | 词法分析器类                                                           |
// +------------------------------------------------------------------------+
// | Copygight (c) 2003 - 2011 Taobao.com. All Rights Reserved              |
// +------------------------------------------------------------------------+
// | Author: yixuan.zzq <yixuan.zzq@taobao.com>                             |
// +------------------------------------------------------------------------+
// | 2011年10月                                                             |
// +------------------------------------------------------------------------+

var Types   = {
  UNKNOWN     : 0,				/**<    未知的 */
  KEYWORD     : 1,				/**<    关键字 */
  NUMBER      : 2,				/**<    数  字 */
  STRING      : 3,				/**<    字符串 */
  FUNCTION    : 4,				/**<    函数名 */
  VARIABLE    : 5,				/**<    变  量 */
  PARAMS      : 6,                /**<    绑定值 */
  OPERATOR    : 7,				/**<    运算符 */
  COMMAS      : 8,				/**<    标  点 */
  MEMORY		  : 9,

  COMMENT     : 99,       /**<    注  释 */

}

var Parser  = function(query) {
  var tks = [], pre = Types.UNKNOWN;
  var tmp = '', cur = '', sub = '', nxt = '';

  for (var i = 0; i < query.length; i++) {
    cur = query.charAt(i);

    /* {{{ 注释 */
    if ('/' == cur && '*' == query.charAt(i + 1)) {
      tmp = '';
      i++;
      while (++i < query.length) {
        sub = query.charAt(i);
        nxt = query.charAt(i+1);
        if ('*' == sub && '/' == nxt) {
          tmp = tmp.substr(0,tmp.length-1);
          i++;
          break;
        }
        if(tmp === ''){
          tmp += (sub + nxt);
        }else{
          tmp += nxt;
        }
      }
      tks[tks.length] = {
        'text'  : tmp.replace(/^[\*\s]+/, '').replace(/[\s\*]+$/, ''),
        'type'  : Types.COMMENT,
      };
    }
    /* }}} */

    /* {{{ 字符串 */
    else if ("'" == cur || '"' == cur || '`' == cur) {
      tmp = '';
      while (i < query.length && cur != (sub = query.charAt(++i))) {
        tmp += ("\\" == sub) ? query.charAt(++i) : sub;
      }
      tks[tks.length] = {
        'text'  : tmp,
        'type'  : ('`' == cur) ? Types.VARIABLE : Types.STRING,
      };
    }
    /* }}} */

    /* {{{ 绑定变量 */
    else if (':' == cur) {
      tmp = cur;
      while (i < query.length) {
        sub = query.charAt(++i);
        if (!(/^\w+$/i.test(sub))) {
          break;
        }
        tmp += sub;
      }
      i--;
      tks[tks.length] = {
        'text'  : tmp,
        'type'  : Types.PARAMS,
      };
    }
    /* }}} */

    /* {{{ 函数名 */
    else if (/^[a-z_]+$/i.test(cur)) {
      tmp = cur;
      while (i < query.length) {
        sub = query.charAt(++i);
        if (!(/^[\w\.:]+$/i.test(sub))) {
          break;
        }
        tmp += sub;
      }
      i--;
      tks[tks.length] = {
        'text'  : tmp,
        'type'  : '(' == sub ? Types.FUNCTION : Types.KEYWORD,
      };
    }
    /* }}} */

    /* {{{ 数字 */

    else if (('-' == cur && Types.VARIABLE != pre) || /\d+/.test(cur)) {
      tmp = cur;
      while (i < query.length) {
        sub = query.charAt(++i);
        if (!(/^[\d\.]+$/.test(sub))) {
          break;
        }
        tmp += sub;
      }
      i--;

      if("-" == tmp){
        tks[tks.length] = {
          'text'  : '-',  /**<    类型转换 */
          'type'  : Types.OPERATOR
        };	
      }else{
        tks[tks.length] = {
          'text'  : tmp - 0,  /**<    类型转换 */
          'type'  : Types.NUMBER
        };	
      }
    }
    /* }}} */

    /* {{{ 标点 */
    else if (/^[\,;\(\)]+$/.test(cur)) {
      tks[tks.length] = {
        'text'  : cur,
        'type'  : Types.COMMAS,
      };
    }
    /* }}} */

    /* {{{ 运算符 */
    else if (/^(\+|\-|\*|\/|>|<|=|!)$/.test(cur)) {
      tmp = cur;
      while (i < query.length) {
        sub = query.charAt(++i);
        if (!(/^(\+|\*|\/|>|<|=|!)+$/.test(sub))) {
          break;
        }
        tmp += sub;
      }
      i--;

      tks[tks.length] = {
        'text'  : tmp,
        'type'  : Types.OPERATOR,
      };
    }
    /* }}} */

    pre = (tks.length === 0) ? pre : tks[tks.length - 1].type;
  }

  return tks;
}

/* {{{ public construct() */
var Lexter  = function(query) {
  this.tokens = (query instanceof Array) ? query : Parser(query.toString());
  this.blocks = [];

  var express = 0;
  var calcmap = {
    "(" : 1,
    ")" : -1,
  };

  for (var i = 0; i < this.tokens.length; ++i) {
    var tks = this.tokens[i];
    if (tks.type == Types.COMMAS && undefined !== calcmap[tks.text]) {
      express += calcmap[tks.text];
    } else if (!express) {
      this.blocks[this.blocks.length] = i;
    }
  }
}
/* }}} */

/* {{{ public getAll() */
Lexter.prototype.getAll     = function() {
  return this.tokens;
}
/* }}} */

/* {{{ public indexOf() */
Lexter.prototype.indexOf    = function(who, off) {
  var pos = 0;
  var tks = null;

  try {
    var exp = new RegExp(who.text, 'i');
  } catch (e) {
    var exp = who.text.toLowerCase();
  }
  var off = (off == undefined) ? 0 : off + 1;

  // xxx: 这里可以用二分法
  for (var i = 0; i < this.blocks.length; ++i) {
    pos = this.blocks[i];
    if (pos < off) {
      continue;
    }

    tks = this.tokens[pos];
    if (who.type == tks.type && ((exp instanceof RegExp && exp.test(tks.text)) || exp == tks.text.toLowerCase())) {
      return this.blocks[i];
    }
  }

  return -1;
}
/* }}} */

/*{{{ static vars()*/
exports.vars = function(idx,tokens,isString){
  var res;
  if(isString){
    var lexter = new Lexter(tokens);
    tokens = lexter.getAll();
  }
  if(!tokens[idx]){return null;}
  switch(tokens[idx]["type"]){
    case Types.OPERATOR:
      res = [tokens[idx-1],tokens[idx+1]];
      break;
    case Types.FUNCTION:
      res = [];
      var temp = [];
      var expr = 0;
      for(var i  = idx+1, count=tokens.length;i<count;i++){
        var tk = tokens[i];
        if(tk["type"] != Types.COMMAS){
          temp.push(tk);
          continue;
        }
        switch(tk["text"]){
          case "(":
            if(expr>0){
              temp.push(tk);
            }
          expr++;
          break;
          case ")":
            if((--expr)==0){
              res.push(temp);
              temp = [];
              i = count;
              break;
            }else{temp.push(tk);}
          break;
          case ",":
            if(expr == 1){
              res.push(temp);
              temp = [];
            }else{temp.push(tk);}
          break;
          default:
          break;
        }
      }
      break;
    default :
      res = null;
      break;
  }
  return res;
}
/*}}}*/

/*{{{ static text()*/
exports.text = function(stack,comma){
  var res = [];
  for(var idx in stack){
    var token = stack[idx];
    if(!token || !token.type || token.text == null){
      res.push(null);
    }else{
      switch(token.type){
        case Types.STRING :
          res.push("'"+token.text+"'");
          break;
        case Types.VARIABLE :
          res.push(token.text);
          break;
        default:
          res.push(token.text);
          break;
      }
    }
    if(comma){
      res.push(comma);
    }
  }
  if(comma){res.pop();}
  return res;
}
/*}}}*/

exports.types   = Types;
exports.create  = function(query) {
  return new Lexter(query);
}

