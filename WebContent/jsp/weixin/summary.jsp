<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<meta http-equiv="Pragma" content="no-cache" /> 
<meta http-equiv="Cache-Control" content="no-cache" /> 
<meta http-equiv="Expires" content="0" /> 
<style type='text/css'>
</style>
<script type="text/javascript">
function configAppMenu()
{
	top.openView("配置微信公众号自定义菜单", "weixin!presetmenu.action?sysid=<ww:property value='sysid'/>&id=<ww:property value='weixinSummary.weixinno'/>");
}

function queryUsers()
{
	top.openView("查询微信公众号用户数据", "weixin!users.action?sysid=<ww:property value='sysid'/>&id=<ww:property value='weixinSummary.weixinno'/>");
}

function queryCallback()
{
	top.openView("查询微信公众号回调记录", "weixin!callbackquery.action?sysid=<ww:property value='sysid'/>&id=<ww:property value='weixinSummary.weixinno'/>");
}
function previewCallback(){

	top.openView('预览微信回调的内网URL<ww:property value="weixinSummary.summaryurl"/> 如果返回Unknown Request表示服务正常', 'http!get.action?link=<ww:property value="weixinSummary.summaryurl"/>');
}
</script>
</head>
<body>
<div class='ui-tabs .ui-tabs-panel' style='padding:10px 10px'>

	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-info'></i> <ww:text name="微信公众号基本信息"/></div>
		<div class="panel-body" style='padding-bottom:0px;'>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon input-group-addon-font">微信名称<span class='fa fa-weixin' style='margin-left:6px'></span></span>
					<input class="form-control form-control-font" value='<ww:property value="weixinSummary.weixinno"/>'>
					<span class="input-group-addon input-group-addon-font" style='border-left: 1px solid #eee;'><ww:property value="weixinSummary.type"/><span class='fa fa-tag'></span></span>
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon input-group-addon-font">应用名称<span class='fa fa-bookmark' style='margin-left:12px'></span></span>
					<input class="form-control form-control-font" value='<ww:property value="weixinSummary.name"/>'>
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon input-group-addon-font">运行URL<span class='fa fa-mail-reply' style='margin-left:6px'></span></span>
					<input class="form-control form-control-font" value='<ww:property value="weixinSummary.runurl"/>'>
				</div>
			</div>
			<!-- 
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon input-group-addon-font">微信回调<span class='fa fa-weixin' style='margin-left:6px'></span></span>
					<input class="form-control form-control-font" value='<ww:property value="weixinSummary.summaryurl"/>'
						onclick='previewCallback()'>
				</div>
			</div>
			-->
		</div>
	</div>
	<div class="panel panel-default">
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-info'></i> <ww:text name="公众号程序运行状态"/></div>
		<div class="panel-body" style='padding-bottom:0px;'>
			<div class="form-group">
				<div class="input-group" style='width:292px;'>
					<span class="input-group-addon input-group-addon-font">用户数<span class='fa fa-group' style='margin-left:6px'></span></span>
					<input class="form-control form-control-font" value='<ww:property value="weixinSummary.userscount"/>' tyle='text-align: right;'>
					<span class="input-group-addon input-group-addon-font" style='border-left: 1px solid #eee;'><ww:property value="weixinSummary.status" escape="false"/></span>
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon input-group-addon-font">接口访问令牌<span class='fa fa-openid' style='margin-left:16px'></span></span>
					<input class="form-control form-control-font" value='<ww:property value="weixinSummary.accessToken"/>'>
					<span class="input-group-addon input-group-addon-font" style='border-left: 1px solid #eee;border-right: 1px solid #eee;'><span class='fa fa-clock-o'></span>过期时间:</span>
					<input class="form-control form-control-font" value='<ww:property value="weixinSummary.expireDate"/>'>
					<span class="input-group-addon input-group-addon-font" style='border-left: 1px solid #eee;border-right: 1px solid #eee;'><span class='fa fa-history'></span>更新时间:</span>
					<input class="form-control form-control-font" value='<ww:property value="weixinSummary.getDate"/>'>
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon input-group-addon-font">JSAPI访问令牌<span class='fa fa-html5' style='margin-left:6px'></span></span>
					<input class="form-control form-control-font" value='<ww:property value="weixinSummary.jsapiTicket"/>'>
					<span class="input-group-addon input-group-addon-font" style='border-left: 1px solid #eee;border-right: 1px solid #eee;'><span class='fa fa-clock-o'></span>过期时间:</span>
					<input class="form-control form-control-font" value='<ww:property value="weixinSummary.jsapiTicketExpireDate"/>'>
					<span class="input-group-addon input-group-addon-font" style='border-left: 1px solid #eee;border-right: 1px solid #eee;'><span class='fa fa-history'></span>更新时间:</span>
					<input class="form-control form-control-font" value='<ww:property value="weixinSummary.jsapiTicketGetDate"/>'>
				</div>
			</div>
		</div>
	</div>
	<div style='width:500px;'>
		<button type="button" class="btn btn-outline btn-info btn-block"
			style='width:160px;float:left;padding-right:10px;margin-top:0px;' onclick='queryCallback();'><i class="fa fa-mail-reply"></i> 查询回调记录 </button>
		<button type="button" class="btn btn-outline btn-success btn-block"
			style='width:160px;float:left;margin-left:10px;margin-top:0px;' onclick='queryUsers();'><i class="fa fa-group"></i> 查询用户情况 </button>
		<button type="button" class="btn btn-outline btn-primary btn-block"
			style='width:160px;padding-left:10px;float:right;margin-top:0px;' onclick='configAppMenu();'><i class="fa fa-cogs"></i> 配置公众号菜单</button>
	</div>
</div>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
</html>