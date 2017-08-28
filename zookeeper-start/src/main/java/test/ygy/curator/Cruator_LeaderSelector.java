package test.ygy.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.utils.EnsurePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by guoyao on 2017/8/28.
 */
//leader选举
public class Cruator_LeaderSelector {

    private static final Logger log=LoggerFactory.getLogger(Cruator_LeaderSelector.class);

    private static final String MASTER_PATH="/curator-master";

    public static void main(String[] args) throws  Exception {
        CuratorFramework cf=Curator_Start.getBaseBuilder().build();
        cf.start();
       log.warn(" zookeeper  启动成功");

        //保证节点存在
        new EnsurePath(MASTER_PATH+"/AAAA").ensure(cf.getZookeeperClient());
        //   * @param client          the client  客户端
     //  * @param leaderPath      the path for this leadership group  节点路径
     //* @param executorService thread pool to use   线程池
     // * @param listener        listener   监听器
        LeaderSelector leaderSelector = new LeaderSelector(
                cf
                , MASTER_PATH
                , new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {

                log.warn(" i am master ");
                Thread.sleep(3000);
                log.warn(" off master ");
            }
        });
        leaderSelector.autoRequeue();
        leaderSelector.start();

        Thread.sleep(200000000);
        cf.close();
    }
}
