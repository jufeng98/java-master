package org.javamaster.invocationlab.admin.util;

import org.javamaster.invocationlab.admin.enums.ProjectType;
import org.javamaster.invocationlab.admin.model.erd.ApplyBean;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.model.erd.DatatypeBean;
import org.javamaster.invocationlab.admin.model.erd.EntitiesBean;
import org.javamaster.invocationlab.admin.model.erd.ErdOnlineModel;
import org.javamaster.invocationlab.admin.model.erd.FieldsBean;
import org.javamaster.invocationlab.admin.model.erd.IndexsBean;
import org.javamaster.invocationlab.admin.model.erd.ModulesBean;
import org.javamaster.invocationlab.admin.model.erd.Table;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.javamaster.invocationlab.admin.consts.ErdConst.ERD_PREFIX;
import static org.javamaster.invocationlab.admin.util.DbUtils.getTableColumns;
import static org.javamaster.invocationlab.admin.util.DbUtils.getTableIndexes;
import static org.javamaster.invocationlab.admin.util.DbUtils.getTableInfo;

/**
 * @author yudong
 * @date 2023/2/12
 */
@Slf4j
public class ErdUtils {

    public static EntitiesBean tableToEntity(EntitiesBean entitiesBean, DatabaseMetaData databaseMetaData,
                                             List<DatatypeBean> datatypeBeans,
                                             Predicate<Pair<ApplyBean, Column>> predicate) {
        String tableName = entitiesBean.getTitle();
        Table table = getTableInfo(tableName, databaseMetaData);
        if (table == null) {
            return entitiesBean;
        }
        entitiesBean.setChnname(table.getRemarks());

        List<Column> columns = getTableColumns(tableName, databaseMetaData);
        entitiesBean.setFields(toFieldsBeans(columns, datatypeBeans, predicate));

        List<IndexsBean> indexesBeans = getTableIndexes(tableName, databaseMetaData);
        entitiesBean.setIndexs(indexesBeans);

        return entitiesBean;
    }

    public static List<FieldsBean> toFieldsBeans(List<Column> columns, List<DatatypeBean> datatypeBeans,
                                                 Predicate<Pair<ApplyBean, Column>> predicate) {
        return columns.stream()
                .map(column -> {
                    FieldsBean fieldsBean = new FieldsBean();
                    fieldsBean.setChnname(column.getRemarks());
                    fieldsBean.setName(column.getName());

                    DatatypeBean datatypeBean = findDatatypeBean(datatypeBeans, column, predicate);

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

    public static DatatypeBean findDatatypeBean(List<DatatypeBean> datatypeBeans, Column column, Predicate<Pair<ApplyBean, Column>> predicate) {
        List<DatatypeBean> list = datatypeBeans.stream()
                .filter(datatypeBean -> predicate.test(Pair.of(datatypeBean.getApply(), column)))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
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

    public static ModulesBean findModulesBean(String moduleCode, ErdOnlineModel erdOnlineModel) {
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
