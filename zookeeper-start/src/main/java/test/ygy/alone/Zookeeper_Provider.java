package test.ygy.alone;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by guoyao on 2017/8/24.
 */
public class Zookeeper_Provider {
    private static final Logger log=LoggerFactory.getLogger(Zookeeper_Provider.class);

    private static final String CONNECT_STRING="192.168.150.130:2181,192.168.150.132:2181,192.168.150.133:2181";

    private static final int SESSION_TIME_OUT=10 * 1000;  // 10秒

    public static CountDownLatch countDownLatch=new CountDownLatch(1);

    public static ZooKeeper getZK() throws Exception {
        return new ZooKeeper(CONNECT_STRING, SESSION_TIME_OUT, (x) -> {
            if (Watcher.Event.KeeperState.SyncConnected == x.getState()) {
                //建立了连接后，设置加入主线程
                countDownLatch.countDown();
                log.warn(" 连接zookeeper 服务 ok ");
            }
        });
    }
}
