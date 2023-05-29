package org.javamaster.invocationlab.admin.service.invocation;

import org.javamaster.invocationlab.admin.service.invocation.entity.DubboParamValue;
import org.javamaster.invocationlab.admin.service.invocation.entity.PostmanDubboRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yudong
 * @date 2022/11/13
 */
public class AbstractInvoker {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    protected Converter<PostmanDubboRequest, DubboParamValue> converter;
}
