<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html lang="cn" class="no-js">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta http-equiv="X-UA-Compatible" content="IE=9; IE=8; IE=7; IE=EDGE" />

<title><ww:property value='systemName'/></title>
<link href="images/logo1.png" rel="icon" type="image/png">
<!-- Begin Page Progress Bar Files -->
<script type="text/javascript" src="skin/defone/js/pace.min.js"></script>
<link href="skin/defone/css/pace-theme-minimal.css" rel="stylesheet">
<!-- // Page Progress Bar Files -->
<!-- Core CSS - Include with every page -->
<link href="skin/defone/css/bootstrap.min.css" rel="stylesheet">
<!-- Font Awesome Icons -->
<link href="skit/css/font-awesome.min.css" rel="stylesheet">
<!-- Themify Icons -->
<link href="skin/defone/css/themify-icons.css" rel="stylesheet">
<!-- Io"D:/focusnt/report/trunk/IDE/tomcat/webapps/cos/css/easyui.css"nIcons Pack -->
<link href="skin/defone/css/ionicons.min.css" rel="stylesheet">
<!-- Awesome Bootstrap Checkboxes -->
<link href="skin/defone/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
<!-- Page-Level Plugin CSS - Dashboard -->
<link href="skin/defone/css/morris.css" rel="stylesheet">
<link href="skin/defone/css/timeline.css" rel="stylesheet">
<!-- Date Range Picker Stylesheet -->
<link href="skin/defone/css/daterangepicker.css" rel="stylesheet">
<link href="skin/defone/css/default.css" type="text/css" rel="stylesheet" id="style_color" />
<link href="skin/defone/less/animate.less" rel="stylesheet/less" />
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<style type='text/css'>
.cmps-ul {
	list-style:none; 
	padding-left: 0px;
}

.cmps-ul li {
	float: left;
	width: 200px;
	cursor: pointer;
	padding-right: 20px;
	padding-bottom: 20px;
}

.col-cmp{
	width: 200px;
	float: left;
	cursor: pointer;
}
.col-cmp-img{
   	width:128px;
   	height:128px;
}
.funcintro{
	list-style:none; 
}
.funcintro li{
	width: 220px;
	margin-top: 10px;
	margin-bottom: 10px;
	float: left;
}
.important {
	color:<ww:property value='themeColor'/>;
	font-weight: bold;
}
</style>
</head>
<body style="background:#ffffff;">
	<div class="container-fluid" style="border-bottom: 0px solid #dfdfdf;padding-bottom:0px;padding-right:0px;">
     	<ww:if test='status==-1'>
	    <div class="row" style='margin-left:0px;margin-right:30px;margin-bottom:20px;'>
			<div class="panel panel-danger">
               <div class="panel-heading">异常提醒</div>
               <div class="panel-body">
                   <p><ww:property value="responseException"/></p>
                   <p><i class="fa fa-envelope"></i>系统管理员联系方式：<a href='mailto:<ww:property value="sysConfig.getString('SysContact')"/>'> <ww:property value="sysConfig.getString('SysContact')"/></a></p>
                   <p><button type="button" class="btn btn-outline btn-primary" onclick='doNotify(-1);' style='width:320px'><i class="fa fa-user"></i> 向系统管理员发送系统通知 </button></p>
               </div>
            </div>      
        </div>  
        </ww:if>
		<ww:if test='status==1||status==2'>
	    <div class="row" style='margin-left:0px;margin-right:30px;margin-bottom:20px;'>
	        <div class="well">
	            <h3>云架构开放式应用服务框架</h3>
	            <p>是一套能够开发各种类型应用服务系统的软件开发框架。具备跨平台、多进程、分布式等特性。平台核心引擎能够在各种硬件服务器环境下运行，支持包括JAVA、C语言程序、服务器脚本多种程序.</p>
	            <p>您的应用服务系统【<ww:property value="viewTitle"/>】可能由一个或一个以上的模块子系统组成。开发或部署您的应用服务系统，需要从创建模块子系统配置开始。只有这样您才可以为系统配置权限菜单。
	            <a href='#' onclick='tourDeveloper("dev-modules");'>点击了解该配置功能</a></p>
	            <br/>
	            <br/><button type="button" class="btn btn-primary btn-lg btn-block" 
	            	onclick='top.openView("我的系统开发管理", "modules!navigate.action")'>立刻开发我的系统</button>
	        </div>
        </div>
        </ww:if>
    	<ww:if test='status==3'>
	    <div class="row" style='margin-left:0px;margin-right:30px;margin-bottom:20px;'>
		<div class="panel panel-danger">
              <div class="panel-heading">提醒</div>
              <div class="panel-body">
                  <p>您的账号目前没有配置模块子系统业务权限，如有需要请联系您的权限管理员申请设置权限。</p>
                  <p><i class="fa fa-envelope"></i>系统管理员联系方式：<a href='mailto:<ww:property value="sysConfig.getString('SysContact')"/>'> <ww:property value="sysConfig.getString('SysContact')"/></a></p>
                  <p><button type="button" class="btn btn-outline btn-primary" onclick='doNotify(3);'><i class="fa fa-user"></i> 向系统管理员发送系统通知 </button></p>
              </div>
         	  </div>
        </div>     
        </ww:if>
        <ww:if test='status==4'>
	    <div class="row" style='margin-left:0px;margin-right:30px;margin-bottom:20px;'>
	        <div class="well">
	            <h3>云架构开放式应用服务框架</h3>
	            <p>是一套能够开发各种类型应用服务系统的软件开发框架。具备跨平台、多进程、分布式等特性。平台核心引擎能够在各种硬件服务器环境下运行，支持包括JAVA、C语言程序、服务器脚本多种程序.</p>
	            <p>您的应用服务系统【<ww:property value="viewTitle"/>】已经配置了模块子系统菜单。您需要为系统配置角色权限。
	            <a href='#' onclick='tourUser("role-manager");'>点击了解该功能</a></p>
	            <br/>
	            <br/><button type="button" class="btn btn-primary btn-lg btn-block"
	            	onclick='top.openView("角色权限管理", "role!manager.action")'>立刻配置角色权限组</button>
	            <br/>
	        </div>
	    </div>
        </ww:if>
       	<ww:if test='status==0'>
 		<ul class='cmps-ul'>
			<ww:iterator value="listData" status="loop">
			<li onclick="openComp(0, '<ww:property value="getString('SysName')"/>', '<ww:property value="getString('id')"/>');">
	            <div class="panel panel-icon panel-primary">
	                <div class="panel-heading"
	                	style="color: #606060;background-color: <ww:property value="themeColorLight"/>;border-bottom: 1px solid #efefef;"
	                ><i class="fa fa-bookmark"></i> <ww:property value="getString('SysName')"/></div>
	                <div class="panel-body" style="text-align:center">
	                	<img class="col-cmp-img" src='modulelogo/<ww:property value="getString('id')"/>'>
	                </div>
	            </div>
	        </li>
	        </ww:iterator>
			<ww:if test='developer'>
			<li onclick='top.openView("集群监控管理", "monitor!navigate.action")'>
	            <div class="panel panel-icon panel-primary">
	                <div class="panel-heading" 
	                	style="color: #606060;background-color: #fff;border-bottom: 1px solid #efefef;"
	                ><i class="fa fa-server" style='color:<ww:property value="themeColor"/>;'></i> 集群监控管理</div>
	                <div class="panel-body" style="text-align:center">
	                	<i class="fa fa-server" style='font-size:128px;color:<ww:property value="themeColor"/>;margin-left:-96px;'></i>
	                </div>
	            </div>
	        </li>
	        <li onclick='top.openView("集群文件管理", "files!navigate.action")'>
	            <div class="panel panel-icon panel-primary">
	                <div class="panel-heading" style="color: #606060;background-color: #fff;border-bottom: 1px solid #efefef;"
	                ><i class="fa fa-folder-open" style='color:<ww:property value="themeColor"/>;'></i> 集群文件管理</div>
	                <div class="panel-body" style="text-align:center">
	                	<i class="fa fa-folder-open" style='font-size:128px;color:<ww:property value="themeColor"/>;margin-left:-96px;'></i>
	                </div>
	            </div>
	        </li>
	        <li onclick='top.openView("集群程序管理", "control!navigate.action")'>
	            <div class="panel panel-icon panel-primary">
	                <div class="panel-heading" style="color: #606060;background-color: #fff;border-bottom: 1px solid #efefef;"
	                ><i class="fa fa-th-large" style='color:<ww:property value="themeColor"/>;'></i> 集群程序管理</div>
	                <div class="panel-body" style="text-align:center">
	                	<i class="fa fa-th-large" style='font-size:128px;color:<ww:property value="themeColor"/>;margin-left:-96px;'></i>
	                </div>
	            </div>
	        </li>
	        <li onclick='top.openView("集群Zookeeper管理", "zookeeper!navigate.action")'>
	            <div class="panel panel-icon panel-primary">
	                <div class="panel-heading" style="color: #606060;background-color: #fff;border-bottom: 1px solid #efefef;"
	                ><i class="fa fa-delicious" style='color:<ww:property value="themeColor"/>;'></i> 集群Zookeeper管理</div>
	                <div class="panel-body" style="text-align:center">
	                	<i class="fa fa-delicious" style='font-size:128px;color:<ww:property value="themeColor"/>;margin-left:-96px;'></i>
	                </div>
	            </div>
	        </li>
	        <li onclick='top.openView("集群SSH管理", "rpc!navigate.action")'>
	            <div class="panel panel-icon panel-primary">
	                <div class="panel-heading" style="color: #606060;background-color: #fff;border-bottom: 1px solid #efefef;"
	                ><i class="fa fa-terminal" style='color:<ww:property value="themeColor"/>;'></i> 集群SSH管理</div>
	                <div class="panel-body" style="text-align:center">
	                	<i class="fa fa-terminal" style='font-size:128px;color:<ww:property value="themeColor"/>;margin-left:-96px;'></i>
	                </div>
	            </div>
	        </li>
	        <li onclick='top.openView("我的系统开发管理", "modules!navigate.action")'>
	            <div class="panel panel-icon panel-primary">
	                <div class="panel-heading" style="color: #606060;background-color: #fff;border-bottom: 1px solid #efefef;"
	                ><i class="fa fa-user-circle" style='color:#ccc;'></i> 开发我的系统</div>
	                <div class="panel-body" style="text-align:center">
	                	<i class="fa fa-user-circle" style='font-size:128px;color:#ccc;margin-left:-96px;'></i>
	                </div>
	            </div>
	        </li>
	        </ww:if>
		</ul>
        </ww:if>
	</div>
	<ww:if test="developer">
	<div style='margin-left:30px;margin-right:30px;margin-top:0px;'>
        <div class="panel panel-default">
   			<div class="panel-heading"><span><i class='skit_fa_btn fa fa-mortar-board'></i> 开发者功能介绍</span></div>
   			<div class="panel-body" style='padding: 0px;'>
   			<ul class='funcintro'>
   				<li><a href="#" onclick="tourDeveloper('sftcfg-preset');"><i class="fa fa-copyright fa-fw"></i> 软件参数配置</a></li>
                <li><a href="#" onclick="tourDeveloper('user-propmgr');" class='important'><i class="fa fa-user-md fa-fw"></i> 用户扩展属性管理</a></li>
                <li><a href="#" onclick="tourDeveloper('dev-modules');" class='important'><i class="fa fa-puzzle-piece fa-fw"></i> 我的系统开发管理</a></li>
                <li><a href="#" onclick="tourDeveloper('dev-moudles-menus-publish');" class='important'><i class="fa fa-code fa-fw"></i> 审核我的系统菜单配置发布管理</a></li>
	            <li><a href="#" onclick="tourDeveloper('control-publish');" class='important'><i class="fa fa-upload fa-fw"></i> 审核集群程序发布管理</a></li>
	            
				<li><a href="#" onclick="tourDeveloper('zookeeper-navigate');" class='important'><i class="fa fa-table fa-fw"></i> 集群Zookeeper管理器</a></li>
				<li><a href="#" onclick="tourDeveloper('files-navigate');" class='important'><i class="fa fa-folder-open-o fa-fw"></i> 集群文件管理器</a></li>
				<li id=''><a href="#" onclick="tourDeveloper('rpc-navigate');" class='important'><i class="fa fa-terminal"></i> 集群SSH管理器</a></li>
				<li ><a href="#" onclick="tourDeveloper('helper-cosdatabase');"><i class="fa fa-database fa-fw"></i> 集群主数据库管理</a></li>
				<li id=''><a href="#" onclick="tourDeveloper('control-navigate');" class='important'><i class="fa fa-git"></i> 集群程序管理器</a></li>
                <li id=''><a href="#" onclick="tourDeveloper('control-publish');" class='important'><i class="fa fa-cloud-upload"></i> 集群程序发布管理</a></li>
            
             	<li id=''><a href="#" onclick="tourDeveloper('diggcfg-datasource');"><i class="fa fa-database fa-fw"></i> JDBC数据库监控配置</a></li>
	            <li><a href="#" onclick="tourDeveloper('diggcfg-datasource');"><i class="fa fa-database fa-fw"></i> 数据源配置</a></li>
	            <li><a href="#" onclick="tourDeveloper('diggcfg-setquery');"><i class="fa fa-digg fa-fw"></i> 元数据查询配置</a></li>
	            
                <li><a href="javascript:skit_alert('敬请期待');"><i class="fa fa-skyatlas fa-fw"></i> 可是解决方案</a></li>
                <li><a href="javascript:skit_alert('敬请期待');"><i class="fa fa-stumbleupon fa-fw"></i> 爱普多解决方案</a></li>
                <li><a href="javascript:skit_alert('敬请期待');"><i class="fa fa-github-square fa-fw"></i> Github开源</a></li>
                <li><a href="#" onclick="tourDeveloper('helper-developer');"><i class="fa cloud-download fa-fw"></i> 开发者下载区</a></li>
                <li><a href="javascript:skit_alert('敬请期待');"><i class="fa fa-group fa-fw"></i> 开发者论坛</a></li>
                <li><a href="javascript:skit_alert('敬请期待');"><i class="fa fa-comments fa-fw"></i> 开发者吐槽区</a></li>
                <li><a href="javascript:skit_alert('敬请期待');"><i class="fa fa-book fa-fw"></i> 开发者手册</a></li>
				<li><a href="#" onclick="tourDeveloper('helper-cos-data-structure');"><i class="fa fa-book fa-fw"></i>主数据库设计文档</a></li>
				<li><a href="#" onclick="tourDeveloper('helper-icon');"><i class="fa fa-navicon fa-fw"></i>系统可用图标展示</a></li>
                <li><a href="#" onclick="tourDeveloper('helper-jarseye');"><i class="fa fa-eye fa-fw"></i> 查找JAVA类属于哪个JAR</a></li>
                <li><a href="#" onclick="tourDeveloper('helper-regexp');"><i class="fa fa-columns fa-fw"></i> 正则表达式测试工具</a></li>
   			</ul>
   			</div>
   		</div>
   	</div>
	<div style='margin-left:30px;margin-right:30px;margin-top:20px;'>
        <div class="panel panel-default">
   			<div class="panel-heading"><span><i class='skit_fa_btn fa fa-tags'></i> 用户功能介绍</span></div>
   			<div class="panel-body" style='padding: 0px;'>
   			<ul class='funcintro'>
	            <li><a href="#" onclick="tourUser('monitor-navigate');"><i class="fa fa-eye fa-fw"></i> 系统监控</a></li>
	            <li><a href="#" onclick="tourUser('sysalarm-query');"><i class="fa fa-exclamation-triangle fa-fw"></i> 系统告警</a></li>
	            <li><a href="#" onclick="tourUser('syslog-query');"><i class="fa fa-history fa-fw"></i> 系统日志</a></li>
	            <li><a href="#" onclick="tourUser('email-outbox');"><i class="fa fa-envelope fa-fw"></i> 系统邮箱</a></li>
	            <li><a href="#" onclick="tourUser('syscfg-preset');"><i class="fa fa-cog fa-fw"></i> 系统参数配置</a></li>
	            <li><a href="#" onclick="tourUser('sysalarm-config');"><i class="fa fa-exclamation fa-fw"></i> 系统告警配置</a></li>
	            <li><a href="#" onclick="tourUser('notify-config');"><i class="fa fa-envelope-square fa-fw"></i> 系统消息配置</a></li>
	            <li><a href="#" onclick="tourUser('role-manager');"><i class="fa fa-users fa-fw"></i> 角色权限管理</a></li>
	            <li><a href="#" onclick="tourUser('user-manager');"><i class="fa fa-user fa-fw"></i> 用户权限管理</a></li>
	            <li><a href="#" onclick="tourUser('user-password');"><i class="fa fa-user-secret fa-fw"></i> 修改密码</a></li>
	            <li><a href="#" onclick="tourUser('user-password');"><i class="fa fa-book fa-fw"></i> 帮助</a></li>
	            <li><a href="#" onclick="tourUser('sftcfg-about');"><i class="fa fa-info-circle fa-fw"></i> 关于</a></li>
   			</ul>
   			</div>
   		</div>
   	</div>
   	</ww:if>
</body>
<script type="text/javascript">
var account = "<ww:property value='userAccount'/>";//用户账号
function doNotify(type)
{
	if( window.top != window && top && window.top.sendSystemNotify )
	{
		if( type == 3)
			window.top.sendSystemNotify(null, "用户投诉", 
				"用户["+account+"]登录后发现没有被配置模块子系统业务权限。",
				"请协助该用户为其配置正确的权限；或者，该用户不是合法登录用户，请删除或禁用该用户登录访问。");
		else if( type == -1 )
		{
			window.top.sendSystemNotify(null, "用户投诉", 
				"用户["+account+"]登录后发现分布式应用协调程序不能正常工作。",
				"请立刻打开系统监控模块检查定位分布式应用协调程序不能正常工作的原因，并尽快恢复系统的正常工作。",
				null,
				"打开系统监控",
				"<%=Kit.URL_PATH(request)%>omt!monitor.action");
		}
	}
}
/*
	打开组件
	status 组件状态
	name 组件名称
	id 组件id
	defaultView 组件首页打开的链接
	navigate 是否导航的标识
 */
function openComp(status, name, id)
{
	switch(status)
	{
	case 0:
		//如果已经登录就进入模块的界面
		parent.openSubsystem(id);
		break;
	case 1:
		top.skit_alert( '<ww:text name="label.ema.module.error"/>' );
		break;
	case 2:
		top.skit_alert( '<ww:text name="label.ema.components.notopen"/>' );
		break;
	case 3:
		top.skit_alert( '<ww:text name="label.ema.components.disabled"/>' );
		break;
	case 10://退出
	case 11://重新登录
		parent.window.location.href = "login!signout.action";
		break;
	default:
		top.skit_alert( '<ww:text name="label.ema.components.open.error"/>' );
		break;
	}
}
function tourDeveloper(id)
{
	if( parent.window && parent.window.openTourDeveloper )
	{
		if( id == "dev-modules" )
		parent.window.openTourDeveloper(
				id,
				"关于模块子系统配置",
				"在【开发者社区】系统超级管理员可以为系统配置模块子系统，该配置根据业务需求用于分类构建不同子系统，在模块子系统下可配置对应的模块子系统菜单，菜单可授权给指定角色权限组。");
		else if( id == "files-navigate" )
			parent.window.openTourDeveloper(
					id,
					"关于集群文件管理器",
					"COS提供了功能强大的集群文件管理软件，在【开发者社区】系统超级管理员或者开发者可以进入集群的文件管理器，操作集群每台伺服器的文件资源，目录浏览搜索、文件预览、下载、上传等。");
		else if( id == "zookeeper-navigate" )
			parent.window.openTourDeveloper(
					id,
					"关于分布式协调服务(Zookeeper)集群",
					"COS集成了Zookeeper，在【开发者社区】系统超级管理员或者开发者可以进入集群的Zookeeper可视化管理器，操作集群每台伺服器的Zookeeper资源。");
		else
			parent.skit_alert("暂未配置该功能引导介绍");
	}
}
function tourUser(id)
{
	if( parent.window && parent.window.openTourUser )
	{
		if( id == "role-manager" )
		parent.window.openTourUser(
				id,
				"关于角色权限配置",
				"在【用户功能菜单区】系统超级管理员可以配置角色权，通过对模块子系统的菜单向角色权限组设置进行系统的权限分配。");
		else
			parent.skit_alert("暂未配置该功能引导介绍");
	}
}
try
{
	parent.skit_hiddenMask();
	var uniqid = "<ww:property value="uniqeCompoent.getString('id')"/>";
	if( uniqid ) parent.openSubsystem(uniqid);

	var forceResetPassword = <ww:property value='forceResetPassword'/>;
	if( forceResetPassword )
	{
		var message = "<ww:property value='responseMessage'/>";
		parent.openView(message, "user!password.action?chance=<ww:property value='forceResetPasswordCount'/>");
	}
}
catch(e)
{
	alert(e);
}
</script>
<ww:if test='status==1'>
<SCRIPT type="text/javascript">
if( parent.userActionMemory )
{
	var showTour = parent.userActionMemory["tourDeveloper#dev-modules"];
	if( showTour != "1" )
	{
		tourDeveloper("dev-modules");
	}
}
</SCRIPT>
</ww:if>
<ww:if test='status==2'>
<SCRIPT type="text/javascript">
if( parent.userActionMemory )
{
	var showTour = parent.userActionMemory["tourDeveloper#dev-modules"];
	if( showTour != "1" )
	{
		tourDeveloper("dev-modules");
	}
}
</SCRIPT>
</ww:if>
<ww:if test='status==4'>
<SCRIPT type="text/javascript">
if( parent.userActionMemory )
{
	var showTour = parent.userActionMemory["tourUser#role-manager"];
	if( showTour != "1" )
	{
		tourUser("role-manager");
	}
}
</SCRIPT>
</ww:if>
</html>