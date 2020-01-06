package com.focus.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;

import com.focus.util.Log;
import com.focus.util.Tools;

public class SubTableTest
{
	public static void main(String args[])
	{
		Log.getInstance().setSubroot("SubTableTest");
        Log.getInstance().setDebug(true);
        Log.getInstance().setLogable(true);
        Log.getInstance().start();		
		Log.msg("Startup to sub.");
		args = new String[]{"60","tb_wappush_sent","tb_sms_sent","tb_mms_sent"};
		int saveDays = args.length>0&&Tools.isNumeric(args[0])?Integer.parseInt(args[0]):7;
		long timestampe = System.currentTimeMillis();
		Connection connection = null;
		try
		{
			connection = SubTable.connect();
			Calendar calendar = Calendar.getInstance();
			for(int day = 0; day < 10; day++)
			{//连续执行10天生成数据
				calendar.setTimeInMillis(timestampe);	
				calendar.add(Calendar.DAY_OF_MONTH, day);//得到昨天的日期 		
				for(int i = 1; i < args.length; i++ )
				{
					SubTable subTable = new SubTable(args[i], saveDays, calendar.getTimeInMillis());
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
}
