package com.focus.cos.digg;

import org.json.JSONObject;

public abstract class OLTP 
{
	/**
	 * 根据不同的查询组件的需求处理数据
	 * @param data
	 */
	public void hanlde(Cache data, JSONObject digg) 
	{
		for(JSONObject row : data)
		{
			handle(row, digg);
		}
	}
	
	/**
	 * 处理单行数据，配合单行逻辑
	 * @param row
	 */
	public abstract void handle(JSONObject row, JSONObject digg);
}
