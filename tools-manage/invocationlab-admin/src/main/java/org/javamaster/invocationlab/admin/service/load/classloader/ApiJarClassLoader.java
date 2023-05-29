package org.javamaster.invocationlab.admin.service.load.classloader;

import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author yudong
 * 加载api.jar,每个api.jar有一个ApiJarClassLoader加载
 * 这样可以方便进行重新加载和卸载
 * 这里使用了jdk默认的双亲委派加载机制
 * -------------------------------
 * 有一个特殊场景:
 * 用户发送请求到dubbo,从页面过来的数据时json,这时候需要把
 * 这个数据通过jackson反序列话成api.jar里面的dto实例
 * 有些dto里面用到了jackson里面的特殊注解,这时候在反序列话的
 * 时候是 父类加载器里面的类(jackson框架里面的反序列化类)需要识别子类里面的类(由ApiJarClassLoader加载,通常是jackson框架里面的注解),
 * 不然这些注解会解析失败导致数据反序列化不对,这时就需要破坏双亲委派的机制,使用Thread.currentThread().getContextClassLoader()来
 * 完成这个功能,参考{真正请求dubbo的地方}
 */
public class ApiJarClassLoader extends URLClassLoader {
    private final URL[] libUrls;

    public ApiJarClassLoader(URL[] urls) {
        super(urls, Thread.currentThread().getContextClassLoader());
        this.libUrls = urls;
    }

    public Class<?> loadClassWithResolve(String name) throws ClassNotFoundException {

        return loadClass(name, true);
    }

    public void appendURL(URL url) {
        addURL(url);
    }

    @Override
    protected void addURL(URL url) {
        super.addURL(url);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (Throwable throwable) {
            Class<?> aClass = tryLoadClass(throwable.getMessage());
            if (aClass != null) {
                return aClass;
            }
            throw throwable;
        }
    }

    public Class<?> tryLoadClass(String msg) {
        for (URL libUrl : libUrls) {
            try {
                JarFile jarFile = new JarFile(libUrl.getFile());
                ZipEntry entry = jarFile.getEntry(msg.replace(".", "/") + ".class");
                if (entry != null) {
                    InputStream inputStream = jarFile.getInputStream(entry);
                    byte[] bytes = StreamUtils.copyToByteArray(inputStream);
                    return loadClassBytes(bytes);
                }
                jarFile.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public Class<?> loadClassBytes(byte[] bytes) {
        return defineClass(null, bytes, 0, bytes.length);
    }

    @Override
    public URL findResource(String name) {
        return super.findResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        return super.findResources(name);
    }

}
