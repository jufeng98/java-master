package org.javamaster.b2c.core.listener;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Date;


/**
 * 记录应用启动失败异常到文件
 *
 * @author yudong
 * @date 2020/9/20
 */
public class ServiceStartFailedListener implements ApplicationListener<ApplicationFailedEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {
        try {
            String time = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            File path = new File("tmp");
            if (!path.exists()) {
                Files.createDirectories(path.toPath());
            }
            File file = new File(path, "exception.log");
            try (PrintWriter printWriter = new PrintWriter(file)) {
                printWriter.println("# " + time);
                event.getException().printStackTrace(printWriter);
            }
            logger.info("start exception message write to file:{}", file.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
