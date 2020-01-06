package com.focus.cos.api;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.focus.cos.control.COSApi;
import com.focus.cos.ops.dao.EmailOutboxDao;
import com.focus.sql.Dao;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;

public class EmailServlet extends AbaseServlet
{
	private static final long serialVersionUID = -6082791442929441606L;
	private EmailOutboxDao dao = null;
	private long lastDeleteOutbox;//上次删除系统邮件的时间
	private Thread threadDeleteOutbox = null;
	public EmailServlet(COSApi server) throws Exception
	{
		super(server);
		if( !this.server.isProxy() )
		{
			dao = new EmailOutboxDao();
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
		if( s != null && s instanceof Sysemail )
		{
			Sysemail outbox = (Sysemail)s;
			if( outbox.getSubject() == null || outbox.getSubject().isEmpty() )
			{
				throw new Exception("没有设置系统邮件的标题(subject)");
			}
			if( outbox.getContent() == null || outbox.getContent().isEmpty() )
			{
				throw new Exception("没有设置系统邮件的内容(content)");
			}
			if( outbox.getMailTo() == null || outbox.getMailTo().isEmpty() || outbox.getMailTo().indexOf("@") == -1 )
			{
				throw new Exception("没有设置系统邮件的收件地址或邮箱格式不正确(mailTo)");
			}
			try
			{
				long id = Dao.getMaxId("TB_EMAIL_OUTBOX", "EID") + 1;
				outbox.setEid(id);
				outbox.setRequestTime(Calendar.getInstance().getTime());
				dao.save(outbox);
				int result = dao.execute();
				if( result == 1 )
				{
					response.put("id", id);
				}
				else
				{
					response.put("error", "发送系统邮件没生效");
				}
			}
			catch(Exception e)
			{
				Log.err("Failed to execute:", e);
				throw new Exception("执行邮件记录保存操作出现异常["+e.getMessage()+"]");
			}
			if( System.currentTimeMillis() - lastDeleteOutbox > Tools.MILLI_OF_HOUR ) {
				//每小时清理一次
				if( threadDeleteOutbox == null ){
					threadDeleteOutbox = new Thread(){
						public void run(){
							lastDeleteOutbox = System.currentTimeMillis();
							try{
								String path = "/cos/config/system";
								JSONObject syscfg = server.getZookeeper().getJSONObject(path);
								if( syscfg != null ){
									int days = 30;
									if( syscfg.has("SysemailDays") ){
										days = Integer.parseInt(syscfg.getString("SysemailDays"));
									}
									boolean b = dao.delete(lastDeleteOutbox-days*Tools.MILLI_OF_DAY);
									Log.err("Succeed to execute delete data from TB_EMAIL_OUTBOX for result "+b);
								}
								else{
									Log.err("Not found the config of system from "+path);
								}
							}
							catch(Exception e){
								Log.err("Failed to delete data from TB_EMAIL_OUTBOX", e);
							}
							threadDeleteOutbox = null;
						}
					};
					threadDeleteOutbox.start();
				}
			}
		}
		else
		{
			response.put("error", "Unknown email("+s+").");
		}
	}
}
