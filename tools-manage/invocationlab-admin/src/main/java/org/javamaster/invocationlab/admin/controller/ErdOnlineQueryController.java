package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.annos.ErdRolesAllowed;
import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.enums.RoleEnum;
import org.javamaster.invocationlab.admin.model.ResultVo;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.erd.Tree;
import org.javamaster.invocationlab.admin.service.ErdOnlineQueryService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author yudong
 */
@RestController
@RequestMapping(value = "/ncnb", produces = MediaType.APPLICATION_JSON_VALUE)
public class ErdOnlineQueryController {
    @Autowired
    private ErdOnlineQueryService erdOnlineQueryService;

    @RequestMapping(value = "/hisProject/load", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<JSONArray> load(@RequestBody JSONObject jsonObjectReq) throws Exception {
        return ResultVo.success(erdOnlineQueryService.load(jsonObjectReq));
    }

    @RequestMapping(value = "/queryHistory", method = {RequestMethod.GET})
    public ResultVo<JSONObject> queryHistory(String queryId, @SessionAttribute("tokenVo") TokenVo tokenVo) {
        return ResultVo.success(erdOnlineQueryService.queryHistory(queryId, tokenVo));
    }

    @RequestMapping(value = "/queryInfo/tree", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<List<Tree>> getQueryTreeList(String projectId) throws Exception {
        return ResultVo.success(erdOnlineQueryService.getQueryTreeList(projectId));
    }

    @RequestMapping(value = "/queryInfo", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<Boolean> createDirOrQuery(@RequestBody JSONObject jsonObjectReq,
                                              @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineQueryService.createDirOrQuery(jsonObjectReq, tokenVo));
    }

    @RequestMapping(value = "/queryInfo/{projectId}/{treeNodeId}", method = {RequestMethod.DELETE})
    public ResultVo<Integer> deleteQueryTree(@PathVariable String projectId, @PathVariable String treeNodeId) throws Exception {
        return ResultVo.success(erdOnlineQueryService.deleteQueryTree(projectId, treeNodeId));
    }

    @RequestMapping(value = "/queryInfo/{treeNodeId}", method = {RequestMethod.GET})
    public ResultVo<Tree> queryTree(@PathVariable String treeNodeId) throws Exception {
        return ResultVo.success(erdOnlineQueryService.queryTree(treeNodeId));
    }

    @RequestMapping(value = "/queryInfo/{treeNodeId}", method = {RequestMethod.PUT})
    public ResultVo<Tree> saveQueryTree(@RequestBody JSONObject jsonObjectReq, @PathVariable String treeNodeId,
                                        @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineQueryService.saveQueryTree(jsonObjectReq, treeNodeId, tokenVo));
    }

    @RequestMapping(value = {"/getDbs"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<List<String>> getDbs(@RequestBody JSONObject jsonObjectReq, HttpServletRequest request,
                                         @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineQueryService.getDbs(jsonObjectReq, tokenVo));
    }

    @RequestMapping(value = {"/getTables"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<List<String>> getTables(@RequestBody JSONObject jsonObjectReq, HttpServletRequest request,
                                            @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineQueryService.getTableNames(jsonObjectReq, tokenVo));
    }

    @RequestMapping(value = {"/getTableColumns"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<List<Column>> getTableColumns(@RequestBody JSONObject jsonObjectReq, HttpServletRequest request,
                                                  @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineQueryService.getTableColumns(jsonObjectReq, tokenVo));
    }

    @ErdRolesAllowed(value = RoleEnum.ERD_SQL_EXECUTE, msg = "没有执行SQL的权限")
    @RequestMapping(value = {"/queryInfo/exec", "/queryInfo/explain"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<JSONObject> execSql(@RequestBody JSONObject jsonObjectReq, HttpServletRequest request,
                                        @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        boolean explain = request.getRequestURI().contains("explain");
        return ResultVo.success(erdOnlineQueryService.execSql(jsonObjectReq, explain, tokenVo));
    }

    @ErdRolesAllowed(value = RoleEnum.ERD_SQL_EXECUTE, msg = "没有执行SQL的权限")
    @RequestMapping(value = {"execUpdate"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<List<JSONObject>> execUpdate(@RequestBody JSONObject jsonObjectReq,
                                                 @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        List<JSONObject> data = Lists.newArrayList();
        try {
            data = erdOnlineQueryService.execUpdate(jsonObjectReq, tokenVo);
        } catch (ErdException e) {
            ResultVo<List<JSONObject>> resultVo = ResultVo.success(data);
            resultVo.setMsg(e.getMessage());
            return resultVo;
        }
        return ResultVo.success(data);
    }

    @ErdRolesAllowed(value = RoleEnum.ERD_SQL_EXECUTE, msg = "没有执行SQL的权限")
    @RequestMapping(value = {"/queryInfo/exportSql"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> exportSql(@RequestBody JSONObject jsonObjectReq,
                                            @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        Triple<String, MediaType, byte[]> triple = erdOnlineQueryService.exportSql(jsonObjectReq, tokenVo);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(triple.getLeft(), StandardCharsets.UTF_8.name()));
        headers.setContentType(triple.getMiddle());
        return new ResponseEntity<>(triple.getRight(), headers, HttpStatus.OK);
    }
}
