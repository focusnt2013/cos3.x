package com.focus.synch;

import com.focus.util.Log;
import com.focus.util.Tools;

/**
 * <p>Title: EMA平台</p>
 *
 * <p>Description: EMA后台服务器与工具程序</p>
 *
 * <p>Copyright: Copyright FOCUS(c) 2009</p>
 *
 * <p>Company: FOCUS</p>
 *
 * @author Focus Lau
 * @version 1.0
 */
public class FileSynchClient
    extends FileSynchAgent
{    
    public FileSynchClient(String ip, int port)
        throws Exception
    {
        super(ip, port);
    }

    public static void main(String args[])
    {
        ModuleID = "FileSynchClient";

        String ip = "127.0.0.1";
        int i = 0;
        if(args.length > i)	//模块名
        {
            ModuleID = args[i];
        }
        i++;
        
        //启动日志管理器
        Log.getInstance().setSubroot(ModuleID);
        Log.getInstance().setDebug(false);
        Log.getInstance().setLogable(true);
        Log.getInstance().start();
        
        int port = 9527;
        if( args.length > i )	//ip+端口
        {
            ip = args[i];
            String params[] = ip.split(":");    
            if(params.length == 2)
            {
            	ip =params[0];
            	port = Integer.valueOf(params[1]);
            }
        }
        i++;
        Log.msg("Ready to connect "+ ip +":"+ port);
        if (args.length > i )	//本地目标地址（对方同步到本地的路径）
        {
        	if(!args[i].trim().isEmpty())
        	{
        		String params[] = args[i].split("/:");
        		SynchPath = params[0];
        		if(!SynchPath.equals(args[i]))
        		{
        			//强推模式
        			omitOppSynchInd = true;
        		}
        	}
        }
        i++;
        Log.msg("Local path:" + SynchPath);
        for( ; i < args.length; i++ )
        {
        	String[] params = Tools.split(args[i], ":");
            String path = params[0];
            if(path.indexOf("*.*") != -1)
            {
            	path = path.replace("*.*", "");
            	ListPath.add(path);
            	Log.msg("path:" + path + " is synch path.");
            	onlyFileSynchPath.add(path);
            	Log.msg("path:" + path + " is only file synch path.");
            	HalfSynchPath.add(path);	// 如果只同步目录下的文件，那么目录就是半同步，不会删除对方目录和文件
    			if( params.length > 1 && !params[1].isEmpty() )
    			{
    				FilterSynchPath.put(path, params[1].trim());
    				Log.msg("Add filter("+path+":"+params[1]+").");
    			}
            	Log.msg("path:" + path + " is half synch path.");
            }
            else
            {
            	ListPath.add(path);
                Log.msg("path:" + path + " is synch path.");
        		if(!args[i].equals(params[0]))
        		{
        			if( params.length > 1 && !params[1].isEmpty() )
        			{
        				FilterSynchPath.put(path, params[1].trim());
        				Log.msg("Add filter("+path+":"+params[1]+").");
        			}
        			HalfSynchPath.add(path);
        			Log.msg("path:" + path + " is half synch path.");
        		}
            }
        }
        
        try
        {
            FileSynchClient instance = new FileSynchClient(ip, port);
            if (instance.connect() == CONNECTION)
            {
                Log.msg("Succeed to connect to " + ip);
            	instance.startSynchThread();
            }
            else
            {
                Log.war("Failed to setup connect from " + ip);
                //退出程序 关闭日志
                Log.getInstance().close();
                System.exit(0);//表示连接失败
            }
        }
        catch (Exception ex)
        {
        	Log.err("Failed to setup synch from"+ip, ex);
        	Log.getInstance().close();
        	System.exit(0x82);
        }
    }

    public void disconnect()
    {
        Log.war(ModuleID+" disconnect:");
        Log.err(lastException);
        //退出程序 关闭日志
        Log.getInstance().close();
        System.exit(0);//表示连接断开而退出
    }
}
