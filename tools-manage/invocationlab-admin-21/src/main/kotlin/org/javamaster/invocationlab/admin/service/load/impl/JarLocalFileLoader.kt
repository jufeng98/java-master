package org.javamaster.invocationlab.admin.service.load.impl

import org.javamaster.invocationlab.admin.config.BizException
import org.javamaster.invocationlab.admin.consts.Constant
import org.javamaster.invocationlab.admin.service.GAV
import org.javamaster.invocationlab.admin.service.context.InvokeContext.putMethod
import org.javamaster.invocationlab.admin.service.creation.PostmanService
import org.javamaster.invocationlab.admin.service.creation.entity.MethodEntity
import org.javamaster.invocationlab.admin.service.creation.entity.ParamEntity
import org.javamaster.invocationlab.admin.service.creation.entity.RequestParam
import org.javamaster.invocationlab.admin.service.load.Loader
import org.javamaster.invocationlab.admin.service.load.classloader.ApiJarClassLoader
import org.javamaster.invocationlab.admin.util.BuildUtils.buildMethodNameKey
import org.javamaster.invocationlab.admin.util.BuildUtils.buildServiceKey
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils.get
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils.remove
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.reflect.Modifier
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

/**
 * 通过ApiJarClassLoader来实现api.jar的加载
 *
 * @author yudong
 */
object JarLocalFileLoader : Loader {
    private val logger: Logger = LoggerFactory.getLogger(JarLocalFileLoader::class.java)
    private val LOADER_MAP: MutableMap<String, ApiJarClassLoader> = ConcurrentHashMap()

    fun loadInterfaceInfoAndInitLoader(service: PostmanService) {
        try {
            doLoadInterfaceInfoAndInitLoader(service)
        } catch (e: Exception) {
            logger.error("error", e)
            throw BizException("缺少class,重新刷新服务即可解决!!!")
        }
    }

    fun getJarLibPath(serviceName: String, version: String): String {
        val apiJarPath = System.getProperty(Constant.USER_HOME)
        val versionDirName = version.replace("\\.".toRegex(), "_")
        val dir = File(apiJarPath + File.separator + versionDirName + "_" + serviceName)
        return dir.absolutePath + File.separator + "lib"
    }

    fun getApiFilePath(serviceName: String, gav: GAV): String {
        val jarPath = getJarLibPath(serviceName, gav.version!!)
        return jarPath + File.separator + gav.artifactID + ".jar"
    }

    @JvmStatic
    val allClassLoader: Map<String, ApiJarClassLoader>
        get() = LOADER_MAP

    fun getJarLibUrls(jarPath: String): MutableList<URL> {
        val jarLibFiles = getJarLibFiles(jarPath)

        return jarLibFiles.stream()
            .map { obj: File -> getFileUrls(obj) }
            .filter { obj: URL? -> Objects.nonNull(obj) }
            .collect(Collectors.toList())!!
    }

    private fun getJarLibFiles(jarPath: String): List<File> {
        val baseFile = File(jarPath)
        val files = baseFile.listFiles() ?: return emptyList()
        return Arrays.stream(files).collect(Collectors.toList())
    }

    private fun getFileUrls(jarFile: File): URL? {
        var urls: URL? = null
        try {
            urls = jarFile.toURI().toURL()
        } catch (e: MalformedURLException) {
            logger.warn(jarFile.absolutePath + "转换为url失败", e)
        }
        return urls
    }

    fun initClassLoader(serviceName: String, version: String): ApiJarClassLoader {
        val jarLibPath = getJarLibPath(serviceName, version)
        val libUrlList = getJarLibUrls(jarLibPath)
        return ApiJarClassLoader(libUrlList.toTypedArray<URL>())
    }

    /**
     * 通过ApiJarClassLoader加载所有的接口,同时解析接口里面的所有方法构建运行时类信息,添加到invokeContext里面
     */
    private fun doLoadInterfaceInfoAndInitLoader(service: PostmanService) {
        val serviceKey = buildServiceKey(service.getCluster(), service.getServiceName())

        val oldClzLoader = LOADER_MAP[serviceKey]
        if (oldClzLoader != null) {
            logger.info("销毁旧加载器:{}", oldClzLoader.javaClass)
            oldClzLoader.close()
        }

        val oldGavVersion = get<String>(ThreadLocalUtils.OLD_GAV_VERSION)
        if (StringUtils.isNotBlank(oldGavVersion)) {
            val jarLibPath = getJarLibPath(service.getServiceName(), oldGavVersion!!)
            val parentFile = File(jarLibPath).parentFile
            FileUtils.deleteDirectory(parentFile)
            remove(ThreadLocalUtils.OLD_GAV_VERSION)
            logger.info("完成删除旧版本服务目录:{}", parentFile)
        }

        val apiJarClassLoader = initClassLoader(service.getServiceName(), service.getGav().version!!)
        LOADER_MAP[serviceKey] = apiJarClassLoader
        logger.info("初始化新加载器:{}", apiJarClassLoader.javaClass)

        for (interfaceModel in service.getInterfaceModels()) {
            val methodNames = interfaceModel.methodNames
            var clazz: Class<*>
            try {
                clazz = apiJarClassLoader.loadClassWithResolve(interfaceModel.interfaceName!!)
            } catch (e: ClassNotFoundException) {
                logger.error("找不到class,有可能是注册了dubbo服务,但是class文件却没放到api包里暴露出去:{}", e.message)
                continue
            }
            val methods = clazz.declaredMethods
            //清空之前的内容，应用在重启的时候会重新load class,所以需要把原来的clear,再加进去一次，这个以后可以优化
            interfaceModel.methods = mutableListOf()
            for (method in methods) {
                //只加载应用注册到zk里面的方法
                if (!methodNames.contains(method.name) || !Modifier.isPublic(method.modifiers)) {
                    continue
                }
                val methodModel = MethodEntity()
                //设置运行时信息
                methodModel.method = method
                val paramStr = StringBuilder("(")
                val requestParamList: MutableList<RequestParam> = ArrayList()
                //如果没有参数，表示空参数，在调用的时候是空数组，没有问题
                for (parameter in method.parameters) {
                    val paramModel = ParamEntity()
                    paramModel.name = parameter.name
                    paramModel.type = parameter.parameterizedType.typeName
                    val wholeName = parameter.parameterizedType.typeName
                    val simpleName = wholeName.substring(wholeName.lastIndexOf(".") + 1)
                    paramStr.append(simpleName).append(",")
                    methodModel.params.addLast(paramModel)
                    val requestParam = RequestParam()
                    requestParam.paraName = parameter.name
                    requestParam.targetParaType = parameter.type
                    requestParamList.add(requestParam)
                }
                val allParamsName = if (paramStr.length == 1) {
                    "$paramStr)"
                } else {
                    paramStr.substring(0, paramStr.length - 1) + ")"
                }
                interfaceModel.methods.addLast(methodModel)
                val extendMethodName = method.name + allParamsName
                methodModel.name = extendMethodName
                val methodKey = buildMethodNameKey(
                    service.getCluster(),
                    service.getServiceName(),
                    interfaceModel.group!!,
                    interfaceModel.interfaceName!!,
                    interfaceModel.version,
                    methodModel.name!!
                )
                putMethod(methodKey, requestParamList)
            }
            //设置运行时信息,主要用于在页面请求的时候可以通过json序列化为json string的格式
            interfaceModel.interfaceClass = clazz
        }
        logger.info(
            "完成载入服务{}共{}个接口到InvokeContext",
            service.getServiceName(),
            service.getInterfaceModels().size
        )
        service.setLoadedToClassLoader(true)
    }
}
