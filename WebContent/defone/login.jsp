<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html class="js flexbox canvas canvastext webgl no-touch geolocation postmessage websqldatabase indexeddb hashchange history draganddrop websockets rgba hsla multiplebgs backgroundsize borderimage borderradius boxshadow textshadow opacity cssanimations csscolumns cssgradients cssreflections csstransforms csstransforms3d csstransitions fontface generatedcontent video audio localstorage sessionstorage webworkers applicationcache svg inlinesvg smil svgclippaths" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta charset="utf-8">
    <meta content="IE=edge,chrome=1" http-equiv="X-UA-Compatible">
	<title>登录 <ww:property value='systemName'/></title>
    <meta content="lab2023" name="author">
    <meta content="" name="description">
    <meta content="" name="keywords">
    <link href="./skin/defone/login/login.css" rel="stylesheet" type="text/css">
    <link href="./skin/defone/login/font-awesome.min.css" rel="stylesheet" type="text/css">
    <link href="images/cos.ico" rel="shortcut icon" type="image/x-icon">
    <style type="text/css">
    ._vercode{
		background-image: url();
		display:-moz-inline-box;/*解决IE10 非兼容模式显示不了的问题*/
		display:inline-block;/*解决IE10 非兼容模式显示不了的问题*/
		vertical-align:middle;/*解决IE10 非兼容模式显示不了的问题*/
		cursor:pointer;
		color:orgen;
		width:53px;
		height:22px;
		background-repeat: no-repeat;
		border:1px solid #cccccc;
		margin-left:5px;
		margin-top:5px;
		float:left;
	}
    ._vercode_tip{
		display:-moz-inline-box;/*解决IE10 非兼容模式显示不了的问题*/
		display:inline-block;/*解决IE10 非兼容模式显示不了的问题*/
		vertical-align:middle;/*解决IE10 非兼容模式显示不了的问题*/
		cursor:pointer;
		width:60px;
		height:22px;
		background-repeat: no-repeat;
		border:0px solid #cccccc;
		font-size:8pt;
		color:red;
		margin-left:3px;
		margin-top:5px;
		float:left;
	}
	.logo-icon i[class*='icon-']{margin-left:-5px;font-size:140%;color: <ww:property value='themeColor'/>}
	legend{display:block;
		width:100%;
		padding:0;
		margin-bottom:20px;
		font-size: 20px;
		line-height:inherit;
		color:#333333;border:0;
		border-bottom:1px solid #e5e5e5;
		font-family: NSimSun, 'Microsoft Yahei', '微软雅黑', STHupo, STKaiti, Arial, sans-serif, Helvetica, 'Hiragino Sans GB';
		}
    </style>
  </head>
  <body class="login">
    <div class="wrapper" style='width:300px;'>
      <div class="row">
        <div class="col-lg-12">
          <div class="brand text-center">
            <h1 style='margin-bottom:10px'>
              <div class="logo-icon" style="background:#fff">
                <i class="icon-signin" id="icon-login"></i>
              </div>
            </h1>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col-lg-12">
		  <form action="logon" method="POST">
		  	<input type='hidden' name='username' id="username1" />
		  	<input type='hidden' name='password' id="password1" />
		  	<input type='hidden' name='j_captcha_response' id='j_captcha_response1'/>
            <fieldset class="text-center">
              <legend class=''><ww:property value='systemName'/></legend>
              <div class="form-group">
                <input id="username" class="form-control"
                	 placeholder="<ww:text name="label.ema.login.username"/>" type="text"
                	data-title='请输入用户名'
					data-toggle="tooltip" 
					data-placement="top"
					data-trigger='manual'
					onkeydown='$("#username").tooltip("hide")'
					onblur='$("#username").tooltip("hide")'>
              </div>
              <div class="form-group">
                <input id="password" class="form-control" placeholder="<ww:text name="label.ema.login.password"/>" type="password"
                	data-title='请输入用户密码'
					data-toggle="tooltip" 
					data-placement="top"
					data-trigger='manual'
					onkeydown='$("#password").tooltip("hide")'
					onblur='$("#password").tooltip("hide")'>
              </div>
              <div class="form-group">
                <input class="form-control" placeholder="验证码" type="text" style="width:80px;float:left;font-size:14px"
                    id="j_captcha_response" maxlength="7"
                    data-title='请输入图片中的验证码'
					data-toggle="tooltip" 
					data-placement="top"
					data-trigger='manual'
					onkeydown='$("#j_captcha_response").tooltip("hide")'
					onblur='$("#j_captcha_response").tooltip("hide")'
					>
                <span id="imgVerify" class="_vercode" title="点击更换验证码" onclick="changeVercode()">_/ .\</span>
                <span id="tipVerify" class="_vercode_tip"></span>
              </div>
			  <div class="form-group" style="clear: both;">
	              <div class="text-center" id='div-login'>
	                <div class="checkbox" style='margin-bottom:0px;'><label id="tips" style="color:red">&nbsp;</label></div>
	              </div>
	              <div class="text-center" id='div-login'>
	                <a class="btn btn-default" href="javascript:exe_login();">登录</a>
	              </div>
	          </div>
              <div class="text-center">
                <a href="main"><img src="<ww:property value='systemLogo'/>"></a>
              </div>
            </fieldset>
          </form>
        </div>
      </div>
    </div>
    <!-- Footer -->
    <!-- Javascripts -->
<script src="./skin/defone/login/ga.js"></script>
<script src="./skin/defone/login/jquery.min.js" type="text/javascript"></script>
<script src="./skin/defone/login/jquery-ui.js" type="text/javascript"></script>
<script src="./skin/defone/js/jquery.blockui.js" type="text/javascript"></script>
<script src="./skin/defone/login/modernizr.min.js" type="text/javascript"></script>
<script src="./skin/defone/login/login.js" type="text/javascript"></script>
<script src="skit/js/jquery.inputmask.min.js"></script>
<script src="skit/js/jsencrypt.min.js"></script>
<script type="text/javascript">
	$("#j_captcha_response").inputmask("mask", {"mask": "9 9 9 9"});
    var t = 0;
    var forboden = false;
    function changeVercode()
	{
		var tipVerify = document.getElementById('tipVerify');
		if( forboden )
		{
			tipVerify.innerHTML = "点太快了";
			return;
		}
		tipVerify.innerHTML = "";
		imgVerify.style.backgroundImage = "url(jcaptcha?v=3.16.3.29&t="+(t++)+")";
		window.setTimeout("forboden=false;",3000);
		forboden = true;
	}
	changeVercode();
	/*登录*/
	function exe_login()
	{
		var username = document.getElementById("username").value;
		var password = document.getElementById("password").value;
		if( username == '' )
		{
	    	document.getElementById( "username" ).focus();
			$("#username").tooltip("show");
			return;
		}
		if( password == '' )
		{
	    	document.getElementById( "password" ).focus();
			$("#password").tooltip("show");
			return;
		}
		var j_captcha_response = document.getElementById("j_captcha_response").value;
		j_captcha_response = j_captcha_response.replace(/\s/g, ""); 
		j_captcha_response = j_captcha_response.replace(/[_]/g, "");
		if( j_captcha_response == '' )
		{
			//var tipVerify = document.getElementById('tipVerify');
			//tipVerify.innerHTML = "请输入图片中的验证码";
	    	document.getElementById( "j_captcha_response" ).focus();
			$("#j_captcha_response").tooltip("show");
			return;
		}
		
		document.getElementById("icon-login").className = "icon-signin icon-spin";
		document.getElementById("div-login").disabled = true; 
		$.blockUI({
	     	message: '<h2><i class="icon-spinner icon-spin"></i></h2>',
	     	css: {
	     		border: 'none', 
	          padding: '15px', 
	          background: 'none',
	     	},
	     	overlayCSS: { backgroundColor: '#FFF' },
	     	timeout: 2000 
	   	});	
		
		var encrypt = new JSEncrypt();
        encrypt.setPublicKey(publicKey);
//        var encryptedUN = encrypt.encrypt(username);
        var encryptedUN1 = encrypt.encrypt(username);
        var encryptedPW = encrypt.encrypt(password);
        var encryptedCC = encrypt.encrypt(j_captcha_response);
        document.getElementById( "username1" ).value = encryptedUN1;
        document.getElementById( "password1" ).value = encryptedPW;
		document.getElementById("j_captcha_response1").value = encryptedCC;
		document.forms[0].submit();
	}
	
	function reset_input()
	{
		return true;
	}
	
	document.onkeydown=function(event) 
	{ 
		var e = event ? event :(window.event ? window.event : null); 
		if(e.keyCode==13){ 
			//执行的方法 
	        exe_login();
		} 
	}
	var publicKey = "<ww:property value='lingpai'/>";
	document.getElementById('tips').innerHTML = "<ww:property value='%{responseException}' escape='false'/>";
	var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
	var isOpera = userAgent.indexOf("Opera") > -1;
	var isFirefox = userAgent.indexOf("Firefox") > -1;
	var isDoNet = navigator.userAgent.indexOf(".NET") != -1;
	var isWindows = navigator.userAgent.indexOf("Windows") != -1;
	var isCompatible = navigator.userAgent.indexOf("compatible") != -1;
	var isMSIE = navigator.userAgent.indexOf("MSIE") != -1;
	if( ( isCompatible && isMSIE && !isOpera) ||
	    (!isCompatible && isDoNet && isWindows)
	  )
	{
		window.moveTo(0,0);
		window.resizeTo(screen.availWidth,screen.availHeight);
	    window.outerWidth=screen.availWidth;        
	    window.outerHeight=screen.availHeight; 
	}
</script>
</body></html>