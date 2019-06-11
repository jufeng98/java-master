package org.javamaster.b2c.core.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.javamaster.b2c.core.enums.EnumBase;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>实现将数字转换为实现了EnumBase接口的枚举类对象</p>
 *
 * @author yudong
 * @date 2019/6/10
 */
public class EnumBaseTypeHandler<E extends Enum<?> & EnumBase> extends BaseTypeHandler<E> {

    private Class<E> type;

    public EnumBaseTypeHandler() {
    }

    public EnumBaseTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return EnumBase.codeOf(type, rs.getInt(columnName));
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return EnumBase.codeOf(type, rs.getInt(columnIndex));
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return EnumBase.codeOf(type, cs.getInt(columnIndex));
    }
}
