const path = require('path')
const fs = require('fs');

function resolve(dir) {
  return path.join(__dirname, '.', dir)
}

const tmpPath = resolve('.')
const publicPath = resolve('public')

const curSrc = path.resolve(tmpPath, "env-config-prod.js");
const curDest = path.resolve(publicPath, "env-config.js");
fs.createReadStream(curSrc).pipe(fs.createWriteStream(curDest));
console.log('完成复制'+curSrc+'到'+curDest)