package com.focus.cos.ops.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.focus.cos.api.Sysnotify;
import com.focus.sql.ConnectionPool;
import com.focus.sql.Dao;
import com.focus.util.Log;

public class NotifyDao extends Dao
{   
    public void prepareInsert() throws SQLException
    {
        super.setPrepareStatement( "INSERT INTO TB_NOTIFIES(NID, USERACCOUNT, TITLE, FILTER, NOTIFYTIME, CONTEXT, CONTEXTLINK, CONTEXTIMG, ACTION, ACTIONLINK, STATE, PRIORITY)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", true );
    }

	public void prepareUpdate() throws SQLException
    {
        super.setPrepareStatement( "UPDATE TB_NOTIFIES SET NOTIFYTIME=?, CONTEXT=?, CONTEXTLINK=?, CONTEXTIMG=?,ACTION=?,PRIORITY=? WHERE NID=?", true );
    }
    /**
     * 更新
     * @param Sysnotify
     */
    public void update( Sysnotify notify ) throws SQLException
    {
        int j = 1;
        Timestamp nullTimestamp = new Timestamp(0);
        if( notify.getNotifytime() != null )
        {
            pstmt.setTimestamp( j++, new Timestamp(notify.getNotifytime().getTime()) );
        }
        else
        {
            pstmt.setTimestamp( j++, nullTimestamp );        	
        }
        pstmt.setString( j++, notify.getContext() );
        pstmt.setString( j++, notify.getContextlink() );
        pstmt.setString( j++, notify.getContextimg() );
        pstmt.setString( j++, notify.getAction() );
        pstmt.setString( j++, notify.getActionlink() );
        pstmt.setInt( j++, notify.getPriority() );
        pstmt.setLong( j++, notify.getNid() );
    }

    /**
     * 新增
     * @param Sysnotify
     */
    public void save( Sysnotify notify ) throws SQLException
    {
        int j = 1;
        pstmt.setLong( j++, notify.getNid() );
        pstmt.setString( j++, notify.getUseraccount() );
        pstmt.setString( j++, notify.getTitle() );
        pstmt.setString( j++, notify.getFilter() );
        Timestamp nullTimestamp = new Timestamp(0);
        if( notify.getNotifytime() != null )
        {
            pstmt.setTimestamp( j++, new Timestamp(notify.getNotifytime().getTime()) );
        }
        else
        {
            pstmt.setTimestamp( j++, nullTimestamp );        	
        }
        pstmt.setString( j++, notify.getContext() );
        pstmt.setString( j++, notify.getContextlink() );
        pstmt.setString( j++, notify.getContextimg() );
        pstmt.setString( j++, notify.getAction() );
        pstmt.setString( j++, notify.getActionlink() );
        pstmt.setInt( j++, notify.getState() );
        pstmt.setInt( j++, notify.getPriority() );
    }

    /**
     * 查询指定条件的用户数据
     * @return
     */
    public static Sysnotify find(String filter, String title, String useraccount)
    {
    	Sysnotify notify = null;
        Connection con = ConnectionPool.getInstance().getConnection();
        if(con == null )
        {
            Log.war("Failed to query the users for connection pool null.");
            return notify;
        }
        Statement stmt = null;
        ResultSet rs = null;   
        try
        {
        	String sqlStr = "SELECT * FROM TB_NOTIFIES";
        	String sqlWhere = "";
        	if( filter != null )
        	{
        		sqlWhere += " AND FILTER='" + filter+"'";
        	}
        	if( title != null )
        	{
        		sqlWhere += " AND TITLE='"+title+"'";
        	}
        	if( useraccount != null )
        	{
        		sqlWhere += " AND USERACCOUNT='"+useraccount+"'";
        	}
        	if( !sqlWhere.isEmpty() )
        	{
        		sqlStr += " WHERE" + sqlWhere.substring(4);
        	}
            stmt = con.createStatement();
            rs = stmt.executeQuery( sqlStr );
//            StringBuffer sb = new StringBuffer(sqlStr);
            if( rs.next() )
            {
            	notify = new Sysnotify();
            	notify.setNid(rs.getLong("NID"));
            	notify.setTitle(rs.getString("TITLE"));
            	notify.setTitle(rs.getString("TITLE"));
            	notify.setContext(rs.getString("CONTEXT"));
            	notify.setContextlink(rs.getString("CONTEXTLINK"));
            }
//            Log.war(sb.toString());
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
        return notify;
    }
	/**
     * 加载指定ID后的消息
     * @return
     */
    public static List<Sysnotify> loadNotifiesList( long SEEDNID )
    {
        List<Sysnotify> list = new ArrayList<Sysnotify> ();
        Connection con = ConnectionPool.getInstance().getConnection();
        if(con == null )
        {
            Log.war("Failed to loadNotifiesList for connection pool null.");
            return list;
        }
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            //每次装载短信数量不超过SMGPAccessConfig.AccSMSendSpeedLimit配置并且为未发送状态的短信
            String sqlStr =
                "SELECT * FROM TB_NOTIFIES WHERE nid>"+ SEEDNID +" AND state=0 ORDER BY NID";
            stmt = con.createStatement();
            rs = stmt.executeQuery(sqlStr);

            while(rs.next())
            {
            	Sysnotify notify = new Sysnotify();
            	notify.setNid(rs.getLong("nid"));
            	notify.setUseraccount(rs.getString("useraccount"));
            	notify.setContext(rs.getString("context"));
            	notify.setTitle(rs.getString("title"));
            	notify.setFilter(rs.getString("filter"));
            	notify.setPriority(rs.getInt("priority"));
            	notify.setState(rs.getInt("state"));
                notify.setNotifytime(new Date(rs.getTimestamp("notifytime").getTime()));
                list.add(notify);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.err(e);
            // 数据库异常，强行退出
            System.exit(0x89);
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
        return list;
    }
}
