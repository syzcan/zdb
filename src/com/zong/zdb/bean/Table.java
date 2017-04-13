package com.zong.zdb.bean;

import java.util.List;

public class Table {
	private String tableName;
	private String comment;
	private int totalResult;

	private List<ColumnField> columnFields;

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
	}
}
