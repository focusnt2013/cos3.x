package com.focus.cos.web.dev.action;

import org.json.JSONObject;

import com.focus.cos.digg.OLTP;

public class UserQuery extends OLTP {

	@Override
	public void handle(JSONObject row, JSONObject digg)
	{
//		if( row.has("subscribe_time") )
//		{
//			row.put("subscribe_time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", row.getInt("subscribe_time")));
//		}
		if( row.has("subscribe") )
		{
			row.put("bind", row.has("bind")?row.getInt("bind")==1:row.getInt("subscribe")==1);
		}
	}
}
