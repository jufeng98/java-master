package org.javamaster.java.agent.transformer;

import javassist.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * @author yudong
 * @date 2020/10/15
 */
@Slf4j
public class WatchReTransformer implements ClassFileTransformer {

    private boolean watch = false;
    private String sessionId;
    private String watchClassName;
    private byte[] originalBytes;

    public void setWatch(boolean watch) {
        this.watch = watch;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setWatchClassName(String watchClassName) {
        this.watchClassName = watchClassName;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null) {
            return classfileBuffer;
        }
        String name = className.replace("/", ".");
        if (!name.equals(watchClassName)) {
            return classfileBuffer;
        }
        try {
            if (watch) {
                originalBytes = classfileBuffer;
                ClassPool cp = ClassPool.getDefault();
                cp.importPackage("org.javamaster.java.agent.collector");
                cp.importPackage("org.javamaster.java.agent.advice");
                cp.insertClassPath(new ByteArrayClassPath(name, classfileBuffer));
                CtClass cc = cp.get(name);
                cc.defrost();
                for (CtMethod declaredMethod : cc.getDeclaredMethods()) {
                    if (Modifier.toString(declaredMethod.getModifiers()).contains("static")) {
                        continue;
                    }
                    declaredMethod.insertAfter(String.format("WatchCollector.add(\"%s\", new Advice(this.getClass().getClassLoader(), this.getClass(), this, $args, $_,new Boolean(false)));",
                            sessionId));
                    CtClass ctClass = ClassPool.getDefault().get("java.lang.Exception");
                    declaredMethod.addCatch(String.format("{WatchCollector.add(\"%s\", new Advice(this.getClass().getClassLoader(), this.getClass(), this, $args, null,new Boolean(true)));throw $e;}",
                            sessionId), ctClass);
                }
                log.info("class:{} methods add watch aspect", className);
                return cc.toBytecode();
            } else {
                log.info("class:{} methods remove watch aspect", className);
                return originalBytes;
            }
        } catch (Exception e) {
            log.error("{},{}", className, e.getMessage());
        }
        return classfileBuffer;
    }
}
