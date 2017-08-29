package test.ygy.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by guoyao on 2017/8/28.
 */
public class Curator_Listener {

    private static final Logger log=LoggerFactory.getLogger(Curator_Listener.class);

    public static void main(String[] args) throws  Exception  {

        CountDownLatch countDownLatch=new CountDownLatch(1);

        CuratorFramework cf=Curator_Start.getYGYBuilder().build();
        cf.start();
        log.warn(" zookeeper  启动成功");

        //创建一个节点
        cf.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath("/test", "init".getBytes());

        //nodeCache  client ：客户端  path ： 节点路径  dataIsCompressed ： 是否压缩
        NodeCache nodeCache=new NodeCache(cf, "/test", false);
        nodeCache.start(true);
        // nodeCache 可以监听指定节点数据变化，与节点是否存在
        nodeCache.getListenable().addListener(
                ()->{
                    log.warn("data changed to " + new String(nodeCache.getCurrentData().getData()));
                    countDownLatch.countDown();
                }
        );

        //设置新值，
        cf.setData().forPath("/test", " new data ".getBytes());
        countDownLatch.await();
        cf.delete().forPath("/test");


        Thread.sleep(20000);
        cf.close();
    }
}
