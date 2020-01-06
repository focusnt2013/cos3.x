package com.focus.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.focus.util.ConfigUtil;
import com.focus.util.Log;

/**
 * <p>Title: EMA后台服务程序</p>
 *
 * <p>Description: EMA后台服务程序</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: FOCUS</p>
 *
 * @author Focus Lau
 * @version 1.0
 */
public class Dao
{
    /*数据库连接对象*/
    private Connection connection = null;
    /* 预处理对象 */
    protected PreparedStatement pstmt = null;

    protected Dao()
    {
    	this.openDatabase();
        this.connection = ConnectionPool.getInstance().getConnection();
    }

    protected void setPrepareStatement( String sqlStr, boolean autoCommit ) throws SQLException
    {
        if( connection == null )
        {
            throw new SQLException( "Failed to get connection from pool." );
        }
        this.connection.setAutoCommit( autoCommit );
        this.pstmt = this.connection.prepareStatement( sqlStr );
    }

    public void addBatch()
    {
        if( pstmt != null )
        {
            try
            {
                pstmt.addBatch();
//                countBatch += 1;
            }
            catch( SQLException ex )
            {
                Log.err( ex );
                // 数据库异常，强行退出
                System.exit(0x89);
            }
        }
    }
    /**
     * 执行批处理
     * @return int
     */
    public int execute() throws SQLException
    {
        return pstmt.executeUpdate();
    }
    /**
     * 执行批处理
     * @return int
     */
    public int batch()
    {
        try
        {
            return pstmt.executeBatch().length;
        }
        catch( SQLException ex )
        {
            Log.err( ex );
            // 数据库异常，强行退出
            System.exit(0x89);
        }
        return 0;
    }

    public int getUpdateCount()
    {
        if( pstmt != null )
        {
            try
            {
                return pstmt.getUpdateCount();
            }
            catch( SQLException ex )
            {
                Log.err( ex );
            }
        }
        return 0;
    }

    public void rollback()
    {
        if( connection != null && pstmt != null )
        {
            try
            {
                this.connection.rollback();
//                countBatch = 0;
            }
            catch( SQLException ex )
            {
                Log.err( ex );
            }
        }
    }
    /**
     * 提交确认
     */
    public void commit()
    {
        if( connection != null )// && countBatch > 0 )
        {
            try
            {
                this.connection.commit();
                this.connection.setAutoCommit( true );
//                countBatch = 0;
            }
            catch( SQLException ex )
            {
                Log.err( ex );
            }
        }
    }

    /**
     * 释放数据库连接到连接池
     */
    public void close()
    {
        if( pstmt != null )
        {
            try
            {
                this.pstmt.close();
            }
            catch( SQLException ex )
            {
                Log.err( ex );
            }
        }

        if( connection != null )
        {
            ConnectionPool.getInstance().freeConnection( this.connection );
        }
    }

    public int getCountBatch()
    {
        return 0;//countBatch;
    }

    public Connection getConnection()
    {
        return connection;
    }
    
    /**
     * 打开数据库连接
     */
    public void openDatabase()
    {
    	if( !ConnectionPool.isConnect() )
    	{
	        //启动数据库连接管理
	        String serverHost = ConfigUtil.getString("serverHost");
	        String dbType = ConfigUtil.getString("dbType");
	        String dbName = ConfigUtil.getString("dbName");
	        String dbDriver = ConfigUtil.getString("dbDriver");
	        String dbUser = ConfigUtil.getString("dbUser");
	        String dbPassword = ConfigUtil.getString("dbPassword");
	        try
	        {
	            ConnectionPool.connect(serverHost, dbType, dbName, dbDriver,
	                                       dbUser, dbPassword);
	        }
	        catch(Exception e)
	        {
	            Log.err(e);            
	        }
    	}
    }
    

    /**
     * 
     * @return
     */
    public static long getMaxId(String table, String id)
    {
    	String sqlStr = "SELECT MAX("+id+") FROM "+table;
        Connection con = ConnectionPool.getInstance().getConnection();
        if(con == null )
        {
            Log.war("Failed to get id for connection pool null.");
            return 0;
        }
        Statement stmt = null;
        ResultSet rs = null;   
        try
        {
            stmt = con.createStatement();
            rs = stmt.executeQuery( sqlStr );
            if( rs.next() )
            {
            	return rs.getLong(1);
            }
        }
        catch( Exception e )
        {
    		Log.err("Failed to load data for user", e);
        }
        finally
        {
        	try
            {
                if( rs != null )
                	rs.close();
                if( stmt != null )
                	stmt.close();
                ConnectionPool.getInstance().freeConnection( con );
            }
            catch (SQLException ex)
            {
            }
        }
        return 0;
    }
}
