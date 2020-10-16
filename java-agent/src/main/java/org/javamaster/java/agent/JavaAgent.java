package org.javamaster.java.agent;

import com.alibaba.fastjson.JSONObject;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import io.javalin.Javalin;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.ognl.Ognl;
import org.apache.ibatis.ognl.OgnlContext;
import org.javamaster.java.agent.collector.CostTimeCollector;
import org.javamaster.java.agent.collector.WatchCollector;
import org.javamaster.java.agent.transformer.CostTimeTransformer;
import org.javamaster.java.agent.transformer.WatchReTransformer;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yudong
 * @date 2020/10/14
 */
@Slf4j
public class JavaAgent {

    public static Instrumentation inst;

    public static void premain(String agentArgs, Instrumentation inst) {
        JavaAgent.inst = inst;
        inst.addTransformer(new CostTimeTransformer());

        Javalin app = Javalin.create(
                config -> {
                    config.defaultContentType = "application/json";
                    config.autogenerateEtags = true;
                    config.addStaticFiles("/public");
                    config.asyncRequestTimeout = 10_000L;
                    config.enforceSsl = false;
                }
        ).start(7000);

        app.get("/", ctx -> ctx.result("Hello Java Agent!!!"));

        app.get("/jps", ctx -> {
            List<VirtualMachineDescriptor> list = VirtualMachine.list();
            List<Map<Object, Object>> jvmList = list.stream().map(virtualMachineDescriptor -> {
                Map<Object, Object> jvmMap = new LinkedHashMap<>();
                jvmMap.put("pid", virtualMachineDescriptor.id());
                jvmMap.put("displayName", virtualMachineDescriptor.displayName());
                jvmMap.put("provider", virtualMachineDescriptor.provider());
                return jvmMap;
            }).collect(Collectors.toList());
            ctx.result(JSONObject.toJSONString(jvmList, true));
        });

        app.ws("/websocket/watchConsumer", ws -> {
            WatchReTransformer watchReTransformer = new WatchReTransformer();
            String className = "org.javamaster.b2c.core.service.impl.IExamServiceImpl";
            ws.onConnect(ctx -> {
                log.info("websocket {} connected", ctx.getSessionId());
                watchReTransformer.setWatch(true);
                watchReTransformer.setSessionId(ctx.getSessionId());
                watchReTransformer.setWatchClassName(className);
                inst.addTransformer(watchReTransformer, true);
                inst.retransformClasses(Class.forName(className));
                inst.removeTransformer(watchReTransformer);
                WatchCollector.addListener(className, (key, advice) -> {
                    if (!key.equals(ctx.getSessionId())) {
                        return;
                    }

                    OgnlContext context = new OgnlContext();
                    context.setRoot(advice);

                    Object reqExpression = Ognl.parseExpression("{params[0].toString(),params[1].toString()}");
                    Object reqValue = Ognl.getValue(reqExpression, context, context.getRoot());

                    Object resExpression = Ognl.parseExpression("{returnObj.{#this.toString()}}");
                    Object resValue = Ognl.getValue(resExpression, context, context.getRoot());

                    Map<Object, Object> map = new HashMap<>(6, 1);
                    map.put("params", reqValue);
                    map.put("returnObj", resValue);
                    map.put("clazz", advice.getClazz());
                    map.put("classLoader", advice.getLoader().getClass().getName());
                    map.put("target", advice.getTarget().toString());
                    map.put("isThrow", advice.getThrow());
                    ctx.send(JSONObject.toJSONString(map, true));
                });
            });

            ws.onClose(ctx -> {
                log.info("websocket {} closed", ctx.getSessionId());
                watchReTransformer.setWatch(false);
                inst.addTransformer(watchReTransformer, true);
                inst.retransformClasses(Class.forName(className));
                inst.removeTransformer(watchReTransformer);
                WatchCollector.removeListener(className);
            });

        });

        app.get("/getCostTime", ctx -> ctx.result(JSONObject.toJSONString(CostTimeCollector.getCostTime(), true)));
    }
}
