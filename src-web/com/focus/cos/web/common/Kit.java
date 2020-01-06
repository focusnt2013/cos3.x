package com.focus.cos.web.common;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.json.JSONObject;

import com.focus.util.Tools;
/**
 * Description:公共类
 * Create Date:Oct 17, 2008
 * @author Focus
 *
 * @since 1.0
 * @rebuild at 2016 by liuxue
 */
public class Kit
{
	protected static final Log log = LogFactory.getLog(Kit.class);
	public static boolean EnableHttps = false;

    public static String LOC(HttpServletRequest request)
    {
    	int port = COSConfig.getLocalWebPort();
    	if( 80 == port )
    	{
    		return "http://127.0.0.1/";
    	}
		return "http://127.0.0.1:"+port+"/";
    }
    
    public static String URL(HttpServletRequest request)
    {
    	if(EnableHttps)
    		return "https://" + request.getHeader("Host") + "/";
		return "http://" + request.getHeader("Host") + "/";
    }
    /**
     * 得到HTTP路径前缀
     * @return String
     * @author Focus
     */
    public static String URL_PATH(HttpServletRequest request)
    {
    	StringBuffer url = new StringBuffer();
    	if( request != null )
    	{
        	if(EnableHttps)
	    		url.append("https://");
	    	else
	    		url.append("http://");
    		url.append(request.getHeader("Host"));
    		String path = request.getContextPath();
    		if( path.isEmpty() || path.charAt(0) != '/' )
    			url.append('/');
    		url.append(path);
    		if( url.charAt(url.length()-1) != '/' )
    			url.append('/');
    	}
		return url.toString();
    }
    
    /**
     * 得到HTTP路径前缀
     * @return String
     * @author Focus
     */
    public static String URL_LOCAL(HttpServletRequest request)
    {
    	StringBuffer url = new StringBuffer();
    	if( request != null )
    	{
//        	if(EnableHttps)
//	    		url.append("https://");
//	    	else
	    		url.append("http://");
    		url.append("127.0.0.1:");
    		url.append(COSConfig.getLocalWebPort());
    		String path = request.getContextPath();
    		if( path.isEmpty() || path.charAt(0) != '/' )
    			url.append('/');
    		url.append(path);
    		if( url.charAt(url.length()-1) != '/' )
    			url.append('/');
    	}
		return url.toString();
    }

    /**
     * 得到HTTP路径前缀
     * @return String
     * @author Focus
     */
    public static String URLPATH(HttpServletRequest request)
    {
    	StringBuffer url = new StringBuffer();
    	if( request != null )
    	{
        	if(EnableHttps)
	    		url.append("https://");
	    	else
	    		url.append("http://");
    		url.append(request.getHeader("Host"));
    		String path = request.getContextPath();
    		if( path.isEmpty() || path.charAt(0) != '/' )
    			url.append('/');
    		url.append(path);
    		if( url.charAt(url.length()-1) != '/' )
    			url.append('/');
    		
    		path = request.getServletPath();
    		if( path.charAt(0) == '/' ) path = path.substring(1);
    		url.append(path);
    		String queryStr = request.getQueryString();
    		if( queryStr != null && !queryStr.isEmpty() )
    		{
	    		url.append("?");
	    		url.append(request.getQueryString());
    		}
    	}
		return url.toString();
    }

    /**
     * Web service服务路径
     * @param request
     * @return String
     */
    public static String URL_SERVICE(HttpServletRequest request)
    {
        StringBuffer url = new StringBuffer();

    	if(EnableHttps)
        	url.append("https://");
        else
        	url.append("http://");
        url.append(request.getServerName());

        if(request.getServerPort() != 80)
        {
        	url.append(":");
        	url.append(request.getServerPort());
        }
        url.append(request.getContextPath());
        return url.toString();
    }

    /**
     * 短信Web Service访问路径
     * @param request
     * @return
     */
    public static String URL_WS_SMS(HttpServletRequest request)
    {
    	return URL_SERVICE(request)+"/services/SMSService";
    }
    
    /**
     * 彩信Web Service访问路径
     * @param request
     * @return String
     */
    public static String URL_WS_MMS(HttpServletRequest request)
    {
    	return URL_SERVICE(request)+"/services/MMSService";
    }
    
    /**
     * 取Web服务,Web Service服务主机名
     * @return String
     */
    public static String getHostName()
    {
        String hostName = "";
        try
        {
            hostName = InetAddress.getLocalHost().getHostName();
        }
        catch(UnknownHostException e)
        {
            log.error("getHostName异常",e);
        }
        return hostName;
    }

    /**
     * 取得DWR的绝对路径
     * @param request
     * @return String
     * @author Focus
     */
    public static String URL_DWRPATH(HttpServletRequest request)
    {
        return URL_PATH(request) + "dwr/";
    }
    
    /**
     * 包含include文件
     * @param request
     * @return
     * @author Focus
     */
    public static String URL_INCPATH(HttpServletRequest request)
    {
        return URL_PATH(request) + "include/";
    }
    
    /**
     * 取图片资源绝对路径
     * @param request
     * @return String
     * @author Focus
     */
    public static String URL_IMAGEPATH(HttpServletRequest request)
    {
        return URL_PATH(request) + "images/";
    }
    public static String URL_IMGDB(HttpServletRequest request)
    {
        return URL_PATH(request) + "images/db/";
    }
    
    
    public static String URL_IMGMMS(HttpServletRequest request)
    {
        return URL_PATH(request) + "images/mms/";
    }

    public static String URL_IMGICON(HttpServletRequest request)
    {
        return URL_PATH(request) + "images/icons/";
    }
    
    public static String URL_SKINPATH(HttpServletRequest request)
    {
        return URL_PATH(request) + "skin/";
    }
    
    public static String URL_SKITPATH(HttpServletRequest request)
    {
        return URL_PATH(request) + "skit/";
    }
    
    /**
     * 取JS文件绝对路径
     * @param request
     * @return String
     * @author Focus
     */
    public static String URL_SCRIPTPATH(HttpServletRequest request)
    {
        return URL_PATH(request) + "js/";
    }

    /**
     * 取样式文件绝对路径
     * @param request
     * @return String
     * @author Focus
     */
    public static String URL_CSSPATH(HttpServletRequest request)
    {
        return URL_PATH(request) + "css/";
    }

    /**
     * 取得资源的绝对路径
     * @param request
     * @return String
     * @author Focus
     */
    public static String URL_RESOURCE(HttpServletRequest request)
    {
        return URL_PATH(request) + "resources/";
    }

    /**
     * 取得资源的CSS的绝对路径
     * @param request
     * @param CssFile
     * @return String
     */
    public static String getResourceCSSTag(HttpServletRequest request, String cssFile)
    {
        return "<LINK REL='dns-prefetch' TYPE='text/css' HREF='" + URL_RESOURCE(request) + "css/" + cssFile + "'/>";
    }

    /**
     * 生成样式文件标签
     * @param request
     * @param CssFile
     * @return String
     */
    public static String getCSSTag(HttpServletRequest request, String CssFile)
    {
        return "<LINK REL='dns-prefetch' TYPE='text/css' HREF='" + URL_CSSPATH(request) + CssFile + "'/>";
    }

    /**
     * 生成JS文件标签
     * @param request
     * @param JsFile
     * @return String
     */
    public static String getJSTag(HttpServletRequest request, String JsFile)
    {
    	String jsurl = null;
    	if( JsFile.startsWith("skit/") )
    		jsurl = "<script language='javascript' src='" + URL_PATH(request) + JsFile + "'></script>";
    	else if( JsFile.startsWith("skit") || JsFile.equals("global.js") )
    		jsurl = "<script language='javascript' src='" + URL_PATH(request) + "skit/js/"+JsFile + "'></script>";
    	else if( JsFile.startsWith("datepicker") )
    		jsurl = "<script language='javascript' src='" + URL_PATH(request) + "skit/"+JsFile + "'></script>";
		else
			jsurl = "<script language='javascript' src='" + URL_SCRIPTPATH(request) + JsFile + "'></script>";
    	return jsurl;
    }

    /**
     * 得到DWR的JS路径
     * @param request
     * @param JsFile
     * @return String
     */
    public static String getDwrJsTag(HttpServletRequest request, String JsFile)
    {
        return "<script language='javascript' src='"+URL_PATH(request)+"dwr/" + JsFile + "'></script>";
    }

    /**
     * 生成图片标签
     * @param request
     * @param ImgFile
     * @return String
     */
    public static String getIMGTag(HttpServletRequest request, String ImgFile)
    {
        return "<img src='" + URL_IMAGEPATH(request) + ImgFile + "'/>";
    }

    /**
     * 清除缓存
     * @param request
     * @param response
     */
    public static void noCache(HttpServletRequest request, HttpServletResponse response)
    {
        response.setContentType("text/html");
        response.setHeader("pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Cache-Control", "no-store");
        if( request.getHeader("User-Agent") != null &&
        	request.getHeader("User-Agent").indexOf("MSIE") != -1)
        {
            response.setDateHeader("Expires", 0);
        }
    }
    
    public static String today()
    {
    	return Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis());
    }

    public static String lastYear()
    {
    	return Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis()-Tools.MILLI_OF_DAY*365);
    }

    public static String tomorrow()
    {
    	return Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis()+Tools.MILLI_OF_DAY);
    }

    public static String nextYear()
    {
    	return Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis()+Tools.MILLI_OF_DAY*365);
    }
    /**
     * 同步top页日期时间，格式 EEE HH:mm 
     * @return String
     */
    public static String getDatetime()
    {
    	Date date = new Date();
    	return DateFormatUtils.format(date, "EEE HH:mm:ss")+"<br>"+DateFormatUtils.format(date, "yyyy-MM-dd");
    }

    public static QueryMeta getCommonDate(String type)
    {
    	QueryMeta meta = new QueryMeta();
    	Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.add(Calendar.DAY_OF_MONTH, 1);
		meta.setEndDate(now.getTime());
		now.add(Calendar.DAY_OF_MONTH, -1);
//		String dateFormat = "yyyy-MM-dd HH:mm:ss";
    	if(type.equals("today"))//今天
    	{
    		meta.setStartDate(now.getTime());
    	}
    	else if(type.equals("yestday"))
    	{
    		now.add(Calendar.SECOND, -1);
    		meta.setEndDate(now.getTime());
    		now.add(Calendar.DAY_OF_MONTH, -1);
    		meta.setStartDate(now.getTime());
    	}
    	else if(type.equals("week"))//本周
    	{
    		int minus = new GregorianCalendar().get(GregorianCalendar.DAY_OF_WEEK) - 2;
    		if(minus < 0)//本周还没有开始
    		{
    			now.add(Calendar.DAY_OF_WEEK, -7);
    		}
    		else
    		{
    			now.set(Calendar.DAY_OF_WEEK, 2);//星期一
    		}
    		meta.setStartDate(now.getTime());
    	}
    	else if(type.equals("oneWeek"))//近一周
    	{
    		now.add(Calendar.DAY_OF_YEAR, -7);
    		meta.setStartDate(now.getTime());
    	}
    	else if(type.equals("month"))//本月
    	{
    		now.set(Calendar.DAY_OF_MONTH, 1);
    		meta.setStartDate(now.getTime());
    	}
    	else if(type.equals("oneMonth"))//近一月
    	{
    		now.add(Calendar.MONTH, -1);
    		meta.setStartDate(now.getTime());
    	}
    	else
    	{
    	}
    	return meta;
    }
    
    /**
     * 
     * @param param
     * @return
     */
    public static long getTimeMillis(String time)
    {
		try
		{
			SimpleDateFormat sdft = new SimpleDateFormat(Tools.getTimeFormat(time));
			Date date = sdft.parse(time);
			return date!=null?date.getTime():0;
		}
		catch (Exception e)
		{
		}
		return 0;
    }
    /**
     * 
     * @param param
     * @return
     */
    public static QueryMeta getMetaDate(String param)
    {
    	QueryMeta meta = new QueryMeta();
    	if(param !=null && !param.equals(""))
    	{
    		String[] p = param.split("&");
    		if(p[0].equals(""))
    		return meta;
    	}
    	
    	Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.add(Calendar.DAY_OF_MONTH, 1);
		meta.setEndDate(now.getTime());
		now.add(Calendar.DAY_OF_MONTH, -1);
		String dateFormat = "yyyy-MM-dd HH:mm:ss";
    	if(param.startsWith("today"))//今天
    	{
    		if( param.length() > 5 )
    		{
    			dateFormat = param.substring(6);
    		}
//    		now.set(Calendar.HOUR_OF_DAY, 0);
//    		now.set(Calendar.MINUTE, 0);
//    		now.set(Calendar.SECOND, 0);
    		meta.setStartDate(now.getTime());
    	}
    	else if(param.startsWith("yestday"))//昨天
    	{
    		if( param.length() > 7 )
    		{
    			dateFormat = param.substring(8);
    		}
    		meta.setEndDate(now.getTime());
//    		now.set(Calendar.HOUR_OF_DAY, 0);
//    		now.set(Calendar.MINUTE, 0);
//    		now.set(Calendar.SECOND, 0);
    		now.add(Calendar.DAY_OF_MONTH, -1);
    		meta.setStartDate(now.getTime());
    	}
    	else if(param.startsWith("week"))//本周
    	{
    		if( param.length() > 4 )
    		{
    			dateFormat = param.substring(5);
    		}
//    		now.set(Calendar.HOUR_OF_DAY, 0);
//    		now.set(Calendar.MINUTE, 0);
//    		now.set(Calendar.SECOND, 0);
    		
    		int minus = new GregorianCalendar().get(GregorianCalendar.DAY_OF_WEEK) - 2;
    		if(minus < 0)//本周还没有开始
    		{
    			now.add(Calendar.DAY_OF_WEEK, -7);
    		}
    		else
    		{
    			now.set(Calendar.DAY_OF_WEEK, 2);//星期一
    		}
    		meta.setStartDate(now.getTime());
    	}
    	else if(param.startsWith("oneWeek"))//近一周
    	{
    		if( param.length() > 7 )
    		{
    			dateFormat = param.substring(8);
    		}
//    		now.set(Calendar.HOUR_OF_DAY, 0);
//    		now.set(Calendar.MINUTE, 0);
//    		now.set(Calendar.SECOND, 0);
    		now.add(Calendar.DAY_OF_YEAR, -7);
    		meta.setStartDate(now.getTime());
    	}
    	else if(param.startsWith("month"))//本月
    	{
    		if( param.length() > 5 )
    		{
    			dateFormat = param.substring(6);
    		}
    		now.set(Calendar.DAY_OF_MONTH, 1);
//    		now.set(Calendar.HOUR_OF_DAY, 0);
//    		now.set(Calendar.MINUTE, 0);
//    		now.set(Calendar.SECOND, 0);
    		meta.setStartDate(now.getTime());
    	}
    	else if(param.startsWith("oneMonth"))//近一月
    	{
    		if( param.length() > 8 )
    		{
    			dateFormat = param.substring(9);
    		}
//    		now.set(Calendar.HOUR_OF_DAY, 0);
//    		now.set(Calendar.MINUTE, 0);
//    		now.set(Calendar.SECOND, 0);
    		now.add(Calendar.MONTH, -1);
    		meta.setStartDate(now.getTime());
    	}
    	else if(param.startsWith("period"))//近一月
    	{
    		if( param.length() > 6 )
    		{
    			dateFormat = param.substring(7);
    		}
//    		now.set(Calendar.HOUR_OF_DAY, 0);
//    		now.set(Calendar.MINUTE, 0);
//    		now.set(Calendar.SECOND, 0);
    		meta.setStartDate(now.getTime());
    	}
    	meta.set("dateFormat", dateFormat);
    	return meta;
    }
    
	/**
	 * 对路径进行解析修正
	 * @param src
	 * @return
	 */
	public static String getHref(String src, HttpServletRequest request)
	{
        int len = src.length();
        StringBuffer sbMatch = null;
        StringBuffer sbCmd = new StringBuffer();
        for( int i = 0; i < len; i++ )
        {
            char c = src.charAt( i );
            if( c == '%' )
            {
                if( sbMatch == null )
                {
                    sbMatch = new StringBuffer();
                }
                else
                {
                    String property = System.getProperty( sbMatch.toString() );
                    if( property == null )
                    {
                    	property = "";
                    }
                    if( !property.startsWith("http://") && request != null )
                    {
                    	sbCmd.append( Kit.URL(request) + property );
                    }
                    else
                    {
                    	sbCmd.append( property );
                    }
                    sbMatch = null;
                }
            }
            else if( sbMatch == null )
            {
                sbCmd.append( c );
            }
            else if( sbMatch != null )
            {
                sbMatch.append( c );
            }
        }
        
		return sbCmd.toString();
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
        if( size < Ks )
        {
        	sb.append(length + "B");
        }
        else if( size < Ms )
        {
        	sb.append(Tools.DF.format(size/Ks) + "K");
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
	 * 
	 * @param length
	 * @return
	 */
	public static String bytesScale(double size)
	{
		StringBuffer sb = new StringBuffer();
        if( size < Ms )
        {
        	size = size/Ks;
        	sb.append(Tools.DF.format(size) + "K/s");
        }
        else if( size < Gs )
        {
        	sb.append(Tools.DF.format(size/Ms) + "M/s");
        }
        else if( size < Ts )
        {
        	sb.append(Tools.DF.format(size/Gs) + "G/s");
        }
        else
        {
        	sb.append(Tools.DF.format(size/Ts) + "T/s");
        }
        return sb.toString();		
	}
	
	
	/**
	 * 得到数据约束
	 * @param size
	 * @return
	 */
	public static String getCount(float size)
	{
		StringBuffer sb = new StringBuffer();
		if (size < 10000)
		{
			int relSize = (int)size;
			sb.append(relSize+"个");
		}
		else if (size < 1000000)
		{
			size = size / 10000;
			sb.append(Tools.DF.format(size) + "万");
		}
		else if (size < 10000000)
		{
			size = size / 1000000;
			sb.append(Tools.DF.format(size) + "百万");
		}
		else if (size < 100000000)
		{
			size = size / 10000000;
			sb.append(Tools.DF.format(size) + "千万");
		}
		else
		{
			size = size / 100000000;
			sb.append(Tools.DF.format(size) + "亿");
		}
		return sb.toString();
	}	
	
	public static String getDurationMs(long duration)
	{
		if (duration < 1000)
		{
			return duration + "毫秒.";
		}
		long s = duration / 1000;
		String runtime = s + "秒.";
		if (s < 60)
		{
			runtime = s + "秒.";
		}
		else if (s < 60 * 60)
		{
			long m = s / 60;
			s = s % 60;
			runtime = m + "分钟," + s + "秒.";
		}
		else if (s < 24 * 60 * 60)
		{
			long h = s / 3600;
			long m = s % 3600 / 60;
			runtime = h + "小时," + m + "分钟.";
		}
		else if (s >= 24 * 60 * 60)
		{
			long d = s / (24 * 3600);
			long h = s % (24 * 3600) / 3600;
			runtime = d + "天," + h + "小时.";
		}
		return runtime;
	}

	public static String getDurationS(int duration)
	{
		long s = duration;
		String runtime = s + "秒.";
		if (s < 60)
		{
			runtime = s + "秒.";
		}
		else if (s < 60 * 60)
		{
			long m = s / 60;
			s = s % 60;
			runtime = m + "分钟," + s + "秒.";
		}
		else if (s < 24 * 60 * 60)
		{
			long h = s / 3600;
			long m = s % 3600 / 60;
			runtime = h + "小时," + m + "分钟.";
		}
		else if (s >= 24 * 60 * 60)
		{
			long d = s / (24 * 3600);
			long h = s % (24 * 3600) / 3600;
			runtime = d + "天," + h + "小时.";
		}
		return runtime;
	}
	
	/**
	 * 显示单位为分的钱
	 * @param fen
	 * @return
	 */
	public static String showFen(int fen)
	{
		double f = fen;
		return Tools.DF.format(f/100)+"元";
	}

	/**
	 * 显示单位为分的钱
	 * @param fen
	 * @return
	 */
	public static String fee(int fen)
	{
		double f = fen;
		return Tools.DF.format(f/100);
	}


	/**
	 * 显示单位为分的钱
	 * @param fen
	 * @return
	 */
	public static String shares(int shares)
	{
		final java.text.DecimalFormat df1 = new java.text.DecimalFormat("0.0000");
		double s = shares;
		return df1.format(s/10000)+"股";
	}
	/**
	 * 
	 * @param fen
	 * @return
	 */
	public static String showFen(long fen)
	{
		double f = fen;
		final java.text.DecimalFormat df1 = new java.text.DecimalFormat("0.0000");
		return df1.format(f/10000)+"元";
	}
	/**
	 * 显示小数点后2位的百分数
	 * @param percent
	 * @return
	 */
	public static String showPercent(int percent)
	{
		double p = percent;
		return Tools.DF.format(p/100)+"%";
	}
	public static String showPercent(long percent)
	{
		double p = percent;
		return Tools.DF.format(p/100)+"%";
	}
	/**
	 * 显示小数点后2位的百分数
	 * @param percent
	 * @return
	 */
	public static String showShares(int shares)
	{
		final java.text.DecimalFormat df1 = new java.text.DecimalFormat("0.0000");
		double s = shares;
		return df1.format(s/10000)+"股";
	}
	
	public static String getMoney(long money)
	{
		final java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
		final java.text.DecimalFormat df1 = new java.text.DecimalFormat("0.0");
		if (money < 10000)
		{
			return df.format(money);
		}
		long m = money / 10000;
		if (m < 1000)
		{
			return m + "万";
		}
		else if (m < 10000)
		{
			double d = m;
			d /= 1000;
			return df1.format(d)+"千万";
		}
		else if (m < 1000000) 
		{
			double d = m;
			d /= 10000;
			return df1.format(d)+"亿";
		}
		else
		{
			return (m/10000)+"亿";
		}
	}
	/**
	 * 得到主控的端口
	 * @param host
	 * @return
	public static int getControlPort(String host)
	{
		java.io.File file = new java.io.File(PathFactory.getDataPath(), "monitor/"+host);
		if( file.exists() )
		{
			return Integer.parseInt(new String(IOHelper.readAsByteArray(file)));
		}
		return 9529;
	}
	*/

	/**
	 * 抓取文字中的数字
	 * @param str
	 * @return
	 */
	public static String getAmount(String str)
	{
		// TODO Auto-generated method stub
		StringBuffer text = new StringBuffer(str);
		for(int i = 0; i < text.length();)
		{
			char c = text.charAt(i);
			if( c > '9' || c < '0' )
				text.deleteCharAt(i);
			else
				i += 1;
		}
		if( text.length() == 0 ) return "0";
		return text.toString();
	}
	/**
	 * 编码
	 * @param code
	 * @return
    public static final String encodeUnicode(String code)
    {
		try
		{
			byte[] buffer = code.getBytes("GB2312");
	        StringBuffer buf = new StringBuffer();
	        for(int i = 0; i < buffer.length; i++)
	        {
	        	buf.append("%");
	            buf.append(Integer.toHexString(buffer[i] < 0 ? 0x0100 +
	                                           buffer[i] : buffer[i]));
	        }
	        return buf.toString();
		}
		catch (UnsupportedEncodingException e)
		{
			return "";
		}
    }
    
    public static final String decodeUnicode(String code)
    {
    	code = code.replaceAll("%", "");
        byte buffer[] = new byte[code.length() / 2];
        if(code.length() % 2 != 0)
        {
            return code;
        }
        for(int i = 0; i < code.length(); i += 2)
        {
            char c1 = code.charAt(i);
            char c2 = code.charAt(i + 1);
            int b = Tools.getH2D(c1) * 16 + Tools.getH2D(c2);
            buffer[i / 2] = (byte) b;
        }
        try
		{
			return new String(buffer, "utf-8");
		}
		catch (UnsupportedEncodingException e)
		{
			return code;
		}
    }
	 */

    /**
     * 还原Unicode编码的字符串
     * 
     * @param src
     * @return String
     * @author Focus
     */
    public static String unicode2Chr(String src) {
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
    }
    
    /**
     * 字符串准成unicode
     * @param src
     * @return
     */
    public static String chr2Unicode(String src) {
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
    }
    
    /**
     * 得到指定request的真实请求IP
     * @param request
     * @return
     */
    public static String getIp(HttpServletRequest request)
    {
        String ip = request.getRemoteAddr();
        String ip_ = request.getHeader("x-forwarded-for");
        ip_ = ip_ == null ? request.getHeader("x-real-ip") : ip_;
        if (ip_ != null)  ip = ip_;
        return ip;
    }
    
    /**
     * 合并两个json对象
     * @param src
     * @param target
     */
    public static void merge(JSONObject src, JSONObject target)
    {
    	Iterator<?> iterator = target.keys();
    	while( iterator.hasNext() )
    	{
    		String key = iterator.next().toString();
    		if( src.has(key) ) continue;
    		src.put(key, target.get(key));
    	}
    }
    
    /**
     * 合并两个json对象
     * @param src
     * @param target
     */
    public static void merge(JSONObject src, Document target)
    {
    	src.put("_id", target.remove("_id").toString());
    	mergeMongo(src, new JSONObject(target.toJson()));
    }
    
    private static void mergeMongo(JSONObject src, JSONObject data){
		Iterator<?> iterator = data.keys();
    	while( iterator.hasNext() )
    	{
    		String key = iterator.next().toString();
    		Object val = data.get(key);
    		if( val instanceof JSONObject ){
				JSONObject obj = (JSONObject)val;
				if( obj.has("$numberLong") ){
					val = Long.parseLong(obj.getString("$numberLong"));
				}
				else {
					mergeMongo(obj, obj);
				}
    		}
    		src.put(key, val);
    	}
    }

	public static String getSkinCssPath(String cssfile)
	{
		String url = "skin/"+Skin.getInstance().getName()+"/css/"+cssfile;
		return url;
	}

	public static String getSkinCssPath(String cssfile, String dir)
	{
		String url = dir+"skin/"+Skin.getInstance().getName()+"/css/"+cssfile;
		return url;
	}
	
	public static void main(String args[]){
		String url = "680074007400700073003A002F002F007300330061002E007000730074006100740070002E0063006F006D002F0074006F0075007400690061006F002F006D006F006E00690074006F0072002F00730064006B002F0073006C00610072006400610072002E006A007300";
		System.out.println(Kit.unicode2Chr(url));
	}
}
