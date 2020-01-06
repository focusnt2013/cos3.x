package com.focus.cos.web.ops.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerGroup;
import org.csource.fastdfs.TrackerServer;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.control.Command;
import com.focus.cos.web.Version;
import com.focus.cos.web.common.COSConfig;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.paginate.PageBean;
import com.focus.cos.web.ops.service.FilesMgr;
import com.focus.cos.web.ops.service.MonitorMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.Tools;

/**
 * COS的集群文件资源管理器
 * @author focus lau
 * @create 2017-02-07
 */
public class FilesAction extends OpsAction 
{
	private static final long serialVersionUID = 4745006847372010934L;
	private static final Log log = LogFactory.getLog(FilesAction.class);
	/*文件内容编码格式*/
	private String encoding;
	/*相对路径的根目录路径*/
	private String rootdir = "";
	//文件大小
	private long length;
	/*文件管理器*/
	protected FilesMgr filesMgr;
	/*是否可解压*/
	private boolean decompressable;
	/*以逗号分割的文件名称集合*/
	private String filenames;
	//分页配置
	protected PageBean pageBean;
	
	public FilesAction()
	{
		pageBean = new PageBean();
		this.pageBean.setPageCount(1);
		this.pageBean.setPage(-1);
		this.pageBean.setPageSize((int)Kit.Ms);
	}
	
	/**
	 * 上传dfs
	 * @return
	 */
	public String upfdfs()
	{
		JSONObject rsp = new JSONObject();
		HttpServletRequest request = super.getRequest();
		HttpServletResponse response = super.getResponse();
		if( uploadfile == null || uploadfile.length() == 0 ){
			rsp.put("errcode", 1);
			rsp.put("errmsg", "不允许上传空文件");
			return super.response(super.getResponse(), rsp.toString());
		}
		String fdfsaddr = request.getParameter("fastdfs.tracker_servers");
		if( fdfsaddr == null || fdfsaddr.isEmpty() ){
			rsp.put("errcode", 2);
			rsp.put("errmsg", "客户端没有提供文件目标入库的FastDFS地址");
			return super.response(super.getResponse(), rsp.toString());
		}
		String prefixurl = request.getParameter("prefixurl");
		if( prefixurl == null || prefixurl.isEmpty() ){
			rsp.put("errcode", 3);
			rsp.put("errmsg", "客户端没有提供目标入库文件的URL地址前缀");
			return super.response(super.getResponse(), rsp.toString());
		}
//		Enumeration<String> names = request.getParameterNames();
//		while( names.hasMoreElements() )
//		{
//			String name = names.nextElement();
//			String value = request.getParameter(name);
//			System.err.println(name+"="+value);
//		}
		String filename = request.getParameter("filename");
		if( filename == null ){
			filename = request.getParameter("fileId");
		}
		if( filetype == null ){
			int i = filename.lastIndexOf('.');
			filetype = filename.substring(i+1);
		}
		log.info(String.format("Upload the file of %s from \r\n\t%s\r\n\t%s\r\n\t%s",
			filetype, filename, uploadfile, prefixurl));
		TrackerServer server = null;
		try{
			Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(filetype);
			if (iter.hasNext()) {
				ImageReader reader = iter.next();
				ImageInputStream iis = new FileImageInputStream(uploadfile);
				reader.setInput(iis);
				int width = reader.getWidth(reader.getMinIndex());
				int height = reader.getHeight(reader.getMinIndex());
				rsp.put("width", width);
				rsp.put("height", height);
				reader.dispose();
				iis.close();
			}	
			String args[] = Tools.split(fdfsaddr, ",");
			InetSocketAddress[] servers = new InetSocketAddress[args.length];
			for(int i = 0; i < args.length; i++){
				String addr[] = Tools.split(args[i], ":");
				servers[i] = new InetSocketAddress(addr[0], Integer.parseInt(addr[1]));
			}
			TrackerGroup group = new TrackerGroup(servers);
			TrackerClient client = new TrackerClient(group);
			server = client.getConnection(); 
            NameValuePair meta_list [] = new NameValuePair[]{ 
                new NameValuePair("filename", filename), 
                new NameValuePair("size", String.valueOf(uploadfile.length())) 
            }; 
			StorageClient storageClient = new StorageClient(server, null);
			String fileIds[] = storageClient.upload_file(uploadfile.getAbsolutePath(), filetype, meta_list);
			String url = String.format("%s%s/%s", prefixurl, fileIds[0], fileIds[1]);
			log.info("Succeed to upload to "+url);
			rsp.put("errcode", 0);
			rsp.put("errmsg", "ok");
			rsp.put("url", url);
			rsp.put("size", uploadfile.length());
			rsp.put("filename", filename);
			rsp.put("filetype", filetype);
		}
		catch(Exception e){
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
    		return super.response(response, 500, rsp.getString("errmsg"));
		}
		return super.response(response, rsp.toString());
	}
	
	/**
	 * 伺服器的资源管理器导航
	 * @return
	 */
	public String navigate()
	{
		try
		{
			JSONObject privileges = getMonitorMgr().getClusterPrivileges(super.getUserRole(), super.getUserAccount());
			JSONArray clusters = this.getMonitorMgr().getClusterTree(privileges, super.isSysadmin(), false);
			if( Tools.isNumeric(id) )
			{
				clusters = this.getMonitorMgr().getClusterTree(clusters, Integer.parseInt(id));
			}
			jsonData = "[]";
			if( clusters == null )
			{
				clusters = new JSONArray();
			}
//			System.err.println("naviagte "+clusters.toString(4));
	//		if( this.grant )
	//		{
	//			JSONObject local = new JSONObject();
	//			local.put("name", "本地服务器的Zookeeper");
	//			local.put("ip", "127.0.0.1");
	//			local.put("port", WebWorkConfigLoader.getLocalMonitorPort());
	//			local.put("icon", "images/icons/cluster.png");
	//			local.put("target", "local");
	//			trees.put(local);
	//		}
			this.grant = super.isSysadmin();
			jsonData = clusters.toString();
		} catch (Exception e) {
			super.responseException = e.getMessage();
		}
		return "navigate";
	}

	/**
	 * 文件编辑界面
	 * @return
	 */
	public String edit(){
		if( path == null || path.isEmpty() ){
			this.setResponseException("没有提供编辑文件的路径");
			return "404";
		}
		if( id != null )
		{
			JSONObject server = this.getMonitorMgr().getServer(this.getServerId());
			log.info("Open the files of "+server+" by id "+id);
			if( server == null )
			{
				this.setResponseException("未能找到您要打开的伺服器。");
				return "404";
			}
				ip = server.getString("ip");
				port = server.getInt("port");
//				if( !server.has("state") || server.getInt("state") == 0 )
				if( this.getMonitorMgr().getState(this.getServerId()) == 0 )
				{
					this.setResponseException("伺服器监控未启动，不能打开其文件管理器。");
					return "404";
				}
		}
		else{
			if( port == 0 ){
				port = COSConfig.getLocalControlPort();
			}
		}

		try 
		{
			this.grant = super.isSysadmin();
			if( !grant )
			{
				JSONObject privileges = this.getMonitorMgr().getServerPrivileges(super.getUserRole(), super.getUserAccount(), this.getMonitorMgr().getServer(this.getServerId()));
				grant = privileges.has("files")&&privileges.getBoolean("files");
			}
			JSONArray trees = new JSONArray();
			path = path==null?"":path;
			path = path.replaceAll("\\\\", "/");
			int i = path.lastIndexOf("/");
			filenames = "";
			if( i != -1  ){
				filenames = path.substring(i+1);
				if( !filenames.isEmpty() ){
					path = path.substring(0, i);
				}
			}
			if( path.endsWith("/") ){
				path = path.substring(0, path.length() - 1);
			}
			boolean relative = true;
			if( path.startsWith("/") || path.indexOf(":") == 1 ){
				relative = false;
			}
			JSONObject treeNode = new JSONObject();
			treeNode.put("name", "相对路径目录");
			if( !filenames.isEmpty() ){
				treeNode.put("name", "相对路径目录("+filenames+")");
			}
			treeNode.put("path", path);
			treeNode.put("id", "root");
			treeNode.put("iconClose", "images/icons/folder_closed.png");
			treeNode.put("iconOpen",  "images/icons/folder_opened.png");
			treeNode.put("isParent", true);
			trees.put(treeNode);
			if( relative ){
				JSONObject rootDir = filesMgr.fetchFiles(ip, port, "", relative, null);
//				System.err.println(rootDir.toString(4));
				treeNode.put("rootdir", rootDir.getString("rootdir"));
			}			
			this.jsonData = trees.toString();
			return "edit";
		}
		catch (Exception e) 
		{
			super.getSession().setAttribute("referer", "files!open.action");
			super.getSession().setAttribute("exception", e);
			this.responseException = "打开集群伺服器【"+ip+"】文件编辑器异常"+e.getMessage();
			super.getSession().setAttribute("exceptionTips", responseException);
			return "exception";
		}
	}

	/**
	 * 列表文件
	 * @return
	 */
	public String list(){
		try 
		{
			if( this.rootdir != null && !this.rootdir.isEmpty() ){
				path = this.rootdir+path;
			}
			JSONObject list = filesMgr.fetchFiles(ip, port, path, false, null);
			System.err.println(list.toString(4));
			this.jsonData = list.toString();
			return "list";
		}
		catch (Exception e) 
		{
			super.getSession().setAttribute("referer", "files!open.action");
			super.getSession().setAttribute("exception", e);
			this.responseException = "打开集群伺服器【"+ip+"】文件列表异常"+e.getMessage();
			super.getSession().setAttribute("exceptionTips", responseException);
			return "exception";
		}
	}
	/**
	 * 伺服器资源管理器
	 * @return
	 */
	public String open()
	{
		if( id != null )
		{
			JSONObject server = this.getMonitorMgr().getServer(this.getServerId());
			log.info("Open the files of "+server+" by id "+id);
			if( server == null )
			{
				this.setResponseException("未能找到您要打开的伺服器。");
				return "404";
			}
				ip = server.getString("ip");
				port = server.getInt("port");
//				if( !server.has("state") || server.getInt("state") == 0 )
				if( this.getMonitorMgr().getState(this.getServerId()) == 0 )
				{
					this.setResponseException("伺服器监控未启动，不能打开其文件管理器。");
					return "404";
				}
		}

		try 
		{
			this.grant = super.isSysadmin();
			if( !grant )
			{
				JSONObject privileges = this.getMonitorMgr().getServerPrivileges(super.getUserRole(), super.getUserAccount(), this.getMonitorMgr().getServer(this.getServerId()));
				grant = privileges.has("files")&&privileges.getBoolean("files");
			}
			JSONArray trees = new JSONArray();
			path = path==null?"":path;
			path = path.replaceAll("\\\\", "/");
			int i = path.indexOf('/');
			int j = path.indexOf(":");
			boolean relative = i < 0;
			if( j > 0 ){
				relative = i > 3 || i < 0;
			}
			JSONObject treeNode = filesMgr.fetchFiles(ip, port, path, relative, null);
			treeNode.put("name", "缺省工作目录");
			treeNode.put("iconClose", "images/icons/folder_closed.png");
			treeNode.put("iconOpen",  "images/icons/folder_opened.png");
			treeNode.put("open", true);
			trees.put(treeNode);
			
			if( this.isSysadmin() ) {
				treeNode = new JSONObject();
				treeNode.put("path", "/");//当前缺省节点
				treeNode.put("name", ip+"系统目录");
				treeNode.put("icon", "images/icons/folder_up.png");
				treeNode.put("isParent", true);
				trees.put(treeNode);	
			}
			this.jsonData = trees.toString();
//			System.err.println(trees.toString(4));
			return "open";
		}
		catch (Exception e) 
		{
			super.getSession().setAttribute("referer", "files!open.action");
			super.getSession().setAttribute("exception", e);
			this.responseException = "打开集群伺服器【"+ip+"】文件管理器异常"+e.getMessage();
			super.getSession().setAttribute("exceptionTips", responseException);
			return "exception";
		}
	}
	
	/**
	 * 上传ZIP压缩包，到集群伺服器。自动解压
	 * @return
	 */
	public String importzip()
	{
		String version = this.getMonitorMgr().getControlVersion(ip, port);
		final String baseline = "3.17.5.16";
		if( !Version.match(version, baseline) )
		{
			JSONObject rsp = new JSONObject();
			this.responseMessage = "伺服器【"+ip+"】主控版本("+version+")暂不支持压缩包上传且自动解压，请升级您的主控引擎到("+baseline+")。";
			rsp.put("alt", responseMessage);
			return response(super.getResponse(), rsp.toString());
		}
		this.editable = true;
		return importfile();
	}
	/**
	 * 上传文件
	 * @return
	 */
	public String importfile()
	{
		JSONObject rsp = new JSONObject();
		if( this.uploadfile == null || !uploadfile.exists() )
		{
			rsp.put("alt", "上传文件["+path+"]到集群伺服器【"+ip+"】失败，因为未能收到文件包");
			return response(super.getResponse(), rsp.toString());
		}
		if( path == null || path.isEmpty() )
		{
			if( this.rootdir == null || this.rootdir.isEmpty() )
			{
				rsp.put("alt", "上传文件到集群伺服器【"+ip+"】失败，因为未能收到文件路径参数");
				return response(super.getResponse(), rsp.toString());
			}
		}
		String filename = super.getRequest().getParameter("filename");
		if( filename == null || filename.isEmpty() )
		{
			rsp.put("alt", "上传文件到集群伺服器【"+ip+"】失败，因为未能收到文件名参数");
			return response(super.getResponse(), rsp.toString());
		}
		File file = new File(rootdir+path, filename);
//		if( !filename.endsWith(path) )
//		{
//			rsp.put("alt", "上传文件["+filename+"]到集群伺服器【"+ip+"】"+path+"失败，因为未能文件名参数错误");
//			return response(super.getResponse(), rsp.toString());
//		}
		log.info("Upload the file of "+file.getPath()+" from tempfile "+uploadfile+" to "+ip+":"+port);
		String filepath = file.getPath();
		byte[] buf = filepath.getBytes();
		int offset = 0;
    	byte[] payload = new byte[64*1024];
    	payload[offset++] = Command.CONTROL_COPYFILE;
    	payload[offset++] = (byte)buf.length;//文件路径
    	offset = Tools.copyByteArray(buf, payload, offset);
    	payload[offset++] = editable?(byte)7:0;//是否自动解压缩文件
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
        try
        {
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            InetAddress addr = InetAddress.getByName( ip );
            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port );
            datagramSocket.send( request );
			DatagramPacket response = new DatagramPacket(payload, payload.length);
			datagramSocket.receive(response);
			int port1 = Tools.bytesToInt(payload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port1);
			socket = new Socket();
			socket.connect(endpoint, 15000);
            OutputStream out = socket.getOutputStream();
            FileInputStream fis = new FileInputStream(uploadfile);
            int len = 0;
            while( (len = fis.read(payload)) != -1 )
            {
            	out.write(payload, 0, len);
            	out.flush();
            }
            fis.close();
            out.close();

            this.responseMessage = "上传文件到集群伺服器【"+ip+"】"+filepath+"成功。";
            if( editable )
            {
//            	int progress = 0;
            	do
            	{
					datagramSocket.receive(response);
					payload = getGZIPResult(response);
		            String json = new String(payload, "UTF-8");
		            System.err.println(json);
		            if( json.startsWith("{") )
		            {
		            	JSONObject result = new JSONObject(json);
		            	if( result.has("error") )
		            	{
		            		throw new Exception(result.getString("error"));
		            	}
		            	if( result.has("import") )
		            	{
		                    this.responseMessage = "上传文件到集群伺服器【"+ip+"】"+filepath+"成功。";
		            	}
		            	if( result.has("decompression") )
		            	{
		            		this.responseMessage += "解压文件到"+result.getString("decompression")+"。";
		            		break;
		            	}
//		            	if( result.has("progress") )
//		            	{
//		            		progress = result.getInt("progress");
//		            	}
		            }
            	}
            	while( true );
            }
            log.info("Succeed to upload the file of "+filepath+" to "+ip+":"+port1);
    		logoper(responseMessage, "文件管理", null, "files!open.action?ip="+ip+"&port="+port);
			rsp.put("alt", responseMessage);
			rsp.put("succeed", true);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
					null,
					"files!open.action?ip="+ip+"&port="+port,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        }
        catch(Exception e)
        {
            this.responseException = "上传文件到集群伺服器【"+ip+"】"+filepath+"出现异常"+e.getMessage();
    		logoper(responseException, "文件管理", null, "files!open.action?ip="+ip+"&port="+port, e);
    				SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseException, super.getUserAccount()),
					null,
					"files!open.action?ip="+ip+"&port="+port,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        }
        finally
        {
            length = uploadfile.length();
            uploadfile.delete();
    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
            	if( socket != null ) socket.close();
			}
			catch (IOException e)
			{
			}
        }
		return this.response(super.getResponse(), rsp.toString());
	}
	
	/**
	 * 复制文件或文件夹
	 * @return
	 */
	public String copy()
	{
		String version = this.getMonitorMgr().getControlVersion(ip, port);
		final String baseline = "3.17.5.16";
		if( !Version.match(version, baseline) )
		{
			this.responseMessage = "伺服器【"+ip+"】主控版本("+version+")暂不支持文件和文件夹批量同步拷贝，请升级您的主控引擎到("+baseline+")。";
			return "alert";
		}
		
		String srcpath = super.getRequest().getParameter("srcpath");
		String destpath = super.getRequest().getParameter("destpath");
		String destserver = super.getRequest().getParameter("destserver");
		File srcdir = new File(srcpath);
		log.info("Copy "+srcdir.getPath()+"/"+filenames+" to '"+destpath+"'("+destserver+") from "+ip+":"+port);
		StringBuffer logtxt = new StringBuffer();
//		log.info("Download "+path +"(rootdir:"+rootdir+") from "+ip+":"+port);
    	byte[] payload = new byte[1024*1024];
    	int offset = 0;
    	ServletOutputStream sos = null;
    	FileOutputStream fos = null;
    	getResponse().setContentType("text/html");
    	getResponse().setCharacterEncoding("UTF-8");
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
    	File tempdir = new File(PathFactory.getAppPath(), "temp/"+String.valueOf(System.currentTimeMillis()));
    	tempdir.mkdirs();
        File tempfile = new File(tempdir, srcdir.getName()+".zip" );
    	try
    	{
    		sos = getResponse().getOutputStream();
    		sos.print("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
    		String txt = "同步拷贝文件或文件夹("+srcpath+")到'"+(destpath.isEmpty()?"缺省工作目录":destpath)+"'";
    		responseMessage = txt;
    		pagereport(sos, -1, false, txt);
    		logtxt.append(txt);
        	payload[offset++] = Command.CONTROL_GETFILE;
        	payload[offset++] = 1;//标记要压缩
        	byte buf[] = srcpath.getBytes("UTF-8");
        	Tools.intToBytes(buf.length, payload, offset, 2);
        	offset += 2;
        	offset = Tools.copyByteArray(buf, payload, offset);
    		payload[offset++] = 0;
        	if( filenames != null && !filenames.isEmpty() )
        	{
        		String[] args = Tools.split(filenames, ",");
        		if( args.length > 0 )
        		{
	            	ArrayList<String> names = new ArrayList<String>();
        			payload[offset-1] = 1;
        	    	Tools.intToBytes(args.length, payload, offset, 2);
        	    	offset += 2;
	        		for(String filename: args)
	        		{
	        	    	payload[offset++] = (byte)filename.length();
	        	    	offset = Tools.copyByteArray(filename.getBytes("UTF-8"), payload, offset);
	        	    	pagereport(sos, 1, false, filename);
	            		logtxt.append("\r\n\t"+filename);
	            		if( !filename.isEmpty() ) names.add(filename);
	        		}
	        		if( names.size() == 1 )
	        		{
	        			tempfile = new File(tempdir, names.get(0)+".zip" );
	        		}
        		}
        	}
    		ArrayList<JSONObject> servers = new ArrayList<JSONObject>();
    		String[] args = Tools.split(destserver, ",");
    		pagereport(sos, 0, false, "目标伺服器包括:");
    		logtxt.append("\r\n目标伺服器包括:");
    		for( String arg : args )
    		{
    			if( !Tools.isNumeric(arg) ) continue;
    			int sid = Integer.parseInt(arg);
    			if( sid == 0 ) continue;
    			JSONObject server = this.getMonitorMgr().getServer(sid);
    			if( server == null )
    			{
    				txt = "未知伺服器["+sid+"]";
    				pagereport(sos, 1, false, "未知伺服器["+sid+"]");
            		logtxt.append("\r\n\t"+txt);
    				continue;
    			}
    			if( !server.has("state") || !server.has("ip") || !server.has("title") || !server.has("port") )
    			{
    				txt = "伺服器【"+server.getString("ip")+"】状态未知: "+server.toString();
    				pagereport(sos, 1, false, txt);
            		logtxt.append("\r\n\t"+txt);
    				continue;
    			}
    			if( server.getInt("state") == MonitorMgr.STATE_INT )
    			{
    				txt = "伺服器【"+server.getString("ip")+"】主控连接未建立不能同步拷贝";
    				pagereport(sos, 1, false, txt);
            		logtxt.append("\r\n\t"+txt);
    				continue;
    			}
    			servers.add(server);
    			txt = server.getString("ip")+" ["+server.getString("title")+"].";
    			pagereport(sos, 1, false, txt);
        		logtxt.append("\r\n\t"+txt);
    		}
    		if( servers.isEmpty() )
    		{
    			throw new Exception("同步拷贝目标伺服器都不可用。");
    		}
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(7000);
            InetAddress addr = InetAddress.getByName( ip );
            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port );
            datagramSocket.send( request );
            pagereport(sos, 0, true, "向伺服器【"+ip+"】请求下载要拷贝的文件或文件夹请求...");
			DatagramPacket response = new DatagramPacket(payload, payload.length);
			datagramSocket.receive(response);
			port = Tools.bytesToInt(payload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port);
			pagereport(sos, 1, true, "收到响应发起数据传送管道....");
			datagramSocket.close();
			socket = new Socket();
			socket.connect(endpoint, 60000);
            InputStream is = socket.getInputStream();
            long flag = is.read();
            if( flag != 11 && flag != 1)
            {
            	throw new Exception("伺服器【"+ip+"】错误的返回值("+flag+")");
            }
            
            pagereport(sos, 1, true, "接收文件数据准备保存到临时目录"+tempfile.getAbsolutePath()+".");
            long percent = payload.length;
            if( flag == 11 )
            {
            	is.read(payload, 0, 8);
            	flag = Tools.bytesToLong(payload, 0);
                pagereport(sos, 1, true, "接收文件数据大小: "+Kit.bytesScale(flag));
                sos.write("\r\n\t进度: |".getBytes("UTF-8"));
                sos.flush();
                percent = flag/100;
            }
			int len, step = 0, progress = 0;
			fos = new FileOutputStream(tempfile); 
			long size = 0;
			while( (len = is.read(payload, 0, payload.length)) != -1  )
    		{
    			length -= len;
    			size += len;
    			fos.write(payload, 0, len);
    			fos.flush();
    			if( size > percent )
    			{
    				step = writeProgress(sos, step, progress++);
    				size = 0;
    			}
    		}
			writeProgress(sos, step, 100);
			sos.flush();
    		is.close();
    		fos.close();
    		pagereport(sos, 0, true, "开始执行跨域同步拷贝(共"+servers.size()+"伺服器)...");
    		logtxt.append("\r\n跨域同步拷贝...");
    		for( JSONObject server : servers )
    		{
    			synchCcopy(server, destpath, tempfile, sos, logtxt);
//    			SynchCopy synchCopy = new SynchCopy(servers, server, destpath, tempfile, sos, logtxt);
//    			synchCopy.start();
    		}
    		logtxt.append("\r\n...完成跨域同步拷贝.");
    		logoper(responseMessage, "文件管理", logtxt.toString(), "files!open.action?ip="+ip+"&port="+port);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
					logtxt.toString(),
					null,
                    "情况确认", "#feedback?to="+super.getUserAccount());
    	}
		catch(Exception e)
		{
			log.error("Failed to copy for exception: ", e);
    		if( sos != null )
    		{
        		pagereport(sos, 0, true, "执行同步拷贝出现异常: "+e.getMessage());
    		}
			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(out);
			e.printStackTrace(ps);
			logtxt.append("\r\n");
			logtxt.append(out.toString());
			ps.close();

            this.responseException = responseMessage+"出现异常"+e.getMessage();
    		logoper(responseException, "文件管理", logtxt.toString(), "files!open.action?ip="+ip+"&port="+port, e);
			SvrMgr.sendNotiefiesToSystemadmin(
			super.getRequest(),
			"集群文件管理",
			String.format("用户[%s]"+responseException, super.getUserAccount()),
			logtxt.toString(),
			null,
            "情况确认", "#feedback?to="+super.getUserAccount());
		}
    	finally
    	{
    		if( fos != null )
				try {
					fos.close();
				} catch (IOException e2) {
				}
    		if( socket != null )
				try {
					socket.close();
				} catch (IOException e1) {
				}
    		if( sos != null )
    		{
        		pagereport(sos, 0, true, "完成同步拷贝操作.");
    			try {
					sos.println("</body>\r\n\r\n<script type='text/javascript' LANGUAGE='JavaScript'>if(parent&&parent.setScrollBottom){parent.setScrollBottom();}if(parent&&parent.skit_hiddenLoading){parent.skit_hiddenLoading();}</script></html>");
					sos.close();
				} catch (IOException e) {
				}
    		}
    		try {
				FileUtils.deleteDirectory(tempdir);
			} catch (IOException e) {
			}
    	}
    	return null;
	}
	
	/**
	 * 
	 */
	private void synchCcopy(JSONObject server, String path, File tempfile, ServletOutputStream sos, StringBuffer mainLog)
	{
		String filepath = new File(path, tempfile.getName()).getPath();
		String targetip = server.getString("ip");
		int targetport = server.getInt("port");
		String cosid = server.getString("security-key");
		
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
		pagereport(sos, 0, true, "同步拷贝文件到伺服器【"+ip+":"+cosid+"】 "+filepath);
        try
        {
			byte[] buf = filepath.getBytes("UTF-8");
			int offset = 0;
	    	byte[] payload = new byte[64*1024];
	    	payload[offset++] = Command.CONTROL_COPYFILE;
	    	payload[offset++] = (byte)buf.length;//文件路径
	    	offset = Tools.copyByteArray(buf, payload, offset);
	    	payload[offset++] = (byte)8;//是否自动解压缩文件并删除压缩文件
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            InetAddress addr = InetAddress.getByName( targetip );
            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, targetport );
            datagramSocket.send( request );
    		pagereport(sos, 1, true, "发送拷贝文件请求...");
			DatagramPacket response = new DatagramPacket(payload, payload.length);
			datagramSocket.receive(response);
			int port1 = Tools.bytesToInt(payload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port1);
			socket = new Socket();
    		pagereport(sos, 1, true, "收到响应发起数据传送管道...");
			socket.connect(endpoint, 60000);
            OutputStream out = socket.getOutputStream();
            FileInputStream fis = new FileInputStream(tempfile);
            int len = 0;
    		pagereport(sos, 1, true, "读取临时文件开始传送文件数据共"+tempfile.length());
            while( (len = fis.read(payload)) != -1 )
            {
            	out.write(payload, 0, len);
            	out.flush();
            }
            fis.close();
            out.close();
    		pagereport(sos, 1, true, "完成文件传送开始解压...");
            //再次接收数据报文
            sos.write("\r\n\t进度: |".getBytes("UTF-8"));
    		int step = 0, progress = 0;
            do
            {
				datagramSocket.receive(response);
				payload = getGZIPResult(response);
	            String json = new String(payload, "UTF-8");
            	JSONObject result = new JSONObject(json);
            	if( result.has("error") )
            	{
            		throw new Exception(result.getString("error"));
            	}
            	if( result.has("decompression") )
            	{
            		sos.write("|100%".getBytes());
            		
            		pagereport(sos, 1, true, "在伺服器完成文件拷贝"+result.getString("decompression")+". 耗时"+Kit.getDurationMs(result.getLong("duration"))+"毫秒");
    	            mainLog.append("\r\n向伺服器【"+targetip+"】同步拷贝文件成功到目录"+result.getString("decompression")+".");
    	            sos.flush();
    	            break;
            	}
            	if( result.has("progress") )
            	{
            		progress = result.getInt("progress");
	            	for(; step < progress/2; step++)
	            	{
	            		switch( step )
	            		{
	            		case 8: sos.write('2');break;
	            		case 18: sos.write('4');break;
	            		case 28: sos.write('6');break;
	            		case 38: sos.write('8');break;
	            		case 9:
	            		case 19:
	            		case 29:
	            		case 39:
	            			sos.write('0');
	            			break;
	            		case 10:
	            		case 20:
	            		case 30:
	            		case 40:
	            			sos.write('%');
	            			break;
	            		default: sos.write('+');break;
	            		}
	            		sos.flush();
	            	}
            	}
            }
            while(true);
        }
        catch(Exception e)
        {
        	log.error("", e);
    		pagereport(sos, 1, true, "解压文件失败因为出现异常:"+e.getMessage());
            mainLog.append("\r\n向伺服器【"+targetip+"】同步拷贝文件出现异常"+e.getMessage());
        }
        finally
        {
    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
            	if( socket != null ) socket.close();
			}
			catch (IOException e)
			{
			}
        }
	}
	
	/**
	 * 
	 * @param t
	 * @return
	private String time(int t)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("\r\n");
		while( t-- > 0 ){
			sb.append("\t");
		}
		sb.append("["+Tools.getFormatTime("HH:mm:ss", System.currentTimeMillis())+"] ");
		return sb.toString();
	}
	 */
	/**
	class SynchCopy extends Thread
	{
		private StringBuffer mainLog;
		private File tempfile;
		private String filepath;
		private String targetip;
		private int targetport;
		private ServletOutputStream sos;
		private String cosid;
		private JSONObject server;
		private ArrayList<JSONObject> servers;
		
		public SynchCopy(ArrayList<JSONObject> servers, JSONObject server, String path, File file, ServletOutputStream sos, StringBuffer mainLog) 
		{
			this.servers = servers;
			this.server = server;
			this.mainLog = mainLog;
			this.tempfile = file;
			this.filepath = new File(path, file.getName()).getPath();
			this.targetip = server.getString("ip");
			this.targetport = server.getInt("port");
			this.sos = sos;
			this.cosid = server.getString("security-key");
		}
		
		public void run()
		{
	    	DatagramSocket datagramSocket = null;
	    	Socket socket = null;
	    	write(time(0)+"同步拷贝文件到伺服器【"+ip+":"+cosid+"】 "+filepath);
	        try
	        {
				byte[] buf = filepath.getBytes("UTF-8");
				int offset = 0;
		    	byte[] payload = new byte[64*1024];
		    	payload[offset++] = Command.CONTROL_COPYFILE;
		    	payload[offset++] = (byte)buf.length;//文件路径
		    	offset = Tools.copyByteArray(buf, payload, offset);
		    	payload[offset++] = (byte)8;//是否自动解压缩文件并删除压缩文件
	        	datagramSocket = new DatagramSocket();
	            datagramSocket.setSoTimeout(15000);
	            InetAddress addr = InetAddress.getByName( targetip );
	            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, targetport );
	            datagramSocket.send( request );
	            write(time(1)+"发送拷贝文件请求...");
				DatagramPacket response = new DatagramPacket(payload, payload.length);
				datagramSocket.receive(response);
				int port1 = Tools.bytesToInt(payload, 0, 4);
				InetSocketAddress endpoint = new InetSocketAddress(addr, port1);
				socket = new Socket();
				write(time(1)+"收到响应发起数据传送管道...");
				socket.connect(endpoint, 60000);
	            OutputStream out = socket.getOutputStream();
	            FileInputStream fis = new FileInputStream(this.tempfile);
	            int len = 0;
	            write(time(1)+"读取临时文件开始传送文件数据共"+this.tempfile.length());
	            while( (len = fis.read(payload)) != -1 )
	            {
	            	out.write(payload, 0, len);
	            	out.flush();
	            }
	            fis.close();
	            out.close();
	            write(time(1)+"完成文件传送开始解压...");
	            //再次接收数据报文
	            write("\r\n\t|--------20%-------40%-------60%-------80%----------|100%");
	            write("\r\n\t ");
	    		int step = 0, progress = 0;
	            do
	            {
					datagramSocket.receive(response);
					payload = getGZIPResult(response);
		            String json = new String(payload, "UTF-8");
		            log.debug("The response is "+json);
		            if( json.startsWith("{") )
		            {
		            	JSONObject result = new JSONObject(json);
		            	if( result.has("error") )
		            	{
		            		throw new Exception(result.getString("error"));
		            	}
		            	if( result.has("progress") )
		            	{
		            		progress = result.getInt("progress");
			            	for(; step < progress/2; step++)
			            		write("+");
		            	}
		            	if( result.has("decompression") )
		            	{
		    	            write(time(1)+"在伺服器完成文件拷贝"+result.getString("decompression")+".");
		    	            mainLog.append("\r\n向伺服器【"+targetip+"】同步拷贝文件成功到目录"+result.getString("decompression")+".");
		            	}
		            }
		            else
		            {
	    	            write(time(1)+"解压文件失败因为伺服器主控引擎版本不支持解压拷贝功能.");
	    	            mainLog.append("\r\n向伺服器【"+targetip+"】同步拷贝文件失败因为伺服器主控引擎版本不支持解压拷贝功能.");
	    	            break;
		            }
	            }
	            while(progress<100);
	        }
	        catch(Exception e)
	        {
	        	log.error("", e);
	            write(time(1)+"解压文件失败因为出现异常:"+e.getMessage());
	            mainLog.append("\r\n向伺服器【"+targetip+"】同步拷贝文件出现异常"+e.getMessage());
	        }
	        finally
	        {
	    		try
				{
	    			if( datagramSocket != null ) datagramSocket.close();
	            	if( socket != null ) socket.close();
				}
				catch (IOException e)
				{
				}
	    		synchronized ( servers )
	    		{
	    			servers.remove(this.server);
	    			servers.notify();
				}
	        }
		}
		
		private void write(String outstr)
		{
    		synchronized (sos)
    		{
    			try 
    			{
    				sos.write(outstr.toString().getBytes("UTF-8"));
    				sos.flush();
    			}
    			catch (Exception e) 
    			{
    			}
    		}
		}
	}
*/
	
	/**
	 * 复制文件到多台服务器
	 * @return
	 */
	public String precopy()
	{
		try
		{
			JSONObject privileges = getMonitorMgr().getClusterPrivileges(super.getUserRole(), super.getUserAccount());
			super.localDataObject = privileges;
			JSONArray clusters = this.getMonitorMgr().getClusterTree(privileges, super.isSysadmin(), false);
			if( clusters == null )
			{
				clusters = new JSONArray();
			}
	//		System.err.println(clusters.toString(4));
			this.grant = super.isSysadmin();
			setClusterCheckbox(clusters, getServerId());
	//		if( this.grant )
	//		{
	//			JSONObject local = new JSONObject();
	//			local.put("name", "本地服务器的Zookeeper");
	//			local.put("ip", "127.0.0.1");
	//			local.put("port", WebWorkConfigLoader.getLocalMonitorPort());
	//			local.put("icon", "images/icons/cluster.png");
	//			local.put("target", "local");
	//			trees.put(local);
	//		}
			jsonData = clusters.toString();		
		} catch (Exception e) {
			super.responseException = e.getMessage();
		}
		return "copy";
	}
	
	/**
	 * 设置集群checkbox
	 * @param clusters
	 */
	private void setClusterCheckbox(JSONArray clusters, int id)
	{
		for(int i = 0; i < clusters.length(); i++)
		{
			JSONObject c = clusters.getJSONObject(i);
			if( c.has("children") )
			{
				this.setClusterCheckbox(c.getJSONArray("children"), id);
			}
			else
			{
				c.put("name", c.getString("name") + " ["+(c.has("title")?c.getString("title"):(c.has("stateinfo")?c.getString("stateinfo"):"Disconnect"))+"]");
				if( id == c.getInt("id") ) c.put("checked", true);
				int stat = this.getMonitorMgr().getState(c.getInt("id"));
				if( stat == MonitorMgr.STATE_INT )
				{
					c.put("chkDisabled", true);
					continue;
				}
				if( c.has("chkDisabled") ) c.remove("chkDisabled");
				if( grant )
				{
					continue;
				}
				
				if( localDataObject == null )
				{
					c.put("chkDisabled", true);
				}
				else
				{
					String pid = "manager.cluster."+c.getInt("id")+".f";
					if( !localDataObject.has(pid) || !localDataObject.getBoolean(pid) )
						c.put("chkDisabled", true);
				}
			}
		}
	}

	/**
	 * 批量下载文件或文件夹
	 * @return
	 */
	public String downloads()
	{
		String version = this.getMonitorMgr().getControlVersion(ip, port);
		final String baseline = "3.17.5.16";
		if( !Version.match(version, baseline) )
		{
			this.responseMessage = "伺服器【"+ip+"】主控版本("+version+")暂不支持文件和文件夹批量下载，请升级您的主控引擎到("+baseline+")。";
			return "alert";
		}
		this.editable = true;
//		String filename = super.getRequest().getParameter("filename");
//		System.err.println("filename="+filename);
		return download();
	}
	/**
	 * 下载文件
	 * 发送包:[命令字:1][模块ID字符串长度:1][模块ID:n]
	 * 响应包:[结束标识:1][字符串长度:2][字符串:n]
	 * 确认包：[标记:1](0表示继续，1表示中止)
	 * @return
	 */
	public String download()
	{
		if( path == null || path.isEmpty() )
		{
			this.responseException = "请选择要下载的文件或文件夹。";
			return "alert";
		}
		log.info("Download "+path +"(rootdir:"+rootdir+") from "+ip+":"+port);
		int port0 = port;
		String filepath = rootdir + path;
    	byte[] payload = new byte[64*1024];
    	int offset = 0;
    	ServletOutputStream out = null;
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
        try
        {
        	payload[offset++] = Command.CONTROL_GETFILE;
        	String command = super.getRequest().getParameter("command");
        	byte c = 1;
        	if( Tools.isNumeric(command) ) c = Byte.parseByte(command);
        	payload[offset++] = c;//zip;
        	byte buf[] = filepath.getBytes("UTF-8");
        	Tools.intToBytes(buf.length, payload, offset, 2);
        	offset += 2;
        	offset = Tools.copyByteArray(buf, payload, offset);
        	payload[offset++] = 0;
        	if( editable )
        	{
        		if( filenames != null )
        		{
            		String args[] = Tools.split(filenames, ",");
            		if( args.length > 0 )
            		{
            			payload[offset] = 1;
            	    	Tools.intToBytes(args.length, payload, offset, 2);
            	    	offset += 2;
    	        		for(String fielname: args)
    	        		{
    	        	    	payload[offset++] = (byte)fielname.length();
    	        	    	offset = Tools.copyByteArray(fielname.getBytes("UTF-8"), payload, offset);
    	        		}
            		}
        		}
        	}
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(7000);
            InetAddress addr = InetAddress.getByName( ip );
            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port );
            datagramSocket.send( request );
			DatagramPacket response = new DatagramPacket(payload, payload.length);
			datagramSocket.setSoTimeout(7000);
			datagramSocket.receive(response);
			port = Tools.bytesToInt(payload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port);
			datagramSocket.close();
			socket = new Socket();
			socket.connect(endpoint, 7000);
            InputStream is = socket.getInputStream();
            String filename = path.substring(path.lastIndexOf('/')+1);
            int flag = is.read();
            switch(flag)
            {
            case 10:
            case 11:
            	is.read(payload, 0, 8);
            	long size = Tools.bytesToLong(payload, 0, 8);
        		log.info("The length of donwload is "+size);
            	break;
            }
            if( flag == 0 || flag == 10 )
            {
            	if( filetype != null )
            	{//提供文件内容的类型
            		getResponse().setContentType(filetype);
            	}
            	else
            	{
            		throw new Exception("横须不可能出现到这里来");
            	}
            }
            else
            {
            	int i = filename.lastIndexOf(".");
            	filename = i!=-1?(filename.substring(0, i)+".zip"):(filename+".zip");
				getResponse().setContentType("application/binary;charset=ISO8859_1");
//        		getResponse().setHeader("Content-disposition", "attachment; filename="+filename);
            }
            getResponse().setHeader("Content-disposition", "attachment; filename*=UTF-8''"+URLEncoder.encode(filename,"UTF-8"));
    		out = getResponse().getOutputStream();
			int len;
			while( (len = is.read(payload, 0, payload.length)) != -1  )
    		{
    			length -= len;
        		out.write(payload, 0, len);
				out.flush();
    		}
    		is.close();
            this.responseMessage = "从集群伺服器【"+ip+"】下载"+filepath+"成功。";
    		logoper(responseMessage, "文件管理", null, "files!open.action?ip="+ip+"&port="+port0);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
					null,
					"files!open.action?ip="+ip+"&port="+port0,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        }
        catch(Exception e)
        {
        	this.responseException ="从集群伺服器【"+ip+"】下载"+filepath+" 出现异常 "+e.getMessage()+"，"+(port==0?"未能收到服务端响应":"服务端已响应但出现错误");
    		logoper(responseException, "文件管理", "", "files!open.action?ip="+ip+"&port="+port, e);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseException, super.getUserAccount()),
					null,
					"files!open.action?ip="+ip+"&port="+port0,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        	return "alert";
        }
        finally
        {
    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
            	if( socket != null ) socket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
        }
		return null;
	}
	
	/**
	 * 解压缩指定文件
	 * @return
	 */
	public String uncompress()
	{
		if( path == null || path.isEmpty() )
		{
			this.responseException = "请选择要下载的文件或文件夹。";
			return "alert";
		}
		log.info("Uncompress "+path +"(rootdir:"+rootdir+") from "+ip+":"+port);
		int port0 = port;
		String filepath = rootdir + path;
    	byte[] payload = new byte[64*1024];
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
    	String destpath = super.getRequest().getParameter("command");
    	ServletOutputStream sos = null;
        try
        {
			getResponse().setContentType("text/html");
			getResponse().setCharacterEncoding("UTF-8");
			sos = getResponse().getOutputStream();
			sos.print("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
			sos.write(("收到向伺服器【"+ip+"】解压文件请求.").getBytes("UTF-8"));
			sos.flush();
        	int offset = 0;
        	payload[offset++] = Command.CONTROL_DECOMPRESS;
        	byte buf[] = filepath.getBytes("UTF-8");
        	payload[offset++] = (byte)buf.length;
        	offset = Tools.copyByteArray(buf, payload, offset);
        	if( !destpath.isEmpty() )
        	{
            	if( !destpath.startsWith("/") )
            	{
            		destpath = rootdir + destpath;
            	}
            	if( !filepath.startsWith(destpath) )
            	{
                	payload[offset++] = (byte)destpath.length();
                	offset = Tools.copyByteArray(destpath.getBytes("UTF-8"), payload, offset);
            	}
            	else
            	{
            		payload[offset++] = 0;
            	}
        	}
        	else
        	{
        		payload[offset++] = 0;
        	}
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(30000);
            InetAddress addr = InetAddress.getByName( ip );
            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port );
            datagramSocket.send( request );
            sos.write(("\r\n请求解压"+path+"到"+(destpath.isEmpty()?"缺省工作目录":destpath)).getBytes("UTF-8"));
			DatagramPacket response = new DatagramPacket(payload, payload.length);
//			sos.write("\r\n|++++++++20%++++++++40%++++++++60%++++++++80%++++++++++|100%".getBytes());
//			sos.write("\r\n ".getBytes());
			sos.write("\r\n请耐心等待".getBytes("UTF-8"));
			sos.write("\r\n进度: |".getBytes("UTF-8"));
			sos.flush();
    		int step = 0, progress = 0;
    		JSONObject result = null;
            do
            {
				datagramSocket.receive(response);
				payload = getGZIPResult(response);
	            String json = new String(payload, "UTF-8");
            	result = new JSONObject(json);
            	if( result.has("error") )
            	{
            		throw new Exception(result.getString("error"));
            	}
            	if( result.has("decompression") )
            	{
            		sos.write("|100%".getBytes());
            		sos.write(("\r\n解压缩成功：耗时"+Kit.getDurationMs(result.getLong("duration"))+"毫秒").getBytes("UTF-8"));
            		sos.write(("\r\n解压缩到："+result.getString("decompression")).getBytes("UTF-8"));
            		sos.flush();
        			break;
            	}
            	if( result.has("progress") )
            	{
            		progress = result.getInt("progress");
	            	for(; step < progress/2; step++)
	            	{
	            		switch( step )
	            		{
	            		case 8: sos.write('2');break;
	            		case 18: sos.write('4');break;
	            		case 28: sos.write('6');break;
	            		case 38: sos.write('8');break;
	            		case 9:
	            		case 19:
	            		case 29:
	            		case 39:
	            			sos.write('0');
	            			break;
	            		case 10:
	            		case 20:
	            		case 30:
	            		case 40:
	            			sos.write('%');
	            			break;
	            		default: sos.write('+');break;
	            		}
	            		sos.flush();
	            	}
            	}
            }
            while(true);
            this.responseMessage = "从集群伺服器【"+ip+"】执行文件"+filepath+"解压缩到"+destpath+"成功。";
    		logoper(responseMessage, "文件管理", result.toString(4), "files!open.action?ip="+ip+"&port="+port0);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
					null,
					"files!open.action?ip="+ip+"&port="+port0,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        }
        catch(Exception e)
        {
        	log.error("", e);
        	this.responseException ="从集群伺服器【"+ip+"】执行文件"+filepath+"解压缩到"+destpath+" 出现异常 "+e.getMessage()+"，"+(port==0?"未能收到服务端响应":"服务端已响应但出现错误");
    		logoper(responseException, "文件管理", "", "files!open.action?ip="+ip+"&port="+port, e);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseException, super.getUserAccount()),
					null,
					"files!open.action?ip="+ip+"&port="+port0,
                    "情况确认", "#feedback?to="+super.getUserAccount());
    		if( sos != null )
				try {
					sos.write(("解压文件失败因为出现异常:"+e.getMessage()).getBytes("UTF-8"));
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        }
        finally
        {
        	if( sos != null )
	        	try {
					sos.println("</body>\r\n\r\n<script type='text/javascript' LANGUAGE='JavaScript'>if(parent&&parent.setScrollBottom){parent.setScrollBottom();}if(parent&&parent.skit_hiddenLoading){parent.skit_hiddenLoading();}</script></html>");
					sos.flush();
	        	} catch (IOException e1) {
				}
        	if( sos != null )
				try {
					sos.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
            	if( socket != null ) socket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
        }
		return null;
	}

	/**
	 * 在线浏览文件
	 * 判断文件的内容类型调用显示预览文件摘要
	 * @return
	 */
	public String preview()
	{
		log.debug("Preview the file(len="+length+") of "+path+"(rootdir:"+rootdir+") from "+ip+":"+port);
		String filepath = rootdir + path;
    	try
    	{
    		super.showBottom = super.getRequest().getParameter("noclose")==null;
    		long size = length;
			String filename = path.substring(path.lastIndexOf('/')+1);
    		//通过扩展文件名进行判断
    		JSONObject type = filesMgr.getFileTypeByExtension(path);
    		if( type == null )
    		{
    			if( length > 0 )
    			{
    				type = this.filesMgr.getFileTypeByBinary(ip, port, filepath, length, super.getResponse());
    				if( type == null ) throw new Exception("绝对不会运行到这里来");
    			}
    			else
    			{
    				type = new JSONObject();
    				type.put("icon", "file-o");
    				type.put("value", "application/octet-stream");
    				type.put("description", "未知类型");
    			}
    		}
    		super.scrollModelAutoFit = !type.getString("value").startsWith("text")&&length<Kit.Ms;
    		String filetime = super.getRequest().getParameter("filetime");//获取文件时间
			if( filetime == null ) filetime = "未知时间";
			type.put("filename", filename);
			type.put("time", filetime);
			filetype = type.getString("value");
			type.put("size", Kit.bytesScale(size));
			type.put("length", size);
			type.remove("payload");
			type.remove("offset");
			localDataObject = type;
//			System.err.println(type.toString(4));
			this.jsonData = type.toString();
			length = size;
			decompressable = filetype.endsWith("zip")||filetype.endsWith("x-gzip")||filetype.endsWith("java-archive");
			//判断用户是否有权限可执行编辑操作
			this.editable = filetype.startsWith("text");
			this.snapshotable = this.filesMgr.isPreviewable(filetype);
			this.grant = super.isSysadmin();
			if( !grant )
			{
				JSONObject privileges = this.getMonitorMgr().getServerPrivileges(super.getUserRole(), super.getUserAccount(), this.getMonitorMgr().getServer(this.getServerId()));
				grant = privileges.has("files")&&privileges.getBoolean("files");
			}
    		logoper("预览集群伺服器【"+ip+"】文件["+filepath+"]，文件类型是"+filetype+"，文件大小是"+size, "文件管理", null, "files!preview.action?ip="+ip+"&port="+port+"&path="+Kit.chr2Unicode(path));
    		return "showfile";
    	}
    	catch(Exception e)
    	{
			super.getSession().setAttribute("referer", "files!preview.action");
			super.getSession().setAttribute("exception", e);
			this.responseException = "打开集群伺服器【"+ip+"】文件["+filepath+"]预览异常"+e.getMessage();
			super.getSession().setAttribute("exceptionTips", responseException);
    		return "exception";
    	}
	}
	
	/**
	 * 显示文件
	 * @return
	 */
	public String show()
	{
		HttpServletResponse response = super.getResponse();
		String filepath = rootdir + path;
		log.info("Show the file(len="+length+",type="+filetype+") of "+path+"(rootdir:"+rootdir+") from "+ip+":"+port);
		try
		{
			messageCode = path.endsWith(".xml")?"xml":messageCode;
			messageCode = path.endsWith(".json")?"json":messageCode;
			messageCode = path.endsWith(".js")?"js":messageCode;
			messageCode = path.endsWith(".css")?"css":messageCode;
			if( !messageCode.isEmpty() )
			{
				byte[] data = this.filesMgr.fetchfile(ip, port, filepath);
				int len = data.length>10240?10240:data.length;
				java.nio.charset.Charset charset = this.filesMgr.getDetector().detectCodepage(new ByteArrayInputStream(data, 0, len), len);
				if( charset != null ){
//					System.err.println(charset.displayName());
					if( "UTF-8".equalsIgnoreCase(charset.displayName()) ||
						"US-ASCII".equals(charset.displayName()) )
					{
						message = new String(data, charset);
						encoding = charset.displayName();
					}
					else{
						message = new String(data, "GBK");
						encoding = "GBK";
					}
				}
				else{
					message = new String( data );
					encoding = "";
				}
//	            String charset = "ISO-8859-1";
//	            if( !message.equals(new String(message.getBytes(charset), charset)) )
//	            {
//	            	charset = "UTF-8";
//	            	message = new String(data, charset);
//	            	if( !message.equals(new String(message.getBytes(charset), charset)))
//		            {
//	            		charset = "GBK";
//		            	message = new String(data, charset);
//		            }
//	            }
	            if( "js".equals(messageCode) ){
	            	
	            	return "javascript";
	            }
				return "xml_json_css_sql";
			}

			if( filetype.startsWith("text") )
			{
				if( length == 0 || length > Kit.Ms )
				{
					length = length==0?Kit.Ms:length;
					this.path = filepath;
					pageBean = new PageBean();
					long pageCount = this.length/Kit.Ms;//缺省1M分页一次
					pageCount += this.length%Kit.Ms!=0?1:0;
					this.pageBean.setPageCount((int)pageCount);
					this.pageBean.setPage(pageBean.getPageCount());
					this.pageBean.setPageSize((int)Kit.Ms);
					return "pageshow";
				}
				else
				{
					byte[] data = this.filesMgr.fetchfile(ip, port, filepath);
					int len = data.length>10240?10240:data.length;
					java.nio.charset.Charset charset = this.filesMgr.getDetector().detectCodepage(new ByteArrayInputStream(data, 0, len), len);
					if( charset != null ){
//						System.err.println(charset.displayName());
						if( "UTF-8".equalsIgnoreCase(charset.displayName()) ||
							"US-ASCII".equals(charset.displayName()) )
						{
							message = new String(data, charset);
							encoding = charset.displayName();
						}
						else{
							message = new String(data, "GBK");
							encoding = "GBK";
						}
					}
					else{
						message = new String( data );
						encoding = "";
					}
					return "text";
					//return filesMgr.showfile(response, ip, port, filepath, length, "text");
				}
			}
			if( filesMgr.isPreviewable(filetype) )
			{
				String filename = path.substring(path.lastIndexOf('/')+1);
				response.setContentType(filetype);
				response.setContentLength((int)length);
				response.setHeader("Content-disposition", "inline; filename*=UTF-8''"+URLEncoder.encode(filename,"UTF-8"));
				response.setHeader("Access-Control-Allow-Origin", "*");
				response.setHeader("Access-Control-Allow-Methods", "*");
				return filesMgr.showfile(response, ip, port, filepath, length, "preview");
			}
		}
    	catch(Exception e)
    	{
    		e.printStackTrace();
//			super.getSession().setAttribute("referer", "files!show.action");
//			super.getSession().setAttribute("exception", e);
//			this.responseException = "打开集群伺服器【"+ip+"】文件["+filepath+"]显示异常"+e.getMessage();
//			super.getSession().setAttribute("exceptionTips", responseException);
//    		return "exception";
    	}
		return null;
	}

	public PageBean getPageBean() {
		return pageBean;
	}
	public void setPageBean(PageBean pageBean) {
		this.pageBean = pageBean;
	}
	/**
	 * 显示文件头部二进制
	 * @return
	 */
	public String digit()
	{
		String filepath = rootdir + path;
		try
		{
			return filesMgr.showfile(super.getResponse(), ip, port, filepath, length, "digit");
		}
    	catch(Exception e)
    	{
    		log.error("", e);
			super.getSession().setAttribute("referer", "files!digit.action");
			super.getSession().setAttribute("exception", e);
			this.responseException = "打开集群伺服器【"+ip+"】文件["+filepath+"]二进制预览异常"+e.getMessage();
			super.getSession().setAttribute("exceptionTips", responseException);
    		return "exception";
    	}
	}
	/**
	 * 编辑文本文件
	 * @return
	 */
	public String textedit()
	{
		String filepath = rootdir + path;
		try
		{
//			String encoding = super.getRequest().getParameter("encoding");//得到文本文件的编码格式
			log.info("Edit the file("+filetype+", length="+length+") of "+filepath+" to "+ip+":"+port);
//			this.localData = filesMgr.showfile(null, ip, port, filepath, length, "gettext");
			byte[] data = this.filesMgr.fetchfile(ip, port, filepath);
			int len = data.length>10240?10240:data.length;
			java.nio.charset.Charset charset = this.filesMgr.getDetector().detectCodepage(new ByteArrayInputStream(data, 0, len), len);
			if( charset != null ){
//				System.err.println(charset.displayName());
				if( "UTF-8".equalsIgnoreCase(charset.displayName()) ||
					"US-ASCII".equals(charset.displayName()) )
				{
					localData = new String(data, charset);
					encoding = charset.displayName();
				}
				else{
					localData = new String(data, "GBK");
					encoding = "GBK";
				}
			}
			else{
				localData = new String( data );
				encoding = "";
			}
//			localData = new String( data);
//            String charset = "ISO-8859-1";
//            if( !localData.equals(new String(localData.getBytes(charset), charset)) )
//            {
//            	charset = "GBK";
//            	localData = new String(data, charset);
//            	String tmp = new String(localData.getBytes(charset), charset);
//            	if( !localData.equals(tmp))
//	            {
//            		charset = "UTF-8";
//            		localData = new String(data, charset);
//	            }
//            }
			logoper("打开集群伺服器【"+ip+"】文件["+filepath+"]的文本编辑，文件类型是"+filetype, "文件管理", null, "files!textedit.action?ip="+ip+"&port="+port+"&filetype="+filetype+"&path="+Kit.chr2Unicode(path));
			return "textedit";
		}
    	catch(Exception e)
    	{
    		log.error("", e);
			super.getSession().setAttribute("referer", "files!textedit.action");
			super.getSession().setAttribute("exception", e);
			this.responseException = "打开集群伺服器【"+ip+"】文件["+filepath+"]编辑异常"+e.getMessage();
			super.getSession().setAttribute("exceptionTips", responseException);
    		return "exception";
    	}
	}
	
	/**
	 * 文本文件保存
	 * @return
	 */
	public String textsave()
	{
        String encoding = super.getRequest().getParameter("encoding");
		log.info("Save the file("+filetype+", encoding="+encoding+") of "+path+" to "+ip+":"+port);
		String filepath = rootdir + path;
		byte[] buf = filepath.getBytes();
		int offset = 0;
    	byte[] payload = new byte[256];
    	payload[offset++] = Command.CONTROL_COPYFILE;
    	payload[offset++] = (byte)buf.length;//文件路径
    	offset = Tools.copyByteArray(buf, payload, offset);
    	payload[offset++] = 0;
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
        try
        {
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            InetAddress addr = InetAddress.getByName( ip );
            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port );
            datagramSocket.send( request );
			DatagramPacket response = new DatagramPacket(payload, payload.length);
			datagramSocket.receive(response);
			datagramSocket.close();
			int port1 = Tools.bytesToInt(payload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port1);
			socket = new Socket();
			socket.connect(endpoint, 15000);
            OutputStream out = socket.getOutputStream();
            String textedit = super.getRequest().getParameter("editorContent");
            if( encoding != null && !encoding.isEmpty() )
            	buf = textedit.getBytes(encoding);
            else buf = textedit.getBytes();
            length = buf.length;
        	out.write(buf);
            out.flush();
            Thread.sleep(1000);
            out.close();
            log.info("Succeed to save the file("+length+") of "+filepath+" to "+ip+":"+port1);
            this.responseMessage = "保存文本文件["+filepath+"]到集群伺服器【"+ip+"】成功。";
    		logoper(responseMessage, "文件管理", "", "files!open.action?ip="+ip+"&port="+port);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
					null,
					"files!open.action?ip="+ip+"&port="+port,
                    "情况确认", "#feedback?to="+super.getUserAccount());
    		return this.show();
        }
        catch(Exception e)
        {
            this.responseException = "保存文本文件["+filepath+"]到集群伺服器【"+ip+"】出现异常:"+e.getMessage();
    		logoper(responseException, "文件管理", "", "files!open.action?ip="+ip+"&port="+port, e);
			super.getSession().setAttribute("referer", "files!textsave.action");
			super.getSession().setAttribute("exception", e);
			super.getSession().setAttribute("exceptionTips", responseException);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseException, super.getUserAccount()),
					null,
					"files!open.action?ip="+ip+"&port="+port,
                    "情况确认", "#feedback?to="+super.getUserAccount());
    		return "exception";
        }
        finally
        {
    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
            	if( socket != null ) socket.close();
			}
			catch (IOException e)
			{
			}
        }
	}
	/**
	 * 预览日志
	 * @return
	 */
	public String previewlog()
	{
		pageBean = new PageBean();
		long pageCount = this.length/Kit.Ms;//缺省1M分页一次
		pageCount += this.length%Kit.Ms!=0?1:0;
		this.pageBean.setPageCount((int)pageCount);
		this.pageBean.setPage(pageBean.getPageCount());
		this.pageBean.setPageSize((int)Kit.Ms);
		return "pageshow";
	}
	/**
	 * 分页显示文本文件
	 * @return
	 */
	public String pageshow()
	{
		log.debug("Show the page("+pageBean.getPage()+", size="+pageBean.getPageSize()+") of file "+path+"(rootdir:"+rootdir+")");
		String filepath = rootdir + path;
    	byte[] payload = new byte[64*1024];
    	int offset = 0;
    	payload[offset++] = Command.CONTROL_GETFILE;//得到文件
    	payload[offset++] = 0;//不压缩获取文件
    	byte buf[] = filepath.getBytes();
    	Tools.intToBytes(buf.length, payload, offset, 2);
    	offset += 2;
    	offset = Tools.copyByteArray(buf, payload, offset);
    	Tools.intToBytes(0xFF, payload, offset, 1);
    	offset += 1;
    	Tools.intToBytes(pageBean.getPage(), payload, offset, 4);
    	offset += 4;
    	Tools.longToBytes(pageBean.getPageSize(), payload, offset, 8);
    	offset += 8;
    	ServletOutputStream out = null;
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
		int len = 0;
        try
        {//Failed to get file 
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            InetAddress addr = InetAddress.getByName( ip );
            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port );
            datagramSocket.send( request );
			DatagramPacket response = new DatagramPacket(payload, payload.length);
			datagramSocket.receive(response);
			port = Tools.bytesToInt(payload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port);
			datagramSocket.close();
			socket = new Socket();
			socket.connect(endpoint, 15000);
            InputStream is = socket.getInputStream();
			getResponse().setContentType("text/html");
			String encoding = super.getRequest().getParameter("previewCharset");
			if( encoding == null || encoding.isEmpty() )
				encoding = System.getProperty("service.config.encode", "UTF-8");
			getResponse().setCharacterEncoding(encoding);
			
    		out = getResponse().getOutputStream();
    		out.println("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");

    		int ch = is.read();
			offset = 0;
			while( (len = is.read(payload, 0, payload.length)) != -1  )
    		{
    			length -= len;
    			offset = 0;
    			for(int i = 0; i < len; i++ )
    			{
    				ch = payload[i];
        			if( ch == '<' )
        			{
        				int size = i - offset;
        				out.write(payload, offset, size);
        				offset += size + 1;
        				out.print("&lt;");
        			}
        			else if( ch == '>' ) 
        			{
        				int size = i - offset;
        				out.write(payload, offset, size);
        				offset += size + 1;
        				out.print("&gt;");
        			}
    			}
        		out.write(payload, offset, len - offset);
				out.flush();
    		}
    		out.println("</body>\r\n\r\n<script type='text/javascript' LANGUAGE='JavaScript'>if(parent&&parent.setScrollBottom){parent.setScrollBottom();}if(parent&&parent.skit_hiddenLoading){parent.skit_hiddenLoading();}</script></html>");
    		is.close();
        }
        catch(Exception e)
        {
        	log.error("len="+len+",offset="+offset, e);
        	getSession().setAttribute("referer", "files!pageshow.action");
        	getSession().setAttribute("exceptionTips", "打开文件 "+filepath+" 出现 异常"+e+", "+(port==0?"未能收到服务端响应":"服务端已响应但出现错误"));
			super.getSession().setAttribute("exception", e.getCause());
			try
			{
				super.getResponse().sendRedirect(Kit.URL_PATH(super.getRequest())+ "500");
			} 
			catch (IOException e1) {
			}
        }
        finally
        {
    		try
			{
            	if( out != null ) out.close();
			}
			catch (IOException e)
			{
			}
    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
			}
			catch (Exception e)
			{
			}
    		try
			{
            	if( socket != null ) socket.close();
			}
			catch (IOException e)
			{
			}
        }
		return null;
	}

	public String getRootdir() {
		return rootdir;
	}
	
	public void setRootdir(String rootdir) {
		this.rootdir = rootdir;
	}
	
	public void setFilesMgr(FilesMgr filesMgr) {
		this.filesMgr = filesMgr;
	}
	public long getLength() {
		return length;
	}
	
	public void setLength(long length) {
		this.length = length;
	}

	public boolean isDecompressable() {
		return decompressable;
	}

	public String getFilenames() {
		return filenames;
	}
	public void setFilenames(String filenames) {
		this.filenames = filenames;
	}

	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}
