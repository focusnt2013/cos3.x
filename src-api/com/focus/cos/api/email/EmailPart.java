package com.focus.cos.api.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

import com.focus.cos.api.ApiUtils;

public class EmailPart implements java.io.Serializable
{
	private static final long serialVersionUID = -7396905586840569091L;

	public static final int CT_TEXT = 0;
	public static final int CT_IMAGE = 1;
	public static final int CT_ATTACHMENT = 2;	
	// 资源标识
	private int id;
	private String cid;
	// 资源名称
	private String name;
	// 资源后缀
	private String suffix;
	// 资源类型
	private int type;
	// 文字内容
	private String text;
	// 资源路径
	private File resource;
	// 资源URL
	private String resourceUrl;
	private int width;
	private int height;
	// 其他配置参数
	private HashMap<String, Object> mapParameter = new HashMap<String, Object>();

	public EmailPart(int id)
	{
		this.id = id;
	}

	public Object getParameter(String name)
	{
		return mapParameter.get(name);
	}

	public String getParameterString(String name)
	{
		Object attr = mapParameter.get(name);
		if (attr == null)
			return "";
		return attr.toString();
	}

	public String getParameters()
	{
		Iterator<?> iterator = this.mapParameter.keySet().iterator();
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (iterator.hasNext())
		{
			if (i > 0)
			{
				sb.append('&');
			}
			String name = iterator.next().toString();
			String value = this.mapParameter.get(name).toString();
			sb.append(name);
			sb.append('=');
			sb.append(value);
			i += 1;
		}
		return sb.toString();
	}

	public void setPatameter(String name, Object value)
	{
		mapParameter.put(name, value);
	}
	
	public HashMap<String, Object> getMapParameter()
	{
		return mapParameter;
	}

	public void setMapParameter(HashMap<String, Object> mapParameter)
	{
		this.mapParameter = mapParameter;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getSuffix()
	{
		return suffix;
	}

	public void setSuffix(String suffix)
	{
		this.suffix = suffix;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public String getText()
	{
		if(text == null && getType() == CT_TEXT && getResource() != null && getResource().exists())
		{
            try
            {
                FileInputStream fis = new FileInputStream( getResource() );
                byte[] buffer = new byte[fis.available() ];
                fis.read( buffer );
                fis.close();
                return new String( buffer, "GBK" );
            }
            catch( Exception e )
            {
                return "";
            }
		}
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public File getResource()
	{
		return resource;
	}

	public void setResource(File resource)
	{
		this.resource = resource;
	}
	
	public void setResource(byte[] payload, int offset, int length)
	{
        try
        {
            OutputStream out = new FileOutputStream( resource, false );
            out.write( payload, offset, length );
            out.close();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
	}

	public void setResource(byte[] payload)
	{
        try
        {
            OutputStream out = new FileOutputStream( resource, false );
            out.write( payload );
            out.close();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
	}
	
	public long length()
	{
		if(this.resource!= null)
		{
			return this.resource.length();
		}
		else if( this.text != null )
		{
			return this.text.length();
		}
		return -1;
	}
	
	public static int typeOf(String ct)
	{
        if (ct.indexOf("gif")!=-1)
        {
        	return CT_IMAGE;
        }
        if (ct.indexOf("tif")!=-1)
        {
        	return CT_IMAGE;
        }
        if (ct.indexOf("bmp")!=-1)
        {
        	return CT_IMAGE;
        }
        if (ct.indexOf("jpg")!=-1 || ct.indexOf("jpeg")!=-1)
        {
        	return CT_IMAGE;
        }
        if (ct.indexOf("txt")!=-1)
        {
        	return CT_TEXT;
        }
        else
        	return CT_ATTACHMENT;
	}

	public void setDefaultCid()
	{
		// 由于ISAG方会按照contentid为帧进行排序，所以，这个地方顺序生产CID
		if(suffix.charAt(0)=='.')
		{
			this.cid = String.format("z%04d", id)+suffix;
		}
		else
		{
			this.cid = String.format("z%04d", id)+"."+suffix;
		}
	}
	
	public String getResourceUrl()
	{
		return resourceUrl;
	}

	public void setResourceUrl(String resourceUrl)
	{
		this.resourceUrl = resourceUrl;
	}

	public String getCid()
	{
		return cid;
	}

	public void setCid(String cid)
	{
		this.cid = cid;
	}

	 public byte[] getPayload()
	 {
        if(getType() == CT_TEXT)
        {
            try
            {
                return getText().getBytes("UTF-8");
            }
            catch (UnsupportedEncodingException ex)
            {
            }
        }
        else if(getType() == CT_IMAGE )
        {
            return ApiUtils.readAsByteArray( getResource() );
        }
        return null;
    }

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

}
