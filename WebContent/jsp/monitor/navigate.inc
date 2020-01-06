<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%> 
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<%=Kit.getDwrJsTag(request,"interface/MonitorMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body>
<form>
<input type='hidden' name='id' id='id' value=''>
<input type='hidden' name='ip' id='ip' value=''>
<input type='hidden' name='port' id='port' value=''>
<TABLE style='width:100%;height:100%;margin-top:3px'>
<TR><TD width='250'>
        <div class="panel panel-default" style='border: 1px solid #aaaaaa'>
   			<div class="panel-heading"><i class='skit_fa_btn fa fa-server'></i> 集群伺服器导航树</div>
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
							<li onclick="view();"><i class='skit_fa_btn fa fa-street-view'></i> 以独立视图打开</li>
						</ul>
					</div>
				</div>
   			</div>
   		</div>	
	</TD>
	<TD style='width:3px;'/>
	<TD valign='top'>
		<div id="tabL" style="position:relative;display:none">
			<ul id='ulTab'>
			</ul>
		</div>
	</TD>
</TR>
</TABLE>
</form>
</body>
<SCRIPT type="text/javascript">
var ifrid;// = "i-1";
function resizeWindow()
{
	var div = document.getElementById( 'divTree' );
	div.style.height = windowHeight - 78;
	if( ifrid )
	{
		div = document.getElementById( ifrid );
		var h = document.getElementById("ulTab").clientHeight;
		if( h == 0 ) h = 33;
		div.style.height = windowHeight - h - 15;
	}
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
</SCRIPT>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<style type='text/css'>
.ui-tabs .ui-tabs-panel {
    display: block;
    border-width: 0;
    padding: 0px;
    background: none;
}
</style>
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

$('#tabL').tabs({
/*    add: function(event, ui) {
        alert(ui.panel.id);
        alert($tabs);
        $tabs.tabs('select', '#' + ui.panel.id);
    },*/
    remove: function(event, ui) {
		$("#tabL").tabs('select', '#i-1');
    },
    select: function(event, ui) {
		ifrid = ui.panel.id;
		if( resizeWindow ) resizeWindow();
    }
});
document.getElementById( 'tabL' ).style.display = "";
var timestamp = <ww:property value='timestamp'/>;
//########################################################################
	var setting = {
		//editable: false,
		//fontCss : {color:"red"},
		check: {
			enable: false
		},
		callback: {
			onClick: onClick,
			onRightClick: onRightClick,
			onCollapse: onCollapse,
			onExpand: onExpand
		},
		view: {
			addDiyDom: addDiyDom
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
		saveExpandMemory(memoryTree);
	}
	
	function onExpand(event, treeId, treeNode) 
	{
		expandMemory[treeNode.id] = true;
		saveExpandMemory(memoryTree);
	}
	
	function onClick(event, treeId, treeNode)
	{
		myZtree.expandNode(treeNode, true);
		if( treeNode.ip ) open(treeNode);
	}

	function onRightClick(event, treeId, treeNode)
	{
		if( treeNode.isParent ) return;
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
	}
	
	function addDiyDom(treeId, treeNode) {
		var editStr = "";
		if( treeNode.ico )
		{
			editStr = "<i class='fa "+treeNode.ico+"' id='ico_fa_menu_" + treeNode.tId + "'></i>";
			
		}
		if( treeNode.warn )
		{
			editStr += "<i class='skit_fa_icon_war fa fa-exclamation-triangle' id='ico_fa_menu_warn_" + treeNode.tId + "'></i>";
		}
		if( treeNode.zkmyid && memoryTree == "zookeeper!navigate.open" )
		{
			editStr += "<img src='images/number/"+treeNode.zkmyid+".png' title='#"+treeNode.zkmyid+"集群伺服器'/>";
		}
		if( editStr )
		{
			var aObj = $("#" + treeNode.tId + "_a");
			if( aObj ) aObj.after(editStr);
		}
	}

	$(document).ready(function(){
		try
		{
			var zNodes = <ww:property value="jsonData" escape="false"/>;
			var memory = getUserActionMemory(memoryTree);
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
			var serverid = getUserActionMemory(memoryServer);
			var node = null;
			if( serverid )
			{
				var node = myZtree.getNodeByParam("id", serverid);
				if( node )
				{
					myZtree.selectNode(node);
					open(node);
					getServerStates();
					return;
				}
			}
			//serverid = -1;
			var nodes = myZtree.getNodesByParam("server", true, null);
			if( !nodes || nodes.length == 0 )
			{
				window.top.skit_alert('集群未配置任何伺服器，不能使用集群导航', "后台提示", function(){
					window.top.closeView();
				});
			}
			else
			{
				var node = nodes[0];
				myZtree.selectNode(node);
				open(node);
			}
			getServerStates();
		}
		catch(e)
		{
			alert(e);
		}
	});
//#########################################################################
function removeTab(id)
{
	var node = myZtree.getNodeByParam("id", -1);
	if( node )
	{
		myZtree.selectNode(node);
		open(node);
	}
	$("#tabL").tabs('remove', '#i'+id);
}

function open(node)
{
	if(!node){
		var nodes = myZtree.getSelectedNodes();
		node = nodes[0];
	}
	if( node.isParent ) return;
	if( node.state == 0 )
	{
		skit_alert("伺服器【"+node.id+"】可能未启动监控");
	}
	var i = document.getElementById( "i"+node.id );
	if( i ){}else
	{
		var id = "i"+node.id;
		var ulTab = document.getElementById( "ulTab" );
		var li = document.createElement("li");
		li.innerHTML = "<a href='#i"+node.id+"'>"+node.name+" <i class='fa fa-remove' style='cursor:pointer' title='关闭选项卡' onclick='removeTab("+node.id+")'></i></a>";
		ulTab.appendChild(li);
		i = document.createElement("iframe");
		i.name = id;
		i.id = id;
		i.className = "nonicescroll";
		i.style.width = "100%";
		i.style.border = "0px";
		i.style.display = "none";
		document.getElementById( "tabL" ).appendChild(i);
		$("#tabL").tabs("refresh");
	}
	$("#tabL").tabs('select', '#i'+node.id);
	document.getElementById( "id" ).value = node.id?node.id:"";
	document.getElementById( 'ip' ).value = node.ip;
	document.getElementById( 'port' ).value = node.port;
	document.forms[0].action = hrefOpen;
	document.forms[0].method = "POST";
	document.forms[0].target = "i"+node.id;
	document.forms[0].submit();
	setUserActionMemory(memoryServer, node.id);
}
function view()
{
	var node = myZtree.getSelectedNodes()[0];
	var title = titleView+" - "+node.ip;
	if( node.name ) title += node.name;
	openView(title, hrefView+"?id="+node.id+"&ip="+node.ip+"&port="+node.port);
	hideRMenu();
}

function getServerStates()
{
	var begintime = nowtime();//更新集群状态变化信息出现异常
	MonitorMgr.getServerStates(-1, {
		callback:function(response){
			if( response.succeed )
			{//表示正在执行升级
				try
				{
					var states = jQuery.parseJSON(response.result);
					var filter = new Object();
					for(var i = 0; i < states.length; i++)
					{
						var stat = states[i];
						filter[stat.id] = true;
						var node = myZtree.getNodeByParam("id", stat.id );
						if( node && node.ip )
						{
							if( stat.state >= 0 )
							{
								node["state"] = stat.state;
								setServerState(node);
							}
							if( node.name != stat.name )
							{
								node.name = stat.name;
								myZtree.updateNode(node);
							}
						}
					}
					rmServer(myZtree.getNodes(), filter);
					window.setTimeout("getServerStates()",60000);
				}
				catch(e)
				{
					top.window.skit_alert("解析集群状态变化信息出现异常"+e.message+", 行数"+e.lineNumber, "异常提示", function(){
						window.setTimeout("getServerStates()",60000);
					});
				}
			}
			else
			{
				top.window.skit_alert(response.message, "异常提示", function(){
					window.setTimeout("getServerStates()",15000);
				});
			}
		},
		timeout:24000,
		errorHandler:function(message) {
			skit_alert("自"+begintime+"更新集群状态变化信息出现异常"+message, "异常提示", function(){
				window.setTimeout("getServerStates()",15000);
			});
		}
	});
}

function rmServer(nodes, filter)
{
	for(var i = 0; i < nodes.length; i++)
	{
		var node = nodes[i];
		if( node.children )
		{
			rmServer(node.children, filter);
		}
		else if( !node.isParent )
		{
			if( filter[node.id] ){}else
			{
				myZtree.removeNode(node);
			}
		}
	}
}

function setServerState(treeNode)
{
	var i = document.getElementById("ico_stat_"+treeNode.tId);
	if( i )
	{
		if( treeNode.state >= 0 )
		{
			i.src = getServerStateIcon(treeNode.state);
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
		var editStr = "<img src='"+getServerStateIcon(treeNode.state)+"' title='"+(treeNode.stateinfo?treeNode.stateinfo:"继承子节点")+"' id='ico_stat_"+treeNode.tId+"'>";
		var aObj = $("#" + treeNode.tId + "_a");
		if( aObj ) aObj.before(editStr);
	}
}

function getServerStateIcon(state)
{
	switch(state)
	{
	case 0:
		return "images/icons/stop.png";
	default:
		return "images/icons/links.png";
	}
}
</SCRIPT>