package com.focus.cos.web.ops.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKDeleter;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

public class ZookeeperMgr extends SvrMgr
{
	private static final Log log = LogFactory.getLog(ZookeeperMgr.class);
	/**
	 * 
	 * @param path
	 * @param ip
	 * @param port
	 * @return
	 */
	public AjaxResult<String> getNodes(String path, String ip, int port)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		JSONArray data = this.getNodesData(path, ip, port);
		if( data != null )
		{
			rsp.setSucceed(true);
			rsp.setResult(data.toString());
//			System.err.println(data.toString(4));
		}
		else
		{
			rsp.setMessage("连接分布式程序协调服务("+ip+":"+port+")失败");
		}
		return rsp;
	}

	/**
	 * 得到节点数据
	 * @param path
	 * @param ip
	 * @param port
	 * @return
	 */
	public byte[] getNodeData(String path, String ip, int port)
	{
		Zookeeper zookeeper = null;
		try
		{
			zookeeper = Zookeeper.getInstance(ip+":"+port);
			Stat stat = zookeeper.exists(path, false);
			if( stat != null )
			{
				return zookeeper.getData(path, false, stat);
			}
		}
		catch(Exception e)
		{
			log.error("Failed to get the node from "+path+" for "+e);
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
		return null;
	}
	/**
	 * 得到节点数据
	 * @param path
	 * @param ip
	 * @param port
	 * @return
	 */
	public JSONArray getNodesData(String path, String ip, int port)
	{
		log.info("Open zookeeper("+ip+":"+port+").");
		Zookeeper zookeeper = null;
		try
		{
//			File dir = new File(PathFactory.getDataPath(), "zkbak/"+ip+"_"+port);
			zookeeper = Zookeeper.getInstance(ip+":"+port);
			List<String> names = zookeeper.getChildren(path, false);
			JSONArray children = new JSONArray();
			if( path.equals("/") ) path = "";
			for(String name : names)
			{
				JSONObject child = new JSONObject();
				child.put("name", name);
				child.put("path", path+"/"+name);
				child.put("isParent", true);
				child.put("iconClose", "images/icons/folder_closed.png");
				child.put("iconOpen",  "images/icons/folder_opened.png");
				children.put(child);
				Stat stat = zookeeper.exists(child.getString("path"), false);
				child.put("ctime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", stat.getCtime()));
				child.put("mtime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", stat.getMtime()));
				child.put("size", Kit.bytesScale(stat.getDataLength()));
				child.put("version", stat.getVersion());
				child.put("isParent", stat!=null&&stat.getNumChildren()>0);
//				String md5 = Tools.encodeMD5(path+"/"+name);
//				File file = new File(dir, md5);
//				child.put("backup", file.exists());
			}
			//System.err.println(children.toString(4));
			return children;
		}
		catch(Exception e)
		{
			log.error("Failed to get the nodes from "+path+" for "+e);
			return null;
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
	}

	/**
	 * 取消指定集群节点备份
	 * @param ip
	 * @param port
	 * @param path
	 * @return
	 */
	public AjaxResult<String> cancelBackup(String ip, int port, String path)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		try
		{
			File dir = new File(PathFactory.getDataPath(), "zkbak/"+ip+"_"+port);
			if( dir.exists() )
			{
				FileUtils.deleteDirectory(dir);
				if( dir.exists() ){
					result.setMessage("取消Zookeeper("+(ip+":"+port)+")自动镜像备份失败。");
				}
				else{
					result.setSucceed(true);
					result.setMessage("取消Zookeeper("+(ip+":"+port)+")自动镜像备份成功。");
				}
			}
			else
			{
				result.setSucceed(true);
				result.setMessage("该Zookeeper("+(ip+":"+port)+")并没有设置自动镜像备份。");
			}
		}
		catch (Exception e)
		{
			result.setMessage("取消Zookeeper("+(ip+":"+port)+")自动镜像备份出现异常"+e);
		}
		return result;
	}
	/**
	 * 设置数据
	 * @param ip
	 * @param port
	 * @param path
	 * @param text
	 * @return
	 */
	public AjaxResult<String> setData(String ip, int port, String path, String text)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		Zookeeper zookeeper = null;
		try
		{
			System.err.println(text);
			if( text.startsWith("{") ){
				new JSONObject(text);
			}
			if( text.startsWith("[") ){
				new JSONArray(text);
			}
			long ts = System.currentTimeMillis();
			zookeeper = Zookeeper.getInstance(ip+":"+port);
			Stat stat = zookeeper.exists(path);
			zookeeper.setData(path, text.getBytes("UTF-8"), stat.getVersion(), text.length() > 1000000);
			result.setSucceed(true);
			ts = System.currentTimeMillis() - ts;
			result.setMessage("设置Zookeeper("+(ip+":"+port)+")节点【"+path+"】数据成功，耗时"+ts+"毫秒.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result.setMessage("设置Zookeeper("+(ip+":"+port)+")节点【"+path+"】数据出现异常:"+e);
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
		return result;
	}

	/**
	 * 设置数据
	 * @param ip
	 * @param port
	 * @param path
	 * @param text
	 * @return
	 */
	public AjaxResult<String> createJSONNode(String ip, int port, String path)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		Zookeeper zookeeper = null;
		try
		{
			zookeeper = Zookeeper.getInstance(ip+":"+port);
			zookeeper.createNode(path, "{}".getBytes("UTF-8"));
			result.setSucceed(true);
			result.setMessage("创建Zookeeper("+(ip+":"+port)+")JSON节点【"+path+"】数据成功.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			result.setMessage("创建Zookeeper("+(ip+":"+port)+")JSON节点【"+path+"】数据出现异常:"+e);
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
		return result;
	}
	/**
	 * 设置指定集群节点备份
	 * @param ip
	 * @param port
	 * @param path
	 * @return
	 */
	public AjaxResult<String> setBackup(String ip, int port, String path)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		try
		{
			File dir = new File(PathFactory.getDataPath(), "zkbak/"+ip+"_"+port);
//			if( !dir.exists() ) dir.mkdirs();
//			File file = new File(dir, md5);
			if( dir.exists() )
			{
				result.setMessage("该Zookeeper("+(ip+":"+port)+")已经设置了自动镜像备份。");
			}
			else
			{
				dir.mkdirs();
				result.setSucceed(true);
				result.setMessage("该Zookeeper("+(ip+":"+port)+")成功设置了自动镜像备份。");
			}
		}
		catch (Exception e)
		{
			result.setMessage("设置Zookeeper("+(ip+":"+port)+")自动镜像备份出现异常"+e);
		}
		return result;
	}
	
	private HashMap<String, ZKDeleter> mapDeleteNodes = new HashMap<String, ZKDeleter>();
	/**
	 * 删除节点
	 * @param ip
	 * @param port
	 * @param path
	 * @return
	 */
	public AjaxResult<String> deleteNode(String ip, int port, String path)
	{
		AjaxResult<String> result = new AjaxResult<String>();
		try
		{
			ZKDeleter deleter = new ZKDeleter(ip, port, path);
			mapDeleteNodes.put(deleter.getName(), deleter);
			result.setResult(deleter.getName());
			deleter.start();
			result.setSucceed(true);
		}
		catch (Exception e)
		{
			result.setMessage("执行删除Zookeeper("+(ip+":"+port)+")节点【"+path+"】出现异常"+e);
		}
		return result;
	}
	/**
	 * 检查删除结果
	 * @return
	 */
	public AjaxResult<Integer> getDeleteResult(String id)
	{
		AjaxResult<Integer> result = new AjaxResult<Integer>();
		ZKDeleter task = mapDeleteNodes.get(id);
		if( task != null )
		{
			result.setSucceed(task.isRunning());
			result.setResult(task.getProgress());
			if( !task.isRunning() ){
				if( task.getException() != null )
				{
					result.setMessage("删除集群伺服器【"+task.getIp()+"】ZK节点["+task.getDeletepath()+"]包括所有子节点共"+task.getPathDeletes().size()+"节点出现异常："+task.getException().getMessage());
					logoper(LogSeverity.ERROR, result.getMessage(), "ZK管理");
				}
				else
				{
					result.setMessage("删除集群伺服器【"+task.getIp()+"】ZK节点["+task.getDeletepath()+"]包括所有子节点共"+task.getPathDeletes().size()+"节点成功。");
					logoper(result.getMessage(), "ZK管理", "", "");
				}
				mapDeleteNodes.remove(id);
				sendNotiefiesToSystemadmin(
						"集群ZK管理",
						String.format("用户[%s]"+result.getMessage(), getAccountName()),
	                    "删除的文件或文件夹如下所示："+task.toString(),
	                    null,
	                    "查看ZK管理器", "zookeeper!navigate.action");
				try 
				{
					sendNotiefieToAccount(
							super.getAccountName(),
							"集群ZK管理",
							"您"+result.getMessage(),
					        "删除的节点如下所示："+task.toString(),
					        null,
					        "查看ZK管理器", "zookeeper!navigate.action");
				}
				catch (Exception e) 
				{
				}
			}
		}
		else
		{
			result.setMessage("删除任务["+id+"]已经消失");
			result.setResult(0);
		}
		return result;
	}
}
