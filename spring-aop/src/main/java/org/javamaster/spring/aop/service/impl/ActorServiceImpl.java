package org.javamaster.spring.aop.service.impl;

import org.javamaster.spring.aop.service.ActorService;
import org.springframework.stereotype.Service;

/**
 * @author yudong
 * @date 2021/4/26
 */
@Service
public class ActorServiceImpl implements ActorService {

    @Override
    public Integer createActor(String jufeng98) {
        return 1;
    }

}
