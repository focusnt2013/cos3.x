package com.focus.util;

import java.io.IOException;
import java.io.InputStream;

public class BlockedInputStream extends java.io.FilterInputStream
{
	public BlockedInputStream(InputStream in) 
	{
		// TODO Auto-generated constructor stub
		super(in);
	}
	
//	public int read() throws IOException
//	{
//		if(din == null)
//		{
//			throw new IOException();
//		}
//		
//		return din.read();
//	}
	
	public int read(byte[] b,int off,int len)throws IOException
    {
//		if(din == null)
//		{
//			throw new IOException();
//		}
		
		if(off < 0 || len < 0 || len > b.length - off)
		{
			throw new IndexOutOfBoundsException();
		}
		
		int offset = off;
    	int remainLen = len;
    	int c = 0;
    	while( remainLen > 0 )
    	{
    		c = read();
    		if( c == -1 )
    		{
    			Log.war("read return -1 at offset:" + offset + ",remainLen:" + remainLen);
                return -1;
    		}
    		
    		b[offset++] = (byte)c;
    		remainLen--;
    	}

    	return len;
    }
	
//	public void close() throws IOException
//	{
//		if(din != null)
//		{
//			din.close();
//		}
//	}
//	
//	public long skip(long n) throws IOException
//	{
//		if(din == null)
//		{
//			throw new IOException();
//		}
//		
//		return din.skip(n);
//	}
}
