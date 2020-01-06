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
<%=Kit.getDwrJsTag(request,"interface/RoleMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body style='margin:0px 1px;overflow-y:hidden;'>
<div id="tabL" style="position:relative;">
	<ul id='ulTab'>
		<li><a href="#i-modules">模块子系统菜单权限<i class='skit_fa_icon fa fa-info-circle'></i></a></li>
		<li><a href="#i-cluster">集群伺服器管控权限<i class='skit_fa_icon fa fa-info-circle'></i></a></li>
	</ul>
	<div id='i-modules'><ul id='myZtree' class='ztree'></ul></div>
	<iframe name='i-cluster' id='i-cluster' style='width:100%;border:0px;'></iframe>
</div>
</body>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var div = document.getElementById( 'i-modules' );
	div.style.height = windowHeight - 34;
	div = document.getElementById( 'i-cluster' );
	div.style.height = windowHeight - 34;
}
resizeWindow();
$( '#i-modules' ).niceScroll({cursorcolor: '#aaa',railalign: 'right', cursorborder: "none", horizrailenabled: false, zindex: 2001, left: '245px', cursoropacitymax: 0.6, cursorborderradius: "0px", spacebarenabled: false });
</SCRIPT>
<SCRIPT type="text/javascript">
$('#tabL').tabs({
    select: function(event, ui) {
		if( resizeWindow ) resizeWindow();
		setUserActionMemory("role!set.tab", ui.panel.id);
		if( ui.panel.id == "i-cluster" )
		{
			var ifr = document.getElementById( 'i-cluster' );
			if( ifr.src == "" ){
				ifr.src = "role!controlprivileges.action?id=<ww:property value="id"/>&pid=<ww:property value="pid"/>";
			}
		}
    }
});
document.getElementById( 'tabL' ).style.display = "";
</SCRIPT>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<SCRIPT type="text/javascript">
var timestamp = <ww:property value='timestamp'/>;
	//########################################################################
	var setting = {
		//editable: false,
		//fontCss : {color:"red"},
		check: {
			enable: true
		},
		callback: {
			onClick: onClick,
			beforeExpand: beforeExpand,
			onExpand: onExpand
		},
		view: {
			addHoverDom: addHoverDom,
			removeHoverDom: removeHoverDom,
			addDiyDom: addDiyDom
		}
	};

	function addHoverDom(treeId, treeNode) {
		if( treeNode.id == -1 || treeNode.nocheck || !treeNode.children ) return;
		var sObj = $("#" + treeNode.tId + "_span");
		if ( $("#mBtn_"+treeNode.tId).length>0) return;
		var addStr = "<span class='button chk checkbox_true_full_focus' style='cursor:pointer' id='mBtn_" + treeNode.tId+ "' "+
			"title='单选该功能权限【"+treeNode.name+"】，不选择其下子权限' onfocus='this.blur();'></span>";
		sObj.after(addStr);
		var btn = $("#mBtn_"+treeNode.tId);
		if(btn) btn.bind("click", function(){
			var ztree = $.fn.zTree.getZTreeObj("myZtree");
			var nodes = ztree.getSelectedNodes();
			if( nodes.length > 0 )
			{
				ztree.checkNode (nodes[0], false, true);
				ztree.checkNode (nodes[0], true, false);
			}
		});
	};
	function removeHoverDom(treeId, treeNode) {
		$("#mBtn_"+treeNode.tId).unbind().remove();
	};

	
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
		if( editStr )
		{
			var aObj = $("#" + treeNode.tId + "_a");
			if( aObj ) aObj.after(editStr);
		}
	}
	
	function onClick(event, treeId, treeNode)
	{
		myZtree.expandNode(treeNode, null, null, null, true);
		setUserActionMemory("role!set.tree", treeNode.id);
	}

	var clusterPrivileges;
	$(document).ready(function(){
		var jsoncluster = '<ww:property value="jsoncluster" escape="false"/>';
		try
		{
			var zNodes = <ww:property value="jsonData" escape="false"/>;
			//var zNodes = jQuery.parseJSON(json);
			$.fn.zTree.init($("#myZtree"), setting, zNodes);
			myZtree = $.fn.zTree.getZTreeObj("myZtree");
			clusterPrivileges = jQuery.parseJSON(jsoncluster);
			//$.fn.zTree.init($("#myZtree1"), setting1, zNodes);
			var tab = getUserActionMemory("role!set.tab");
			if( tab )
			{
				$("#tabL").tabs('select', '#'+tab);
			}
			var id = getUserActionMemory("role!set.tree");
			if( id )
			{
				var node = myZtree.getNodeByParam("id", id);
				if( node )
				{
					myZtree.expandNode(node, true, false);
				}
			}
		}
		catch(e)
		{
			alert(e);
		}
	});
	//#########################################################################
function filter(node) {
    return node.checked;
}
//设置角色的集群伺服器权限
function setRoleClusterPrivileges(serverid, value, item)
{
	var server = clusterPrivileges[serverid];
	if( item == "readable" )
	{
		if( value )
		{
			if( !server )
			{
				server = new Object();
				clusterPrivileges[serverid] = server;
			}
		}
		else
		{
			if( server )
			{
				delete clusterPrivileges[serverid];
			}
		}
	}
	else
	{
		if( !server )
		{
			server = new Object();
			clusterPrivileges[serverid] = server;
		}
		server[item] = value;
	}
}
	
function saveRole(pid, id, name)
{
	skit_showLoading();
	var privileges = new Object();

	var nodes = $.fn.zTree.getZTreeObj("myZtree").getNodesByFilter(filter);
	for(var i = 0; i < nodes.length; i++ )
	{
		var node = nodes[i];
		privileges[node.id] = true;
	}
	//showObject(clusterPrivileges);
	privileges["##cluster"] = clusterPrivileges;
	if( id == 1 )
	{
		delete privileges["##cluster"];
	}
	/*var nodes1 = $.fn.zTree.getZTreeObj("myZtree1").getNodesByFilter(filter);
	for(var i = 0; i < nodes1.length; i++ )
	{
		var node = nodes1[i];
		privileges[node.id] = true;
	}*/
	privileges["parent"] = pid;
	//nodes = myZtree.transformToArray(nodes);
	var json = JSON.stringify(privileges);
	RoleMgr.saveRole(pid, id, name, json, timestamp, {
		callback:function(rsp) {
			skit_hiddenLoading();
			skit_alert(rsp.message);
			try
			{
				timestamp = rsp.timestamp;
				parent.onSaveRole(timestamp, rsp.result);
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
</SCRIPT>
</html>