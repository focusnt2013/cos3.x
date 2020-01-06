package com.focus.cos.api;

import java.util.ArrayList;

import org.json.JSONObject;

public class ApiTest {

	public static void main(String[] args) 
	{
//		File file = new File("D:/focusnt/cos/trunk/IDE/data/identity");
//		Key identity = (Key)IOHelper.readSerializableNoException(file);
		System.setProperty("cos.identity", "D:/focusnt/cos/trunk/IDE/data/identity");
		String cosapi = "http://127.0.0.1:9079/";//"http://omtapi.focusnt.com/";//http://127.0.0.1:9079/";
		try
		{
			byte[] payload = ApiUtils.doGet(cosapi, null, false);
			String json = new String(payload, "UTF-8");
			JSONObject result = new JSONObject(json);
			System.out.println(result.toString(4));
			System.setProperty("cos.api.port", "9079");
			ArrayList<Sysuser> users = SysuserClient.listUser(-1, -1, Status.Enable.getValue());
			StringBuffer data = new StringBuffer();
			data.append('[');
			Sysuser sysuser = null;
			for(Sysuser user : users)
			{
				if( data.length() > 1 ) data.append(',');
				data.append('{');
				data.append("'account':'"+user.getUsername()+"',");
				data.append("'name':'"+user.getRealname()+"',");
				data.append("'email':'"+user.getEmail()+"',");
				data.append("'roleid':"+user.getRoleid()+",");
				data.append("'creator':"+user.getCreator()+",");
//				data.append("'role':'"+RoleMgr.getRoleName(user.getRoleid())+"',");
				data.append("'SUBSCRIBER':false");
				data.append('}');
				sysuser = user;
			}
			data.append(']');
			System.err.println(data.toString());
			Sysuser admin = SysuserClient.getUser("admin");
			System.err.println(admin.getRealname());
			sysuser.setUsername("test");
			sysuser.setCreator("admin");
			sysuser.setPassword("123456");
			sysuser.setId(-1);
			SysuserClient.add(sysuser);
			System.err.print(sysuser.getId());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
