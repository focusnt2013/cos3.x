package com.focus.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HttpUtils
{
	public static void main(String args[])
	{
		JSONObject msg = null;
		try
		{
//			String oldCookie = "pgv_pvi=39613440; CNZZDATA1253162245=1226592335-1416796398-%7C1416962951; _ga=GA1.3.1906687854.1416796401; _gat=1";
//			String setCookie = "JSESSIONID=124DFD10D7A2B53A4225BC8F162FFB63; Path=/";
//			String newCookie = resetCookie(oldCookie, setCookie);
//			String url = "http://bcscdn.baidu.com/new-repackonline/91hiapk/AndroidPhone/4.3.5/1/1009594a/20140901115314/91hiapk_AndroidPhone_4-3-5_1009594a.apk?response-content-disposition=attachment;filename=91hiapk_AndroidPhone_1009594a.apk&response-content-type=application/vnd.android.package-archive";
//			download(url);
//			byte[] payload = getFile("https://192.168.80.130:10443/static/gateone.js");
//			System.out.println(new String(payload));
			JSONObject req = new JSONObject();
			req.put("begin_date", "2018-05-05");
			req.put("end_date", "2018-05-05");
			String access_token = "9_foAXdwaLokJUiMkO6n-HCayPK_WbJ2i-1ZVe_ha0dqC13k9-Qsb8B5Njk8rsrSfWsXhIwChrC_tcDKk5ChFsMvf7UsASRJuCfHEjTDPetmgjodi5bYN1yan6P04qbk0GFhEcYpwBpbhENeyGHBAjAAAYSR";
			String url = "https://api.weixin.qq.com/datacube/getarticletotal?access_token="+access_token;
			Document doc = HttpUtils.post(url, null, req.toString().getBytes("UTF-8"));
			JSONObject rsp = new JSONObject(doc.body().text());
			System.err.println(rsp.toString(4));
			
//			if( rsp.has("list") ){
//				JSONArray array = rsp.getJSONArray("list");
//				for(int i = 0; i < array.length(); i++){
//					msg = array.getJSONObject(i);
//					if( msg.has("details") ){
//						JSONArray details = msg.getJSONArray("details");
//						JSONObject detail = null;
//						JSONObject detail_last = null;
//						JSONObject detail_day = null;
//						for(int j = 0; j < details.length(); j++){
//							detail = details.getJSONObject(j);
//							if( detail_last != null ){
//								detail_day = new JSONObject();
//								detail_day.put("stat_date", detail.getString("stat_date"));
//								Iterator<?> iterator = detail.keys();
//								while( iterator.hasNext() )
//								{
//									String key = iterator.next().toString();
//									Object val = detail.get(key);
//									if( !(val instanceof String) ){
//										if( detail_last.has(key) ){
//											int count1 = detail.getInt(key);
//											int count0 = detail_last.getInt(key);
//											detail_day.put(key, count1-count0);
//										}
//									}
//								}
//							}
//							else{
//								detail_day = detail;
//							}
//							detail_day.put("msgid", msg.getString("msgid"));
//							detail_day.put("ref_date", msg.getString("ref_date"));
//							detail_day.put("title", msg.getString("title"));
//							detail_last = detail;
//							System.err.println(detail_day.toString(4));
//						}
//						if( detail != null ){
//							Iterator<?> iterator = detail.keys();
//							while( iterator.hasNext() )
//							{
//								String key = iterator.next().toString();
//								msg.put(key, detail.get(key));
//							}
//						}
//						msg.remove("details");
//						System.out.println(msg.toString(4));
//					}
//				}
//			}
//			else if( rsp.has("errcode") ){
//				System.err.println(rsp.toString(4));
//			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println(msg.toString(4));
		}
	}
	
	public static void download(String url) throws Exception
	{
		Log.msg("Begin "+url);
		HttpURLConnection con = null;
		try
		{
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setConnectTimeout( 7000 );
			con.setReadTimeout( 15000 );
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");
			con.setRequestProperty("Accept-Language", "zh-cn");
			con.setRequestProperty("x-wap-profile", "http://wap1.huawei.com/uaprof/HW_HUAWEI_P6-C00_1_20130425.xml");
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
	        	sb.append(con.getResponseMessage());
	        	System.out.println(sb);
	        	return;
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
//			boolean gzip = true;
//			InputStream is = null;
//			if( gzip ) is = new GZIPInputStream(con.getInputStream());
//			else is = con.getInputStream();
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (con != null)
			{
				con.disconnect();
			}
		}
	}
	/**
	 * 
	 */
	public static Document molishouji(String url, String baseUri)
		throws Exception
	{
		Log.msg("Begin "+url);
		HttpURLConnection con = null;
		try
		{
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setConnectTimeout( 7000 );
			con.setReadTimeout( 15000 );
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");
			con.setRequestProperty("Accept-Language", "zh-cn");
			con.setRequestProperty("x-wap-profile", "http://wap1.huawei.com/uaprof/HW_HUAWEI_P6-C00_1_20130425.xml");
			con.setRequestProperty("Connection", "close");
			con.setRequestProperty("Pragma", "no-cache");
			con.setRequestProperty("Cache-Control", "no-cache");
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.2.2; zh-cn; HUAWEI P6-C00 Build/HuaweiP6-C00) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30 V1_AND_SQ_5.1.1_158_YYB_D QQ/5.1.1.2245");
			con.connect();
			boolean gzip = true;
			InputStream is = null;
			if( gzip ) is = new GZIPInputStream(con.getInputStream());
			else is = con.getInputStream();
			return Jsoup.parse(is, "UTF-8", baseUri);
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (con != null)
			{
				con.disconnect();
			}
		}
	}

	/**
	 * 扒取网页数据
	 * @return
	 */
	public static Document crwal(String url, String chartset, String cookie)
		throws Exception
	{
		return crwal(url, chartset, cookie, null);
	}
	public static Document crwal(String url, String chartset, String cookie, String referer)
		throws Exception
	{
		HttpURLConnection con = null;
		try
		{
			String[] params = getBaseAndHost(url);
			String baseUri = params[0];
			String host = params[1];
			if( url.startsWith(baseUri+"/") )
			{
				url = baseUri+url.substring(baseUri.length()+1);
			}
			if(url.startsWith("https"))
			{
				javax.net.ssl.HostnameVerifier hv = new javax.net.ssl.HostnameVerifier() {  
					public boolean verify(String urlHostName, javax.net.ssl.SSLSession session) {  
						Log.war("Warning: URL Host: " + urlHostName + " vs. "  
								+ session.getPeerHost());  
						return true;  
					}  
				};  
				trustAllHttpsCertificates();  
				HttpsURLConnection.setDefaultHostnameVerifier(hv);  
			}
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setConnectTimeout( 7000 );
			con.setReadTimeout( 15000 );
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");
			con.setRequestProperty("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
			con.setRequestProperty("Cache-Control", "max-age=0");
			con.setRequestProperty("Connection", "Keep-Alive");
			if( cookie != null )
				con.setRequestProperty("Cookie", cookie);
			con.setRequestProperty("Host", host);
			if( referer != null )
				con.setRequestProperty("Referer", referer);
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
//			System.out.println(url+"\r\n\t"+cookie+"\r\n\t"+con.getRequestProperties()+"\r\n\t"+con.getHeaderFields());
			con.connect();
			if( con.getResponseCode() == 200 )
			{
				String responseCharset = null;
				String ct = con.getContentType();
				int i = ct.indexOf("charset=") ;
				if( i != -1 ) responseCharset=ct.substring(i+"charset=".length());
				if(responseCharset!= null && responseCharset.endsWith(";"))
					responseCharset = responseCharset.substring(0, responseCharset.length()-1);
				InputStream is = con.getInputStream();
				boolean gzip = "gzip".equals(con.getHeaderField("Content-Encoding"));
				if( gzip ) is = new GZIPInputStream(is);
				if(responseCharset != null && !responseCharset.isEmpty())
				{
					return Jsoup.parse(is, responseCharset, baseUri);
				}
				else
				{
					return Jsoup.parse(is, chartset, baseUri);
				}
			}
			else
			{
				throw new Exception(con.getResponseMessage());
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (con != null)
			{
				con.disconnect();
			}
		}
	}
	
	/**
	 * 执行网络数据的提交
	 */
	public static Document post(String url, Map<String, String> requestParams, byte[] writeBytes)
		throws Exception
	{
		HttpURLConnection con = null;
		try
		{
			String[] params = getBaseAndHost(url);
			String baseUri = params[0];
//			String host = params[1];
			if( url.startsWith(baseUri+"/") )
			{
				url = baseUri+url.substring(baseUri.length()+1);
			}
			if(url.startsWith("https"))
			{
				javax.net.ssl.HostnameVerifier hv = new javax.net.ssl.HostnameVerifier() {  
					public boolean verify(String urlHostName, javax.net.ssl.SSLSession session) {  
						Log.war("Warning: URL Host: " + urlHostName + " vs. "  
								+ session.getPeerHost());  
						return true;  
					}  
				};  
				trustAllHttpsCertificates();  
				HttpsURLConnection.setDefaultHostnameVerifier(hv);  
			}
			System.out.println("Post "+url);
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setConnectTimeout( 7000 );
			con.setReadTimeout( 15000 );
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");
			con.setRequestProperty("Accept-Language", "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
			con.setRequestProperty("Cache-Control", "max-age=0");
			con.setRequestProperty("Connection", "Keep-Alive");
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
			String contentType = null;
			if( requestParams != null ) contentType = requestParams.get("Content-Type");
			String chartset = "UTF-8";
			if(contentType != null)
			{
				chartset = getCharsetFromCT(contentType);
				int i = contentType.indexOf("charset=") ;
				if( i != -1 ) 
				{
					int j = contentType.indexOf(";",i);
					if(j != -1)
					{
						chartset=contentType.substring(i+"charset=".length(), j).trim();
					}
					else
					{
						chartset=contentType.substring(i+"charset=".length()).trim();
					}
				}
			}
			if( requestParams != null )
				for(Entry<String, String> param : requestParams.entrySet())
				{
					con.setRequestProperty(param.getKey(), param.getValue());
				}
			con.connect();

			if(writeBytes != null)
			{
				con.getOutputStream().write(writeBytes);
			}
			
			if( con.getResponseCode() == 200 )
			{
				String responseCharset = getCharsetFromCT(con.getContentType());
				InputStream is = con.getInputStream();
				boolean gzip = "gzip".equals(con.getHeaderField("Content-Encoding"));
				if( gzip ) is = new GZIPInputStream(is);
				if(responseCharset != null && !responseCharset.isEmpty())
				{
					return Jsoup.parse(is, responseCharset, baseUri);
				}
				else
				{
					return Jsoup.parse(is, chartset, baseUri);
				}
			}
			else
			{
				byte[] rspbytes = readFullInputStream(con.getErrorStream());
				throw new Exception(con.getResponseMessage()+"("+con.getResponseCode()+") ["+new String(rspbytes, "UTF-8")+"]");
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (con != null)
			{
				con.disconnect();
			}
		}
	}
	

	/**
	 * 读取输入流为二进制数组
	 * @param servletInputStream
	 * @return
	 */
	public static byte[] readFullInputStream(InputStream is)
	{
		List<byte[]> all = new ArrayList<byte[]>();
		int read = -1;
		int count = 0;
		byte[] catchs = new byte[1024];
		try {
			while((read = is.read(catchs))>-1){
				count += read;
				byte[] data = new byte[read];
				System.arraycopy(catchs,0, data, 0, read);
				all.add(data);
			}
		} catch (IOException e) {
			Log.err(e);
		}
		
		byte[] allDatas = new byte[count];
		int copyCounts = 0;
		for(byte[] b : all){
			System.arraycopy(b, 0, allDatas, copyCounts, b.length);
			copyCounts += b.length;
		}
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return allDatas;
	}
	/**
	 * 回答问题
	 * @param aid
	 * @return
	 */
	public static Document crwal(String url)
		throws Exception
	{
		return crwal(url, (String)null);
	}
	/**
	 * 
	 */
	public static Document crwal(String url, String chartset)
		throws Exception
	{
		return crwal(url, chartset, null);
	}

	public static Document crwal(String url, boolean gzip, String cookie)
		throws Exception
	{
		Log.msg("Begin "+url);
		HttpURLConnection con = null;
		try
		{
			String[] params = getBaseAndHost(url);
			String baseUri = params[0];
			String host = params[1];
			if(url.startsWith("https"))
			{
				javax.net.ssl.HostnameVerifier hv = new javax.net.ssl.HostnameVerifier() {  
					public boolean verify(String urlHostName, javax.net.ssl.SSLSession session) {  
						Log.war("Warning: URL Host: " + urlHostName + " vs. "  
								+ session.getPeerHost());  
						return true;  
					}  
				};  
				trustAllHttpsCertificates();  
				HttpsURLConnection.setDefaultHostnameVerifier(hv);  
			}
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setConnectTimeout( 7000 );
			con.setReadTimeout( 15000 );
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			if( gzip ) con.setRequestProperty("Accept-Encoding", "gzip, deflate");
			con.setRequestProperty("Accept-Language", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//			con.setRequestProperty("Cache-Control", "no-cache");
			con.setRequestProperty("Connection", "Keep-Alive");
//			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			con.setRequestProperty("Host", host);
			if( cookie != null ) con.setRequestProperty("Cookie", cookie);
//			con.setRequestProperty("Referer", referer);
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");

			con.connect();
			InputStream is = null;
			if( gzip ) is = new GZIPInputStream(con.getInputStream());
			else is = con.getInputStream();
			
			String respCharset = getCharsetFromCT(con.getContentType());
			if(respCharset != null)
			{
				return Jsoup.parse(is, respCharset, baseUri);
			}
			else
			{
				return Jsoup.parse(is, "UTF-8", baseUri);
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (con != null)
			{
				con.disconnect();
			}
		}
	}
	
	/**
	 * 使用Post方式爬取
	 * @param url
	 * @param baseUri
	 * @param chartset
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static Document crwalByPost(String url, String chartset, String params, String cookie)
	throws Exception
	{
		HttpURLConnection con = null;
		try
		{
			String[] ret = getBaseAndHost(url);
			String baseUri = ret[0];
			String host = ret[1];
			
			if(url.startsWith("https"))
			{
				javax.net.ssl.HostnameVerifier hv = new javax.net.ssl.HostnameVerifier() {  
					public boolean verify(String urlHostName, javax.net.ssl.SSLSession session) {  
						Log.war("Warning: URL Host: " + urlHostName + " vs. "  
								+ session.getPeerHost());  
						return true;  
					}  
				};  
				trustAllHttpsCertificates();  
				HttpsURLConnection.setDefaultHostnameVerifier(hv);  
			}
			
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setDoOutput(true);
			con.setConnectTimeout( 7000 );
			con.setReadTimeout( 15000 );
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	//		con.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
	//		con.setRequestProperty("Accept-Encoding", "gzip, deflate");
			con.setRequestProperty("Accept-Language", "zh-cn");
	//		con.setRequestProperty("Cache-Control", "no-cache");
			con.setRequestProperty("Connection", "Keep-Alive");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			con.setRequestProperty("Host", host);
	//		con.setRequestProperty("Referer", referer);
			if(cookie != null && !cookie.isEmpty())
			{
				con.setRequestProperty("Cookie", cookie);
			}
				
			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
			con.connect();
			con.getOutputStream().write(params.getBytes(chartset));
			
			if( con.getResponseCode() == 200 )
			{
				InputStream is = con.getInputStream();
				boolean gzip = "gzip".equals(con.getHeaderField("Content-Encoding"));
				if( gzip ) is = new GZIPInputStream(is);
				
				String respCharset = getCharsetFromCT(con.getContentType());
				if(respCharset != null)
				{
					return Jsoup.parse(is, respCharset, baseUri);
				}
				else
				{
					return Jsoup.parse(is, chartset, baseUri);
				}
			}
			else
			{
				throw new Exception(con.getResponseMessage());
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			if (con != null)
			{
				con.disconnect();
			}
		}
	}
	
	public static Document crwalByPost(String url, String chartset, String params)
	throws Exception
	{
		return crwalByPost(url, chartset, params, null);
	}
	/**
	 * 从链接得到图片数据，然后自动压缩
	 * @return
	 */
	public static byte[] catchImage(String link)
	{
		long ts = System.currentTimeMillis();
		if( link == null )
		{
			return null;
		}
		byte buffer[] = null;
		OutputStream out = null;
		//根据链接去下载图牄1�7
		HttpURLConnection connection = null;
		try
		{
			URLConnection theConnection = new URL(link).openConnection();
	        if( !(theConnection instanceof HttpURLConnection) )
	        {
	    		return null;
	        }
	        int responseCode = 0;
	        if( link.startsWith("https") )
	        {
	        	HttpsURLConnection connection1 = (HttpsURLConnection) theConnection;
	        	connection1.setDoInput(true);  
	        	connection1.setDoOutput(true);  
	        	connection1.setRequestMethod( "GET" );
	        	connection1.setConnectTimeout( 15000 );
	        	connection1.setReadTimeout( 60000 );
	        	//connection1.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.0.2) Gecko/20060308 Firefox/1.5.0.2" );
	        	connection.setRequestProperty("User-Agent", "Opera/9.23 (Windows NT 5.1; U; en)" );
	        	connection1.connect();
	        	responseCode = connection1.getResponseCode();
	        	connection = connection1;
	        }
	        else
	        {
		        connection = (HttpURLConnection) theConnection;
		        connection.setRequestMethod( "GET" );
		        connection.setConnectTimeout( 15000 );
		        connection.setReadTimeout( 60000 );
//		        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.0.2) Gecko/20060308 Firefox/1.5.0.2" );
		        connection.setRequestProperty("User-Agent", "Opera/9.23 (Windows NT 5.1; U; en)" );
		        connection.connect();
		        responseCode = connection.getResponseCode();
	        }
	        if(responseCode!=200)
	        {
	        	String err = responseCode+"\t"+connection.getContentType()+"\t"+connection.getResponseMessage();//+"\t"+connection.getContent();
	        	System.out.println(err);
	        	return null;
	        }
//			System.out.println("ContentType"+connection.getContentType());
	        String contentType = connection.getContentType();
	        if( contentType.indexOf("image") ==-1 )
	        {
	    		return null;
	        }
	        int contentLength = connection.getContentLength();
	        if( contentLength <= 0 )
	        {
	        	contentLength = 1024*1024;
	        }
	        buffer = new byte[contentLength];
	        int len = -1, off = 0;
	        InputStream inputStream = connection.getInputStream();
	        boolean gzip = "gzip".equals(connection.getHeaderField("Content-Encoding"));
			if( gzip ) inputStream = new GZIPInputStream(inputStream);
	        while( ( len = inputStream.read(buffer, off, contentLength) ) != -1 )
	        {
	        	contentLength -= len;
	        	off += len;
	        	if( contentLength == 0 ) break;
	        }
	        if( off == 0 )
	        {
	        	System.out.println("<"+(System.currentTimeMillis()-ts)+">Failed to catch images for error inputstream "+link);
	    		return null;
	        }
	        return buffer;
		}
		catch(SocketTimeoutException e)
		{
			System.out.println("<"+(System.currentTimeMillis()-ts)+">Failed(SocketTimeoutException) to get image from "+link +".");
		}
		catch(Exception e)
		{
			System.out.println("<"+(System.currentTimeMillis()-ts)+">Failed to get image from "+link +" for "+ e);
		}
		finally
		{
			if( out != null )
				try
				{
					out.close();
				}
				catch (IOException e)
				{
				}
			if( connection != null )
				connection.disconnect();
		}
		
		return null;
	}
	/**
	 * 下载链接内容到文仄1�7
	 * @param link
	 * @return
	 */
	public static boolean save(String link, File file)
		throws Exception
	{
		if( link == null )
		{
			return false;
		}
		byte buffer[] = null;
		OutputStream out = null;
		//根据链接去下载图牄1�7
		HttpURLConnection connection = null;
		try
		{
			URLConnection theConnection = new URL(link).openConnection();
	        if( !(theConnection instanceof HttpURLConnection) )
	        {
	    		return false;
	        }
	        int responseCode = 0;
	        if( link.startsWith("https") )
	        {
	        	HttpsURLConnection connection1 = (HttpsURLConnection) theConnection;
	        	connection1.setDoInput(true);  
	        	connection1.setDoOutput(true);  
	        	connection1.setRequestMethod( "GET" );
	        	connection1.setConnectTimeout( 15000 );
	        	connection1.setReadTimeout( 60000 );
	        	//connection1.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.0.2) Gecko/20060308 Firefox/1.5.0.2" );
	        	connection1.setRequestProperty("User-Agent", "Opera/9.23 (Windows NT 5.1; U; en)" );
	        	connection1.connect();
	        	responseCode = connection1.getResponseCode();
	        	connection = connection1;
	        }
	        else
	        {
		        connection = (HttpURLConnection) theConnection;
		        connection.setRequestMethod( "GET" );
		        connection.setConnectTimeout( 15000 );
		        connection.setReadTimeout( 60000 );
//		        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.0.2) Gecko/20060308 Firefox/1.5.0.2" );
		        connection.setRequestProperty("User-Agent", "Opera/9.23 (Windows NT 5.1; U; en)" );
		        connection.connect();
		        responseCode = connection.getResponseCode();
	        }
	        if(responseCode!=200)
	        {
	        	String err = responseCode+"\t"+connection.getContentType()+"\t"+connection.getResponseMessage();//+"\t"+connection.getContent();
	        	throw new Exception(err);
	        }
	        int contentLength = connection.getContentLength();
	        if( contentLength <= 0 )
	        {
	        	contentLength = 1024*1024;
	        }
	        buffer = new byte[contentLength];
	        int len = -1, off = 0;
	        InputStream inputStream = connection.getInputStream();
	        boolean gzip = "gzip".equals(connection.getHeaderField("Content-Encoding"));
			if( gzip ) inputStream = new GZIPInputStream(inputStream);
	        while( ( len = inputStream.read(buffer, off, contentLength) ) != -1 )
	        {
	        	contentLength -= len;
	        	off += len;
	        	if( contentLength == 0 ) break;
	        }
	        if( off == 0 )
	        {
	        	throw new Exception("Failed to catch page for error inputstream "+link);
	        }
	        inputStream.close();
	        File parent = file.getParentFile();
	        if( !parent.exists() )
	        	parent.mkdirs();
	        IOHelper.writeFile(file, buffer, 0, off);
			return true;
		}
		catch(Exception e)
		{
			throw e;
		}
		finally
		{
			if( out != null )
				try
				{
					out.close();
				}
				catch (IOException e)
				{
				}
			if( connection != null )
				connection.disconnect();
		}
	}
	public static BufferedImage getImage(String link)
	{
		long ts = System.currentTimeMillis();
		if( link == null )
		{
			return null;
		}
		byte buffer[] = null;
		OutputStream out = null;
		//根据链接去下载图牄1�7
		HttpURLConnection connection = null;
		try
		{
			URLConnection theConnection = new URL(link).openConnection();
	        if( !(theConnection instanceof HttpURLConnection) )
	        {
	    		return null;
	        }
	        int responseCode = 0;
	        if( link.startsWith("https") )
	        {
	        	HttpsURLConnection connection1 = (HttpsURLConnection) theConnection;
	        	connection1.setDoInput(true);  
	        	connection1.setDoOutput(true);  
	        	connection1.setRequestMethod( "GET" );
	        	connection1.setConnectTimeout( 15000 );
	        	connection1.setReadTimeout( 60000 );
	        	//connection1.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.0.2) Gecko/20060308 Firefox/1.5.0.2" );
	        	connection.setRequestProperty("User-Agent", "Opera/9.23 (Windows NT 5.1; U; en)" );
	        	connection1.connect();
	        	responseCode = connection1.getResponseCode();
	        	connection = connection1;
	        }
	        else
	        {
		        connection = (HttpURLConnection) theConnection;
		        connection.setRequestMethod( "GET" );
		        connection.setConnectTimeout( 15000 );
		        connection.setReadTimeout( 60000 );
//		        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.0.2) Gecko/20060308 Firefox/1.5.0.2" );
		        connection.setRequestProperty("User-Agent", "Opera/9.23 (Windows NT 5.1; U; en)" );
		        connection.connect();
		        responseCode = connection.getResponseCode();
	        }
	        if(responseCode!=200)
	        {
	        	String err = responseCode+"\t"+connection.getContentType()+"\t"+connection.getResponseMessage();//+"\t"+connection.getContent();
	        	System.out.println(err);
	        	return null;
	        }
//			System.out.println("ContentType"+connection.getContentType());
	        String contentType = connection.getContentType();
	        if( contentType.indexOf("image") ==-1 )
	        {
	    		return null;
	        }
	        int contentLength = connection.getContentLength();
	        if( contentLength <= 0 )
	        {
	        	contentLength = 1024*1024;
	        }
	        buffer = new byte[contentLength];
	        int len = -1, off = 0;
	        InputStream inputStream = connection.getInputStream();
	        boolean gzip = "gzip".equals(connection.getHeaderField("Content-Encoding"));
			if( gzip ) inputStream = new GZIPInputStream(inputStream);
	        while( ( len = inputStream.read(buffer, off, contentLength) ) != -1 )
	        {
	        	contentLength -= len;
	        	off += len;
	        	if( contentLength == 0 ) break;
	        }
	        if( off == 0 )
	        {
	        	System.out.println("<"+(System.currentTimeMillis()-ts)+">Failed to catch images for error inputstream "+link);
	    		return null;
	        }
//        	System.out.println(off);
	        inputStream.close();
			inputStream = new ByteArrayInputStream(buffer, 0, off);
	        return ImageIO.read(inputStream);
		}
		catch(SocketTimeoutException e)
		{
			System.out.println("<"+(System.currentTimeMillis()-ts)+">Failed(SocketTimeoutException) to get image from "+link +".");
		}
		catch(Exception e)
		{
			System.out.println("<"+(System.currentTimeMillis()-ts)+">Failed to get image from "+link +" for "+ e);
		}
		finally
		{
			if( out != null )
				try
				{
					out.close();
				}
				catch (IOException e)
				{
				}
			if( connection != null )
				connection.disconnect();
		}
		
		return null;
	}
	
	/**
	 * 获取云盾的跳转链掄1�7
	 * @param oraUrl
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static String getYunDunRedirectUrl(Document doc) throws Exception
	{
		Element body = doc.body();
		if(body.children().size() == 2 && "访问本页面，您的浏览器需要支持javascript!".equals(body.child(1).text().trim()))
		{
			Element script = body.child(0);
			String scriptText = script.childNodes().get(0).toString();
			String[] formulaArray = Tools.split(scriptText, ";");
			StringBuffer urlBuf = new StringBuffer();
			for(int i=1;i<formulaArray.length - 3;i++)
			{
				int startPos = formulaArray[i].indexOf("'");
				int endPos = formulaArray[i].indexOf("'", startPos+1);
				int plusPos = formulaArray[i].indexOf("+");
				String urlPart = formulaArray[i].substring(startPos + 1, endPos);
				if(plusPos > endPos)
				{
					urlBuf.insert(0, urlPart);
				}
				else if(plusPos < startPos)
				{
					urlBuf.append(urlPart);
				}
				else
				{
					throw new Exception("unknown script structure.");
				}
			}
			
			return urlBuf.toString();
		}
		
		return null;
	}
	
	public static String[] getBaseAndHost(String url) throws Exception
	{
		final String HTTP = "http://";
		final String HTTPS = "https://";
		int i, j, k; 
		if( url.startsWith(HTTP) )
		{
			i = 7;
			j = url.indexOf('/', i);
		}
		else if( url.startsWith(HTTPS) )
		{
			i = 8;
			j = url.indexOf('/', i);
		}
		else
			throw new Exception("Unknown url "+url);
		
		if(j != -1)
		{
			String baseUri = url.substring(0, j + 1);
			String host = url.substring(i, j);
			k = url.lastIndexOf('/');
			String pathUri = url.substring(0, k + 1);
			return new String[]{baseUri, host, pathUri, i==7?HTTP:HTTPS};
		}
		else
		{
			String baseUri = url + "/";
			String host = url.substring(i);
			return new String[]{baseUri, host, "", i==7?HTTP:HTTPS};
		}
	}
	
	/**
	 * 从链接得到文件
	 * @return
	 */
	public static byte[] getFile(String link)
	{
		long ts = System.currentTimeMillis();
		if( link == null )
		{
			return null;
		}
		byte buffer[] = null;
		OutputStream out = null;
		//根据链接去下载
		HttpURLConnection connection = null;
		try
		{
	        int responseCode = 0;
	        if( link.startsWith("https") )
	        {
	        	HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
					@Override
					public boolean verify(String arg0, SSLSession arg1) {
						// TODO Auto-generated method stub
						return true;
					}
	        		
	        	});
	        	HttpsURLConnection connection1 = (HttpsURLConnection) new URL(link).openConnection();;
	        	SSLContext sslcontext = SSLContext.getInstance("TLS");
	        	X509TrustManager x509 = new X509TrustManager(){
					@Override
					public void checkClientTrusted(X509Certificate[] arg0,
							String arg1) throws CertificateException {
						// TODO Auto-generated method stub
					}

					@Override
					public void checkServerTrusted(X509Certificate[] arg0,
							String arg1) throws CertificateException {
						// TODO Auto-generated method stub
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						// TODO Auto-generated method stub
						return null;
					}
        		};
	        	sslcontext.init(null, new TrustManager[]{x509}, new SecureRandom()); 
	        	connection1.setSSLSocketFactory(sslcontext.getSocketFactory());  
	        	connection1.setDoInput(true);  
	        	connection1.setDoOutput(true);  
	        	connection1.setRequestMethod( "GET" );
	        	connection1.setConnectTimeout( 15000 );
	        	connection1.setReadTimeout( 60000 );
	        	//connection1.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.0.2) Gecko/20060308 Firefox/1.5.0.2" );
	        	connection1.setRequestProperty("User-Agent", "Opera/9.23 (Windows NT 5.1; U; en)" );
	        	connection1.connect();
	        	responseCode = connection1.getResponseCode();
	        	connection = connection1;
	        }
	        else
	        {
		        connection = (HttpURLConnection)  new URL(link).openConnection();
		        connection.setRequestMethod( "GET" );
		        connection.setConnectTimeout( 15000 );
		        connection.setReadTimeout( 60000 );
//		        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.8.0.2) Gecko/20060308 Firefox/1.5.0.2" );
		        connection.setRequestProperty("User-Agent", "Opera/9.23 (Windows NT 5.1; U; en)" );
		        connection.connect();
		        responseCode = connection.getResponseCode();
	        }
	        if(responseCode!=200)
	        {
	        	String err = responseCode+"\t"+connection.getContentType()+"\t"+connection.getResponseMessage();//+"\t"+connection.getContent();
	        	System.out.println(err);
	        	return null;
	        }
//			System.out.println("ContentType"+connection.getContentType());
	        String contentType = connection.getContentType();
	        int contentLength = connection.getContentLength();
	        if( contentLength <= 0 )
	        {
	        	contentLength = 1024*1024;
	        }
	        buffer = new byte[contentLength];
	        int len = -1, off = 0;
	        InputStream inputStream = connection.getInputStream();
	        while( ( len = inputStream.read(buffer, off, contentLength) ) != -1 )
	        {
	        	contentLength -= len;
	        	off += len;
	        	if( contentLength == 0 ) break;
	        }
	        if( off == 0 )
	        {
	        	System.out.println("<"+(System.currentTimeMillis()-ts)+">Failed to catch images for error inputstream "+link);
	    		return null;
	        }
	        
	        if(buffer.length == off)
	        {
	        	return buffer;
	        }
	        else
	        {
	        	byte[] payload = new byte[off];
	        	System.arraycopy(buffer, 0, payload, 0, off);
	        	return payload;
	        }
		}
		catch(SocketTimeoutException e)
		{
			System.err.println("<"+(System.currentTimeMillis()-ts)+">Failed(SocketTimeoutException) to get image from "+link +".");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.err.println("<"+(System.currentTimeMillis()-ts)+">Failed to get file from "+link +" for "+ e);
		}
		finally
		{
			if( out != null )
				try
				{
					out.close();
				}
				catch (IOException e)
				{
				}
			if( connection != null )
				connection.disconnect();
		}
		
		return null;
	}

    public static void trustAllHttpsCertificates() throws Exception {  
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];  
        javax.net.ssl.TrustManager tm = new miTM();  
        trustAllCerts[0] = tm;  
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext  
                .getInstance("SSL");  
        sc.init(null, trustAllCerts, null);  
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc  
                .getSocketFactory());  
    }  
  
    static class miTM implements javax.net.ssl.TrustManager,  
            javax.net.ssl.X509TrustManager {  
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
            return null;  
        }  
  
        public boolean isServerTrusted(  
                java.security.cert.X509Certificate[] certs) {  
            return true;  
        }  
  
        public boolean isClientTrusted(  
                java.security.cert.X509Certificate[] certs) {  
            return true;  
        }  
  
        public void checkServerTrusted(  
                java.security.cert.X509Certificate[] certs, String authType)  
                throws java.security.cert.CertificateException {  
            return;  
        }  
  
        public void checkClientTrusted(  
                java.security.cert.X509Certificate[] certs, String authType)  
                throws java.security.cert.CertificateException {  
            return;  
        }  
    }  
    
    public static String resetCookie(String oldCookie, String resetCookies)
    {
    	List<String> resetCookie = new java.util.ArrayList<String>();
    	if(resetCookies != null && !resetCookies.isEmpty())
    	{
    		String params[] = Tools.split(resetCookies, ";");
    		for(String param: params)
    		{
    			param = param.trim();
    			resetCookie.add(param);
    		}
    	}
    	
    	return resetCookie(oldCookie, resetCookie);
    }
    
    /**
	 * 重置cookie
	 * @param oldCookie			老的cookie
	 * @param resetCookie		response中返回的霄1�7要修改的部分
	 * @return					新的cookie
	 */
	public static String resetCookie(String oldCookie, List<String> resetCookie)
	{
		Map<String, String> oldCookieMap = new HashMap<String, String>();
		if(oldCookie != null && !oldCookie.isEmpty())
		{
			String oldParams[] = Tools.split(oldCookie, ";");
			for(String oldParam:oldParams)
			{
				oldParam = oldParam.trim();
				if(!oldParam.isEmpty())
				{
					String kv[] = Tools.split(oldParam, "=");
					String key = kv[0].trim();
					if(!key.isEmpty())
					{
						String value = kv[1].trim();
						oldCookieMap.put(key, value);
					}
				}
			}
		}
		StringBuffer setCookie = new StringBuffer();
		for( String resetParam : resetCookie )
		{
			resetParam = resetParam.trim();
			if(!resetParam.isEmpty())
			{
				String kv[] = Tools.split(resetParam, "=");
				String key = kv[0].trim();
				if(!key.isEmpty())
				{
					String value = kv[1].trim();
					oldCookieMap.put(key, value);
				}
			}
		}

		StringBuffer sb = new StringBuffer();
		int i = 0;
		for(Entry<String, String> entry :oldCookieMap.entrySet())
		{
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());
			i++;
			if(i < oldCookieMap.size())
			{
				sb.append("; ");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * 从http的content-type中获取字符集取
	 * @param contentType
	 * @return	字符集（空表示没有指定字符集
	 */
	private static String getCharsetFromCT(String contentType)
	{
		String charset = null;
		if(contentType != null)
		{
			int i = contentType.indexOf("charset=") ;
			if( i != -1 ) 
			{
				int j = contentType.indexOf(";",i);
				if(j != -1)
				{
					charset=contentType.substring(i+"charset=".length(), j).trim();
				}
				else
				{
					charset=contentType.substring(i+"charset=".length()).trim();
				}
			}
		}
		
		return charset;
	}
	
	public static Element getElementByClass(Element e, String className)
	{
		if( e == null || className == null || className.isEmpty() ) return null;
		Elements es = e.getElementsByClass(className);
		if( es == null || es.isEmpty() ) return null;
		return es.get(0);
	}

	
	public static Element getElementByTag(Element e, String tagName)
	{
		if( e == null || tagName == null || tagName.isEmpty() ) return null;
		Elements es = e.getElementsByTag(tagName);
		if( es == null || es.isEmpty() ) return null;
		return es.get(0);
	}

    /**
     * 
     * @param ip
     * @return
     */
    public static String getIpRegion(String ip)
    {
    	String region = "未知";
    	if( ip == null ) return region;
    	if( ip.equals("127.0.0.1") || ip.startsWith("192.168") || ip.equals("::1") ) return ip;
    	//http://www.ip138.com/ips138.asp?ip=127.0.0.1&action=2
    	String url = "http://www.ip138.com/ips138.asp?ip="+ip+"&action=2";
    	try
		{
    		Document doc = HttpUtils.crwal(url);
    		Element e = HttpUtils.getElementByClass(doc, "ul1");
    		if( e != null )
    		{
    			e = HttpUtils.getElementByTag(e, "li");
    			if( e != null )
    			{
    				region = e.text();
    				int i = region.indexOf('：');
    				if( i != -1 )
    				{
    					region = region.substring(i+1);//本站数据：福建省厦门市  电信
        				i = region.indexOf(' ');
        				if( i != -1 )
        				{
        					String arg0 = region.substring(0, i);
        					String arg1 = region.substring(i).trim();
        					i = arg0.indexOf('市');
        					if( i != -1 && i != arg0.length() - 1 )
        						arg0 = arg0.substring(0, i+1);
        					region = arg0+" "+arg1;
        				}
    				}
    			}
    		}
		}
		catch (Exception e)
		{
		}
		return region;
    }
}
