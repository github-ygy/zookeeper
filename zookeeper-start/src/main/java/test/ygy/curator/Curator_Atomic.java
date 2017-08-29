package test.ygy.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by guoyao on 2017/8/29.
 */
public class Curator_Atomic {

    private static final Logger log=LoggerFactory.getLogger(Curator_Atomic.class);

    public static void main(String[] args) throws Exception {

        CuratorFramework cf=Curator_Start.getBaseBuilder().build();
        cf.start();
        log.warn("  zookeeper 启动成功");

        DistributedAtomicInteger distributedAtomicInteger=
                new DistributedAtomicInteger(cf, "/test/atomic", new RetryNTimes(3, 2000));
        AtomicValue<Integer> increment1=distributedAtomicInteger.increment();
        log.warn("  atomic post inc1 = " + increment1.postValue());
        log.warn("  atomic pre inc1 = " + increment1.preValue());
        //22:32:31.193 [main] WARN test.ygy.curator.Curator_Atomic -   atomic post inc1 = 1
        //22:32:31.193 [main] WARN test.ygy.curator.Curator_Atomic -   atomic pre inc1 = 0
        AtomicValue<Integer> increment2=distributedAtomicInteger.increment();
        log.warn("  atomic post inc2 = " + increment2.postValue());
        log.warn("  atomic pre inc2 = " + increment2.preValue());
        //22:32:31.212 [main] WARN test.ygy.curator.Curator_Atomic -   atomic post inc2 = 2
        //22:32:31.212 [main] WARN test.ygy.curator.Curator_Atomic -   atomic pre inc2 = 1


        Thread.sleep(20000);
        cf.close();
    }
}
