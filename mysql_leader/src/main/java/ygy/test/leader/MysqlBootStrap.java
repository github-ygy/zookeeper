package ygy.test.leader;

import com.sun.org.apache.regexp.internal.RE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ygy.test.DAL.DO.HeartBeatDO;
import ygy.test.DAL.mapper.HeartBeatDOMapper;
import ygy.test.common.contants.HeartBeatContants;
import ygy.test.common.contants.HeartBeatRoleContants;
import ygy.test.common.contants.SqlStatus;
import ygy.test.common.util.ConcurrentDateUtil;
import ygy.test.common.util.SystemConfigUtil;
import ygy.test.task.BusinessTask;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * Created by guoyao on 2017/9/1.
 */
@Component
public class MysqlBootStrap {

    private static final Logger log=LoggerFactory.getLogger(MysqlBootStrap.class);

    private static final int HEART_BEAT_RATE=30 * 1000;

    private static final int INVALIDATE_MASTER_SECOND=60;

    private static final String hostName=SystemConfigUtil.getHostname();

    @Autowired
    private HeartBeatDOMapper heartBeatDOMapper;

    @Autowired
    private BusinessTask businessTask;

    /**
     * 初始化启动
     */
    @PostConstruct
    public void startBootStrapAsyn() {
        //new Thread(
        //        ()-> {
        //            try {
        //                MysqlBootStrap.this.compaignInBackground();
        //            } catch (Exception e) {
        //                log.error("startBootStrapAsyn error " ,e);
        //            }
        //        }
        //,"mysqlBootStrap").start();
        //log.info(" startBootStrapAsyn is starting in back");
        try {
            compaignInBackground();
            log.info(" start bootStrap in back  success ");
        } catch (Exception e) {
            log.error(" start bootStrap error ", e);
        }
    }

    @Scheduled(fixedDelay=HEART_BEAT_RATE, initialDelay=HEART_BEAT_RATE * 2)
    public void checkMaster() {
        //检验是否有合格的master 存在
        HeartBeatDO existDO=heartBeatDOMapper.selectByMaster(HeartBeatRoleContants.ROLE_MASTER);
        try {
            boolean masterStatus=false;
            try {
                masterStatus=validateMaster(existDO);
            } catch (Exception e) {
                log.error(" check master is not exist ,init faile  ");
                registerHeartBeatByMysql();
                return;
            }

            // 存在合理 master  并且为当前服务
            if (masterStatus && hostName.equals(existDO.getHostName())) {
                //无论本项目是否正在运行，重启开启即可
                businessTask.start();
                //修改状态即可
                heartBeatDOMapper.updateCurrentStatus(existDO);
                log.info(" i am master ,i am running ok ");
                return;
            }

            // 存在合理 master  不是当前服务
            if (masterStatus && !hostName.equals(existDO.getHostName())) {
                //无论本项目是否正在运行，关闭即可
                businessTask.stop();
                //修改状态
                existDO.setHostName(hostName);
                existDO.setRole(HeartBeatRoleContants.ROLE_SLAVE);
                heartBeatDOMapper.updateCurrentStatus(existDO);
                log.info(" i am not  master ,i am running ok ");
                return;
            }

            //存在不合理的master
            //无论不合理master是否为本身服务，启动即可，并且修改为master
            existDO.setRole(HeartBeatRoleContants.ROLE_SLAVE);
            heartBeatDOMapper.updateCurrentStatus(existDO);
            existDO.setHostName(hostName);
            businessTask.start();
            existDO.setRole(HeartBeatRoleContants.ROLE_MASTER);
            heartBeatDOMapper.updateCurrentStatus(existDO);
            log.info(" i am master ,i am running ok  ");
        } catch (Exception e) {
            log.error("checkMaster  error", e);
            //无论出现什么异常，关闭服务状态
            businessTask.stop();
        }

    }

    private boolean validateMaster(HeartBeatDO existDO) throws Exception {
        if (null == existDO) {
            throw new Exception();
        }
        Date updateTime=existDO.getUpdateTime();
        long second=ConcurrentDateUtil.diffDate(updateTime, new Date(), ConcurrentDateUtil.Type.SECOND);
        if (second > INVALIDATE_MASTER_SECOND) {
            return false;
        }
        return true;
    }


    //开启业务
    private void startBusiness() {
        new Thread(
                () -> businessTask.start()
        ).start();
    }

    private void compaignInBackground() {
        //TODO validate hostname
        // 初始化启动，注册为slave状态。
        int registerStatus=registerHeartBeatByMysql();
        if (HeartBeatContants.REGIST_FAIL == registerStatus) {
            log.error(" compaignInBackground  registerHeartBeatByMysql  fail param hostName = " + hostName);
            return;
        }
        log.info("compaignInBackground registerHeartBeatByMysql  success  hostName = " + hostName);
        // 注册成功后 开始抢占master
        int compaignStatus=compaignMaster();
        if (HeartBeatContants.COMPAIGN_FAIL == compaignStatus) {
            log.warn(" compaignInBackground  compaignMaster init fail param hostName = " + hostName);
            return;
        }
        //启动业务程序
        startBusiness();
        log.info("compaignInBackground compaignMaster success  hostName = " + hostName);
    }

    private int compaignMaster() {
        try {
            HeartBeatDO existDO=heartBeatDOMapper.selectByMaster(HeartBeatRoleContants.ROLE_MASTER);
            if (null == existDO) {
                existDO=new HeartBeatDO();
                existDO.setRole(HeartBeatRoleContants.ROLE_MASTER);
                existDO.setHostName(hostName);
                heartBeatDOMapper.updateCurrentStatus(existDO);
                return HeartBeatContants.COMPAIGN_SUCCESS;
            }
        } catch (Exception e) {
            log.info(" compaignMaster error param  hostName = " + hostName, e);
        }
        return HeartBeatContants.COMPAIGN_FAIL;
    }

    private int registerHeartBeatByMysql() {
        HeartBeatDO heartBeatDO=new HeartBeatDO();
        heartBeatDO.setRole(HeartBeatRoleContants.ROLE_SLAVE);
        heartBeatDO.setHostName(hostName);
        //更新role角色为slave
        try {
            int updateStatus=heartBeatDOMapper.updateCurrentStatus(heartBeatDO);
            if (updateStatus < SqlStatus.UPDATE_SUCCESS) {
                heartBeatDOMapper.insert(heartBeatDO);
            }
            return HeartBeatContants.REGIST_SUCCESS;
        } catch (Exception e) {
            log.error(" heart beat error  hostName =  " + hostName, e);
            return HeartBeatContants.REGIST_FAIL;
        }
    }
}
