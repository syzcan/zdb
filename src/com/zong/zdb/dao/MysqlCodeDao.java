package com.zong.zdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.zong.zdb.bean.ColumnField;
import com.zong.zdb.bean.Table;
import com.zong.zdb.util.Page;
import com.zong.zdb.util.PageData;

public class MysqlCodeDao implements IJdbcDao {
	private static final Logger logger = Logger.getLogger(MysqlCodeDao.class);
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public Connection conn;
	public String database;

	public MysqlCodeDao(String database, Connection conn) {
		this.database = database;
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
				st.close();
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
			String cols = "COLUMN_NAME,COLUMN_COMMENT,DATA_TYPE,COLUMN_KEY,IS_NULLABLE,CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION,NUMERIC_SCALE,COLUMN_DEFAULT";
			String sql = "select " + cols + " from information_schema.columns where table_schema='" + database
					+ "' and table_name='" + tableName + "'";
			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象，st属局部变量
			rs = st.executeQuery(sql); // 执行sql查询语句，返回查询数据的结果集

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
				rowCount++;
			}
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("==>      Total: " + rowCount);
			// conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			logger.error("查询数据失败");
			logger.error(e.toString(), e);
		} finally {
			// 需要手动关闭Statement和ResultSet，Connection在连接池中不关闭
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
			String sql = "select table_name,table_comment,table_rows from information_schema.tables where table_schema='"
					+ database + "' and table_type='BASE TABLE'";
			st = (Statement) conn.createStatement();
			rs = st.executeQuery(sql);

			while (rs.next()) {
				String tableName = rs.getString("table_name");
				String comment = rs.getString("table_comment");
				int tableRows = rs.getInt("table_rows");
				Table table = new Table();
				table.setTableName(tableName);
				table.setComment(dealComment(comment));
				table.setTotalResult(tableRows);
				list.add(table);
				rowCount++;
			}
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("==>      Total: " + rowCount);
			// conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			logger.error("查询数据失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
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
		int rowCount = 0;
		Table table = null;
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			String sql = "select table_name,table_comment,table_rows from information_schema.tables where table_schema='"
					+ database + "' and table_type='BASE TABLE' and table_name='" + tableName + "'";
			st = (Statement) conn.createStatement();
			rs = st.executeQuery(sql);

			while (rs.next()) {
				String table_comment = rs.getString("table_comment");
				int table_rows = rs.getInt("table_rows");
				table = new Table();
				table.setTableName(tableName);
				table.setComment(dealComment(table_comment));
				table.setTotalResult(table_rows);
				table.setColumnFields(showTableColumns(tableName));
				rowCount++;
			}
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("==>      Total: " + rowCount);
			// conn.close(); // 关闭数据库连接
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
			sql += " limit " + page.getCurrentResult() + "," + page.getShowCount();

			st = (Statement) conn.createStatement();
			rs = st.executeQuery(sql);
			List<ColumnField> fields = showTableColumns(tableName);
			while (rs.next()) {
				PageData pd = new PageData();
				for (ColumnField columnField : fields) {
					pd.put(columnField.getColumn(), rs.getString(columnField.getColumn()));
				}
				list.add(pd);
				rowCount++;
			}
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("==>      Total: " + rowCount);
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
			String countSql = "select count(1) " + sql.substring(sql.indexOf("from"));
			st = (Statement) conn.createStatement();
			rs = st.executeQuery(countSql);
			while (rs.next()) {
				count = rs.getInt(1);
			}
			logger.debug("==>  Preparing: " + countSql);
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
		List<PageData> list = new ArrayList<PageData>();
		int rowCount = 0;
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			st = (Statement) conn.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				PageData pd = new PageData();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					pd.put(rs.getMetaData().getColumnName(i), rs.getString(i));
				}
				list.add(pd);
				rowCount++;
			}
			// sql日志
			logger.debug("==>  Preparing: " + sql);
			logger.debug("==>      Total: " + rowCount);
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
			logger.debug("==>       Total: " + rowCount);
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
			logger.debug("==>       Total: " + rowCount);
		} catch (SQLException e) {
			logger.error("更新数据失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean createTable(PageData tableData) {
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			String tableName = tableData.getString("tableName");
			String comment = tableData.getString("comment");
			List<PageData> columns = (List<PageData>) tableData.get("columns");
			String sql = "create table " + tableName + "(";
			for (PageData col : columns) {
				String column = col.getString("column");
				String type = col.getString("type");
				String remark = col.getString("remark");
				String key = col.getString("key");
				sql += column + " " + type + ("PRI".equals(key) ? " primary key" : "") + " comment '"
						+ (remark == null ? "" : remark) + "',";
			}
			sql = sql.replaceAll(",$", "") + ") comment '" + (comment == null ? "" : comment) + "'";
			st = (Statement) conn.createStatement();
			st.executeUpdate(sql);
			// sql日志
			logger.debug("==>   Preparing: " + sql);
			return true;
		} catch (SQLException e) {
			logger.error("创建表失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean alterTable(PageData tableData) {
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			String tableName = tableData.getString("tableName");
			String comment = tableData.getString("comment");
			// 查询原表结构
			Table table = showTable(tableName);
			st = (Statement) conn.createStatement();
			// 更新表注释
			if (comment != null && !comment.equals(table.getComment())) {
				String sql = "alter table " + tableName + " comment '" + comment + "'";
				st.executeUpdate(sql);
				logger.debug("==>   Preparing: " + sql);
			}
			List<ColumnField> columnFields = table.getColumnFields();
			List<PageData> columns = (List<PageData>) tableData.get("columns");
			for (PageData col : columns) {
				String column = col.getString("column");
				String type = col.getString("type");
				String remark = col.getString("remark");
				boolean flag = true;
				for (ColumnField columnField : columnFields) {
					// 更新字段注释
					if (columnField.getColumn().equals(column)) {
						if (remark != null && !remark.equals(columnField.getRemark())) {
							String sql = "alter table " + tableName + " modify column " + column + " " + type
									+ " comment '" + remark + "'";
							st.executeUpdate(sql);
							logger.debug("==>   Preparing: " + sql);
						}
						// 已经存在
						flag = false;
					}
				}
				// 不存在则插入新字段
				if (flag) {
					String sql = "alter table " + tableName + " add column " + column + " " + type + " comment '"
							+ (remark == null ? "" : remark) + "'";
					st.executeUpdate(sql);
					logger.debug("==>   Preparing: " + sql);
				}
			}
			// DDL不支持事务回滚
			return true;
		} catch (SQLException e) {
			logger.error("更新表失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
		}
		return false;
	}
}
