package com.focus.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class Subprocessor {

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
    			sleep(100);
    			OutputStream out = this.process.getOutputStream();
    			out.write(' ');
    			out.close();
			}
    		catch( Exception e ) 
    		{
			}
    	}
    }
    
	public static String exec0(Object... args)
    {
    	ArrayList<String> list = new ArrayList<String>();
    	Anykey anykey = null;
    	for(int i = 0; i < args.length; i++)
    	{
    		if( args[i] instanceof Anykey )
    		{
    			anykey = (Anykey)args[i];
    			break;
    		}
    		list.add(args[i].toString());
    	}
		return execute(list, anykey);
    }
	
	public static String exec1(Object... args)
    {
    	ArrayList<String> list = new ArrayList<String>();
    	for(int i = 0; i < args.length; i++)
    	{
    		list.add(args[i].toString());
    	}
		return execute(list, null);
    }

	/*
	 * 执行无阻塞的程序
	 */
	public static String execute(ArrayList<String> list, Anykey anykey)
    {
        Process process = null;
        InputStream is = null;
        try
        {
            ProcessBuilder pb = new ProcessBuilder(list);
            pb.redirectErrorStream(true);
            process = pb.start();
            is = process.getInputStream();
            if( anykey != null ) anykey.active(process);
            BufferedReader reader = new BufferedReader( new InputStreamReader(is) );
            String line = null;
            StringBuilder sb = new StringBuilder();
            while( (line = reader.readLine()) != null )
			{
            	sb.append(line);
            	sb.append("\r\n");
			}
            reader.close();
            return sb.toString();
        }
        catch (Exception e)
        {
        	return "Failed to execcute for "+e;
        }
        finally
        {
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
	 * 检查pid对应的程序是否存在
	 * @param pid
	 * @return
	 */
    public static boolean pid(String pid){
		String OSNAME = System.getProperty("os.name");
		boolean IsWindows = OSNAME.toLowerCase().indexOf("window") != -1;
		if( IsWindows )
		{//tasklist | findstr "3096"
    		String rsp = exec0("tasklist", "/fi", "\"PID eq "+pid+"\"");
    		if( rsp.indexOf(pid) != -1 )
    		{
    			return true;
    		}
		}
		else
		{
    		String rsp = exec0("kill","-0",pid);
    		if( rsp == null || rsp.isEmpty() )
    		{
    			return true;
    		}
		}
		return false;
    }
    /**
     * 根据pid杀进程
     * @param pid
     */
    public static String kill(String pid){
		String OSNAME = System.getProperty("os.name");
		boolean IsWindows = OSNAME.toLowerCase().indexOf("window") != -1;
		if( IsWindows ){
    		return exec0("taskkill", "/F", "/PID", pid);
		}
		else{
    		return exec0("kill", pid);
		}
    }
    
    /**
     * 根据pid强制杀进程
     * @param pid
     */
    public static String forcekill(String pid){
		String OSNAME = System.getProperty("os.name");
		boolean IsWindows = OSNAME.toLowerCase().indexOf("window") != -1;
		if( IsWindows ){
    		return exec0("taskkill", "/F", "/PID", pid);
		}
		else{
    		return exec0("kill", "-9", pid);
		}
    }
}
