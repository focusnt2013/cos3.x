package com.focus.net;

public interface Agent
    extends Runnable
{
    /*����*/
    public int CONNECTION = 0;
    /*�Ͽ�*/
    public int DISCONNECTION = 1;
    /*δ֪����*/
    public int UNKNOWN_HOST = 2;
    /*�쳣*/
    public int EXCEPTION = 3;

    /*��������*/
    public int connect();

    /*��������*/
    public int send( Packet out );

    /*��������*/
    public int receive( Packet input );

    /*�ر�����*/
    public void close();

    /*�ص������������ӶϿ���ʱ�򴥷�*/
    public void disconnect();

    /*�õ�״̬*/
    public int getStatus();

}
