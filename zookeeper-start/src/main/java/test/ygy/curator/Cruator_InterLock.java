package test.ygy.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guoyao on 2017/8/28.
 */
public class Cruator_InterLock {

    private static final Logger log=LoggerFactory.getLogger(Cruator_InterLock.class);

    private  static  int index = 0 ;

    public static void main(String[] args) throws Exception {

        CuratorFramework cf=Curator_Start.getBaseBuilder().build();
        cf.start();
        log.warn(" zookeeper  启动成功");
        ExecutorService executorService=Executors.newFixedThreadPool(100);
        CountDownLatch countDownLatch=new CountDownLatch(100);

        //zookeeper 锁
        InterProcessMutex interProcessMutex=new InterProcessMutex(cf,"/test/lock");
        for (int i=0; i < 100; i++) {
            executorService.execute(()->{
                try {
                    Thread.sleep(1000);
                    interProcessMutex.acquire();
                    index++;
                    interProcessMutex.release();
                    countDownLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                    countDownLatch.countDown();
                }
            });
        }
        executorService.shutdown();
        countDownLatch.await();
        log.warn("   index add 100 times = " + index);

    }
}
