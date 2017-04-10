package com.zong.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc 配置文件加载工具
 * @author zong
 * @date 2016年11月27日 上午2:09:30
 */
public class Properties {
	private Map data = new HashMap();

	public Properties(String dbname) {
		List<Map> dbs = (List<Map>) Config.configData.get(Config.DBS);
		for (Map map : dbs) {
			if (map.get("dbname").equals(dbname)) {
				data.put(Config.PACKAGE_NAME, Config.configData.get(Config.PACKAGE_NAME));
				data.put(Config.PACKAGE_BEAN, Config.configData.get(Config.PACKAGE_BEAN));
				data.put(Config.PACKAGE_MAPPER, Config.configData.get(Config.PACKAGE_MAPPER));
				data.put(Config.PACKAGE_SERVICE, Config.configData.get(Config.PACKAGE_SERVICE));
				data.put(Config.PACKAGE_CONTROLLER, Config.configData.get(Config.PACKAGE_CONTROLLER));
				data.put(Config.PACKAGE_JSP, Config.configData.get(Config.PACKAGE_JSP));
				data.put("jdbc.url", map.get("jdbc.url"));
				data.put("jdbc.username", map.get("jdbc.username"));
				data.put("jdbc.password", map.get("jdbc.password"));
				data.put("jdbc.database", map.get("jdbc.database"));
				data.put("jdbc.driverClassName", map.get("jdbc.driverClassName"));
			}
		}
	}

	public String getProperty(String key) {
		return (String) data.get(key);
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

}
