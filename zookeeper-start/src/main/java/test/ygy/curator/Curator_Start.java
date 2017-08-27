package test.ygy.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by guoyao on 2017/8/27.
 */
public class Curator_Start {

    private static final Logger log = LoggerFactory.getLogger(Curator_Start.class);

    public static final  String CONNECT_STRING = "192.168.150.130:2181,192.168.150.132:2181,192.168.150.133:2181";

    public static final  int SESSION_TIMEOUT_MS = 5000;

    public static final  int CONNECTION_TIMEOUT_MS  = 5000 ;


    public static void main(String[] args) throws  Exception {


        //public static CuratorFramework newClient(
        // String connectString,   //服务器连接地址
        // int sessionTimeoutMs,   //会话超时时间，单位为毫秒 默认60000
        // int connectionTimeoutMs,  //连接创建超时时间，单位为毫秒。
        // RetryPolicy retryPolicy) {  //重试策略接口
        //public boolean      allowRetry(int retryCount,   //已经重试的次数
        // long elapsedTimeMs,  //从第一次重试开始已经花费的时间，单位毫秒
        // RetrySleeper sleeper);  //用于sleep指定的时间

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        //public ExponentialBackoffRetry(int baseSleepTimeMs,   初始sleep时间
        // int maxRetries,      最大重试次数
        // int maxSleepMs)      最大sleep时间
        // copied from Hadoop's RetryPolicies.java  ExponentialBackoffRetry 睡眠策略
        //long sleepMs = baseSleepTimeMs * Math.max(1, random.nextInt(1 << (retryCount + 1)));
        // sleepMs 将不会超过最大sleep时间

        //CuratorFramework curatorFramework=CuratorFrameworkFactory.newClient(
        //        CONNECT_STRING, SESSION_TIMEOUT_MS, CONNECTION_TIMEOUT_MS, retryPolicy);

        //fluent风格
        CuratorFramework curatorFramework=CuratorFrameworkFactory.builder()
                .connectString(CONNECT_STRING)
                .sessionTimeoutMs(SESSION_TIMEOUT_MS)
                .connectionTimeoutMs(CONNECTION_TIMEOUT_MS)
                .namespace("isolation")   //不同的命名空间，隔离zookeeper业务，互不干扰
                .retryPolicy(retryPolicy).build();

        curatorFramework.start();
        log.warn(" 启动成功");

        Thread.sleep(10000000);
        curatorFramework.close();
    }

    public static CuratorFrameworkFactory.Builder getBaseBuilder() {
        return  CuratorFrameworkFactory.builder()
                .connectString(CONNECT_STRING)
                .sessionTimeoutMs(SESSION_TIMEOUT_MS)
                //.connectionTimeoutMs(CONNECTION_TIMEOUT_MS)
                .retryPolicy(new ExponentialBackoffRetry(1000,3));
    }
}
