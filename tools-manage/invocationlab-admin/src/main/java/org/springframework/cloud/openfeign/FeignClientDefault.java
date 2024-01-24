package org.springframework.cloud.openfeign;

import org.javamaster.invocationlab.admin.service.AppFactory;
import org.javamaster.invocationlab.admin.service.Pair;
import org.javamaster.invocationlab.admin.service.invocation.entity.DubboParamValue;
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest;
import org.javamaster.invocationlab.admin.service.registry.Register;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils;
import feign.Client;
import feign.Request;
import feign.Response;
import org.apache.commons.lang3.RandomUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.util.List;

import static org.javamaster.invocationlab.admin.consts.Constant.FEIGN_PARAM;

/**
 * @author yudong
 * @date 2022/11/9
 */
public class FeignClientDefault extends Client.Default {

    public FeignClientDefault() {
        this(null, null);
    }

    public FeignClientDefault(SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier) {
        super(sslContextFactory, hostnameVerifier);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        Pair<PostmanDubboRequest, DubboParamValue> pair = ThreadLocalUtils.get(FEIGN_PARAM);
        String serviceName = pair.getLeft().getServiceName();
        AppFactory appFactory = SpringUtils.getContext().getBean(AppFactory.class);
        Register register = appFactory.getRegisterFactory(pair.getLeft().getCluster()).get(pair.getLeft().getCluster());
        String host;
        if (pair.getRight().isUseDubbo()) {
            host = pair.getRight().getDubboUrl().replace("dubbo://", "");
        } else {
            List<String> instances = register.getServiceInstances(serviceName);
            int i = RandomUtils.nextInt(0, instances.size());
            host = instances.get(i);
        }
        String url = request.url().replace(serviceName.toLowerCase(), host);
        request = Request.create(
                request.httpMethod(),
                url,
                request.headers(),
                request.requestBody()
        );
        return super.execute(request, options);
    }
}
