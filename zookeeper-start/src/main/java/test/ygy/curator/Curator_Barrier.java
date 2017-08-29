package test.ygy.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by guoyao on 2017/8/29.
 */
public class Curator_Barrier {

    private static final Logger log=LoggerFactory.getLogger(Curator_Barrier.class);

    public static void main(String[] args) throws Exception {
        CuratorFramework cf=Curator_Start.getBaseBuilder().build();
        cf.start();
        log.warn(" zookeeper 启动成功");


        DistributedBarrier distributedBarrier = new DistributedBarrier(cf,"/test/barries");
        DistributedAtomicInteger distributedAtomicInteger=
                new DistributedAtomicInteger(cf, "/test/ato", new RetryNTimes(3, 2000));
        //cf.create().creatingParentsIfNeeded().forPath("/test/barries");
        Stat stat1=cf.checkExists().usingWatcher(new CuratorWatcher() {
            @Override
            public void process(WatchedEvent event) throws Exception {

            }
        }).forPath("/test/barries");
        log.warn(" check  stat1 = " + stat1);

        for(int i = 0 ; i < 10 ; i ++) {
            new Thread(
                    ()->{
                        try {
                            distributedBarrier.setBarrier();   //设置栅栏
                            Thread.sleep(1000);
                            log.warn(Thread.currentThread().getName() + " is waiting");
                            distributedAtomicInteger.increment();  //计数
                            distributedBarrier.waitOnBarrier();   //无线等待
                            //do work
                            Thread.sleep(2000);
                            log.warn(Thread.currentThread().getName() + " is running");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ," thread -- " + i).start();
        }
        int index = 0 ;
        while (distributedAtomicInteger.get().postValue() < 10) {

            //等待所有任务进入
            if (index < distributedAtomicInteger.get().postValue()) {
                log.warn(" 当前已备注数为 " + distributedAtomicInteger.get().postValue());
                index = distributedAtomicInteger.get().postValue();
            }

        }
        Stat stat2=cf.checkExists().usingWatcher(new CuratorWatcher() {
            @Override
            public void process(WatchedEvent event) throws Exception {

            }
        }).forPath("/test/barries");
        log.warn(" check  stat2 = " + stat2);
        distributedBarrier.removeBarrier();
        Stat stat3=cf.checkExists().usingWatcher(new CuratorWatcher() {
            @Override
            public void process(WatchedEvent event) throws Exception {

            }
        }).forPath("/test/barries");
        log.warn(" check  stat3 = " + stat3);


        Thread.sleep(100000);
        cf.close();
    }
}
