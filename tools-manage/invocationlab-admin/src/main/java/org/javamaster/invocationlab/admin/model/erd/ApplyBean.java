package org.javamaster.invocationlab.admin.model.erd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yudong
 * @date 2023/2/12
 */
@NoArgsConstructor
@Data
public class ApplyBean {
    @JsonProperty("JAVA")
    private JAVABean jAVA;
    @JsonProperty("MYSQL")
    private MYSQLBean mYSQL;
    @JsonProperty("ORACLE")
    private ORACLEBean oRACLE;
    @JsonProperty("SQLServer")
    private SQLServerBean sQLServer;
    @JsonProperty("PostgreSQL")
    private PostgreSQLBean postgreSQL;

    @NoArgsConstructor
    @Data
    public static class JAVABean {
        private String type;
    }

    @NoArgsConstructor
    @Data
    public static class MYSQLBean {
        private String type;
    }

    @NoArgsConstructor
    @Data
    public static class ORACLEBean {
        private String type;
    }

    @NoArgsConstructor
    @Data
    public static class SQLServerBean {
        private String type;
    }

    @NoArgsConstructor
    @Data
    public static class PostgreSQLBean {
        private String type;
    }
}
