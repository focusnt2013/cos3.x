package com.focus.cos.api;

import java.io.OutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.focus.cos.control.COSApi;
import com.focus.cos.ops.dao.LogDao;
import com.focus.sql.Dao;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;

public class LogServlet extends AbaseServlet
{
	private static final long serialVersionUID = -6082791442929441606L;
	private LogDao dao = null;
	private long lastDeleteSyslog;//上次删除系统日志的时间
	private Thread threadDeleteSyslog = null;
	public LogServlet(COSApi server) throws Exception
	{
		super(server);
		if( !this.server.isProxy() )
		{
			dao = new LogDao();
			dao.prepareInsert();
		}
	}

	@Override
	public void query(HttpServletRequest request, HttpServletResponse response, StringBuffer log) throws Exception 
	{
		response.setStatus(404);
		OutputStream out = response.getOutputStream();
		out.write("请求的服务并不存在".getBytes());
		out.flush();
		out.close();
		log.append("\r\n\t404 not found service.");
	}

	@Override
	public synchronized void save(HttpServletRequest request, byte[] payload, JSONObject response) throws Exception  
	{
		Serializable s = IOHelper.readSerializableNoException(payload);
		if( s != null && s instanceof Syslog )
		{
			Syslog log = (Syslog)s;
			if( log.getLogtext() == null || log.getLogtext().isEmpty() )
			{
				throw new Exception("没有设置系统日志的内容(text)");
			}
			if( log.getLogseverity() == null )
			{
				throw new Exception("没有设置系统日志的级别(severity)");
			}
			if( log.getLogtype() == null )
			{
				throw new Exception("没有设置系统日志的类型(type)");
			}
			if( log.getAccount() == null || log.getAccount().isEmpty() )
			{
				throw new Exception("没有设置系统日志的账户(account)");
			}
			if( log.getCategory() == null || log.getCategory().isEmpty() )
			{
				throw new Exception("没有设置系统日志的分类(category)");
			}
			long id = Dao.getMaxId("TB_SYSLOG", "logid") + 1;
			log.setLogid(id);
			log.setLogtime(Calendar.getInstance().getTime());
			dao.save(log);
			int r = dao.execute();
			if( r == 1 )
			{
				response.put("id", id);
			}
			else
			{
				response.put("error", "记录系统日志没生效");
			}
		}
		else
		{
			response.put("error", "Unknown syslog("+s+").");
		}
		if( System.currentTimeMillis() - lastDeleteSyslog > Tools.MILLI_OF_HOUR ) {
			//每小时清理一次
			if( threadDeleteSyslog == null ){
				threadDeleteSyslog = new Thread(){
					public void run(){
						lastDeleteSyslog = System.currentTimeMillis();
						try{
							String path = "/cos/config/system";
							JSONObject syscfg = server.getZookeeper().getJSONObject(path);
							if( syscfg != null ){
								int days = 30;
								if( syscfg.has("SyslogDays") ){
									days = Integer.parseInt(syscfg.getString("SyslogDays"));
								}
								boolean b = dao.delete(lastDeleteSyslog-days*Tools.MILLI_OF_DAY);
								Log.err("Succeed to execute delete data from TB_SYSLOG for result "+b);
							}
							else{
								Log.err("Not found the config of system from "+path);
							}
						}
						catch(Exception e){
							Log.err("Failed to delete data from TB_SYSLOG", e);
						}
						threadDeleteSyslog = null;
					}
				};
				threadDeleteSyslog.start();
			}
		}
	}
	
	/**
	 * 
	 * @param log
	 */
	public synchronized void write(Syslog log)
	{
		try 
		{
			long id = Dao.getMaxId("TB_SYSLOG", "logid") + 1;
			log.setLogid(id);
			log.setLogtime(Calendar.getInstance().getTime());
			dao.save(log);
			dao.execute();
		}
		catch (SQLException e) 
		{
			Log.err("Failed to write log.", e);
		}
	}
}
