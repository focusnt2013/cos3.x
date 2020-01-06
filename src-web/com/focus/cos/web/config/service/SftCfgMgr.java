package com.focus.cos.web.config.service;

import org.json.JSONObject;

/**
 * 组件管理
 * @author focus
 *
 */
public class SftCfgMgr extends CfgMgr
{
	private static JSONObject config;
	public static String id = "software";
	
	public String modifyProperty(String key, String value, String label)
	{
		return modifyProperty(config, id, key, value, label);
	}
	
	/**
	 * 
	 * @return
	 */
	public static JSONObject getConfig()
	{
		if( config != null ) return config;
		return config = getConfig(id);
	}
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static String get(String key)
	{
		if( config == null ) getConfig();
		if( config != null )
		{
			return config.has(key)?config.getString(key):"";
		}
		return "";
	}
}
