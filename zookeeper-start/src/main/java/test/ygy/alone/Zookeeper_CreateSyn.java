package test.ygy.alone;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by guoyao on 2017/8/24.
 */
public class Zookeeper_CreateSyn {

    private static final Logger log=LoggerFactory.getLogger(Zookeeper_CreateSyn.class);


    public static void main(String[] args) throws  Exception{

        ZooKeeper zk=Zookeeper_Provider.getZK();
        Zookeeper_Provider.countDownLatch.await();
        //创建节点
        //public void create(
        // String path,    //数据节点路径
        // byte[] data,    //节点数据
        // List<ACL> acl,   //acl策略
        // CreateMode createMode, // 节点类型 PERSISTENT 持久化  PERSISTENT_SEQUENTIAL 顺序持久化  EPHEMERAL 临时  EPHEMERAL_SEQUENTIAL 顺序临时
        // StringCallback cb,     // 异步回调方法
        // Object ctx)      //异步回调传递参数
        String result1=zk.create("/test-zk", "ygy-test1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        log.warn(" result is " + result1);   //节点路径

        String result2=zk.create("/test-zk", "ygy-test1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        log.warn(" result is " + result2);   // 报错，不能创建相同的节点

        String result3=zk.create("/test-zk-seq", "ygy-test1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        log.warn(" result is " + result3);   // 节点路径 + 数字

        Thread.sleep(20000);
        log.warn( " main class over");
        zk.close();
    }
}
