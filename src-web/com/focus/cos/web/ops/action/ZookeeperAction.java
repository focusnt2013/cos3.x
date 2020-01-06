package com.focus.cos.web.ops.action;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.ops.service.FilesMgr;
import com.focus.cos.web.ops.service.ZookeeperMgr;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;
import com.opensymphony.webwork.ServletActionContext;

/**
 * ZK监控action
 * @author focus
 *
 */
public class ZookeeperAction extends OpsAction
{
	private static final long serialVersionUID = -5120124401687877178L;
	private static final Log log = LogFactory.getLog(ZookeeperAction.class);
	/*查看路径*/
	private String path = "/";
	/*ZK的管理器*/
	private ZookeeperMgr zookeeperMgr;
	/*ZK的管理器*/
	private FilesMgr filesMgr;

	/**
	 * 导入元数据查询配置模板
	 * @return
	 */
	public String importdata()
	{
		JSONObject rsp = new JSONObject();
		if( this.uploadfile == null || !uploadfile.exists() )
		{
			rsp.put("alt", "导入子系统配置包失败，因为未能收到文件包");
			return response(super.getResponse(), rsp.toString());
		}
		log.info("Import the data of zookeeper("+uploadfile.length()+") from "+uploadfile);
		ZipFile zip = null;
        StringBuilder sb = new StringBuilder();
        try
        {
        	Zookeeper zookeeper = ZKMgr.getZookeeper();
	        zip = new ZipFile(uploadfile);
	        Enumeration<?> entries = zip.entries();
	        String firstpath = null;
	        long length = 0;
	        while(entries.hasMoreElements())
	        {
	            ZipEntry entry = (ZipEntry)entries.nextElement();
	            path = entry.getName();
            	byte[] payload = IOHelper.readAsByteArray(zip.getInputStream(entry));
	            if(path.startsWith("_")){
	            	path = path.substring(1);
	            	payload = null;
	            }
	            path = Kit.unicode2Chr(path);
	            if( firstpath == null ) firstpath = path;
            	sb.append("\r\n\t"+path);
            	length += payload!=null?payload.length:0;
            	sb.append("\t"+(payload!=null?payload.length:-1));
            	Stat stat = zookeeper.exists(path);
            	if( stat != null ){
            		sb.append("\trecoved.");
            		stat = zookeeper.setData(path, payload, stat.getVersion());
            	}
            	else{
            		sb.append("\tcreated.");
            		zookeeper.create(path, payload);
            	}
	        }
			rsp.put("alt", String.format("成功导入%s等%s个节点，%sZooKeeper数据。", firstpath, zip.size(), Kit.bytesScale(length)));
			rsp.put("succeed", true);
			log.info(sb.toString());
        }
        catch(Exception e)
        {
        	log.error(sb.toString(), e);
			rsp.put("alt", "导入ZooKeeper数据失败，因为出现异常"+e.getMessage());
        }
		finally
		{
			if( zip != null )
				try
				{
					zip.close();
				}
				catch (IOException e) {
				}
		}
		return this.response(super.getResponse(), rsp.toString());
	}
	/**
	 * 导出ZK数据
	 * @return
	 */
	public String export()
	{
		log.info("Export the data of "+path);
		ZipOutputStream out = null;
		try 
		{
			File p = new File(path);
			String filename = p.getName();
			filename = "zk__"+filename;
			filename = Tools.replaceStr(filename, ".", "_");
			filename = filename+".zip";
			getResponse().setContentType("application/binary;charset=ISO8859_1");
			getResponse().setHeader("Content-disposition", "attachment; filename*=UTF-8''"+URLEncoder.encode(filename,"UTF-8"));
			out = new ZipOutputStream(new CheckedOutputStream(getResponse().getOutputStream(), new CRC32()));
			Zookeeper zookeeper = ZKMgr.getZookeeper();
				this.responseMessage = "导出ZooKeeper【"+path+"】成功。";
			this.zookeeperMgr.exportZookeeper(zookeeper, path, out);
    		logoper(responseMessage, "集群ZK管理", "", null);
        }
        catch(Exception e)
        {
        	this.responseException ="导出ZooKeeper【"+path+"】出现异常 "+e.getMessage();
    		logoper(responseException, "开发管理", null, null, e);
        	return "alert";
        }
        finally
        {
    		try
			{
            	if( out != null ) out.close();
			}
			catch (Exception e)
			{
				log.error("Failed to close the outputstream of zip.", e);
			}
        }
		return null;
	}

	/**
	 * 下载
	 * @return
	 */
	public String download()
	{
		log.info("Download the data of "+path);
    	ServletOutputStream out = null;
		try 
		{
	        byte[] payload = ZKMgr.getZookeeper().getData(path);
			int i = path.lastIndexOf("/");
			String filename = path.substring(i+1);
    		JSONObject type = filesMgr.getFileTypeByExtension(filename);
    		if( type != null )
    		{
    			filetype = type.getString("value");
    		}
    		else
    		{
    			filetype = "application/octet-stream";
    		}
			
			getResponse().setContentType(filetype);
            getResponse().setHeader("Content-disposition", "attachment; filename*=UTF-8''"+URLEncoder.encode(filename,"UTF-8"));
            getResponse().setHeader("Access-Control-Allow-Origin", "*");
			getResponse().setHeader("Access-Control-Allow-Methods", "*");
    		out = getResponse().getOutputStream();
    		out.write(payload);
    		out.flush();
            this.responseMessage = "从ZK["+path+"]下载数据成功。";
    		logoper(responseMessage, "集群ZK管理", new String(payload, "UTF-8"), null);
        }
        catch(Exception e)
        {
        	log.error("", e);
        	this.responseException ="从ZK["+path+"]下载数据出现异常 "+e.getMessage();
    		logoper(responseException, "集群ZK管理", null, null, e);
        	return "alert";
        }
        finally
        {
    		try
			{
            	if( out != null ) out.close();
			}
			catch (Exception e)
			{
			}
        }
		return null;
	}
	/**
	 * 服务器节点Zookeeper导航
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
			this.grant = super.isSysadmin();
//			System.err.println("naviagte "+clusters.toString(4));
			jsonData = clusters.toString();
		} catch (Exception e) {
			super.responseException = e.getMessage();
		}
		return "navigate";
	}
	
	/**
	 * 打开指定服务器的Zookeeper
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
		}
		log.info("Open zookeeper("+ip+":"+port+").");
		JSONArray children = zookeeperMgr.getNodesData("/", ip, port);
		if( children == null )
		{
			super.setResponseException("分布式程序协调服务没有激活");
			return "alert";
		}
		JSONArray trees = new JSONArray();
		JSONObject root = new JSONObject();
		File dir = new File(PathFactory.getDataPath(), "zkbak/"+ip+"_"+port);
//		String md5 = Tools.encodeMD5("/");
//		File file = new File(dir, md5);
		root.put("backup", dir.exists());
		root.put("name", "/");
		root.put("path", "/");
		root.put("iconClose", "images/icons/folder_closed.png");
		root.put("iconOpen",  "images/icons/folder_opened.png");
		root.put("isParent", true);
		root.put("open", true);
		trees.put(root);
		root.put("children", children);
		this.jsonData = trees.toString();
		this.grant = super.isSysadmin();
		if( !this.grant )
		{
			try
			{
				JSONObject privileges = this.getMonitorMgr().getServerPrivileges(super.getUserRole(), super.getUserAccount(), this.getMonitorMgr().getServer(this.getServerId()));
				grant = privileges.has("zookeeper")&&privileges.getBoolean("zookeeper");
			} catch (Exception e) {
				super.responseException = e.getMessage();
			}
		}
		return "open";
	}
	/**
	 * 预览数据
	 * @return
	 */
	public String preview()
	{
    	ServletOutputStream out = null;
    	String outstr = null;
        try
        {
        	byte[] payload = this.zookeeperMgr.getNodeData(path, ip, port);
        	if( payload == null )
        	{
        		outstr = "该节节点数据为空";
        	}
        	else if( path.endsWith(".png") )
        	{
        		int i = path.lastIndexOf("/");
        		String name = path.substring(i+1);
        		HttpServletResponse response = null;
		        response = ServletActionContext.getResponse();
				response.setContentType("image/png");
				response.setHeader("Content-disposition", "inline; filename="+name+".png");
	    		out = getResponse().getOutputStream();
				out.write(payload);
				out.flush();
				return null;
        	}
        	if( path.endsWith(".xml") )
        	{
        		messageCode = "xml";
        		message = new String(payload, "UTF-8");
				return "xml_json_css_sql";
        	}
        	if( path.endsWith(".json") )
        	{
        		messageCode = "json";
        		message = new String(payload, "UTF-8");
				return "xml_json_css_sql";
        	}
			if( outstr == null ) {
				outstr = new String(payload, "UTF-8");
			}
			if( (outstr.startsWith("{") &&  outstr.endsWith("}")) ||
				(outstr.startsWith("[") &&  outstr.endsWith("]")) )
    		{
        		messageCode = "json";
        		message = outstr;
				return "xml_json_css_sql";
    		}
			else if( outstr.startsWith("<?xml") )
			{
        		messageCode = "xml";
        		message = outstr;
				return "xml_json_css_sql";
			}
			else if( payload != null && payload.length < 512 ){
	    		outstr = new String(payload, "UTF-8");
			}
			else if( payload != null ){
	    		outstr = Tools.getBinaryString(payload, 0, 1024);
			}

    		out = getResponse().getOutputStream();
			getResponse().setContentType("text/html");
			getResponse().setCharacterEncoding("UTF-8");
    		out.println("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
    		if( payload != null && payload.length == 0 )
    		{
        		outstr = "该节点数据是空字符串。";
    		}
    		out.write(outstr.getBytes("UTF-8"));
    		out.println("</body>\r\n\r\n<script type='text/javascript' LANGUAGE='JavaScript'>if(parent&&parent.setScrollBottom){parent.setScrollBottom();}if(parent&&parent.skit_hiddenLoading){parent.skit_hiddenLoading();}</script></html>");
        }
        catch(Exception e)
        {
        	this.responseException = "预览Zookeeper节点数据 出现 异常"+e.getMessage();
        	return "alert";
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

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}
	
	public void setZookeeperMgr(ZookeeperMgr zookeeperMgr) {
		this.zookeeperMgr = zookeeperMgr;
	}

	public void setFilesMgr(FilesMgr filesMgr) {
		this.filesMgr = filesMgr;
	}
}
