<%@page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<style type='text/css'>
.xml
{
	font-size: 12px;
    font-family: 微软雅黑;
	background-color: #000;
    border: 1px solid #efefef;
    color: #fff;
    width: 100%;
}
</style>
<SCRIPT TYPE="text/javascript">
function doSave()
{
	skit_confirm("您确定提交伺服器【<ww:property value='ip'/>:<ww:property value='port'/>】的主控配置吗？", function(yes){
		if( yes )
		{
			document.forms[0].submit();
		}
	});
}
function doCancel()
{
	top.window.closeView();
}
</SCRIPT>
</head>
<body onResize='resizeWindow()'>
<form action="control!savexml.action" method="POST">
<input type='hidden' name='ip' value='<ww:property value="ip"/>'/>
<input type='hidden' name='port' value='<ww:property value="port"/>'/>
<input type='hidden' name='serverkey' value='<ww:property value="serverkey"/>'/>
<table style='width:100%'>
<tr>
	<td width='800' style='padding-right:10px'>
        <div class="panel panel-default">
   			<div class="panel-heading"><span><i class='skit_fa_btn fa fa-check-square-o'></i> 关于伺服器主控配置</span></div>
   			<div class="skit_view_div panel-body">
   			<iframe src='helper!developer.action?id=control' name='controlXmlIntro' id='controlXmlIntro' class='nonicescroll' style='width:100%;border:0px;'></iframe>
   			</div>
		</div>
	</td>
	<td>
        <div class="panel panel-default">
   			<div class="panel-heading"><span><i class='skit_fa_btn fa fa-check-square-o'></i> 伺服器【<ww:property value="ip"/>】主控control.xml</span></div>
   			<div class="panel-body" style='padding: 0px;'>
   				<textarea name='controlXml' id='controlXml' class='skit_view_div xml' ><ww:property value='controlXml'/></textarea>
   			</div>
		</div>
	</td>
</tr>
<tr>
	<td align='center' style='padding-right:0px' colspan='2'>
		<div style='width:280'>
			<button type="button" class="btn btn-outline btn-primary btn-block"
				style='width:128px;float:left;padding-right:10px;' onclick='doSave();'><i class="fa fa-save"></i> 保存 </button>
			<button type="button" class="btn btn-outline btn-danger btn-block"
				style='width:128px;padding-left:10px;' onclick='doCancel();'><i class="fa fa-quite"></i> 取消</button></div>
	</td>
</tr>
</table>
</form>
</body>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<SCRIPT TYPE="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	document.getElementById("controlXml").style.height = windowHeight - 108;
	document.getElementById("controlXmlIntro").style.height = windowHeight - 140;
}
resizeWindow();
</SCRIPT>
</html>