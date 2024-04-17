package org.javamaster.invocationlab.admin.service.impl;

import com.alibaba.druid.DbType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.javamaster.invocationlab.admin.model.erd.ApplyBean;
import org.javamaster.invocationlab.admin.model.erd.Column;
import org.javamaster.invocationlab.admin.util.DbUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service("MYSQL")
public class MySqlDbServiceImpl extends AbstractDbService {

    @Override
    public List<String> getDbNames(JdbcTemplate jdbcTemplate) {
        String defaultDbName = DbUtils.resolveUrlDbName(jdbcTemplate);

        List<String> dbs = jdbcTemplate.queryForList("show databases").stream()
                .map(map -> map.get("Database").toString())
                .collect(Collectors.toList());
        dbs.remove(defaultDbName);
        dbs.add(0, defaultDbName);
        return dbs;
    }

    @Override
    protected Predicate<Pair<ApplyBean, Column>> datatypePredicate() {
        return pair -> pair.getLeft().getMYSQL().getType().equals(pair.getRight().getTypeName().toUpperCase());
    }

    @Override
    protected String getExplainSql(String querySql) {
        return "desc " + querySql;
    }

    @Override
    protected String getTableDdlSql(JdbcTemplate jdbcTemplate, String tableName) {
        List<Map<String, Object>> list = jdbcTemplate.queryForList("show create table " + tableName);
        return list.get(0).get("Create Table").toString();
    }

    @Override
    public DbType getDbType() {
        return DbType.mysql;
    }

}
