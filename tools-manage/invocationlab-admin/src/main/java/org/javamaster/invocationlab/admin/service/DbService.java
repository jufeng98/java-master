package org.javamaster.invocationlab.admin.service;

import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.CommonErdVo;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.SqlExecResVo;
import org.javamaster.invocationlab.admin.model.erd.Table;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.service.impl.MySqlDbServiceImpl;
import org.javamaster.invocationlab.admin.service.impl.PostgreSqlDbServiceImpl;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DbService {
    Map<String, DbService> DB_MAP = new HashMap<String, DbService>() {
        {
            put("MYSQL", new MySqlDbServiceImpl());
            put("PostgreSQL", new PostgreSqlDbServiceImpl());
        }
    };

    static DbService getInstance(String select) {
        DbService dbService = DB_MAP.get(select);
        if (dbService == null) {
            throw new ErdException("暂时不支持当前数据库:" + select);
        }
        return dbService;
    }

    void checkDb(DbsBean dbsBean);

    List<String> getDbs(DbsBean dbsBean);

    List<Table> getTables(DbsBean dbsBean, String selectDB);


    List<SqlExecResVo> execUpdate(DbsBean dbsBean, TokenVo tokenVo, CommonErdVo reqVo);

    List<Column> getTableColumns(DbsBean dbsBean, String selectDB, String tableName);

    SqlExecResVo execSql(CommonErdVo reqVo, DbsBean dbsBean, TokenVo tokenVo);

    Triple<String, MediaType, byte[]> exportSql(DbsBean dbsBean, TokenVo tokenVo, CommonErdVo reqVo);

    Integer getTableRecordTotal(CommonErdVo reqVo, DbsBean dbsBean, TokenVo tokenVo) throws Exception;

    ModulesBean refreshModule(ErdOnlineModel erdOnlineModel, String moduleName);

}
