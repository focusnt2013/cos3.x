package com.focus.cos.api;

import java.io.Serializable;
import java.util.Date;

public class Syslog implements Serializable
{
	private static final long serialVersionUID = -7904875829525227970L;
	/*日志ID*/
	private Long logid;
	/*日志类型*/
	private Integer logtype; //操作日志(1), 运行日志(2), 用户日志(3), 安全日志(4);
	/*日志级别*/
	private Integer logseverity;//INFO(1), DEBUG(2), ERROR(3);
	/*日志时间*/
	private Date logtime;
	/*日志描述内容*/
	private String logtext;
	/*分类,根据日志类型有不同含义
	 * 运行日志: 记录主机唯一标识
	 * 操作日志：特指主界面框架系统的登录用户唯一标识
	 * 用户日志: 记录用户的cookie
	 * 安全日志：特指主界面框架系统的登录用户唯一标识
	 */
	private String account;
	/*分类,根据日志类型有不同含义
	 * 运行日志: 就记录所属模块子系统
	 * 操作日志：不同模块操作（中文含义）与系统通知一致
	 * 用户日志: 记录的是action类别user!manager
	 * 安全日志：类别
	 */
	private String category;
	/*上下文数据*/
	private String context;
	/*上下文链接*/
	private String contextlink;
	
	/** default constructor */
	public Syslog()
	{
		this.logtime = new Date();
		this.logtext = "";
	}

	public Long getLogid()
	{
		return logid;
	}

	public void setLogid(Long logid)
	{
		this.logid = logid;
	}

	public Integer getLogtype()
	{
		return logtype;
	}

	public void setLogtype(Integer logtype)
	{
		this.logtype = logtype;
	}

	public Integer getLogseverity()
	{
		return logseverity;
	}

	public void setLogseverity(Integer logseverity)
	{
		this.logseverity = logseverity;
	}

	public Date getLogtime()
	{
		return logtime;
	}

	public void setLogtime(Date logtime)
	{
		this.logtime = logtime;
	}

	public String getLogtext()
	{
		return logtext;
	}

	public void setLogtext(String logtext)
	{
		this.logtext = logtext;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getContextlink() {
		return contextlink;
	}

	public void setContextlink(String contextlink) {
		this.contextlink = contextlink;
	}
}
