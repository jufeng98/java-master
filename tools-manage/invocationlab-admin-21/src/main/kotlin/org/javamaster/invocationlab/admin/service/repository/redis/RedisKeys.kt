package org.javamaster.invocationlab.admin.service.repository.redis

/**
 * @author yudong
 */
object RedisKeys {
    private const val PREFIX = "rpc_postman_"

    const val CLUSTER_REDIS_KEY: String = PREFIX + "zk_address"

    const val CLUSTER_REDIS_KEY_TYPE: String = PREFIX + "zk_address_type"

    const val SCENE_CASE_KEY: String = PREFIX + "scene_case"

    const val CASE_KEY: String = PREFIX + "test_case_group"
    const val CASE_KEY_SUB: String = CASE_KEY + "_sub"

    const val RPC_MODEL_KEY: String = PREFIX + "models"
    const val RPC_MODEL_JDK_KEY: String = PREFIX + "models_jdk"
    const val RPC_MODEL_JDK_MAP_KEY: String = PREFIX + "models_map_jdk"

}
