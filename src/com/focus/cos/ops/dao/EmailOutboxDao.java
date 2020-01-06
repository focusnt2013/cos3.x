package com.focus.cos.ops.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.focus.cos.api.Sysemail;
import com.focus.sql.ConnectionPool;
import com.focus.sql.Dao;
import com.focus.util.Log;

public class EmailOutboxDao extends Dao
{
    public void prepareInsert() throws SQLException
    {
        super.setPrepareStatement( "INSERT INTO TB_EMAIL_OUTBOX(EID, REQUEST_TIME, MAIL_TO, MODULE, CC, BCC, SUBJECT, CONTENT, STATE, ATTACHMENTS)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", true );
    }
    
	public void prepareUpdate() throws SQLException
    {
        super.setPrepareStatement( "UPDATE TB_EMAIL_OUTBOX SET STATE=?, RESULT=? WHERE EID=?", false );
    }

    /**
     * 删除时间前的数据
     * @param time
     * @throws SQLException
     */
    public boolean delete(long time) throws SQLException{
    	PreparedStatement stmt = this.getConnection().prepareStatement("DELETE FROM TB_EMAIL_OUTBOX WHERE REQUEST_TIME<?");
    	stmt.setTimestamp( 1, new Timestamp(time) );
    	boolean b = stmt.execute();
    	stmt.close();
    	return b;
    }
    /**
     *
     */
    public void save( Sysemail email ) throws SQLException
    {
        int j = 1;
        pstmt.setLong( j++, email.getEid() );
        Timestamp nullTimestamp = new Timestamp(0);
        if( email.getRequestTime() != null )
        {
            pstmt.setTimestamp( j++, new Timestamp(email.getRequestTime().getTime()) );
        }
        else
        {
            pstmt.setTimestamp( j++, nullTimestamp );        	
        }
        pstmt.setString( j++, email.getMailTo() );
        pstmt.setString( j++, email.getSysid());
        pstmt.setString( j++, email.getCc() );
        pstmt.setString( j++, email.getBcc() );
        pstmt.setString( j++, email.getSubject() );
        pstmt.setString( j++, email.getContent() );
        pstmt.setInt( j++, email.getState() );
        pstmt.setString( j++, email.getAttachments() );
    }
    
	public void update( long eid, int state, String result ) throws SQLException
    {
        pstmt.setInt( 1, state );
        pstmt.setString( 2, result );
        pstmt.setLong( 3, eid );
        pstmt.addBatch();
    }
	
	public static List<Sysemail> loadOutboxEmail()
    {
        List<Sysemail> list = new ArrayList<Sysemail> ();
        Connection con = ConnectionPool.getInstance().getConnection();
        if(con == null )
        {
            Log.war("Failed to loadOutboxEmail for connection pool null.");
            return list;
        }
        Statement stmt = null;
        ResultSet rs = null;   
        try
        {
        	String sqlStr = "SELECT * FROM TB_EMAIL_OUTBOX WHERE STATE=0 ORDER BY EID LIMIT 300";
            stmt = con.createStatement();
            rs = stmt.executeQuery( sqlStr );
            while( rs.next() )
            {
            	Sysemail outbox = new Sysemail();
                outbox.setEid(rs.getLong("EID"));
                outbox.setRequestTime(rs.getDate("REQUEST_TIME"));
                outbox.setMailTo(rs.getString("MAIL_TO"));
                outbox.setCc(rs.getString("CC"));
                outbox.setBcc(rs.getString("BCC"));
                outbox.setSysid(rs.getString("MODULE"));
                outbox.setSubject(rs.getString("SUBJECT"));
                outbox.setContent(rs.getString("CONTENT"));
                outbox.setState(rs.getShort("STATE"));
                outbox.setAttachments(rs.getString("ATTACHMENTS"));
                outbox.setResult(rs.getString("RESULT"));
                list.add(outbox);
            }
        }
        catch( Exception e )
        {
            Log.err( e );
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
