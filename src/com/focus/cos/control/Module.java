package com.focus.cos.control;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.control.ModuleMemeory;
import com.focus.control.ModulePerf;
import com.focus.cos.api.AlarmSeverity;
import com.focus.cos.api.AlarmType;
import com.focus.cos.api.Sysalarm;
import com.focus.cos.api.SysalarmClient;
import com.focus.util.Base64;
import com.focus.util.ConfigUtil;
import com.focus.util.F;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.QuickSort;
import com.focus.util.Subprocessor;
import com.focus.util.Tools;

/**
 * <p>Title: 模块子系统</p>
 *
 * <p>Description: 对主控多每个程序进行监控与管理</p>
 *
 * <p>Copyright: Copyright (c) focusnt 2008-2016</p>
 *
 * <p>Company: </p>
 *
 * @author focus lau
 * @version 1.0
 */
public abstract class Module implements Runnable
{
	protected static final long serialVersionUID = 6399668296699083416L;
	public static final int Shutdown_Type_Socket = 0;
    public static final int Shutdown_Type_Command = 1;
    public static final int Shutdown_Type_Destroy = 2;
    public static final int Shutdown_Type_Pid = 3;

    public static final int STATE_INIT = 0;//待机
    public static final int STATE_STARTUP = 1;//启动
    public static final int STATE_SHUTDOWN_WAIT = 2;//等待关闭
    public static final int STATE_SHUTDOWN = 3;//关闭
    public static final int STATE_SUSPEND_WAIT = 5;//暂停
    public static final int STATE_SUSPEND = 6;//暂停
    public static final int STATE_CLOSE = 7;//关闭
    public static final int STATE_REMOVED = -1;//移除
    public static final String STATE_LABLES[] = {"STATE_INIT", "STATE_STARTUP", "", "STATE_SHUTDOWN_WAIT", "STATE_SHUTDOWN", "STATE_SUSPEND_WAIT", "STATE_SUSPEND", "STATE_CLOSE"};
    
    public static final int MODE_NORMAL = 0;//由主控自动控制启动停止，如果服务关闭就在约定时间restartup重启
    public static final int MODE_SUSPEND = 1;//启动后先暂停，由界面控制启动停止，服务关闭就在约定时间restartup重启
    public static final int MODE_SINGLE = 2;//交给人工控制做单次启动，用户点击一次启动一次
    public static final int MODE_DAEMON_AUTO = 3;//主控负责检查进程是否启动，如果没有就自动启动进程，主控退出时不关闭该进程
    public static final int MODE_DAEMON_HAND = 4;//主控并不负责自动启动或关闭进程;用户只能在界面人工启动或停止服务，记录上次启动时间或关闭以及执行后状态变化
    /*引擎工作模式*/
    protected int mode; 
    /*模块id*/
    protected String id;
    /*模块名称*/
    protected String name;
    /*子进程句柄*/
    protected Process subprocess = null;
    /*模块激活标识*/
    protected boolean enabled = false;
    /*模块是否启动标识*/
    protected boolean starting = false;
    /*模块的版本号*/
    protected String version = null;
    /*关闭子程序的文本*/
    protected String shutdownText;
    /*关闭子程序的二进制码流*/
    protected String shutdownBinary;
    /*关闭子程序的端口*/
    protected int shutdownPort = 8005;
    /*关闭子程序的端口*/
    protected ArrayList<String> startupCommands = new ArrayList<String> ();
    /*关闭子程序的端口*/
    protected ArrayList<String> shutdownCommands = new ArrayList<String> ();
    /*关闭shutdown类型*/
    protected int shutdownType = Shutdown_Type_Destroy;
    /*模块依赖*/
    protected String dependence;
    /*暂停模块执行*/
    protected boolean suspend = false;
    /*模块开始时间戳*/
//    protected long startTimestamp;
    /*模块关闭时间戳*/
    protected long shutdownTimestamp;
    /*延迟启动时间*/
    protected int delayedStartInterval;
    //状态发生改变的时候触发
    private boolean debug = false;
    // 模块可以运行的最早时间
    protected String startTime;
    // 模块可以运行的最晚时间
    protected String endTime;
    // 模块在到达可以运行最晚时间后，如果还在运行，是否需要关闭（通常是强行杀掉）。
    protected boolean isShutdownRunning = false;
    //主备模块0表示非主备模块，1表示备份模块，2表示主备都适用的模块（比如监控模块、数据库模块）
    protected int standby;
    //强行重启的等待时间
    protected String forcereboot;
    //强行重启的触发时间
    protected long forcereboottrigger = 0;
    //模块描述
    protected String remark;
    //日志文件路径
    protected String logfile;
    //Linux PID文件路径（Linux有效）
    protected String pidfile;
    //程序的PID集合
    protected JSONObject pids = new JSONObject();
    //JAVA程序的标识
    protected boolean java;
	//是否守护运行
    protected boolean daemon = false;
	//模块是否持续中断，缺省不是，只有在某个工作模式下才需要持续中断
    protected boolean interrupt = false;
    /*模块重启时间间隔，如果interrupt=false，只中断一个周期*/
    protected int interruptTime = 1000;//缺省中断1秒
    //模块性能对象
    protected ModulePerf modulePerf = new ModulePerf();
    //上次运行系统监控的时间
    protected long lastRunTime = 0;
    //上次检查内存使用情况
    protected long lastGcTime = 0;
    //当前对象实例句柄
    protected Module instance;
    //日志目录
    protected F dirLog = null;
    protected String dirLogMark = null;
    //监控GC文件计算内存的对象
    protected MonitorGC monitorGC;
    //启动进程的返回状态
    protected int process_status = 0;
    //上次的启动状态
    protected int process_status_last = -1;
    /*主控程序*/
    protected ModuleManager manager;

    //是否调试信息输出
    public abstract void onChangeStatus(Module module);
    //结束程序前回调
    public abstract void onFinish();
    
    public Module(ModuleManager manager)
    {
		this.manager = manager;
    	instance = this;
    	monitorGC = new MonitorGC(modulePerf);
    }
    
    public void setMonitorInfo(String info)
    {
    	if( info == null ) return;
    	this.modulePerf.setMonitorInfo(info);
    }
    
    public String getStateInfo()
    {
    	return STATE_LABLES.length>this.modulePerf.getState()?STATE_LABLES[this.modulePerf.getState()]:"Unknown("+this.getModulePerf().getState()+")";
    }
    
    public int getState()
    {
    	return this.modulePerf.getState();
    }
    /**
     * 模块状态变化
     * @param state
     */
    public void setState(int state)
    {
    	this.setState(state, null);
    }
    public void setState(int state, String info)
    {
    	boolean notsame = state != modulePerf.getState();
		modulePerf.setState(state);
    	if( state == STATE_STARTUP )
    	{
    		setMonitorInfo(info==null?"Service startup.":info);
    	}
    	else if( state == STATE_SHUTDOWN_WAIT )
    	{
    		setMonitorInfo(info==null?"Service shutdowning...":info);
    	}
    	else if( state == STATE_SUSPEND_WAIT )
    	{
    		setMonitorInfo(info==null?"Service suspending...":info);
    	}
    	else if( state == STATE_SUSPEND )
    	{
    		setMonitorInfo(info==null?"Service suspend.":info);
    	}
    	else if( state == STATE_SHUTDOWN )
    	{
    		setMonitorInfo(info==null?"Service shutdown.":info);
    	}
    	else if( state == STATE_CLOSE )
    	{
    		modulePerf.setMonitorInfo(info==null?"Service close.":info);
    	}
    	else if( state == STATE_INIT )
    	{
    		if( Module.MODE_SINGLE == this.mode )
    		{
    			setMonitorInfo(info==null?"Service execute wait.":info);
    		}
    		else if( Module.MODE_DAEMON_AUTO == this.mode || Module.MODE_DAEMON_HAND == this.mode )
    		{
    			setMonitorInfo(info==null?"Service daemon.":info);
    		}
    		else
    		{
    			setMonitorInfo(info==null?"Service config initialize.":info);
    		}
        	if( this.startupCommands.isEmpty() )
        	{
//        		modulePerf.setState(this.masterOrStandby()?STATE_STARTUP:STATE_INIT);
        		modulePerf.setState(STATE_INIT);
//        		modulePerf.setStatupTime(Calendar.getInstance().getTime());
//        		setMonitorInfo(this.masterOrStandby()?"Service startup.":"Service standby.");	
        		setMonitorInfo("Service daemon.");	
        	}
    		this.modulePerf.setStartupCommands(startupCommands);
    		this.modulePerf.setShutdownCommands(shutdownCommands);
    	}
    	if( notsame){
    		onChangeStatus(this);
    	}
//    	org.tanukisoftware.wrapper.security.WrapperReport.getInstance();
    }
    
    /**
     * 暂停模块运行
     */
    public void suspend()
    {
    	this.signal_shutdown = true;
    	Log.print( "%s suspend from control.", this.toString() );
    	if( mode==Module.MODE_SINGLE ||
    		mode==Module.MODE_DAEMON_HAND )
    	{
	        this.setState(STATE_SHUTDOWN_WAIT);
    	}
    	else
    	{
	        this.setState(STATE_SUSPEND_WAIT);
	        this.suspend = true;
    	}
        Shutdown s = new Shutdown(this){
			public void doShutdown()
			{
				synchronized(this.module)
				{
			    	if( mode==Module.MODE_SINGLE ||
		        		mode==Module.MODE_DAEMON_HAND )
		        	{
			        	Log.print( "%s shutdown.", this.toString() );
		        	}
		        	else
		        	{
		            	Log.print( "%s suspend.", this.toString() );
						module.daemon = false;
						module.interrupt = false;//从中断状态恢复
		        	}
					this.module.notify();
					if(subprocess==null)
					{
						setState(STATE_SUSPEND);
					}
				}
			}
        };
        s.start();
    }

    /**
     * 从界面发出的重启操作
     */
    public synchronized void restartup()
    {
    	this.signal_shutdown = true;
		Log.print( "%s restartup from control.", this.toString());
    	if( getState() == Module.STATE_STARTUP  )
		{
			Log.msg( "["+id+"] Todo shutdown." );
            setState(STATE_SHUTDOWN_WAIT);
	        Shutdown s = new Shutdown(this){
				public void doShutdown()
				{
					synchronized(this.module)
					{
						module.daemon = false;
						module.interrupt = false;//从中断状态恢复
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
    }
    /*关闭信号*/
    protected boolean signal_shutdown = false;
    /**
     * 停止模块
     */
    public void shutdown()
    {
        if( MODE_DAEMON_HAND == mode || MODE_DAEMON_AUTO == mode )
        {//如果工作模式是守护模式，除了指令控制关闭，否则都不能执行关闭
            Log.war( "["+id+"] Not need to shutdown for daemon("+mode+")." );
        	return;
        }
        Shutdown s = new Shutdown(this){
			public void doShutdown()
			{
			}
        };
        s.start();
    }
    /**
     * 关闭模块引擎
     * @author Focus Lau
     *
     */
    abstract class Shutdown extends Thread
    {
    	Module module;
    	public Shutdown(Module module)
    	{
    		this.module = module;
    	}
    	
    	public abstract void doShutdown();
    	public void run()
    	{
            switch( shutdownType )
            {
                case Shutdown_Type_Socket:
                    try
                    {
                        Log.war( "["+id+"] Shutdown by socket." );
                        Socket s = new Socket( "127.0.0.1", shutdownPort );
                        OutputStream out = s.getOutputStream();

                        if(getShutdownText() != null)
                        {
                        	out.write( shutdownText.getBytes() );
                        }
                        else if(getShutdownBinary() != null)
                        {
                        	out.write( Tools.StringToByteArray(shutdownBinary));
                        }
                        out.flush();
                        Thread.sleep(500);
                        out.close();
                        s.close();
                    }
                    catch( Exception e )
                    {
                    	synchronized( instance )
                    	{
	                        if( subprocess != null )
	                        {
	                            Log.war( "["+id+"] Force to shutdown" );
	                    		try {
	                    			subprocess.destroy();
									instance.wait(5000);
								} catch (Exception e1) {
								}
	                        }
                    	}
                    }
                    break;
                case Shutdown_Type_Command:
                	if( mode == MODE_DAEMON_AUTO || mode == MODE_DAEMON_HAND )
                	{
	                	Log.war( "["+id+"] Shutdown by command "+ getCommonds(shutdownCommands) );
	                	String result = Tools.os_exec(shutdownCommands.toArray(new String[shutdownCommands.size()]));
                    	Log.msg( "["+id+"] Execute shutdown and result is " + result);
                	}
                	else
                	{
	                	if( shutdownTimestamp == 0 ||
	                		System.currentTimeMillis() - shutdownTimestamp > 10000 )
	                	{//如果第一次关闭或者离上次关闭时间已经过了10秒
	                		shutdownTimestamp = System.currentTimeMillis();
	    	                try
	    	                {
	    	                	Log.war( "["+id+"] Shutdown by command " + getCommonds(shutdownCommands) );
//	    	                	if( !shutdownCommands.isEmpty() && shutdownCommands.get(0).indexOf("java") != -1 && ConfigUtil.getWorkPath().indexOf("IDE") !=-1 )
//	    	                	{
//	    	                		break;//本地调测可不执行停止指令.
//	    	                	}
	    	                    ProcessBuilder pb = new ProcessBuilder( shutdownCommands );
	    	                    //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
	    	                    pb.redirectErrorStream( true );
	    	                    Process processShutdwon = pb.start();
	    	                    openSubprocessReader( processShutdwon, shutdownCommands );
	    	                    int status = processShutdwon.waitFor();
	    	                    if( status == 0 )
	    	                    {
	    	                    	Log.msg( "["+id+"] Succeed to execute the command of shutdown (" + status + ")." );
	    	                    	synchronized( instance )
	    	                    	{
	    	                    		instance.wait(5000);
	    	                    	}
	    	                    }
	    	                    else
	    	                    {
	    	                    	Log.war( "["+id+"] Failed to execute the command of shutdown (" + status + ")." );
		                        	synchronized( instance )
		                        	{
			    	                    if( subprocess != null )
			    	                    {
			    	                        Log.war( "["+id+"] Force to shutdown " + id );
			    	                        subprocess.destroy();
											instance.wait(5000);
			    	                    }
		                        	}
	    	                    }
	    	                }
	    	                catch( Exception e )
	    	                {
	                        	synchronized( instance )
	                        	{
		    	                    if( subprocess != null )
		    	                    {
		    	                        Log.war( "["+id+"] Force to shutdown " + id );
		    	                        subprocess.destroy();
		    	                    }
	                        	}
	    	                }
	                	}
                	}
                    break;
                case Shutdown_Type_Pid:
                	F filepid = getPidfile();
                	if( filepid != null )
                	{
                		try
                		{
                			String pid = new String(IOHelper.readFirstLine(filepid));
                			if( Tools.isNumeric(pid) )
                			{
	    	                	Log.war( "["+id+"] Shutdown by pid("+pid+") from " + filepid );
	    	                	String result = Subprocessor.kill(pid);
    	                    	Log.msg( "["+id+"] Execute kill the process("+pid+") and result is '" + result + "'" );
                			}
                			else
                			{
	    	                	Log.war( "["+id+"] Failed to kill the process("+pid+") from " + filepid );
                				
                			}
                		}
                		catch(Exception e)
                		{
	                    	Log.war( "["+id+"] Failed to kill the process by pidfile(" + filepid + ") for exception "+e );
                		}
                	}
                	break;
                default:
                	synchronized( instance )
                	{
	                    if( subprocess != null )
	                    {
	                        Log.war( "["+id+"] Shutdown by destroy." );
	                        try
	                        {
	                        	subprocess.destroy();
	                            instance.wait(5000);//等5秒钟直到收到信号
	                        }
	                        catch( Exception e )
	                        {
	                            Log.err( "["+id+"] Failed to execute shutdown", e);
	                        }
	                    }
                	}
                    break;
            }
        	synchronized( instance )
        	{
                forcekill(false);
        	}
        	doShutdown();
    	}
    }
    
    /**
     * 强行关闭 kill -9
     * @param quite
     */
    private StringBuffer lastQuiteLogs = new StringBuffer();//上次退出日志
    public StringBuffer getLastQuiteLogs() {
		return lastQuiteLogs;
	}

	protected void forcekill(boolean quite){
        //检查pid文件是否还存在，如果还存在强行干掉
    	if( pids.length() == 0 ){
    		modulePerf.setProcessInfo("{}", 0);
    		if( signal_shutdown ) modulePerf.setDead("");
    		return;//没有PID数据，无法执行强制杀
    	}
        if( signal_shutdown ){//是用户发起的关闭记录该日志
        	Log.war(String.format("[%s] Todo force(quite:%s) kill %s", id, quite, this.pids.toString(4)));
        }
        lastQuiteLogs = new StringBuffer(String.format("[%s] Check its pid(%s) for shutdown(%s)", id, pids.length(), signal_shutdown));
        Iterator<?> allpids = pids.keys();
        ArrayList<String> removing = new ArrayList<String>();
        boolean a = false;
        while(allpids.hasNext()){
        	String pid = allpids.next().toString();
        	lastQuiteLogs.append(String.format("\r\n\tFound pid %s", pid));
        	boolean r = Subprocessor.pid(pid);
//        	if( quite ){
//            	System.out.println(String.format("\t[%s][%s] The pid(%s) exist %s.",
//            		id, Tools.getFormatTime("mm:ss SSS", System.currentTimeMillis()), pid, r));
//        	}
        	if( r ){
        		lastQuiteLogs.append(String.format(" running."));
            	if( daemon ){
            		lastQuiteLogs.append(String.format("\tThe process is damon."));
            	}
            	else{
            		try{
                		F f = new F(ConfigUtil.getWorkPath(), "log/"+id+"/the_program_can_not_quite_by_signal.txt");
            			String ret = Subprocessor.forcekill(pid);
            			lastQuiteLogs.append(String.format("\tForce kill result is '%s'.", ret));
            			Sysalarm alarm = new Sysalarm();
            	        alarm.setSysid(sysid);
            	        alarm.setResponser(modulePerf.getProgrammer());
            	        alarm.setContact(modulePerf.getProgrammerContact());
            	        alarm.setSeverity(AlarmSeverity.ORANGE.getValue());
            	        alarm.setType(AlarmType.S.getValue());
            	        alarm.setId(id+"_Shutdown");
            	        alarm.setTitle("主控运行程序["+name+"]ID["+id+"]发现该进程不能用正常的关闭信号退出");
            	        StringBuffer w = new StringBuffer("该程序进程["+pid+"]在程序退出后仍然在运行，可能的原因包括程序没有资源释放逻辑、程序出现异常无法正确退出，以及操作系统出现问题。");
            	        alarm.setCause(w.toString());
            	        if(debugLog.size()>0){
            	        	w.append("该程序最近"+debugLog.size()+"条程序日志");
            	        	w.append("\r\n... ...");
            	        	for(String s : debugLog)
            	        		w.append("\r\n"+s);
            	        	try {
            	        		IOHelper.writeFile(f, w.toString().getBytes("UTF-8"));
            	        	} catch (UnsupportedEncodingException e) {
            	        	}
            	        }
            			w.append("\r\n【重要提示 】该程序在收到正常的关闭信号后无法正常退出进程被主控强制kill"+
    					"，有可能会造成该程序的数据异常，需要开发人员优化");
            	        alarm.setText(w.toString());
            	        this.modulePerf.setDead(alarm.getText());//设置死进程标识
            			SysalarmClient.send(alarm);//向系统管理员发送告警
            		}
            		catch(Exception e){
            			Log.err(e);
            		}
        			a = true;
            	}
        	}
        	else{
        		lastQuiteLogs.append(" quite bingo.");
    			this.modulePerf.setDead(null);
        	}
        	removing.add(pid);
        }
        if( signal_shutdown ){//是用户发起的关闭记录该日志
        	lastQuiteLogs.append("\r\n\t::"+a);
        	Log.war(lastQuiteLogs.toString());
        }
        
        if( pids.length() > 1 ){
        	SysalarmClient.autoconfirm("Sys", id + "_Zombie", 
        			String.format("程序[%s %s]的僵尸进程已经全部关闭: %s", id, name, pids.toString(4)));
        }
        
        if( !a ){
    		F f = new F(ConfigUtil.getWorkPath(), "log/"+id+"/the_program_can_not_quite_by_signal.txt");
    		if( f.exists() ){
    			f.delete();
            	SysalarmClient.autoconfirm("Sys", id + "_Shutdown", 
        			String.format("程序[%s %s]收到关闭信号后以及可以正常退出", id, name));
    		}
        }

        for(String pid : removing ){
        	pids.remove(pid);
        }
    }

    /**
     * 检查模块是否具备启动条件
     */
    public void startup()
    {
    	if( pidfile != null && !pidfile.isEmpty() ) 
    	{//如果pid文件做了配置，检查内存
            manager.status("[%s] check the memory by pid from %s.", id, this.pidfile);
    		this.checkMemoryFromPid(this.getPidfile());
            manager.status("[%s] change the status.", id);
    		this.onChangeStatus(this);
    	}
    	else if( monitorGC.execute() ) 
    	{//如果pid文件没有配置，执行gc检查
            manager.status("[%s] check the memory by java-gc.", id);
    		this.onChangeStatus(this);
    	}

    	if( suspend )
    	{
    		return;
    	}
    	
    	if( this.starting && ( forcereboottrigger > 0 && forcereboottrigger < System.currentTimeMillis() ) )
    	{//当前正在运行同时触发了强制重启的时间，那么强制重启服务
    		signal_shutdown = true;//给一个关闭信号
    		this.shutdown();//关闭服务
    		return;
    	}
    	
    	boolean isEnabled = isEnabled();
    	boolean isValidTime = isValidTime();
    	//模块是激活的，同时在有效时间内，同时双机有效
        if( isEnabled && isValidTime )
        {
        	//判断模块是否处于启动中
            if( !starting )
            {//启动模块线程
                manager.status("[%s] startup.", id);
                Thread thread = new Thread( this );
                thread.start();
            }
        }
        else if(isEnabled && !isValidTime && !this.isShutdownRunning)
        {//模块是激活的，但是不在有效运行时间范围内，
        	// 如果一切条件正常的情况下，如果超过了许可时间，但是不要求强行关闭模块
        	// 不做任何事情
//        	if( !isValidTime && starting )
        	if( starting )
        	{
                manager.status("[%s] shutdown.", id);
            	this.shutdown();
        	}
        }
        else
        {
            //如果不允许启动则停止线程
            if( starting )
            {
                manager.status("[%s] Close.", id);
            	this.close();
            }
        }
        manager.status("[%s] Finish startup.", id);
    }
    /**
     * 打开子进程阅读器
     */
    protected void openSubprocessReader( Process p, ArrayList<String> commands )
    {
        Thread thread = new SubprocessReader(id, p, commands){
			@Override
			public void hanldeException(String line)
			{
				if( line.indexOf("INFO") != -1 ) return;
				if( "Zookeeper".equals(id) ){
					if(line.endsWith("caught end of stream exception") ){
						return;
					}
					if(line.endsWith("likely client has closed socket") ){
						return;
					}
					return;
				}
				synchronized(theExceptions){
					timestamp = System.currentTimeMillis();
					if(timestamp-lastExceptionTimestamp>Tools.MILLI_OF_MINUTE){
						lastExceptionTimestamp = System.currentTimeMillis();
						theExceptions.addLast("=="+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", timestamp)+"==");
					}
					countException += 1;
					theExceptions.addLast(line);
					if( theExceptions.size() > 64 ) theExceptions.pollFirst();
					if( timestamp-alarmTimestamp>Tools.MILLI_OF_MINUTE ){
						alarmTimestamp = timestamp;//
						alarmTimestamp /= 1000;
						alarmTimestamp *= 1000;
						alarmTimestamp += 1;
						return;
					}
					alarmTimestamp += 1;
					if(timestamp%1000<=2){
						return;
					}
				}
				if( this.alarmThread != null ){
					return;
				}
				if( !manager.isClosing() ){
					alarmThread = new Thread(){
						public void run()
						{
							try
							{
								int count = 0;
								while( System.currentTimeMillis()-timestamp < 3000 && count < 10 )
								{
									if( subprocess == null ){
										break;
									}
									count += 1;
									sleep(3000);//上次错误信息发生时间如果小于3秒就等待
								}
								if( subprocess != null && !manager.isClosing() ){
									if( modulePerf.getPrintException() == null ||
										modulePerf.getPrintException().indexOf("退出关闭异常") == -1 ){
										Sysalarm alarm = new Sysalarm();
										alarm.setSysid(sysid);
										alarm.setResponser(modulePerf.getProgrammer());
										alarm.setContact(modulePerf.getProgrammerContact());
										alarm.setSeverity(AlarmSeverity.YELLOW.getValue());
										alarm.setType(AlarmType.S.getValue());
										alarm.setId(id+"_Exception");
										alarm.setCause("程序BUG");
										alarm.setTitle("程序["+name+"]ID["+id+"]连续出现疑似异常信息");
										StringBuffer sb = new StringBuffer();
										synchronized(theExceptions){
											if( !theExceptions.isEmpty() ) sb.append("\r\n\t... ...");
											for(String s : theExceptions)
												sb.append("\r\n\t"+s);
										}
										alarm.setText(String.format("程序启动于%s，程序负责人【%s】，运行生命周期中出现%s次疑似异常信息，日志摘要如下详情请查看完整日志：%s", 
												modulePerf.getStartupTime(), modulePerf.getProgrammer(), countException, sb.toString()));
										modulePerf.setCountException(countException);
										modulePerf.setPrintException(alarm.getText());
										SysalarmClient.save(alarm);//向系统管理员发送告警
										Log.print("[Thread:%s] Send the alarm(%s) to master, %s exceptions recevied, %s logs saved.",
												this.getId(), alarm.getTitle(), countException, theExceptions.size());
										manager.sendModuleMonitorData(instance, ModuleManager.N_MU);//因为程序状态或者内存不变化不会向Portal推送模块状态信息
									}
								}
							}
							catch(Exception e)
							{
								Log.err("Failed to send alarm", e);
							}
			        		alarmThread = null;
						}
					};
					alarmThread.start();
				}
			}

			@Override
			public void handleOutput(String line)
			{
				if( line.startsWith("#Version:") ){
					setVersion( line.substring("#Version:".length()) );
//					Log.msg(String.format("[%s] Get the version %s from process.", id, getVersion()));
				}
				else{
					printDebug(line, null);
					handleProcessOutput(line);
					debugLog.push(line);
					if( debugLog.size() > 16 ) debugLog.pollLast();
				}
			}
        };
        thread.start();
    }
    
    /**
     * 处理模块输出
     * @param line
     */
    protected abstract void handleProcessOutput(String line);
    
    /*通过程序输出流捕获的日志记录*/
    private LinkedList<String> debugLog = new LinkedList<String>();//调试记录
    /**
     * 打印调试信息
     * @param line
     */
    protected F debugOutputF;//打印调试信息到文件
    protected PrintWriter debugOutputFPrinter;//打印调试信息到文件
    protected PrintWriter debugOutputSocketPrinter;//打印调试信息到Socket通道流
    protected synchronized void closeDebugPrinter(){
    	if( debugOutputFPrinter != null ){
    		debugOutputFPrinter.close();
    		debugOutputFPrinter = null;
    	}
    }
    
    protected synchronized void printDebug(String line, Exception exception)
    {
		this.status("[打印调测]进入");
    	if( debugOutputFPrinter == null ){
    		if( debug ){
    			F dir = new F(ConfigUtil.getWorkPath(), "log/"+id);
    			if( !dir.exists() ){
    				dir.mkdirs();
    			}
    			this.status("[打印调测]日志目录: %s", dir.getPath());
    			try
    			{
    				debugOutputF = new F(dir, "debug.txt");
    				if(debugOutputF.exists() && debugOutputF.length() > 0 ){
    					F file = new F(dir, "debug_"+Tools.getFormatTime("yyyyMMddHHmmss")+".txt");
    					boolean r = debugOutputF.renameTo(file);
    					if( !r ){
    						Log.print("[%s]Failed to save the history of debug to %s.", id, file.getName());
    					}
    				}
    				debugOutputFPrinter = new PrintWriter(debugOutputF);
    			}
    			catch (Exception e)
    			{
    				Log.err("["+id+"] Failed to create the printer of debug", e);
    			}
    			this.clearDebug(dir);
    		}
    	}
		if( debugOutputFPrinter != null )
			try
			{
    			this.status("[打印调测]打印");
				debugOutputFPrinter.println(line);
				debugOutputFPrinter.flush();
				if( this.debugOutputF.length() > 1024*1024*10 )
				{
					debugOutputFPrinter.close();
					debugOutputFPrinter = null;
				}
			}
			catch(Exception e)
			{
				Log.err("Failed to write debug to the file of "+id, e);
				debugOutputFPrinter.close();
				debugOutputFPrinter = null;
			}
		if( debugOutputSocketPrinter != null )
			try
			{
    			this.status("[打印调测]SOKCET");
				debugOutputSocketPrinter.println(line);
				debugOutputSocketPrinter.flush();
				if( exception != null ) exception.printStackTrace(debugOutputSocketPrinter);
			}
			catch(Exception e)
			{
				Log.err("Failed to print the debug to socket("+socketDebug.getInetAddress()+":"+socketDebug.getPort()+").");
				try
				{
					debugOutputSocketPrinter.close();
				}
				catch (Exception e1)
				{
					Log.err("Failed to close the printer of debug for exception", e);
				}
				if( socketDebug != null )
					try
					{
						socketDebug.close();
					}
					catch (Exception e1)
					{
						Log.err("Failed to close the socket of debug for exception", e);
					}
			}
    }
    
    /**
     * 清除多余的Debug日志
     * @param dir
     */
    private void clearDebug(F dir){
    	F[] files = dir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File file, String name) {
				return name.startsWith("debug_");
			}
			
		});
    	if( files.length > 10 ){
			Log.msg("Found the debug("+files.length+") of "+dir.getName()+" need to clear all.");
			for(File file : files){
				file.delete();
			}
    	}
    	else if( files != null && files.length > 5 ){
			StringBuilder sb = new StringBuilder("Found the debug("+files.length+") of "+dir.getName()+" need to clear:");
			QuickSort sorter = new QuickSort(){
				@Override
				public boolean compareTo(Object sortSrc, Object pivot) {
					return ((File)sortSrc).lastModified()>((File)pivot).lastModified();
				}
				
			};
			sorter.sort(files);
			for(int i = 5; i < files.length; i++){
				File file = files[i];
				sb.append("\r\n\t"+file.getPath());
				boolean r = file.delete();
				sb.append("\tremoved "+r);
			}
			Log.msg(sb.toString());
		}
//		if("games-web".equalsIgnoreCase(dir.getName()))
    }

	public void setDebug(boolean debug)
	{
		this.debug = debug;
		this.switchDebug();//开关调试信息
	}
	/**
	 *设置调试开关
	 * @return
	 */
	public synchronized void switchDebug()
	{
		if( debug )
		{
//			if( debugOutputFPrinter == null )
//				try
//				{
//    				debugOutputF = new F(ConfigUtil.getWorkPath(), "log/"+id+"/debug.txt");
//					debugOutputFPrinter = new PrintWriter(debugOutputF);
//				}
//				catch (FileNotFoundException e)
//				{
//				}
		}
		else
		{
			if( debugOutputFPrinter != null )
			{
				debugOutputFPrinter.close();
				debugOutputFPrinter = null;
			}
		}
	}
	
	private Socket socketDebug;
	public synchronized void openSocketDebug(InetAddress addr, int port)
	{
		try
		{
			Thread.sleep(700);
			socketDebug = new Socket(addr, port);
			this.debugOutputSocketPrinter = new PrintWriter(socketDebug.getOutputStream());
			debugOutputSocketPrinter.println("================================================================");
			debugOutputSocketPrinter.println("The debug of "+id+" has been opened.");
			debugOutputSocketPrinter.println("================================================================");
			Log.msg("Succeed to open debug socket("+addr+":"+port+");");
		}
		catch (Exception e)
		{
			Log.err("Failed to open debug for "+e.getMessage());
		}
	}
    /**
     * 进程退出后调用
     * @param status
     */
    protected int countExceptionQuite = 0;
    protected synchronized void handleExceptionQuite(int status, Exception e)
    {
		this.status("[程序异常退出处理]进入, 有异常对象:%s", e!=null);
        if( this.manager.isClosed() || this.manager.isClosing() )
        {
        	return;
        }
        this.status("[程序异常退出处理]执行GC分析，如果是JAVA程序");
    	if( monitorGC.execute() )
    	{//执行内存监控
    		this.status("[程序异常退出处理]调用状态改变回调");
    		this.onChangeStatus(this);
    	}
    	countExceptionQuite += 1;
    	this.status("[程序异常退出处理]次数%s", countExceptionQuite);
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("程序[%s]ID[%s]退出关闭异常(进程退出码%s)", name, id, status));
		sb.append("\r\n该程序已出现"+countExceptionQuite+"次异常日志，详情请查看程序日志。");
		if( e != null )
			try
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
				PrintStream ps = new PrintStream(out);
				e.printStackTrace(ps);
				sb.append("\r\n");
				sb.append(out.toString());
				ps.close();
			}
			catch(Exception e1)
			{
			}

		this.status("[程序异常退出处理]检查死进程%s", pids.length());
		//读取最后一段系统日志
		if( pids.length() > 0 ){
			sb.append("\r\n\r\n程序有"+pids.length()+"个进程"+pids.toString(4));
			
		}
		else if( !startupCommands.isEmpty() ){
			sb.append("\r\n\r\n程序还没有产生PID进程信息，启动指令如下所示:");
			for( String command : startupCommands )
				sb.append("\r\n\t"+command);
		}
		else{
			sb.append("\r\n\r\n程序没有启动指令");
		}
		//读取最后一段系统日志
		if( !debugLog.isEmpty() ){
			sb.append("\r\n\r\n以下是捕获的最近"+debugLog.size()+"条程序日志");
			sb.append("\r\n\t... ...");
			for(String s : debugLog)
				sb.append("\r\n\t"+s);
		}
		this.status("[程序异常退出处理]发送告警");
		Sysalarm alarm = new Sysalarm();
        alarm.setSysid(sysid);
        alarm.setResponser(modulePerf.getProgrammer());
        alarm.setContact(modulePerf.getProgrammerContact());
        alarm.setSeverity(AlarmSeverity.YELLOW.getValue());
        alarm.setType(AlarmType.S.getValue());
        alarm.setId(id+"_Startup");
        alarm.setTitle("程序["+name+"]ID["+id+"]退出关闭出现异常(关闭信号:"+status+")");
        alarm.setCause("应用引擎出现故障或者变成僵死进程，请分析程序日志解决");
        if( e != null ) alarm.setCause(e.getMessage());
		sb.append("\r\n【重要提示 】解决告警后如果启动正常，该告警将自动确认关闭。");
        alarm.setText(sb.toString());
		SysalarmClient.send(alarm);//向系统管理员发送告警
		
		if( e != null ){
			Log.err(String.format("[%s] Found the process of program quite by exception"+
				"\r\n\tThe count of quite is %s"+ 
				"\r\n\tThe alarm has been sent to cos-web", 
				id, countExceptionQuite), e);
		}
		else{
			final Random random = new Random();
			int r = random.nextInt(10)+1;
			if( countExceptionQuite % r == 0 )
			{
				Log.err(String.format("[%s] Found the process of program quite by exception"+
					"\r\n\tThe count of quite is %s"+ 
					"\r\n\tThe alarm has been sent to cos-web", 
					id, countExceptionQuite));
			}
		}
		modulePerf.setCountException(countExceptionQuite);
		modulePerf.setPrintException(alarm.getText());;
		this.status("[程序异常退出处理]完成退出");
    }
    
    public synchronized void close()
    {
        starting = false;
        this.shutdown();
        this.notify();
    }
    
    /**
     * 强行中止
     */
    public synchronized void abort()
    {
        if( subprocess != null )
        {
        	try
        	{
	            Log.war( "["+id+"] Force to abort." );
	            subprocess.destroy();
        	}
        	catch(Exception e)
        	{
        		Log.err("["+id+"] Failed to force to abort.", e);
        	}
        }
    }
    /*程序执行中*/
    protected boolean executing;
    public boolean isExecuting()
    {
        return executing;
    }

    /**
     * 判断模块是否在允许的时间范围内
     * @return
     */
    public boolean isValidTime()
    {
    	long startTime = 0l;
    	long endTime = Long.MAX_VALUE;
    	long nowTime  = 0;
    	
    	if(this.getStartTime() == null || this.getEndTime() == null)
    	{
    		return true;
    	}
    	
    	try
    	{
    		String starthhmm[] = this.getStartTime().split(":");
    		String endhhmm[] = this.getEndTime().split(":");
    		
        	Calendar now = Calendar.getInstance();
        	nowTime = now.getTimeInMillis();
    		now.set(Calendar.HOUR_OF_DAY, Integer.valueOf(starthhmm[0]));
    		now.set(Calendar.MINUTE, Integer.valueOf(starthhmm[1]));
    		now.set(Calendar.SECOND, 0);
    		startTime = now.getTimeInMillis();
    		now.set(Calendar.HOUR_OF_DAY, Integer.valueOf(endhhmm[0]));
    		now.set(Calendar.MINUTE, Integer.valueOf(endhhmm[1]));
    		now.set(Calendar.SECOND, 59);
    		endTime = now.getTimeInMillis();
    		
    	}
    	catch (Exception e) {
//    		e.printStackTrace();
		}
    	
    	return startTime <= nowTime && nowTime <= endTime;
    }
    
    
    /**
     * 执行启动
     */
    protected void executeStartup()
    {
    	if( daemon || interrupt || suspend ) return;//如果处于守护状态不执行启动
    	if( startupCommands.isEmpty() )
    	{
        	//如果工作模式是daemon*那么进入守护模式daemon=true
        	daemon = mode==Module.MODE_DAEMON_AUTO||mode==Module.MODE_DAEMON_HAND;
        	//如果工作模式是单步模式，那么中断interrupt=true进入持续中断状态
        	interrupt = mode==Module.MODE_SINGLE;
    		return;
    	}
    	if( this.mode == MODE_DAEMON_AUTO || MODE_DAEMON_HAND == this.mode )
    	{//判断守护进程是否真正运行
    		if( isRunFromPid() )
    		{//模块已经运行，直接返回
    			daemon = true;
    			interrupt = false;
    			return;
    		}
    	}
        try
        {
        	ArrayList<String> commands = new ArrayList<String>();
        	java = false;
            for( String command : startupCommands )
            {
            	commands.add( command );
            	if( !java && command.indexOf("java") != -1 )
            	{
            		java = true;
            		modulePerf.setType(1);//程序类型
            		F dirLog = new F("../log/"+id);
            		if( !dirLog.exists() ){
            			dirLog.mkdirs();
            		}
                	F file = new F(dirLog, Tools.getFormatTime("yyyyMMddHHmmss", System.currentTimeMillis())+".gc");            		
//                	Log.msg("Start GC monitor(out:"+file.getPath()+")");
                	commands.add( "-Xloggc:"+file.getAbsolutePath() );
                	commands.add( "-D"+ConfigUtil.getString("service.name")+".subprocess.id="+this.id );
                	commands.add( "-Dcom.sun.management.jmxremote" );
                	commands.add( "-Dfile.encoding=UTF-8" );
                	commands.add( "-Dsun.jnu.encoding=UTF-8" );
                	commands.add( "-Duser.dir="+System.getProperty("user.dir") );
                    appendDProperty("cos.api", commands);
                    appendDProperty("cos.api.port", commands);
                    appendDProperty("cos.id", commands);
                    appendDProperty("cos.identity", commands);
                    appendDProperty("control.port", commands);
            		commands.add("-Dcos.control.version="+WrapperShell.version());
            		commands.add("-Dcos.service.name="+ConfigUtil.getString("service.name", ""));
            		commands.add("-Dcos.service.desc="+Base64.encode(ConfigUtil.getString("service.desc", "").getBytes("UTF-8")));
            	}
            }
            ProcessBuilder pb = new ProcessBuilder( commands );
            //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
            pb.redirectErrorStream( true );
    		this.setForcereboot(this.forcereboot);//设置下次强制重启时间
    		subprocess = pb.start();
    		openSubprocessReader( subprocess, commands );
            this.modulePerf.setStartupTime(Calendar.getInstance().getTime());
            setState(STATE_STARTUP);
            startupAutoConfirm();
            process_status_last = process_status;
            process_status = 0;
            this.executing = true;
    		signal_shutdown = false;
            process_status = subprocess.waitFor();
            finishTimestamp = System.currentTimeMillis();
            this.executing = false;
        	//如果工作模式是daemon*那么进入守护模式daemon=true
        	daemon = mode==Module.MODE_DAEMON_AUTO||mode==Module.MODE_DAEMON_HAND;
            if( this.daemon )
            {//如果是守护模块就将启动指令输出
            	Log.msg("["+id+"] Startup("+process_status+", mode="+this.mode+"):"+getCommonds(commands));
            }
        	//如果工作模式是单步模式，那么中断interrupt=true进入持续中断状态
        	interrupt = mode==Module.MODE_SINGLE;
//			this.status("执行退出逻辑，daemon=%s, interrupt=%s", daemon, interrupt);
            //启动模式
        	if( !daemon )
        	{
    			if( process_status != 0 && !suspend ){
    				if( !signal_shutdown ){
//    					this.status("处理异常退出%s", process_status);
    					this.handleExceptionQuite(process_status, null);//父程序执行的关闭会返回143，这种情况下不告警
//    					this.status("因为程序错误码退出打印启动指令");
    					printDebug("["+id+"] Startup:"+getCommonds(commands), null);
    				}
    			}
    			else
    			{
    				printDebug( "["+id+"] Process quite for(" + process_status + ").", null );
    	            countExceptionQuite = 0;
    	            if( !this.manager.isClosed() )
    	            {
    	            	if( process_status_last != 0 ){
    	            		SysalarmClient.autoconfirm(sysid, id+"_Startup", "程序再次启动后正常退出");
    	            	}
//    	            	SysalarmClient.autoconfirm(sysid, id+"_Exception", "程序再次启动后正常退出");
    	            }
    			}
    			setState(Module.STATE_SHUTDOWN, "Service quit("+process_status+")");
        	}
        }
        catch( Exception e )
        {
            //因为程序如果是因为程序文件不在或者其它原因，就没法确认这个错误了。
            setState(Module.STATE_SHUTDOWN, "Failed to startup service for "+e.toString());
            handleExceptionQuite(-1, e);
        }
        closeDebugPrinter();//关闭debug输出
		this.status("调用完成函数");
    	this.finish();
		this.status("完成回调");
        this.onFinish();
    }
    /*模块管理器状态*/
	private String status;
	/*模块管理器状态时间*/
	private long status_timestamp;
	/**
	 * 记录状态
	 * @param info
	 * @param args
	 */
    public void status(String info, Object... args){
    	status_timestamp = System.currentTimeMillis();
    	if( args == null || args.length == 0 ) status = info;
    	else status = String.format(info, args);
	}
    
    /*结束进程时间戳*/
    protected long finishTimestamp;
    public long getFinishTimestamp() {
		return finishTimestamp;
	}
	/**
     * 完成的时候被调用
     */
    protected synchronized void forcekill()
	{
    	this.forcekill(false);
	}
    

	/**
     * 完成的时候被调用
     */
    public synchronized void finish()
	{
    	try
		{
    		if( signal_shutdown ){
    			Log.war(String.format("[%s] Succeed to quite(%s) from subprocessor and have %s pids",
    				id, process_status, this.pids.length()));
    		}
        	if( subprocess != null && process_status != 0 ){
        		subprocess.destroy();
        	}
    		subprocess = null;
        	shutdownTimestamp = 0;
            lastRunTime = System.currentTimeMillis();
            if( this.pids.length() > 0 ){//非守护程序同时pid数据不为空的时候执行kill操作
//            	System.out.println(String.format("[%s][%s][pids:%s] Wait to kill before 2 secoonds at status %s.",
//            		id, Tools.getFormatTime("mm:ss SSS", System.currentTimeMillis()), pids.length(), process_status));
            	if( mode==Module.MODE_DAEMON_AUTO||mode==Module.MODE_DAEMON_HAND ){
            		Log.print("[%s] Found %s pid%s when daemon.", id, pids.length(), (pids.length()>1?"s":""));
            	}
            	else{
            		this.wait(2000);//杀死进程之前等待2000秒
            	}
//            	System.out.println(String.format("[%s][%s][pids:%s] Begin to kill.", id, Tools.getFormatTime("mm:ss SSS", System.currentTimeMillis()), pids.length()));
            	forcekill(true);
            	this.notifyAll();
            	if( this.daemon ){
            		Log.war(lastQuiteLogs.toString());
            	}
            }
            else{
            	if( mode==Module.MODE_DAEMON_AUTO||mode==Module.MODE_DAEMON_HAND ){
            		Log.print("[%s] Finish to startup daemon by status %s.", id, process_status);
            	}
            }
            if( "Zookeeper".equals(id) || "COSPortal".equals(id) || "COSApi".equals(id) || "ProgramLoader".equals(id) ){
                Log.war( this, "["+id+"] Process finish to quite.\r\n"+lastQuiteLogs.toString());
    			System.out.println(String.format("@COS$ [%s] Process finish to execute.", id));
            }
		}
		catch (Exception e)
		{
			Log.err(e);
		}
	}
    /**
     * 启动自动过滤
     */
    protected void startupAutoConfirm()
    {
    	monitorGC.reset();
        Thread thread = new Thread(){
        	public void run()
        	{
        		synchronized(instance)
        		{
	        		try
					{
	        			long ts = System.currentTimeMillis();
	        			instance.wait(Tools.MILLI_OF_MINUTE);//1分钟后如果仍然未退出工作正常，那么自动确认告警
	        			ts = System.currentTimeMillis() - ts;
	        			if( mode==Module.MODE_DAEMON_AUTO||mode==Module.MODE_DAEMON_HAND ){
	        				Log.msg(String.format("%s after %sts.", instance.toString(), ts));
	        				if( starting && subprocess != null && executing ) {
								Sysalarm alarm = new Sysalarm();
				                alarm.setSysid(sysid);
				                alarm.setResponser(modulePerf.getProgrammer());
				                alarm.setContact(modulePerf.getProgrammerContact());
				                alarm.setSeverity(AlarmSeverity.BLUE.getValue());
				                alarm.setType(AlarmType.S.getValue());
				                alarm.setId(id+"_Startup");
				                alarm.setCause("配置的启动指令是阻塞式的");
				                alarm.setTitle("程序["+name+"]ID["+id+"]的启动配置是【守护】模式但执行结果是【阻塞】模式");
				                StringBuffer sb = new StringBuffer();
				                for(String s : startupCommands)
				                	sb.append("\r\n\t"+s);
				                alarm.setText(String.format("程序启动于%s，程序负责人【%s】，"+
				                	"配置的程序启动运行模式是【守护】模式，但是启动指令执行后程序没有退出，"+
				                	"实际运行结果是【阻塞】式。\r\n为了更好的控制你的程序运行，你可以将程序运行模式改为【非守护】模式。\r\n当前实际启动指令如下：%s", 
				                	modulePerf.getStartupTime(), modulePerf.getProgrammer(), sb.toString()));
								modulePerf.setDead(alarm.getText());
				        		SysalarmClient.save(alarm);//向系统管理员发送告警
								manager.sendModuleMonitorData(instance, ModuleManager.N_MU);//因为程序状态或者内存不变化不会向Portal推送模块状态信息
		        			}
	        				else{
		        				SysalarmClient.close(sysid, id+"_Startup", "运行模式为【守护】的程序已经正常配置运行");
		        				modulePerf.setCountException(0);
		        				modulePerf.setPrintException("");
	        				}
    					}
	        			else if( starting && subprocess != null && executing ) {
	        				SysalarmClient.close(sysid, id+"_Startup", "程序再次启动后超过"+ts+"分钟运行工作正常");
	        				modulePerf.setCountException(0);
	        				modulePerf.setPrintException("");
	        			}
					}
					catch (Exception e)
					{
					}
        		}
        	}
        };
        thread.start();
    }
    
    /**
     * 增加扩展的命令参数
     * @param key
     */
	protected void appendDProperty(String key, ArrayList<String> commands)
	{
		String val = ConfigUtil.getString(key);
		if( val == null ) return;
		commands.add("-D"+key+"="+val);
	}
    /**
     * 执行中断，每次运行进程后需要间隔一段时间才执行下一次
     */
	private synchronized void executeInterrupt()
    {
		if( interrupt )
		{
			this.setState(STATE_INIT);
		}
		else
		{
			this.setState(STATE_SHUTDOWN);
		}
        do
        {//如果不是暂停状态，那么等待重启间隔时间
        	if( !suspend && enabled && isValidTime() && starting )
        	{
	            try
	            {
	                wait( this.interruptTime );
	            }
	            catch( InterruptedException e )
	            {
	            }
        	}
        }
        while( this.interrupt && enabled && isValidTime() && starting);
    }

    /**
     * 执行进程守护，如果PIDFILE有效还需要根据PID来判断进程状态
     */
	private long pidMemorySeed = 0;
	private int maxMemUsed;//曾经使用的最大内存
	private void checkMemoryFromPid(F filepid){

    	if( filepid == null )
    	{
    		return;
    	}
    	//TODO:检查PID文件以及进程是否存在，如果存在将模块状态置为启动（启动时间为文件时间），不存在置为关闭
		String pid = IOHelper.readFirstLine(filepid);
		if( !Tools.isNumeric(pid) )
		{
			return;
		}
		this.manager.status("[%s] check the memory by pid %s", id, pid);
		//dci       3424 83.2  7.3 13105324 1193664 ?    Sl   21:10  31:58 /home/dci/mongodb/bin/mongod --config /home/dci/mongodb/mongodb.conf
		/*USER：说明该程序是属于哪一个人的；
		PID：该程序的代号；
		%CPU：代表该程序使用了多少 CPU 资源；
		%MEM：代表该程序使用了多少的 RAM ；
		VSZ, RSS：占去的 ram 的大小（ bytes ）；
		TTY：是否为登入者执行的程序？若为 tty1-tty6 则为本机登入者，若为 pts/?? 则为远程登入者执行的程序
		STAT：该程序的状态
		START：该程序开始的日期；
		TIME：该程序运行的时间？
		COMMAND：该程序的内容啦！*/
        BufferedReader bufferedReader = null;
//    	        InputStream process_reader = null;
        Process process = null;
//    	        String line = null;
		try
		{
			ArrayList<String> commands = new ArrayList<String>();
			if( WrapperShell.isWindows  ){
				commands.add( "tasklist" );
				commands.add( "/fi" );
				commands.add( "\"PID eq "+pid+"\"" );
				ProcessBuilder pb = new ProcessBuilder( commands );
	            //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
	            pb.redirectErrorStream( true );
	    		process = pb.start();
	            bufferedReader = new BufferedReader(
	                new InputStreamReader( process.getInputStream() ) );
	            String tasklist = null, line = null;;
	            while ((line = bufferedReader.readLine()) != null)
	            {
	            	line = line.trim();
	            	if( !line.isEmpty() ){
	            		tasklist = line;
	            	}
	            }
//	    		Log.msg(String.format("[%s] tasklist /fi \"PID eq %s\"\r\n%s", id, pid, tasklist));
//	            映像名称                       PID 会话名              会话#       内存使用
//	            ========================= ======== ================ =========== ============
//	            mysqld.exe                   27196 Console                    2    171,788 K	            
	            String[] args = Tools.split(tasklist!=null?tasklist.trim():null);
	            if( args.length > 4 )
	            {
	        		ModuleMemeory memory = new ModuleMemeory(id, ModuleManager.LocalIp);
	    			memory.setId(pidMemorySeed++);
	    			memory.setRuntime(System.currentTimeMillis());
	    			String m = args[4];
	    			m = m.replaceAll(",", "");
	    			int curmem = Integer.parseInt(m);
	    			if(curmem != maxMemUsed ){
	    				maxMemUsed = curmem;
	    			}
	    			memory.setHc(maxMemUsed);
	    			memory.setHe(curmem);
	    			modulePerf.setUsageMemory(curmem+"K/"+maxMemUsed+"K");
//	    			if( modulePerf.getMemories().size() > 2048 )
//	    			{
//	    				modulePerf.getMemories().remove(0);
//	    			}
	    			modulePerf.addModuleMemeory(memory);
	            }
			}
			else{
				commands.add( "pmap" );
				commands.add( "-x" );
				commands.add( pid );
//	    				String[] commands = new String[8];
//	    				int i = 0;
//	    				commands[i++] = setCommand("%java.home%/bin/java");
//	    	    		commands[i++] = "-Djava.class.path="+System.getProperty("java.class.path");
//	    	    		commands[i++] = "com.focus.control.JSH";
//	    				commands[i++] = "ps";
//	    				commands[i++] = "aux";
//	    				commands[i++] = "|";
//	    				commands[i++] = "grep";
//	    				commands[i++] = pid;
				this.manager.status("[%s] check the memory by command %s", id, commands.toString());
				ProcessBuilder pb = new ProcessBuilder( commands );
	            //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
	            pb.redirectErrorStream( true );
	    		process = pb.start();
				this.manager.status("[%s] start the check of memory.", id);
//	                    StringBuffer sb = new StringBuffer("# ps"+' '+"aux"+' '+"|"+' '+"grep"+' '+pid+"\r\n");
//	        			int b = 0;
//	        			//out.write(sh_reader.available());
//	        			while( (b = process_reader.read() ) != -1 )
//	        			{
//	        				sb.append((char)b);
//	        			}
	            bufferedReader = new BufferedReader(
	                new InputStreamReader( process.getInputStream() ) );
	            String pmap = null, line = null;;
	            while ((line = bufferedReader.readLine()) != null)
	            {
	            	pmap = line;
	            }
	            //mapped: 16708K    writeable/private: 1688K    shared: 4K
	            //total kB        69281528 4022868  439744
//		        		Log.msg(String.format("[%s] pid is %s\r\n\t%s", id, pid, pmap));
				this.manager.status("[%s] %s", id, pmap);
	            String[] args = Tools.split(pmap);
	            if( args.length > 4 )
	            {
	        		ModuleMemeory memory = new ModuleMemeory(id, ModuleManager.LocalIp);
	    			memory.setId(pidMemorySeed++);
	    			memory.setRuntime(System.currentTimeMillis());
//	            			memory.setHc(Integer.parseInt(args[1].substring(0, args[1].length()-1)));
//	            			memory.setHe(Integer.parseInt(args[3].substring(0, args[3].length()-1)));
	    			memory.setHc(Integer.parseInt(args[2]));
	    			memory.setHe(Integer.parseInt(args[3]));
	    			modulePerf.setUsageMemory(args[3]+"K/"+args[2]+"K");
//	    			if( modulePerf.getMemories().size() > 2048 )
//	    			{
//	    				modulePerf.getMemories().remove(0);
//	    			}
	    			modulePerf.addModuleMemeory(memory);
					this.manager.status("[%s] %s/%s", id, pmap, memory.getHc(), memory.getHe());
	            }
			}
			this.manager.status("[%s] check zombie.", id);
			synchronized(this){
				JSONObject p = new JSONObject();
				p.put("pid", pid);
				p.put("startup_command", new JSONArray(getStartupCommands()));
				p.put("shutdown_command", new JSONArray(getShutdownCommands()));
				pids.put(pid, p);
				this.manager.status("[%s] check process by pid %s.", id, pid);
				Iterator<?> allpids = pids.keys();
				this.manager.status("[%s] check process by pid %s by iterator", id, pid);
				ArrayList<String> removing = new ArrayList<String>();
				this.manager.status("[%s] begin to check process by pid %s by iterator", id, pid);
				while(allpids.hasNext()){
					this.manager.status("[%s] coming to check process by pid %s by iterator", id, pid);
					pid = allpids.next().toString();
					this.manager.status("[%s] check process by pid %s on %s", id, pid, System.getProperty("os.name"));
					if( !Subprocessor.pid(pid) ){
						this.manager.status("[%s] found none zombie %s", id, pid);
						removing.add(pid);
					}
				}
				for(String pid2 : removing ){
					pids.remove(pid2);
				}
				this.manager.status("[%s] set the info of pid", id);
				if( pids!=null ){
					this.modulePerf.setProcessInfo(pids.toString(), pids.length());
				}
				else{
					this.modulePerf.setProcessInfo("{}", 0);
				}
			}
    	}
        catch( Exception e )
        {
			Log.err(String.format("[%s] Failed to check memory by pid %s",id, pid), e);
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
	}

	/**
	 * 发送死进程告警
	 */
	private void sendDeadAlarm(String alarmTitle, String alarmCuase)
	{
		 StringBuffer w = new StringBuffer();
		 if( debugLog.size() > 0 ){
			 w.append("该程序最近"+debugLog.size()+"条程序日志");
			 w.append("\r\n... ...");
			 for(String s : debugLog)
				 w.append("\r\n"+s);
		 }
		 this.modulePerf.setDead(alarmCuase+"\r\n"+w.toString());
		 Sysalarm alarm = new Sysalarm();
		 alarm.setSysid(sysid);
		 alarm.setResponser(modulePerf.getProgrammer());
		 alarm.setContact(modulePerf.getProgrammerContact());
		 alarm.setSeverity(AlarmSeverity.RED.getValue());
		 alarm.setType(AlarmType.S.getValue());
		 alarm.setId(id+"_Startup");
		 alarm.setTitle(alarmTitle);
		 alarm.setCause(alarmCuase);
		 alarm.setText(modulePerf.getDead());
		 SysalarmClient.send(alarm);//向系统管理员发送告警
	}
	/**
	 * 是否运行
	 * @return
	 */
    private boolean isRunFromPid()
    {
    	F filepid = this.getPidfile();
		this.modulePerf.setDaemon(mode==MODE_DAEMON_AUTO||mode==MODE_DAEMON_HAND);//设置程序是否是守护程序
//		Log.msg(String.format("[%s] the path(%s) of pid is %s", id, pidfile, filepid!=null?filepid.getAbsoluteFile():"null"));
    	if( filepid == null )
    	{
    		if( pidfile!=null && !pidfile.isEmpty() )
    		{
    			 filepid = new F(pidfile);
    			 if( !filepid.exists() ){
    				 this.sendDeadAlarm("主控引擎检查程序["+name+"]ID["+id+"]发现进程PID文件不存在，疑似产生死进程问题",
    					String.format("该程序没有产生进程PID文件:%s, 可能程序已经运行，请系统管理员尽快处理", filepid.getPath()));
    			 }
    		}
    		this.setState(STATE_SHUTDOWN, "NotFoundPid");
    		return false;
    	}
    	try
    	{
	    	//TODO:检查PID文件以及进程是否存在，如果存在将模块状态置为启动（启动时间为文件时间），不存在置为关闭
    		String pid = IOHelper.readFirstLine(filepid);
			if( Tools.isNumeric(pid) )
			{
        		this.modulePerf.setStartupTime(new Date(filepid.lastModified()));
        		if( Subprocessor.pid(pid) ){
   				 	this.modulePerf.setDead("");
   				 	SysalarmClient.autoconfirm(sysid, id+"_Startup",
   				 		String.format("程序检测到正常启动，通过PID文件[%s]读取进程ID[%s]能够正常检测到程序运行", filepid.getPath(), pid));
//        			checkMemoryFromPid(filepid);不执行内存检查，统一由外部主控循环线程对程序内存进行扫描
        			if( this.getState() != STATE_STARTUP ) {
        				this.setState(STATE_STARTUP);//原先的状态不是启动状态
        			}
        			return true;
        		}
        		else {
        			sendDeadAlarm("主控引擎检查程序["+name+"]ID["+id+"]疑似产生死进程或者该程序不能正常启动",
        				String.format("该程序已经启动进程实例并在运行，实际的进程ID与写入PID文件[%s]的进程ID[%s]不一致，可能的原因是程序在不断的重启，或者产生死进程", 
        					filepid.getPath(), pid));
        		}
        		this.setState(STATE_SHUTDOWN,"Shutdown("+pid+").");
			}
			else
			{
    			this.setState(STATE_CLOSE, "PID Unknown("+pid+")");
			}
    	}
        catch( Exception e )
        {
			this.setState(STATE_SHUTDOWN, "Service exception.");
			Log.err(e);
        }
    	return false;
    }
    
    /**
		dci       3424 83.2  7.3 13105324 1193664 ?    Sl   21:10  31:58 /home/dci/mongodb/bin/mongod --config /home/dci/mongodb/mongodb.conf
		0         1PID 2CPU  3MEM 4VSZ     5RSS     TTY  STAT START  TIME  COMMAND
		/*USER：说明该程序是属于哪一个人的；
		PID：该程序的代号；
		%CPU：代表该程序使用了多少 CPU 资源；
		%MEM：代表该程序使用了多少的 RAM ；
		VSZ, RSS：占去的 ram 的大小（ bytes ）；
		TTY：是否为登入者执行的程序？若为 tty1-tty6 则为本机登入者，若为 pts/?? 则为远程登入者执行的程序
		STAT：该程序的状态
		START：该程序开始的日期；
		TIME：该程序运行的时间？
		COMMAND：该程序的内容啦！
    private void monitorPid(String psaux)
    {
		ModuleMemeory memory = null;
		try
		{			
		}
		catch(Exception e)
		{
			Log.err("Failed to monitor pid for exception:", e);
		}
    }
     */
    private synchronized void executeDaemon()
    {
    	long count = 0;
    	//如果模块处于启动状态，一直循环，直到启动中止!STATE_STARTUP或者模块关闭starting=false
        while(daemon && enabled && isValidTime() && starting )
        {
            try
            {
                wait( 7000 );//30秒检查一次
                if( !isRunFromPid() && (Module.MODE_DAEMON_AUTO == mode || Module.MODE_DAEMON_HAND == mode) )
                {
                	daemon = false;
                	if( count > 0 ){
                		Log.war(this.toString()+" quite daemon.");
                	}
                }
                count += 1;
            }
            catch( Exception e )
            {
            }
        }
    }
    /**
     * 运行暂停，如果suspend状态为true就执行暂停
     */
    protected synchronized void executeSuspend()
    {
        if( suspend )
        {//如果是暂停状态进入服务暂停
            this.setState(STATE_SUSPEND);
            do
            {//暂停阻塞
                if(enabled && isValidTime() && starting)
                {
                    try
                    {
                        wait( 100 );
                        if( !suspend ){
                        	Log.msg(this.toString()+" quite suspend.");
                        }
                    }
                    catch( InterruptedException e )
                    {
                    }
                }
                else
                {
                	break;
                }
            }
            while(suspend);
        }
    }
    
    /**
     * 运行模块
     */
    public void run()
    {
    	Log.msg("["+id+"] Module thread(daemon="+this.daemon+",interrupt="+this.interrupt+",suspend="+this.suspend+") begin to run.");
        starting = true;
        while( enabled && isValidTime() && starting )
        {
        	//如果在Noraml以及AutoDaemon的工作模式下，能够正常启动
    		this.executeStartup();
    		//如果在Daemon*工作模式下，进入daemon守护状态，持续等待或者跟踪PIDFILE文件
    		this.executeDaemon();
    		//执行中断只有在MODE_SINGLE工作模式下才长期中断，否则等待一个中断周期
        	this.executeInterrupt();
        	//如果suspend=true，那么执行暂停，知道暂停被恢复
        	this.executeSuspend();
        }
        // 模块退出时，恢复为待机状态
        starting = false;
        this.setState(STATE_CLOSE);
        Log.war(String.format("%s\r\n\tThe thread of module quited.", this.toString()));
    }

    public void setName( String name )
    {
        this.name = name;
        this.modulePerf.setName(name);
    }
    /**
     * 设置中断时间
     * @param restartInterval
     */
    public void setInterruptTime( int restartInterval )
    {
        this.interruptTime = restartInterval==0?interruptTime:(restartInterval * 1000);
        this.modulePerf.setRestartInterval(interruptTime);
    }

    public void setId( String id )
    {
        this.id = id;
        this.modulePerf.setId(id);
        java.io.File moduleLogPath = null;
		if( this.getStartupCommands().indexOf("org.apache.catalina.startup.Bootstrap") != -1 )
		{
			moduleLogPath = new java.io.File(System.getProperty("catalina.home")+"/logs/");
		}
		else
		{
			moduleLogPath = new java.io.File(ConfigUtil.getWorkPath(), "log/"+id);
		}
		this.modulePerf.setModuleLogPath(moduleLogPath);
    }

    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
        F f = new F(ConfigUtil.getWorkPath(), "log/"+id+"/the_program_can_not_quite_by_signal.txt");
        if( this.enabled ){
    		if( f.exists() ){
    			try {
    				Log.war(String.format("[%s] Found dead from %s", id, f.getAbsolutePath()));
    				modulePerf.setDead(""+new String(IOHelper.readAsByteArray(f), "UTF-8"));
    			} catch (UnsupportedEncodingException e) {
    			}
    		}
    		else{
    			modulePerf.setDead("");
    		}
        }
        else{
        	if( f.exists() ){
        		f.delete();
        	}
			modulePerf.setDead("");
        }
    }

    public void setVersion( String version )
    {
        this.version = version;
        this.modulePerf.setVersion(version);
    }

    public void setShutdownType( int shutdownType )
    {
        this.shutdownType = shutdownType;
    }

    public void setShutdownText( String shutdownText )
    {
        this.shutdownText = shutdownText;
        this.modulePerf.setShutdownText(shutdownText);
    }

    public void setShutdownPort( int shutdownPort )
    {
        this.shutdownPort = shutdownPort;
    }

    public Module setDependence( String dependence )
    {
        this.dependence = dependence!=null?dependence:"";
        this.modulePerf.setDependence(dependence);
        return this;
    }

    public String getName()
    {
        return name;
    }

    public String getId()
    {
        return id;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public String getVersion()
    {
        return version;
    }

    public int getShutdownType()
    {
        return shutdownType;
    }

    public String getShutdownText()
    {
        return shutdownText;
    }

    public int getShutdownPort()
    {
        return shutdownPort;
    }

    public ArrayList<String> getShutdownCommands()
    {
        return shutdownCommands;
    }

    public String getDependence()
    {
        return dependence;
    }
	public String getShutdownBinary() 
	{
		return shutdownBinary;
	}
	public void setShutdownBinary(String shutdownBinary) 
	{
		this.shutdownBinary = shutdownBinary;
		this.modulePerf.setShutdownBinary(shutdownBinary);
	}
	public int getDelayedStartInterval() 
	{
		return delayedStartInterval;
	}
	public void setDelayedStartInterval(int delayedStartInterval) 
	{
		this.delayedStartInterval = delayedStartInterval;
		this.modulePerf.setDelayedStartInterval(delayedStartInterval);
	}
	public long getStartupTimestamp() {
		return this.modulePerf.getStartupTimestamp();
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
		this.modulePerf.setStartTime(startTime);
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
		this.modulePerf.setEndTime(startTime);
	}
	public int getStandby()
	{
		return standby;
	}
	public void setStandby(int standby)
	{
		this.standby = standby;
		this.modulePerf.setStandby(standby);
	}
	public boolean isShutdownRunning() {
		return isShutdownRunning;
	}
	public void setShutdownRunning(boolean isShutdownRunning) {
		this.isShutdownRunning = isShutdownRunning;
		this.modulePerf.setShutdownRunning(isShutdownRunning);
	}
	
	public void clear()
	{
		this.startupCommands.clear();
		this.shutdownCommands.clear();
	}
	public ArrayList<String> getStartupCommands()
	{
		return startupCommands;
	}
	
	/**
	 * 设定下次强制重启的时间
	 * @param time
	 */
	public void setForcereboot(String forcereboot)
	{
		this.forcereboot = forcereboot;
		if( forcereboot == null || forcereboot.isEmpty() )
		{
			return;
		}
		
		String args[] = forcereboot.split(":");
		if( args.length < 1 )
		{
			return;
		}
		
		Calendar calendar = Calendar.getInstance();
		int offset = 1;
		if( "m".equals(args[0]) )
		{//每月重启一次m:<day_month>[:HH:MM:ss]
			calendar.add(Calendar.MONTH, 1);
			if( args.length > offset )
				calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(args[offset++]));
		}
		else if( "w".equals(args[0]) )
		{//每周重启一次w:<day_week>[:HH:MM:ss]
			calendar.add(Calendar.WEEK_OF_MONTH, 1);
			if( args.length > offset )
				calendar.set(Calendar.DAY_OF_WEEK, Integer.parseInt(args[offset++]));
		}
		else if( "d".equals(args[0]) )
		{//每天重启一次d:day_of_interval:[HH:MM:ss]
			if( args.length > offset )
			{
				int n =  Integer.parseInt(args[offset]);
				if( n < 1 )
				{
					n = 1;
				}
				calendar.add(Calendar.DAY_OF_MONTH, n);
			}
		}
		else if( "h".equals(args[0]) )
		{//每小时重启一次或者，每n小时启动一次 h:n:[MM:ss]
			if( args.length > offset )
			{
				int n =  Integer.parseInt(args[offset]);
				if( n < 1 )
				{
					n = 1;
				}
				calendar.add(Calendar.HOUR_OF_DAY, n);
				args[offset] = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
			}
		}
		else if( Tools.isNumeric(args[0]) )
		{//单纯的数字
			this.forcereboottrigger = System.currentTimeMillis() + Integer.parseInt(args[0])*1000;
			return;
		}
		else
		{
			return;
		}
		
		if( args.length > offset )
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(args[offset++]));
		if( args.length > offset )
			calendar.set(Calendar.MINUTE, Integer.parseInt(args[offset++]));
		if( args.length > offset )
			calendar.set(Calendar.SECOND, Integer.parseInt(args[offset++]));
		this.forcereboottrigger = calendar.getTimeInMillis();
	}
	public String getRemark()
	{
		return remark;
	}
	public void setRemark(String remark)
	{
		this.remark = remark;
		this.modulePerf.setRemark(remark);
	}
	
	public String toString()
	{
		return String.format("[%s][mode:%s][exec:%s][state:%s '%s'][status: %s %s] %s, endexe:%s, %s",
				id,
				mode, 
				executing,
				STATE_LABLES[this.getState()],
				modulePerf.getMonitorInfo(),
				this.status,
				Tools.getFormatTime(this.status_timestamp),
				this.subprocess+"/"+process_status, 
				Tools.getFormatTime("MM-dd HH:mm:ss", finishTimestamp),
				name);
	}
	

    /**
     * 设置命令
     * @param command String
     * @return String
     */
    public static String setCommand( String command )
    {
    	command = command.trim();
        int len = command.length();
        StringBuffer sbMatch = null;
        StringBuffer sbCmd = new StringBuffer();
        for( int i = 0; i < len; i++ )
        {
            char c = command.charAt( i );
            if( c == '%' )
            {
                if( sbMatch == null )
                {
                    sbMatch = new StringBuffer();
                }
                else
                {
                    String property = System.getProperty( sbMatch.toString() );
                    sbCmd.append( property );
                    sbMatch = null;
                }
            }
            else if( sbMatch == null )
            {
                sbCmd.append( c );
            }
            else if( sbMatch != null )
            {
                sbMatch.append( c );
            }
        }

        return sbCmd.toString();
    }
	public ModulePerf getModulePerf()
	{
		return modulePerf;
	}
	public void setSuspend(boolean suspend)
	{
		this.suspend = suspend;
	}
	public int getMode()
	{
		return mode;
	}
	/**
	 * 设置工作模式
	 * @param mode
	 */
	public void setMode(int mode)
	{
		this.mode = mode;
		switch(mode)
		{
		case Module.MODE_SUSPEND:
			this.suspend = true;//先暂停
			break;
		case Module.MODE_SINGLE:
			this.interrupt = true;//中断持续进行
			break;
		case Module.MODE_DAEMON_AUTO:
			this.daemon = false;
			break;
		case Module.MODE_DAEMON_HAND:
			this.daemon = true;
			break;
		default:
			this.mode = Module.MODE_NORMAL;//缺省情况为普通工作模式
			break;
		}
//		Log.msg("Module("+id+") work mode is "+this.mode+", enable is "+enabled);
	}
	public String getLogfile()
	{
		return logfile;
	}
	public F getLogdir()
	{
		return dirLog;
	}
	public void setLogfile(String logfile)
	{
		this.logfile = logfile;
        //日志目录
		if( dirLog == null )
		{
			if( this.logfile != null && !this.logfile.isEmpty() )
			{
				String logpath = this.logfile;
	    		int k = logpath.indexOf("*"); 
	    		if( k != -1 )
	    		{
	    			int i = logpath.lastIndexOf("/");
	    			if( i != -1 )
	    			{
	    				int l = logpath.lastIndexOf('*');
	    				dirLogMark = k<l?logpath.substring(k+1, l):logpath.substring(k+1);
	    				logpath = logpath.substring(0, i);
	    				dirLog = new F(logpath);
	    			}
	    		}
			}
			if( dirLog == null )
			{
		        dirLog = new F(ConfigUtil.getWorkPath(), "log/"+id+"/");
		    	if( !dirLog.exists() )
		    	{
		    		dirLog.mkdir();
		    	}
			}
		}
	}
	public void setPidfile(String pidfile)
	{
		this.pidfile = pidfile;
	}
	public void setCfgfile(String cfgfile)
	{
		this.modulePerf.setCfgfile(cfgfile);
	}
	public String getCommonds(ArrayList<String> commands)
	{
		StringBuffer sb = new StringBuffer();
		for(String commond : commands )
		{
			sb.append(commond);
			sb.append(' ');
		}
		return sb.toString();
	}
	/**
	 * 得到PID文件句柄
	 * @return
	 */
	public F getPidfile()
	{
		if( pidfile == null || pidfile.isEmpty() ) return null;
		int k = pidfile.indexOf("*");
		String pidpath = pidfile;
		if( k > 0 )
		{
    		String p = null;
			int i = pidpath.lastIndexOf("/");
			if( i != -1 )
			{
				int l = pidpath.lastIndexOf('*');
				p = k<l?pidpath.substring(k+1, l):pidpath.substring(k+1);
				pidpath = pidpath.substring(0, i);
			}
			else
			{
				return null;
			}

    		try
    		{
        		F path = new F(pidpath);
	    		if( path.exists() )
	    		{
	    			F files[] = path.listFiles();
	    			for( F file : files )
	    			{
	    				if( file.isFile() &&  file.getName().indexOf(p) != -1)
	    				{
	    					return file;
	    				}
	    			}
	    		}
    		}
    		catch(Exception e)
    		{
    		}
    		return null;
		}
		
		F file = new F(pidpath);
		return file.exists()?file:null;
	}

	private String sysid = "Sys";
	public void setProgramMaintenace(String programmer, String contact, String sysid)
	{
		this.sysid = sysid==null||sysid.isEmpty()?this.sysid:sysid;
		this.modulePerf.setProgrammer(programmer);
		this.modulePerf.setProgrammerContact(contact);
	}
	
	/**
	 * 进程数量大于1表示有僵尸
	 * @return true表示有僵尸
	 */
	public synchronized boolean hasZombie(){
		this.manager.status("Check zombie by pids(%s)", pids.length());
		if( this.pids.length() > 1 ){
	        ArrayList<String> removing = new ArrayList<String>();
			Iterator<?> allpids = pids.keys();
			while(allpids.hasNext()){
				String pid = allpids.next().toString();
				if( !Subprocessor.pid(pid) ){
					removing.add(pid);//发现进程已经消失
				}
			}
	        for(String pid : removing ){
	        	pids.remove(pid);
	        }
	        if( pids.length() > 1 ){
	        	return true;//任然有僵尸
	        }
			this.manager.status("Check zombie by pids(%s)", pids.length());
	        SysalarmClient.autoconfirm("Sys", id + "_Zombie", 
	        		String.format("程序[%s %s]的僵尸进程已经自动关闭: %s", id, name, pids.toString(4)));
		}
		return false;
	}
	
    /**
     * 设置进程ID数据
     * @param pid
     */
	public synchronized void setProcessInfo(JSONObject pid) {
		this.pids.put(pid.getString("pid"), pid);
		if( pids!=null ){
			this.modulePerf.setProcessInfo(pids.toString(), pids.length());
		}
		else{
			this.modulePerf.setProcessInfo("{}", 0);
		}
	}
	/**
	 * 设置网络状态数据
	 * @param netstat
	 */
	public synchronized void setNetstat(JSONArray netstat) {
		
		this.modulePerf.setNetstat(netstat!=null?netstat.toString():"[]");
	}
	
	public int getProcessStatus(){
		return process_status;
	}
	/**
	 * 得到PIDS
	 * @return
	 */
	public JSONObject getPids() {
		return this.pids;
	}
	
	/**
	 * 得到JAVA的运行路径
	 * @return
	 */
	public String getJavaBinpath(){
		if( java && !this.startupCommands.isEmpty() ){
			String path = this.startupCommands.get(0);
			int i = path.lastIndexOf('/');
			if( i == -1 ) {
				String separator_file = System.getProperty("file.separator", "/");
	        	path = System.getProperty("java.home");
	        	if( !path.endsWith("bin"+separator_file+"java") )
	        		path += ""+separator_file+"bin"+separator_file;
	        	return path.endsWith(separator_file)?path:(path+separator_file);
			}
			return path.substring(0, i+1);
		}
		return null;
	}
}
