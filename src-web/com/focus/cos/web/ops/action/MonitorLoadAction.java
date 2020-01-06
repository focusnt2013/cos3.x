package com.focus.cos.web.ops.action;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.control.ModuleMemeory;
import com.focus.control.ModulePerf;
import com.focus.cos.control.Command;
import com.focus.cos.control.MonitorGCHistory;
import com.focus.cos.web.ops.service.Monitor.RunFetchMonitor;
import com.focus.cos.web.ops.vo.ChartDataset;
import com.focus.cos.web.ops.vo.MonitorServer;
import com.focus.util.Tools;

public class MonitorLoadAction extends OpsAction
{
	private static final long serialVersionUID = 3912754392617510563L;
	private static final Log log = LogFactory.getLog(MonitorLoadAction.class);

	private ArrayList<ChartDataset> clusterloadDatasets = null;
	private int command = -1;
	
	public String resetflow()
	{
		int count = getMonitorMgr().getMonitor().resetMonitorFlowInof();
		super.setResponseMessage(count+"服务器监控器流量数据清零。");
		return "alert";
	}
	/**
	 * 监控负载流量汇总统计表查看
	 * @return
	 */
	public String runnerflow()
	{
		localDataArray = new JSONArray();
		getMonitorMgr().getMonitor().loadMonitorFlowInfo(localDataArray);
		if( localDataArray.length() > 0 )
		{
//			System.err.println(localDataArray.toString(4));
			this.summaryObject = (JSONObject)localDataArray.remove(localDataArray.length()-1);
		}
//		this.localData = localDataArray.toString();
		return super.grid("/grid/local/sysrunnerflow.xml");
//		try
//		{
    		/*StringBuffer colM = new StringBuffer();
    		colM.append("[");
    		colM.append("\r\n\t{ title: '主机名称', dataIndx: 'host', width: 120");
			colM.append(",dataType: 'string', filter: { type: 'textbox', condition: 'contain', listeners: ['change']}");
			colM.append("},");
    		colM.append("\r\n\t{ title: 'COS版本', dataIndx: 'version', dataType: 'string', width: 80");
			colM.append(",dataType: 'string', filter: { type: 'textbox', condition: 'contain', listeners: ['change']}");
			colM.append("},");
    		colM.append("\r\n\t{ title: '监控启动时间', dataIndx: 'startuptime', width: 128,dataType: 'string', align: 'center'}");

        	colM.append(",\r\n\t{ title: '整个监控周期', align: 'center', editable: false");
        	colM.append(", colModel: [");
    		colM.append("\r\n\t\t{ title: '数据', dataIndx: 'totalsize', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '消息', dataIndx: 'totalcount', dataType: 'int', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '时间', dataIndx: 'duration', dataType: 'string', align: 'center', width: 80},");
            colM.append("\r\n\t\t{ title: '流量', dataIndx: 'avgload', dataType: 'string', align: 'center', width: 80}");
        	colM.append("\r\n\t]}");

        	colM.append(",\r\n\t{ title: '当前采集周期', align: 'center', editable: false");
        	colM.append(", colModel: [");
    		colM.append("\r\n\t\t{ title: '数据', dataIndx: 'currsize', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '消息', dataIndx: 'currcount', dataType: 'int', align: 'center', width: 48},");
            colM.append("\r\n\t\t{ title: '时间', dataIndx: 'period', dataType: 'string', align: 'center', width: 80},");
            colM.append("\r\n\t\t{ title: '流量', dataIndx: 'load', dataType: 'string', align: 'center', width: 80}");
        	colM.append("\r\n\t]}");

    		colM.append(",\r\n\t{ title: '上次心跳', dataIndx: 'hearttime', width: 64, dataType: 'string'}");
    		colM.append(",\r\n\t{ title: '异常', dataIndx: 'errorcount', width: 32, align: 'center', dataType: 'int'}");
    		colM.append(",\r\n\t{ title: '主机名称', dataIndx: 'name', dataType: 'string', width: 128");
    		colM.append(",dataType: 'string', filter: { type: 'textbox', condition: 'contain', listeners: ['change']}");
			colM.append("}");
			
        	colM.append(",\r\n\t{ title: '主机信息数据同步', align: 'center', editable: false");
        	colM.append(", colModel: [");
    		colM.append("\r\n\t\t{ title: '消息频次', dataIndx: 'info01', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '当前占比', dataIndx: 'info02', dataType: 'percent', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '累计数据', dataIndx: 'info03', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '总占比',   dataIndx: 'info04', dataType: 'percent', align: 'center', width: 64}");
        	colM.append("\r\n\t]}");

        	colM.append(",\r\n\t{ title: '程序信息数据同步', align: 'center', editable: false");
        	colM.append(", colModel: [");
    		colM.append("\r\n\t\t{ title: '消息频次', dataIndx: 'info11', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '当前占比', dataIndx: 'info12', dataType: 'percent', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '累计数据', dataIndx: 'info13', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '总占比',   dataIndx: 'info14', dataType: 'percent', align: 'center', width: 64}");
        	colM.append("\r\n\t]}");

        	colM.append(",\r\n\t{ title: '负载历史数据同步', align: 'center', editable: false");
        	colM.append(", colModel: [");
    		colM.append("\r\n\t\t{ title: '消息频次', dataIndx: 'info21', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '当前占比', dataIndx: 'info22', dataType: 'percent', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '累计数据', dataIndx: 'info23', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '总占比',   dataIndx: 'info24', dataType: 'percent', align: 'center', width: 64}");
        	colM.append("\r\n\t]}");

        	colM.append(",\r\n\t{ title: '接收的心跳信息', align: 'center', editable: false");
        	colM.append(", colModel: [");
    		colM.append("\r\n\t\t{ title: '消息频次', dataIndx: 'info31', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '当前占比', dataIndx: 'info32', dataType: 'percent', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '累计数据', dataIndx: 'info33', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '总占比',   dataIndx: 'info34', dataType: 'percent', align: 'center', width: 64}");
        	colM.append("\r\n\t]}");
    		colM.append("\r\n]");
    		this.colModel = colM.toString();*/
        	
    		/*StringBuffer dataM = new StringBuffer();
    		dataM.append("{");
    		dataM.append("\r\n\tdataType: 'JSON',");
    		dataM.append("\r\n\tlocation: 'local',");
//    		dataM.append("\r\n\tsorting: 'local',");
//    		dataM.append("\r\n\tsortIndx: 'startuptime',");
//    		dataM.append("\r\n\tsortDir: 'up',");
    		dataM.append("\r\n\tdata: filterRows(dataLocal)");
    		dataM.append("\r\n}");
    		this.dataModel = dataM.toString();*/
			

//    		System.out.println(summary);
    		/*
    		filterModel.put("on", true);

    		BasicDBObject toolbar = null;
    		toolbar = new BasicDBObject("label", "重置流量监控");
    		toolbar.put("icon", "ui-icon-bookmark");
    		toolbar.put("function", "resetflow();");
    		toolbars.add(toolbar);*/

//    		BasicDBObject dataOptions = new BasicDBObject();
//    		BasicDBList options;
//    		options = new BasicDBList();
//    		List<?> list = this.sysalarmMgr.getJdbcDao().getAlarmType();
//    		if( list != null )
//	    		for(Object e : list )
//	    		{
//	    			if( e == null) continue;
//	        		options.add(new BasicDBObject(e.toString(), e.toString()));
//	    		}
//    		dataOptions.put("ORGTYPE", options);
//    		options = new BasicDBList();
//    		list = this.sysalarmMgr.getJdbcDao().getAlarmSeverity();
//    		if( list != null )
//	    		for(Object e : list )
//	    		{
//	    			if( e == null) continue;
//	        		options.add(new BasicDBObject(e.toString(), e.toString()));
//	    		}
//    		dataOptions.put("ORGSEVERITY", options);
//    		options = new BasicDBList();
//    		options.add(new BasicDBObject("-1", "待确认"));options.add(new BasicDBObject("0", "已确认"));
//    		dataOptions.put("ACTIVESTATUS", options);
//    		//构造模块子系统选项
//    		options = new BasicDBList();
//    		if( listCmpCfg != null )
//	    		for(Profile e : listCmpCfg )
//	    		{
//	        		options.add(new BasicDBObject(e.getId(), e.getName()));
//	    		}
//    		dataOptions.put("MODULE", options);
//    		filterModel.put("options", dataOptions.toString());
//    		//构造labels数据
//    		BasicDBObject dataLabels = new BasicDBObject();
//    		Iterator<String> iterator = dataOptions.keySet().iterator();
//    		while(iterator.hasNext())
//    		{
//    			String name = iterator.next();
//    			options = (BasicDBList)dataOptions.get(name);
//    			BasicDBObject labels = new BasicDBObject();
//    			for(Object o : options)
//    			{
//    				BasicDBObject e = (BasicDBObject)o;
//    				String key = e.keySet().iterator().next();
//    				String val = e.getString(key);
//    				labels.put(key, val);
//    			}
//    			dataLabels.put(name, labels);
//    		}
//    		filterModel.put("labels", dataLabels.toString());
//    		
//    		BasicDBObject sObject = new BasicDBObject();
//    		sObject.put("sql", sql);
//    		sObject.put("colModel", colModel);
//    		sObject.put("sqlExport", sql);
//    		super.getSession().setAttribute(Common.URLPATH(super.getRequest()), sObject);
//    		exportUrl = "helper!exportSql.action";
//		}
//		catch(Exception e)
//		{
//			super.setResponseException("打开页面出现异常"+e);
//			log.error("Failed to monitorload", e);
//		}
//		return "gridquery";
	}
	
	/**
	 * 服务器汇总数据总表
	 * @return
	 */
	public String cluster()
	{
		if( !Tools.isNumeric(id) )
		{
			setResponseException("只能打开指定集群的伺服器负载监控.");
			return "close";
		}
		try
		{
    		StringBuffer colM = new StringBuffer();
    		colM.append("[");
    		colM.append("\r\n\t{ title: '主机名称', dataIndx: 'host', width: 120");
			colM.append(",dataType: 'string', filter: { type: 'textbox', condition: 'contain', listeners: ['change']}");
			colM.append("}");

        	colM.append(",\r\n\t{ title: '忙时分析', dataIndx: 'busyhours', width: 260, dataType: 'string'}");
        	
        	colM.append(",\r\n\t{ title: '实时负载', align: 'center', editable: false");
        	colM.append(", colModel: [");
    		colM.append("\r\n\t\t{ title: 'CPU', dataIndx: 'currentCpuload', dataType: 'percent', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '内存', dataIndx: 'currentMemload', dataType: 'percent', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '网络输入', dataIndx: 'currentNetloadI', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '网络输出', dataIndx: 'currentNetloadO', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '磁盘输入', dataIndx: 'currentIOloadI', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '磁盘输出', dataIndx: 'currentIOloadO', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '温度', dataIndx: 'currentTemperature', dataType: 'string', align: 'center', width: 64}");
        	colM.append("\r\n\t]}");

        	colM.append(",\r\n\t{ title: '当日负载均值', align: 'center', editable: false");
        	colM.append(", colModel: [");
    		colM.append("\r\n\t\t{ title: 'CPU', dataIndx: 'onedayCpuload', dataType: 'percent', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '内存', dataIndx: 'onedayMemload', dataType: 'percent', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '网络输入', dataIndx: 'onedayNetloadI', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '网络输出', dataIndx: 'onedayNetloadO', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '磁盘输入', dataIndx: 'onedayIOloadI', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '磁盘输出', dataIndx: 'onedayIOloadO', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '温度', dataIndx: 'onedayTemperature', dataType: 'string', align: 'center', width: 64}");
        	colM.append("\r\n\t]}");
        	

        	colM.append(",\r\n\t{ title: '7日负载均值', align: 'center', editable: false");
        	colM.append(", colModel: [");
    		colM.append("\r\n\t\t{ title: 'CPU', dataIndx: 'sevendayCpuload', dataType: 'percent', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '内存', dataIndx: 'sevendayMemload', dataType: 'percent', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '网络输入', dataIndx: 'sevendayNetloadI', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '网络输出', dataIndx: 'sevendayNetloadO', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '磁盘输入', dataIndx: 'sevendayIOloadI', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '磁盘输出', dataIndx: 'sevendayIOloadO', dataType: 'string', align: 'center', width: 64},");
            colM.append("\r\n\t\t{ title: '温度', dataIndx: 'sevendayTemperature', dataType: 'string', align: 'center', width: 64}");
        	colM.append("\r\n\t]}");

    		colM.append("\r\n]");
    		this.colModel = colM.toString();
    		
    		debugJSONArray(colModel);
    		
    		StringBuffer dataM = new StringBuffer();
    		dataM.append("{");
    		dataM.append("\r\n\tdataType: 'JSON',");
    		dataM.append("\r\n\tlocation: 'local',");
    		dataM.append("\r\n\tsorting: 'local',");
    		dataM.append("\r\n\tsortIndx: 'host',");
    		dataM.append("\r\n\tsortDir: 'up',");
    		dataM.append("\r\n\tdata: filterRows(dataLocal)");
    		dataM.append("\r\n}");
    		this.dataModel = dataM.toString();

//    		JSONObject dataLights = new JSONObject();
//    		dataLights.put("dataType", new JSONObject().put("-1", new JSONObject().put("pq_cellcls", "gray")));
//    		filterModel.put("lights", dataLights.toString());
    		
    		int clusterid = Integer.parseInt(id);
    		int roleid = super.getUserRole();
			JSONObject privileges = getMonitorMgr().getClusterPrivileges(super.getUserRole(), super.getUserAccount());
			JSONArray data = new JSONArray();
			HashMap<Integer, JSONObject> filter = new HashMap<Integer, JSONObject>();
			getMonitorMgr().setClusterFilter(filter, getMonitorMgr().getCluster(clusterid));
			//System.err.println(filter.toString());
			getMonitorMgr().getMonitor().loadClusterLoadInof(data, filter, privileges, roleid==1);
    		this.localData = data.toString();
    		filterModel.put("on", true);

    		freezeCols = 1;
    		selectionModel = "{ type: 'row', mode: 'range' }";
    		sortable = true;
		}
		catch(Exception e)
		{
			super.setResponseException("打开页面出现异常"+e);
			log.error("Failed to monitorload", e);
		}
		return "gridquery";
	}

	/**
	 * 服务器汇总数据总表
	 * @return
	 */
	public String clusterchart()
	{
//		clusterloadDatasets = new ArrayList<ChartDataset>();
//		try
//		{
//			this.getMonitorMgr().getMonitor().loadServerMonitorChartData(clusterloadDatasets, command);
//        }
//        catch(Exception e)
//        {
//			log.error("Failed to loadServerMonitorInof for", e);
//        	this.responseException = "Failed to load the data of servers-monitor-inof("+e.toString()+").";
//        }
		if( !Tools.isNumeric(id) )
		{
			setResponseException("只能打开指定集群的伺服器负载跟踪图表.");
			return "close";
		}
		this.listData = this.getMonitorMgr().getClusterMonitorServers(Integer.parseInt(id), super.getUserRole());
		if( listData.isEmpty() )
		{
			setResponseException("打开集群负载跟踪图表未能获得任何伺服器列表.");
			return "close";
		}
		id = ((MonitorServer)listData.get(0)).getId();
		return "clusterchart";
	}

	/**
	 * 指定服务器监控
	 * @return
	 */
	public String serverchart()
	{
		filetype = filetype==null||filetype.isEmpty()?"gray":filetype;
		if( !Tools.isNumeric(id) )
		{
			setResponseException("不能打开服务器负载监控图表，因为未知伺服器("+id+")主机");
			return "close";
		}
		JSONObject server = this.getMonitorMgr().getServer(Integer.parseInt(id));
		if( server == null )
		{
			responseException = "未能查找到该伺服器监(ID"+id+")控配置，打开页面失败.";
			return "404";
		}
		clusterloadDatasets = new ArrayList<ChartDataset>();
		try
		{
			ip = server.getString("ip");
			port = server.getInt("port");
//			this.omtMgr.loadServerMonitorChartData(clusterloadDatasets, host, controlport);
			RunFetchMonitor runner = this.getMonitorMgr().getMonitor().getRunFetchMonitor(ip, port);
			if( runner != null )
			{
				if( command >= 0 )
				{
					super.freezeCols = 1;
					clusterloadDatasets.add(getMonitorMgr().getMonitor().loadServerMonitorChartData(runner, command));
				}
				else
				{
					super.freezeCols = 4;
					clusterloadDatasets.add(getMonitorMgr().getMonitor().loadServerMonitorChartData(runner, 1));
					clusterloadDatasets.add(getMonitorMgr().getMonitor().loadServerMonitorChartData(runner, 0));
					clusterloadDatasets.add(getMonitorMgr().getMonitor().loadServerMonitorChartData(runner, 2));
					clusterloadDatasets.add(getMonitorMgr().getMonitor().loadServerMonitorChartData(runner, 3));
				}
			}
//			for(ChartDataset e : clusterloadDatasets){
//				JSONArray json = new JSONArray("["+e.getDataSeries()+"]");
//				System.err.println(json.toString(4));
//			}
        }
        catch(Exception e)
        {
			log.error("Failed to loadServerMonitorInof for", e);
        	this.responseException = "Failed to load the data of servers-monitor-inof("+e.toString()+").";
        }
		return "serverchart";
	}
	/**
	 * 显示内存的GC情况
	 */
	private StringBuffer timeSeries;
	private StringBuffer gcCounter;
	private StringBuffer fullGcCounter;
	private StringBuffer hb;
	private StringBuffer he;
	private StringBuffer hc;
	private StringBuffer maxGcDelay;
	private StringBuffer minGcDelay;
	private StringBuffer avgGcDelay;
	public String modulememory()
	{
		if( id == null || id.isEmpty() || ip == null || ip.isEmpty())
		{
			setResponseException("不能打开模块内存监控图表因为未知服务器主机获知未知程序");
			return "close";
		}
		ModulePerf perf = getMonitorMgr().getMonitor().getModulePerf(ip, port, id);
		if( perf == null )
		{
			setResponseException("不能打开模块内存监控图表因为未知服务器主机("+ip+")");
			return "close";
		}
		this.command = perf.getType()==1?1:this.command;
        try
        {
        	this.setMemoryChartData(perf.getMemories());
		}
		catch (Exception e)
		{
			log.error("", e);
		}		
		return "modulememory";
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private MonitorGCHistory getGCHistory(String id, String ip, int port)
		throws Exception
	{
		log.info("Get the history of gc("+id+") from "+ip+":"+port+".");
		int offset = 0;
		byte[] pathbuf = id.getBytes("UTF-8");
    	byte[] payload = new byte[1024];
    	payload[0] = Command.CONTROL_GC;
    	payload[1] = (byte)pathbuf.length;//文件路径
    	offset = Tools.copyByteArray(pathbuf, payload, 2);
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
			datagramSocket.setSoTimeout(15000);
			datagramSocket.receive(response);
			port = Tools.bytesToInt(payload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port);
			datagramSocket.close();
			socket = new Socket();
			socket.connect(endpoint, 15000);
	    	ObjectInputStream oos = new ObjectInputStream(socket.getInputStream());
	        return (MonitorGCHistory)oos.readObject();
        }
        catch(Exception e)
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
	 * 设置内存图表数据
	 * @param memories
	 */
	private void setMemoryChartData(List<ModuleMemeory> memories)
		throws Exception
	{
		filetype = filetype==null||filetype.isEmpty()?"gray":filetype;
    	log.info("Set "+memories.size()+" items of module("+id +") from "+ip+":"+port);
    	int i = 0;
    	timeSeries = new StringBuffer();
    	gcCounter = new StringBuffer();
    	fullGcCounter = new StringBuffer();
    	hb = new StringBuffer();
    	he = new StringBuffer();
    	hc = new StringBuffer();
    	maxGcDelay = new StringBuffer();
    	minGcDelay = new StringBuffer();
    	avgGcDelay = new StringBuffer();
    	String time = null, time0 = null;
    	int gc = 0, fullGc = 0;
    	double maxD = 0, minD = 0, sumD = 0;
    	long ts = 0;
    	ModuleMemeory memory = null;
    	boolean isHistory = memories instanceof MonitorGCHistory;
    	JSONObject annotation = new JSONObject();
    	JSONArray labels = new JSONArray();
//    	JSONObject labelOptions = new JSONObject();
//    	if( isHistory ){
//    		labelOptions.put("backgroundColor", "rgba(255,255,255,0.5)");
//    		labelOptions.put("verticalAlign", "bottom");
//    		labelOptions.put("y", "-15");
//    	}
    	if(!isHistory&&!memories.isEmpty()){
    		memories.get(0).setBegin(true);
    	}
    	for( ; i < memories.size(); i++ )
    	{
    		memory = memories.get(i);
    		if( ts > memory.getGctime().getTime() )
    		{
            	timeSeries = new StringBuffer();
            	gcCounter = new StringBuffer();
            	fullGcCounter = new StringBuffer();
            	hb = new StringBuffer();
            	he = new StringBuffer();
            	hc = new StringBuffer();
            	maxGcDelay = new StringBuffer();
            	minGcDelay = new StringBuffer();
            	avgGcDelay = new StringBuffer();
            	ts = 0;
            	gc = 0;
            	fullGc = 0;
            	maxD = 0;
            	minD = 0;
            	sumD = 0;
    			continue;
    		}
    		ts = memory.getGctime().getTime();
    		time = Tools.getFormatTime("MM-dd HH:mm", ts);
    		if( memory.isBegin() ){
    			JSONObject label = new JSONObject();
    			label.put("text", "程序在"+time+"启动");
    			JSONObject point = new JSONObject();
    			label.put("point", point);
    			point.put("xAxis", 0);
    			point.put("yAxis", 0);
    			point.put("x", 0);
    			point.put("y", 0);
    			label.put("startup_time", time);
    			labels.put(label);
//        		annotation.put("labelOptions", labelOptions);
        		annotation.put("labels", labels);
    		}
//        		System.out.println(j+":\t"+Tools.getFormatTime("MM-dd HH:mm:00", ts)+", "+memory.getHc());
    		if( time0 != null && !time.equals(time0) )
    		{
    			this.set(time0, gc, fullGc, minD, maxD, sumD, memory);
            	gc = 0; fullGc = 0; minD = 0; maxD = 0; sumD = 0;
    		}
    		gc += memory.getGccount();
    		fullGc += memory.getFullgccount();
    		minD = (memory.getDmin()<minD||minD==0)?memory.getDmin():minD;
    		maxD = memory.getDmax()>maxD?memory.getDmax():maxD;
    		sumD += memory.getDsum();
    		time0 = time;
    	}
    	if( time0 != null && memory != null ){
			this.set(time0, gc, fullGc, minD, maxD, sumD, memory);
    	}
    	this.jsonData = annotation.toString();
//    	System.err.println(jsonData);
	}
	
	/**
	 * 
	 * @param time
	 * @param gc
	 * @param fullGc
	 * @param minD
	 * @param maxD
	 * @param avgD
	 * @param memory
	 */
	private void set(String time, int gc, int fullGc, double minD, double maxD, double sumD, ModuleMemeory memory){
		if( timeSeries.length() > 0 ){
			timeSeries.append(",");
			gcCounter.append(",");
			fullGcCounter.append(",");
    		hc.append(",");
    		hb.append(",");
    		he.append(",");
    		maxGcDelay.append(",");
    		minGcDelay.append(",");
    		avgGcDelay.append(",");
		}
		timeSeries.append("'");
//		Calendar c = Calendar.getInstance();
//		c.setTimeInMillis(memory.getRuntime());
//		c.set(Calendar.SECOND, 0);
//		c.set(Calendar.MILLISECOND, 0);
//		timeSeries.append(c.getTimeInMillis());
		timeSeries.append(time);
		timeSeries.append("'");
		gcCounter.append(gc);
		fullGcCounter.append(fullGc);
		hc.append(((long)memory.getHc())*1024);
		hb.append(((long)memory.getHb())*1024);
		he.append(((long)memory.getHe())*1024);
		maxGcDelay.append(maxD*1000);
		minGcDelay.append(minD*1000);
		avgGcDelay.append(sumD*1000/(gc+fullGc));
	}
	/**
	 * 查看JAVA程序GC日志
	 * @return
	 */
	public String gc()
	{
		if( id == null || id.isEmpty() || ip == null || ip.isEmpty())
		{
			setResponseException("不能打开模块内存监控图表因为未知服务器主机获知未知程序");
			return "close";
		}
		this.command = 1;
		try
        {
        	MonitorGCHistory history = this.getGCHistory(id, ip, port);
        	this.setMemoryChartData(history);
		}
		catch (Exception e)
		{
			log.error("", e);
		}
		return "modulememory";
	}
	
	/**
	 * 
	 * @return
	 */
	public String modulecpu()
	{
		return "modulecpu";
	}
	
	/**
	 * 
	 * @return
	 */
	public String modulediskspace()
	{
		return "modulediskspace";
	}
	
	/**
	 * 
	 * @return
	 */
	public String modulediskio()
	{
		return "modulediskio";
	}
	
	/**
	 * 
	 * @return
	 */
	public String modulenetio()
	{
		return "modulenetio";
	}
	
	public int getCommand()
	{
		return command;
	}
	public void setCommand(int command)
	{
		this.command = command;
	}
	public String getTimeSeries()
	{
		return timeSeries.toString();
	}

	public String getGcCounter()
	{
		return gcCounter.toString();
	}

	public String getFullGcCounter()
	{
		return fullGcCounter.toString();
	}

	public String getHb()
	{
		return hb.toString();
	}

	public String getHe()
	{
		return he.toString();
	}

	public String getHc()
	{
		return hc.toString();
	}

	public String getAvgGcDelay()
	{
		return avgGcDelay.toString();
	}

	public String getMinGcDelay()
	{
		return minGcDelay.toString();
	}

	public String getMaxGcDelay()
	{
		return maxGcDelay.toString();
	}
	
	public ArrayList<ChartDataset> getChartDatasets()
	{
		return this.clusterloadDatasets;
	}
}
