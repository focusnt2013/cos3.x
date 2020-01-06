package com.focus.cos.api;


public enum Status
{
	Enable("启用",(byte)1), Disable("禁用",(byte)2),Suspension("暂停使用",(byte)3);
	private String name;
    private byte value;

    private Status(String name,byte v)
    {
    	this.name = name;
        this.value = v;
    }
    
    public String getStringValue()
	{
		return this.name;
	}
    
    public byte getValue()
    {
        return this.value;
    }
    
    public static Status get(String value)
	{
		for (Status s : Status.values())
		{
			if (s.getStringValue().equalsIgnoreCase(value))
			{
				return s;
			}
		}
		return null;
	}
    
    public static Status get(byte ind)
    {
        for(Status s :Status.values())
        {
            if(s.getValue() == ind)
            {
                return s;
            }
        }
        return null;
    }
}
