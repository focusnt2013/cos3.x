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
<body>
<table style='width:100%;'><tr>
<td style='width:50%;' valign='top' class='ui-tabs ui-tabs-panel'>
	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-bars'></i>模块子系统菜单权限</div>
		<div class="panel-body">
			<ul id='myZtree' class='ztree'></ul>
		</div>
	</div>
</td>
<td style='width:50%' valign='top' class='ui-tabs ui-tabs-panel'>
	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-server'></i>集群管控权限</div>
		<div class="panel-body">
			<ul id='myZtree1' class='ztree'></ul>
		</div>
	</div>
</td>	
</tr></table>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<SCRIPT type="text/javascript">
var timestamp = <ww:property value='timestamp'/>;
	//########################################################################
	var setting = {
		editable: false,
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
			addDiyDom: addDiyDom
		}
	};
	var setting1 = {
		editable: false,
		check: {
			enable: true
		},
		view: {
			addHoverDom: addHoverDom,
			addDiyDom: addDiyDom
		}
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
	}

	$(document).ready(function(){
		var left = <ww:property value="jsonData" escape="false"/>;
		var right = <ww:property value="jsoncluster" escape="false"/>;
		try
		{
			$.fn.zTree.init($("#myZtree"), setting, left);
			myZtree = $.fn.zTree.getZTreeObj("myZtree");
			$.fn.zTree.init($("#myZtree1"), setting1, right);
		}
		catch(e)
		{
			alert(e);
		}
	});
	//#########################################################################
</SCRIPT>
</html>