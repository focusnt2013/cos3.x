package com.focus.cos.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import com.focus.util.Log;

/**
 * 子进程阅读器
 * @author focus
 *
 */
public abstract class SubprocessReader extends Thread
{
	private String id;
    private Process subprocess;
    protected ArrayList<String> commands;
//    protected int countClassNotFoundException = 0;
    protected int countException = 0;
//    protected String stringClassNotFoundException;
    protected LinkedList<String> theExceptions = new LinkedList<String>();
    protected long timestamp;//异常时间戳
    protected Thread alarmThread;
    protected long lastExceptionTimestamp;
    protected long alarmTimestamp;
    public SubprocessReader( String id, Process p, ArrayList<String> commands )
    {
    	this.id = id;
        this.subprocess = p;
        this.commands = commands;
    }
    
    /**
     * 检测到子进程因为ClassNotFound错误的时候调用
     */
//    public abstract void hanldeClassNotFoundException(String line);
    /**
     * 检测到子进程有相关错误的时候调用
     */
    public abstract void hanldeException(String line);
    /**
     * 处理子程序输出的日志信息
     */
    public abstract void handleOutput(String line);

    public void run()
    {
//        if( "Zookeeper".equals(id) || 
//        	"COSPortal".equals(id) ){
//        	System.out.println(String.format("@COS$ [%s] The reader of daemon(inner) has been start.", id));
//            Log.msg( String.format("[%s] The debug-reader of daemon(inner) has been start.", id) );
//        }
        BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader( subprocess.getInputStream() ) );
        String line = null;
        try
        {
            while( ( line = bufferedReader.readLine() ) != null )
            {
//            	if( line.indexOf("ClassNotFoundException") != -1 )
//            	{
//            		this.hanldeClassNotFoundException(line);
//            	}
//            	else 
            	if( line.toLowerCase().indexOf("exception") != -1 || line.toLowerCase().indexOf("error") != -1 )
            	{
            		if(timestamp == 0 ){
            			timestamp = System.currentTimeMillis();
            		}
            		this.hanldeException(line);
            	}
            	this.handleOutput(line);
            }
        }
        catch( Exception e )
        {
            Log.err( String.format("[%s] The debug-reader of program break for %s", id, e.getMessage()) );
        }
        finally
        {
            if(null != subprocess )
            {
                InputStream is = subprocess.getInputStream();
                InputStream es = subprocess.getErrorStream();
                OutputStream os = subprocess.getOutputStream();
                //Do something with the proc or inputstrea
                try
                {
                    bufferedReader.close();
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
            }
        }

    }
}
