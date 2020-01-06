package com.focus.cos;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import com.focus.cos.api.ApiUtils;

public class HttpTest {

	public static void main(String[] args)
	{
		String url = "http://1212.ip138.com/ic.asp";
		HttpURLConnection con = null;
		try
		{
			if(url.startsWith("https"))
			{
				javax.net.ssl.HostnameVerifier hv = new javax.net.ssl.HostnameVerifier() {  
					public boolean verify(String urlHostName, javax.net.ssl.SSLSession session) {  
						return true;  
					}  
				};  
				ApiUtils.trustAllHttpsCertificates();  
				HttpsURLConnection.setDefaultHostnameVerifier(hv);  
			}
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setConnectTimeout( 7000 );
			con.setReadTimeout( 15000 );
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");
			con.setRequestProperty("Accept-Language", "zh-cn");
			con.setRequestProperty("Connection", "close");
			con.setRequestProperty("Pragma", "no-cache");
			con.setRequestProperty("Cache-Control", "no-cache");
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.2.2; zh-CN; HUAWEI P6-C00 Build/HuaweiP6-C00) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/9.9.3.478 U3/0.8.0 Mobile Safari/533.1");
			con.connect();

        	int responseCode = con.getResponseCode();
        	StringBuffer sb = new StringBuffer();
        	sb.append("doGet "+responseCode+" [Content-Type] "+con.getContentType()+" [Encoding] "+con.getContentEncoding()+" [Date] "+con.getDate()+"]");
	        if(responseCode!=200)
	        {
	        	sb.append("\r\n"+con.getResponseMessage());
	        }
			Iterator<String> iterator = con.getHeaderFields().keySet().iterator();
			while(iterator.hasNext())
			{
				String key = iterator.next();
				if( key == null ) continue;
				List<String> list = con.getHeaderFields().get(key);
				sb.append("\r\n\t[");
				sb.append(key);
				sb.append("] ");
				sb.append(list);
				sb.append(" ");
				sb.append(list.size());
			}
        	System.out.println(sb);
        	
			InputStream is = con.getInputStream();
			boolean gzip = "gzip".equals(con.getHeaderField("Content-Encoding"));
			if( gzip ) is = new GZIPInputStream(is);
			byte[] payload = ApiUtils.readFullInputStream(is);
        	System.out.println(new String(payload, "GB2312"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (con != null)
			{
				con.disconnect();
			}
		}
	}
}
