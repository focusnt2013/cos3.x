package com.focus.sql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import com.focus.util.Tools;

/**
 * 备份数据库数据到SQL文件中
 * @author think
 *
 */
public class BackupToSql{
	public static int restore(String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPassword, String table, ArrayList<String> sqls){
		Connection connection = null;
		Statement statement = null;
		int count = -1;
		try{
            Class.forName(jdbcDriver);
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUser, jdbcPassword); 
            statement = connection.createStatement();
            for(String sql : sqls){
            	try{
            		count += statement.executeUpdate(sql);
            	}
            	catch(Exception e){
            		e.printStackTrace();
            	}
            }
		}
		catch (Exception e){
			e.printStackTrace();
		}
        finally
        {
        	if( statement != null )
				try
				{
					statement.close();
				}
				catch (SQLException e)
				{
				} 
        	if( connection != null )
				try
				{
					connection.close();
				}
				catch (SQLException e)
				{
				}
        }
		return count;
	}
	/**
	 * 导出指定表数据的SQL
	 * @param jdbcUrl
	 * @param jdbcUser
	 * @param jdbcPassword
	 * @param table
	 * @return
	 */
	public static ArrayList<String> export(String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPassword, String table, HashMap<String, String> filters){
		ArrayList<String> sqls = new ArrayList<String>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		try{
            Class.forName(jdbcDriver);
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUser, jdbcPassword); 
            statement = connection.createStatement();
            rs = statement.executeQuery("select * from "+table);
            while(rs.next()){
            	StringBuilder sql = new StringBuilder("INSERT INTO "+table);
            	sql.append("(");
            	ResultSetMetaData meta = rs.getMetaData();
                int j = 0;
            	for(int i = 1; i <= meta.getColumnCount(); i++ ) {
                	if( filters != null && !filters.containsKey(meta.getColumnLabel(i).toLowerCase()) ){
                		continue;//不包含的字段跳过
                	}
                	if( j > 0 ){
                		sql.append(",");
                	}
                	j += 1;
                	sql.append(meta.getColumnLabel(i));
                }
            	j = 0;
            	sql.append(")VALUES(");
            	for(int i = 1; i <= meta.getColumnCount(); i++ ){
                	if( filters != null && !filters.containsKey(meta.getColumnLabel(i).toLowerCase()) ){
                		continue;//不包含的字段跳过
                	}
                	if( j > 0 ){
                		sql.append(",");
                	}
                	j += 1;
                	int type = meta.getColumnType(i);
                	Object data = null;
                	switch( type ){
            		case java.sql.Types.TIMESTAMP:
            			Timestamp ts = rs.getTimestamp(i);
            			if( ts != null ) {
                			sql.append("'");
                			sql.append(Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", ts.getTime()));
                			sql.append("'");
            			}
            			else {
                			sql.append("null");
            			}
            			break;
            		case java.sql.Types.DATE:
            			Date date = rs.getDate(i);
            			if( date != null ) {
                			sql.append("'");
                			sql.append(Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", date.getTime()));
                			sql.append("'");
            			}
            			else {
                			sql.append("null");
            			}
            			break;
            		case java.sql.Types.TIME:
            			Time time = rs.getTime(i);
            			if( time != null ) {
                			sql.append("'");
                			sql.append(Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", time.getTime()));
                			sql.append("'");
            			}
            			else {
                			sql.append("null");
            			}
            			break;
            		case java.sql.Types.BINARY:
            			sql.append("null");
            			break;
            		case java.sql.Types.BOOLEAN:
            		case java.sql.Types.BIGINT:
            		case java.sql.Types.DOUBLE:
            		case java.sql.Types.DECIMAL:
            		case java.sql.Types.NUMERIC:
            		case java.sql.Types.FLOAT:
            		case java.sql.Types.INTEGER:
            		case java.sql.Types.TINYINT:
            		case java.sql.Types.BIT:
            			sql.append(rs.getObject(i));
            			break;
            		case java.sql.Types.CLOB:
            		case java.sql.Types.BLOB:
            		case java.sql.Types.NULL:
            		case java.sql.Types.CHAR:
            		case java.sql.Types.VARCHAR:
            		default:// java.sql.Types.VARCHAR:
            			data = rs.getObject(i);
            			if( data != null ){
            				sql.append("'");
            				sql.append(data);
            				sql.append("'");
            			}
            			else{
            				sql.append("null");
            			}
            			break;
	                }
            	}
            	sql.append(")");
            	sqls.add(sql.toString());
            }
		}
		catch (Exception e){
			e.printStackTrace();
		}
        finally
        {
        	if( rs != null )
				try
				{
					rs.close();
				}
				catch (SQLException e)
				{
				}  
        	if( statement != null )
				try
				{
					statement.close();
				}
				catch (SQLException e)
				{
				} 
        	if( connection != null )
				try
				{
					connection.close();
				}
				catch (SQLException e)
				{
				}
        }
		return sqls;
	}
}
