package org.javamaster.invocationlab.admin.service.load.classloader

import org.springframework.util.StreamUtils
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarFile

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
class ApiJarClassLoader(private val libUrls: Array<URL>) :
    URLClassLoader(libUrls, Thread.currentThread().contextClassLoader) {

    fun loadClassWithResolve(name: String): Class<*> {
        return loadClass(name, true)
    }

    public override fun addURL(url: URL) {
        super.addURL(url)
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        try {
            return super.loadClass(name, resolve)
        } catch (throwable: Throwable) {
            val aClass = tryLoadClass(throwable.message)
            if (aClass != null) {
                return aClass
            }
            throw throwable
        }
    }

    private fun tryLoadClass(msg: String?): Class<*>? {
        for (libUrl in libUrls) {
            try {
                val jarFile = JarFile(libUrl.file)
                val entry = jarFile.getEntry(msg!!.replace(".", "/") + ".class")
                if (entry != null) {
                    val inputStream = jarFile.getInputStream(entry)
                    val bytes = StreamUtils.copyToByteArray(inputStream)
                    return loadClassBytes(bytes)
                }
                jarFile.close()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
        return null
    }

    private fun loadClassBytes(bytes: ByteArray): Class<*> {
        return defineClass(null, bytes, 0, bytes.size)
    }

    override fun findResource(name: String): URL {
        return super.findResource(name)
    }

    override fun findResources(name: String): Enumeration<URL> {
        return super.findResources(name)
    }
}
