package com.focus.cos.api;

public enum Sex
{
	Male("男",(byte)1), Female("女",(byte)2) , Unknown("未知",(byte)3);
	
	private String name;
    private byte value;

    private Sex(String name,byte v)
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
    
    public static Sex get(String value)
	{
		for (Sex s : Sex.values())
		{
			if (s.getStringValue().equalsIgnoreCase(value))
			{
				return s;
			}
		}
		return null;
	}
    
    public static Sex get(byte ind)
    {
        for(Sex s :Sex.values())
        {
            if(s.getValue() == ind)
            {
                return s;
            }
        }
        return null;
    }
}
