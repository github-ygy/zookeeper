package test.ygy.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by guoyao on 2017/8/30.
 */
public class Curator_Zkpath {

    private static final Logger log=LoggerFactory.getLogger(Curator_Zkpath.class);

    public static void main(String[] args) throws  Exception {
        CuratorFramework cf=Curator_Start.getYGYBuilder().build();
        cf.start();
        log.warn(" zookeeper 启动成功");

        log.warn(" zkpath fix Name Space " + ZKPaths.fixForNamespace("namespace", "/test/zk-path"));
        //16:01:18.210 [main] WARN test.ygy.curator.Curator_Zkpath -  zkpath fix Name Space /namespace/test/zk-path
        log.warn(" zkpath make path  "+ ZKPaths.makePath("parent","child"));
        //16:06:48.684 [main] WARN test.ygy.curator.Curator_Zkpath -  zkpath make path  /parent/child
        log.warn("  zkpath get Node  " + ZKPaths.getNodeFromPath("/parent/child"));  //必须以/开头
        //16:08:06.019 [main] WARN test.ygy.curator.Curator_Zkpath -   zkpath get Node  child
        log.warn(" zkpath get path and node " + ZKPaths.getPathAndNode("/parent/child"));

        ZKPaths.mkdirs(cf.getZookeeperClient().getZooKeeper(),"/parent/child1");
        ZKPaths.mkdirs(cf.getZookeeperClient().getZooKeeper(),"/parent/child2");

        List<String> sortedChildren=ZKPaths.getSortedChildren(cf.getZookeeperClient().getZooKeeper(), "/parent");
        log.warn("sortedChildren =  " + sortedChildren);
        //17:05:50.557 [main] WARN test.ygy.curator.Curator_Zkpath - sortedChildren =  [child1, child2]
        ZKPaths.deleteChildren(cf.getZookeeperClient().getZooKeeper(),"/parent",false);
    }
}
