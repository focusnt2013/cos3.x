package com.focus.cos.control;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import com.focus.cos.api.SysalarmClient;
import com.focus.util.Base64;
import com.focus.util.ConfigUtil;
import com.focus.util.F;
import com.focus.util.Log;
import com.focus.util.Tools;

/**
 * 系统内嵌子程序运行器
 * @author focus
 *
 */
public abstract class SystemRunner extends Module
{
	protected String className;
    /*扩展指令*/
    protected ArrayList<String> extendsCommands = new ArrayList<String> ();
    /*扩展参数，在启动指令的后面*/
    protected ArrayList<String> extendsProperties = new ArrayList<String> ();
    /*系统运行间隔频次*/
    protected long frequency;
    /*超时时间*/
    protected long timeout;
//    /*classpath -cp*/
//    private StringBuffer _cp = new StringBuffer();
    /*-classpath 标签*/
    protected String cpTag = "-cp";
    /*classpath*/
    protected StringBuffer cp = new StringBuffer();
    /**/
    protected String ms = "64";
    protected String mx = "512";
    
    /**
     * 构造
     * @param moduleId
     */
	public SystemRunner(String moduleId, ModuleManager manager)
	{
		super(manager);
        this.setId( moduleId );
    	this.setStandby(0);
        this.setPidfile("");
        this.setLogfile("");
        this.setEnabled(true);
        this.setMode(Module.MODE_NORMAL);
        this.setInterruptTime(30000);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("WrapperReport", "v3.16.8.27");
        map.put("Zookeeper", "v3.4.6");
        this.setVersion( map.get(moduleId) );
        this.setDependence(null);
        this.setDelayedStartInterval(0);
        this.setForcereboot(null);
        this.setState(STATE_INIT);
	}
	/**
	 * 初始化程序参数 
	 */
	public abstract void initliaize() throws Exception;

	/**
     * 每隔指定时间执行一次系统监控信息采集，非阻塞方式
     */
    public void startup()
    {
    	if( suspend || !enabled ) return;//....
    	if( monitorGC.execute() )
    	{//执行内存监控
    		this.onChangeStatus(this);
    	}
    	long ts = System.currentTimeMillis() - lastRunTime;
    	if( timeout == 0 ) timeout = frequency;//系统监控的时间间隔(秒)
    	if( subprocess == null && ts > frequency )
    	{//超过时间间隔，同时没有在执行即启动s
//    		if( timeout == 0 && ts < Tools.MILLI_OF_MINUTE ){
//    			return;
//    		}
    		lastRunTime = System.currentTimeMillis();
    		Thread thread = new Thread(this);
    		thread.start();
            if( "Zookeeper".equals(id) || "COSPortal".equals(id) || "COSApi".equals(id) || "ProgramLoader".equals(id))
            {
        		Log.print("[%s][%s] startup.", id, thread.toString());
            }
    	}
    	else if( timeout > 0 && subprocess != null && ts > timeout )
    	{//如果超过时间3倍间隔时间没有完成执行，那么强行中止服务运行
    		subprocess.destroy();
    		Log.war("Abort "+this.getId()+" execute because the time is too long("+Tools.getFormatTime("HH:mm:ss", lastRunTime )+").");
    	}
    }

    /**
     * 从界面发出的重启操作
     */
    public synchronized void restartup()
    {
    	this.signal_shutdown = true;
		Log.msg( "["+id+"] Restartup from control at "+this.getStateInfo() );
    	if( getState() == Module.STATE_STARTUP  )
		{
            setState(STATE_SHUTDOWN_WAIT);
	        Shutdown s = new Shutdown(this){
				public void doShutdown()
				{
					synchronized(this.module)
					{
						module.suspend = false;//从暂停状态恢复
						this.module.notify();
					}
				}
	        };
	        s.start();
		}
    	else
    	{
    		forcekill(true);
			daemon = false;
			interrupt = false;//从中断状态恢复
			suspend = false;//从暂停状态恢复
			this.notify();
    	}
		lastRunTime = 0;
    }
    /**
     * 添加附加的classpath
     * @param path
    protected void appendClasspath(String path)
    {
    	_cp.append(System.getProperty("path.separator", ";"));
    	_cp.append(path);
    }
     */
    
    public void run()
    {
    	StringBuffer sb = new StringBuffer();
        try
        {
        	starting = true;
        	java = true;
    		modulePerf.setType(1);//程序类型JAVA
        	extendsCommands.clear();
            Iterator<?> itr = System.getProperties().keySet().iterator();
            while(itr.hasNext())
            {
            	String key = itr.next().toString();
            	if( key.equals("control.port") || key.equals("file.encoding") )
            	{
                	extendsCommands.add( "-D"+key+"="+System.getProperty(key));
            	}
            }
        	extendsCommands.add( "-Duser.dir="+System.getProperty("user.dir") );
            /*
    # The default database-jdbc of cos, you need to config it if you set true above runner.email & runner.alarm & runner.notify. 
    wrapper.jdbc.driver=org.h2.Driver
    wrapper.jdbc.user=sa
    wrapper.jdbc.password=
    wrapper.jdbc.url=jdbc:h2:tcp://localhost/../h2/cos          
             */
            extendsCommands.add("-Dcos.control.version="+WrapperShell.version());
            extendsCommands.add("-Dcos.service.name="+ConfigUtil.getString("service.name", ""));
            extendsCommands.add("-Dcos.service.desc="+Base64.encode(ConfigUtil.getString("service.desc", "").getBytes("UTF-8")));
        	initliaize();
        	if(manager.isDatabaseStandby()){
        		appendDProperty("cos.jdbc.driver", extendsCommands);
        		appendDProperty("cos.jdbc.url", extendsCommands);
        		appendDProperty("cos.jdbc.user", extendsCommands);
        		appendDProperty("cos.jdbc.password", extendsCommands);
        	}
            appendDProperty("cos.api", extendsCommands);
            appendDProperty("cos.api.port", extendsCommands);
            appendDProperty("cos.id", extendsCommands);
            appendDProperty("cos.identity", extendsCommands);
            appendDProperty("control.port", extendsCommands);

        	F lib = new F(ConfigUtil.getWorkPath(), "lib/");
        	if( !dirLog.exists() ) dirLog.mkdirs();
        	F gcFile = new F(dirLog, Tools.getFormatTime("yyyyMMddHHmmss", System.currentTimeMillis())+".gc");
			String separator_file = System.getProperty("file.separator", "/");
        	String separator_path = System.getProperty("path.separator", ";");
        	if( cp.length() == 0 ){
        		cp.append("."+separator_file);
        		cp.append(separator_path);
        		cp.append(lib.getPath());
        		cp.append(separator_file);
        		cp.append("*");//+_cp.toString();
        	}
        	String jdk = System.getProperty("java.home");
        	if( !jdk.endsWith("bin"+separator_file+"java") )
        		jdk += ""+separator_file+"bin"+separator_file+"java";

        	startupCommands = new ArrayList<String>();
        	startupCommands.add( jdk );
        	startupCommands.add( "-Xms"+ms+"m" );
        	startupCommands.add( "-Xmx"+mx+"m" );
        	startupCommands.add( "-Xloggc:"+gcFile.getAbsolutePath() );
        	startupCommands.add( "-D"+ConfigUtil.getString("service.name")+".subprocess.id="+this.id );
        	startupCommands.add( "-Dcom.sun.management.jmxremote" );
        	startupCommands.add( "-Dfile.encoding=UTF-8" );
        	startupCommands.add( "-Dsun.jnu.encoding=UTF-8" );
        	startupCommands.add( cpTag );
        	startupCommands.add( cp.toString() );
        	for( String command : extendsCommands )
            	startupCommands.add( command );
        	startupCommands.add( className );
        	for( String command : extendsProperties )
            	startupCommands.add( command );

        	sb.append("\r\n\t");
        	for(String command : startupCommands)
        	{
        		if( sb.length() > 0 ) sb.append(' ');
        		sb.append(command);
        	}

        	ProcessBuilder pb = new ProcessBuilder( startupCommands );
            //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
            pb.redirectErrorStream( true );
            subprocess = pb.start();
            this.openSubprocessReader(subprocess, startupCommands);
            printDebug("["+id+"] Startup by below commands"+sb, null);
            executing = true;
            this.modulePerf.setStartupTime(Calendar.getInstance().getTime());
            super.setState(Module.STATE_STARTUP);
            if( "Zookeeper".equals(id) || 
            	"COSPortal".equals(id) ||
            	"COSApi".equals(id) ||
            	"ProgramLoader".equals(id)){
//            	Log.msg("["+id+"] Startup by below commands"+sb);
//            	StringBuilder sb1 = new StringBuilder();
//            	String pid = Subprocessor.findjavapid("-Dcos.subprocess.id="+this.id);
//            	Iterator<String> iterator = map.keySet().iterator();
//            	while(iterator.hasNext()){
//            		String key = iterator.next();
//            		sb1.append("\r\n\t"+key+": "+map.get(key));
//            	}
//            	sb1.append("\r\n\tThe pid of process is "+pid);
//            	System.out.println();
            	System.out.println(String.format("@COS$ Begin to startup %s(after %s ms at %s)", 
					id, System.currentTimeMillis() - lastRunTime, Tools.getFormatTime("HH:mm:ss", lastRunTime )));
				if( "COSPortal".equals(id) ){
					System.out.println(String.format("@COS$ Please wait for moment to open the web-browser(COS-WEB)."));
				}
            	this.startupAutoConfirm();
            }
            process_status_last = process_status;
            process_status = 0;
            process_status = subprocess.waitFor();
            finishTimestamp = System.currentTimeMillis();
            executing = false;
            if( "Zookeeper".equals(id) || "COSPortal".equals(id) || "COSApi".equals(id) || "ProgramLoader".equals(id))
            {
        		Log.print("[%s] shutdown at %s", id, process_status);
            }
            if( process_status == 0 )
            {
                super.setState(Module.STATE_SHUTDOWN, "程序正常退出");
	            countExceptionQuite = 0;
	            if( !manager.isClosing() )
	            {
	            	if( process_status_last != 0 ){
	            		SysalarmClient.autoconfirm("Sys", id+"_Startup", "程序再次启动后正常退出");
	            	}
	            }
            }
            else
            {
            	if( suspend )
                {
            		super.setState(Module.STATE_SUSPEND, "程序被暂停");
                }
            	else if( signal_shutdown )
                {
            		super.setState(Module.STATE_SHUTDOWN, "程序停止");
                }
            	else
            	{
            		super.setState(Module.STATE_SHUTDOWN, "程序异常关闭");
            		if( mode != STATE_SHUTDOWN_WAIT && mode != STATE_SUSPEND_WAIT )
            		{
	            		this.handleExceptionQuite(process_status, null);
            		}
            	}
            }
            subprocess = null;
        }
        catch( Exception e )
        {
            super.setState(Module.STATE_SHUTDOWN, "程序运行异常"+e);
        	handleExceptionQuite(-1, e);
        }
//        if("WrapperReport".equals(id)){
//        	System.out.println();
//        	System.out.println("["+id+"] On finish.");
//        }
        finish();
    	onFinish();
    	starting = false;
    }

    public void shutdown()
    {
    	signal_shutdown = true;
    	close();
    	super.forcekill(false);
    }
    /**
     * 关闭
     */
    public void close()
    {
        Log.msg( "Close the system runner "+this.getId()+"(myprocess="+subprocess+")." );
    	if( subprocess != null )
    	{
    		subprocess.destroy();
    	}
    }

	@Override
	public void onChangeStatus(Module module)
	{
		manager.sendModuleMonitorData(module, ModuleManager.N_MU);
	}

	@Override
	protected void handleProcessOutput(String line) {
	}
}
