package com.focus.cos.web.user.vo;

import java.util.Calendar;
import java.util.Date;

import com.focus.cos.api.Sex;
import com.focus.cos.api.Status;
import com.focus.util.Tools;

/**
 * Description:User实体
 * Create Date:Oct 19, 2008
 * @author Nixin
 *
 * @since 1.0
 */
public class User implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	private int id;
	private int roleid;
	private String username = "";
	private String password = "";
	private String realname = "";
	private String email = "";
	private Byte status = 0;
	private Byte sex = 0;
	private Date lastLogin;		//最后一次登录时间
	private String lastLoginIp;
	private String lastLoginRegion;
	private Date lastChangePassword;	//最后一次修改密码时间
	private int errorCount;		//一天连续密码输入错误次数
	private String historyPassword;	//近五次修改密码记录
	private String creator;	//创建者
	/*扩展属性，临时生成的token*/
	private String token;
	private String rolename;
	/** default constructor */
	public User()
	{
		this.roleid = -1;
		this.username = "";
		this.password = "";
		this.realname = "";
		this.email = "";
		this.status = Status.Enable.getValue();
		this.sex = Sex.Male.getValue();
		lastLogin = Calendar.getInstance().getTime();
		lastChangePassword = Calendar.getInstance().getTime();
		errorCount = 0;
		historyPassword = "";
	}

	public Integer getId()
	{
		return this.id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public int getRoleid()
	{
		return roleid;
	}

	public void setRole(byte roleid)
	{
		this.roleid = roleid;
	}

	public byte getRole()
	{
		return (byte)this.roleid;
	}
	
	public void setRoleid(int roleid)
	{
		this.roleid = roleid;
	}
	
	public String getUsername()
	{
		return this.username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return this.password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getRealname()
	{
		return this.realname;
	}

	public void setRealname(String realname)
	{
		this.realname = realname;
	}

	public String getEmail()
	{
		return this.email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public Byte getStatus()
	{
		return this.status;
	}

	public void setStatus(Byte status)
	{
		this.status = status;
	}

	public Byte getSex()
	{
		return this.sex==0?1:this.sex;
	}

	public void setSex(Byte sex)
	{
		this.sex = sex;
	}

	public Date getLastLogin()
	{
		return lastLogin;
	}
	
	public long getLastLoginTime()
	{
		if(lastLogin==null) return 0;
		return lastLogin.getTime();
	}

	public void setLastLogin(Date lastLogin)
	{
		if( lastLogin != null )
			this.lastLogin = lastLogin;
	}

	public String getLastLoginIp() {
		return lastLoginIp;
	}

	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}

	public String getLastLoginRegion() {
		return lastLoginRegion;
	}

	public void setLastLoginRegion(String lastLoginRegion) {
		this.lastLoginRegion = lastLoginRegion;
	}

	public Date getLastChangePassword()
	{
		return lastChangePassword;
	}

	public long getLastChangePasswordTime()
	{
		if(lastChangePassword==null) return 0;
		return lastChangePassword.getTime();
	}

	public void setLastChangePassword(Date lastChangePassword)
	{
		this.lastChangePassword = lastChangePassword;
	}

	public int getErrorCount()
	{
		return errorCount;
	}

	public void setErrorCount(int errorCount)
	{
		this.errorCount = errorCount;
	}

	public String getHistoryPassword()
	{
		return historyPassword;
	}

	public void setHistoryPassword(String historyPassword)
	{
		this.historyPassword = historyPassword;
	}

	public String toString()
	{
		return username+"["+realname+"]";
	}

	public String getToken()
	{
		return token;
	}

	public void setToken()
	{
		this.token = this.username+this.password+this.getLastLoginTime();
		this.token = Tools.encodeMD5(token);
	}
	
	public String getRolename() {
		return rolename;
	}

	public void setRolename(String rolename) {
		this.rolename = rolename;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}
}