package com.focus.cos.ops.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.focus.cos.api.Syslog;
import com.focus.sql.Dao;

public class LogDao extends Dao
{
	
    public void prepareInsert() throws SQLException
    {
        super.setPrepareStatement( "INSERT INTO TB_SYSLOG(logid, logtype, logseverity, logtime, logtext, context, contextlink, account, category)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)", true );
    }
    
    /**
     * 删除时间前的数据
     * @param time
     * @throws SQLException
     */
    public boolean delete(long time) throws SQLException{
    	PreparedStatement stmt = this.getConnection().prepareStatement("DELETE FROM TB_SYSLOG WHERE logtime<?");
    	stmt.setTimestamp( 1, new Timestamp(time) );
    	boolean b = stmt.execute();
    	stmt.close();
    	return b;
    }
    /**
     *
     */
    public void save( Syslog log ) throws SQLException
    {
        int j = 1;
        pstmt.setLong( j++, log.getLogid() );
        pstmt.setInt( j++, log.getLogtype() );
        pstmt.setInt( j++, log.getLogseverity() );
        Timestamp nullTimestamp = new Timestamp(0);
        if( log.getLogtime() != null )
        {
            pstmt.setTimestamp( j++, new Timestamp(log.getLogtime().getTime()) );
        }
        else
        {
            pstmt.setTimestamp( j++, nullTimestamp );        	
        }
        pstmt.setString( j++, log.getLogtext() );
        pstmt.setString( j++, log.getContext() );
        pstmt.setString( j++, log.getContextlink() );
        pstmt.setString( j++, log.getAccount() );
        pstmt.setString( j++, log.getCategory() );
    }
}
