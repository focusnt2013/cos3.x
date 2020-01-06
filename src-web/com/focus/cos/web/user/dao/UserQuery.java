package com.focus.cos.web.user.dao;

import java.util.Iterator;

import org.json.JSONObject;

import com.focus.cos.digg.OLTP;
import com.focus.cos.web.common.ZKMgr;
import com.focus.util.Zookeeper;

public class UserQuery extends OLTP {

	@Override
	public void handle(JSONObject row, JSONObject digg)
	{
		String username = row.getString("USERNAME");
		row.put("privileges", "user!privileges.action?ww=-1&id="+username);
		Zookeeper zookeeper = null;
		try
		{
			zookeeper = ZKMgr.getZookeeper();
			String zkpath = "/cos/user/properties/"+username;
			JSONObject userprops = zookeeper.getJSONObject(zkpath, true);
			if( userprops != null)
			{
				Iterator<?> iter = userprops.keys();
				while(iter.hasNext())
				{
					String key = iter.next().toString();
					row.put(key, userprops.get(key));
				}
			}
			zkpath = "/cos/login/user/"+username;
			JSONObject login = zookeeper.getJSONObject(zkpath);
			if( login != null )
			{
				row.put("login_count", login.has("count")?login.getInt("count"):0);
			}
		}
		catch(Exception e)
		{
		}
	}
}
