package org.javamaster.spring.refresh.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * @author yudong
 * @date 2022/9/9
 */
@Slf4j
@Configuration
public class ZookeeperListenerConfig {
    @Autowired
    private DataSourceSsmConfig dataSourceSsmConfig;

    @EventListener(RefreshEvent.class)
    public void eventListener(RefreshEvent refreshEvent) {
        Object event = refreshEvent.getEvent();
        if (!(event instanceof TreeCacheEvent)) {
            return;
        }
        TreeCacheEvent treeCacheEvent = (TreeCacheEvent) event;
        if (treeCacheEvent.getType() != TreeCacheEvent.Type.NODE_UPDATED) {
            return;
        }
        log.info("event:{}", treeCacheEvent);
    }

}
