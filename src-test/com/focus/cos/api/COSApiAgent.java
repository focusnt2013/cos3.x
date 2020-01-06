package com.focus.cos.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.focus.util.IOHelper;
import com.focus.util.Tools;

/**
 * 
 * @author focus
 *
 */
public abstract class COSApiAgent extends Thread
{
	private Process process;
	private boolean running = false;
	private int port;
	public COSApiAgent(int port)
	{
		this.port = port;
	}
	
	public boolean isRunning()
	{
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
        	startupCommands.add( i++,  "-Dcos.jdbc.driver="+System.getProperty("cos.jdbc.driver", ""));
        	startupCommands.add( i++,  "-Dcos.jdbc.url="+System.getProperty("cos.jdbc.url", "") );
        	startupCommands.add( i++,  "-Dcos.jdbc.user="+System.getProperty("cos.jdbc.user", "") );
        	startupCommands.add( i++,  "-Dcos.jdbc.password="+System.getProperty("cos.jdbc.password", "") );
        	startupCommands.add( i++,  "-Dcos.api.port="+port );
        	startupCommands.add( i++,  "-Dcontrol.port="+System.getProperty("control.port", "9527") );
        	startupCommands.add( i++,  "-Dservice.name=Agent111");
        	startupCommands.add( i++,  "-Dservice.desc=测试Agent");
        	startupCommands.add( "-cp" );
        	startupCommands.add( cp.toString() );
        	startupCommands.add("com.focus.cos.control.COSApi");
        	startupCommands.add("COSApiAgent");
        	StringBuffer sb = new StringBuffer();
        	for(String command : startupCommands)
        	{
        		sb.append("\r\n\t");
        		sb.append(command);
        	}
        	System.out.println(sb.toString());

        	ProcessBuilder pb = new ProcessBuilder( startupCommands );
            //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
            pb.redirectErrorStream( false );
            process = pb.start();
    		Thread threadError = new Thread()
    		{
    		    public void run()
    		    {
    		    	InputStream erris = process.getErrorStream();
		            try
		            {
		            	int ch = 0;
		                while( (ch = erris.read()) != -1 )
		                {
		                	System.err.write(ch);
		                }
		            }
		            catch( Exception e )
		            {
		            }
		            finally
		            {
		            	try {
							erris.close();
						} catch (IOException e) {
						}
		            }
    		    }
    		};
    		threadError.start();
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
    	                		IOHelper.read(is, payload, 4);
    	                		len = Tools.bytesToInt(payload, 0, 4);
    	                		IOHelper.read(is, payload, len);
    	                		setApiProxy(payload, len);
    	                		break;
    	                	}
    	                }
    		        }
    		        catch( Exception e )
    		        {
    		        }
    		        System.out.println("[COSApiAgent-Reader] Quite.");
    		    }
    		};
    		thread.start();
            running = true;
            System.out.println("[COSApiAgent] Quite: "+process.waitFor());
            running = false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public abstract void setVersion(String version);
	public abstract void setApiPort(int port);
	public abstract void setApiProxy(byte[] payload, int length);
}
