package com.focus.cos.web.config.service;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.WebContextFactory;
import org.json.JSONObject;

import com.focus.cos.control.Command;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.COSConfig;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.Skin;
import com.focus.cos.web.ops.service.Monitor.RunFetchMonitor;
import com.focus.cos.web.ops.service.MonitorMgr;
import com.focus.util.IOHelper;
import com.focus.util.Tools;

/**
 * 组件管理
 * @author focus
 *
 */
public class SysCfgMgr extends CfgMgr
{
	private static final Log log = LogFactory.getLog(SysCfgMgr.class);
	public static String id = "system";
	/**/
	private static JSONObject config;

	/**
	 * 得到恢复数据进度
	 * @return
	 */
	public String getRestoreDatabaseProgress()
	{
		try
		{
			org.directwebremoting.WebContext web = WebContextFactory.get(); 
			HttpServletRequest request = web.getHttpServletRequest();
	        String referer = request.getHeader("referer");
	        referer += ".restore.database";
	        String progress = (String)request.getSession().getAttribute(referer);
	        return progress;
		}
		catch(Exception e)
		{
			return "获取恢复数据操作进度出现异常:"+e;
		}
	}
	/**
	 * 恢复数据库
	 * @param ip
	 * @param port
	 * @param mode
	 * @return
	 */
	public AjaxResult<String> restoreDatabase(
		String masterip,
		int masterport,
		String masterrootdir,
		String standbyip,
		int standbyport,
		String standbyrootdir,
		String standbydatabase)
	{
		JSONObject result = new JSONObject();
		DatagramSocket datagramSocket = null;
		AjaxResult<String> rsp = new AjaxResult<String>();
		StringBuilder sb = new StringBuilder(String.format("将伺服器【%s:%s】主数据库数据%sh2替换伺服器【%s:%s】的主数据库数据%sh2",
				standbyip, standbyport, standbyrootdir, masterip, masterport, masterrootdir));
		File tempfile = null;
		try
		{
			masterrootdir = masterrootdir.replaceAll("\\\\", "/");
			standbyrootdir = standbyrootdir.replaceAll("\\\\", "/");
			org.directwebremoting.WebContext web = WebContextFactory.get(); 
			HttpServletRequest request = web.getHttpServletRequest();
	        String referer = request.getHeader("referer");
	        referer += ".restore.database";
	        HttpSession session = request.getSession();
	        
			RunFetchMonitor master = MonitorMgr.getTracker().getRunFetchMonitor(masterip, masterport);
			if( master == null ){
				sb.append("\r\n\t主数据库伺服器【"+masterip+":"+masterport+"】没有正常工作，不能执行数据库恢复操作.");
				return rsp;
			}
			RunFetchMonitor standby = MonitorMgr.getTracker().getRunFetchMonitor(standbyip, standbyport);
			if( standby == null ){
				sb.append("\r\n\t备份数据库伺服器【"+standbyip+":"+standbyport+"】没有正常工作，不能执行数据库恢复操作.");
				return rsp;
			}
			//TODO:第一步，执行备份数据库停止操作
			sb.append(String.format("\r\n\t[%s]#1步 停止备份数据库: ", Tools.getFormatTime("HH:mm:ss", System.currentTimeMillis())));
			session.setAttribute(referer, sb.toString());
			JSONObject json = this.executeDatabaseOper(standby, 0);
			sb.append(json.getString("result"));
			result.put("standby", json);
			Thread.sleep(3000);
            
			//TODO:第2步，复制备份数据库文件
			sb.append(String.format("\r\n\t[%s]#2步 ", Tools.getFormatTime("HH:mm:ss", System.currentTimeMillis())));
			sb.append(String.format("复制伺服器【%s:%s】主数据库文件%scos.mv.db", standbyip, standbyport, standbyrootdir));
			session.setAttribute(referer, sb.toString());
			tempfile = super.fetchFiles(standbyip, standbyport, standbyrootdir+"h2/cos.mv.db");
			sb.append("\r\n\t\t");
			sb.append(String.format("复制数据库文件%s成功，数据长度%s", tempfile.getPath(), tempfile.length()));
			//TODO:第3步，停止主数据库伺服器数据库服务
			sb.append(String.format("\r\n\t[%s]#3步 停止主数据库:", Tools.getFormatTime("HH:mm:ss", System.currentTimeMillis())));
			session.setAttribute(referer, sb.toString());
			json = this.executeDatabaseOper(master, 0);
			sb.append(json.getString("result"));
			result.put("master", json);
            
			//TODO:第4步，备份数据文件
			sb.append(String.format("\r\n\t[%s]#4步  备份主数据库文件:", Tools.getFormatTime("HH:mm:ss", System.currentTimeMillis())));
			session.setAttribute(referer, sb.toString());
			json = this.executeDatabaseOper(master, 3);
			sb.append(json.getString("result"));
			result.put("master", json);
            
			//TODO:第5步，拷贝文件到主数据库伺服器
			sb.append(String.format("\r\n\t[%s]#5步 ", Tools.getFormatTime("HH:mm:ss", System.currentTimeMillis())));
			sb.append(String.format("拷贝数据文件到伺服器【%s:%s】主数据库文件%scos.mv.db", masterip, masterport, masterrootdir));
			session.setAttribute(referer, sb.toString());
			super.copyFiles(masterip, masterport, masterrootdir+"h2/restore.zip", tempfile);
			sb.append(String.format("\r\n\t\t拷贝数据库文件成功"));
			Thread.sleep(Tools.MILLI_OF_MINUTE);
			super.deleteFiles(masterip, masterport, masterrootdir+"h2/restore.zip");
			super.deleteFiles(masterip, masterport, masterrootdir+"h2/restore/");
			
			//TODO:第6步，移除备份服务器的数据文件
			/*sb.append(String.format("\r\n\t[%s]#6步 移除备数据库器数据文件:", Tools.getFormatTime("HH:mm:ss", System.currentTimeMillis())));
			session.setAttribute(referer, sb.toString());
			json = this.executeDatabaseOper(standby, 4);
			sb.append(json.getString("result"));
			result.put("standby", json);*/
            
			//TODO:第7步，启动备份服务器的是数据看文件
			/*sb.append(String.format("\r\n\t[%s]#7步 启动备份数据库:", Tools.getFormatTime("HH:mm:ss", System.currentTimeMillis())));
			session.setAttribute(referer, sb.toString());
			json = this.executeDatabaseOper(standby, 1);
			sb.append(json.getString("result"));
			result.put("standby", json);*/
            
			//TODO:第8步，启动主服务器
			sb.append(String.format("\r\n\t[%s]#6步 启动主数据库:", Tools.getFormatTime("HH:mm:ss", System.currentTimeMillis())));
			session.setAttribute(referer, sb.toString());
			json = this.executeDatabaseOper(master, 1);
			sb.append(json.getString("result"));
			result.put("master", json);
            
			//TODO:第8步，启动主服务器数据库集群
			/*sb.append(String.format("\r\n\t[%s]#9步 启动数据库集群:", Tools.getFormatTime("HH:mm:ss", System.currentTimeMillis())));
			session.setAttribute(referer, sb.toString());
			json = this.executeDatabaseOper(master, 2, standbydatabase);
			sb.append(json.getString("result"));*/
            
			result.put("master", json);
			rsp.setSucceed(true);
		}
		catch(Exception e)
		{
			log.error("Failed to restore the database for exception.", e);
			sb.append(String.format("\r\n[ERROR]执行异常: %s", e.getMessage()));
		}
        finally
        {
        	if( datagramSocket != null )
        	{
        		datagramSocket.close();
        	}
        	rsp.setMessage(sb.toString());
        	rsp.setResult(result.toString());
        	if( tempfile != null ){
        		IOHelper.deleteDir(tempfile.getParentFile());
        	}
        }
		return rsp;		
	}
	
	/**
	 * 执行数据库操作
	 * @param cosnode
	 * @param mode
	 * @return
	 * @throws Exception
	 */
	private JSONObject executeDatabaseOper(RunFetchMonitor cosnode, int mode)
		throws Exception
	{
		return this.executeDatabaseOper(cosnode, mode, "");
	}
	private JSONObject executeDatabaseOper(RunFetchMonitor cosnode, int mode, String param)
		throws Exception
	{
		DatagramSocket datagramSocket = null;
		try
		{
			byte[] payload = new byte[64*1024];
			payload[0] = Command.CONTROL_H2;
			payload[1] = (byte)mode;
			payload[2] = (byte)param.length();
			int off = Tools.copyByteArray(param.getBytes(), payload, 3);
			datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(180000);
            DatagramPacket request = new DatagramPacket(payload, 0, off, InetAddress.getByName( cosnode.getIp() ), cosnode.getPort() );
            datagramSocket.send( request );
            DatagramPacket response = new DatagramPacket( payload, 0, payload.length, request.getAddress(), request.getPort() );
            datagramSocket.receive(response);
            String json = new String(payload, 0, response.getLength(), "UTF-8");
            JSONObject result = new JSONObject(json);
            result.put("cosworking", true);
            result.put("cosaddr", cosnode.toString());
            result.put("ip", cosnode.getIp());
            result.put("cid", cosnode.getServerid());
            result.put("name", cosnode.getSysDesc().getDescript());
            result.put("length", result.getLong("size"));
            result.put("size", Kit.bytesScale(result.getLong("size")));
			if( !result.has("starttime") ){
				result.put("starttime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", cosnode.getStartuptime()));
			}
			cosnode.getSysDesc().setProperty("h2", json);
			return result;
		}
		catch(Exception e)
		{
			log.error("Failed to set the status of database ", e);
			throw e;
		}
        finally
        {
        	if( datagramSocket != null )
        	{
        		datagramSocket.close();
        	}
        }
	}
	/**
	 * 执行数据库操作
	 * @param ip
	 * @param port
	 * @param mode 0停止数据库，1重启数据库 2启动数据库集群 3系统备份 4移除备份 5拷贝备份
	 * @param standby 执行数据库操作对端服务
	 * @return
	 */
	public AjaxResult<String> executeDatabaseOper(
		String ip,
		int port,
		int mode,
		String standby)
	{
		DatagramSocket datagramSocket = null;
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			RunFetchMonitor cosnode = MonitorMgr.getTracker().getRunFetchMonitor(ip, port);
			if( cosnode != null ){
	            JSONObject result = executeDatabaseOper(cosnode, mode, standby);
	            rsp.setMessage(result.getString("result"));
				rsp.setSucceed(true);
				rsp.setResult(result.toString());
				super.logoper(rsp.getMessage(), "系统管理", result.toString(4), null);
			}
			else{
	            rsp.setMessage("伺服器【"+ip+":"+port+"】主控未能正常工作.");
			}
		}
		catch(Exception e)
		{
			log.error("Failed to set the status of database ", e);
			rsp.setMessage("执行主数据库操作出现异常("+e+").");
		}
        finally
        {
        	if( datagramSocket != null )
        	{
        		datagramSocket.close();
        	}
        }
		return rsp;
	}
	
	/**
	 * 得到本机的数据库配置
	 * @return
	 * @throws Exception
	 */
	public JSONObject getDatabaseConfig()
		throws Exception
	{
		JSONObject dbcfg = new JSONObject();
		try {
			String ip = Tools.getLocalIP();
			int port = COSConfig.getLocalControlPort();
			RunFetchMonitor local = MonitorMgr.getTracker().getRunFetchMonitor(ip, port);
			if( local != null ){
				if( local.getSysDesc() != null ){
					String h2json = local.getSysDesc().getPropertyValue("h2");
					if( h2json != null & !h2json.isEmpty() ){
						try{
							JSONObject e0 = new JSONObject(h2json);
							dbcfg.put(e0.getString("type"), e0);
							e0.put("cosworking", true);
							e0.put("cosaddr", local.toString());
							e0.put("ip", local.getIp());
							e0.put("cid", local.getServerid());
							e0.put("name", local.getSysDesc().getDescript());
							e0.put("length", e0.getLong("size"));
							e0.put("size", Kit.bytesScale(e0.getLong("size")));
							if( !local.isConnect() ){
								e0.put("working", false);
								e0.put("status", "主控引擎已经关闭");
								e0.put("cosworking", false);
							}
							if( !e0.has("starttime") ){
								e0.put("starttime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", local.getStartuptime()));
							}
							String h2Standby = e0.getString("standby");
							RunFetchMonitor standby = MonitorMgr.getTracker().getRunFetchMonitorByDatabase(h2Standby);
							if( standby != null && standby.getSysDesc() != null ){
								JSONObject e1 = getStandbyConfig(standby);
								dbcfg.put(e1.getString("type"), e1);
							}
							else if( standby == null ){
								standby = MonitorMgr.getTracker().getRunFetchMonitorByStandbyDatabase(e0.getString("ip")+":"+e0.getInt("port"));
								JSONObject e1 = getStandbyConfig(standby);
								if( e1 != null ){
									dbcfg.put(e1.getString("type"), e1);
									e0.put("standby", e1.getString("ip")+":"+e1.getInt("port"));
								}
							}
							if( !local.isConnect() ){
								e0.put("working", false);
							}
						}
						catch(Exception e){
							throw new Exception("加载伺服器【"+ip+":"+port+"】的主数据库配置("+h2json+")失败，因为"+e);
						}
					}
					else{
						throw new Exception("加载伺服器【"+ip+":"+port+"】的主数据库配置失败，因为您的主控引擎版本【"+local.getSysDesc().getControlVersion()+"】不支持。");
					}
				}
				else{
					throw new Exception("加载伺服器【"+ip+":"+port+"】的主数据库配置失败，因为未能同步监控数据");
				}
			}
			else{
				throw new Exception("没能加载伺服器【"+ip+":"+port+"】的主数据库配置，可能是因为主控引擎服务没有正确启动。");
			}
		} catch (Exception e) {
			log.error("Failed to get the config of database.", e);
			throw new Exception("加载伺服器主数据库配置失败，因为"+e);
		}
		return dbcfg;
	}
	
	/**
	 * 设置备份数据库配置
	 * @param standby
	 * @param dbcfg
	 */
	private JSONObject getStandbyConfig(RunFetchMonitor standby){
		if( standby != null && standby.getSysDesc() != null ){
			String h2json = standby.getSysDesc().getPropertyValue("h2");
			JSONObject e1 = new JSONObject(h2json);
			e1.put("cosworking", true);
			e1.put("cosaddr", standby.toString());
			e1.put("ip", standby.getIp());
			e1.put("cid", standby.getServerid());
			e1.put("name", standby.getSysDesc().getDescript());
			e1.put("ip", standby.getIp());
			e1.put("length", e1.getLong("size"));
			e1.put("size", Kit.bytesScale(e1.getLong("size")));
			if( !e1.has("starttime") ){
				e1.put("starttime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", standby.getStartuptime()));
			}
			if( !standby.isConnect() ){
				e1.put("working", false);
				e1.put("status", "主控引擎已经关闭");
				e1.put("cosworking", false);
			}
			return e1;
		}
		return null;
	}
	
	public String modifyProperty(String key, String value, String label)
	{
		return modifyProperty(config, id, key, value, label);
	}
	
	/**
	 * 
	 * @return
	 */
	public static JSONObject getConfig()
	{
		if( config != null ) return config;
		return config = getConfig(id);
	}
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static String get(String key)
	{
		if( config == null ) getConfig();
		if( config != null )
		{
			return config.has(key)?config.getString(key):"";
		}
		return "";
	}
	/**
	 * 改变
	 */
	public void changeSkin(String skinName)
	{
		try
		{
			Skin.changeSkin(skinName);
			File portalFile = new File(PathFactory.getWebappPath(), "portal.jsp");
			if( portalFile.exists() )
			{
				portalFile.setLastModified(System.currentTimeMillis());
			}
		}
		catch(Exception e)
		{
			log.error("Failed to change skin:", e);
		}
	}
}
