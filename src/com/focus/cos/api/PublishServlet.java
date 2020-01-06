package com.focus.cos.api;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;

import com.focus.cos.CosServer;
import com.focus.cos.control.COSApi;
import com.focus.cos.control.ProgramLoader;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

public class PublishServlet extends AbaseServlet
{
	private static final long serialVersionUID = -4767791262424774539L;
	public PublishServlet(COSApi server) throws Exception
	{
		super(server);
	}

	@Override
	public void query(HttpServletRequest request, HttpServletResponse response, StringBuffer log) throws Exception 
	{
		response.setStatus(404);
		OutputStream out = response.getOutputStream();
		out.write("请求的服务并不存在".getBytes());
		out.flush();
		out.close();
		log.append("\r\n\t404 not found service.");
		
//		String method = request.getHeader("method");
//		String ci = Tools.encodeMD5(request.getHeader("COS-ID"));
//		log.append("\r\n\tmethod: "+method+",ci="+ci);
//		if( method == null || method.isEmpty() )
//		{
//			log.append("\tunknown request.");
//			unknown(request);
//			return;
//		}
//		if( "remove".equals(method) )
//		{
//			ZooKeeper zookeeper = server.getZooKeeper();
//			JSONObject result = new JSONObject();
//			try
//			{
//				String programId = request.getHeader("Program-ID");
//				String remark = request.getHeader("Remark");
//				String operuser = request.getHeader("Operuser");
//				log.append("\r\n\tid: "+programId+",operuser: "+operuser+", remark: "+remark);
//				String zkpath0 = "/cos/temp/program/publish/"+ci+":"+programId;
////				String zkpath1 = "/cos/config/program/"+ci+"/publish/"+programId;
////				Stat stat1 = zookeeper.exists(zkpath1, false);
////				if( stat1 != null )
////				{
////					log.append("\r\n\tFound the config from "+zkpath1);
////					JSONObject config = new JSONObject(new String(zookeeper.getData(zkpath1, false, stat1), "UTF-8"));
////					config.put("oper", ProgramLoader.OPER_DELETING);//
////					config.put("operlog", remark);
////					config.put("operuser", operuser);
////					Stat stat = zookeeper.exists(zkpath0, false);
////					if( stat != null )
////					{
////						result.put("status", 0);
////						result.put("remark", "重置删除指定程序配置申请审核");
////						log.append("\r\n\tChange oper(del) to "+zkpath0);
////						zookeeper.setData(zkpath0, config.toString().getBytes("UTF-8"), stat.getVersion());
////					}
////					else
////					{
////						result.put("status", 1);
////						result.put("remark", "添加删除指定程序配置申请审核");
////						log.append("\r\n\tAdd oper(del) to "+zkpath0);
////						COSApi.createNode(zookeeper, zkpath0, config.toString().getBytes("UTF-8"));
////					}
////				}
////				else
////				{
////					log.append("\r\n\tNot found the config from "+zkpath1);
//					Stat stat = zookeeper.exists(zkpath0, false);
//					if( stat != null )
//					{
//						JSONObject config = new JSONObject(new String(zookeeper.getData(zkpath0, false, stat), "UTF-8"));
//						if( config.has("oper") )
//						{
//							if( config.getInt("oper") == ProgramLoader.OPER_ADDING )
//							{
//								zookeeper.delete(zkpath0, stat.getVersion());
//								log.append("\r\n\tDelete oper "+zkpath0);
//								result.put("status", 3);
//								result.put("remark", "因为程序配置是新增直接删除申请记录");
//							}
//							else if( config.getInt("oper") == ProgramLoader.OPER_EDITING )
//							{
//								config.put("oper", ProgramLoader.OPER_DELETING);//
//								config.put("operlog", remark);
//								config.put("operuser", operuser);
//								zookeeper.setData(zkpath0, config.toString().getBytes("UTF-8"), stat.getVersion());
//								log.append("\r\n\tChange the oper from editing to delete for "+zkpath0);
//								result.put("status", 3);
//								result.put("remark", "因为程序配置是修改状态将状态改为删除");
//							}
//						}
//					}
//					else
//					{
//						COSApi.createNode(zookeeper, zkpath0, config.toString().getBytes("UTF-8"));
//						
//						log.append("\r\n\tNot found the temp of prgram from "+zkpath0);
//						result.put("status", 2);
//						result.put("remark", "程序配置和审核申请都不存在");
//					}
////				}
//				super.write(ci, response, result);
//			}
//			catch(Exception e)
//			{
//				Log.err("", e);
//				result.put("status", -1);
//				result.put("remark", "出现异常"+e.getMessage());
//				super.write(ci, response, result);
//			}
//		}
//		else
//		{
//			unknown(request);
//		}
	}

	@Override
	public synchronized void save(HttpServletRequest request, byte[] payload, JSONObject response) throws Exception  
	{
		Zookeeper zookeeper = server.getZookeeper();
		String from = request.getParameter("from");
		String ci = COSApi.getRequestValue(request, "COS-ID");
		Syslog log = new Syslog();
		log.setLogtype(LogType.运行日志.getValue());
		log.setAccount(from);
		log.setCategory("集群程序管理");
		log.setLogtext("程序["+from+"]从客户端["+request.getRemoteAddr()+"/"+getIp(request)+"]发送来修改程序配置的请求");

		String json = new String(payload, "UTF-8");
		JSONObject config = new JSONObject(json);
		if( !config.has("ip") || !config.has("port") )
		{
			response.put("error", "Not config the ip or port of publish to the program.");
			return;
		}
		if( !config.has("oper") )
		{
			config.put("oper", ProgramLoader.OPER_ADDING);
			config.put("operlog", "伺服器["+config.getString("ip")+"]通过接口提交程序发布审批");
			config.put("operuser", from);
		}
		if( !config.has("serverkey") )
		{
			String serverkey = Tools.encodeMD5(ci);
			config.put("serverkey", serverkey);
		}
		String serverkey = config.getString("serverkey");
    	config.put("opertime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
		String zkpath = "/cos/temp/program/publish/"+serverkey+":"+config.getString("id");
		Stat stat = zookeeper.exists(zkpath, false);
		if( stat != null )
		{
			JSONObject old = new JSONObject(new String(zookeeper.getData(zkpath, false, stat), "UTF-8"));
			config.put("oper", old.getInt("oper"));
			zookeeper.setData(zkpath, config.toString().getBytes("UTF-8"), stat.getVersion());
			response.put("result", "覆盖申请程序配置数据"+zkpath+", 操作码是"+old.getInt("oper")+", 备注: "+config.getString("operlog"));
		}
		else
		{
			CosServer.createNode(zookeeper, zkpath, config.toString().getBytes("UTF-8"));
			response.put("result", "新增申请程序配置数据"+zkpath+", 操作码是"+config.getInt("oper")+", 申请发起程序是"+config.getString("operuser")+", "+config.getString("operlog"));
		}
		log.setContext(config.toString(4));
		log.setLogseverity(LogSeverity.INFO.getValue());
		server.write(log);

		Sysnotify notify = new Sysnotify();
		notify.setFilter("集群程序管理");
		notify.setTitle(config.getString("operlog"));
		notify.setNotifytime(new Date());
		notify.setPriority(0);
		notify.setContext("收到客户端["+request.getRemoteAddr()+"/"+getIp(request)+"]发送来的修改了程序配置请求，该配置参数生效需要系统管理员进行审核发布。"+
			"\r\n"+config.toString(4));
		notify.setAction("程序管理");
		notify.setActionlink("control!navigate.action");
		ArrayList<Sysuser> users = SysuserClient.listUser(1, -1, Status.Enable.getValue());
		for(Sysuser user : users)
		{
			notify.setUseraccount(user.getUsername());
			server.write(notify);
		}
	}
}
