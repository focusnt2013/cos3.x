package com.focus.cos.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 用于非MVC层的国际化通用类
 * @author 余宁
 *
 */
public class Tools4i18n
{
	private static String local; ///区域
	
	private static Properties pro = new Properties();
	/**
	 * 类初始化
	 */
	static{
		InputStream stream = Tools4i18n.class.getClassLoader().getResourceAsStream("webwork.properties");
		
		try
		{
			pro.load(stream);
			local = pro.get("webwork.locale").toString();
			String path = pro.get("webwork.custom.i18n.resources").toString().replaceAll("\\.", "/")
						+ "_" + local + ".properties";
			
			InputStream i18nStream = Tools4i18n.class.getClassLoader().getResourceAsStream(path);
			if(i18nStream != null)
			pro.load(i18nStream);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 获得国际化文本
	 * @return
	 */
	public static String getI18nProperty(String key){
		if(pro != null)
			return pro.getProperty(key);
		return "";
	}
	
	public static void main(String[] args)
	{
		

	}
}
