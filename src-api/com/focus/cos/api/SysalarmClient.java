package com.focus.cos.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class SysalarmClient
{
	/**
	 * 提交告警
	 * @param alarm
	 * @return 如果有异常返回异常
	 */
	public static void send(Sysalarm alarm)
	{
		try
		{
			save(alarm);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送告警
	 * @param from 告警是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param alarm 告警的完整对象
	 * @throws Exception
	 */
	public static void save(Sysalarm alarm) throws Exception
	{
		byte[] payload = ApiUtils.doPost("api/alarm", alarm);
		JSONObject json = new JSONObject(new String(payload, "UTF-8"));
		if( json.has("error") )
		{
			throw new Exception(json.getString("error"));
		}
		else if( json.has("id") )
		{
			alarm.setAlarmid(json.getLong("id"));
		}
	}
	
	/**
	 * 发送告警
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param sysid 系统标识,该模块程序属于哪个子系统
	 * @param id 告警的标识
	 * @param severity 告警级别
	 * @param type 告警类型
	 * @param title 告警比
	 * @param text 告警描述
	 * @param cause 告警原因
	 * @return
	public static Sysalarm send(
			String sysid,
			String id,
			String severity,
			String type,
			String title,
			String text,
			String cause)
	{
		try
		{
			Sysalarm alarm = new Sysalarm();
			alarm.setSeverity(severity);
			alarm.setType(type);
			alarm.setTitle(title);
			alarm.setText(text);
			alarm.setCause(cause);
			alarm.setActiveStatus(-1);
			alarm.setSysid(sysid);
			alarm.setId(id);
			save(alarm);
			return alarm;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	 */

	/**
	 * 自动确认告警
	 * @param from 告警是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param sysid 系统标识,该模块程序属于哪个子系统
	 * @param id 告警的标识
	 * @param remark 告警关闭的备注，什么原因关闭
	 */
	public static List<Sysalarm> autoconfirm(String sysid, String id, String remark)
	{
		try
		{
			List<Sysalarm> list = close(sysid, id, remark);
			return list;
//			StringBuffer sb = new StringBuffer("Succeed to auto confirm "+list.size()+" alsrms:");
//			for(Sysalarm e : list)
//			{
//				sb.append("\r\n\t["+e.getAlarmid()+"] "+e.getTitle());
//			}
//			Log.msg(sb.toString());
		}
		catch(Exception e)
		{
			return null;
//			Log.err("Failed to auto confirm the alarms of "+sysid+"("+id+") for "+e);
		}
	}
	/**
	 * 自动确认关闭告警
	 * @param from 告警是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param sysid 系统标识,该模块程序属于哪个子系统
	 * @param id 告警的标识
	 * @param remark 告警关闭的备注，什么原因关闭
	 */
	public static List<Sysalarm> close(String sysid, String id, String remark) throws Exception
	{
		ArrayList<Sysalarm> list = new ArrayList<Sysalarm>();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("method", "close");
		parameters.put("sysid", sysid);
		parameters.put("id", id);
		parameters.put("remark", remark);
		byte[] payload = ApiUtils.doGet("api/alarm", parameters, false);

		ObjectInputStream ois = null;
		try
		{
			Object obj = null;
			ois = new ObjectInputStream(new ByteArrayInputStream(payload));
			while((obj = ois.readObject())!=null)
			{
				list.add((Sysalarm)obj);
				if( ois.read() != 7 ) break;
			}
		}
		catch (Exception e)
		{
		}
        finally
        {
        	if( ois != null )
	            try
				{
					ois.close();
				}
				catch (IOException e)
				{
				}
        }
		return list;
	}
	/**
	 * 确认指定告警
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param alarms 确认告警的ID，逗号分割
	 * @param ackuser 确认的用户的发起操作的用户
	 * @param remark 确认告警的原因
	 */
	public static List<Sysalarm> confirm(String alarms, String ackuser, String remark) throws Exception
	{
		ArrayList<Sysalarm> list = new ArrayList<Sysalarm>();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("method", "confirm");
		parameters.put("alarms", alarms);
		parameters.put("ackuser", ackuser);
		parameters.put("remark", remark);
		byte[] payload = ApiUtils.doGet("api/alarm", parameters, false);
		ObjectInputStream ois = null;
		try
		{
			Object obj = null;
			ois = new ObjectInputStream(new ByteArrayInputStream(payload));
			while((obj = ois.readObject())!=null)
			{
				list.add((Sysalarm)obj);
				if( ois.read() != 7 ) break;
			}
		}
		catch (Exception e)
		{
			throw e;
		}
        finally
        {
        	if( ois != null )
	            try
				{
					ois.close();
				}
				catch (IOException e)
				{
				}
        }
		return list;
	}
	
	/**
	 * 
	 * @param wsUrl
	 * @param ids
	public static List<Sysalarm> clear(String alarms) throws Exception
	{
		ArrayList<Sysalarm> list = new ArrayList<Sysalarm>();
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("method", "clear");
		parameters.put("alarms", alarms);
		byte[] payload = ApiUtils.doGet("api/alarm", parameters, false);
		ByteArrayInputStream bais = new ByteArrayInputStream(payload);
		Serializable obj = null;
		while((obj = ApiUtils.readSerializables(bais))!=null)
		{
			list.add((Sysalarm)obj);
		}
		return list;
	}
	 */
	/**
	 *  查询系统告警
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param orgSeverity
	 * @param orgType
	 * @param DN
	 * @return
	 */
	public static List<Sysalarm> query(
			AlarmSeverity severity,
			AlarmType type,
			String sysid,
			String dn,
			Boolean activing)
	{
		ArrayList<Sysalarm> alarms = new ArrayList<Sysalarm>();
		ObjectInputStream ois = null;
		try
		{
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("method", "query");
			if( severity != null ) parameters.put("severity", severity.getValue());
			if( type != null ) parameters.put("type", type.getValue());
			if( dn != null ) parameters.put("dn", dn);
			if( sysid != null ) parameters.put("sysid", sysid);
			if( activing != null ) parameters.put("status", activing?"-1":"0");
			byte[] payload = ApiUtils.doGet("api/alarm", parameters, false);

			Object obj = null;
			ois = new ObjectInputStream(new ByteArrayInputStream(payload));
			while((obj = ois.readObject())!=null)
			{
				alarms.add((Sysalarm)obj);
				if( ois.read() != 7 ) break;
			}
		}
		catch (Exception e)
		{
		}
        finally
        {
        	if( ois != null )
	            try
				{
					ois.close();
				}
				catch (IOException e)
				{
				}
        }
		return alarms;
	}
}