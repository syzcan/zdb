package com.zong.zdb.service;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.zong.zdb.bean.ColumnField;
import com.zong.zdb.bean.Table;
import com.zong.zdb.util.ZDBConfig;

/**
 * @desc 表数据模板集合
 * @author zong
 * @date 2017年4月14日
 */
@SuppressWarnings("rawtypes")
public class TemplateRoot extends HashMap implements Map, Serializable {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public TemplateRoot put(Object key, Object value) {
		super.put(key, value);
		return this;
	}

	public String getString(Object key) {
		Object object = get(key);
		if (object != null) {
			return object.toString();
		}
		return null;
	}

	public static TemplateRoot createTemplateRoot(Table table) {
		String packageName = (String) ZDBConfig.configData.get("packageName");
		return createTemplateRoot(table, createObjectName(table.getTableName()), createClassName(table.getTableName()),
				packageName);
	}

	public static TemplateRoot createTemplateRoot(Table table, String objectName, String className,
			String packageName) {
		if (packageName == null || packageName.equals("")) {
			packageName = "com.zong.web";
		}
		/**
		 * jsp上层目录名xxx/，WEB-INF/jsp/[xxx/]objectName/objectName_list.jsp，默认为""
		 */
		String packageJsp = (String) ZDBConfig.configData.get("packageJsp");
		TemplateRoot root = new TemplateRoot();
		root.put("packageBean", packageName + ".bean");
		root.put("packageMapper", packageName + ".dao");
		root.put("packageService", packageName + ".service");
		root.put("packageController", packageName + ".controller");
		root.put("packageJsp", packageJsp == null ? "" : packageJsp);
		root.put("table", table);
		root.put("objectName", objectName);
		root.put("className", className);
		root.put("importPackage", getImportPackage(table));
		root.put("nowDate", new Date());
		return root;
	}

	public String getPackageBeanPath() {
		return getPackagePath(getString(ZDBConfig.PACKAGE_BEAN));
	}

	public String getPackageMapperPath() {
		return getPackagePath(getString(ZDBConfig.PACKAGE_MAPPER));
	}

	public String getPackageServicePath() {
		return getPackagePath(getString(ZDBConfig.PACKAGE_SERVICE));
	}

	public String getPackageControllerPath() {
		return getPackagePath(getString(ZDBConfig.PACKAGE_CONTROLLER));
	}

	/**
	 * 将带点的包名称转为斜杠/路径
	 * 
	 * @param packageName
	 */
	public static String getPackagePath(String packageName) {
		String path = "";
		String[] names = packageName.split("\\.");
		for (String name : names) {
			path += name + "/";
		}
		return path;
	}

	/**
	 * 根据字段类型判断需要导入的包
	 */
	public static String getImportPackage(Table table) {
		StringBuffer importPackage = new StringBuffer();
		for (int i = 0; i < table.getColumnFields().size(); i++) {
			ColumnField columnField = table.getColumnFields().get(i);
			if (columnField.getJavaType().equals("Date") && importPackage.indexOf("import java.util.Date;") < 0) {
				importPackage.append("import java.util.Date;\r\n");
			}
			if (columnField.getJavaType().equals("BigDecimal")
					&& importPackage.indexOf("import java.math.BigDecimal;") < 0) {
				importPackage.append("import java.math.BigDecimal;\r\n");
			}
		}
		return importPackage.toString();
	}

	private static String createClassName(String tableName) {
		String[] names = tableName.split("_");
		StringBuffer nameBuffer = new StringBuffer();
		for (int i = 0; i < names.length; i++) {
			String name = names[i].toLowerCase();
			name = name.substring(0, 1).toUpperCase() + name.substring(1);
			nameBuffer.append(name);
		}
		return nameBuffer.toString();
	}

	private static String createObjectName(String tableName) {
		String[] names = tableName.split("_");
		StringBuffer nameBuffer = new StringBuffer();
		for (int i = 0; i < names.length; i++) {
			String name = names[i].toLowerCase();
			if (i > 0) {
				name = name.substring(0, 1).toUpperCase() + name.substring(1);
			}
			nameBuffer.append(name);
		}
		return nameBuffer.toString();
	}
}
