package com.focus.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

import com.focus.util.ConfigUtil;
import com.focus.util.Log;
import com.focus.util.Tools;

public class SubTable
{
	private String table;//需要分表的表
	
	private int saveDays;//保存的天数
	
	private long timestamp;//时间戳

	/**
	 * 连接数据库
	 * @return
	 * @throws Exception
	 */
	protected static Connection connect() throws Exception
	{
		String dbDriver = ConfigUtil.getString("dbDriver");
		String dbUser = ConfigUtil.getString("dbUser");
	    String dbPassword = ConfigUtil.getString("dbPassword");
	    String dbUrl = ConfigUtil.getString("dbUrl");
		Log.msg("Connection:"+
		        "\n\tdbDriver="+dbDriver+
		        "\n\tdbUser="+dbUser+
		        "\n\tdbPassword="+dbPassword+
		        "\n\tdbUrl="+dbUrl
		        );	    
        Class.forName(dbDriver).newInstance();
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
	}
	
	public static void main(String args[])
	{
		int saveDays = args.length>0&&Tools.isNumeric(args[0])?Integer.parseInt(args[0]):7;
		long timestampe = System.currentTimeMillis();
		//只允许每天凌晨5点钟执行引擎
		Connection connection = null;
		Log.getInstance().setSubroot("SubTable");
        Log.getInstance().setDebug(true);
        Log.getInstance().setLogable(true);
        Log.getInstance().start();
		Log.msg("Startup to sub.");		
		try
		{
			connection = SubTable.connect();
			for(int i = 1; i < args.length; i++ )
			{
				SubTable subTable = new SubTable(args[i], saveDays, timestampe);
				try
				{
					if( subTable.createNewTableTemp(connection) )
					{
						if( subTable.renameOldTalbe(connection) )
						{
							subTable.setNewTableName(connection);
						}
						subTable.deprecated(connection);
					}
				}
				catch (Exception e)
				{
					Log.err("Faield to sub for exception:");
					Log.err(e);
				}
			}
		}
		catch (Exception e1)
		{
			Log.err(e1);
		}
		finally
		{
			if( connection != null )
			try
			{
				connection.close();
			}
			catch (SQLException e)
			{
			}
		}
		Log.getInstance().close();
	}

	protected SubTable(String table, int saveDays, long timestamp)
	{
		this.saveDays = saveDays<7?-7:-saveDays;//至少保存7填
		this.table = table;
		this.timestamp = timestamp;
	}
	
	/**
	 * 创建新标的临时表 同时创建索引
	 * @param connection
	 * @return 分表的表名
	 * @throws Exception
	 */
	protected boolean createNewTableTemp(Connection connection) throws Exception
	{
		if( !this.existTable(connection, this.table) )
		{
			Log.war("Failed to sub the table "+table + " for the table not found.");
			return false;
		}
		String subfix = "_"+Tools.getFormatTime("yyyyMMdd", this.timestamp);
		String tableSub = this.table+subfix;
		Log.war("Tod sub the table "+tableSub + ".");
		if( this.existTable(connection, tableSub) )
		{
			Log.war("Delete the the sub-table "+tableSub + ".");
			this.execute(connection, "DROP TABLE "+tableSub);
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		calendar.add(Calendar.DAY_OF_MONTH, -1);//得到昨天的日期 
		String subfix1 = "_"+Tools.getFormatTime("yyyyMMdd", calendar.getTimeInMillis());
		String tableSub1 = this.table+subfix1;
		if( this.existTable(connection, tableSub1) )
		{
			Log.war("No need to create the sub-table "+tableSub1 + " for the sub-table found.");
			return false;
		}

		StringBuffer sqlTable = new StringBuffer();
		sqlTable.append("CREATE TABLE "+tableSub+" (\n");
		String pk = null;
		ResultSet rs = connection.getMetaData().getPrimaryKeys( null, null, table);
		if(rs.next())
		{
			pk = rs.getString("COLUMN_NAME");
		}
		rs.close();
        rs = connection.getMetaData().getColumns( null, "%", table, "%");
        int i = 0;
		while(rs.next())
		{
			if( i > 0 )
			{
				sqlTable.append(",\n");
			}
			String columnName = rs.getString("COLUMN_NAME");
			String typeNmae = rs.getString("TYPE_NAME");
			this.appendCrateTableColumn(sqlTable, columnName, typeNmae, rs.getInt("COLUMN_SIZE"), rs.getBoolean("NULLABLE"), rs.getString("COLUMN_DEF"));
			if( ConfigUtil.getString("dbUrl").indexOf("mysql") != -1 &&
				columnName.equalsIgnoreCase(pk) )
			{//如果列名和主键列名匹配，同时数据库是mysql那么添加增长属性
				sqlTable.append(" auto_increment");
			}
			i += 1;
		}
		if( pk != null )
		{
			sqlTable.append(",\n");
			sqlTable.append("PRIMARY KEY ("+pk+")");
		}
		rs.close();
		if( ConfigUtil.getString("dbUrl").indexOf("mysql") == -1 )
		{
			throw new Exception("Failed to create the sub-table "+tableSub + " for only support mysql.");
		}
		sqlTable.append("\n)ENGINE=INNODB DEFAULT CHARSET=utf8;");
//		Log.msg("Succeed to build the sql of cureate table:\n"+sqlTable);
		this.execute(connection, sqlTable.toString());
		rs = connection.getMetaData().getIndexInfo(null, null, table, false, false);
		while(rs.next())
		{
//		    TABLE_CAT String => table catalog (may be null)
//		    TABLE_SCHEM String => table schema (may be null)
//		    TABLE_NAME String => table name
//		    NON_UNIQUE boolean => Can index values be non-unique? false when TYPE is tableIndexStatistic
//		    INDEX_QUALIFIER String => index catalog (may be null); null when TYPE is tableIndexStatistic
//		    INDEX_NAME String => index name; null when TYPE is tableIndexStatistic
//		    TYPE short => index type:
//		        tableIndexStatistic - this identifies table statistics that are returned in conjuction with a table's index descriptions
//		        tableIndexClustered - this is a clustered index
//		        tableIndexHashed - this is a hashed index
//		        tableIndexOther - this is some other style of index
//		    ORDINAL_POSITION short => column sequence number within index; zero when TYPE is tableIndexStatistic
//		    COLUMN_NAME String => column name; null when TYPE is tableIndexStatistic
//		    ASC_OR_DESC String => column sort sequence, "A" => ascending, "D" => descending, may be null if sort sequence is not supported; null when TYPE is tableIndexStatistic
//		    CARDINALITY int => When TYPE is tableIndexStatisic then this is the number of rows in the table; otherwise it is the number of unique values in the index.
//		    PAGES int => When TYPE is tableIndexStatisic then this is the number of pages used for the table, otherwise it is the number of pages used for the current index.
//		    FILTER_CONDITION String => Filter condition, if any. (may be null)
//			int TYPE = rs.getShort("TYPE");
//			int CARDINALITY = rs.getInt("CARDINALITY");
			boolean NON_UNIQUE = rs.getBoolean("NON_UNIQUE");
			if( !NON_UNIQUE )
			{
				continue;
			}
			String tableNmae = rs.getString("TABLE_NAME");
			String ascOrDesc = rs.getString("ASC_OR_DESC");
			ascOrDesc = ascOrDesc.equals("A")?"ASC":"DESC";
			String columnName = rs.getString("COLUMN_NAME");
			String indexName = "idx_"+tableNmae+"_"+columnName+subfix;
			String sqlIndex = "CREATE INDEX "+indexName+" on "+tableSub+"("+columnName+" "+ascOrDesc+")";
//			Log.msg("Succeed to build the sql of cureate index(TYPE="+TYPE+
//			        ",CARDINALITY="+CARDINALITY+
//			        ",NON_UNIQUE="+NON_UNIQUE+
//			        "):\n"+sqlIndex);
			this.execute(connection, sqlIndex);
//		create index idx_wappush_sent_gwwappushid on TB_WAPPUSH_SENT (gwwappushid ASC);
		}
		rs.close();
		return true;
	}
	
	protected void appendCrateTableColumn(
			StringBuffer sql,
			String columnName,
			String typeName, 
			int columnSize,
			boolean nullable,
			String defaultValue )
	{
		sql.append(columnName);
		sql.append(" ");
		sql.append(typeName);
		if( !typeName.equalsIgnoreCase("datetime") && !typeName.equalsIgnoreCase("bigint unsigned") )
		{
			sql.append("(");
			sql.append(columnSize);
			sql.append(")");
		}
		sql.append(" ");
		sql.append(nullable?"NULL":"NOT NULL");
		sql.append(defaultValue!=null?(" DEFAULT "+defaultValue):"");
	}
	
	/**
	 * 将旧表重命名
	 * @param connection
	 * @throws Exception
	 */
	protected boolean renameOldTalbe(Connection connection) throws Exception
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		calendar.add(Calendar.DAY_OF_MONTH, -1);//得到昨天的日期
		String subfix = "_"+Tools.getFormatTime("yyyyMMdd", calendar.getTimeInMillis());
		String tableSub = this.table+subfix;//Tools.getFormatTime("yyyyMMdd", this.timestamp);
		Log.msg("Todo rename the old-table "+table + " to "+ tableSub );
		if( this.existTable(connection, tableSub) )
		{
			Log.war("Failed to rename the old-table "+table + " for the sub-table found.");
			return false;
		}
		if( !this.existTable(connection, this.table) )
		{
			throw new Exception("Failed to rename the old-table "+table + " for the table not found.");
		}
        String sqlStr = "ALTER table "+this.table+" RENAME "+tableSub;
//		Log.msg("Succeed to build the sql of renameOldTalbe:"+sqlStr );
        this.execute(connection, sqlStr);
        return true;
	}
	
	/**
	 * 将临时命名的新标改为正式表
	 * @param connection
	 * @throws Exception
	 */
	protected void setNewTableName(Connection connection) throws Exception
	{
		String subfix = "_"+Tools.getFormatTime("yyyyMMdd", this.timestamp);
		Log.msg("Todo set the new-table "+this.table+subfix + " to "+ table );
		if( !this.existTable(connection, this.table+subfix) )
		{
			throw new Exception("Failed to create the blank sub-table "+table + " for the temp-table not found.");
		}
		if( this.existTable(connection, this.table) )
		{
			throw new Exception("Failed to create the blank sub-table "+table + " for the sub-table found it.");
		}
        String sqlStr = "ALTER table "+this.table+subfix+" RENAME "+this.table;
//		Log.msg("Succeed to build the sql of setNewTableName:"+sqlStr );
        this.execute(connection, sqlStr);
	}
	
	/**
	 * 丢弃过期不要的数据表
	 * @param connection
	 * @throws Exception
	 */
	protected void deprecated(Connection connection) throws Exception
	{
		Log.msg("Todo deprecated from "+Tools.getFormatTime("yyyyMMdd", timestamp) + " to "+saveDays+" days expired.");
		Calendar calendar = Calendar.getInstance();
		int countUnexist = 0;//不存在计数
		for( int i = saveDays; i > -90; i-- )
		{
			calendar.setTimeInMillis(this.timestamp);
			calendar.add(Calendar.DAY_OF_YEAR, i);
			String tableDiscard = this.table+"_"+Tools.getFormatTime("yyyyMMdd", calendar.getTimeInMillis());
			if( !this.existTable(connection, tableDiscard) )
			{
				countUnexist += 1;
				if( countUnexist > 2 )
				{
					Log.msg("Not found the table deprecated after "+tableDiscard);
					break;//如果连续不存在超过2个表示后面已经没有需要丢弃的表了
				}
				continue;
			}
			countUnexist = 0;
	        String sqlStr = "DROP TABLE "+ tableDiscard;
			Log.msg(sqlStr);
	        this.execute(connection, sqlStr);
		}
	}
	/**
	 * 是否存在表
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	private boolean existTable(Connection connection, String from) throws Exception
	{
        ResultSet rs = null;
        try
        {
        	DatabaseMetaData dbMetaData= connection.getMetaData();
        	rs = dbMetaData.getTables(null, null, from, new String[]{"TABLE"});
        	if(rs.next())
        	{
        		return true;
        	}
        }
        catch (Exception e)
        {
            // TODO:数据库操作错误，生成告警
            throw e;
		}
        finally
        {
        	try
            {
        		if(rs != null)
        		{
        			rs.close();
        		}
            }
            catch (SQLException ex)
            {
            }
        }
    	return false;
	}
	
	/**
	 * 执行SQL语句
	 * @param connection
	 * @param sqlStr
	 * @return
	 * @throws Exception
	 */
	private void execute(Connection connection, String sqlStr) throws Exception
	{
        Statement stmt = null;
        try
        {
            //每次装载短信数量不超过SMGPAccessConfig.AccSMSendSpeedLimit配置并且为未发送状态的短信
            stmt = connection.createStatement();
            stmt.execute(sqlStr);
        }
        catch(Exception e)
        {
        	Log.err("Failed to execute sql:\n"+sqlStr);
            throw e;
        }
        finally
        {
            try
            {
                if( stmt != null )
                    stmt.close();
            }
            catch (SQLException ex)
            {
            }
        }
	}

	public String getTable()
	{
		return table;
	}

	public void setTable(String table)
	{
		this.table = table;
	}
}
