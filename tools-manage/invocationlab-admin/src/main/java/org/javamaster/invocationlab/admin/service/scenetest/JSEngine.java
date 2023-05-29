package org.javamaster.invocationlab.admin.service.scenetest;

import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.invocation.Invocation;
import org.javamaster.invocationlab.admin.service.invocation.Invoker;
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest;
import org.javamaster.invocationlab.admin.util.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * javaScript执行引擎
 *
 * @author yudong
 */
class JSEngine {
    private final static String engineName = "JavaScript";
    private final static ScriptEngineManager manager = new ScriptEngineManager();
    private final static ScriptEngine engine = manager.getEngineByName(engineName);
    private static String internalJsFunction;

    protected static Map<String, Object> runScript(List<Pair<PostmanDubboRequest, Invocation>> requestList, Invoker<Object,
            PostmanDubboRequest> sender, String scriptCode) {
        if (StringUtils.isEmpty(internalJsFunction)) {
            //"script/propertyOperation.js"
            //"script/sendWrapper.js"
            String[] pathArray = new String[]{"script/propertyOperation.js", "script/sendWrapper.js"};
            internalJsFunction = getAllJsContent(pathArray);
        }
        //添加默认的js函数
        scriptCode = scriptCode + "\n" + internalJsFunction;
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            Bindings bindings = engine.createBindings();
            bindings.put("reqs", requestList);
            bindings.put("sender", sender);
            bindings.put("rst", map);
            engine.eval(scriptCode, bindings);
            return map;
        } catch (Exception e) {
            String expResult = ExceptionUtils.getMessage(e) + "\r\n" + ExceptionUtils.getStackTrace(e);
            map.put("ScriptException", expResult);
            return map;
        }
    }

    private static String getAllJsContent(String[] pathArray) {
        StringBuilder sb = new StringBuilder();
        for (String path : pathArray) {
            URL url = JSEngine.class.getClassLoader().getResource(path);
            String content = FileUtils.readStringFromUrl(url);
            sb.append(content);
            sb.append("\n");
        }
        return sb.toString();
    }
}
