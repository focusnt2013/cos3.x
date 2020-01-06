<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<link type="text/css" href="skit/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/UserMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body onResize='resizeWindow()' style='overflow-y:hidden;'>
<TABLE style='width:100%;margin-top:3px;margin-bottom:3px;'>
<TR><TD style='width:260px;padding-right:3px;'>
        <div class="panel panel-default" style='border: 1px solid #aaaaaa' id='divZtree'>
   			<div class="panel-heading"><i class='skit_fa_btn fa fa-group'></i>用户导航
	   			<div style='float:right;right:4px;top:0px;' id='divSavePrivileges'>
	 				<button type="button" class='btn btn-outline btn-primary btn-xs' onclick='addUser(<ww:property value='userRole'/>)'>
	 					<i class='fa fa-user-plus'></i> 新增</button>
                </div>
            </div>
   			<div class="panel-body" style='padding: 0px;'>
   				<div class="input-group custom-search-form" style='padding:3px;'>
                    <input class="form-control" placeholder="输入关键字找用户" id='searchBar' onkeyup='return searchUser(this);' type="text" style='font-size: 12px;height:28px;'>
                    <span class="input-group-btn">
                        <button class="btn btn-inverse" type="button" onclick='searchUser()'>
                            <i class="fa fa-search"></i>
                        </button>
                    </span>
                </div>
                <div id='divUsers'>
					<ul id='myZtree' class='ztree'></ul>
					<div id="rMenu">
						<ul>
							<li onclick="addUser();" id='liAddUser'><i class='skit_fa_icon_blue skit_fa_btn fa fa-user-plus'></i> 新增用户</li>
							<li onclick="resetPassword();" id='liPassword'><i class='skit_fa_icon_red skit_fa_btn fa fa-user-secret'></i> 重置密码</li>
							<li onclick="setUserState();" id='liUserState'><i class='skit_fa_icon_red skit_fa_btn fa fa-user-times'></i> 停用该账户</li>
							<li onclick="sendSysnotify();" id='liSysnotify'><i class='skit_fa_icon_blue skit_fa_btn fa fa-envelope'></i> 发送系统消息</li>
						</ul>
					</div>
				</div>
   			</div>
   		</div>		
	</TD>
	<TD>
        <div class="panel panel-default" style='border: 1px solid #aaaaaa' id='divUser'>
   			<div class="panel-heading"><span id='headUser'><i class='skit_fa_btn fa fa-users'></i>用户列表查询</span>
                <div style='float:right;right:4px;top:0px;display:none' id='divSave'>
 				<button type="button" class='btn btn-outline btn-primary btn-xs' onclick='setUser()'>
 					<i class='fa fa-save'></i> 保存用户配置</button>
                </div>
                <div style='float:right;padding-right:4px;top:0px;display:none' id='divCancelAddUser'>
 				<button type="button" class="btn btn-outline btn-danger btn-xs" onclick='cancelAddUser()'>
 					<i class='fa fa-close'></i> 取消新增用户</button>
                </div>
	        </div>
   			<div class="panel-body" style='padding: 0px;'>
				<iframe name='iUser' id='iUser' style='width:100%;border:0px;' src="" class='nonicescroll'></iframe>
   			</div>
   		</div>
	</TD>
	<TD style='padding-left:3px;width:260px;display:none' id='tdPrivileges'>
        <div class="panel panel-default" style='border: 1px solid #aaaaaa' id='divPrivileges'>
   			<div class="panel-heading"><span><i class='skit_fa_btn fa fa-check-square-o'></i>用户权限配置</span>
                <div style='float:right;right:4px;top:0px;display:' id='divSavePrivileges'>
 				<button type="button" class='btn btn-outline btn-primary btn-xs' onclick='setPrivileges()'>
 					<i class='fa fa-save'></i> 设置</button>
                </div>
                <div style='float:right;padding-right:4px;top:0px;display:none' id='divClearPrivileges'>
 				<button type="button" class="btn btn-outline btn-danger btn-xs" onclick='clearPrivileges()'>
 					<i class='fa fa-close'></i> 清除</button>
                </div>
	        </div>
   			<div class="panel-body" style='padding: 0px;'>
				<iframe name='iPrivileges' id='iPrivileges' style='width:100%;border:0px;'></iframe>
   			</div>
   		</div>
	</TD>
</TR>
</TABLE>
</body>
<SCRIPT type="text/javascript">
function resizeWindow()
{
	var div = document.getElementById( 'divUsers' );
	div.style.height = windowHeight - 72;
	div = document.getElementById( 'iPrivileges' );
	div.style.height = windowHeight - 38;
	div = document.getElementById( 'iUser' );
	div.style.height = windowHeight - 38;
}
</SCRIPT>

<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<SCRIPT type="text/javascript">
$( '#divUsers' ).niceScroll({
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
		check: {
			enable: false
		},
		callback: {
			onRightClick: onRightClick,
			onClick: onClick,
		},
		callback: {
			onRightClick: onRightClick,
			onClick: onClick,
			beforeDrag: beforeDrag,
			beforeDrop: beforeDrop,
			beforeDragOpen: beforeDragOpen,
			onDrag: onDrag,
			onDrop: onDrop,
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
			showRenameBtn: false
		},
		view: {
			addHoverDom: addHoverDom,
			removeHoverDom: removeHoverDom,
			addDiyDom: addDiyDom
		}
	};

	function beforeDrop(treeId, treeNodes, targetNode, moveType, isCopy) {
		if( targetNode.id == 1 && moveType != "inner"  )
		{
			skit_alert("不能将用户移动到角色权限组外。");
			return false;
		}
		if( targetNode.id == -1  )
		{
			skit_alert("不能将用户移动到【未分配角色用户】组下");
			return false;
		}
		return true;
	}
	function showRenameBtn(treeId, treeNode) {
		return treeNode.id!=1;
	}	
	
	var _oldName;
	function beforeRename(treeId, treeNode, newName, isCancel) {
		if (newName.length == 0) {
			myZtree.cancelEditName();
			skit_alert("角色名称不能为空.");
			return false;
		}
		else if (newName.length > 16) {
			myZtree.cancelEditName();
			skit_alert("角色名称不能超过16个字.");
			return false;
		}
		_oldName = treeNode.name;
		return true;
	}
	
	
	function addHoverDom(treeId, treeNode) {
		if( treeNode.id == -1 || treeNode.id == -2 ) return;
		if( !treeNode.isParent ) return;
		var sObj = $("#" + treeNode.tId + "_span");
		if ( $("#addBtn_"+treeNode.tId).length>0) return;
		var addStr = "<span class='button add' id='addBtn_" + treeNode.tId+ "' title='新增用户' onfocus='this.blur();'></span>";
		sObj.after(addStr);
		var btn = $("#addBtn_"+treeNode.tId);
		if (btn) btn.bind("click", function(){
			addUser();
		});
	};
	
	function removeHoverDom(treeId, treeNode) {
		$("#addBtn_"+treeNode.tId).unbind().remove();
	};

	function addDiyDom(treeId, treeNode) {
		var editStr = "";
		if( treeNode.abort == 2 ){
			treeNode["ico"] = "skit_fa_icon_gray fa-times-circle";
		}
		else if( treeNode.abort == 3 ){
			treeNode["ico"] = "skit_fa_icon_gray fa-minus-circle";
		}
		if( treeNode.ico )
		{
			editStr = "<i class='fa "+treeNode.ico+"' id='ico_fa_role_" + treeNode.tId + "'></i>";
			var aObj = $("#" + treeNode.tId + "_a");
			if( aObj ) aObj.after(editStr);
		}
	}
	
	function onRightClick(event, treeId, treeNode)
	{
		if( treeNode.id==-1) return;
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
		document.getElementById("liUserState").style.display = treeNode.isParent?"none":"";
		document.getElementById("liPassword").style.display = treeNode.isParent?"none":"";
		document.getElementById("liAddUser").style.display = treeNode.isParent?"":"none";
		document.getElementById("liSysnotify").style.display = !treeNode.isParent||treeNode.usercount?"":"none";
	}
	
	function onClick(event, treeId, treeNode)
	{
		//myZtree.expandNode(treeNode, null, null, null, true);
		myZtree.expandNode(treeNode, true);
		presetUser(treeNode);
	}

	$(document).ready(function(){
		try
		{
			var zNodes = <ww:property value="jsonData" escape="false"/>;
			$.fn.zTree.init($("#myZtree"), setting, zNodes);
			myZtree = $.fn.zTree.getZTreeObj("myZtree");
			myZtreeMenu = $("#rMenu");

			var managerid = getUserActionMemory("user!manager.action");
			var node = myZtree.getNodeByParam("id", managerid);
			if( node )
			{
				myZtree.selectNode(node);
				myZtree.expandNode(node, null, null, null, true);
				presetUser(node);
			}
		}
		catch(e)
		{
			alert(e);
		}
	});
	//#########################################################################
function setUserStateFlag(treeNode, state)
{
	if( state == 1 )
	{
		var ico_fa_role = document.getElementById( "ico_fa_role_" + treeNode.tId);
		if( ico_fa_role ) ico_fa_role.parentNode.removeChild(ico_fa_role);
			treeNode["abort"] = 1;
			treeNode["ico"] = "";
	}
	else
	{
		treeNode["abort"] = state;
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
				setUserStateFlag(child, state);
			}
		}
	}
}

function setUserState()
{
	hideRMenu();
	var treeNode = myZtree.getSelectedNodes()[0];
	var state = treeNode.abort;//?1:0;
	if( state == 1 )
	{
		if( treeNode.id == "<ww:property value='account'/>" )
		{
			skit_alert("您不能禁用自己。");
			return;
		}

		skit_confirm("您确定要禁用用户【"+treeNode.rname+"】吗？该用户将无法登录。", function(yes){
			if( yes )
			{
				doSetUserState(treeNode, 0);
			}
		});
	}
	else
	{
		doSetUserState(treeNode, 1);
	}
}

function doSetUserState(treeNode, state)
{
	skit_showLoading();
	UserMgr.setUserState(treeNode.id, state==0?true:false, timestamp, {
		callback:function(rsp) {
			skit_hiddenLoading();
			skit_alert(rsp.message);
			try
			{
				timestamp = rsp.timestamp>0?rsp.timestamp:timestamp;
				if( rsp.succeed )
				{
					setUserStateFlag(treeNode, state);
				}
				else if( rsp.result )
				{
					myZtree.destroy();
					var zNodes = jQuery.parseJSON(rsp.result);
					$.fn.zTree.init($("#myZtree"), setting, zNodes);
					myZtree = $.fn.zTree.getZTreeObj("myZtree");
					expandAll(true);
					myZtreeMenu = $("#rMenu");
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

function cancelAddUser()
{
	var nodes = myZtree.getSelectedNodes();
	if( nodes && nodes.length > 0 )
	{
		treeNodeSelection = null;
		presetUser(nodes[0]);
	}
	else
	{
		document.getElementById("headUser").innerHTML = "<i class='skit_fa_btn fa fa-users'></i>用户列表查询";
		document.getElementById("divCancelAddUser").style.display = "none";
		document.getElementById("divSave").style.display = "none";
		var url = "user!query.action?account=<ww:property value='account'/>";
		document.getElementById("tdPrivileges").style.display = "none";
		document.getElementById("iUser").src = url;
	}
}

function getUserRoleids(node)
{
	var ids = "";
	if(node.isParent)
	{
		ids += ",";
		ids += node.id;
	}
	if( node["children"] )
	{
		for(var i = 0; i < node.children.length; i++)
		{
			var child = node.children[i];
			if( child )
			{
				ids += getUserRoleids(child);
			}
		}
	}
	return ids;
}

function addUser(pid)
{
	if( pid ){}else
	{
		hideRMenu();
		var treeNode = myZtree.getSelectedNodes()[0];
		if( treeNode.isParent )
		{
			pid = treeNode.id;
		}
	}
	document.getElementById("divSave").style.display = "";
	document.getElementById("headUser").innerHTML = "<i class='skit_fa_btn fa fa-plus-circle'></i>新增用户";
	document.getElementById("divCancelAddUser").style.display = "";
	document.getElementById("tdPrivileges").style.display = "";
	var url = "user!preset.action?v=2&pid="+pid;
	document.getElementById("iUser").src = url;
}

var treeNodeSelection;
function presetUser(treeNode)
{
	if( treeNodeSelection == treeNode ) return;
	treeNodeSelection = treeNode;
	if( treeNode.isParent )
	{
		document.getElementById("headUser").innerHTML = "<i class='skit_fa_btn fa fa-users'></i>用户列表查询["+treeNode.rname+"]";
		document.getElementById("divCancelAddUser").style.display = "none";
		document.getElementById("divSave").style.display = "none";
		var url = "";
		if( treeNode.id == -1 )
		{
			var node = treeNode;
			if( node["children"] )
			{
				var ids = "";
				for(var i = 0; i < node.children.length; i++)
				{
					var child = node.children[i];
					if( child.roleid )
					{
						ids += ",";
						ids += child.roleid;
					}
				}
				url = "user!query.action?account=<ww:property value='account'/>&id="+ids;
			}
		}
		else
		{
			url = "user!query.action?account=<ww:property value='account'/>&id="+getUserRoleids(treeNode);
			setUserActionMemory("user!manager.action", treeNode.id);
		}
		document.getElementById("tdPrivileges").style.display = "none";
		document.getElementById("iUser").src = url;
		return;
	}
	if( treeNode.abort > 1 )
	{
		skit_confirm("该用户【"+treeNode.rname+"】已经被禁止登录。您是否同意启用激活该用户？", function(yes){
			if( yes )
			{
				setUserState();
			}
		});
	}
	document.getElementById("divSave").style.display = "";
	document.getElementById("headUser").innerHTML = "<i class='skit_fa_btn fa fa-edit'></i>修改用户配置["+treeNode.rname+"]";
	document.getElementById("divCancelAddUser").style.display = "none";
	//tour1();
	document.getElementById("tdPrivileges").style.display = "";
	var url = "user!preset.action?id="+treeNode.id;
	document.getElementById("iUser").src = url;
	url = "user!privileges.action?id="+treeNode.id;
	document.getElementById("iPrivileges").src = url;
	$( '#iUser' ).niceScroll({cursorcolor: '#aaa',railalign: 'right', cursorborder: "none", horizrailenabled: false, zindex: 2001, left: '245px', cursoropacitymax: 0.6, cursorborderradius: "0px", spacebarenabled: false });
	$( '#iPrivileges' ).niceScroll({cursorcolor: '#aaa',railalign: 'right', cursorborder: "none", horizrailenabled: false, zindex: 2001, left: '245px', cursoropacitymax: 0.6, cursorborderradius: "0px", spacebarenabled: false });
}

function setUser()
{
	var ifr = window.frames['iUser'];
	if( ifr )
	{
		if( ifr.window && ifr.window.setUser )
		{
			ifr.window.setUser(function(tips, succeed, account, name, ts, users){
				skit_alert(tips);
				if(succeed){
					timestamp = ts;
					myZtree.destroy();
					$.fn.zTree.init($("#myZtree"), setting, jQuery.parseJSON(users));
					myZtree = $.fn.zTree.getZTreeObj("myZtree");
					myZtreeMenu = $("#rMenu");
					var nodes = myZtree.getNodesByParamFuzzy("name", name+"("+account+")", null);
					//alert(name+"("+account+"): "+nodes.length);
					for(var i = 0; i < nodes.length; i++)
					{
						if( nodes[i].isParent ) continue;
						myZtree.selectNode(nodes[i]);
					//alert(nodes[i].name);
						presetUser(nodes[i]);
						break;
					}
				}
			});
		}
	}
}

function setPrivileges()
{
	var ifr = window.frames['iPrivileges'];
	if( ifr )
	{
		if( ifr.window && ifr.window.setPrivileges )
		{
			ifr.window.setPrivileges();
		}
	}
}

function searchUser(input)
{
	var text = document.getElementById( "searchBar" ).value;
	text = text.trim();
	if( text == "" )
	{
		return false;
	}
	
	var nodes = myZtree.getNodesByParamFuzzy("name", text, null);
	for(var i = 0; i < nodes.length; i++)
	{
		if( nodes[i].isParent ) continue;
		myZtree.selectNode(nodes[i]);
		presetUser(nodes[i]);
		break;
	}
}

function showClearPrivilegesButton()
{
	document.getElementById("divClearPrivileges").style.display = "";
}

function hideClearPrivilegesButton()
{
	document.getElementById("divClearPrivileges").style.display = "none";
}

function clearPrivileges()
{
	var treeNode = myZtree.getSelectedNodes()[0];
	skit_confirm("你将清除用户【"+treeNode.rname+"】的所有用户私有权限，该用户将恢复默认拥有角色权限组赋予的权限，请确认？", function(yes){
		if( yes )
		{
			UserMgr.clearPrivileges(treeNode.id, treeNode.roleid, timestamp, {
				callback:function(rsp) {
					skit_hiddenLoading();
					try
					{
						skit_alert(rsp.message);
						timestamp = rsp.timestamp>0?rsp.timestamp:timestamp;
						if( rsp.succeed )
						{
							var url = "user!privileges.action?id="+treeNode.id;
							document.getElementById("iPrivileges").src = url;
						}
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
			UserMgr.dragDropRole(node.id, targetPid, timestamp, {
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
							myZtree.destroy();
							var zNodes = jQuery.parseJSON(rsp.result);
							$.fn.zTree.init($("#myZtree"), setting, zNodes);
							myZtree = $.fn.zTree.getZTreeObj("myZtree");
							myZtreeMenu = $("#rMenu");
							expandAll(true);
						}
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
		catch(e)
		{
			skit_alert(e);
		}
	}
}

function sendSysnotify()
{
	hideRMenu();
	var node = myZtree.getSelectedNodes()[0];
	var to;
	if( node.isParent )
	{
		tips = "向角色权限组【"+node.rname+"】所有用户发消息";
	}
	else
	{
		tips = "向用户【"+node.rname+"】发消息";
		to = node.id;
	}
	top.skit_text(tips,'',function(yes, val){
		if( yes ){
			top.sendSystemNotify(to, "用户互动", "<ww:property value='account'/>向您发送了一条互动消息", val);
		}
	}, "请输入您要发送的消息，不允许为空。");		
}

var inputResetPassword = document.createElement("input");
inputResetPassword.id = "inputResetPassword";
function resetPassword()
{
	var node = myZtree.getSelectedNodes()[0];
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
		span.innerHTML = "<i class='fa fa-key'></i>用户密码";
		div3.appendChild(span);
		inputResetPassword.className = "form-control";
		inputResetPassword.type = "password";
		inputResetPassword.placeholder = "请输入密码必须是字母数字以及符号，不少于6位";
		div3.appendChild(inputResetPassword);
	
		var SM = new SimpleModal({"btn_ok":"重置密码","btn_cancel":"取消","width":520});
	    var content = "<div style='height:96px;width:480px;border:0px solid red;overflow-y:auto;'>"+div0.outerHTML+"</div>"
	    SM.show({
	    	"title":"为用户【"+node.rname+"】重置密码",
	        "model":"confirm",
	        "callback": function(){
	        	inputResetPassword = document.getElementById(inputResetPassword.id);
	        	var password = inputResetPassword.value;
				if( password == "" )
				{
					skit_errtip("密码不允许为空.", inputResetPassword);
			        return false;
				}
		        if( !password.match(new RegExp("^([A-Za-z])|([a-zA-Z0-9])|([a-zA-Z0-9])|([a-zA-Z0-9_])+$")) )
		        {
		        	skit_errtip("密码必须是字母数字以及下划线符号", inputResetPassword);
			        return false;
		        }
				if( password.length < 6 || password.length > 64 )
		        {
					skit_errtip("密码不少于6个字不超过64个字", inputResetPassword);
			        return false;
		        }

				skit_showLoading();
				UserMgr.resetPassword(node.id, password, timestamp, {
					callback:function(rsp) {
						skit_hiddenLoading();
						skit_alert(rsp.message);
						try
						{
							timestamp = rsp.timestamp>0?rsp.timestamp:timestamp;
							if( rsp.succeed )
							{
							}
							else if( rsp.result )
							{
								myZtree.destroy();
								var zNodes = jQuery.parseJSON(rsp.result);
								$.fn.zTree.init($("#myZtree"), setting, zNodes);
								myZtree = $.fn.zTree.getZTreeObj("myZtree");
								expandAll(true);
								myZtreeMenu = $("#rMenu");
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

function changeCreator(account, creator, callback){
	if( account == creator ){
		callback(true);
		return;
	}
	window.top.skit_confirm("您是否要更改账户【"+account+"】归属为【"+creator+"】",function(yes){
		if( yes ){
			UserMgr.changeCreator(account, creator, {
				callback:function(rsp) {
					skit_hiddenLoading();
					skit_alert(rsp.message);
					callback(rsp.succeed);
				},
				timeout:30000,
				errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
			});
		}
		else{
			callback(false);
		}
	});
}
</SCRIPT>
<link href="skin/defone/css/simplemodal.css" rel="stylesheet">
<script src="skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="skin/defone/js/simple-modal.js?v=3"></script>
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
		        title: "角色配置树",
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
//tour();
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
		        title: "新增角色",
		        content: "新增新的角色，在导航树上勾选权限项。",
		        placement: "left"
		    },{
		        element: "#divSave",
		        title: "保存角色配置",
		        content: "点击该按钮新增才有效。",
		        placement: "bottom"
		    },{
		        element: "#divCancelAddUser",
		        title: "取消新增角色",
		        content: "角色创建后不允许被删除。",
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
</SCRIPT>
</html>