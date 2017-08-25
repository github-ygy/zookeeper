package test.ygy.alone;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by guoyao on 2017/8/25.
 */
public class Zookeeper_childrenSynAsyn {

    private static final Logger log=LoggerFactory.getLogger(Zookeeper_childrenSynAsyn.class);

    private static final String CONNECT_STRING="192.168.150.130:2181,192.168.150.132:2181,192.168.150.133:2181";

    private static final int SESSION_TIME_OUT=10 * 1000;  // 10秒

    private static CountDownLatch countDownLatch=new CountDownLatch(1);

    private static ZooKeeper zooKeeper ;

    public static  void main(String [] args ) throws  Exception {

        zooKeeper=getZK_Children();
        countDownLatch.await();

        zooKeeper.create("/test-zk", "ygy-test1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        zooKeeper.create("/test-zk/test1", "ygy-test1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        //开启watch时，则会观察下一个create节点时的childer事件
        //同步
        //List<String> children=zooKeeper.getChildren("/test-zk", true);
        //log.warn(children.toString());

        //异步
        zooKeeper.getChildren("/test-zk", true, new AsyncCallback.Children2Callback() {
            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
                log.warn(" rc = " + rc + " path = " + path + "　ctx =" + ctx + " children = " + children.toString() + " stat = " + stat);
            }
        }," param ");

        zooKeeper.create("/test-zk/test2", "ygy-test1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);


        Thread.sleep(20000);
        zooKeeper.close();
    }


    public static ZooKeeper getZK_Children() throws Exception {
        return new ZooKeeper(CONNECT_STRING, SESSION_TIME_OUT, (x) -> {
            if (Watcher.Event.KeeperState.SyncConnected == x.getState()) {
                log.warn(" 连接zookeeper 服务 ok ");
                if (x.getType() == Watcher.Event.EventType.None && null == x.getPath()) {
                    log.warn(x.toString());
                    //00:50:59.309 [main-EventThread] WARN test.ygy.alone.Zookeeper_Start - WatchedEvent state:SyncConnected type:None path:null
                    countDownLatch.countDown();
                    //发生node改变事件
                } else if (x.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    try {
                        log.warn(" watch result = " + zooKeeper.getChildren(x.getPath() ,true));
                    } catch (Exception e) {
                        log.error(" get error",e);
                    }

                }
            }
        });
    }
}
