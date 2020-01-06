
package com.focus.cos.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.Key;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.focus.cos.control.COSApi;
import com.focus.util.Log;
import com.focus.util.Tools;

public abstract class AbaseServlet extends HttpServlet
{
	private static final long serialVersionUID = -6082791442929441606L;
	protected COSApi server = null;
	public AbaseServlet(COSApi server)
	{
		this.server = server;
	}

	protected void unknown(HttpServletRequest request)
	{
		StringBuffer sb = new StringBuffer("Unknown reqeust("+request.getRequestURI()+") from "+request.getRemoteAddr()+"/"+getIp(request));
		Enumeration<String> names = request.getHeaderNames();
		while( names.hasMoreElements() )
		{
			String key = names.nextElement();
			String value = request.getHeader(key);
			if( "connection".equalsIgnoreCase(key) )
				value = "keep-alive";
			sb.append("\r\n\t\t"+key+" = "+value);
		}
		Log.war(sb.toString());
	}
	
	/**
	 * 重定向到指定服务器
	 * @param request
	 */
	protected void redirect(HttpServletRequest request, HttpServletResponse response, byte[] payload)
	{
		if( !this.server.isProxy() )
		{
			return;
		}
		StringBuffer log = new StringBuffer();
		Map<String, String> parameters = new HashMap<String, String>();
		Enumeration<String> names = request.getHeaderNames();
		while( names.hasMoreElements() )
		{
			String key = names.nextElement();
			String value = request.getHeader(key);
			parameters.put(key, value);
		}
		String cosapi = server.getCosapi();
		String path = request.getServletPath();
		cosapi += path.startsWith("/")?path.substring(1):path;
		try 
		{
			String ci = COSApi.getRequestValue(request, "COS-ID");
			String from = COSApi.getRequestValue(request, "From");
			String sid = COSApi.getRequestValue(request, "SID");
			long ts = System.currentTimeMillis();
			log.append("Redirect("+request.getMethod()+
					", "+request.getRequestURI()+
					", "+from+"@"+ci+":"+sid+") from "+getIp(request)+" to "+cosapi);
			byte[] buffer;
			if( payload != null )
			{
				buffer = ApiUtils.doPost(cosapi, payload, parameters, 2);
			}
			else
			{
				buffer = ApiUtils.doGet(cosapi, parameters, true);
			}
			ts = System.currentTimeMillis() - ts;
			log.append("\r\n\tAfter "+ts+"ms get "+buffer.length+" bytes");
			Log.msg(log.toString());
			this.write(response, buffer);
		} 
		catch (Exception e) 
		{
			log.append("\r\n\tFailed to redirect for "+e.getMessage());
			Log.err(log.toString());
			response.setStatus(500);
			try 
			{
				OutputStream out = response.getOutputStream();
				out.write(e.getMessage().getBytes());
				out.flush();
				out.close();
			} 
			catch (Exception e1) 
			{
			}
			if( e.getMessage().endsWith("Unauthorized") ){
				Log.war("Stop current runtime for need to register again.");
				System.exit(0);
			}
			else{
				int i = e.getMessage().indexOf(": ");
				if( i != -1 )
				{
					String status = e.getMessage().substring(0, i);
					if( Tools.isNumeric(status) && 401 == Integer.parseInt(status) )
					{
						Log.war("Stop current runtime for need to register again.");
						System.exit(0);
					}
				}
			}
		}
	}
	public abstract void save(HttpServletRequest request, byte[] payload, JSONObject response) throws Exception;
	public abstract void query(HttpServletRequest request, HttpServletResponse response, StringBuffer log) throws Exception;
	/**
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		String ci = COSApi.getRequestValue(request, "COS-ID");
		String from = COSApi.getRequestValue(request, "From");
		String sid = COSApi.getRequestValue(request, "SID");
		if( !server.auth(request))
		{
			//获得请求的ID，如果不是本地请求则拒绝
			response.setStatus(401);
			StringBuffer sb = new StringBuffer("["+(server.isProxy()?"Proxy":"Agent")+"] Unauthentic reqeust("+request.getRequestURI()+", "+from+"@"+ci+":"+sid+") from "+getIp(request));
			Iterator<String> iterator = server.getIdentities().keySet().iterator();
			while( iterator.hasNext() )
			{
				String key = iterator.next();
				sb.append("r\n\t"+key+": "+server.getIdentities().get(key).toString());
			}
			Enumeration<String> names = request.getHeaderNames();
			while( names.hasMoreElements() )
			{
				String key = names.nextElement();
				String value = request.getHeader(key);
				if( "connection".equalsIgnoreCase(key) )
					value = "keep-alive";
				sb.append("\r\n\t\t"+key+" = "+value);
			}
			OutputStream out = response.getOutputStream();
			out.write("Unauthentic client".getBytes());
			out.flush();
			out.close();
			Log.war(sb.toString());
			return;
		}
		if( server.isProxy() )
		{
			redirect(request, response, null);
			return;
		}
		StringBuffer log = new StringBuffer("["+(server.isProxy()?"Proxy":"Agent")+"] GET("+request.getRequestURI()+", "+from+"@"+ci+":"+sid+") from "+request.getRemoteAddr()+"/"+getIp(request));
		try 
		{
			this.query(request, response, log);
			Log.msg(log.toString());
		}
		catch (Exception e) 
		{
			Log.err(log.toString(), e);
		}
	}
	
	/**
	 * 受理消息请求
	 * @param request
	 * @param response
	 * @return
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException 
	{
		String ci = COSApi.getRequestValue(request, "COS-ID");
		String from = COSApi.getRequestValue(request, "From");
		String sid = COSApi.getRequestValue(request, "SID");
		if( !server.auth(request) && !request.getServletPath().equals("/") )
		{
			//获得请求的ID，如果不是本地请求则拒绝
			response.setStatus(401);
			StringBuffer sb = new StringBuffer("["+(server.isProxy()?"Proxy":"Agent")+"] Unauthentic reqeust("+request.getRequestURI()+", "+from+"@"+ci+":"+sid+") from "+getIp(request));
			Enumeration<String> names = request.getHeaderNames();
			while( names.hasMoreElements() )
			{
				String key = names.nextElement();
				String value = request.getHeader(key);
				if( "connection".equalsIgnoreCase(key) )
					value = "keep-alive";
				sb.append("\r\n\t\t"+key+" = "+value);
			}
			OutputStream out = response.getOutputStream();
			out.write("Unauthentic client".getBytes());
			out.flush();
			out.close();
			Log.war(sb.toString());
			return;
		}
		
		ServletInputStream servletInputStream = request.getInputStream();
		byte[] payload = readFullInputStream(servletInputStream);
		if( server.isProxy() )
		{
			redirect(request, response, payload);
			return;
		}
		StringBuffer log = new StringBuffer("["+(server.isProxy()?"Proxy":"Agent")+"] Post("+request.getRequestURI()+", "+from+"@"+ci+":"+sid+") from "+getIp(request));
		log.append("\r\n\tReceive "+payload.length+" bytes.");
		JSONObject rsp = new JSONObject();
		Key identity = server.getIdentity(ci);
		try
		{
			if( payload.length == 0 )
			{
				rsp.put("error", "Not found incoming data.");
			}
			else
			{
				if( identity != null && !request.getServletPath().equals("/") )
				{
					Cipher c1 = Cipher.getInstance("DES");
					c1.init(Cipher.DECRYPT_MODE, identity);
					payload = c1.doFinal(payload);
					log.append("\r\n\tSucceed to decrypt "+payload.length);
				}
	//			System.out.println(new String(s, "UTF-8"));
				save(request, payload, rsp);
				log.append("\r\n\t"+rsp.toString());
			}
		}
		catch (Exception e) 
		{
			log.append("\r\n\tFound exception "+e);
			rsp.put("error", e.getMessage());
		}
		finally
		{
			write(ci, response, rsp);
			Log.msg(log.toString());
		}
	}
	
	/**
	 * 读取输入流为二进制数组
	 * @param servletInputStream
	 * @return
	 */
	public static byte[] readFullInputStream(InputStream servletInputStream)
	{
		List<byte[]> all = new ArrayList<byte[]>();
		int read = -1;
		int count = 0;
		byte[] catchs = new byte[1024];
		try {
			while((read = servletInputStream.read(catchs))>-1){
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
		
		return allDatas;
	}

	/**
	 * 写序列化对象给客户端
	 * @param response
	 * @param args
	 */
	protected void write(HttpServletResponse response, byte[] payload)
	{
		OutputStream out = null;
		try 
		{
			response.setStatus(200);
			out = response.getOutputStream();
			out.write(payload, 0, payload.length);
			out.flush();
		}
		catch (IOException e) 
		{
			response.setStatus(500);
		}
		finally
		{
			if( out != null )
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}
	/**
	 * 写序列化对象给客户端
	 * @param response
	 * @param args
	 */
	protected void write(String id, HttpServletResponse response, JSONObject json)
	{
		Key identity = server.getIdentity(id);
		OutputStream out = null;
		try 
		{
			response.setContentType("JSON");
			out = response.getOutputStream();
			byte[] payload = null;
			if( identity != null )
			{
				response.setStatus(200);
				Cipher c0 = Cipher.getInstance("DES");  
	            c0.init(Cipher.ENCRYPT_MODE, identity);
				payload = c0.doFinal(json.toString().getBytes("UTF-8"));
				out.write(payload);
			}
			else
			{
				response.setStatus(401);
				payload = "无效的请求".getBytes();
			}
			out.write(payload);
			out.flush();
		}
		catch (Exception e) 
		{
			response.setStatus(500);
		}
		finally
		{
			if( out != null )
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}
	/**
	 * 写序列化对象给客户端
	 * @param response
	 * @param args
	 */
	protected void write(String id, HttpServletResponse response, Serializable[] args, StringBuffer log)
	{
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		OutputStream out = null;
		try 
		{
			Key identity = server.getIdentity(id);
			if( identity != null )
			{
				baos = new ByteArrayOutputStream();
				response.setContentType("Serializable");
				out = response.getOutputStream();
				response.setStatus(200);
				oos = new ObjectOutputStream(baos);
				for(int i = 0; i < args.length;)
				{
					Serializable s = args[i++];
					oos.writeObject(s);
					if( i < args.length ) oos.write(7);
					oos.flush();
				}
				oos.close();
				Cipher c0 = Cipher.getInstance("DES");  
	            c0.init(Cipher.ENCRYPT_MODE, identity);
	            byte[] payload = baos.toByteArray();
				out.write(c0.doFinal(baos.toByteArray()));
				out.flush();
				log.append("\r\n\tsucceed to write "+args.length+" objects("+payload.length+" bytes) by identity("+identity+").");
			}
			else
			{
				out = response.getOutputStream();
				response.setStatus(401);
				out.write("无效的请求".getBytes());
				out.flush();
				log.append("\r\n\tfailed to write for not found identify.");
			}
		}
		catch (Exception e) 
		{
			log.append("\r\n\tfailed to write for "+e.getMessage());
			response.setStatus(500);
		}
		finally
		{
			if( oos != null )
				try {
					oos.close();
				} catch (IOException e) {
				}
			if( baos != null )
				try {
					baos.close();
				} catch (IOException e) {
				}
			if( out != null )
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}
	
    /**
     * 得到指定request的真实请求IP
     * @param request
     * @return
     */
    public static String getIp(HttpServletRequest request)
    {
        String wlanIp = request.getHeader("x-forwarded-for");
        wlanIp = wlanIp == null ? request.getHeader("x-real-ip") : wlanIp;
        if (wlanIp == null)  wlanIp = "unknown";
        return request.getRemoteAddr()+"/"+wlanIp;
    }
}
