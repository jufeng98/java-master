package org.javamaster.invocationlab.admin.service;

import org.javamaster.invocationlab.admin.enums.ProjectType;
import org.javamaster.invocationlab.admin.enums.RoleGroupEnum;
import org.javamaster.invocationlab.admin.model.PageVo;
import org.javamaster.invocationlab.admin.model.RecordsVo;
import org.javamaster.invocationlab.admin.model.StatisticVo;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.GroupGetVo;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.erd.UsersVo;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author yudong
 */
public interface ErdOnlineProjectService {

    StatisticVo statistic() throws Exception;

    PageVo getProjectList(TokenVo tokenVo, ProjectType projectType, String projectName) throws Exception;

    void saveRecordsVoToGroupUsers(String projectId, List<UsersVo> reqUsersVos, RoleGroupEnum roleGroupEnum, TokenVo tokenVo) throws Exception;

    void delRecordsVoFromGroupUsers(String projectId, List<UsersVo> reqUsersVos, TokenVo tokenVo) throws Exception;

    RecordsVo findRecordsVoByProjectId(String projectId, TokenVo tokenVo) throws Exception;

    ErdOnlineModel getProjectDetail(String projectId, TokenVo tokenVo) throws Exception;

    void eraseSensitiveInfo(ErdOnlineModel erdOnlineModel, String userId);

    void resumeSensitiveInfo(ErdOnlineModel erdOnlineModel, String userId);

    String createProject(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception;

    String deleteProject(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception;

    String updateProjectBasicInfo(RecordsVo recordsVoReq, TokenVo tokenVo) throws Exception;

    Boolean saveProject(ErdOnlineModel erdOnlineModelReq, TokenVo tokenVo) throws Exception;

    ModulesBean refreshProjectModule(JSONObject jsonObjectReq, TokenVo tokenVo) throws Exception;

    GroupGetVo getProjectBasicInfo(String projectId, TokenVo tokenVo) throws Exception;
}
