package org.javamaster.invocationlab.admin.model.erd

import org.javamaster.invocationlab.admin.annos.AllOpen

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author yudong
 * @date 2023/2/12
 */


@AllOpen
class ApplyBean {
    @JsonProperty("JAVA")
    var jAVA: JAVABean? = null

    @JsonProperty("MYSQL")
    var mYSQL: MYSQLBean? = null

    @JsonProperty("ORACLE")
    var oRACLE: ORACLEBean? = null

    @JsonProperty("SQLServer")
    var sQLServer: SQLServerBean? = null

    @JsonProperty("PostgreSQL")
    var postgreSQL: PostgreSQLBean? = null


    @AllOpen
    class JAVABean {
        var type: String? = null
    }


    @AllOpen
    class MYSQLBean {
        var type: String? = null
    }


    @AllOpen
    class ORACLEBean {
        var type: String? = null
    }


    @AllOpen
    class SQLServerBean {
        var type: String? = null
    }


    @AllOpen
    class PostgreSQLBean {
        var type: String? = null
    }
}
