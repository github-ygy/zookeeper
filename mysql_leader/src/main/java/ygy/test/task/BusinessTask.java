package ygy.test.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by guoyao on 2017/9/2.
 */
@Component
public class BusinessTask {

    private static final Logger log=LoggerFactory.getLogger(BusinessTask.class);

    private volatile boolean isStart=false;  //默认为false ;

    public void start() {
        isStart=true;
    }


    public void stop() {
        isStart=false;
    }

    @Scheduled(fixedRate=60 * 1000)
    public void testServer() {
        if (!isStart) {
            log.error(" 服务未开启，请 check ");
            return;
        }
        log.warn("  服务已开启");
    }
}

