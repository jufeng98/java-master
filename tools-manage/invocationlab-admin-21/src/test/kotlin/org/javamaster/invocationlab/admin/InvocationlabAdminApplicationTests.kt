package org.javamaster.invocationlab.admin

import org.javamaster.invocationlab.admin.model.erd.CheckboxesVo
import org.javamaster.invocationlab.admin.util.JsonUtils
import org.junit.jupiter.api.Test

//@SpringBootTest
class InvocationlabAdminApplicationTests {

    @Test
    fun contextLoads() {
        val bean = CheckboxesVo()
        bean.defaultValue = listOf(12L, 33L)
        val res = JsonUtils.jacksonObjectMapper.writeValueAsString(bean)
        println(res)
    }

}
