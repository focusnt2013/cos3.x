package com.focus.cos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.security.Key;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;

import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.ops.service.EmailSender;
import com.focus.util.Base64;
import com.focus.util.F;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 * 内置程序的基类
 * @author focus
 *
 */
public class CosServer 
{
	public static String OSNAME = "";
	public static boolean IsWindows = false;
	
	public String jdk()
	{
		String separator_file = System.getProperty("file.separator", "/");
		String JDK = System.getProperty("java.home");
    	if( !JDK.endsWith("bin"+separator_file+"java") )
    		JDK += ""+separator_file+"bin"+separator_file+"java";
    	JDK = JDK.replace('\\', '/');
    	return JDK;
	}
	
	public static void uniqueProcess(String pname)
	{
		OSNAME = System.getProperty("os.name");
		IsWindows = OSNAME.toLowerCase().indexOf("window") != -1;
    	try
		{
    		F dir = new F("../log");
    		F pidfile = new F("../log/"+pname+"/shell.pid");
    		if( dir.exists() )
    		{
    			pidfile = new F(pname+".pid");
    		}
    		if( pidfile.exists() )
    		{
    			String pid = IOHelper.readFirstLine(pidfile);
        		if( IsWindows )
        		{//tasklist | findstr "3096"
            		String rsp = exec(new String[]{"tasklist", "/fi", "\"PID eq "+pid+"\""}, false);
            		if( rsp.indexOf(pid) != -1 )
            		{
            			System.err.println("Found start program running...");
            			System.exit(1);
            			return;
            		}
        		}
        		else
        		{
            		String rsp = exec(new String[]{"kill","-0",pid});
            		if( rsp == null || rsp.isEmpty() )
            		{
            			System.err.println("Found start program running...");
            			System.exit(1);
            			return;
            		}
        		}
    		}
    		String runname = ManagementFactory.getRuntimeMXBean().getName();
    		Log.msg(pidfile.getAbsolutePath()+":"+runname);
    		String pid = runname.split("@")[0];
    		IOHelper.writeFile(pidfile, pid.getBytes());
		}
		catch (Exception e)
		{
			Log.err("Failed to unique-process", e);
		}
	}

	public static String getSecurityKey() throws Exception
	{
		String path = System.getProperty("cos.identity", "../data/identity");
		F fileIdentity = new F(path); 
		//读取数字证书并初始化
		Key identity = (Key)IOHelper.readSerializable(fileIdentity);
    	Cipher c = Cipher.getInstance("DES");
        c.init(Cipher.WRAP_MODE, identity);//再用数字证书构建另外一个DES密码器
        return Base64.encode(c.wrap(identity));
	}
    /**
     * 构建系统监控程序的版本数据
     */
	public static void buildTimeline(Zookeeper zookeeper, String sercurityKey, String id, String name, String remark, String[][] versions)
	{
		F vFile = new F("../version.txt");
		String zkpath = "";
		try
		{
			zkpath = "/cos/config/program/"+Tools.encodeMD5(sercurityKey)+"/version/"+id;
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat != null && vFile.exists() && vFile.lastModified() < stat.getMtime() )
				return;
			JSONObject timeline = new JSONObject();
			JSONArray date = new JSONArray();
			timeline.put("date", date);
			timeline.put("name", name);
			//是一套能够开发各种类型应用服务系统的软件开发框架。具备跨平台、多进程、分布式等特性。平台核心引擎能够在各种硬件服务器环境下运行，支持包括JAVA、C语言程序、服务器脚本多种程序.
			timeline.put("remark", remark);
			for(String values[] : versions )
			{
				String version = values[0];
				String text = values[1];
				String args[] = Tools.split(version, ".");
				int year = Integer.parseInt(args[1]);
				int month = Integer.parseInt(args[2]);
				int day = Integer.parseInt(args[3]);
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(0);
				calendar.set(2000+year, month-1, day);
				JSONObject timeline0 = new JSONObject();
				timeline0.put("version", version);
				timeline0.put("time", Tools.getFormatTime("MM/dd/yyyy", calendar.getTimeInMillis()));
				timeline0.put("text", text);
				date.put(timeline0);
			}
			if( stat == null )
			{
				String[] args = zkpath.split("/");
				String path = "";
				for(String arg : args)
				{
					if( arg.isEmpty() ) continue;
					path += ("/"+arg);
					stat = zookeeper.exists(path, false);
					if( stat == null )
					{
						if( path.equals(zkpath) )
							zookeeper.create(zkpath, timeline.toString().getBytes("UTF-8"));
						else
							zookeeper.create(path, new byte[0]);
					}
				}
				Log.msg("Succeed to add the timeline of version for program("+id+").");
			}
			else
			{
				Log.msg("Succeed to update the timeline of version for program("+id+").");
				zookeeper.setData(zkpath, timeline.toString().getBytes("UTF-8"), stat.getVersion());
			}
			if( !vFile.exists() ) vFile.createNewFile();
		}
		catch (Exception e)
		{
			Log.err("Failed to set the timeline of version for program("+id+", "+vFile.getAbsolutePath()+"["+vFile.exists()+"]).", e);
		}
	}
	/**
	 * 获取系统参数配置或者组件配置
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public JSONObject getEmailConfig(String path, Zookeeper zookeeper)
		throws Exception
	{
		Stat stat = zookeeper.exists(path, false);
		if( stat != null )
		{
			JSONObject profile0 = new JSONObject(new String(zookeeper.getData(path, false, stat), "UTF-8"));
			if( profile0.has("POP3Username") && profile0.has("SMTP") && profile0.has("POP3Password") )// && profile0.has("SMTPAuth") )
			{
				String from = profile0.getString("POP3Username");
				String host = profile0.getString("SMTP");
				String userName = profile0.getString("POP3Username");
				String password = profile0.getString("POP3Password");
				if( password.isEmpty() && profile0.has("POP3PasswordEncrypt") )
				{
					password = profile0.getString("POP3PasswordEncrypt");
				}
//				String auth = profile0.getString("SMTPAuth");
				if( !from.isEmpty() && !host.isEmpty() && !userName.isEmpty() && !password.isEmpty() )// && !auth.isEmpty() )
					return profile0;
			}
		}
		return null;
	}

	/**
	 * 设置配置
	 * @param path
	 * @param key
	 * @param value
	 * @throws Exception
	public static void setConfig(String path, String key, String value, ZooKeeper zookeeper)
			throws Exception
	{
		Stat stat = zookeeper.exists(path, false);
		if( stat != null )
		{
			JSONObject data = new JSONObject(new String(zookeeper.getData(path, false, stat), "UTF-8"));
			data.put(key, value);
			zookeeper.setData(path, data.toString().getBytes("UTF-8"), stat.getVersion());
		}
	}
	 */
	/**
	 * 获取系统参数配置或者组件配置
	 * @param path
	 * @return
	 * @throws Exception
	public static JSONObject getConfig(String path, ZooKeeper zookeeper)
		throws Exception
	{
		Stat stat = zookeeper.exists(path, false);
		if( stat != null )
		{
			return new JSONObject(new String(zookeeper.getData(path, false, stat), "UTF-8"));
		}
		return null;
	}
	 */

    /**
     * 执行操作系统command命令
     *
     * @param cmds
     * @return
     */
	public static String exec(String cmd)
    {
		return exec(new String[]{cmd});
    }
	public static String exec(String[] cmds)
    {
    	return exec(cmds, null, true);
    }
    public static String exec(String[] cmds, boolean print)
    {
    	return exec(cmds, null, print);
    }
    public static String exec(String[] cmds, Anykey anykey, boolean print)
    {
    	StringBuffer sb = new StringBuffer();
    	ArrayList<String> list = new ArrayList<String>();
    	for(String cmd : cmds)
    	{
    		if( sb.length() > 0 ) sb.append(" ");
    		sb.append(cmd);
    		list.add(cmd);
    	}
        Process process = null;
        InputStream is = null;
        try
        {
            ProcessBuilder pb = new ProcessBuilder(list);
            pb.redirectErrorStream(true);
            process = pb.start();
            // 获取屏幕输出显示
            pb.redirectErrorStream( true );
            is = process.getInputStream();
            int ch = 0;
            byte[] payload = new byte[1024];
            int i = 0;
            if( anykey != null ) anykey.active(process);
            while( (ch = is.read()) != -1 )
            {
            	payload[i++] = (byte)ch;
            	if( print )	System.out.write(ch);
            }
            return new String(payload, 0, i);
        }
        catch (Exception e)
        {
        	System.err.println("Failed to execcute '"+sb.toString()+"'.");
        	return "";
        }
        finally
        {
        	process.destroy();
        	if( is != null )
				try 
        		{
					is.close();
				}
        		catch (IOException e)
        		{
				}
        }
    }
    
    /**
     * 
     * @author focus
     *
     */
    public static class Anykey extends Thread
    {
    	Process process;
    	public void active(Process p)
    	{
    		process = p;
    		this.start();
    	}
    	
    	public void run()
    	{
    		try 
    		{
    			sleep(1000);
    			OutputStream out = this.process.getOutputStream();
    			out.write(' ');
    			out.close();
			}
    		catch( Exception e ) 
    		{
			}
    	}
    }
    
    public static class Somekey extends Anykey
    {
    	private String keys;
    	
    	public Somekey(String keys)
    	{
    		this.keys = keys;
    	}

    	public void run()
    	{
    		try 
    		{
    			sleep(1000);
    			OutputStream out = this.process.getOutputStream();
    			out.write(keys.getBytes());
    			out.write('\r');
    			out.flush();
    			out.close();
			}
    		catch( Exception e ) 
    		{
			}
    	}
    }
    
    /**
     * 
     * @param zookeeper
     * @param title
     * @param context
     * @throws Exception 
     */

	public static void sendSystemEmail(
		Zookeeper zookeeper,
		String title,
		Exception e)
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(out);
			e.printStackTrace(ps);
			sendSystemEmail(zookeeper, title, out.toString());
		}
		catch(Exception e1)
		{
		}
	}
	
	public static void sendSystemEmail(
		Zookeeper zookeeper,
		String title,
		String context) throws Exception
	{
		if( zookeeper == null ) return;
		String path = "/cos/config/system";
		JSONObject profile = zookeeper.getJSONObject(path);
		if( profile == null )
		{
			throw new Exception("Not found config("+path+") from zookeeper.");
		}
		String username = profile.has("SysContactName")?profile.getString("SysContactName"):"超级管理员";
		String portalUrl = profile.has("PortalUrl")?profile.getString("PortalUrl"):"";
		String to[] = new String[]{profile.getString("SysContact")};
		EmailSender.sendSystemEmail(to, null, title, context, portalUrl, username, profile);
	}

	public static void sendSystemEmail(
		JSONObject profile,
		String title,
		String content) throws Exception
	{
		if( profile == null )
		{
			throw new Exception("Not found config from zookeeper.");
		}
		String username = profile.has("SysContactName")?profile.getString("SysContactName"):"超级管理员";
		String portalUrl = profile.has("PortalUrl")?profile.getString("PortalUrl"):"";
		String to[] = new String[]{profile.getString("SysContact")};
		EmailSender.sendSystemEmail(to, null, title, content, portalUrl, username, profile);
	}
	

	/**
	 * 创建节点
	 * @param zookeeper
	 * @param zkpath
	 * @param payload
	 * @throws Exception
	 */
	public static void createNode(Zookeeper zookeeper, String zkpath, byte[] payload)
		throws Exception
	{
		Stat stat = null;
		String[] args = zkpath.split("/");
		String path = "";
		for(String arg : args)
		{
			if( arg.isEmpty() ) continue;
			path += ("/"+arg);
			stat = zookeeper.exists(path, false);
			if( stat == null )
			{
				if( path.equals(zkpath) )
				{
					zookeeper.create(zkpath, payload);
				}
				else
					zookeeper.create(path, new byte[0]);
			}
		}
	}
}
