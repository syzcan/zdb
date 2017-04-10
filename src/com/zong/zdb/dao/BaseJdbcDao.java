package com.zong.zdb.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.zong.zdb.util.Properties;

public class BaseJdbcDao {
	protected static String driverClassName;
	protected static String url;
	protected static String username;
	protected static String password;
	protected static String database;
	protected static String connKey;;

	// 数据库连接池
	public static Map<String, Connection> conns = new HashMap<String, Connection>();
	public static Connection conn;
	public static PreparedStatement ps;
	public static ResultSet rs;
	public static Statement st;

	public BaseJdbcDao(Properties props) {
		driverClassName = props.getProperty("jdbc.driverClassName");
		url = props.getProperty("jdbc.url");
		username = props.getProperty("jdbc.username");
		password = props.getProperty("jdbc.password");
		String[] ss = url.split("\\?")[0].split("/");
		database = ss[ss.length - 1];
		connKey = driverClassName + url + username + password;
	}

	public BaseJdbcDao() {

	}

	// 连接数据库的方法
	public static Connection getConnection() {
		try {
			// 初始化驱动包
			Class.forName(driverClassName);
			if (conns.get(connKey) == null) {
				// 根据数据库连接字符，名称，密码给conn赋值
				conn = DriverManager.getConnection(url, username, password);
				conns.put(connKey, conn);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println(conns.get(connKey));
		return conns.get(connKey);
	}
}
