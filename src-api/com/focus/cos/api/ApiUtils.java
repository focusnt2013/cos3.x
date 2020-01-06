package com.focus.cos.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.Key;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import com.focus.cos.api.util.ConfigUtil;

public class ApiUtils 
{
	private static Key Identity;
	private static String COSId;
	private static String COSApi;
	
	/**
	 * 重置
	 */
	public static boolean reset()
	{
		String port = ConfigUtil.getString("cos.api.port");
		if( port != null && !port.isEmpty() )
		{
			COSApi = "http://127.0.0.1:"+port+"/";
			return true;
		}
		else{
			return false;
		}
	}
	/**
	 * 检查api是否可用
	 * @return
	 */
	public static JSONObject checkapi()
	{
		try
		{
			byte[] payload = ApiUtils.doGet("", null, true);
			return new JSONObject(new String(payload, "UTF-8"));
		}
		catch (Exception e)
		{
			JSONObject response = new JSONObject();
			response.put("error", e.getMessage());
			return response;
		}
	}
	/**
	 * 设置接口的访问地址
	 * @param uri
	 * @throws Exception
	 */
	private static String buildurl(String uri) throws Exception
	{
		if( uri.startsWith("http://") )
		{
			return uri;
		}
		if( COSApi == null )
		{//端口配置从哪里来:1、通过系统参数配置获取；2、通过
			if( !reset() ){
				throw new Exception("Please ensure your service of COS is running, and set the 'cos.api.port' at config.properties or System.setProperty(\"cos.api.port\", \"<port>\").");
			}
		}
		return COSApi + uri;
	}
	/**
	 * 提交数据
	 */
	public static byte[] doPost(String uri, Serializable serializable)
		throws Exception
	{
		return doPost(uri, convertObjectToBytes(serializable), null, serializable instanceof Key ? 1 : 0);
	}
	public static byte[] doPost(String uri, byte[] payload)
		throws Exception
	{
		return doPost(uri, payload, null, 0);
	}
	public static byte[] doPost(String uri, Map<String, String> parameters)
		throws Exception
	{
		return doPost(uri, null, parameters, 0);
	}
	public static byte[] doPost(String uri, Serializable serializable, Map<String, String> parameters)
		throws Exception
	{
		return doPost(uri, convertObjectToBytes(serializable), parameters, 0);
	}
	/**
	 * 
	 * @param uri
	 * @param payload
	 * @param parameters
	 * @param noencrypt 2表示重定向
	 * @return
	 * @throws Exception
	 */
	public static byte[] doPost(String uri, byte[] payload, Map<String, String> parameters, int noencrypt)
		throws Exception
	{
		String url = buildurl(uri);
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
				trustAllHttpsCertificates();  
				HttpsURLConnection.setDefaultHostnameVerifier(hv);  
			}
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setConnectTimeout( 7000 );
			con.setReadTimeout( 15000 );
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			Key key = setRequestProperty(con);
			if( noencrypt == 0 )
			{
				if( key == null )
				{
					throw new Exception("请设置您的服务正确的证书路径: System.setProperty(\"cos.identity\", \"data/identity\");");
				}
			}
			if( parameters != null )
				for(Entry<String, String> param : parameters.entrySet())
				{
					if( 2 == noencrypt ) con.setRequestProperty(param.getKey(), param.getValue());
					else con.setRequestProperty(param.getKey(), encode(param.getValue().getBytes("UTF-8")));
				}
			
			con.connect();
			if( payload != null )
			{
				if( noencrypt == 1 || noencrypt == 2 )
				{
					con.getOutputStream().write(payload);
				}
				else
				{
					Cipher c0 = Cipher.getInstance("DES");  
					c0.init(Cipher.ENCRYPT_MODE, key);
					con.getOutputStream().write(c0.doFinal(payload));
				}
				con.getOutputStream().flush();
			}
			if( con.getResponseCode() != 200 )
			{
				throw new Exception(con.getResponseMessage());
			}
			InputStream is = con.getInputStream();
			boolean gzip = "gzip".equals(con.getHeaderField("Content-Encoding"));
			if( gzip ) is = new GZIPInputStream(is);
			payload = readFullInputStream(is);
			if( noencrypt == 2 ) return payload;
			Cipher c1 = Cipher.getInstance("DES");
			c1.init(Cipher.DECRYPT_MODE, key);
    		return c1.doFinal(payload);
		}
		catch (Exception e)
		{
			throw new Exception("Failed to post to "+url+" for "+e.getMessage());
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
	 * @param con
	 */
	private static long SID = 0;
	private static Key setRequestProperty(HttpURLConnection con)
	{
		if( Identity == null )
		{
			String path = System.getProperty("cos.identity", "../data/identity");
			File fileIdentity = new File(getRealpath(path)); 
			try
			{
				//读取数字证书并初始化
				Identity = (Key)readSerializable(fileIdentity);
		    	Cipher c = Cipher.getInstance("DES");
		        c.init(Cipher.WRAP_MODE, Identity);//再用数字证书构建另外一个DES密码器
		        c.wrap(Identity);
		        COSId = encode(c.wrap(Identity));
			}
			catch(Exception e)
			{
			}
		}
		if( Identity != null )
		{
			try
			{
				
				setRequestValue(con, "COS-Version", System.getProperty("cos.version", "0.0.0.0"));
				setRequestValue(con, "COS-Path", System.getProperty("user.dir"));
				setRequestValue(con, "COS-Name", System.getProperty("cos.service.name", ""));
				String desc = System.getProperty("cos.service.desc", "");
				setRequestValue(con, "COS-Desc",  desc.isEmpty()?desc:new String(decode(desc), "UTF-8"));
				setRequestValue(con, "COS-ControlPort", System.getProperty("control.port", "0"));
				setRequestValue(con, "COS-ID", System.getProperty("cos.id", COSId));
				setRequestValue(con, "User-Agent", "COS/3.0");
				setRequestValue(con, "COS-IP", getLocalIP());
				setRequestValue(con, "From", getClassMethodName());
				setRequestValue(con, "SID", String.valueOf(SID++));//序列号
				setRequestValue(con, "COS-API-Port", System.getProperty("cos.api.port", "0"));
			}
			catch(Exception e)
			{
			}
		}
		return Identity;
	}
	
	/**
	 * 得到类方法名
	 * @return
	 */
	private static String getClassMethodName()
	{
		StackTraceElement[] stes = new Exception().getStackTrace();
		if( stes != null && stes.length > 0 )
		{
			StackTraceElement first = stes[0];
			for(StackTraceElement e: stes)
			{
				if( !e.getClassName().equals(first.getClassName()) )
				{
					if( !e.getClassName().endsWith("Client") )
					{
						return e.getClassName()+"."+e.getMethodName();
					}
				}
			}
		}
		return "";
	}
	/**
	 * 
	 * @param con
	 * @param id
	 * @param val
	 */
    protected static void setRequestValue(HttpURLConnection con, String id, String val)
    {
		try {
			con.setRequestProperty(id, encode(val.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
		}
    }
	
	/**
	 * 
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	public static byte[] doGet(String uri, Map<String, String> parameters, boolean noencrypt)
		throws Exception
	{
		String url = buildurl(uri);
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
				trustAllHttpsCertificates();  
				HttpsURLConnection.setDefaultHostnameVerifier(hv);  
			}
			con = (HttpURLConnection)new URL(url).openConnection();
			con.setConnectTimeout( 7000 );
			con.setReadTimeout( 15000 );
			con.setRequestMethod("GET");
			Key key = setRequestProperty(con);
			if( key == null )
			{
				throw new Exception("请设置证书路径System.setProperty(\"cos.identity\", \"data/identity\"");
			}
			if( parameters != null )
				for(Entry<String, String> param : parameters.entrySet())
				{
					if( noencrypt ) con.setRequestProperty(param.getKey(), param.getValue());
					else con.setRequestProperty(param.getKey(), encode(param.getValue().getBytes("UTF-8")));
				}
			con.connect();
			if( con.getResponseCode() != 200 )
			{
				throw new Exception(con.getResponseCode()+": "+con.getResponseMessage());
			}
			InputStream is = con.getInputStream();
			boolean gzip = "gzip".equals(con.getHeaderField("Content-Encoding"));
			if( gzip ) is = new GZIPInputStream(is);
			byte[] payload = readFullInputStream(is);
			if( noencrypt ) return payload;
			Cipher c1 = Cipher.getInstance("DES");
			c1.init(Cipher.DECRYPT_MODE, key);
    		return c1.doFinal(payload);
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
		
		byte[] allDatas = new byte[count];
		int copyCounts = 0;
		for(byte[] b : all){
			System.arraycopy(b, 0, allDatas, copyCounts, b.length);
			copyCounts += b.length;
		}
		
		return allDatas;
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

    public static final Serializable readSerializable( File file )
        throws
        Exception

    {
        if( !file.exists() )
        {
            throw new Exception( "Failed to serialize for unknown path "+file.getAbsolutePath() );
        }
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        Serializable s = null;
        try
        {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            s = (Serializable) ois.readObject();
            return s;
        }
        catch(Exception e )
        {
            throw e;
        }
        finally
        {
            ois.close();
            fis.close();
        }
    }

    public static final byte[] readAsByteArray( File file )
    {
        if( file != null && file.exists() )
        {
            try
            {
                FileInputStream fis = new FileInputStream( file );
                byte[] buffer = new byte[ fis.available() ];
                fis.read( buffer );
                fis.close();
                return buffer;
            }
            catch( Exception e )
            {
                return new byte[0 ];
            }
        }
        else
        {
            return new byte[0 ];
        }
	}
    
	private static final String getRealpath(String path)
	{
		if( path.indexOf('\\') > 0 )
		{
			path = path.replace("\\", "/");
		}
		if( path.startsWith("/") ) return path;
		int i = path.indexOf(':');
		if( i > 0 ) return path;

		String userdir = System.getProperty("user.dir");
		if( userdir.indexOf('\\') > 0 )
		{
			userdir = userdir.replace("\\", "/");
		}
		if( !userdir.endsWith("/") ) userdir += "/";
		return userdir+path;
	}
	/**
	 * 得到本地IP地址
	 * @return
	 * @throws Exception 
	 */
	public static String getLocalIP() throws Exception
	{
    	StringBuffer ipInfo = new StringBuffer();
		String ip = null;
		Enumeration<NetworkInterface> enu =java.net.NetworkInterface.getNetworkInterfaces(); 
	    while (enu.hasMoreElements()) 
	    {
		    NetworkInterface net = enu.nextElement();
		    if( ipInfo.length() > 0 ) ipInfo.append("\r\n");
		    ipInfo.append(net.getName());
			ipInfo.append(" 【");
		    ipInfo.append(net.getDisplayName());
			ipInfo.append(" 】");
		    
		    String tag = net.getName().toLowerCase();
		    if( !tag.startsWith("eth") &&
		    	!tag.startsWith("em") &&
		    	!tag.startsWith("en") &&
		    	!tag.startsWith("net") &&
		    	!tag.startsWith("wlan") &&
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
	    	if( ip != null && tag.endsWith("1") ) break;
	    }
	    if( ip != null ) return ip;
	    throw new Exception("Failed to get the ipinof of local:\r\n"+ipInfo.toString());
    }
	

    private static char baseChar[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3',
        '4', '5', '6', '7', '8', '9', '+', '/'
    };
    private static byte position[] = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, 62, -1, 63, -1, 63, 52, 53,
        54, 55, 56, 57, 58, 59, 60, 61, -1, -1,
        -1, 0, -1, -1, -1, 0, 1, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
        25, -1, -1, -1, -1, -1, -1, 26, 27, 28,
        29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
        39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
        49, 50, 51, -1, -1, -1, -1, -1
    };

    public static String encode(byte b[])
    {
        int code = 0;
        StringBuffer sb = new StringBuffer((b.length - 1) / 3 << 6);
        for(int i = 0; i < b.length; i++)
        {
            code |= b[i] << 16 - (i % 3) * 8 & 255 << 16 - (i % 3) * 8;
            if(i % 3 == 2 || i == b.length - 1)
            {
                sb.append(baseChar[(code & 0xfc0000) >>> 18]);
                sb.append(baseChar[(code & 0x3f000) >>> 12]);
                sb.append(baseChar[(code & 0xfc0) >>> 6]);
                sb.append(baseChar[code & 0x3f]);
                code = 0;
            }
        }

        if(b.length % 3 > 0)
            sb.setCharAt(sb.length() - 1, '=');
        if(b.length % 3 == 1)
            sb.setCharAt(sb.length() - 2, '=');
        return sb.toString();
    }
    
    /**
     * 
     * @param code
     * @return
     */
    public static byte[] decode(String code)
    {
        if(code == null)
            return null;
        int len = code.length();
        if(len % 4 != 0)
        {
            return code.getBytes(); // new IllegalArgumentException("Base64 string length must be 4*n");
        }
        if(code.length() == 0)
            return new byte[0];
        int pad = 0;
        if(code.charAt(len - 1) == '=')
            pad++;
        if(code.charAt(len - 2) == '=')
            pad++;
        int retLen = (len / 4) * 3 - pad;
        byte ret[] = new byte[retLen];
        for(int i = 0; i < len; i += 4)
        {
            int j = (i / 4) * 3;
            char ch1 = code.charAt(i);
            char ch2 = code.charAt(i + 1);
            char ch3 = code.charAt(i + 2);
            char ch4 = code.charAt(i + 3);
            int tmp = position[ch1] << 18 | position[ch2] << 12 | position[ch3] << 6 | position[ch4];
            ret[j] = (byte)((tmp & 0xff0000) >> 16);
            if(i < len - 4)
            {
                ret[j + 1] = (byte)((tmp & 0xff00) >> 8);
                ret[j + 2] = (byte)(tmp & 0xff);
                continue;
            }
            if(j + 1 < retLen)
                ret[j + 1] = (byte)((tmp & 0xff00) >> 8);
            if(j + 2 < retLen)
                ret[j + 2] = (byte)(tmp & 0xff);
        }

        return ret;
    }

    /**
     * 将序列化对象转换为字节流
     * @param obj
     * @return
     */
    public static byte[] convertObjectToBytes(Object obj)
    {
    	if (obj == null) return null;
    	
    	ByteArrayOutputStream out = null;
    	ObjectOutputStream oos = null;
    	try 
    	{
    		out = new ByteArrayOutputStream();
        	oos = new ObjectOutputStream(out);
            oos.writeObject(obj);
            oos.flush();
            
            return out.toByteArray();
		}
    	catch (Exception e)
    	{
		}
    	finally
    	{
    		try {
	    		if (oos != null) 
				{
					oos.close();
				}
	    		if (out != null)
				{
	    			out.close();
				}
    		}
    		catch (IOException e) {}
		}
    	
    	return null;
    }

    /**
     * 从二级制字节流中读取序列化对象
     * @param payload
     * @return
     */
    public static final Serializable readSerializableNoException( byte payload[] )
    {
    	return readSerializableNoException( payload, 0, payload.length );
    }
    /**
     * 从二级制字节流中读取序列化对象
     * @param payload
     * @param offset
     * @param length
     * @return
     */
    public static final Serializable readSerializableNoException( byte payload[], int offset, int length )
    {
        try
        {
            return readSerializable( payload, offset, length );
        }
        catch( Exception ex )
        {
            return null;
        }
    }

    /**
     * 
     * @param payload
     * @param offset
     * @param length
     * @return
     * @throws Exception
     */
    public static final Serializable readSerializable( byte payload[], int offset, int length )
        throws Exception
    {
        ObjectInputStream ois = null;
        Serializable s = null;
        try
        {
            ois = new ObjectInputStream(new java.io.ByteArrayInputStream(payload, offset, length));
            s = (Serializable) ois.readObject();
            return s;
        }
        catch(Exception e )
        {
            throw e;
        }
        finally
        {
            ois.close();
        }
    }

    /**
     * 
     * @param payload
     * @param offset
     * @param length
     * @return
     * @throws Exception
    public static final Serializable readSerializables( ObjectInputStream ois )
    {
    	if( ois == null ) return null;
        Serializable s = null;
        try
        {
            return (Serializable) ois.readObject();
        }
        catch(Exception e )
        {
        	return null;
        }
        finally
        {
        	if( ois != null )
	            try
				{
					ois.close();
				}
				catch (IOException e)
				{
				}
        }
    }
     */
}
