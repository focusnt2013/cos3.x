package com.focus.cos.control;

import com.focus.util.Tools;


/**
 * 帮助开发人员跟踪模块异常，如果发现模块异常第一时间发送邮件通知开发人员
 * 下次再发送时间间隔1小时
 * 从control.xml读取模块负责人、以及日志路径
 * @author focus
 *
 */
public class DeveloperTracker implements java.io.Serializable
{
	private static final long serialVersionUID = 2762156067200823973L;
	/*跟踪模块*/
	private transient Module module;
	/*上次通知时间*/
	private long lastNotifyTime;
	/*异常计数*/
	private int exceptionCounter;
	/*异常摘要*/
	private String exceptionTips;
	/*今日异常计数*/
	private int exceptionCounterToday;
	
	public DeveloperTracker(Module module)
	{
		this.module = module;
	}
	
	/**
	 * 执行跟踪
	 */
	public void execute()
	{
	}
	
	/**
	 * 向程序员发送邮件
	private void sendAlarmEmail()
	{
		String subject = "[127.0.0.1]"+"+module.getName()+"+"程序("+module.getId()+")异常 "+exceptionTips;
		StringBuffer text = new StringBuffer();
		text.append(module.getProgrammer()+" 你好:\r\n");
		text.append("\t\t我发现部署在[1]的程序["+module.getName()+"]["+module.getId()+"]从");
		text.append(Tools.getFormatTime("yyyy-MM-dd HH:mm", lastNotifyTime));
		text.append("以来，出现"+exceptionCounter+"次异常，今天累计异常次数是"+exceptionCounterToday+"次，频繁的异常会导致系统故障。\r\n");
		text.append("\t\t附件是该程序的日志，请通过它分析定位异常的原因，尽快解决该问题。\r\n\r\n");
		text.append("谢谢你的支持，祝你工作愉快。\r\n");
		text.append("\r\n【COS程序助手】");
		lastNotifyTime = System.currentTimeMillis();
		text.append(Tools.getFormatTime("yyyy-MM-dd HH:mm", lastNotifyTime));
		String cosWeb = System.getProperty("cos.web");
		if( cosWeb != null && cosWeb.startsWith("http://") )
		{
			String emailWs = cosWeb+"services/EmailService";
		}
	}
	 */
}
