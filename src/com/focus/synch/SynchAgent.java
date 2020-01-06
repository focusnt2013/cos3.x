package com.focus.synch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.focus.util.Log;

/**
 * <p>Title: 数据同步代理</p>
 *
 * <p>Description: 实现通用的socket通信机制</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: FOCUS</p>
 *
 * @author Focus Lau
 * @version 1.0
 */
public abstract class SynchAgent
    implements Runnable
{
    /*连接*/
    public static final int CONNECTION = 0x80;
    /*断开*/
    public static final int DISCONNECTION = 0x81;
    /*未知主机*/
    public static final int UNKNOWN_HOST = 0x82;
    /*异常*/
    public static final int EXCEPTION = 0x83;
    /*连接状态*/
    protected int status = DISCONNECTION;
    /*最后一次异常*/
    public Exception lastException = null;
    /*地址*/
    protected String ip;
    /*端口*/
    protected int port;
    /*通讯*/
    protected Socket socket;
    /*输入流*/
    protected DataInputStream in;
    /*输出流*/
    protected DataOutputStream out;

    public SynchAgent( String ip, int port)
        throws Exception
    {
        this.ip = ip;
        this.port = port;
    }

    public SynchAgent( Socket socket )
        throws Exception
    {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        this.port = socket.getPort();
    }

    /**
     *
     * @return int
     */
    protected int getStatus()
    {
        return status;
    }

    protected void setStatus( int status )
    {
        this.status = status;
    }

    /**
     * 连接联系人
     * @return int 连接状态
     */
    public int connect()
    {
        if( getStatus() != CONNECTION )
        {
            try
            {
                if( socket == null )
                {
                    socket = new Socket( ip, port );
                    in = new DataInputStream( socket.getInputStream() );
                    out = new DataOutputStream( socket.getOutputStream() );
                    Log.msg( "Connect to(" + ip + ":" + port + ")." );
                }
                else
                {
                    in = new DataInputStream( socket.getInputStream() );
                    out = new DataOutputStream( socket.getOutputStream() );
                    Log.msg( "Connect from(" + ip + ":" + port + ")." );
                }
                Thread thread = new Thread( this );
                thread.start();
                setStatus( CONNECTION );
            }
            catch( UnknownHostException e )
            {
                Log.err( "Failed to connect for " + e );
                setStatus( UNKNOWN_HOST );
            }
            catch( IOException e )
            {
                Log.err( "Failed to connect for " + e );
                setStatus( DISCONNECTION );
            }
            catch( Exception e )
            {
                Log.err( "Failed to connect for " + e );
                setStatus( DISCONNECTION );
            }
        }
        return this.getStatus();
    }

    /**
     * 管理代理
     */
    public void close()
    {
        Log.war( "Close agent." );
        if( this.socket != null )
        {
            this.setStatus( DISCONNECTION );
            try
            {
                this.out.close();
                this.in.close();
                this.socket.close();
            }
            catch( Exception e )
            {
                Log.err( "Failed to close socket for " + e );
            }
            this.socket = null;
        }
        else
        {
            Log.err( "The socket is null." );
        }
    }

    public void run()
    {
        Log.msg( "Succeed to connect." );
        while( status == CONNECTION )
        {
            if( socket == null )
            {
                try
                {
                    Thread.sleep( 100 );
                }
                catch( InterruptedException e )
                {
                }
                continue ;
            }

            //接收命令
            try
            {
                //通过回调函数处理
                this.receive();
            }
            catch( SocketException e )
            {
                Log.err( "Failed to receive for net(" + e + ")." );
                lastException = e;
                this.setStatus( DISCONNECTION );
            }
            catch( IOException e )
            {
                Log.err( "Failed to receive for net(" + e + ")." );
                lastException = e;
                this.setStatus( DISCONNECTION );
            }
            catch( Exception e )
            {
                Log.err( e );
                lastException = e;
                this.setStatus( DISCONNECTION );
            }
        }

        synchronized( this )
        {
            if( this.socket != null )
            {
                try
                {
                    this.out.close();
                    this.in.close();
                    this.socket.close();
                }
                catch( IOException e )
                {
                    Log.err( "Failed to close socket for " + e );
                }
            }
            socket = null;
            this.notifyAll();
        }
        Log.msg( "The thread of receive quite." );
        //调用回调函数通知连接已经断开
        this.disconnect();
    }
    public void read(byte[] b, int off, int len) throws Exception
    {
        while(len>0)
        {
        	int readLen = in.read(b, off,len);
        	if(readLen == -1)
        	{
        		throw new Exception("Failed to read socket.");
        	}
        	off+= readLen;
        	len-=readLen;
        }
    }
    /**
     * 端口链接
     */
    public abstract void disconnect();
    /**
     * 接收消息数据
     * @return
     * @throws Exception
     */
    public abstract void receive() throws Exception;
    /**
     * 发送消息数据
     * @return int
     */
    public abstract void send() throws Exception;
    
    public String toString()
    {
        return ip+":"+port;
    }
}
