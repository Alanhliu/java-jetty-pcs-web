package com.siasun.pcsweb.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	private final static Logger logger = LoggerFactory.getLogger(Constants.class.getName());

	// 服务器IP和端口
	public final static String IP;
	public final static int PORT;

	public final static String USER_PATH = System.getProperty("user.dir");

	// ems属性
	public final static String EmsProp = "物品";
	public final static String EmsCity = "沈阳市";
	public final static String EmsDefaultweight = "默认重量";
	
	// 短信地址
	public final static String MESSAGE_URL;

	static {
		Properties prop = new Properties();
		try {
			InputStream is = new FileInputStream(new File(//System.getProperty("user.dir") +
					"/Users/siasun/Desktop/pcs-web/cfg.properties"));
			if (null != is) {
				prop.load(is);
			}
		} catch (Exception e) {
			logger.error("Cfg.properties init failed.", e);
		}

		// 服务器
		IP = prop.getProperty("SERVER.IP");
		PORT = Integer.parseInt(prop.getProperty("SERVER.PORT"));
		MESSAGE_URL = prop.getProperty("MESSAGE.URL");
		logger.info("Cfg.properties init done.");
	}

}