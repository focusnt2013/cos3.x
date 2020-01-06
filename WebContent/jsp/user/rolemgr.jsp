<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/RoleMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body onResize='resizeWindow()' style='overflow-y:hidden;'>
<TABLE style='width:100%;margin-top:3px;margin-bottom:3px;'>
<TR>
	<TD width='260' style='padding-right:3px;'>
        <div class="panel panel-default" style='border: 1px solid #aaaaaa' id='divZtree'>
   			<div class="panel-heading"><i class='skit_fa_btn fa fa-group'></i>角色权限组配置</div>
   			<div class="panel-body" style='padding: 0px;' id='divRoles'>
				<ul id='myZtree' class='ztree'></ul>
				<div id="rMenu">
					<ul>
						<li onclick="addRole();" id='liAddRole'><i class='skit_fa_icon_blue skit_fa_btn fa fa-plus-circle'></i> 新增角色权限组</li>
						<li onclick="renameRole();" id='liRename'><i class='skit_fa_icon_blue skit_fa_btn fa fa-edit'></i> 重新命名</li>
						<li onclick="setRoleState();" id='liRoleState'><i class='skit_fa_icon_yellow skit_fa_btn fa fa-minus-circle'></i> 禁用角色权限组</li>
						<li onclick="delRole();" id='liRoleState'><i class='skit_fa_icon_red skit_fa_btn fa fa-close'></i> 删除角色权限组</li>
					</ul>
				</div>
   			</div>
   		</div>		
	</TD>
	<TD valign='top'>
        <div class="panel panel-default" style='border: 1px solid #aaaaaa' id='divPrivileges'>
   			<div class="panel-heading"><span id='headPrivileges'><i class='skit_fa_btn fa fa-check-square-o'></i>权限配置</span>
                <div style='float:right;right:4px;top:0px;display:none' id='divSave'>
 				<button type="button" class='btn btn-outline btn-primary btn-xs' onclick='confirmSaveRole()'>
 					<i class='fa fa-save'></i> 保存权限配置</button>
                </div>
                <div style='float:right;padding-right:4px;top:0px;display:none' id='divCancelAddRole'>
 				<button type="button" class="btn btn-outline btn-danger btn-xs" onclick='cancelAddRole()'>
 					<i class='fa fa-close'></i> 取消新增角色</button>
                </div>
	        </div>
   			<div class="panel-body" style='padding: 0px;'>
				<iframe name='iRolePrivileges' id='iRolePrivileges' class='nonicescroll' style='width:100%;height:<ww:property value='wh-50'/>px;border:0px;'></iframe>
   			</div>
   		</div>
	</TD>
</TR>
</TABLE>
</body>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var h = windowHeight - 38;
	var div = document.getElementById( 'divRoles' );
	div.style.height = h;
	div = document.getElementById( 'iRolePrivileges' );
	div.style.height = h;
	//$( '#iRolePrivileges' ).attr("style", 'width:100%;border:0px;height:"+h+"px');
	//div.style.height = document.getElementById( 'myZtree' ).clientHeight;
}
resizeWindow();

$( '#divRoles' ).niceScroll({
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
//$( '#iRolePrivileges' ).niceScroll({cursorcolor: '#aaa',railalign: 'right', cursorborder: "none", horizrailenabled: false, zindex: 2001, left: '245px', cursoropacitymax: 0.6, cursorborderradius: "0px", spacebarenabled: false });
</SCRIPT>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<SCRIPT type="text/javascript">
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
			beforeDrag: beforeDrag,
			beforeDrop: beforeDrop,
			beforeDragOpen: beforeDragOpen,
			onDrag: onDrag,
			onDrop: onDrop,
			beforeRename: beforeRename,
			onRename: onRename
		},
		edit: {
			drag: {
				autoExpandTrigger: true,
				prev: dropPrev,
				inner: dropInner,
				next: dropNext
			},
			enable: true,
			showRemoveBtn: false,
			showRenameBtn: showRenameBtn
		},
		view: {
			addHoverDom: addHoverDom,
			removeHoverDom: removeHoverDom,
			addDiyDom: addDiyDom
		}
	};

	function addHoverDom(treeId, treeNode) {
		if( treeNode.id == -1 ) return;
		var sObj = $("#" + treeNode.tId + "_span");
		if ( $("#addBtn_"+treeNode.tId).length>0) return;
		var addStr = "<span class='button add' id='addBtn_" + treeNode.tId+ "' title='新增角色权限组' onfocus='this.blur();'></span>";
		sObj.after(addStr);
		var btn = $("#addBtn_"+treeNode.tId);
		if (btn) btn.bind("click", function(){
			addRole();
		});
	};
	
	function removeHoverDom(treeId, treeNode) {
		$("#addBtn_"+treeNode.tId).unbind().remove();
	};

	function addDiyDom(treeId, treeNode) {
		var editStr = "";
		if( treeNode.abort ) treeNode["ico"] = "skit_fa_icon_gray fa-minus-circle";
		if( treeNode.ico )
		{
			editStr = "<i class='fa "+treeNode.ico+"' id='ico_fa_role_" + treeNode.tId + "'></i>";
			var aObj = $("#" + treeNode.tId + "_a");
			if( aObj ) aObj.after(editStr);
		}
	}
	
	function beforeDrop(treeId, treeNodes, targetNode, moveType, isCopy) {
		if( targetNode.id == 1 && moveType != "inner"  ) return false;
		return true;
	}
	function showRenameBtn(treeId, treeNode) {
		return treeNode.id != <ww:property value='userRole'/>;
	}
	
	var _oldName;
	function beforeRename(treeId, treeNode, newName, isCancel) {
		if (newName.length == 0) {
			myZtree.cancelEditName();
			skit_alert("角色权限组名称不能为空.");
			return false;
		}
		else if (newName.length > 16) {
			myZtree.cancelEditName();
			skit_alert("角色权限组名称不能超过16个字.");
			return false;
		}
		else if( treeNode.id == <ww:property value='userRole'/> )
		{
			myZtree.cancelEditName();
			skit_alert("不能对自己的根角色权限组进行命名.");
			return false;
		}
		_oldName = treeNode.name;
		return true;
	}
	
	function onRename(e, treeId, treeNode, isCancel) 
	{
		if( isCancel )
		{
			return;
		}
		//执行新增
		if( treeNode.id == -1 )
		{
			presetRole(treeNode);
			return;
		}
		//确认改名
		skit_confirm("您确定要将角色权限组名称改为"+treeNode.name, function(yes){
			if( yes )
			{
				skit_showLoading();
				RoleMgr.renameRole(treeNode.id, treeNode.name, timestamp, {
					callback:function(rsp) {
						skit_hiddenLoading();
						skit_alert(rsp.message);
						try
						{
							timestamp = rsp.timestamp>0?rsp.timestamp:timestamp;
							if( rsp.succeed )
							{
							}
							else
							{
								if( rsp.result )
								{
									onSaveRole(timestamp, rsp.result);
								}
								else
								{
									treeNode.name = _oldName;
									myZtree.updateNode (treeNode);
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
			}
			else
			{
				treeNode.name = _oldName;
				myZtree.updateNode (treeNode);
			}
		});
	}
	
	function onRightClick(event, treeId, treeNode)
	{
		if( treeNode.id==-1 ) return;
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
		var html = "<i class='skit_fa_icon_red skit_fa_btn fa fa-minus-circle'></i> 禁用角色权限组";
		if( treeNode.abort )
		{
			var html = "<i class='skit_fa_icon_blue skit_fa_btn fa fa-check-circle'></i> 启用角色权限组";
		}
		document.getElementById("liRoleState").style.display = treeNode.id==1?"none":"";
		document.getElementById("liRename").style.display = treeNode.id==1?"none":"";
		document.getElementById("liRoleState").innerHTML = html;
	}
	
	function onClick(event, treeId, treeNode)
	{
		myZtree.expandNode(treeNode, true);
		//myZtree.expandNode(treeNode, null, null, null, true);
		presetRole(treeNode);
	}

	$(document).ready(function(){
		var json = '<ww:property value="jsonData" escape="false"/>';
		try
		{
			var zNodes = jQuery.parseJSON(json);
			$.fn.zTree.init($("#myZtree"), setting, zNodes);
			myZtree = $.fn.zTree.getZTreeObj("myZtree");
			myZtreeMenu = $("#rMenu");
			var managerid = getUserActionMemory("role!manager.action");
			var node = myZtree.getNodeByParam("id", managerid);
			if( node )
			{
				myZtree.selectNode(node);
				myZtree.expandNode(node, null, null, null, true);
				presetRole(node);
			}
		}
		catch(e)
		{
			alert(e);
		}
	});
	//#########################################################################
function renameRole()
{
	hideRMenu();
	var node = myZtree.getSelectedNodes()[0];
	myZtree.editName(node);
}
function addRole()
{
	hideRMenu();
	var node = myZtree.getSelectedNodes()[0];
	var newNode = new Object();
	for( var key in node )
	{
		if( key == "children" ||
			key == "level" ||
			key == "tId" ||
			key == "nomenu" ||
			key == "parentTId" ||
			key.indexOf("check") == 0 ||
			key.indexOf("chk") == 0 ||
			key.indexOf("get") == 0 ||
			key == "editNameStatus" ||
			key == "isAjaxing" ||
			key == "isFirstNode" ||
			key == "isLastNode" ||
			key == "open" ||
			key == "halfCheck" ||
			key == "zAsync" ||
			key == "editNameFlag" ||
			key == "nocheck" ||
			key == "chkDisabled" ||
			key == "chkDisabled" ||
			key == "isHover" 
			) continue;
		newNode[key] = node[key];
	}
	//showObject(newNode);
	newNode.ico = "skit_fa_icon_blue fa fa-plus-circle";
	newNode.id = -1;
	newNode.name = newNode.name+"的子角色权限组";
	var nodes = myZtree.addNodes(node, newNode);
	myZtree.editName(nodes[0]);
	myZtree.selectNode(nodes[0], false);
	myZtree.expandNode(nodes[0], true);//, null, null, null, true);
	presetRole(nodes[0]);
}
	
function setRoleSateFlag(treeNode, state)
{
	if( state == 1 )
	{
		var ico_fa_role = document.getElementById( "ico_fa_role_" + treeNode.tId);
	if( ico_fa_role ) ico_fa_role.parentNode.removeChild(ico_fa_role);
		treeNode["abort"] = false;
		treeNode["ico"] = "";
	}
	else
	{
		treeNode["abort"] = true;
		treeNode["ico"] = "skit_fa_icon_gray fa-minus-circle";
		var aObj = $("#" + treeNode.tId + "_a");
		if( aObj ) aObj.after("<i class='fa "+treeNode.ico+"' id='ico_fa_role_" + treeNode.tId + "'></i>");
	}

	if( treeNode && treeNode["children"] )
	{
		for(var i = 0; i < treeNode.children.length; i++)
		{
			var child = treeNode.children[i];
			if( child )
			{
				setRoleSateFlag(child, state);
			}
		}
	}
}

function setRoleState()
{
	hideRMenu();
	var treeNode = myZtree.getSelectedNodes()[0];
	var state = treeNode.abort?1:0;
	if( state == 1 )
	{
		doSetRoleState(treeNode, state);
	}
	else
	{
		if( treeNode.id == 1 )
		{
			skit_alert("您不能禁用【系统管理员组】角色权限组。");
			return;
		}
		skit_confirm("您确定要禁用角色权限组【"+treeNode.name+"】吗？该角色权限组下所有用户登录后将无法使用角色权限功能。", function(yes){
			if( yes )
			{
				doSetRoleState(treeNode, state);
			}
		});
	}
}

function doSetRoleState(treeNode, state)
{
	skit_showLoading();
	RoleMgr.setRoleState(treeNode.id, state==0?true:false, timestamp, {
		callback:function(rsp) {
			skit_hiddenLoading();
			skit_alert(rsp.message);
			try
			{
				timestamp = rsp.timestamp>0?rsp.timestamp:timestamp;
				if( rsp.succeed )
				{
					setRoleSateFlag(treeNode, state);
				}
				else if( rsp.result )
				{
					onSaveRole(timestamp, rsp.result);
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
}

function delRole()
{
	hideRMenu();
	var treeNode = myZtree.getSelectedNodes()[0];
	skit_confirm("您确定要删除角色权限组【"+treeNode.name+"】吗？该角色权限组下所有用户登录后将被禁止登录。", function(yes){
		if( yes ){
			RoleMgr.delRole(treeNode.id, timestamp, {
				callback:function(rsp) {
					skit_hiddenLoading();
					skit_alert(rsp.message);
					try{
						timestamp = rsp.timestamp>0?rsp.timestamp:timestamp;
						if( rsp.succeed ){
							onSaveRole(timestamp, rsp.result);
						}
						else{
							skit_alert(rsp.message);
						}
					}
					catch(e){
						alert(e);
					}
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
	});
}

function cancelAddRole()
{
	var node = myZtree.getSelectedNodes()[0];
	if( node.id == -1 )
	{
		var parent = node.getParentNode();
		myZtree.removeNode(node);
		parent["isParent"] = true;
		document.getElementById( parent.tId + "_ico").className = "button ico_open";
		myZtree.selectNode(parent);
		document.getElementById("divCancelAddRole").style.display = "none";
		presetRole(parent);
	}
}

function presetRole(treeNode)
{
	if( treeNode.abort )
	{
		skit_confirm("该角色权限组【"+treeNode.name+"】权限已经被禁用，若希望该角色权限组下用户使用授权功能，需要激活该角色权限组。", function(yes){
			if( yes )
			{
				setRoleState();
			}
		});
	}
	document.getElementById("divSave").style.display = "";
	if( treeNode.id == -1 ){
		document.getElementById("headPrivileges").innerHTML = "<i class='skit_fa_btn fa fa-plus-circle'></i>新增权限配置["+treeNode.name+"]";
		document.getElementById("divCancelAddRole").style.display = "";
		tour2();
	}
	else
	{
		if( treeNode.id != 1&& treeNode.id == <ww:property value='userRole'/> )
		{
			document.getElementById("divSave").style.display = "none";
			document.getElementById("headPrivileges").innerHTML = "<i class='skit_fa_btn fa fa-user-md'></i>查看权限配置["+treeNode.name+"]["+treeNode.id+"]";
		}
		else
		{
			document.getElementById("headPrivileges").innerHTML = "<i class='skit_fa_btn fa fa-edit'></i>修改权限配置["+treeNode.name+"]["+treeNode.id+"]";
		}
		document.getElementById("divCancelAddRole").style.display = "none";
		tour1();
		setUserActionMemory("role!manager.action", treeNode.id);
	}

	var parent = treeNode.getParentNode();
	var pid = parent?parent.id:-1;
	var url = "role!preset.action?pid="+pid+"&id="+treeNode.id;
	document.getElementById("iRolePrivileges").src = url;
}

function confirmSaveRole()
{
	hideRMenu();
	var treeNode = myZtree.getSelectedNodes()[0];
	var tips = "您确定要保存角色权限组【"+treeNode.name+"】的权限配置吗？如果该角色权限组是启用状态，该角色权限组下所有用户登录后将使用被授权角色权限功能。";
	skit_confirm(tips, function(yes){
		if( yes )
		{
			var ifr = window.frames['iRolePrivileges'];
			if( ifr )
			{
				if( ifr.window && ifr.window.saveRole )
				{
					var parent = treeNode.getParentNode();
					var pid = parent?parent.id:-1;
					ifr.window.saveRole(pid, treeNode.id, treeNode.name);
				}
			}
		}
	});
}

function onSaveRole(ts, json)
{
	timestamp = ts;
	var node = myZtree.getSelectedNodes()[0];
	var id = node.id;
	myZtree.destroy();
	var zNodes = jQuery.parseJSON(json);
	$.fn.zTree.init($("#myZtree"), setting, zNodes);
	myZtree = $.fn.zTree.getZTreeObj("myZtree");
	myZtreeMenu = $("#rMenu");
	expandAll(true);
	node = myZtree.getNodeByParam("id", id);
	if( node )
	{
		myZtree.selectNode(node);
		presetRole(node);
	}
}

function onDrop(event, treeId, treeNodes, targetNode, moveType, isCopy)
{
	if( moveType )
	{
		try
		{
			var node = treeNodes[0];
			var targetPid = targetNode.id;
			if( moveType != "inner" )
			{
				var parentTarget = targetNode.getParentNode();
				if( !parentTarget ) parentTarget == targetNode;
				targetPid = parentTarget.id;
			}
			//alert(moveType+" source["+parentSource.path+":"+treeNodes[0].path+"]   target["+parentTarget.path+":"+targetNode.path+"]");
			RoleMgr.dragDropRole(moveType, node.id, targetNode.id, targetPid, timestamp, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						timestamp = rsp.timestamp>0?rsp.timestamp:timestamp;
						if( rsp.succeed )
						{
						}
						else
						{
							skit_alert(rsp.message);
						}
						if( rsp.result )
						{
							onSaveRole(timestamp, rsp.result);
						}
					}
					catch(e)
					{
						skit_alert("拖拽角色权限组操作出现异常"+e);
					}
				},
				timeout:30000,
				errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
			});			
		}
		catch(e)
		{
			skit_alert(e);
		}
	}
}
</SCRIPT>
<script src="skit/js/bootstrap-tour.min.js"></script>
<SCRIPT type="text/javascript">
function tour()
{
	var showTour = getUserActionMemory("tourRolemgr");
	if( showTour != "1" )
	{
		setUserActionMemory("tourRolemgr", "1");
		var tour = new Tour({
		    steps: [{
		        element: "#divZtree",
		        title: "角色权限组配置树",
		        content: "可通该导航树配置角色，包括新增、改名、禁用启用，以及移动归属位置。"
		    },{
		        element: "#divPrivileges",
		        title: "可视化编辑",
		        content: "点击左侧导航角色项进行编辑。",
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
tour();
function tour1()
{
	var showTour = getUserActionMemory("tourRolemgrSet");
	if( showTour != "1" )
	{
		setUserActionMemory("tourRolemgrSet", "1");
		var tour = new Tour({
		    steps: [{
		        element: "#divPrivileges",
		        title: "权限导航树",
		        content: "在导航树上勾选权限项。",
		        placement: "left"
		    },{
		        element: "#divSave",
		        title: "保存权限配置",
		        content: "点击该按钮修改才有效。",
		        placement: "bottom"
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

function tour2()
{
	var showTour = getUserActionMemory("tourRolemgrAdd");
	if( showTour != "1" )
	{
		setUserActionMemory("tourRolemgrAdd", "1");
		var tour = new Tour({
		    steps: [{
		        element: "#divPrivileges",
		        title: "新增角色权限组",
		        content: "新增新的角色权限组，在导航树上勾选权限项。",
		        placement: "left"
		    },{
		        element: "#divSave",
		        title: "保存角色权限组配置",
		        content: "点击该按钮新增才有效。",
		        placement: "bottom"
		    },{
		        element: "#divCancelAddRole",
		        title: "取消新增角色权限组",
		        content: "角色权限组创建后不允许被删除。",
		        placement: "bottom"
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
if( window.top != window && top && window.top.resizeFrame )
{
	window.top.resizeFrame();
}
</SCRIPT>
</html>