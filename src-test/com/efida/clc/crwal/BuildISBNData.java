package com.efida.clc.crwal;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.util.IOHelper;
import com.focus.util.Tools;

public class BuildISBNData {

	public static void main(String[] args) {
		try {
			String json = new String(IOHelper.readAsByteArray(new File("D:/focusnt/clc/trunk/DESIGN/数据用例/case_文字_小说_冰与火之歌/3其它/5官方登记/官方原始数据.json")), "UTF-8");
			JSONArray data = new JSONArray(json);
			for(int i = 0 ; i < data.length(); i++){
				JSONObject e = data.getJSONObject(i);
				build(e);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void build(JSONObject e) throws Exception{
		JSONObject out = new JSONObject();
		out.put("workName", e.getString("正书名"));
		out.put("registerCode", "");
		out.put("contactName", "国家新闻出版广电总局");
		out.put("contactPhone", "010-83138000");
		out.put("contactEmail", "clc@nrta.gov.cn");
		out.put("citiaoType", 3);
		out.put("citiaoClass", 5);
		out.put("work_copyright_type", 1);
		out.put("workContentType", "text");
		JSONArray attaches = new JSONArray();
		out.put("attaches", attaches);
		JSONObject attache = new JSONObject();
		attaches.put(attache);
		attache.put("attachDesc", "引用国家新闻广电出版总局信息。");
		attache.put("attachUrl", e.getString("referer"));
		attache.put("isPublic", 1);
		
		JSONObject officialRegister = new JSONObject();
		out.put("officialRegister", officialRegister);
		officialRegister.put("itemName", "ISBN");
		officialRegister.put("itmeCode", e.getString("ISBN"));
		officialRegister.put("register", e.getString("出版单位"));
		officialRegister.put("itmeReferer", e.getString("referer"));
		officialRegister.put("remark", String.format("该图书于%s在%s出版", e.getString("出版时间"), e.getString("出版时间")));
		e.remove("referer");
		String isbn = e.remove("ISBN").toString();
		officialRegister.put("metadata", e);

		out.put("createTime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss"));
		out.put("editType", 1);
		out.put("audit_status", "wait_audit");
	
		String filename = String.format("D:/focusnt/clc/trunk/DESIGN/数据用例/case_文字_小说_冰与火之歌/3其它/5官方登记/isbn_%s.json", isbn);
		IOHelper.writeFile(new File(filename), out.toString(4).getBytes("UTF-8"));
	}
}
