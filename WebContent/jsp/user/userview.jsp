<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/UserMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body>
<table style='width:100%;'><tr>
<td style='width:50%;' valign='top' id='tdUserProp'>
<div class='ui-tabs .ui-tabs-panel' style='padding:10px 10px'>
	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-user'></i>用户基本信息</div>
		<div class="panel-body">
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">用户账号<span class='fa fa-user-secret' style='margin-left:6px'></span></span>
					<input class="form-control" value="<ww:property value='theuser.username'/>" type="text" id='userAccount'
						<ww:if test='account!=null'>readonly='true'</ww:if>
					 >
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">真实姓名<span class='fa fa-credit-card' style='margin-left:6px'></span></span>
					<input class="form-control" value="<ww:property value='theuser.realname'/>" type="text" id='userName'>
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">用户邮箱<span class='fa fa-at' style='margin-left:6px'></span></span>
					<input class="form-control" value="<ww:property value='theuser.email'/>" type="text" id='userEmail'>
				</div>
			</div>
			<div class="form-group" id='roleSelForm'>
   				<div class="input-group">
					<span class="input-group-addon">角色权限<span class='fa fa-group' style='margin-left:6px'></span></span>
					<input class="form-control" value="<ww:property value='theuser.rolename'/>" type="text" id='userEmail'>
				</div>
			</div>
		</div>
	</div>
	
	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-user-md'></i>扩展用户属性</div>
		<div class="panel-body">
			<ww:iterator value="listData" status="loop">
			<ww:if test='has("use")&&getBoolean("use")'>
			<div class="form-group">
     			<div class="input-group">
					<span class="input-group-addon"><ww:property value='getString("name")'/>
					<ww:if test='has("icon")'><span class='fa <ww:property value='getString("icon")'/>' style='margin-left:6px'></span></ww:if>
					<ww:elseif test='getBoolean("nullable")'><span class='fa fa-circle-o' style='margin-left:6px'></span></ww:elseif>
					<ww:else><span class='fa fa-circle' style='margin-left:6px'></span></ww:else>
					</span>
					<input class="form-control" value="<ww:property value='getString("value")'/>" type="text">
				</div>
			</div>
			</ww:if>
			</ww:iterator>
		</div>
	</div>
</div>
</td>
<td style='width:50%' valign='top'>
<iframe id='iPrivileges' style='width:100%;height:1024px;border:0px;' src="user!privileges.action?ww=-1&&id=<ww:property value='theuser.username'/>"></iframe>
</td>	
</tr></table>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>

<SCRIPT type="text/javascript">
var div = document.getElementById( 'iPrivileges' );
div.style.height = document.getElementById( 'tdUserProp' ).clientHeight;
</SCRIPT>
</html>