package com.focus.cos.control;

public class Command 
{
    public static final int CONTROL_RESTART = 0;//重启
    public static final int CONTROL_SUSPEND = 1;//暂停
    public static final int CONTROL_GETFILE = 2;//返回文件数据
    public static final int CONTROL_CONTROLXMLPREVIEW = 3;//主控文件预览
    public static final int CONTROL_DU = 4;//查看指定目录的磁盘使用情况??????????????
    public static final int CONTROL_LOGFILELIST = 5;//日志文件列表
    public static final int CONTROL_GETFILELIST = 6;//返回文件列表
    public static final int CONTROL_DELETEFILE = 7;//删除文件
    public static final int CONTROL_CONTROLXMLCONFIG = 8;//主控文件配置
    public static final int CONTROL_GC = 9;//查看GC日志历史数据
    public static final int CONTROL_COPYFILE = 11;//拷贝文件
    public static final int CONTROL_CLOSEALL = 12;//关闭所有服务
    public static final int CONTROL_SUSPENDALL = 13;//暂停所有服务
    public static final int CONTROL_GETMONITOR = 14;//得到服务器监控信息
    public static final int CONTROL_RESTARTALL = 15;//重启
    public static final int CONTROL_GETSERVERSMONITOR = 16;//得到服务器硬件监控信息
    public static final int CONTROL_CLEARLOGS = 17;//删除日志
    public static final int CONTROL_DECOMPRESS = 18;//解压指定文件
    public static final int CONTROL_SSH = 22;//模拟SSH控制指令
    public static final int CONTROL_DEBUG = 23;//获取子程序的输出数据
    public static final int CONTROL_UPGRADECHECKE = 24;//升级
    public static final int CONTROL_UPGRADE = 25;//升级
    public static final int CONTROL_H2 = 26;//控制h2数据库
    public static final int CONTROL_MAKEFILE = 27;//删除文件
    public static final int CONTROL_MAKEDIR = 28;//创建目录
    public static final int CONTROL_JMAP = 29;//执行JMAP操作
    public static final int CONTROL_JSTACK = 30;//执行JSTACK操作
}
