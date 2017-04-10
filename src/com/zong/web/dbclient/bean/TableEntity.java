package com.zong.web.dbclient.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc 数据表结构模型，用于freemarker模板生成代码
 * @author zong
 * @date 2016年3月13日
 */
public class TableEntity {

	private String tableName;
	private String comment;
	private int totalResult;

	private List<ColumnField> primaries = new ArrayList<ColumnField>();
	private ColumnField primary;
	/**
	 * 属性和列对象集合，不包含主键
	 */
	private List<ColumnField> columnFields;
	/**
	 * bean包名称
	 */
	private String packageBean = "com.zong.web.bean";
	/**
	 * mapper包名称
	 */
	private String packageMapper = "com.zong.web.dao";
	/**
	 * service包名称
	 */
	private String packageService = "com.zong.web.service";
	/**
	 * controller包名称
	 */
	private String packageController = "com.zong.web.controller";
	/**
	 * jsp上层目录名xxx/，WEB-INF/jsp/[xxx/]objectName/objectName_list.jsp，默认为""
	 */
	private String packageJsp = "";
	/**
	 * 是否有表前缀，true生成类名将删除第一个下划之前的字符
	 */
	private boolean tablePrefix;
	/**
	 * 建表sql
	 */
	private String createSql;

	private Map<String, Object> root;
	private String objectName;
	private String className;

	public TableEntity() {

	}

	public TableEntity(String tableName) {
		this.tableName = tableName;
	}

	public TableEntity(String tableName, String packageBean, String packageMapper, String packageService,
			String packageController) {
		this.tableName = tableName;
		this.packageBean = packageBean;
		this.packageMapper = packageMapper;
		this.packageService = packageService;
		this.packageController = packageController;
	}

	/**
	 * 获取表主键列对象
	 */
	public ColumnField getPrimary() {
		return primary;
	}

	/**
	 * 返回所有列【不包含主键】拼接字符 username,password
	 */
	public String getColumNames() {
		StringBuffer columNames = new StringBuffer();
		for (int i = 0; i < columnFields.size(); i++) {
			ColumnField columnField = columnFields.get(i);
			columNames.append(columnField.getColumn());
			if (i != columnFields.size() - 1) {
				columNames.append(",");
			}
			columNames.append("\r\n");
		}
		return columNames.toString();
	}

	/**
	 * 返回所有列【不包含主键】赋值拼接字符 #{username,jdbcType=VARCHAR},#{password,jdbcType=VARCHAR}
	 */
	public String getColumValues() {
		StringBuffer columNames = new StringBuffer();
		for (int i = 0; i < columnFields.size(); i++) {
			ColumnField columnField = columnFields.get(i);
			columNames.append("#{" + columnField.getField() + ",jdbcType=" + columnField.getJdbcType() + "}");
			if (i != columnFields.size() - 1) {
				columNames.append(",");
			}
			columNames.append("\r\n");
		}
		return columNames.toString();
	}

	/**
	 * 返回所有列sql拼接字符 name "name",password "password"
	 */
	public String getSqlColumNamesValues() {
		StringBuffer sb = new StringBuffer();
		int count = 1;
		for (int i = 0; i < columnFields.size(); i++) {
			if (count++ % 6 == 0) {
				sb.append("\r    ");
			}
			sb.append(columnFields.get(i).getColumn() + " \"" + columnFields.get(i).getField() + "\",");
		}
		String result = sb.toString();
		result = result.substring(0, result.length() - 1);
		return result;
	}

	/**
	 * 将表名转为java格式的类名
	 */
	public String getClassName() {
		if(className==null||className.equals("")){
			return createClassName();
		}
		return className;
	}
	
	public String createClassName(){
		String tbName = tableName;
		if (tablePrefix && tableName.indexOf("_") > -1) {
			tbName = tableName.substring(tableName.indexOf("_") + 1);
		}
		String[] names = tbName.split("_");
		StringBuffer nameBuffer = new StringBuffer();
		for (int i = 0; i < names.length; i++) {
			String name = names[i].toLowerCase();
			name = name.substring(0, 1).toUpperCase() + name.substring(1);
			nameBuffer.append(name);
		}
		className = nameBuffer.toString();
		return className;
	}

	/**
	 * 将表名转为java格式的实体名，首字母小写
	 */
	public String getObjectName() {
		if(objectName==null||objectName.equals("")){
			return createObjectName();
		}
		return objectName;
	}
	
	public String createObjectName(){
		String tbName = tableName;
		if (tablePrefix && tableName.indexOf("_") > -1) {
			tbName = tableName.substring(tableName.indexOf("_") + 1);
		}
		String[] names = tbName.split("_");
		StringBuffer nameBuffer = new StringBuffer();
		for (int i = 0; i < names.length; i++) {
			String name = names[i].toLowerCase();
			if (i > 0) {
				name = name.substring(0, 1).toUpperCase() + name.substring(1);
			}
			nameBuffer.append(name);
		}
		objectName = nameBuffer.toString();
		return objectName;
	}

	/**
	 * 将表名转为java格式的实体名全大写
	 */
	public String getObjectNameUpper() {
		return getObjectName().toUpperCase();
	}

	/**
	 * 将表名转为java格式的实体名全小写
	 */
	public String getObjectNameLower() {
		return getObjectName().toLowerCase();
	}

	/**
	 * 根据字段类型判断需要导入的包
	 */
	public String getImportPackage() {
		StringBuffer importPackage = new StringBuffer();
		for (int i = 0; i < columnFields.size(); i++) {
			ColumnField columnField = columnFields.get(i);
			/*
			 * if (columnField.getType().equals("Date") && importPackage.indexOf("import java.util.*;") < 0) {
			 * importPackage.append("import java.util.*;\r\n"); } if (columnField.getType().equals("BigDecimal") &&
			 * importPackage.indexOf("import java.math.*;") < 0) { importPackage.append("import java.math.*;\r\n"); }
			 */
			if (columnField.getType().equals("Date") && importPackage.indexOf("import java.util.Date;") < 0) {
				importPackage.append("import java.util.Date;\r\n");
			}
			if (columnField.getType().equals("BigDecimal")
					&& importPackage.indexOf("import java.math.BigDecimal;") < 0) {
				importPackage.append("import java.math.BigDecimal;\r\n");
			}
		}
		return importPackage.toString();
	}

	/**
	 * 将带点的包名称转为斜杠/路径
	 * 
	 * @param packageName
	 */
	private static String getPackagePath(String packageName) {
		String path = "";
		String[] names = packageName.split("\\.");
		for (String name : names) {
			path += name + "/";
		}
		return path;
	}

	public List<ColumnField> getColumnFields() {
		return columnFields;
	}

	/**
	 * 设置属性列同时分离主键到属性primary
	 * 
	 * @throws Exception
	 */
	public void setColumnFields(List<ColumnField> columnFields) {
		this.columnFields = columnFields;
		for (int i = 0; i < columnFields.size(); i++) {
			ColumnField columnField = columnFields.get(i);
			if ("PRI".equalsIgnoreCase(columnField.getKey())) {
				// primary = columnFields.remove(i);
				// break;
				primaries.add(columnField);
			}
		}
		if (primaries.isEmpty()) {
			System.err.println(tableName + "表没有主键");
		}else{
			primary = primaries.get(0);
		}
		columnFields.removeAll(primaries);
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getPackageBeanPath() {
		return getPackagePath(packageBean);
	}

	public String getPackageMapperPath() {
		return getPackagePath(packageMapper);
	}

	public String getPackageServicePath() {
		return getPackagePath(packageService);
	}

	public String getPackageControllerPath() {
		return getPackagePath(packageController);
	}

	public String getPackageBean() {
		return packageBean;
	}

	public void setPackageBean(String packageBean) {
		this.packageBean = packageBean;
	}

	public String getPackageMapper() {
		return packageMapper;
	}

	public void setPackageMapper(String packageMapper) {
		this.packageMapper = packageMapper;
	}

	public String getPackageService() {
		return packageService;
	}

	public void setPackageService(String packageService) {
		this.packageService = packageService;
	}

	public String getPackageController() {
		return packageController;
	}

	public void setPackageController(String packageController) {
		this.packageController = packageController;
	}

	public void setPrimary(ColumnField primary) {
		this.primary = primary;
	}

	public boolean isTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(boolean tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public String getPackageJsp() {
		return packageJsp;
	}

	public void setPackageJsp(String packageJsp) {
		this.packageJsp = packageJsp;
	}

	public String getCreateSql() {
		return createSql;
	}

	public void setCreateSql(String createSql) {
		this.createSql = createSql;
	}

	public Map<String, Object> getRoot() {
		root = new HashMap<String, Object>(); // 创建数据模型
		root.put("tableEntity", this);
		root.put("columnFields", getColumnFields());
		root.put("className", getClassName()); // 类名
		root.put("objectName", getObjectName()); // 实体名
		root.put("packageBean", getPackageBean()); // 包名
		root.put("packageMapper", getPackageMapper());
		root.put("packageService", getPackageService());
		root.put("packageController", getPackageController());
		root.put("packageJsp", getPackageJsp());
		root.put("nowDate", new Date()); // 当前日期
		return root;
	}

	public void setRoot(Map<String, Object> root) {
		this.root = root;
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

	public List<ColumnField> getPrimaries() {
		return primaries;
	}

	public void setPrimaries(List<ColumnField> primaries) {
		this.primaries = primaries;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
