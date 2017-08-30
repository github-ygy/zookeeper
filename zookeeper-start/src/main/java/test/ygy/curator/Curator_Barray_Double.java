package test.ygy.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by guoyao on 2017/8/30.
 */
public class Curator_Barray_Double {

    private static final Logger log=LoggerFactory.getLogger(Curator_Barray_Double.class);

    public static void main(String[] args) throws Exception {
        CuratorFramework cf=Curator_Start.getYGYBuilder().build();
        cf.start();
        log.warn(" zookeeper 启动成功");
        for(int i = 0 ; i < 10 ; i ++) {
            new Thread(
                    ()->{
                        try {
                            log.warn(Thread.currentThread().getName() + " is waiting");
                            // 由ourPath 控制个数，需要每次都new一个对象
                            DistributedDoubleBarrier distributedDoubleBarrier = new DistributedDoubleBarrier(cf,"/test/barries",5);
                            distributedDoubleBarrier.enter();   //设置栅栏
                            Thread.sleep(1000);
                            //do work
                            log.warn(Thread.currentThread().getName() + " is working");
                            distributedDoubleBarrier.leave();   //退出
                            log.warn(Thread.currentThread().getName() + " is out");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ," thread -- " + i).start();
        }

        Thread.sleep(100000);
        cf.close();


    }
}
