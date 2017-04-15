package com.zong.zdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.zong.zdb.bean.ColumnField;
import com.zong.zdb.bean.Table;
import com.zong.zdb.util.Page;
import com.zong.zdb.util.PageData;

/**
 * @desc
 * @author zong
 * @date 2016年3月23日
 */
public class OracleCodeDao implements IJdbcDao {

	public Connection conn;
	public PreparedStatement ps;
	public ResultSet rs;
	public Statement st;
	public String username;

	public OracleCodeDao(String username, Connection conn) {
		this.username = username;
		this.conn = conn;
	}

	public OracleCodeDao() {

	}

	/**
	 * 查询某个表的所有字段
	 */
	public List<ColumnField> showTableColumns(String tableName) {
		List<ColumnField> list = new ArrayList<ColumnField>();
		try {
			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象，st属局部变量
			String sql = "select * from DBA_TAB_COLUMNS where Table_Name='" + tableName + "' and lower(OWNER)=lower('"
					+ username + "') ORDER BY COLUMN_ID";
			ResultSet rs = st.executeQuery(sql); // 执行sql查询语句，返回查询数据的结果集

			while (rs.next()) {
				// 根据字段名获取相应的值
				String column = rs.getString("COLUMN_NAME");
				String columnType = rs.getString("DATA_TYPE");
				Long dataLength = rs.getLong("DATA_LENGTH");
				Integer dataPrecision = rs.getInt("DATA_PRECISION");
				Integer dataScale = rs.getInt("DATA_SCALE");
				String defaultValue = rs.getString("DATA_DEFAULT");
				String canNull = rs.getString("NULLABLE").equals("Y") ? "YES" : "NO";
				ColumnField columnField = new ColumnField();
				columnField.setColumn(column);
				columnField.setColumnType(columnType);
				columnField.setDataLength(dataLength);
				columnField.setDataPrecision(dataPrecision);
				columnField.setDataScale(dataScale);
				columnField.setDefaultValue(defaultValue);
				columnField.setCanNull(canNull);
				// 转换字段和类型到java属性名和属性类型
				columnField.transColumnToField(column);
				columnField.transColumnType(columnType);
				list.add(columnField);
			}
			// 查询字段备注
			sql = "select * from user_col_comments where Table_Name='" + tableName + "'";
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String column = rs.getString("COLUMN_NAME");
				String comment = rs.getString("COMMENTS");
				for (ColumnField columnField : list) {
					if (columnField.getColumn().equals(column)) {
						columnField.setRemark(comment);
					}
				}
			}
			// 查询表主键
			sql = "select cu.* from user_cons_columns cu, user_constraints au where cu.constraint_name = au.constraint_name and au.constraint_type = 'P' and au.table_name = '"
					+ tableName + "' order by position";
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String column = rs.getString("COLUMN_NAME");
				for (ColumnField columnField : list) {
					if (columnField.getColumn().equals(column)) {
						columnField.setKey("PRI");
					}
				}
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
			String sql = "select * from user_tab_comments ORDER BY table_name";
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
				// 根据字段名获取相应的值
				String tableName = rs.getString("TABLE_NAME");
				String comment = rs.getString("COMMENTS");
				Table table = new Table();
				table.setTableName(tableName);
				table.setComment(comment);
				list.add(table);
			}
			sql = "select table_name,num_rows from user_tables ORDER BY num_rows desc";
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				int numRows = rs.getInt("NUM_ROWS");
				for (Table table : list) {
					if (table.getTableName().equals(tableName)) {
						table.setTotalResult(numRows);
					}
				}
			}
			// conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			System.out.println("查询数据失败");
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public Table showTable(String tableName) {
		Table table = new Table();
		try {
			String sql = "select * from user_tab_comments where TABLE_NAME = '" + tableName + "'";
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
				// 根据字段名获取相应的值
				String comment = rs.getString("COMMENTS");
				table.setTableName(tableName);
				table.setComment(comment);
				table.setColumnFields(showTableColumns(tableName));
			}
			sql = "select table_name,num_rows from user_tables where TABLE_NAME = '" + tableName
					+ "' ORDER BY num_rows desc";
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String TABLE_NAME = rs.getString("TABLE_NAME");
				int numRows = rs.getInt("NUM_ROWS");
				if (table.getTableName().equals(TABLE_NAME)) {
					table.setTotalResult(numRows);
				}
			}
			// conn.close();
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
			int offset = page.getCurrentResult() + 1;// oracle用rownum分页从1开始
			String pageSql = "select * from (select u.*, rownum r from (" + sql + ") u where rownum<"
					+ (offset + page.getShowCount()) + ") where r>=" + offset;
			st = (Statement) conn.createStatement();
			ResultSet rs = st.executeQuery(pageSql);
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
					ResultSetMetaData metaData = rs.getMetaData();
					pd.put(metaData.getColumnName(i), rs.getObject(metaData.getColumnName(i)));
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
