package com.zong.zdb.util;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @desc 配置读取与写入
 * @author zong
 * @date 2016年11月27日 下午10:48:35
 */
public class Config {
	public static final String JDBC_DRIVER = "jdbc.driverClassName";
	public static final String JDBC_URL = "jdbc.url";
	public static final String JDBC_USERNAME = "jdbc.username";
	public static final String JDBC_PASSWORD = "jdbc.password";

	public static final String CONFIG_DATA = "configData";
	public static final String PACKAGE_NAME = "packageName";
	public static final String PACKAGE_BEAN = "packageBean";
	public static final String PACKAGE_MAPPER = "packageMapper";
	public static final String PACKAGE_SERVICE = "packageService";
	public static final String PACKAGE_CONTROLLER = "packageController";
	public static final String PACKAGE_JSP = "packageJsp";
	public static final String DBS = "dbs";
	public static Map configData;

	public static Map readConfig() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		configData = mapper.readValue(cutComment(FileUtils.readTxt(FileUtils.getClassResources() + "zdb.json")),
				HashMap.class);
		return configData;
	}

	public static Map readConfig(String json) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		configData = mapper.readValue(cutComment(json), HashMap.class);
		return configData;
	}

	public static Map writeConfig(String json) throws Exception {
		FileUtils.writeTxt(FileUtils.getClassResources() + "config.json", json);
		ObjectMapper mapper = new ObjectMapper();
		configData = mapper.readValue(cutComment(json), HashMap.class);
		return configData;
	}

	private static String cutComment(String content) {
		String[] lines = content.split("\n");
		StringBuffer sb = new StringBuffer();
		for (String line : lines) {
			if (!line.trim().startsWith("#")) {
				sb.append(line + "\n");
			}
		}
		return sb.toString();
	}

	public static Map getConfigData() {
		if (configData == null) {
			try {
				configData = readConfig();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return configData;
	}
}
