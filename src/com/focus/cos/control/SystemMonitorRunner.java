package com.focus.cos.control;

import com.focus.util.ConfigUtil;

public abstract class SystemMonitorRunner extends SystemRunner
{
	public static final String Name = "系统监控程序"; 
	public static final String Remark = "定时采集服务器系统CPU、内存、硬盘的工作状态；根据数据库监控配置执行本机数据库连接监控；执行COS主界面框架系统的WEBService服务连接监测。";
    protected long count_run;//计数运行系统监控的次数
	public SystemMonitorRunner(ModuleManager manager)
    {
    	super("SystemMonitor", manager );
    	this.className = "com.focus.cos.control.SystemMonitor";
        this.setName( Name );
        this.setRemark(Remark);
//    	extendsCommands.add( "-Dmonitor.local="+ConfigUtil.getString("monitor.local") );
//    	extendsCommands.add( "-Dmonitor.ping="+ConfigUtil.getString("monitor.ping") );
//    	extendsCommands.add( "-Dmonitor.catchhostperf="+ConfigUtil.getString("monitor.catchhostperf") );
//    	extendsCommands.add( "-Dmonitor.storages="+ConfigUtil.getString("monitor.storages") );
    }

	@Override
	public void initliaize() throws Exception
	{
    	this.frequency = ConfigUtil.getInteger("monitor.frequency", 30)*1000;
    	timeout = 240000;
    	super.setDebug("true".equalsIgnoreCase(ConfigUtil.getString("monitor.debug", "false")));
    	count_run += 1;
	}
}
