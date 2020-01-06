package com.focus.weixin;

public class CallbackServerTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CallbackServer.main(new String[]{
			"banquanjia_svr",
			"192.168.1.114:9076",
			"/cos/config/modules/Sys/weixin/banquanjia_svr",
			"com.focus.weixin.DefaultCallbackServlet",
		});
	}
}
