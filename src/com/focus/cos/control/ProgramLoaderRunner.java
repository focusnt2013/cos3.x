package com.focus.cos.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;

public class ProgramLoaderRunner extends SystemRunner
{
	public static final String Name = "程序配置管理监听引擎"; 
	public static final String Remark = "管理主控引擎配置下所有用户程序配置。服务功能包括：通知主控引擎加载指定的用户程序配置启动运行；读取config/control.xml文件加载程序配置项，通知系统管理员进行程序发布审核。";
	
	private ConfigReader reader = null;
	
    public ProgramLoaderRunner(ModuleManager manager)
    {
    	super( "ProgramLoader", manager );
    	super.className = "com.focus.cos.control.ProgramLoader";
        this.setName( Name );
        this.setRemark(Remark);
        this.setDelayedStartInterval(5000);
    }

	@Override
	public void initliaize() throws Exception
	{
    	this.frequency = 0;
    	this.timeout = 0;
	}
	
	/**
	 * 打开子进程收信机
	 */
    protected synchronized void openSubprocessReader( Process p, ArrayList<String> commands )
    {
    	if( reader == null )
    	{
    		reader = new ConfigReader(p, commands);
    		reader.start();
    	}
    }
    
    private ModuleManager getManager()
    {
    	return manager;
    }
    
    class ConfigReader extends Thread
    {
        private Process subprocess;
        protected ArrayList<String> commands;
        public ConfigReader( Process p, ArrayList<String> commands )
        {
            this.subprocess = p;
            this.commands = commands;
        }
        
        public void run()
        {
            int method = -1;
            int len = 0;
            String subejct = null;
            InputStream is = subprocess.getInputStream();
            try
            {
            	byte[] payload = new byte[65536];
            	String id = null;
                while( ( method = is.read() ) != -1 )
                {
                	switch(method)
                	{
                	case 0://标题
                		len = is.read();
                		IOHelper.read(is, payload, len);
                		setVersion(new String(payload, 0, len));
                		len = is.read();
                		IOHelper.read(is, payload, len);
                		subejct = new String(payload, 0, len, "UTF-8");
                		getManager().setSubject(subejct);
            	        System.out.println("@COS$ ProgramLoader work on.");
                    	System.out.println();
            	        System.out.println("@COS$");
                		break;
                	case 1://配置
                		IOHelper.read(is, payload, 4);
                		len = Tools.bytesToInt(payload, 0, 4);
                		IOHelper.read(is, payload, len);
                		getManager().setModuleConfig(payload, len);
                		break;
                	case 2://运行开关
                		len = is.read();
                		IOHelper.read(is, payload, len);
                		id = new String(payload, 0, len);
                		getManager().setModuleSwitch(id, is.read()==1?true:false);
                		break;
                	case 3://DEBUG开关
                		len = is.read();
                		IOHelper.read(is, payload, len);
                		id = new String(payload, 0, len);
                		getManager().setModuleDebug(id, is.read()==1?true:false);
                		break;
                	}
                }
            }
            catch( Exception e )
            {
                Log.err( "Failed to Buffered-Reader for " + e );
            }
            finally
            {
                if(null != subprocess )
                {
                    InputStream es = subprocess.getErrorStream();
                    OutputStream os = subprocess.getOutputStream();
                    try
                    {
                        is.close();
                        is = null;
                        es.close();
                        es = null;
                        os.close();
                        os = null;
                    }
                    catch(IOException e)
                    {
                    }
                    subprocess = null;
                    reader = null;
                }
            }

        }
    }

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}
}
