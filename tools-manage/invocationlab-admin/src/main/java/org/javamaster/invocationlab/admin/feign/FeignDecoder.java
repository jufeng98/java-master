package org.javamaster.invocationlab.admin.feign;

import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import org.apache.commons.lang.RandomStringUtils;
import org.javamaster.invocationlab.admin.model.Result;
import org.javamaster.invocationlab.admin.model.sso.LoginLdapResVo;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;

import static org.javamaster.invocationlab.admin.consts.ErdConst.ADMIN_CODE;

@Component
public class FeignDecoder implements Decoder {

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        LoginLdapResVo ldapResVo = new LoginLdapResVo();
        ldapResVo.setRefreshToken(RandomStringUtils.randomAlphanumeric(10));
        ldapResVo.setEmail("123456@qq.com");
        ldapResVo.setAccount(ADMIN_CODE);
        ldapResVo.setRealName("admin");
        ldapResVo.setToken(RandomStringUtils.randomAlphanumeric(10));
        ldapResVo.setMobileNo("13800138000");

        return Result.success(ldapResVo);
    }
}
