package com.zong.web.dbclient.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.zong.util.Properties;
import com.zong.web.dbclient.bean.ColumnField;
import com.zong.web.dbclient.bean.TableEntity;

public class MysqlCodeDao extends BaseJdbcDao implements IJdbcDao {

	public MysqlCodeDao(Properties props) {
		super(props);
	}

	public MysqlCodeDao() {

	}

	/**
	 * 查询某个表的所有字段
	 */
	public List<ColumnField> showTableColumns(String tableName) {
		conn = getConnection(); // 同样先要获取连接，即连接到数据库.
		List<ColumnField> list = new ArrayList<ColumnField>();
		try {
			String sql = "select * from information_schema.columns where table_schema='" + database + "' and table_name='" + tableName + "'";
			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象，st属局部变量
			ResultSet rs = st.executeQuery(sql); // 执行sql查询语句，返回查询数据的结果集

			while (rs.next()) { // 判断是否还有下一个数据
				// 根据字段名获取相应的值
				String column = rs.getString("COLUMN_NAME");
				String comment = rs.getString("COLUMN_COMMENT");
				String columnType = rs.getString("DATA_TYPE");
				String columnKey = rs.getString("COLUMN_KEY");
				String canNull = rs.getString("IS_NULLABLE");
				Integer dataLength = rs.getInt("CHARACTER_MAXIMUM_LENGTH");
				Integer dataPrecision = rs.getInt("NUMERIC_PRECISION");
				Integer dataScale = rs.getInt("NUMERIC_SCALE");
				String defaultValue = rs.getString("COLUMN_DEFAULT");
				ColumnField columnField = new ColumnField();
				columnField.setColumn(column);
				columnField.setField(transColumn(column));
				columnField.setColumnType(columnType);
				columnField.setKey(columnKey);
				columnField.setRemark(comment);
				columnField.setCanNull(canNull);
				columnField.setDataLength(dataLength);
				columnField.setDataPrecision(dataPrecision);
				columnField.setDataScale(dataScale);
				columnField.setDefaultValue(defaultValue);
				columnField.setType(columnType);
				list.add(columnField);
			}
			//conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			System.out.println("查询数据失败");
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 获取当前用户所有表名
	 */
	public List<TableEntity> showTables() {
		conn = getConnection(); // 同样先要获取连接，即连接到数据库.

		List<TableEntity> list = new ArrayList<TableEntity>();
		try {
			String sql = "select table_name,table_comment,table_rows from information_schema.tables where table_schema='" + database + "' and table_type='BASE TABLE'";
			st = (Statement) conn.createStatement(); // 创建用于执行静态sql语句的Statement对象，st属局部变量
			ResultSet rs = st.executeQuery(sql); // 执行sql查询语句，返回查询数据的结果集

			while (rs.next()) {
				String tableName = rs.getString("table_name");
				String comment = rs.getString("table_comment");
				int tableRows = rs.getInt("table_rows");
				TableEntity tableEntity = new TableEntity();
				tableEntity.setTableName(tableName);
				tableEntity.setComment(dealComment(comment));
				tableEntity.setTotalResult(tableRows);
				list.add(tableEntity);
			}
			//conn.close(); // 关闭数据库连接
		} catch (SQLException e) {
			System.out.println("查询数据失败");
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 字段名转换为属性名，首字母小写，下划线后一个单词大写开头，然后取消下划线
	 */
	public static String transColumn(String column) {
		String[] names = column.split("_");
		StringBuffer nameBuffer = new StringBuffer();
		for (int i = 0; i < names.length; i++) {
			String name = names[i].toLowerCase();
			if (i != 0) {
				name = name.substring(0, 1).toUpperCase() + name.substring(1);
			}
			nameBuffer.append(name);
		}
		return nameBuffer.toString();
	}

	private static String dealComment(String comment) {
		if (comment.indexOf("InnoDB") >= 0) {
			if (comment.indexOf(";") >= 0) {
				comment = comment.substring(0, comment.lastIndexOf(";"));
			} else {
				comment = "";
			}
		}
		return comment;
	}

}
