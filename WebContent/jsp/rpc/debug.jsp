<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<style type='text/css'>
<jsp:include page='<%=Kit.getSkinCssPath("skit_view.css", "../../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_table.css", "../../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_button.css", "../../")%>'/>
</style>
<!-- Core CSS - Include with every page -->
<link href="skit/css/bootstrap.min.css" rel="stylesheet">
<!-- Font Awesome Icons -->
<link href="skit/css/font-awesome.css" rel="stylesheet">
<%=Kit.getJSTag(request, "skit_button.js")%>
<%=Kit.getDwrJsTag(request,"interface/RpcMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body>
<table style='width:100%'>
	<tr><td>
	<div id='ack' valign='top' class='skit_view_div' style='border:solid #7F9DB9 1px;background-color: #000000;color:#FFFFFF;font-size: 11pt;'>
	<table style='width:100%'>
	<tr><td id='ack0' style='background-color: #000000;color:#FFFFFF;font-size: 11pt;'></td></tr>
	<tr><td id='ack1' style='background-color: #000000;color:#FFFFFF;font-size: 11pt;'></td></tr>
	</table>
	</div></td></tr>
</table>
<div style='position:absolute;top:28px;left:28px;' id='toolbar'>
    <button type="button" class="btn btn-success btn-circle" onclick="switchDebug(this)" title='点击打开调测管道' id='btnSwitch'>
    	<i class="fa fa-bug"></i></button><br/><br/>
    <button type="button" class="btn btn-info btn-circle" onclick="restartup()" title='重启当前引擎' id='btnRestartup'>
   		<i class="fa fa-repeat"></i></button><br/><br/>
    <button type="button" class="btn btn-default btn-circle" onclick="wondow.history.back();" title='回到系统监控'>
   		<i class="fa fa-backward"></i></button>   		
</div>
<form action="#" method="post">
<input type='hidden' name='command'/>
<input type='hidden' name='ip' value='<ww:property value="ip"/>' id='host'/>
<input type='hidden' name='port' value='<ww:property value="port"/>' id='port'/>
<input type='hidden' name='id' id='id' value='<ww:property value="id"/>' />
<iframe name='downloadFrame' style='display:none'></iframe>
</form>
</body>
<script type="text/javascript">
function resizeWindow()
{
	var div = document.getElementById( 'ack' );
	div.style.height = windowHeight - 3;	
	var toolbar = document.getElementById( 'toolbar' );
	toolbar.style.top = windowHeight - toolbar.clientHeight - 10;
	toolbar.style.left = windowWidth - toolbar.clientWidth - 10;
}
</script>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<script type="text/javascript">
function restartup()
{
	document.forms[0].command.value = 0;
	document.forms[0].action = "control!program.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "downloadFrame";
	document.forms[0].submit();
}
var debugOpended = false;
function switchDebug(btn)
{
	if( debugOpended )
	{
		RpcMgr.closeDebug("<ww:property value='ip'/>",<ww:property value="port"/>,"<ww:property value='id'/>", {
			callback:function(result) {
				if( result != "" )
				{
					skit_alert(result)
				}
				document.getElementById("btnSwitch").innerHTML = "<i class='fa fa-bug'></i>";
				document.getElementById("btnSwitch").title = "点击打开调测管道";
				debugOpended = false;
			},
			timeout:120000,
			errorHandler:function(message) {skit_alert(message);}
		});
	}
	else
	{
		RpcMgr.openDebug("<ww:property value='ip'/>",<ww:property value="port"/>,"<ww:property value='id'/>", {
			callback:function(result) {
				if( result != "" )
				{
					skit_alert(result)
				}
				else
				{
					btn.innerHTML = "<i class='fa fa-bug fa-spin'></i>";
					btn.title = "点击关闭调测管道";
					debugOpended = true;
					window.setTimeout("refresh()",1000);
				}
			},
			timeout:120000,
			errorHandler:function(message) {skit_alert(message);viewLock=false;}
		});
	}
}
//刷屏
var offset = 0;//刷屏的数据偏移量
function refresh()
{
	if( !debugOpended )
	{
		return;
	}
	//parent.setViewTitle(offset);
	RpcMgr.getDebugResponse("<ww:property value='ip'/>", <ww:property value="port"/>,"<ww:property value='id'/>", offset, {
		callback:function(response) {
			var ack = document.getElementById("ack");
			if( response && response.offset > offset )
			{
				var html = document.getElementById("ack0").innerHTML;
				for( var i in response.messages )
				{
					var line = response.messages[i];
					if( line.indexOf("@<ww:property value='host'/>") > 0 )
					{
						line = "<span style='color:red;font-weight:bold'>"+line+"</span>";
					}
					html += line;
					html += "<br/>";
				}
				document.getElementById("ack0").innerHTML = html;
				if( response.esc )
				{
					document.getElementById("ack1").innerHTML = "<span style='color:red;font-weight:bold'>"+response.user+"@"+response.host+"# _"+"</span><br/><br/>"; 
				}
				else if( response.lastLine )
				{
					document.getElementById("ack1").innerHTML = response.lastLine+"<br/><br/>";
				}
				else
				{
					document.getElementById("ack1").innerHTML = "&nbsp;";
				}
				//执行一次指令后，将滚动条跳到最后
				ack.scrollTop = ack.scrollHeight;
				offset = response.offset;
			}
			window.setTimeout("refresh()",1000);
		},
		timeout:30000,
		errorHandler:function(message) {alert(message);window.setTimeout("refresh()",3000);}
	});
}
</script>
</html>