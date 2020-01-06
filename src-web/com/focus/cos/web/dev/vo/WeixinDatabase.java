package com.focus.cos.web.dev.vo;

/**
 * 微信数据库配置
 * @author focus
 *
 */
public class WeixinDatabase 
{
	/*数据库类型*/
	private String type = "mongodb";
	/*数据库名称*/
	private String username = "";
	/*数据库密码*/
	private String password = "";
	/*数据库IP*/
	private String ip = "";
	/*数据库端口*/
	private int port = 27017;
	/*数据库名称*/
	private String database = "";
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
}
