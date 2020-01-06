package com.focus.synch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.focus.util.ConfigUtil;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;

/**
 * <p>Title: 行业网关模拟</p>
 *
 * <p>Description: 模拟EMA需要接入的各种行业网关</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: FOCUS</p>
 *
 * @author Focus Lau
 * @version 1.0
 */
public abstract class FileSynchAgent extends SynchAgent
{
    public static String ModuleID = "FileSynch";
    //需要更新的文件夹
    protected static List<String> ListPath = new ArrayList<String>();
    //同步路径
    protected static String SynchPath = ConfigUtil.getWorkPath();
    // 半同步路径（只同步源上有的到目标，不删除目标中源没有的文件和目录）
    protected static List<String> HalfSynchPath = new ArrayList<String>();
    // 过滤文件或文件夹
    protected static Map<String, String> FilterSynchPath = new HashMap<String, String>();
    // 文件同步路径(只同步源上制定路径下的文件（非递归），二级目录不需要同步)
    protected static List<String> onlyFileSynchPath = new ArrayList<String>();
    protected File fileSynchList;//同步清单文件
    
    protected byte[] fileOutBytes = new byte[SIZE_BUFFER];
    protected byte[] fileInBytes = new byte[SIZE_BUFFER];
    // 是否忽略对端发过来的同步通知（也就是是否是强推模式）
    protected static boolean omitOppSynchInd = false;
    
    public FileSynchAgent( String ip, int port)
        throws Exception
    {
        super(ip, port);
        //fileSynchList = new File(ConfigUtil.APP_PATH, "log/"+ModuleID+"/synch.txt");
    }

    public FileSynchAgent( Socket socket )
        throws Exception
    {
        super(socket);
        //fileSynchList = new File(ConfigUtil.APP_PATH, "log/"+ModuleID+"/synch.txt");
    }
    
    /**
     * 写同步日志
     * @param log
     */
    private void writeSynchLog(String log)
    {
    	if( fileSynchList != null )
    	{
	    	FileOutputStream writer = null;
	        try
	        {
	        	writer = new FileOutputStream( fileSynchList, true );
	            byte[] buffer = log.getBytes();
	            writer.write( buffer );
	            writer.write( '\r' );
	            writer.write( '\n' );
	            writer.flush();
	        }
	        catch( Exception e )
	        {
	        	Log.err(e);
	        }
	        finally
	        {
	            if( writer != null )
	            {
	            	try
					{
						writer.close();
					}
					catch (IOException e)
					{
					}
	            }
	        }
    	}
    }
    /**
     * 过滤
     * @param path
     * @param filters
     * @return
     */
    private boolean filter(File path, String filters[])
    {
    	if( filters == null )
    	{
    		return false;
    	}
    	boolean isFilter = false;
    	for( String filter : filters )
    	{
    		if( filter.endsWith("/") && path.isDirectory() )
    		{
    			isFilter = filter.indexOf(path.getName()) != -1;
    		}
    		else if( path.isFile() )
    		{
	    		if( filter.startsWith("*") )
	    		{
	    			String suffix = filter.substring(1);
	    			isFilter = path.getName().endsWith(suffix);
	    		}
	    		else if( filter.endsWith("*") )
	    		{
	    			String suffix = filter.substring(0, filter.length() - 1 );
	    			isFilter = path.getName().startsWith(suffix);
	    		}
	    		else
	    		{
	    			isFilter = path.getName().equals(filter);
	    		}
    		}
    		
    		if( isFilter ) break;
    	}
    	return isFilter;
    }
    /**
     * 将文件同步清单发送过去
     * @param path File 文件实际路径
     * @param dir String 文件虚拟路径
     * @param mapSynchFiles 映射的同步文件列表
     */
    /**
     * @param path			文件实际路径
     * @param strPath		文件虚拟路径
     * @param mapSynchFiles	返回的同步文件清单
     * @param absolute		是否使用绝对路径
     * @param halfSynch		是否是单向同步（不根据本地的目录结构删除对端的目录或者文件）
     * @param recurssive	递归层数
     * @param filters	    过滤项
     */
    protected void getSychFileList(
    		File path,
    		String strPath,
    		HashMap<String, Long[]> mapSynchFiles,
    		boolean absolute,
    		boolean halfSynch,
    		int recurssive,
    		String[] filters)
    {
    	if( !path.exists() )
    	{
        	Log.war("Failed to get synch path:"+path.getPath());
    		return;
    	}
    	//过滤不传送
    	if( filter(path, filters) )
    	{
    		return;
    	}
    	//将文件加入同步指示表中
        if( path.isFile() )
        {
        	if( strPath.endsWith(".synch") || path.isHidden() )
        	{
        		return;//正在传送的文件
        	}
        	if( absolute )
        	{
        		mapSynchFiles.put(path.getPath(), new Long[]{halfSynch?(-path.lastModified()):path.lastModified(), path.length()});
        	}
        	else
        	{
        		mapSynchFiles.put(strPath, new Long[]{halfSynch?(-path.lastModified()):path.lastModified(), path.length()});
        	}
            return;
        }
        if( !strPath.endsWith("/") )
        {
        	strPath += "/";
        }
        
        recurssive--;
        if(recurssive >= 0)
    	{
        	//同步目录也要发送过去。如果目录是半同步模式，通过时间戳设置为负数通知对端。
        	mapSynchFiles.put(strPath, new Long[]{halfSynch?(-path.lastModified()):path.lastModified(), path.length()});				
	        File files[] = path.listFiles();
	        for( File file : files )
	        {
	        	String strPathChild = strPath+file.getName();//子目录的文件或目录名
	        	getSychFileList(file, strPathChild, mapSynchFiles, absolute, halfSynch, recurssive, filters);
	        }
    	}
    }

    /**
     * 处理文件同步请求
     * synch_req码流格式：命令字（1）虚拟路径字符串长度（4）虚拟路径字符串（NA）时间戳字段字符串的长度（1）时间戳字段字符串（NA）
     */
    protected void handle_synch_req() throws Exception
    {
    	byte[] bytesLength = new byte[4];
        //----------------------------------------------------------------------
        //读取虚拟路径字符串长度
        read( bytesLength, 0, 4 );
        int pathStrLen = Tools.bytesToInt(bytesLength, 0 , 4);//虚拟路径字符串长度
        //----------------------------------------------------------------------
        byte pathStrBuf[] = new byte[pathStrLen];
        //读取虚拟路径字符串数据
        read( pathStrBuf, 0, pathStrLen );
        String pathStr = new String(pathStrBuf, "UTF-8");
        //----------------------------------------------------------------------
        //读取时间戳字段字符串的长度
        read( bytesLength, 0, 1 );
        int timestampStrLen = bytesLength[0];
        //----------------------------------------------------------------------
        byte timestampStrBuf[] = new byte[timestampStrLen];
        //读取时间戳字符串数据
        read( timestampStrBuf, 0, timestampStrLen );
        String timestampStr = new String(timestampStrBuf);
        long timestamp = Long.parseLong(timestampStr);
        File fileSynch = new File(SynchPath, pathStr);
//        Log.msg(fileSynch.getPath());
        this.submit_synch_rsp(pathStr, fileSynch, timestamp);
    }
    /**
     * 同步确认
     */
    protected void handle_synch_cnf()
    {
    	Log.msg("Succeed to handle_synch_cnf(status="+statusSynch+").");
		this.statusSynch = STATUS_SYNCH_IND_WAIT;//等待下一次同步周期（7秒）
    }
    /**
     * 同步指示
     * synch_ind码流格式：命令字（1）HashMap长度（4）HashMap数据（NA）
     * @param serializable
     */
    @SuppressWarnings("unchecked")
	protected void handle_synch_ind() throws Exception
    {
        byte[] bytesLength = new byte[4];
        //----------------------------------------------------------------------
        //读取虚拟路径字符串长度
        read( bytesLength, 0, 4 );
        int dataLen = Tools.bytesToInt(bytesLength, 0 , 4);//数据长度
        //----------------------------------------------------------------------
        byte payload[] = new byte[dataLen];
        if( dataLen > 0 )
        {
	        //读取传输的数据
	        read(payload, 0, dataLen);
        }
        else
        {
        	Log.msg("Recieve synch.ind(null).");
        }
        
        synchronized( this )
        {
        	if(omitOppSynchInd)
        	{
        		Log.msg("omit opposite synch-ind.");
        		statusSynch = STATUS_SYNCH_WAIT;//收到同步指示但同步清单为空
        		this.submit_synch_cnf();//发送同步确认
        		return;
        	}

        	//判断是否可以执行同步，当等待休息的时候可以处理接收；同时收到的数据必须有效>0，才执行同步处理
	        if( payload.length > 0 && (
	        	statusSynch == STATUS_SYNCH_IDL || 
	        	statusSynch == STATUS_SYNCH_WAIT ||
	        	statusSynch == STATUS_SYNCH_IND_WAIT ) )
	        {//有空闲的话启动线程执行同步
		    	ByteArrayInputStream bais = new ByteArrayInputStream(payload);
		    	ObjectInputStream oos = new ObjectInputStream(bais);
		        Object serializable = oos.readObject();
		    	if( serializable instanceof HashMap )
		    	{
		    		HashMap<String, Long[]> mapSynchFiles = (HashMap<String, Long[]>)serializable;
		    		HandleSynchInd thread = new HandleSynchInd(mapSynchFiles);//处理同步文件映射表（需要启动一个线程）
		    		thread.start();
		    	}
	        }
	        else
	        {//否则直接返回确认
	        	if( statusSynch == STATUS_SYNCH_IND_WAIT )
	        	{//在等待同步指示的状态下改变状态
	        		statusSynch = STATUS_SYNCH_WAIT;//收到同步指示但同步清单为空
	        	}
	        	this.submit_synch_cnf();//发送同步确认
	        }
        }
    }
    
    class HandleSynchInd extends Thread
    {
    	private HashMap<String, Long[]> mapSynchFiles;
    	HandleSynchInd(HashMap<String, Long[]> mapSynchFiles)
    	{
    		this.mapSynchFiles = mapSynchFiles; 
    	}
    	
	    public void run()
	    {
	    	statusSynch = STATUS_SYNCH_IND_PROC;//同步只是等待
	    	writeSynchLog("----------"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis())+"(CountFiles="+mapSynchFiles.size()+")------------");
	    	Log.msg("Recieve synch.ind("+mapSynchFiles.size()+").");
	    	int countSynch = 0;
			HashMap<String, File> mapSynchPaths = new HashMap<String, File>();//保存同步路径的映射关系
			try
			{
				Map<String, Long[]> cache = new HashMap<String, Long[]>();
	    		for(String strSynchFile:mapSynchFiles.keySet())
	    		{
	    			Long[] fileinfo = mapSynchFiles.get(strSynchFile);
	    			long timestamp = fileinfo[0];
	    			long filelength = fileinfo[1];
	    			File file = new File(SynchPath, strSynchFile);
	    			if(strSynchFile.endsWith("/"))
	    			{
	    				// 源是目录
	    				if(!file.exists())
		    			{
	    					// 如果目的地没有目录，需要删除
	    					Log.msg("Dir:" + strSynchFile + " is inexist, try create it.");
	    					file.mkdirs();
	    					writeSynchLog("MKDIR:" + "\t" +file.getPath());
		    			}
	    				else if(!file.isDirectory())
	    				{
	    					// 如果目的地存在与目录名一样的文件，需要删除，并重建文件
	    					Log.msg("Dir:" + strSynchFile + " has same name file conflicted, try remove and recreate it.");
	    					writeSynchLog("DELETE FILE:" + "\t" +file.getPath());
	    					file.delete();
	    					file.mkdir();
	    					writeSynchLog("MKDIR:" + "\t" +file.getPath());
	    				}
	    				else if(timestamp >= 0)//不需要半同步的要检查删除多余的文件和目录
	    				{
	    					//将<同步清单>中属于目录的文件插入<同步路径映射表>，用于后面清楚不需要的文件
		    				mapSynchPaths.put(file.getPath(), file);
	    				}
	    			}
	    			else
	    			{
	    				// 源是文件
	    				if( !file.exists() ||//文件不存在直接同步过来
	    					(timestamp < 0 && file.lastModified() != Math.abs(timestamp))||//如果文件的时间戳是负数，同时文件时间与传递的时间不一致表示无论啥情况必须同步过来
	    					(timestamp < 0 && file.length() != filelength )||
	    					file.lastModified() < timestamp )
		    			{
		    				//文件不存在或者文件的时间戳不一致（发生改变）
				        	Log.msg("Synch file(update!path="+file.getPath()+").");
		    				submit_synch_req(strSynchFile, timestamp);//发起同步请求
		    				countSynch += 1;
		    			}
	    				else if( file.isDirectory())
	    				{
	    					Log.msg("Synch file(dir conflicted!=" + strSynchFile + "), try remove the dir.");
	    					IOHelper.deleteFile(file);
	    					writeSynchLog("DELETE DIR:" + "\t" +file.getPath());
	    					
	    					Log.msg("Synch dir(update!path="+file.getPath()+").");
		    				submit_synch_req(strSynchFile, timestamp);//发起同步请求
		    				countSynch += 1;
	    				}
	    			}
	    			//将本地文件绝对路径保存到<同步清单映射表>中
	    			cache.put(file.getPath(), new Long[]{file.lastModified(), file.length()});
	    		}
	    		mapSynchFiles.putAll(cache);
	    		//当前服务需要同步的文件需要加入到不能够删除的文件中，因为当期服务发起的文件可能与对端不一致
	            for( int i = 0; i < ListPath.size(); i++ )
	            {//只要加入到<同步清单映射表>，稍后就会根据它过滤不需要的文件并进行删除
	                File path = new File(SynchPath, ListPath.get(i));
	                if( path.exists() )
	                {
	                	getSychFileList(path, ListPath.get(i), mapSynchFiles, true, false, Integer.MAX_VALUE, null);
	                }
	            }
	    		//搜索所有同步路径，将不需要的文件删除
	    		for(File synchPath:mapSynchPaths.values())
	    		{
	    			File files[] = synchPath.listFiles();
	    			for( File file:files )
	    			{
    					if( !mapSynchFiles.containsKey( file.getPath() ) )
    					{//同步文件是否存在，如果不存在则删除对应的文件
    						IOHelper.deleteFile(file);
				        	Log.msg("Synch file(delete!path="+file.getPath()+").");
				        	writeSynchLog("DELETE DIR(FILE):" + "\t" +file.getPath());
    					}
	    			}
	    		}
	        }
	        catch(Exception e)
	        {
	        	Log.err("Failed to handle_synch_ind:");
	        	Log.err(e);
	        }
	        finally
	        {
	        	Log.msg("Wait for handle all rsp(count="+countSynchReq+").");
		        while(countSynchReq > 0)
		        {
		        	try
		        	{
		        		Thread.sleep(100);
		        	}
		        	catch (Exception e) 
		        	{
		        		Log.err(e);
		        	}
		        }
		        
		        synchronized(countSynchReqObj)
	        	{
		        	countSynchReq = 0;
	        	}
		        statusSynch = FileSynchAgent.STATUS_SYNCH_WAIT;//结束同步之后进入休息状态
		        writeSynchLog("**********"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis())+"(CountSynch="+countSynch+")************");
		        submit_synch_cnf();//完成后发起同步确认消息
	        }
	    }
    }
    /**
     * 对同步请求做响应，将同步文件的数据发送给对端
     * synch_rsp码流格式：命令字（1）文件相对路径字符串长度（4）文件相对路径字符串（NA）时间戳字段字符串的长度（1）时间戳字段字符串（NA）文件数据长度(4)文件数据（NA）
     * @param synchFile
     * @param fileSynch
     * @param timestamp 发送前的时间
     * @throws Exception
     */
    protected synchronized void submit_synch_rsp(String synchFile, File fileSynch, long timestamp) throws Exception
    {
        Log.msg("handle_synch_req("+fileSynch.getPath()+
                ",remote="+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss SSS",Math.abs(timestamp))+
                ",locale="+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss SSS",fileSynch.lastModified())+
                ",size="+fileSynch.length());
    	FileInputStream fis = null;
    	File tempFile = null;
    	try
    	{
    		byte[] bytesLength = new byte[4];
	        byte[] bytesPathStr = synchFile.getBytes("UTF-8");
//	        Tools.intToBytes(bytesPathStr.length, bytesLength);
	        Tools.intToBytes(bytesPathStr.length, bytesLength, 0, 4);
	        
	        if( !fileSynch.exists() )
	        {
	        	submit_failed_synch_rsp(synchFile);
	        }
	        else if( Math.abs(timestamp) != fileSynch.lastModified() )
	        {
				Log.war("The file(" + synchFile + ") maybe used by another programs.");
	        	submit_failed_synch_rsp(synchFile);
	        }
	        else
	        {
	        	//文件时间戳
		        String timestampStr = Long.toString(timestamp);
		        String timeshow = Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", fileSynch.lastModified());
		        //文件码流
		        long fileLen = fileSynch.length();
		        if(fileLen <= SIZE_MAX_MEMORY_FILE)
		        {
		        	// 直接读到内存后再发送
		        	try
		        	{
		        		fis = new FileInputStream( fileSynch );
		        	}
		        	catch (Exception e)
		        	{
						Log.war("Failed to build InputStream(" + synchFile + ") for exception:"+e);
						submit_failed_synch_rsp(synchFile);
				        return;
					}
		        	
//		        	byte[] fileBytes = new byte[(int)fileLen];
		        	int offset = 0;
		        	while(fileLen > offset)
		        	{
		        		int readLen = fis.read( fileOutBytes , offset, (int)fileLen - offset);
		        		if(readLen == -1)
		        		{
		        			Log.war("file is changed.");
		        			submit_failed_synch_rsp(synchFile);
		        			return;
		        		}
		        		
		        		offset += readLen;
		        	}
		        	
		        	
		        	// 从内存缓存中把文件向socket中写入
		        	out.write(CMD_SYNCH_RSP);
			        //文件相对路径
			        out.write(bytesLength);
			        out.write(bytesPathStr);
		        	out.write((byte)(timestampStr.getBytes().length));
			        out.write(timestampStr.getBytes());
			        Tools.intToBytes((int)fileLen, bytesLength, 0, 4);
			        out.write(bytesLength);//消息长度
			        
			        int totalLen = 0;
			        while(totalLen < fileLen)
			        {
			        	int segmentLen = (int)fileLen - totalLen > SIZE_BUFFER?SIZE_BUFFER:(int)fileLen - totalLen;
			    		out.write(fileOutBytes, totalLen, segmentLen);
				        out.flush();
				        
				        totalLen += segmentLen;
			        }
			        
			        if(totalLen == 0)
			        {
			        	out.flush();
			        }

			        Log.msg("submit_synch_rsp("+synchFile+",time="+timeshow+",size="+fileLen+",use memory buffer).");
		        }
		        else
		        {
		        	// 拷贝到临时文件，再从临时文件读取发送
		        	tempFile = new File(ConfigUtil.getWorkPath(),"temp/" + ModuleID + "_" + this.ip + "_" + this.port + "_out");
		        	File dir = tempFile.getParentFile();
		        	if(dir != null && !dir.exists())
		        	{
		        		dir.mkdirs();
		        	}
		        	
		        	try
		        	{
		        		copyFile(fileSynch, tempFile);
		        	}
		        	catch (Exception e)
		        	{
		        		Log.err("Failed to copy to temp file.");
		        		submit_failed_synch_rsp(synchFile);
		        		return;
		        	}
		        	
		        	if(tempFile.length() != fileLen)
		        	{
		        		Log.war("Temp file is diffrent from source.");
		        		submit_failed_synch_rsp(synchFile);
		        		return;
		        	}
		        	
		        	try
		        	{
		        		fis = new FileInputStream( tempFile );
		        	}
		        	catch (Exception e)
		        	{
						Log.war("file:" + synchFile + " may use by another programs.");
						submit_failed_synch_rsp(synchFile);
				        return;
					}
		        	
		        	out.write(CMD_SYNCH_RSP);
			        //文件相对路径
			        out.write(bytesLength);
			        out.write(bytesPathStr);
			        
		        	
		        	out.write((byte)(timestampStr.getBytes().length));
			        out.write(timestampStr.getBytes());
//			        Tools.intToBytes((int)fileLen, bytesLength);
			        Tools.intToBytes((int)fileLen, bytesLength, 0, 4);
			        out.write(bytesLength);//消息长度
			        
			        int totalLen = 0;
			        int bufferLen = SIZE_BUFFER > (int)fileLen?(int)fileLen:SIZE_BUFFER;
//			        byte[] payload = new byte[bufferLen];
			        while(totalLen < fileLen)
			        {
			            int len = fis.read( fileOutBytes , 0, (int)fileLen - totalLen > bufferLen?bufferLen:(int)fileLen - totalLen);
			    		out.write(fileOutBytes, 0, len);
				        out.flush();
				        
				        totalLen += len;
			        }
			        
			        if(totalLen == 0)
			        {
			        	out.flush();
			        }

			        Log.msg("submit_synch_rsp("+synchFile+",time="+timeshow+",size="+fileLen+",use tempfile).");
		        }
		        
	        }
	    }
	    catch(Exception e)
	    {
	    	Log.err("Failed to submit_synch_rsp:");
	    	Log.err(e);
	    	throw e;
	    }
	    finally
	    {
	    	if(fis != null)
	    	{
	    		fis.close();
	    	}
	    	
	    	if(tempFile != null && tempFile.exists())
	    	{
	    		tempFile.delete();
	    	}
	    }
    }    
    
    /**
     * 发送失败的同步应答（对方对于这种应答不处理）
     * @throws Exception
     */
    private void submit_failed_synch_rsp(String synchFile)throws Exception
    {
    	byte[] bytesLength = new byte[4];
        byte[] bytesPathStr = synchFile.getBytes();
        Tools.intToBytes(bytesPathStr.length, bytesLength, 0, 4);
    	//文件时间戳
        out.write(CMD_SYNCH_RSP);
        //文件相对路径
        out.write(bytesLength);
        out.write(bytesPathStr);
        
    	String timestampStr = "-1";// 文件不存在为
    	out.write((byte)(timestampStr.getBytes().length));	//时间戳畅读
        out.write(timestampStr.getBytes());					//时间戳
        Tools.intToBytes(0, bytesLength, 0, 4);
        out.write(bytesLength);								//消息长度
        Log.msg("submit_synch_rsp("+synchFile+",inexists).");
        out.flush();
    }
    /**
     * 将同步的指示发给对端
     * synch_ind码流格式：命令字（1）HashMap长度（4）HashMap数据（NA）
     * @return
     */
    protected synchronized void submit_synch_ind(java.io.Serializable serializable)
    	throws Exception
    {
    	if( statusSynch != STATUS_SYNCH_IDL )
    	{
    		Log.msg("Failed to submit synch.ind for status is "+statusSynch);
    		return;//不在空闲状态
    	}
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	byte[] payload = null;
        try
        {
        	statusSynch = STATUS_SYNCH_CNF_WAIT;//等待确认
        	if( serializable != null )
        	{
	        	ObjectOutputStream oos = new ObjectOutputStream(baos);
		        oos.writeObject(serializable);
        		payload = baos.toByteArray();
        	}
        	else
        	{
        		payload = new byte[0]; 
        	}
	        out.write(CMD_SYNCH_IND);
	        byte[] bytesLength = new byte[4];
	        Tools.intToBytes(payload.length, bytesLength, 0, 4);
	        out.write(bytesLength);//消息长度
        	int len = 0;
        	int off = 0;
        	while( off < payload.length )//消息分段发送，免得出现溢出
        	{
        		len = payload.length - off;
        		len = len>SIZE_BUFFER?SIZE_BUFFER:len;
        		out.write(payload, off, len);
        		off += len;
    	        out.flush();
        	}
	        Log.msg("submit_synch_ind(size="+payload.length+").");
	        out.flush();
        }
        catch(Exception e)
        {
        	Log.err("Failed to synch_ind:");
        	Log.err(e);
        	throw e;
        }
        finally
        {
        	try
			{
				baos.close();
			}
			catch (IOException e)
			{
			}
        }
    }
    public static final int SIZE_BUFFER = 500*1024;
    public static final int SIZE_MAX_MEMORY_FILE = 500*1024;
    /**
     * 处理同步文件响应，将同步的文件保存到本地目录
     * synch_rsp码流格式：命令字（1）文件相对路径字符串长度（4）文件相对路径字符串（NA）时间戳字段字符串的长度（1）时间戳字段字符串（NA）文件数据长度(4)文件数据（NA）
     * @param fileSynch
     */
    protected void handle_synch_rsp() throws Exception
    {
        byte[] bytesLength = new byte[4];
        //----------------------------------------------------------------------
        //读取虚拟路径字符串长度
        read( bytesLength, 0, 4 );
        int pathStrLen = Tools.bytesToInt(bytesLength, 0 , 4);//虚拟路径字符串长度
        //----------------------------------------------------------------------
        byte pathStrBuf[] = new byte[pathStrLen];
        //读取虚拟路径字符串数据
        read( pathStrBuf, 0, pathStrLen );
        String synchFile = new String(pathStrBuf, "UTF-8");
        //----------------------------------------------------------------------
        //读取时间戳字段字符串的长度
        read( bytesLength, 0, 1 );
        int timestampStrLen = bytesLength[0];
        //----------------------------------------------------------------------
        byte timestampStrBuf[] = new byte[timestampStrLen];
        //读取时间戳字符串数据
        read( timestampStrBuf, 0, timestampStrLen );
        String timestampStr = new String(timestampStrBuf);
        long timestamp = Long.parseLong(timestampStr);
        
        //----------------------------------------------------------------------
        //传输数据的长度
        read( bytesLength, 0, 4 );
        int dataLen = Tools.bytesToInt(bytesLength, 0 , 4);//数据长度

    	FileOutputStream fos = null;
        File file = new File(SynchPath, synchFile);
		File tempSynchFile = new File(SynchPath, synchFile+".synch");
        try
        {
        	Log.msg("handle_synch_rsp("+synchFile+", time="+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", Math.abs(timestamp))+",size="+dataLen + ")");
//	        Log.msg(synchFile);
	        File dir = file.getParentFile();
	        if( !dir.exists() )
	        {
	            dir.mkdirs();
	            writeSynchLog("MKDIR:" + "\t" +dir.getPath());
	        }
	        if( timestamp != 0 && timestamp != -1)//如果时间戳有效，写数据到文件
	        {
	        	boolean isFileValid = true;
	        	if(file.exists() && timestamp > 0 && file.lastModified() > timestamp )
	        	{
	        		Log.war("Not need to synch for timestamp older.");
	        		isFileValid = false;
	        	}
	        	timestamp = Math.abs(timestamp); 
        		int bufferLen = dataLen>SIZE_BUFFER?SIZE_BUFFER:dataLen;
//		        byte[] payload = new byte[bufferLen];
        		if(isFileValid)
        		{
        			fos = new FileOutputStream(tempSynchFile);
        		}
        		
        		int writedLen = 0;
        		while(writedLen < dataLen)
        		{
        			int readLen = in.read(fileInBytes, 0,dataLen - writedLen > bufferLen? bufferLen:dataLen - writedLen);
        			writedLen += readLen;
        		
        			if(isFileValid)
        			{
        				fos.write(fileInBytes,0,readLen);
        			}
        		}
        		
        		if(isFileValid)
        		{
        			writeSynchLog("ADD(UPDATE) FILE:" + "\t" + file.getPath()+"\t"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", timestamp)+"\t"+file.length());
        		}
	        }
        }
        catch (Exception e) 
        {
        	Log.err("Failed to handle_synch_rsp:");
        	Log.err(e);
        	throw e;
		}
        finally
        {
        	synchronized(countSynchReqObj)
            {
            	countSynchReq -= 1;
            }

        	if(fos != null)
        	{
        		fos.close();
        		file.delete();
        		tempSynchFile.renameTo(file);
        		tempSynchFile.delete();
	        	file.setLastModified(timestamp);
        	}
        }
    }

    private Integer countSynchReqObj = 0;
    private int countSynchReq = 0;//发起同步请求技术
    /**
     * 发起文件同步请求
     * synch_req码流格式：命令字（1）虚拟路径字符串长度（4）虚拟路径字符串（NA）时间戳字段字符串的长度（1）时间戳字段字符串（NA）
     * @param strSynchFile 同步文件的相对路径
     * @param timestamp 同步文件的时间戳（本地文件的时间戳）
     */
    protected synchronized void submit_synch_req( String strSynchFile, long timestamp )
    	throws Exception
    {
    	try
    	{
	        byte[] bytesLength = new byte[4];
	        byte[] bytesPathStr = strSynchFile.getBytes("UTF-8");
//	        Tools.intToBytes(bytesPathStr.length, bytesLength);
	        Tools.intToBytes(bytesPathStr.length, bytesLength, 0, 4);
	        out.write(CMD_SYNCH_REQ);							// 命令字
	        out.write(bytesLength);								// 路径长度
	        out.write(bytesPathStr);							// 路径
	        String timestampStr = Long.toString(timestamp);
	        out.write((byte)(timestampStr.getBytes().length));				// 时间戳长度
	        out.write(timestampStr.getBytes());								// 时间戳
	        out.flush();
	        
	        synchronized (countSynchReqObj)
	        {
	        	countSynchReq += 1;
			}
	        
	        Log.msg("Succeed to submit_synch_req("+strSynchFile+", countSynchReq="+countSynchReq+").");
        }
        catch(Exception e)
        {
        	Log.err("Failed to submit_synch_req:");
        	throw e;
        }
    }
    
    /**
     * 发起文件同步确认消息
     */
    protected synchronized void submit_synch_cnf()
    {
    	try
    	{
	        out.write(CMD_SYNCH_CNF);
	        out.flush();
	        Log.msg("Succeed to submit_synch_cnf.");
        }
        catch(Exception e)
        {
        	Log.err("Failed to submit_synch_cnf:");
        	Log.err(e);
        }
    }    

	public static final int CMD_HEART = 0x00;//心跳
	public static final int CMD_SYNCH_REQ = 0x01;//同步 
	public static final int CMD_SYNCH_RSP = 0x81;//同步响应
	public static final int CMD_SYNCH_IND = 0x02;//指示对端以下文件清单需要同步
	public static final int CMD_SYNCH_CNF = 0x82;//对端确认收到文件清单
	
	public static final int STATUS_SYNCH_IDL = 0;//空闲状态
	public static final int STATUS_SYNCH_CNF_WAIT = 1;//发出了同步指示
	public static final int STATUS_SYNCH_IND_PROC = 2;//收到了同步指示并处理
	public static final int STATUS_SYNCH_IND_WAIT = 4;//等待对端发起同步
	public static final int STATUS_SYNCH_WAIT = 5;//执行完一轮同步休息时间
    protected int statusSynch = STATUS_SYNCH_IDL;
    public void receive()
        throws Exception
    {
        //读取时间戳字段字符串的长度
        int cmd = in.read();
        if( cmd == -1 )
        {
            throw new Exception( "错误的协议解析！" );
        }

        if( cmd == CMD_HEART )//收到心跳
        {
        }
        else if( cmd == CMD_SYNCH_REQ )//收到同步文件请求
        {
        	this.handle_synch_req();
        }
        else if( cmd == CMD_SYNCH_RSP )//收到同步的文件响应（同步文件发送过来了，需要保存
        {
        	this.handle_synch_rsp();
        }
        else if( cmd == CMD_SYNCH_IND )//收到同步的文件清单
        {
        	this.handle_synch_ind();
        }
        else if( cmd == CMD_SYNCH_CNF )//收到同步的文件清单
        {
        	this.handle_synch_cnf();
        }
        else
        {
            throw new Exception( "未知的协议命令字("+cmd+")错误！" );
        }
    }

    /**
     * 启动同步线程
     */
    protected void startSynchThread()
    {
        Log.msg("Start Synch-Thread.");
        Thread thread = new Thread()
        {
            public void run()
            {
            	Random random = new Random();
            	long startTime = System.currentTimeMillis();
                Log.msg("Synch-Thread start(status="+getStatus() +").");
                int count = 0;
                while (getStatus() == CONNECTION)
                {
                    try
					{
                    	switch( statusSynch )
                    	{
                    	case STATUS_SYNCH_WAIT:
                    		
                    		if( startTime > System.currentTimeMillis() - Tools.MILLI_OF_HOUR )
                    		{
	                    		long timeout = 5000 + random.nextInt(5)*1000;
	                    		sleep(timeout);//等待下一个周期
	                    		if( statusSynch == STATUS_SYNCH_WAIT )
	                    		{
	                    			statusSynch = STATUS_SYNCH_IDL;//恢复空闲
	                    		}
                    		}
                    		else
                    		{//每小时重启一次
                                Log.war("Synch-Thread abort.");
                    			close();
                    		}
                    		break;
                    	default:
                    		count += 1;
                    		send();//尝试执行发送提交
                    		break;
                    	}
                    	if( count % 100 == 0 )
                    	{
                            Log.msg("Synch-Thread["+ip+":"+port+"] run(status="+getStatus() +", "+count+").");
                    	}
					}
					catch (Exception e)
					{
                    	Log.err(e);
                    	close();
                    	break;
					}
                }
                Log.war("Synch-Thread close.");
            }
        };
        thread.start();
    }
    /**
     * 发送命令
     */
	public void send() throws Exception
	{
		//执行同步之后在执行休息
	 	if( statusSynch == STATUS_SYNCH_IDL )
        {
			submit_synch_ind();//先发送
        }
        else
        {
            Thread.sleep(500);//处于同步中，不提交新的指示
//            Log.msg("Cannot submit synch_ind for status("+statusSynch+") is not idl." );
        }
	}
    /**
     * 发送文件同步清单（将本地存储的文件清单发送给对端，对方根据清单中的数据比较是否有更新）
     * @return int
     */
    public void submit_synch_ind() throws Exception
    {
    	HashMap<String, Long[]> mapSynchFiles = new HashMap<String, Long[]>();
        for( String synchPath : ListPath )
        {
            File path = new File(SynchPath, synchPath);
            if( path.exists() )
            {
            	boolean halfSynch = false;
            	String filters[] = null;
            	if(HalfSynchPath.contains(synchPath))	// 如果单向同步，就把目录的时间戳设置为-1，这样对端就不会把目录加入检查
            	{
            		String filterFormat = FilterSynchPath.get(synchPath);//取出过滤文件
            		if( filterFormat != null && !filterFormat.isEmpty() )
            		{
            			Log.msg("Set filters:"+ filterFormat);
            			filters = Tools.split(filterFormat, " ");
            		}
            		halfSynch = true;
            	}
            	// 如果是只同步*.*文件，那么只需要设置递归层数为1层
            	int recursive = onlyFileSynchPath.contains(synchPath)?1:Integer.MAX_VALUE;
            	getSychFileList(path, synchPath, mapSynchFiles, false, halfSynch, recursive, filters);
            }
            else
            {
            	Log.war("Failed to add synch path("+path.getPath()+") that is not exist.");
            }
        }
        if( mapSynchFiles.size() > 0 )
        {
        	this.submit_synch_ind(mapSynchFiles);//发送同步文件列表到对端
        }
        else
        {
        	//如果没有需要同步的数据
        	this.submit_synch_ind(null);//发送空的清单到对端，实现心跳
        }
    }
    
    private void copyFile(File src, File dest) throws Exception
    {
    	FileInputStream fis = null;
    	FileOutputStream fos = null;
    	try
    	{
    		if(!src.exists())
    		{
    			return;
    		}
    		long fileLen = src.length();
    		fis = new FileInputStream(src);
    		fos = new FileOutputStream(dest);
//    		byte[] fileBytes = new byte[SIZE_BUFFER];
    		
        	long writenLen = 0;
        	while(writenLen < fileLen)
        	{
        		long totalReadLen = (fileLen - writenLen - SIZE_BUFFER > 0?SIZE_BUFFER:fileLen - writenLen);
        		int offset = 0;
        		while(totalReadLen > offset)
            	{
            		int readLen = fis.read( fileOutBytes , offset, (int)totalReadLen - offset);
            		if(readLen == -1)
            		{
            			throw new Exception();
            		}
            		
            		offset += readLen;
            	}
        		
        		writenLen += totalReadLen;
        		fos.write(fileOutBytes, 0, (int)totalReadLen);
        	}
    	}
    	catch (Exception e) 
    	{
			Log.err(e);
			throw e;
		}
    	finally
    	{
    		if(fis != null)
    		{
    			fis.close();
    		}
    		
    		if(fos != null)
    		{
    			fos.close();
    		}
    	}
    }
}
