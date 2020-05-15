package org.javamaster.spring.transactional.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * @author yudong
 * @date 2020/5/15
 */
@Component
public class AddressHelper {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String save() {
        // 其他业务逻辑处理
        // ......
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement("insert into country(country) values (?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, "中国");
            return preparedStatement;
        }, keyHolder);
        short countryId = keyHolder.getKey().shortValue();

        // 其他业务逻辑处理
        // ......
        GeneratedKeyHolder keyHolder1 = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement("insert into city(city,country_id) values (?,?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, "北京");
            preparedStatement.setInt(2, countryId);
            return preparedStatement;
        }, keyHolder1);
        short cityId = keyHolder1.getKey().shortValue();

        return countryId + " " + cityId;
    }

}
