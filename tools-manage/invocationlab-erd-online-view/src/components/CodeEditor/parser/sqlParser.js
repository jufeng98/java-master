var Tool = require('./sqlParseTool.js');
var Lexter = require('./lexter.js');

var parsers = {
	'select': require('./select.js'),
};

exports.parse = function(sql) {
  sql = sql.trim();
  var who = sql.substr(0,sql.indexOf(' ')).toLowerCase();
  if(parsers[who] === undefined){
    throw new Error("Unsupport sentence");
  }
  return  parsers[who].createObj(sql);
}

exports.RELATE = Tool.RELATE;
exports.JOIN = Tool.JOIN;
exports.ORDER = Tool.ORDER;
exports.types = Lexter.types;
