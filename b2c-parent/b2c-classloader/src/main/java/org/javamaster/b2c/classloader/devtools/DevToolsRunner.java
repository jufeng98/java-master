package org.javamaster.b2c.classloader.devtools;

import org.javamaster.b2c.classloader.ClassLoaderApplication;
import org.javamaster.b2c.classloader.utils.SerializeUtils;
import org.javamaster.b2c.dubbo.dto.UserBaseDto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

@Component
public class DevToolsRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // null意味着是BootstrapClassLoader
        System.out.println("Object classloader:" + Object.class.getClassLoader());
        // RestartClassLoader
        System.out.println("ClassLoaderApplication classloader:"
                + ClassLoaderApplication.class.getClassLoader().getClass().getSimpleName());
        //AppClassLoader
        System.out.println("ClassLoaderApplication classloader parent:"
                + ClassLoaderApplication.class.getClassLoader().getParent().getClass().getSimpleName());
        // ExtClassLoader
        System.out.println("ClassLoaderApplication classloader parent's parent:"
                + ClassLoaderApplication.class.getClassLoader().getParent().getParent().getClass().getSimpleName());
        // UserBaseDto是已导入到IDE的项目其它模块类文件
        UserBaseDto userBaseDto = new UserBaseDto();
        System.out.println("userBaseDto object classloader:"
                + userBaseDto.getClass().getClassLoader().getClass().getSimpleName());
        byte[] bytes = SerializationUtils.serialize(userBaseDto);
        Object deserializeUserBaseDto = SerializationUtils.deserialize(bytes);
        System.out.println("deserialize userBaseDto object classloader:"
                + deserializeUserBaseDto.getClass().getClassLoader().getClass().getSimpleName());
//        userBaseDto = (UserBaseDto) deserializeUserBaseDto;

        deserializeUserBaseDto = SerializeUtils.deserialize(bytes);
        userBaseDto = (UserBaseDto) deserializeUserBaseDto;
        System.out.println(userBaseDto);
    }

}
