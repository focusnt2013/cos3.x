package com.focus.cos.control;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.control.HostPerf;
import com.focus.control.HostPerfHisotry;
import com.focus.control.HostPerfPoint;
import com.focus.control.Storage;
import com.focus.control.SystemPerf;
import com.focus.cos.CosServer;
import com.focus.cos.api.AlarmSeverity;
import com.focus.cos.api.AlarmType;
import com.focus.cos.api.ApiUtils;
import com.focus.cos.api.Sysalarm;
import com.focus.cos.api.SysalarmClient;
import com.focus.util.Base64X;
import com.focus.util.ConfigUtil;
import com.focus.util.F;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;
import com.sun.management.OperatingSystemMXBean;

/**
 * <p>Title: 系统监控</p>
 *
 * <p>Description: 采集服务器主机信息以及其他相关新</p>
 *
 * <p>Copyright: Copyright (c) 2011</p>
 *
 * <p>Company: FOCUSNT</p>
 *
 * @author Focus Lau
 * @version 2.1
 */
public class SystemMonitor extends CosServer
{
    public static String ModuleID = "SystemMonitor";
    /*系统配置信息*/
    public static final int CPUTIME = 1000;

    public static final int FAULTLENGTH = 10;

    public static final String CRLF = System.getProperty("line.separator");

    public static final int OS_Unknown = 0;

    public static final int OS_WinNT = 1;

    public static final int OS_Win9x = 2;

    public static final int OS_Linux = 3;

    public static final int OS_Unix = 4;
    //缓存上次性能数据的文件夹
    private F pathPerf = new F(ConfigUtil.getWorkPath() + "/data/monitor/perf/");
    //系统性能对象实例
    private SystemPerf sysPerf = new SystemPerf();

	//Ping的列表地址
    private List<String> pingList = new ArrayList<String>();
    //存储的列表
    private List<String> storageList = new ArrayList<String>();
    //Zookeeper
	private Zookeeper zookeeper;
    /**
     * 主机性能采集
     */
    private void collectHostPerf()
    {
        try
        {
        	sysPerf.setProperty("IPInfo", Tools.collectIpInfo());
            setCpuLoad(sysPerf);
            setMemoryLoad(sysPerf);
            setDiskSpaceLoad(sysPerf);
//            setSystemUpTime(sysPerf);
            setSystemNetIOLoad(sysPerf);
            setSystemIOLoad(sysPerf);
        }
        catch (Exception e)
        {
            Log.err("Failed to collect Host-Perf.");
            Log.err(e);
        }
    }
    /**
     * 针对Linux系统收集文件处理器信息
    private void collectFilehandlesInforLinux()
    {
        String str = Tools.os_exec(new String[]{"lsof","|w"});
        Log.msg( "lsof |wc -1: " + str );
        sysPerf.setFilesOpen(str);
    }
     */

    private void collectSystemInfo()
    {
        switch (os_type())
        {
        case OS_WinNT:
            collectSystemInforWindows();
            break;
        case OS_Linux:
        case OS_Unix:
            collectSystemInforLinux();
            break;
        default:
        }
    }

    private void collectSystemInforLinux()
    {
//        systemInfo.addProperty(new Property("HostName",Tools.os_exec(new String[]{"hostname"}),"计算机名"));
    	sysPerf.setHostName(Tools.os_exec(new String[]{"hostname"}));
//        systemInfo.addProperty(new Property("OSVersion",Tools.os_exec(new String[]{"head","-n", "1", "/etc/issue"}),"操作系统版本"));
        sysPerf.getProperties().put("OSName", "Linux");
        sysPerf.getProperties().put("OSVersion", Tools.os_exec(new String[]{"head","-n", "1", "/etc/issue"}));
//        systemInfo.addProperty(new Property("SystemUpTime",Tools.os_exec(new String[]{"uptime"}),"系统运行时间"));
        sysPerf.setSystemUpTime(Tools.os_exec(new String[]{"uptime"}));
//        systemInfo.addProperty(new Property("DiskInfo",Tools.os_exec(new String[]{"df","-h"}),"分区使用情况"));
        sysPerf.getProperties().put("DiskInfo", Tools.os_exec(new String[]{"df","-h"}));
        sysPerf.getProperties().put("PhysicalMemoryInfo", Tools.os_exec(new String[]{"free"}));
    }
    
    /**
     * 收集windows的数据
     */
    private void collectSystemInforWindows()
    {
        try
        {
            Process process = Runtime.getRuntime().exec("systeminfo");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GB2312"));
            String line = null;
            String label = "";
            String value = "";
            StringBuffer info = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null)
            {
                if( line.trim().length() == 0 )
                {
                    continue;
                }
                int i = line.indexOf(":");
                if( i != -1 )
                {
                    label = line.substring(0, i).trim();
                    value = line.substring(i + 1).trim();
                    if( label.equals("系统启动时间") || label.equals("System Boot Time")  )
                    {
                    	value = value.replaceAll(",", "");
//                        systemInfo.addProperty(new Property("SystemUpTime", value, label));
                    	sysPerf.setSystemUpTime(value);
                    }
                    else if( label.equals("OS 名称") || label.equals("OS Name") )
                    {
                        sysPerf.getProperties().put("OSName", value);
                    }
                    else if( label.equals("OS 版本") || label.equals("OS Version")  )
                    {
//                        systemInfo.addProperty(new Property("OSVersion", value, label));
                        sysPerf.getProperties().put("OSVersion", value);
                    }
                    else if( label.equals("物理内存总量") || label.equals("Total Physical Memory")  )
                    {
//                        systemInfo.addProperty(new Property("PhysicalMemoryInfo", value, label));
                        sysPerf.getProperties().put("PhysicalMemoryInfo", value);
                    }
                    else if( label.equals("主机名") || label.equals("Host Name")  )
                    {
//                        systemInfo.addProperty(new Property("HostName", value, label));
                    	sysPerf.setHostName(value);
                    }
                    if( info.length() > 0 )info.append("\r\n");
                    info.append(line.trim());
                }
            }
//            systemInfo.addProperty(new Property("HostInfo", info.toString(), "主机相关信息"));
            sysPerf.getProperties().put("HostInfo", info.toString());

            F file = new F(ConfigUtil.getWorkPath(), "data/monitor/out");
            Calendar calendar = Calendar.getInstance();
            int day0 = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.setTimeInMillis(file.lastModified());
            int day1 = calendar.get(Calendar.DAY_OF_MONTH);
            if( day0 != day1 ) Log.msg("Exec systeminfo:\r\n"+info);
        }
        catch( Exception e )
        {
            Log.err("Failed to collect Host-Perf for window.");
            Log.err(e);
        }
    }
/*
    private void setSystemUpTime(HostPerf hostPerf)
    {
        Property property = systemInfo.getProperty("SystemUpTime");
        if( property != null )
        {
        	hostPerf.setSystemUpTime(property.getValue());
        }
    }
    */
    private double collectDouble(double a, double b, int p) {
    	return (int)(a/b * p + 0.5)/(double)100;
    }
    
    private void collectStorage() {
    	int storageUsage = 0;
    	double storageUsageInfo = 0;
    	List<Storage> storages= new ArrayList<Storage>();
        for (String storage : storageList) {
        	F file = new F(storage);
        	if (file.exists()) {
	        	double d = 1073741824;// = 1024*1024*1024
	        	double totalSpace = collectDouble(file.getTotalSpace(), d, 100);
	        	double ableSpace = collectDouble(file.getTotalSpace() - file.getUsableSpace(), d, 100);
	        	double percent = collectDouble(file.getTotalSpace() - file.getUsableSpace(), file.getTotalSpace(), 10000);
	        	Storage s = new Storage();
	        	s.setAddress(file.getPath());
	        	s.setTotalSpace(totalSpace);
	        	s.setAbleSpace(ableSpace);
	        	s.setPercent(percent);
	       		storages.add(s);
	       		
	       		Log.msg(file.getPath() + " : " + ableSpace + "G/" + totalSpace + "G " + percent + "%");
	       		
	       		int su = (int)(percent * 100);
				if (su > storageUsage) {
					storageUsageInfo = percent / 100;
					storageUsage = su;
				}
        	}
        	else {
        		
        	}
       	}
        sysPerf.setProperty("Storage", storages);//磁阵使用情况监听
        sysPerf.setProperty("StorageUsage", storageUsage);//磁阵空间使用大数表示形式(80% 对应 8000)
        sysPerf.setProperty("StorageUsageInfo", storageUsageInfo);//磁阵空间使用小数表示形式(80% 对应 0.8)
    }

    /**
     * 获取磁盘空间占用率
     *
     * @return
     */
    private void setDiskSpaceLoad(HostPerf hostPerf)
    {
    	collectStorage();
    	
        F file = new F(".");
        Log.msg("TotalSpaceSize:"+file.getTotalSpace());
        Log.msg("UsableSpaceSize:"+file.getUsableSpace());
        double totalSpaceSize = file.getTotalSpace();
        double usableSpaceSize = file.getUsableSpace();
        hostPerf.setDiskUsed(file.getUsableSpace());
        hostPerf.setDiskUsage((int)((file.getTotalSpace()-file.getUsableSpace())*10000/file.getTotalSpace()));
        double diskUsageInfo = (totalSpaceSize-usableSpaceSize)/totalSpaceSize;
        hostPerf.setDiskUsageInfo(diskUsageInfo);
        F load85 = new F(pathPerf, "diskload85");
        F load95 = new F(pathPerf, "diskload95");
        if( diskUsageInfo > 0.95 )
        {//磁盘空间大于0.8发出告警
            if( load95.exists() && System.currentTimeMillis() - load95.lastModified() > 2*Tools.MILLI_OF_MINUTE )
            {
            	String time = Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", load95.lastModified() );
            	Log.war("Sysalarm for diskUsageInfo is "+diskUsageInfo);
    	    	Sysalarm alarm = new Sysalarm();
    	        alarm.setSysid("Sys");
    	        alarm.setSeverity(AlarmSeverity.BLACK.getValue());
    	        alarm.setType(AlarmType.D.getValue());
    	        alarm.setId(ModuleID+"_Storage");
    	        alarm.setTitle("服务器剩余磁盘空间严重不足");
    	        alarm.setText("服务器磁盘空间从时间"+time+"以来持续超过95%，当前是"+hostPerf.getDiskUsageInfo());
    	        alarm.setCause("服务器磁盘配置不足或使用超出预期");
    	        SysalarmClient.send(alarm);
            }
    		else
    			try
    			{
    				load95.createNewFile();
    			}
    			catch (IOException e)
    			{
    			}
        }
        else if( diskUsageInfo > 0.85)
        {//磁盘空间大于0.8发出告警
            if( load85.exists() && System.currentTimeMillis() - load95.lastModified() > 2*Tools.MILLI_OF_MINUTE )
            {
            	String time = Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", load85.lastModified() );
            	Log.war("Sysalarm for diskUsageInfo is "+diskUsageInfo);
    	    	Sysalarm alarm = new Sysalarm();
    	        alarm.setSysid("Sys");
    	        alarm.setSeverity(AlarmSeverity.ORANGE.getValue());
    	        alarm.setType(AlarmType.D.getValue());
    	        alarm.setId(ModuleID+"_Storage");
    	        alarm.setTitle("服务器剩余磁盘空间不足");
    	        alarm.setText("服务器磁盘空间从时间"+time+"以来持续超过85%，当前是"+hostPerf.getDiskUsageInfo());
    	        alarm.setCause("服务器磁盘配置不足或使用超出预期");
    	        SysalarmClient.send(alarm);
            }
    		else
    			try
    			{
    				load85.createNewFile();
    			}
    			catch (IOException e)
    			{
    			}
        }
        else{
        	if( load85.exists() || load95.exists() ){
        		load85.delete();
        		load95.delete(); 
        		SysalarmClient.autoconfirm("Sys", ModuleID+"_Storage", "服务器磁盘空间降低到"+hostPerf.getDiskUsageInfo());
        	}
        }
    }

    /**
     * 获取操作系统类型代码
     *
     * @return
     */
    private int os_type()
    {
        String osName = System.getProperty("os.name");
        String os = osName.toUpperCase();
//        Log.msg("os:" + os);
        if (os.startsWith("WINDOWS"))
        {
//            if (os.endsWith("NT") || os.endsWith("2000") ||
//                os.endsWith("XP") || os.endsWith("VISTA"))
//            {
                return OS_WinNT;
//            }
//            else
//            {
//                return OS_Win9x;
//            }
        }
        else if (os.indexOf("LINUX") > 0)
        {
            return OS_Linux;
        }
        else if (os.indexOf("UX") > 0)
        {
            return OS_Unix;
        }
        else
        {
        	Log.war("OS_Unknown:" + os);
            return OS_Unknown;
        }
    }

    /**
     * window操作系统的可用磁盘空间
     *
     * @param s
     * @return
    private long os_freesize_win(String s)
    {
        String lastLine = os_lastline(s);

        if (lastLine == null)
        {
            Log.msg("(lastLine == null)");
            return -1;
        }
        else
        {
            lastLine = lastLine.trim().replaceAll(",", "");
        }
        // 分析
        String items[] = os_split(lastLine);
        if (items.length < 4)
        {
            Log.msg("DIR result error: " + lastLine);
            return -1;
        }
        if (items[2] == null)
        {
            Log.msg("DIR result error: " + lastLine);
            return -1;
        }
        long bytes = Long.parseLong(items[2]);
        return bytes;
    }
     */

    /**
     * unix操作系统的可用磁盘空间
     *
     * @param s
     * @return
    private long os_freesize_unix(String s)
    {
        String lastLine = os_lastline(s); // 获取最后一航；
        if (lastLine == null)
        {
            Log.msg("(lastLine == null)");
            return -1;
        }
        else
        {
            lastLine = lastLine.trim();
        }
        // 格式：/dev/sda1 101086 12485 83382 14% /boot
        String[] items = os_split(lastLine);
        System.out.println("os_freesize_unix() 目录:\t" + items[0]);
        System.out.println("os_freesize_unix() 总共:\t" + items[1]);
        System.out.println("os_freesize_unix() 已用:\t" + items[2]);
        System.out.println("os_freesize_unix() 可用:\t" + items[3]);
        System.out.println("os_freesize_unix() 可用%:\t" + items[4]);
        System.out.println("os_freesize_unix() 挂接:\t" + items[5]);

        if (items[3] == null)
        {
            Log.msg("(ss[3]==null)");
            return -1;
        }
        return Long.parseLong(items[3]) * 1024; // 按字节算
    }

    private String[] os_split(String s)
    {
        String[] ss = s.split(" "); // 空格分隔；
        List ssl = new ArrayList(16);
        for (int i = 0; i < ss.length; i++)
        {
            if (ss[i] == null)
            {
                continue;
            }
            ss[i] = ss[i].trim();
            if (ss[i].length() == 0)
            {
                continue;
            }
            ssl.add(ss[i]);
        }
        String[] ss2 = new String[ssl.size()];
        ssl.toArray(ss2);
        return ss2;
    }

    private String os_lastline(String s)
    {
        // 获取多行输出的最后一行；
        BufferedReader br = new BufferedReader(new StringReader(s));
        String line = null;
        String lastLine = null;
        try
        {
            while ((line = br.readLine()) != null)
            {
                lastLine = line;
            }
        }
        catch (Exception e)
        {
            Log.msg("parseFreeSpace4Win() " + e);
        }
        return lastLine;
    }

     */
    /**
     * 获得内存使用率.
     *
     * @return
     */
    private void setMemoryLoad(HostPerf hostPerf)
    {
         OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
         double totalPhysicalMemorySize = osmxb.getTotalPhysicalMemorySize();
         double freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();
         long total = osmxb.getTotalSwapSpaceSize() + osmxb.getTotalPhysicalMemorySize();//总的交换空间和总的护理空间
         this.sysPerf.setProperty("SwapSpaceSize", osmxb.getTotalSwapSpaceSize());
         int osType = os_type();
         Log.msg(osType+" TotalPhysicalMemorySize:"+osmxb.getTotalPhysicalMemorySize()+
                 ", FreePhysicalMemorySize:"+osmxb.getFreePhysicalMemorySize()+
                 ", SwapSpaceSize:"+osmxb.getTotalSwapSpaceSize()+
                 ", total="+total+", "+osmxb.getSystemLoadAverage());
         double phyMemUsage = 0;
         switch (osType)
         {
         case OS_WinNT:
        	 hostPerf.setPhyMemUsed(osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize());
//        	 hostPerf.setPhyMemUsage((int)(hostPerf.getPhyMemUsed()*10000/total));
         	 hostPerf.setPhyMemUsage((int)((totalPhysicalMemorySize-freePhysicalMemorySize)*10000/(totalPhysicalMemorySize)));
        	 phyMemUsage = (totalPhysicalMemorySize-freePhysicalMemorySize)/(totalPhysicalMemorySize);
        	 hostPerf.setPhyMemUsageInof(phyMemUsage);
        	 break;
         case OS_Linux:
         case OS_Unix:
         {
             F file = new F( "/proc/meminfo" );
             List<String> lines = IOHelper.readLines(file, "UTF-8");
             long cached = 0;
             if( lines != null )
             {
            	 for(String line : lines )
            	 {
            		 line = line.toLowerCase();
            		 if( line.startsWith("cached:") )
            		 {
            			 String val = line.substring("cached:".length());
            			 int i = val.lastIndexOf("kb");
            			 if( i != -1 )
            			 {
            				 val = val.substring(0, i-1);
            				 val = val.trim();
            				 if( Tools.isNumeric(val) )
            				 {
            					 cached = Long.parseLong(val)*1024;
            				 }
            			 }
            			 break;
            		 }
            	 }
             }
             cached = cached*8/10;//Cached的8成作为可用
        	 hostPerf.setPhyMemUsed(osmxb.getTotalPhysicalMemorySize()- osmxb.getFreePhysicalMemorySize() - cached );
         	 hostPerf.setPhyMemUsage((int)((totalPhysicalMemorySize-freePhysicalMemorySize-cached)*10000/(totalPhysicalMemorySize)));
        	 phyMemUsage = (totalPhysicalMemorySize-freePhysicalMemorySize-cached)/(totalPhysicalMemorySize);
        	 hostPerf.setPhyMemUsageInof(phyMemUsage);
             this.sysPerf.setProperty("Cached", cached);
        	 break;
         }
         default:
        	 hostPerf.setPhyMemUsed(osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize());
//         	 hostPerf.setPhyMemUsage((int)(hostPerf.getPhyMemUsed()*10000/total));
         	 hostPerf.setPhyMemUsage((int)((totalPhysicalMemorySize-freePhysicalMemorySize)*10000/(totalPhysicalMemorySize)));
        	 phyMemUsage = (totalPhysicalMemorySize-freePhysicalMemorySize)/(totalPhysicalMemorySize);
    	     hostPerf.setPhyMemUsageInof(phyMemUsage);
        	 break;
        }
        F load85 = new F(pathPerf, "memload85");
        F load95 = new F(pathPerf, "memload95");
        if( phyMemUsage > 0.95 )
        {//内存空间大于0.8发出告警
            if( load95.exists() && System.currentTimeMillis() - load95.lastModified() > 2*Tools.MILLI_OF_MINUTE )
            {
            	SysalarmClient.autoconfirm("Sys", ModuleID+"_Memory", "服务器磁盘空间升高到"+hostPerf.getPhyMemUsageInof());
            	String time = Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", load95.lastModified() );
		     	Log.war("Sysalarm for phyMemUsage is "+phyMemUsage);
		    	Sysalarm alarm = new Sysalarm();
		        alarm.setSysid("Sys");
		        alarm.setSeverity(AlarmSeverity.BLACK.getValue());
		        alarm.setType(AlarmType.D.getValue());
		        alarm.setId(ModuleID+"_Memory");
		        alarm.setTitle("服务器剩余内存空间严重不足");
		        alarm.setText("服务器内存负载从时间"+time+"以来持续超过95%，当前是"+hostPerf.getPhyMemUsageInof());
    	        alarm.setCause("服务器内存配置不足或使用超出预期");
		        SysalarmClient.send(alarm);
            }
			else
				try
				{
					load95.createNewFile();
				}
				catch (IOException e)
				{
				}
        }
        else if( phyMemUsage > 0.85 )
        {//内存空间大于0.8发出告警
            if( load85.exists() && System.currentTimeMillis() - load85.lastModified() > 2*Tools.MILLI_OF_MINUTE )
            {
//            	SysalarmClient.autoconfirm("Sys", ModuleID+"_Memory", "服务器内存负责降低到"+hostPerf.getPhyMemUsageInof());
            	String time = Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", load85.lastModified() );
	        	Log.war("Sysalarm for phyMemUsage is "+phyMemUsage);
	 	    	Sysalarm alarm = new Sysalarm();
		        alarm.setSysid("Sys");
		        alarm.setSeverity(AlarmSeverity.ORANGE.getValue());
		        alarm.setType(AlarmType.D.getValue());
		        alarm.setId(ModuleID+"_Memory");
	 	        alarm.setTitle("服务器剩余内存空间不足");
		        alarm.setText("服务器内存负载从时间"+time+"以来持续超过85%，当前是"+hostPerf.getPhyMemUsageInof());
    	        alarm.setCause("服务器内存配置不足或使用超出预期");
	 	        SysalarmClient.send(alarm);
            }
			else
				try
				{
					load85.createNewFile();
				}
				catch (IOException e)
				{
				}
        }
        else{
        	if( load85.exists() || load95.exists() ){
        		load85.delete();
        		load95.delete(); 
        		SysalarmClient.autoconfirm("Sys", ModuleID+"_Memory", "服务器内存负载降低到"+hostPerf.getPhyMemUsageInof());
        	}
        }
    }

    /**
     * 获得CPU使用率.
     *
     * @return
     */
    private void setCpuLoad(HostPerf hostPerf)
    {
        int osType = os_type();

        switch (osType)
        {
        case OS_WinNT:
            setCpuLoadForWindows(hostPerf);
            break;
        case OS_Linux:
        case OS_Unix:
            setCpuLoadForLinux(hostPerf);
            break;
        default:
        }
        F load85 = new F(pathPerf, "cpuload85");
        F load95 = new F(pathPerf, "cpuload95");
        if( hostPerf.getCpuLoad() > 9500 )
        {//内存空间大于0.8发出告警
            if( load95.exists() && System.currentTimeMillis() - load95.lastModified() > 2*Tools.MILLI_OF_MINUTE )
            {
            	SysalarmClient.autoconfirm("Sys", ModuleID+"_Cpu", "服务器CPU负载升高到"+hostPerf.getCpuLoadInfo());
            	String time = Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", load95.lastModified() );
		     	Log.war("Sysalarm for cpu is "+hostPerf.getCpuLoadInfo());
		    	Sysalarm alarm = new Sysalarm();
		        alarm.setSysid("Sys");
		        alarm.setSeverity(AlarmSeverity.RED.getValue());
		        alarm.setType(AlarmType.D.getValue());
		        alarm.setId(ModuleID+"_Cpu");
		        alarm.setTitle("服务器CPU负载极高");
		        alarm.setText("服务器CPU负载从时间"+time+"以来持续超过95%，当前是"+hostPerf.getCpuLoadInfo());
    	        alarm.setCause("服务器CPU使用超出预期或者部署的程序出现死循环");
		        SysalarmClient.send(alarm);
            }
			else
				try
				{
					load95.createNewFile();
				}
				catch (IOException e)
				{
				}
        }
        else if( hostPerf.getCpuLoad() > 8500 )
        {//内存空间大于0.8发出告警
            if( load85.exists() && System.currentTimeMillis() - load85.lastModified() > 2*Tools.MILLI_OF_MINUTE )
            {
            	SysalarmClient.autoconfirm("Sys", ModuleID+"_Cpu", "服务器CPU负载降低到"+hostPerf.getCpuLoadInfo());
            	String time = Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", load85.lastModified() );
		     	Log.war("Sysalarm for cpu is "+hostPerf.getCpuLoadInfo());;
	 	    	Sysalarm alarm = new Sysalarm();
		        alarm.setSysid("Sys");
		        alarm.setSeverity(AlarmSeverity.YELLOW.getValue());
		        alarm.setType(AlarmType.D.getValue());
		        alarm.setId(ModuleID+"_Memory");
	 	        alarm.setTitle("服务器CPU负载过高");
		        alarm.setText("服务器CPU负载从时间"+time+"以来持续超过85%，当前是"+hostPerf.getCpuLoadInfo());
    	        alarm.setCause("服务器CPU使用超出预期或者部署的程序出现死循环");
	 	        SysalarmClient.send(alarm);
            }
			else
				try
				{
					load85.createNewFile();
				}
				catch (IOException e)
				{
				}
        }
        else{
        	if( load85.exists() || load95.exists() ){
        		load85.delete();
        		load95.delete();
        		SysalarmClient.autoconfirm("Sys", ModuleID+"_Cpu", "服务器CPU负载降低到"+hostPerf.getCpuLoadInfo());
        	}
        }
    }
    
    /**
     * 读取处理器数据
     * @param file
     * @return
     * @throws Exception
     */
    private CpuStat readProcStat(F file) throws Exception
    {
        BufferedReader br = null;
        String line = null;
        CpuStat cpuStat = new CpuStat();
        try
        {
            br = new BufferedReader( new InputStreamReader(new FileInputStream( file ) ) );
            line = br.readLine() ;
            if( line == null || line.isEmpty() ) return cpuStat;
            StringTokenizer token = new StringTokenizer( line );
            token.nextToken();
            //cpu 32888288 6926 14454049 2105995454 10647778 662 148491 0 0
            cpuStat.setUser(Long.valueOf( token.nextToken() ));
            cpuStat.setNice(Long.valueOf( token.nextToken() ));
            cpuStat.setSystem(Long.valueOf( token.nextToken() ));
            cpuStat.setIdle(Long.valueOf( token.nextToken() ));
            cpuStat.setIowait(Long.valueOf( token.nextToken() ));
            cpuStat.setIrq(Long.valueOf( token.nextToken() ));
            cpuStat.setSoftirq(Long.valueOf( token.nextToken() ));
        }
        catch( Exception e )
        {
            Log.err( "Failed to get Cpu-Load for linux.", e );
        }
        finally
        {
            if( br != null )
            {
                try
                {
                    br.close();
                }
                catch( IOException ex )
                {
                }
            }
        }
        return cpuStat;
    }

    private void setCpuLoadForLinux(HostPerf hostPerf)
    {
        F file = new F( "/proc/stat" );
        F fileProcStat = new F(pathPerf, "ProcStat");
        try
        {
        	if( !fileProcStat.exists() )
        	{
        		Log.war("Not found "+fileProcStat);
            	IOHelper.writeFile(fileProcStat, new FileInputStream( file ));
        		return;
        	}
            CpuStat cpuStatPass = this.readProcStat(fileProcStat);
            CpuStat cpuStat = this.readProcStat(file);
            long marginUser = cpuStat.user - cpuStatPass.user;
            long marginNice = cpuStat.nice - cpuStatPass.nice;
            long marginSystem = cpuStat.system - cpuStatPass.system;
            long marginIdl = cpuStat.idle - cpuStatPass.idle;
            long marginIowait = cpuStat.iowait - cpuStatPass.iowait;
            long marginIrq = cpuStat.irq - cpuStatPass.irq;
            long marginSoftirq = cpuStat.softirq - cpuStatPass.softirq;
            double marginTotal = marginUser + marginNice + marginSystem +
                               marginIdl +  marginIowait + marginIrq +
                               marginSoftirq;
            Log.msg("MarginIdl:"+marginIdl+",MarginTotal:"+marginTotal);
            double marginLoad = marginTotal - marginIdl;
            if( marginTotal > 0 )
            {
	            hostPerf.setCpuLoad(((int)marginLoad*10000)/((int)marginTotal));
	            hostPerf.setCpuLoadInfo(marginLoad/marginTotal);
            }
        	IOHelper.writeFile(fileProcStat, new FileInputStream( file ));
        }
        catch( Exception e )
        {
            Log.err( "Failed to get Cpu-Load for linux.", e );
        }
    }

    protected void setCpuLoadForWindows(HostPerf hostPerf)
    {
        double cpuLoad = 0;
        try
        {
            String procCmd = System.getenv("windir") +
                             "\\system32\\wbem\\wmic.exe process get Caption,CommandLine,KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
            // 取进程信息
            long ts = System.currentTimeMillis();
            long[] c0 = readWindowsCpu(Runtime.getRuntime().exec(procCmd));
            cpuLoad = ((double)c0[1])/(c0[1]+c0[0]);
            Log.msg(String.format("Check the load of cpu is %s, the idletime is %s, the kneltime&usertime is %s.", 
            		cpuLoad, c0[0], c0[1]));
            Thread.sleep(CPUTIME);
            ts = System.currentTimeMillis();
            long[] c1 = readWindowsCpu(Runtime.getRuntime().exec(procCmd));
            cpuLoad = ((double)c1[1])/(c1[1]+c1[0]);
            Log.msg(String.format("Check the load of cpu is %s, the idletime is %s, the kneltime&usertime is %s.",
            		cpuLoad, c1[0], c1[1]));
            if (c0 != null && c1 != null)
            {
                long idletime = Math.abs(c1[0] - c0[0]);
                long busytime = Math.abs(c1[1] - c0[1]);
                cpuLoad = ((double)busytime)/(busytime+idletime);
                Log.msg(String.format("Succeed to get the load of cpu is %s, the busytime is %s, the idletime is %s, after %s ts.",
                		cpuLoad, busytime, idletime, System.currentTimeMillis()-ts));
                hostPerf.setCpuLoad((int)((busytime*10000)/(busytime+idletime)));
            }
        }
        catch (Exception e)
        {
            Log.err("Failed to get CPU-LOAD.");
            Log.err(e);
        }
        hostPerf.setCpuLoadInfo(cpuLoad);
    }

    /**
     * 读取CPU信息.
     *
     * @param proc
     * @return
     */
    private long[] readWindowsCpu(final Process proc)
    {
        long[] retn = new long[2];
        String line = null;
        InputStreamReader ir = null;
        StringBuffer sb = new StringBuffer("Read the info from the command of cpu");
        try
        {
            proc.getOutputStream().close();
            ir = new InputStreamReader(proc.getInputStream(), System.getProperty("file.encoding"));
            LineNumberReader input = new LineNumberReader(ir);
            line = input.readLine();
            if (line == null || line.length() < FAULTLENGTH)
            {
                return null;
            }
            int capidx = line.indexOf("Caption");
            int cmdidx = line.indexOf("CommandLine");
            int kmtidx = line.indexOf("KernelModeTime");
            int rocidx = line.indexOf("ReadOperationCount");
            int umtidx = line.indexOf("UserModeTime");
            int wocidx = line.indexOf("WriteOperationCount");
            long idletime = 0;
            long kneltime = 0;
            long usertime = 0;
            sb.append("\r\n\t"+line);
            while ((line = input.readLine()) != null)
            {
                if (line.length() < wocidx)
                {
                    continue;
                }
                // 字段出现顺序：Caption,CommandLine,KernelModeTime,ReadOperationCount,
                String caption = substring(line, capidx, cmdidx - 1).trim();
                String cmd =     substring(line, cmdidx, kmtidx - 1).trim();
                if (cmd.indexOf(".exe") >= 0)
                {
                    continue;
                }
                sb.append("\r\n\t"+line);
                if (caption.equals("System Idle Process") || caption.equals("System"))
                {
                    idletime += Long.valueOf(substring(line, kmtidx, rocidx - 1).trim()).longValue();
                    idletime += Long.valueOf(substring(line, umtidx, wocidx - 1).trim()).longValue();
                    continue;
                }
                String str0 = substring(line, kmtidx, rocidx - 1).trim();
                if( Tools.isNumeric(str0))
                	kneltime += Long.valueOf(str0);
                else
                    sb.append("\r\n\t\tFailed to parse kneltime "+str0);
                str0 = substring(line, umtidx, wocidx - 1).trim();
                if( Tools.isNumeric(str0))
                	usertime += Long.valueOf(str0);
                else
                    sb.append("\r\n\t\tFailed to parse usertime "+str0);
            }
            retn[0] = idletime;
            retn[1] = kneltime + usertime;
//            F file = new F(ConfigUtil.getWorkPath(), "data/monitor/out");
//            Calendar calendar = Calendar.getInstance();
//            int day0 = calendar.get(Calendar.DAY_OF_MONTH);
//            calendar.setTimeInMillis(file.lastModified());
//            int day1 = calendar.get(Calendar.DAY_OF_MONTH);
//            if( day0 != day1 ) Log.msg(sb.toString());
            return retn;
        }
        catch (Exception ex)
        {
            Log.err("Failed to read cpu from cmd "+sb, ex);
        }
        finally
        {
        	if( ir != null )
	            try
	            {
	                ir.close();
	            }
	            catch (Exception e)
	            {
	                Log.err("Failed to close ir", e);
	            }
        }
        return null;
    }

    private String substring(String src, int start_idx, int end_idx)
    {
        byte[] b = src.getBytes();
        String tgt = "";
        for (int i = start_idx; i <= end_idx; i++)
        {
            tgt += (char) b[i];
        }
        return tgt;
    }
    
    /**
     * 查看网络连接状况
     */
    private void collectNetInfo()
    {
    	if( pingList.isEmpty() ) 
    	{
    		return;
    	}
    	try
    	{
	    	int osType = os_type();
			String srcIp = pingList.get(0);
			if(srcIp == null || srcIp.trim().isEmpty())
			{
				return;
			}
			ArrayList<Object> ping = new ArrayList<Object>();
//			ArrayList<Property> ping = new ArrayList<Property>(); 
			for(int i = 0; i < pingList.size(); i++)
			{
				switch (osType) 
				{
				case OS_WinNT:
				{
					String cmds[] = new String[2];
					cmds[0] = "ping";
					cmds[1] = pingList.get(i);
					String ret = Tools.os_exec(cmds);
					int index = ret.indexOf("Ping statistics for");
					if(index != -1)
					{
//						JSONObject obj = new JSONObject();
//						obj.put(pingList.get(i), ret.substring(index, ret.length()));
						ping.add(ret.substring(index, ret.length()));
//						ping.add(new Property(pingList.get(i),ret.substring(index, ret.length()), ""));
						sysPerf.setProperty("IP" + i, ret.substring(index, ret.length()));
					}
					break;
				}
				
		        case OS_Linux:
		        case OS_Unix:
		        {
					String cmds[] = new String[3];
					cmds[0] = "ping";
					cmds[1] = "-c4";
					cmds[2] = pingList.get(i);
					String ret = Tools.os_exec(cmds);
					int index = ret.indexOf("---");
					if(index != -1)
					{
						ping.add(ret.substring(index, ret.length()));
//						ping.add(new Property(pingList.get(i),ret.substring(index, ret.length()), ""));
						sysPerf.setProperty("IP" + i, ret.substring(index, ret.length()));
					}
					break;
				}
				default:
					break;
				}
    		}
			sysPerf.setProperty("PING", ping);
    	}
    	catch (Exception e) 
    	{
    		Log.err(e);
		}
    }
    /**
     * 监控相关应用信息
     */
    private void collectApplicationInfo()
    {
		this.checkDatabaseConnect();
		this.checkCosInterface();
		F myid = new F("../data/zkdat/myid");
		if( myid.exists() )
		{//是ZK集群模式
			sysPerf.setProperty("zookeeper.myid", IOHelper.readFirstLine(myid));
			sysPerf.setProperty("zookeeper.servers", ConfigUtil.getString("zookeeper.servers"));
		}
    }
    
    /**
     * 采集COS接口检测
     */
    private void checkCosInterface()
    {
    	JSONObject cosws = new JSONObject();
    	cosws.put("state", 0);
		String url = ConfigUtil.getString("cos.api", "");
		if( url.isEmpty() ) url = "http://127.0.0.1:"+ConfigUtil.getString("cos.api.port");
		if( !url.startsWith("http://") ) url = "http://"+url;
    	cosws.put("url", url);
    	Log.print("Check cos-ws connect to %s", url);
        F coswserr = new F(pathPerf, "coswserr");
		long ts = coswserr.lastModified();
		try
		{
			byte[] payload = ApiUtils.doGet(url, null, false);
			String json = new String(payload, "UTF-8");
			JSONObject rsp = new JSONObject(json);
			cosws.put("state", 1);
			cosws.put("id", rsp.getString("id"));
			cosws.put("name", rsp.getString("name"));
			cosws.put("desc", rsp.getString("desc"));
			cosws.put("controlport", rsp.getInt("controlport"));
			cosws.put("exception", "");
			if( coswserr.exists() ){
				SysalarmClient.autoconfirm("Sys", ModuleID+"_Cosws", "接口已经恢复");
				coswserr.delete();
			}
		}
		catch (Exception e)
		{
			cosws.put("state", 0);
	    	cosws.put("exception", e.getMessage());
	    	cosws.put("yes", coswserr.exists());
			try
			{
				IOHelper.writeFile(coswserr, cosws.toString(4).getBytes("UTF-8"));
			}
			catch (IOException e1)
			{
			}
		}
		finally
		{
			if( cosws.getInt("state") == 0 && ts > 0 )
			{
				Sysalarm alarm = new Sysalarm();
		        alarm.setSysid("Sys");
		        alarm.setId(ModuleID+"_Cosws");
		        alarm.setSeverity(cosws.getBoolean("yes")?AlarmSeverity.RED.getValue():AlarmSeverity.YELLOW.getValue());
		        alarm.setType(AlarmType.S.getValue());
		        alarm.setCause(cosws.getString("exception"));
		        alarm.setTitle("从“"+this.sysPerf.getHost()+"”连接COS接口服务出现异常");
		        alarm.setText("系统监控引擎检查发现连接【COS接口服务】异常错误"+cosws.getString("exception")+"，请系统管理员检查相关配置参数("+url+")是否正确设置。");
				SysalarmClient.send(alarm);
			}
			sysPerf.setProperty("Cosws", cosws.toString());
	    	Log.print("Finish to check the cos-ws %s", cosws.toString());
		}
    }
    
    /**
     * 检查数据库是否正常（已是否能连接上并打开数据库为准）
     * @param type
     * @param host
     * @param dbname
     * @param user
     * @param pwd
     * @return
     */
    private void checkDatabaseConnect()
    {
		int port = 0;
		String mp = System.getProperty("control.port", "9081");
		if( Tools.isNumeric(mp) ) port = Integer.parseInt(mp);
		JSONArray array = new JSONArray();
		try
		{
	        File dir = new File(ConfigUtil.getWorkPath(), "data/monitor/database");
	        if( !dir.exists()){
	        	dir.mkdirs();
	        }
			StringBuffer sb = new StringBuffer("Check database connect(zkport:"+port+") for "+this.sysPerf.getSecurityKey()+"("+Tools.encodeMD5(this.sysPerf.getSecurityKey())+")");
			if( System.getProperty("cos.jdbc.driver") != null )
			{
				JSONObject monitor = new JSONObject();
				monitor.put("jdbc.driver", System.getProperty("cos.jdbc.driver"));
				monitor.put("jdbc.url", System.getProperty("cos.jdbc.url"));
				monitor.put("jdbc.username", System.getProperty("cos.jdbc.user"));
				monitor.put("jdbc.password", System.getProperty("cos.jdbc.password", ""));
				monitor.put("jdbc.name", "COS核心数据库");
				this.checkDatabase(monitor, sb);
				array.put(monitor);
			}
			zookeeper = Zookeeper.getInstance("127.0.0.1:"+port);
			String path = "/cos/config/monitor/database";
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				path = "/cos/config/monitor/database/"+Tools.encodeMD5(this.sysPerf.getSecurityKey());
				stat = zookeeper.exists(path, false); 
				if( stat != null)
				{
					List<String> list = zookeeper.getChildren(path, false);
					sb.append("Succeed to setup "+list.size()+" configs from "+path);
					for( String nodepath : list )
					{
						nodepath = path+"/"+nodepath;
						stat = zookeeper.exists(nodepath, false);
						sb.append("\r\n\t"+nodepath);
						if( stat == null ) continue;
						String json = new String(zookeeper.getData(nodepath, false, stat), "UTF-8");
						JSONObject monitor = new JSONObject(json);
						this.checkDatabase(monitor, sb);
						array.put(monitor);
					}
				}
			}
			Log.msg(sb.toString());
		}
		catch(Exception e)
		{
			Log.err("Failed to check the database", e);
		}
		finally
		{
			sysPerf.setProperty("Database", array.toString());//在此之前先将数据库状态修改为不能连接
			if( zookeeper != null ) zookeeper.close();
		}
    }
    
    /**
     * 检查数据库状态
     * @param monitor
     * @param sb
     */
    private void checkDatabase(JSONObject monitor, StringBuffer sb)
    {
    	String name = monitor.getString("jdbc.name");
    	String jdbcUrl = monitor.getString("jdbc.url");
    	String jdbcUsername = monitor.getString("jdbc.username");
    	String jdbcUserpswd = monitor.getString("jdbc.password");
    	if( !jdbcUserpswd.isEmpty() ) jdbcUserpswd = Base64X.decode2str(jdbcUserpswd);
    	String driverClass = monitor.getString("jdbc.driver");
		sb.append("\r\n\t\t"+name+" "+jdbcUrl+" ");
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
        File file = new File(ConfigUtil.getWorkPath(), "data/monitor/database/"+Tools.encodeMD5(jdbcUrl));
		try 
        {
        	Class.forName(driverClass); 
            con = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);
            monitor.put("Status", 1);
            
            if( driverClass.toLowerCase().indexOf("mysql") != -1 )
            {
	            stmt = con.createStatement();
				rs = stmt.executeQuery("SHOW STATUS;");
				while (rs.next()) monitor.put(rs.getString(1), rs.getString(2));
            }
		    sb.append("ok.");
		    if( file.exists() ){
		    	file.delete();
		    	SysalarmClient.autoconfirm("Sys", "Database", "数据库连接工作正常");
		    }
        }
		catch(Exception e)
		{
		    sb.append(e.toString());
			Log.err("Failed to check "+driverClass+" from "+jdbcUrl);
			monitor.put("Status", 0);
			monitor.put("Exception", e.toString());
			Sysalarm alarm = new Sysalarm();
	        alarm.setSysid("Sys");
	        alarm.setId(ModuleID+"_Database_"+jdbcUrl);
	        alarm.setSeverity(AlarmSeverity.BLACK.getValue());
	        alarm.setType(AlarmType.S.getValue());
	        alarm.setCause(e.toString());
	        alarm.setTitle("从“"+this.sysPerf.getHost()+"”连接数据库【"+name+"】出现异常");
			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
				PrintStream ps = new PrintStream(out);
				e.printStackTrace(ps);
		        alarm.setText("系统监控引擎检查发现连接【数据库"+jdbcUrl+"】异常错误"+e+"，请系统管理员检查相关配置参数("+jdbcUrl+" -p "+jdbcUserpswd+")是否正确设置。\r\n"+out.toString());
				ps.close();
				IOHelper.writeFile(file, alarm.getText().getBytes("UTF-8"));
			}
			catch(Exception e1)
			{
			}
			SysalarmClient.send(alarm);
		}
		finally
		{
			 if( rs != null ) try { rs.close();} catch (SQLException e){}
			 if( stmt != null ) try { stmt.close();} catch (SQLException e){}
			 if( con != null ) try { con.close();} catch (SQLException e){}
		}    	
    }
    
    /**
     * 保存监控采集到的数据到历史文件
     */
    private void save()
    {
        //在data目录下生成monitor/out文件保存系统信息
        F file = new F(ConfigUtil.getWorkPath(), "data/monitor/out");
        IOHelper.writeSerializable(file, sysPerf);
        
    	HostPerfPoint point = new HostPerfPoint();
    	point.setTime(sysPerf.getPerfTime().getTime());
    	point.setCpuload(sysPerf.getCpuLoad());
    	point.setMemusage(sysPerf.getPhyMemUsage());
    	point.setMemused(sysPerf.getPhyMemUsed());
    	point.setNetload0(netload0);
    	point.setNetload1(netload1);
    	point.setIoload0(this.rs);
    	point.setIoload1(this.ws);
        file = new F(ConfigUtil.getWorkPath(), "data/monitor/host");//历史记录
        HostPerfHisotry history = null;
        if( file.exists() )
        {
        	history = (HostPerfHisotry)IOHelper.readSerializableNoException(file);
        }
        if( history == null )
        {
        	history = new HostPerfHisotry();
        }
        history.add(point);
        //删除7天前的历史数据，只保留7天内的数据
        int d = point.getTime() - 8*Tools.SECOND_OF_DAY;
        if( history.get(0).getTime() < d )
        {
        	HostPerfHisotry history0 = new HostPerfHisotry();
        	for( HostPerfPoint p : history )
        		if( p.getTime() > d ) history0.add(p);
        	history = history0;
        }
        IOHelper.writeSerializable(file, history);
    	Log.msg("Succeed to save system-perf to history.");
    }
    
    /**
     * 初始化
     */
    private void initialize()
    {
        try
        {
        	sysPerf.setSecurityKey(getSecurityKey());
        	sysPerf.setHost(Tools.getLocalIP());
        }
        catch(Exception e)
        {
        	Log.err("Failed to initialize"+e);
        	sysPerf.setHost("127.0.0.1");
        }
        //本机IP
    	Log.msg("local:"+sysPerf.getHost());
    	if( !pathPerf.exists() )
    	{
    		pathPerf.mkdirs();
    	}
    	//性能时间
    	sysPerf.setPerfTime(new Date(System.currentTimeMillis()));
        //本机IP
    	String pings = ConfigUtil.getString("monitor.ping"); 
    	Log.msg("ping "+pings);
    	if( pings != null && !pings.isEmpty() )
        {
			String argsIp[] = Tools.split(pings, ",");
			for( String ip : argsIp )
			{
				pingList.add(ip);
			}
        }
    	
//    	String catchhostperf = System.getProperty("monitor.catchhostperf");
//    	Log.msg("catchhostperf:"+catchhostperf);
//    	this.catchHostPerf = "true".equals(catchhostperf);
    	
    	String storages = ConfigUtil.getString("monitor.storages");
    	Log.msg("storages:"+storages);
		String[] argsStorage = Tools.split(storages, ",");
		for (String storage : argsStorage) 
		{
			storageList.add(storage);
		}
    }
    
    /**
     * 获取系统IO负载
     * @param sysPerf
     */
	long rs = 0;
	long ws = 0;
    private void setSystemIOLoad(SystemPerf sysPerf)
    {
        int osType = os_type();
        String commands[] = null;
        String out = null;
        String line = null;
        switch (osType)
        {
        case OS_WinNT:
//        	if( commands != null )
//        		out = Tools.os_exec(commands);
        	break;
        case OS_Linux:
        case OS_Unix:
        	commands = new String[]{"iostat", "-x"};
        	out = Tools.os_exec(commands);
    		try
    		{
    			BufferedReader br = new BufferedReader(new java.io.InputStreamReader(new ByteArrayInputStream(out.getBytes())));
    			boolean begin = false;
    			while((line = br.readLine()) != null )
    			{
//    				System.out.println(line);
    				if( !begin )
    				{
    					int i = line.indexOf("Device:");
    					if( i != -1 )
    					{
    						begin = true;
    					}
    					continue;
    				}
    				int i = 0;
    				String a[] = line.split(" ");
    				for( String p : a )
    				{
    					if( !p.isEmpty() )
    					{
    						i += 1;
    						if( i == 6 )
    						{
    							rs += Double.parseDouble(p)*1024;
    						}
    						else if( i == 7 )
    						{
    							ws += Double.parseDouble(p)*1024;
    							break;
    						}
    					}
    				}
    			}
	            sysPerf.setProperty("IOLoadrs", rs);
	            sysPerf.setProperty("IOLoadws", ws);
    		}
    		catch (Exception e)
    		{
    			Log.err("Exception to decode iostat:\r\n"+out);
    		}
       	 break;
        default:
       	 break;
       }
    }
    /**
     * 设置网络IO负载
     * @param sysPerf
     */
    private int netload0 = 0;
    private int netload1 = 0;
    private void setSystemNetIOLoad(SystemPerf sysPerf)
    {
        int osType = os_type();
        String commands[] = new String[]{"netstat", "-s", "-e"};
        String out = null;
        String line = null;
		String input = null;
		String output = null;
        BufferedReader bufferedReader = null;
		Process process = null;
        switch (osType)
        {
        case OS_WinNT:
    		try
    		{
    			long received = 0;
    			long sent = 0;
    			ProcessBuilder pb = new ProcessBuilder( commands );
	            //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
	            pb.redirectErrorStream( true );
	    		process = pb.start();
	    		bufferedReader = new BufferedReader(
	                new InputStreamReader( process.getInputStream() ) );
	            while ((line = bufferedReader.readLine()) != null)
	            {
	            	line = line.trim();
	            	if( !line.isEmpty() ){
	            		String args[] = Tools.split(line);
	            		if( args.length == 3 ){
	            			if( !Tools.isNumeric(args[1]) || !Tools.isNumeric(args[2]) )
	            			{
	            				Log.err("Failed to decode netstat:"+line);
	            				return;
	            			}
//	            			Log.msg(line);
	            			received = Long.parseLong(args[1]);
	            			sent = Long.parseLong(args[2]);
	            			break;
	            		}
	            	}
	            }
    	        F fileNetStat = new F(pathPerf, "NetStat");
    	        NetStat netStat = null;
    	        if( fileNetStat.exists() )
    	        {
    	        	netStat = (NetStat)IOHelper.readSerializableNoException(fileNetStat);
    	        	long ts = (System.currentTimeMillis() - fileNetStat.lastModified())/1000;
                	Log.msg(String.format("%s(s) received: %s - %s = %s; sent: %s - %s = %s.", ts,
                		received, netStat.getReceived(), received - netStat.getReceived(),
                		sent, netStat.getSent(), sent - netStat.getSent()));
                	
    	        	long iload = (received - netStat.getReceived())/ts;
    	        	long oload = (sent - netStat.getSent())/ts;
    	            sysPerf.setProperty("NetIOLoad0", iload);
    	            sysPerf.setProperty("NetIOLoad1", oload);
    	            if( iload >= 0x80000000L || oload >= 0x80000000L )
    	            {
    	            	Log.war("Found invalid netload "+iload+","+oload);
    	            }
    	            else
    	            {
    	            	this.netload0 = (int)iload;
    	            	this.netload1 = (int)oload;
    	            	Log.msg("Succeed to set netload "+iload+","+oload);
    	            }
    	        }
    	        else
    	        	netStat = new NetStat();
    	        netStat.setReceived(received);
    	        netStat.setSent(sent);
    	        IOHelper.writeSerializable(fileNetStat, netStat);
    		}
    		catch (Exception e)
    		{
    			Log.err("Failed to decode netstat(windows)"+line, e);
    		}
            finally
            {
                if( bufferedReader != null )
                {
                    try
                    {
                        bufferedReader.close();
                        process.destroy();
                    }
                    catch (Exception e)
                    {
                        Log.err(e);
                    }
                }
            }
       	 break;
        case OS_Linux:
        case OS_Unix:
        	commands = new String[]{"netstat", "-s"};
        	out = Tools.os_exec(commands);
    		try
    		{
    			BufferedReader br = new BufferedReader(new java.io.InputStreamReader(new ByteArrayInputStream(out.getBytes())));
    			while((line = br.readLine()) != null )
    			{
    				int i = line.indexOf("InOctets:");
    				if( i != -1 )
    				{
    					i += "InOctets:".length();
    					input = line.substring(i).trim();
    					continue;
    				}
    				i = line.indexOf("OutOctets:");
    				if( i != -1 )
    				{
    					i += "OutOctets:".length();
    					output = line.substring(i).trim();
    					continue;
    				}
    			}
    			if( !Tools.isNumeric(input) || !Tools.isNumeric(output) )
    			{
    				Log.err("Failed to decode netstat:"+out);
    				return;
    			}
    			long received = Long.parseLong(input);
    			long sent = Long.parseLong(output);
    	        F fileNetStat = new F(pathPerf, "NetStat");
    	        NetStat netStat = (NetStat)IOHelper.readSerializableNoException(fileNetStat);
    	        if( netStat != null )
    	        {
    	        	long ts = (System.currentTimeMillis() - fileNetStat.lastModified())/1000;
    	        	long iload = (received - netStat.getReceived())/ts;
    	        	long oload = (sent - netStat.getSent())/ts;
    	            sysPerf.setProperty("NetIOLoad0", iload);
    	            sysPerf.setProperty("NetIOLoad1", oload);
    	            if( iload >= 0x80000000L || oload >= 0x80000000L )
    	            {
    	            	Log.war("Found invalid netload "+iload+","+oload);
    	            }
    	            else
    	            {
    	            	this.netload0 = (int)iload;
    	            	this.netload1 = (int)oload;
    	            	Log.msg("Succeed to set netload "+iload+","+oload);
    	            }
    	        }
    	        else
    	        	netStat = new NetStat();
    	        netStat.setReceived(received);
    	        netStat.setSent(sent);
    	        IOHelper.writeSerializable(fileNetStat, netStat);
    	        br.close();
    		}
    		catch (Exception e)
    		{
    			Log.err("Exception to decode netstat(linux):\t"+out);
    		}
       	 break;
        default:
       	 break;
       }
    }
    
    public SystemPerf getSysPerf() {
		return sysPerf;
	}
	
	public static String[][] Versions = {
		{"3.16.8.28",	"全新数据库监控功能，优化其它监控细节"},
		{"3.16.9.20",	"告警细节问题以及其它BUG"},
		{"3.16.9.24",	"告警自动确认备注"},
		{"3.16.10.4",	"COS服务接口检测"},
		{"3.16.10.18",	"解决内存告警无法自动确认的问题"},
		{"3.16.11.22",	"解决频繁报cos-web的接口服务链接不正常的问题，第一次出现不报告警，连续第二次才报"},
		{"3.17.3.20",	"解决Windows的操作系统采集的英文兼容问题"},
		{"3.17.3.21",	"解决本机环境下初始化IP地址失败和自动生成版本时间线数据失败的问题"},
		{"3.17.6.13",	"针对CAP-API最新的重构实现新的接口监听"},
		{"3.17.6.18",	"Linux环境下Cached占用内存要从内存监控计算公式中乘以80%不算做被占用内存"},
		{"3.17.8.3",	"解决新配置中涉及密码的问题"},
		{"3.18.3.16",	"解决Windows下检测网络流量数据的读取错误问题"},
		{"3.18.4.10",	"继续兼容解决Windows下检测网络流量数据的读取错误问题"},
		{"3.18.5.10",	"解决重复连续告警的问题，不能自动确认告警"},
		{"3.18.6.21",	"增加查看Linux内存情况的特性"},
		{"3.18.6.28",	"解决读取WindowsCPU负载出现负数的问题"},
		{"3.18.7.5",	"优化COS接口检查告警合理性;解决接口检查后不能发送告警的问题"},
		{"3.18.7.16",	"出现COS接口检查不通的情况第一次不发送告警"},
	};
	
    public static void main(String[] args)
    {
        //启动日志管理器
        Log.getInstance().setSubroot(ModuleID);
        Log.getInstance().setDebug(false);
        Log.getInstance().setLogable(true);
        Log.getInstance().start();

        String Version = Versions[Versions.length-1][0];
      	System.out.println("#Version:"+Version);
        StringBuffer info = new StringBuffer("================================================================================================");
		info.append("\r\n"+ModuleID+" "+Version);
		info.append("\r\n\tThe dir of user is "+System.getProperty("user.dir"));
		info.append("\r\n\tCopyright (C) 2008-2016 Focusnt.  All Rights Reserved.");
		info.append("\r\n================================================================================================");
		Log.msg( info.toString() );
        try
        {
            SystemMonitor monitor = new SystemMonitor();
            monitor.initialize();
            monitor.collectSystemInfo();
            monitor.collectHostPerf();
            monitor.collectNetInfo();
            monitor.collectApplicationInfo();
        	monitor.save();
        }
        catch (Exception e)
        {
        	Log.err(e);
        }
        Log.msg("Finish " + ModuleID + " process.");
        //关闭日志管理器
        Log.getInstance().close();
    }
}
