package com.focus.cos.api;

import org.json.JSONObject;

public class SyslogClient
{
	public static void submit(Syslog log)
	{
		try
		{
			write(log);
		}
		catch(Exception e)
		{
		}
	}
	/**
	 * 写日志
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param log
	 * @throws Exception
	 */
	public static long write(Syslog log) throws Exception
	{
		byte[] payload = ApiUtils.doPost("api/log", log);
		JSONObject json = new JSONObject(new String(payload, "UTF-8"));
		if( json.has("error") )
		{
			throw new Exception(json.getString("error"));
		}
		if( json.has("id") )
		{
			log.setLogid(json.getLong("id"));
			return json.getLong("id");
		}
		return 0L;
	}

	/**
	 * 写日志
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param type
	 * @param severity
	 * @param text
	 * @param account
	 * @param category
	 * @param context
	 * @param contextlink
	 * @return
	 */
	public static Syslog write(
			LogType type,
			LogSeverity severity,
			String text,
			String account,
			String category,
			String context,
			String contextlink)
	{
		Syslog log = new Syslog();
		try
		{
			log.setAccount(account);
			log.setCategory(category);
			log.setContext(context);
			log.setContextlink(contextlink);
			log.setLogtext(text);
			log.setLogseverity(severity.getValue());
			log.setLogtype(type.getValue());
			write(log);
		}
		catch(Exception e)
		{
		}
		return log;
	}
}
