package com.focus.cos.control;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public abstract class JSH extends Thread
{
//	private OutputStream out = null;
	private PrintStream output = null;
//	private PrintWriter sh_writer = null;
	private InputStream sh_reader_err = null;
	private InputStream sh_reader = null;
	private Process sh = null;
	protected String command = null;//指令参数

    public static void main(String args[])
    {
    	JSH jsh = new JSH(System.out){
			public void finish()
			{
		        System.exit(0);
			}
    		
    	};
    	jsh.startupSystemReader();//启动系统监听
    	jsh.execute(args);
    }

    public JSH(OutputStream out, String command)
    {
    	this.output = new PrintStream(out);
    	this.command = command;
    }
    
    public JSH(OutputStream out)
    {
    	this.output = new PrintStream(out);
    }
    
    public abstract void finish();
    
    public void startupSystemReader()
    {
    	SystemReader reader = new SystemReader();
    	reader.start();
    }

    /**
     * 写指令
     * @param command
     */
    public void write(String command) throws Exception
    {
    	try
    	{
			if( sh == null )
			{
				throw new Exception("Failed to exec("+command+") for ssh_out closed.");
			}
			byte payload[] = command.getBytes();
			for( int j = 0; j < payload.length; j++ )
			{
				sh.getOutputStream().write(payload[j]);
				sh.getOutputStream().flush();
				sleep(300);
			}
			sh.getOutputStream().write('\n');
			sh.getOutputStream().flush();
			sh.getOutputStream().write('\r');
			sh.getOutputStream().flush();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace(output);
    	}
    }
    
    public void run()
    {
    	this.execute(command.split(" "));
    }
    /**
     * 执行
     * @param args
     */
    public void execute(String args[])
    {
    	if( args.length == 0 )
    	{
    		System.out.println("Failed to execute for null input.");
    		return;
    	}
        try
        {    	
            ProcessBuilder pb = new ProcessBuilder( args );
            pb.redirectErrorStream( true );
            sh = pb.start();
//            sh_writer = new PrintWriter(sh.getOutputStream());
//            sh_writer.flush();
			sh.getOutputStream().write('\n');
            sh.getOutputStream().flush();
            sh_reader = sh.getInputStream();
            sh_reader_err = sh.getErrorStream();;
            //从SH读取数据
            Thread thread = new Thread()
            {
            	public void run()
            	{
            		try
            		{
//            			System.out.println("read sh inputstream:"+sh_reader.available());
            			int b = 0;
            			//out.write(sh_reader.available());
            			while( (b = sh_reader.read() ) != -1 )
            			{
            				output.write(b);
            			}
            		}
            		catch(Exception e)
            		{
            			e.printStackTrace(output);
            		}
                	sh_reader = null;
            	}
            };
            thread.start();
            //读取SH错误数据
            thread = new Thread()
            {
            	public void run()
            	{
            		try
            		{
            			int b = 0;
//            			System.out.println("read sh error inputstream:"+sh_reader_err.available());
            			while( (b = sh_reader_err.read() ) != -1 )
            			{
            				output.write(b);
            			}
            		}
            		catch(Exception e)
            		{
            			e.printStackTrace(output);
            		}
                	sh_reader_err = null;
            	}
            };
            thread.start();
    		sh.waitFor(); // 等待编译完成
			Thread.sleep(300);
        }
        catch (Exception e)
        {
			e.printStackTrace(output);
        }
        finally
        {
        	if( sh_reader != null )
                try
                {
                	sh_reader.close();
                }
                catch (Exception e)
                {
                }

        	if( sh_reader_err != null )
                try
                {
                	sh_reader_err.close();
                }
                catch (Exception e)
                {
                }
//        	if( sh_writer != null )
//                try
//                {
//                	sh_writer.close();
//                }
//                catch (Exception e)
//                {
//                }
        	if( sh != null )
                try
                {
                	sh.destroy();
                }
                catch (Exception e)
                {
                }
                sh = null;
        }
        finish();
    }
    
    class SystemReader extends Thread
    {
    	public void run()
    	{
//    		BufferedReader user_reader = new BufferedReader( new InputStreamReader(System.in) );
//    		String line = null;
    		try
			{
    			byte[] payload = new byte[1024];
    			int len = 0;
    			int offset = 0;
    			while( (len = System.in.read(payload, offset, payload.length-offset)) != -1 )
    			{
//    				System.out.print("len:"+len);
    				for( int i = 0; i < len; i++ )
    				{
    					int b = payload[offset];
    					if( b == 0x0A || b == 0x0D )
    					{
//    						System.out.print(new String(payload, 0, offset));
    						for( int j = 0; j < offset; j++ )
    						{
    							sh.getOutputStream().write(payload[j]);
        						sh.getOutputStream().flush();
								sleep(300);
    						}
    						sh.getOutputStream().write('\r');
    						sh.getOutputStream().flush();
    						sleep(300);
    						sh.getOutputStream().write('\n');
    						sh.getOutputStream().flush();
    						offset = 0;
    						break;
    					}
    					else
    					{
    						offset += 1;
    					}
    				}
    			}
//    			byte[] payload = new byte[10240];
//    			int b = 0;
//    			int offset = 0;
//				while( (b = System.in.read()) != -1 )
//				while( (line = user_reader.readLine()) != null )
				{
//    				System.out.print(Integer.toHexString(b));
//    				System.out.print(' ');
//					if( b == 0xa || b == 0xd )
//					{
//						Tools.printb(payload, 0, offset);
//						sh_writer.println(new String(payload, 0, offset));
//						sh_writer.flush();
//						offset = 0;
//					}
//	    			else if( b != '\n' && b != '\r' )
//	    			{
//	    				payload[offset++] = (byte)b;
//	    			}
//					System.out.println(line);
//					sh_writer.println(line);
//					sh_writer.flush();
//					user_reader.reset();
				}
			}
			catch (Exception e)
			{
    			e.printStackTrace(output);
			}
			System.exit(1);
    	}
    }
}
