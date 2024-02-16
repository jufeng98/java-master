package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.annos.ErdRolesAllowed
import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.config.GlobalLog.Companion.log
import org.javamaster.invocationlab.admin.enums.RoleEnum
import org.javamaster.invocationlab.admin.model.ResultVo
import org.javamaster.invocationlab.admin.model.erd.*
import org.javamaster.invocationlab.admin.service.ErdOnlineQueryService
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.tuple.Triple
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriUtils
import java.nio.charset.StandardCharsets

/**
 * @author yudong
 */
@RestController
@RequestMapping(value = ["/ncnb"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ErdOnlineQueryController {

    @Autowired
    private lateinit var erdOnlineQueryService: ErdOnlineQueryService

    @RequestMapping(value = ["/hisProject/load"], method = [RequestMethod.GET, RequestMethod.POST])
    fun load(@RequestBody jsonObjectReq: JSONObject): ResultVo<JSONArray> {
        val projectId = jsonObjectReq.getString("projectId")
        return ResultVo.success(erdOnlineQueryService.load(projectId))
    }

    @RequestMapping(value = ["/queryHistory"], method = [RequestMethod.GET])
    fun queryHistory(reqVo: CommonErdVo, @SessionAttribute("tokenVo") tokenVo: TokenVo): ResultVo<JSONObject> {
        return ResultVo.success(erdOnlineQueryService.queryHistory(reqVo, tokenVo))
    }

    @RequestMapping(value = ["/queryInfo/tree"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getQueryTreeList(projectId: String): ResultVo<List<Tree>> {
        return ResultVo.success(erdOnlineQueryService.getQueryTreeList(projectId))
    }

    @RequestMapping(value = ["/queryInfo"], method = [RequestMethod.GET, RequestMethod.POST])
    fun createDirOrQuery(
        @RequestBody reqVo: QueryReqVo,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<Boolean> {
        return ResultVo.success(erdOnlineQueryService.createDirOrQuery(reqVo, tokenVo))
    }

    @RequestMapping(value = ["/queryInfo/{projectId}/{treeNodeId}"], method = [RequestMethod.DELETE])
    fun deleteQueryTree(@PathVariable projectId: String, @PathVariable treeNodeId: String): ResultVo<Int> {
        return ResultVo.success(erdOnlineQueryService.deleteQueryTree(projectId, treeNodeId))
    }

    @RequestMapping(value = ["/queryInfo/{treeNodeId}"], method = [RequestMethod.GET])
    fun queryTree(@PathVariable treeNodeId: String): ResultVo<Tree> {
        return ResultVo.success(erdOnlineQueryService.queryTree(treeNodeId))
    }

    @RequestMapping(value = ["/queryInfo/{treeNodeId}"], method = [RequestMethod.PUT])
    fun saveQueryTree(
        @RequestBody reqVo: SaveQueryReqVo,
        @PathVariable treeNodeId: String,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<Tree> {
        reqVo.treeNodeId = treeNodeId
        val resultVo: ResultVo<Tree> = ResultVo.success(erdOnlineQueryService.saveQueryTree(reqVo, tokenVo))
        resultVo.msg = "保存SQL成功!"
        return resultVo
    }

    @RequestMapping(value = ["/getDbs"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getDbs(
        @RequestBody jsonObjectReq: JSONObject,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<List<String>> {
        val projectId = jsonObjectReq.getString("projectId")
        return ResultVo.success(erdOnlineQueryService.getDbs(projectId, tokenVo))
    }

    @RequestMapping(value = ["/getTables"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getTables(
        @RequestBody reqVo: CommonErdVo,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<List<Table>> {
        return ResultVo.success(erdOnlineQueryService.getTables(reqVo, tokenVo))
    }

    @RequestMapping(value = ["/getTableColumns"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getTableColumns(
        @RequestBody reqVo: CommonErdVo,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<List<Column>> {
        return ResultVo.success(erdOnlineQueryService.getTableColumns(reqVo, tokenVo))
    }

    @ErdRolesAllowed(value = [RoleEnum.ERD_SQL_EXECUTE], msg = "没有执行SQL的权限")
    @PostMapping(value = ["/queryInfo/exec", "/queryInfo/explain"])
    fun execSql(
        @RequestBody reqVo: CommonErdVo, request: HttpServletRequest,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<SqlExecResVo> {
        reqVo.isExport = false
        reqVo.explain = request.requestURI.contains("explain")
        reqVo.sql = reqVo.sql!!.trim { it <= ' ' }
        return ResultVo.success(erdOnlineQueryService.execSql(reqVo, tokenVo))
    }

    @ErdRolesAllowed(value = [RoleEnum.ERD_SQL_EXECUTE], msg = "没有执行SQL的权限")
    @PostMapping("/queryInfo/getTableRecordTotal")
    fun getTableRecordTotal(
        @RequestBody reqVo: CommonErdVo,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<Int> {
        return ResultVo.success(erdOnlineQueryService.getTableRecordTotal(reqVo, tokenVo))
    }

    @ErdRolesAllowed(value = [RoleEnum.ERD_SQL_EXECUTE], msg = "没有执行SQL的权限")
    @PostMapping("execUpdate")
    fun execUpdate(
        @RequestBody reqVo: CommonErdVo,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<List<SqlExecResVo>> {
        reqVo.isExport = false
        reqVo.explain = false
        var list: List<SqlExecResVo> = emptyList()
        try {
            list = erdOnlineQueryService.execUpdate(reqVo, tokenVo)
        } catch (e: ErdException) {
            log.error("执行错误:{}", reqVo, e)
            val resultVo: ResultVo<List<SqlExecResVo>> = ResultVo.success(list)
            resultVo.code = e.code
            resultVo.msg = e.message
            return resultVo
        }
        return ResultVo.success(list)
    }

    @ErdRolesAllowed(value = [RoleEnum.ERD_SQL_EXECUTE], msg = "没有执行SQL的权限")
    @PostMapping(value = ["/queryInfo/exportSql"])
    fun exportSql(
        @RequestBody reqVo: CommonErdVo,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResponseEntity<ByteArray> {
        reqVo.isExport = true
        reqVo.explain = false
        val triple: Triple<String, MediaType, ByteArray> = erdOnlineQueryService.exportSql(reqVo, tokenVo)
        val headers = HttpHeaders()
        headers.setContentDispositionFormData("attachment", UriUtils.encode(triple.left, StandardCharsets.UTF_8.name()))
        headers.contentType = triple.middle
        return ResponseEntity<ByteArray>(triple.right, headers, HttpStatus.OK)
    }

    @ErdRolesAllowed(value = [RoleEnum.ERD_SQL_EXECUTE])
    @PostMapping("/queryInfo/aes")
    fun aes(@RequestBody reqVo: AesReqVo, @SessionAttribute("tokenVo") tokenVo: TokenVo): ResultVo<String> {
        return ResultVo.success(erdOnlineQueryService.aes(reqVo, tokenVo))
    }
}
