package ygy.test.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 系统配置工具
 */
public class SystemConfigUtil {

	public static final String ERROR_NAME="error name!"; 
	/**
	 * 获得服务器名字
	 * 	失败返回： ERROR_NAME 常量
	 * @return
	 */
	public static String getHostname(){
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return ERROR_NAME;
		}
	}
}
