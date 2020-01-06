package com.focus.cos.web.dev.action;

import org.bson.Document;
import org.json.JSONObject;

import com.focus.cos.digg.OLTP;

public class CallbackQuery extends OLTP {

	@Override
	public void handle(JSONObject row, JSONObject digg)
	{
//		if( row.has("IP") )
//		{
//			row.put("Region", HttpUtils.getIpRegion(row.getString("IP")));
//		}
		if( row.has("SendLocationInfo") )
		{
//			System.err.print(row.get("SendLocationInfo"));
			Document obj = (Document)row.get("SendLocationInfo");
//			JSONObject obj = row.getJSONObject("SendLocationInfo");
			row.put("SendLocationInfo", obj.getString("Label"));
			row.put("Longitude", obj.getString("Location_Y"));
			row.put("Latitude", obj.getString("Location_X"));
			row.put("Precision", obj.getString("Scale"));
			row.put("Remark", obj.getString("SendLocationInfo"));
		}
		if( row.has("Content") )
		{
			row.put("Remark", row.getString("Content"));
		}
		if( row.has("Recognition") )
		{
			row.put("Remark", row.getString("Recognition"));
		}
		if( row.has("Label") )
		{
			row.put("Remark", row.getString("Label"));
		}
		if( row.has("Label") )
		{
			row.put("Remark", row.getString("Label"));
		}
		if( row.has("Event") )
		{
			if( "VIEW".equals(row.getString("Event")) || "CLICK".equals(row.getString("Event")) )
			{
				row.put("Remark", row.getString("EventKey"));
			}
			else if( "LOCATION".equals(row.getString("Event")) )
			{
				row.put("Remark", "经纬度:"+row.getString("Longitude")+","+row.getString("Latitude")+"; 精度:"+row.getString("Precision"));
			}
			else if( "location_select".equals(row.getString("Event")) )
			{
				row.put("Remark", row.getString("SendLocationInfo"));
			}
		}
		if( row.has("MsgType") )
		{
			if( "link".equals(row.getString("MsgType")) )
			{
				row.put("Remark", row.getString("Url"));
				row.put("MediaUrl", row.getString("Url"));
			}
		}
		if( row.has("Location_X") )
		{
			row.put("Longitude", row.getString("Location_Y"));
			row.put("Latitude", row.getString("Location_X"));
			row.put("Precision", row.getString("Scale"));
		}
		if( row.has("PicUrl") )
		{
			row.put("MediaUrl", "z/"+row.getString("PicUrl"));
			row.put("Remark", row.getString("PicUrl"));
		}
	}
}
