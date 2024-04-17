package org.javamaster.invocationlab.admin.service;

import com.alibaba.druid.DbType;
import org.apache.commons.lang3.tuple.Triple;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.CommonErdVo;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.SqlExecResVo;
import org.javamaster.invocationlab.admin.model.erd.Table;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.springframework.http.MediaType;

import java.util.List;

public interface DbService {
    void checkDb(DbsBean dbsBean);

    List<String> getDbNames(DbsBean dbsBean);

    List<Table> getTables(DbsBean dbsBean, String selectDB);

    DbType getDbType();

    List<SqlExecResVo> execUpdate(DbsBean dbsBean, TokenVo tokenVo, CommonErdVo reqVo);

    List<Column> getTableColumns(DbsBean dbsBean, String selectDB, String tableName);

    SqlExecResVo execSql(CommonErdVo reqVo, DbsBean dbsBean, TokenVo tokenVo);

    Triple<String, MediaType, byte[]> exportSql(DbsBean dbsBean, TokenVo tokenVo, CommonErdVo reqVo);

    Integer getTableRecordTotal(CommonErdVo reqVo, DbsBean dbsBean, TokenVo tokenVo) throws Exception;

    ModulesBean refreshModule(ErdOnlineModel erdOnlineModel, String moduleName);

}
