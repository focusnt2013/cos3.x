package com.focus.cos.web.ops.vo;

import java.util.Date;

import com.focus.util.Tools;

/**
 * Description:
 * Create Date:Dec 3, 2008
 * @author Focus
 *
 * @since 1.0
 */
public class Sysalarm implements java.io.Serializable
{
	private static final long serialVersionUID = 2283206323414723395L;
	private Long alarmid;
	private String dn;           //网元的识别名,通常采用IP地址
	private String orgseverity;  //告警级别
	private String orgtype;      //告警类型
	private String probablecause;//告警原因
	private Date eventtime;      //告警发生的时间
	private Date acktime;        //告警确认时间
	private Date cleartime;      //告警清除时间
	private Integer activestatus;//活动状态 -1:实时;0:确认;1:清除;
	private String alarmTitle;   //告警标题
	private String alarmText;    //告警内容
	private String ackUser;      //确认用户
	private String ackRemark;    //确认备注
	private String module;       //告警模块
	private String id;           //告警标识
	private int frequency;
    private String responser;
    private String contact;
	private String serverkey;

	/** default constructor */
	public Sysalarm()
	{
//		this.acktime = new Date();
//		this.cleartime = new Date();
	}

	/** full constructor */
	public Sysalarm(Long alarmid, String dn, String orgseverity,
			String orgtype, String probablecause, Date eventtime,
			Date acktime, Date cleartime, Integer activestatus,
			String alarmTitle, String alarmText,String module,String id)
	{
		this.alarmid = alarmid;
		this.dn = dn;
		this.orgseverity = orgseverity;
		this.orgtype = orgtype;
		this.probablecause = probablecause;
		this.eventtime = eventtime;
		this.acktime = acktime;
		this.cleartime = cleartime;
		this.activestatus = activestatus;
		this.alarmTitle = alarmTitle;
		this.alarmText = alarmText;
		this.module = module;
		this.id = id;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}
	
	public Long getAlarmid()
	{
		return this.alarmid;
	}

	public void setAlarmid(Long alarmid)
	{
		this.alarmid = alarmid;
	}

	public String getDn()
	{
		return this.dn;
	}

	public void setDn(String dn)
	{
		this.dn = dn;
	}

	public String getOrgseverity()
	{
		return orgseverity;
	}

	public void setOrgseverity(String orgseverity)
	{
		this.orgseverity = orgseverity;
	}

	public String getOrgtype()
	{
		return orgtype;
	}

	public void setOrgtype(String orgtype)
	{
		this.orgtype = orgtype;
	}

	public String getProbablecause()
	{
		return this.probablecause;
	}

	public void setProbablecause(String probablecause)
	{
		this.probablecause = probablecause;
	}

	public Date getEventtime()
	{
		return this.eventtime;
	}
	
    public String getTime()
    {
    	if( this.eventtime != null )
    	{
    		return Tools.getFormatTime("yyyy-MM-dd HH:mm", this.eventtime.getTime());
    	}
    	return "Unknown time.";
    }

	public void setEventtime(Date eventtime)
	{
		this.eventtime = eventtime;
	}

	public Date getAcktime()
	{
		return this.acktime;
	}

	public void setAcktime(Date acktime)
	{
		this.acktime = acktime;
	}

	public Date getCleartime()
	{
		return this.cleartime;
	}

	public void setCleartime(Date cleartime)
	{
		this.cleartime = cleartime;
	}

	public Integer getActivestatus()
	{
		return this.activestatus;
	}

	public void setActivestatus(Integer activestatus)
	{
		this.activestatus = activestatus;
	}

	public String getAlarmTitle()
	{
		if( alarmTitle == null || alarmTitle.isEmpty() ) return alarmTitle;
		if( alarmTitle.indexOf('\'') != -1 )
		{
			alarmTitle = alarmTitle.replaceAll("'", "‘");
		}
		if( alarmTitle.indexOf("<br>") != -1 )
		{
			alarmTitle = alarmTitle.replaceAll("<br>", "\r\n");
		}
		return this.alarmTitle;
	}

	public void setAlarmTitle(String alarmTitle)
	{
		this.alarmTitle = alarmTitle;
	}

	public String getAlarmText()
	{
		return this.alarmText;
	}

	public void setAlarmText(String alarmText)
	{
		this.alarmText = alarmText;
	}

	public String getAckUser()
	{
		return ackUser;
	}

	public void setAckUser(String ackUser)
	{
		this.ackUser = ackUser;
	}

	public String getAckRemark()
	{
		return ackRemark;
	}

	public void setAckRemark(String ackRemark)
	{
		this.ackRemark = ackRemark;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getFrequency()
	{
		return frequency;
	}

	public void setFrequency(Integer frequency)
	{
		if( frequency != null )	this.frequency = frequency;
	}
    public String getResponser() {
		return responser;
	}

	public String getServerkey() {
		return serverkey;
	}
	public void setResponser(String responser) {
		this.responser = responser;
	}

	public void setServerkey(String serverkey) {
		this.serverkey = serverkey;
	}
	/**
	 * 扩展参数
	 */
	private String contextUrl;
	private String severityColor;
	private String severityFontColor;

	public String getSeverityFontColor() {
		return severityFontColor;
	}

	public void setSeverityFontColor(String severityFontColor) {
		this.severityFontColor = severityFontColor;
	}

	public String getSeverityColor() {
		return severityColor;
	}

	public void setSeverityColor(String severityColor) {
		this.severityColor = severityColor;
	}

	public String getContextUrl() {
		return contextUrl;
	}

	public void setContextUrl(String contextUrl) {
		this.contextUrl = contextUrl;
	}
}