/*
 * @(#)clsTools.java
 *
 * Copyright 2000-2001 Liu Xue. All Rights Reserved.
 *
 * Use is subject to license terms.
 *
 */
package com.focus.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

/*
 * Author: Xue Liu
 * Date: 2002/04/16
 */
public class Tools
{
    public static final long MILLI_OF_WEEK = 7 * 24 * 60 * 60 * 1000;
    public static final long MILLI_OF_DAY = 24 * 60 * 60 * 1000;
    public static final long MILLI_OF_HOUR = 60 * 60 * 1000;
    public static final long MILLI_OF_MINUTE = 60 * 1000;
    public static final int SECOND_OF_WEEK = 7 * 24 * 60 * 60;
    public static final int SECOND_OF_DAY = 24 * 60 * 60;
    public static final int SECOND_OF_HOUR = 60 * 60;
    public static final int SECOND_OF_MINUTE = 60;

    private static Config i18nConfig;

    // 来自stackoverflow的MD5计算方法，调用了MessageDigest库函数，并把byte数组结果转换成16进制
    private static String baiduSign(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    /**
     * 
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toQueryString(Map<String, String> data) {
        StringBuffer queryString = new StringBuffer();
        try{
        	for (Entry<?, ?> pair : data.entrySet()) {
        		queryString.append(pair.getKey() + "=");
        		queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8") + "&");
        	}
        	if (queryString.length() > 0) {
        		queryString.deleteCharAt(queryString.length() - 1);
        	}
        }
        catch(Exception e){
        }
        return queryString.toString();
    }
    /**
     * 得到经纬度对应的地址
     * http://lbsyun.baidu.com/index.php?title=webapi/guide/webservice-placeapi#service-page-anchor-1-3
     * AK: mc09p0xAjIqE3YvhOlDKX9C8Ar6Msk8s
     * SK: 7XDpn0lyR85jG6QSQUsnjazvKt9ftwn3
     * @param log
     * @param lat
     * @return
     */
    private final static String SK = "7XDpn0lyR85jG6QSQUsnjazvKt9ftwn3";
    private final static String AK = "mc09p0xAjIqE3YvhOlDKX9C8Ar6Msk8s";
    public static String location(String log, String lat, String query){ 
        Map<String, String> paramsMap = new LinkedHashMap<String, String>();
        paramsMap.put("location", String.format("%s,%s", log, lat));
        paramsMap.put("output", "json");
        paramsMap.put("page_size", "1");
        paramsMap.put("query", query);
        paramsMap.put("radius", "1000");
        paramsMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
        paramsMap.put("ak", AK);
    	String params = toQueryString(paramsMap);
    	String signature = String.format("/place/v2/search?%s%s", params, SK);
//    	System.err.println(signature);
    	StringBuilder location = new StringBuilder();
	    try 
	    {
	    	String sn = baiduSign(URLEncoder.encode(signature, "UTF-8"));
	    	String url = String.format("http://api.map.baidu.com/place/v2/search?%s&sn=%s", params, sn); 
	    	Document doc = HttpUtils.crwal(url);
	    	JSONObject json = new JSONObject(doc.text());
//	    	System.out.println(json.toString(4)); 
	    	if( json.has("status") && json.getInt("status") == 0 ){
	    		if( json.has("results") ) {
	    			JSONArray results = json.getJSONArray("results");
	    			if( results.length() > 0 ){
	    				JSONObject l = results.getJSONObject(0);
	    				String address = l.has("address")?l.getString("address"):"";
	    				String province = l.has("province")?l.getString("province"):"";
	    				String city = l.has("city")?l.getString("city"):"";
	    				String name = l.has("name")?l.getString("name"):"";
	    				location.append(name);
	    				location.append("(");
	    				if( address.indexOf(province) == -1 ){
	    					location.append(province);
	    				}
	    				if( address.indexOf(city) == -1 ){
	    					location.append(city);
	    				}
	    				location.append(address);
	    				location.append(")附近");
	    			}
	    		}
	    	}
//	    	System.out.println(json.toString(4));
	    } catch (Exception e) {  
	    	e.printStackTrace();
	    }  
	    return location.toString();
	} 
    
    /**
     * 通过IP得到地址
     * @param ip
     * @return
     */
    public static String location(String ip){ 
        Map<String, String> paramsMap = new LinkedHashMap<String, String>();
        paramsMap.put("ip", ip);
        paramsMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
        paramsMap.put("ak", AK);
    	String params = toQueryString(paramsMap);
    	String signature = String.format("/location/ip?%s%s", params, SK);
	    try 
	    {
	    	String sn = baiduSign(URLEncoder.encode(signature, "UTF-8"));
	    	String url = String.format("http://api.map.baidu.com/location/ip?%s&sn=%s", params, sn);  
	    	Document doc = HttpUtils.crwal(url);
	    	JSONObject json = new JSONObject(doc.text());
	    	if( json.has("status") && json.getInt("status") == 0 ){
		    	if( json.has("content") ) {
		    		JSONObject content = json.getJSONObject("content");
	    			return content.has("address")?content.getString("address"):"";
		    	}
	    	}
	    } catch (Exception e) {  
	    	e.printStackTrace();
	    }  
	    return "";
	} 
    /**
     * 删除HTML标签
     * @param str
     * @return
     */
    public static String delHTMLTag(String str){ 
		if( str == null ) return "";
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式 
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式 
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式 
         
        Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE); 
        Matcher m_script=p_script.matcher(str); 
        str = m_script.replaceAll(""); //过滤script标签 
         
        Pattern p_style = Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE); 
        Matcher m_style = p_style.matcher(str); 
        str = m_style.replaceAll(""); //过滤style标签 
         
        Pattern p_html = Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE); 
        Matcher m_html = p_html.matcher(str); 
        str = m_html.replaceAll(""); //过滤html标签 

        return str.trim(); //返回文本字符串 
    } 
	public static String getJSONValue(String str)
	{
		if( str == null ) return "";
		StringBuffer buf = new StringBuffer();
		boolean b = false;
		for(int i = 0; i < str.length(); i++ )
		{
			char c = str.charAt(i);
			if( c == '<' )
			{
				b = true;
				continue;
			}
			if( c == '>' )
			{
				b = false;
				continue;
			}
			if( b ) continue;
			buf.append(c);
		}
		return buf.toString();
	}
	
    /**
     * 转换日期成为标准的整形表示的时间（采用默认的间隔符－）
     * @param date 字符串格式的日期表现（yyyy-MM-dd）
     * @return
     */
    public static Random random = new Random();

    public static final int getRandomInt(int p0)
    {
        return random.nextInt(p0);
    }

    public static final int getDateInSeconds(String date)
    {
        return getDateInSeconds(date, "-");
    }

    /**
     *  转换日期成为标准的整形表现的时间
     * @param date 字符串格式的日期表现
     * @param Separator 表现的间隔符
     * @return
     */
    public static final int getDateInSeconds(String date, String Separator)
    {
        return(int) (getDateInMillis(date, Separator) / 1000);
    }

    /**
     * 转换日期成为标准的长整形表现的时间
     * @param date
     * @param Separator
     * @return
     */
    public static final long getDateInMillis(String date, String Separator)
    {
        if(date == null || date.length() == 0)
        {
            return -1;
        }
        //YYYY-MM-DD日期格式必须满足这样的形式
        String[] format = date.split(Separator);
        Calendar time = Calendar.getInstance();
        time.clear();
        try
        {
            time.set(Integer.parseInt(format[0]),
                     Integer.parseInt(format[1]) - 1,
                     Integer.parseInt(format[2]));
        }
        catch(Exception e)
        {
            return 0;
        }
        return time.getTimeInMillis();
    }

	/**
     * 将一定格式的时间字符串转换成毫米数
     * @param time 需要转换的时间字符串
     * @param formatStr 时间格式字符串
     * @return
     */
    public static final long convertTimeToLong(String time, String formatStr) {
		SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
		
		Date dt = null;
		try {
			dt = sdf.parse(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dt.getTime();
	}
	
    public static final long getDateInMillis(String date)
    {
        if(date.length() < 10)
        {
            return 0;
        }
        String[] format = new String[3];
        format[0] = date.substring(0, 4);
        format[1] = date.substring(5, 7);
        format[2] = date.substring(8, 10);
//        System.out.println("format[0]="+format[0]);
//        System.out.println("format[1]="+format[1]);
//        System.out.println("format[2]="+format[2]);
        Calendar time = Calendar.getInstance();
        time.clear();
        try
        {
            time.set(Integer.parseInt(format[0]),
                     Integer.parseInt(format[1]) - 1,
                     Integer.parseInt(format[2]));
        }
        catch(Exception e)
        {
            return 0;
        }
        return time.getTimeInMillis();
    }

    /**
     * 得到精确到秒的时间数据
     * @param KKMMSS 精确时间的字符串形式（默认:间隔）
     * @return
     */
    public static final int getTimeInSeconds(String KKMMSS)
    {
        return getTimeInSeconds(KKMMSS, ":");
    }

    /**
     * 得到精确到秒的时间数据
     * @param KKMMSS 精确时间的字符串形式
     * @param Separator 时间的间隔符
     * @return
     */
    public static final int getTimeInSeconds(String KKMMSS, String Separator)
    {
        return(int) (getTimeInMillis(KKMMSS, Separator) / 1000);
    }

    /**
     * 将用于显示的时间字符串转换成精确到毫秒的时间数据
     * @param KKMMSS
     * @param Separator
     * @return
     */
    public static final long getTimeInMillis(String KKMMSS, String Separator)
    {
        //YYYY-MM-DD日期格式必须满足这样的形式
        String[] format = KKMMSS.split(Separator);
        long millis = 0;
        switch(format.length)
        {
            case 1:
                millis = Integer.parseInt(format[0]) * 60 * 60;
                break;
            case 2:
                millis = Integer.parseInt(format[0]) * 60 * 60 +
                    Integer.parseInt(format[1]) * 60;
                break;
            case 3:
                millis = Integer.parseInt(format[0]) * 60 * 60 +
                    Integer.parseInt(format[1]) * 60 +
                    Integer.parseInt(format[2]);
                break;
        }

        return millis * 1000;
    }

    /**
     * 将标准的长整形的时间转换成指定格式的时间表现
     * @param TimeFormat 可配的时间表现的格式（可以是yyyy年MM月dd日 HH:mm:ss）
     * @param millis
     * @return
     */
    public static final String getFormatTime(String TimeFormat)
    {
        return getFormatTime(TimeFormat, System.currentTimeMillis());
    }
    public static final String getFormatTime(long millis)
    {
        return getFormatTime("yyyy-MM-dd HH:mm:ss", millis);
    }
    public static final String getFormatTime(String TimeFormat, long millis)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(TimeFormat);
        java.util.Calendar time = java.util.Calendar.getInstance();
        time.setTimeInMillis(millis);

        return sdf.format(time.getTime());
    }

    /**
     * 将标准的整形的时间转换成指定格式的时间表现
     * @param TimeFormat 可配的时间表现的格式（可以是yyyy年MM月dd日 HH:mm:ss）
     * @param seconds
     * @return
     */
    public static final String getFormatTime(String TimeFormat, int seconds)
    {
        return getFormatTime(TimeFormat, ( (long) seconds) * 1000);
    }

    public static final long toSmartTime(String KKMMSS, String Separator)
    {
        try
        {
            //YYYY-MM-DD日期格式必须满足这样的形式
            String[] format = split(KKMMSS, Separator);
            long millis = 0;
            switch(format.length)
            {
                case 1:
                    millis = Integer.parseInt(format[0]) * 60 * 60;
                    break;
                case 2:
                    millis = Integer.parseInt(format[0]) * 60 * 60 +
                        Integer.parseInt(format[1]) * 60;
                    break;
                case 3:
                    millis = Integer.parseInt(format[0]) * 60 * 60 +
                        Integer.parseInt(format[1]) * 60 +
                        Integer.parseInt(format[2]);
                    break;
            }
            return millis * 1000;
        }
        catch(Exception e)
        {
        }
        return -1;
    }

    /**
     * 转换精确时间为指定的格式（加入了时区的概念）
     * @param TimeFormat 可配的时间表现的格式（可以是 HH:mm:ss）
     * @param lt
     * @return
     */
    public static final String getSmartTime(int lt)
    {
        return getSmartTime("HH:mm:ss", lt);
    }

    public static final String getSmartTime(String TimeFormat, int lt)
    {
        int s = lt % SECOND_OF_DAY;
        int hour = s / SECOND_OF_HOUR;
        int minute = s % SECOND_OF_HOUR / SECOND_OF_MINUTE;
        int seconds = s % SECOND_OF_MINUTE;

        StringBuffer sb = new StringBuffer();
        String[] formats = TimeFormat.split(":");
        for(int i = 0; i < formats.length; i++)
        {
            if(formats[i].equals("HH"))
            {
                sb.append(hour > 9 ? "" + hour : "0" + hour);
            }
            else if(formats[i].equals("mm"))
            {
                sb.append(minute > 9 ? "" + minute : "0" + minute);
            }
            else if(formats[i].equals("ss"))
            {
                sb.append(minute > 9 ? "" + minute : "0" + minute);
            }
            else
            {
                sb.append(formats[i]);
            }
            if(i + 1 < formats.length)
            {
                sb.append(":");
            }
        }
        return sb.toString();
    }
    
    /**
     * 日期的月份加减计算
     * @param num
     * @return
     */
    public static final Date addMonth(Date startTime, int monthNumToAdd)
    {
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(startTime);
    	int year = cal.get(Calendar.YEAR);
    	int month = cal.get(Calendar.MONTH);
    	int day = cal.get(Calendar.DAY_OF_MONTH);
    	int hour = cal.get(Calendar.HOUR_OF_DAY);
    	int minute = cal.get(Calendar.MINUTE);
    	int second = cal.get(Calendar.SECOND);

    	if(monthNumToAdd >= 0)
    	{
    		year += (month + monthNumToAdd)/12;
    		month = (month + monthNumToAdd)%12;
    	}
    	else
    	{
    		if(month + monthNumToAdd >= 0)
    		{
    			month = month + monthNumToAdd;
    		}
    		else
    		{
    			year += (month + monthNumToAdd)/12 - 1;
    			month = 11 - (0 - month - 1 - monthNumToAdd)%12;
    		}
    	}
    	
    	cal.set(year, month, 1, hour, minute, second);
    	int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    	if(maxDay < day)
    	{
    		day = maxDay;
    	}
    	
    	try
    	{
    		cal.set(year, month, day, hour, minute, second);
    	}
    	catch (Exception e)
    	{
			return null;
		}
    	
    	return cal.getTime();
    }

    //**************************************************
     public static final String replaceStr(String param_source,
                                           String param_char,
                                           String param_replace)
     {
         StringBuffer workStr = new StringBuffer(param_source.length() + 1024);
         int idx, i = 0;

         idx = param_source.indexOf(param_char);
         while(idx != -1)
         {
             //System.out.print(param_source.length());
             workStr.append(param_source.substring(i, idx));
             workStr.append(param_replace);
             i = idx + param_char.length();

             idx = param_source.indexOf(param_char, i);
             //System.out.print(":"+param_source.length());
             //System.out.println("   "+i+","+idx);
         }

         if(i != param_source.length())
         {
             workStr.append(param_source.substring(i));
         }
         //System.out.println(workStr.toString());
         return workStr.toString();
     }

    //**************************************************
     public static final String charTrim(String param_source,
                                         String param_char)
     {
         // Start from the begining
         int frontIdx = 0, rearIdx = param_source.length();
         while(param_source.substring(frontIdx,
                                      frontIdx + 1).equals(param_char))
         {
             frontIdx++;
         }
         while(param_source.substring(rearIdx - 1,
                                      rearIdx).equals(param_char))
         {
             rearIdx--;
         }
         return param_source.substring(frontIdx, rearIdx - 1);
     }

    //**************************************************
     public static final int generateHash(String magicStr)
     {
         int i = magicStr.length();
         char ac[] = new char[i];
         magicStr.getChars(0, i, ac, 0);
         int j = 13;
         for(int k = i - 1; k >= 0; k--)
         {
             j = ac[k] + j * 29;
         }
         if(j < 0)
         {
             j *= -1;
         }
         return j;
     }

    //**************************************************
     public static final String generateKey(int magicInt)
     {
         return Integer.toString(
             magicInt & 0xffff ^ magicInt >> 16 & 0xffff, 16);
     }

    //binary translate
    public static final String get4DBinary(int magicInt)
    {
        String str = Integer.toBinaryString(magicInt);
        int size = str.length();

        if(size < 4)
        {
            for(int i = 4 - size; i > 0; i--)
            {
                str = "0" + str;
            }
        }
        else if(size > 4)
        {
            str = str.substring(size - 4);
        }
        return str;
    }

    //**************************************************
     public static final String getValidStr(String param_str)
     {
         return(getValidStr(param_str, ""));
     }

    //**************************************************
     public static final String getValidStr(String param_str,
                                            String param_default)
     {
         return(param_str != null ?
                (!param_str.trim().equals("") ? param_str.trim() :
                 param_default) : param_default);
     }

    //**************************************************
     public static final String getHexToDec(String HexStr)
     {
         if(HexStr.length() < 6)
         {
             for(int i = 1; i <= (6 - HexStr.length()); i++)
             {
                 HexStr = HexStr + "F";
             }
         }
         return Integer.toString(getH2D(HexStr.substring(0, 1)) * 16 +
                                 getH2D(HexStr.substring(1, 2)));
     }

    //**************************************************
     public static final int getH2D(String Hex00)
     {
         int k = 0;
         char[] buf = Hex00.toCharArray();
         for(int i = 0; i < buf.length; i++)
         {
             int n = getH2D(buf[i]);
             int m = buf.length - i;
             m = (0x10 * (m - 1) * n) + (m == 1 ? n : 0);
             k += m;
             //System.out.println(k + " = "+n+"*"+m);
         }
         return k;
     }

    public static final int getH2D(char Hex00)
    {
        if(Hex00 == 'A' || Hex00 == 'a')
        {
            return 10;
        }
        else if(Hex00 == 'B' || Hex00 == 'b')
        {
            return 11;
        }
        else if(Hex00 == 'C' || Hex00 == 'c')
        {
            return 12;
        }
        else if(Hex00 == 'D' || Hex00 == 'd')
        {
            return 13;
        }
        else if(Hex00 == 'E' || Hex00 == 'e')
        {
            return 14;
        }
        else if(Hex00 == 'F' || Hex00 == 'f')
        {
            return 15;
        }
        return Integer.parseInt(String.valueOf(Hex00));
    }

    //**************************************************
     public static long getSeed(String s1)
     {
         long seed = 0L;
         for(int i = 0; i < s1.length() - 1; i++)
         {
             seed += s1.charAt(i) * s1.charAt(i + 1);
         }

         while(seed < 0x2710)
         {
             seed <<= 2;
         }
         return seed;
     }

 	public final static long Ks = 1024;
 	public final static long Ms = 1024*Ks;
 	public final static long Gs = 1024*Ms;
 	public final static long Ts = 1024*Gs;
 	public final static long Ps = 1024*Ts;
 	/**
 	 * 
 	 * @param length
 	 * @return
 	 */
 	public static String bytesScale(long length)
 	{
 		double size = length;
 		StringBuffer sb = new StringBuffer();
         if( size < Ms )
         {
         	size = size/Ks;
         	sb.append(Tools.DF.format(size) + "K");
         }
         else if( size < Gs )
         {
         	sb.append(Tools.DF.format(size/Ms) + "M");
         }
         else if( size < Ts )
         {
         	sb.append(Tools.DF.format(size/Gs) + "G");
         }
         else
         {
         	sb.append(Tools.DF.format(size/Ts) + "T");
         }
         return sb.toString();		
 	}
    /**
     * 缺省空格分隔
     * @param str
     * @return
     */
    public static String[] split(String str)
    {
    	//dci       3424 83.2  7.3 13105324 1193664 ?    Sl   21:10  31:58 /home/dci/mongodb/bin/mongod --config /home/dci/mongodb/mongodb.conf
        if( str == null || str.length() == 0 )
        {
            return new String[0];
        }
        ArrayList<String>  splits = new ArrayList<String>();
        boolean chunk = false;
        StringBuffer buf = new StringBuffer();
        for( int i = 0; i < str.length(); i++ )
        {
        	char c = str.charAt(i);
        	if( chunk )
        	{
        		if( c == ' ' )
        		{
        			splits.add(buf.toString());
        			buf = new StringBuffer();
        			chunk = false;
        		}
        		else
            		buf.append(c);
        	}
        	else
        	{
        		if( c == ' ' ) continue;
        		buf.append(c);
        		chunk = true;
        	}
        }
        if( buf.length() > 0 )
			splits.add(buf.toString());
        String[] args = new String[splits.size()];
        return splits.toArray(args);
    }
    /*
     * Javascript Split function
     */
    public static String[] split(String str, String s)
    {
        if( str == null || str.length() == 0 )
        {
            return new String[0];
        }
        int count = 0;
        int i = 0, bi = 0, ei = 0;
        while(ei != -1)
        {

            ei = str.indexOf(s, bi);
            bi = ei + s.length();
            count++;
        }

        ei = 0;
        bi = 0;
        String[] splits = new String[count];
        while(ei != -1)
        {
            ei = str.indexOf(s, bi);
            if(ei == -1)
            {
                splits[i++] = str.substring(bi).trim();
            }
            else
            {
                splits[i++] = str.substring(bi, ei).trim();
            }
            bi = ei + s.length();
        }

        return splits;
    }

    public static final void copyFile(File src, File dest) throws Exception
    {
        if(!src.exists())
        {
            throw new Exception("要拷备的源文件不存在:" + src.getPath());
        }

        if(dest.exists())
        {
            if(!dest.delete())
            {
                throw new Exception("要拷备的目的文件不能被删除:" + dest.getPath());
            }
        }

        try
        {
            FileInputStream in = new FileInputStream(src);
            byte buf[] = new byte[in.available()];
            in.read(buf);
            in.close();
            FileOutputStream out = new FileOutputStream(dest, false);
            out.write(buf);
            out.close();
            dest.setLastModified(src.lastModified());
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    /*
     * Copy byte array to a file
     */
    public static int copyByteArray(byte[] source, byte[] target, int begin)
    {
        if(source == null || target == null)
        {
            return -1;
        }
        
        if( target.length < source.length+begin ){
        	return -1;
        }

        if(source.length + begin > target.length)
        {
            for(int i = begin; i < target.length; i++)
            {
                target[i] = source[i];
            }
            return target.length;
        }
        else
        {

            for(int i = begin, j = 0; j < source.length; i++, j++)
            {
                target[i] = source[j];
            }
            return begin + source.length;
        }
    }

    /*
     * Copy byte array to a file
     */
    public static int copyByteArray(byte[] source, byte[] target)
    {
        if(source == null || target == null)
        {
            return -1;
        }

        if(source.length > target.length)
        {
            for(int i = 0; i < target.length; i++)
            {
                target[i] = source[i];
            }
            return target.length;
        }

        for(int i = 0; i < source.length; i++)
        {
            target[i] = source[i];
        }

        return source.length;
    }

    /**
     * 拷贝字节码流到字符串，以0x00结尾
     * @param payload byte[]
     * @param offset int
     * @param len int
     * @return String
     */
    public static String copyBytes2Str( byte[] payload, int offset, int len, String charSet )
    {
        int i = 0;
        for( ; i < len; i++ )
        {
            if( payload[ offset+i ] == 0 )
            {
                break;
            }
        }
        String ret = null;
        try
        {
        	ret = new String( payload, offset, i ,charSet);
        }
        catch (Exception e)
        {
			// TODO: handle exception
		}
        return ret;
    }
    
    /**
     * 将 iso-8859-1 串转换成本地串
     * @param s, 输入串
     * @return 新的字符串
     */
    public static final String toLocal(String source)
    {
        String target = getValidStr(source);
        try
        {
            target = new String(source.getBytes(), "iso-8859-1");
        }
        catch(Exception e)
        {
        }
        return target;
    }

    public static final void intToBytes(int s, byte[] buf, int offset,
                                        int length)
    {
        for(int i = length - 1; i >= 0; i--)
        {
            //按位运算
            if(s != 0)
            {
                buf[offset + i] = (byte) (s & 0xFF);
                s >>= 8;
            }
            else
            {
                buf[offset + i] = 0;
            }
        }
    }

    public static final void intToBytes(int s, byte[] buf)
    {
        for(int i = buf.length - 1; i >= 0; i--)
        {
            if(s == 0)
            {
            	buf[i] = 0;
            }
            else
            {
                buf[i] = (byte) (s & 0xFF);
                s >>= 8;
            }
        }
    }

    public static final void longToBytes(long s, byte[] buf)
    {
        for(int i = buf.length - 1; i >= 0; i--)
        {
            if(s == 0)
            {
            	buf[i] = 0;
            }
            else
            {
                buf[i] = (byte) (s & 0xFF);
                s >>= 8;
            }
        }
    }
    
    public static final void longToBytes(long s, byte[] buf, int offset, int len)
    {
        for(int i = offset + len - 1; i >= offset; i--)
        {
            if(s == 0)
            {
            	buf[i] = 0;
            }
            else
            {
                buf[i] = (byte) (s & 0xFF);
                s >>= 8;
            }
        }
    }
    /**
     * 将整形数转换成为字节数
     * @param s
     * @return
     */
    public static final byte[] intToBytes(int s, int size)
    {
        byte[] buf = new byte[size];
        for(int i = size - 1; i >= 0; i--)
        {
            if(s == 0)
            {
            	buf[i] = 0;
            }
            else
            {
                buf[i] = (byte) (s & 0xFF);
                s >>= 8;
            }
        }
        return buf;
    }

    public static final byte[] intToBytes(int s)
    {
        return intToBytes(s, 4);
    }

    /**
     * 将字节流转换成无符号的短整形
     * @param b
     * @return
     */
    public static final short bytesToShort(byte[] b)
    {
        return(short) bytesToInt(b);
    }

    /**
     * 将字节流转换成整形数
     * @param b
     * @return
     */
    public static final int bytesToInt(byte[] b)
    {
        int s = 0;
        for(int pos = 0; pos < b.length; pos++)
        {
            int iTemp = b[pos] < 0 ? (0x100 + b[pos]) : b[pos];
            s += iTemp << (8 * (b.length - pos - 1));
        }
        return s;
    }

    /**
     *
     * @param b数据缓存
     * @param offset从数据的那个位置开始处理
     * @param length处理数据的长度
     * @return
     */
    public static final int bytesToInt(byte[] b, int offset, int length)
    {
        int s = 0;
        for(int i = 0; i < length; i++)
        {
            int iTemp = b[offset + i] < 0 ? (0x0100 + b[offset + i]) : b[offset + i];
            s += iTemp << (8 * (length - i - 1));
        }
        return s;
    }

    /**
     *
     * @param b数据缓存
     * @param offset从数据的那个位置开始处理
     * @param length处理数据的长度
     * @return
     */
    public static final long bytesToLong(byte[] b, int offset, int length)
    {
        long s = 0;
        for(int i = 0; i < length; i++)
        {
        	long iTemp = b[offset + i] < 0 ? (0x0100 + b[offset + i]) : b[offset + i];
            s += iTemp << (8 * (length - i - 1));
        }
        return s;
    }

    public static final long bytesToLong(byte[] b, int offset)
    {
    	StringBuffer sb = new StringBuffer();;
    	for( int i = 0; i < 8; i++ )
    	{
    		int temp = b[offset + i] < 0 ? (0x0100 + b[offset + i]) : b[offset + i];
            if(temp < 0x10)
            {
                sb.append('0');
            }
    		sb.append(Integer.toHexString(temp));
    	}
    	return Long.parseLong(sb.toString(), 16);
    }
    /**
     *
     * @param buffer
     * @param off
     * @param length
     * @return
     */
    public static final byte[] truncate(byte[] buffer, int off, int length)
    {
        int nSize = 0;
        if(buffer.length < length + off)
        {
            nSize = buffer.length - off;
        }
        else
        {
            nSize = length;
        }

        byte[] buf = new byte[nSize];
        for(int i = 0; i < nSize; i++)
        {
            buf[i] = buffer[off + i];
        }
        return buf;
    }

    public static final String getIP(int ip)
    {
        return getIP(Tools.intToBytes(ip, 4));
    }

    public static final String getIP(byte[] buf)
    {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < buf.length; i++)
        {
            buffer.append(buf[i] < 0 ? (0x0100 + buf[i]) : buf[i]);
            if(i + 1 < buf.length)
            {
                buffer.append(".");
            }
        }

        return buffer.toString();
    }

    /**
     * 得到持续时间，如果
     * @param duration
     * @return d天 HH:mm:ss
     */
    public static final String getDuration(long duration)
    {
//        duration = System.currentTimeMillis() - duration;
//        if(duration > 0)
//        {
//            return(duration / MILLI_OF_DAY) + "天 " +
//                getSmartTime( (int) (duration % MILLI_OF_DAY) / 1000);
//        }
//        return "0天00:00:00";

		if (duration < 1000)
		{
			return duration + "ms.";
		}
		long s = duration / 1000;
		String runtime = s + "s.";
		if (s < 60)
		{
			runtime = s + "s.";
		}
		else if (s < 60 * 60)
		{
			long m = s / 60;
			s = s % 60;
			runtime = m + "m," + s + "s.";
		}
		else if (s < 24 * 60 * 60)
		{
			long h = s / 3600;
			long m = s % 3600 / 60;
			runtime = h + "h," + m + "m.";
		}
		else if (s >= 24 * 60 * 60)
		{
			long d = s / (24 * 3600);
			long h = s % (24 * 3600) / 3600;
			runtime = d + "d," + h + "h.";
		}
		return runtime;
    }

    public static final String getDuration(int duration)
    {
        duration = current() - duration;
        if(duration > 0)
        {
            return(duration / SECOND_OF_DAY) + "天 " +
                getSmartTime(duration % SECOND_OF_DAY);
        }
        return "0天00:00:00";
    }

    public static final int current()
    {
        return(int) (System.currentTimeMillis() / 1000);
    }

    public static final String toUnicode(byte[] buf)
    {
        int i = buf.length / 2;
        char[] cs = new char[i];
        int j;
        for(j = 0; j < i; j++)
        {
            cs[j] = (char) (buf[2 * j + 1] & 0xff);
            cs[j] <<= 8;
            cs[j] += (char) (buf[2 * j] & 0xff);
        }
        return new String(cs);
        /*       try
               {
                   return  new String(buf, "Unicode");
               }
               catch(Exception e)
               {
                   return "";
               }*/
    }

    public static final byte[] toUnicode(String source)
    {
        byte[] des = null;
        int bytesCount = source.length() * 2;
        if(bytesCount >= 1)
        {
            des = new byte[bytesCount];
            char charTemp;
            for(int index = 0; index < source.length(); index++)
            {
                charTemp = source.charAt(index);
                des[index * 2 + 1] = (byte) (charTemp >>> 8 & 0xff);
                des[index * 2] = (byte) (charTemp & 0xff);
            }
        }
        else
        {
            des = new byte[0];
        }
        return(des);
        /*       try
               {
                   return str.getBytes("Unicode");
               }
               catch(Exception e)
               {
                   return new byte[0];
               }*/
    }

    public static final void printb(byte[] buffer)
    {
        printb(buffer, 0, buffer.length);
    }

    public static String getBinaryString(byte[] buffer, int offset, int length)
    {
        StringBuffer sb = new StringBuffer();
        for(int i = offset, j = 0; i < buffer.length && j < length; i++, j++)
        {
            sb.append("0x");
            int k = buffer[i] < 0 ? 256 + buffer[i] : buffer[i];
            if(k < 0x10)
            {
                sb.append('0');
            }
            sb.append(Integer.toHexString(k));
            sb.append(" ");

            if(i % 8 == 7)
            {
                sb.append(" ");
            }
            if(i % 16 == 15)
            {
                sb.append('\n');
            }

            if(i + 1 == buffer.length)
            {
                if(i % 16 != 0)
                {
                    sb.append('\n');
                }
                return sb.toString();
            }
        }
        return sb.toString();
    }
    
    public static final void printb(byte[] buffer, int offset, int length)
    {
        StringBuffer sb = new StringBuffer();
        for(int i = offset, j = 0; i < buffer.length && j < length; i++, j++)
        {
            //System.out.println("if( i + 1("+(i+1)+") == buffer.length("+buffer.length+") )  ");
            sb.append("0x");
            int k = buffer[i] < 0 ? 256 + buffer[i] : buffer[i];
            if(k < 0x10)
            {
                sb.append('0');
            }
            sb.append(Integer.toHexString(k));
            sb.append(" ");

            if(i % 8 == 7)
            {
                sb.append(" ");
            }
            if(i % 16 == 15)
            {
                sb.append('\n');
            }

            if(i + 1 == buffer.length)
            {
                if(i % 16 != 0)
                {
                    sb.append('\n');
                }
                System.out.println(sb.toString());
                return;
            }
        }
        System.out.println(sb.toString());
    }

    public static final String getUnicode(byte[] buffer)
    {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < buffer.length; i++)
        {
            if(i % 2 == 0)
            {
                buf.append("\\u");
            }
            buf.append(Integer.toHexString(buffer[i] < 0 ?
                                           0x0100 + buffer[i] :
                                           buffer[i]));
        }
        return buf.toString();
    }

    /**
     * 得到生日
     * @param age
     * @return
     */
    public static final int getBirthday(int age)
    {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR) - age;
        now.set(year, 0, 1);
        return(int) (now.getTimeInMillis() / 1000);
    }

    public static final int getAge(int birthday)
    {
        return getAge( ( (long) birthday) * 1000);
    }

    public static final int getAge(long birthday)
    {
        java.util.Calendar now = java.util.Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        now.setTimeInMillis(birthday);
        return year - now.get(Calendar.YEAR);
    }

    public static final String getBytesString(byte[] buffer)
    {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < buffer.length; i++)
        {
            sb.append(Integer.toHexString(buffer[i] < 0 ?
                                          0x0100 + buffer[i] :
                                          buffer[i]));
            sb.append(" ");
        }

        return sb.toString();
    }

    public static java.text.DecimalFormat DFORMAT = new java.text.DecimalFormat(
        "0.00 KB");
    public static java.text.DecimalFormat DF = new java.text.DecimalFormat(
        "0.00");
    
    public static java.text.DecimalFormat DF_PERCENT_DOUBLE = new java.text.DecimalFormat("0.0000");
    public static final String getDecimalStr(int d)
    {
        double d1 = d;
        return DF.format(d1 / 100);
    }

    public static final String getKBStr(long size)
    {
        double d = size;
        return DFORMAT.format(d / 1024);
    }

    public static final String getDoubleForPercent(double d)
    {
    	return DF_PERCENT_DOUBLE.format(d);
    }
    
    public static String trim(String content)
    {
        StringBuffer newContent = new StringBuffer(content.trim());
        int i = 0;
        for(; i < newContent.length(); i++)
        {
            if(newContent.charAt(i) != 0x20 &&
               newContent.charAt(i) != '\t' )
            {
                break;
            }
        }
        return newContent.substring(i);
    }

    public static String getUniqueValue()
    {
        StringBuffer stringbuffer = new StringBuffer();
        int code = stringbuffer.hashCode() >> 8;
        stringbuffer.append(Integer.toHexString(code)).
            append(Long.toHexString(System.currentTimeMillis() >> 16));
        String timestamp = String.valueOf(System.currentTimeMillis());
        stringbuffer.append(timestamp.substring(timestamp.length()-3));
        return stringbuffer.toString();
    }

    public static final String checkHttpUri(String url, String prefix)
    {
//System.out.print(url);
        String u = url;
        if(u == null)
        {
            return null;
        }
        u = u.trim();
        if(u.length() == 0)
        {
            return null;
        }
        //u = u.toLowerCase();

        int k = 0, j = 0;
        if(u.length() > k && u.charAt(k) == 'h')
        {
            k += 1;
        }
        if(u.length() > k && u.charAt(k) == 't')
        {
            k += 1;
        }
        if(u.length() > k && u.charAt(k) == 't')
        {
            k += 1;
        }
        if(u.length() > k && u.charAt(k) == 'p')
        {
            k += 1;
        }
        if(u.length() > k && u.charAt(k) == ':')
        {
            k += 1;
            j += 1;
        }
        if(u.length() > k && u.charAt(k) == '/')
        {
            k += 1;
            j += 1;
        }
        if(u.length() > k && u.charAt(k) == '/')
        {
            k += 1;
            j += 1;
        }

//System.out.println(j+","+k);
        if(j >= 2 && k < 4)
        {
            k = 0;
        }
        if(j < 2 && k < 5)
        {
            k = 0;
        }
        if(j == 0)
        {
            k = 0;
        }
        if(k >= u.length())
        {
            return null;
        }
        if(j >= u.length())
        {
            return null;
        }
        //System.out.println(j+","+k);
        if(u.charAt(k) == '/')
        {
            k += 1;
        }

        if(k != 7)
        {
            u = prefix + u.substring(k);
        }

        int i = u.lastIndexOf("/");
//System.out.println(i+" , "+u.substring(0, i));
        if(i == u.length() - 1)
        {
            u = u.substring(0, i);
        }
//System.out.println(u);
        return u;
    }

    public static final String decodeUnicode(String src, char sper)
    {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while(i < src.length())
        {
            char c = src.charAt(i++);
            //System.out.println(c);
            if(c == sper)
            {
                String s = src.substring(i, i + 2);
                //System.out.println(s);
                c = (char) Tools.getH2D(s);
                i += 2;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static final String checkHttpUri(String url)
    {
        String u = url;
        if(u == null)
        {
            return null;
        }
        u = u.trim();
        if(u.length() == 0)
        {
            return null;
        }
        //u = u.toLowerCase();

        int k = 0, j = 0;
        if(u.length() > k && u.charAt(k) == 'h')
        {
            k += 1;
        }
        if(u.length() > k && u.charAt(k) == 't')
        {
            k += 1;
        }
        if(u.length() > k && u.charAt(k) == 't')
        {
            k += 1;
        }
        if(u.length() > k && u.charAt(k) == 'p')
        {
            k += 1;
        }
        if(u.length() > k && u.charAt(k) == ':')
        {
            k += 1;
            j += 1;
        }
        if(u.length() > k && u.charAt(k) == '/')
        {
            k += 1;
            j += 1;
        }
        if(u.length() > k && u.charAt(k) == '/')
        {
            k += 1;
            j += 1;
        }

//System.out.println(j+","+k);
        if(j >= 2 && k < 4)
        {
            k = 0;
        }
        if(j < 2 && k < 5)
        {
            k = 0;
        }
        if(j == 0)
        {
            k = 0;
        }
        if(k >= u.length())
        {
            return null;
        }
        if(j >= u.length())
        {
            return null;
        }
        //System.out.println(j+","+k);
        if(u.charAt(k) == '/')
        {
            k += 1;
        }
        u = "http://" + u.substring(k);
//System.out.println(u);
        int i = u.lastIndexOf("/");
//System.out.println(i+" , "+u.substring(0, i));
        if(i == u.length() - 1)
        {
            u = u.substring(0, i);
        }
        return u;
    }

    /**
     * 从文件中获取消息内容
     * @param fileName
     * @return
     * @throws IOException
     */
    public static final byte[] readFromFile(File file) throws IOException
    {
        FileInputStream fin = new FileInputStream(file);
        byte[] buf = new byte[fin.available()];
        fin.read(buf);
        fin.close();
        return buf;
    }

    public static final byte[] readFirstLineFromFile(File file) throws IOException
    {
        FileInputStream fin = new FileInputStream(file);
        java.io.ByteArrayOutputStream out = new ByteArrayOutputStream();
        int c = 0;
        while( (c = fin.read() ) != -1 )
        {
        	if( c == '\r' || c == '\n' )
        	{
        		break;
        	}
        	out.write(c);
        }
        fin.close();
        return out.toByteArray();
    }
//
//    //判断是否合法的地址
//    public static boolean isIPAddress(String strNum)
//    {
//        boolean b = true;
//        PatternCompiler compiler = new Perl5Compiler();
//        try
//        {
//            Pattern pattern = compiler.compile(
//                "(\\d{1,3})(\\.)(\\d{1,3})(\\.)(\\d{1,3})(\\.)(\\d{1,3})");
//            PatternMatcher matcher = new Perl5Matcher();
//            b = matcher.matches(strNum, pattern);
//            strNum = strNum.replace('.', ':');
//            String[] fromStrs = strNum.split(":");
//
//            //逐个检查每位是否小于255。
//            for (int i = 0; i < fromStrs.length; i++)
//            {
//                if (Integer.valueOf(fromStrs[i]).intValue() > 255)
//                {
//                    b = false;
//                }
//            }
//        }
//        catch (Exception e)
//        {
//        }
//        return b;
//    }
    
    /**
     * 
     * 输入内涵日期的字符串，返回日期的格式类型
     * return yyyyMM yyyyMMdd yyyyMMddHH 
     */
    public static String getDatePattern(String input)
    {
    	final String yyyyMMdd = "^.*[\\-\\_]((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";
		try
		{
			Pattern pattern = Pattern.compile(yyyyMMdd);
			Matcher isNum = pattern.matcher( input );
	        if( isNum.matches() )
	        {
	            return isNum.group();
	        }
		}
		catch (Exception e)
		{
		}
    	return "";
    }

    /**
     * 返回输入的路径文件是否存在
     * @param path
     * @return
     */
    public static final boolean isValidFile(String path)
    {
        File file = new File(path);
        if(file.exists())
        {
            return true;
        }
        return false;
    }

    /**
     *
     * @param folder
     * @return
     */
    public static final boolean createFolder(String folder)
    {
        File fd = new File(folder);
        if(fd.exists() && fd.isDirectory())
        {
            return true;
        }

        return fd.mkdir();
    }

    public static final boolean deleteAll(File dir)
    {
        File files[] = dir.listFiles();
        for(int i = 0; i < files.length; i++)
        {
            if(files[i].isFile())
            {
                if(!files[i].delete())
                {
                    return false;
                }
            }
            else
            {
                if(!deleteAll(files[i]))
                {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private final static String[] hexDigits =
        {
        "0", "1", "2", "3", "4", "5", "6",
        "7",
        "8", "9", "a", "b", "c", "d", "e",
        "f"};

    /**
     * 转换字节数组为16进制字串
     * @param b 字节数组
     * @return 16进制字串
     */
    public static String byteArrayToString(byte[] b)
    {
        StringBuffer resultSb = new StringBuffer();
        for(int i = 0; i < b.length; i++)
        {
            resultSb.append(byteToHexString(b[i])); //若使用本函数转换则可得到加密结果的16进制表示，即数字字母混合的形式
            //resultSb.append(byteToNumString(b[i]));//使用本函数则返回加密结果的10进制数字字串，即全数字形式
        }
        return resultSb.toString();
    }

    public static String byteArrayToString(byte[] b, int offset, int len)
    {
        StringBuffer resultSb = new StringBuffer();
        for(int i = 0; i < len; i++)
        {
            resultSb.append(byteToHexString(b[offset++])); //若使用本函数转换则可得到加密结果的16进制表示，即数字字母混合的形式
            //resultSb.append(byteToNumString(b[i]));//使用本函数则返回加密结果的10进制数字字串，即全数字形式
        }
        return resultSb.toString();
    }

    private static String byteToNumString(byte b)
    {
        int _b = b;
        if(_b < 0)
        {
            _b = 256 + _b;
        }

        return String.valueOf(_b);
    }

    private static String byteToHexString(byte b)
    {
        int n = b;
        if(n < 0)
        {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static String encodeMD5(String origin)
    {
        String resultString = null;

        try
        {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToString(md.digest(resultString.getBytes()));
        }
        catch(Exception ex)
        {

        }
        return resultString;
    }

    public static String fillPathEnds(String path)
    {
        path = path.trim();
        if(path.endsWith("/") || path.endsWith("\\"))
        {
            return path;
        }
        return path + "/";
    }

    public static String host2Path(String host)
    {
        StringBuffer sb = new StringBuffer();
        String splits[] = Tools.split(host, ".");
        for(int i = splits.length - 1; i >= 0; i--)
        {
            sb.append(splits[i]);
            if(i > 0)
            {
                sb.append("/");
            }
        }
        return sb.toString();
    }
    
    /**
     * 将字符串编码Unicode
     * @param src
     * @return
     */
    public static final String encodeUnicode(String src)
    {
        StringBuffer s = new StringBuffer();
        if (src != null && !"".equals(src)) {
            String hex = "";

            for (int i = 0; i < src.length(); i++) {
                hex = Integer.toHexString((int) src.charAt(i));
                if (hex.length() < 4) {
                    while (hex.length() < 4) {
                        hex = "0".concat(hex);
                    }
                }
                hex = hex.substring(2, 4).concat(hex.substring(0, 2));
                s.append(hex.toUpperCase());
            }
        }
        return s.toString();
//        byte[] buffer = code.getBytes();
//        StringBuffer buf = new StringBuffer();
//        for(int i = 0; i < buffer.length; i++)
//        {
//            buf.append(Integer.toHexString(buffer[i] < 0 ? 0x0100 +
//                                           buffer[i] : buffer[i]));
//        }
//        return buf.toString();
    }

    /**
     * 将Unicde字符串解码成正常可读数据
     * @param src
     * @return
     */
    public static final String decodeUnicode(String src)
    {
        if (src == null || src.isEmpty()) {
            return src;
        }
        String tempStr = "";
        String returnStr = "";

        // 将编码过的字符串进行重排
        for (int i = 0; i < src.length() / 4; i++) {
            if (0 == i) {
                tempStr = src.substring(4 * i + 2, 4 * i + 4);
                tempStr += src.substring(4 * i, 4 * i + 2);
            } else {
                tempStr += src.substring(4 * i + 2, 4 * i + 4);
                tempStr += src.substring(4 * i, 4 * i + 2);
            }
        }

        byte[] b = new byte[tempStr.length() / 2];

        try {
            // 将重排过的字符串放入byte数组，用于进行转码
            for (int j = 0; j < tempStr.length() / 2; j++) {
                String subStr = tempStr.substring(j * 2, j * 2 + 2);
                int b1 = Integer.decode("0x" + subStr).intValue();
                b[j] = (byte) b1;
            }
            // 转码
            returnStr = new String(b, "utf-16");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            return src;
        }
        return returnStr;
//        byte buffer[] = new byte[code.length() / 2];
//        if(code.length() % 2 != 0)
//        {
//            return code;
//        }
//        for(int i = 0; i < code.length(); i += 2)
//        {
//            char c1 = code.charAt(i);
//            char c2 = code.charAt(i + 1);
//            int b = Tools.getH2D(c1) * 16 + Tools.getH2D(c2);
//            buffer[i / 2] = (byte) b;
//        }
//        return new String(buffer);
    }

    public static final String unicode(byte[] buffer)
    {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < buffer.length; i++)
        {
            buf.append(Integer.toHexString(buffer[i] < 0 ? 0x0100 +
                                           buffer[i] : buffer[i]));
        }
        return buf.toString();
    }

    public static final byte[] unicode(String code)
    {
        byte buffer[] = new byte[code.length() / 2];
        if(code.length() % 2 != 0)
        {
            return null;
        }
        for(int i = 0; i < code.length(); i += 2)
        {
            char c1 = code.charAt(i);
            char c2 = code.charAt(i + 1);
            int b = Tools.getH2D(c1) * 16 + Tools.getH2D(c2);
            buffer[i / 2] = (byte) b;
        }
        return buffer;
    }
    
    public static String getLinkMapFile(String uri)
    {
        StringBuffer sb = new StringBuffer();
        try
        {
            URL url = new URL(uri);
//            System.out.println( "url.getFile():" + url.getFile() );
//            System.out.println( "url.getQuery():" + url.getQuery() );
//            System.out.println( "url.getRef():" + url.getRef() );
//            System.out.println( "url.getUserInfo():" + url.getUserInfo() );
            String file = url.getFile();
            String query = url.getQuery();
            String ref = url.getRef();
            String userInfo = url.getUserInfo();

            //处理文件名称问题
            int fromIndex = file.length() - 1;
            if(query != null)
            {
                fromIndex = file.indexOf(query);
            }
            int k = file.lastIndexOf("/", fromIndex);
            if(file == null || file.length() == 0 ||
               k == file.length() - 1)
            {
                sb.append("index.html");
            }
            else if(k != -1)
            {
                sb.append(file.substring(k + 1));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getLinkMapPath(String uri)
    {
        StringBuffer sb = new StringBuffer();
        try
        {
            URL url = new URL(uri);
            String protocol = url.getProtocol();
            String host = host2Path(url.getHost());
            String path = url.getPath();

            sb.append(protocol);
            sb.append("/");
            sb.append(host);
            if(url.getPort() != -1)
            {
                sb.append("/:");
                sb.append(url.getPort());
            }

            int k = path.lastIndexOf('/');
            if(k != -1)
            {
                sb.append(path.substring(0, k));
                sb.append("/");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static final String currentFormatTime(String fomat)
    {
        return Tools.getFormatTime(fomat, System.currentTimeMillis());
    }

    public static final String currentFormatTime()
    {
        return Tools.getFormatTime("yyyy年MM月dd日", System.currentTimeMillis());
    }

    public static final int readFontSize(String font)
    {
        if(font == null)
        {
            return 0;
        }
        int size = 0;

        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < font.length(); i++)
        {
            char ch = font.charAt(i);
            if(ch >= '0' && ch <= '9')
            {
                sb.append(ch);
            }
        }

        if(sb.length() > 0)
        {
            try
            {
                size = Integer.parseInt(sb.toString());
            }
            catch(Exception e)
            {
            }
        }
        return size;
    }

    private static String fetchTagName()
    {
        String s1 = " \n\ra ";
        int state = 0;
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < s1.length() && state < 2; i++)
        {
            char ch = s1.charAt(i);
            System.out.print(ch);
            System.out.print(' ');
            switch(state)
            {
                case 0:
                    if(ch != 0x0d && ch != 0x0a && ch != ' ')
                    {
                        sb.append(ch);
                        state = 1;
                    }
                    break;
                case 1:
                    if(ch != ' ')
                    {
                        sb.append(ch);
                    }
                    else
                    {
                        state = 2;
                    }
                    break;
            }
        }
        System.out.println();
        return sb.toString();
    }

    /**
     * 转换字符串到整形
     * @param s String
     * @return int
     */
    public static double convertStr2Double(String s, double def)
    {
        if(s == null)
        {
            return def;
        }

        try
        {
            return Double.parseDouble(s);
        }
        catch(Exception e)
        {
        }
        return def;
    }

    /**
     * 转换字符串到整形
     * @param s String
     * @return int
     */
    public static int convertStr2Int(String s, int def)
    {
        if(s == null)
        {
            return def;
        }
        s = s.trim();
        if(s.length() == 0)
        {
            return def;
        }

        try
        {
            return Integer.parseInt(s);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return def;
    }

    public static byte[] StringToByteArray(String str)
    {
        char[] buf = str.toCharArray();
        if(buf.length % 2 != 0)
        {
            return null;
        }

        byte[] payload = new byte[buf.length / 2];

        for(int i = 0; i < buf.length; i += 2)
        {
            int b = getH2D(buf[i]) * 0x10 + getH2D(buf[i + 1]);
            payload[i / 2] = (byte) b;
        }

        return payload;
    }

    public static int StringToByteArray(String str, byte[] payload, int offset)
    {
        char[] buf = str.toCharArray();
        if(buf.length % 2 != 0)
        {
            return offset;
        }

        for(int i = 0; i < buf.length; i += 2)
        {
            int b = getH2D(buf[i]) * 0x10 + getH2D(buf[i + 1]);
            payload[offset++] = (byte) b;
        }
        return offset;
    }

    public static Date str2Date(String date, String pattern)
    {
        if(date == null)
        {
            return null;
        }
        SimpleDateFormat sdf = null;
        if(pattern == null)
        {
            sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        }
        else
        {
            sdf = new SimpleDateFormat(pattern);
        }

        Date d = null;
        try
        {
            d = sdf.parse(date);

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return d;
    }

//    public static String encodeWapPush(String subject, String url)
//    {
//        StringBuffer sb = new StringBuffer();
//        try
//        {
//            url = url.trim().toLowerCase();
//            if(url.startsWith("http://"))
//            {
//                url = url.substring(7);
//            }
//            sb.append("02056A0045C6080C03");
//            sb.append(byteArrayToString(url.getBytes()));
//            sb.append("00");
//            sb.append("0AC30720");
//            sb.append(getFormatTime("yyMMddHHmmss", current()));
//            sb.append("10C30420");
//            sb.append(getFormatTime("yyMMdd", current()));
//            sb.append("0103");
//            sb.append(byteArrayToString(subject.getBytes("UTF-8")));
//            sb.append("000101");
//        }
//        catch(UnsupportedEncodingException ex)
//        {
//        }
//        int len = sb.length() / 2;
//        sb.insert(0, "0605040B8423F0810606038D" + Integer.toHexString(len));
//        return sb.toString();
//    }

    public static boolean isNumeric( String str )
    {
    	if( str == null || str.length() == 0 )
    	{
    		return false;
    	}
        Pattern pattern = Pattern.compile( "^(-?\\d+)(\\.\\d+)?$" );//"^(0|[1-9][0-9]*|-[1-9][0-9]*)$" );
        Matcher isNum = pattern.matcher( str );
        if( !isNum.matches() )
        {
            return false;
        }
        return true;
    }

    /**
     * 将格式“xx 天 xx 小时 xx 分 xx 秒”的时间转换成秒
     *
     * @param startTime
     * @return
     */
    public static long getStartTime(String startTime)
    {
        long time = 0;

        if("".equals(startTime))
        {
            return time;
        }

        String day = "";
        String hour = "";
        String minute = "";
        String second = "";

        int index = 0;

        if(startTime.indexOf("天") != -1)
        {
            index = startTime.indexOf("天");
            day = startTime.substring(0, index - 1).trim();
        }

        if(startTime.indexOf("小时") != -1)
        {
            index = startTime.indexOf("小时");
            hour = startTime.substring(index - 3, index - 1).trim();
        }

        if(startTime.indexOf("分") != -1)
        {
            index = startTime.indexOf("分");
            minute = startTime.substring(index - 3, index - 1).trim();
        }

        if(startTime.indexOf("秒") != -1)
        {
            index = startTime.indexOf("秒");
            second = startTime.substring(index - 3, index - 1).trim();
        }

        time = Long.valueOf(day).longValue() * SECOND_OF_DAY + Long.valueOf(hour).longValue() * SECOND_OF_HOUR + Long.valueOf(minute).longValue() * SECOND_OF_MINUTE + Long.valueOf(second).longValue();

        return time;
    }

	/**
	 * 得到实体的类型
	 * @param entry
	 * @return
	 */
    public static String getFileType(String filename)
    {
    	filename = filename.toLowerCase();
        if (filename.indexOf("gif")!=-1)
            return "gif";
        if (filename.indexOf("tif")!=-1)
            return "tif";
        if (filename.indexOf("tiff")!=-1)
            return "tif";
        if (filename.indexOf("jpg")!=-1 || filename.indexOf("jpeg")!=-1 )
            return "jpg";
        if ( filename.indexOf("bmp")!=-1)
            return "jpg";
        return "";
    }
    
    public static void main(String[] args)
    {
    	String timestamp = String.valueOf(System.currentTimeMillis());
    	System.out.println(timestamp.substring(timestamp.length()-3));
//    	String link = "http://www.xinhuanet.com/ent/2019-03/11/c_1124217755.htm";
//    	String src = "x.png";
//		int i = link.lastIndexOf("/");
//		if( i != -1 ){
//			src = link.substring(0, i+1)+src;
//		}
//		System.out.println(src);
//    	System.err.println(location("103.92377","30.57447", "公司"));
//    	String s = Tools.os_exec(new String[]{"netstat","-aon", "|findstr \"7528\""}, "GBK");
//    	System.err.println(s);
//    	args = Tools.split("	            mysqld.exe                   27196 Console                    2    171,788 K	            ".trim());
//    	for( String arg : args ){
//    		System.err.println(arg);
//    	}
//    	System.err.println(Tools.isNumeric("0.33"));
//    	System.out.println(Tools.isNumeric("-35.55"));
//    	System.err.println(Tools.isNumeric("1"));
//    	System.out.println(Tools.isNumeric("-2"));
//    	System.out.println(Tools.isNumeric("asdf"));
//    	String logfile = "/log/";
//		StringBuffer path = new StringBuffer(logfile);
//		if( logfile.startsWith("../") )
//		{
//			path.delete(0, 3);
//		}
//		if( path.indexOf("*") != -1 )
//		{
//			int i = path.lastIndexOf("/");
//			if( i > 2 )
//			{
//				path.delete(i, path.length());
//			}
//		}
//		else
//		{
//			if(path.charAt(path.length()-1)=='/')
//			{
//				path.deleteCharAt(path.length()-1);
//			}
//		}
//		System.err.println(path);
		
//    	String datas[] = Tools.split("total kB        69281528 4022868  439744");
//    	int i = 0;
//    	for(String data:datas)
//    		System.out.println("【"+(i++)+"】"+data);
//    	System.out.println(countChar(" jdbc:h2:tcp://10.10.10.92:9092,localhost:9092/../h2/cos", ','));
//    	showLocalIpInfo();
//    	try
//		{
//    		System.out.println("【"+getLocalIP()+"】");
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//    	String str = "dci       3424 83.2  7.3 13105324 1193664 ?    Sl   21:10  31:58 /home/dci/mongodb/bin/mongod --config /home/dci/mongodb/mongodb.conf";
//    	args = split(str);
//    	System.out.println(str);
//    	for(String arg : args)
//    	{
//    		System.out.println('\t'+arg);
//    	}
//    	try
//		{
//			InetAddress localhost = InetAddress.getLocalHost();
//	    	System.out.println(localhost.getHostAddress());
//		}
//		catch (UnknownHostException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	System.out.println(Tools.encodeUnicode("财经"));
//    	Pattern pattern = Pattern.compile( "/login/([a-zA-Z0-9/-]+)/" );
//    	Matcher matcher = pattern.matcher( "/login/9ce8b8df-6e88-4322-ab48-21155718f94e" );
//    	System.out.println(matcher.matches()+", "+matcher);
//    	
//    	System.out.println(Integer.toHexString(0x1+0x80));
////    	System.out.println(Integer.toHexString(0x92&0x0F));
//    	System.out.println(Integer.toHexString(0x11&0xF0));
//    	System.out.println(Integer.toHexString(0x92&0xF0));
//    	System.out.println(Integer.parseInt("192"));
//    	String s = "Report:120,0,10560,119,1580836";
//    	System.out.println(s.substring("Report:".length()));
//    	System.out.println(s.substring("Report:".length()).split(",").length);
//    	long x = 0;
//    	if( 4294967295L == x )
//    	{
//    		
//    	}
//    	int y = 0;
//    	if( -1 == y )
//    	{
//    		//抛弃
//    	}
//    	long y1 = 0x100000000L+y;
//    	System.out.println(0x100000000L+0xFFFFFFF4);
//    	System.out.println(0xFFFFFFFFL);
//    	System.out.println(Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", 1453700533296L));
//    	System.out.println(Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", 1339689853));
//    	String result[] = split("错误原因.int(10)\\,<>\\,成功\\;错误原因.int(5)\\,<3>\\,成3功", "\\,");
//    	for( String str : result )
//    	{
//    		System.out.println(str);
//    	}
//    	System.out.println(Tools.getTimeFormat("2010-10-12"));
//    	try
//		{
//			zip(new File("E:/project/mpc/trunk/CODE/debug/log/InteractiveSmsScanner/ossu-engine.log"), new File("x:/a.zip"));
//		}
//		catch (Exception e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	byte buf[] = new byte[12];
////    	File file = new File("E:/project/mpc/trunk/CODE/debug/log/AdminPortal/20110630163645.gc");
//    	long a = 83051557387L;
//    	Tools.longToBytes(a, buf, 0, 8);
//    	Tools.printb(buf, 0, 8);
////    	System.out.println(file.lastModified()+":"+Tools.bytesToLong(buf, 2));
////    	byte buf1[] = {0,0,0x01,0x30,(byte)0xdf,0x0a,(byte)0xc8,(byte)0xe1};
////    	Tools.printb(buf1, 0, 8);
//    	System.out.println(":"+Tools.bytesToLong(buf, 0, 8));
//        Pattern pattern = Pattern.compile( "*.pid" );
//        Matcher isNum = pattern.matcher( "IREAD.pid" );
//        System.out.println(isNum.matches());
//    	System.out.println(getDatePattern("TB_PRE_STAT_CTEL_OP_20101120"));
//    	System.out.println(getFormatTime("yyyy-MM-dd HH:mm:ss", 1329401709006L));
//    	String regex = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";
//    	String regex = "[0-9]+";
//    	Pattern pattern = Pattern.compile( "^a[0-9]+.html" );
//    	System.out.println(pattern.matcher( "a544.html" ).matches());
//    	System.out.println(pattern.matcher( "http://4-ps.googleusercontent.com/x/www.equlu.com/img01.taobaocdn.com/imgextra/i1/92592768/xT2kcWxXdFXXXXXXXXX_,21,2192592768.jpg.pagespeed.ic.ay6SECBifG.jpg" ).matches());
//    	String regex = "(\\d{14})(\\_)13007304492(\\_)(([A-Z]*)([\u4E00-\u9FA5]*))(\\_)([\u4E00-\u9FA5]*)(\\_)([\u4E00-\u9FA5]*)(\\_)([\u4E00-\u9FA5]*)";//_13007304492_(\\w*)
//    	String regex1 = "(\\d{1,3})(\\.)(\\d{1,3})(\\.)(\\d{1,3})(\\.)(\\d{1,3})";
//    	String regex2 = "(\\d{14})(\\_)13007318371(\\_)(\\d{3})(\\_)(\\d{3})(\\_)(\\d{4})(\\_)(\\d{1})(\\_)(\\d{2})(\\_)(\\d{1})(\\_)(\\d{2})(\\_)(\\d{1})(\\_)(\\d{1})";
//    	Pattern pattern = Pattern.compile( regex );
//    	Pattern pattern1 = Pattern.compile( regex1 );
//    	Pattern pattern2 = Pattern.compile( regex2 );
//    	System.out.println(pattern.matcher( "20120224173403_13007304492_HTTP浏览_即时通信/飞信_上下行_文字" ).matches());
//    	System.out.println(pattern1.matcher( "255.255.255.255" ).matches());
//    	System.out.println(pattern2.matcher( "20120308000000_13007318371_460_250_0000_2_21_2_03_0_0" ).matches());
//    	java.io.ByteArrayOutputStream out = new ByteArrayOutputStream();
//    	out.write(1);
//    	out.write(2);
//    	out.write(3);
//    	out.write(3);
//    	Tools.printb(out.toByteArray());
//    	ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
//    	int ch = 0;
//    	while( (ch = input.read()) != -1)
//    	{
//    		System.out.println(ch);
//    	}
//    	String labe = new String("MapReduce介绍");
//		System.out.println(labe.length());
//        byte[] chars;
//		try
//		{
//			chars = labe.getBytes("utf-8");
//	        Tools.printb(chars);
//        	int offset = 0;
//        	for( int i = 0; i < labe.length(); i++ )
//        	{
//        		if( offset + 2 > 14 )
//        		{
//        			System.out.println(offset+":"+i);
//        			labe = labe.substring(0, i)+"*";
//        			break;
//        		}
//        		int c = labe.charAt(i);
//        		System.out.println(c);
//    			offset += (c>128?2:1);
//        	}
//
//    		System.out.println(labe);
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
    }
    
    /**
	 * 判断文本文件编码格式 ANSI： 无格式定义； Unicode： 前两个字节为FFFE； Unicode big endian：
	 * 前两字节为FEFF； UTF-8： 前两字节为EFBB；
	 * 
	 * @param file
	 * @return
	 */
	public static String getTxtCharset(File file)
	{
		String charset = "GBK";
		byte[] first3Bytes = new byte[3];
		try
		{
			boolean checked = false;
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			bis.mark(0);
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1)
				return charset;
			if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE)
			{
				charset = "UTF-16LE";
				checked = true;
			}
			else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF)
			{
				charset = "UTF-16BE";
				checked = true;
			}
			else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF)
			{
				charset = "UTF-8";
				checked = true;
			}
			bis.reset();
			if (!checked)
			{
				int loc = 0;

				while ((read = bis.read()) != -1)
				{
					loc++;
					if (read >= 0xF0)
						break;
					if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
						break;
					if (0xC0 <= read && read <= 0xDF)
					{
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
							// (0x80
							// -
							// 0xBF),也可能在GB编码内
							continue;
						else
							break;
					}
					else if (0xE0 <= read && read <= 0xEF)
					{// 也有可能出错，但是几率较小
						read = bis.read();
						if (0x80 <= read && read <= 0xBF)
						{
							read = bis.read();
							if (0x80 <= read && read <= 0xBF)
							{
								charset = "UTF-8";
								break;
							}
							else
								break;
						}
						else
							break;
					}
				}
			}

			bis.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return charset;
	}
	
	public static String getI18nProperty(String key)
	{
		if(i18nConfig == null)
		{
			File workPath;
			File file = new File("bootstrap.jar").getAbsoluteFile();
			if( file.exists() )
			{//判断是否是WEB应用
				workPath = file.getParentFile().getParentFile().getParentFile();//bin/tomcat/debug/
			}
			else
			{
				workPath = new File(com.focus.util.ConfigUtil.getWorkPath());
			}		
			
			File configFile = new File(workPath, "config/config.properties");
			String stri18nfile = "config/i18n/messages_zh_CN.properties";
			String locale = "zh_CN";
			if(configFile.exists())
			{
				try
				{
					Config config = new Config(configFile);
					locale = config.getString("emasys.locale", "zh_CN");
					if(locale.equalsIgnoreCase("zh_CN"))
					{
						stri18nfile = "config/i18n/messages_zh_CN.properties";
					}
					else if(locale.equalsIgnoreCase("en_US"))
					{
						stri18nfile = "config/i18n/messages_en_US.properties";
					}
				}
				catch (Exception e) {
					// TODO: handle exception
				}
			}
			
			File i18nFile = new File(workPath, stri18nfile);
			if(i18nFile.exists())
			{
				try
				{
					i18nConfig = new Config(i18nFile);
					return i18nConfig.getString(key, "");
				}
				catch (Exception e) {
				}
			}
		}
		else
		{
			return i18nConfig.getString(key, "");
		}
		
		return "";
	}
	
	public static String getI18nProperty(String key, String defaultValue)
	{
		String value = getI18nProperty(key);
		if(value.isEmpty())
		{
			return defaultValue;
		}
		else
		{
			return value;
		}
	}
	/**
	 * 计算文本中出现指定字符的个数
	 * @param scr
	 * @param ch
	 * @return
	 */
	public static int countChar(String scr, char ch)
	{
		if( scr == null )
			return 0;
		int count = 0;
		int len = scr.length();
		for( int i = 0; i < len; i++ )
		{
			count += scr.charAt(i)==ch?1:0;
		}
		return count;
	}
	
	/**
	 * 根据输入的时间字符串得到时间格式
	 * @param timeStr
	 * @return
	 */
	public static String getTimeFormat(String timeStr)
	{
		timeStr = timeStr.trim();
		StringBuffer format = new StringBuffer();
		int countDateSper = Tools.countChar(timeStr, '-');
		int countTimeSper = Tools.countChar(timeStr, ':');
		int countSpaceSper = Tools.countChar(timeStr, ' ');
		if( countDateSper == 1 )
		{
			format.append("MM-dd");
		}
		else if( countDateSper == 2 )
		{
			format.append("yyyy-MM-dd");
		}
		if( countSpaceSper > 0 )
		{
			if( countDateSper == 0 )
			{
				format.append("dd");
			}
			format.append(" ");
		}
		if( countTimeSper == 2 )
		{
			format.append("HH:mm:ss");
		}
		else if( countTimeSper == 1 )
		{
			format.append("HH:mm");
		}
		if( countSpaceSper > 1 )
		{
			format.append(" ");
			format.append("SSS");
		}

		if( countDateSper == 0 && countSpaceSper == 0 && timeStr.length() == 14 )
		{
			format.append("yyyyMMddHHmmss");
		}
		if( countDateSper == 0 &&  countSpaceSper == 0 && timeStr.length() == 12 )
		{
			format.append("yyyyMMddHHmm");
		}
		if( countDateSper == 0 &&  countSpaceSper == 0 && timeStr.length() == 10 )
		{
			format.append("yyyyMMddHH");
		}
		if( countDateSper == 0 &&  countSpaceSper == 0 && timeStr.length() == 8 )
		{
			format.append("yyyyMMdd");
		}		
		if( countDateSper == 0 &&  countSpaceSper == 0 && timeStr.length() == 6 )
		{
			format.append("HHmmss");
		}
		return format.toString();
	}
	/**
	 * 压缩ZIP包
	 * @param file
	 * @param zipFile
	 * @throws Exception
	 */
	public static void zip(File file, File zipFile) throws Exception
	{
		ZipOutputStream zipOS = null;
		FileInputStream is = null;
		try
		{
			CheckedOutputStream cos = new CheckedOutputStream(new FileOutputStream(zipFile),new CRC32());
    		zipOS = new ZipOutputStream(cos);
    		is = new FileInputStream(file);
    		zipOS.putNextEntry(new ZipEntry(file.getName()));
    		int c;
    		while ((c = is.read()) != -1)
    		{
    			zipOS.write(c); 
    		}
    		zipOS.closeEntry();
		}
		catch (Exception e) 
		{
			throw e;
		}
		finally
		{
			if(is != null)
			{
				is.close();
			}
			if(zipOS != null)
			{
//				zipOS.finish();
				zipOS.close();
			}
		}
	}

    /**
     * 获取主机ip信息
     * @throws Exception 
     */
    public static String collectIpInfo()
    {
    	StringBuffer ipInfo = new StringBuffer();
    	try
    	{
			Enumeration<NetworkInterface> enu =java.net.NetworkInterface.getNetworkInterfaces(); 
		    while (enu.hasMoreElements()) 
		    {
			    NetworkInterface net = enu.nextElement();
			    if( ipInfo.length() > 0 ) ipInfo.append("\r\n");
			    ipInfo.append(net.getName());
				ipInfo.append(" 【");
			    ipInfo.append(net.getDisplayName());
				ipInfo.append(" 】");
			    Enumeration<InetAddress> ip2 = net.getInetAddresses();
			    while (ip2.hasMoreElements() ) 
			    {
				    InetAddress address = ip2.nextElement();
					ipInfo.append("\r\n\t");
				    ipInfo.append(address.getHostAddress());
					ipInfo.append(" [anylocal=");
				    ipInfo.append(address.isAnyLocalAddress());
					ipInfo.append(",linklocal=");
				    ipInfo.append(address.isLinkLocalAddress());
					ipInfo.append(",loopback=");
				    ipInfo.append(address.isLoopbackAddress());
					ipInfo.append("]");
			    }
		    }
        } 
    	catch (Exception e) 
    	{
        }
    	return ipInfo.toString();
    }	
	/**
	 * 得到本地IP地址
	 * @return
	 * @throws Exception 
	 */
	public static String getLocalIP() throws Exception
	{
		String ip = null;
		Enumeration<NetworkInterface> enu =java.net.NetworkInterface.getNetworkInterfaces(); 
	    while (enu.hasMoreElements()) 
	    {
		    NetworkInterface net = enu.nextElement();
		    String tag = net.getName().toLowerCase();
		    if( !tag.startsWith("eth") &&
		    	!tag.startsWith("em") &&
		    	!tag.startsWith("en") &&
		    	!tag.startsWith("net") &&
		    	!tag.startsWith("wlan") &&
		    	!tag.startsWith("bond0") &&
		    	!tag.startsWith("eno1") ) continue;
		    Enumeration<InetAddress> es = net.getInetAddresses();
		    while (es.hasMoreElements() ) 
		    {
			    InetAddress address = es.nextElement();
			    if( address.isLoopbackAddress() ) continue;
			    if( address.isLinkLocalAddress() && ip != null ) continue;
			    String addr = address.getHostAddress();
			    if( addr.indexOf('.') != -1 ){
			    	ip = addr;
			    }
		    }
	    	if( ip != null && tag.endsWith("1") ) break;
	    }
	    if( ip != null ) return ip;
	    throw new Exception("Failed to get the ipinof of local:\r\n"+collectIpInfo());
    }

	/**
	 * 得到本地IP地址
	 * @return
	 * @throws Exception 
	 */
	public static Map<String,NetworkInterface> getLocalIPs() throws Exception
	{
		HashMap<String, NetworkInterface> map = new HashMap<String, NetworkInterface>();
		Enumeration<NetworkInterface> enu =java.net.NetworkInterface.getNetworkInterfaces(); 
	    while (enu.hasMoreElements()) 
	    {
		    NetworkInterface net = enu.nextElement();
		    String tag = net.getName().toLowerCase();
		    if( !tag.startsWith("eth") &&
		    	!tag.startsWith("em") &&
		    	!tag.startsWith("en") &&
		    	!tag.startsWith("net") &&
		    	!tag.startsWith("wlan") &&
		    	!tag.startsWith("bond0") &&
		    	!tag.startsWith("eno1") ) continue;
		    Enumeration<InetAddress> es = net.getInetAddresses();
		    while (es.hasMoreElements() ) 
		    {
			    InetAddress address = es.nextElement();
			    if( address.isLoopbackAddress() ) continue;
			    String addr = address.getHostAddress();
			    if( addr.indexOf('.') != -1 ){
			    	map.put(addr, net);
			    }
		    }
	    }
	    return map;
    }	
	/**
	 * 显示本地IP信息
	 * @throws Exception
	 */
    public static void showLocalIpInfo()
    {
    	StringBuffer ipInfo = new StringBuffer();
    	try
    	{
			ipInfo.append("Show the infomation of local-network:");
			Enumeration<NetworkInterface> enu =java.net.NetworkInterface.getNetworkInterfaces(); 
		    while (enu.hasMoreElements()) 
		    {
			    NetworkInterface net = enu.nextElement();
				ipInfo.append("\r\n\t");
			    ipInfo.append(net.getName());
				ipInfo.append(" (");
			    ipInfo.append(net.getDisplayName());
				ipInfo.append(")");
			    Enumeration<InetAddress> ip2 = net.getInetAddresses();
			    while (ip2.hasMoreElements() ) 
			    {
				    InetAddress address = ip2.nextElement();
					ipInfo.append("\r\n\t\t");
				    ipInfo.append(address.getHostAddress());
					ipInfo.append(" [anylocal=");
				    ipInfo.append(address.isAnyLocalAddress());
					ipInfo.append(",linklocal=");
				    ipInfo.append(address.isLinkLocalAddress());
					ipInfo.append(",loopback=");
				    ipInfo.append(address.isLoopbackAddress());
					ipInfo.append(",loopback=");
					ipInfo.append("]");
			    }
		    }
        } 
    	catch (SocketException e) 
    	{
            e.printStackTrace();
        }
	    System.out.println(ipInfo);
    }
	/**
	 * 合法手机号
	 * @param number
	 * @return
	 */
	public static boolean isMobileNumber(String number)
	{
		return number.matches("^((13[0-9])|(15[0-9])|(17[0-9])|(18[0-9])){1}([0-9]){8}$");
	}
	
    /**
     * 执行操作系统command命令
     *
     * @param cmds
     * @return
     */
    public static String os_exec(String[] cmds)
    {
    	return os_exec(cmds, "UTF-8");
    }
    
    public static String os_exec(String[] cmds, String charset)
    {
        try
        {
            byte[] payload = _os_exec(cmds);
            return new String(payload, charset);
        }
        catch (Exception e)
        {
    		StringBuffer err = new StringBuffer();
    		for(String commond : cmds )
    		{
    			err.append(commond);
    			err.append(' ');
    		}
            Log.err("Failed to execute:" + err, e);
            return e.getMessage();
        }
    }
    
    /**
     * 执行直接返回结果
     * @param cmds
     * @return
     */
    public static byte[] os_exec1(String[] cmds)
    {
        try
        {
        	return _os_exec(cmds);
        }
        catch (Exception e)
        {
        	return new byte[0];
        }
    }
    /**
     * 执行操作系统command命令
     *
     * @param cmds
     * @return
     * @throws Exception 
     */
    public static byte[] _os_exec(String[] cmds) throws Exception
    {
        Process process = null;
        try
        {
        	ArrayList<String> list = new ArrayList<String>();
        	for(int i = 0; i < cmds.length; i++)
        	{
        		list.add(cmds[i].toString());
        	}
            ProcessBuilder pb = new ProcessBuilder( list );
            pb.redirectErrorStream( true );
            process = pb.start();
            // 获取屏幕输出显示
            pb.redirectErrorStream( true );
            byte[] payload = readAsByteArray(process.getInputStream());
//            bufferedReader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
//            while ((line = bufferedReader.readLine()) != null)
//            {
//                if( i++ > 1 )
//                {
//                    sb.append("\r\n");
//                }
//                sb.append(line);
//            }
            process.waitFor(); // 等待编译完成
            return payload;
        }
        catch (Exception e)
        {
        	throw e;
        }
        finally
        {
            if( process != null )
            {
                try
                {
                    process.destroy();
                }
                catch (Exception e)
                {
                    Log.err(e);
                }
            }
        }
    }
    
    public static final byte[] readAsByteArray( InputStream is )
    	throws Exception
    {
		List<byte[]> all = new ArrayList<byte[]>();
		int read = -1;
		int count = 0;
		byte[] catchs = new byte[65536];
		try {
			while((read = is.read(catchs))>-1){
				count += read;
				byte[] data = new byte[read];
				System.arraycopy(catchs,0, data, 0, read);
				all.add(data);
			}
		} catch (IOException e) {
		}
		finally{
			is.close();
		}
		
		byte[] allDatas = new byte[count];
		int copyCounts = 0;
		for(byte[] b : all){
			System.arraycopy(b, 0, allDatas, copyCounts, b.length);
			copyCounts += b.length;
		}
		
		return allDatas;
	}
}
