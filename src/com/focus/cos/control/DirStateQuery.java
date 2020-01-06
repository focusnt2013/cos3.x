package com.focus.cos.control;

import java.io.File;
import java.util.ArrayList;
import java.util.SortedMap;

import com.focus.util.ConfigUtil;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;
import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
/**
 * 目录状态查询
 * 内存数据库，用于存储结构化数据
 * 使用的是Berkeley DB（JE）技术提供轻量级数据库支持
 * 数据将被保存到硬盘上持久化存储
 * 该类将这一系列过程进行封装，提供用户简单的接口就像操作Map一样
 * @author Focus
 *
 */
public abstract class DirStateQuery
{
	private Environment env = null;
    /*目录情况数据库*/
    private Database database = null;
    /*不知是什么，序列化对象数据库存储需要*/
    private ClassCatalog catalog;
    /*binding键*/
	private EntryBinding<String> keyBinding = null;
	/*binding值*/
	private EntryBinding<DirState> dataBinding = null;
    /*存储的MAP*/
    private SortedMap<String, DirState> storedMap = null;// 通过该该对象接口实现对数据添加获取
    //数据库配置
    private DatabaseConfig dbConfig = null;
    /*数据库路径*/
    private File path = null;
	/*运行*/
	private boolean running;
    
    /**
     * 目录状态查询
     */
    public DirStateQuery()
    {
        this.path = new File(ConfigUtil.getWorkPath(), "data/dirstate");
        do{
        	if( !path.exists() )
        	{
        		this.path.mkdirs();
        	}
        	try{
        		reset();
        		Log.msg("Succeed to initilaize the memory of DirStateQuery from "+this.path.getPath());
        	}
        	catch(Exception e){
        		Log.err("Failed to initialize the memory of DirStateQuery from "+this.path, e);
        		close();
        		IOHelper.deleteDir(this.path);
        	}
        }
        while(!this.path.exists());
    }    
    
    /**
     * 得到目录占用了多少磁盘空间
     * @param path
     * @return
     */
    public DirState get( String path )
    {
    	if( storedMap == null ) return null;
    	File dir = new File(path);
        return this.storedMap.get( Tools.encodeMD5(dir.getPath()) );
    }
    public DirState get( File dir )
    {
    	if( storedMap == null ) return null;
        DirState state = this.storedMap.get( Tools.encodeMD5(dir.getPath()) );
        return state;
    }
    /**
     * 创建扫描
     * @param path
     * @return 如果已经有覆盖需求的扫描器在工作就返回false
     */
    private ArrayList<DirStateScan> scanning = new ArrayList<DirStateScan>();
    public synchronized boolean createScan(String path){
    	if( path.equals("/") ){
    		Log.print("[DirStateQuery] Skip /.", path);
    		return false;
    	}
    	boolean b = true;
    	DirStateScan e = null;
    	for( int i = 0; i < scanning.size(); i++ ){
    		e = scanning.get(i);
    		if( !e.isRunning() ){
    			scanning.remove(i);
    			i -= 1;
    			continue;
    		}
    		if( e.contain(path) ){//"/home".startsWith("/home/efida");
    			b = false;
    			break;
    		}
    		if( e.parent(path) ){// "/home/efida".startsWith("/home");
    			//请求的目录包含了上级目录
    			e.abort();//中止运行
    		}
    	}
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < scanning.size(); i++ ){
			e = scanning.get(i);
			sb.append("\r\n\t");
			sb.append(e.toString());
		}
    	if( b ){
    		e = new DirStateScan(path, this);
    		e.start();
    		scanning.add(e);
    		Log.print("[DirStateQuery]Succeed to create the scan of '%s', totoal is %s scans.%s", path, scanning.size(), sb.toString());
    	}
    	else{
			Log.print("[DirStateQuery][%s]Not need to create the scan of %s for contain %s.%s", e.getId(), path, e.getPath(), sb.toString());
    	}
    	return b;
    }
    
    /**
     * 当扫描结束通知
     * @param state
     */
    public abstract void notify(DirState state);
    
    /**
     * 删除一个键值对
     * @param key
     */
    public DirState remove(String path)
    {
    	return this.storedMap.remove(Tools.encodeMD5(path));
    }
    
    /**
     * 重新设置
     */
    public void reset()
    	throws Exception
    {
    	this.close();

        // environment is transactional
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate( true ); // 如果不存在指定目录的数据库那么创建一个
        envConfig.setTransactional( true ); // 设置支持事务
        this.env = new Environment( path, envConfig );
        this.dbConfig = new DatabaseConfig();
        this.dbConfig.setTransactional( true );
        this.dbConfig.setAllowCreate( true );

        // catalog is needed for serial bindings (java serialization)
        Database catalogDb = env.openDatabase( null, "catalog", dbConfig );
        this.catalog = new StoredClassCatalog( catalogDb );

        // use plain old byte arrays to store the key
        keyBinding = new StringBinding();
        // use String serial binding for data entries
        dataBinding = new SerialBinding<DirState>(catalog, DirState.class);

        database = env.openDatabase( null, "dirstates", dbConfig );
        storedMap = new StoredSortedMap<String, DirState>( database, keyBinding, dataBinding, true );
    }
    /**
     * 关闭Frontier
     */
    public void close()
    {
    	if( running ){
    		running = false;
        	Log.msg("[DirStateQuery] close it by notify.");
    		synchronized(this){
    			this.notify();//通知结束线程
    		}
    	}
        //IOFactory.print( "public synchronized void close()" );
//        Log.msg( this, "Close database." );
        if( database != null )
	    {
	        try
	        {
	            this.database.close();
	        }
	        catch( Exception e )
	        {
	            Log.err( "Failed to close the database of dir_state_query" , e );
	        }
        }

        if( catalog != null )
        {
	        try
	        {
	            this.catalog.close();
	        }
	        catch( Exception e )
	        {
	            Log.err( "Failed to close the catalog of dir_state_query", e );
	        }
        }

        if( env != null )
        {
	        try
	        {
	            this.env.cleanLog();
	            this.env.close();
	        }
	        catch( Exception e )
	        {
	            Log.err( "Failed to close the environment of dir_state_query", e);
	        }
        }
        this.database = null;
        this.catalog = null;
        this.env = null;
        this.dbConfig = null;
        this.storedMap = null;
        this.keyBinding = null;
        this.dataBinding = null;
    }
    
    /**
     * 扫描结果集合，判断数据是否存在
    public void run()
    {
    	running = true;
    	Log.msg("[DirStateQuery] The check start.");
    	while(running)
    	{
    		long ts = System.currentTimeMillis();
        	List<DirState> list = new ArrayList<DirState>();
        	Collection<DirState> valueSet = this.storedMap.
        			values();
        	list.addAll(valueSet);
        	ts = System.currentTimeMillis() - ts;
        	Log.print("[DirStateQuery] Found %s states need to check from %sms.", list.size(), ts);
        	StringBuilder sb = new StringBuilder();
        	int count = 0;
        	for(DirState e : list){
        		File dir = new File(e.getPath());
        		if( dir.exists() ){
        			continue;
        		}
        		sb.append("\r\n\t");
        		sb.append(e.toString());
        		this.remove(e.getPath());
        		count += 1;
        	}
        	Log.print("[DirStateQuery] Found %s dirs have been deleted.%s", count, sb.toString());
        	list.clear();
        	synchronized(this){
        		if( running ){
        			try {
        	        	Log.msg("[DirStateQuery] Wait next day to check.");
        				this.wait(Tools.MILLI_OF_DAY);//一天检查一次
        			} catch (InterruptedException e) {
        				e.printStackTrace();
        			}
        		}
        	}
    	}
    	running = false;
    	Log.msg("[DirStateQuery] closed.");
    }
     */
    
    protected Database getDatabase() {
		return database;
	}

    protected EntryBinding<String> getKeyBinding() {
		return keyBinding;
	}

    protected EntryBinding<DirState> getDataBinding() {
		return dataBinding;
	}
}
