package com.zong.web.dbclient.dao;

import java.util.List;

import com.zong.web.dbclient.bean.ColumnField;
import com.zong.web.dbclient.bean.TableEntity;

public interface IJdbcDao {
	/**
	 * 获取当前用户【数据库】所有表名和描述
	 */
	public List<TableEntity> showTables();

	/**
	 * 获取某个表字段
	 */
	public List<ColumnField> showTableColumns(String tableName);

}
