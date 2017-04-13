package com.zong.zdb.dao;

import java.util.List;

import com.zong.zdb.bean.ColumnField;
import com.zong.zdb.bean.Table;

public interface IJdbcDao {
	/**
	 * 获取当前用户【数据库】所有表名和描述
	 */
	public List<Table> showTables();

	/**
	 * 获取某个表字段
	 */
	public List<ColumnField> showTableColumns(String tableName);

	/**
	 * 获取某个表信息
	 * 
	 * @param tableName
	 */
	public Table showTable(String tableName);
}
