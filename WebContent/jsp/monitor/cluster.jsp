<%@page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/css/costable.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/MonitorMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>

<SCRIPT TYPE="text/javascript">
var zookeeperServersColors = new Object();
var zookeeperServers;
var trIndexSelected = 0;
/*刷新数据*/
function tick()
{
	MonitorMgr.getMonitorServers(<ww:property value='id'/>, {
		callback:function(rsp) {
			if( rsp )
				try
				{
					var table = document.getElementById("tableServers");
					for( var i = 0; i < rsp.objects.length; i++ )
					{
						var server = rsp.objects[i];
						//showObject(server);
						buildMonitor(table, server);
					}
					document.getElementById("spanCount").innerHTML = rsp.objects.length;
					document.getElementById("tdRunStat").innerHTML = rsp.result;
					window.setTimeout("tick()",7000);
				}
				catch(e)
				{
					skit_alert("解析主机集群监控列表出现异常"+e, "异常提示", function(){
						window.setTimeout("tick()",7000);
					});
				}
			else
				window.setTimeout("tick()",7000);
					
		},
		timeout:120000,
		errorHandler:function(message) {
			skit_alert("获取主机集群监控列表出现异常"+e, "异常提示", function(){
				window.setTimeout("tick()",7000);
			});
		}
	});
}
var trServerId = -100;
var trServerIP = "";
var trServerPort = 0;
//构建监控项目
function buildMonitor(table, monitor)
{
	var tr = document.getElementById("tr-"+monitor.id);
	if( tr ){}else
	{
		tr = table.insertRow(table.rows.length-1);
		tr.id = "tr-"+monitor.id;
		tr.onmouseover = new Function("this.style.backgroundColor='<ww:property value='themeColorLight'/>';");
		tr.onmouseout = new Function("this.style.backgroundColor='';");
		tr.onclick = new Function("trServerId="+monitor.id+";trServerIP='"+monitor.ip+"';trServerPort="+monitor.port+";");
		var cell = tr.insertCell(0);
		cell.className = "cell";
		cell.width = 160;
		
		cell = tr.insertCell(1);
		cell.className = "cell";
		
		cell = tr.insertCell(2);
		cell.className = "cell";
		cell.width = 48;
		cell.style.backgroundColor = "#f1f1f1";
		cell.align = 'center';
		cell = tr.insertCell(3);
		cell.className = "cell";
		cell.width = 48;
		cell.align = 'center';
		cell = tr.insertCell(4);
		cell.className = "cell";
		cell.width = 48;
		cell.style.backgroundColor = "#f1f1f1";
		cell.align = 'center';
		cell = tr.insertCell(5);
		cell.className = "cell";
		cell.width = 48;
		cell.align = 'center';
		cell = tr.insertCell(6);
		cell.className = "cell";
		cell.width = 48;
		cell.style.backgroundColor = "#f1f1f1";
		cell.align = 'center';
		cell = tr.insertCell(7);
		cell.className = "cell";
		cell.width = 48;
		cell.align = 'center';
		cell = tr.insertCell(8);
		cell.ondblclick = new Function('openZookeeperServers(this)');
		cell.className = "cell";
		cell.width = 48;
		cell.style.backgroundColor = "#f1f1f1";
		cell.align = 'center';
		cell = tr.insertCell(9);
		cell.className = "cell";
		cell.width = 48;
		cell.align = 'center';
		cell = tr.insertCell(10);
		cell.className = "cell";
		cell.width = 64;
		cell.style.backgroundColor = "#f1f1f1";
		cell.align = 'center';
		cell = tr.insertCell(11);
		cell.style.display = 'none';
		cell = tr.insertCell(12);
		cell.style.display = 'none';
		cell = tr.insertCell(13);
		cell.style.display = 'none';
		cell = tr.insertCell(14);
		cell.style.display = 'none';
		cell = tr.insertCell(15);
		cell.style.display = 'none';
	}
	var ulhtml = document.getElementById("programomt").innerHTML;
	tr.cells[1].style = "color:#cdcdcd;";
	if( monitor.expired )
	{
		tr.cells[0].style.color = "#ff62b0";
		tr.cells[0].innerHTML = 
			"<a href='#'><i class='skit_fa_icon_red fa fa-files-o' style='width:16px;' title='文件资源管理器不可用'></i></a>"+
			"<a href='#'><i class='skit_fa_icon_red fa fa-institution' style='width:16px;' title='Zookeeper管理器不可用'></i></a>"+
			monitor.ip;
		tr.cells[1].innerHTML = "<i class='fa fa-remove'></i>"+monitor.name;
	}
	else
	{
		tr.cells[0].innerHTML = 
			"<a href='javascript:openFiles();'><i class='fa fa-files-o' style='width:16px;' title='打开文件资源管理器'></i></a>"+
			"<a href='javascript:openZookeeperServer();'><i class='fa fa-institution' style='width:16px;' title='打开Zookeeper管理器'></i></a>"+
			monitor.ip;
		tr.cells[1].innerHTML = 
			"<span class='dropdown'>"+
			"<a class='dropdown-toggle' data-toggle='dropdown' title='点击打开菜单操作伺服器相关功能' aria-expanded='false' style='color:#cdcdcd;cursor:pointer;TEXT-DECORATION: none'>"+
			"<i class='fa fa-toggle-down'></i>"+
			monitor.name+"</a>"+ulhtml+"</span>";
	}
//		"<a href='javascript:openView(\"显示主控配置control.xml\",\"omt!viewControlConfig.action?host="+monitor.id+"\");' style='color:#cdcdcd'>"+monitor.name+"</a>";
	var i = 2;
	tr.cells[i++].innerHTML = "<img src='"+monitor.cpuState+"'>"
	tr.cells[i++].innerHTML = "<img src='"+monitor.memoryState+"'>"
	tr.cells[i++].innerHTML = "<img src='"+monitor.diskState+"'>"
	tr.cells[i++].innerHTML = "<img src='"+monitor.netState+"'>"
	tr.cells[i++].innerHTML = "<img src='"+monitor.databaseState+"'>"
	tr.cells[i].style.backgroundImage = "url("+monitor.zookeeperState+")";
	tr.cells[i].style.backgroundPosition = "center center";
	tr.cells[i].style.backgroundRepeat = "no-repeat";
	tr.cells[i].style.fontWeight = "bold";
	tr.cells[i].style.fontSize = "7pt";
	tr.cells[i].align = "center";
	if( monitor.zookeeperServers != "" )
	{
		var color = zookeeperServersColors[monitor.zookeeperMd5];
		if( !color )
		{
			switch(Object.keys(zookeeperServersColors).length)
			{
			case 0:
				color = "#e8ffc6";
				break;
			case 1:
				color = "#ffe3eb";
				break;
			case 2:
				color = "#d0ffff";
				break;
			default:
				color = "#fcd081";
				break;
			}
			zookeeperServersColors[monitor.zookeeperMd5] = color;
		}
		tr.cells[i].style.backgroundColor = color;
	}
	tr.cells[i++].innerHTML = monitor.zookeeperMyid;
	tr.cells[i++].innerHTML = "<img src='"+monitor.coswsState+"'>";
	tr.cells[i++].innerHTML = "<img src='"+monitor.programState+"'>"
	tr.cells[i++].innerHTML = monitor.runState;
	tr.cells[i++].innerHTML = monitor.name;
	tr.cells[i++].innerHTML = monitor.securityKey;
	tr.cells[i++].innerHTML = monitor.zookeeperServers;
	tr.cells[i++].innerHTML = monitor.expired;
	tr.cells[i++].innerHTML = monitor.id;
}

function openZookeeperServers(td)
{
	var host = document.getElementById("host").value;
	if( host == "" )
	{
		skit_alert("请从监控服务器列表选择指定服务器。");
		return;
	}
	var valid = document.getElementById("valid").value;
	if( valid == "false" )
	{
		skit_message(zookeeperServers, "本服务器【"+host+"】主控服务的处于未启动或者异常状态无法打开它的分布式应用协调程序(Zookeeper)管理视图。", 480, 480);
		return;
	}
	if( td && td.style.backgroundImage && td.style.backgroundImage.indexOf("gray.gif") != -1 )
	{
		skit_message(zookeeperServers, "本服务器【"+host+"】的未正常启动不能打开分布式应用协调程序(Zookeeper)管理视图。", 480, 480);
		return;
	}
	var i = host.indexOf(":");
	var ip = host.substring(0, i);
	var port = host.substring(i+1);
	openView("Zookeeper管理器("+host+")","zk!query.action?ip="+ip+"&port="+port);
	if( zookeeperServers == "" )
	{
		skit_alert("本服务器【"+host+"】的分布式应用协调程序(Zookeeper)是单机模式。");
	}
	else
	{
		skit_message(zookeeperServers, "分布式应用协调程序集群", 480, 480);
	}
}

function openSsh(host)
{
	if( host ) { } else
	{
		host = document.getElementById("host").value;
	}
	if( host == "" )
	{
		skit_alert("请从监控服务器列表选择指定服务器。");
		return;
	}
	var valid = document.getElementById("valid").value;
	if( valid == "false" )
	{
		skit_alert("本服务器【"+host+"】主控服务的处于未启动或者异常状态无法打开它的简易SSH管理视图。");
		return;
	}
	openView("SSH>>"+host,"rpc!open.action?host="+host);
}

function openFiles(host)
{
	if( host ) { } else
	{
		host = document.getElementById("host").value;
	}
	if( host == "" )
	{
		skit_alert("请从监控服务器列表选择指定服务器。");
		return;
	}
	var valid = document.getElementById("valid").value;
	if( valid == "false" )
	{
		skit_alert("本服务器【"+host+"】主控服务的处于未启动或者异常状态无法打开它的文件管理器视图。");
		return;
	}
	openView("文件管理器>>"+host,"files!navigate.action?id="+host);
}

</SCRIPT>
</head>
<body onload='tick();'>
<form>
<input type='hidden' name='id' id='id' value=''>
<input type='hidden' name='ip' id='ip' value=''>
<input type='hidden' name='port' id='port' value=''>
<table id="tableServers" class="panel panel-default">
<tr>
 	<td class='head' width='160'>伺服器</td>
 	<td class='head'></td>
	<td width='48' class='head' align='center'>CPU</td>
	<td width='48' class='head' align='center'>内存</td>
	<td width='48' class='head' align='center'>磁盘</td>
	<td width='48' class='head' align='center'>网络</td>
	<td width='48' class='head' align='center'>数据库</td>
	<td width='48' class='head' align='center' title='分布式应用协调程序（Zookeeper）'>ZK</td>
	<td width='48' class='head' align='center' title='云架构开放式应用服务框架接口可用性监测'>接口</td>
	<td width='48' class='head' align='center'>程序</td>
	<td width='64' class='head' align='center'>运行/配置</td>
</tr>
<tr class='unline'><td style='font-weight:bold' colspan='2'>服务器合计  <span id='spanCount'></span> 台</td>
	<td align='right' style='color:gray' colspan='8'>合计运行程序</td>
	<td align='center' style='font-weight:bold' id='tdRunStat'></td>
</tr>
</table>
<div style='display: none' id='programomt'>
<ul class="dropdown-menu pull-right" style='cursor:pointer'>
<ww:if test='sysadmin'>
<li><a onclick='configControl()'><i class='skit_fa_toolbar_war fa fa-cog' style='width:16px;'></i> 配置伺服器主控程序</a></li>
<li><a onclick='configService()'><i class='skit_fa_toolbar_war fa fa-plus-square' style='width:16px;'></i> 新增伺服器程序</a></li>
<li class='divider'></li>
<li><a onclick='restartup()'><i class='skit_fa_toolbar_red fa fa-repeat' style='width:16px;'></i> 重启伺服器主控程序</a></li>
<li><a onclick='suspend()'><i class='skit_fa_toolbar_red fa fa-minus-circle' style='width:16px;'></i> 暂停伺服器主控程序</a></li>
<li><a onclick='resetMonitor()'><i class='skit_fa_toolbar fa fa-refresh fa-fw' style='width:16px;'></i> 重置伺服器监控</a></li>
<li class='divider'></li>
</ww:if>
<li><a onclick='openFiles()'><i class='skit_fa_toolbar fa fa-folder-open' style='width:16px;'></i> 打开文件管理器</a></li>
<li><a onclick='openZookeeperServer()'><i class='skit_fa_toolbar fa fa-institution fa-fw' style='width:16px;'></i> 打开Zookeeper管理器</a></li>
<li><a onclick='viewZookeeperClusterInfo()'><i class='skit_fa_toolbar fa fa-institution fa-fw' style='width:16px;'></i> 查看Zookeeper集群信息</a></li>
<ww:if test='sysadmin'>
<li><a onclick='viewMonitorRunnerInfo()'><i class='skit_fa_toolbar fa fa-eye fa-fw' style='width:16px;'></i> 查看监控运行信息</a></li>
</ww:if>
<li><a onclick='viewServerLoadInfo()'><i class='skit_fa_toolbar fa fa-line-chart fa-fw' style='width:16px;'></i> 查看服务器负载详情</a></li>
<li><a onclick='wrapperlog()'><i class='skit_fa_toolbar fa fa-file-word-o fa-fw' style='width:16px;'></i> 查看主控日志</a></li>
</ul>
</div>
<iframe name="downloadframe" id="downloadframe" style="display:none;border:1px solid red"></iframe>
</form>
</body>
<SCRIPT type="text/javascript">
function monitordatabase()
{
	if( trServerId <= -1 )
	{
		skit_alert("请选择你要操作的伺服器。");
		return;
	}
	setUserActionMemory("monitorcfg!databases.server", trServerId);
	openView("JDBC数据库监控配置: "+trServerIP, "monitorcfg!databases.action");
}

function configControl()
{
	if( trServerId <= -1 )
	{
		skit_alert("请选择你要操作的伺服器。");
		return;
	}
	openView("配置伺服器主控程序: "+trServerIP, "control!configxml.action?ip="+trServerIP+"&port="+trServerPort);
}

function configService()
{
	if( trServerId <= -1 )
	{
		skit_alert("请选择你要操作的伺服器。");
		return;
	}
	openView("配置伺服器主控程序: "+trServerIP, "control!configxml.action?ip="+trServerIP+"&port="+trServerPort);
}

//重新加载监控
function resetMonitor()
{
	if( trServerId <= -1 )
	{
		skit_alert("请选择你要操作的伺服器。");
		return;
	}
	MonitorMgr.resetMonitor(trServerId,{
		callback:function(result) {
			if( response.succeed )
			{
			}
			else
			{
				skit_alert(response.message);
			}
		},
		timeout:120000,
		errorHandler:function(message) {}
	});
}

function restartup()
{
	if( trServerId <= -1 )
	{
		skit_alert("请选择你要操作的伺服器。");
		return;
	}
	var tip = "您确定重启该主机("+trServerIP+")的主控服务吗?";
	document.getElementById( "id" ).value = trServerId;
	document.getElementById( 'ip' ).value = trServerIP;
	document.getElementById( 'port' ).value = trServerPort;
	skit_confirm(tip, function(yes){
		if( yes )
		{
			document.forms[0].action = "control!restartup.action";
			document.forms[0].target = "downloadframe";
			document.forms[0].submit();
		}
	});
}
//暂停所有服务
function suspend()
{
	if( trServerId <= -1 )
	{
		skit_alert("请选择你要操作的伺服器。");
		return;
	}
	var tip = "您确定暂停该主机("+trServerIP+")的主控服务下所有程序引擎吗?";
	document.getElementById( "id" ).value = trServerId;
	document.getElementById( 'ip' ).value = trServerIP;
	document.getElementById( 'port' ).value = trServerPort;
	skit_confirm(tip, function(yes){
		if( yes )
		{
			document.forms[0].action = "control!suspend.action";
			document.forms[0].target = "downloadframe";
			document.forms[0].submit();
		}
	});
}

var viewMonitorRunnerInfoOpened = false;
function viewMonitorRunnerInfo()
{
	if( viewMonitorRunnerInfoOpened ) return;
	if( trServerId <= -1 )
	{
		skit_alert("请选择你要操作的伺服器。");
		return;
	}
	MonitorMgr.getMonitorRunnerInfo(trServerIP, trServerPort,{
		callback:function(result) {
			skit_message(result, "运行监控信息", 480, 480, function(yes){
				viewMonitorRunnerInfoOpened = false;
			});
		},
		timeout:120000,
		errorHandler:function(message) {}
	});
}

//查看wrapper日志
function wrapperlog()
{
	if( trServerId <= -1 )
	{
		skit_alert("请选择你要操作的伺服器。");
		return;
	}
	skit_confirm("预览伺服器【"+trServerIP+"】的主控wrapper日志，您是否压缩该文件？", function(yes){
		if( yes )
		{
			document.forms[0].path.value = "../log/wrapper.log";
			document.forms[0].action = "files!download.action";
			document.forms[0].method = "POST";
			document.forms[0].target = "downloadframe";
			document.forms[0].submit();
		}
		else
		{
			openView(trServerIP+">>wrapper.log","files!showlog.action?path=../log/wrapper.log&ip="+trServerIP+"&port="+trServerPort);
		}
	});
}

function viewServerLoadInfo()
{
	if( trServerId <= -1 )
	{
		skit_alert("请选择你要操作的伺服器。");
		return;
	}
	openView("查看服务器负载详情: "+trServerId, "monitorload!serverchart.action?id="+trServerId);
}

function openFiles()
{
	if( trServerId <= -1 )
	{
		skit_alert("请选择你要操作的伺服器。");
		return;
	}
	setUserActionMemory("files!navigate.server", trServerId);
	openView("集群文件管理器: "+trServerIP, "files!navigate.action?id=<ww:property value='id'/>");
}
function openZookeeperServer()
{
	if( trServerId <= -1 )
	{
		skit_alert("请选择你要操作的伺服器。");
		return;
	}
	setUserActionMemory("zookeeper!navigate.server", trServerId);
	openView("分布式协调服务(Zookeeper)集群管理器: "+trServerIP, "zookeeper!navigate.action?id=<ww:property value='id'/>");
}

function viewZookeeperClusterInfo()
{
	if( trServerId <= -1 )
	{
		skit_alert("请选择你要操作的伺服器。");
		return;
	}
	MonitorMgr.getZookeeperConfig(trServerId, {
		callback:function(response){
			if( response.succeed )
			{//表示正在执行升级
				if( response.result == "" )
				{
					skit_alert("该伺服器【"+trServerIP+"】的分布式应用协调程序(Zookeeper)是单机模式。");
				}
				else
				{
					skit_message(response.result, "该伺服器【"+trServerIP+"】的分布式应用协调程序(Zookeeper)是集群模式", 480, 480);
				}
			}
			else
			{
				skit_alert(response.message);
			}
		},
		timeout:10000,
		errorHandler:function(message) {skit_alert("获取分布式应用协调程序(Zookeeper)的模式信息出现异常："+message);}
	});
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<script src="skin/defone/js/bootstrap.js"></script>
</html>