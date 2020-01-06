<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/MonitorMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body>
<form>
<input type='hidden' name='id' id='id' value=''>
<input type='hidden' name='ip' id='ip'>
<input type='hidden' name='port' id='port'>
<input type='hidden' name='path' id='path'>
<TABLE style='width:100%;height:100%;margin-top:3px'>
<TR><TD width='250'>
        <div class="panel panel-default" style='border: 1px solid #aaaaaa'>
   			<div class="panel-heading"><i class='skit_fa_icon fa fa-bars'></i>集群伺服器导航树
   				<ww:if test='sysadmin'>
                <div style='float:right;right:4px;top:0px;' id='divAdd'>
 				<button type="button" class='btn btn-outline btn-primary btn-xs' onclick='addCluster(true)'>
 					<i class='fa fa-plus-circle'></i> 新增</button>
                </div>
                </ww:if>
   			</div>
   			<div class="panel-body" style='padding: 0px;'>
   				<div class="input-group custom-search-form" style='padding:3px;'>
                    <input class="form-control" placeholder="输入关键字找伺服器" id='searchBar' onkeyup='return searchServer(this);' type="text" style='font-size: 12px;height:28px;'>
                    <span class="input-group-btn">
                        <button class="btn btn-inverse" type="button" onclick='searchServer()'>
                            <i class="fa fa-search"></i>
                        </button>
                    </span>
                </div>
                <div id='divTree'>
					<ul id='myZtree' class='ztree'></ul>
					<div id="rMenu">
						<ul>
							<li onclick="addCluster();" id='liAddCluster'><i class='skit_fa_icon fa fa-plus-circle'></i> 新增集群</li>
							<li onclick="delCluster(true);" id='liDelCluster'><i class='skit_fa_icon_red fa fa-remove'></i> 删除集群与伺服器</li>
							<li onclick="delCluster(false);" id='liRmvCluster'><i class='skit_fa_icon_war fa fa-remove'></i> 移除集群留下伺服器</li>
							<li onclick="addServer();" id='liAddServer'><i class='skit_fa_icon fa fa-plus-circle'></i> 新增伺服器监控</li>
							<li onclick="delServer();" id='liDelServer'><i class='skit_fa_icon_red fa fa-remove'></i> 移除伺服器监控</li>
							<li id='menuServer1' class="divider"></li>
							<ww:if test='sysadmin'>
							<li id='menuServer01' onclick='configPrograms()'><i class='skit_fa_icon_war fa fa-windows' style='width:16px;'></i> 伺服器程序管理</li>
							<li id='menuServer02' onclick='configcontrol()'><i class='skit_fa_icon_blue fa fa-cogs fa-fw' style='width:16px;'></i> 修改主控配置文件
							<li id='menuServer03' onclick='monitordatabase()'><i class='skit_fa_icon_blue fa fa-database fa-fw' style='width:16px;'></i> 数据库监控配置
							<li id='menuServer1' class='divider'></li>
							<li id='menuServer11' onclick='restartup()'><i class='skit_fa_icon_red fa fa-repeat' style='width:16px;'></i> 重启伺服器主控引擎</li>
							<li id='menuServer12' onclick='suspend()'><i class='skit_fa_icon_red fa fa-minus-circle' style='width:16px;'></i> 暂停伺服器所有程序</li>
							<li id='menuServer34' onclick='resetMonitor()'><i class='skit_fa_icon fa fa-refresh fa-fw' style='width:16px;'></i> 重置伺服器监控</li>
							<li id='menuServer11' onclick='upgrade()'><i class='skit_fa_icon_blue fa fa-cloud-download' style='width:16px;'></i> 升级伺服器主控程序</li>
							<li id='menuServer2' class='divider'></li>
							</ww:if>
							<li id='menuServer31' onclick='openFiles()'><i class='skit_fa_icon fa fa-folder-open' style='width:16px;'></i> 打开文件管理器</li>
							<li id='menuServer32' onclick='openZookeeperServer()'><i class='skit_fa_icon_blue fa fa-institution fa-fw' style='width:16px;'></i> 打开Zookeeper管理器</li>
							<li id='menuServer39' onclick='ssh()'><i class='skit_fa_icon_war fa fa-terminal fa-fw' style='width:16px;'></i> 打开安全远程控制SSH</li>
							<li id='menuServer3' class='divider'></li>
							<li id='menuServer36' onclick='viewServerLoadInfo()'><i class='skit_fa_icon fa fa-line-chart fa-fw' style='width:16px;'></i> 查看服务器负载详情</li>
							<ww:if test='sysadmin'>
							<li id='menuServer33' onclick='viewZookeeperClusterInfo()'><i class='skit_fa_icon fa fa-institution fa-fw' style='width:16px;'></i> 查看Zookeeper集群信息</li>
							<li id='menuServer35' onclick='viewMonitorRunnerInfo()'><i class='skit_fa_icon fa fa-eye fa-fw' style='width:16px;'></i> 查看监控运行信息</li>
							<li id='menuServer37' onclick='wrapperlog()'><i class='skit_fa_icon fa fa-file-code-o fa-fw' style='width:16px;'></i> 查看主控日志</li>
							<li id='menuServer38' onclick='wrapperlogx()'><i class='skit_fa_icon fa fa-warning fa-fw' style='width:16px;'></i> 查看主控错误日志</li>
							</ww:if>
						</ul>
					</div>
				</div>
   			</div>
   		</div>	
	</TD>
	<TD style='width:3px;'/>
	<TD valign='top'>
        <div class="panel panel-default" style='border: 1px solid #aaaaaa' id='divMonitor'>
   			<div class="panel-heading"><span><i class='skit_fa_icon fa fa-server'></i> <span id='headTitle'>集群伺服器监控</span></span>
				<span class='dropdown' style='float: right;cursor:pointer;display:none' id='spanServerMgr'>
				  <a href='#'
					onmouseover='this.style.color="#000000";this.style.fontWeight="bold";'
					onmouseout='this.style.color="#566B52";this.style.fontWeight="normal";' 
					class='dropdown-toggle' 
					data-toggle='dropdown' 
					aria-expanded='false' 
					style='color:#566B52;TEXT-DECORATION: none'>
					<i class='skit_fa_icon fa fa-android'></i>服务器管理</a>
					<ul class="dropdown-menu pull-right">
					<ww:if test='sysadmin'>
					<li onclick='configPrograms()'><a><i class='skit_fa_icon_war fa fa-windows' style='width:16px;'></i> 伺服器程序管理</a></li>
					<li onclick='configcontrol()'><a><i class='skit_fa_icon_blue fa fa-cogs fa-fw' style='width:16px;'></i> 修改主控配置文件</a>
					<li onclick='monitordatabase()'><a><i class='skit_fa_icon_blue fa fa-database fa-fw' style='width:16px;'></i> 数据库监控配置</a>
					<li class='divider'></li>
					<li onclick='restartup()'><a><i class='skit_fa_icon_red fa fa-repeat' style='width:16px;'></i> 重启伺服器主控引擎</a></li>
					<li onclick='suspend()'><a><i class='skit_fa_icon_red fa fa-minus-circle' style='width:16px;'></i> 暂停伺服器所有程序</a></li>
					<li onclick='resetMonitor()'><a><i class='skit_fa_icon fa fa-refresh fa-fw' style='width:16px;'></i> 重置伺服器监控</a></li>
					<li onclick='upgrade()'><a><i class='skit_fa_icon_blue fa fa-cloud-download' style='width:16px;'></i> 升级伺服器主控程序</a></li>
					<!-- <li><a onclick='close()'><i class='skit_fa_icon_red fa fa-ban' style='width:16px;'></i> <ww:text name="label.ema.button.close"/></a></li> -->
					<li class='divider'></li>
					</ww:if>
					<li onclick='openFiles()'><a><i class='skit_fa_icon fa fa-folder-open' style='width:16px;'></i> 打开文件管理器</a></li>
					<li onclick='openZookeeperServer()'><a><i class='skit_fa_icon_blue fa fa-institution fa-fw' style='width:16px;'></i> 打开Zookeeper管理器</a></li>
					<li onclick='ssh()' id='liSsh'><a><i class='skit_fa_icon_war fa fa-terminal' style='width:16px;'></i> 打开安全远程控制SSH</a></li>
					<li class='divider'></li>
					<li onclick='viewServerLoadInfo()'><a><i class='skit_fa_icon fa fa-line-chart fa-fw' style='width:16px;'></i> 查看服务器负载详情</a></li>
					<li onclick='viewNetstatInfo()'><a><i class='skit_fa_icon_red fa fa-snowflake-o fa-fw' style='width:16px;'></i> 查看服务器端口使用情况</a></li>
					<ww:if test='sysadmin'>
					<li onclick='viewZookeeperClusterInfo()'><a><i class='skit_fa_icon fa fa-institution fa-fw' style='width:16px;'></i> 查看Zookeeper集群信息</a></li>
					<li onclick='viewMonitorRunnerInfo()'><a><i class='skit_fa_icon fa fa-eye fa-fw' style='width:16px;'></i> 查看监控运行信息</a></li>
					<li onclick='wrapperlog()'><a><i class='skit_fa_icon fa fa-file-code-o fa-fw' style='width:16px;'></i> 查看主控日志</a></li>
					<li onclick='wrapperlogx()'><a><i class='skit_fa_icon fa fa-warning fa-fw' style='width:16px;'></i> 查看主控错误日志</a></li>
					</ww:if>
					</ul>
				</span>   
				<span class='dropdown' style='float: right;cursor:pointer' id='spanClusterMgr'><a href='#'
					onmouseover='this.style.color="#000000";this.style.fontWeight="bold";'
					onmouseout='this.style.color="#566B52";this.style.fontWeight="normal";' 
					class='dropdown-toggle' 
					data-toggle='dropdown' 
					aria-expanded='false' 
					style='color:#566B52;TEXT-DECORATION: none'>
					<i class='skit_fa_icon fa fa-server'></i>集群管理</a>
					<ul class="dropdown-menu pull-right">
					<li onclick='listCluster();'><a><i class='skit_fa_icon_blue fa fa-bars fa-fw'></i> 集群监控列表</a></li>
					<li class='divider'></li>
					<li onclick='openMonitorload("cluster");'><a><i class='skit_fa_icon_blue fa fa-list-ol fa-fw'></i> 服务器负载监控</a></li>
					<li onclick='openMonitorload("clusterchart");'><a><i class='skit_fa_icon_blue fa fa-line-chart fa-fw'></i> 服务器负载跟踪图表</a></li>
					<ww:if test='sysadmin'>
					<li onclick='openMonitorload("runnerflow");'><a><i class='skit_fa_icon_blue fa fa-stack-overflow fa-fw'></i> 监控器流量负载跟踪</a></li>
					</ww:if>
					<li class='divider'></li>
					<li onclick='openZookeeperNavigate()'><a><i class='skit_fa_icon fa fa-institution fa-fw'></i> 打开集群Zookeeper管理器</a></li>
					<li onclick='openFilesNavigate()'><a><i class='skit_fa_icon fa fa-folder-open'></i> 打开集群文件管理器</a></li>
					<li onclick='getClusterStatesInfo()'><a><i class='skit_fa_icon fa fa-eye'></i> 查看伺服器状态明细</a></li>
					</ul>
				</span>   							
	        </div>
   			<div class="panel-body" style='padding: 0px;'>
   				<iframe name='iMonitor' id='iMonitor' style='width:100%;height:<ww:property value='wh-50'/>px;border:0px;'></iframe>
				<iframe name="downloadframe" id="downloadframe" style="display:none;border:1px solid red"></iframe>
			</div>
		</div>
	</TD>
</TR>
</TABLE>
</form>
</body>
<SCRIPT type="text/javascript">
var sysadmin = <ww:property value='sysadmin'/>;
var rootid = -1;//<ww:property value='rootid'/>;
if( "<ww:property value='rootid'/>" ){
	rootid = Number(<ww:property value='rootid'/>);
}
var grant = <ww:property value='grant'/>;
function openZookeeperNavigate()
{
	var nodes = myZtree.getSelectedNodes();
	if( nodes == null || nodes.length == 0 ) return;
	var node = nodes[0];
	openView("分布式协调服务(Zookeeper)集群管理器: "+node.name,"zookeeper!navigate.action?id="+node.id);
}
function openFilesNavigate()
{
	var nodes = myZtree.getSelectedNodes();
	if( nodes == null || nodes.length == 0 ) return;
	var node = nodes[0];
	openView("集群文件管理器: "+node.name,"files!navigate.action?id="+node.id);
}

function openMonitorload(tag)
{
	var nodes = myZtree.getSelectedNodes();
	if( nodes == null || nodes.length == 0 ) return;
	var node = nodes[0];
	if( tag == "cluster" )
	{
		openView("集群伺服器负载监控: "+node.name,"monitorload!cluster.action?id="+node.id);
	}
	else if( tag == "clusterchart" )
	{
		openView("集群伺服器负载跟踪图表: "+node.name,"monitorload!clusterchart.action?id="+node.id);
	}
	else if( tag == "runnerflow" )
	{
		openView("集群伺服器监控器流量负载跟踪: "+node.name, "monitorload!runnerflow.action?id="+node.id);
	}
}
function resizeWindow()
{
	var div = document.getElementById( 'divTree' );
	div.style.width = 248;
	div.style.height = windowHeight - 76;
	div = document.getElementById( 'iMonitor' );
	div.style.height = windowHeight - 42;
}
</SCRIPT>

<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<SCRIPT type="text/javascript">
$( '#divTree' ).niceScroll({
	cursorcolor: '<%=(String)session.getAttribute("System-Theme")%>',
	railalign: 'right',
	cursorborder: "none",
	horizrailenabled: true, 
	zindex: 2001,
	left: '245px',
	cursoropacitymax: 0.6,
	cursorborderradius: "0px",
	spacebarenabled: false 
});
var timestamp = <ww:property value='timestamp'/>;
//########################################################################
	var setting = {
		//editable: false,
		//fontCss : {color:"red"},
		check: {
			enable: false
		},
		callback: {
			onRightClick: onRightClick,
			onClick: onClick,
			beforeRename: beforeRename,
			beforeRemove: beforeRemove,
			beforeDrag: sysadmin?beforeDrag:false,
			beforeDrop: beforeDrop,
			beforeDragOpen: beforeDragOpen,
			onDrag: onDrag,
			onDrop: onDrop,
			onCollapse: onCollapse,
			onExpand: onExpand,
			onRename: onRename
		},
		edit: {
			drag: {
				autoExpandTrigger: true,
				prev: dropPrev,
				inner: dropInner,
				next: dropNext
			},
			enable: sysadmin,
			showRemoveBtn: showRemoveBtn,
			showRenameBtn: showRenameBtn
		},
		view: {
			addDiyDom: addDiyDom,
			showTitle: showTitle
		},
		data: {
			key: {
				title: "title"
			}
		}
	};
	function onCollapse(event, treeId, treeNode) 
	{
		expandMemory[treeNode.id] = false;
		saveExpandMemory("monitor!navigate.open");
	}
	
	function onExpand(event, treeId, treeNode) 
	{
		expandMemory[treeNode.id] = true;
		saveExpandMemory("monitor!navigate.open");
	}
	
	function beforeDrop(treeId, treeNodes, targetNode, moveType, isCopy) {
		if( targetNode.nomenu && moveType != "inner"  ) return false;
		return true;
	}
	
	function beforeRemove(e, treeId, treeNode)
	{
		var node = myZtree.getSelectedNodes()[0];
		if( node.newMenu )
		{
			return true;
		}
		delMenu();
		return false;
	}
	
	function onRightClick(event, treeId, treeNode) {
		if( treeNode.isParent && sysadmin)
		{
			document.getElementById( 'liAddCluster' ).style.display = "";
			document.getElementById( 'liDelCluster' ).style.display = "";
			document.getElementById( 'liRmvCluster' ).style.display = "";
			if( treeNode.id == 0 )
			{
				document.getElementById( 'liDelCluster' ).style.display = "none";
				document.getElementById( 'liRmvCluster' ).style.display = "none";
			}
			document.getElementById( 'liAddServer' ).style.display = "";
			document.getElementById( 'liDelServer' ).style.display = "none";
			var liMenuServers = $("li[id^='menuServer']");
			for(var i = 0; i < liMenuServers.length; i+=1 )
			{
				var li = liMenuServers[i];
				if( li.hide ) li.hide();
				else li.style.display = "none";
			}
		}
		else
		{
			document.getElementById( 'liAddCluster' ).style.display = "none";
			document.getElementById( 'liDelCluster' ).style.display = "none";
			document.getElementById( 'liRmvCluster' ).style.display = "none";
			document.getElementById( 'liAddServer' ).style.display = "none";
			document.getElementById( 'liDelServer' ).style.display = treeNode.id==-1?"none":(sysadmin?"":"none");
			var liMenuServers = $("li[id^='menuServer']");
			for(var i = 0; i < liMenuServers.length; i+=1 )
			{
				var li = liMenuServers[i];
				if( li.hide ) li.hide();
				else li.style.display = "";
			}
			document.getElementById( 'menuServer39' ).style.display = "none";
			if(treeNode.icon.indexOf("ssh") != -1 ){
				document.getElementById( 'menuServer39' ).style.display = "";
			}
		}
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
	}

	function onClick(event, treeId, treeNode)
	{
		myZtree.expandNode(treeNode, true);
		//selectTreeNode(treeNode);//myZtree.getSelectedNodes()[0];
		document.getElementById( 'liSsh' ).style.display = "none";
		if(treeNode.icon && treeNode.icon.indexOf("ssh") != -1 ){
			document.getElementById( 'liSsh' ).style.display = "";
		}
		open(treeNode);
	}

	function showTitle(treeId, treeNode) {
		return treeNode.ip?true:false;
	}
	
	function showRenameBtn(treeId, treeNode) {
		if( !sysadmin ) return false;
		return treeNode.id>0&&(treeNode.ip?false:true);
	}
	
	function showRemoveBtn(treeId, treeNode){
		return false;
	}
	
	function addDiyDom(treeId, treeNode)
	{
		setClusterState(treeNode);
	}

	var _oldName;
	function beforeRename(treeId, treeNode, newName, isCancel) {
		if (newName.length == 0) {
			myZtree.cancelEditName();
			skit_alert("集群分组名称不能为空.");
			return false;
		}
		else if (newName.length<2||newName.length>10) {
			myZtree.cancelEditName();
			skit_alert("集群分组名称不能超过10个字.");
			return false;
		}
		_oldName = treeNode.name;
		return true;
	}
	
	function onRename(e, treeId, treeNode, isCancel) 
	{
		if( isCancel || _oldName == treeNode.name )
		{
			return;
		}
		skit_confirm("您确定要将集群分组名称从【"+_oldName+"】改为【"+treeNode.name+"】", function(yes){
			if( yes )
			{
				skit_showLoading();
				MonitorMgr.renameCluster(treeNode.id, treeNode.name, timestamp, {
					callback:function(rsp) {
						skit_hiddenLoading();
						skit_alert(rsp.message);
						try
						{
							if( !rsp.succeed )
							{
								treeNode.name = _oldName;
							}
							myZtree.updateNode (treeNode);
						}
						catch(e)
						{
							alert(e);
						}
					},
					timeout:30000,
					errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
				});
			}
			else
			{
				treeNode.name = _oldName;
				myZtree.updateNode (treeNode);
			}
		});
	}

	$(document).ready(function(){
		try
		{
			var zNodes = <ww:property value="jsonData" escape="false"/>;
			initZtree(zNodes);
			window.setTimeout("getClusterStates()",15000);
		}
		catch(e)
		{
			skit_alert("初始化集群伺服导航出现异常: "+e.message+", 行数"+e.lineNumber);
		}
	});
//#########################################################################
function initZtree(zNodes)
{
	var memory = getUserActionMemory("monitor!navigate.open");
	if( memory && memory != "" )
	{
		var args = memory.split(",");//用空格分隔，取出需要收起来的树节点id
		for( var i = 0; i < args.length; i++ )
		{//遍历扫描，收起所有记忆的节点
			var id = args[i];//tr的id
			expandMemory[id] = true;
		}
		setNodeMemoryOpen(zNodes, expandMemory);
	}
	$.fn.zTree.init($("#myZtree"), setting, zNodes);
	myZtree = $.fn.zTree.getZTreeObj("myZtree");
	myZtreeMenu = $("#rMenu");
	var clusterid = getUserActionMemory("monitor!navigate.cluster");
	if( !clusterid && clusterid != 0 )
	{
		clusterid = 0;
	}
	var node = myZtree.getNodeByParam("id", clusterid);
	if( node )
	{
		myZtree.selectNode(node);
		open(node);
	}
}
function listCluster()
{
	var nodes = myZtree.getSelectedNodes();
	open(nodes[0]);
}

function open(node)
{
	if( node )
	{
		document.getElementById( 'id' ).value = node.id;
		if( node.ip ){
			document.getElementById( 'headTitle' ).innerHTML = "集群伺服器监控： "+node.name+" "+node.title; 
			document.forms[0].action = "monitor!server.action";
			document.getElementById( 'spanServerMgr' ).style.display = "";
			document.getElementById( 'spanClusterMgr' ).style.display = "none";
		}
		else{
			document.getElementById( 'headTitle' ).innerHTML = "集群监控： "+node.name; 
			document.forms[0].action = "monitor!cluster.action";
			document.getElementById( 'spanServerMgr' ).style.display = "none";
			document.getElementById( 'spanClusterMgr' ).style.display = "";
		}
		document.forms[0].method = "POST";
		document.forms[0].target = "iMonitor";
		document.forms[0].submit();
		setUserActionMemory("monitor!navigate.cluster", node.id);
	}
	else skit_alert("打开了未知节点.");
}

function delServer()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( nodes == null || nodes.length == 0 ) return;
	var node = nodes[0];
	var parentNode = node.getParentNode();
	
	var tips = "你确定要移除伺服器【"+node.ip+":"+node.port+"】？";
	skit_confirm(tips, function(yes){
		if( yes )
		{
			MonitorMgr.delServer(node.id, rootid, timestamp,{
				callback:function(response){
					if( response.succeed )
					{
						myZtree.removeNode(node);
						if( parentNode )
						{
							parentNode["isParent"] = true;
							myZtree.updateNode(parentNode);
							myZtree.selectNode(parentNode);
							open(parentNode);
						}
					}
					else skit_alert(response.message);
				},
				timeout:1000000,
				errorHandler:function(message) {
					skit_hiddenLoading();
				}
			});	
		}
	});
}

function delCluster(delserver)
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( nodes == null || nodes.length == 0 ) return;
	var node = nodes[0];
	var parentNode = node.getParentNode();
	
	var tips = "";
	if( delserver ) tips = "你确定要删除集群【"+node.name+"】以及以下的所有伺服器？";
	else tips = "你确定要移除集群【"+node.name+"】？该集群下的所有伺服器将被移动到缺省集群或父集群中";
	skit_confirm(tips, function(yes){
		if( yes )
		{
			MonitorMgr.delCluster(node.id, delserver, rootid, timestamp,{
				callback:function(response){
					if( response.succeed )
					{
						if( parentNode ) setUserActionMemory("monitor!navigate.cluster", parentNode.id);
						else setUserActionMemory("monitor!navigate.cluster", 0); 

						var zNodes = jQuery.parseJSON(response.result);
						initZtree(zNodes);
					}
					else skit_alert(response.message);
				},
				timeout:1000000,
				errorHandler:function(message) {
					skit_hiddenLoading();
				}
			});	
		}
	});
}

var inputClusterName = document.createElement("input");
inputClusterName.id = "inputClusterName";
function addCluster(root)
{
	hideRMenu();
	var parentNode = null;
	var parentId = -7;
	if( root ){}else{
		var nodes = myZtree.getSelectedNodes();
		if( nodes.length > 0 ){
			parentNode = nodes[0];
			parentId = nodes[0].id;
		}
	}
	try
	{
		var div0 = document.createElement("div");
		div0.className = "panel panel-default";
		var div1 = document.createElement("div");
		div1.className = "panel-body";
		div1.style.paddingBottom = 1;
		div0.appendChild(div1);
		
		var div2 = document.createElement("div");
		div2.className = "form-group";
		div1.appendChild(div2);
		var div3 = document.createElement("div");
		div3.className = "input-group";
		div2.appendChild(div3);
		var span = document.createElement("span");
		span.className = "input-group-addon";
		span.innerHTML = "<i class='fa fa-key'></i>集群分组名称";
		div3.appendChild(span);
		inputClusterName.className = "form-control";
		inputClusterName.type = "text";
		inputClusterName.placeholder = "只能含有汉字、数字、字母、不能以下划线开头和结尾，不超过10个字";
		div3.appendChild(inputClusterName);
	
		var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消","width":520});
	    var content = "<div style='height:96px;width:480px;border:0px solid red;overflow-y:auto;'>"+div0.outerHTML+"</div>"
	    SM.show({
	    	"title":root?"新增集群":"在【"+parentNode.name+"】下新增集群",
	        "model":"confirm",
	        "callback": function(){
	        	inputClusterName = document.getElementById(inputClusterName.id);
	        	var clusterName = inputClusterName.value;
				if( clusterName == "" )
				{
					skit_errtip("集群分组名称不允许为空.", inputClusterName);
			        return false;
				}
		        if( !clusterName.match(new RegExp("^[a-zA-Z0-9_\u4e00-\u9fa5]+$")) )
		        {
		        	skit_errtip("集群分组名称只能含有汉字、数字、字母、下划线不能以下划线开头和结尾", inputClusterName);
			        return false;
		        }
				if( clusterName.length<2||clusterName.length>32 )
		        {
					skit_errtip("集群分组名称不少于2个字不超过32个字", inputClusterName);
			        return false;
		        }
				skit_showLoading();
				MonitorMgr.addCluster(parentId, clusterName, timestamp, {
					callback:function(rsp) {
						skit_hiddenLoading();
						try
						{
							timestamp = rsp.timestamp>0?rsp.timestamp:timestamp;
							if( rsp.succeed )
							{
								nodes = myZtree.addNodes(parentNode, -1, jQuery.parseJSON(rsp.result));
								myZtree.selectNode(nodes[0]);
								open(nodes[0]);
							}
							else
							{
								skit_alert(rsp.message);
							}
						}
						catch(e)
						{
							alert(e);
						}
					},
					timeout:30000,
					errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
				});
	        	return true;
	        },
	        "cancelback": function(){
	        },
	    	"contents": content
	    });
	}
	catch(e)
	{
		alert(e);
	}
}
var inputServerIP = document.createElement("input");
inputServerIP.id = "inputServerIP";
var inputServerPort = document.createElement("input");
inputServerPort.id = "inputServerPort";
var selectSynchMode = document.createElement("select");
selectSynchMode.className = "form-control form-control-font";
selectSynchMode.options.add(new Option("通过TCP/IP监控通道同步","tcp"));
selectSynchMode.options.add(new Option("通过HTTP/JSON监控通道同步", "json"));
selectSynchMode.id = "synchmode";
var memIP = "";
var memPort = "";
function addServer()
{
	var nodes = myZtree.getSelectedNodes();
	var parentNode = null;
	var parentId = null;
	if( nodes.length > 0 ){
		parentNode = nodes[0];
		parentId = nodes[0].id;
	}
	try
	{
		var div0 = document.createElement("div");
		div0.className = "panel panel-default";
		var div1 = document.createElement("div");
		div1.className = "panel-body";
		div1.style.paddingBottom = 1;
		div0.appendChild(div1);
		
		var div2 = document.createElement("div");
		div2.className = "form-group";
		div1.appendChild(div2);
		var div3 = document.createElement("div");
		div3.className = "input-group";
		div2.appendChild(div3);
		var span = document.createElement("span");
		span.className = "input-group-addon";
		span.innerHTML = "<i class='fa fa-key'></i>伺服器ＩＰ地址";
		div3.appendChild(span);
		inputServerIP.className = "form-control";
		inputServerIP.type = "text";
		div3.appendChild(inputServerIP);
		
		div2 = document.createElement("div");
		div2.className = "form-group";
		div1.appendChild(div2);
		div3 = document.createElement("div");
		div3.className = "input-group";
		div2.appendChild(div3);
		span = document.createElement("span");
		span.className = "input-group-addon";
		span.innerHTML = "<i class='fa fa-key'></i>伺服器监控端口";
		div3.appendChild(span);
		inputServerPort.className = "form-control";
		inputServerPort.type = "text";
		inputServerPort.style.width = 80;
		div3.appendChild(inputServerPort);

		div2 = document.createElement("div");
		div2.className = "form-group";
		div1.appendChild(div2);
		div3 = document.createElement("div");
		div3.className = "input-group";
		div2.appendChild(div3);
		span = document.createElement("span");
		span.className = "input-group-addon";
		span.innerHTML = "<i class='fa fa-key'></i>伺服器监控同步";
		div3.appendChild(span);
		div3.appendChild(selectSynchMode);

		var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消","width":520});
	    var content = "<div style='height:184px;width:480px;border:0px solid red;overflow-y:auto;'>"+div0.outerHTML+"</div>"
	    SM.show({
	    	"title":"新增要监控的伺服器",
	        "model":"confirm",
	        "callback": function(){
	        	inputServerIP = document.getElementById(inputServerIP.id);
	        	var ip = inputServerIP.value;
	            var regexp2 = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";
	            var m2 = ip.match(new RegExp(regexp2));
	            if( !m2 )
	            {
		        	skit_errtip("请输入正确的IP地址", inputServerIP);
			        return false;
	            }
	            inputServerPort = document.getElementById(inputServerPort.id);
	            var port = Number(inputServerPort.value);
	            if( port<1024 || port>65535 )
	            {
		        	skit_errtip("端口请输入1024到65535的整数", inputServerPort);
	            	return false;
	            }
	            selectSynchMode = document.getElementById(selectSynchMode.id);
	            memIP = ip;
	            memPort = port;
				MonitorMgr.addServer(parentId, ip, port, selectSynchMode.value, timestamp, {
					callback:function(rsp) {
						skit_hiddenLoading();
						try
						{
							timestamp = rsp.timestamp>0?rsp.timestamp:timestamp;
							if( rsp.succeed )
							{
								myZtree.addNodes(parentNode, -1, jQuery.parseJSON(rsp.result));
							}
							else
							{
								skit_alert(rsp.message);
								if( rsp.result )
								{
									var existNode = jQuery.parseJSON(rsp.result);
									var node = myZtree.getNodeByParam("id", existNode.id );
									if( node )
									{
										myZtree.selectNode(node);
										open(node);
									}
								}
							}
						}
						catch(e)
						{
							alert(e);
						}
					},
					timeout:30000,
					errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
				});
	        	return true;
	        },
	        "cancelback": function(){
	        	
	        },
	    	"contents": content
	    });
	    $('#'+inputServerIP.id).inputmask('ip');
	    document.getElementById(inputServerIP.id).value = memIP;
	    document.getElementById(inputServerPort.id).value = memPort;
	}
	catch(e)
	{
		alert(e);
	}
}

function beforeDrop(treeId, treeNodes, targetNode, moveType, isCopy) {
	if( !targetNode ){
		skit_alert("不能移动节点到自身上");
		return false;//伺服器不能移动到根节点
	}
	if( targetNode.ip && moveType == "inner" )
	{
		skit_alert("不能将集群节点移动到伺服器节点下。");
		return false;
	}
	if( treeNodes[0].ip  )
	{
		if( moveType != "inner" && targetNode.pid < 0 )
		{
			skit_alert("伺服器节点不能被移动到根目录下");
			return false;//伺服器不能移动到根节点
		}
	}
	if( treeNodes[0].id == -1  ){
		skit_alert("缺省本地伺服器节点不能被移动");
		return false;//伺服器不能移动到根节点
	}
	if( treeNodes[0].id == 0 && moveType == "inner" ){
		skit_alert("缺省集群不能被移动到其它目录下，允许在根目录平移");
		return false;//伺服器不能移动到根节点
	}
	if( treeNodes[0].id == 0 && targetNode.pid > 0 ){
		skit_alert("缺省集群不能被移动到子目录下");
		return false;//伺服器不能移动到根节点
	}
	parentBeforeDrop = treeNodes[0].getParentNode()
	return true;
}
var parentBeforeDrop;
function onDrop(event, treeId, treeNodes, targetNode, moveType, isCopy)
{
	if( !targetNode ) return;
	if( moveType )
	{
		try
		{
			var node = treeNodes[0];
			var nodeParent = node.getParentNode();
			//alert("0"+parentBeforeDrop.name);
			MonitorMgr.dragDropCluster(moveType, node.id, targetNode.id, timestamp, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						timestamp = rsp.timestamp>0?rsp.timestamp:timestamp;
						if( rsp.succeed )
						{
							node.pid = rsp.result;
							if( parentBeforeDrop )
							{
								parentBeforeDrop["isParent"] = true;
								myZtree.updateNode(parentBeforeDrop);
							}
							myZtree.updateNode(node);
						}
						else
						{
							skit_alert(rsp.message);
						}
					}
					catch(e)
					{
						skit_alert("移动集群监控目录出现异常"+e.message+", 行数"+e.lineNumber);
					}
				},
				timeout:30000000,
				errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
			});	
		}
		catch(e)
		{
			skit_alert("移动集群监控目录出现异常"+e.message+", 行数"+e.lineNumber);
		}
	}
}

function setClusterState(treeNode)
{
	var i = document.getElementById("ico_stat_"+treeNode.tId);
	if( i )
	{
		if( treeNode.state >= 0 )
		{
			i.src = getClusterStateIcon(treeNode.state);
			if( treeNode.stateinfo ) i.title = treeNode.stateinfo;
			else i.title = "继承子节点";
		}
		else
		{
			i.parentNode.removeChild(i);
		}
	}
	else if( treeNode.state >= 0 )
	{
		var editStr = "<img src='"+getClusterStateIcon(treeNode.state)+"' title='"+(treeNode.stateinfo?treeNode.stateinfo:"继承子节点")+"' id='ico_stat_"+treeNode.tId+"' style='margin-right:2px;'>";
		var aObj = $("#" + treeNode.tId + "_a");
		if( aObj ) aObj.before(editStr);
	}

	if( treeNode.zkmyid )
	{
		i = document.getElementById("ico_zk_"+treeNode.tId);
		if( !i ){
			var editStr = "<img src='images/number/"+treeNode.zkmyid+".png' title='ZooKeeper#"+treeNode.zkmyid+"集群伺服器' id='ico_zk_"+treeNode.tId+"'/>";
			var aObj = $("#" + treeNode.tId + "_a");
			if( aObj ) aObj.after(editStr);
		}
	}
}

function getClusterStateIcon(state)
{
	switch(state)
	{
	case 1:
		return "images/icons/green.png?v=1";
	case 2:
		return "images/icons/yellow.png?v=1";
	case 3:
		return "images/icons/danger.gif?v=1";
	}
	return "images/icons/gray.png?v=1";
}

function rmCluster(nodes, filter)
{
	for(var i = 0; i < nodes.length; i++)
	{
		var node = nodes[i];
		if( filter[node.id] )
		{
			if( node.children )
			{
				rmCluster(node.children, filter);
			}
		}
		else
		{
			if( node.ip )
			{
				myZtree.removeNode(node);
			}
		}
	}
}

function viewNetstatInfo(){
	var node = getServer();
	if( node == null ) return;
	openView("查看服务器端口详情: "+node.ip, "monitor!netstat.action?ip="+node.ip+"&port="+node.port);
}

function viewServerLoadInfo()
{
	var node = getServer();
	if( node == null ) return;
	openView("查看服务器负载详情: "+node.ip, "monitorload!serverchart.action?id="+node.id);
}

function openFiles()
{
	var node = getServer();if( node == null ) return;
	if( node.state == 0 )
	{
		skit_alert("伺服器未正常启动不能打开文件管理视图。");
		return;
	}
	openView("伺服器【"+node.ip+"】文件管理器: "+node.name, "files!open.action?id="+node.id);
}

function openZookeeperServer()
{
	var node = getServer();if( node == null ) return;
	if( node.state == 0 )
	{
		skit_alert("伺服器未正常启动不能打开分布式应用协调程序(Zookeeper)管理视图。");
		return;
	}
	openView("分布式协调服务(Zookeeper)集群管理器: "+node.name, "zookeeper!open.action?id="+node.id);
}

function viewZookeeperClusterInfo()
{
	var node = getServer();
	if( node == null ) return;
	MonitorMgr.getZookeeperConfig(node.id, {
		callback:function(response){
			if( response.succeed )
			{//表示正在执行升级
				if( response.result == "" )
				{
					skit_alert("该伺服器【"+node.ip+"】的Zookeeper是单机模式。");
				}
				else
				{
					skit_message(response.result, "该伺服器【"+node.ip+"】的Zookeeper是集群模式", 480, 480);
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

//重新加载监控
function resetMonitor()
{
	var node = getServer(); if( node == null ) return;
	MonitorMgr.resetMonitor(node.id,{
		callback:function(response) {
			if( response.succeed )
			{
				node = myZtree.getNodeByParam("id", node.id );
				if( node )
				{
					node["state"] = 0;
					setClusterState(node);
				}
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

function monitordatabase()
{
	var node = getServer(); if( node == null ) return;
	if( node.state == 0 )
	{
		skit_alert("伺服器未正常启动不能打开JDBC数据库监控配置。");
		return;
	}
	setUserActionMemory("monitorcfg!databases.server", node.id);
	openView("JDBC数据库监控配置: "+node.name, "monitorcfg!databases.action?id="+node.getParentNode().id);
}

function ssh(){
	var node = getServer(); if( node == null ) return;
	openView("安全远程控制SSH: "+node.name, "rpc!navigate.action?id="+node.id);
}

/*function configControl()
{
	var node = getServer(); if( node == null ) return;
	openView("配置伺服器主控程序: "+node.name, "control!configxml.action?ip="+node.ip+"&port="+node.port);
}*/

function configcontrol(){
	var node = getServer(); if( node == null ) return;
	openView("配置伺服主控系统: "+node.name, "files!edit.action?ip="+node.ip+"&port="+node.port+"&path=config/");
}

function configPrograms()
{
	var node = getServer(); if( node == null ) return;
	openView("伺服器【"+node.ip+"】程序管理: "+node.name, "control!open.action?id="+node.id);
}

function restartup()
{
	var node = getServer(); if( node == null ) return;
	var tip = "您确定重启该主机("+node.name+")的主控服务吗?";
	skit_confirm(tip, function(yes){
		if( yes )
		{
			document.forms[0].action = "control!restartup.action";
			document.forms[0].target = "downloadframe";
			document.forms[0].submit();
		}
	});
}

function upgrade()
{
	var node = getServer(); if( node == null ) return;
	var tip = "您确定检查升级该主机("+node.name+")是否有最新的主控引擎版本吗?";
	skit_confirm(tip, function(yes){
		if( yes )
		{
			openView("系统升级","control!checkupgrade.action?ip="+node.ip+"&port="+node.port);
		}
	});
}
//暂停所有服务
function suspend()
{
	var node = getServer(); if( node == null ) return;
	var tip = "您确定暂停该主机("+node.name+")的主控服务下所有程序引擎吗?";
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
	var node = getServer(); if( node == null ) return;
	MonitorMgr.getMonitorRunnerInfo(node.ip, node.port,{
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
	var node = getServer(); if( node == null ) return;
	skit_confirm("预览伺服器【"+node.ip+"】的主控wrapper日志，您是否压缩该文件？", function(yes){
		if( yes )
		{
			document.forms[0].path.value = "log/wrapper.log";
			document.forms[0].action = "files!download.action";
			document.forms[0].method = "POST";
			document.forms[0].target = "downloadframe";
			document.forms[0].submit();
		}
		else
		{
			openView(node.ip+">>wrapper.log","files!show.action?filetype=text&path=log/wrapper.log&ip="+node.ip+"&port="+node.port);
		}
	});
}
//查看wrapper日志
function wrapperlogx()
{
	var node = getServer(); if( node == null ) return;
	skit_confirm("预览伺服器【"+node.ip+"】的主控wrapper错误日志，您是否压缩该文件？", function(yes){
		if( yes )
		{
			document.forms[0].path.value = "log/wrapper.log.x";
			document.forms[0].action = "files!download.action";
			document.forms[0].method = "POST";
			document.forms[0].target = "downloadframe";
			document.forms[0].submit();
		}
		else
		{
			openView(node.ip+">>wrapper.log","files!show.action?filetype=text&path=log/wrapper.log.x&ip="+node.ip+"&port="+node.port);
		}
	});
}

function getServer()
{
	hideRMenu();
	var nodes = myZtree.getSelectedNodes();
	if( nodes == null || nodes.length < 1 || nodes[0].isParent )
	{
		skit_alert("请从集群伺服器导航树选择指定服务器。");
		return null;
	}
	document.getElementById( "id" ).value = nodes[0].id;
	document.getElementById( "ip" ).value = nodes[0].ip;
	document.getElementById( "port" ).value = nodes[0].port;
	return nodes[0];
}

function searchServer()
{
	var text = document.getElementById( "searchBar" ).value;
	text = text.trim();
	if( text == "" )
	{
		return false;
	}
	
	var nodes = myZtree.getNodesByParamFuzzy("ip", text, null);
	for(var i = 0; i < nodes.length; i++)
	{
		if( nodes[i].isParent ) continue;
		myZtree.selectNode(nodes[i]);
		open(nodes[i]);
		break;
	}
}

function getClusterStatesInfo()
{
	MonitorMgr.getClusterStates(rootid, 0, {
		callback:function(response){
			if( response.succeed )
			{//表示正在执行升级
				try
				{
					var info = "";
					var states = jQuery.parseJSON(response.result);
					for(var i = 0; i < states.length; i++)
					{
						var stat = states[i];
						if( stat.ip )
						{
							info += "\r\nid="+stat.id;
							info += ", ip="+stat.ip;
							info += ", state="+stat.state
							info += ", stateinfo="+stat.stateinfo
							info += ", "+stat.title;
						}
					}
					skit_message(info, "伺服器状态信息", 480, 480);
				}
				catch(e)
				{
					skit_alert("解析集群状态变化信息出现异常"+e.message+", 行数"+e.lineNumber, "异常提示", function(){
					});
				}
			}
			else
			{
				skit_alert(response.message, "异常提示", function(){
				});
			}
		},
		timeout:10000,
		errorHandler:function(message) {
			skit_alert("更新集群状态变化信息出现异常"+message, "异常提示", function(){
			});
		}
	});
}
var serverCount = <ww:property value='ww'/>;
var timestamp1 = 0;
//得到集群的状态
var count_error = 0;
var time_error = 0;
function getClusterStates()
{
	MonitorMgr.getClusterStates(rootid, timestamp1, {
		callback:function(response){
			if( response.succeed )
			{//表示正在执行升级
				try
				{
					if( serverCount >= 1024 ){
						timestamp1 = response.timestamp;
					}
					var parent = null;
					var count = 0;
					var info = "";
					var states = jQuery.parseJSON(response.result);
					var filter = new Object();
					for(var i = 0; i < states.length; i++)
					{
						var stat = states[i];
						filter[stat.id] = true;
						var node = null;
						if( parent ){
							node = myZtree.getNodeByParam("id", stat.id, parent);
						}
						if( !node ){
							node = myZtree.getNodeByParam("id", stat.id);
						}
						if( node )
						{
							if( node.state != stat.state ){
								count += 1;
								if( stat.state >= 0 )
								{
									node["state"] = stat.state;
									setClusterState(node);
									if( stat.state > 1 )
									{
										info += "\r\nid="+stat.id+", name="+stat.name+", state="+stat.state;
									}
								}
								if( node.name != stat.name )
								{
									node.name = stat.name;
									myZtree.updateNode(node);
								}
							}
						}
						else if( serverCount < 1024 )
						{
							count += 1;
							var parentNode = null;
							if( stat.pid >= 0  ) parentNode = myZtree.getNodeByParam("id", stat.pid );
							var nodes = myZtree.addNodes(parentNode, -1, stat);	
							node = nodes[0];
						}
						if(node && !node.ip ){
							parent = node;
						}
					}
					//alert("getClusterStates:"+info);
					if( serverCount < 1024 ){
						rmCluster(myZtree.getNodes(), filter);
					}
					//alert(begintime+"\r\n"+endtime+"\r\n"+count+"/"+states.length);
					window.setTimeout("getClusterStates()",15000);
					/*if( states.length > 1000 ){
						large_cursor = large_step*1000;
						large_step += 1;
						large_skip = large_step*1000;
						if( large_skip > states.length ){
							large_skip = states.length;
							large_step = 0;
						}
					}*/
				}
				catch(e)
				{
					top.window.skit_alert("解析集群状态变化信息出现异常"+e.message+", 行数"+e.lineNumber, "异常提示", function(){
						window.setTimeout("getClusterStates()",7000);
					});
				}
			}
			else
			{
				top.window.skit_alert(response.message, "异常提示", function(){
					window.setTimeout("getClusterStates()",7000);
				});
			}
		},
		timeout:10000,
		errorHandler:function(message) {
			if( count_error == 10 ){
				skit_alert("自"+time_error+"以来已经出现"+count_error+"次更新集群状态变化出现异常"+message, "异常提示", function(){
					getClusterStates();
				});
				count_error = 0;
			}
			else{
				if( count_error == 0 ){
					time_error = nowtime();
				}
				count_error += 1;
				window.setTimeout("getClusterStates()",7000);
			}
		}
	});
}
</SCRIPT>
<link href="skin/defone/css/simplemodal.css" rel="stylesheet">
<script src="skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="skin/defone/js/simple-modal.js?v=3"></script>
<script src="skit/js/jquery.inputmask.bundle.min.js"></script>
<script src="skit/js/auto-line-number.js"></script>
<script src="skit/js/bootstrap-tour.min.js"></script>
<SCRIPT type="text/javascript">
function tour()
{
	var showTour = getUserActionMemory("tourModules");
	if( showTour != "1" )
	{
		setUserActionMemory("tourModules", "1");
		var tour = new Tour({
		    steps: [{
		        element: "#myZtree",
		        title: "模块菜单导航显示",
		        content: "可通过该导航树查找、查看、新增、删除菜单。"
		    },{
		        element: "#tabL",
		        title: "菜单可视化编辑",
		        content: "可对导航树上显示的菜单进行可是化编辑，设置菜单的名称、URL地址，以及图标等。",
		        placement: "left"
		    }],
		    container: 'body',
		    backdrop: true,
		    keyboard: true,
		    storage: false
		});
		// Initialize the tour
		tour.init();
		// Start the tour
		tour.start();
	}
}
</SCRIPT>
</html>