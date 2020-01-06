<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html> 
<body style='overflow-y:hidden;background-color:#aaa;'>
<form action="user!changepassword.action">
	<div class="panel panel-default" style='width:512px;height:220px;position:absolute' id='div412'>
		<div class="panel-heading" style='font-size:14px;padding:15px 15px'><i class='skit_fa_btn fa user-secret'></i>
			请重置自己的密码
			<ww:if test="chance>0"><span style='color:red'>还有<ww:property value='chance'/>次将禁止登录</span></ww:if>
			<ww:if test="chance==0"><span style='color:red'>下次登录时账号将被禁用</span></ww:if>
			</div>
		<div class="panel-body">
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">旧用户密码<span class='fa fa-key' style='margin-left:6px'></span></span>
					<input class="form-control" 
						placeholder="请输入原用户密码用于校验您的身份" 
						data-toggle="tooltip" 
						data-placement="top"
						data-title='请输入原用户密码用于校验您的身份，如果忘记请联系您的系统管理员'
						data-trigger='manual'
						onkeydown='$("#oldPassword").tooltip("hide")'
						type="password" 
						id='oldPassword'
						name='oldpassword'>
				</div>
			</div>
			<div class="form-group">
				<div class="input-group">
					<span class="input-group-addon">新用户密码<span class='fa fa-key' style='margin-left:6px'></span></span>
					<input class="form-control"
						data-title=''
						data-toggle="tooltip" 
						data-placement="top"
						placeholder=""
						data-trigger='manual'
						onkeydown='$("#newPassword").tooltip("hide")'
						type="password"
						id='newPassword'
						name='newpassword'>
				</div>
			</div>
			
            <div class="col-xs-12 text-center" style='margin-top:15px;'>
                <div class="col-xs-12 col-sm-10 emphasis" style='width:100%;border:0px solid red'>
            		<button type="button" class="btn btn-outline btn-primary btn-block" onclick='doSubmit();'><i class="fa user-secret"></i> 提交 </button>
                </div>
            </div>
            
		</div>
	</div>
</form>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<SCRIPT type="text/javascript">
function resizeWindow()
{
	var div = document.getElementById( 'div412' );
	var top = windowHeight/2 - div.clientHeight/2;
	var left = windowWidth/2 - div.clientWidth/2;
	div.style.top = top;
	div.style.left = left;
}
resizeWindow();
//var regexp2 = new RegExp("^[^%&',;=?$/x22]+$");
//var regexp1 = new RegExp("^(?![A-Z]+$)(?![a-z]+$)(?!\\d+$)(?![\\W_]+$)$");
var account = "<ww:property value='theuser.username'/>";
var authPasswordRegexp = "<ww:property value='authPasswordRegexp'/>";
var authUnincludeUsername = <ww:property value='authUnincludeUsername'/>;
var authPasswordLength = <ww:property value='authPasswordLength'/>;
var regexptips = "";
if( authUnincludeUsername )
{
	regexptips += "密码不能有账号名称;";
}
if( authPasswordRegexp )
{
	authPasswordRegexp = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{"+authPasswordLength+",32}$";
	regexptips += "含有大小写字母、数字、特殊符号的两种及以上，长度在"+authPasswordLength+"-32之间";
	$("#newPassword").attr("data-title", regexptips);
	$("#newPassword").attr("placeholder", "含有大小写字母、数字、特殊符号的两种及以上，长度在"+authPasswordLength+"-32之间");
}
else
{
	regexptips += "请输入新用户密码不少于"+authPasswordLength+"个字";
	$("#newPassword").attr("data-title", regexptips);
	$("#newPassword").attr("placeholder", "请输入新用户密码不少于"+authPasswordLength+"个字");
}

function doSubmit()
{
	var oldPassword = document.getElementById( 'oldPassword' ).value;
	if( oldPassword == "" )
	{
		document.getElementById( "oldPassword" ).focus();
		$("#oldPassword").tooltip("show");
		return;
	}
	var newPassword = document.getElementById( 'newPassword' ).value;
	if( newPassword < authPasswordLength )
	{
		$("#newPassword").attr("data-title", "请输入新用户密码不少于"+authPasswordLength+"个字");
		$("#newPassword").tooltip("show");
		document.getElementById( "newPassword" ).focus();
		return;
	}
	if( authUnincludeUsername )
	{
		if( newPassword.indexOf(account) != -1 )
		{
			$("#newPassword").tooltip("show");
			document.getElementById( "newPassword" ).focus();
			return;
		}
	}
	if( authPasswordRegexp )
	{
		var m1 = newPassword.match(new RegExp(authPasswordRegexp));
	    if( !m1 )
	    {
			document.getElementById( "newPassword" ).focus();
			$("#newPassword").tooltip("show");
	        return;
	    }
		
	}
	document.forms[0].submit();
}
</SCRIPT>
</html>