package com.focus.weixin;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.jsoup.nodes.Document;

import com.focus.util.HttpUtils;

public class CallbackServletTest {

	public static void main(String[] args) {

		try {
			String reqXML = "<xml>" +
			"<ToUserName><![CDATA[banquanjia_svr]]></ToUserName>" +
			"<FromUserName><![CDATA[oehZyswMxME-oNb0Lai9g_U-MiDs]]></FromUserName>" +
			"<CreateTime>"+(System.currentTimeMillis() / 1000)+"</CreateTime>" +
			"<MsgType><![CDATA[text]]></MsgType>" +
			"<MsgID><![CDATA[123]]></MsgID>" +
			"<Content><![CDATA[aaa]]></Content>" +
			"</xml>";
			Document doc = HttpUtils.post("http://localhost:9996/callback", new HashMap<String, String>(), 
				reqXML.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
