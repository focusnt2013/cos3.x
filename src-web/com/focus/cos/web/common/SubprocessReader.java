package com.focus.cos.web.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.commons.logging.Log;

public class SubprocessReader extends Thread
{
    protected Process subprocess;
    protected Log log;
    public SubprocessReader( Process p, Log log )
    {
    	this.log = log;
        this.subprocess = p;
    }

    public void run()
    {
        //当进程结束时会关闭这两个流
        BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader( subprocess.getInputStream() ) );
        String line = null;
        try
        {
            while( ( line = bufferedReader.readLine() ) != null )
            {
            	log.debug(line);
            }
        }
        catch( Exception e )
        {
        	log.info( "Failed to Buffered-Reader for " + e );
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
	            subprocess.destroy();
	        }
        }

    }
	/**
	 * 关闭
	 */
	public void close()
	{
		try
		{
			subprocess.destroy();
		}
		catch(Exception e)
		{
		}
	}
}
