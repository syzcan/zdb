package com.zong.zdb.dao;

import java.util.List;

import com.zong.zdb.bean.ColumnField;
import com.zong.zdb.bean.Table;
import com.zong.zdb.util.Page;
import com.zong.zdb.util.PageData;

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

	/**
	 * 分页查询表数据
	 * 
	 * @param page
	 */
	public List<PageData> showTableDatas(Page page);

	/**
	 * 执行sql查询表数据
	 * 
	 * @param sql
	 */
	public List<PageData> showSqlDatas(String sql);

	/**
	 * 插入数据
	 * 
	 * @param tableName
	 * @param data
	 */
	public void insert(String tableName, PageData data);

	/**
	 * 更新数据
	 * 
	 * @param tableName
	 * @param data 更新的字段键值
	 * @param idPd 主键或查询条件键值
	 */
	public void update(String tableName, PageData data, PageData idPd);

	/**
	 * 创建数据表
	 * 
	 * @param tableData {"tableName":"user","comment":"用户表","columns":[{"column":"id","type":"int(11)","remark":"主键"},{"column":"name","type":"varchar(50)","remark":"姓名"}]}
	 * @return
	 */
	public boolean createTable(PageData tableData);
	
	/**
	 * 更新表结构，只更新表注释、更新字段或新增字段，不删减字段
	 * 
	 * @param tableData
	 * @return
	 */
	public boolean alterTable(PageData tableData);
}
