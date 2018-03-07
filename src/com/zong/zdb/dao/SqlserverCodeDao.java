package com.zong.zdb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

public class SqlserverCodeDao implements IJdbcDao {

	private static final Logger logger = Logger.getLogger(MysqlCodeDao.class);
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public Connection conn;

	public SqlserverCodeDao(Connection conn) {
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

	@Override
	public List<Table> showTables() {
		int rowCount = 0;
		List<Table> list = new ArrayList<Table>();
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			String sql = "SELECT o.object_id, o.name table_name, o.type, CAST(ep.value AS NVARCHAR(MAX)) table_comment, o.create_date, o.modify_date, st.row_count table_rows FROM sys.all_objects o LEFT JOIN sys.schemas s ON o.schema_id = s.schema_id LEFT JOIN sys.tables t ON o.object_id = t.object_id LEFT JOIN sys.extended_properties ep ON (o.object_id = ep.major_id AND ep.class = 1 AND ep.minor_id = 0 AND ep.name = 'MS_Description') LEFT JOIN (SELECT object_id, SUM(ROWS) row_count FROM sys.partitions WHERE index_id < 2 GROUP BY object_id) st ON o.object_id = st.object_id WHERE s.name = N'dbo' AND (o.type='U' OR o.type='S') ORDER BY o.name";
			st = (Statement) conn.createStatement();
			rs = st.executeQuery(sql);

			while (rs.next()) {
				String tableName = rs.getString("table_name");
				String comment = rs.getString("table_comment");
				int tableRows = rs.getInt("table_rows");
				Table table = new Table();
				table.setTableName(tableName);
				table.setComment(comment);
				table.setTotalResult(tableRows);
				list.add(table);
				rowCount++;
			}
			// sql日志
			logger.debug("==>   Preparing: " + sql.replaceAll("\\s+", " ").replaceAll("\n", " "));;
			logger.debug("<==       Total: " + rowCount);
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
	public List<ColumnField> showTableColumns(String tableName) {
		int rowCount = 0;
		List<ColumnField> list = new ArrayList<ColumnField>();
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			String sql = "SELECT col.name AS COLUMN_NAME , ISNULL(cast(ep.[value] as varchar(500)), '') AS COLUMN_COMMENT , t.name AS DATA_TYPE , col.length AS NUMERIC_PRECISION , ISNULL(COLUMNPROPERTY(col.id, col.name, 'Scale'), 0) AS NUMERIC_SCALE , CASE WHEN EXISTS ( SELECT 1 FROM dbo.sysindexes si INNER JOIN dbo.sysindexkeys sik ON si.id = sik.id AND si.indid = sik.indid INNER JOIN dbo.syscolumns sc ON sc.id = sik.id AND sc.colid = sik.colid INNER JOIN dbo.sysobjects so ON so.name = si.name AND so.xtype = 'PK' WHERE sc.id = col.id AND sc.colid = col.colid ) THEN 'PRI' ELSE '' END AS COLUMN_KEY , CASE WHEN col.isnullable = 1 THEN 'YES' ELSE 'NO' END AS IS_NULLABLE , ISNULL(comm.text, '') AS COLUMN_DEFAULT FROM dbo.syscolumns col LEFT JOIN dbo.systypes t ON col.xtype = t.xusertype inner JOIN dbo.sysobjects obj ON col.id = obj.id AND obj.xtype = 'U' AND obj.status >= 0 LEFT JOIN dbo.syscomments comm ON col.cdefault = comm.id LEFT JOIN sys.extended_properties ep ON col.id = ep.major_id AND col.colid = ep.minor_id AND ep.name = 'MS_Description' LEFT JOIN sys.extended_properties epTwo ON obj.id = epTwo.major_id AND epTwo.minor_id = 0 AND epTwo.name = 'MS_Description' WHERE obj.name = '"
					+ tableName + "' ORDER BY col.colorder";
			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象，st属局部变量
			rs = st.executeQuery(sql); // 执行sql查询语句，返回查询数据的结果集

			while (rs.next()) { // 判断是否还有下一个数据
				// 根据字段名获取相应的值
				String column = rs.getString("COLUMN_NAME");
				String comment = rs.getString("COLUMN_COMMENT");
				String dataType = rs.getString("DATA_TYPE");
				String columnKey = rs.getString("COLUMN_KEY");
				String canNull = rs.getString("IS_NULLABLE");
				// Long dataLength = rs.getLong("CHARACTER_MAXIMUM_LENGTH");
				Integer dataPrecision = rs.getInt("NUMERIC_PRECISION");
				Integer dataScale = rs.getInt("NUMERIC_SCALE");
				String defaultValue = rs.getString("COLUMN_DEFAULT");
				ColumnField columnField = new ColumnField();
				columnField.setColumn(column);
				columnField.setDataType(dataType);
				columnField.setKey(columnKey);
				columnField.setRemark(comment);
				columnField.setCanNull(canNull);
				// columnField.setDataLength(dataLength);
				columnField.setDataPrecision(dataPrecision);
				columnField.setDataScale(dataScale);
				columnField.setDefaultValue(defaultValue);
				// 转换字段和类型到java属性名和属性类型
				columnField.transColumnToField(column);
				columnField.transColumnType(dataType);
				list.add(columnField);
				rowCount++;
			}
			// sql日志
			logger.debug("==>   Preparing: " + sql.replaceAll("\\s+", " ").replaceAll("\n", " "));;
			logger.debug("<==       Total: " + rowCount);
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

	@Override
	public Table showTable(String tableName) {
		int rowCount = 0;
		Table table = null;
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			String sql = "SELECT o.object_id, o.name table_name, o.type, CAST(ep.value AS NVARCHAR(MAX)) table_comment, o.create_date, o.modify_date, st.row_count table_rows FROM sys.all_objects o LEFT JOIN sys.schemas s ON o.schema_id = s.schema_id LEFT JOIN sys.tables t ON o.object_id = t.object_id LEFT JOIN sys.extended_properties ep ON (o.object_id = ep.major_id AND ep.class = 1 AND ep.minor_id = 0 AND ep.name = 'MS_Description') LEFT JOIN (SELECT object_id, SUM(ROWS) row_count FROM sys.partitions WHERE index_id < 2 GROUP BY object_id) st ON o.object_id = st.object_id WHERE s.name = N'dbo' AND (o.type='U' OR o.type='S') and o.name='"
					+ tableName + "' ORDER BY o.name";
			st = (Statement) conn.createStatement();
			rs = st.executeQuery(sql);

			while (rs.next()) {
				String comment = rs.getString("table_comment");
				int table_rows = rs.getInt("table_rows");
				table = new Table();
				table.setTableName(tableName);
				table.setComment(comment);
				table.setTotalResult(table_rows);
				table.setColumnFields(showTableColumns(tableName));
				rowCount++;
			}
			// sql日志
			logger.debug("==>   Preparing: " + sql.replaceAll("\\s+", " ").replaceAll("\n", " "));;
			logger.debug("<==       Total: " + rowCount);
			// conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			logger.error("查询数据失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
		}
		return table;
	}

	@Override
	public List<PageData> showTableDatas(Page page) {
		int rowCount = 0;
		List<PageData> list = new ArrayList<PageData>();
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			String tableName = page.getTable();
			page.setTotalResult(count(tableName));// 统计总数
			String sql = "SELECT * FROM ( SELECT row_number () OVER (ORDER BY tempcolumn) temprownumber ,* FROM ( SELECT TOP "
					+ (page.getCurrentResult() + page.getShowCount()) + " tempcolumn = 0 ,* FROM " + tableName
					+ " ) t ) tt WHERE temprownumber > " + page.getCurrentResult();
			// 分页，条件筛选
			if (page.getPd().get("orderColumn") != null && !page.getPd().get("orderColumn").equals("")) {
				sql += " order by " + page.getPd().get("orderColumn") + " " + page.getPd().get("orderType");
			}

			st = (Statement) conn.createStatement();
			rs = st.executeQuery(sql);
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
			logger.debug("==>   Preparing: " + sql.replaceAll("\\s+", " ").replaceAll("\n", " "));;
			logger.debug("<==       Total: " + rowCount);
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

	private int count(String tableName) throws SQLException {
		int count = 0;
		ResultSet rs = null;
		Statement st = null;
		try {
			String countSql = "select count(1) from " + tableName;
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
			logger.debug("==>   Preparing: " + sql.replaceAll("\\s+", " ").replaceAll("\n", " "));;
			logger.debug("<==       Total: " + rowCount);
		} catch (SQLException e) {
			logger.error("查询数据失败");
			logger.error(e.toString(), e);
		} finally {
			closeStatement(rs, st, pst);
		}
		return list;
	}

	@Override
	public List<PageData> showSqlDatas(String sql, List<String> params) {
		List<PageData> list = new ArrayList<PageData>();
		int rowCount = 0;
		ResultSet rs = null;
		Statement st = null;
		PreparedStatement pst = null;
		try {
			pst = conn.prepareStatement(sql);
			for (int i = 0; i < params.size(); i++) {
				pst.setString(i + 1, params.get(i));
			}
			rs = pst.executeQuery();
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
			logger.debug("==>   Preparing: " + sql.replaceAll("\\s+", " ").replaceAll("\n", " "));;
			logger.debug("==>      params: " + params);
			logger.debug("<==       Total: " + rowCount);
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
			logger.debug("==>   Preparing: " + sql.replaceAll("\\s+", " ").replaceAll("\n", " "));;
			logger.debug("==>      params: " + params.toString().replaceAll(",$", ""));
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
			logger.debug("==>   Preparing: " + sql.replaceAll("\\s+", " ").replaceAll("\n", " "));;
			logger.debug("==>      params: " + params.toString().replaceAll(",$", ""));
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
		return false;
	}

	@Override
	public boolean alterTable(PageData tableData) {
		// TODO Auto-generated method stub
		return false;
	}

}
