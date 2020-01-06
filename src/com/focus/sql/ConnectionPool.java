/**
 * This inner class represents a connection pool. It creates new
 * connections on demand, up to a max number if specified.
 * It also makes sure a connection is still open before it is
 * returned to a client.
 */
package com.focus.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConnectionPool
    extends LinkedList<Object> implements Runnable
{
	private static final long serialVersionUID = -6731220206116931046L;
	private static final Log log = LogFactory.getLog(ConnectionPool.class);
    static final long INTERVAL = 30000; //监控线程的监控间隔
    private static ConnectionPool handle = null;
    public int checkedOut = 0;
    private static String Driver;
    private static String Password;
    private static String URL;
    private static String User;

    public static ConnectionPool getInstance()
    {
        return handle;
    }

    public static boolean isConnect()
    {
        return handle != null;
    }

    public static void connect() throws Exception
    {
        if(isConnect())
        {
            return;
        }

        handle = new ConnectionPool(Driver, URL, User, Password);
    }

    public static void connect(String serverHost,
                               String dbType,
                               String dbName,
                               String dbDriver,
                               String dbUser,
                               String dbPassword) throws Exception
    {
    	ConnectionPool.connect(serverHost, 0, dbType, dbName, dbDriver, dbUser, dbPassword);
    }
    public static void connect(String serverHost,
    						   int port,
                               String dbType,
                               String dbName,
                               String dbDriver,
                               String dbUser,
                               String dbPassword) throws Exception
    {
        if(isConnect())
        {
            return;
        }

        String dbUrl = null; //"jdbc:" + dbType + "://" + serverHost + "/" + dbName;
//        jdbc:oracle:thin:@localhost:1521:my_db
        if("oracle".equalsIgnoreCase(dbType))
        {
        	port = port==0?1521:port;
            dbUrl = "jdbc:oracle:thin:@" + serverHost + ":"+port+":" + dbName;
        }
        else if("mysql".equalsIgnoreCase(dbType))
        {
        	port = port==0?3306:port;
            dbUrl = "jdbc:mysql://" + serverHost + ":"+port+"/" + dbName;
        }
        else if("sybase".equalsIgnoreCase(dbType))
        {
        	port = port==0?4100:port;
            dbUrl = "jdbc:sybase:Tds:" + serverHost + ":"+port+"/" + dbName;
        }
        else
        {
            dbUrl = "jdbc:" + dbType + "://" + serverHost + "/" + dbName;
        }
        handle = new ConnectionPool(dbDriver, dbUrl, dbUser, dbPassword);
    }
    
    public static void connect(String dbUrl,
            String dbDriver,
            String dbUser,
            String dbPassword) throws Exception
	{
		if(isConnect())
		{
			return;
		}
		
		handle = new ConnectionPool(dbDriver, dbUrl, dbUser, dbPassword);
	}

    public static void disconnect()
    {
        if(isConnect())
        {
        	handle.close();
        }
    }
    /**
     * Creates new connection pool.
     *
     * @param name The pool name
     * @param URL The JDBC URL for the database
     * @param user The database user, or null
     * @param password The database user password, or null
     * @param maxConn The maximal number of connections, or 0
     *   for no limit
     */
    protected ConnectionPool(
        String driver,
        String url,
        String user,
        String password) throws Exception
    {
        Driver = driver;
        URL = url;
        User = user;
        Password = password;
        Class.forName(Driver).newInstance();
        Connection c = this.getConnection();
        if(c == null)
        {
            return;// throw new Exception("Failed to connect("+URL+", "+User+", "+Password+")");
        }
        this.freeConnection(c);
        Thread thread = new Thread(this);
        thread.start();
        handle = this;
    }

    private synchronized void close()
    {
        this.isRunning = false;
        this.notify();
    }

    /**
     * Checks in a connection to the pool. Notify other Threads that
     * may be waiting for a connection.
     *
     * @param con The connection to check in
     */
    public synchronized void freeConnection(Connection con)
    {
    	if( con != null ) push(con);
        checkedOut -= 1;
//        log.debug("Free connection(size="+size()+",checkedOut="+checkedOut+").");
        notifyAll();
    }

    /**
     * Checks out a connection from the pool. If no free connection
     * is available, a new connection is created unless the max
     * number of connections has been reached. If a free connection
     * has been closed by the database, it's removed from the pool
     * and this method is called again recursively.
     */
    public synchronized Connection getConnection()
    {
        Connection dbconn = null;
//        System.out.println("Get connection(size="+size()+",checkedOut="+checkedOut+").");
        if(size() > 0)
        {
            dbconn = (Connection) poll();
        }
        else
        {
            int capacity = 10;
            if(capacity == 0 || checkedOut < capacity)
            {
                dbconn = newConnection();
            }
            else
            {
            	log.error("Cannot get connect for pool(size="+size()+",checkedOut="+checkedOut+").");
            }
        }

        if(dbconn != null)
        {
            checkedOut += 1;
        }
        return dbconn;
    }

    /**
     * Checks out a connection from the pool. If no free connection
     * is available, a new connection is created unless the max
     * number of connections has been reached. If a free connection
     * has been closed by the database, it's removed from the pool
     * and this method is called again recursively.
     * <P>
     * If no connection is available and the max number has been
     * reached, this method waits the specified time for one to be
     * checked in.
     *
     * @param timeout The timeout value in milliseconds
     */
    public Connection getConnection(long timeout)
    {
        long startTime = System.currentTimeMillis();
        Connection dbconn = null;
        while( (dbconn = getConnection()) == null)
        {
            synchronized(this)
            {
                try
                {
                    wait(10);
                }
                catch(InterruptedException e)
                {}
            }

            if( (System.currentTimeMillis() - startTime) >= timeout)
            {
                //Log.logWarning(this, "获取数据库连接超时！");
                break;
            }
        }
        return dbconn;
    }

    /**
     * Closes all available connections.
     */
    private synchronized void release()
    {
        Connection dbconn = null;
        try
        {
            while(!isEmpty() && (dbconn = (Connection)this.poll()) != null)
            {
                dbconn.close();
                checkedOut -= 1;
            }
        }
        catch(NoSuchElementException e)
        {
        }
        catch(SQLException e)
        {
        }
    }

    /**
     * Creates a new connection, using a userid and password
     * if specified.
     */
    private Connection newConnection()
    {
        Connection con = null;
        try
        {
//            if(User == null)
//            {
//                Properties props = new Properties();
//                props.put("user", User);
//                props.put("password", Password);
//                props.put("defaultRowPrefectch", "30");
//                props.put("dufaultBatchValue", "5");
//                con = DriverManager.getConnection(URL, props);
//                con.setAutoCommit(false);
//                con.commit();
//            }
//            else
//            {
            con = DriverManager.getConnection(URL, User, Password);
//            }
        }
        catch(SQLException e)
        {
            log.error("Fail to create db-connection from \"" + URL + ", "+ User + ", " + Password + "\"", e);
            return null;
        }
        return con;
    }

    /**
     * 该线程用来检查连接池中超时的对象
     * @see java.lang.Runnable#run()
     */
    protected boolean isRunning;
    public void run()
    {
        isRunning = true;
        while(isRunning)
        {
            for(int k = 0; k < size(); k++)
            {
                Connection dbconn = getConnection();
                try
                {
                    if(dbconn.isValid(3000))
                    {
                        freeConnection(dbconn);
                    }
                    else
                    {
                    	StringBuffer sb = new StringBuffer();
                    	if( !dbconn.isClosed() )
                    	{
	                    	Properties properties = dbconn.getClientInfo();
	                    	sb.append("Found invalid connection.");
	                    	Enumeration<Object> e1 = properties.keys();
		                	while (e1.hasMoreElements()) {
		                        Object key = e1.nextElement();
		                        sb.append("\r\n\t");
		                        sb.append(key);
		                        sb.append("=");
		                        sb.append(properties.get(key));
		                    }
	                    	log.info(sb.toString());
	                        dbconn.close();
                    	}
                        checkedOut -= 1;
                    }
                }
                catch(Exception e)
                {
                    log.error("Failed to release database connection:", e);
                }
            }

            synchronized(this)
            {
                try
                {
                    wait(INTERVAL);
                }
                catch(InterruptedException e)
                {}
            }
        }
        release();
        handle = null;
    }

    public static void setPassword(String str)
    {
        Password = str;
    }

    public static void setUser(String str)
    {
        User = str;
    }

    public static void setDriver(String str)
    {
        Driver = str;
    }

    public static void setURL(String serverHost,
                              String dbType,
                              String dbName)
    {

        String dbUrl = null; //"jdbc:" + dbType + "://" + serverHost + "/" + dbName;
//        jdbc:oracle:thin:@localhost:1521:my_db
        if("oracle".equalsIgnoreCase(dbType))
        {
            dbUrl = "jdbc:oracle:thin:@" + serverHost + ":1521:" + dbName;
        }
        else if("mysql".equalsIgnoreCase(dbType))
        {
            dbUrl = "jdbc:mysql://" + serverHost + ":3306/" + dbName;
        }
        else
        {
            dbUrl = "jdbc:" + dbType + "://" + serverHost + "/" + dbName;
        }

        URL = dbUrl;
    }
}
