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
<body><div class='ui-tabs .ui-tabs-panel' style='padding:10px 10px'>
	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-user'></i>程序基本信息</div>
		<div class="panel-body">
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">程序标识<span class='fa fa-user-secret' style='margin-left:6px'></span></span>
					<input class="form-control" value="<ww:property value='serviceConfig.id'/>" type="text" id='serviceConfig.id'
						readonly="<ww:property value='editable'/>"
					 >
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">程序名称<span class='fa fa-credit-card' style='margin-left:6px'></span></span>
					<input class="form-control" value="<ww:property value='serviceConfig.name'/>" type="text" id='serviceConfig.name'
						readonly="<ww:property value='editable'/>">
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">程序版本<span class='fa fa-at' style='margin-left:6px'></span></span>
					<input class="form-control" value="<ww:property value='serviceConfig.version'/>" type="text" id='serviceConfig.version'
						readonly="<ww:property value='editable'/>">
				</div>
			</div>
			<div class="form-group" id='roleSelForm'>
   				<div class="input-group">
					<span class="input-group-addon">版本描述<span class='fa fa-group' style='margin-left:6px'></span></span>
					<input class="form-control" value="<ww:property value='serviceConfig.versionRelease'/>" type="text" id='serviceConfig.versionRelease'
						readonly="<ww:property value='editable'/>">
				</div>
			</div>
		</div>
	</div>
	
	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-user-md'></i>程序主控参数</div>
		<div class="panel-body">
			<div class="form-group" id='roleSelForm'>
   				<div class="input-group">
					<span class="input-group-addon">工作模式<span class='fa fa-group' style='margin-left:6px'></span></span>
					<input class="form-control" type="text" id='serviceConfig.mode'
						readonly="<ww:property value='editable'/>">
				</div>
			</div>
		</div>
	</div>
</div>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<SCRIPT type="text/javascript">
var jsonData = '<ww:property value="jsonData" escape="false"/>';
try
{
	var json = jQuery.parseJSON(jsonData);
	document.getElementById( 'serviceConfig.mode' ).value = json.id
}
catch(e)
{
	alert("初始化目录树异常"+e.message+", 行数"+e.lineNumber);
}
</SCRIPT>
</html>