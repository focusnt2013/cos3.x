package com.focus.cos.control;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.json.JSONArray;

import com.focus.cos.api.AlarmSeverity;
import com.focus.cos.api.AlarmType;
import com.focus.cos.api.Sysalarm;
import com.focus.cos.api.SysalarmClient;
import com.focus.cos.wrapper.WrapperPlugins;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Subprocessor;
import com.focus.util.Tools;

/**
 * COS的正常运行以来JDK的一些功能，需要安装JDK
 * @author think
 *
 */
public abstract class MyJDK extends WrapperPlugins implements Runnable {
	/*定时器用于安装JDK*/
	private Timer timer = new Timer();
	/**
	 * 构造重构
	 * @param workdir
	 * @param name
	 * @param desc
	 */
	public MyJDK(File workdir, String name, String desc){
		super(workdir, name, desc);
	}
	
	public File getWorkdir(){
		return this.workpath;
	}
	
	/**
	 * 资源关闭
	 */
	public void close(){
		timer.cancel();
	}

    /**
     * 根据标记查找JAVA程序并杀死关闭它
     * @param tag
     * @return
     */
    public String killjava(String tag)
    {
		String separator_file = System.getProperty("file.separator", "/");
    	String jdk = System.getProperty("java.home");
    	if( !jdk.endsWith("bin"+separator_file+"java") )
    		jdk += ""+separator_file+"bin"+separator_file+"jsp";
    	StringBuilder sb = new StringBuilder("Find all process of java by "+tag);
        BufferedReader bufferedReader = null;
        Process process = null;
		try
		{
			ArrayList<String> commands = new ArrayList<String>();
			commands.add( jdk );
			commands.add( "-lv" );
			ProcessBuilder pb = new ProcessBuilder( commands );
            pb.redirectErrorStream( true );
    		process = pb.start();
            bufferedReader = new BufferedReader(
                new InputStreamReader( process.getInputStream() ) );
            String line = null;;
            while ((line = bufferedReader.readLine()) != null)
            {
            	line = line.trim();
            	if( line.isEmpty() ) continue;
            	if( line.indexOf(tag) != -1 ){
            		int i = line.indexOf(" ");
            		String pid = line.substring(0, i);
            		pid = pid.trim();
            		if( Tools.isNumeric(pid) ){
            			sb.append(String.format("\r\n\tFound %s need to kill.\r\n\t\t%s", pid, line));
            			sb.append(String.format("\r\n\t\t%s", Subprocessor.forcekill(pid)));
            		}
            	}
            }
    	}
        catch( Exception e )
        {
			sb.append(String.format("\r\n\t%s", e.getMessage()));
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
        return sb.toString();
    }
    
    /* 运行进程 */
    private HashMap<String, JSONArray> myjps = new HashMap<String, JSONArray>();
    
    /**
     * 
     * @param id
     * @return
     */
    public JSONArray getJps(String id){
    	return this.myjps.get(id);
    }
	
    /**
     * 一个小时后检查
     */
    public void run(){
		synchronized (this) {
			try {
				this.wait(Tools.MILLI_OF_MINUTE);
			} catch (InterruptedException e) {
			}
			
			if( System.currentTimeMillis() - jpstime > Tools.MILLI_OF_MINUTE ){
				try{
					Log.err("Failed to jsp not finish after "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", jpstime));
					jps_process.destroy();
					this.wait(Tools.MILLI_OF_MINUTE);
					if( System.currentTimeMillis() - jpstime > Tools.MILLI_OF_MINUTE ){
						Log.err("Found the jps not quite after destory.");
						Sysalarm alarm = new Sysalarm();
		            	alarm.setSysid("Sys");
		            	alarm.setSeverity(AlarmSeverity.RED.getValue());
		            	alarm.setType(AlarmType.S.getValue());
		            	alarm.setId(WrapperShell.ModuleID+"_myjdk");
		            	alarm.setCause("未知");
		            	alarm.setTitle("主控引擎执行JAVA进程检查出现严重异常");
		            	alarm.setText("从"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", jpstime)+"执行JPS检查进程工作情况，JPS程序未能在1分钟内执行结束，执行destroy后也未能让JPS程序结束运行。");
		            	SysalarmClient.send(alarm);
					}
					else{
						Log.war("Found the jps quite after destory at "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", jpstime));
						Sysalarm alarm = new Sysalarm();
		            	alarm.setSysid("Sys");
		            	alarm.setSeverity(AlarmSeverity.ORANGE.getValue());
		            	alarm.setType(AlarmType.S.getValue());
		            	alarm.setId(WrapperShell.ModuleID+"_myjdk");
		            	alarm.setCause("未知");
		            	alarm.setTitle("主控引擎执行JAVA进程检查出现异常");
		            	alarm.setText("从"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", jpstime)+"执行JPS检查进程工作情况，JPS程序未能在1分钟内执行结束，执行destroy后JPS程序结束运行。");
		            	SysalarmClient.send(alarm);
					}
				}
				catch(Exception e){
					Log.err("Failed to destory jps", e);
				}
			}
		}
	}
   
	public abstract void parse(String[] args);
    /**
     * 根据标记查找JAVA的PID
     * @param tag
     * @return
     */
    private Process jps_process;
    private long jpstime = 0;
    public void jps()
    {
		File jpsfile = new File(workpath, "jdk1.8/bin/jps");
        BufferedReader bufferedReader = null;
        Process process = null;
		try
		{
			myjps.clear();
			ArrayList<String> commands = new ArrayList<String>();
			commands.add( jpsfile.getAbsolutePath() );
			commands.add( "-lv" );
			ProcessBuilder pb = new ProcessBuilder( commands );
            pb.redirectErrorStream( true );
            this.jpstime = System.currentTimeMillis();
    		process = pb.start();
    		jps_process = process;
    		Thread thread = new Thread(this);
    		thread.start();
            bufferedReader = new BufferedReader(
                new InputStreamReader( process.getInputStream() ) );
            String line = null;
            while ((line = bufferedReader.readLine()) != null)
            {
            	line = line.trim();
            	if( line.isEmpty() ) continue;
            	String args[] = Tools.split(line, " ");
//            	System.out.println(String.format("[%s] %s.", args[0], line));
            	if( args.length > 2 ){
            		this.parse(args);
            	}
//            	System.out.println(String.format("\t#%s checked.", args[0], args.length));
            }
//        	System.out.println("#: ok");
    	}
        catch( Exception e )
        {
        	Log.err("Failed to handle the jps", e);
        	if( e.getMessage().indexOf("Permission denied") != -1 ){
            	Sysalarm alarm = new Sysalarm();
            	alarm.setSysid("Sys");
            	alarm.setSeverity(AlarmSeverity.ORANGE.getValue());
            	alarm.setType(AlarmType.S.getValue());
            	alarm.setId(WrapperShell.ModuleID+"_myjdk");
            	alarm.setCause("没有正确执行chmod 755授权指令");
            	alarm.setTitle("主控引擎内置的JDK1.8没有可执行权限");
            	alarm.setText(e.getMessage());
            	SysalarmClient.send(alarm);
        	}
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
            this.jpstime = System.currentTimeMillis();
            synchronized (this) {
            	this.notify();
			}
        }
    }
    
    private boolean intalling;
	public void install() throws Exception{
		String osname = System.getProperty("os.name").toLowerCase();
		String osarch = System.getProperty("os.arch").toLowerCase();
		this.pluginsName = "jdk1.8";
		if(osarch.indexOf("64") !=-1){
			osarch = "x64";
		}
		else{
			osarch = "x32";
		}
		if( isWindows() )
		{//linux-jdk1.8-x64
			pluginsName = pluginsName+"-windows-"+osarch;
		}
		else if( isLinux() ){
			pluginsName = pluginsName+"-linux-"+osarch;
		}
		else
		{
			throw new Exception(String.format("暂不支持安装%s的JDK", osname));
		}
		abortPrintProgress();
		Log.msg("Begin to install jdk(1.8).");
		Thread thread = new Thread(){
			public void run(){
				if( report() ){
					Log.msg("Succeed to report before install jdk.");
					File dir = install(pluginsName);
					if( dir != null ){
						Log.msg("Succeed to install jdk(1.8) to "+dir);
						if( isLinux() ){
							File bindir = new File(workpath, "jdk1.8/bin");
							String path = bindir.getAbsolutePath();
							String result = Subprocessor.exec0("chmod", "755", "-R", path);
							Log.msg(String.format("Permission grant by 'chmod 755 %s'\r\n\tThe result of execute is '%s'.", path,result));
						}
					}
					else{
						Log.err("Failed to install jdk(1.8) after 3 mintues try again.");
						timer.schedule(new TimerTask(){
							@Override
							public void run() {
								try {
									install();
								} catch (Exception e) {
								}
							}
							
						}, Tools.MILLI_OF_MINUTE*3);
					}
				}
				else{
					Log.err("Failed to report before install jdk.");
					Log.war("Wait for 3 minutes to intall the jdk(1.8) again.");
					timer.schedule(new TimerTask(){
						@Override
						public void run() {
							try {
								install();
							} catch (Exception e) {
							}
						}
						
					}, Tools.MILLI_OF_MINUTE*3);
				}
				intalling = false;
			}
		};
		intalling = true;
		thread.start();
	}
	
	/**
	 * 是否安装了JDK(通过检查jps文件判断)
	 * @return
	 */
	public boolean uninstall(){
		File jpsfile = new File(workpath, "jdk1.8/bin/jps");
		if( isWindows() ){
			jpsfile = new File(workpath, "jdk1.8/bin/jps.exe");
		}
		return !jpsfile.exists()||intalling;
	}
	
	/**
	 * 
	 */
	public File install(File tmpfile, File setupdir, StringBuffer logcontext) 
	{
		ZipFile zip = null;
		boolean haserror = false;
		try
		{
	        zip = new ZipFile(tmpfile, Charset.forName("GBK"));
	        Enumeration<?> entries = zip.entries();
	        ArrayList<ZipEntry> list = new ArrayList<ZipEntry>();
	        while(entries.hasMoreElements())
	        {
	            ZipEntry entry = (ZipEntry)entries.nextElement();  
	            list.add(entry);
	        }
//    		int percent = list.size()/20;
//    		int step = setupprogress/2;
    		setupdir = new File(workpath, "jdk1.8");
    		if( !setupdir.exists() )
    		{
    			setupdir.mkdirs();
    		}
    		StringBuffer errorlog = new StringBuffer();
	        for(int i = 0; i < list.size(); i++)
	        {
	        	ZipEntry entry = list.get(i);
	            String outPath = entry.getName().replace("\\", "/"); 
	            File file = new File(setupdir, outPath);
	            if( entry.isDirectory() )
	            {
	            	file.mkdirs();
	            	this.setuptips = "mkdir "+file.getPath();
	            }
	            else
	            {
	            	InputStream in = zip.getInputStream(entry);
	            	if( !file.getParentFile().exists() ){
	            		file.getParentFile().mkdirs();
	            	}
	            	String r = IOHelper.writeFile(file, in);
	            	if( r != null ){
	            		errorlog.append("\r\n\t"+entry.getName()+"("+entry.getSize()+") extract "+r);
	            	}
	            	this.setuptips = "extract "+file.getPath();
	            }
	            logcontext.append("\r\n"+setuptips);
//            	this.setupprogress = 80 + i/percent;
//            	for(; step <= setupprogress/2; step++)
//            		System.out.print("+");
	        }
	        list.clear();
//	        System.out.print(" "+setupprogress+"%");
//			System.out.println();
			String version = tmpfile.getName();
			version = version.substring(0, version.lastIndexOf(".zip"));
			version = version.substring(version.lastIndexOf("-")+1);
			Log.msg("Succeed to download "+pluginsName+"("+version+") to "+setupdir.getPath());
			if( errorlog.length() > 0 ){
				errorlog.insert(0, "Found below files not write:");
				Log.war(errorlog.toString());
			}
			return setupdir;
		}
		catch (Exception e) 
		{
			Log.err("Failed to download "+pluginsName+" for exception", e);
			haserror = true;
			return null;
		}
		finally
		{
			if( zip != null )
				try
				{
					zip.close();
					if( haserror )
					{
						tmpfile.delete();
					}
				}
				catch (IOException e) {
				}
		}
	}	
	@Override
	public void handleReportResponse(int flag, DataInputStream dis) throws Exception {
	}

    private static String OS = System.getProperty("os.name").toLowerCase();  
    public static boolean isLinux(){  
        return OS.indexOf("linux")>=0;  
    }  
      
    public static boolean isMacOS(){  
        return OS.indexOf("mac")>=0&&OS.indexOf("os")>0&&OS.indexOf("x")<0;  
    }  
      
    public static boolean isMacOSX(){  
        return OS.indexOf("mac")>=0&&OS.indexOf("os")>0&&OS.indexOf("x")>0;  
    }  
      
    public static boolean isWindows(){  
        return OS.indexOf("windows")>=0;  
    }  
      
    public static boolean isOS2(){  
        return OS.indexOf("os/2")>=0;  
    }  
      
    public static boolean isSolaris(){  
        return OS.indexOf("solaris")>=0;  
    }  
      
    public static boolean isSunOS(){  
        return OS.indexOf("sunos")>=0;  
    }  
      
    public static boolean isMPEiX(){  
        return OS.indexOf("mpe/ix")>=0;  
    }  
      
    public static boolean isHPUX(){  
        return OS.indexOf("hp-ux")>=0;  
    }  
      
    public static boolean isAix(){  
        return OS.indexOf("aix")>=0;  
    }  
      
    public static boolean isOS390(){  
        return OS.indexOf("os/390")>=0;  
    }  
      
    public static boolean isFreeBSD(){  
        return OS.indexOf("freebsd")>=0;  
    }  
      
    public static boolean isIrix(){  
        return OS.indexOf("irix")>=0;  
    }  
      
    public static boolean isDigitalUnix(){  
        return OS.indexOf("digital")>=0&&OS.indexOf("unix")>0;  
    }  
      
    public static boolean isNetWare(){  
        return OS.indexOf("netware")>=0;  
    }  
      
    public static boolean isOSF1(){  
        return OS.indexOf("osf1")>=0;  
    }  
      
    public static boolean isOpenVMS(){  
        return OS.indexOf("openvms")>=0;  
    }  
}
