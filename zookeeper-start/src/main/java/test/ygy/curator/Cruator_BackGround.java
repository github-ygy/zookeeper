package test.ygy.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guoyao on 2017/8/27.
 */
public class Cruator_BackGround {

    private  static  final Logger log =LoggerFactory.getLogger(Cruator_BackGround.class);

    public static void main(String[] agrs) throws  Exception {

        ExecutorService executorService=Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch=new CountDownLatch(4);

        CuratorFramework cf=Curator_Start.getBaseBuilder().build();
        cf.start();
        log.warn(" zookeeper 启动成功");
        Thread.sleep(10000);
        //使用线程池
        cf.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .inBackground(
                        (x, y) -> {   //x ,y : CuratorFramework client 客户端  CuratorEvent event事件
                            log.warn(" event = " + y);
                            log.warn(" execcutors threadName = " + Thread.currentThread().getName());
                            countDownLatch.countDown();
                        }
                ,executorService).forPath("/test", "test back".getBytes());
        //00:28:38.572 [pool-1-thread-1] WARN test.ygy.curator.Cruator_BackGround -  event = CuratorEventImpl{type=CREATE, resultCode=0, path='/test', name='/test', children=null, context=null, stat=null, data=null, watchedEvent=null, aclList=null}
        //00:28:38.572 [pool-1-thread-1] WARN test.ygy.curator.Cruator_BackGround -  execcutors threadName = pool-1-thread-1

        cf.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .inBackground(
                        (x, y) -> {   //x ,y : CuratorFramework client 客户端  CuratorEvent event事件
                            log.warn(" event = " + y);
                            log.warn(" execcutors threadName = " + Thread.currentThread().getName());
                            countDownLatch.countDown();
                        }
                        ,executorService).forPath("/test", "test back".getBytes());
        //00:28:38.621 [pool-1-thread-2] WARN test.ygy.curator.Cruator_BackGround -  event = CuratorEventImpl{type=CREATE, resultCode=-110, path='/test', name='null', children=null, context=null, stat=null, data=null, watchedEvent=null, aclList=null}
        //00:28:38.622 [pool-1-thread-2] WARN test.ygy.curator.Cruator_BackGround -  execcutors threadName = pool-1-thread-2

        //不使用线程池,默认使用main-EventThread ，多个事件默认时，是严格按照顺序执行。
        cf.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .inBackground(
                        (x, y) -> {   //x ,y : CuratorFramework client 客户端  CuratorEvent event事件
                            log.warn(" event = " + y);
                            log.warn(" execcutors threadName = " + Thread.currentThread().getName());
                            countDownLatch.countDown();
                        }
                ).forPath("/test", "test back".getBytes());
        //00:28:38.622 [main-EventThread] WARN test.ygy.curator.Cruator_BackGround -  event = CuratorEventImpl{type=CREATE, resultCode=-110, path='/test', name='null', children=null, context=null, stat=null, data=null, watchedEvent=null, aclList=null}
        //00:28:38.622 [main-EventThread] WARN test.ygy.curator.Cruator_BackGround -  execcutors threadName = main-EventThread

        cf.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .inBackground(
                        (x, y) -> {   //x ,y : CuratorFramework client 客户端  CuratorEvent event事件
                            log.warn(" event = " + y);
                            log.warn(" execcutors threadName = " + Thread.currentThread().getName());
                            countDownLatch.countDown();
                        }
                ).forPath("/test", "test back".getBytes());
        //00:28:38.623 [main-EventThread] WARN test.ygy.curator.Cruator_BackGround -  event = CuratorEventImpl{type=CREATE, resultCode=-110, path='/test', name='null', children=null, context=null, stat=null, data=null, watchedEvent=null, aclList=null}
        //00:28:38.623 [main-EventThread] WARN test.ygy.curator.Cruator_BackGround -  execcutors threadName = main-EventThread

        countDownLatch.await();

        Thread.sleep(10000);
        executorService.shutdown();
        cf.close();
    }
}
