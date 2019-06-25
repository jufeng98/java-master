package org.javamaster.b2c.classloader.controller;

import org.javamaster.b2c.classloader.loader.HotSwapClassLoader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;

/**
 * @author yudong
 * @date 2019/6/25
 */
@RestController
@RequestMapping("/api")
public class ExecuteController {

    /**
     * 这里要做安全控制，因为可以在服务器上执行任意代码
     *
     * @param multipartFile
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/execute", method = {RequestMethod.POST})
    public Object execute(@RequestPart("file") MultipartFile[] multipartFile) throws Exception {
        byte[] bytes = multipartFile[0].getBytes();
        HotSwapClassLoader classLoader = new HotSwapClassLoader();
        Class<?> clz = classLoader.loadClassBytes(bytes);
        Method method = clz.getDeclaredMethod("execute");
        method.setAccessible(true);
        return method.invoke(null);
    }
}
