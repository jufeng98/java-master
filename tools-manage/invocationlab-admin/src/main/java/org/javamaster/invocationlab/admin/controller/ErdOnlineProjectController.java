package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.annos.ErdRolesAllowed;
import org.javamaster.invocationlab.admin.enums.ProjectType;
import org.javamaster.invocationlab.admin.enums.RoleEnum;
import org.javamaster.invocationlab.admin.model.ResultVo;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.GroupGetVo;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.PageVo;
import org.javamaster.invocationlab.admin.model.erd.RecordsVo;
import org.javamaster.invocationlab.admin.model.erd.SaveProjectVo;
import org.javamaster.invocationlab.admin.model.erd.SortModuleReqVo;
import org.javamaster.invocationlab.admin.model.erd.StatisticVo;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.service.ErdOnlineProjectService;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author yudong
 */
@RestController
@RequestMapping(value = "/ncnb/project", produces = MediaType.APPLICATION_JSON_VALUE)
public class ErdOnlineProjectController {
    @Autowired
    private ErdOnlineProjectService erdOnlineProjectService;
    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/statistic", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<StatisticVo> statistic() throws Exception {
        return ResultVo.success(erdOnlineProjectService.statistic());
    }

    @RequestMapping(value = {"/recent"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<PageVo> getAllProjectList(@RequestParam(required = false) String projectName,
                                              @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        PageVo groupPage = erdOnlineProjectService.getProjectList(tokenVo, ProjectType.GROUP, projectName);
        PageVo personalPage = erdOnlineProjectService.getProjectList(tokenVo, ProjectType.PERSONAL, projectName);
        groupPage.getRecords().addAll(personalPage.getRecords());
        groupPage.setTotal(groupPage.getTotal() + personalPage.getTotal());
        return ResultVo.success(groupPage);
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
    public ResultVo<ErdOnlineModel> getProjectDetail(@PathVariable String id, HttpServletResponse response,
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
    public ResultVo<Boolean> saveProject(@RequestBody SaveProjectVo saveProjectVo,
                                         @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineProjectService.saveProject(saveProjectVo, tokenVo));
    }

    @ErdRolesAllowed(RoleEnum.ERD_PROJECT_SAVE)
    @RequestMapping(value = "/sortModule", method = {RequestMethod.POST})
    public ResultVo<String> sortModule(@RequestBody SortModuleReqVo reqVo,
                                         @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineProjectService.sortModule(reqVo, tokenVo));
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

    @PostMapping("/exportErd")
    public ResponseEntity<byte[]> exportErd(@RequestBody JSONObject jsonObjectReq,
                                               HttpServletResponse response,
                                               @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        Pair<String, byte[]> pair = erdOnlineProjectService.exportErd(jsonObjectReq, tokenVo);
        HttpHeaders headers = new HttpHeaders();
        String name = pair.getLeft();
        String type = jsonObjectReq.getString("type");
        String fileName = "";
        if (type.equals("Word")) {
            fileName = name + ".docx";
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            headers.setContentType(new MediaType("application", "vnd.ms-word"));
        } else if (type.equals("PDF")) {
            fileName = name + ".pdf";
            headers.setContentType(MediaType.APPLICATION_PDF);
        }
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
        return new ResponseEntity<>(pair.getRight(), headers, HttpStatus.OK);
    }
}
