package com.focus.cos.control;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import com.focus.util.Log;
import com.focus.util.Tools;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.OperationStatus;

/**
 * 目录扫描枪
 * @author think
 *
 */
public class DirStateScan extends Thread {
	/*正在扫描的目录*/
	private File dir;
	/*开启时间*/
	private long start_time;
	/*结束时间*/
	private long end_time;
	/*运行*/
	private boolean running;
	/*中止*/
	private boolean abort;
	/*扫描间隔*/
	private long interval;
	/*查询器*/
	private DirStateQuery query;
	/*扫描产生了多少个目录*/
	private int count;

	/**
	 * 
	 */
	public DirStateScan(String path, DirStateQuery query){
		this.query = query;
		dir = new File(path);
	}
	
	public String getPath(){
		return dir.getPath();
	}
	/**
	 * 周期
	 * @return
	 */
	public long duration(){
		if( end_time == 0 ){
			return System.currentTimeMillis() - start_time;
		}
		return end_time - start_time;
	}
	/**
	 * 开始时间
	 * @return
	 */
	public String getStartTime(){
		return Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", start_time);
	}
	/**
	 * 结束时间
	 * @return
	 */
	public String getEndTime(){
		return Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", end_time);
	}
    /**
     * 记录指定目录的占用磁盘空间
     * @param path
     * @param size
     * @return
     * @throws Exception
     */
    public boolean put( DirState dirState ) throws Exception
    {
    	String key = Tools.encodeMD5(dirState.getPath());
        DatabaseEntry entry1 = new DatabaseEntry();
        query.getKeyBinding().objectToEntry( key, entry1 );
        DatabaseEntry entry2 = new DatabaseEntry();
        query.getDataBinding().objectToEntry( dirState, entry2 );
		try
		{
//			Log.msg("Save path("+path+", "+size+") to database(status="+status+")");
	        return OperationStatus.SUCCESS == this.query.getDatabase().put( null, entry1, entry2 );
		}
		catch (Exception e)
		{
			throw e;
		}
    }
    /**/
    private StringBuilder logs = new StringBuilder();
    private File dirScanning = null;
    /**
     * 递归查询目录情况
     * @param dir
     * @throws Exception 
     */
    private void watch(File dir, DirState dirState) throws Exception{
    	dirScanning = dir;
    	File[] files = dir.listFiles();
    	if( files == null ){
    		return;
    	}
    	for(File file : files)
    	{
    		if( file.isFile() ){
    			dirState.count(1);
    			dirState.size(file.length());
    		}
    		else{
    	    	DirState subState = new DirState(file);
    	    	this.watch(file, subState);
    	    	dirState.count(subState.count());
    	    	dirState.size(subState.size());
    		}
    		if( abort ){
        		return;//如果收到中止信号就立刻返回
        	}
    	}
    	if( !abort ){
    		boolean r = this.put(dirState);
    		if( count % 1000 == 0 ){
    			logs.append("\r\n\t");
    			logs.append(dirState);
    			if( r ) logs.append(" [1]");
    			else{
    				logs.append(" [0]");
    			}
    			logs.append("\r\n\t... ...");
    		}
    		count += 1;
    	}
    	if( System.currentTimeMillis() - interval > 100 ){
    		Thread.sleep(1);
    		interval = System.currentTimeMillis();
    	}
    }
	/**
	 * 
	 */
	public void run(){
		start_time = System.currentTimeMillis();
		running = true;
		interval = System.currentTimeMillis();
		logs.append(String.format("[DirStateScan][%s] Scan %s.", this.getId(), dir.getPath()));
		DirState state = new DirState(dir);
		try{
			this.watch(dir, state);
			end_time = System.currentTimeMillis();
			logs.append(String.format("\r\n  ==Close the scan(%s) of %s begin at %s duration %s, the abort is %s, %s",
				count, dir.getPath(), getStartTime(), duration(), abort, state.toString()));
		}
		catch(Exception e){
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			ps.close();
			logs.append("\r\n ==Failed to scan for "+baos.toString());
		}
		finally{
			Log.msg(logs.toString());
		}
		running = false;
		query.notify(state);
	}
	
	public void abort(){
		abort = true;
		Log.print("[DirStateScan][%s]Abort the scan of %s begin at %s duration %s.", this.getId(), dir.getPath(), getStartTime(), duration());
	}

	public boolean isRunning() {
		return running;
	}
	
	/**
	 * 指定路径是在当前扫描范围内
	 * @param path
	 * @return
	 */
	public boolean contain(String path){
		File dir0 = new File(path);
		return dir0.getPath().startsWith(dir.getPath());
	}
	
	/**
	 * 指定目录是不是扫描路径的父目录
	 * @param path
	 * @return
	 */
	public boolean parent(String path){
		File dir0 = new File(path);
		return this.dir.getPath().startsWith(dir0.getPath());
	}

	public int getCount() {
		return count;
	}

	public boolean isAbort() {
		return abort;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getPath());
		sb.append(" running:");
		sb.append(isRunning());
		sb.append(" abort:");
		sb.append(isAbort());
		sb.append(' ');
		sb.append(getStartTime());
		sb.append(' ');
		sb.append(duration());
		sb.append("ms ");
		sb.append(getCount());
		sb.append(" scanning:");
		sb.append(this.dirScanning);
		return sb.toString();
	}
}
