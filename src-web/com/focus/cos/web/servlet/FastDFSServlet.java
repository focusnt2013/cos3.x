package com.focus.cos.web.servlet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerGroup;
import org.csource.fastdfs.TrackerServer;
import org.json.JSONObject;

import com.focus.util.Tools;

/**
 * Description: 实现DFS
 * Create Date:Oct 20, 2019
 * @author FocusLau
 * @since 1.0
 */
public class FastDFSServlet extends HttpServlet
{
	private static final long serialVersionUID = -9128884568497817187L;
	private static final Log log = LogFactory.getLog(FastDFSServlet.class);

	public void init(ServletConfig servletConfig) throws ServletException
	{
		log.info("init "+servletConfig.getServletName());
		super.init(servletConfig);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException,
			IOException
	{
		log.info("doGet");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		JSONObject rsp = new JSONObject();
		ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
		TrackerServer server = null;
		FileItem uploadfile = null;
		try{
			String filename = null;//request.getParameter("filename");
			List<FileItem> fileItems = upload.parseRequest(request);
			for( FileItem item : fileItems ){
				if( item.getName() != null ){//是文件
					uploadfile = item;
					System.err.println(String.format("%s(%s) Content-Type:%s, Size:%s", 
							item.getFieldName(), item.getName(), item.getContentType(), item.getSize()));
				}
				else{
					System.err.println(String.format("%s=%s", item.getFieldName(), item.getString()));
				}
				if( "fileId".equals(item.getFieldName()) ){
					filename = item.getString();
				}
			}
			String fdfsaddr = request.getParameter("fastdfs.tracker_servers");
			if( fdfsaddr == null || fdfsaddr.isEmpty() ){
				rsp.put("errcode", 2);
				rsp.put("errmsg", "客户端没有提供文件目标入库的FastDFS地址");
				response(response, rsp.toString());return;
			}
			String prefixurl = request.getParameter("prefixurl");
			if( prefixurl == null || prefixurl.isEmpty() ){
				rsp.put("errcode", 3);
				rsp.put("errmsg", "客户端没有提供目标入库文件的URL地址前缀");
				response(response, rsp.toString());return;
			}
			Enumeration<String> names = request.getParameterNames();
			while( names.hasMoreElements() )
			{
				String name = names.nextElement();
				String value = request.getParameter(name);
				System.err.println(name+"="+value);
			}
			int i = filename.lastIndexOf('.');
			String filetype = filename.substring(i+1);
			log.info(String.format("Upload the file of %s from \r\n\t%s\r\n\t%s",
				filetype, filename, prefixurl));
			
			String args[] = Tools.split(fdfsaddr, ",");
			InetSocketAddress[] servers = new InetSocketAddress[args.length];
			for(i = 0; i < args.length; i++){
				String addr[] = Tools.split(args[i], ":");
				servers[i] = new InetSocketAddress(addr[0], Integer.parseInt(addr[1]));
			}
			TrackerGroup group = new TrackerGroup(servers);
			TrackerClient client = new TrackerClient(group);
			server = client.getConnection(); 
			byte[] payload = uploadfile.get();
            NameValuePair meta_list [] = new NameValuePair[]{ 
                new NameValuePair("filename", filename), 
                new NameValuePair("size", String.valueOf(payload.length)) 
            }; 
			StorageClient storageClient = new StorageClient(server, null);
			String fileIds[] = storageClient.upload_file(payload, filetype, meta_list);
			String url = String.format("%s%s/%s", prefixurl, fileIds[0], fileIds[1]);
			log.info("Succeed to upload to "+url);
			rsp.put("errcode", 0);
			rsp.put("errmsg", "ok");
			rsp.put("url", url);
			rsp.put("size", payload.length);
			rsp.put("filename", filename);
		}
		catch(Exception e){
			e.printStackTrace();
			//connect to server 127.0.0.1:22122 fail
			if( e instanceof java.lang.NullPointerException ){
				rsp.put("errcode", 4);
				rsp.put("errmsg", "无法连接FastDFS系统上传文件失败");
			}
			else{
				//log.error("Failed to upload the file to", e);
				rsp.put("errcode", 4);
				rsp.put("errmsg", "上传文件到FastDFS出现异常"+e);
			}
		}
		finally{
			if( server != null ){
				try {
					server.close();
				} catch (IOException e) {
				}
			}
		}
		log.info(rsp.toString(4));
		if( rsp.getInt("errcode") != 0 ){
    		response(response, 500, rsp.getString("errmsg"));
		}
		response(response, rsp.toString());
	}
	/**
	 * 返回json数据
	 * @param rsp
	 * @param json
	 * @return
	 */
	public String response(HttpServletResponse rsp, String json)
	{
		ServletOutputStream sos = null;
		try
		{
			rsp.setContentType("application/json;charset=utf8");
    		rsp.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");
    		rsp.setStatus(200);
    		sos = rsp.getOutputStream();
    		sos.write(json.getBytes("UTF-8"));
		}
		catch (IOException e)
		{
			rsp.setStatus(500);
			log.error("Failed to resonse json", e);
		}
        finally
        {
        	if( sos != null )
	    		try
				{
	    			sos.flush();
	    			sos.close();
				}
				catch (IOException e)
				{
					log.error("", e);
				}
        }
		return null;
	}

	/**
	 * 返回错误
	 * @param rsp
	 * @param status
	 * @param msg
	 * @return
	 */
	public String response(HttpServletResponse rsp, int status, String msg)
	{
		rsp.setStatus(500);
		ServletOutputStream sos = null;
		try
		{
			sos = rsp.getOutputStream();
    		sos.write(msg.getBytes("UTF-8"));
		}
		catch(Exception e){
			log.error("Failed to response "+status, e);
		}
        finally
        {
        	if( sos != null )
	    		try
				{
	    			sos.flush();
	    			sos.close();
				}
				catch (IOException e)
				{
					log.error("", e);
				}
        }
		return null;
	}
}
