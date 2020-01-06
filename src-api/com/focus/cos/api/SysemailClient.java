package com.focus.cos.api;

import org.json.JSONObject;

public class SysemailClient
{
	public static final String TAG_SNAPSHOT = "snapshot==";
	public static final String TAG_IMAGES = "images==";
	public static final String TAG_HTML = "html==";
	/**
	 * 发送系统邮箱
	 * @param from
	 * @param outbox
	 * @throws Exception
	 */
	public static void send(Sysemail outbox) throws Exception
	{
		byte[] payload = ApiUtils.doPost("api/email", outbox);
		JSONObject json = new JSONObject(new String(payload, "UTF-8"));
		if( json.has("error") )
		{
			throw new Exception(json.getString("error"));
		}
		else if( json.has("id") )
		{
			outbox.setEid(json.getLong("id"));
		}
	}
	
	/**
	 * 发送系统邮件
	 * 普通模式：普通邮件正文
	 * HTML超文本模式：邮件正文内容以'html=='作为前缀开始，邮件内容以HTML网页内容构成，系统将其转换为邮箱可识别的界面
	 * 图片模式：邮件正文内容以'images=='作为前缀开始，邮件内容是以逗号分割的一个或多个图片链接ＵＲＬ地址
	 * 快照模式：邮件正文内容以'snapshot=='作为前缀开始，邮件内容是一个条可访问的ＵＲＬ地址，邮件系统会去下载它的内容作为邮件正文。
	 * @param from 接口操作是由那个模块程序发送，与自己配置的模块程序标识一致
	 * @param to 收件人
	 * @param cc 抄送人
	 * @param subject 标题
	 * @param content 邮件正文内容，可以设置4种格式
	 * @param attachment 邮件附件，提供可下载附件ＵＲＬ地址，系统可去下载压缩后作为附件放在邮件中。
	 * @param sysid 系统标识, 表示替那个子系统发送邮件，默认为空，表示不发送
	 * @return
	 * @throws Exception
	 */
	public static Sysemail send(String to,String cc, String subject,String content,String attachment, String sysid) throws Exception
	{
		Sysemail outbox = new Sysemail();
		outbox.setMailTo(to);
		outbox.setCc(cc);
		outbox.setSubject(subject);
		outbox.setContent(content);
		outbox.setAttachments(attachment);
		outbox.setSysid(sysid);
		send(outbox);
		return outbox;
	}

	public static Sysemail send(String to,String cc, String subject,String content,String attachment) throws Exception
	{
		Sysemail outbox = new Sysemail();
		outbox.setMailTo(to);
		outbox.setCc(cc);
		outbox.setSubject(subject);
		outbox.setContent(content);
		outbox.setAttachments(attachment);
		send(outbox);
		return outbox;
	}
	
	public static Sysemail send(String to,String subject,String content,String attachment) throws Exception
	{
		Sysemail outbox = new Sysemail();
		outbox.setMailTo(to);
		outbox.setSubject(subject);
		outbox.setContent(content);
		outbox.setAttachments(attachment);
		send(outbox);
		return outbox;
	}
	
	public static Sysemail send(String to,String subject,String content) throws Exception
	{
		Sysemail outbox = new Sysemail();
		outbox.setMailTo(to);
		outbox.setSubject(subject);
		outbox.setContent(content);
		send(outbox);
		return outbox;
	}
}
