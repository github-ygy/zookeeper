package ygy.test.DAL.mapper;

import ygy.test.DAL.DO.HeartBeatDO;

/**
 * Created by guoyao on 2017/9/1.
 */
public interface HeartBeatDOMapper {

    int updateCurrentStatus(HeartBeatDO heartBeatDO);

    int insert(HeartBeatDO heartBeatDO);

    HeartBeatDO selectByMaster(String roleMaster);

    int resetRole2Slave(HeartBeatDO heartBeatDO);
}
