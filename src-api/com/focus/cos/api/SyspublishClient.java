package com.focus.cos.api;

import org.json.JSONObject;

import com.focus.util.Tools;

public class SyspublishClient 
{
	/**
	 * 发布程序，提交程序配置申请，由系统管理员审批
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param config
	 * @param isAdd 是否是新增配置，或者修改
	 * @param remark 操作的说明
	 * @param operfrom 是从那个账号或者程序发起的
	 * @throws Exception
	 */
	public static void submit(JSONObject config) throws Exception
	{
    	config.put("opertime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
		byte[] payload = ApiUtils.doPost("api/publish", config.toString().getBytes("UTF-8"));
		JSONObject json = new JSONObject(new String(payload, "UTF-8"));
		if( json.has("error") )
		{
			throw new Exception(json.getString("error"));
		}
	}
	/**
	 * 发布程序，提交程序配置申请，由系统管理员审批
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param config
	 * @param isAdd 是否是新增配置，或者修改
	 * @param remark 操作的说明
	 * @param operfrom 是从那个账号或者程序发起的
	 * @throws Exception
	 */
	public static void publish(JSONObject config, boolean isAdd, String remark, String operfrom) throws Exception
	{
    	config.put("oper", isAdd?0:1);
    	config.put("operlog", remark);
    	config.put("operuser", operfrom);
    	config.put("opertime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
		byte[] payload = ApiUtils.doPost("api/publish", config.toString().getBytes("UTF-8"));
		JSONObject json = new JSONObject(new String(payload, "UTF-8"));
		if( json.has("error") )
		{
			throw new Exception(json.getString("error"));
		}
		else
		{
			
		}
	}
	
	/**
	 * 删除指定程序
	 * @param from
	 * @param id
	 * @param operuser
	 * @param remark
	 * @throws Exception
	public static int remove(String id, String operuser, String remark) throws Exception
	{
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("method", "remove");
		parameters.put("Program-ID", id);
		parameters.put("Operuser", operuser);
		parameters.put("Remark", remark);
		byte[] payload = ApiUtils.doGet("api/publish", parameters, false);
		JSONObject result = new JSONObject(new String(payload, "UTF-8"));
		if( result.has("status") )
		{
			return result.getInt("status");
		}
		return -1;
	}
	 */
}