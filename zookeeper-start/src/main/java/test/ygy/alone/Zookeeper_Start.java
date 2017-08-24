package test.ygy.alone;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by guoyao on 2017/8/24.
 */
public class Zookeeper_Start {

    private static final Logger log=LoggerFactory.getLogger(Zookeeper_Start.class);

    private static final String CONNECT_STRING="192.168.150.130:2181,192.168.150.132:2181,192.168.150.133:2181";

    private static final int SESSION_TIME_OUT= 10* 1000;  // 10秒

    private static CountDownLatch countDownLatch=new CountDownLatch(1);

    public static void main(String[] args) throws  Exception{

        //构造zookeeper
        //public ZooKeeper(String connectString,    // Zookeeper 连接服务器地址
        // int sessionTimeout,                      //会话超时时间（毫秒）
        // Watcher watcher,                         //事件通知处理器
        // long sessionId,                          //会话id
        // byte[] sessionPasswd,                    //会话秘钥   // 会话id 秘钥 复用可以恢复会话
        // boolean canBeReadOnly)                   //只读模式
        //创建最基本的zookeeper

        ZooKeeper zooKeeper = new ZooKeeper(CONNECT_STRING, SESSION_TIME_OUT, (x)-> {
            //获取事件的状态
            //Disconnected(0),
            //SyncConnected(3),
            //AuthFailed(4),
            //ConnectedReadOnly(5),
            //SaslAuthenticated(6),
            //Expired(-112);
            Watcher.Event.KeeperState keeperState=x.getState();
            //事件类型
            //None(-1),
            //NodeCreated(1),
            //NodeDeleted(2),
            //NodeDataChanged(3),
            //NodeChildrenChanged(4);
            Watcher.Event.EventType eventType=x.getType();
            if (Watcher.Event.KeeperState.SyncConnected == keeperState) {
                //建立了连接后，设置加入主线程
                countDownLatch.countDown();
                log.info(" 连接zookeeper 服务 ok ");
            }
        });

        //等待zookeeper连接结束
        countDownLatch.await();
        System.out.println( " main class over");
        zooKeeper.close();   //释放资源
    }
}
