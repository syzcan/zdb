package com.zong.zdb.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zong.zdb.bean.ColumnField;
import com.zong.zdb.bean.Table;
import com.zong.zdb.dao.IJdbcDao;
import com.zong.zdb.util.ZDBConfig;

public class JdbcCodeService {

	private static JdbcCodeService codeService;
	private static String CURRENT_DB = "zong_db_666666_88888888";
	private String driverClassName;
	private String url;
	private String username;
	private String password;

	public static JdbcCodeService getInstance() {
		if (codeService == null) {
			codeService = new JdbcCodeService();
		}
		return codeService;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IJdbcDao getCurrentDBDao() {
		if (getDao(CURRENT_DB) != null) {
			return getDao(CURRENT_DB);
		}
		Map db = new HashMap();
		db.put(ZDBConfig.JDBC_DRIVER, driverClassName);
		db.put(ZDBConfig.JDBC_URL, url);
		db.put(ZDBConfig.JDBC_USERNAME, username);
		db.put(ZDBConfig.JDBC_PASSWORD, password);
		db.put(ZDBConfig.DBNAME, CURRENT_DB);
		Map configData = ZDBConfig.getConfigData();
		List<Map> dbs = (List<Map>) configData.get(ZDBConfig.DBS);
		if (dbs == null) {
			dbs = new ArrayList<Map>();
		}
		dbs.add(db);
		configData.put(ZDBConfig.DBS, dbs);
		ZDBConfig.setConfigData(configData);

		return getDao(CURRENT_DB);
	}

	private static IJdbcDao getDao(String dbname) {
		return ZDBConfig.getDao(dbname);
	}

	public Table showTable(String dbname, String tableName) {
		return getDao(dbname).showTable(tableName);
	}

	public List<Table> showTables(String dbname) {
		return getDao(dbname).showTables();
	}

	public List<ColumnField> showTableColumns(String dbname, String tableName) {
		return getDao(dbname).showTableColumns(tableName);
	}

	public Table currentTable(String tableName) {
		return getCurrentDBDao().showTable(tableName);
	}

	public List<Table> currentTables() {
		return getCurrentDBDao().showTables();
	}

	public List<ColumnField> currentTableColumns(String tableName) {
		return getCurrentDBDao().showTableColumns(tableName);
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
