package com.focus.cos.api;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import com.focus.util.IOHelper;
import com.focus.util.Tools;

/**
 * 
 * @author focus
 *
 */
public abstract class COSApiProxy extends Thread
{
	private Process process;
	private int apiport;
	private boolean running;

	public COSApiProxy(int apiport)
	{
		this.apiport = apiport;
	}

	public boolean isRunning() {
		return running;
	}
	
	public void close()
	{
		if( process != null )
			process.destroy();
	}

	public void run()
	{
		try
		{
    		int i = 0;
			String separator_file = System.getProperty("file.separator", "/");
        	String separator_path = System.getProperty("path.separator", ";");
        	String jdk = System.getProperty("java.home");
        	if( !jdk.endsWith("bin"+separator_file+"java") )
        		jdk += ""+separator_file+"bin"+separator_file+"java";
        	File classesDir = new File("build/classes");
        	File libDir = new File("lib");
        	File weblibDir = new File("WebContent/WEB-INF/lib");

        	StringBuffer cp = new StringBuffer();
        	cp.append(classesDir.getAbsolutePath().replace("\\", "/"));
        	cp.append(separator_path);
        	cp.append(libDir.getAbsolutePath().replace("\\", "/")+"/*");
        	cp.append(separator_path);
        	cp.append(weblibDir.getAbsolutePath().replace("\\", "/")+"/*");
        	ArrayList<String> startupCommands = new ArrayList<String>();
        	startupCommands.add( i++, jdk );
        	startupCommands.add( i++,  "-Xms16m" );
        	startupCommands.add( i++,  "-Xmx64m" );
        	startupCommands.add( i++,  "-XX:PermSize=8M" );
        	startupCommands.add( i++,  "-XX:MaxPermSize=32m" );
        	startupCommands.add( i++,  "-Dcos.identity=data/identity");
        	startupCommands.add( i++,  "-Dcos.api=127.0.0.1:"+apiport );
        	startupCommands.add( i++,  "-Dservice.name=Proxy222");
        	startupCommands.add( i++,  "-Dservice.desc=测试Agent222");
        	startupCommands.add( "-cp" );
        	startupCommands.add( cp.toString() );
        	startupCommands.add("com.focus.cos.control.COSApi");
        	startupCommands.add("COSApiProxy");
        	StringBuffer sb = new StringBuffer();
        	for(String command : startupCommands)
        	{
        		sb.append("\r\n\t");
        		sb.append(command);
        	}
//        	System.out.println(sb.toString());

        	ProcessBuilder pb = new ProcessBuilder( startupCommands );
            //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
            pb.redirectErrorStream( true );
            process = pb.start();
    		Thread thread = new Thread()
    		{
    		    public void run()
    		    {
    	            int method = -1;
    	            int len = 0;
    		    	InputStream is = process.getInputStream();
    		        try
    		        {
    	            	byte[] payload = new byte[65536];
    	                while( ( method = is.read() ) != -1 )
    	                {
    	                	switch(method)
    	                	{
    	                	case 0://标题
    	                		len = is.read();
    	                		IOHelper.read(is, payload, len);
    	                		setVersion(new String(payload, 0, len));
    	                		IOHelper.read(is, payload, 4);
    	                		setApiPort(Tools.bytesToInt(payload, 0, 4));
    	                		break;
    	                	case 1:
    	                		break;
    	                	case 2:
    	                		IOHelper.read(is, payload, 4);
    	                		len = Tools.bytesToInt(payload, 0, 4);
    	                		IOHelper.read(is, payload, len);
    	                		setApiAgent(payload, len);
    	                		break;
    	                	}
    	                }
    		        }
    		        catch( Exception e )
    		        {
    		        }
    		        System.out.println("[COSApiProxy-Reader] Quite.");
    		    }
    		};
    		thread.start();
            running = true;
            System.out.println("[COSApiProxy] Quite: "+process.waitFor());
            running = false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public abstract void setVersion(String version);
	public abstract void setApiPort(int port);
	public abstract void setApiAgent(byte[] payload, int length);
}
