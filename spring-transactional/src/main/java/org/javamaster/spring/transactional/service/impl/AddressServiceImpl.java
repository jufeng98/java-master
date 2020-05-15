package org.javamaster.spring.transactional.service.impl;

import lombok.SneakyThrows;
import org.javamaster.spring.transactional.service.AddressHelper;
import org.javamaster.spring.transactional.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.*;

/**
 * @author yudong
 * @date 2020/5/15
 */
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private AddressHelper addressHelper;

    @SneakyThrows
    @Override
    public String transactionOriginalResearch() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            short countryId;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            try {
                // 其他业务逻辑处理
                // ......
                preparedStatement = conn.prepareStatement("insert into country(country) values (?)", Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, "中国");
                preparedStatement.executeUpdate();
                resultSet = preparedStatement.getGeneratedKeys();
                resultSet.next();
                countryId = resultSet.getShort(1);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            }

            short cityId;
            try {
                // 其他业务逻辑处理
                // ......
                preparedStatement = conn.prepareStatement("insert into city(city, country_id) values (?,?)", Statement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, "北京");
                preparedStatement.setShort(2, countryId);
                preparedStatement.executeUpdate();
                resultSet = preparedStatement.getGeneratedKeys();
                resultSet.next();
                cityId = resultSet.getShort(1);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            }
            // country表和city表均保存成功,此处设置个Savepoint(保存点)
            Savepoint savepointCity = conn.setSavepoint("city");

            int affect;
            try {
                // 其他业务逻辑处理
                // ......
                preparedStatement = conn.prepareStatement("insert into category(name) values (?)");
                preparedStatement.setString(1, "分类名称");
                affect = preparedStatement.executeUpdate();
                if (true) {
                    throw new RuntimeException("模拟抛出异常");
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback(savepointCity);
                throw new RuntimeException(e);
            } finally {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
            return countryId + " " + cityId + " " + affect;
        }
    }

    /**
     * 声明式事务
     */
    @Override
    @Transactional(timeout = 6000, rollbackFor = Exception.class)
    public String transactionResearch() {
        String res = addressHelper.save();
        // 其他业务逻辑处理
        // ......
        int affect = jdbcTemplate.update("insert into category(name) values (?)", "分类名称");
        if (true) {
            throw new RuntimeException("模拟抛出异常");
        }
        return res + " " + affect;
    }

    /**
     * 编程式事务
     */
    @Override
    public String transactionResearch1() {

        return transactionTemplate.execute(status -> {

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
            Object savepointCity = status.createSavepoint();

            int affect = 0;
            try {
                // 其他业务逻辑处理
                // ......
                affect = jdbcTemplate.update("insert into category(name) values (?)", "分类名称");
                if (true) {
                    throw new RuntimeException("模拟抛出异常");
                }
            } catch (Exception e) {
                status.rollbackToSavepoint(savepointCity);
            }

            return countryId + " " + cityId + " " + affect;
        });
    }


}
