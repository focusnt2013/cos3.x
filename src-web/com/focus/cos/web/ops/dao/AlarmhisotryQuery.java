package com.focus.cos.web.ops.dao;

import org.json.JSONObject;

import com.focus.cos.digg.OLTP;
import com.focus.cos.web.ops.service.MonitorMgr;

public class AlarmhisotryQuery extends OLTP {

	@Override
	public void handle(JSONObject e, JSONObject digg)
	{
		try
		{

			if(!e.has("ID")) return;
			if( e.has("SERVERKEY") )
			{
	    		JSONObject server = MonitorMgr.getInstance().getServer(e.getString("SERVERKEY"));
	    		if( server != null )
	    		{
	    			e.put("DN", server.getString("ip")+":"+server.getInt("port"));
	    			e.put("DNRemark", server.has("remark")?server.getString("remark"):"未知名称");
	    			e.put("DNId", server.has("id")?server.getInt("id"):0);
	    		}
			}
    		int freq = e.has("FREQUENCY")?e.getInt("FREQUENCY"):0;
    		e.put("NOTIFIED", freq>0);
		}
		catch(Exception e2)
		{
		}
	}
}
