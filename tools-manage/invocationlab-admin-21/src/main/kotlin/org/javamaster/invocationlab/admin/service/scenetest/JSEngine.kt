package org.javamaster.invocationlab.admin.service.scenetest

import org.javamaster.invocationlab.admin.service.Pair
import org.javamaster.invocationlab.admin.service.invocation.Invocation
import org.javamaster.invocationlab.admin.service.invocation.Invoker
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest
import org.javamaster.invocationlab.admin.util.FileUtils.readStringFromUrl
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * javaScript执行引擎
 *
 * @author yudong
 */
internal object JSEngine {
    private const val ENGINE_NAME = "JavaScript"
    private val manager = ScriptEngineManager()
    private val engine: ScriptEngine = manager.getEngineByName(ENGINE_NAME)
    private var internalJsFunction: String? = null

    fun runScript(
        requestList: List<Pair<PostmanDubboRequest, Invocation>>?,
        sender: Invoker<Any, PostmanDubboRequest>,
        scriptCode: String
    ): Map<String, Any> {
        var newScriptCode = scriptCode
        if (StringUtils.isEmpty(internalJsFunction)) {
            //"script/propertyOperation.js"
            //"script/sendWrapper.js"
            val pathArray = arrayOf("script/propertyOperation.js", "script/sendWrapper.js")
            internalJsFunction = getAllJsContent(pathArray)
        }
        //添加默认的js函数
        newScriptCode = """
             $newScriptCode
             $internalJsFunction
             """.trimIndent()
        val map: MutableMap<String, Any> = LinkedHashMap()
        try {
            val bindings = engine.createBindings()
            bindings["reqs"] = requestList
            bindings["sender"] = sender
            bindings["rst"] = map
            engine.eval(newScriptCode, bindings)
            return map
        } catch (e: Exception) {
            val expResult = """
                ${ExceptionUtils.getMessage(e)}
                ${ExceptionUtils.getStackTrace(e)}
                """.trimIndent()
            map["ScriptException"] = expResult
            return map
        }
    }

    private fun getAllJsContent(pathArray: Array<String>): String {
        val sb = StringBuilder()
        for (path in pathArray) {
            val url = JSEngine::class.java.classLoader.getResource(path)
            val content = url?.let { readStringFromUrl(it) }
            sb.append(content)
            sb.append("\n")
        }
        return sb.toString()
    }
}
