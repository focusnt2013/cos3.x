package com.focus.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;

public class IOHelper
{
	/**
	 * 得到目录的数据大小
	 * @param dir
	 * @return
	 */
	public final static long getDirSize(File dir)
	{
		if( !dir.exists() ) return 0;
		if( dir.isFile() ) return 0;
		
		long size = 0;
		File[] files = dir.listFiles();
		for(File file : files)
		{
			if( file.isDirectory() )
			{
				size += getDirSize(file);
			}
			else
			{
				size += file.length();
			}
		}
		return size;
	}
	
	public static final long[] getPathSize( File path )
	{
		if( path == null || !path.exists() )
		{
			return new long[]{0,0};
		}
		if( path.isFile() )
		{
			return new long[]{1,path.length()};
		}
		long length[] = {0,0};
		File files[] = path.listFiles();
		for( File file : files )
		{
			long result[] = getPathSize(file);
			length[0] += result[0];
			length[1] += result[1];
		}
		return length;
	}
	
    public static final int deleteFile( File file )
    {
        if( file == null )
        {
            return 0 ;
        }
        
        int count = 0;
        if( file.exists() && file.isDirectory() )
        {
            File files[] = file.listFiles();
            for(int i = 0; i < files.length; i++)
            {
            	count += deleteFile(files[i]);
            }
        }
        if( file.exists() )
        {
        	count += file.delete()?1:0;
        }
        return count;
    }
    /**
     * 删除指定目录下所有文件
     * @param dir
     */
    public static final int deleteDir( File dir )
    {
        if( dir == null )
        {
            return 0;
        }
        
        int count = 0;
        if( dir.exists() && dir.isDirectory() )
        {
            File files[] = dir.listFiles();
            for(int i = 0; i < files.length; i++)
            {
            	count += deleteFile(files[i]);
            }
        }
        dir.delete();
        return count;
    }
    
    public static final void moveFile(File srcFile, File destFile)
    {
    	if( srcFile == null || destFile == null || !srcFile.exists())
        {
            return;
        }

        if( srcFile.isDirectory())
        {
        	if(!destFile.exists())
        	{
        		destFile.mkdirs();
        	}
            File files[] = srcFile.listFiles();
            for(int i = 0; i < files.length; i++)
            {
            	File dest = new File(destFile, files[i].getName());
            	moveFile(files[i],dest);
            }
            
            srcFile.delete();
        }
        else if(srcFile.isFile())
        {
        	if(destFile.exists())
        	{
        		destFile.delete();
        	}
        	
        	Log.msg("move src(" + srcFile.getPath() + ") to dest(" + destFile.getPath()+ ")." );
        	
        	srcFile.renameTo(destFile);
        }
    }
    
    /**
     * 压缩文件到gzip中
     * @param file
     * @return
     */
    public static final File gzip(File file)
    {
    	if( file.isDirectory() ) return null;
    	File outfile = new File(file.getParentFile(), file.getName()+".gz");
		try
		{
        	FileOutputStream fos = new FileOutputStream(outfile);
            GZIPOutputStream gos = new GZIPOutputStream(fos);
            gos.write(IOHelper.readAsByteArray(file));
            gos.flush();
			gos.finish();
			gos.close();
            fos.close();
		}
		catch(Exception e)
		{
		}
		return outfile;
    }
    
    public static final void writeFile(File file, byte[] payload)
    {
        try
        {
        	File dir = file.getParentFile();
        	if( !dir.exists() ) dir.mkdirs();
	    	FileOutputStream out = new FileOutputStream(file);
	    	out.write(payload);
	    	out.close();
        }
        catch( Exception e )
        {
        	e.printStackTrace();
        }
    }

    public static final void writeFile(File file, byte[] payload, int offset, int length)
    {
        try
        {
	    	FileOutputStream out = new FileOutputStream(file);
	    	out.write(payload, offset, length);
	    	out.close();
        }
        catch( Exception e )
        {
        }
    }

    /**
     * 写输入流数据到文件
     * @param file
     * @param is
     * @return 写文件的结果
     */
    public static final String writeFile(File file, InputStream is)
    {
    	long size = 0;
        try
        {
	    	FileOutputStream out = new FileOutputStream(file);
	    	byte payload[] = new byte[65536];
	    	int len = 0;
	    	while( (len = is.read(payload, 0, payload.length)) != -1 )
	    	{
	    		size += len;
	    		out.write(payload, 0, len);
	    	}
	    	out.flush();
	    	out.close();
        }
        catch( Exception e )
        {
        	return String.format("Failed to write(%s) to %s for %s.", size, file.getName(), e.getMessage());
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
        return null;
    }
    
    public static final byte[] readAsByteArray( File file )
    {
        if( file != null && file.exists() )
        {
            try
            {
                FileInputStream fis = new FileInputStream( file );
                byte[] buffer = new byte[ fis.available() ];
                fis.read( buffer );
                fis.close();
                return buffer;
            }
            catch( Exception e )
            {
                return new byte[0 ];
            }
        }
        else
        {
            return new byte[0 ];
        }
	}

    public static final byte[] readAsByteArray( InputStream is )
    	throws Exception
    {
//        byte[] buffer = new byte[ is.available() ];
//        is.read( buffer );
//        is.close();
//        return buffer;

		List<byte[]> all = new ArrayList<byte[]>();
		int read = -1;
		int count = 0;
		byte[] catchs = new byte[65536];
		try {
			while((read = is.read(catchs))>-1){
				count += read;
				byte[] data = new byte[read];
				System.arraycopy(catchs,0, data, 0, read);
				all.add(data);
			}
		} catch (IOException e) {
		}
		finally{
			is.close();
		}
		
		byte[] allDatas = new byte[count];
		int copyCounts = 0;
		for(byte[] b : all){
			System.arraycopy(b, 0, allDatas, copyCounts, b.length);
			copyCounts += b.length;
		}
		
		return allDatas;
	}

    public static final Serializable readSerializable( File file )
        throws
        Exception

    {
        if( !file.exists() )
        {
            throw new Exception( "Failed to serialize for unknown path "+file.getAbsolutePath() );
        }
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        Serializable s = null;
        try
        {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            s = (Serializable) ois.readObject();
            return s;
        }
        catch(Exception e )
        {
            throw e;
        }
        finally
        {
            ois.close();
            fis.close();
        }
    }

    public static final Serializable readSerializable( String path )
        throws
        Exception
    {
        return readSerializable( new File( path ) );
    }

    public static final void writeSerializable( File file, Serializable serial)
    {
        try
        {
        	File dir = file.getParentFile();
        	if( !dir.exists() ) dir.mkdirs();
            FileOutputStream fos = new FileOutputStream( file );
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream( fos );
            oos.writeObject( serial );
            oos.close();
            fos.close();
        }
        catch( FileNotFoundException e )
        {
        }
        catch( IOException e )
        {
        }
    }

    public static final void writeSerializable(
        String name, Serializable serial )
    {
        File file = new File( name );
        writeSerializable(file,serial);
    }


    public static final void writeSerializable(
        File dir, String name, Serializable serial )
    {
        if( !dir.exists() )
        {
            dir.mkdirs();
        }

        File file = new File( dir, name );
        try
        {
            FileOutputStream fos = new FileOutputStream( file, false );
            ObjectOutputStream oos = null;
            oos = new ObjectOutputStream( fos );
            oos.writeObject( serial );
            oos.close();
            fos.close();
        }
        catch( FileNotFoundException e )
        {
        }
        catch( IOException e )
        {
        }
    }

    public static final void writeSerializable(
        String path, String name, Serializable serial )
    {
        File dir = new File( path );
        if( !dir.exists() )
        {
            dir.mkdirs();
        }
        writeSerializable(dir,name,serial);
    }

    public static final Serializable readSerializableNoException( File path )
     {
        try
        {
            return readSerializable( path );
        }
        catch( Exception ex )
        {
            return null;
        }
    }

    /*
    public static final Serializable readSerializableNoException( String path )
     {
        try
        {
            return readSerializable( new F( path ) );
        }
        catch( Exception ex )
        {
            return null;
        }
    }
*/
    /**
     * 
     * @param payload
     * @param offset
     * @param length
     * @return
     * @throws Exception
     */
    public static final Serializable readSerializable( byte payload[], int offset, int length )
        throws Exception
    {
        ObjectInputStream ois = null;
        Serializable s = null;
        try
        {
            ois = new ObjectInputStream(new java.io.ByteArrayInputStream(payload, offset, length));
            s = (Serializable) ois.readObject();
            return s;
        }
        catch(Exception e )
        {
            throw e;
        }
        finally
        {
            ois.close();
        }
    }

    /**
     * 
     * @param payload
     * @param offset
     * @param length
     * @return
     * @throws Exception
     */
    public static final Serializable readSerializableNoException( InputStream is )
    {
    	if( is == null ) return null;
        ObjectInputStream ois = null;
        Serializable s = null;
        try
        {
            ois = new ObjectInputStream(is);
            s = (Serializable) ois.readObject();
            return s;
        }
        catch(Exception e )
        {
        	return null;
        }
        finally
        {
            try
			{
				ois.close();
	            is.close();
			}
			catch (IOException e)
			{
			}
        }
    }
    /**
     * 从二级制字节流中读取序列化对象
     * @param payload
     * @return
     */
    public static final Serializable readSerializableNoException( byte payload[] )
    {
    	return readSerializableNoException( payload, 0, payload.length );
    }
    /**
     * 从二级制字节流中读取序列化对象
     * @param payload
     * @param offset
     * @param length
     * @return
     */
    public static final Serializable readSerializableNoException( byte payload[], int offset, int length )
    {
        try
        {
            return readSerializable( payload, offset, length );
        }
        catch( Exception ex )
        {
            return null;
        }
    }
    
    /**
     * 写序列化对象到输出流
     * @param serial
     * @param out
     */
    public static final void writeSerializable(Serializable serial, OutputStream out)
    {
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream( out );
            oos.writeObject( serial );
            oos.flush();
        }
        catch( Exception e )
        {
        }
    }
    
    /**
     * 将序列化对象转换为字节流
     * @param obj
     * @return
     */
    public static byte[] convertObjectToBytes(Object obj)
    {
    	if (obj == null) return null;
    	
    	ByteArrayOutputStream out = null;
    	ObjectOutputStream oos = null;
    	try 
    	{
    		out = new ByteArrayOutputStream();
        	oos = new ObjectOutputStream(out);
            oos.writeObject(obj);
            oos.flush();
            
            return out.toByteArray();
		}
    	catch (Exception e)
    	{
		}
    	finally
    	{
    		try {
	    		if (oos != null) 
				{
					oos.close();
				}
	    		if (out != null)
				{
	    			out.close();
				}
    		}
    		catch (IOException e) {}
		}
    	
    	return null;
    }
    
    /**
     * 从文件中读取所有行数据
     * @return
     */
    public static List<String> readLines(File file, String charset)
    {
		try {
			return readLines(new FileInputStream(file), charset);
		} catch (FileNotFoundException e) {
		}
		return new ArrayList<String>();
    }

    public static List<String> readLines(InputStream is, String charset)
    {
    	ArrayList<String> lines = new ArrayList<String>();
    	BufferedReader reader = null;
		try
		{
			if( charset != null ) reader = new BufferedReader( new InputStreamReader(is, charset) );
			else reader = new BufferedReader( new InputStreamReader(is) );
			String line = null;
			while( (line = reader.readLine()) != null )
				lines.add(line);
		}
		catch (Exception e)
		{
		}
		finally
		{
			if( reader != null )
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
				}
		}
		return lines;
    }

    /**
     * @return
     */
    public static String readFirstLine(File file)
    {
    	String line = null;
    	BufferedReader reader = null;
		try
		{
			reader = new BufferedReader( new InputStreamReader(new FileInputStream(file)) );
			line = reader.readLine();
		}
		catch (Exception e)
		{
		}
		finally
		{
			if( reader != null )
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
				}
		}
		return line;
    }
    /**
     * 
     * @param src
     * @param dest
     * @return
     */
    public static final boolean copy( File src, File dest )
    {
        if( src != null && src.exists() )
        {
        	if( !dest.getParentFile().exists() )
        		dest.getParentFile().mkdirs();
        	FileInputStream fis = null;
        	FileOutputStream out = null;
            try
            {
            	fis = new FileInputStream( src );
            	out = new FileOutputStream(dest);
    			int len = 0;
    			byte[] payload = new byte[64*1024];
    			while( (len = fis.read(payload)) != -1 )
                {
                	out.write(payload, 0, len);
                	out.flush();
                }
                return true;
            }
            catch( Exception e )
            {
            }
            finally
            {
            	if( fis != null )
					try
					{
						fis.close();
					}
					catch (IOException e)
					{
					}
            	if( fis != null )
					try
					{
						out.close();
					}
					catch (IOException e)
					{
					}
            }
        }
    	return false;
	}

	/**
	 * 读取数据
	 * @param is
	 * @param payload
	 * @param length
	 * @return
	 * @throws Exception
	 */
	public static int read(InputStream is, byte[] payload, int length)
	    throws Exception
	{
		int offset = 0;
		while( length > 0 )
		{
			int len = is.read(payload, offset, length);
			offset += len;
			length -= len;
		}
		return offset;
	}
	
	public static void main(String[] args){
		String path = "D:/focusnt/cos/trunk/IDE/h2/cos.mv.db/";
		File dir = new File(path);
		File file = new File(dir, "cos.mv.db");
		try {
			FileUtils.copyFile(file, new File("D:/focusnt/cos/trunk/IDE/h2/cos.mv.db"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
