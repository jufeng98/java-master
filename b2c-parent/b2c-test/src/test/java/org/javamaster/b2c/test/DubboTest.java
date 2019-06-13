package org.javamaster.b2c.test;

import com.alibaba.fastjson.JSONObject;
import org.javamaster.b2c.dubbo.api.UserDubboService;
import org.javamaster.b2c.dubbo.dto.UserBaseDto;
import org.javamaster.b2c.test.utils.DubboUtils;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * @author yudong
 * @date 2019/6/13
 */
public class DubboTest {

    @AfterClass
    public static void exit() {
        System.exit(0);
    }

    @Test
    public void test() {
        // UserDubboService是已经注册到zookeeper注册中心的Dubbo服务，这里不指定调用特定服务实例
        UserDubboService service = DubboUtils.getService(UserDubboService.class, "1.0.0", null);
        UserBaseDto userBaseDto = service.getByUsername("1050106266");
        System.out.println(JSONObject.toJSONString(userBaseDto, true));
        // 如果是调用测试本地Dubbo接口，只需要把项目启动起来，然后就可以在这里随意修改请求参数测试，非常方便
        // 这里指定调用此127.0.0.1地址提供的Dubbo服务
        service = DubboUtils.getService(UserDubboService.class, "1.0.0", "127.0.0.1:21899");
        userBaseDto = service.getByUsername("1050106266");
        System.out.println(JSONObject.toJSONString(userBaseDto, true));
    }
}
