package ygy.test.leader;

import com.sun.org.apache.regexp.internal.RE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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

    private static final int HEART_BEAT_RATE=50 * 1000;

    private static final int INVALIDATE_MASTER_SECOND=120 ;

    private static final String hostName=SystemConfigUtil.getHostname();

    private  static  volatile boolean isConflict = true;

    @Autowired
    private HeartBeatDOMapper heartBeatDOMapper;

    @Autowired
    private BusinessTask businessTask;

    /**
     * 初始化启动
     */
    @PostConstruct
    public void startBootStrapAsyn() {
        try {
            compaignInBackground();
            log.info(" start bootStrap in back  success ");
        } catch (Exception e) {
            log.error(" start bootStrap error ", e);
        }
    }

    @Scheduled(fixedRate = HEART_BEAT_RATE  )
    public void startHeartBeatCheckMaster() {
        if (isConflict) {
            log.error(" startHeartBeatCheckMaster hostName conflict  schedule start fail hostName =" + hostName);
            return;
        }
        try {
            //检验是否有合格的master 存在
            HeartBeatDO existDO= heartBeatDOMapper.selectByMaster(HeartBeatRoleContants.ROLE_MASTER);
            if (existDO == null) {
                log.error("startHeartBeatCheckMaster selectByMaster  master is not exist ,init fail  ");
                compaignInBackground();
                return;
            }
            boolean masterStatus=validateRunning(existDO);
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
                updateStatus(hostName, HeartBeatRoleContants.ROLE_SLAVE);
                log.info(" i am not  master ,i am running ok ");
                return;
            }
            //存在不合理的master
            //无论不合理master是否为本身服务，启动即可，并且修改为master
            existDO.setRole(HeartBeatRoleContants.ROLE_SLAVE);
            heartBeatDOMapper.updateCurrentStatus(existDO);
            updateStatus(hostName, HeartBeatRoleContants.ROLE_MASTER);
            businessTask.start();
            log.info(" i am master ,i am running ok  ");
        } catch (Exception e) {
            log.error("checkMaster  error", e);
            //无论出现什么异常，关闭服务状态,修改自身状态
            businessTask.stop();
            updateStatus(hostName, HeartBeatRoleContants.ROLE_SLAVE);
        }
    }

   private void  updateStatus(String role ,String hostName) {
       HeartBeatDO heartBeatDO=new HeartBeatDO();
       heartBeatDO.setRole(role);
       heartBeatDO.setHostName(hostName);
       heartBeatDOMapper.updateCurrentStatus(heartBeatDO);
   }


    private boolean validateRunning(HeartBeatDO existDO)  {
        if (null == existDO) {
            return false;
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
        isConflict = true ;   //如果出现 master is not exist 则关闭任务
        boolean hostNameStatus=validateHostName();
        if (hostNameStatus) {
            log.error("compaignInBackground  validateHostName hostName conflict  hostName =" + hostName);
            return;
        }
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
            isConflict = false ;
            return;
        }
        //启动业务程序
        startBusiness();
        isConflict = false ;
        log.info("compaignInBackground compaignMaster success  hostName = " + hostName);
    }

    private boolean validateHostName()  {
        if (StringUtils.isEmpty(hostName)) {
            return false ;
        }
        // 查询是否已经存在正在运行的hostName
        HeartBeatDO heartBeatDO=heartBeatDOMapper.selectByHostName(hostName);

        //有可能是本机需要重启，并不是冲突
        boolean validateStatus=validateRunning(heartBeatDO);
        if (validateStatus) {
            try {
                Thread.sleep(INVALIDATE_MASTER_SECOND * 1000);
            } catch (InterruptedException e) {
               log.error(" validateHostName  thead sleep eroor ",e);
            }
            HeartBeatDO nowHeartBeatDO=heartBeatDOMapper.selectByHostName(hostName);
            return  validateRunning(nowHeartBeatDO);
        }
        return false ;
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
