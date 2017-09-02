package ygy.test.DAL.mapper;

import ygy.test.DAL.DO.HeartBeatDO;

/**
 * Created by guoyao on 2017/9/1.
 */
public interface HeartBeatDOMapper {

    HeartBeatDO selectByHostName(String hostName);
}
