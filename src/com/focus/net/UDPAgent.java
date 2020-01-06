package com.focus.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class UDPAgent
    implements Agent
{
    /*端口*/
    protected int port;
    /**/
    protected UDPConnection connection;
    /*连接状态*/
    protected int status = DISCONNECTION;
    /*每个包的缓冲区大小*/
    private int sizeOfPagebuf = 1024;
    /*一共初始化多少个包*/
    private int sizeOfPages = 64;
    /*异步发送*/
    private Consumer consumer;
    /*命令包的类名*/
    private String className = null;
    /*最后一次异常*/
    public Exception lastException = null;

    public UDPAgent( int port )
    {
        this.port = port;
    }

    public UDPAgent()
    {
    }

    public void run()
    {
        while( status == CONNECTION )
        {
            if( this.className == null ||
                sizeOfPagebuf == 0 ||
                sizeOfPages == 0 )
            {
                try
                {
                    Thread.sleep( 100 );
                }
                catch( InterruptedException ex )
                {
                }
                continue ;
            }

            Packet packet = null;
            try
            {
                packet = malloc();//分配包
                if( status != CONNECTION )
                {
                    break;
                }

                if( packet.receive() )
                {
                    this.receive( packet );
                }
//                System.out.println( "packet.receive()" );
            }
            catch( UnknowPacketException e )
            {
                //收到未知的包就丢弃
//                e.printStackTrace();
                lastException = e;
            }
            catch( BufferExhaustedException e )
            {
//                e.printStackTrace();
                status = EXCEPTION;
                lastException = e;
            }
            catch( IOException e )
            {
//                e.printStackTrace();
                status = DISCONNECTION;
                lastException = e;
            }
            catch( Exception e )
            {
                e.printStackTrace();
                status = DISCONNECTION;
                lastException = e;
            }
            finally
            {
                if( packet != null )
                {
                    packet.free();
                }
            }
        }

        if( this.consumer != null )
        {
            this.consumer.close();
            consumer = null;
        }
        close();
        disconnect();
    }

    public int connect()
    {
        if( connection == null )
        {
            try
            {
                DatagramSocket ds = null;
                if( this.port > 0 )
                {
                    ds = new DatagramSocket( this.port );
                }
                else
                {
                    ds = new DatagramSocket();
                    this.port = ds.getPort();
                }
                connection = new UDPConnection( ds, sizeOfPages, sizeOfPagebuf );
                //启动消费者模块
                this.consumer = new Consumer();
                this.consumer.start();
                Thread thread = new Thread( this );
                thread.start();
                status = CONNECTION;
            }
            catch( SocketException ex )
            {
                this.status = DISCONNECTION;
            }
            catch( Exception ex )
            {
                /** @todo Handle this exception */
                this.status = DISCONNECTION;
            }

        }
        return 0;
    }

    public int send( Packet out )
    {
        Packet packet = ( Packet ) out;
        if( getStatus() == CONNECTION )
        {
            consumer.post( packet );
        }
        return getStatus();
    }

    public synchronized void close()
    {
        if( this.connection != null )
        {
            this.connection.close();
            this.connection = null;
        }
    }

    public int getStatus()
    {
        return 0;
    }

    public int getPort()
    {
        return port;
    }

    /**
     * 得到发送包
     * @return TCPPacket
     */
    public Packet malloc()
        throws Exception
    {
        return Packet.malloc( this.connection, this.className );
    }
}
