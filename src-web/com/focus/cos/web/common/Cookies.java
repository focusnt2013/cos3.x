package com.focus.cos.web.common;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Cookies
{
	private int maxAge;// 有效时间,单位秒
	private String path; // cookie路径
	private Cookie[] cookies = {};

	public Cookies()
	{
		maxAge = 8*3600;
		path = "/";
	}

	public Cookies(int maxAge)
	{
		this.maxAge = maxAge;
	}
	
	public void putCookie(HttpServletResponse response, String name, String value)
	{
		try
		{
			Cookie cookie = new Cookie(name, encode(value));
			cookie.setMaxAge(maxAge);
			cookie.setPath(path);
			response.addCookie(cookie);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String getCookie(HttpServletRequest request, String name)
	{
		if (cookies == null || cookies.length == 0)
		{
			cookies = request.getCookies();
		}
		String returnStr;
		returnStr = null;
		try
		{
			for (int i = 0; cookies != null && i < cookies.length; i++)
			{
				cookies[i].setPath(path);
				if (cookies[i].getName().equals(name))
				{
					cookies[i].setMaxAge(-1);
					returnStr = cookies[i].getValue().toString();
					break;
				}
			}
			return decode(returnStr);
		}
		catch (Exception e)
		{
			return decode(returnStr);
		}
	}

	public void removeCookie(HttpServletResponse response, String name)
	{
		putCookie(response, name, null);
	}

	private static String decode(String value)
	{
		String result = "";
		if (!isEmpty(value))
		{
			try
			{
				result = java.net.URLDecoder.decode(value, "GBK");
			}
			catch (UnsupportedEncodingException ex)
			{
				ex.printStackTrace();
			}
		}
		return result;
	}

	private static String encode(String value)
	{
		String result = "";
		if (!isEmpty(value))
		{
			try
			{
				result = java.net.URLEncoder.encode(value, "GBK");
			}
			catch (UnsupportedEncodingException ex)
			{
				ex.printStackTrace();
			}
		}
		return result;
	}

	private static boolean isEmpty(String value)
	{
		if (value == null || value.trim().equals(""))
			return true;
		else
			return false;
	}

	public boolean isEmpty(String value1, String value2)
	{
		if (null == value1 || null == value2 || "".equals(value1) || "".equals(value2))
			return true;
		else
			return false;
	}
	
	public void setMaxAge(int maxAge)
	{
		this.maxAge = maxAge;
	}
}
