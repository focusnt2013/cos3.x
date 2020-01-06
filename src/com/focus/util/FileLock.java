package com.focus.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class FileLock 
{
	private FileChannel fileChannel;
	private java.nio.channels.FileLock fileLock;
	/**
	 * @param mutexFile
	 * @return
	 */
	public static FileLock getFileLock(File mutexFile) throws Exception
    {
    	Log.msg("try to getFileLock.");
    	FileChannel lockfc = null;
    	java.nio.channels.FileLock flock = null;
    	try
    	{
        	if( !mutexFile.exists() ) 
        	{ 
        		File path= mutexFile.getParentFile();
            	if(!path.exists()) 
            	{ 
            		path.mkdirs(); 
            	} 
            	mutexFile.createNewFile(); 
        	} 
        	RandomAccessFile fis = new RandomAccessFile(mutexFile,"rw"); 
        	lockfc = fis.getChannel(); 
        	flock = lockfc.lock(); 
        	FileLock retFileLock = new FileLock();
        	retFileLock.fileChannel = lockfc;
        	retFileLock.fileLock = flock;
        	
        	Log.msg("suc to getFileLock.");
        	return retFileLock;
    	}
    	catch (Exception e)
    	{
			// TODO: handle exception
    		try
    		{
    			if( flock != null ) 
                { 
                	flock.release(); 
                	flock = null;
                } 
                if( lockfc != null ) 
                { 
                	lockfc.close(); 
                	lockfc = null;
                } 
    		}
    		catch (Exception ex) 
    		{
				// TODO: handle exception
			}
    		
    		Log.err("failed to getFileLock.");
    		throw e;
		}
    	
    	
    }
    
    /**
     * �ͷ��ļ���
     */
    public void release() throws Exception
    {
    	try
		{
			if( fileLock != null ) 
            { 
				fileLock.release(); 
				fileLock = null;
            } 
            if( fileChannel != null ) 
            { 
            	fileChannel.close(); 
            	fileChannel = null;
            }
            
            Log.msg("suc to release FileLock.");
		}
		catch (Exception ex) 
		{
			// TODO: handle exception
			Log.err("failed to release FileLock.");
			throw ex;
		}
    }
}
