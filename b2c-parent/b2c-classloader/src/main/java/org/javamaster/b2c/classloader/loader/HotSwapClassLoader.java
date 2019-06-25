package org.javamaster.b2c.classloader.loader;

/**
 * @author yudong
 * @date 2019/6/25
 */
public class HotSwapClassLoader extends ClassLoader {
    public HotSwapClassLoader() {
        // 实现提交的执行代码可以访问服务端引用类库
        super(HotSwapClassLoader.class.getClassLoader());
    }

    public Class<?> loadClassBytes(byte[] bytes) {
        return defineClass(null, bytes, 0, bytes.length);
    }
}
