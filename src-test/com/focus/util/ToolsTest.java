package com.focus.util;

import java.util.regex.Pattern;

public class ToolsTest {

	public static void main(String[] args)
	{
		
		String jsonData = "'";
		try
		{
			System.err.println(Tools.replaceStr(jsonData, "'", "\\'"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		String command = "127.0.0.1:9175,127.0.0.1:9275,127.0.0.1:9375";
		String addresses[] = Tools.split(command, ",");
		Pattern pattern = Pattern.compile( "^"
				+ "(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\."
				+ "(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\."
				+ "(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\."
				+ "(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$" );
		int i = 0;
		for(; i < addresses.length; i++)
		{
			String address = addresses[i];
			String ip = address;
			String p = "";
			args = Tools.split(address, ":");
			if( args.length == 2 )
			{
				ip = args[0];
				p = args[1];
			}
			if( !pattern.matcher( ip ).matches() )
	        {
				break;
	        }
			if( !p.isEmpty() && !Tools.isNumeric(p) )
			{
				break;
			}
		}
		System.err.println(i);
//		System.err.println(Tools.collectIpInfo());
		pattern = Pattern.compile( "^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&amp;%\\$\\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(\\:[0-9]+)*(/($|[a-zA-Z0-9\\.\\,\\?\\!\\'\\\\\\+&amp;%\\$#\\=~_\\-]+))*$" );
		System.err.println(pattern.matcher("http://omtapi.focusnt.com").matches());
		System.err.println(pattern.matcher("http://omtapi.focusnt.com/").matches());
		System.err.println(pattern.matcher("http://10.10.10.1:9089/").matches());
		System.err.println(pattern.matcher("http://10.10.10.1:9089").matches());
		System.err.println(pattern.matcher("http://opt.efida.com.cn/zk!query.action?ip=10.10.10.200&port=9088").matches());
		System.err.println(pattern.matcher("adsdf").matches());
//    	F path = new F("/");
//    	F[] files = path.listFiles();
//    	for(F f : files)
//    	{
//    		System.out.println(f.getPath());
//    	}
//		try {
//			String v = new String(IOHelper.readAsByteArray(new File("D:/focusnt/cos/trunk/CODE/main/log/ProgramLoader/1.txt")), "UTF-8");
//			JSONArray config = new JSONArray(v);
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
	}
	
	public static String getJSONValue(String str)
	{
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
}
