package org.javamaster.invocationlab.admin.service.impl

import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.consts.ErdConst
import org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX
import org.javamaster.invocationlab.admin.consts.ErdConst.PROJECT_ROLE_GROUP_USERS
import org.javamaster.invocationlab.admin.consts.ErdConst.PROJECT_STATISTIC
import org.javamaster.invocationlab.admin.consts.ErdConst.USER_PROJECT_ROLE_GROUP
import org.javamaster.invocationlab.admin.enums.ProjectType
import org.javamaster.invocationlab.admin.enums.ProjectType.Companion.getByType
import org.javamaster.invocationlab.admin.enums.RoleGroupEnum
import org.javamaster.invocationlab.admin.handler.WebSocketHandler
import org.javamaster.invocationlab.admin.model.erd.*
import org.javamaster.invocationlab.admin.pdf.ImageTagProcessor
import org.javamaster.invocationlab.admin.pdf.MyFontProvider
import org.javamaster.invocationlab.admin.service.DbService
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService
import org.javamaster.invocationlab.admin.service.ErdOnlineRoleService
import org.javamaster.invocationlab.admin.util.DbUtils.getDefaultDb
import org.javamaster.invocationlab.admin.util.ErdUtils.getPair
import org.javamaster.invocationlab.admin.util.SessionUtils.getFromSession
import org.javamaster.invocationlab.admin.util.SessionUtils.saveToSession
import com.alibaba.fastjson.JSONObject
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.tool.xml.Pipeline
import com.itextpdf.tool.xml.XMLWorker
import com.itextpdf.tool.xml.XMLWorkerHelper
import com.itextpdf.tool.xml.css.CssFilesImpl
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver
import com.itextpdf.tool.xml.html.CssAppliersImpl
import com.itextpdf.tool.xml.html.HTML
import com.itextpdf.tool.xml.html.Tags
import com.itextpdf.tool.xml.parser.XMLParser
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Pair
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.web.util.HtmlUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Collectors

/**
 * @author yudong
 */
@Service
class ErdOnlineProjectServiceImpl : ErdOnlineProjectService {
    @Autowired
    private lateinit var stringRedisTemplate: StringRedisTemplate

    @Autowired
    private lateinit var redisTemplateJackson: RedisTemplate<String, Any>

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var erdOnlineRoleService: ErdOnlineRoleService

    @Suppress("unused")
    @Autowired
    private lateinit var webSocketHandler: WebSocketHandler


    override fun statistic(): StatisticVo {
        val map = redisTemplateJackson.opsForHash<Any, Any>().entries(PROJECT_STATISTIC)
        val sum = map.values.stream()
            .mapToInt { vo: Any ->
                val statisticVo = vo as StatisticVo
                statisticVo.total!!
            }
            .sum()
        val statisticVo = StatisticVo()
        statisticVo.total = if (sum == 0) 38 else sum
        return statisticVo
    }


    override fun getProjectList(tokenVo: TokenVo, projectType: ProjectType, projectName: String?): PageVo {
        return getUserPageVo(tokenVo.userId, projectType, projectName)
    }


    override fun saveRecordsVoToGroupUsers(
        projectId: String, reqUsersVos: List<UsersVo>,
        roleGroupEnum: RoleGroupEnum, tokenVo: TokenVo
    ) {
        val projectType = ProjectType.GROUP
        val recordsVo = findRecordsVoByProjectId(projectId, tokenVo)
        for (reqUsersVo in reqUsersVos) {
            val pageVo = getUserPageVo(reqUsersVo.id, projectType, null)
            val optional = pageVo.records!!.stream()
                .filter { userRecordsVo: RecordsVo -> userRecordsVo.id == recordsVo.id }
                .findAny()
            if (optional.isPresent) {
                val userRecordsVo = optional.get()
                BeanUtils.copyProperties(recordsVo, userRecordsVo)
            } else {
                pageVo.records!!.add(recordsVo)
            }
            pageVo.total = pageVo.records!!.size
            saveUserPageVo(pageVo, reqUsersVo.id, projectType)
            redisTemplateJackson.opsForHash<Any, Any>().put(
                USER_PROJECT_ROLE_GROUP + reqUsersVo.id,
                projectId, roleGroupEnum.name
            )
        }
    }


    override fun delRecordsVoFromGroupUsers(projectId: String, reqUsersVos: List<UsersVo>, tokenVo: TokenVo) {
        val projectType = ProjectType.GROUP
        for (reqUsersVo in reqUsersVos) {
            val pageVo = getUserPageVo(reqUsersVo.id, projectType, null)
            removeProjectFromRecords(pageVo.records, projectId)
            pageVo.total = pageVo.records!!.size
            saveUserPageVo(pageVo, reqUsersVo.id, projectType)
            redisTemplateJackson.opsForHash<Any, Any>().delete(USER_PROJECT_ROLE_GROUP + reqUsersVo.id, projectId)
        }
    }


    private fun getUserPageVo(userId: String?, projectType: ProjectType, projectName: String?): PageVo {
        val pair = getPair(projectType, userId!!)
        var jsonDataStr = stringRedisTemplate.opsForHash<Any, Any>()[pair.left, pair.right] as String?
        if (jsonDataStr == null) {
            jsonDataStr = PAGE_JSON_STR
        }
        val pageVo = objectMapper.readValue(jsonDataStr, PageVo::class.java)
        if (StringUtils.isNotBlank(projectName)) {
            pageVo.records!!.removeIf { recordsVo: RecordsVo ->
                !recordsVo.projectName!!.contains(
                    projectName!!
                )
            }
            pageVo.total = pageVo.records!!.size
        }
        return pageVo
    }


    private fun saveUserPageVo(pageVo: PageVo, userId: String?, projectType: ProjectType) {
        val pair = getPair(projectType, userId!!)
        stringRedisTemplate.opsForHash<Any, Any>()
            .put(pair.left, pair.right, objectMapper.writeValueAsString(pageVo))
    }


    override fun findRecordsVoByProjectId(projectId: String, tokenVo: TokenVo): RecordsVo {
        val records = findRecords(ProjectType.PERSONAL, tokenVo)
        records.addAll(findRecords(ProjectType.GROUP, tokenVo))
        return records.stream()
            .filter { recordsVo: RecordsVo -> recordsVo.id == projectId }
            .findAny()
            .get()
    }


    private fun findRecords(projectType: ProjectType, tokenVo: TokenVo): MutableList<RecordsVo> {
        val pageVo = getUserPageVo(tokenVo.userId, projectType, null)
        return pageVo.records!!
    }


    override fun getProjectDetail(projectId: String, tokenVo: TokenVo): ErdOnlineModel {
        saveToSession("$SESSION_SAVE_KEY:$projectId", getRedisModCount(projectId))

        val jsonDataStr = stringRedisTemplate.opsForValue()[ERD_PREFIX + projectId]
        val erdOnlineModel = objectMapper.readValue(jsonDataStr, ErdOnlineModel::class.java)

        eraseSensitiveInfo(erdOnlineModel, tokenVo.userId!!)

        return erdOnlineModel
    }

    override fun eraseSensitiveInfo(erdOnlineModel: ErdOnlineModel, userId: String) {
        val defaultDb: DbsBean
        try {
            defaultDb = getDefaultDb(erdOnlineModel)
        } catch (e: Exception) {
            // 没有默认数据源,忽略
            return
        }
        redisTemplateJackson.opsForHash<Any?, Any>().put(ErdConst.PROJECT_DS, erdOnlineModel.id!!, defaultDb)

        val userRoleGroup = erdOnlineRoleService.getUserRoleGroup(userId, erdOnlineModel.id!!)
        if (userRoleGroup != RoleGroupEnum.OWNER) {
            val properties = defaultDb.properties
            properties!!.username = "*****"
            properties.password = "*****"
            properties.url = "*****"
            properties.cipherType = "*****"
            properties.cipherKey = "*****"
        }
    }

    override fun resumeSensitiveInfo(erdOnlineModel: ErdOnlineModel, userId: String) {
        val userRoleGroup = erdOnlineRoleService.getUserRoleGroup(userId, erdOnlineModel.id!!) ?: return
        if (userRoleGroup != RoleGroupEnum.OWNER) {
            try {
                val defaultDb =
                    redisTemplateJackson.opsForHash<Any, Any>()[ErdConst.PROJECT_DS, erdOnlineModel.id!!] as DbsBean?
                val dbsBean = getDefaultDb(erdOnlineModel)
                BeanUtils.copyProperties(defaultDb!!, dbsBean)
            } catch (e: Exception) {
                // 没有默认数据源,忽略
            }
        }
    }


    override fun getDefaultDb(projectId: String, tokenVo: TokenVo): DbsBean {
        val defaultDb = redisTemplateJackson.opsForHash<Any, Any>()[ErdConst.PROJECT_DS, projectId] as DbsBean?
        if (defaultDb != null) {
            return defaultDb
        }
        val erdOnlineModel = getProjectDetail(projectId, tokenVo)
        return getDefaultDb(erdOnlineModel)
    }


    private fun saveProjectDetail(erdOnlineModel: ErdOnlineModel?, tokenVo: TokenVo) {
        resumeSensitiveInfo(erdOnlineModel!!, tokenVo.userId!!)
        stringRedisTemplate.opsForValue()[ERD_PREFIX + erdOnlineModel.id] =
            objectMapper.writeValueAsString(erdOnlineModel)
        val sum = erdOnlineModel.projectJSON!!.modules!!.stream()
            .mapToInt { module: ModulesBean -> module.entities!!.size }
            .sum()
        val statisticVo = StatisticVo()
        statisticVo.total = sum
        redisTemplateJackson.opsForHash<Any?, Any>().put(PROJECT_STATISTIC, erdOnlineModel.id!!, statisticVo)
    }


    override fun createProject(jsonObjectReq: JSONObject, tokenVo: TokenVo): String {
        val projectType = getByType(jsonObjectReq.getInteger("type"))
        var projectId = UUID.randomUUID().toString().replace("-", "")
        projectId = projectType.type.toString() + "0" + projectId.substring(2)
        val recordsVo = RecordsVo()
        recordsVo.id = projectId
        recordsVo.projectName = jsonObjectReq.getString("projectName")
        recordsVo.description = jsonObjectReq.getString("description")
        recordsVo.tags = jsonObjectReq.getString("tags")
        recordsVo.creator = tokenVo.userId
        val now = Date()
        recordsVo.createTime = now
        recordsVo.updater = tokenVo.userId
        recordsVo.updateTime = now
        recordsVo.type = projectType.type
        val pageVo = getUserPageVo(tokenVo.userId, projectType, null)
        val records = pageVo.records
        records!!.add(recordsVo)
        pageVo.total = records.size
        saveUserPageVo(pageVo, tokenVo.userId, projectType)

        val erdOnlineModel = objectMapper.readValue(jsonObjectReq.toJSONString(), ErdOnlineModel::class.java)
        erdOnlineModel.id = projectId
        erdOnlineModel.projectName = recordsVo.projectName
        erdOnlineModel.type = projectType.type.toString() + ""
        saveProjectDetail(erdOnlineModel, tokenVo)

        erdOnlineRoleService.saveRolesWhenCreateProject(projectId, tokenVo)
        return "操作成功"
    }


    override fun deleteProject(jsonObjectReq: JSONObject, tokenVo: TokenVo): String {
        val projectId = jsonObjectReq.getString("id")
        val projectType = getByType(jsonObjectReq.getIntValue("type"))

        val pageVo = getUserPageVo(tokenVo.userId, projectType, null)
        removeProjectFromRecords(pageVo.records, projectId)
        pageVo.total = pageVo.records!!.size
        saveUserPageVo(pageVo, tokenVo.userId, projectType)

        if (projectType == ProjectType.GROUP) {
            delRelateUserRecord(projectId, tokenVo, RoleGroupEnum.ADMIN)
            delRelateUserRecord(projectId, tokenVo, RoleGroupEnum.ORDINARY)
        }

        stringRedisTemplate.delete(ERD_PREFIX + projectId)

        erdOnlineRoleService.delRolesWhenDelProject(projectId, tokenVo)
        return "操作成功"
    }

    private fun removeProjectFromRecords(records: MutableList<RecordsVo>?, projectId: String) {
        if (records == null) {
            return
        }
        records.removeIf(Predicate<RecordsVo> { recordsVo: RecordsVo -> recordsVo.id == projectId })
    }


    override fun updateProjectBasicInfo(recordsVoReq: RecordsVo, tokenVo: TokenVo): String {
        val projectType = getByType(recordsVoReq.type)
        val pageVo = getUserPageVo(tokenVo.userId, projectType, null)
        val records = pageVo.records
        records!!.stream()
            .filter { record: RecordsVo -> record.id == recordsVoReq.id }
            .forEach { record: RecordsVo ->
                record.projectName = recordsVoReq.projectName
                record.description = recordsVoReq.description
                record.tags = recordsVoReq.tags
                record.updater = tokenVo.userId
                record.updateTime = Date()
            }
        saveUserPageVo(pageVo, tokenVo.userId, projectType)

        val erdOnlineModel = getProjectDetail(recordsVoReq.id!!, tokenVo)
        erdOnlineModel.projectName = recordsVoReq.projectName
        saveProjectDetail(erdOnlineModel, tokenVo)

        if (projectType == ProjectType.GROUP) {
            updateRelateUserRecord(recordsVoReq.id!!, tokenVo, RoleGroupEnum.ADMIN)
            updateRelateUserRecord(recordsVoReq.id!!, tokenVo, RoleGroupEnum.ORDINARY)
        }
        return "操作成功"
    }

    private fun updateRelateUserRecord(projectId: String, tokenVo: TokenVo, roleGroupEnum: RoleGroupEnum) {
        @Suppress("UNCHECKED_CAST")
        val usersVos =
            redisTemplateJackson.opsForHash<Any, Any>()[PROJECT_ROLE_GROUP_USERS + projectId, roleGroupEnum.name] as List<UsersVo>?
        if (usersVos.isNullOrEmpty()) {
            return
        }
        saveRecordsVoToGroupUsers(projectId, usersVos, roleGroupEnum, tokenVo)
    }

    private fun delRelateUserRecord(projectId: String, tokenVo: TokenVo, roleGroupEnum: RoleGroupEnum) {
        @Suppress("UNCHECKED_CAST")
        val usersVos =
            redisTemplateJackson.opsForHash<Any, Any>()[PROJECT_ROLE_GROUP_USERS + projectId, roleGroupEnum.name] as List<UsersVo>?
        if (usersVos.isNullOrEmpty()) {
            return
        }
        delRecordsVoFromGroupUsers(projectId, usersVos, tokenVo)
    }

    private fun getRedisModCount(projectId: String?): Long {
        return stringRedisTemplate.execute { connection: RedisConnection ->
            var redisModCount = 1L
            val bytes = connection.hashCommands().hGet(
                REDIS_SAVE_KEY.toByteArray(StandardCharsets.UTF_8),
                projectId!!.toByteArray(StandardCharsets.UTF_8)
            )
            if (bytes != null) {
                redisModCount = String(bytes, StandardCharsets.UTF_8).toLong()
            }
            redisModCount
        }!!
    }

    private fun checkDataChanged(projectId: String?) {
        val redisModCount = getRedisModCount(projectId)
        val sessionModCount = getFromSession<Long>("$SESSION_SAVE_KEY:$projectId")
        if (redisModCount != sessionModCount) {
            throw ErdException(
                405,
                "服务器数据已发生变化，为避免互相覆盖导致数据丢失，所有保存都将失败。请刷新页面后再重试！"
            )
        }
    }

    private fun increaseModCount(projectId: String?, connection: RedisConnection) {
        val newSessionModCount = connection.hashCommands().hIncrBy(
            REDIS_SAVE_KEY.toByteArray(StandardCharsets.UTF_8),
            projectId!!.toByteArray(StandardCharsets.UTF_8), 1
        )
        saveToSession("$SESSION_SAVE_KEY:$projectId", newSessionModCount)
    }

    @Synchronized
    override fun saveProject(saveProjectVo: SaveProjectVo, tokenVo: TokenVo): Boolean {
        val erdOnlineModelReq = saveProjectVo.erdOnlineModel
        return stringRedisTemplate.execute<Boolean> { connection: RedisConnection ->
            checkDataChanged(erdOnlineModelReq!!.id)
            try {
                validateRelation(erdOnlineModelReq, tokenVo)

                increaseModCount(erdOnlineModelReq.id, connection)

                saveProjectDetail(erdOnlineModelReq, tokenVo)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            true
        }!!
    }


    override fun sortModule(reqVo: SortModuleReqVo, tokenVo: TokenVo): String {
        val sortMap: MutableMap<String?, Int> = Maps.newHashMap()
        val sortModuleVos = reqVo.sortModuleVos
        for (i in sortModuleVos!!.indices) {
            sortMap[sortModuleVos[i].name] = i
        }
        val erdOnlineModel = getProjectDetail(reqVo.projectId!!, tokenVo)
        val modules = erdOnlineModel.projectJSON!!.modules
        for (module in modules!!) {
            val sort = sortMap[module.name]
            module.sort = sort
        }
        modules.sortWith(Comparator { a, b -> a.sort!!.compareTo(b.sort!!) })

        return stringRedisTemplate.execute<String> { connection: RedisConnection ->
            checkDataChanged(erdOnlineModel.id)
            increaseModCount(erdOnlineModel.id, connection)

            try {
                saveProjectDetail(erdOnlineModel, tokenVo)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            "调整顺序成功,即将重新载入页面!"
        }!!
    }

    private fun validateRelation(erdOnlineModelReq: ErdOnlineModel?, tokenVo: TokenVo) {
        erdOnlineModelReq!!.projectJSON!!.modules!!
            .forEach(Consumer forEach@{ modulesBean: ModulesBean ->
                val entityTitles: Set<String> = modulesBean.entities!!.stream()
                    .map(EntitiesBean::title)
                    .collect(
                        Collectors.toCollection { LinkedHashSet() }
                    )
                val nodeTitles = modulesBean.graphCanvas!!.nodes!!.stream()
                    .map(NodesBean::title)
                    .collect(Collectors.toSet())

                val entitySize = entityTitles.size
                val nodeSize = nodeTitles.size
                val tmp = Sets.newHashSet(entityTitles)
                tmp.removeAll(nodeTitles)
                if (tmp.size + nodeSize == entitySize) {
                    return@forEach
                }
                try {
                    if (!isModifyTableName(erdOnlineModelReq.id, modulesBean, tokenVo, entityTitles, tmp)) {
                        throw ErdException("检测到关系图数据出现错乱,保存失败,请刷新页面后再重试!")
                    }
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            })
    }


    private fun isModifyTableName(
        projectId: String?, modulesBean: ModulesBean, tokenVo: TokenVo,
        entityTitles: Set<String>, needCheckTitles: Set<String>
    ): Boolean {
        val tableNames: List<String> = Lists.newArrayList(entityTitles)
        val dbModules = getProjectDetail(projectId!!, tokenVo).projectJSON!!.modules
        for (dbModulesBean in dbModules!!) {
            if (dbModulesBean.name != modulesBean.name) {
                continue
            }
            outer@ for (needCheckTitle in needCheckTitles) {
                for (i in tableNames.indices) {
                    if (tableNames[i] != needCheckTitle) {
                        continue
                    }
                    val dbEntitiesBean = dbModulesBean.entities!![i]
                    val dbFieldNames = dbEntitiesBean.fields!!.stream()
                        .map(FieldsBean::name)
                        .collect(Collectors.toSet())

                    val entitiesBean = modulesBean.entities!![i]
                    val fieldNames = entitiesBean.fields!!.stream()
                        .map(FieldsBean::name)
                        .collect(Collectors.toSet())

                    if (!org.apache.commons.collections4.CollectionUtils.isEqualCollection(dbFieldNames, fieldNames)) {
                        return false
                    }

                    continue@outer
                }
            }
        }
        return true
    }


    override fun refreshProjectModule(jsonObjectReq: JSONObject, tokenVo: TokenVo): ModulesBean {
        val projectId = jsonObjectReq.getString("id")
        val moduleName = jsonObjectReq.getString("moduleName")

        val erdOnlineModel = getProjectDetail(projectId, tokenVo)
        resumeSensitiveInfo(erdOnlineModel, tokenVo.userId!!)

        val dbsBean = getDefaultDb(projectId, tokenVo)
        val dbService = DbService.getInstance(dbsBean.select!!)

        val modulesBean = dbService.refreshModule(erdOnlineModel, moduleName)

        saveProjectDetail(erdOnlineModel, tokenVo)
        return modulesBean
    }


    override fun getProjectBasicInfo(projectId: String, tokenVo: TokenVo): GroupGetVo {
        val recordsVo = findRecordsVoByProjectId(projectId, tokenVo)
        val groupGetVo = GroupGetVo()
        groupGetVo.id = recordsVo.id
        groupGetVo.configJSON = ""
        groupGetVo.projectJSON = ""
        groupGetVo.projectName = recordsVo.projectName
        groupGetVo.description = recordsVo.description
        groupGetVo.type = recordsVo.type.toString() + ""
        groupGetVo.tags = recordsVo.tags
        groupGetVo.delFlag = "0"
        groupGetVo.creator = recordsVo.creator
        groupGetVo.createTime = recordsVo.createTime
        groupGetVo.updater = recordsVo.updater
        groupGetVo.updateTime = recordsVo.updateTime
        return groupGetVo
    }


    override fun exportErd(jsonObjectReq: JSONObject, tokenVo: TokenVo): Pair<String, ByteArray> {
        val projectId = jsonObjectReq.getString("projectId")
        val erdOnlineModel = getProjectDetail(projectId, tokenVo)

        @Suppress("UNCHECKED_CAST")
        val imageMap = jsonObjectReq["imgs"] as MutableMap<String, String>
        imageMap.replaceAll { _: String?, v: String -> "data:image/png;base64,$v" }
        val context = initVelocityContext(erdOnlineModel, imageMap)
        val tpl = Velocity.getTemplate("erd.vm", "UTF-8")
        val stringWriter = StringWriter()
        tpl.merge(context, stringWriter)
        val bytes = convertHtmlToPdf(stringWriter.toString())
        return Pair.of(erdOnlineModel.projectName, bytes)
    }

    @Suppress("unused")
    fun htmlEscape(s: String?): String {
        return HtmlUtils.htmlEscape(s!!)
    }

    @Suppress("unused")
    fun getColumnType(database: List<DatabaseBean?>, datatype: List<DatatypeBean>, type: String): String {
        val fieldTypes: MutableSet<String?> = Sets.newHashSet()
        for (databaseBean in database) {
            fieldTypes.add(getFieldType(datatype, type))
        }
        return StringUtils.join(fieldTypes, "/")
    }

    fun getFieldType(datatype: List<DatatypeBean>, type: String): String? {
        val datatypeBeans = datatype.stream()
            .filter { it: DatatypeBean -> it.code == type }.collect(Collectors.toList())
        if (datatypeBeans.isEmpty()) {
            return type
        }
        return datatypeBeans[0].apply!!.mYSQL!!.type
    }

    companion object {
        init {
            initVelocity()
        }

        private const val REDIS_SAVE_KEY: String = ERD_PREFIX + "save:modCountRedis"
        private const val SESSION_SAVE_KEY = "erd:save:modCount"
        private const val PAGE_JSON_STR =
            "{\"records\":[],\"total\":0,\"size\":100,\"current\":1,\"orders\":[],\"searchCount\":true,\"pages\":1}"

        fun convertHtmlToPdf(html: String): ByteArray {
            ByteArrayOutputStream().use { out ->
                val document = Document()

                val writer = PdfWriter.getInstance(document, out)

                val bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", false)
                val myFontProvider = MyFontProvider(
                    BaseColor.BLACK, "", "",
                    false, size = 10f, style = 1, baseFont = bfChinese
                )

                // 为该Document创建一个Writer实例
                val pdfwriter = PdfWriter.getInstance(document, out)
                pdfwriter.setViewerPreferences(PdfWriter.HideToolbar)
                // 打开当前的document
                document.open()
                val tagProcessorFactory = Tags.getHtmlTagProcessorFactory()
                tagProcessorFactory.removeProcessor(HTML.Tag.IMG)
                tagProcessorFactory.addProcessor(ImageTagProcessor(), HTML.Tag.IMG)

                val cssFiles = CssFilesImpl()
                cssFiles.add(XMLWorkerHelper.getInstance().defaultCSS)
                val cssResolver = StyleAttrCSSResolver(cssFiles)
                val hpc = HtmlPipelineContext(CssAppliersImpl(myFontProvider))
                hpc.setAcceptUnknown(true).autoBookmark(true).setTagFactory(tagProcessorFactory)
                val htmlPipeline = HtmlPipeline(hpc, PdfWriterPipeline(document, writer))
                val pipeline: Pipeline<*> = CssResolverPipeline(cssResolver, htmlPipeline)

                val worker = XMLWorker(pipeline, true)

                val charset = StandardCharsets.UTF_8
                val xmlParser = XMLParser(true, worker, charset)

                ByteArrayInputStream(html.toByteArray(StandardCharsets.UTF_8)).use {
                    xmlParser.parse(it, charset)
                }

                document.close()
                return out.toByteArray()
            }
        }

        fun initVelocityContext(erdOnlineModel: ErdOnlineModel, images: Map<String, String>?): VelocityContext {
            val context = VelocityContext()
            context.put("projectName", erdOnlineModel.projectName)
            context.put("modules", erdOnlineModel.projectJSON!!.modules)
            context.put("images", images)
            context.put("datatypes", erdOnlineModel.projectJSON!!.dataTypeDomains!!.datatype)
            var databases = erdOnlineModel.projectJSON!!.dataTypeDomains!!.database
            databases = databases!!.stream()
                .filter { it: DatabaseBean ->
                    val fileShow = it.fileShow ?: return@filter false
                    fileShow
                }
                .collect(Collectors.toList())
            context.put("databases", databases)
            val str = databases.stream()
                .map(DatabaseBean::code)
                .collect(Collectors.joining("/"))
            context.put("databaseColumn", str)
            context.internalPut("helperObj", ErdOnlineProjectServiceImpl())
            return context
        }

        private fun initVelocity() {
            val prop = Properties()
            prop["file.resource.loader.class"] = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader"

            Velocity.init(prop)
        }
    }
}
