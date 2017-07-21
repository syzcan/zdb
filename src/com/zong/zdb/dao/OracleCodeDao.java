package com.zong.zdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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
	private static final Logger logger = Logger.getLogger(MysqlCodeDao.class);
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public Connection conn;
	public String username;

	public OracleCodeDao(String username, Connection conn) {
		this.username = username;
		this.conn = conn;
	}

	/**
	 * 关闭资源
	 */
	private void closeStatement(ResultSet rs, Statement st, PreparedStatement pst) {
		if (st != null) {
			try {
				st.close();
			} catch (Exception e) {
			}
		}
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
			}
		}
		if (pst != null) {
			try {
				pst.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 查询某个表的所有字段
	 */
	public List<ColumnField> showTableColumns(String tableName) {
		int rowCount = 0;
		List<ColumnField> list = new ArrayList<ColumnField>();
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象，st属局部变量
			String cols = "COLUMN_NAME,DATA_TYPE,DATA_LENGTH,DATA_PRECISION,DATA_SCALE,DATA_DEFAULT,NULLABLE";
			String sql = "select " + cols + " from sys.all_tab_columns where table_name='" + tableName
					+ "' and lower(OWNER)=lower('" + username + "') order by column_id";
//			String sql = "select " + cols + " from dba_tab_columns where table_name='" + tableName
//					+ "' and lower(OWNER)=lower('" + username + "') order by column_id";
			rs = st.executeQuery(sql); // 执行sql查询语句，返回查询数据的结果集

			while (rs.next()) {
				// 根据字段名获取相应的值
				String column = rs.getString("COLUMN_NAME");
				String dataType = rs.getString("DATA_TYPE");
				Long dataLength = rs.getLong("DATA_LENGTH");
				Integer dataPrecision = rs.getInt("DATA_PRECISION");
				Integer dataScale = rs.getInt("DATA_SCALE");
				String defaultValue = rs.getString("DATA_DEFAULT");
				String canNull = rs.getString("NULLABLE").equals("Y") ? "YES" : "NO";
				ColumnField columnField = new ColumnField();
				columnField.setColumn(column);
				columnField.setDataType(dataType);
				columnField.setDataLength(dataLength);
				columnField.setDataPrecision(dataPrecision);
				columnField.setDataScale(dataScale);
				columnField.setDefaultValue(defaultValue);
				columnField.setCanNull(canNull);
				// 转换字段和类型到java属性名和属性类型
				columnField.transColumnToField(column);
				columnField.transColumnType(dataType);
				list.add(columnField);
				rowCount++;
			}
			rs.close();
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("<==      Total: " + rowCount);
			rowCount = 0;
			// 查询字段备注
			sql = "select COLUMN_NAME,COMMENTS from user_col_comments where table_name='" + tableName + "'";
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String column = rs.getString("COLUMN_NAME");
				String comment = rs.getString("COMMENTS");
				for (ColumnField columnField : list) {
					if (columnField.getColumn().equals(column)) {
						columnField.setRemark(comment);
					}
				}
				rowCount++;
			}
			rs.close();
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("<==      Total: " + rowCount);
			rowCount = 0;
			// 查询表主键
			sql = "select cu.COLUMN_NAME from user_cons_columns cu, user_constraints au where cu.constraint_name = au.constraint_name and au.constraint_type = 'P' and au.table_name = '"
					+ tableName + "' order by position";
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String column = rs.getString("COLUMN_NAME");
				for (ColumnField columnField : list) {
					if (columnField.getColumn().equals(column)) {
						columnField.setKey("PRI");
					}
				}
				rowCount++;
			}
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("<==      Total: " + rowCount);
			// conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			logger.error("查询数据失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
		}
		return list;
	}

	/**
	 * 获取当前用户所有表名
	 */
	public List<Table> showTables() {
		int rowCount = 0;
		List<Table> list = new ArrayList<Table>();
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			String sql = "select * from user_tab_comments order by table_name";
			st = (Statement) conn.createStatement();
			rs = st.executeQuery(sql);

			while (rs.next()) {
				// 根据字段名获取相应的值
				String tableName = rs.getString("TABLE_NAME");
				String comment = rs.getString("COMMENTS");
				Table table = new Table();
				table.setTableName(tableName);
				table.setComment(comment);
				list.add(table);
				rowCount++;
			}
			rs.close();
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("<==      Total: " + rowCount);
			rowCount = 0;
			sql = "select TABLE_NAME,NUM_ROWS from user_tables order by num_rows desc";
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				int numRows = rs.getInt("NUM_ROWS");
				for (Table table : list) {
					if (table.getTableName().equals(tableName)) {
						table.setTotalResult(numRows);
					}
				}
				rowCount++;
			}
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("<==      Total: " + rowCount);
			// conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			logger.error("查询数据失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
		}
		return list;
	}

	@Override
	public Table showTable(String tableName) {
		int rowCount = 0;
		Table table = null;
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			String sql = "select * from user_tab_comments where table_name = '" + tableName + "'";
			st = (Statement) conn.createStatement();
			rs = st.executeQuery(sql);

			while (rs.next()) {
				// 根据字段名获取相应的值
				String comment = rs.getString("COMMENTS");
				table = new Table();
				table.setTableName(tableName);
				table.setComment(comment);
				table.setColumnFields(showTableColumns(tableName));
				rowCount++;
			}
			rs.close();
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("<==      Total: " + rowCount);
			rowCount = 0;
			sql = "select TABLE_NAME,NUM_ROWS from user_tables where table_name = '" + tableName
					+ "' order by NUM_ROWS desc";
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String TABLE_NAME = rs.getString("TABLE_NAME");
				int numRows = rs.getInt("NUM_ROWS");
				if (table.getTableName().equals(TABLE_NAME)) {
					table.setTotalResult(numRows);
				}
				rowCount++;
			}
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("<==      Total: " + rowCount);
			// conn.close();
		} catch (SQLException e) {
			logger.error("查询数据失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
		}
		return table;
	}

	public List<PageData> showTableDatas(Page page) {
		int rowCount = 0;
		List<PageData> list = new ArrayList<PageData>();
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
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
			rs = st.executeQuery(pageSql);
			List<ColumnField> fields = showTableColumns(tableName);
			while (rs.next()) {
				PageData pd = new PageData();
				for (ColumnField columnField : fields) {
					String value = "";
					Object obj = rs.getObject(columnField.getColumn());
					if (obj instanceof Timestamp) {
						value = dateFormat.format(obj);
					} else {
						value = obj == null ? "" : obj.toString();
					}
					pd.put(columnField.getColumn(), value);
				}
				list.add(pd);
				rowCount++;
			}
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("<==      Total: " + rowCount);
			// conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			logger.error("查询数据失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
		}
		page.setDatas(list);
		return list;
	}

	private int count(String sql) throws SQLException {
		int count = 0;
		ResultSet rs = null;
		Statement st = null;
		try {
			String countSql = "select count(*) " + sql.substring(sql.indexOf("from"));
			st = (Statement) conn.createStatement();
			rs = st.executeQuery(countSql);
			while (rs.next()) {
				count = rs.getInt(1);
			}
			logger.debug("==>  Preparing: " + sql);
		} catch (SQLException e) {
			logger.error("查询数据失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, null);
		}
		return count;
	}

	@Override
	public List<PageData> showSqlDatas(String sql) {
		int rowCount = 0;
		List<PageData> list = new ArrayList<PageData>();
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			st = (Statement) conn.createStatement();
			rs = st.executeQuery(sql);

			while (rs.next()) {
				PageData pd = new PageData();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					String value = "";
					Object obj = rs.getObject(rs.getMetaData().getColumnName(i));
					if (obj instanceof Timestamp) {
						value = dateFormat.format(obj);
					} else {
						value = obj == null ? "" : obj.toString();
					}
					pd.put(rs.getMetaData().getColumnName(i), value);
				}
				list.add(pd);
				rowCount++;
			}
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("<==      Total: " + rowCount);
		} catch (SQLException e) {
			logger.error("查询数据失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
		}
		return list;
	}

	@Override
	public void insert(String tableName, PageData data) {
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			// 拼接预编译sql
			String sql = "insert into " + tableName + "(";
			for (Object column : data.keySet()) {
				sql += column + ",";
			}
			sql = sql.replaceAll(",$", "");
			sql += ") values(";
			for (int i = 0; i < data.keySet().size(); i++) {
				sql += "?,";
			}
			sql = sql.replaceAll(",$", "");
			sql += ")";
			pst = conn.prepareStatement(sql);
			StringBuffer params = new StringBuffer();
			int index = 1;
			// 参数赋值
			for (Object column : data.keySet()) {
				Object value = data.get(column);
				if (value != null) {
					String valueType = value.getClass().getName();
					if (valueType == "java.util.Date") {
						value = dateFormat.format(value);
					}
					pst.setString(index++, value.toString());
					// 拼接参数记录日志
					params.append(value + "(" + valueType + "),");
				} else {
					pst.setString(index++, null);
					// 拼接参数记录日志
					params.append("null,");
				}
			}
			int rowCount = pst.executeUpdate();
			// sql日志
			logger.debug("==>   Preparing: " + sql);
			logger.debug("==>  Parameters: " + params.toString().replaceAll(",$", ""));
			logger.debug("<==       Total: " + rowCount);
		} catch (SQLException e) {
			logger.error("插入数据失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
		}
	}

	@Override
	public void update(String tableName, PageData data, PageData idPd) {
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			// 拼接预编译sql
			String sql = "update " + tableName + " set ";
			for (Object column : data.keySet()) {
				sql += column + "=?,";
			}
			sql = sql.replaceAll(",$", "");
			sql += " where 1=1 ";
			for (Object column : idPd.keySet()) {
				Object value = idPd.get(column);
				if (value != null && value.getClass().getName() == "java.util.Date") {
					value = dateFormat.format(value);
				}
				sql += " and " + column + "='" + value + "',";
			}
			sql = sql.replaceAll(",$", "");
			pst = conn.prepareStatement(sql);
			StringBuffer params = new StringBuffer();
			int index = 1;
			// 参数赋值
			for (Object column : data.keySet()) {
				Object value = data.get(column);
				if (value != null) {
					String valueType = value.getClass().getName();
					if (valueType == "java.util.Date") {
						value = dateFormat.format(value);
					}
					pst.setString(index++, value.toString());
					// 拼接参数记录日志
					params.append(value + "(" + valueType + "),");
				} else {
					pst.setString(index++, null);
					// 拼接参数记录日志
					params.append("null,");
				}
			}
			int rowCount = pst.executeUpdate();
			// sql日志
			logger.debug("==>   Preparing: " + sql);
			logger.debug("==>  Parameters: " + params.toString().replaceAll(",$", ""));
			logger.debug("<==       Total: " + rowCount);
		} catch (SQLException e) {
			logger.error("更新数据失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
		}
	}

	@Override
	public boolean createTable(PageData tableData) {
		// TODO Auto-generated method stub
		logger.warn("尚未开发");
		return false;
	}

	@Override
	public boolean alterTable(PageData tableData) {
		// TODO Auto-generated method stub
		logger.warn("尚未开发");
		return false;
	}
}
