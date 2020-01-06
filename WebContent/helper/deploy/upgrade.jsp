<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%Kit.noCache(request,response);%>
<html>
<%=Kit.getDwrJsTag(request,"interface/HelperMgr.js")%> 
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<SCRIPT type="text/javascript">
var upgradable = <ww:property value='editable'/>;
if( upgradable )
{
	window.top.skit_confirm("你是否要立刻升级【主界面框架系统】？", function(yes){
		if( yes )
		{
			HelperMgr.doUpgrade({
				callback:function(response){
					if( response.succeed )
					{//表示正在执行升级
						window.top.skit_progress(response.message);
						window.setTimeout("getUpgradeResult()",500);
					}
					else
					{
						window.top.skit_alert(response.message);
					}
				},
				timeout:10000,
				errorHandler:function(message) {
				}
			});	
		}
	});
}
else
{
	window.top.skit_alert('<ww:property value="responseConfirm"/>', "异常提示", function(){
		window.top.closeReloadView();
	});
}

//检查升级结果
function getUpgradeResult()
{
	HelperMgr.getUpgradeResult({
		callback:function(response){
			if( response.succeed )
			{//表示正在执行升级
				window.top.setProgress(response.result, response.message);
				window.setTimeout("getUpgradeResult()",500);
			}
			else
			{
				if( response.result > 100 )
				{
					window.top.skit_confirm(response.message+"<br/>你是否要重启【主界面框架系统】让新版本生效？", function(yes){
						if( yes )
						{
							HelperMgr.doUpgradeRetartup({
								callback:function(tips){
									window.top.skit_alert(tips);
								},
								timeout:10000,
								errorHandler:function(message) {
								}
							});	
						}
					});
				}
				else
				{
					window.top.skit_alert(response.message);
				}
			}
		},
		timeout:10000,
		errorHandler:function(message) {window.top.skit_alert("升级出现异常："+message)}
	});	
}
</SCRIPT>
<body style='font-size:9pt;color:#fff;margin-left:64px;margin-right:64px;margin-top:64px;'>
<pre>
<ww:property value="%{responseMessage}"/>
</pre>
</body>
</html>