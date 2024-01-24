package org.javamaster.invocationlab.admin.service.repository.redis;

/**
 * @author yudong
 */
public class RedisKeys {

    private final static String PREFIX = "admin";

    public final static String CLUSTER_REDIS_KEY = PREFIX + "zk_address";

    public final static String CLUSTER_REDIS_KEY_TYPE = PREFIX + "zk_address_type";

    public static final String SCENE_CASE_KEY = PREFIX + "scene_case";

    public static final String CASE_KEY = PREFIX + "test_case_group";
    public static final String CASE_KEY_SUB = CASE_KEY + "_sub";

    public static final String RPC_MODEL_KEY = PREFIX + "models";
    public static final String RPC_MODEL_JDK_KEY = PREFIX + "models_jdk";
    public static final String RPC_MODEL_JDK_MAP_KEY = PREFIX + "models_map_jdk";

    public static final String USER_KEY = PREFIX + "user_all";
}
