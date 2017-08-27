package test.ygy.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by guoyao on 2017/8/27.
 */
public class Curator_Crud {

    private static final Logger log=LoggerFactory.getLogger(Curator_Crud.class);

    public static void main(String[] args) throws Exception {

        CuratorFramework cf=Curator_Start.getBaseBuilder().build();

        cf.start();
        log.warn(" zookeeper 启动成功");


        //创建节点  默认创建持久节点，节点数据默认为空
        //************* 报错KeeperErrorCode = ConnectionLoss for /zk-test  curator 与 服务器版本冲突
        //cf.create().forPath("/zk-test");

        //递归创建
        //cf.create().creatingParentsIfNeeded()   //如果父节点不存在 递归创建
        //        .withMode(CreateMode.PERSISTENT)    //指定节点类型
        //        .forPath("/test/child", "init".getBytes());   //初始化值  /test：空  test/child：init

        //删除节点
        //cf.delete().forPath("/test/child");

        //递归删除当前节点及当前节点的所有子节点
        //cf.delete().guaranteed()    //只要当前会话内，curator会在后台持续进行删除，直到节点被删除
        //        .deletingChildrenIfNeeded()
        //        .forPath("/test");

        //获取节点数据
        //byte[] bytes=cf.getData().forPath("/test");
        //log.warn(" /test 's data is " + new String(bytes));
        //23:37:09.563 [main] WARN test.ygy.curator.Curator_Crud -  /test 's data is 1111

        //获取节点数据，同时获取stat(服务器创建新stat替换原stat)
        //Stat oldStat = new Stat() ;
        //byte[] bytes=cf.getData().storingStatIn(oldStat).forPath("/test");
        //log.warn(" stat  =" + oldStat);
        //23:39:20.694 [main] WARN test.ygy.curator.Curator_Crud -  stat  =21474836550,21474836550,1503848137880,1503848137880,0,0,0,0,4,0,21474836550


        //更新节点数据
        Stat stat=cf.setData()
                .withVersion(-1)  //根据版本信息来修改，可从stat中获取实现cas
                .forPath("/test", "new data ".getBytes());
        log.warn(" updated  stat = " + stat);
        //23:45:05.168 [main] WARN test.ygy.curator.Curator_Crud -  updated  stat = 21474836550,21474836556,1503848137880,1503848704703,1,0,0,0,9,0,21474836550

        Thread.sleep(2000);


        cf.close();

    }
}
