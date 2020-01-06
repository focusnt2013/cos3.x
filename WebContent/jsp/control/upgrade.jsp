<%@page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<style type='text/css'>
</style>
<SCRIPT TYPE="text/javascript">
</SCRIPT>
</head>
<body onResize='resizeWindow()'>
<form action="" method="POST">
<input type='hidden' name='ip' value='<ww:property value="ip"/>'/>
<input type='hidden' name='port' value='<ww:property value="port"/>'/>
<input type='hidden' name='serverkey' value='<ww:property value="serverkey"/>'/>
<input type='hidden' name='yaochongqi' id='yaochongqi' value='4'/>
<table style='width:100%' border='0'>
<tr>
	<td width='50%' style='padding-right:10px'>
        <div class="panel panel-default" style='margin-top:10px;'>
   			<div class="panel-heading"><span><i class='skit_fa_btn fa fa-check-square-o'></i> 升级检查</span></div>
   			<div class="skit_view_div panel-body">
   			<iframe src='control!upgradedownload.action?ip=<ww:property value="ip"/>&port=<ww:property value="port"/>' id='upgradedownload' class='nonicescroll' style='width:100%;border:0px;'></iframe>
   			</div>
		</div>
	</td>
	<td width='50%'>
        <div class="panel panel-default" style='margin-top:10px;'>
   			<div class="panel-heading"><span><i class='skit_fa_btn fa fa-check-square-o'></i> 版本展示</span></div>
   			<div class="panel-body" style='padding: 0px;'>
   			<iframe src='<ww:property value="responseRedirect"/>' name='versiontimeline' id='versiontimeline' class='nonicescroll' style='width:100%;border:0px;'></iframe>
   			</div>
		</div>
	</td>
</tr>
</table>
</form>
</body>
<SCRIPT TYPE="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	document.getElementById("upgradedownload").style.height = windowHeight - 90;
	document.getElementById("versiontimeline").style.height = windowHeight - 60;
}

function doSubmit()
{
	skit_confirm("是否要重启主控系统?", function(yes){
		document.getElementById("yaochongqi").value = yes?4:0;
		document.forms[0].action = "control!upgrade.action";
		document.forms[0].target = "versiontimeline";
		document.forms[0].submit();
	});
}

function doUpgrade(result, version, webversion)
{
	if( result )
	{
		var tips = "您是否确定";
		if( version ) tips += "将主控引擎系统版本升级到["+version+"]";
		if( webversion ){
			tips += ", 将主界面框架系统版本升级到["+webversion+"]？";
		}
		skit_confirm(tips, function(yes){
			if( yes )
			{
				window.setTimeout("doSubmit()", 100);
			}
		});
	}
	else
	{
		skit_alert("不能执行升级，详情请看控制台返回信息。");
	}
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
</html>