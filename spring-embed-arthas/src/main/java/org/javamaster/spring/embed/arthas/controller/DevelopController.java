package org.javamaster.spring.embed.arthas.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.taobao.arthas.core.command.klass100.JadCommand;
import com.taobao.arthas.core.command.klass100.OgnlCommand;
import com.taobao.arthas.core.command.logger.LoggerCommand;
import com.taobao.arthas.core.command.model.JadModel;
import com.taobao.arthas.core.command.model.JvmModel;
import com.taobao.arthas.core.command.model.LoggerModel;
import com.taobao.arthas.core.command.model.OgnlModel;
import com.taobao.arthas.core.command.monitor200.JvmCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.session.impl.SessionImpl;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.util.HotSwapper;
import lombok.extern.slf4j.Slf4j;
import org.javamaster.spring.embed.arthas.HotSwapAgentMain;
import org.javamaster.spring.embed.arthas.utils.ExceptionUtils;
import org.springframework.asm.ClassReader;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springsource.loaded.agent.SpringLoadedAgent;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import static com.taobao.arthas.core.shell.session.Session.INSTRUMENTATION;

/**
 * 开发专用，接口需要做好安全控制
 *
 * @author yudong
 * @date 2022/6/4
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/appTest")
public class DevelopController {

    /**
     * class热部署
     */
    @PostMapping(value = "/hotswap", produces = "text/plain")
    public String hotswap(@RequestBody JsonNode jsonNode) {
        try {
            ClassPool.getDefault().insertClassPath(new ClassClassPath(HotSwapper.class));
            Instrumentation inst = HotSwapAgentMain.startAgentAndGetInstrumentation();

            String base64 = jsonNode.get("base64").asText();
            base64 = base64.replace("data:application/octet-stream;base64,", "");
            byte[] bytes = Base64Utils.decodeFromString(base64);
            ClassReader classReader = new ClassReader(bytes);
            String className = classReader.getClassName().replace("/", ".");
            Class<?> aClass = Class.forName(className);

            ClassDefinition classDefinition = new ClassDefinition(aClass, bytes);
            inst.redefineClasses(classDefinition);
            return "hotswap " + className + " success";
        } catch (Throwable throwable) {
            log.error("error", throwable);
            return ExceptionUtils.getStackTrace(throwable);
        }
    }

    /**
     * 反编译class
     */
    @PostMapping(value = "/jad", produces = "text/plain")
    public String jad(String classPattern) {
        try {
            final JadModel[] models = new JadModel[1];

            // 直接使用Arthas的JadCommand类实现反编译
            JadCommand command = new JadCommand();
            command.setClassPattern(classPattern);
            command.setCode(Integer.toHexString(this.getClass().getClassLoader().hashCode()));
            command.process(commandProcess(models));

            JadModel model = models[0];
            return Arrays.toString(model.getClassInfo().getClassloader()) + "\r\n" +
                    model.getClassInfo().getClassLoaderHash() + "\r\n" +
                    model.getClassInfo().getName() + "\r\n" +
                    model.getLocation() + "\r\n" +
                    model.getSource() + "\r\n";
        } catch (Throwable throwable) {
            log.error("error", throwable);
            return ExceptionUtils.getStackTrace(throwable);
        }
    }

    /**
     * 执行ognl
     */
    @PostMapping(value = "/ognl", produces = "text/plain")
    public String ognl(String ognl) {
        try {
            final OgnlModel[] models = new OgnlModel[1];

            OgnlCommand command = new OgnlCommand();
            command.setExpress(ognl);
            command.setHashCode(Integer.toHexString(this.getClass().getClassLoader().hashCode()));
            command.process(commandProcess(models));

            OgnlModel model = models[0];
            return model.getValue() + "";
        } catch (Throwable throwable) {
            log.error("error", throwable);
            return ExceptionUtils.getStackTrace(throwable);
        }
    }

    @PostMapping(value = "/jvm", produces = "text/plain")
    public String jvm() {
        try {
            final JvmModel[] models = new JvmModel[1];

            JvmCommand command = new JvmCommand();
            command.process(commandProcess(models));

            JvmModel model = models[0];
            return JSON.toJSONString(model.getJvmInfo(), true);
        } catch (Throwable throwable) {
            log.error("error", throwable);
            return ExceptionUtils.getStackTrace(throwable);
        }
    }

    @PostMapping(value = "/logger", produces = "text/plain")
    public String logger(@RequestBody JsonNode jsonNode) {
        try {
            final LoggerModel[] models = new LoggerModel[1];

            LoggerCommand command = new LoggerCommand();
            if (!StringUtils.isEmpty(jsonNode.get("name").asText())) {
                command.setName(jsonNode.get("name").asText());
            }
            command.setHashCode(Integer.toHexString(this.getClass().getClassLoader().hashCode()));
            command.process(commandProcess(models));

            LoggerModel model = models[0];
            return JSON.toJSONString(model.getLoggerInfoMap(), true);
        } catch (Throwable throwable) {
            log.error("error", throwable);
            return ExceptionUtils.getStackTrace(throwable);
        }
    }

    @SuppressWarnings("all")
    private <T> CommandProcess commandProcess(T[] res) throws Exception {
        ClassPool.getDefault().insertClassPath(new ClassClassPath(HotSwapper.class));
        Instrumentation inst = HotSwapAgentMain.startAgentAndGetInstrumentation();

        return (CommandProcess) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{CommandProcess.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("session".equals(method.getName())) {
                            SessionImpl session = new SessionImpl();
                            session.put(INSTRUMENTATION, inst);
                            return session;
                        } else if ("appendResult".equals(method.getName())
                                && args[0].getClass() == res.getClass().getComponentType()) {
                            if (args[0].getClass() == LoggerModel.class) {
                                if (!((LoggerModel) args[0]).getLoggerInfoMap().isEmpty()) {
                                    res[0] = (T) args[0];
                                }
                            } else {
                                res[0] = (T) args[0];
                            }
                            return proxy;
                        } else if ("end".equals(method.getName())) {
                            if (args != null && args.length > 1 && (int) args[0] == -1) {
                                throw new RuntimeException((String) args[1]);
                            }
                        }
                        return proxy;
                    }
                });
    }
}
