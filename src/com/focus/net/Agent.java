package com.focus.net;

public interface Agent
    extends Runnable
{
    /*连接*/
    public int CONNECTION = 0;
    /*断开*/
    public int DISCONNECTION = 1;
    /*未知主机*/
    public int UNKNOWN_HOST = 2;
    /*异常*/
    public int EXCEPTION = 3;

    /*建立连接*/
    public int connect();

    /*发送数据*/
    public int send( Packet out );

    /*接收数据*/
    public int receive( Packet input );

    /*关闭连接*/
    public void close();

    /*回调函数，当连接断开的时候触发*/
    public void disconnect();

    /*得到状态*/
    public int getStatus();

}
