package com.focus.cos.api;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.focus.cos.control.COSApi;
import com.focus.cos.ops.dao.AlarmDao;
import com.focus.sql.Dao;
import com.focus.util.IOHelper;
import com.focus.util.Log;

/**
 * 告警处理
 * @author focus
 *
 */
public class AlarmServlet extends AbaseServlet
{
	private static final long serialVersionUID = -6082791442929441606L;
	public AlarmServlet(COSApi server) throws Exception
	{
		super(server);
	}

	@Override
	public void query(HttpServletRequest request, HttpServletResponse response, StringBuffer log) throws Exception 
	{
		String method = COSApi.getRequestValue(request, "method");
		if( method == null || method.isEmpty() )
		{
			unknown(request);
			return;
		}
		String ci = COSApi.getRequestValue(request, "COS-ID");
		log.append("\r\n\tmethod="+method+" "+ci);
		AlarmDao dao = new AlarmDao();
		try
		{
			if( "query".equals(method) )
			{
				String severity = COSApi.getRequestValue(request, "severity");
				String type = COSApi.getRequestValue(request, "type");
				String module = COSApi.getRequestValue(request, "sysid");
				String id = COSApi.getRequestValue(request, "id");
				String status = COSApi.getRequestValue(request, "status");
				log.append("\r\n\t(module="+module+" and dn="+request.getRemoteAddr()+" and id="+id+" and severity="+severity+" and type="+type+" and status="+status+")");
				List<Sysalarm> list = dao.query(severity, type, module, id, ci, status, null);
				Serializable[] args = new Serializable[list.size()];
				list.toArray(args);
				super.write(ci, response, args, log);
			}
			else if( "close".equals(method) )
			{
				String remark = COSApi.getRequestValue(request, "remark");
				String module = COSApi.getRequestValue(request, "sysid");
				String id = COSApi.getRequestValue(request, "id");
				log.append("\r\n\t(module="+module+" and dn="+request.getRemoteAddr()+" and id="+id+") "+remark);
				List<Sysalarm> list = dao.close(module, id, ci, remark);
				Serializable[] args = new Serializable[list.size()];
				list.toArray(args);
				log.append("\r\n\tFound "+list.size()+" alarms are closed.");
				for(Sysalarm alarm : list)
				{
					log.append("\r\n\t\t["+alarm.getAlarmid()+"] "+alarm.getSysid()+" "+alarm.getTitle());
				}
				super.write(ci, response, args, log);
			}
			else if( "confirm".equals(method) )
			{
				String remark = COSApi.getRequestValue(request, "remark");
				String ackuser = COSApi.getRequestValue(request, "ackuser");
				String alarms = COSApi.getRequestValue(request, "alarms");
				log.append("\r\n\t(alarms="+alarms+" and ackuser="+ackuser+") "+remark);
				List<Sysalarm> list = dao.confirm(alarms, ackuser, remark);
				Serializable[] args = new Serializable[list.size()];
				list.toArray(args);
				for(Sysalarm alarm : list)
				{
					log.append("\r\n\t["+alarm.getAlarmid()+"] "+alarm.getSysid()+" "+alarm.getTitle());
				}
				super.write(ci, response, args, log);
			}
			else if( "clear".equals(method) )
			{
				String alarms = COSApi.getRequestValue(request, "alarms");
				dao.prepareConfirm();
				List<Sysalarm> list = dao.clear(alarms);
				Serializable[] args = new Serializable[list.size()];
				list.toArray(args);
				for(Sysalarm alarm : list)
				{
					log.append("\r\n\t["+alarm.getAlarmid()+"] "+alarm.getSysid()+" "+alarm.getTitle());
				}
				super.write(ci, response, args, log);
			}
			else
			{
				unknown(request);
			}
		}
		catch(Exception e)
		{
			throw e;
		}
		finally 
		{
			dao.close();
		}
	}

	@Override
	public synchronized void save(HttpServletRequest request, byte[] payload, JSONObject response) throws Exception  
	{
		Serializable s = IOHelper.readSerializableNoException(payload);
		String ci = COSApi.getRequestValue(request, "COS-ID");
		if( s != null && s instanceof Sysalarm )
		{
			Sysalarm alarm = (Sysalarm)s;
			if( alarm.getTitle() == null || alarm.getTitle().isEmpty() )
			{
				throw new Exception("没有设置系统告警的标题(title)");
			}
			if( alarm.getSysid() == null || alarm.getSysid().isEmpty() )
			{
				throw new Exception("没有设置系统告警的模块(module)");
			}
			if( alarm.getId() == null || alarm.getId().isEmpty() )
			{
				throw new Exception("没有设置系统告警的标识(id)");
			}
			if( alarm.getText() == null || alarm.getText().isEmpty() )
			{
				throw new Exception("没有设置系统告警的内容(text)");
			}
			if( alarm.getSeverity() == null )
			{
				throw new Exception("没有设置系统告警的级别(severity)");
			}
			if( alarm.getType() == null )
			{
				throw new Exception("没有设置系统告警的类型(type)");
			}
			long alarmid = 0;
			AlarmDao dao = new AlarmDao();
			long ts = System.currentTimeMillis();
			try
			{
				if( alarm.getCause() == null )
					alarm.setCause("");
				alarm.setEventTime(Calendar.getInstance().getTime());
				alarm.setDn(request.getRemoteAddr());
				if("127.0.0.1".equals(alarm.getDn())){
					alarm.setDn(COSApi.LocalIp);
				}
				alarm.setServerkey(ci);
				Sysalarm old = dao.find(alarm.getSysid(), alarm.getServerkey(), alarm.getId(), alarm.getSeverity(), alarm.getType(), alarm.getTitle());
				if( old != null )
				{
					alarmid = old.getAlarmid();
					alarm.setAlarmid(alarmid);
					alarm.setFrequnce(old.getActiveStatus()==-1?old.getFrequnce():0);
					dao.prepareUpdate();
					dao.update(alarm);
					response.put("reset", true);
					response.put("dn", alarm.getDn());
					response.put("title", alarm.getTitle());
					response.put("id", alarmid);
				}
				else
				{
					long id = Dao.getMaxId("TB_SYSALARM", "ALARMID") + 1;
					alarm.setAlarmid(id);
					dao.prepareInsert();
					dao.save(alarm);
					response.put("title", alarm.getTitle());
					response.put("dn", alarm.getDn());
					response.put("reset", false);
				}
				int result = dao.execute();
				if( result != 1 )
				{
					response.put("error", "发送系统告警没生效");
				}
				else if( old == null )
				{
					response.put("id", alarm.getAlarmid());
				}
			}
			catch(Exception e)
			{
				ts = System.currentTimeMillis() - ts;
				Log.err("Failed to execute("+alarmid+", dur: "+ts+"ms):", e);
				throw new Exception("执行告警记录保存操作出现异常");
			}
			finally
			{
				dao.close();
			}
		}
		else
		{
			response.put("error", "Unknown alarm("+s+").");
		}
	}
}
