package org.javamaster.spring.embed.arthas;

import com.sun.tools.attach.VirtualMachine;
import javassist.ClassPool;
import javassist.CtClass;
import sun.misc.URLClassPath;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Vector;
import java.util.jar.*;
import java.util.jar.Attributes.Name;

/**
 * @author yudong
 * @date 2022/6/4
 */
public class HotSwapAgentMain {
    private static Instrumentation instrumentation = null;

    public static void agentmain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
    }

    /**
     * 动态创建JavaAgent的jar包让jvm加载从而取得Instrumentation对象
     */
    public static Instrumentation startAgentAndGetInstrumentation() throws Exception {
        if (instrumentation != null) {
            return instrumentation;
        }

        correctToolsLoadedOrder();

        File agentJar = createJavaAgentJarFile();

        String nameOfRunningVm = ManagementFactory.getRuntimeMXBean().getName();
        String pid = nameOfRunningVm.substring(0, nameOfRunningVm.indexOf(64));
        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(agentJar.getAbsolutePath(), null);
        vm.detach();
        instrumentation = getInstrumentationFromSystemClassLoader();
        return instrumentation;
    }

    /**
     * 同时引入了windows和linux环境下的tools包,为了避免tools包加载的不确定性(系统可能会加载到错误的tools包),
     * 对两个tools包的加载顺序根据当前运行环境必要时进行交换,确保系统优先加载的是正确的tools包
     */
    private static void correctToolsLoadedOrder() throws Exception {
        ClassLoader classLoader = HotSwapAgentMain.class.getClassLoader();
        Field ucp = URLClassLoader.class.getDeclaredField("ucp");
        ucp.setAccessible(true);
        URLClassPath urlClassPath = (URLClassPath) ucp.get(classLoader);
        Field pathField = urlClassPath.getClass().getDeclaredField("path");
        Field loadersField = urlClassPath.getClass().getDeclaredField("loaders");
        pathField.setAccessible(true);
        loadersField.setAccessible(true);
        ArrayList<URL> urls = (ArrayList<URL>) pathField.get(urlClassPath);
        ArrayList<Object> loaders = (ArrayList<Object>) loadersField.get(urlClassPath);
        int windowsToolsJarIndex = Integer.MAX_VALUE;
        int linuxToolsJarIndex = Integer.MAX_VALUE;
        for (int i = 0; i < urls.size(); i++) {
            if (urls.get(i).getPath().endsWith("tools.jar")) {
                windowsToolsJarIndex = i;
            } else if (urls.get(i).getPath().endsWith("tools-linux-1.8.0.jar")) {
                linuxToolsJarIndex = i;
            }
        }
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            if (linuxToolsJarIndex < windowsToolsJarIndex) {
                Collections.swap(urls, linuxToolsJarIndex, windowsToolsJarIndex);
                Collections.swap(loaders, linuxToolsJarIndex, windowsToolsJarIndex);
            }
        } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            if (windowsToolsJarIndex < linuxToolsJarIndex) {
                Collections.swap(urls, linuxToolsJarIndex, windowsToolsJarIndex);
                Collections.swap(loaders, linuxToolsJarIndex, windowsToolsJarIndex);
            }
        }
    }

    /**
     * 当调用loadAgent时jvm肯定用的是AppClassLoader类加载器来加载HotSwapAgentMain类,所以直接用反射取得Instrumentation对象
     */
    private static Instrumentation getInstrumentationFromSystemClassLoader() throws Exception {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        Field classesField = ClassLoader.class.getDeclaredField("classes");
        classesField.setAccessible(true);
        Vector<Class<?>> classes = (Vector<Class<?>>) classesField.get(systemClassLoader);

        Class<?> clz = null;
        for (Class<?> aClass : classes) {
            if (aClass.getName().equals(HotSwapAgentMain.class.getName())) {
                clz = aClass;
                break;
            }
        }
        Objects.requireNonNull(clz);
        Field field = clz.getDeclaredField("instrumentation");
        field.setAccessible(true);
        return (Instrumentation) field.get(null);
    }

    private static File createJavaAgentJarFile() throws Exception {
        File jar = File.createTempFile("agent", ".jar");
        jar.deleteOnExit();
        Manifest manifest = new Manifest();
        Attributes attrs = manifest.getMainAttributes();
        attrs.put(Name.MANIFEST_VERSION, "1.0");
        attrs.put(new Name("Agent-Class"), HotSwapAgentMain.class.getName());
        attrs.put(new Name("Can-Retransform-Classes"), "true");
        attrs.put(new Name("Can-Redefine-Classes"), "true");
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jar.toPath()), manifest)) {
            writeClassFile(HotSwapAgentMain.class, jos);
            writeClassFile(ClassPool.class, jos);
            writeClassFile(CtClass.class, jos);
        }
        return jar;
    }

    private static void writeClassFile(Class<?> clz, JarOutputStream jos) throws Exception {
        String cname = clz.getName();
        JarEntry e = new JarEntry(cname.replace('.', '/') + ".class");
        jos.putNextEntry(e);
        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = pool.get(cname);
        jos.write(clazz.toBytecode());
        jos.closeEntry();
    }
}
