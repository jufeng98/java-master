package org.javamaster.invocationlab.admin.service

import org.javamaster.invocationlab.admin.enums.ProjectType
import org.javamaster.invocationlab.admin.enums.RoleGroupEnum
import org.javamaster.invocationlab.admin.model.erd.*
import com.alibaba.fastjson.JSONObject
import org.apache.commons.lang3.tuple.Pair

/**
 * @author yudong
 */
interface ErdOnlineProjectService {

    fun statistic(): StatisticVo

    fun getProjectList(tokenVo: TokenVo, projectType: ProjectType, projectName: String?): PageVo

    fun saveRecordsVoToGroupUsers(
        projectId: String,
        reqUsersVos: List<UsersVo>,
        roleGroupEnum: RoleGroupEnum,
        tokenVo: TokenVo
    )

    fun delRecordsVoFromGroupUsers(projectId: String, reqUsersVos: List<UsersVo>, tokenVo: TokenVo)

    fun findRecordsVoByProjectId(projectId: String, tokenVo: TokenVo): RecordsVo

    fun getProjectDetail(projectId: String, tokenVo: TokenVo): ErdOnlineModel

    fun eraseSensitiveInfo(erdOnlineModel: ErdOnlineModel, userId: String)

    fun resumeSensitiveInfo(erdOnlineModel: ErdOnlineModel, userId: String)

    fun getDefaultDb(projectId: String, tokenVo: TokenVo): DbsBean

    fun createProject(jsonObjectReq: JSONObject, tokenVo: TokenVo): String

    fun deleteProject(jsonObjectReq: JSONObject, tokenVo: TokenVo): String

    fun updateProjectBasicInfo(recordsVoReq: RecordsVo, tokenVo: TokenVo): String

    fun saveProject(saveProjectVo: SaveProjectVo, tokenVo: TokenVo): Boolean

    fun refreshProjectModule(jsonObjectReq: JSONObject, tokenVo: TokenVo): ModulesBean

    fun getProjectBasicInfo(projectId: String, tokenVo: TokenVo): GroupGetVo

    fun exportErd(jsonObjectReq: JSONObject, tokenVo: TokenVo): Pair<String, ByteArray>

    fun sortModule(reqVo: SortModuleReqVo, tokenVo: TokenVo): String
}
