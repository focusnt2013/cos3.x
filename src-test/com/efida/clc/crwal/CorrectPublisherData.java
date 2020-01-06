package com.efida.clc.crwal;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.util.IOHelper;

public class CorrectPublisherData {

	public static void main(String[] args) {
		JSONArray out0 = new JSONArray();//完整输出
		JSONArray out1 = new JSONArray();//图书出版单位
		JSONArray out2 = new JSONArray();//音像出版单位
		JSONArray out3 = new JSONArray();//电子出版单位
		JSONArray out4 = new JSONArray();//单位名称待校验出版机构数据
		try {
			String json1 = new String(IOHelper.readAsByteArray(new File("D:/focusnt/clc/trunk/DESIGN/采集数据/出版单位/1.json")), "UTF-8");
			String json10 = new String(IOHelper.readAsByteArray(new File("D:/focusnt/clc/trunk/DESIGN/采集数据/出版单位/10.json")), "UTF-8");
			String json11 = new String(IOHelper.readAsByteArray(new File("D:/focusnt/clc/trunk/DESIGN/采集数据/出版单位/11.json")), "UTF-8");
			JSONArray data1 = new JSONArray(json1);
			JSONArray data10 = new JSONArray(json10);
			JSONArray data11 = new JSONArray(json11);

			correct(data1, out0, out1, out2, out3, out4);
			correct(data10, out0, out1, out2, out3, out4);
			correct(data11, out0, out1, out2, out3, out4);
			
			IOHelper.writeFile(new File("D:/focusnt/clc/trunk/DESIGN/采集数据/出版单位/全部出版单位数据.json"), 
				out0.toString(4).getBytes("UTF-8"));
			IOHelper.writeFile(new File("D:/focusnt/clc/trunk/DESIGN/采集数据/出版单位/图书出版单位数据.json"), 
				out1.toString(4).getBytes("UTF-8"));
			IOHelper.writeFile(new File("D:/focusnt/clc/trunk/DESIGN/采集数据/出版单位/音像出版单位数据.json"), 
				out2.toString(4).getBytes("UTF-8"));
			IOHelper.writeFile(new File("D:/focusnt/clc/trunk/DESIGN/采集数据/出版单位/电子出版单位数据.json"), 
				out3.toString(4).getBytes("UTF-8"));
			IOHelper.writeFile(new File("D:/focusnt/clc/trunk/DESIGN/采集数据/出版单位/单位名称待校验出版机构数据.json"), 
				out4.toString(4).getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void correct(JSONArray input, JSONArray out0, JSONArray out1, JSONArray out2, JSONArray out3, JSONArray out4) throws Exception{
		for(int i = 0; i < input.length(); i++ ){
			JSONObject e = input.getJSONObject(i);
			String name = e.getString("出版物单位名称");
			if( name.endsWith("出版社") && (name.indexOf("公司")>0) ){
				name = name.substring(name.indexOf("公司")+2);	
			}
			else{
				int j = name.indexOf("有限责任公司");
				if( j > 0 ){
					name = name.substring(0, j);
				}
				j = name.indexOf("有限公司");
				if( j > 0 ){
					name = name.substring(0, j);
				}
				j = name.indexOf("出版社");
				if( j > 0 ){
					name = name.substring(0, j+3);
				}
				j = name.indexOf("出版传媒");
				if( j > 0 ){
					name = name.substring(0, j+2);
				}
				j = name.indexOf("（");
				if( j > 0 ){
					name = name.substring(j+1);
				}
//				if( name.endsWith("	") ){
//					name = name.replaceAll("出版传媒", "出版社");
//				}
			}
			e.put("name", name);
			out0.put(e);
			if( "图书出版单位".equals(e.getString("类型")) ){
				out1.put(e);
			}
			else if( "音像出版单位".equals(e.getString("类型")) ){
				out2.put(e);
			}
			else if( "电子出版物出版单位".equals(e.getString("类型")) ){
				out3.put(e);
			}
			else {
				out4.put(e);
			}
		}
	}
}
