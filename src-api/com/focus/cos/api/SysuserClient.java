package com.focus.cos.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class SysuserClient
{
	/**
	 * 新增用户归属到操作用户的权限下
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param user 新增的用户
	 * @throws Exception
	 */
	public static void add(Sysuser user) throws Exception
	{
//		Map<String, String> parameters = new HashMap<String, String>();
//		parameters.put("oper", oper);
		byte[] payload = ApiUtils.doPost("api/user", user);
		JSONObject json = new JSONObject(new String(payload, "UTF-8"));
		if( json.has("error") )
		{
			throw new Exception(json.getString("error"));
		}
		else if( json.has("id") )
		{
			user.setId(json.getInt("id"));
		}
	}
	
	/**
	 * 获得指定用户的角色
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param username 查询指定用户角色的用户名称
	 * @return
	 * @throws Exception
	 */
	public static JSONObject getRole(String username) throws Exception
	{
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("method", "getRole");
		parameters.put("username", username);
		byte[] payload = ApiUtils.doGet("api/user", parameters, false);
		JSONObject json = new JSONObject(new String(payload, "UTF-8"));
		if( json.has("error") )
		{
			throw new Exception(json.getString("error"));
		}
		return json;
	}

	/**
	 * 罗列所有用户，包括admin用户
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 */
	public static ArrayList<Sysuser> list()
	{
		return listUser(-1, -1, -1, null);
	}
	/**
	 * 
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param role -1表示取所有角色
	 * @param sex -1表示取所有性别用户, 1表示男, 2表示女, 3表示未知
	 * @param status -1表示取所有用户, 1表示有效, 表示暂停
	 * @return
	 */
	public static ArrayList<Sysuser> listUser(String creator)
	{
		return listUser(-1, -1, 1, creator);
	}
	public static ArrayList<Sysuser> listUser(int role, int sex, int status)
	{
		return listUser(role, sex, status, null);
	}
	public static ArrayList<Sysuser> listUser(int role, int sex, int status, String creator)
	{
		ArrayList<Sysuser> users = new ArrayList<Sysuser>();
		ObjectInputStream ois = null;
		try
		{
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("method", "listUser");
			if( role != -1 ) parameters.put("roleid", String.valueOf(role));
			if( sex != -1 ) parameters.put("sex", String.valueOf(sex));
			if( status != -1 ) parameters.put("status", String.valueOf(status));
			if( creator != null && !creator.isEmpty() ) parameters.put("creator", String.valueOf(creator));
			byte[] payload = ApiUtils.doGet("api/user", parameters, false);
			Object obj = null;
			if( payload.length > 4 ){
				ois = new ObjectInputStream(new ByteArrayInputStream(payload));
				while((obj = ois.readObject())!=null)
				{
					users.add((Sysuser)obj);
					if( ois.read() != 7 ) break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
		return users;
	}

	/**
	 * 查询指定用户的信息
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param username 查询的用户名称
	 * @return
	 */
	public static Sysuser getUser(String username)
	{
		try
		{
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("method", "getUser");
			parameters.put("username", username);
			byte[] payload = ApiUtils.doGet("api/user", parameters, false);
			if( payload != null && payload.length > 0 )
			{
				return (Sysuser)ApiUtils.readSerializableNoException(payload);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
