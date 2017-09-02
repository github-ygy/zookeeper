package ygy.test.task;

import org.springframework.stereotype.Component;

/**
 * Created by guoyao on 2017/9/2.
 */
@Component
public class BusinessTask {

    private volatile boolean isStart = false  ;  //默认为false ;

    public void start() {
        isStart = true ;
    }


    public void stop() {
        isStart = false ;
    }
}
