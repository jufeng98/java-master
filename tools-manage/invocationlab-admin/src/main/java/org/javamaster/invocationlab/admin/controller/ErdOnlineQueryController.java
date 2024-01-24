package org.javamaster.invocationlab.admin.controller;

import org.javamaster.invocationlab.admin.annos.ErdRolesAllowed;
import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.enums.RoleEnum;
import org.javamaster.invocationlab.admin.model.ResultVo;
import org.javamaster.invocationlab.admin.model.erd.AesReqVo;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.CommonErdVo;
import org.javamaster.invocationlab.admin.model.erd.QueryReqVo;
import org.javamaster.invocationlab.admin.model.erd.SaveQueryReqVo;
import org.javamaster.invocationlab.admin.model.erd.SqlExecResVo;
import org.javamaster.invocationlab.admin.model.erd.Table;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.model.erd.Tree;
import org.javamaster.invocationlab.admin.service.ErdOnlineQueryService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * @author yudong
 */
@Slf4j
@RestController
@RequestMapping(value = "/ncnb", produces = MediaType.APPLICATION_JSON_VALUE)
public class ErdOnlineQueryController {
    @Autowired
    private ErdOnlineQueryService erdOnlineQueryService;

    @RequestMapping(value = "/hisProject/load", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<JSONArray> load(@RequestBody JSONObject jsonObjectReq) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        return ResultVo.success(erdOnlineQueryService.load(projectId));
    }

    @RequestMapping(value = "/queryHistory", method = {RequestMethod.GET})
    public ResultVo<JSONObject> queryHistory(CommonErdVo reqVo, @SessionAttribute("tokenVo") TokenVo tokenVo) {
        return ResultVo.success(erdOnlineQueryService.queryHistory(reqVo, tokenVo));
    }

    @RequestMapping(value = "/queryInfo/tree", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<List<Tree>> getQueryTreeList(String projectId) throws Exception {
        return ResultVo.success(erdOnlineQueryService.getQueryTreeList(projectId));
    }

    @RequestMapping(value = "/queryInfo", method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<Boolean> createDirOrQuery(@RequestBody QueryReqVo reqVo,
                                              @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineQueryService.createDirOrQuery(reqVo, tokenVo));
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
    public ResultVo<Tree> saveQueryTree(@RequestBody SaveQueryReqVo reqVo, @PathVariable String treeNodeId,
                                        @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        reqVo.setTreeNodeId(treeNodeId);
        ResultVo<Tree> resultVo = ResultVo.success(erdOnlineQueryService.saveQueryTree(reqVo, tokenVo));
        resultVo.setMsg("保存SQL成功!");
        return resultVo;
    }

    @RequestMapping(value = {"/getDbs"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<List<String>> getDbs(@RequestBody JSONObject jsonObjectReq, HttpServletRequest request,
                                         @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        String projectId = jsonObjectReq.getString("projectId");
        return ResultVo.success(erdOnlineQueryService.getDbs(projectId, tokenVo));
    }

    @RequestMapping(value = {"/getTables"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<List<Table>> getTables(@RequestBody CommonErdVo reqVo,
                                           @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineQueryService.getTables(reqVo, tokenVo));
    }

    @RequestMapping(value = {"/getTableColumns"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResultVo<List<Column>> getTableColumns(@RequestBody CommonErdVo reqVo, HttpServletRequest request,
                                                  @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineQueryService.getTableColumns(reqVo, tokenVo));
    }

    @ErdRolesAllowed(value = RoleEnum.ERD_SQL_EXECUTE, msg = "没有执行SQL的权限")
    @PostMapping({"/queryInfo/exec", "/queryInfo/explain"})
    public ResultVo<SqlExecResVo> execSql(@RequestBody CommonErdVo reqVo, HttpServletRequest request,
                                          @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        reqVo.setIsExport(false);
        reqVo.setExplain(request.getRequestURI().contains("explain"));
        reqVo.setSql(reqVo.getSql().trim());
        return ResultVo.success(erdOnlineQueryService.execSql(reqVo, tokenVo));
    }

    @ErdRolesAllowed(value = RoleEnum.ERD_SQL_EXECUTE, msg = "没有执行SQL的权限")
    @PostMapping("/queryInfo/getTableRecordTotal")
    public ResultVo<Integer> getTableRecordTotal(@RequestBody CommonErdVo reqVo,
                                                 @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineQueryService.getTableRecordTotal(reqVo, tokenVo));
    }

    @ErdRolesAllowed(value = RoleEnum.ERD_SQL_EXECUTE, msg = "没有执行SQL的权限")
    @PostMapping("execUpdate")
    public ResultVo<List<SqlExecResVo>> execUpdate(@RequestBody CommonErdVo reqVo,
                                                   @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        reqVo.setIsExport(false);
        reqVo.setExplain(false);
        List<SqlExecResVo> list = Collections.emptyList();
        try {
            list = erdOnlineQueryService.execUpdate(reqVo, tokenVo);
        } catch (ErdException e) {
            log.error("执行错误:{}", reqVo, e);
            ResultVo<List<SqlExecResVo>> resultVo = ResultVo.success(list);
            resultVo.setCode(e.getCode());
            resultVo.setMsg(e.getMessage());
            return resultVo;
        }
        return ResultVo.success(list);
    }

    @ErdRolesAllowed(value = RoleEnum.ERD_SQL_EXECUTE, msg = "没有执行SQL的权限")
    @RequestMapping(value = {"/queryInfo/exportSql"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> exportSql(@RequestBody CommonErdVo reqVo,
                                            @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        reqVo.setIsExport(true);
        reqVo.setExplain(false);
        Triple<String, MediaType, byte[]> triple = erdOnlineQueryService.exportSql(reqVo, tokenVo);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(triple.getLeft(), StandardCharsets.UTF_8.name()));
        headers.setContentType(triple.getMiddle());
        return new ResponseEntity<>(triple.getRight(), headers, HttpStatus.OK);
    }

    @ErdRolesAllowed(value = RoleEnum.ERD_SQL_EXECUTE)
    @PostMapping("/queryInfo/aes")
    public ResultVo<String> aes(@RequestBody AesReqVo reqVo, @SessionAttribute("tokenVo") TokenVo tokenVo) throws Exception {
        return ResultVo.success(erdOnlineQueryService.aes(reqVo, tokenVo));
    }
}
