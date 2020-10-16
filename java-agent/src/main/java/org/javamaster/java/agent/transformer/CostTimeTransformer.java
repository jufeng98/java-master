package org.javamaster.java.agent.transformer;

import javassist.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yudong
 * @date 2020/10/14
 */
@Slf4j
public class CostTimeTransformer implements ClassFileTransformer {

    private static final Set<String> OBJ_METHODS;

    static {
        OBJ_METHODS = new HashSet<>();
        for (Method method : Object.class.getMethods()) {
            OBJ_METHODS.add(method.getName());
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        try {
            if (className == null) {
                return classfileBuffer;
            }
            if (className.contains("$$")) {
                return classfileBuffer;
            }
            if (className.startsWith("org/javamaster/b2c/core") || className.startsWith("org/springframework/web/servlet")) {
                String name = className.replace("/", ".");
                ClassPool cp = ClassPool.getDefault();
                cp.importPackage("org.javamaster.java.agent.collector");
                cp.insertClassPath(new ByteArrayClassPath(name, classfileBuffer));
                CtClass cc = cp.get(name);
                for (CtMethod method : cc.getDeclaredMethods()) {
                    if (method.getName().contains("$")) {
                        continue;
                    }
                    if (OBJ_METHODS.contains(method.getName())) {
                        continue;
                    }
                    if (Modifier.toString(method.getModifiers()).contains("native")) {
                        continue;
                    }
                    if (method.isEmpty()) {
                        continue;
                    }
                    method.addLocalVariable("startCtTime", CtClass.longType);
                    method.insertBefore("startCtTime=System.currentTimeMillis();");
                    method.insertAfter("CostTimeCollector.addCostTime(Thread.currentThread(),Thread.currentThread().getStackTrace(),System.currentTimeMillis() - startCtTime);");
                }
                log.info("class:{} methods add cost times aspect", className);
                return cc.toBytecode();
            }
        } catch (Exception e) {
            log.error("{},{}", className, e.getMessage());
        }
        return classfileBuffer;
    }

}
