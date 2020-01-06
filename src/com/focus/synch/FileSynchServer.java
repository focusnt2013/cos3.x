package com.focus.synch;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.focus.util.Log;

/**
 * <p>Title: 文件同步服务器</p>
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
public class FileSynchServer extends FileSynchAgent
{
    public FileSynchServer(Socket socket)
        throws Exception
    {
        super(socket);
        statusSynch = STATUS_SYNCH_IND_WAIT;//暂不执行同步，等待对端同步只是
    }
    
    public static int bindPort = 9527;
    public static void main(String args[])
    {
    	int i=0;
        if(args.length > i)						//模块名称
        {
            ModuleID = args[i];
        }
        i++;
        //启动日志管理器
        Log.getInstance().setSubroot(ModuleID);
        Log.getInstance().setDebug(true);
        Log.getInstance().setLogable(true);
        Log.getInstance().start();
        if(args.length > i)						//监听端口
        {
        	bindPort = Integer.valueOf(args[i]);
        }
        i++;
        Log.msg("server bind port:" + bindPort);
        if(args.length > i)						//本地目标地址（对方同步到本地的路径）
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
        Log.msg("local receive path:" + SynchPath);
        for( ; i < args.length; i++ )			//同步清单
        {
            String[] params = args[i].split(":");
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

        Thread thread = new Thread(new Runnable()
        {
              public void run()
              {
                  Log.msg("Start "+ModuleID+" listen.");
                  Socket socket;
                  ServerSocket ss = null;
                  try
                  {
                      int localPort = bindPort;
                      ss = new ServerSocket(localPort);
                      Log.msg(this, "Succeed to socket(" + localPort + ")..");
                      while(true)
                      {
                          socket = ss.accept();
                          FileSynchServer fileSynchServer = new FileSynchServer(socket);
                          if(fileSynchServer.connect() == CONNECTION)
                          {
                              Log.msg("Succeed to setup connect from "+fileSynchServer.toString());
                              fileSynchServer.startSynchThread();
                          }
                          else
                          {
                              Log.war("Failed to setup connect from "+fileSynchServer.toString());
                          }
                      }
                  }
                  catch(Exception e)
                  {
                      Log.war(ModuleID+" break for " + e);
                      System.exit(0x82);
                  }
                  finally
                  {
                      try
                      {
                          if(ss != null)
                          {
                              ss.close();
                          }
                      }
                      catch(IOException e)
                      {
                          Log.war("Faild to close socket for " + e);
                      }
                  }
                  Log.msg("Finish " + ModuleID + " process.");
                  Log.getInstance().close();
                  System.exit(-1);
              }
        });
        thread.start();
    }

    public void disconnect()
    {
        Log.war(ModuleID+" disconnect:"+lastException);
    }
}
