package com.focus.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.web.ops.vo.ModuleTrack;
import com.focus.util.Tools;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class SystemPerf extends HostPerf
{
	private static final long serialVersionUID = 6515652042805661329L;
	private String descript;// 系统描述
	private String hostName;// 主机名称
	private HashMap<String, Object> properties = new HashMap<String, Object>();
//	private long swapSpaceSize;//增加交换空间虚拟内存
	
	/**
	 * 得到安全钥匙
	 */
	public String getSecurityKey()
	{
		if( this.properties.containsKey("key") )
		{
			return this.properties.get("key").toString();
		}
		return null;
	}
	/**
	 * 得到安全钥匙
	 */
	public String getServerid()
	{
		String key = this.getSecurityKey();
		return key!=null?Base64.encode(key.getBytes()):null;
	}
	/**
	 * 得到安全钥匙
	 */
	public void setSecurityKey(String value)
	{
		this.properties.put("key", value);
	}

	public void setProperty(String name, Object value)
	{
		this.properties.put(name, value);
	}

	public boolean existProperty(String name)
	{
		return this.properties.containsKey(name);
	}

	public Object getProperty(String name)
	{
		return this.properties.get(name);
	}

	public String getPropertyValue(String name)
	{
		Object v = properties.get(name);
		if (v == null)
		{
			v = "";
		}
		return v.toString();
	}

	public String getHostName()
	{
		return hostName;
	}

	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	public String getDescript()
	{
		return descript;
	}

	public void setDescript(String descript)
	{
		this.descript = descript;
	}

	public HashMap<String, Object> getProperties()
	{
		return properties;
	}
	
	/**
	 * 磁阵信息
	 * @return
	 */
	public String getStoragesInfo()
	{
		Object obj = getProperty("Storage");
		if (null != obj && obj instanceof ArrayList<?>)
		{
			List<?> storages = (ArrayList<?>) obj;
			if( !storages.isEmpty() )
			{
				StringBuffer buffer = new StringBuffer();
				for(Object e : storages)
				{
					Storage storage = (Storage)e;
					buffer.append("[" + storage.getAddress() + " ");
					buffer.append(storage.getAbleSpace() + "G/" + storage.getTotalSpace() + "G ");
					buffer.append(storage.getPercent() + "%]");
				}
				return buffer.toString();
			}
		}
		return ""; 
	}
	/**
	 * 内存使用信息
	 * @return
	 */
	public String getPhyMemUsageDetail()
	{
		long used = getPhyMemUsed();
		long total = super.getPhyMemUsage()>0?(used*10000/super.getPhyMemUsage()):0;//已使用的物理内存/可用的物理内存
		String swapSpaceSize = "";
		String cachedSzie = "";
		if( properties.containsKey("SwapSpaceSize") )
		{
			long l = (Long)properties.get("SwapSpaceSize");
			swapSpaceSize = " "+getShowSpace(l)+"(SwapSpace)";
		}
		if( properties.containsKey("Cached") )
		{
			long l = (Long)properties.get("Cached");
			cachedSzie = " "+getShowSpace(l)+"(Cached)";
		}
		if( this.getPhyMemTotal() > 0 ) total = this.getPhyMemTotal();
		return getShowSpace(used)+"/"+getShowSpace(total)+cachedSzie+swapSpaceSize; 
	}
	/**
	 * 磁盘使用信息
	 */
	public String getDiskUsageDetail()
	{
		long space = getDiskUsed();
		long total = space*10000/(10000-super.getDiskUsage());
		long used = total - space;
		return getShowSpace(used)+"/"+getShowSpace(total); 
	}

	/**
	 * 网络负载
	 */
	public String getNetLoad()
	{
		Long iload = (Long)getProperty("NetIOLoad0");
		Long oload = (Long)getProperty("NetIOLoad1");
		return "I("+(iload!=null?getShowSpace(iload):"...")+")/O("+(oload!=null?getShowSpace(oload):"...")+")"; 
	}

	/**
	 * IO负载
	 */
	public String getIOLoad()
	{
		Long iload = (Long)getProperty("IOLoadrs");
		Long oload = (Long)getProperty("IOLoadws");
		return "I("+(iload!=null?getShowSpace(iload):"...")+")/O("+(oload!=null?getShowSpace(oload):"...")+")"; 
	}
	
	public static String getShowSpace(long size)
	{
        if( size < 1024 )
        {
        	return size+"B";
        }
        else if( size < 1024*1024 )
        {
        	size = size/1024;
        	return Tools.DF.format(size)+"K";
        }
        else if( size < 1024*1024*1024 )
        {
        	return Tools.DF.format(size/(1024*1024))+"M";
        }
        else
        {
        	return Tools.DF.format(size/(1024*1024*1024))+"G";
        }
	}
	
	public String getControlVersion()
	{
		String version = (String)this.properties.get("cos.control.version");
		if( version == null || version.isEmpty() ) return "1.13.4.12";
		return version;
	}
	
	/**
	 * 返回数据库状态
	 * @return
	 */
	public String getDatabaseStates()
	{
		String d = this.getPropertyValue("Database");
		StringBuffer str = new StringBuffer("<table width='100%'>");
		if( d.isEmpty() )
		{
			str.append("<tr><td align='center' style='color:#ccc'>没有数据库监控信息</td></tr></table>");
			return str.toString();
		}
		if( !d.startsWith("[") )
		{
			return getPropertyValue("Database")+"/"+getPropertyValue("DatabaseException");
		}
//		str.append("<tr>");
//		str.append("<td width='64' class='skit_table_head'>数据库类型</td>");
//		str.append("<td width='160' class='skit_table_head'>数据库描述</td>");
//		str.append("<td width='320' class='skit_table_head'>JDBC</td>");
//		str.append("<td class='skit_table_head'>状态</td>");
//		str.append("</tr>");
		try
		{
			JSONArray array = new JSONArray(d);
			for(int i = 0; i < array.length(); i++)
			{
				JSONObject json = array.getJSONObject(i);
				String type = json.getString("jdbc.driver");
				if( type.indexOf("mysql") != -1 ) type = "mysql";
				else if( type.indexOf("sybase") != -1 ) type = "sybase";
				else if( type.indexOf("oracle") != -1 ) type = "oracle";
				else if( type.indexOf("h2") != -1 ) type = "h2";
				String name = json.getString("jdbc.name");
				String jdbc = json.getString("jdbc.url");
				String status = "未知状态";
				String icon = "skit_fa_icon_gray";
				if( json.has("Status") && json.getInt("Status") == 1){
//					StringBuffer sb = new StringBuffer();
//					Iterator<String> itr = json.keys();
//					while(itr.hasNext())
//					{
//						String key = itr.next();
//						String val = json.get(key).toString();
//						sb.append(key+": "+val+"\r\");
//					}
					status = "<a href='javascript:showDatabaseStatus("+i+", \"显示"+type+"数据库【"+name+"】状态\");'>正常</a>&nbsp;";
					status += "<a href='javascript:openDatabase("+i+", \""+type+"数据库【"+name+"】查询分析器\");'>查询分析器</a>";
					icon += "skit_fa_icon_blue";
				}
				if( json.has("Status") && json.getInt("Status") == 0 && json.has("Exception") ){
					status = "<span style='color:red'>"+json.getString("Exception")+"</span>";
					icon = "skit_fa_icon_red";
				}
				str.append("<tr><td align='center' width='30'><i class='"+icon+" fa fa-link'></i></td>");
				str.append("<td width='480'><span class='databasetitle'>"+name+"("+jdbc+")</span></td>");
				str.append("<td>"+status+"</td>");
				str.append("<td style='display:none' id='database"+i+"'>"+json+"</td></tr>");
				str.append("</tr>");
			}
		}
		catch(Exception e)
		{
		}
		str.append("</table>");
		return str.toString();
	}

	public int getCpuState()
	{
		if( !properties.containsKey("Running") ) return 0;
		//System.err.println(Tools.getFormatTime("yyyy-MM-dd HH:mm", getPerfTime().getTime()));
		if(getCpuLoad() > 8500 )
		{
			return 3;
		}
		if(getCpuLoad() > 6000)
		{
			return 2;
		}
		return 1;
	}

	public int getProgramState()
	{
		if( !properties.containsKey("Running") ) return 0;
		int dead = 0;//死进程数量
		int exception = 0;//错误进程数量
		if( properties.containsKey("DeadProgram") ){
			dead = (int)properties.get("DeadProgram");
		}
		if( properties.containsKey("ExceptionProgram") ){
			exception = (int)properties.get("ExceptionProgram");
		}
		if( dead > 0 ) return 3;
		if( exception > 0 ) return 2;
		return 1;
	}

	public String getProgramInfo()
	{
		if( !properties.containsKey("Running") ) return "伺服监控已停止";
		int dead = 0;//死进程数量
		int exception = 0;//错误进程数量
		if( properties.containsKey("DeadProgram") ){
			dead = (int)properties.get("DeadProgram");
		}
		if( properties.containsKey("ExceptionProgram") ){
			exception = (int)properties.get("ExceptionProgram");
		}
		StringBuffer sb = new StringBuffer();
		if( dead > 0 ){
			sb.append(dead+"个程序疑似死进程");
		}
		if( exception > 0 ){
			if( sb.length()>0){
				sb.append(",");
			}
			sb.append(exception+"个程序有异常");
		}
		return sb.length()>0?sb.toString():"程序运行正常";
	}
	//内存状态
	public int getMemoryState()
	{
		if( !properties.containsKey("Running") ) return 0;
		if(getPhyMemUsage() > 8500 )
		{
			return 3;
		}
		if(getPhyMemUsage() > 6000)
		{
			return 2;
		}
		return 1;
	}
	
	/**
	 * 得到PING检测
	 * @return
	 */
	public String getPingState(){
		Object obj = getProperty("PING");
		int count = 0;
		if( obj != null )
		{
			ArrayList<?> array = (ArrayList<?>)obj;
			for(Object e : array )
			{
				String info = "";
				if( e instanceof String )
					info = e.toString().toLowerCase();
				count += (info.indexOf(" 0% packet loss") == -1 && info.indexOf("lost = 0") == -1 )?1:0;
			}
		}
		if( count > 0 ){
			return "PING检测发生"+count+"次丢包";
		}
		return "PING检测正常";
	}
	//网络状态
	public int getNetState()
	{
		if( !properties.containsKey("Running") ) return 0;
		Object obj = getProperty("PING");
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
				return 3;
			}
			else if( count > 0 )
			{//有一个就警告
				return 2;
			}
		}
		Object NetIOLoad0 = this.getProperty("NetIOLoad0");
		Object NetIOLoad1 = this.getProperty("NetIOLoad1");
		if( NetIOLoad0 == null || NetIOLoad1 == null ){
			return 0;
		}
		return 1;
	}
	
	//磁盘状态
	public int getDiskState()
	{
		if( !properties.containsKey("Running") ) return 0;
		int storageUsage = 0;
		Object obj = getProperty("StorageUsage");
		if (null != obj)
		{
			storageUsage = Integer.parseInt(obj.toString());
		}
		if(getDiskUsage() > 8500 || storageUsage > 8500)
		{
			return 3;
		}
		if(getDiskUsage() > 6000 || storageUsage > 6000)
		{
			return 2;
		}
		if(getDiskUsage() > 0 || storageUsage > 0)
		{
			return 1;
		}
		return 0;
	}

	
	//其它接口
	public int getDatabaseState()
	{
		if( !properties.containsKey("Running") ) return 0;
		String d = getPropertyValue("Database");
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
							return 3;
						}
//						else if("connecting".equals(d) )
//						{
//							return "images/icons/yellow.gif";
//						}
					}
				}
				if( array.length() > 0 ) return 1;
			}
			catch(Exception e)
			{
			}
			return 0;
		}
		
		if("disconnect".equals(d) )
		{
			return 3;
		}
		else if("connecting".equals(d) )
		{
			return 2;
		}
		else if("connect".equals(d) )
		{
			return 1;
		}
		return 0;
	}

	public int getZookeeperState()
	{
		if( !properties.containsKey("Running") ) return 0;
		ModuleTrack module = (ModuleTrack)getProperty("Zookeeper");
		if( module != null && module.getState() == 1 )
		{
			if( System.currentTimeMillis() - module.getStatupTime().getTime() < Tools.MILLI_OF_MINUTE )
				return 2;
			else
				return 1;
		}
		else if( module == null )
		{
			return 0;
		}
		else
			return 3;
	}
	

	public int getCoswsState()
	{
		if( !properties.containsKey("Running") ) return 0;
		String d = getPropertyValue("Cosws");
		if( !d.startsWith("{") )
		{
			return 0;
		}
		try
		{
			JSONObject cosws = new JSONObject(d);
			if( cosws.has("state") && cosws.getInt("state") == 1)
			{
				return 1;
			}
			return 3;
		}
		catch(Exception e)
		{
			return 1;
		}
	}
	
	public void loadModuleTrack(ArrayList<ModuleTrack> modules)
	{
		Object obj = this.getProperty("ModuleTrack");
		if( obj != null && obj instanceof ArrayList<?> )
		{
			ArrayList<?> list = (ArrayList<?>)obj;
			for(Object o : list)
			{
				ModuleTrack track = (ModuleTrack)o;
				modules.add(track);
			}
		}
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer("State system-perf("+this.getSecurityKey()+")");
		sb.append("\r\n\tcpu: "+this.getCpuState());
		sb.append("\r\n\tmem: "+this.getMemoryState());
		sb.append("\r\n\tdisk: "+this.getDiskState());
		sb.append("\r\n\tnet: "+this.getNetState());
		sb.append("\r\n\tzk: "+this.getZookeeperState());
		sb.append("\r\n\tdatabase: "+this.getDatabaseState());
		sb.append("\r\n\tws: "+this.getCoswsState());
		return sb.toString();
	}
}
