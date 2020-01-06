package com.efida.eec.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import com.focus.cos.web.util.RsaKeyTools;
import com.focus.util.HttpUtils;
import com.focus.util.IOHelper;
import com.focus.util.Tools;


public class BuildHaimaCloud {
	
	private static JSONObject addCluster(int parent, String name)
		throws Exception
	{
		String privatekey = "-----BEGIN RSA PRIVATE KEY-----"+
			"\r\nMIIBOwIBAAJBAKplWuCzmJo7LBWRpBO61FnvjWk61yS++e0F39ut7NnfN6DrRq7v"+
			"\r\nIj8Y5US62nUyLuvNVKY2iMer8X/57WfZDQsCAwEAAQJBAI8lNoMuXTS0IQS9pSkt"+
			"\r\n+tbS9+k/FR45kZwdI0JZinA8RbVayZ+s8soCRvunGDuuUVB1TsIh1O7H3OTVeWpc"+
			"\r\nMgECIQDfiO9BIneyroSFLrLLJjvNJLtU5SNEsHFzgUeycbi40QIhAMMktDu8RfNK"+
			"\r\nGiLw/aBLlYfy7wk+Dzc3mydphO5CSF8bAiBtZE3xyjRZtR4VLy1ATY2mbvteKGSC"+
			"\r\nEPb0V6gGo7CKgQIhAIsQKfxzw+mHQi7qS+OiWXIjNiMA/bjcwI2Kjbd4trhHAiA+"+
			"\r\nx6GFKRWoqsUTMmeUNCcqIob3slCbrQFYWSyCO7GGzA=="+
			"\r\n-----END RSA PRIVATE KEY-----";
		PEMParser parser = new PEMParser(new StringReader(privatekey));
		PEMKeyPair pari = (PEMKeyPair)parser.readObject();
        parser.close();
        byte[] pk1 = pari.getPrivateKeyInfo().getEncoded();
        String token = "123456";
        String timestamp = Tools.getFormatTime("yyyyMMddHHmmss", System.currentTimeMillis());
        String nonce = Tools.getUniqueValue();
        
		byte[] signautreResult = RsaKeyTools.sign(token+timestamp+nonce, pk1);
		String signature = RsaKeyTools.bytes2String(signautreResult);
        String url = "http://manage.haimacloud.com/configmonitor/3jvw5qn7t5t/"+timestamp+"/"+nonce+"/"+signature;
        JSONObject obj = new JSONObject();
        obj.put("action", "addCluster");
        obj.put("name", name);
        obj.put("parent", parent);
        Document doc = HttpUtils.post(url, null, ("data="+obj.toString()).getBytes());
        JSONObject result = new JSONObject(doc.getElementsByTag("body").get(0).text());
        return result;
	}

	private static JSONObject addServer(int parent, String ip)
		throws Exception
	{
		String privatekey = "-----BEGIN RSA PRIVATE KEY-----"+
			"\r\nMIIBOwIBAAJBAKplWuCzmJo7LBWRpBO61FnvjWk61yS++e0F39ut7NnfN6DrRq7v"+
			"\r\nIj8Y5US62nUyLuvNVKY2iMer8X/57WfZDQsCAwEAAQJBAI8lNoMuXTS0IQS9pSkt"+
			"\r\n+tbS9+k/FR45kZwdI0JZinA8RbVayZ+s8soCRvunGDuuUVB1TsIh1O7H3OTVeWpc"+
			"\r\nMgECIQDfiO9BIneyroSFLrLLJjvNJLtU5SNEsHFzgUeycbi40QIhAMMktDu8RfNK"+
			"\r\nGiLw/aBLlYfy7wk+Dzc3mydphO5CSF8bAiBtZE3xyjRZtR4VLy1ATY2mbvteKGSC"+
			"\r\nEPb0V6gGo7CKgQIhAIsQKfxzw+mHQi7qS+OiWXIjNiMA/bjcwI2Kjbd4trhHAiA+"+
			"\r\nx6GFKRWoqsUTMmeUNCcqIob3slCbrQFYWSyCO7GGzA=="+
			"\r\n-----END RSA PRIVATE KEY-----";
		PEMParser parser = new PEMParser(new StringReader(privatekey));
		PEMKeyPair pari = (PEMKeyPair)parser.readObject();
        parser.close();
        byte[] pk1 = pari.getPrivateKeyInfo().getEncoded();
        String token = "123456";
        String timestamp = Tools.getFormatTime("yyyyMMddHHmmss", System.currentTimeMillis());
        String nonce = Tools.getUniqueValue();
        
		byte[] signautreResult = RsaKeyTools.sign(token+timestamp+nonce, pk1);
		String signature = RsaKeyTools.bytes2String(signautreResult);
        String url = "http://manage.haimacloud.com/configmonitor/3jvw5qn7t5t/"+timestamp+"/"+nonce+"/"+signature;
        JSONObject obj = new JSONObject();
        obj.put("action", "addServer");
        obj.put("ip", ip);
        obj.put("port", 9075);
        obj.put("parent", parent);
        Document doc = HttpUtils.post(url, null, ("data="+obj.toString()).getBytes());
        JSONObject result = new JSONObject(doc.getElementsByTag("body").get(0).text());
        return result;
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	private static void test1() throws Exception
	{
        File file = new File("D:\\efida\\temp\\haimacloud.xlsx");
		JSONObject idc = new JSONObject();
		JSONArray data = new JSONArray(new String(IOHelper.readAsByteArray(new File("D:\\efida\\temp\\haimacloud.txt")), "UTF-8"));
		for(int i = 1; i < data.length(); i++){
			JSONObject e = data.getJSONObject(i);
			if( e.has("children") ){
				JSONArray children = e.getJSONArray("children");
				for(int j = 0; j < children.length(); j++){
					JSONObject o = children.getJSONObject(j);
//					System.out.println(o.toString(4));
					JSONObject o1 = new JSONObject();
					o1.put("id", o.getInt("id"));
					idc.put(o.getString("name"), o1);
				}
			}
		}
		XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
		for(int i = 5; i < 6; i++){
			XSSFSheet sheet = wb.getSheetAt(i);
			XSSFRow row;
			XSSFCell cell;
			int parent = 0;
			for(int j = 1 , rowCount = 0; rowCount < sheet.getPhysicalNumberOfRows() ; j++ ){
				row = sheet.getRow(j);
				if( row == null ){
					break;
				}
				cell = row.getCell(0);
				String idc_name = cell.getStringCellValue();
				if( "天津数据中心".equalsIgnoreCase(idc_name) ){
					idc_name = "总部数据中心";
				}
				cell = row.getCell(1);
				String idc_jijia = cell.getStringCellValue();
				JSONObject cdn = null;
				cdn = idc.getJSONObject(idc_name);
				if( !cdn.has("cabinets") )
				{
					cdn.put("cabinets", new JSONObject());
				}
				parent = cdn.getInt("id");
				JSONObject cabinets = cdn.getJSONObject("cabinets");
				if( !cabinets.has(idc_jijia) ){
					cabinets.put(idc_jijia, new JSONObject());
					JSONObject result = addCluster(parent, idc_jijia+"机柜");
					System.out.println(result.toString(4));
				}
				JSONObject jijia = cabinets.getJSONObject(idc_jijia);
				cell = row.getCell(2);
				String jijia_server = cell.getStringCellValue();
				if( !jijia.has(jijia_server) ){
					jijia.put(jijia_server, 1);
				}
				
			}
		}
		System.err.println(idc.toString(4));
	}

	/**
	 * 
	 * @throws Exception
	 */
	private static void deleteServer(int id) throws Exception
	{
		String privatekey = "-----BEGIN RSA PRIVATE KEY-----"+
				"\r\nMIIBOwIBAAJBAKplWuCzmJo7LBWRpBO61FnvjWk61yS++e0F39ut7NnfN6DrRq7v"+
				"\r\nIj8Y5US62nUyLuvNVKY2iMer8X/57WfZDQsCAwEAAQJBAI8lNoMuXTS0IQS9pSkt"+
				"\r\n+tbS9+k/FR45kZwdI0JZinA8RbVayZ+s8soCRvunGDuuUVB1TsIh1O7H3OTVeWpc"+
				"\r\nMgECIQDfiO9BIneyroSFLrLLJjvNJLtU5SNEsHFzgUeycbi40QIhAMMktDu8RfNK"+
				"\r\nGiLw/aBLlYfy7wk+Dzc3mydphO5CSF8bAiBtZE3xyjRZtR4VLy1ATY2mbvteKGSC"+
				"\r\nEPb0V6gGo7CKgQIhAIsQKfxzw+mHQi7qS+OiWXIjNiMA/bjcwI2Kjbd4trhHAiA+"+
				"\r\nx6GFKRWoqsUTMmeUNCcqIob3slCbrQFYWSyCO7GGzA=="+
				"\r\n-----END RSA PRIVATE KEY-----";
		PEMParser parser = new PEMParser(new StringReader(privatekey));
		PEMKeyPair pari = (PEMKeyPair)parser.readObject();
        parser.close();
        byte[] pk1 = pari.getPrivateKeyInfo().getEncoded();
        String token = "123456";
        String timestamp = Tools.getFormatTime("yyyyMMddHHmmss", System.currentTimeMillis());
        String nonce = Tools.getUniqueValue();
        
		byte[] signautreResult = RsaKeyTools.sign(token+timestamp+nonce, pk1);
		String signature = RsaKeyTools.bytes2String(signautreResult);
        String url = "http://manage.haimacloud.com/configmonitor/3jvw5qn7t5t/"+timestamp+"/"+nonce+"/"+signature;
        JSONObject obj = new JSONObject();
        obj.put("action", "delServer");
        obj.put("id", id);
        Document doc = HttpUtils.post(url, null, ("data="+obj.toString()).getBytes());
        JSONObject result = new JSONObject(doc.getElementsByTag("body").get(0).text());
	}

	/**
	 * 
	 * @throws Exception
	 */
	private static void test2() throws Exception
	{
        File file = new File("D:\\efida\\temp\\haimacloud.xlsx");
		JSONObject jijia = new JSONObject();
		JSONArray data = new JSONArray(new String(IOHelper.readAsByteArray(new File("D:\\efida\\temp\\haimacloud.txt")), "UTF-8"));
		for(int i = 1; i < data.length(); i++){
			JSONObject e = data.getJSONObject(i);
			if( e.has("children") ){
				JSONArray children = e.getJSONArray("children");
				for(int j = 0; j < children.length(); j++){
					JSONObject o = children.getJSONObject(j);
//					System.out.println(o.toString(4));
					JSONObject o1 = new JSONObject();
					o1.put("id", o.getInt("id"));
//					idc.put(o.getString("name"), o1);
					if( o.has("children") ){
						JSONArray children1 = o.getJSONArray("children");
						for(int k = 0; k < children1.length(); k++){
							JSONObject o2 = children1.getJSONObject(k);
							jijia.put(o2.getString("name"), o2);
						}
					}
				}
			}
		}
		XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
		for(int i = 0; i < 1; i++){
			XSSFSheet sheet = wb.getSheetAt(i);
			XSSFRow row;
			XSSFCell cell;
			int parent = 0;
			int serverid = 0;
			for(int j = 1 , rowCount = 0; rowCount < sheet.getPhysicalNumberOfRows() ; j++ ){
				row = sheet.getRow(j);
				if( row == null ){
					break;
				}
				cell = row.getCell(0);
				String idc_name = cell.getStringCellValue();
				if( "天津数据中心".equalsIgnoreCase(idc_name) ){
					idc_name = "总部数据中心";
				}
				cell = row.getCell(1);
				String idc_jijia = cell.getStringCellValue();
				cell = row.getCell(2);
				String jijia_server = cell.getStringCellValue();
				
				if( !jijia.has(idc_jijia+"机柜") ){
					continue;
				}
				JSONObject jj = null;
				jj = jijia.getJSONObject(idc_jijia+"机柜");
				parent = jj.getInt("id");
				if( !jj.has("servers") ){
					jj.put("servers", new JSONObject());
				}
				JSONObject servers = jj.getJSONObject("servers");
				
				if( !servers.has(jijia_server) ){
					servers.put(jijia_server, new JSONObject());
					JSONObject result = addCluster(parent, "ARM服务器"+jijia_server);
					serverid = result.getInt("id");
					System.out.println(result.toString(4));
				}
				JSONObject arms = servers.getJSONObject(jijia_server);
				cell = row.getCell(6);
				String armip = cell.getStringCellValue();
				arms.put(armip, row.getCell(7).getStringCellValue());
				JSONObject result = addServer(serverid, armip);
				System.out.println(result.toString(4));
			}
		}
		System.err.println(jijia.toString(4));
	}
	
	public static void main(String[] args) {
		try {
//			for(int i = 832; i <= 1009; i++ ){
//				deleteServer(i);
//			}
			test2();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
