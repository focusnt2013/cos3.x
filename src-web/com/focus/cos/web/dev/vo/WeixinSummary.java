package com.focus.cos.web.dev.vo;

import org.json.JSONObject;

/**
 * 微信汇总摘要
 * @author focus
 *
 */
public class WeixinSummary 
{
	private JSONObject data;
	
	public WeixinSummary(JSONObject data)
	{
		this.data = data;
	}
	
	public String getWeixinno()
	{
		return this.data.has("weixinno")?this.data.getString("weixinno"):"";
	}

	public String getName()
	{
		return this.data.has("name")?this.data.getString("name"):"";
	}
	
	public String getRunurl()
	{
		return this.data.has("runurl")?this.data.getString("runurl"):"";
	}
	
	public String getSummaryurl()
	{
		return this.data.has("summaryurl")?this.data.getString("summaryurl"):"";
	}

	public String getType()
	{
		return this.data.has("type")?this.data.getString("type"):"";
	}
	
	public String getStatus()
	{
		return this.data.has("status")?this.data.getString("status"):"";
	}

	public String getAccessToken()
	{
		return this.data.has("access_token")?this.data.getString("access_token"):"";
	}
	
	public String getJsapiTicket()
	{
		return this.data.has("jsapi_ticket")?this.data.getString("jsapi_ticket"):"";
	}
	
	public String getJsapiTicketExpireDate()
	{
		return this.data.has("jsapi_ticket_expire_date")?this.data.getString("jsapi_ticket_expire_date"):"";
	}

	public String getJsapiTicketGetDate()
	{
		return this.data.has("jsapi_ticket_get_date")?this.data.getString("jsapi_ticket_get_date"):"";
	}

	public String getExpireDate()
	{
		return this.data.has("expire_date")?this.data.getString("expire_date"):"";
	}

	public String getGetDate()
	{
		return this.data.has("get_date")?this.data.getString("get_date"):"";
	}

	public int getUserscount()
	{
		return this.data.has("userscount")?this.data.getInt("userscount"):0;
	}

	public String getProgrammer()
	{
		return this.data.has("programmer")?this.data.getString("programmer"):"";
	}
}
