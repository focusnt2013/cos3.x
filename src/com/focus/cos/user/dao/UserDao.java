package com.focus.cos.user.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.focus.cos.api.Sysuser;
import com.focus.sql.ConnectionPool;
import com.focus.sql.Dao;
import com.focus.util.Log;

public class UserDao extends Dao
{
    public void prepareInsert() throws SQLException
    {
        super.setPrepareStatement( "INSERT INTO TB_USER(id, roleid, username, password, realname, email, status, sex, creator)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)", true );
    }
	
    /**
     * 保存数据到SYSALARM表
     *
     * @param sysAlarm
     */
    public void save( Sysuser user ) throws SQLException
    {
        int j = 1;
        pstmt.setInt( j++, user.getId() );
        pstmt.setInt( j++, user.getRoleid() );
        pstmt.setString( j++, user.getUsername() );
        pstmt.setString( j++, user.getPassword() );
        pstmt.setString( j++, user.getRealname() );
        pstmt.setString( j++, user.getEmail() );
        pstmt.setInt( j++, user.getStatus() );
        pstmt.setInt( j++, user.getSex() );
        pstmt.setString( j++, user.getCreator() );
    }

    /**
     * 得到指定用户的角色ID
     * @param username
     * @return
     */
    public static int getRoleid(String username)
    {
        Connection con = ConnectionPool.getInstance().getConnection();
        if(con == null )
        {
            Log.war("Failed to auth the user for connection pool null.");
            return -1;
        }
        Statement stmt = null;
        ResultSet rs = null;   
        try
        {
        	String sqlStr = "SELECT roleid FROM TB_USER WHERE username='"+username+"'";
            stmt = con.createStatement();
            rs = stmt.executeQuery( sqlStr );
            if( rs.next() )
            {
            	return rs.getInt(1);
            }
            else
            {
                return 0;
            }
        }
        catch( Exception e )
        {
    		Log.err("Failed to load data for user", e);
    		return -2;
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
    }
    /**
     * 校验用户
     * @param username
     * @param password
     * @return
    public static boolean auth(String username, String password)
    {
        Connection con = ConnectionPool.getInstance().getConnection();
        if(con == null )
        {
            Log.war("Failed to auth the user for connection pool null.");
            return false;
        }
        Statement stmt = null;
        ResultSet rs = null;   
        try
        {
        	String sqlStr = "SELECT count(id) FROM TB_USER username='"+username+"' and password='"+password+"'";
            stmt = con.createStatement();
            rs = stmt.executeQuery( sqlStr );
            if( rs.next() )
            {
            	return rs.getInt(1)==1;
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
        return false;
    }
     */
    
    /**
     * 查询指定条件的用户数据
     * @return
     */
    public static List<Sysuser> query(String roleid, String status, String sex, String username, String creator)
    {
        List<Sysuser> list = new ArrayList<Sysuser> ();
        Connection con = ConnectionPool.getInstance().getConnection();
        if(con == null )
        {
            Log.war("Failed to query the users for connection pool null.");
            return list;
        }
        Statement stmt = null;
        ResultSet rs = null;   
        Sysuser user = null;
        try
        {
        	String sqlStr = "SELECT id, roleid, username, realname, email, status, sex, creator FROM TB_USER";
        	String sqlWhere = "";
        	if( roleid != null )
        	{
        		sqlWhere += " AND roleid=" + roleid;
        	}
        	if( status != null )
        	{
        		sqlWhere += " AND status=" + status;
        	}
        	if( sex != null )
        	{
        		sqlWhere += " AND sex=" + sex;
        	}
        	if( username != null )
        	{
        		sqlWhere += " AND username='"+username+"'";
        	}
        	if( creator != null )
        	{
        		sqlWhere += " AND creator='"+creator+"'";
        	}
        	if( !sqlWhere.isEmpty() )
        	{
        		sqlStr += " WHERE" + sqlWhere.substring(4);
        	}
            stmt = con.createStatement();
            rs = stmt.executeQuery( sqlStr );
            StringBuffer sb = new StringBuffer(sqlStr);
            while( rs.next() )
            {
            	user = new Sysuser();
            	user.setId(rs.getInt("id"));
            	user.setRoleid(rs.getByte("roleid"));
                user.setUsername(rs.getString("username"));
                user.setRealname(rs.getString("realname"));
                user.setEmail(rs.getString("email"));
                user.setStatus(rs.getByte("status"));
                user.setSex(rs.getByte("sex"));
                user.setCreator(rs.getString("creator"));
                list.add(user);
                sb.append("\r\n\t"+user.getUsername()+"\t"+user.getRealname());
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
        return list;
    }
}
