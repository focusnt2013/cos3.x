package com.focus.cos.control;

import com.focus.util.ConfigUtil;
import com.focus.util.F;

/**
 * 
 * @author think
 *
 */
public class COSPortalRunner extends SystemRunner
{
	public static final String Name = "主界面框架系统"; 
	public static final String Remark = "提供全面的包括程序管理、系统管理、系统权限等界面功能。"+
						"业务系统开发者和维护人员通过登录该系统进行开发、集成与维护工作；"+
						"业务系统用户通过登录该系统操作开发集成的各项业务系统功能。";
	
    public COSPortalRunner(ModuleManager manager)
    {
    	super( "COSPortal", manager );
    	super.className = "org.apache.catalina.startup.Bootstrap";
        this.setName( Name );
        this.setRemark(Remark);
    }

	@Override
	public void initliaize() throws Exception
	{
    	this.frequency = 0;
    	this.timeout = 0;
    	this.setDebug(true);
    	ms = "128";
    	mx = "768";
    	super.cpTag = "-classpath";
    	F tomcat = new F(ConfigUtil.getString("runner.cosportal")); 
    	F bootstrapjar = new F(tomcat, "bin/bootstrap.jar");
    	F daemonjar = new F(tomcat, "bin/commons-daemon.jar");
    	F julijar = new F(tomcat, "bin/tomcat-juli.jar");
    	String separator = System.getProperty("path.separator", ";");
    	cp = new StringBuffer();
    	cp.append(bootstrapjar.getAbsolutePath());
    	cp.append(separator);
    	cp.append(daemonjar.getAbsolutePath());
    	cp.append(separator);
    	cp.append(julijar.getAbsolutePath());
    	extendsCommands.add( "-XX:PermSize=64M");
    	extendsCommands.add( "-XX:MaxPermSize=128m" );
    	extendsCommands.add( "-Dcatalina.base="+tomcat.getAbsolutePath() );
    	extendsCommands.add( "-Dcatalina.home="+tomcat.getAbsolutePath() );
    	extendsCommands.add( "-Djava.security.egd=file:/dev/./urandom");
    	F tmpdir = new F(tomcat, "tmp");
    	extendsCommands.add( "-Djava.io.tmpdir="+tmpdir.getAbsolutePath() );
    	extendsProperties.clear();
    	extendsProperties.add("start");

    	String jdk = System.getProperty("java.home");
    	if( !jdk.endsWith("bin/java") )
    		jdk += "/bin/java";
    	super.getShutdownCommands().add(jdk);
    	super.getShutdownCommands().add("-classpath");
    	super.getShutdownCommands().add(cp.toString());
    	super.getShutdownCommands().add( "-Dcatalina.base="+tomcat.getAbsolutePath() );
    	super.getShutdownCommands().add( "-DDcatalina.home="+tomcat.getAbsolutePath() );
    	super.getShutdownCommands().add( "-Djava.io.tmpdir="+tmpdir.getAbsolutePath() );
    	super.getShutdownCommands().add("org.apache.catalina.startup.Bootstrap");
    	super.getShutdownCommands().add("stop");
		if( !manager.isDatabaseStandby() )
		{
			manager.portalNostartup();
			throw new Exception("主数据库未启动不能启动[主界面框架系统]");
		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}
}
