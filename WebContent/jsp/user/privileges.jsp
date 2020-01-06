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
<%=Kit.getDwrJsTag(request,"interface/UserMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body>
<ul id='myZtree' class='ztree'></ul>
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
			removeHoverDom: removeHoverDom,
			addDiyDom: addDiyDom
		},
		data: {
			key: {
				title: "title"
			}
		}
	};
	
	function addHoverDom(treeId, treeNode) {
		if( treeNode.id == -1 || !treeNode.children || treeNode.children.length == 0  ) return;
		if( treeNode.nocheck )
		{
			var sObj = $("#" + treeNode.tId + "_span");
			if ( $("#cBtn_"+treeNode.tId).length>0) return;
			var ico = "";
			var title = "全选集群【"+treeNode.name+"】下所有权限";
			var chkall = treeNode.chkall?true:false;
			if( chkall ) ico = "checkbox_true_full_focus";
			else{
				title = "全取消集群【"+treeNode.name+"】下所有权限";
				ico = "checkbox_false_part_focus"
			}
			var addStr = "<span class='button chk "+ico+"' style='cursor:pointer' id='cBtn_" + treeNode.tId+ "' "+
				"title='"+title+"' onfocus='this.blur();'></span>";
			sObj.after(addStr);
			var btn = $("#cBtn_"+treeNode.tId);
			if(btn) btn.bind("click", function(){
				var ztree = $.fn.zTree.getZTreeObj("myZtree");
				var nodes = ztree.getSelectedNodes();
				if( nodes.length > 0 )
				{
					var children = nodes[0].children;
					if( children )
						for(var i = 0; i < children.length; i++)
						{
							ztree.checkNode (children[i], chkall, true);
						}
					nodes[0]["chkall"] = !chkall;
				}
			});
			return;
		}
		var sObj = $("#" + treeNode.tId + "_span");
		if ( $("#cBtn_"+treeNode.tId).length>0) return;
		var title = "单选该功能权限【"+treeNode.name+"】，不选择其下子权限";
		if( treeNode.cluster ) title = "只允许角色权限组"+treeNode.name+"，了解服务器工作状况和文件以及日志，不能做重启文件等管控操作";
		var addStr = "<span class='button chk checkbox_true_full_focus' style='cursor:pointer' id='cBtn_" + treeNode.tId+ "' "+
		"title='"+title+"' onfocus='this.blur();'></span>";
		sObj.after(addStr);
		var btn = $("#cBtn_"+treeNode.tId);
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
		$("#cBtn_"+treeNode.tId).unbind().remove();
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
		try
		{
			var zNodes = <ww:property value="jsonData" escape="false"/>;
			$.fn.zTree.init($("#myZtree"), setting, zNodes);
			myZtree = $.fn.zTree.getZTreeObj("myZtree");
		}
		catch(e)
		{
			alert(e);
		}
	});
	//#########################################################################
function filter(node) {
    return !node.checked&&!node.nocheck;
}	
function setPrivileges()
{
	var privileges = new Object();
	var nodes = $.fn.zTree.getZTreeObj("myZtree").getNodesByFilter(filter);
	for(var i = 0; i < nodes.length; i++ )
	{
		var node = nodes[i];
		privileges[node.id] = false;
	}
	if( nodes.length == 0 )
	{
		skit_alert("你没有为用户【<ww:property value='theuser.realname'/>】的设置任何用户私有权限，权限设置后可通过清除功能恢复默认角色权限组功能。");
		return;
	}
	var json = JSON.stringify(privileges);
	skit_showLoading();
	skit_confirm("你将为用户【<ww:property value='theuser.realname'/>】设置用户权限，用户权限只限于角色权限组范围内，该用户将失去角色权限组赋予的部分权限功能，请确认？", function(yes){
		if( yes )
		{
			UserMgr.setPrivileges("<ww:property value='theuser.username'/>", <ww:property value='theuser.roleid'/>, json, timestamp, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						skit_alert(rsp.message);
						timestamp = rsp.timestamp>0?rsp.timestamp:timestamp;
					}
					catch(e)
					{
						skit_alert("改变用户角色权限组操作出现异常"+e);
					}
				},
				timeout:30000,
				errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
			});			
		}
	});
}
parent.hideClearPrivilegesButton();
<ww:if test='hasToolbar'>
parent.showClearPrivilegesButton();
</ww:if>
</SCRIPT>
</html>