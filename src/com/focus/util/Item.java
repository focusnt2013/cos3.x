package com.focus.util;

public class Item
{
    private String name;
    private String value;

    public Item( String n, String v )
    {
        this.name = n;
        this.value = v;
    }

    public String getValue()
    {
        return value;
    }

    public String getName()
    {
        return name;
    }

    public void setValue( String value )
    {
        this.value = value;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String toString()
    {
        return this.name;
    }
}
