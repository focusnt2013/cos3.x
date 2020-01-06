package com.focus.cos.api;

import java.util.ArrayList;

public class SysuserClientTest 
{
	public static void main(String[] args) 
	{
		System.setProperty("cos.identity", "D:/focusnt/cos/trunk/IDE/data/identity");
		System.setProperty("cos.api.port", "9079");
		ArrayList<Sysuser> users = SysuserClient.listUser(1, -1, Status.Enable.getValue());
		for(Sysuser user : users)
		{
			System.out.println(user.toString());
		}
	}
}
