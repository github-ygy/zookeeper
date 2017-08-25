package test.ygy.alone;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by guoyao on 2017/8/25.
 */
public class Zookeeper_DataSynAsyn  {

    private static final Logger log=LoggerFactory.getLogger(Zookeeper_Provider.class);

    //private static final String CONNECT_STRING="192.168.150.130:2181,192.168.150.132:2181,192.168.150.133:2181";

    private static final String CONNECT_STRING="www.ygy.com:2181";

    private static final int SESSION_TIME_OUT=10 * 1000;  // 10秒

    public static CountDownLatch countDownLatch=new CountDownLatch(1);

    private  static  ZooKeeper zooKeeper ;

    private static Stat stat=new Stat();  //数据信息（每次从zookeeper获取后将会被替换为获取的数据信息）

    public static void main(String[] args) throws  Exception {

         zooKeeper=getZkData();
        countDownLatch.await();

        zooKeeper.create("/zk-test", "ygy".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        //打印节点数据信息，同时开启watch事件
        log.warn("data = " + new String(zooKeeper.getData("/zk-test",true,stat )) );
        log.warn(" stat = "+ stat);

        //设置数据参数
        //public void setData(
        // String path,     //节点路径
        // byte[] data,     //数据
        // int version,    //版本
        // StatCallback cb,   //回调函数
        // Object ctx) {     //回调函数 param

        //同步
        //修改数据,触发watch事件
        //zooKeeper.setData("/zk-test", "redata-ygy".getBytes(), -1);
        //14:49:29.224 [main] WARN test.ygy.alone.Zookeeper_Provider - data = ygy
        //14:49:29.225 [main] WARN test.ygy.alone.Zookeeper_Provider -  stat = 10,10,1503643768812,1503643768812,0,0,0,0,3,0,10
        //14:49:29.310 [main-EventThread] WARN test.ygy.alone.Zookeeper_Provider - watch = redata-ygy
        //14:49:29.310 [main-EventThread] WARN test.ygy.alone.Zookeeper_Provider -  watch stat = 10,11,1503643768812,1503643768911,1,0,0,0,10,0,10


        //异步
        zooKeeper.setData("/zk-test", "redata-ygy".getBytes(), -1, new AsyncCallback.StatCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, Stat stat) {
                log.warn(" rc = " + rc + " path = " + path + " ctx = " + ctx + " stat = " + stat);
            }
        }," param ");
        //15:13:26.629 [main] WARN test.ygy.alone.Zookeeper_Provider - data = ygy
        //15:13:26.629 [main] WARN test.ygy.alone.Zookeeper_Provider -  stat = 18,18,1503645206488,1503645206488,0,0,0,0,3,0,18
        //15:13:26.710 [main-EventThread] WARN test.ygy.alone.Zookeeper_Provider - watch = redata-ygy
        //15:13:26.711 [main-EventThread] WARN test.ygy.alone.Zookeeper_Provider -  watch stat = 18,19,1503645206488,1503645206589,1,0,0,0,10,0,18
        //15:13:26.713 [main-EventThread] WARN test.ygy.alone.Zookeeper_Provider -  rc = 0 path = /zk-test ctx =  param  stat = 18,19,1503645206488,1503645206589,1,0,0,0,10,0,18
        Thread.sleep(20000);
        zooKeeper.close();

    }


    private static ZooKeeper getZkData() throws  Exception  {
        return new ZooKeeper(CONNECT_STRING, SESSION_TIME_OUT, (x) -> {
            if (Watcher.Event.KeeperState.SyncConnected == x.getState()) {
                //初始创建时
                if (Watcher.Event.EventType.None == x.getType() && null == x.getPath()) {
                    countDownLatch.countDown();
                    log.warn(" 连接zookeeper 服务 ok ");
                } else if (x.getType() == Watcher.Event.EventType.NodeDataChanged) {
                    //数据状态发生改变
                    try {
                        //获取数据
                        log.warn("watch = " + new String(zooKeeper.getData(x.getPath(),true,stat )) );
                        log.warn(" watch stat = " + stat.toString());
                    } catch (Exception e) {
                        log.error(" data get error ",e);
                    }
                }
            }
        });
    }



}
