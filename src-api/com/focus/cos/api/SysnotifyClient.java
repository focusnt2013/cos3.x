package com.focus.cos.api;

import org.json.JSONObject;

public class SysnotifyClient
{
	public static void submit(Sysnotify notify)
	{
		try
		{
			send(notify);
		}
		catch(Exception e)
		{
		}
	}
	
	/**
	 * 发送系统通知
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param notify
	 * @throws Exception
	 */
	public static void send(Sysnotify notify) throws Exception
	{
		byte[] payload = ApiUtils.doPost("api/notify", notify);
		JSONObject json = new JSONObject(new String(payload, "UTF-8"));
		if( json.has("error") )
		{
			throw new Exception(json.getString("error"));
		}
		else if( json.has("id") )
		{
			notify.setNid(json.getLong("id"));
		}
	}
}
