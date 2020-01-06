package com.focus.cos.api;

public enum LogSeverity
{
	INFO(1), DEBUG(2), ERROR(3);
    private int value;

    private LogSeverity(int v)
    {
        this.value = v;
    }
    
    public int getValue()
    {
        return this.value;
    }
    
    public static LogSeverity get(int ind)
    {
        for(LogSeverity s :LogSeverity.values())
        {
            if(s.getValue() == ind)
            {
                return s;
            }
        }
        return null;
    }
}
