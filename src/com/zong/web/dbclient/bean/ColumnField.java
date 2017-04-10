package com.zong.web.dbclient.bean;

/**
 * @desc 实体属性和数据表字段映射
 * @author zong
 * @date 2016年3月13日
 */
public class ColumnField {
	/**
	 * 字段列名
	 */
	private String column;
	/**
	 * 属性名称
	 */
	private String field;
	/**
	 * 字段类型对应的java类型，由传入字段类型type进行转换
	 */
	private String type;
	/**
	 * 字段定义类型
	 */
	private String columnType;
	/**
	 * 数据最多长度
	 */
	private Integer dataLength;
	/**
	 * 数据精确度
	 */
	private Integer dataPrecision;
	/**
	 * number类型精度，小数点位数
	 */
	private Integer dataScale;
	/**
	 * jdbcType，由传入字段类型type进行转换
	 */
	private String jdbcType;
	/**
	 * 是否可以为空YES/NO
	 */
	private String canNull;
	/**
	 * 是否主键PRI/MUL，为主键的时候key=PRI
	 */
	private String key;
	/**
	 * 默认值
	 */
	private String defaultValue;
	/**
	 * mysql自增主键extra=auto_increment
	 */
	private String extra;
	/**
	 * 字段注释
	 */
	private String remark;

	public String getColumn() {
		return column;
	}

	/**
	 * 字段名转换为属性名，首字母小写，下划线后一个单词大写开头，然后取消下划线
	 */
	public void setColumn(String column) {
		String[] names = column.split("_");
		StringBuffer nameBuffer = new StringBuffer();
		for (int i = 0; i < names.length; i++) {
			String name = names[i].toLowerCase();
			if (i != 0) {
				name = name.substring(0, 1).toUpperCase() + name.substring(1);
			}
			nameBuffer.append(name);
		}
		this.field = nameBuffer.toString();
		this.column = column;
	}

	public String getField() {
		return field;
	}

	/**
	 * 获取首字母大写的属性名
	 */
	public String getFieldUpper() {
		return getField().substring(0, 1).toUpperCase() + getField().substring(1);
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getType() {
		return type;
	}

	/**
	 * 数据库字段类型匹配为实体属性类型、jdbcType
	 */
	public void setType(String type) {
		type = type.toLowerCase();
		if (type.matches(".*((char)|(varchar)|(text)).*")) {
			type = "String";
			jdbcType = "VARCHAR";
		} else if (type.matches(".*((int)|(bigint)|(integer)).*")) {
			type = "Integer";
			jdbcType = "INTEGER";
		} else if (type.matches(".*(bit).*")) {
			type = "Boolean";
			jdbcType = "BIT";
		} else if (type.matches(".*(float).*")) {
			type = "Float";
			jdbcType = "DECIMAL";
		} else if (type.matches(".*(double).*")) {
			type = "Double";
			jdbcType = "DECIMAL";
		} else if (type.matches(".*(decimal).*")) {
			type = "BigDecimal";
			jdbcType = "DECIMAL";
		} else if (type.matches(".*((date)|(datetime)|(timestamp)).*")) {
			type = "Date";
			jdbcType = "TIMESTAMP";
		} else if (type.matches(".*(number).*")) {
			if (dataScale == 0) {
				type = "Integer";
				jdbcType = "INTEGER";
			} else {
				type = "BigDecimal";
				jdbcType = "DECIMAL";
			}
		} else if (type.matches(".*(clob).*")) {
			type = "String";
			jdbcType = "CLOB";
		}
		this.type = type;
	}

	public String getCanNull() {
		return canNull;
	}

	public void setCanNull(String canNull) {
		this.canNull = canNull;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String toString() {
		return getType() + " " + getField();
	}

	public String getJdbcType() {
		return jdbcType;
	}

	public void setJdbcType(String jdbcType) {
		this.jdbcType = jdbcType;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public Integer getDataScale() {
		return dataScale;
	}

	public void setDataScale(Integer dataScale) {
		this.dataScale = dataScale;
	}

	public Integer getDataLength() {
		return dataLength;
	}

	public void setDataLength(Integer dataLength) {
		this.dataLength = dataLength;
	}

	public Integer getDataPrecision() {
		return dataPrecision;
	}

	public void setDataPrecision(Integer dataPrecision) {
		this.dataPrecision = dataPrecision;
	}
}
