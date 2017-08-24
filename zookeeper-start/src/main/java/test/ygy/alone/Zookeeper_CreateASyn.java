package test.ygy.alone;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by guoyao on 2017/8/24.
 */
public class Zookeeper_CreateASyn {

    private static final Logger log=LoggerFactory.getLogger(Zookeeper_CreateASyn.class);


    public static void main(String[] args) throws  Exception{

        ZooKeeper zk=Zookeeper_Provider.getZK();

        Zookeeper_Provider.countDownLatch.await();

        //异步调用参数说明
        //rc = -110 表示已经有此节点 -4 客户端与服务端断开连接 -112 会话已过期  0 接口调用成功
        //path 节点路径
        //接口传入 ctx 参数
        //实际zookeeper服务器节点创建路径（顺序节点后有数字）
        zk.create("/test-zk-asyn", "ygy-test1".
                        getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL, new AsyncCallback.StringCallback() {
                    @Override
                    public void processResult(int rc, String path, Object ctx, String name) {
                        log.warn(" rc = " + rc + " path = " + path + " ctx = " + ctx  + " name = " + name);
                    }
                }," param "
        );
        //23:32:32.772 [main-EventThread] WARN test.ygy.alone.Zookeeper_CreateASyn -  rc = 0 path = /test-zk-asyn ctx =  param  name = /test-zk-asyn
        zk.create("/test-zk-asyn", "ygy-test1".
                        getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL, new AsyncCallback.StringCallback() {
                    @Override
                    public void processResult(int rc, String path, Object ctx, String name) {
                        log.warn(" rc = " + rc + " path = " + path + " ctx = " + ctx  + " name = " + name);
                    }
                }," param "
        );
        //23:32:32.773 [main-EventThread] WARN test.ygy.alone.Zookeeper_CreateASyn -  rc = -110 path = /test-zk-asyn ctx =  param  name = null
        zk.create("/test-zk-asyn-seq", "ygy-test1".
                        getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL, new AsyncCallback.StringCallback() {
                    @Override
                    public void processResult(int rc, String path, Object ctx, String name) {
                        log.warn(" rc = " + rc + " path = " + path + " ctx = " + ctx  + " name = " + name);
                    }
                }," param "
        );
        //23:32:33.424 [main-EventThread] WARN test.ygy.alone.Zookeeper_CreateASyn -  rc = 0 path = /test-zk-asyn-seq ctx =  param  name = /test-zk-asyn-seq0000000003
        Thread.sleep(20000);
        log.warn( " main class over");
        zk.close();
    }
}
