package com.zong.zdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.zong.zdb.bean.ColumnField;
import com.zong.zdb.bean.Table;
import com.zong.zdb.util.Page;
import com.zong.zdb.util.PageData;

public class MysqlCodeDao implements IJdbcDao {
	public Connection conn;
	public PreparedStatement ps;
	public ResultSet rs;
	public Statement st;
	public String database;

	public MysqlCodeDao(String database, Connection conn) {
		this.database = database;
		this.conn = conn;
	}

	/**
	 * 查询某个表的所有字段
	 */
	public List<ColumnField> showTableColumns(String tableName) {
		List<ColumnField> list = new ArrayList<ColumnField>();
		try {
			String sql = "select * from information_schema.columns where table_schema='" + database
					+ "' and table_name='" + tableName + "'";
			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象，st属局部变量
			ResultSet rs = st.executeQuery(sql); // 执行sql查询语句，返回查询数据的结果集

			while (rs.next()) { // 判断是否还有下一个数据
				// 根据字段名获取相应的值
				String column = rs.getString("COLUMN_NAME");
				String comment = rs.getString("COLUMN_COMMENT");
				String columnType = rs.getString("DATA_TYPE");
				String columnKey = rs.getString("COLUMN_KEY");
				String canNull = rs.getString("IS_NULLABLE");
				Long dataLength = rs.getLong("CHARACTER_MAXIMUM_LENGTH");
				Integer dataPrecision = rs.getInt("NUMERIC_PRECISION");
				Integer dataScale = rs.getInt("NUMERIC_SCALE");
				String defaultValue = rs.getString("COLUMN_DEFAULT");
				ColumnField columnField = new ColumnField();
				columnField.setColumn(column);
				columnField.setColumnType(columnType);
				columnField.setKey(columnKey);
				columnField.setRemark(comment);
				columnField.setCanNull(canNull);
				columnField.setDataLength(dataLength);
				columnField.setDataPrecision(dataPrecision);
				columnField.setDataScale(dataScale);
				columnField.setDefaultValue(defaultValue);
				// 转换字段和类型到java属性名和属性类型
				columnField.transColumnToField(column);
				columnField.transColumnType(columnType);
				list.add(columnField);
			}
			// conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			System.out.println("查询数据失败");
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 获取当前用户所有表名
	 */
	public List<Table> showTables() {
		List<Table> list = new ArrayList<Table>();
		try {
			String sql = "select table_name,table_comment,table_rows from information_schema.tables where table_schema='"
					+ database + "' and table_type='BASE TABLE'";
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
				String tableName = rs.getString("table_name");
				String comment = rs.getString("table_comment");
				int tableRows = rs.getInt("table_rows");
				Table table = new Table();
				table.setTableName(tableName);
				table.setComment(dealComment(comment));
				table.setTotalResult(tableRows);
				list.add(table);
			}
			// conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			System.out.println("查询数据失败");
			e.printStackTrace();
		}
		return list;
	}

	private String dealComment(String comment) {
		if (comment.indexOf("InnoDB") >= 0) {
			if (comment.indexOf(";") >= 0) {
				comment = comment.substring(0, comment.lastIndexOf(";"));
			} else {
				comment = "";
			}
		}
		return comment;
	}

	@Override
	public Table showTable(String tableName) {
		Table table = new Table();
		try {
			String sql = "select table_name,table_comment,table_rows from information_schema.tables where table_schema='"
					+ database + "' and table_type='BASE TABLE' and table_name='" + tableName + "'";
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
				String table_comment = rs.getString("table_comment");
				int table_rows = rs.getInt("table_rows");
				table.setTableName(tableName);
				table.setComment(dealComment(table_comment));
				table.setTotalResult(table_rows);
				table.setColumnFields(showTableColumns(tableName));
			}
			// conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			System.out.println("查询数据失败");
			e.printStackTrace();
		}
		return table;
	}

	public List<PageData> showTableDatas(Page page) {
		List<PageData> list = new ArrayList<PageData>();
		try {
			String tableName = page.getTable();
			String sql = "select * from " + tableName;
			page.setTotalResult(count(sql));// 统计总数
			// 分页，条件筛选
			if (page.getPd().get("orderColumn") != null && !page.getPd().get("orderColumn").equals("")) {
				sql += " order by " + page.getPd().get("orderColumn") + " " + page.getPd().get("orderType");
			}
			sql += " limit " + page.getCurrentResult() + "," + page.getShowCount();

			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);
			List<ColumnField> fields = showTableColumns(tableName);
			while (rs.next()) {
				PageData pd = new PageData();
				for (ColumnField columnField : fields) {
					pd.put(columnField.getColumn(), rs.getString(columnField.getColumn()));
				}
				list.add(pd);
			}
			// conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			System.out.println("查询数据失败");
			e.printStackTrace();
		}
		page.setDatas(list);
		return list;
	}

	private int count(String sql) throws SQLException {
		int count = 0;
		String countSql = "select count(*) " + sql.substring(sql.indexOf("from"));
		st = (Statement) conn.createStatement();
		ResultSet rs = st.executeQuery(countSql);
		while (rs.next()) {
			count = rs.getInt(1);
		}
		return count;
	}

	@Override
	public List<PageData> showSqlDatas(String sql) {
		List<PageData> list = new ArrayList<PageData>();
		try {
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
				PageData pd = new PageData();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					pd.put(rs.getMetaData().getColumnName(i), rs.getString(i));
				}
				list.add(pd);
			}
		} catch (SQLException e) {
			System.out.println("查询数据失败");
			e.printStackTrace();
		}
		return list;
	}
}
