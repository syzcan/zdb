package com.zong.zdb.bean;

import java.util.ArrayList;
import java.util.List;

public class Table {
	protected String tableName;
	protected String comment;
	protected int totalResult;

	protected List<ColumnField> columnFields;
	// 按普通列和主键列分组
	protected List<ColumnField> normalColumns = new ArrayList<ColumnField>();
	protected List<ColumnField> primaryColumns = new ArrayList<ColumnField>();
	// 唯一主键，有多个联合主键时只保存第一次
	protected ColumnField primary;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getTotalResult() {
		return totalResult;
	}

	public void setTotalResult(int totalResult) {
		this.totalResult = totalResult;
	}

	public List<ColumnField> getColumnFields() {
		return columnFields;
	}

	public void setColumnFields(List<ColumnField> columnFields) {
		this.columnFields = columnFields;
		setNormalPrimaryColumns();
	}

	public List<ColumnField> getNormalColumns() {
		return normalColumns;
	}

	public void setNormalColumns(List<ColumnField> normalColumns) {
		this.normalColumns = normalColumns;
	}

	public List<ColumnField> getPrimaryColumns() {
		return primaryColumns;
	}

	public void setPrimaryColumns(List<ColumnField> primaryColumns) {
		this.primaryColumns = primaryColumns;
	}

	public ColumnField getPrimary() {
		return primary;
	}

	public void setPrimary(ColumnField primary) {
		this.primary = primary;
	}

	/**
	 * 分组普通列和主键列
	 */
	private void setNormalPrimaryColumns() {
		for (int i = 0; i < columnFields.size(); i++) {
			ColumnField columnField = columnFields.get(i);
			if ("PRI".equalsIgnoreCase(columnField.getKey())) {
				primaryColumns.add(columnField);
			} else {
				normalColumns.add(columnField);
			}
		}
		if (primaryColumns.isEmpty()) {
			System.err.println(tableName + "表没有主键");
		} else {
			primary = primaryColumns.get(0);
		}
	}
}
