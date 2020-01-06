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
    <link href="images/cos.ico" rel="shortcut icon" type="image/x-icon">
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
    <!-- IonIcons Pack -->
    <link href="skin/defone/css/ionicons.min.css" rel="stylesheet">
    <!-- Awesome Bootstrap Checkboxes -->
    <link href="skin/defone/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
    <!-- Page-Level Plugin CSS - Dashboard -->
    <link href="skin/defone/css/morris.css" rel="stylesheet">
    <link href="skin/defone/css/timeline.css" rel="stylesheet">
    <!-- Date Range Picker Stylesheet -->
    <link href="skin/defone/css/daterangepicker.css" rel="stylesheet">
    <link href="skin/defone/css/<ww:property value='defaultView'/>.css" type="text/css" rel="stylesheet" id="style_color" />
    <link href="skin/defone/less/animate.less" rel="stylesheet/less" />
    <!-- tour -->    
	<link href="skin/defone/css/bootstrap-tour.min.css" rel="stylesheet">
	<style type='text/css'>
	body {overflow:auto; overflow-x:hidden; margin-top:0px; margin-left:0px; margin-bottom:0px; margin-right:0px }
	.col-cmp{
		width: 256px;
		cursor: pointer;
	}
	#style_bookmark {
		position:fixed;
		z-index:1000;
		top:100px;
		right:-237px;
		margin-right:0;
		padding:15px;
		border-width:1px 0 1px 1px;
		border-style:solid;
		border-color:#e1e1e1;
		border-color:rgba(0,0,0,.2);
		background-color:rgba(255,255,255,.9);
		width:237px;
		-webkit-transition:all 200ms cubic-bezier(.55,.055,.675,.19);
		transition:all 200ms cubic-bezier(.55,.055,.675,.19)
	}	
	#style_bookmark .bookmark_toggle {
		position:absolute;
		left:-37px;
		top:10px;
		width:36px;
		height:36px;
		line-height:36px;
		padding-left:10px;
		border-width:1px 0 1px 1px;
		border-style:solid;
		border-color:#e1e1e1;
		border-color:rgba(0,0,0,.1);
		background:rgba(0,0,0,.9);
		font-size:20px;
		cursor:pointer;
		color:#fff;
		-webkit-border-radius:4px 0 0 4px;
		-moz-border-radius:4px 0 0 4px;
		border-radius:4px 0 0 4px
	}
	.wallpaper{
		margin-top:0px;
		margin-right:0px;
		margin-bottom:0px;
		margin-left:0px;
		background-image:url();
       	background-size:cover; 
       	background-position: center center;
       	background-attachment: fixed;
	}
    .heading-title {
     	padding: 5px;
      	font: 55px/100% "微软雅黑", "Lucida Grande", "Lucida Sans", Helvetica, Arial, Sans;;
      	text-transform: uppercase;
  		text-shadow: -1px -1px 0 #fff,1px 1px 0 #000,1px 1px 0 #000;
	}
	.iframe-menu {
	    float: right;
	    right: 30px;
	    margin-top: -6px;
	    font-weight: 100;
	}
	.span-num{
		position: relative;
		top: -15px;
		display:-moz-inline-box;/*解决IE10 非兼容模式显示不了的问题*/
		display:inline-block;/*解决IE10 非兼容模式显示不了的问题*/
		vertical-align:middle;/*解决IE10 非兼容模式显示不了的问题*/
	}
	.menu_text_overflow{
	    display:block;/*内联对象需加*/
	    width:200px;
	    word-break:keep-all;/* 不换行 */
	    white-space:nowrap;/* 不换行 */
	    overflow:hidden;/* 内容超出宽度时隐藏超出部分的内容 */
	    text-overflow:ellipsis;/* 当对象内文本溢出时显示省略标记(...) ；需与overflow:hidden;一起使用。*/
	}
	.navigate_text_overflow{
	    font-weight:bold;/*内联对象需加*/
	    width:400px;
	    word-break:keep-all;/* 不换行 */
	    white-space:nowrap;/* 不换行 */
	    overflow:hidden;/* 内容超出宽度时隐藏超出部分的内容 */
	    text-overflow:ellipsis;/* 当对象内文本溢出时显示省略标记(...) ；需与overflow:hidden;一起使用。*/
	}
	.skit_common_textarea
	{
		font-size: 12px;
		margin:0 0 1px;
		width: 100%;
		height:120px;
		background: #fffce9;
		border:solid #e6ecf4 1px ;
		color: #494949;
	}
	.skit_common_input
	{
		font-size: 12px;
		margin:0 0 1px;
		width: 100%;
		height:26px;
		background: #fffce9;
		border:solid #c6d5f6 1px ;
		color: #494949;
	}
	.btn.btn-outline.btn-primary:hover {
	    background-color: #fff;
	    border: 1px solid #455a64;
	    color: #455a64;
	}.btn.btn-primary:hover {
	    background-color: #fff;
	}
	.btn.btn-primary {
	    background-color: #455a64;
	    border-color: #455a64;
	}
	.btn {
	    -webkit-border-radius: 20px;
	    -moz-border-radius: 20px;
	    border-radius: 20px;
	    border: none;
	    outline: 0 !important;
	}
	.btn, a {
	    -webkit-transition: all .25s ease-out;
	    -moz-transition: all .25s ease-out;
	    -o-transition: all .25s ease-out;
	    transition: all .25s ease-out;
	}
	.notify-time {
		font-size:8pt;
		color:#a0a0a0
	}
	.notify-name {
		font-size:12px;
		width:288px;
		font-family:微软雅黑,Roboto,sans-serif;
		white-space:nowrap;
		overflow:hidden;
		text-overflow:ellipsis;
		word-break:keep-all;
		display:block;
		padding-left:3px;
	}
	.notify-summary {
		font-size:11px;
		width:268px;
		color:#a0a0a0;
		font-family:微软雅黑,Roboto,sans-serif;
		white-space:nowrap;
		overflow:hidden;
		text-overflow:ellipsis;
		max-width:90%;
		word-break:keep-all;
		display:block;
		padding-left:3px;
	}
	.notify-tr
	{
		padding-top: 1px;
		height:23px;
	    BORDER-BOTTOM: 1px dashed #e6e6e6;
	    BORDER-LEFT: 0px solid #FFF;
		word-spacing: 2px;
		font-size:12px;
		padding-left: 3px;
		padding-right: 3px;
	}
.bar-menu {
	z-index:1000;
	float:left;
	width: 100%;
	padding:5px 0;
	margin:2px 0 0;
	font-size:14px;
	text-align:left;
	list-style:none;
	background-color:#fff;
	-webkit-background-clip:padding-box;
	background-clip:padding-box;
	border-radius:4px;
}
.bar-menu .divider {
	height:1px;
	margin:4px 0;
	overflow:hidden;
	background-color:#e5e5e5
}
.bar-menu>li>a {
	display:block;
	padding:3px 20px;
	clear:both;
	font-weight:400;
	line-height:1.42857143;
	color:#333;
	font-size: 9pt;
	white-space:nowrap
}
.bar-menu>li>a:focus,.bar-menu>li>a:hover {
	color:#262626;
	text-decoration:none;
	background-color:#f5f5f5
}
.bar-menu>.active>a,.bar-menu>.active>a:focus,.bar-menu>.active>a:hover {
	color:#fff;
	text-decoration:none;
	background-color:#337ab7;
	outline:0
}
.bar-menu>.disabled>a,.bar-menu>.disabled>a:focus,.bar-menu>.disabled>a:hover {
	color:#777
}
.bar-menu>.disabled>a:focus,.bar-menu>.disabled>a:hover {
	text-decoration:none;
	cursor:not-allowed;
	background-color:transparent;
	background-image:none;
	filter:progid:DXImageTransform.Microsoft.gradient(enabled=false)
}
.open>.bar-menu {
	display:block
}
.open>a {
	outline:0
}
.bar-menu-right {
	right:0;
	left:auto
}
.bar-menu-left {
	right:auto;
	left:0
}
.bar-header {
	display:block;
	padding:3px 20px;
	font-size:12px;
	line-height:1.42857143;
	color:#777;
	white-space:nowrap
}
.bar-backdrop {
	position:fixed;
	top:0;
	right:0;
	bottom:0;
	left:0;
	z-index:990;
}
</style>
<%=Kit.getDwrJsTag(request,"interface/HelperMgr.js")%> 
<%=Kit.getDwrJsTag(request,"interface/DescktopMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<script type="text/javascript" src="skit/js/jsencrypt.min.js"></script>
</head>
<body onResize='resizeWindow()' style="overflow-y:hidden; margin-top:0px; margin-left:0px; margin-bottom:0px; margin-right:0px">
	<%@ include file="component.jsp"%>
    <div id="wrapper">
        <nav id="navbar" class="navbar navbar-default navbar-fixed-top" role="navigation" style="margin-bottom: 0;">
            <div class="navbar-header">
            	<button type="button" class="navbar-toggle pull-left margin left" data-toggle="collapse" data-target=".sidebar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" onclick="openView('首页', 'index');" style='cursor: pointer;padding: 2px 5px;' title='返回首页'>
                   <img src="<ww:property value='systemLogo'/>" style='height:45px;margin-left:0px;'>
                </a>
            </div>
            <!-- MEGA MENU -->
            <ul class="nav navbar-top-links navbar-left mega-menu hidden-xs hidden-sm" style='display:' id='ulMywork'>
                <li class="dropdown" id="dropdown" onclick="skit_showMask(-1)">
                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="fa fa-list-ul fa-fw"></i> 我的工作 
                        <span class="badge badge-danger animated pulse" id="badge-mytasks" style="display:none"></span>
                    </a>
                    <div class="dropdown-menu mega-menu animated flipInY">
                        <div class="row">
                            <div class="col-sm-4 border right" style="width:50%" id="task-menu-news">
                                <h3><i class="fa fa-newspaper-o fa fw"></i> 新闻</h3>
                            </div>
                            <div class="col-sm-4" style="width:50%" id="task-menu-notifies">
                                <h3><i class="fa fa-envelope-o fa fw"></i> 系统消息</h3>
			                	<table id='task-menu-notifies-table' width='100%' cellpadding='0' cellspacing='0' style='cursor:pointer'>
			                    </table>
                                <span class="separator top btn btn-primary btn-outline btn-block text-uppercase"
                                	 id="task-menu-notifies-last" onclick="openMessenger()">查看所有系统消息</span>
                            </div>
                        </div>
                    </div>
                </li>
                <li class="dropdown">
                    <a class="dropdown-toggle" href="#" onclick="window.location.reload();">
                        <i class="fa fa-refresh fa-fw"></i>
                    </a>
                </li>
            </ul>
            <!-- // MEGA MENU -->
            <ul class="nav navbar-top-links navbar-right hidden-xs">
          		<ww:iterator value="toolbars" status="loop">
                <li class="dropdown" id="<ww:property value='suid'/>">
                    <a class="dropdown-toggle" data-toggle="dropdown" href="#"
                    	onclick="openView('<ww:property value='label'/>', '<ww:property value='href'/>');">
                    	<ww:property value='label'/><ww:property value='size'/>
                    	<ww:if test='href.equals("monitor!navigate.action")'>
                        <i class="fa fa-server fa-fw"></i>
                        </ww:if><ww:else><i class="fa <ww:property value='icon'/> fa-fw"></i></ww:else>
						<ww:if test="size()>0">
                    	<i class="fa fa-angle-down"></i>
                    	</ww:if>
                    </a>
					<ww:if test="size()>0">	
                    <ul class="dropdown-menu dropdown-user animated fadeInUp">
                   		<ww:iterator value="components" status="loop">
                        <li><a href="#" onclick="openView('<ww:property value='label'/>', '<ww:property value='href'/>')">
                        	<i class="fa <ww:property value='icon'/>"></i> <ww:property value='label'/></a></li>
                        </ww:iterator>
                    </ul>
                    </ww:if>
                </li>
                </ww:iterator>
                <li class="dropdown" onclick="openView('实时告警管理', 'sysalarm!manager.action');" id="dropdown-alarms" style="display:none">
                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                    	系统告警
                        <i class="fa fa-exclamation-triangle fa-fw"></i>
                        <span class="badge badge-notification badge-warning animated fadeIn" id="badge-alarms" style="display:none">0</span>
                    </a>
                    <!-- 
                    <ul class="dropdown-menu dropdown-alerts d-timeline p-l-15 p-t-0 p-b-0 animated fadeInUp">
                        <li></li>
                   		<ww:iterator value="alarms" status="loop">
                        <li class="warning">
                            <span class="circle"></span>
                            <span class="stacked-text">      
                                <i class="fa fa-envelope fa-fw"></i> <a href="#"><ww:property value='module'/></a>
                                <a href="#"><ww:property value='type'/></a> <ww:property value='alarmTitle'/>.
                                <small class="text-muted help-block"><ww:date name="%{eventtime}" format="MM-dd HH:mm"/></small>
                            </span>
                        </li>
                        </ww:iterator>
                        <li onclick="openView('实时告警管理', 'sysalarm!manager.action');">
                            <span class="circle"></span>
                            <i class="fa fa-fw fa-exclamation-triangle"></i> <strong>看所有告警</strong>
                            <i class="fa fa-angle-right"></i>
                        </li>
                    </ul> -->
                </li>
                <!-- /.dropdown -->
                
                <li class="dropdown" onclick="openMessenger()" style="display:none">
                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                  		系统消息
                        <i class="fa fa-envelope fa-fw"></i>
						<span class="badge badge-notification badge-info animated fadeIn" id="badge-notifies" style="display:none"></span>
                    	<!-- <i class="fa fa-angle-down"></i> -->
                    </a>
					<!-- 
                    <ul class="dropdown-menu dropdown-alerts d-timeline p-l-15 p-t-0 p-b-0 animated fadeInUp" id="dropdown-menu-notifies">
                        <li onclick="openMessenger();" id="dropdown-menu-notifies-last">
                            <span class="circle"></span>
                            <i class="fa fa-fw fa-envelope"></i> <strong>看所有通知</strong><i class="fa fa-angle-right"></i>
                        </li>
                    </ul>-->
                    <!-- /.dropdown-alerts -->
                </li>
                <!-- /.dropdown -->
                <li class="dropdown">
                    <a class="dropdown-toggle user" data-toggle="dropdown" href="#" id='triggerUserbar'>
                        <ww:property value='userName'/>
                        <img src="<ww:property value='userHead'/>" alt="" data-src="<ww:property value='userHead'/>" data-src-retina="<ww:property value='userHead'/>" class="img-responsive img-circle user-img" id='myaccount' width="25">
                        <i class="fa fa-angle-down"></i>
                    </a>
                    <ul class="dropdown-menu dropdown-user animated fadeInUp">
                        <li class="user-information">
                             <div class="media">
                                <a class="pull-left" href="#">
                                    <img class="media-object user-profile-image img-circle" src="<ww:property value='userHead'/>" width="25">
                                </a>
                                <div class="media-body">
                                    <h4 class="media-heading"><ww:property value='userName'/>
                                    <span class="label label-info"><ww:property value='userAccount'/></span></h4>
                                    <h6><ww:property value='userInfo'/></h6>
                                  	<span class="label label-warning">上次登录时间</span><ww:date name="lastLoginDate" format="yyyy-MM-dd HH:mm"/>
                                    <a href="login!signout.action" class="text-danger"><i class="fa fa-sign-out fa-fw"></i></a>
                                    <!--<hr style="margin:8px auto">
                                    <span class="label label-default">HTML5/CSS3</span>
                                    <span class="label label-warning">jQuery</span>-->
                                </div>
                            </div>
                        </li>
                        <li class="divider"></li>
                        <!-- 
                        <li><a href="#"><i class="fa fa-tasks"></i> Tasks <span class="badge badge-info pull-right">6 new</span></a></li>
                        <li><a href="#"><i class="fa fa-envelope"></i> Messages <span class="badge badge-info pull-right">4 new</span></a></li>
                        <li><a href="#" onclick="openView('系统通知', 'notify!manager.action');"><i class="fa fa-envelope fa-fw"></i> 系统通知</a></li>
                        -->
                        <ww:if test='developer'>
                        <li id='monitor-navigate'><a href="#" onclick="openView('系统监控', 'monitor!navigate.action');"><i class="fa fa-eye fa-fw"></i> 系统监控</a></li>
                        <li id='sysalarm-query'><a href="#" onclick="openView('系统告警', 'sysalarm!manager.action');"><i class="fa fa-exclamation-triangle fa-fw"></i> 系统告警</a></li>
                        </ww:if>
                        <ww:if test='userRole==1'>
                        <li id='syslog-query'><a href="#" onclick="openView('系统日志', 'syslog!query.action', 'fullscreen');"><i class="fa fa-history fa-fw"></i> 系统日志</a></li>
                        <li id='email-outbox'><a href="#" onclick="openView('系统邮箱', 'email!outbox.action', 'fullscreen');"><i class="fa fa-envelope fa-fw"></i> 系统邮箱</a></li>
                        <li class="divider"></li>
                        <li id='syscfg-preset'><a href="#" onclick="openView('系统参数配置', 'syscfg!preset.action');"><i class="fa fa-cog fa-fw"></i> 系统参数配置</a></li>
                        <li id='sysalarm-config'><a href="#" onclick="openView('系统告警配置', 'sysalarm!config.action');"><i class="fa fa-bell-o fa-fw"></i> 系统告警配置</a></li>
                        <li id='notify-config'><a href="#" onclick="openView('系统消息配置', 'notify!config.action');"><i class="fa fa-envelope-square fa-fw"></i> 系统消息配置</a></li>
                        <!-- <li><a href="#" onclick="openView('系统公告管理', 'notice!query.action');"><i class="fa fa-puzzle-piece fa-fw"></i> 系统公告管理</a></li> -->
                        <li class="divider"></li>
                        <li id='role-manager'><a href="#" onclick="openView('角色权限管理', 'role!manager.action');"><i class="fa fa-users fa-fw"></i> 角色权限管理</a></li>
                        <li id='user-manager'><a href="#" onclick="openView('用户权限管理', 'user!manager.action');"><i class="fa fa-user fa-fw"></i> 用户权限管理</a></li>
                        </ww:if>
                        <li><a href="#" onclick="openView('修改密码', 'user!password.action');"><i class="fa fa-user-secret fa-fw"></i> 修改密码</a></li>
                        <li class="divider"></li>
                        <li><a href="#" onclick="skit_alert('功能暂未开放');"><i class="fa fa-book fa-fw"></i> 帮助</a></li>
                        <li><a href="#" onclick="openView('关于', 'sftcfg!about.action');"><i class="fa fa-info-circle fa-fw"></i> 关于</a></li>
                        <ww:if test='userRole==1'>
                        <li><a href="#" onclick="openView('集群升级管理', 'control!checkupgrades.action');"><i class="fa fa-cloud-download fa-fw"></i> 升级</a></li>
                        </ww:if>
                        <li><a href="login!signout.action" class="text-danger"><i class="fa fa-sign-out fa-fw"></i> 退出</a>
                        </li>
                    </ul>
                    <!-- /.dropdown-user -->
                </li>
                <!-- /.dropdown -->
                <li class="active" title='打开视图书签'>
                <a href="#" class="chat animated fadeIn trigger-sidebar" style='font-size:20px;' id='triggerSidebar'
                   onclick='openTriggerBar()'>
                	<i class="fa fa-bookmark" id="triggerSidebarIcon"></i>
	                <span class="badge badge-notification badge-success animated fadeIn" id="bookmarkTips" style='left:-7px'>1</span>
	                <span class="badge badge-notification badge-danger animated fadeIn" id="messageTips" style='left:-7px;display:none'>0</span>
	                <i class="fa fa-angle-left"></i>
                </a>
                </li>
            </ul>
            <!-- /.navbar-top-links -->

        </nav>
        <!-- /.navbar-static-top -->

        <nav id="menu" class="navbar-default navbar-fixed-side hidden-xs" role="navigation" style="display:none;visibility:visible;">
            <ul class="sidebar-stats">
                <li class="row margin left right inner-b">
                    <div class="col-xs-6 reset padding left" style="padding-top:15px;">
                        <span class="h6" id="title_dtime"></span>
                    </div>
                    <div class="col-xs-6 text-right inner-t reset padding right" style='font-size:24pt;padding-right:10px'>
                    	<i class="fa fa-clock-o animated flip"></i></div>   
                </li>
                <!--
                <li class="row margin left right">
                    <div class="col-xs-6 reset padding left">
                        <span class="text-muted">危险度</span><br/>
                        <span class="h4">0%</span>
                    </div>
                    <div class="easy-pie danger pull-right margin right" data-percent="0%"><div class="percent"></div></div>   
                </li>-->
                <!-- <li class="margin left right">
                    <h5>summary</h5>
                </li>
                <li class="row margin left right inner-b">
                    <div class="col-xs-6 reset padding left">
                        <span class="text-muted">Daily Traffic</span>
                        <span class="h4">630, 201</span>
                    </div>
                    <div class="sparkline warning col-xs-6 text-right inner-t reset padding right"></div>   
                </li>
                <li class="row margin left right inner-t">
                    <span class="text-muted">Project OASIS</span>
                    <span class="text-muted pull-right">40% completed</span>
                    <div class="progress progress-bar-mini progress-striped active">
                        <div class="progress-bar progress-bar-info" data-toggle="tooltip" data-container="body" title="Task progress 40%" role="progressbar" aria-valuenow="40" aria-valuemin="0" aria-valuemax="100" style="width: 40%">
                            <span class="sr-only">40% (success)</span>
                        </div>
                    </div>
                </li>
                <li class="row margin left right">
                    <div class="col-xs-6 reset padding left">
                        <span class="text-muted">CPU Usage</span>
                        <span class="h4">78,5%</span>
                    </div>
                    <div class="easy-pie danger pull-right margin right" data-percent="78%"><div class="percent"></div></div>   
                </li> -->
            </ul>
            <div class="sidebar-collapse">
                <ul class="nav" id="side-menu">
                    <li class="sidebar-user" id="side-menu-user">
                        <div class="user-img">
                            <img src="<ww:property value='userHead'/>"
                             alt="用户角色头像"
                             data-src="<ww:property value='userHead'/>"
                             data-src-retina="<ww:property value='userHead'/>"
                             width="65" height="65" class="img-responsive img-circle animated bounceIn">
                        </div>
                        <div class="user-info">
                            <div class="user-greet">欢迎</div>
                            <div class="user-name"><ww:property value='userName'/></div>
                            <div class="user-status animated bounceInLeft">
                                <span class="label label-success dropdown-toggle">在线</span>
                            </div>
                        </div>
                    </li>
                    <li class="sidebar-search" style="padding-top:5px;padding-bottom:5px;">
                        <div class="input-group custom-search-form">
                            <input type="text" class="form-control" placeholder="快速搜索菜单..." onkeypress='searchFunction(this);'
                            	id='iSearchFunction' style='color:#f3c016;height:22px;border'>
                            <span class="input-group-btn">
                                <button class="btn btn-inverse" type="button" onclick='searchFunction()'>
                                    <i class="fa fa-search"></i>
                                </button>
                            </span>
                        </div>
                        <!-- /input-group -->
                    </li>
                    <li id="home - index" style="display:none">
                    	<a href="#" onclick="openView('首页', 'index');" id='aIndex'><i class="fa fa-home fa-fw"></i>首页</a>
                    </li>
                    <!-- 第一层子系统 -->
                    <ww:iterator value="listData" status="loop">
                    <input type="hidden" id="<ww:property value='suid'/>" value="<ww:property value='defaultView'/>">
                    <input type="hidden" id="<ww:property value='suid'/>_label" value="<ww:property value='label'/>">
                    <li id="<ww:property value='suid'/> - <ww:property value='defaultView'/>"
                    	title="<ww:property value='label'/>"
                    	style="display:none">
                        <a href="#" onclick="openView('<ww:property value="label"/>', '<ww:property value="defaultView"/>', '<ww:property value="target"/>', true);"
                        	style='color:#f3c016;font-weight:bold;'>
                        	<i class="fa <ww:property value='icon'/> fa-fw"></i> <ww:property value='label'/></a>
                    </li>
                    <!-- 第1层子系统的各个模块 -->
                    <ww:iterator value="components" status="loop">
                    <li id="<ww:property value='suid'/>" title="<ww:property value='label'/>" style="display:none">
                        <ww:if test="size()>0">
                        <a href="#" data-href="openView('<ww:property value="label"/>', '<ww:property value="viewHref"/>', '<ww:property value="target"/>', true);"
                           id="<ww:property value='suid'/>-aa"><i class="fa <ww:property value='icon'/> fa-fw"></i> <ww:property value='label'/> <span class="fa arrow"></span></a>
	                    <ul class="nav nav-second-level collapse">
                   			<!-- 第2层子系统的各个模块的功能 -->
                   			<ww:iterator value="components" status="loop">
                            <li id="<ww:property value='suid'/>" title="<ww:property value='label'/>">
                                <ww:if test="size()>0">
                                <a href="#"
                                   id="<ww:property value='suid'/>-aa"
                                   data-li='<ww:property value='suid'/>'
                                   data-href="openView('<ww:property value="label"/>', '<ww:property value="viewHref"/>', '<ww:property value="target"/>', true);"><i class="fa <ww:property value='icon'/> fa-fw"></i><ww:property value='label'/>
                                <span class="fa arrow"></span>
                                </a>
                   				<!-- 第3层各个模块的Action子菜单 -->
                                <ul class="nav nav-third-level collapse">
                   					<ww:iterator value="components" status="loop">
                                    <li id="<ww:property value='suid'/>" title="<ww:property value='label'/>">
                                        <a href="#" onclick="openView('<ww:property value="label"/>', '<ww:property value="viewHref"/>', '<ww:property value="target"/>', true);"
	                                	   id="<ww:property value='suid'/>-aa"><ww:property value='label'/></a>
                                    </li>
                    				</ww:iterator>
                                </ul>
                                </ww:if>
                                <ww:else>
                                <a href="#" onclick="openView('<ww:property value="label"/>', '<ww:property value="viewHref"/>', '<ww:property value="target"/>', true);"
                               	   id="<ww:property value='suid'/>-aa"><i class="fa <ww:property value='icon'/> fa-fw"></i><ww:property value='label'/></a>
                                </ww:else>
                            </li>
                    		</ww:iterator>
	                    </ul>
	                    </ww:if>
	                    <ww:else>
	                    <a href="#" onclick="openView('<ww:property value="label"/>', '<ww:property value="viewHref"/>', '<ww:property value="target"/>', true);"><i class="fa <ww:property value='icon'/> fa-fw"></i> <ww:property value='label'/></a>
	                    </ww:else>
                    </li>
              		</ww:iterator>
                    </ww:iterator>
                </ul>
                <!-- /#side-menu -->
            </div>
            <!-- /.sidebar-collapse -->
        </nav>
        <!-- /.navbar-static-side -->
        <div id="page-wrapper" class="fixed-navbar ">
			<div id="page-header" class="page-header" style='height:150px;background: transparent url("<ww:property value='wallpaper'/>") no-repeat scroll center center;background-size:cover;'>
    			<h3 class="heading heading-title" style="font-size:20pt;"><i class="fa fa-home animated flip"></i>  <ww:property value='systemName'/>
    			<span class="sub-heading heading-title" style="font-size:12pt;font-weight:none;"><ww:property value='sysConfig.getString("SysDescr")'/></span></h3>
			</div>
			<!-- /.col-lg-12 -->
			<!-- Breadcrumbs -->
			<ol id="breadcrumb" class="breadcrumb">
			    <li id="breadcrumb_0"><a src='#' onclick='var r=switchView("index");' style='cursor:pointer'>首页</a></li>
			    <li id="breadcrumb_1"></li>
			    <li id="breadcrumb_2"></li>
			    <li id="breadcrumb_3"></li>
			    <li id="breadcrumb_4"></li>
			    <li id="breadcrumb_5"></li>
				<div class="iframe-menu">
	                 <button onclick="showMenu(this)" title='展开显示菜单栏' type="button" data-action="showmenu" class="btn btn-default btn-circle" id='btnShowMenu'>
	                 	<i class="fa fa-angle-right" id='showMenuIcon'></i></button>
	                 <button onclick="showBanner(this)" title='收起隐藏Banner' type="button" data-action="showbanner" class="btn btn-default btn-circle">
	                 	<i class="fa fa-angle-down" id='showBannerIcon'></i></button>
	                 <button type="button" data-action="reload" class="btn btn-default btn-circle" onclick="reload(this)">
	                 	<i class="fa fa-refresh"></i></button>
	                 <!-- 
	                 	div class="dropdown">
	                     <button type="button" data-action="settings" class="btn btn-default btn-action btn-xs dropdown-toggle hidden-xs hidden-sm" data-toggle="dropdown"><i class="fa fa-cog"></i></button>
	                     <ul class="dropdown-menu animated fadeInDown">
	                         <li><a href="#"><i class="fa fa-comment"></i> Add a note</a></li>
	                         <li><a href="#"><i class="fa fa-tasks"></i> Add to Tasks</a></li>
	                         <li><a href="#"><i class="fa fa-map-marker"></i> Pin to TaskBar</a></li>
	                         <li class="divider"></li>
	                         <li><a href="#"><i class="fa fa-refresh"></i> Reload Content</a></li>
	                     </ul>
	                 </div>
	                 -->
	                 <button type="button" data-action="goback" class="btn btn-default btn-circle" id="btnBack" onclick="goBack(this)">
	                 	<i class="fa fa-arrow-left"></i></button>
	                 <button type="button" data-action="goforward" class="btn btn-default btn-circle" id="btnForward" onclick="goForward(this)">
	                 	<i class="fa fa-arrow-right"></i></button>
	                 <button type="button" data-action="close" class="btn btn-warning btn-circle" id="btnClose" onclick="closeView();">
	                 	<i class="fa fa-close"></i></button>
	            </div>
			</ol>
			<!-- // Breadcrumbs -->
			<table id='feview' cellspacing='0' cellpadding='0' width='100%'></table>
        </div>
        
        <!-- /#page-wrapper -->
        <div id="chat" class="hidden-print">
			<div class="chat-header clearfix" id='chatHeader'>
				<!-- Chat Bar title -->
				<span class="title" id='chatTitle'>已打开视图书签</span>
				<!-- Chat bar close button -->
				<a id='closeSidebar' href="#" class="close-chat pull-right"  data-toggle="tooltip" data-placement="bottom" data-container="body">
					<i class="ion-chevron-right"></i></a>
			</div>
			<ul class="nav nav-tabs" role="tablist"" id='chatTabs'>
				<li role="presentation" class="active">
					<a href="#on-line" aria-controls="on-line" role="tab" data-toggle="tab" aria-expanded="true" id='activeView'
						onclick='switchBar(this)'>
						<i class="fa fa-fw fa-bookmark"></i></a></li>
				<li role="presentation">
					<a href="#on-chat" aria-controls="on-chat" role="tab" data-toggle="tab" aria-expanded="false" id='activeChat'
						onclick='switchBar(this)'>
						<i class="fa fa-comments fa-cog"></i></a></li>
				<li role="presentation">
					<a href="#style_switcher" aria-controls="style_switcher" role="tab" data-toggle="tab" aria-expanded="false" id='activeSwitcher'
						onclick='switchBar(this)'>
						<i class="fa fa-fw fa-cog"></i></a></li>
				<ww:if test='developer'>
				<li role="presentation">
					<a href="#cfg_developers" aria-controls="cfg_developers" role="tab" data-toggle="tab" aria-expanded="false" id='activeDevelopers'
						onclick='switchBar(this)'>
						<i class="fa fa-fw fa-group"></i></a></li>
				</ww:if>
			</ul>
			<div class="tab-content">
				<!-- On-Line list -->
				<div role="tabpanel" class="tab-pane fade in active" id="on-line">
					<div class="contact-list" id='divViewPanel'>
						<ul class="media-list" id="media-list">
						</ul>	
					</div>
				</div>
				<div role="tabpanel" class="tab-pane fade in active" id="on-chat">
					<div class="widget widget-chat">
		                <div class="chat-panel panel panel-default">
		                    <div class="panel-body has-nice-scroll" id="divChat" tabindex="5000" 
		                    	style="overflow-y: hidden; outline: none; border:0px solid red">
		                        <ul class="chat" id="chat-list">
		                        	<li style='display:none' id='chat-list-first'></li>
		                        </ul>
		                    </div>
		                    <div class="panel-footer" id="chatTypeMessage">
		                        <div class="input-group">
		                            <input id="inputDescktopMessage" class="form-control input-sm" placeholder="在这里输入消息..." type="text" style='border:1px solid #efefef;'>
		                            <span class="input-group-btn"><button class="btn btn-warning btn-sm" id="btn-chat" onclick='sendDescktopMessage()'>发送 </button></span>
		                        </div>
		                    </div>
		                </div>
		            </div>
				</div>
				<!-- End of On-Line list -->
				<div role="tabpanel" class="tab-pane fade style_items switcher_open" id="style_switcher" 
					style="position:relative;margin-right:0px;top:10px;border-width:0px;">
                	<h4><i class="fa fa-adjust fa-fw"></i> 色彩样式</h4>
<ul class="clearfix colors">
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" 
		id="Style-default" title="Default" data-bg-color="#18bc9c" data-link-color="#ffffff" data-border-color="#18bc9c" data-style="default" style="background-color: #18bc9c;"></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top"
		id="Style-blue" title="Blue" data-bg-color="#23bab5" data-link-color="#ffffff" data-border-color="#23bab5" data-style="blue" style="background-color: #23bab5;"></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top"
		id="Style-honey_flower" title="Honey Flower" data-bg-color="#674172" data-link-color="#ffffff" data-border-color="#674172" data-style="honey_flower" style="background-color: #674172;"></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" 
		id="Style-razzmatazz" title="Razzmatazz" data-bg-color="#DB0A5B" data-link-color="#ffffff" data-border-color="#DB0A5B" data-style="razzmatazz" style="background-color: #DB0A5B;"></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top"
		id="Style-ming" title="Ming" data-bg-color="#336E7B" data-link-color="#ffffff" data-border-color="#336E7B" data-style="ming" style="background-color: #336E7B;"></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top"
		id="Style-yellow" title="Yellow" data-bg-color="#ffd800" data-link-color="#ffffff" data-border-color="#336E7B" data-style="yellow" style="background-color: #ffd800;"></li>
	<!-- Half Styles
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="Red" data-bg-color="#e96363" data-link-color="#ffffff" data-border-color="#e96363" data-style="red" style="background-color: #e96363;"></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="Green" data-bg-color="#5cb85c" data-link-color="#ffffff" data-border-color="#5cb85c" data-style="green" style="background-color: #5cb85c;"></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="Snuff" data-bg-color="#DCC6E0" data-link-color="#ffffff" data-border-color="#DCC6E0" data-style="snuff" style="background-color: #DCC6E0;"></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="Alice" data-bg-color="#E4F1FE" data-link-color="#444" da data-border-color="#cccccc" data-style="alice" style="background-color: #E4F1FE;"></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="Orange" data-bg-color="#e97436" data-link-color="#ffffff" data-border-color="#e97436" data-style="orange" style="background-color: #e97436;"></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="White" data-bg-color="#ffffff" data-link-color="#444" da data-border-color="#ccc" data-style="white" style="background-color: #ffffff;"></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="(HALF) Green" data-bg-color="#fff" data-sidebar-color="#5cb85c" data-link-color="#444" data-border-color="#5cb85c" data-style="half_green"><span class="half_style" style="background-color: #5cb85c;"></span></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="(HALF) Red" data-bg-color="#fff" data-sidebar-color="#e96363" data-link-color="#444" data-border-color="#e96363" data-style="half_red"><span class="half_style" style="background-color: #e96363;"></span></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="(HALF) Blue" data-bg-color="#fff" data-sidebar-color="#23bab5" data-link-color="#444" data-border-color="#23bab5" data-style="half_blue"><span class="half_style" style="background-color: #23bab5;"></span></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="(HALF) Orange" data-bg-color="#fff" data-sidebar-color="#e97436" data-link-color="#444" data-border-color="#e97436" data-style="half_orange"><span class="half_style" style="background-color: #e97436;"></span></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="(HALF) Alice" data-bg-color="#fff" data-sidebar-color="#E4F1FE" data-link-color="#444" da data-border-color="#cccccc" data-style="half_alice"><span class="half_style" style="background-color: #E4F1FE;"></span></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="(HALF) Honey Flower" data-bg-color="#fff" data-sidebar-color="#674172" data-link-color="#444" data-border-color="#674172" data-style="half_honey_flower"><span class="half_style" style="background-color: #674172;"></span></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="(HALF) Razzmatazz" data-bg-color="#fff" data-sidebar-color="#DB0A5B" data-link-color="#444" data-border-color="#DB0A5B" data-style="half_razzmatazz"><span class="half_style" style="background-color: #DB0A5B;"></span></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="(HALF) Snuff" data-bg-color="#fff" data-sidebar-color="#DCC6E0" data-link-color="#444" data-border-color="#DCC6E0" data-style="half_snuff"><span class="half_style" style="background-color: #DCC6E0;"></span></li>
	<li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="(HALF) Ming" data-bg-color="#fff" data-sidebar-color="#336E7B" data-link-color="#444" data-border-color="#336E7B" data-style="half_ming"><span class="half_style" style="background-color: #336E7B;"></span></li>
	-->
</ul>
	                <h4><i class="fa fa-cogs fa-fw"></i> 其它设置</h4>
	                <ul class="list-group">
	                	<!--
	                    <li class="list-group-item">
	                        <span>Fixed Navbar</span>
	                        <div class="checkbox checkbox-success checkbox-inline pull-right"><input type="checkbox" class="layout_switch" id="fixed_navbar" checked="checked"><label for="fixed_navbar"></label></div>
	                    </li>
	                    <li class="list-group-item">
	                        <span>Sidebar Fixed Sidebar</span>
	                        <div class="checkbox checkbox-success checkbox-inline pull-right"><input type="checkbox" class="layout_switch" id="fixed_sidebar" checked="checked"><label for="fixed_sidebar"></label></div>
	                    </li>-->
	                    <li class="list-group-item">
	                        <span>用户互动免打扰</span>
	                        <div class="checkbox checkbox-success checkbox-inline pull-right">
	                        	<input type="checkbox" class="layout_switch" onclick="switchMessageTrigger(this)" checked='checked' id='fixed_triggermessage'><label for="fixed_triggermessage"></label></div>
	                    </li>
	                    <ww:if test="userRole==1">
	                    <li class="list-group-item">
	                        <span>系统自动升级</span>
	                        <div class="checkbox checkbox-success checkbox-inline pull-right">
	                        	<input type="checkbox" class="layout_switch" onclick="setAutoUpgrade(this)"
	    	                    	<ww:if test='autoupgrade'>checked='checked'</ww:if>
	                        		id='fixed_autoupgrade'><label for="fixed_autoupgrade"></label></div>
	                    </li>
	                    </ww:if>
	                    <li class="list-group-item">
	                        <span>显示软件信息</span>
	                        <div class="checkbox checkbox-success checkbox-inline pull-right">
	                        	<input type="checkbox" class="layout_switch" id="fixed_footer"><label for="fixed_footer"></label></div>
	                    </li>
	                </ul>
				</div>
				<div role="tabpanel" class="tab-pane fade in active" id="cfg_developers">
					<div class="contact-list" id='divDeveloperPanel'>
	                    <ul class="bar-menu animated fadeInUp">
	                    	<ww:if test='userRole==1'>
	                    	<li id='sftcfg-preset'><a href="#" onclick="openView('软件参数配置', 'sftcfg!preset.action');"><i class="fa fa-copyright fa-fw"></i> 软件参数配置</a></li>
	                    	<li id='user-propmgr'><a href="#" onclick="openView('用户扩展属性配置', 'user!propmgr.action');"><i class="fa fa-user-md fa-fw"></i> 用户扩展属性配置</a></li>
		                    <li id='control-proxy'><a href="#" onclick="openView('主控工作站管理', 'monitorcfg!apiproxy.action');"><i class="fa fa-window-restore fa-fw"></i> 主控工作站管理</a></li>
		                    <li id='jdbc-monitor'><a href="#" onclick="openView('JDBC数据库监控配置', 'monitorcfg!databases.action');"><i class="fa fa-database fa-fw"></i> JDBC数据库监控配置</a></li>
		                    <li class="divider"></li>
	                        <li id='system-security'><a href="#" onclick="openView('我的系统接口管理', 'security!manager.action');"><i class="fa fa-assistive-listening-systems fa-fw"></i> 我的系统接口管理</a></li>
	                        </ww:if>
	                        <li id='dev-modules'><a href="#" onclick="openView('我的系统开发管理', 'modules!navigate.action');"><i class="fa fa-codepen fa-fw"></i> 我的系统开发管理</a></li>
	                        <li class="divider"></li>
							<li id='zookeeper-navigate'><a href="#" onclick="openView('集群Zookeeper(分布式协调服务)管理', 'zookeeper!navigate.action');"><i class="fa fa-table fa-fw"></i> 集群Zookeeper管理</a></li>
							<li id='files-navigate'><a href="#" onclick="openView('集群文件管理', 'files!navigate.action');"><i class="fa fa-folder-open-o"></i> 集群文件管理</a></li>
							<li id='rpc-navigate'><a href="#" onclick="openView('集群SSH管理', 'rpc!navigate.action');"><i class="fa fa-terminal"></i> 集群SSH管理</a></li>
							<li id='control-navigate'><a href="#" onclick="openView('集群程序管理', 'control!navigate.action');"><i class="fa fa-windows"></i> 集群程序管理</a></li>
	                    	<ww:if test='userRole==1'>
	                        <li id='helper-cosdatabase'><a href="#" onclick="openView('集群主数据库管理', 'syscfg!database.action');"><i class="fa fa-database fa-fw"></i> 集群主数据库管理</a></li>
							</ww:if>
	                        <li class="divider"></li>
<!--
	                        <li><a href="javascript:skit_alert('敬请期待');"><i class="fa fa-skyatlas fa-fw"></i> 可是解决方案</a></li>
	                        <li><a href="javascript:skit_alert('敬请期待');"><i class="fa fa-stumbleupon fa-fw"></i> 爱普多解决方案</a></li>
	                        <li class="divider"></li>
	                        <li><a href="javascript:skit_alert('敬请期待');"><i class="fa fa-github fa-fw"></i> Github开源</a></li>
-->
	                        <li><a href="#" onclick="openView('在线介绍与下载', 'helper!developer.action?v=1');"><i class="fa fa-cloud-download fa-fw"></i> 在线介绍与下载</a></li>
<!--
	                        <li><a href="javascript:skit_alert('敬请期待');"><i class="fa fa-group fa-fw"></i> 开发者论坛</a></li>
	                        <li><a href="javascript:skit_alert('敬请期待');"><i class="fa fa-comments fa-fw"></i> 开发者吐槽区</a></li>
	                        <li><a href="javascript:skit_alert('敬请期待');"><i class="fa fa-book fa-fw"></i> 开发者手册</a></li>
-->
							<li><a href="#" onclick="openView('主数据库设计文档', 'helper!pdf.action?filename=cos-data-structure.pdf');"><i class="fa fa-book fa-fw"></i>主数据库设计文档</a></li>
	                        <li class="divider"></li>
							<li><a href="#" onclick="openView('系统图标(Font Awesome4.7)感谢Dave Gandy', 'http://fontawesome.dashgame.com/');"><i class="fa fa-image fa-fw"></i>系统图标(Font Awesome)</a></li>
	                        <li><a href="#" onclick="openView('系统图标(jQuery)', 'https://api.jqueryui.com/resources/icons-list.html');"><i class="fa fa-image fa-fw"></i>系统图标(jQuery-UI)</a></li>
	                        <li id='editor-javascript'><a href="#" onclick="openView('JAVASCRIPT脚本编辑器', 'editor!javascript.action?ww=%_windowWidth%');"><i class="fa fa-font fa-fw"></i> JAVASCRIPT脚本编辑器</a></li>
	                        <li id='editor-xml'><a href="#" onclick="openView('XML脚本编辑器', 'editor!xml.action');"><i class="fa fa-text-width fa-fw"></i> XML脚本编辑器</a></li>
	                        <li id='editor-json'><a href="#" onclick="openView('JSON脚本编辑器', 'editor!json.action');"><i class="fa fa-code fa-fw"></i> JSON脚本编辑器</a></li>
	                        <li id='editor-css'><a href="#" onclick="openView('CSS脚本编辑器', 'editor!css.action');"><i class="fa fa-rss fa-fw"></i> CSS脚本编辑器</a></li>
	                        <li id='editor-sql'><a href="#" onclick="openView('SQL脚本编辑器', 'editor!sql.action');"><i class="fa fa-rss fa-fw"></i> SQL脚本编辑器</a></li>
	                        <li id='editor-compare'><a href="#" onclick="openView('TEXT比较编辑器', 'editor!compare.action');"><i class="fa fa-columns fa-fw"></i> TEXT比较编辑器</a></li>
	                        <li id='rpc-findjars'><a href="#" onclick="openView('定位查找服务器JAR包', 'rpc!findjars.action');"><i class="fa fa-eye fa-fw"></i> 查找JAVA类属于哪个JAR</a></li>
	                        <li id='helper-regexp'><a href="#" onclick="openView('正则表达式测试工具', 'helper!regexp.action');"><i class="fa fa-columns fa-fw"></i> 正则表达式测试工具</a></li>
	                        <li id='helper-inputmask'><a href="#" onclick="openView('JQUERY输入校验样例', 'http://www.cnblogs.com/suizhikuo/p/3443932.html');"><i class="fa fa-columns fa-fw"></i> JQUERY输入校验样例</a></li>
	                        <li id='helper-colors'><a href="#" onclick="openView('颜色表', 'helper!colors.action?col=10');"><i class="fa fa-th fa-fw"></i> 颜色表</a></li>
	                        <li id='helper-maven-dependence'><a href="#" onclick="openView('Maven依赖查询', 'http://repository.sonatype.org/index.html');"><i class="fa fa-connectdevelop fa-fw"></i> Maven依赖查询</a></li>
	                        <ww:property value="developerMenu" escape='false'/>
	                    </ul>
					</div>
				</div>
			</div>
		</div>
        <!-- Style Switcher switcher_open
        <div id="style_bookmark" class="hidden-print">
            <a class="bookmark_toggle chat animated fadeIn" style="padding-top:7px;"><i class="fa fa-bookmark"></i>
            <span class="badge badge-notification badge-success animated fadeIn" id="bookmarkTips">1</span></a>
        </div> -->
  		<!-- footer -->
        <footer >
            <p>COS.WEB <i class="fa fa-connectdevelop fa-fw"></i> <ww:property value='version'/>
                <ww:if test='userRole==1'>
               <a onclick='upgrade();' title="<ww:property value='versionTips'/>" style='cursor:pointer'><i class="fa fa-hand-o-up fa-fw"></i>升级</a>
               </ww:if>
               <a onclick='openView("COS软件介绍", "intro");' style='cursor:pointer'><i class="fa fa-info fa-fw"></i>查看介绍</a>
               <a onclick='hiddenBooter();' style='cursor:pointer'><i class="fa fa-arrow-down fa-fw"></i>隐藏</a>
            </p>
            <p>Copyright<i class="fa fa-copyright fa-fw"></i>focusnt.inc 2009-2016 All Rights Reserved
             <i class="fa fa-gavel fa-fw"></i>当使用软件，代表您已接受了《<a onclick='openView("COS软件许可及服务协议", "privacy");'>COS软件许可及服务协议</a>》	
            </p>
        </footer>
    </div>
</body>
<!-- /#wrapper -->
<!-- Core Scripts - Include with every page -->
<script src="skin/defone/js/jquery-2.1.4.min.js"></script>
<script src="skit/js/jquery.md5.js"></script>
<!-- Classie Plugin -->
<script src="skin/defone/js/classie.js"></script>
<!-- SlimScroll -->
<script src="skin/defone/js/jquery.slimscroll.min.js"></script>
<!-- Javascript Cookies -->
<script src="skin/defone/js/jquery.cookie.js"></script>

<script type="text/javascript">
	function hiddenBooter()
	{
        $('footer').removeClass('fixed-footer');
        $('#page-wrapper').removeClass('m-b-60')
		document.getElementById("fixed_footer").checked = false;
	}
    var primaryColor = '#303641',
        dangerColor = '#F22613',
        successColor = '#2ecc71',
        warningColor = '#F5AB35',
        infoColor = '#3498db',
        inverseColor = '#111',
        cursorColor = ( $.cookie('cursorColor') ) ? $.cookie('cursorColor') : '#333';
    
            $.cookie('dev', false);
    // Setting URL 
    //var themeStyle = ( $.cookie('themeStyle') ) ? $.cookie('themeStyle') : 'default';
    //var style_color_css = 'skin/defone/css/'+themeStyle+'.css';
    //$("#style_color").attr('href', style_color_css);
</script>


<!-- jQuery easing | Script -->
<script src="skin/defone/js/jquery.easing.min.js"></script>

<!-- Bootstrap minimal -->
<script src="skin/defone/js/bootstrap.min.js"></script>
<!-- Sparkline | Script -->
<script src="skin/defone/js/jquery.sparkline.js"></script>
<!-- Easy Pie Charts | Script -->
<script src="skin/defone/js/jquery.easypiechart.min.js"></script>
<!-- Date Range Picker | Script -->
<!--<script src="skin/defone/js/moment.min.js"></script
<script src="skin/defone/js/daterangepicker.js"></script> -->
<!-- BlockUI for reloading panels and widgets -->
<script src="skin/defone/js/jquery.blockui.js"></script>
<script src="skin/defone/js/jquery-ui.custom.min.js"></script>
<!-- 
<script src="skin/defone/js/jquery.alerts.js"></script>
 -->
<script src="skin/defone/js/holder.js"></script>
<script src="skin/defone/js/jquery.metisMenu.js"></script>
<!-- Page-Level Plugin Scripts - Dashboard -->
<script src="skin/defone/js/raphael-2.1.0.min.js"></script>
<script src="skin/defone/js/jquery.flot.js"></script>
<script src="skin/defone/js/jquery.flot.tooltip.min.js"></script>
<script src="skin/defone/js/jquery.flot.resize.js"></script>
<script src="skin/defone/js/jquery.nicescroll.min.js"></script> 
<!-- Notify.js - Desktop Notifications -->
<script src="skin/defone/js/notify.js"></script>
<!-- Init Scripts - Include with every page -->
<script src="skin/defone/js/init.js"></script>
<script src="skin/defone/js/default.js"></script>
<!-- tour -->
<script src="skin/defone/js/bootstrap-tour.min.js"></script>
<!-- 以下是本地脚本 -->
<script type="text/javascript">
var lastLogin = <ww:property value='timestamp'/>;
var account = "<ww:property value='userAccount'/>";//登录用户账号
var sysadmin = <ww:property value='sysadmin'/>;
var themeStyle = ( $.cookie('themeStyle') ) ? $.cookie('themeStyle') : "<ww:property value='defaultView'/>";
DescktopMgr.getUserActionMemory(account, {	
	callback:function(json) {
		try
		{
			if( json && json != "" )
			{
				userActionMemory = jQuery.parseJSON(json);
				//showJson(userActionMemory, "aaa", 1024, 480);
				/*var js = DescktopMgr.getUserActionMemory+"";
				editJavascript(js, "bbb", 1024, 480, function(){
	        			alert(1);
	        		}, function(){
	        			alert(2);
        		});*/
			}
		}
		catch(e)
		{
			alert("初始化用户操作行为数据失败("+e+"): "+json);			
		}
		window.setTimeout("handleDescktopTips()",1000);//1秒执行第一次系统消息检查
		if( userActionMemory ) tour();
		else userActionMemory = new Object();
	},
	timeout:10000,
	errorHandler:function(message) { 
	}
});
var tsNotify = 0;
var countNotifes = 0;//系统消息的计数器
<ww:if test="user==null">
window.location.reload();
</ww:if>
<ww:else>tsNotify = <ww:property value='userLastLoginTimestamp'/>;</ww:else>
//调整窗口的位置
var _windowWidth = 0;
var _windowHeight = 0;
var panelBorderWidth = 0;//视图边框宽度
var panelBorderHeight = 0;//视图边框宽度
function resizeWindow()
{
	try
	{
		var args = resizeWindow.arguments;
		var id = args.length>0?args[0]:curMenuItemId;
		var iPanel = document.getElementById(id);
		_windowWidth = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
		_windowHeight = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
		//alert("onresize:"+_windowWidth+","+_windowHeight);
		//alert(_windowHeight);
		//alert("1："+iPanel.clientHeight);
		var h0 = document.getElementById("navbar").clientHeight;
		//alert("h0="+h0);
		var h1 = document.getElementById("page-header").clientHeight;
		//alert("h1="+h1);
		var h2 = document.getElementById("breadcrumb").clientHeight;
		//alert("h2="+h2);
		var w1 = document.getElementById("menu").clientWidth;
		var h = _windowHeight - h1 - h2 - h0;
		//alert("h="+h);
		iPanel.style.height = h+"px";
		//alert("2："+iPanel.clientHeight);
		h = document.getElementById("chatHeader").clientHeight;
		h += document.getElementById("chatTabs").clientHeight;
		h += document.getElementById("chatTypeMessage").clientHeight;
		h = _windowHeight - h;
		var divChat = document.getElementById("divChat");
		divChat.style.height = h+"px";
	}
	catch (e)
	{
		alert("e="+e);
	}
}
//初始化权限点
var pers = new Object();
var viewPers;
{
	var i = 0;
	<ww:iterator value="permissions" status="loop">
	    i = 0;
		viewPers = new Array();
		<ww:iterator value="actions">
		viewPers[i++] = ["<ww:property value='actionId'/>", <ww:property value='hasPermission'/>];
		</ww:iterator>
		pers["<ww:property value='viewId'/>"] = viewPers;
	</ww:iterator>
}

function openNews(title, href)
{
	openView(title, href);
    document.getElementById("dropdown").className='dropdown';
}
openView("首页","index");
//alert($.getJSON);
//skit_alert("欢迎您回来使用<ww:property value='systemName'/>","登录提醒");
//skit_input("欢迎您回来使用<ww:property value='systemName'/>","请输入");
//skit_confirm("您确定要删除该记录吗？");
//alert('document.domain='+document.domain);
var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
var isOpera = userAgent.indexOf("Opera") > -1;
var isFirefox = userAgent.indexOf("Firefox") > -1;
var isDoNet = navigator.userAgent.indexOf(".NET") != -1;
var isWindows = navigator.userAgent.indexOf("Windows") != -1;
var isCompatible = navigator.userAgent.indexOf("compatible") != -1;
var isMSIE = navigator.userAgent.indexOf("MSIE") != -1;
if( ( isCompatible && isMSIE && !isOpera) ||
    (!isCompatible && isDoNet && isWindows)
  )
{
	window.moveTo(0,0);
	window.resizeTo(screen.availWidth,screen.availHeight);
    window.outerWidth=screen.availWidth;        
    window.outerHeight=screen.availHeight; 
}

function tour()
{
	var showTour = userActionMemory["tour"];
	if( showTour != "1" )
	{
		//showObject(userActionMemory);
		setUserActionMemory("tour", "1");
		var tour = new Tour({
		    steps: [
			{
			    element: "#myaccount",
			    title: "我的账号",
			    content: "修改密码、退出登录等入口在这里。系统管理员拥有缺省功能权限入口，如角色管理、用户管理。开发者手册入口",
			    placement: "bottom"
			}
		    ,
		    {
		        element: "#feview",
		        title: "主界面框架",
		        content: "系统中通过菜单点击打开的页面都在该区域以视图窗口的形式呈现。",
			    placement: "top"
		    }
		    ,
		    {
		        element: "#triggerSidebar",
		        title: "视图窗口书签",
		        content: "点击打开视图窗口书签，在这里你将查看到所有已打开的视图窗口。",
			    placement: "left"
		    }
		    ,
		    {
		        element: "#breadcrumb_0",
		        title: "视图窗口导航",
		        content: "系统打开的视图窗口的位置地址。",
			    placement: "top"
		    }
		    ,
		    {
		        element: ".iframe-menu",
		        title: "视图窗口操作按钮区域",
		        content: "通过该区域按钮展开系统banner、刷新视窗口，以及前进、后退、关闭窗口。",
			    placement: "top"
		    }
		    ,
		    {
		        element: "#dropdown",
		        title: "我的工作",
		        content: "系统消息通知提示列表在这里。",
			    placement: "bottom"
		    }
		    ],
		    container: 'body',
		    //backdrop: true,
		    keyboard: true,
		    storage: false
		});
		// Initialize the tour
		tour.init();
		// Start the tour
		tour.start();
	}
}

function openTourDeveloper(id, title, content)
{
	var triggerSidebar = document.getElementById("triggerSidebar");
	triggerSidebar.click();
	document.getElementById("activeDevelopers").click();
	window.setTimeout("tourDeveloper('"+id+"','"+title+"','"+content+"')",1000);//1秒执行第一次系统消息检查
}

function tourDeveloper(id, title, content)
{
	if( id )
	{
		id = "#"+id;
	}
	else
	{
		id = "#cfg_developers";
	}
	if( !title )
	{
		title = "关于开发者社区";
	}
	if( !content )
	{
		content = "所有涉及开发者功能都在该区域可见。系统超级管理员可以进行相关配置操作。";
	}
	setUserActionMemory("tourDeveloper"+id, "1");
	var tour = new Tour({
	    steps: [{
		    element: id,
		    title: title,
		    content: content,
		    placement: "left"
		}],
	    container: 'body',
	    backdrop: true,
	    keyboard: true,
	    storage: false
	});
	try
	{
		// Initialize the tour
		tour.init();
		// Start the tour
		tour.start();
	}
	catch(e)
	{
		skit_alert(e);
	}
}
function openTourUser(id, title, content)
{
	var triggerUserbar = document.getElementById("triggerUserbar");
	triggerUserbar.click();
	window.setTimeout("tourUser('"+id+"','"+title+"','"+content+"')",1000);//1秒执行第一次系统消息检查
}

function tourUser(id, title, content)
{
	if( id )
	{
		id = "#"+id;
	}
	else
	{
		return;
	}
	setUserActionMemory("tourUser"+id, "1");
	var tour = new Tour({
	    steps: [{
		    element: id,
		    title: title,
		    content: content,
		    placement: "left"
		}],
	    container: 'body',
	    backdrop: true,
	    keyboard: true,
	    storage: false
	});
	try
	{
		// Initialize the tour
		tour.init();
		// Start the tour
		tour.start();
	}
	catch(e)
	{
		skit_alert(e);
	}
}

var disableNotifies = "<ww:property value='sysConfig.getString("DisableNotifies")'/>";
if( "true" == disableNotifies ){
	document.getElementById("ulMywork").style.display = "none";	
}
$(document).ready(function(){
	try
	{
		document.getElementById("Style-"+themeStyle).className = "switch-style style_active";
		$(document).keydown(function(e){
			//var ev = window.event||e;
			if( e.ctrlKey ){
				if(e.keyCode == 83){
		            e.preventDefault();  
		            window.event.returnValue= false;
				}
				if(e.keyCode == 87){
		            e.preventDefault();  
		            window.event.returnValue= false;
				}
			}
		});
	}
	catch(e)
	{
		alert(e);
	}
});

function getNews(){
	var news_url = "<%=Kit.URL_PATH(request)%>p/http://admin.focusnt.com/";
	$.getJSON(news_url+"newss/13448", function(json){
		var node = document.getElementById("task-menu-news");
		var list = json;
		for(var i = 0; i < list.length; i+=1 )
		{
	//alert("for(var i = 0; i < list.length; i+=1 )");
			var n = list[i];
	//alert("var n = list[i]; "+n);
			var html = '';
			if( n.pic != null && n.pic != "" )
			{
				html += '<img src="'+n.pic+'" alt="'+n.title+'" data-src="'+n.pic+'" data-src-retina="'+n.pic+'"class="img-responsive img-circle"width="32">';
			}
			html += '<a href="#" onclick="openNews(\''+n.title+'\', \''+news_url+'news/'+n.id+'\');"><strong>'+n.title+'</strong></a>';
			html += '<span class="pull-right text-muted"><em class="label label-info">'+n.from+'</em></span>';
			var div = document.createElement("div");
			div.innerHTML = html;
			node.appendChild(div);
			div = document.createElement("div");
			div.style.color = "#888";
			div.title = n.keywords;
			div.innerHTML = n.description;
			node.appendChild(div);
		}
	});
}
</script>
</html>