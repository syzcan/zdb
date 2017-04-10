package com.zong.zdb.service;

import java.util.ArrayList;
import java.util.List;

import com.zong.zdb.bean.ColumnField;
import com.zong.zdb.bean.Table;
import com.zong.zdb.bean.TableEntity;
import com.zong.zdb.dao.IJdbcDao;
import com.zong.zdb.dao.MysqlCodeDao;
import com.zong.zdb.dao.OracleCodeDao;
import com.zong.zdb.util.Properties;

public class JdbcCodeService {
	private static final String DRIVER_ORACLE = "oracle.jdbc.driver.OracleDriver";
	private static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";

	private IJdbcDao makeJdbcDao(String dbname) {
		Properties props = new Properties(dbname);
		String driverClassName = props.getProperty("jdbc.driverClassName");
		if (driverClassName.equals(DRIVER_MYSQL)) {
			return new MysqlCodeDao(props);
		} else if (driverClassName.equals(DRIVER_ORACLE)) {
			return new OracleCodeDao(props);
		}
		return null;
	}

	public List<Table> showTables(String dbname) {
		List<TableEntity> list = makeJdbcDao(dbname).showTables();
		List<Table> tables = new ArrayList<Table>();
		for (TableEntity tableEntity : list) {
			Table table = new Table();
			table.setTableName(tableEntity.getTableName());
			table.setComment(tableEntity.getComment());
			table.setTotalResult(tableEntity.getTotalResult());
			tables.add(table);
		}
		return tables;
	}

	public List<ColumnField> showTableColumns(String dbname, String tableName) {
		return makeJdbcDao(dbname).showTableColumns(tableName);
	}

	
}
