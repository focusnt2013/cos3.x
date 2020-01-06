package com.focus;

/**
 * <p>Title: </p>
 *
 * <p>Description: 实现EMA配置与测试的管理</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: FOCUS</p>
 *
 * @author Focus Lau
 * @version 1.0
 */
@Deprecated
public class Property implements java.io.Serializable
{
    private static final long serialVersionUID = 4112578634029274840L;
    private String name;
    private String value;
    private String remark;

    public Property()
    {
    }

    public Property( String name, String value, String remark )
    {
        this.setName( name );
        this.setValue( value );
        this.setRemark( remark );
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setValue( String value )
    {
        this.value = value;
    }

    public void setRemark( String remark )
    {
        this.remark = remark;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }

    public String getRemark()
    {
        return remark;
    }

    public String toString()
    {
        return value;
    }
}
