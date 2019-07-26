rem maven编译完成后打包前生成SPI文件,此文件是注解处理器能起作用的关键
cd ${project.build.directory}\\classes\\META-INF\\services
echo org.javamaster.mocklombok.javac.processor.GenerateGetMethodProcessor>javax.annotation.processing.Processor
echo org.javamaster.mocklombok.javac.processor.ClassChecker>>javax.annotation.processing.Processor
