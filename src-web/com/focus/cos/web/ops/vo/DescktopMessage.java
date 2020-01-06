package com.focus.cos.web.ops.vo;

import java.util.Date;

import org.ocpsoft.prettytime.PrettyTime;

import com.focus.util.Tools;

/**
 * 桌面消息
 * @author focus
 *
 */
public class DescktopMessage
{
	private String id;
    public String getId() {
		return id;
	}

	private String account;//发消息用户
    private String username;//发消息用户名字
    private String content;//消息内容
	private String head;//头像
	private long timestamp;
	
	public DescktopMessage()
	{
		this.id = java.util.UUID.randomUUID().toString();
		this.timestamp = System.currentTimeMillis();
	}
    
    public String getPrettytime() {
		return Tools.replaceStr(PrettyTime.getInstance().format(new Date(this.timestamp)), " ", "");
//    	return Tools.getFormatTime("MM-dd HH:mm", this.timestamp);
    }

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
