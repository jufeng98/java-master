package org.javamaster.invocationlab.admin.controller

import org.javamaster.invocationlab.admin.annos.ErdRolesAllowed
import org.javamaster.invocationlab.admin.enums.ProjectType
import org.javamaster.invocationlab.admin.enums.ProjectType.Companion.getByType
import org.javamaster.invocationlab.admin.enums.RoleEnum
import org.javamaster.invocationlab.admin.model.ResultVo
import org.javamaster.invocationlab.admin.model.ResultVo.Companion.success
import org.javamaster.invocationlab.admin.model.erd.*
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService
import com.alibaba.fastjson.JSONObject
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * @author yudong
 */
@RestController
@RequestMapping(value = ["/ncnb/project"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ErdOnlineProjectController {
    @Autowired
    private lateinit var erdOnlineProjectService: ErdOnlineProjectService

    @RequestMapping(value = ["/statistic"], method = [RequestMethod.GET, RequestMethod.POST])
    fun statistic(): ResultVo<StatisticVo> {
        return success(erdOnlineProjectService.statistic())
    }

    @RequestMapping(value = ["/recent"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getAllProjectList(
        @RequestParam(required = false) projectName: String = "",
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<PageVo> {
        val groupPage = erdOnlineProjectService.getProjectList(tokenVo, ProjectType.GROUP, projectName)
        val personalPage = erdOnlineProjectService.getProjectList(tokenVo, ProjectType.PERSONAL, projectName)
        groupPage.records!!.addAll(personalPage.records!!)
        groupPage.total = groupPage.total!! + personalPage.total!!
        return success(groupPage)
    }

    @RequestMapping(value = ["/page"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getProjectList(
        @RequestParam(required = false) type: Int?,
        @RequestParam(required = false) projectName: String?,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<PageVo> {
        val projectType = getByType(type)
        return success(erdOnlineProjectService.getProjectList(tokenVo, projectType, projectName))
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_VIEW)
    @RequestMapping(value = ["/info/{id}"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getProjectDetail(
        @PathVariable id: String, response: HttpServletResponse,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<ErdOnlineModel> {
        return success(erdOnlineProjectService.getProjectDetail(id, tokenVo))
    }

    @RequestMapping(value = ["/add"], method = [RequestMethod.GET, RequestMethod.POST])
    fun createProject(
        @RequestBody jsonObjectReq: JSONObject,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<String> {
        return success(erdOnlineProjectService.createProject(jsonObjectReq, tokenVo))
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_GROUP_DEL)
    @RequestMapping(value = ["/delete"], method = [RequestMethod.GET, RequestMethod.POST])
    fun deleteProject(
        @RequestBody jsonObjectReq: JSONObject,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<String> {
        return success(erdOnlineProjectService.deleteProject(jsonObjectReq, tokenVo))
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_GROUP_EDIT)
    @RequestMapping(value = ["/update"], method = [RequestMethod.GET, RequestMethod.POST])
    fun updateProjectBasicInfo(
        @RequestBody recordsVoReq: RecordsVo,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<String> {
        return success(erdOnlineProjectService.updateProjectBasicInfo(recordsVoReq, tokenVo))
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_SAVE)
    @RequestMapping(value = ["/save"], method = [RequestMethod.POST])
    fun saveProject(
        @RequestBody saveProjectVo: SaveProjectVo,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<Boolean> {
        return success(erdOnlineProjectService.saveProject(saveProjectVo, tokenVo))
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_SAVE)
    @RequestMapping(value = ["/sortModule"], method = [RequestMethod.POST])
    fun sortModule(
        @RequestBody reqVo: SortModuleReqVo,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<String> {
        return success(erdOnlineProjectService.sortModule(reqVo, tokenVo))
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_SAVE)
    @RequestMapping(value = ["/refreshProjectModule"], method = [RequestMethod.POST])
    fun refreshProjectModule(
        @RequestBody jsonObjectReq: JSONObject,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<ModulesBean> {
        return success(erdOnlineProjectService.refreshProjectModule(jsonObjectReq, tokenVo))
    }

    @RequestMapping(value = ["/group/get/{projectId}"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getProjectBasicInfo(
        @PathVariable projectId: String,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResultVo<GroupGetVo> {
        return success(erdOnlineProjectService.getProjectBasicInfo(projectId, tokenVo))
    }

    @PostMapping("/exportErd")
    fun exportErd(
        @RequestBody jsonObjectReq: JSONObject,
        response: HttpServletResponse,
        @SessionAttribute("tokenVo") tokenVo: TokenVo
    ): ResponseEntity<ByteArray> {
        val pair = erdOnlineProjectService.exportErd(jsonObjectReq, tokenVo)
        val headers = HttpHeaders()
        val name = pair.left
        val type = jsonObjectReq.getString("type")
        var fileName = ""
        if (type == "Word") {
            fileName = "$name.docx"
            response.characterEncoding = StandardCharsets.UTF_8.name()
            headers.contentType = MediaType("application", "vnd.ms-word")
        } else if (type == "PDF") {
            fileName = "$name.pdf"
            headers.contentType = MediaType.APPLICATION_PDF
        }
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()))
        return ResponseEntity(pair.right, headers, HttpStatus.OK)
    }
}
