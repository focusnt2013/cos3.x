package com.focus.cos.api;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;

/**
 * 告警
 * @author focus
 *
 */
public class Sysalarm implements Serializable
{
	private static final long serialVersionUID = 9098721193159936438L;
    /*唯一标识*/
    private long alarmid;
    /*告警针对的模块模块*/
    private String module;
    /*网元的识别名，通常采用IP地址*/
    private String dn;
    /*告警级别，设备上报告警消息中的告警级别。分为严重告警、主要告警、次要告警、警告告警和不确定告警五种级别。*/
    private String severity;
    /*告警类型，设备上报告警消息中的告警类型。分为通信告警、环境告警、设备告警、处理错误告警、业务告警、服务质量告警等。*/
    private String type;
    /*告警代码*/
    private int causeCode;
    /*告警原因*/
	private String cause;
    /*告警发生时间，显示格式：YYYMMDDMMSS */
    private java.util.Date eventTime;
    /*告警确认时间 仅在告警已确认时，该项不为空。显示格式：YYYMMDDMMSS*/
    private java.util.Date ackTime;
    /*告警时间 仅在告警已清除时，该项不为空。格式：YYYMMDDMMSS*/
    private java.util.Date clearTime;
    /*活动状态，表示告警是否被清除还是处于活跃状态{0, 1}*/
    private int activeStatus = -1;
    /*告警标题*/
    private String title;
    /*告警内容 必需告警精确定位信息，能够通过单条告警具体明确定位到发生故障的链路。*/
    private String text;
    /*告警确认用户*/
    private String ackUser;
    /*告警确认备注信息*/
    private String ackRemark;
    /*告警标识*/
    private String id;
    /*告警*/
    private int frequnce;
    /*告警处理负责人*/
    private String responser;
    /*告警负责人联系方式*/
    private String contact;
    /*集群伺服器，告警来自哪个集群伺服器*/
    private String serverkey;

	public Sysalarm()
    {
//        try
//        {
//            dn = ApiUtils.getLocalIP();
//        }
//        catch (Exception e)
//        {
//            this.dn = "Unknown";
//        }
        this.eventTime = new Date(System.currentTimeMillis());
    }

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}
	
    public void setSysid( String sysid)
    {
        this.module = sysid;
    }

    public void setActiveStatus(int activeStatus )
    {
        this.activeStatus = activeStatus;
    }

    public void setTitle( String title)
    {

        this.title = title;
    }
    
    public void setException(String info, Exception e)
    {
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(out);
			e.printStackTrace(ps);
	        this.setText(info+"\r\n"+out.toString());
			ps.close();
		}
		catch(Exception e1)
		{
		}
    }

    public void setText( String text)
    {
        this.text = text;
    }

    public void setAckTime(java.util.Date ackTime)
    {
        this.ackTime = ackTime;
    }

    public String getSysid()
    {
        return module;
    }

    public String getDn()
    {
        return dn;
    }

    public java.util.Date getEventTime()
    {
        return eventTime;
    }

    public java.util.Date getAckTime()
    {
        return ackTime;
    }

    public java.util.Date getClearTime()
    {
        return clearTime!=null?clearTime:(new Date(0));
    }

    public int getActiveStatus()
    {
        return activeStatus;
    }

    public String getTitle()
    {
        return title;
    }

    public String getText()
    {
        return text;
    }

    public int getCauseCode()
    {
            return causeCode;
    }

    public void setCauseCode(int causeCode)
    {
        this.causeCode = causeCode;
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

	public String getSeverity()
	{
		return severity;
	}

	public void setSeverity(String severity)
	{
		this.severity = severity;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setDn(String dn)
	{
		this.dn = dn;
	}

	public void setEventTime(java.util.Date eventTime) {
		this.eventTime = eventTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCause()
	{
		return cause;
	}

	public void setCause(String cause)
	{
		this.cause = cause;
	}

	public int getFrequnce()
	{
		return frequnce;
	}

	public void setFrequnce(int frequnce)
	{
		this.frequnce = frequnce;
	}

	public long getAlarmid()
	{
		return alarmid;
	}

	public void setAlarmid(long alarmid)
	{
		this.alarmid = alarmid;
	}

	public void setClearTime(java.util.Date clearTime)
	{
		this.clearTime = clearTime;
	}

    public String getResponser() {
		return responser;
	}

	public void setResponser(String responser) {
		this.responser = responser;
	}

	public String getServerkey() {
		return serverkey;
	}

	public void setServerkey(String serverkey) {
		this.serverkey = serverkey;
	}
}
