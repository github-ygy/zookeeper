package test.ygy.alone;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by guoyao on 2017/8/24.
 */
public class Zookeeper_delSynAsyn {

    private static final Logger log=LoggerFactory.getLogger(Zookeeper_delSynAsyn.class);


    public static void main(String[] args) throws  Exception{

        ZooKeeper zk=Zookeeper_Provider.getZK();
        Zookeeper_Provider.countDownLatch.await();


        //同步删除  只能删除叶子节点
        //public void delete(final String path,  //节点路径
        // int version,       //版本  指定为-1时为所有
        // VoidCallback cb,  //回调函数
        // Object ctx)       //参数
        //zk.delete("/test-zk",-1);

        //00:00:39.073 [main-EventThread] WARN test.ygy.alone.Zookeeper_delSynAsyn -  rc = 0 path = /test-zk ctx =  param
        zk.delete("/test-zk", -1, new AsyncCallback.VoidCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx) {
                log.warn(" rc = " + rc + " path = " + path + " ctx = " + ctx );
            }
        }," param ");

        Thread.sleep(20000);
        log.warn( " main class over");
        zk.close();
    }
}
