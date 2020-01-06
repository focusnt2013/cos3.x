package com.focus.cos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.h2.tools.CreateCluster;
import org.h2.tools.Server;

import com.focus.util.IOHelper;

public class H2 extends Thread
{
	private Server server;
	public H2(Server server)
	{
		this.server = server;
	}
	
	public static void main(String[] args)
	{
		try
		{
			CosServer.uniqueProcess("h2");
			String port0 = args.length>0?args[0]:"9092";
			Server server = Server.createTcpServer(new String[] { 
				"-tcpAllowOthers",
				"-tcpPort",
				port0
			});
			server.start();
			if( args.length > 1 )
			{
				String port1 = args[1];
		    	String urlSource = "jdbc:h2:tcp://localhost:"+port0+"/./h2/cos";
		    	String urlTarget = "jdbc:h2:tcp://127.0.0.1:"+port1+"/./h2/cos";
		    	String user = "sa";
		    	String password = "";
		    	CreateCluster h = new CreateCluster();
				System.out.println("["+port0+"] Startup h2 cluster("+urlSource+", "+urlTarget+").");
		    	h.execute(urlSource, urlTarget, user, password, "localhost:"+port0+",127.0.0.1:"+port1);
			}
			System.out.println("["+port0+"] "+server.getStatus());
			System.out.println("["+port0+"] Startup h2.");
			H2 h2 = new H2(server);
			h2.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 接受指令退出
	 */
	public void run()
	{
		DatagramSocket socket = null;
		byte[] buf = new byte[64];
		try 
		{
			DatagramPacket p = new DatagramPacket(buf, buf.length);
			socket = new DatagramSocket(server.getPort());
			socket.setSoTimeout(0);
			System.out.println("Wait for close.");
			socket.receive(p);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally
		{
			System.out.println("Qute.");
			if( socket != null ) socket.close();
			server.stop();
		}
	}
	/**
	 * 启动资子进程
	 * @param port0
	 * @param port1
	 * @param classpath
	 * @param h2jarpath
	 * @return
	 */
	public static Process startup(int port0, int port1, String classpath, String h2jarpath)
	{
		Process process = null;
		try
		{
    		int i = 0;
			String separator_file = System.getProperty("file.separator", "/");
        	String separator_path = System.getProperty("path.separator", ";");
        	String jdk = System.getProperty("java.home");
        	if( !jdk.endsWith("bin"+separator_file+"java") )
        		jdk += ""+separator_file+"bin"+separator_file+"java";
        	if( classpath == null )
        	{
	        	File classesDir = new File("build/classes");
	        	classpath = classesDir.getAbsolutePath().replace("\\", "/");
        	}
        	if( h2jarpath == null )
        	{
	        	File file = new File("WebContent/WEB-INF/lib/h2.jar");
	        	h2jarpath = file.getAbsolutePath().replace("\\", "/");
        	}

        	StringBuffer cp = new StringBuffer();
        	cp.append(classpath);
        	cp.append(separator_path);
        	cp.append(h2jarpath);
        	ArrayList<String> startupCommands = new ArrayList<String>();
        	startupCommands.add( i++, jdk );
        	startupCommands.add( i++,  "-Xms16m" );
        	startupCommands.add( i++,  "-Xmx64m" );
        	startupCommands.add( i++,  "-XX:PermSize=8M" );
        	startupCommands.add( i++,  "-XX:MaxPermSize=32m" );
        	startupCommands.add( "-cp" );
        	startupCommands.add( cp.toString() );
        	startupCommands.add( "-Duser.dir="+System.getProperty("user.dir") );
        	startupCommands.add("com.focus.cos.H2");
        	startupCommands.add(String.valueOf(port0));
        	if( port1 > 0 ) startupCommands.add(String.valueOf(port1));
        	StringBuffer sb = new StringBuffer();
        	for(String command : startupCommands)
        	{
        		sb.append("\r\n\t");
        		sb.append(command);
        	}
        	ProcessBuilder pb = new ProcessBuilder( startupCommands );
            //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
            pb.redirectErrorStream( true );
            process = pb.start();
//            System.out.println("[H2:"+port0+"-"+port1+"] Quite: "+process.waitFor());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return process;
	}
	/**
	 * 启动非集群模式数据库
	 */
	public static void initialize(File h2dir)
	{
        InputStream is = null;
		Statement statement = null;
		Connection connection = null;
		try
		{
			FileUtils.deleteDirectory(h2dir);
	        String dbDriver = System.getProperty("cos.jdbc.driver");
	        String dbUrl = System.getProperty("cos.jdbc.url");
	        String dbUser = System.getProperty("cos.jdbc.user");
	        String dbPassword = System.getProperty("cos.jdbc.password");
	        Class.forName(dbDriver);
            Class.forName(dbDriver).newInstance();
            connection = DriverManager.getConnection(dbUrl ,dbUser, dbPassword);
	        statement = connection.createStatement();
	    	is = H2.class.getResourceAsStream("/h2.sql");
			String sqlscript = new String(IOHelper.readAsByteArray(is), "UTF-8");
			String sqls[] = sqlscript.split(";");
			for( String sql : sqls)
			{
				statement.executeUpdate(sql.trim());
//				System.out.println(sql.trim());
			}
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {
				statement.close();
			} catch (SQLException e) {
			}
			try {
				connection.close();
			} catch (SQLException e1) {
			}
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}
}
