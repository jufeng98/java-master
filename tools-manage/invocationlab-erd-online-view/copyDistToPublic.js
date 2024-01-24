const path = require('path')
const fs = require('fs');

function resolve(dir) {
  return path.join(__dirname, '.', dir)
}

const distPath = resolve('dist')
const publicPath = resolve('../invocationlab-admin/src/main/resources/public/invocationlab-erd-online-view')

/**
 * 删除文件夹功能
 * @param  {String} url  文件路径，绝对路径
 * @return {Null}
 */
function deleteDir(url){
    var files = [];
    if( fs.existsSync(url) ) {  //判断给定的路径是否存在
        files = fs.readdirSync(url);   //返回文件和子目录的数组
        files.forEach(function(file,index){
            var curPath = path.join(url,file);

            if(fs.statSync(curPath).isDirectory()) { //同步读取文件夹文件，如果是文件夹，则函数回调
                deleteDir(curPath);
            } else {
                fs.unlinkSync(curPath);    //是指定文件，则删除
            }
        });
        fs.rmdirSync(url); //清除文件夹
    }
}

deleteDir(publicPath);
console.log('完成删除'+publicPath)

/**
 * 复制文件夹到目标文件夹
 * @param {string} src 源目录
 * @param {string} dest 目标目录
 * @param {function} callback 回调
 */
const copyDir = (src, dest, callback) => {
  const copy = (copySrc, copyDest) => {
    fs.readdir(copySrc, (err, list) => {
      if (err) {
        callback(err);
        return;
      }
      list.forEach((item) => {
        const ss = path.resolve(copySrc, item);
        fs.stat(ss, (err, stat) => {
          if (err) {
            callback(err);
          } else {
            const curSrc = path.resolve(copySrc, item);
            const curDest = path.resolve(copyDest, item);

            if (stat.isFile()) {
              // 文件，直接复制
              fs.createReadStream(curSrc).pipe(fs.createWriteStream(curDest));
            } else if (stat.isDirectory()) {
              // 目录，进行递归
              fs.mkdirSync(curDest, { recursive: true });
              copy(curSrc, curDest);
            }
          }
        });
      });
    });
  };

  fs.access(dest, (err) => {
    if (err) {
      // 若目标目录不存在，则创建
      fs.mkdirSync(dest, { recursive: true });
    }
    copy(src, dest);
  });
};

copyDir(distPath, publicPath);
console.log('完成复制'+distPath+'到'+publicPath)
