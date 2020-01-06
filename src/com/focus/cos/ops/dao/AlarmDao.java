package com.focus.cos.ops.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.focus.cos.api.Sysalarm;
import com.focus.sql.Dao;
import com.focus.util.Log;

public class AlarmDao extends Dao
{
    public void prepareInsert() throws SQLException
    {
        super.setPrepareStatement( "INSERT INTO TB_SYSALARM(ALARMID, ORGSEVERITY, ORGTYPE, MODULE, DN, ID, PROBABLECAUSE, ALARMTITLE,"
        		+ " ALARMTEXT, EVENTTIME, ACTIVESTATUS, RESPONSER, CONTACT, SERVERKEY)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", true );
    }

	public void prepareUpdate() throws SQLException
    {
        super.setPrepareStatement( "UPDATE TB_SYSALARM SET PROBABLECAUSE=?, ALARMTEXT=?, EVENTTIME=?, FREQUENCY=?, DN=?, ACTIVESTATUS=?, RESPONSER=?, CONTACT=? WHERE ALARMID=?", true );
    }
	
	public void prepareConfirm() throws SQLException
    {
        super.setPrepareStatement( "UPDATE TB_SYSALARM SET ACKUSER=?, ACKREMARK=?, ACKTIME=?, CLEARTIME=?,FREQUENCY=?,ACTIVESTATUS=? WHERE ALARMID=?", false );
    }

	public void prepareNotify() throws SQLException
    {
        super.setPrepareStatement( "UPDATE TB_SYSALARM SET CLEARTIME=?,FREQUENCY=?,ACTIVESTATUS=? WHERE ALARMID=?", false );
    }

	public void update( Sysalarm alarm ) throws SQLException
	{
        int j = 1;
        pstmt.setString( j++, alarm.getCause() );
        pstmt.setString( j++, alarm.getText() );
        pstmt.setTimestamp( j++, new Timestamp(alarm.getEventTime().getTime()) );
        pstmt.setInt( j++ , alarm.getFrequnce());
        pstmt.setString( j++, alarm.getDn() );
        pstmt.setInt( j++ , alarm.getActiveStatus());
        pstmt.setString( j++, alarm.getResponser() );
        pstmt.setString( j++, alarm.getContact() );
        pstmt.setLong( j++ , alarm.getAlarmid());
	}
	/**
	 * 记录通知的时间
	 */
	public void notified(long alarmid, int frequency, int status)
	{
		try
		{
	        int j = 1;
	        pstmt.setTimestamp( j++, new Timestamp(System.currentTimeMillis()) );
	        pstmt.setInt( j++, frequency );
	        pstmt.setInt( j++, status );
	        pstmt.setLong( j++, alarmid );
	        this.addBatch();
		}
		catch (SQLException e)
		{
			Log.err("Failed to notified", e);
		}
	}
	/**
	 * 确认告警
	 * @param alarm
	 * @throws SQLException
	 */
	public void confirm( Sysalarm alarm ) throws SQLException
	{
        int j = 1;
        pstmt.setString( j++, alarm.getAckUser() );
        pstmt.setString( j++, alarm.getAckRemark() );
        pstmt.setTimestamp( j++, new Timestamp(alarm.getAckTime().getTime()) );
        pstmt.setTimestamp( j++, new Timestamp(alarm.getClearTime().getTime()) );
        pstmt.setInt( j++ , alarm.getFrequnce());
        pstmt.setInt( j++ , alarm.getActiveStatus());
        pstmt.setLong( j++ , alarm.getAlarmid());
        this.addBatch();
	}
    /**
     * 保存数据到SYSALARM表
     * @param sysAlarm
     */
    public void save( Sysalarm alarm ) throws SQLException
    {
        int j = 1;
        pstmt.setLong(j++, alarm.getAlarmid());
        pstmt.setString( j++, alarm.getSeverity()!=null?alarm.getSeverity():" " );
        pstmt.setString( j++, alarm.getType()!= null?alarm.getType():" " );
        pstmt.setString( j++, alarm.getSysid() );
        pstmt.setString( j++, alarm.getDn()!= null?alarm.getDn():" " );
        pstmt.setString( j++, alarm.getId() );
        pstmt.setString( j++, alarm.getCause() );
        pstmt.setString( j++, alarm.getTitle() );
        pstmt.setString( j++, alarm.getText() );
        pstmt.setTimestamp( j++, new Timestamp(alarm.getEventTime().getTime()) );
        pstmt.setInt( j++, alarm.getActiveStatus() );
        pstmt.setString( j++, alarm.getResponser() );
        pstmt.setString( j++, alarm.getContact() );
        pstmt.setString( j++, alarm.getServerkey() );
    }

    /**
     * 自动关闭指定告警
     * @param module
     * @param id
     * @param ci
     * @param remark
     * @return
     */
    public List<Sysalarm> close(
    		String module,
    		String id,
    		String serverkey,
    		String remark) throws SQLException
    {
    	List<Sysalarm> list = query(null, null, module, id, serverkey, "-1", null);
		prepareConfirm();
    	for(Sysalarm e : list)
    	{
    		e.setAckRemark(remark);
    		e.setAckTime(new Date());
    		e.setAckUser("");
    		e.setActiveStatus(0);
    		this.confirm(e);
    	}
    	
    	if( !list.isEmpty() )
    	{
    		this.execute();
    		commit();
    	}
    	return list;
    }

    /**
     * 确认指定告警
     * @param alarms
     * @param ackuser
     * @param remark
     * @return
     * @throws SQLException
     */
    public List<Sysalarm> confirm(
    		String alarms,
    		String ackuser,
    		String remark) throws SQLException
    {
    	List<Sysalarm> list = query(null, null, null, null, null, "-1", alarms);
		prepareConfirm();
    	for(Sysalarm e : list)
    	{
    		e.setAckRemark(remark);
    		e.setAckTime(new Date());
    		e.setAckUser(ackuser);
    		e.setActiveStatus(0);
    		this.confirm(e);
    	}
    	if( !list.isEmpty() )
    	{
    		this.batch();
        	commit();
    	}
    	return list;
    }

    /**
     * 确认指定告警
     * @param alarms
     * @param ackuser
     * @param remark
     * @return
     * @throws SQLException
     */
    public List<Sysalarm> clear(String alarms) throws SQLException
    {
    	List<Sysalarm> list = query(null, null, null, null, null, "0", alarms);
		prepareConfirm();
    	for(Sysalarm e : list)
    	{
    		e.setClearTime(new Date());
    		e.setActiveStatus(1);
    		this.confirm(e);
    	}
    	if( !list.isEmpty() )
    	{
    		this.execute();
    		commit();
    	}
    	return list;
    }
    
    /**
     * 
     * @return
     */
    public List<Sysalarm> getAllUnackAlarms()
    {
    	return query(null, null, null, null, null, "-1", null);
    }
    /**
     * 查看告警数据
     * @param severity
     * @param type
     * @param module
     * @param id
     * @param serverkey
     * @param status
     * @param alarms
     * @return
     */
    public List<Sysalarm> query(
    		String severity,
    		String type,
    		String module,
    		String id,
    		String serverkey,
    		String status,
    		String alarms)
    {
        List<Sysalarm> list = new ArrayList<Sysalarm> ();
//        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;   
        Sysalarm alarm = null;
        try
        {
//            String dbDriver = System.getProperty("cos.jdbc.driver", "");
//            String dbUser = System.getProperty("cos.jdbc.user", "");
//            String dbPassword = System.getProperty("cos.jdbc.password", "");
//            String dbUrl = System.getProperty("cos.jdbc.url", "");
//            Class.forName(dbDriver).newInstance();
//            con = DriverManager.getConnection(dbUrl ,dbUser, dbPassword);
//            stmt = getConnection().createStatement();
        	String sqlStr = "SELECT * FROM TB_SYSALARM";
        	String sqlWhere = "";
        	if( severity != null )
        	{
        		sqlWhere += " AND ORGSEVERITY='"+severity+"'";
        	}
        	if( type != null )
        	{
        		sqlWhere += " AND ORGTYPE='"+type+"'";
        	}
        	if( module != null )
        	{
        		sqlWhere += " AND MODULE='"+module+"'";
        	}
        	if( serverkey != null )
        	{
        		sqlWhere += " AND SERVERKEY='"+serverkey+"'";
        	}
        	if( id != null )
        	{
        		sqlWhere += " AND ID='"+id+"'";
        	}
        	if( status != null )
        	{
        		sqlWhere += " AND ACTIVESTATUS=" + status;
        	}
        	if( alarms != null )
        	{
        		sqlWhere += " AND alarmid IN ("+ alarms +") ";
        	}
        	if( !sqlWhere.isEmpty() )
        	{
        		sqlStr += " WHERE" + sqlWhere.substring(4);
        	}
            stmt = getConnection().createStatement();
            rs = stmt.executeQuery( sqlStr );
            while( rs.next() )
            {
                alarm = new Sysalarm();
                alarm.setAlarmid(rs.getLong("ALARMID"));
                alarm.setEventTime(rs.getTimestamp("EVENTTIME"));
                alarm.setClearTime(rs.getTimestamp("CLEARTIME"));
                alarm.setDn(rs.getString("DN"));
                alarm.setSeverity(rs.getString("ORGSEVERITY"));
                alarm.setType(rs.getString("ORGTYPE"));
                alarm.setActiveStatus(rs.getInt("ACTIVESTATUS"));
                alarm.setCause(rs.getString("PROBABLECAUSE"));
                alarm.setTitle(rs.getString("ALARMTITLE"));
                alarm.setSysid(rs.getString("MODULE"));
                alarm.setId(rs.getString("ID"));
                alarm.setFrequnce(rs.getInt("FREQUENCY"));
                alarm.setText(rs.getString("ALARMTEXT"));
                alarm.setResponser(rs.getString("RESPONSER"));
                list.add(alarm);
            }
        }
        catch( Exception e )
        {
        	if( alarm != null )
        	{
        		Log.err("Failed to load data for alarm("+alarm.getAlarmid()+", "+alarm.getSysid()+", "+alarm.getId()+", "+alarm.getSeverity()+", "+alarm.getTitle()+")", e);
        	}
        	else Log.err( e );
        }
        finally
        {
        	try
            {
                if( rs != null ) rs.close();
                if( stmt != null ) stmt.close();
//                if( con != null ) con.close();
            }
            catch (SQLException ex)
            {
            }
        }
        return list;
    }

    /**
     * 查找告警记录
     * @param module
     * @param dn
     * @param id
     * @param severity
     * @param type
     * @param title
     * @return
     */
    public Sysalarm find(String module, String serverkey, String id, String severity, String type, String title)
    {
    	Sysalarm alarm = null;
        Statement stmt = null;
        ResultSet rs = null;   
        String sqlStr = "SELECT * FROM TB_SYSALARM WHERE (ACTIVESTATUS=-1 OR (ACTIVESTATUS=0 AND ACKUSER='') )";
        try
        {
        	if( module != null )
        	{
        		sqlStr += " AND MODULE='" + module+"'";
        	}
        	if( title != null )
        	{
        		sqlStr += " AND ALARMTITLE='"+title+"'";
        	}
        	if( serverkey != null )
        	{
        		sqlStr += " AND SERVERKEY='"+serverkey+"'";
        	}
        	if( id != null )
        	{
        		sqlStr += " AND ID='"+id+"'";
        	}
        	if( severity != null )
        	{
        		sqlStr += " AND ORGSEVERITY='"+severity+"'";
        	}
        	if( type != null )
        	{
        		sqlStr += " AND ORGTYPE='"+type+"'";
        	}
//            String dbDriver = System.getProperty("cos.jdbc.driver", "");
//            String dbUser = System.getProperty("cos.jdbc.user", "");
//            String dbPassword = System.getProperty("cos.jdbc.password", "");
//            String dbUrl = System.getProperty("cos.jdbc.url", "");
//            Class.forName(dbDriver).newInstance();
//            con = DriverManager.getConnection(dbUrl ,dbUser, dbPassword);
            stmt = this.getConnection().createStatement();
            rs = stmt.executeQuery( sqlStr );
            StringBuffer sql = new StringBuffer();
            while( rs.next() )
            {
            	if( alarm == null ){
            		alarm = new Sysalarm();
            		alarm.setAlarmid(rs.getLong("ALARMID"));
            		alarm.setActiveStatus(rs.getInt("ACTIVESTATUS"));
            		alarm.setFrequnce(rs.getInt("FREQUENCY"));
            		continue;
            	}
//            	sb.append("\r\n\tFound "+alarm.getAlarmid());
            	if( sql.length() > 0 ){
            		sql.append(",");
            	}
            	sql.append(rs.getLong("ALARMID"));
            }
            if( sql.length() > 0 ){
            	sqlStr = sql.toString();
            	int r = stmt.executeUpdate("DELETE FROM TB_SYSALARM WHERE ALARMID IN ("+sqlStr+")");
            	Log.war("Found "+r+" alarms repeated have been deleted.");
            }
//            Log.war(sb.toString());
        }
        catch( Exception e )
        {
    		Log.err("Failed to find alarm by sql["+sqlStr+"]", e);
        }
        finally
        {
        	try
            {
                if( rs != null ) rs.close();
                if( stmt != null ) stmt.close();
            }
            catch (SQLException ex)
            {
            }
        }
        return alarm;
    }
}
