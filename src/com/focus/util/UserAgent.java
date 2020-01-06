package com.focus.util;

/**
 * 根据 user agent string 来判断出客户端的浏览器以及平台等信息
 * 
 * @author Defonds
 */
public class UserAgent
{
	private String browserType;// 浏览器类型
	private String browserVersion;// 浏览器版本
	private String platformType;// 平台类型
	private String platformSeries;// 平台系列
	private String platformVersion;// 平台版本
	private String netType;// 网络类型 WIFI NOWIFI UNKNOWN
	private boolean mobile;//是否是移动终端
	private String mobileType;//移动终端机型

	public UserAgent()
	{
	}

	public UserAgent(String browserType, String browserVersion, String platformType, String platformSeries, String platformVersion)
	{
		this.browserType = browserType;
		this.browserVersion = browserVersion;
		this.platformType = platformType;
		this.platformSeries = platformSeries;
		this.platformVersion = platformVersion;
	}

	public String getBrowserType()
	{
		return browserType;
	}

	public void setBrowserType(String browserType)
	{
		this.browserType = browserType;
	}

	public String getBrowserVersion()
	{
		return browserVersion;
	}

	public void setBrowserVersion(String browserVersion)
	{
		this.browserVersion = browserVersion;
	}

	public String getPlatformType()
	{
		return platformType;
	}

	public void setPlatformType(String platformType)
	{
		this.platformType = platformType;
	}

	public String getPlatformSeries()
	{
		return platformSeries==null?(platformVersion==null?"":platformVersion):platformSeries;
	}

	public void setPlatformSeries(String platformSeries)
	{
		this.platformSeries = platformSeries;
	}

	public String getPlatformVersion()
	{
		return platformVersion;
	}

	public void setPlatformVersion(String platformVersion)
	{
		this.platformVersion = platformVersion;
	}

	public String getNetType()
	{
		return netType;
	}

	public void setNetType(String netType)
	{
		this.netType = netType;
	}

	public boolean isMobile()
	{
		return mobile;
	}

	public void setMobile(boolean mobile)
	{
		this.mobile = mobile;
	}

	public String getMobileType()
	{
		return mobileType;
	}

	public void setMobileType(String mobileType)
	{
		this.mobileType = mobileType;
	}

}