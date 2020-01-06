package com.focus.util;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

/**
 * 根据 user agent string 判断用户的平台、浏览器 参考资料
 * **************************************************************************************************************************************************
 * 台式机 Linux Ubuntu Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.2pre)
 * Gecko/20100225 Ubuntu/9.10 (karmic) Namoroka/3.6.2pre
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * Linux Mandriva 2008.1 Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.1)
 * Gecko/2008072403 Mandriva/3.0.1-1mdv2008.1 (2008.1) Firefox/3.0.1
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * Linux suSE 10.1 Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.0.3)
 * Gecko/20060425 SUSE/1.5.0.3-7 Firefox/1.5.0.31
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * Windows XP SP3 Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.1)
 * Gecko/20090624 Firefox/3.5 (.NET CLR 3.5.30729)
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * Windows Vista Mozilla/5.0 (Windows; U; Windows NT 6.1; nl; rv:1.9.2.13)
 * Gecko/20101203 Firefox/3.6.13 Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US;
 * rv:1.9.2.6) Gecko/20100625 Firefox/3.6.6 (.NET CLR 3.5.30729)
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * windows 2000 Mozilla/5.0 (Windows; U; Windows NT 5.0; en-GB; rv:1.8.1b2)
 * Gecko/20060821 Firefox/2.0b2
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * Windows 7 Mozilla/5.0 (Windows NT 6.1; WOW64; rv:14.0) Gecko/20100101
 * Firefox/14.0.1
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * Windows Server 2008 Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US;
 * rv:1.9.1.5) Gecko/20091102 Firefox/3.5.5 (.NET CLR 3.5.30729)
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * iMac OSX 10.7.4 Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:13.0)
 * Gecko/20100101 Firefox/13.0.1
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * Mac OS X Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.2.9)
 * Gecko/20100824 Firefox/3.6.9
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * 手持设备 iPad Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us)
 * AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b
 * Safari/531.21.10
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * iPad 2 Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X; en-us) AppleWebKit/534.46
 * (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * iPhone 4 Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us)
 * AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293
 * Safari/6531.22.7
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * iPhone 5 Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X)
 * AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334
 * Safari/7534.48.3
 * --------------------------------------------------------------------------------------------------------------------------------------------------
 * Android Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91)
 * AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1
 * **************************************************************************************************************************************************
 * 
 * @author Defonds
 */
public class UserAgentUtil
{

	public static void main(String args[])
	{
		final String[] testUA = {
			"Mozilla/5.0 (X11; Linux x86_64; rv:38.0) Gecko/20100101 Firefox/38.0",
			"Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_2 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Mobile/11D257 MicroMessenger/6.0 NetType/WIFI",
			"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.963 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.6 Safari/537.36 LBBROWSER AppEngine-Google; (+http://code.google.com/appengine; appid: s~urlsafe60)",
			"Mozilla/5.0 (Linux; U; Android 4.2.2; zh-cn; HUAWEI P6-C00 Build/HuaweiP6-C00) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30 V1_AND_SQ_5.1.1_158_YYB_D QQ/5.1.1.2245",
			"Mozilla/5.0 (Linux; U; Android 4.2.2; zh-CN; HUAWEI P6-C00 Build/HuaweiP6-C00) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/9.9.3.478 U3/0.8.0 Mobile Safari/533.1",
			"Mozilla/5.0 (Linux; U; Android 4.2.2; zh-cn; HUAWEI P6-C00 Build/HuaweiP6-C00) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30 MicroMessenger/5.4.0.66_r807534.480 NetType/WIFI",
			"Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_2 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Mobile/11D257 MicroMessenger/5.4.2 NetType/WIFI", 
			"Mozilla/5.0 (Linux; U; Android 4.0.3; zh-cn; M032 Build/IML74K) AppleWebKit/533.1 (KHTML, like Gecko)Version/4.0 MQQBrowser/4.1 Mobile Safari/533.1",
		};
		String curua = null;
		try
		{
			StringBuffer sb = new StringBuffer();
			for (String ua : testUA)
			{
				curua = ua;
				sb.append(ua);
				UserAgent userAgent = getUserAgent(ua);
				if (userAgent != null)
				{
					sb.append("\r\n\t[");
					sb.append(userAgent.getPlatformType());
					sb.append(" ");
					sb.append(userAgent.getPlatformSeries());
					sb.append("] [");
					sb.append(userAgent.getBrowserType());
					sb.append(" ");
					sb.append(userAgent.getBrowserVersion());
					if( userAgent.getPlatformType().equals("Android") )
					{
						sb.append("] [");
						sb.append(userAgent.getMobileType());
					}
					sb.append("]\r\n");
				}
				else
				{
					sb.append("\r\n\tUnknown\r\n");
				}
			}
			System.out.print(sb);
		}
		catch (Exception e)
		{
			System.out.println(curua);
			e.printStackTrace();
		}
	}

	/**
	 * 用途：根据客户端 User Agent Strings 判断其浏览器、操作平台 if 判断的先后次序：
	 * 根据设备的用户使用量降序排列，这样对于大多数用户来说可以少判断几次即可拿到结果： >>操作系统:Windows > 苹果 > 安卓 > Linux >
	 * ... >>Browser:Chrome > FF > IE > ...
	 * 
	 * @param userAgentStr
	 * @return
	 */
	public static UserAgent parse(String userAgent)
	{
		try
		{
			return getUserAgent(userAgent);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	public static UserAgent getUserAgent(String userAgent)
		throws Exception
	{
		if (StringUtils.isBlank(userAgent))
		{
			return null;
		}
		
		boolean normal = true;
		String baseinfo = null;
		StringBuffer temp = new StringBuffer();
		int len = userAgent.length();
		HashMap<String, String> kv = new HashMap<String, String>();
        for( int i = 0; i < len; i++ )
        {
            char c = userAgent.charAt( i );
            if( normal )
            {
                if( c == ' ' )
                {
                	if( temp.length() == 0 ) continue;
                	if( temp.indexOf("/") != -1 )
                	{
                		String[] args = StringUtils.split(temp.toString(), '/');
            			kv.put(args[0], args[1]);
                	}
                	else
                	{
                		kv.put(temp.toString(), null);
                	}
                	temp = new StringBuffer();
                }
                else if( c == '(' )
                {
                	normal = false;
                }
                else
                {
                	temp.append(c);
                }
            }
            else
            {
                if( c == ')' )
                {
                	if( baseinfo == null )
                		baseinfo = temp.toString();
                	normal = true;
                	temp = new StringBuffer();
                }
                else
                {
                	temp.append(c);
                }
            }
        }

        if( temp.length() > 0 )
        {
        	if( temp.indexOf("/") != -1 )
        	{
        		String[] args = StringUtils.split(temp.toString(), '/');
    			kv.put(args[0], args[1]);
        	}
        	else
        	{
        		kv.put(temp.toString(), null);
        	}
        }
        
        if( baseinfo == null || kv.isEmpty() )
        	return null;
		UserAgent ua = new UserAgent();
		if (baseinfo.contains("Windows"))
		{// 主流应用靠前
			/**
			 * ****************** 台式机 Windows 系列 ****************** Windows NT
			 * 6.2 - Windows 8 Windows NT 6.1 - Windows 7 Windows NT 6.0 -
			 * Windows Vista Windows NT 5.2 - Windows Server 2003; Windows XP
			 * x64 Edition Windows NT 5.1 - Windows XP Windows NT 5.01 - Windows
			 * 2000, Service Pack 1 (SP1) Windows NT 5.0 - Windows 2000 Windows
			 * NT 4.0 - Microsoft Windows NT 4.0 Windows 98; Win 9x 4.90 -
			 * Windows Millennium Edition (Windows Me) Windows 98 - Windows 98
			 * Windows 95 - Windows 95 Windows CE - Windows CE
			 * 判断依据:http://msdn.microsoft.com/en-us/library/ms537503(v=vs.85).aspx
			 */
			ua.setPlatformType("Windows");
			if (baseinfo.contains("Windows NT 6.2"))
			{// Windows 8
				ua.setPlatformSeries("8");
			}
			else if (baseinfo.contains("Windows NT 6.1"))
			{// Windows 7
				ua.setPlatformSeries("7");
			}
			else if (baseinfo.contains("Windows NT 6.0"))
			{// Windows Vista
				ua.setPlatformSeries("Vista");
			}
			else if (baseinfo.contains("Windows NT 5.2"))
			{// Windows XP x64 Edition
				ua.setPlatformSeries("XP");
				ua.setPlatformVersion("x64 Edition");
			}
			else if (baseinfo.contains("Windows NT 5.1"))
			{// Windows XP
				ua.setPlatformSeries("XP");
			}
			else if (baseinfo.contains("Windows NT 5.01"))
			{// Windows 2000, Service Pack 1 (SP1)
				ua.setPlatformSeries("2000");
				ua.setPlatformVersion("SP1");
			}
			else if (baseinfo.contains("Windows NT 5.0") ||
					 baseinfo.contains("Windows 2000"))
			{// Windows 2000
				ua.setPlatformSeries("2000");
			}
			else if (baseinfo.contains("Windows NT 4.0"))
			{// Microsoft Windows NT 4.0
				ua.setPlatformSeries("NT 4.0");
			}
			else if (baseinfo.contains("Windows 98; Win 9x 4.90"))
			{// Windows Millennium Edition (Windows Me)
				ua.setPlatformSeries("ME");
			}
			else if (baseinfo.contains("Windows 98"))
			{// Windows 98
				ua.setPlatformSeries("98");
			}
			else if (baseinfo.contains("Windows 95"))
			{// Windows 95
				ua.setPlatformSeries("95");
			}
			else if (baseinfo.contains("Windows CE"))
			{// Windows CE
				ua.setPlatformSeries("CE");
			}
			/**
			 * ******* IE 系列 ******* MSIE 10.0 - Internet Explorer 10 MSIE 9.0 -
			 * Internet Explorer 9 MSIE 8.0 - Internet Explorer 8 or IE8
			 * Compatibility View/Browser Mode MSIE 7.0 - Windows Internet
			 * Explorer 7 or IE7 Compatibility View/Browser Mode MSIE 6.0 -
			 * Microsoft Internet Explorer 6
			 * 判断依据:http://msdn.microsoft.com/en-us/library/ms537503(v=vs.85).aspx
			 */
			int i = baseinfo.indexOf("MSIE");
			if( i != -1 )
			{
				ua.setBrowserType("IE");
				ua.setBrowserVersion(baseinfo.substring(i+5, baseinfo.indexOf(';', i)));
			}
			else
			{
				judgeBrowser(ua, kv);
			}
			return ua;
		}
		int i = baseinfo.indexOf("Mac OS X");
		if (i != -1)
		{
			/**
			 * ******** 苹果系列 ******** 
			 * iPod - Mozilla/5.0 (iPod; U; CPU iPhone OS 4_3_1 like Mac OS X; zh-cn) AppleWebKit/533.17.9 (KHTML, like
			 * Gecko) Version/5.0.2 Mobile/8G4 Safari/6533.18.5 iPad -
			 * Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us)
			 * AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4
			 * Mobile/7B334b Safari/531.21.10 iPad2 - Mozilla/5.0 (iPad; CPU OS
			 * 5_1 like Mac OS X; en-us) AppleWebKit/534.46 (KHTML, like Gecko)
			 * Version/5.1 Mobile/9B176 Safari/7534.48.3 iPhone 4 - Mozilla/5.0
			 * (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us)
			 * AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293
			 * Safari/6531.22.7 iPhone 5 - Mozilla/5.0 (iPhone; CPU iPhone OS
			 * 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko)
			 * Version/5.1 Mobile/9A334 Safari/7534.48.3
			 * 判断依据:http://www.useragentstring.com/pages/Safari/
			 * 参考:http://stackoverflow.com/questions/7825873/what-is-the-ios-5-0-user-agent-string
			 * 参考:http://stackoverflow.com/questions/3105555/what-is-the-iphone-4-user-agent
			 */
			ua.setPlatformType("Mac");
			String args[] = baseinfo.split("; ");
			ua.setPlatformSeries(args[0]);
			i = userAgent.lastIndexOf("OS ", i);
			String platformVersion = null;
			if (i != -1)
			{
				i += 3;
				int j = userAgent.indexOf(' ', i);
				platformVersion = userAgent.substring(i, j);
			}
			ua.setPlatformVersion(platformVersion);
			judgeBrowser(ua, kv);
			return ua;
		}
		i = baseinfo.indexOf("Linux");
		if (i != -1)
		{
			/**
			 * ******** linux系列 ******** 
			 * Mozilla/5.0 (X11; Linux x86_64; rv:38.0) Gecko/20100101 Firefox/38.0
			 * 判断依据:http://www.useragentstring.com/pages/Safari/
			 * 参考:http://stackoverflow.com/questions/7825873/what-is-the-ios-5-0-user-agent-string
			 * 参考:http://stackoverflow.com/questions/3105555/what-is-the-iphone-4-user-agent
			 */
			ua.setPlatformType("Linux");
			String args[] = baseinfo.split("; ");
			ua.setPlatformSeries(args[0]);
			i = userAgent.lastIndexOf("rv:", i);
			String platformVersion = null;
			if (i != -1)
			{
				i += 3;
				int j = userAgent.indexOf(' ', i);
				platformVersion = userAgent.substring(i, j);
			}
			ua.setPlatformVersion(platformVersion);
			judgeBrowser(ua, kv);
			return ua;
		}

		i = baseinfo.indexOf("Android");
		if (i != -1)
		{
			ua.setPlatformType("Android");
			String args[] = baseinfo.split("; ");
			if( args.length > 4 )
			{
				if( args[2].startsWith("Android") )
				{
					ua.setPlatformVersion(args[2].substring(8));
				}
				i = args[4].indexOf(" Build/");
				if( i != -1 )
				{
					ua.setMobile(true);
					ua.setMobileType(args[4].substring(0, i));
				}
			}
			else
			{
				for(String arg:args)
				{
					if( arg.startsWith("Android") )
					{
						ua.setPlatformVersion(args[2].substring("Android ".length()));
					}
					else
					{
						i = arg.indexOf(" Build/");
						if( i != -1 )
						{
							ua.setMobile(true);
							ua.setMobileType(args[4].substring(0, i));
						}
					}
				}
			}
			judgeBrowser(ua, kv);
			return ua;
		}
		return null;
	}

	/**
	 * 用途：根据客户端 User Agent Strings 判断其浏览器 if 判断的先后次序：
	 * 根据浏览器的用户使用量降序排列，这样对于大多数用户来说可以少判断几次即可拿到结果： >>Browser:Chrome > FF > IE >
	 * ...
	 * @param userAgent:user agent
	 * @param HashMap<String, String>:平台
	 * @return
	 */
	private static void judgeBrowser(UserAgent userAgent, HashMap<String, String> kv)
	{
		userAgent.setMobile(kv.containsKey("Mobile"));
		if (kv.containsKey("Chrome"))
		{
			/**
			 * *********** Chrome 系列 *********** Chrome 24.0.1295.0 -
			 * Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.15 (KHTML,
			 * like Gecko) Chrome/24.0.1295.0 Safari/537.15 Chrome 24.0.1292.0 -
			 * Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.14 (KHTML,
			 * like Gecko) Chrome/24.0.1292.0 Safari/537.14 Chrome 24.0.1290.1 -
			 * Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.13
			 * (KHTML, like Gecko) Chrome/24.0.1290.1 Safari/537.13
			 * 判断依据:http://www.useragentstring.com/pages/Chrome/
			 */
			userAgent.setBrowserType("Chrome");
			userAgent.setBrowserVersion(kv.get("Chrome"));
		}
		else if (kv.containsKey("Firefox"))
		{
			/**
			 * ******* FF 系列 ******* Firefox 16.0.1 - Mozilla/5.0 (Windows NT
			 * 6.2; Win64; x64; rv:16.0.1) Gecko/20121011 Firefox/16.0.1 Firefox
			 * 15.0a2 - Mozilla/5.0 (Windows NT 6.1; rv:15.0) Gecko/20120716
			 * Firefox/15.0a2 Firefox 15.0.2 - Mozilla/5.0 (Windows NT 6.2;
			 * WOW64; rv:15.0) Gecko/20120910144328 Firefox/15.0.2
			 * 判断依据:http://www.useragentstring.com/pages/Firefox/
			 */
			userAgent.setBrowserType("Firefox");
			userAgent.setBrowserVersion(kv.get("Firefox"));
		}
		else if (kv.containsKey("Safari"))
		{
			userAgent.setBrowserType("Safari");
			userAgent.setBrowserVersion(kv.get("Safari"));
		}
		else if (kv.containsKey("Opera"))
		{
			userAgent.setBrowserType("Opera");
			userAgent.setBrowserVersion(kv.get("Opera"));
		}
		
		if (kv.containsKey("MicroMessenger"))
		{
			userAgent.setBrowserType("MicroMessenger");
			userAgent.setBrowserVersion(kv.get("MicroMessenger"));
		}
		else if (kv.containsKey("QQ"))
		{
			userAgent.setBrowserType("QQ");
			userAgent.setBrowserVersion(kv.get("QQ"));
		}
		else if (kv.containsKey("UCBrowser"))
		{
			userAgent.setBrowserType("UCBrowser");
			userAgent.setBrowserVersion(kv.get("UCBrowser"));
		}
		else if (kv.containsKey("AliApp"))
		{
			userAgent.setBrowserType("AliApp");
			userAgent.setBrowserVersion(kv.get("AliApp"));
		}
	}
}
