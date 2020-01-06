package com.focus.cos.api;


public enum LogType
{
	操作日志(1), 运行日志(2), 用户日志(3), 安全日志(4);
	//Operator(1), Running(2), User(3), Secuerity(4);
    private int value;

    private LogType(int v)
    {
        this.value = v;
    }
    
    public int getValue()
    {
        return this.value;
    }
    
    public static LogType get(int ind)
    {
        for(LogType s :LogType.values())
        {
            if(s.getValue() == ind)
            {
                return s;
            }
        }
        return null;
    }
}
