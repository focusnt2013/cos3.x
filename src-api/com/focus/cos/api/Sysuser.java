package com.focus.cos.api;

import java.io.Serializable;

public class Sysuser implements Serializable
{
	private static final long serialVersionUID = 3391525418250654994L;
	private Integer id;
	private Byte roleid;
	private String username;
	private String password;
	private String realname;
	private String email;
	private Byte status;
	private Byte sex;
	private String creator;
	/*扩展属性的JSON数据结构*/
	private String props;

	public String getProps() {
		return props;
	}
	public void setProps(String props) {
		this.props = props;
	}
	public Integer getId()
	{
		return id;
	}
	public void setId(Integer id)
	{
		this.id = id;
	}
	public Byte getRoleid()
	{
		return roleid;
	}
	public void setRoleid(Byte roleid)
	{
		this.roleid = roleid;
	}
	public String getUsername()
	{
		return username;
	}
	public void setUsername(String username)
	{
		this.username = username;
	}
	public String getPassword()
	{
		return password;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	public String getRealname()
	{
		return realname;
	}
	public void setRealname(String realname)
	{
		this.realname = realname;
	}
	public String getEmail()
	{
		return email;
	}
	public void setEmail(String email)
	{
		this.email = email;
	}
	public Byte getStatus()
	{
		return status;
	}
	public void setStatus(Byte status)
	{
		this.status = status;
	}
	public Byte getSex()
	{
		return sex;
	}
	public void setSex(Byte sex)
	{
		this.sex = sex;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String toString()
	{
		return this.username+"("+this.roleid+", "+this.realname+", "+this.email+", "+this.password+")";
	}
}
