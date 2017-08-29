package test.ygy.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by guoyao on 2017/8/29.
 */
public class Curator_PathChildrenListener {

    private static final Logger log=LoggerFactory.getLogger(Curator_PathChildrenListener.class);

    public static void main(String[] args) throws Exception {

        CuratorFramework cf=Curator_Start.getYGYBuilder().build();
        cf.start();
        log.warn(" zookeepeer  启动成功");

        /**
         * @param client           the client  客服端
         * @param path             path to watch  节点路径
         * @param cacheData        if true, node contents are cached in addition to the stat  是否需要获取节点数据
         * @param dataIsCompressed if true, data in the path is compressed  是否压缩
         * @param executorService  Closeable ExecutorService to use for the PathChildrenCache's background thread
         */

        // 默认线程池为守护线程  主线程执行完毕即停止
        //return (new ThreadFactoryBuilder()).setNameFormat(processName + "-%d").setDaemon(true).build();
        PathChildrenCache pathChildrenCache=new PathChildrenCache(
                cf,
                "/test",
                true
        );
        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        pathChildrenCache.getListenable().addListener(
                new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                        switch (event.getType()) {
                            //10:33:44.078 [PathChildrenCache-0] WARN test.ygy.curator.Curator_PathChildrenListener -  INITIALIZED 初始化
                            case INITIALIZED:
                                log.warn(" INITIALIZED 初始化");
                                break;
                            // 10:33:45.955 [PathChildrenCache-0] WARN test.ygy.curator.Curator_PathChildrenListener -  CHILD_ADDED 新增子节点 ChildData{path='/test/child1', stat=70,70,1503974025843,1503974025843,0,0,0,98542789669486605,11,0,70
                            //, data=[116, 101, 115, 116, 32, 99, 104, 105, 108, 100, 49]}
                            case CHILD_ADDED:
                                log.warn(" CHILD_ADDED 新增子节点 " + event.getData());
                                break;
                            //10:33:54.062 [PathChildrenCache-0] WARN test.ygy.curator.Curator_PathChildrenListener -  CHILD_REMOVED 移除子节点ChildData{path='/test/child3', stat=72,72,1503974029930,1503974029930,0,0,0,98542789669486605,11,0,72
                            // , data=[116, 101, 115, 116, 32, 99, 104, 105, 108, 100, 51]}
                            case CHILD_REMOVED:
                                log.warn(" CHILD_REMOVED 移除子节点" + event.getData());
                                break;
                            //10:33:52.056 [PathChildrenCache-0] WARN test.ygy.curator.Curator_PathChildrenListener -  CHILD_UPDATED 修改子节点数据ChildData{path='/test/child2', stat=71,73,1503974027885,1503974031986,1,0,0,98542789669486605,15,0,71
                            //, data=[110, 101, 119, 32, 99, 104, 105, 108, 100, 50, 32, 100, 97, 116, 97]}
                            case CHILD_UPDATED:
                                log.warn(" CHILD_UPDATED 修改子节点数据" + event.getData());
                                break;
                            case CONNECTION_LOST:
                                log.warn(" CONNECTION_LOST 确认失去连接");
                                break;
                            case CONNECTION_SUSPENDED:
                                log.warn(" CONNECTION_SUSPENDED 连接挂起，可能失去连接");
                                break;
                            case CONNECTION_RECONNECTED:
                                log.warn(" CONNECTION_RECONNECTED 重新获取连接");
                                break;
                            default:
                                break;
                        }
                    }
                }
        );
        //报错 pathChildrenCache 构造方法中已经会确保该path 被创建
        //cf.create().withMode(CreateMode.PERSISTENT).forPath("/test", "test".getBytes());
        Thread.sleep(2000);
        cf.create().withMode(CreateMode.EPHEMERAL).forPath("/test/child1", "test child1".getBytes());
        Thread.sleep(2000);
        cf.create().withMode(CreateMode.EPHEMERAL).forPath("/test/child2", "test child2".getBytes());
        Thread.sleep(2000);
        cf.create().withMode(CreateMode.EPHEMERAL).forPath("/test/child3", "test child3".getBytes());
        Thread.sleep(2000);
        cf.setData().forPath("/test/child2", "new child2 data".getBytes());
        Thread.sleep(2000);
        cf.delete().forPath("/test/child3");
        Thread.sleep(20000);
        cf.close();
    }
}
