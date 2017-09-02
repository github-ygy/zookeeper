package ygy.test.DAL.DO;

import lombok.Data;

import java.util.Date;

/**
 * Created by guoyao on 2017/9/1.
 */
@Data
public class HeartBeatDO {

    private String hostName ;   //host name

    private Date createTime ;   //创建时间

    private String role ;        //角色  master or slave

    private Date updateTime ;    //最近修改时间



}
