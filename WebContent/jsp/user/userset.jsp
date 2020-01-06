<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response); %>
<html>
<head>
<link type="text/css" href="<%=Kit.URL_PATH(request)%>skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="<%=Kit.URL_PATH(request)%>skit/css/bootstrap.css" rel="stylesheet">
<link type="text/css" href="<%=Kit.URL_PATH(request)%>skit/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
<style type='text/css'>
ul.ztree {
    margin-top: 10px;
    border: 1px solid #617775;
    background: #f0f6e4;
    width: 220px;
    height: 360px;
    overflow-y: scroll;
    overflow-x: auto;
}

.form-control:hover{
	cursor: pointer;
}
</style>
<%=Kit.getDwrJsTag(request,"interface/UserMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body style="margin:0px;">
<table style='width:100%' border='0'>
<tr><td>
<div class='' style='padding:0px 0px;margin-bottom:10px;' id='thisbody'>
	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-user'></i>用户基本信息 <ww:property value='account'/></div>
		<div class="panel-body" style='padding-bottom: 0;'>
			<div class="form-group" style="display:<ww:if test='account!=null'>none</ww:if>">
				<div class="input-group">
					<span class="input-group-addon">用户账号<span class='fa fa-user-secret' style='margin-left:6px'></span></span>
					<input class="form-control" type="text" id='userAccount'
						 value="<ww:property value='theuser.username'/>"
						 <ww:if test='account!=null'>readonly='true'</ww:if>
	                	data-title='用户账号不少于4个字不超过64个字，系统内不允许重复，必须是字母数字以及下划线符号'
						data-toggle="tooltip" 
						data-placement="top"
						data-trigger='manual'
						onkeydown='$("#userAccount").tooltip("hide")'
	                	placeholder="请输入用户账号名称不超过16个字，系统内不允许重复"
					 >
				</div>
			</div>
			<ww:if test='account==null'>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">用户密码<span class='fa fa-key' style='margin-left:6px'></span></span>
					<input class="form-control" type="password" id='userPassword'
	                	data-title='用户密码不少于6个字不超过64个字，必须是字母数字以及下划线符号'
						data-toggle="tooltip" 
						data-placement="top"
						data-trigger='manual'
						onkeydown='$("#userPassword").tooltip("hide")'
	                	placeholder="请输入用户密码不少于6个字"
					>
				</div>
			</div>
			</ww:if>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">真实姓名<span class='fa fa-credit-card' style='margin-left:6px'></span></span>
					<input class="form-control" type="text" id='userName'
						value="<ww:property value='theuser.realname'/>"
	                	data-title='用户真实姓名不超过10个字，不少于2个字'
						data-toggle="tooltip" 
						data-placement="top"
						data-trigger='manual'
						onkeydown='$("#userName").tooltip("hide")'
	                	placeholder="请输入用户真实姓名不超过16个字"
					 >
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">用户邮箱<span class='fa fa-at' style='margin-left:6px'></span></span>
					<input class="form-control" type="text" id='userEmail'
						value="<ww:property value='theuser.email'/>" 
	                	data-title='用户账号必须设置正确的邮箱地址'
						data-toggle="tooltip" 
						data-placement="top"
						data-trigger='manual'
						onkeydown='$("#userEmail").tooltip("hide")'
	                	placeholder="请输入用户邮箱地址"
						>
				</div>
			</div>
			<div class="form-group" id='roleSelForm'>
   				<div class="input-group">
					<span class="input-group-addon">角色权限<span class='fa fa-group' style='margin-left:6px'></span></span>
					<input type='hidden' id='roleId'>
					<input class="form-control" value="" title="" 
						type="text" id='roleSel' readonly='true' onclick="showRoleMenu();"
	                	data-title='用户必须选择归属角色权限组，请点击输入框选择角色权限组'
						data-toggle="tooltip" 
						data-placement="top"
						data-trigger='manual'
						onblur='$("#roleSel").tooltip("hide")'
	                	placeholder="请点击输入框选择角色权限组"
						>
     					<div class="input-group-btn">
     						<button type="button" class="btn btn-info" style='display:inline-block;height:34px;'
     							onclick="showRoleMenu();"><span class="caret"></span></button>
   					</div>
				</div>
			</div>
			
			<div class="form-group" <ww:if test='!sysadmin||account==null||account.equals("admin")'>style='display:none'</ww:if>>
   				<div class="input-group">
					<span class="input-group-addon">用户归属<span class='fa fa-group' style='margin-left:6px'></span></span>
					<input type='hidden' id='creatorAccount'>
					<input class="form-control" value="" title="" 
						type="text" id='creatorSel' readonly='true' onclick="showCreatorMenu();"
	                	data-title='用户必须设置归属的用户，归属用户必须是该用户的上级权限用户'
						data-toggle="tooltip" 
						data-placement="top"
						data-trigger='manual'
						onblur='$("#creatorSel").tooltip("hide")'
	                	placeholder="请点击输入框选择用户归属用户"
						>
     					<div class="input-group-btn">
     						<button type="button" class="btn btn-info" style='display:inline-block;height:34px;'
     							onclick="showCreatorMenu();"><span class="caret"></span></button>
   					</div>
				</div>
			</div>
		</div>
	</div>
	
	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-user-md'></i>扩展用户属性</div>
		<div class="panel-body" style='padding-bottom: 0;'>
			<div class="form-group" id='userSexForm'>
     			<div class="input-group">
					<span class="input-group-addon">性别
						<ww:if test='theuser.sex==3'><span class='fa fa-venus-mars' style='margin-left:6px'></span></ww:if>
					  	<ww:if test='theuser.sex==1'><span class='fa fa-mars' style='margin-left:6px'></span></ww:if>
					    <ww:if test='theuser.sex==2'><span class='fa fa-venus' style='margin-left:6px'></span></ww:if>
					</span>
					<select class="form-control" id='userSex'>
						<option value='3' <ww:if test='theuser.sex==3'>selected</ww:if> >未知</option>
						<option value='1' <ww:if test='theuser.sex==1'>selected</ww:if> >男</option>
					  	<option value='2' <ww:if test='theuser.sex==2'>selected</ww:if> >女</option>
					</select>
				</div>
			</div>
			<ww:iterator value="listData" status="loop">
			<ww:if test='has("use")&&getBoolean("use")'>
			<div class="form-group">
     			<div class="input-group">
					<span class="input-group-addon"><ww:property value='getString("name")'/>
					<ww:if test='has("icon")'><span class='fa <ww:property value='getString("icon")'/>' style='margin-left:6px'></span></ww:if>
					<ww:elseif test='getBoolean("nullable")'><span class='fa fa-circle-o' style='margin-left:6px'></span></ww:elseif>
					<ww:else><span class='fa fa-circle' style='margin-left:6px'></span></ww:else>
					</span>
					<input type='hidden' id='<ww:property value='getString("id")'/>Nullable' value='<ww:property value='getBoolean("nullable")'/>'>
					<input type='hidden' id='<ww:property value='getString("id")'/>Name' value='<ww:property value='getString("name")'/>'>
					<input class="form-control" type="text" id='userEx_<ww:property value='getString("id")'/>' 
						value="<ww:property value='getString("value")'/>"
	                	data-title='用户扩展属性不允许为空'
						data-toggle="tooltip" 
						data-placement="top"
						data-trigger='manual'
						onkeydown='$("#userEx_<ww:property value='getString("id")'/>").tooltip("hide")'
						placeholder="请输入用户扩展属性参数 <ww:if test='getBoolean("nullable")'>，该参数允许为空</ww:if>" 
					>
					<span class="input-group-addon input-group-addon-font" style='border-right: 1px solid #eee;'>
						<span class='fa fa-level-down' style='margin-left:6px'></span>继承</span>
					<span class='input-group-addon' style='border-left: 1px solid #eee;'>
						<div class='checkbox checkbox-info' style='cursor:pointer;padding-left: 0px'>
							<input type='checkbox' id='<ww:property value='getString("id")'/>_inherit'  title='勾选后创建的子账户继承该参数'
								<ww:property value='getString("inherit")'/>
							>
							<label></label>
						</div>
					</span>						
				</div>
			</div>
			</ww:if>
			</ww:iterator>
		</div>
	</div>
</div>
</td></tr>
<ww:if test='securityCallbackUrl!=null'>
<tr><td align='center'>
	<button type="button" class="btn btn-outline btn-success btn-block"
		style='width:128px;font-size:12px;' onclick='addsysuser();'><i class="fa fa-user-plus"></i> 创建系统用户 </button>
</td></tr>
</ww:if>
</table>
<div id="divRoleSelector" class="divRoleSelector" style="display:none; position: absolute; z-index: 3;">
	<ul id="myZtree" class="ztree" style="margin-top:0; width:160px;"></ul>
</div>
<div id="divCreatorSelector" class="divCreatorSelector" style="display:none; position: absolute; z-index: 3;">
	<ul id="myZtreeCreator" class="ztree" style="margin-top:0; width:160px;"></ul>
</div>
<iframe name='addsysuserCallback' id='addsysuserCallback' style='display:none;'></iframe>
</body>
<%@ include file="/skit/inc/skit_cos.inc"%>
<%@ include file="/skit/inc/skit_ztree.inc"%>
<SCRIPT type="text/javascript">
var securityCallbackUrl = "<ww:property value='securityCallbackUrl'/>";
var timestamp = <ww:property value='timestamp'/>;

var myZtreeCreator;
var setting1 = {
	view: {
		dblClickExpand: false
	},
	check: {
		enable: true,
		chkStyle: "radio",
		radioType: "all"
	},
	callback: {
		onClick: onClick1,
		onCheck: onClick1
	}
}; 
function onClick1(e, treeId, userNode) {
	if( userNode.chkDisabled ){
		skit_alert("您不能选择用户【"+userNode.name+"】作为归属用户.");
		return;
	}
	//var roleSel = $("#roleSel");
	var creatorAccount = $("#creatorAccount");
	document.getElementById("creatorSel").value = userNode.name;
	//roleSel.attr("value", roleNode.name);
	creatorAccount.attr("value", userNode.id);
	//alert(document.getElementById("creatorAccount").value);
	hideMenu();
	myZtreeCreator.checkNode(userNode, true);
}

function showCreatorMenu() {
	var creatorSel = $("#creatorSel");
	var creatorOffset = $("#creatorSel").offset();
	var width = document.getElementById("creatorSel").clientWidth;
	document.getElementById("myZtreeCreator").style.width = width;
	$("#divCreatorSelector").css({left:creatorOffset.left + "px", top:creatorOffset.top + creatorSel.outerHeight() + "px"}).slideDown("fast");
	$("body").bind("mousedown", onBodyDown);
}

var setting = {
	view: {
		dblClickExpand: false
	},
	check: {
		enable: true,
		chkStyle: "radio",
		radioType: "all"
	},
	callback: {
		onClick: onClick,
		onCheck: onClick
	}
};
function onClick(e, treeId, roleNode) {
	if( roleNode.chkDisabled ){
		skit_alert("您不能选择该角色权限【"+roleNode.name+"】赋予该账户，因为您创建的账号只能归属于您的子权限组.");
		return;
	}
	//var roleSel = $("#roleSel");
	var roleId = $("#roleId");
	document.getElementById("roleSel").value = roleNode.name;
	//roleSel.attr("value", roleNode.name);
	roleId.attr("value", roleNode.id);
	//alert(document.getElementById("roleId").value);
	hideMenu();
	myZtree.checkNode(roleNode, true);
}
function showRoleMenu() {
	var roleObj = $("#roleSel");
	var roleOffset = $("#roleSel").offset();
	var width = document.getElementById("roleSel").clientWidth;
	document.getElementById("myZtree").style.width = width;
	$("#divRoleSelector").css({left:roleOffset.left + "px", top:roleOffset.top + roleObj.outerHeight() + "px"}).slideDown("fast");
	$("body").bind("mousedown", onBodyDown);
}
function hideMenu() {
	$("#divRoleSelector").fadeOut("fast");
	$("#divCreatorSelector").fadeOut("fast");
	$("body").unbind("mousedown", onBodyDown);
}
function onBodyDown(event) {
	if (!(event.target.id == "menuBtn" || 
		event.target.id == "divRoleSelector" ||
		$(event.target).parents("#divRoleSelector").length>0) || 
		event.target.id == "divCreatorSelector" ||
		$(event.target).parents("#divCreatorSelector").length>0 ) {
		hideMenu();
	}
}
$(document).ready(function(){
	try{
		var json = <ww:property value="jsonData" escape="false"/>;
		$.fn.zTree.init($("#myZtree"), setting, json);
		myZtree = $.fn.zTree.getZTreeObj("myZtree");
		expandAll(true);
		<ww:if test='account!=null&&!account.equals("admin")'>
		json = <ww:property value="rowSelect" escape="false"/>;
		$.fn.zTree.init($("#myZtreeCreator"), setting1, json);
		myZtreeCreator = $.fn.zTree.getZTreeObj("myZtreeCreator");
		</ww:if>
		if( '<ww:property value="account"/>' ) {
			document.getElementById( "roleId" ).value = "<ww:property value='theuser.roleid'/>";
			document.getElementById( "roleSel" ).value = "<ww:property value='theuser.rolename'/>";
			document.getElementById( "creatorAccount" ).value = "<ww:property value='creator.username'/>";
			document.getElementById( "creatorSel" ).value = "<ww:property value='creator.realname'/>";
		}
		else{
			document.getElementById( "roleId" ).value = "<ww:property value='theuser.roleid'/>";
			if( document.getElementById( "roleId" ).value != "-1" ){
				document.getElementById( "roleSelForm" ).style.display = "none";
			}
		}
		var wh = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
		var color = "#a0a0a0";
		if( securityCallbackUrl ){
			wh -= 48;
		}
		document.getElementById( "thisbody" ).style.height = wh;
		$("#thisbody").niceScroll({
			cursorcolor: color,
			railalign: 'right',
			cursorborder: "none", 
			horizrailenabled: true, 
			zindex: 2001, 
			left: '0px', 
			cursoropacitymax: 0.6, 
			cursorborderradius: "0px", 
			spacebarenabled: false });
	}
	catch(e){
		alert(e);
	}
});

function setUser(callback) {
	var json = new Object();
	var account = document.getElementById( "userAccount" ).value;account = account.trim();
	var name = document.getElementById( "userName" ).value;name = name.trim();
	var email = document.getElementById( "userEmail" ).value;email = email.trim();
	var password = "";
	if( document.getElementById( "userPassword" )) 
		password = document.getElementById( "userPassword" ).value;
	password = password.trim();
	var roleid = document.getElementById( "roleId" ).value;
	var creator = document.getElementById( "creatorAccount" ).value;
	var rolename = document.getElementById( "roleSel" ).value;
	var sex = document.getElementById( "userSex" ).value;
	var inputEx = $("input[id^='userEx_']");
    
	if( '<ww:property value="account"/>' ) {
	}
	else
	{
		if( account == "" )
		{
			$("#userAccount").tooltip("show");
			document.getElementById( "userAccount" ).focus();
	        return false;
		}
        if( !account.match(new RegExp("^([A-Za-z])|([a-zA-Z0-9])|([a-zA-Z0-9])|([a-zA-Z0-9_])+$")) )
        {
			$("#userAccount").tooltip("show");
			document.getElementById( "userAccount" ).focus();
	        return false;
        }
		if( account.length > 64 || account.length < 4 )
        {
			$("#userAccount").tooltip("show");
			document.getElementById( "userAccount" ).focus();
	        return false;
        }
	    json["username"] = account;
		
		if( password == "" )
		{
			$("#userPassword").tooltip("show");
			document.getElementById( "userPassword" ).focus();
	        return false;
		}
        if( !password.match(new RegExp("^([A-Za-z])|([a-zA-Z0-9])|([a-zA-Z0-9])|([a-zA-Z0-9_])+$")) )
        {
			$("#userPassword").tooltip("show");
			document.getElementById( "userPassword" ).focus();
	        return false;
        }
		if( password.length > 64 || password.length < 6 )
        {
			$("#userPassword").tooltip("show");
			document.getElementById( "userPassword" ).focus();
	        return false;
        }
	    json["password"] = password;
	}

	if( name.length > 16 || name.length < 2)
    {
		$("#userName").tooltip("show");
		document.getElementById( "userName" ).focus();
        return false;
    }
    /*if( !name.match(new RegExp("^[a-zA-Z0-9_\u4e00-\u9fa5]+$")) )
    {
    	skit_errtip("用户真实姓名只能含有汉字、数字、字母、下划线不能以下划线开头和结尾。\r\n"+name, document.getElementById( "userName" ));
        return false;
    }*/
    json["realname"] = name;
    json["roleid"] = roleid;
    if( creator ){
	    json["creator"] = creator;
    }
	if( email == "" )
	{
		$("#userEmail").tooltip("show");
		document.getElementById( "userEmail" ).focus();
        return false;
	}
	if( !email.match(new RegExp("^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$")) )
	{
		$("#userEmail").tooltip("show");
		document.getElementById( "userEmail" ).focus();
        return false;
	}
    json["email"] = email;
	if( roleid == "" )
	{
		$("#roleSel").tooltip("show");
		document.getElementById( "roleSel" ).focus();
    	showRoleMenu();
        return false;
	}
    json["roleid"] = roleid;
    json["rolename"] = rolename;
    json["sex"] = sex;

	var userPropId, i;
	try{
		for(i = 0; i < inputEx.length; i+=1 )
		{
			userPropId = inputEx[i].id;
			userPropId = userPropId.substring("userEx_".length);
			json[userPropId] = inputEx[i].value;
			var nullable = document.getElementById( userPropId+"Nullable" ).value
			//var title = document.getElementById( userPropId+"Name" ).value;
			if( nullable == 'false' && inputEx[i].value == "" )
			{
				$("#"+inputEx[i].id).tooltip("show");
				inputEx[i].focus();
				return false;
			}
			var inherit = userPropId+"_inherit";
			json[inherit] = document.getElementById( inherit ).checked?true:false;
		}
	}
	catch(e){
		alert("["+userPropId+":"+i+"]"+e);
		return false;
	}
	UserMgr.setUser(
			'<ww:property value="account"/>',
			JSON.stringify(json), 
			timestamp, {
		callback:function(rsp) {
			try {
				if( callback ){
					timestamp = rsp.timestamp>0?rsp.timestamp:timestamp;
					//tips, succeed, account, realanme, time, 角色下users//
					callback(rsp.message, rsp.succeed, account, name, timestamp, rsp.result);
				}
				else{
					skit_alert(rsp.message);
				}
			}
			catch(e){
				alert("设置用户参数出现异常"+e);
			}
		},
		timeout:30000,
		errorHandler:function(err) {alert(err); }
	});
	return true;
}

/**
 * 创建新的系统用户
 */
function addsysuser(){
	setUser(function(tips, result, account, name){
        var script;
		try{
	        if( result ){
	     	   script = chr2Unicode("parent.parent.addsysuserCallback('"+tips+"', "+result+", '"+account+"', '"+name+"');");
	        }
	        else{
	     	   script = chr2Unicode("parent.parent.addsysuserCallback('"+tips+"', "+result+");");
	        }
			document.getElementById("addsysuserCallback").src = securityCallbackUrl+script;
		}
		catch(e){
			alert("新增系统用户回调出现异常:"+e);		
		}
	});
}
</SCRIPT>
<style type='text/css'>
.input-group-addon {
    padding: 6px 12px;
    font-size: 12px;
    font-weight: 400;
    line-height: 1;
    color: #555;
    text-align: center;
    background-color: #eee;
    border: 1px solid #ccc;
    border-right-width: 1px;
    border-right-style: solid;
    border-right-color: rgb(204, 204, 204);
    border-radius: 4px;
    border-top-right-radius: 4px;
    border-bottom-right-radius: 4px;
}
.form-control {
    display: block;
    width: 100%;
    height: 34px;
    padding: 6px 12px;
    font-size: 12px;
    line-height: 1.42857143;
    color: #555;
    background-color: #fff;
    background-image: none;
    border: 1px solid #ccc;
    border-radius: 4px;
        border-top-left-radius: 4px;
        border-bottom-left-radius: 4px;
    -webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
    box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
    -webkit-transition: border-color ease-in-out .15s,-webkit-box-shadow ease-in-out .15s;
    -o-transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
    transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
}
</style>
</html>