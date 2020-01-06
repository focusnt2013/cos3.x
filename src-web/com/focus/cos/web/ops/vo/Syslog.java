package com.focus.cos.web.ops.vo;

import java.util.Date;

/**
 * Description:
 * Create Date:Dec 3, 2008
 * @author Focus lau
 *
 * @since 1.0
 */
public class Syslog implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private long logid;

	private int logtype;

	private int logseverity;

	private Date logtime;

	private String logtext;
	/*分类,根据日志类型有不同含义
	 * 运行日志: 记录主机唯一标识
	 * 操作日志：特指主界面框架系统的登录用户唯一标识
	 * 用户日志: ？
	 * 安全日志：？
	 */
	private String account;
	/*分类,根据日志类型有不同含义
	 * 运行日志: 程序唯一标号
	 * 操作日志：不同模块操作（中文含义）与系统通知一致
	 * 用户日志: ？
	 * 安全日志：？
	 */
	private String category;
	/*上下文数据*/
	private String context;
	/*上下文链接*/
	private String contextlink;
	/**/

	/** default constructor */
	public Syslog()
	{
		this.logtime = new Date();
		this.logtext = "";
	}

	/** full constructor */
	public Syslog(long logid, int logtype, int logseverity,Date logtime, String logtext)
	{
		this.logid = logid;
		this.logtype = logtype;
		this.logseverity = logseverity;
		this.logtime = logtime;
		this.logtext = logtext;
	}

	public long getLogid()
	{
		return this.logid;
	}

	public void setLogid(long logid)
	{
		this.logid = logid;
	}

	public int getLogtype()
	{
		return this.logtype;
	}

	public void setLogtype(int logtype)
	{
		this.logtype = logtype;
	}

	public int getLogseverity()
	{
		return this.logseverity;
	}

	public void setLogseverity(int logseverity)
	{
		this.logseverity = logseverity;
	}

	public Date getLogtime()
	{
		return this.logtime;
	}

	public void setLogtime(Date logtime)
	{
		this.logtime = logtime;
	}

	public String getLogtext()
	{
		return this.logtext;
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