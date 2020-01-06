package com.efida.eec.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.util.IOHelper;


public class BuildHaimaCloudMonitor {
	
	public static void main(String[] args) {
		JSONArray data = null;
		XSSFRow row;
		XSSFCell cell;
		XSSFSheet sheet = null;
		int i = 0, j = 0, rowCount;
		try {
	        File file = new File("D:\\efida\\temp\\haimacloud.xlsx");
			JSONObject idc = new JSONObject();
			data = new JSONArray(new String(IOHelper.readAsByteArray(new File("D:\\efida\\temp\\haimacloud.txt")), "UTF-8"));
			for(i = 1; i < data.length(); i++){
				JSONObject e = data.getJSONObject(i);
				if( e.has("children") ){
					JSONArray children = e.getJSONArray("children");
					for(j = 0; j < children.length(); j++){
						JSONObject o = children.getJSONObject(j);
						idc.put(o.getString("name"), o);
					}
				}
			}
			int _id = 1000000;
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
			for(i = 0; i < 31; i++){
				sheet = wb.getSheetAt(i);
				HashMap<String, JSONObject> map_jijia = new HashMap<String, JSONObject>();
				HashMap<String, JSONObject> map_server = new HashMap<String, JSONObject>();
				
				for(j = 1 , rowCount = 0; rowCount < sheet.getPhysicalNumberOfRows() ; j++ ){
					row = sheet.getRow(j);
					if( row == null ){
						break;
					}
					cell = row.getCell(0);
					String idc_name = cell.getStringCellValue();
					cell = row.getCell(1);
					String jijia_name = cell.getStringCellValue();
					cell = row.getCell(2);
					String server_name = cell.getStringCellValue();
					cell = row.getCell(6);
					String armip = cell.getStringCellValue();
					if( idc_name.isEmpty() || jijia_name.isEmpty() || server_name.isEmpty() || armip.isEmpty() ){
						continue;
					}
					
					if( "天津数据中心".equalsIgnoreCase(idc_name) ){
						idc_name = "总部数据中心";
					}
					JSONObject cdn = null;
					cdn = idc.getJSONObject(idc_name);
					if( !cdn.has("children") ){
						cdn.put("children", new JSONArray());
					}
					JSONObject jijia = null;
					if( !map_jijia.containsKey(jijia_name) ){
						jijia = new JSONObject();
						jijia.put("id", _id++);
						jijia.put("pid", cdn.getInt("id"));
						jijia.put("name", jijia_name+"机架");
						jijia.put("isParent", true);
						map_jijia.put(jijia_name, jijia);
						cdn.getJSONArray("children").put(jijia);
					}
					else{
						jijia = map_jijia.get(jijia_name);
					}
					if( !jijia.has("children") ){
						jijia.put("children", new JSONArray());
					}
					JSONObject server = null;
					if( !map_server.containsKey(server_name) ){
						server = new JSONObject();
						server.put("id", _id++);
						server.put("pid", jijia.getInt("id"));
						server.put("name", "ARM服务器"+server_name);
						server.put("isParent", true);
						map_server.put(server_name, server);
						jijia.getJSONArray("children").put(server);
					}
					else{
						server = map_server.get(server_name);
					}
					if( !server.has("children") ){
						server.put("children", new JSONArray());
					}
					JSONObject arm = new JSONObject();
					arm.put("id", _id++);
					arm.put("pid", server.getInt("id"));
					arm.put("ip", armip);
					arm.put("port", 9075);
					arm.put("synchmode", "json");
					server.getJSONArray("children").put(arm);
				}
			}
		} catch (Exception e) {
			if( sheet != null ){
				System.err.println(sheet.getSheetName()+"="+i+","+j);
			}
			e.printStackTrace();
		}
		if( data != null ){
			try {
				IOHelper.writeFile(new File("D:\\efida\\temp\\haimacloud.json"), data.toString().getBytes("UTF-8"));
				JSONArray json = new JSONArray(new String(IOHelper.readAsByteArray(new File("D:\\efida\\temp\\haimacloud.json")), "UTF-8"));
				System.err.println(json.toString(4));
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
