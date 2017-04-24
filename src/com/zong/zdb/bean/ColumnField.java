package com.zong.zdb.bean;

/**
 * @desc 实体属性和数据表字段映射
 * @author zong
 * @date 2016年3月13日
 */
public class ColumnField {
	/**
	 * 属性名称，由列名column转换
	 */
	private String field;
	/**
	 * 字段类型对应的java类型，由传入字段类型type进行转换
	 */
	private String javaType;
	/**
	 * jdbcType，由传入字段类型type进行转换
	 */
	private String jdbcType;
	/**
	 * 字段列名
	 */
	private String column;
	/**
	 * mysql字段定义information_schema.COLUMNS才有这个字段，保存的是varchar(255)这种包含括号
	 */
	private String columnType;
	/**
	 * 字段定义类型，varchar、int、varchar2、date、number..
	 */
	private String dataType;
	/**
	 * 数据最多长度
	 */
	private Long dataLength;
	/**
	 * 数据精确度
	 */
	private Integer dataPrecision;
	/**
	 * number类型精度，小数点位数
	 */
	private Integer dataScale;
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

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public String getJdbcType() {
		return jdbcType;
	}

	public void setJdbcType(String jdbcType) {
		this.jdbcType = jdbcType;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Long getDataLength() {
		return dataLength;
	}

	public void setDataLength(Long dataLength) {
		this.dataLength = dataLength;
	}

	public Integer getDataPrecision() {
		return dataPrecision;
	}

	public void setDataPrecision(Integer dataPrecision) {
		this.dataPrecision = dataPrecision;
	}

	public Integer getDataScale() {
		return dataScale;
	}

	public void setDataScale(Integer dataScale) {
		this.dataScale = dataScale;
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

	/**
	 * 获取首字母大写的属性名
	 */
	public String getFieldUpper() {
		return getField().substring(0, 1).toUpperCase() + getField().substring(1);
	}

	/**
	 * 数据库字段类型匹配为实体属性类型 jdbcType、javaType
	 */
	public void transColumnType(String dataType) {
		String javaType = dataType.toLowerCase();
		String jdbcType = "";
		if (javaType.matches(".*((char)|(varchar)|(text)|(mediumtext)|(longtext)).*")) {
			javaType = "String";
			jdbcType = "VARCHAR";
		} else if (javaType.matches(".*((int)|(bigint)|(integer)).*")) {
			javaType = "Integer";
			jdbcType = "INTEGER";
		} else if (javaType.matches(".*(bit).*")) {
			javaType = "Boolean";
			jdbcType = "BIT";
		} else if (javaType.matches(".*(float).*")) {
			javaType = "Float";
			jdbcType = "DECIMAL";
		} else if (javaType.matches(".*(double).*")) {
			javaType = "Double";
			jdbcType = "DECIMAL";
		} else if (javaType.matches(".*(decimal).*")) {
			javaType = "BigDecimal";
			jdbcType = "DECIMAL";
		} else if (javaType.matches(".*((date)|(datetime)|(timestamp)).*")) {
			javaType = "Date";
			jdbcType = "TIMESTAMP";
		} else if (javaType.matches(".*(number).*")) {
			if (dataScale == 0) {
				javaType = "Integer";
				jdbcType = "INTEGER";
			} else {
				javaType = "BigDecimal";
				jdbcType = "DECIMAL";
			}
		} else if (javaType.matches(".*(clob).*")) {
			javaType = "String";
			jdbcType = "CLOB";
		}
		this.javaType = javaType;
		this.jdbcType = jdbcType;
	}

	/**
	 * 字段名转换为属性名，首字母小写，下划线后一个单词大写开头，然后取消下划线
	 */
	public void transColumnToField(String column) {
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
	}
}
