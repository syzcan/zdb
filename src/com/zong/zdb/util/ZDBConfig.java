package com.zong.zdb.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zong.zdb.dao.IJdbcDao;
import com.zong.zdb.dao.MysqlCodeDao;
import com.zong.zdb.dao.OracleCodeDao;
import com.zong.zdb.service.JdbcCodeService;

/**
 * @desc 配置读取与写入
 * @author zong
 * @date 2016年11月27日 下午10:48:35
 */
public class ZDBConfig {
	public static final String DRIVER_ORACLE = "oracle.jdbc.driver.OracleDriver";
	public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";

	public static final String JDBC_DRIVER = "jdbc.driverClassName";
	public static final String JDBC_URL = "jdbc.url";
	public static final String JDBC_USERNAME = "jdbc.username";
	public static final String JDBC_PASSWORD = "jdbc.password";
	public static final String DBNAME = "dbname";

	public static final String PACKAGE_NAME = "packageName";
	public static final String PACKAGE_BEAN = "packageBean";
	public static final String PACKAGE_MAPPER = "packageMapper";
	public static final String PACKAGE_SERVICE = "packageService";
	public static final String PACKAGE_CONTROLLER = "packageController";
	public static final String PACKAGE_JSP = "packageJsp";
	public static final String DBS = "dbs";
	public static Map configData = new HashMap();;
	public static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 读取配置文件
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Map readConfig() throws Exception {
		configData = objectMapper.readValue(cutComment(FileUtils.readTxt(FileUtils.getClassResources() + "zdb.json")),
				Map.class);
		List<Map> dbDatas = (List<Map>) configData.get(DBS);
		initDataConns(configData);
		return configData;
	}

	public static Map readConfig(String json) throws Exception {
		configData = objectMapper.readValue(cutComment(json), Map.class);
		initDataConns(configData);
		return configData;
	}

	public static Map writeConfig(String json) throws Exception {
		FileUtils.writeTxt(FileUtils.getClassResources() + "zdb.json", json);
		configData = objectMapper.readValue(cutComment(json), Map.class);
		initDataConns(configData);
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

	public static Map setConfigData(Map config) {
		configData = config;
		try {
			initDataConns(configData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return configData;
	}

	public static Map getConfigData() {
		if (configData == null) {
			try {
				configData = readConfig();
			} catch (Exception e) {
				e.printStackTrace();
				configData = new HashMap();
			}
		}
		return configData;
	}

	/**
	 * 配置文件代码生成配置插入到每个数据库链接
	 */
	private static void initCodeConfig(Map configData) throws Exception {
		List<Map> dbDatas = (List<Map>) configData.get(DBS);
		if (dbDatas != null) {
			for (Map data : dbDatas) {
				data.put(ZDBConfig.PACKAGE_NAME, ZDBConfig.configData.get(ZDBConfig.PACKAGE_NAME));
				data.put(ZDBConfig.PACKAGE_BEAN, ZDBConfig.configData.get(ZDBConfig.PACKAGE_BEAN));
				data.put(ZDBConfig.PACKAGE_MAPPER, ZDBConfig.configData.get(ZDBConfig.PACKAGE_MAPPER));
				data.put(ZDBConfig.PACKAGE_SERVICE, ZDBConfig.configData.get(ZDBConfig.PACKAGE_SERVICE));
				data.put(ZDBConfig.PACKAGE_CONTROLLER, ZDBConfig.configData.get(ZDBConfig.PACKAGE_CONTROLLER));
				data.put(ZDBConfig.PACKAGE_JSP, ZDBConfig.configData.get(ZDBConfig.PACKAGE_JSP));
			}
		}
	}

	/**
	 * 初始化数据库连接池
	 */
	private static void initDataConns(Map configData) {
		List<Map> dbDatas = (List<Map>) configData.get(DBS);
		if (dbDatas != null) {
			for (Map data : dbDatas) {
				String driverClassName = data.get(JDBC_DRIVER).toString();
				String url = data.get(JDBC_URL).toString();
				String username = data.get(JDBC_USERNAME).toString();
				String password = data.get(JDBC_PASSWORD).toString();
				String[] ss = url.split("\\?")[0].split("/");
				String database = ss[ss.length - 1];
				IJdbcDao dao = null;
				try {
					// 初始化驱动包
					Class.forName(driverClassName);
					Connection conn = DriverManager.getConnection(url, username, password);
					if (driverClassName.equals(DRIVER_MYSQL)) {
						dao = new MysqlCodeDao(database, conn);
					} else if (driverClassName.equals(DRIVER_ORACLE)) {
						dao = new OracleCodeDao(username, conn);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				data.put("dao", dao);
			}
		}

	}

	public static IJdbcDao getDao(String dbname) {
		List<Map> dbDatas = (List<Map>) configData.get(DBS);
		if (dbDatas != null) {
			for (Map data : dbDatas) {
				if (data.get(DBNAME).equals(dbname)) {
					return (IJdbcDao) data.get("dao");
				}
			}
		}
		return null;
	}

	public static void main(String[] args) {
		try {
			JdbcCodeService codeService = JdbcCodeService.getInstance();
			codeService.setDriverClassName(DRIVER_MYSQL);
			codeService.setUrl("jdbc:mysql://localhost:3306/dwr");
			codeService.setUsername("root");
			codeService.setPassword("123456");
			System.out.println(
					objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(codeService.currentTables()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
