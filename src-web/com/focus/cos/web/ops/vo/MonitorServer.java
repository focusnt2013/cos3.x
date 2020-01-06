package com.focus.cos.web.ops.vo;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.control.ModulePerf;
import com.focus.control.SystemPerf;
import com.focus.cos.control.Module;
import com.focus.cos.web.ops.service.Monitor.RunFetchMonitor;
import com.focus.util.Tools;

/**
 * Description:系统监控显示指标
 * Create Date:2009-11-24
 * Modify Date:2016-10-03
 * @author Focus
 *
 * @since 1.2
 */
public class MonitorServer
{
	protected String id;
	private SystemPerf perf;
	private RunFetchMonitor runner;
	private boolean expired;	//过期
	private String ip;
	private int port;
	/*模块程序的状态*/
	private ArrayList<ModulePerf> modulePerfs = new ArrayList<ModulePerf>();

	public MonitorServer(RunFetchMonitor runner)
	{
		if( runner == null) return;
		this.id = String.valueOf(runner.getServerid());
		this.perf = runner.getSysDesc();
		this.ip = runner.getIp();
		this.port = runner.getPort();
		this.runner = runner;
		this.expired = !runner.isConnect();//监控的连接是否还正常，否则就算超时
		this.modulePerfs = runner.getModulePerfs();
	}
	
	/**
	 * 程序状态
	 * @return
	 */
	public String getProgramState(){
		if( modulePerfs.isEmpty() ){
			return "images/icons/gray.png";
		}
		int state = perf.getProgramState();
		if( state == 3 ){
			return "images/icons/danger.gif";
		}
		if( state == 2 ){
			return "images/icons/yellow.png";
		}
		if( state == 1 ){
			return "images/icons/green.png";
		}
		return "images/icons/gray.png";
	}
	
	//CPU状态
	public String getCpuState()
	{
		if(this.isExpired())
		{
			return "images/icons/gray.png";
		}
		
		if(perf == null)
		{
			return "images/icons/danger.gif";
		}
		if(perf.getCpuLoad() > 8500 )
		{
			return "images/icons/danger.gif";
		}
		if(perf.getCpuLoad() > 6000)
		{
			return "images/icons/yellow.png";
		}
		return "images/icons/green.png";
	}
	//内存状态
	public String getMemoryState()
	{
		if(this.isExpired())
		{
			return "images/icons/gray.png";
		}
		
		if(perf == null)
		{
			return "images/icons/danger.gif";
		}
		if(perf.getPhyMemUsage() > 8500 )
		{
			return "images/icons/danger.gif";
		}
		if(perf.getPhyMemUsage() > 6000)
		{
			return "images/icons/yellow.png";
		}
		return "images/icons/green.png";
	}
	//网络状态
	public String getNetState()
	{
		if(this.isExpired())
		{
			return "images/icons/gray.png";
		}
		
		if(perf == null)
		{
			return "images/icons/danger.gif";
		}
		Object obj = perf.getProperty("PING");
		if( obj != null )
		{
			int count = 0;
			ArrayList<?> array = (ArrayList<?>)obj;
			for(Object e : array )
			{
				String info = "";
				if( e instanceof String )
					info = e.toString().toLowerCase();
				count += (info.indexOf(" 0% packet loss") == -1 && info.indexOf("lost = 0") == -1 )?1:0;
			}
			if( count != 0 && count == array.size() )
			{//如果全部都没有ping通，那么就表示网络都不通
				return "images/icons/danger.gif";
			}
			else if( count > 0 )
			{//有一个就警告
				return "images/icons/yellow.png";
			}
		}
		else{
			Object NetIOLoad0 = perf.getProperty("NetIOLoad0");
			Object NetIOLoad1 = perf.getProperty("NetIOLoad1");
			if( NetIOLoad0 == null || NetIOLoad1 == null ){
				return "images/icons/gray.png";
			}
		}
		return "images/icons/green.png";
	}
	
	//磁盘状态
	public String getDiskState()
	{
		if(this.isExpired())
		{
			return "images/icons/gray.png";
		}
		if(perf == null)
		{
			return "images/icons/danger.gif";
		}
		int storageUsage = 0;
		Object obj = perf.getProperty("StorageUsage");
		if (null != obj)
		{
			storageUsage = Integer.parseInt(obj.toString());
		}
		if(perf.getDiskUsage() == 0 || storageUsage == 0)
		{
			return "images/icons/gray.png";
		}
		if(perf.getDiskUsage() > 8500 || storageUsage > 8500)
		{
			return "images/icons/danger.gif";
		}
		if(perf.getDiskUsage() > 6000 || storageUsage > 6000)
		{
			return "images/icons/yellow.png";
		}
		return "images/icons/green.png";
	}
	
	//其它接口
	public String getDatabaseState()
	{
		if(this.isExpired())
		{
			return "images/icons/gray.png";
		}
		
		if(perf == null)
		{
			return "images/icons/danger.gif";
		}
		String d = perf.getPropertyValue("Database");
		if( d.startsWith("[") )
		{
			try
			{
				JSONArray array = new JSONArray(d);
				for(int i = 0; i < array.length(); i++)
				{
					JSONObject jdbc = array.getJSONObject(i);
					if( jdbc.has("Status") )
					{
						if( jdbc.getInt("Status") == 0)
						{
							return "images/icons/danger.gif";
						}
//						else if("connecting".equals(d) )
//						{
//							return "images/icons/yellow.png";
//						}
					}
				}
				if( array.length() > 0 ) return "images/icons/green.png";
			}
			catch(Exception e)
			{
			}
			return "images/icons/gray.png";
		}
		
		if("disconnect".equals(d) )
		{
			return "images/icons/danger.gif";
		}
		else if("connecting".equals(d) )
		{
			return "images/icons/yellow.png";
		}
		else if("connect".equals(d) )
		{
			return "images/icons/green.png";
		}
		return "images/icons/gray.png";
	}
	
	//运行情况
	public String getRunState()
	{
        int countRun = 0;
        for(ModulePerf module : modulePerfs )
        {
    		countRun += module.getState() == Module.STATE_STARTUP?1:0;
        }
        return countRun+"/"+modulePerfs.size();
	}
	
	public String getZookeeperState()
	{
		if(this.isExpired())
		{
			return "images/icons/gray.png";
		}
		
		if( runner == null ){
			return "images/icons/gray.png";
		}
		
		for(ModulePerf module : runner.getModulePerfs())
		{
			if( module.getId().equals("Zookeeper") )
			{
				if( module.getState() == 1 )
				{
					if( System.currentTimeMillis() - module.getStartupDate().getTime() < Tools.MILLI_OF_MINUTE )
						return "images/icons/yellow.png";
					else
						return "images/icons/green.png";
				}
				else
					return "images/icons/danger.gif";
			}
		}
		return "images/icons/gray.png";
	}
	
	public String getZookeeperMyid()
	{
		if( runner == null ) return "";
		if( perf == null ) return "";
		String value = perf.getPropertyValue("zookeeper.myid");
		return value.isEmpty()?"--":value;
	}

	public String getZookeeperServers()
	{
		if( runner == null ) return "";
		if( perf == null ) return "";
		String[] servers = Tools.split(perf.getPropertyValue("zookeeper.servers"), ",");
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < servers.length; i++ )
		{
			String ip = servers[i];
			if( ip.equals(this.ip) )
			{
				sb.append("<span style='color:red;font-weight:bold;'>");
				sb.append(ip);
				sb.append("(myid="+(i)+")");
				sb.append("</span>");
			}
			else
			{
				sb.append("<span>");
				sb.append(ip);
				sb.append("(myid="+(i)+")");
				sb.append("</span>");
			}
			sb.append("<br>");
		}
		return sb.toString();
	}
	public String getZookeeperMd5()
	{
		if( runner == null ) return "";
		if( perf == null ) return "";
		return Tools.encodeMD5(perf.getPropertyValue("zookeeper.servers"));
	}

	public String getCoswsState()
	{
		if( runner == null ) return "";
		if(this.isExpired())
		{
			return "images/icons/gray.png";
		}
		
		if(perf == null)
		{
			return "images/icons/danger.gif";
		}
		String d = perf.getPropertyValue("Cosws");
		if( !d.startsWith("{") )
		{
			return "images/icons/gray.png";
		}
		try
		{
			JSONObject cosws = new JSONObject(d);
			if( cosws.has("state") && cosws.getInt("state") == 1)
			{
				return "images/icons/green.png";
			}
			return "images/icons/danger.gif";
		}
		catch(Exception e)
		{
			return "images/icons/yellow.png";
		}
	}
	
	public boolean isExpired()
	{
		return expired;
	}

	public String getLastUpdateTime()
	{
		if( perf == null ) return "";
		return Tools.getFormatTime("MM-dd HH:mm:ss", this.perf.getPerfTime().getTime());
	}

	public String getId()
	{
		return id;
	}

	public String getName()
	{
		if( perf == null ) return "无法建立与该服务器的监控连接，需检查相关网络配置";
		return perf.getDescript();
	}

	public String getIp()
	{
		return ip;
	}

	public int getPort()
	{
		return this.port;
	}
	
	public String getSecurityKey()
	{
		if( perf == null ) return "";
		return perf.getSecurityKey();
	}

	public SystemPerf theSystemPerf() 
	{
		return perf;
	}
}
