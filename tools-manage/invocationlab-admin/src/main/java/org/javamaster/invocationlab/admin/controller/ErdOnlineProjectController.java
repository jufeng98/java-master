package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.annos.ErdRolesAllowed;
import org.javamaster.invocationlab.admin.enums.ProjectType;
import org.javamaster.invocationlab.admin.enums.RoleEnum;
import org.javamaster.invocationlab.admin.model.PageVo;
import org.javamaster.invocationlab.admin.model.RecordsVo;
import org.javamaster.invocationlab.admin.model.ResultVo;
import org.javamaster.invocationlab.admin.model.StatisticVo;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.GroupGetVo;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

/**
 * @author yudong
 */
@RestController
@RequestMapping(value = "/ncnb/project", produces = MediaType.APPLICATION_JSON_VALUE)
public class ErdOnlineProjectController {
    @Autowired
    private ErdOnlineProjectService erdOnlineProjectService;

    @RequestMapping(value = "/statistic", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<StatisticVo> statistic() throws Exception {
        return ResultVo.success(erdOnlineProjectService.statistic());
    }

    @RequestMapping(value = {"/recent"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<PageVo> getAllProjectList(@RequestParam(required = false) String projectName,
                                              @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        PageVo personalPage = erdOnlineProjectService.getProjectList(tokenVo, ProjectType.PERSONAL, projectName);
        PageVo groupPage = erdOnlineProjectService.getProjectList(tokenVo, ProjectType.GROUP, projectName);
        personalPage.getRecords().addAll(groupPage.getRecords());
        personalPage.setTotal(personalPage.getTotal() + groupPage.getTotal());
        return ResultVo.success(personalPage);
    }

    @RequestMapping(value = {"/page"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<PageVo> getProjectList(@RequestParam(required = false) Integer type,
                                           @RequestParam(required = false) String projectName,
                                           @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        ProjectType projectType = ProjectType.getByType(type);
        return ResultVo.success(erdOnlineProjectService.getProjectList(tokenVo, projectType, projectName));
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_VIEW)
    @RequestMapping(value = "/info/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<ErdOnlineModel> getProjectDetail(@PathVariable String id,
                                                     @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineProjectService.getProjectDetail(id, tokenVo));
    }

    @RequestMapping(value = "/add", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<String> createProject(@RequestBody JSONObject jsonObjectReq,
                                          @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineProjectService.createProject(jsonObjectReq, tokenVo));
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_GROUP_DEL)
    @RequestMapping(value = "/delete", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<String> deleteProject(@RequestBody JSONObject jsonObjectReq,
                                          @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineProjectService.deleteProject(jsonObjectReq, tokenVo));
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_GROUP_EDIT)
    @RequestMapping(value = "/update", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<String> updateProjectBasicInfo(@RequestBody RecordsVo recordsVoReq,
                                                   @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineProjectService.updateProjectBasicInfo(recordsVoReq, tokenVo));
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_SAVE)
    @RequestMapping(value = "/save", method = {RequestMethod.POST})
    public ResultVo<Boolean> saveProject(@RequestBody ErdOnlineModel erdOnlineModelReq,
                                         @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineProjectService.saveProject(erdOnlineModelReq, tokenVo));
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_SAVE)
    @RequestMapping(value = "/refreshProjectModule", method = {RequestMethod.POST})
    public ResultVo<ModulesBean> refreshProjectModule(@RequestBody JSONObject jsonObjectReq,
                                                      @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineProjectService.refreshProjectModule(jsonObjectReq, tokenVo));
    }

    @RequestMapping(value = "/group/get/{projectId}", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<GroupGetVo> getProjectBasicInfo(@PathVariable String projectId,
                                                    @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineProjectService.getProjectBasicInfo(projectId, tokenVo));
    }

}
