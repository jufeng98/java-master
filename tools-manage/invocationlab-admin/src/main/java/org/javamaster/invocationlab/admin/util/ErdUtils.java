package org.javamaster.invocationlab.admin.util;

import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.enums.ProjectType;
import org.javamaster.invocationlab.admin.model.erd.ApplyBean;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.DatatypeBean;
import org.javamaster.invocationlab.admin.model.erd.DbsBean;
import org.javamaster.invocationlab.admin.model.erd.EntitiesBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.FieldsBean;
import org.javamaster.invocationlab.admin.model.erd.IndexsBean;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.Table;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX;
import static org.javamaster.invocationlab.admin.util.DbUtils.basicDataSource;
import static org.javamaster.invocationlab.admin.util.DbUtils.getDefaultDb;
import static org.javamaster.invocationlab.admin.util.DbUtils.getTableColumns;
import static org.javamaster.invocationlab.admin.util.DbUtils.getTableIndexes;
import static org.javamaster.invocationlab.admin.util.DbUtils.getTableInfo;
import static org.javamaster.invocationlab.admin.util.DbUtils.jdbcTemplate;

/**
 * @author yudong
 * @date 2023/2/12
 */
@Slf4j
public class ErdUtils {

    @SneakyThrows
    public static ModulesBean refreshModule(ErdOnlineModel erdOnlineModel, String name) {
        DbsBean dbsBean = getDefaultDb(erdOnlineModel);
        if (dbsBean == null) {
            throw new ErdException("请先保存并设置默认数据源");
        }
        ModulesBean modulesBean = findModulesBean(name, erdOnlineModel);
        List<DatatypeBean> datatypeBeans = erdOnlineModel.getProjectJSON().getDataTypeDomains().getDatatype();
        BasicDataSource dataSource = basicDataSource(dbsBean.getProperties());
        JdbcTemplate jdbcTemplate = jdbcTemplate(dbsBean.getProperties());
        Connection connection = dataSource.getConnection();
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        List<EntitiesBean> entitiesBeans = modulesBean.getEntities().stream()
                .map(entitiesBean -> tableToEntity(entitiesBean, databaseMetaData, datatypeBeans, jdbcTemplate))
                .collect(Collectors.toList());
        modulesBean.setEntities(entitiesBeans);
        connection.close();
        return modulesBean;
    }

    private static EntitiesBean tableToEntity(EntitiesBean entitiesBean, DatabaseMetaData databaseMetaData,
                                              List<DatatypeBean> datatypeBeans, JdbcTemplate jdbcTemplate) {
        String tableName = entitiesBean.getTitle();
        Table table = getTableInfo(tableName, databaseMetaData);
        if (table == null) {
            return entitiesBean;
        }
        entitiesBean.setChnname(table.getRemarks());
        List<Column> columns = getTableColumns(tableName, databaseMetaData);
        entitiesBean.setFields(toFieldsBeans(columns, datatypeBeans));
        List<IndexsBean> indexesBeans = getTableIndexes(tableName, databaseMetaData);
        entitiesBean.setIndexs(indexesBeans);
        List<Map<String, Object>> list = jdbcTemplate.queryForList("show create table " + tableName);
        entitiesBean.setOriginalCreateTableSql(list.get(0).get("Create Table").toString());
        return entitiesBean;
    }

    private static List<FieldsBean> toFieldsBeans(List<Column> columns, List<DatatypeBean> datatypeBeans) {
        return columns.stream()
                .map(column -> {
                    FieldsBean fieldsBean = new FieldsBean();
                    fieldsBean.setChnname(column.getRemarks());
                    fieldsBean.setName(column.getName());
                    DatatypeBean datatypeBean = findDatatypeBean(datatypeBeans, column);
                    fieldsBean.setTypeName(datatypeBean.getName());
                    fieldsBean.setType(datatypeBean.getCode());
                    fieldsBean.setDataType(datatypeBean.getApply().getMYSQL().getType());
                    if (datatypeBean.getCode().contains("Date")) {
                        fieldsBean.setRemark("");
                    } else {
                        List<String> sizes = Lists.newArrayList(column.getSize() + "");
                        if (column.getDigits() != 0) {
                            sizes.add(column.getDigits() + "");
                        }
                        String size = String.join(",", sizes);
                        fieldsBean.setRemark("(" + size + ")");
                    }
                    fieldsBean.setPk(column.isPrimaryKey());
                    fieldsBean.setNotNull("NO".equals(column.getIsNullable()));
                    fieldsBean.setAutoIncrement("YES".equals(column.getIsAutoincrement()));
                    fieldsBean.setRelationNoShow(false);
                    fieldsBean.setDefaultValue(column.getDef());
                    fieldsBean.setUiHint("");
                    return fieldsBean;
                })
                .collect(Collectors.toList());
    }

    private static DatatypeBean findDatatypeBean(List<DatatypeBean> datatypeBeans, Column column) {
        List<DatatypeBean> list = datatypeBeans.stream()
                .filter(datatypeBean -> datatypeBean.getApply().getMYSQL().getType().equals(column.getTypeName()))
                .collect(Collectors.toList());
        if (list.size() == 0) {
            log.error("type not exists:{}", column);
            DatatypeBean datatypeBean = new DatatypeBean();
            datatypeBean.setName(column.getTypeName());
            datatypeBean.setCode(column.getTypeName());
            ApplyBean applyBean = new ApplyBean();
            ApplyBean.MYSQLBean mysqlBean = new ApplyBean.MYSQLBean();
            mysqlBean.setType(column.getTypeName());
            applyBean.setMYSQL(mysqlBean);
            datatypeBean.setApply(applyBean);
            return datatypeBean;
        }
        return list.get(0);
    }

    private static ModulesBean findModulesBean(String moduleCode, ErdOnlineModel erdOnlineModel) {
        ModulesBean module = null;
        for (ModulesBean modulesBean : erdOnlineModel.getProjectJSON().getModules()) {
            if (modulesBean.getName().equals(moduleCode)) {
                module = modulesBean;
                break;
            }
        }
        return module;
    }

    public static Pair<String, String> getPair(ProjectType projectType, String userId) {
        String name = projectType.name().toLowerCase();
        String prefix = ERD_PREFIX + "page:" + name;
        String key = userId + ":" + name;
        return Pair.of(prefix, key);
    }

}
