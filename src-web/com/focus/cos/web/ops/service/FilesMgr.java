package com.focus.cos.web.ops.service;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.ParsingDetector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.control.Command;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.IOHelper;
import com.focus.util.QuickSort;
import com.focus.util.Tools;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

public class FilesMgr extends SvrMgr
{
	private static final Log log = LogFactory.getLog(FilesMgr.class);
	private HashMap<String, Boolean> previewable = new HashMap<String, Boolean>();
	private CodepageDetectorProxy detector;

	public CodepageDetectorProxy getDetector() {
		return detector;
	}

	public FilesMgr()
	{
		previewable.put("text/plain", true);
		previewable.put("text/html", true);
		previewable.put("text/sgml", true);
		previewable.put("application/postscript", true);
		previewable.put("application/x-shockwave-flash", true);
		previewable.put("image/png", true);
		previewable.put("image/gif", true);
		previewable.put("image/jpg", true);
		previewable.put("image/bmp", true);
		previewable.put("application/x-miff", true);
		previewable.put("image/g3fax", true);
		previewable.put("audio/mp3", true);
		previewable.put("application/x-wav", true);
		previewable.put("video/mpeg", true);
		previewable.put("video/quicktime", true);
		previewable.put("video/mpeg", true);
		previewable.put("video/mp4", true);
		previewable.put("audio/x-mpeg", true);
		previewable.put("audio/mp4", true);
		previewable.put("application/xml", true);
		previewable.put("application/pdf", true);
		previewable.put("image/x-icon", true);
		
		detector = CodepageDetectorProxy.getInstance();
        /*
         * ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于
         * 指示是否显示探测过程的详细信息，为false不显示。
         */
        detector.add(new ParsingDetector(false));
        /*
         * JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码
         * 测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以
         * 再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。
         */
        detector.add(JChardetFacade.getInstance());// 用到antlr.jar、chardet.jar
//        // ASCIIDetector用于ASCII编码测定
//        detector.add(ASCIIDetector.getInstance());
        // UnicodeDetector用于Unicode家族编码的测定
//        detector.add(UnicodeDetector.getInstance());
		
	}
	
	/**
	 * 指定文件类型是否可以预览
	 * @param type
	 * @return
	 */
	public boolean isPreviewable(String type)
	{
		previewable.put("text/sgml", true);
		return this.previewable.containsKey(type) && this.previewable.get(type);
	}
	/**
	 * 
	 * @param path
	 * @return
	 */
	public JSONObject getFileTypeByExtension(String path)
	{
		int i = path.lastIndexOf('.');
		String extension = null;
		if( i != -1 )
		{
			extension = path.substring(i+1);
			if( extension.indexOf('/') != -1 ) extension = null;
		}
		if( extension == null ) return null;
		JSONObject contentType = new JSONObject();
		extension = extension.toLowerCase();
		contentType.put("extension", extension);
		String[] type = new String[3];
		if( extension.equals("mht") || extension.equals("htm") || extension.equals("html"))
		{
			type[0] = "HTML文件";type[1] = "file-code-o";type[2] = "text/html";
		}
		else if( extension.equals("xml") || extension.equals("jsp") || extension.equals("js") || extension.equals("css") || extension.equals("less") )
		{
			type[0] = extension.toUpperCase()+"文件";type[1] = "file-code-o";type[2] = "text/plain";
		}
		else if( extension.equals("png") || extension.equals("jpg") || extension.equals("gif") ||
				 extension.equals("bmp")  )
		{
			type[0] = "图片文件";type[1] = "file-image-o";type[2] = "image/"+extension;
		}
		else if( extension.equals("ico")  )
		{
			type[0] = "图片文件";type[1] = "file-image-o";type[2] = "image/x-icon";
		}
		else if( extension.equals("miff") )
		{
			type[0] = "图片文件";type[1] = "file-image-o";type[2] = "application/x-miff";
		}
		else if( extension.equals("fax") )
		{
			type[0] = "图片文件";type[1] = "file-image-o";type[2] = "image/g3fax";
		}
		else if( extension.equals("rtx") )
		{
			type[0] = "富文本文件";type[1] = "file-text-o";type[2] = "text/richtext";
		}
		else if( extension.equals("bat") )
		{
			type[0] = "批处理文件";type[1] = "file-text-o";type[2] = "text/plain";
		}
		else if( extension.equals("sh") )
		{
			type[0] = "SHELL脚本";type[1] = "file-archive-o";type[2] = "application/x-sh";
		}
		else if( extension.equals("odt") )
		{
			type[0] = "OpenDocument";type[1] = "file-word-o";type[2] = "application/vnd.oasis.opendocument.text";
		}
		else if( extension.equals("exe") )
		{
			type[0] = "应用程序";type[1] = "file-archive-o";type[2] = "application/octet-stream";
		}
		else if( extension.equals("dll") )
		{
			type[0] = "动态连接库";type[1] = "file-archive-o";type[2] = "application/octet-stream";
		}
		else if( extension.equals("java") )
		{
			type[0] = "JAVA代码";type[1] = "file-code-o";type[2] = "text/x-java-source";
		}
		else if( extension.equals("jar") )
		{
			type[0] = "JAVA库";type[1] = "file-archive-o";type[2] = "application/java-archive";
		}
		else if( extension.equals("ttf") || extension.equals("ttc") )
		{
			type[0] = "字体文件";type[1] = "file-code-o";type[2] = "application/x-font-ttf";
		}
		else if( extension.equals("otf") )
		{
			type[0] = "字体文件";type[1] = "file-code-o";type[2] = "application/x-font-otf";
		}
		else if( extension.equals("txt") )
		{
			type[0] = "文本文件";type[1] = "file-text-o";type[2] = "text/plain";
		}
		else if( extension.equals("properties") )
		{
			type[0] = "属性配置";type[1] = "file-text-o";type[2] = "text/plain";
		}
		else if( extension.equals("ini") )
		{
			type[0] = "INI配置";type[1] = "file-text-o";type[2] = "text/plain";
		}
		else if( extension.equals("mp3") )
		{
			type[0] = "MP3文件";type[1] = "file-audio-o";type[2] = "audio/mp3";
		}
		else if( extension.equals("wav") )
		{
			type[0] = "WAV文件";type[1] = "file-audio-o";type[2] = "application/x-wav";
		}
		else if( extension.equals("wma") )
		{
			type[0] = "微软音频";type[1] = "file-audio-o";type[2] = "audio/x-ms-wma";
		}
		else if( extension.equals("mpg") || extension.equals("mpeg") )
		{
			type[0] = "视频文件";type[1] = "file-video-o";type[2] = "video/mpeg";
		}
		else if( extension.equals("m4v") || extension.equals("mp4") || extension.equals("mp4v") || extension.equals("mpg4") )
		{
			type[0] = "视频文件";type[1] = "file-video-o";type[2] = "video/mp4";
		}
		else if( extension.equals("mpega") || extension.equals("abs") )
		{
			type[0] = "音频文件";type[1] = "file-video-o";type[2] = "audio/x-mpeg";
		}
		else if( extension.equals("m4b") || extension.equals("m4r") )
		{
			type[0] = "音频文件";type[1] = "file-audio-o";type[2] = "audio/mp4";
		}
		else if( extension.equals("mov") )
		{
			type[0] = "视频文件";type[1] = "file-video-o";type[2] = "video/quicktime";
		}
		else if( extension.equals("dtd") )
		{
			type[0] = "XML类型定义";type[1] = "file-code-o";type[2] = "text/plain";
		}
		else if( extension.equals("tar") )
		{
			type[0] = "压缩文件(TAR)";type[1] = "file-zip-o";type[2] = "application/x-tar";
		}
		else if( extension.equals("zip") )
		{
			type[0] = "压缩文件(ZIP)";type[1] = "file-zip-o";type[2] = "application/zip";
		}
		else if( extension.equals("gz") )
		{
			type[0] = "压缩文件(GZIP)";type[1] = "file-zip-o";type[2] = "application/x-gzip";
		}
		else if( extension.equals("bz2") )
		{
			type[0] = "压缩文件(BZ2)";type[1] = "file-zip-o";type[2] = "application/x-bzip2";
		}
		else if( extension.equals("swf") )
		{
			type[0] = "FLASH文件";type[1] = "file-image-o";type[2] = "application/x-shockwave-flash";
		}
		else if( extension.equals("ps") )
		{
			type[0] = "PostScript";type[1] = "file-image-o";type[2] = "application/postscript";
		}
		else if( extension.equals("psd") )
		{
			type[0] = "Photoshop文件";type[1] = "file-image-o";type[2] = "image/vnd.adobe.photoshop";
		}
		else if( extension.equals("pdf") )
		{
			type[0] = "PDF文件";type[1] = "file-pdf-o";type[2] = "application/pdf";
		}
		else if( extension.equals("xls") || extension.equals("xlsx") )
		{
			type[0] = "Excel文件";type[1] = "file-excel-o";type[2] = "application/vnd.ms-excel";
		}
		else if( extension.equals("doc") || extension.equals("docx") )
		{
			type[0] = "Word文件";type[1] = "file-word-o";type[2] = "application/msword";
		}
		else if( extension.equals("ppt") || extension.equals("pptx") || extension.equals("pptm") )
		{
			type[0] = "PPT文件";type[1] = "file-powerpoint-o";type[2] = "application/vnd.ms-powerpoint";
		}
		else
		{
			//type[0] = extension.toUpperCase()+"文件";type[1] = "file-archive-o";type[2] = "application/octet-stream";
			return null;
		}
		contentType.put("description", type[0]);
		contentType.put("icon", type[1]);
		contentType.put("value", type[2]);
		return contentType;
	}
	
	/**
	 * 根据文件后缀返回文件类型
	 * @param path
	 * @return
	 */
	public String getFileType(String path){
		JSONObject type = this.getFileTypeByExtension(path);
		if( type == null ){
			type = new JSONObject();
			type.put("description", "");
			type.put("icon", "file-o");
			type.put("value", "application/octet-stream");
		}
//		System.err.println(type.toString(4));
		return type.toString();
	}
	/**
	 * 从伺服器获取目录数据
	 * @param ip
	 * @param port
	 * @param path 只允许绝对路径输入
	 * @param relative true表示相对路径模式
	 * @param rootdir 如果是相对路径根目录, rootdir有效
	 * @return 只会返回绝对路径
	 * @throws Exception
	 */
	public JSONObject fetchFiles(String ip, int port, String path, boolean relative, String rootdir )
		throws Exception
	{
		log.info("Open the files of "+path+" from "+ip+":"+port+".");
		if( path == null ) path = "";
		path = path.trim();
		if( !relative && path.isEmpty() )
			throw new Exception("绝对路径模式下需要提供绝对路径");
		JSONObject dir = new JSONObject();//目录数据	
		dir.put("path", path);
		if(!path.isEmpty())
		{
			if( relative )
			{
				if( rootdir == null ) throw new Exception("相对路径模式下需要提供根目录路径");
				path = rootdir + path;
			}
			if( path.charAt(0) != '/' && path.charAt(1) != ':') 
			{
				throw new Exception("路径不正确"+path);
			}
		}
		int offset = 0;
		byte[] pathbuf = path.getBytes("UTF-8");
    	byte[] payload = new byte[64*1024];
    	payload[0] = Command.CONTROL_GETFILELIST;//得到指定模块文件
    	payload[1] = (byte)pathbuf.length;//文件路径
    	offset = Tools.copyByteArray(pathbuf, payload, 2);
    	DatagramSocket datagramSocket = null;
		try
		{
			ArrayList<JSONObject> data = new ArrayList<JSONObject>();
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(7000);
            InetAddress addr = InetAddress.getByName( ip );
            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port );
            datagramSocket.send( request );
            DatagramPacket response = new DatagramPacket(payload, 0, payload.length, addr, port );
            datagramSocket.receive( response );
            offset = 0;
    		if( payload[offset] == -2 )
    		{
    			throw new Exception("你没有查看集群伺服器【"+ip+"】文件夹"+path+"的权限");
    		}
            boolean newversion = payload[offset++]<0;
            int filecount = Tools.bytesToInt(payload, offset, 4);
            int itemcount = filecount;
            long length = 0;
            offset += 4;
            HashMap<String, JSONObject> map = new HashMap<String, JSONObject>();
            while( filecount > 0)
            {
            	filecount -= 1;
            	JSONObject file = new JSONObject();
            	file.put("isParent", payload[offset++]==1);
	            int len = Tools.bytesToInt(payload, offset, 2);
	            offset += 2;
	            for( int i = 0; i < len; i++ )
	            {
	            	if( payload[i+offset] == '\\' )
	            	{
	            		payload[i+offset] = '/';
	            	}
	            }
	            String filepath = new String(payload, offset, len);
	            String charset = "ISO-8859-1";
	            if( !filepath.equals(new String(filepath.getBytes(charset), charset)) )
	            {
	            	charset = "UTF-8";
            		filepath = new String(payload, offset, len, charset);
	            	if( !filepath.equals(new String(filepath.getBytes(charset), charset)))
		            {
	            		charset = "GBK";
	            		filepath = new String(payload, offset, len, charset);
		            }
	            }
	            file.put("charset", charset);
	            if( relative )
	            {
	            	if( rootdir == null || rootdir.isEmpty() )
	            	{
		            	rootdir = filepath.substring(0, filepath.lastIndexOf('/')+1);
		            	dir.put("rootdir", rootdir);
	            	}
	            	int i = rootdir.length();
	            	if( filepath.length() < i ){
	            		datagramSocket.close();
	            		throw new Exception("您要打开的目录不存在"+path);
	            	}
	            	else{
	            		file.put("path", filepath.substring(rootdir.length()));
	            	}
	            	file.put("rootdir", rootdir);
	            }
	            else file.put("path", filepath);
	            int beginIndex = filepath.lastIndexOf("/");
	            String filename = filepath.substring(beginIndex+1);
	            file.put("name", filename);
	            map.put(filename,file);
	            offset += len;
	            
	            if( !file.getBoolean("isParent") )
	            {
		            long t = Tools.bytesToLong(payload, offset, 8);
		            file.put("timestamp", t);
		            file.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", t));
		            offset += 8;
	            	file.put("length", Tools.bytesToLong(payload, offset, 8));
	            	offset += 8;
	            	length += file.getLong("length");
	            	file.put("size", Kit.bytesScale(file.getLong("length")));
	            }
	            else if ( newversion )
	            {
	            	file.put("count", Tools.bytesToInt(payload, offset, 4));
	            	offset += 4;
	            	file.put("length", Tools.bytesToLong(payload, offset, 8));
	            	offset += 8;
	            	length += file.getLong("length");
	            	file.put("size", Kit.bytesScale(file.getLong("length")));
	            }
	            data.add(file);
            }
            if( newversion )
            {
    	    	JSONObject summary = new JSONObject();
            	summary.put("filecount", Tools.bytesToInt(payload, offset, 4));
            	offset += 4;
            	summary.put("length", length);
            	offset += 8;
            	summary.put("size", Kit.bytesScale(summary.getLong("length")));
            	summary.put("itemcount", itemcount);
        		dir.put("summary", summary);

                int flag = payload[offset++];
                if( flag == 100 ){
                	//读取ll信息
            		int size = Tools.bytesToInt(payload, offset, 4);
            		offset += 4;
            		byte[] buffer = new byte[size];
            		GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(payload, offset, response.getLength() - offset));
            		int len;
            		int off = 0;
            		size = buffer.length;
            		while( (len = gis.read(buffer, off, size)) != -1  )
            		{
            			size -= len;
            			off += len;
            			if( size == 0 ) break;
            		}
            		gis.close();
//            		String ll = new String(buffer, "UTF-8");
//            		System.err.println(ll);
            		List<String> lines = IOHelper.readLines(new ByteArrayInputStream(buffer), "UTF-8");
            		for(String line : lines){
            			String[] args = Tools.split(line, " ");
            			if( args.length < 5 ){
            				continue;
            			}
            			String a0 = null;
            			String a1 = null;
            			String a2 = null;
            			String a3 = null;
            			String a4 = null;
            			String a5 = null;
            			String a6 = null;
            			String a7 = null;
            			String a8 = null;
            			String a9 = null;
            			String a10 = null;
            			for(String arg : args){
            				if(arg.isEmpty()) continue;
            				if( a0 == null ){
            					a0 = arg;
            				}
            				else if( a1 == null ){
            					a1 = arg;
            				}
            				else if( a2 == null ){
            					a2 = arg;
            				}
            				else if( a3 == null ){
            					a3 = arg;
            				}
            				else if( a4 == null ){
            					a4 = arg;
            				}
            				else if( a5 == null ){
            					a5 = arg;
            				}
            				else if( a6 == null ){
            					a6 = arg;
            				}
            				else if( a7 == null ){
            					a7 = arg;
            				}
            				else if( a8 == null ){
            					a8 = arg;
            				}
            				else if( a9 == null ){
            					a9 = arg;
            				}
            				else if( a10 == null ){
            					a10 = arg;
            				}
            				else{
            					break;
            				}
            				//-rw-r--r--. 1 gt gt   4629 3月   7 11:32 wrapper.log
            				//0           1 2  3    4    5   6  7
            			}
            			
            			if(map.containsKey(a8)){
            				JSONObject e = map.get(a8);
            				e.put("privileges", a0);
            				e.put("user", a2);
            				e.put("group", a3);
            				if(!e.has("time")){
            					e.put("time", a5+a6+" "+a7);
            				}
            				if( a9 != null && a10 != null ){
            					e.put("ln", a10);
            				}
//                    		System.err.println(e.toString(4));
            			}
            		}
                }
            }
            QuickSort sorter = new QuickSort(){
				public boolean compareTo(Object sortSrc, Object pivot)
				{
					JSONObject left = (JSONObject)sortSrc;
					JSONObject right = (JSONObject)pivot;
					long l = left.has("timestamp")?left.getLong("timestamp"):-1000000000;
					long r = right.has("timestamp")?right.getLong("timestamp"):-1000000000;
					return l > r;
				}
            };
            sorter.sort(data);
            JSONArray dirs = new JSONArray();
            JSONArray files = new JSONArray();
    		for( JSONObject file : data )
    		{
    			if( file.has("isParent") && file.getBoolean("isParent") ){
    				dirs.put(file);
    			}
    			files.put(new JSONObject(file.toString()));
    		}
    		dir.put("children", dirs);
    		dir.put("files", files);
    		return dir;
		}
		catch(Exception e)
		{
			log.error("Failed to set the path of "+path, e);
			throw e;
		}
		finally
		{
    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
			}
			catch (Exception e)
			{
			}
		}		
	}
	/**
	 * 得到指定路径下的目录
	 * @param ip
	 * @param port
	 * @param path
	 * @return
	public JSONArray getFilesData(String ip, int port, String path)
		throws Exception
	{
    	JSONArray dirs = new JSONArray();
    	ArrayList<JSONObject> files = getFiles(ip, port, path);
    	if( files != null )
    	{
    		for( JSONObject file : files )
    		{
    			dirs.put(file);
    		}
    	}
    	else
    	{
    		throw new Exception("从伺服器("+ip+":"+port+")查询获取文件目录【"+path+"】失败。");
    	}
		return dirs;
	}
	 */
	/**
	 * 得到指定路径下的目录
	 * @param ip
	 * @param port
	 * @param path
	 * @return
	public JSONArray getDirsData(String ip, int port, String path)
		throws Exception
	{
    	JSONArray dirs = new JSONArray();
    	ArrayList<JSONObject> files = getFiles(ip, port, path);
    	if( files != null )
    	{
    		for( JSONObject file : files )
    		{
    			if( !file.has("isParent") || !file.getBoolean("isParent") ) continue;
    			dirs.put(file);
    		}
    	}
    	else
    	{
    		throw new Exception("从伺服器("+ip+":"+port+")查询获取文件目录【"+path+"】失败。");
    	}
		return dirs;
	}
	 */
	/**
	 * 得到指定目录下的数据
	 * @param ip
	 * @param port
	 * @param path
	 * @return
	 */
	public AjaxResult<String> getAllDirs(String ip, int port, String path, String rootdir)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		String dirpath = "";
		try 
		{
			log.info("Get all dirs from "+path+"(rootdir:"+rootdir+")");
			ArrayList<String> dirs = getPaths(path);
			if( dirs.isEmpty() )
			{
				rsp.setMessage("无效的路径"+path);
				return rsp;
			}
			HashMap<String, String> parentFiles = null;
			for(String str : dirs )
			{
				dirpath = str;
				if( parentFiles == null )
				{
					parentFiles = new HashMap<String, String>();
				}
				else if( !parentFiles.containsKey(dirpath) )
				{
					rsp.setMessage("不存在对应目录"+dirpath);
					break;
				}
				else parentFiles.clear();
				JSONObject dir = this.fetchFiles(ip, port, dirpath, rootdir!=null, rootdir);
				rsp.add(dir.toString());
				rsp.setResult(dirpath);
				if( dir.has("children") )
				{
					JSONArray children = dir.getJSONArray("children");
					for(int i = 0; i < children.length(); i++)
					{
						JSONObject child = children.getJSONObject(i);
						parentFiles.put(child.getString("path"), child.getString("name"));
					}
				}
			}
			rsp.setSucceed(true);
		}
		catch (Exception e)
		{
			log.error("Failed to open all dirs from "+dirpath+", rootdir "+rootdir, e);
			rsp.setMessage("在集群伺服器【"+ip+"】跳转路径出现异常:"+e.getMessage()+", 可能是因为您输入的路径["+path+"]错误。");
		}
		return rsp;
	}
	/**
	 * 打开指定目录
	 * @param host
	 * @param port
	 * @param path "" "全路径"
	 * @param path
	 * @return
	 */
	public AjaxResult<String> getDirs(String ip, int port, String path, String rootdir)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try 
		{
			log.info("Get dirs "+path+", rootdir is "+rootdir);
			JSONObject dirs = this.fetchFiles(ip, port, path, rootdir!=null, rootdir);
			rsp.setSucceed(true);
			rsp.setResult(dirs.toString());
//			System.err.println(dirs.toString(4));
		}
		catch (Exception e)
		{
			rsp.setMessage("打开集群伺服器【"+ip+"】目录["+path+"]出现异常【"+e+"】，请重新尝试或检查服务器是否工作正常。");
		}
		return rsp;
	}
	
	/**
	 * 
	 * @param path
	 * @param rootdir
	 * @return
	 * @throws Exception 
	private String getRealPath(String path, String rootdir) throws Exception{
		if( path == null ) path = "";
		path = path.trim();
		boolean relative = true;
		if( path.startsWith("/") || path.indexOf(":") == 1 ){
			relative = false;
		}
		if( !relative && path.isEmpty() )
			throw new Exception("绝对路径模式下需要提供绝对路径");
		JSONObject dir = new JSONObject();//目录数据	
		dir.put("path", path);
		if(!path.isEmpty())
		{
			if( relative )
			{
				if( rootdir == null ) throw new Exception("相对路径模式下需要提供根目录路径");
				path = rootdir + path;
			}
			if( path.charAt(0) != '/' && path.charAt(1) != ':') 
			{
				throw new Exception("路径不正确"+path);
			}
		}
		return path;
	}
	 */
	
	/**
	 * 创建目录
	 * @param ip
	 * @param port
	 * @param path
	 * @param rootdir
	 * @return
	 */
	public AjaxResult<String> mkDir(String ip, int port, String path){
		AjaxResult<String> rsp = new AjaxResult<String>();
		try 
		{
			log.info("Make dir "+path);
			this.makeFile(ip, port, path, true);
			rsp.setSucceed(true);
			rsp.setMessage("创建了目录"+path);
			logoper(rsp.getMessage(), "文件管理", "", "");
			sendNotiefiesToSystemadmin(
					"集群文件管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
                    "",
                    null,
                    "查看文件管理器", "files!navigate.action");
		}
		catch (Exception e)
		{
			rsp.setMessage(e.getMessage());
		}
		return rsp;
	}

	/**
	 * 创建文件
	 * @param ip
	 * @param port
	 * @param path
	 * @param rootdir
	 * @return
	 */
	public AjaxResult<String> mkFile(String ip, int port, String path){
		AjaxResult<String> rsp = new AjaxResult<String>();
		try 
		{
			log.info("Make file "+path);
			this.makeFile(ip, port, path, false);
			rsp.setSucceed(true);
			rsp.setMessage("创建了文件"+path);
			logoper(rsp.getMessage(), "文件管理", "", "");
			sendNotiefiesToSystemadmin(
					"集群文件管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
                    "",
                    null,
                    "查看文件管理器", "files!navigate.action");
		}
		catch (Exception e)
		{
			rsp.setMessage(e.getMessage());
		}
		return rsp;
	}

	/**
	 * 删除集群伺服器下的文件或文件夹
	 * @param ip
	 * @param port
	 * @param paths
	 * @return
	 */
	public AjaxResult<String> deleteFiles(String ip, int port, String[] paths)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try 
		{
			this.deletePaths(ip, port, paths);
			rsp.setSucceed(true);
			StringBuffer sb = new StringBuffer();
			for(String path : paths)
			{
				sb.append("\r\n\t");sb.append(path);
			}
			rsp.setMessage("删除集群伺服器【"+ip+"】"+paths.length+"个文件或文件夹成功");
			logoper(rsp.getMessage(), "文件管理", sb.toString(), "");
			sendNotiefieToAccount(
					super.getAccountName(),
					"集群文件管理",
					"您"+rsp.getMessage(),
                    "删除的文件或文件夹如下所示："+sb,
                    null,
                    "查看文件管理器", "files!navigate.action");
			sendNotiefiesToSystemadmin(
					"集群文件管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
                    "删除的文件或文件夹如下所示："+sb,
                    null,
                    "查看文件管理器", "files!navigate.action");
		}
		catch (Exception e)
		{
			rsp.setMessage("删除集群伺服器【"+ip+"】"+paths.length+"文件或文件夹是出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "文件管理", e);
		}
		return rsp;
	}
	/**
	 * 删除指定目录
	 * @param host
	 * @param port
	 * @param path
	 * @return
	 */
	public AjaxResult<String> deleteDir(String ip, int port, String path)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try 
		{
			this.deletePaths(ip, port, new String[]{path});
			rsp.setSucceed(true);
			rsp.setMessage("删除集群伺服器【"+ip+"】文件夹["+path+"成功");
			logoper(rsp.getMessage(), "文件管理", null);
			sendNotiefieToAccount(
					super.getAccountName(),
					"集群文件管理",
					"您"+rsp.getMessage(),
					"文件删除后不可再修改，如果出现问题请及时与集群系统管理员联系。",
					null,
                    "查看文件管理器", "files!navigate.action");
			sendNotiefiesToSystemadmin(
					"集群文件管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
					null,
					"files!open.action?ip="+ip+"&port="+port,
                    "情况确认", "#feedback?to="+super.getAccountName());
		}
		catch (Exception e)
		{
			rsp.setMessage("删除集群伺服器【"+ip+"】文件夹"+path+"出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "文件管理", e);
		}
		return rsp;
	}

	/**
	 * 创建文件或文件夹
	 * @param ip
	 * @param port
	 * @param paths
	 * @throws Exception
	 */
	private void makeFile(String ip, int port, String path, boolean isDir) throws Exception
	{
		byte[] payload = new byte[256];
		int offset;
		byte[] pathbuf = path.getBytes("UTF-8");
		offset = 0;
    	payload[offset++] = (byte)(isDir?Command.CONTROL_MAKEDIR:Command.CONTROL_MAKEFILE);
    	payload[offset++] = (byte)pathbuf.length;//文件路径
    	offset = Tools.copyByteArray(pathbuf, payload, offset);
    	DatagramSocket datagramSocket = null;
		try 
		{
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(100000);
            DatagramPacket request = new DatagramPacket(payload, 0, offset, InetAddress.getByName( ip ), port );
            datagramSocket.send( request );
            DatagramPacket reponse = new DatagramPacket(payload, 0, payload.length, request.getAddress(), request.getPort() );
            datagramSocket.receive( reponse );
            if( payload[0] != 1 ){
            	int len = payload[1];
            	String tips = new String(payload, 2, len, "UTF-8");
            	throw new Exception(tips);
            }
		}
		catch (Exception e)
		{
			throw e;
		}
        finally
        {
    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
			}
			catch (Exception e)
			{
			}
        }
	}
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param paths
	 * @throws Exception
	 */
	private void deletePaths(String ip, int port, String[] paths) throws Exception
	{
		byte[] payload = new byte[256];
		int offset;
		for(String path : paths)
		{
			byte[] pathbuf = path.getBytes("UTF-8");
			offset = 0;
	    	payload[offset++] = Command.CONTROL_DELETEFILE;//删除指定路径
	    	payload[offset++] = (byte)pathbuf.length;//文件路径
	    	offset = Tools.copyByteArray(pathbuf, payload, offset);
	    	DatagramSocket datagramSocket = null;
			try 
			{
	        	datagramSocket = new DatagramSocket();
	            datagramSocket.setSoTimeout(100000);
	            DatagramPacket request = new DatagramPacket(payload, 0, offset, InetAddress.getByName( ip ), port );
	            datagramSocket.send( request );
	            DatagramPacket reponse = new DatagramPacket(payload, 0, payload.length, request.getAddress(), request.getPort() );
	            datagramSocket.receive( reponse );
			}
			catch (Exception e)
			{
				throw e;
			}
	        finally
	        {
	    		try
				{
	    			if( datagramSocket != null ) datagramSocket.close();
				}
				catch (Exception e)
				{
				}
	        }
		}
	}

	/**
	 * 
	 * @param ip
	 * @param port
	 * @param fiels
	 * @return
	 */
	public AjaxResult<String> getContentType(String ip, int port, String[] paths)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try 
		{
			JSONArray array = new JSONArray();
			// file-code-o file-archive-o file-movie-o file-powerpoint-o file-photo-o file-video-o  file-zip-o
			for(String path: paths)
			{
				JSONObject type = getFileTypeByExtension(path);
				if( type != null ){	array.put(type); continue; }
				type = new JSONObject();
				array.put(type);
				this.setContentType(fetchContentType(ip, port, path), type);
			}
			rsp.setResult(array.toString());
//			System.err.println(array.toString(4));
			rsp.setSucceed(true);
		}
		catch (Exception e)
		{
			rsp.setMessage("获取文件类型出现异常:"+e.getMessage());
		}
		return rsp;
	}
	
	/**
	 * 
	 * @param match
	 * @param type
	 */
	public void setContentType(MagicMatch match, JSONObject type)
	{
		type.put("icon", "file-o");
		type.put("description", "未知类型");
		if( match == null )
		{
			type.put("value", "application/octet-stream");
			return;
		}
		if( match.getMimeType().startsWith("text") )
			type.put("description", "文本文件");
		else type.put("description", match.getDescription());//.getMimeType();
		type.put("value", match.getMimeType());
		if( match.getMimeType().startsWith("application"))
		{
			if( match.getMimeType().endsWith("pdf") ) type.put("icon", "file-pdf-o");
			else if( match.getMimeType().endsWith("x-wav") ) type.put("icon", "file-audio-o");
			else if( match.getMimeType().endsWith("msword") ) type.put("icon", "file-word-o");
			else if( match.getMimeType().endsWith("msexcel") ) type.put("icon", "file-excel-o");
		}
		else if( match.getMimeType().startsWith("text")){

			if( match.getMimeType().endsWith("xml") ) type.put("icon", "file-code-o");
			else type.put("icon", "file-text-o");
		}
		else if( match.getMimeType().startsWith("image")) type.put("icon", "file-image-o");
		else if( match.getMimeType().startsWith("audio")) type.put("icon", "file-audio-o");
		else if( match.getMimeType().startsWith("image")) type.put("icon", "file-image-o");
	}
	
	/**
	 * 获取指定文件的文件类型
	 * @param ip
	 * @param port
	 * @param path
	 * @return
	 */
	private MagicMatch fetchContentType(String ip, int port, String path)
	{
		byte[] payload = new byte[1024];
    	int offset = 0;
    	
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
    	InputStream is = null;
		long ts = System.currentTimeMillis();
		MagicMatch match = null;
        try
        {
        	payload[offset++] = Command.CONTROL_GETFILE;//得到文件
        	payload[offset++] = 0;//不压缩获取文件
        	byte buf[] = path.getBytes("UTF-8");
        	Tools.intToBytes(buf.length, payload, offset, 2);
        	offset += 2;
        	offset = Tools.copyByteArray(buf, payload, offset);
        	Tools.intToBytes(0xFF, payload, offset, 1);
        	offset += 1;
        	Tools.intToBytes(1, payload, offset, 4);
        	offset += 4;
        	Tools.longToBytes(payload.length, payload, offset, 8);
        	offset += 8;
        	
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            InetAddress addr = InetAddress.getByName( ip );
            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port );
            datagramSocket.send( request );
			DatagramPacket response = new DatagramPacket(payload, payload.length);
			datagramSocket.setSoTimeout(15000);
			datagramSocket.receive(response);
			port = Tools.bytesToInt(payload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port);
			datagramSocket.close();
			socket = new Socket();
			socket.connect(endpoint, 15000);
            is = socket.getInputStream();
    		int len;
    		offset = 0;
    		if( is.read() != 0 ) throw new Exception("未知协议数据");
    		while( (len = is.read(payload, offset, payload.length-offset)) != -1  )
    		{
    			offset += len;
    			if( offset == payload.length ) break;
    		}
			String filename = path.substring(path.lastIndexOf('/')+1);
    		log.debug("Fetch the data(payload="+payload.length+",offset="+offset+",ts="+(System.currentTimeMillis()-ts)+") of "+ filename+" from ip "+ip+":"+port);
    		
    		if( filename.toLowerCase().endsWith(".txt") ){
    			match = new MagicMatch();
    			match.setMimeType("text/plain");
    			match.setExtension("txt");
    		}
    		if( match == null )
	    		try
		    	{
	    			match = Magic.getMagicMatch(payload, false);
	    			log.debug("Found the file("+match.getMimeType()+", "+match.getExtension()+",ts="+(System.currentTimeMillis()-ts)+") of "+ filename+" from "+ip+":"+port);
		    	}
	    		catch(MagicMatchNotFoundException e)
	    		{
	    		}
        }
        catch(Exception e)
        {
        	log.error("", e);
        }
        finally
        {
    		try
			{
            	if( is != null ) is.close();
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
		return match;
	}
	/**
	 * 根据文件数据获取文件类型
	 * @param page
	 * @return
	 */
	public JSONObject getFileTypeByBinary(String ip, int port, String path, long length, HttpServletResponse rsp)
		throws Exception
	{
		byte[] outload = new byte[1024];
    	byte[] payload = null;
    	if( length < Kit.Ks ) 
    	{
    		payload = new byte[(int)length];
    	}
    	else
    	{
    		payload = outload;
    	}
    	int offset = 0;
    	outload[offset++] = Command.CONTROL_GETFILE;//得到文件
    	outload[offset++] = 0;//不压缩获取文件
    	byte buf[] = path.getBytes("UTF-8");
    	Tools.intToBytes(buf.length, outload, offset, 2);
    	offset += 2;
    	offset = Tools.copyByteArray(buf, outload, offset);
    	Tools.intToBytes(0xFF, outload, offset, 1);
    	offset += 1;
    	Tools.intToBytes(1, outload, offset, 4);
    	offset += 4;
    	Tools.longToBytes(payload.length, outload, offset, 8);
    	offset += 8;
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
    	InputStream is = null;
		long ts = System.currentTimeMillis();
		JSONObject type = new JSONObject();
        try
        {
			String filename = path.substring(path.lastIndexOf('/')+1);
			type.put("filename", filename);
    		log.debug("Fetch the data(payload="+payload.length+",offset="+offset+",ts="+(System.currentTimeMillis()-ts)+") of "+ filename+" from ip "+ip+":"+port);
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            InetAddress addr = InetAddress.getByName( ip );
            DatagramPacket request = new DatagramPacket(outload, 0, offset, addr, port );
            datagramSocket.send( request );
			DatagramPacket response = new DatagramPacket(outload, payload.length);
			datagramSocket.setSoTimeout(15000);
			datagramSocket.receive(response);
			port = Tools.bytesToInt(outload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port);
			datagramSocket.close();
			socket = new Socket();
			socket.connect(endpoint, 15000);
            is = socket.getInputStream();
    		int len;
    		offset = 0;
    		if( is.read() != 0 ) throw new Exception("未知协议数据");
    		while( (len = is.read(payload, offset, payload.length-offset)) != -1  )
    		{
    			offset += len;
    			length -= len;
    			if( offset == payload.length ) break;
    		}
//    		type.put("payload", payload);
//    		type.put("offset", offset);
//    		if( length > 0 && len != -1 ) type.put("is", is);
//    		type.put("length", (int)length);
    		//通过二进制进行判断
    		MagicMatch match = null;
    		try
	    	{
//    			System.err.println(new String(payload));
    			match = Magic.getMagicMatch(payload, false);
    			log.debug("Found the file("+match.getMimeType()+", "+match.getExtension()+",ts="+(System.currentTimeMillis()-ts)+") of "+ filename+" from "+ip+":"+port);
	    	}
    		catch(MagicMatchNotFoundException e)
    		{
//    			e.printStackTrace();
    		}
    		this.setContentType(match, type);
    		if(match == null){
    			int len1 = payload.length>10240?10240:payload.length;
    			java.nio.charset.Charset charset =this.detector.detectCodepage(new ByteInputStream(payload, len1), len1);
    			if( charset != null ){
    				type.put("value", "text/plain");
    			}
    		}
    		return type;
        }
        catch(Exception e)
        {
        	throw e;
        }
        finally
        {
    		try
			{
            	if( is != null ) is.close();
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
	}

	/**
	 * 提取指定文件的数据
	 * @param ip
	 * @param port
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public byte[] fetchfile(String ip, int port, String path)
		throws Exception
	{
		log.debug("Fetch the file of "+path);
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
    	InputStream is = null;
    	byte[] outload = new byte[1024];
    	int offset = 0;
    	outload[offset++] = Command.CONTROL_GETFILE;//得到文件
    	outload[offset++] = 0;//不压缩获取文件
    	byte buf[] = path.getBytes("UTF-8");
    	Tools.intToBytes(buf.length, outload, offset, 2);
    	offset += 2;
    	offset = Tools.copyByteArray(buf, outload, offset);
        try
        {
	    	datagramSocket = new DatagramSocket();
	        datagramSocket.setSoTimeout(15000);
	        InetAddress addr = InetAddress.getByName( ip );
	        DatagramPacket request = new DatagramPacket(outload, 0, offset, addr, port );
	        datagramSocket.send( request );
			DatagramPacket response = new DatagramPacket(outload, outload.length);
			datagramSocket.setSoTimeout(15000);
			datagramSocket.receive(response);
			port = Tools.bytesToInt(outload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port);
			datagramSocket.close();
			socket = new Socket();
			socket.connect(endpoint, 15000);
	        is = socket.getInputStream();
	        long flag = is.read();
	        if( flag != 10 ) throw new Exception("未知协议数据");
        	is.read(outload, 0, 8);
        	flag = Tools.bytesToLong(outload, 0);
        	int length = (int)flag;
    		log.debug("The length of fetchfile is "+length);
    		byte[] data = new byte[(int)length];
			int len, off = 0;
			while( length > 0 && (len = is.read(data, off, length)) != -1  )
    		{
    			length -= len;
    			off += len;
    		}
	        return data;
        }
        catch(Exception e)
        {
        	throw e;
        }
        finally
        {
    		try
			{
            	if( is != null ) is.close();
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
	}
	/**
	 * 显示文件
	 * @return
	 */
	public String showfile(HttpServletResponse rsp, String ip, int port, String path, long length, String type)
		throws Exception
	{
		log.debug("Show the file("+type+", length="+length+") of "+path);
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
    	InputStream is = null;
    	byte[] outload = new byte[1024];
    	int offset = 0;
    	outload[offset++] = Command.CONTROL_GETFILE;//得到文件
    	outload[offset++] = 0;//不压缩获取文件
    	byte buf[] = path.getBytes("UTF-8");
    	Tools.intToBytes(buf.length, outload, offset, 2);
    	offset += 2;
    	offset = Tools.copyByteArray(buf, outload, offset);
        try
        {
	    	datagramSocket = new DatagramSocket();
	        datagramSocket.setSoTimeout(15000);
	        InetAddress addr = InetAddress.getByName( ip );
	        DatagramPacket request = new DatagramPacket(outload, 0, offset, addr, port );
	        datagramSocket.send( request );
			DatagramPacket response = new DatagramPacket(outload, outload.length);
			datagramSocket.setSoTimeout(15000);
			datagramSocket.receive(response);
			port = Tools.bytesToInt(outload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port);
			datagramSocket.close();
			socket = new Socket();
			socket.connect(endpoint, 15000);
	        is = socket.getInputStream();
	        long flag = is.read();
	        if( flag != 0 && flag != 10 ) throw new Exception("未知协议数据");
	        if( flag == 10 )
	        {
            	is.read(outload, 0, 8);
            	flag = Tools.bytesToLong(outload, 0);
        		log.debug("The length of showfile is "+flag);
	        }
	        if( "preview".equals(type) ) return this.showfile(rsp, null, offset, length, is);
	        else if( "digit".equals(type) ) return this.showdigit(rsp, is, 1024);
	        else if( "text".equals(type) )	return this.showtext(rsp, length, is);
	        else return this.gettext(length, is);
        }
        catch(Exception e)
        {
        	throw e;
        }
        finally
        {
    		try
			{
            	if( is != null ) is.close();
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
	}
	
	/**
	 * 预览显示文件
	 * @param rsp
	 * @param payload
	 * @param offset
	 * @param length
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public String showfile(HttpServletResponse rsp, byte[] payload, int offset, long length, InputStream is)
		throws Exception
	{
		ServletOutputStream out = null;
		long ts = System.currentTimeMillis();
        try
        {
    		out = rsp.getOutputStream();
    		if( payload != null ) out.write(payload, 0, offset);
    		else payload = new byte[64*1024];
    		out.flush();
    		if( is != null )
    		{
    			int len;
    			while( (len = is.read(payload, 0, payload.length)) != -1  )
        		{
        			length -= len;
            		out.write(payload, 0, len);
    				out.flush();
        		}
    		}
    		log.debug("Finish to show the file(spend="+(System.currentTimeMillis()-ts)+", offset="+offset+", length="+length+")");
        }
        catch(Exception e)
        {
        	throw e;
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
            	if( is != null ) is.close();
			}
			catch (IOException e)
			{
			}
        }
		return null;
	}
	
	/**
	 * 得到文本文件数据
	 * @param rsp
	 * @param payload
	 * @param offset
	 * @param length
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public String gettext(long length, InputStream is)
		throws Exception
	{
        try
        {
			String text = null;
			byte[] payload = new byte[(int)length];
    		if( is != null )
    		{
    			int len, offset = 0;
    			while( (len = is.read(payload, offset, payload.length-offset)) != -1  )
        		{
        			length -= len;
        			offset += len;
        			if( offset == payload.length ) break;
        		}
    			if( length != 0 || offset != payload.length) 
				{
    				log.warn("Found eror(length="+length+", offset="+offset+"/"+payload.length+")");
				}
    			String encoding = null;
    			if( payload.length > 1 ) {
					if( payload[0] == (byte)0xFF && payload[1] == (byte)0xFE ) encoding = "Unicode";
					else if( payload[0] == (byte)0xFE && payload[1] == (byte)0xFF ) encoding = "UTF-16BE";
					else if( payload[0] == (byte)0xEF && payload[1] == (byte)0xBB ) encoding = "UTF-8";
					else {
						ByteBuffer buffer = ByteBuffer.wrap(payload, 0, payload.length);
						try
						{
							Charset utf8 = java.nio.charset.Charset.forName("UTF-8");
							CharBuffer buf = utf8.newDecoder().decode(buffer);
							encoding = "UTF-8";
							text = buf.toString();
	//						System.err.println(encoding+":"+buf.toString());
						}
						catch(Exception e)
						{
							encoding = null;
						}
						if( text == null )
						{
							encoding = "GBK";
		            		text = new String(payload);
						}
					}
    			}
				if( text == null && payload != null) text = encoding!=null?new String(payload, encoding):new String(payload);
//    			System.err.println("encoding="+encoding+"\r\n"+text);
    		}
    		return text;
        }
        catch(Exception e)
        {
        	throw e;
        }
        finally
        {
    		try
			{
            	if( is != null ) is.close();
			}
			catch (IOException e)
			{
			}
        }
	}
	/**
	 * 显示文本文件
	 * @param rsp
	 * @param payload
	 * @param offset
	 * @param length
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public String showtext(HttpServletResponse rsp, long length, InputStream is)
		throws Exception
	{
		OutputStreamWriter out = null;
        try
        {
			String encoding = "UTF-8";//super.getRequest().getParameter("previewCharset");
        	rsp.setContentType("text/html");
    		out = new OutputStreamWriter(rsp.getOutputStream(), encoding);
    		rsp.setCharacterEncoding(encoding);
			out.write("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
			byte[] payload = new byte[(int)length];
    		if( is != null )
    		{
    			int len, offset = 0;
    			while( (len = is.read(payload, offset, payload.length-offset)) != -1  )
        		{
        			length -= len;
        			offset += len;
        			if( offset == payload.length ) break;
        		}
    			if( length != 0 || offset != payload.length) 
				{
    				log.warn("Found eror(length="+length+", offset="+offset+"/"+payload.length+")");
				}
    			encoding = null;
    			String text = null;
				if( payload[0] == (byte)0xFF && payload[1] == (byte)0xFE ) encoding = "Unicode";
				else if( payload[0] == (byte)0xFE && payload[1] == (byte)0xFF ) encoding = "UTF-16BE";
				else if( payload[0] == (byte)0xEF && payload[1] == (byte)0xBB ) encoding = "UTF-8";
				else {
					ByteBuffer buffer = ByteBuffer.wrap(payload, 0, payload.length);
					try
					{
						Charset utf8 = java.nio.charset.Charset.forName("UTF-8");
						CharBuffer buf = utf8.newDecoder().decode(buffer);
						encoding = "UTF-8";
						text = buf.toString();
//						System.err.println(encoding+":"+buf.toString());
					}
					catch(Exception e)
					{
						encoding = null;
					}
					if( text == null )
					{
						encoding = "GBK";
	            		text = new String(payload);
					}
//					encoding = "UTF-8";
//		            text = new String(payload, encoding);
//		            if( !text.equals(new String(text.getBytes(encoding), encoding)) )
//		            {
//		            	encoding = "GBK";
//		            	text = new String(payload, encoding);
//		            	if( !text.equals(new String(text.getBytes(encoding), encoding)))
//			            {
//		            		encoding = "未知编码";
//		            		text = new String(payload);
//			            }
//		            }
				}
				if( text == null ) text = new String(payload, encoding);
//    			System.err.println("encoding="+encoding+"\r\n"+text);
    			out.write(text);
    			out.flush();
    		}
    		out.write("</body>\r\n\r\n<script type='text/javascript'>if(parent&&parent.setTextEncoding){parent.setTextEncoding('"+encoding+"');}if(parent&&parent.skit_hiddenLoading){parent.skit_hiddenLoading();}</script></html>");
        }
        catch(Exception e)
        {
        	throw e;
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
            	if( is != null ) is.close();
			}
			catch (IOException e)
			{
			}
        }
		return null;
	}
	/**
	 * 显示文本
	 * @return
	public String showtext(HttpServletResponse rsp, byte[] payload, int offset, long length, InputStream is)
		throws Exception
	{
		ServletOutputStream out = null;
        try
        {
			String encoding = "UTF-8";//super.getRequest().getParameter("previewCharset");
        	rsp.setContentType("text/html");
    		out = rsp.getOutputStream();
			int ch, i = 0;
			boolean noEncoding = true;
			if( payload != null )
			{
				if( payload[0] == 0xFF && payload[1] == 0xFE ) encoding = "Unicode";
				else if( payload[0] == 0xFE && payload[1] == 0xFF ) encoding = "UTF-16";
				else if( payload[0] == 0xFF && payload[1] == 0xBB ) encoding = "UTF-8";
				else encoding = "GBK";
				rsp.setCharacterEncoding(encoding);
				noEncoding = false;
				out.println("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
				while( i < offset  )
	    		{
					ch = payload[i++];
	    			if( ch == '<' ) out.print("&lt;");
	    			else if( ch == '>' ) out.print("&gt;");
	    			else out.write(ch);
	    		}
			}
			else payload = new byte[64*1024];
    		if( is != null )
    		{
//        		while( (ch = is.read()) != -1  )
//        		{
//        			if( ch == '<' ) 
//        				out.print("&lt;");
//        			else if( ch == '>' ) 
//        				out.print("&gt;");
//        			else
//        				out.write(ch);
//        			length -= 1;
//        			if( length == 0 ) break;
//        		}
    			int len; offset = 0;
    			while( (len = is.read(payload, 0, payload.length)) != -1  )
        		{
    				if( noEncoding )
    				{
    					if( payload[0] == (byte)0xFF && payload[1] == (byte)0xFE ) encoding = "Unicode";
    					else if( payload[0] == (byte)0xFE && payload[1] == (byte)0xFF ) encoding = "UTF-16BE";
    					else if( payload[0] == (byte)0xFF && payload[1] == (byte)0xBB ) encoding = "UTF-8";
    					else encoding = "GBK";
    					encoding = "UTF-8";
//    	    			System.err.println("encoding="+Integer.toHexString(payload[0])+","+Integer.toHexString(payload[1]));
    					rsp.setCharacterEncoding(encoding);
    					noEncoding = false;
    					out.println("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
    				}
        			length -= len;
        			offset = 0;
        			for(i = 0; i < len; i++ )
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
    			System.err.println("encoding="+encoding);
    		}
    		out.println("</body>\r\n\r\n<script type='text/javascript'>if(parent&&parent.setTextEncoding){parent.setTextEncoding('"+encoding+"');}if(parent&&parent.skit_hiddenLoading){parent.skit_hiddenLoading();}</script></html>");
        }
        catch(Exception e)
        {
        	throw e;
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
            	if( is != null ) is.close();
			}
			catch (IOException e)
			{
			}
        }
		return null;
	}
	*/
	/**
	 * 显示二进制
	 * @return
	 */
	public String showdigit(HttpServletResponse rsp, InputStream is, int length)
		throws Exception
	{
		ServletOutputStream out = null;
        try
        {
        	byte[] payload = new byte[length];
			int len;
			int offset = 0;
    		while( (len = is.read(payload, offset, payload.length-offset)) != -1  )
    		{
    			offset += len;
    			if( offset == payload.length ) break;
    		}
        	rsp.setContentType("text/html");
			String encoding ="ISO-8859-1";//super.getRequest().getParameter("previewCharset");
			rsp.setCharacterEncoding(encoding);
    		out = rsp.getOutputStream();
    		out.println("<html><body style='font-family: \"NSimSun\";padding:1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
			String str = Tools.getBinaryString(payload, 0, offset);
    		out.write(str.getBytes());
    		out.println();
    		out.println("</body>\r\n\r\n<script type='text/javascript'>if(parent&&parent.skit_hiddenLoading){parent.skit_hiddenLoading();}</script></html>");
        }
        catch(Exception e)
        {
        	throw e;
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
        }
		return null;
	}
	
	/**
	 * 输入路径，输出节点数组
	 * @param path 输入路径
	 * @param rootdir 相对路径的对应跟目录
	 * @return
	private static ArrayList<String> getPaths(String path, String rootdir)
	{
		ArrayList<String> dirs = getPaths(path);
		if( rootdir != null )
			for(int i = 0; i < dirs.size(); i++)
			{
				String dirpath = dirs.get(i);
				dirs.set(i, rootdir+dirpath);
			}
		return dirs;
	}
	 */
	/**
	 * 
	 * @param path
	 * @return
	 */
	private static ArrayList<String> getPaths(String path)
	{
		ArrayList<String> list = new ArrayList<String>();
		if( path == null || path.isEmpty() ){
			list.add("");
			return list;
		}
		path = path.trim();
		if( path.charAt(0) == ':' ){
			return list;
		}
		if( path.equals("/") ){
			list.add("/");
			return list;
		}
		if( path.indexOf(':') != 1 || path.lastIndexOf(':') != 1 )
		{
			if( path.indexOf(':') != -1 ) return list;
		}
		
		if( path.charAt(1) == ':' ){
			if( path.length() == 2 )
			{
				list.add(path+"/");
				return list;
			}
			if( path.charAt(2) != '/' )
			{
				return list;
			}
			if( path.length() == 3 )
			{
				list.add(path);
				return list;
			}
		}
		String separator = "/";//System.getProperty("file.separator");
		String names[] = Tools.split(path, separator);
		String dir = names[0];
		if( dir.isEmpty() ) dir = "/";
		else if( dir.endsWith(":") ) dir += "/";
//		if( dir.charAt(0) != '/' && dir.charAt(1) != ':') dir = "../"+dir;
		list.add(dir);
		for(int i = 1; i < names.length; i++)
		{
			if( names[i].trim().isEmpty() ) continue;
			if( dir.endsWith("/") ) dir += names[i];
			else dir += "/"+names[i];
			list.add(dir);
		}
		return list;
	}
	
	public static void main(String args[])
	{
		String[] paths = new String[]{
				":",
				":aaa",
				"aa:a:",
				"D:aaa", 
				"",
				"/",
				"D:", 
				"D:/",
				"D:/focusnt",
				"D:/focusnt/",
				"D:/focusnt//",
				"/focusnt",
				"/focusnt/",
				"bin",
				"bin/",
				"config/zk",
				"config/zk/",
				"config//zk/",
				"/focusnt/cos",
				"/focusnt/cos/",
				"tomcat/workspace/"};
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for(String path : paths)
		{
			sb.append("\r\ncase["+(i++)+"] '"+path+"'");
			int j = 0;
			ArrayList<String> list = FilesMgr.getPaths(path);
			for(String dir : list )
			{
				sb.append("\r\n\tdir["+(j++)+"] '"+dir+"'");
			}
		}
		System.err.println(sb.toString());
	}
}