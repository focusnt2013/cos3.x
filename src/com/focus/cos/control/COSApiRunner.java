package com.focus.cos.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.focus.cos.api.util.ConfigUtil;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;

public class COSApiRunner extends SystemRunner
{
	public static final String Name = "主控接口服务引擎"; 
	public static final String Remark = "提供主控日志告警，以及扩展服务接口功能的代理及受理。";

	private RegisterReader reader = null;
	
    public COSApiRunner(ModuleManager manager)
    {
    	super( "COSApi", manager );
    	super.className = "com.focus.cos.control.COSApi";
        this.setName( Name );
        this.setRemark(Remark);
    }

	@Override
	public void initliaize() throws Exception
	{
		String s = ConfigUtil.getString("cos.jdbc.driver");
		if( s != null && !s.isEmpty() && !manager.isDatabaseStandby() )
		{
			manager.apiNostartup();
			throw new Exception("主数据库未启动不能启动[主控接口服务引擎]");
		}
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
    		reader = new RegisterReader(p, commands);
    		reader.start();
    	}
    }

    class RegisterReader extends Thread
    {
        private Process subprocess;
        protected ArrayList<String> commands;
        public RegisterReader( Process p, ArrayList<String> commands )
        {
            this.subprocess = p;
            this.commands = commands;
        }
        
        public void run()
        {
            int method = -1;
            int len = 0;
            InputStream is = subprocess.getInputStream();
            try
            {
            	byte[] payload = new byte[65536];
                while( ( method = is.read() ) != -1 )
                {
                	switch(method)
                	{
                	case 0:
                		len = is.read();
                		IOHelper.read(is, payload, len);
                		setVersion(new String(payload, 0, len));
                		IOHelper.read(is, payload, 4);
                		manager.setApiPort(Tools.bytesToInt(payload, 0, 4));
                		break;
                	case 1://主控代理节点注册
                		IOHelper.read(is, payload, 4);
                		len = Tools.bytesToInt(payload, 0, 4);
                		IOHelper.read(is, payload, len);
                		manager.setApiProxy(payload, len);
                		break;
                	case 2://
                		IOHelper.read(is, payload, 4);
                		len = Tools.bytesToInt(payload, 0, 4);
                		IOHelper.read(is, payload, len);
                		manager.setApiAgent(payload, len);
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
                    try
                    {
                        is.close();
                        is = null;
                    }
                    catch(IOException e)
                    {
                    }
                    subprocess = null;
                    reader = null;
                }
            }

    		if( mode != STATE_SHUTDOWN_WAIT && mode != STATE_SUSPEND_WAIT )
    		{
    			
    		}
            System.out.println("The COS-API close.");
            System.out.println("@COS$");
        }
    }

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}
}
