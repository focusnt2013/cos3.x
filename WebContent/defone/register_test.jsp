<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html class="js flexbox canvas canvastext webgl no-touch geolocation postmessage websqldatabase indexeddb hashchange history draganddrop websockets rgba hsla multiplebgs backgroundsize borderimage borderradius boxshadow textshadow opacity cssanimations csscolumns cssgradients cssreflections csstransforms csstransforms3d csstransitions fontface generatedcontent video audio localstorage sessionstorage webworkers applicationcache svg inlinesvg smil svgclippaths" lang="en">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta charset="utf-8">
    <meta content="IE=edge,chrome=1" http-equiv="X-UA-Compatible">
	<title>注册 - 云架构开放式应用服务系统使用登记</title>
    <link href="skin/defone/login/login.css" rel="stylesheet" type="text/css">
    <link href="skin/defone/login/font-awesome.min.css" rel="stylesheet" type="text/css">
    <link href="images/cos.ico" rel="shortcut icon" type="image/x-icon">
	<link rel="stylesheet" href="skit/css/bootstrap.min.css"/>
	<link rel="stylesheet" href="skit/css/fileinput.min.css" />
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
	.file-drop-zone-title
	{
		color: #aaa;
		font-size: 40px;
		padding: 80px 10px;
	}
	#style_switcher ul.colors {
	    margin: 0;
	    padding-left: 108px;
	    list-style: none;
	}
	#style_switcher ul.colors li + li {
    	margin-left: 10px;
	}
	#style_switcher ul.colors li {
	    list-style: none;
	    float: left;
	    width: 26px;
	    height: 26px;
	    margin-bottom: 10px;
	    cursor: pointer;
	    -webkit-box-shadow: inset 0 0 0 1px rgba(0,0,0,.2);
	    -moz-box-shadow: inset 0 0 0 1px rgba(0,0,0,.2);
	    box-shadow: inset 0 0 0 1px rgba(0,0,0,.2);
	}
	#style_switcher .style_active {
	    -webkit-border-radius: 50%;
	    -moz-border-radius: 50%;
	    border-radius: 50%;
	}	
	.logo-icon i[class*='icon-']{margin-left:-5px;font-size:140%;color: <ww:property value='themeColor'/>}
    </style>
  </head>
  <body class="login">
    <div class="wrapper" style='width:480px;' id='myWrapper'>
      <div class="row">
        <div class="col-lg-12">
          <div class="brand text-center">
            <h1>
              <div class="logo-icon" style="background:#ffffff">
                <i class="icon-linux" id="icon-login"></i>
              </div>
            </h1>
          </div>
        </div>
      </div>
      <div class="row">
        <div class="col-lg-12">
		  <form action="cos!signin.action" method="post" enctype="multipart/form-data">
            <fieldset class="text-center" id='step1' style='display:'>
              <legend>使用本软件需要你请提供您的系统信息</legend>
              <div class="form-group">
                <input id="SysName" name="SysName" class="form-control"
                	data-title='系统名称只能含有汉字、数字、字母、下划线不能以下划线开头和结尾;为了完善信息输入的系统名称不少于4个字，不多于15个字'
                	value='牛逼系统孵化器'
					data-toggle="tooltip" 
					data-placement="top"
					data-trigger='manual'
					onkeydown='$("#SysName").tooltip("hide")'
	                placeholder="请输入您的系统名称"
					type="text">
              </div>
              <div class="form-group">
                <input id="SysDescr" name="SysDescr" class="form-control" 
                	data-title='请输入您的系统说明，例如为公司行政管理提供的信息化系统；系统说明只能含有汉字、数字、字母、下划线不能以下划线开头和结尾;为了完善信息输入的系统描述不少于10个字，不多于35个字'
                	value='开发开放式云架构应用服务框架系统'
                	data-toggle="tooltip" 
					data-placement="top"
					data-trigger='manual'
					onkeydown='$("#SysDescr").tooltip("hide")'
                	placeholder="请输入您的系统说明，例如为公司行政管理提供的信息化系统"
					 type="text">
              </div>
              <div class="form-group">
                <input id="SysContactName" name="SysContactName" class="form-control"
                	value='刘学'
                	data-title='请输入您的联系人姓名'
					data-toggle="tooltip" 
					data-placement="top"
					data-trigger='manual'
					onkeydown='$("#SysContactName").tooltip("hide")'
					placeholder="请输入您的联系人姓名"
					type="text">
              </div>
              <div class="form-group">
                <input id="SysContact" name="SysContact" class="form-control"
                	value='liu3xue@163.com'
                	data-title='请输入正确的邮箱格式'
					data-toggle="tooltip" 
					data-placement="top"
					data-trigger='manual'
					onkeydown='$("#SysContact").tooltip("hide")'
					placeholder="请输入您的联系邮箱"
					type="text">
              </div>
              <div class="text-center" id='div-login'>
                <a class="btn btn-default" href="javascript:stepInputSoftware(true);">下一步</a>
                <br/><br/>
                <a href="main"><img src="images/logo.png"></a>
              </div>
            </fieldset>
            <fieldset class="text-center" id='step2' style='display:none'>
              <legend>请提供您的软件信息</legend>
              <div class="form-group">
                <input id="SoftwareName" name="SoftwareName" class="form-control" 
                	value='COS'
                	data-title='请输入您系统的软件英文标识。只能含有字母、数字、下划线;为了完善信息输入的软件名称不少于2个字，不多于20个字'
					data-toggle="tooltip" 
					data-placement="top"
					data-trigger='manual'
					onkeydown='$("#SoftwareName").tooltip("hide")'
                	placeholder="请输入您系统的软件英文标识"
                	type="text">
              </div>
              <div class="form-group">
                <input id="SoftwareVersion" name="SoftwareVersion" class="form-control"
                	value='4.0.0.0'
                	data-title='请输入您系统的软件版本。请按照<数字>.<数字>.<数字>.<数字>格式输入版本号'
					data-toggle="tooltip" 
					data-placement="top"
					data-trigger='manual'
					onkeydown='$("#SoftwareVersion").tooltip("hide")'
                	placeholder="请输入您系统的软件版本号例如1.13.4.12" 
                	type="text">
              </div>
              <div class="form-group">
                <input id="SoftwareVendor" name="SoftwareVendor" class="form-control"
                	value='深圳翰宏网科技术有限公司'
                	data-title='请输入开发者公司或个人名字。开发者公司或个人名称只能含有汉字、数字、字母、下划线不能以下划线开头和结尾;为了完善信息输入的开发商或开发者名称不少于2个字，不多于20个字'
					data-toggle="tooltip" 
					data-placement="top"
					data-trigger='manual'
					onkeydown='$("#SoftwareVendor").tooltip("hide")'
                	placeholder="请输入开发者公司或个人名字"
                	type="text">
              </div>
              <div class="form-group" id='style_switcher'
              		data-title='请选择界面色系样式'
					data-toggle="tooltip" 
					data-placement="right"
					data-trigger='hover'>
             	  	<input type='hidden' name='Theme' id='themeStyle' value='default'>
            	  <ul class="clearfix colors">
                    <li class="switch-style style_active" data-toggle="tooltip" data-container="body" data-placement="top" title="" data-bg-color="#18bc9c" data-link-color="#ffffff" data-border-color="#18bc9c" data-style="default" style="background-color: #18bc9c;" data-original-title="Default"></li>
                    <li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="" data-bg-color="#23bab5" data-link-color="#ffffff" data-border-color="#23bab5" data-style="blue" style="background-color: #23bab5;" data-original-title="Blue"></li>
                    <li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="" data-bg-color="#674172" data-link-color="#ffffff" data-border-color="#674172" data-style="honey_flower" style="background-color: #674172;" data-original-title="Honey Flower"></li>
                    <li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="" data-bg-color="#DB0A5B" data-link-color="#ffffff" data-border-color="#DB0A5B" data-style="razzmatazz" style="background-color: #DB0A5B;" data-original-title="Razzmatazz"></li>
                    <li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="" data-bg-color="#336E7B" data-link-color="#ffffff" data-border-color="#336E7B" data-style="ming" style="background-color: #336E7B;" data-original-title="Ming"></li>
                    <li class="switch-style" data-toggle="tooltip" data-container="body" data-placement="top" title="" data-bg-color="#ffd800" data-link-color="#ffffff" data-border-color="#336E7B" data-style="yellow" style="background-color: #ffd800;" data-original-title="Yellow"></li>
				</ul>
              </div>
              <div class="text-center" id='div-login'>
                <a class="btn btn-default" href="javascript:stepInputSysinfo();">上一步</a>
                <a class="btn btn-default" href="javascript:stepUploadLogo();">下一步</a>
                <br/>
                <a href="main"><img src="images/logo.png"></a>
              </div>
            </fieldset>
            <fieldset class="text-center" id='step3' style='display:none'>
              <legend>为您的系统上传一个LOGO吧</legend>
              <div class="form-group">
                <input type="file" name="importSysLogo" id='SysLogo' placeholder="您系统的LOGO，PNG图片" >
              </div>
              <div class="text-center" id='div-login'>
                <a class="btn btn-default" href="javascript:stepInputSoftware(false);">上一步</a>
                <a class="btn btn-default" href="javascript:signin();">注册</a>
                <br/>
                <a href="main"><img src="images/logo.png"></a>
              </div>
            </fieldset>
          </form>
        </div>
      </div>
    </div>
    <!-- Footer -->
    <!-- Javascripts -->
<script src="skin/defone/login/ga.js"></script>
<script src="skin/defone/login/jquery.min.js" type="text/javascript"></script>
<script src="skin/defone/login/jquery-ui.js" type="text/javascript"></script>
<script src="skin/defone/js/jquery.blockui.js" type="text/javascript"></script>
<script src="skin/defone/login/modernizr.min.js" type="text/javascript"></script>
<script src="skin/defone/login/login.js" type="text/javascript"></script>
<script src="skit/js/fileinput.js"></script>
<script src="skit/js/fileinput_locale_zh.js"></script>
<%@ include file="component.jsp"%>
<script type="text/javascript">
try
{
    $('.switch-style').on('click', function (event) {
        $('#style_switcher li').removeClass('style_active');
        $(this).addClass('style_active');
        var themeStyle = $( this ).attr( 'data-style' );
        $("#themeStyle").attr('value', themeStyle);
        return false;
    });
    
	$("#SysLogo").fileinput({
			language: 'zh', //设置语言
			uploadUrl: 'cos!uploadLogo.action', //上传的地址
			allowedFileExtensions: ['png'],//接收的文件后缀
			showUpload: true, //是否显示上传按钮
			showCaption: false,//是否显示标题
			showClose: false,
			browseClass: "btn btn-primary", //按钮样式     
			previewSettings: { image: {width: "auto", height: "32px"} },
			//dropZoneEnabled: false,//是否显示拖拽区域
			minImageWidth: 50, //图片的最小宽度
			minImageHeight: 32,//图片的最小高度
			maxImageWidth: 200,//图片的最大宽度
			maxImageHeight: 44,//图片的最大高度
			//maxFileSize: 0,//单位为kb，如果为0表示不限制文件大小
			//minFileCount: 0,
			maxFileCount: 1, //表示允许同时上传的最大文件个数
			enctype: 'multipart/form-data',
			validateInitialCount:true,
			previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
			msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
	});
	//导入文件上传完成之后的事件
	$("#SysLogo").on("fileuploaded", function (event, data, previewId, index) {
	});
}
catch (e)
{
	alert(e);
}
//调整窗口的位置
var _windowWidth = 0;
var _windowHeight = 0;
_windowWidth = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
_windowHeight = window.innerHeight || document.documentElement.clientHeight || window.dockument.body.clientHeight;
function resizeWindow()
{
	try
	{
		var h = _windowHeight;
		var w = _windowWidth;
		var div = document.getElementById('myWrapper');
		var top = (h - div.scrollHeight)/2;
		var left = w/2 - div.scrollWidth/2;
		div.style.top = top;
		div.style.left = left;
	}
	catch (e)
	{
		alert(e);
	}
}
resizeWindow();
function stepInputSoftware(chk)
{
	if( chk )
	{
		var SysName = document.getElementById("SysName");
		var SysDescr = document.getElementById("SysDescr");
		var SysContactName = document.getElementById("SysContactName");
		var SysContact = document.getElementById("SysContact");

	    var regexp1 = "^[a-zA-Z0-9_\u4e00-\u9fa5]+$";
	    var m1 = SysName.value.match(new RegExp(regexp1));
	    if( !m1 )
	    {
    		SysName.focus();
    		$("#SysName").tooltip("show");
	        return;
	    }
	    
		if( SysName.value.length<4 || SysName.value.length>15 )
		{
    		SysName.focus();
    		$("#SysName").tooltip("show");
			return;
		}
		
		m1 = SysDescr.value.match(new RegExp(regexp1));
	    if( !m1 )
	    {
	    	SysDescr.focus();
    		$("#SysDescr").tooltip("show");
	        return;
	    }
	    
		if( SysDescr.value.length<10 || SysDescr.value.length>64 )
		{
			SysDescr.focus();
    		$("#SysDescr").tooltip("show");
			return;
		}

		m1 = SysContactName.value.match(new RegExp(regexp1));
	    if( !m1 )
	    {
	    	SysContactName.focus();
    		$("#SysContactName").tooltip("show");
	        return;
	    }
	    
		if( SysContactName.value.length<2 || SysContactName.value.length>16 )
		{
			SysContactName.focus();
    		$("#SysContactName").tooltip("show");
			return;
		}
		//^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$
		//^([\.a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+
		var regexp2 = "^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$";
		var m2 = SysContact.value.match(new RegExp(regexp2));
	    if( !m2 )
	    {
    		$("#SysContact").tooltip("show");
			SysContact.focus();
	        return;
	    }
	}
    
	document.getElementById("step1").style.display = "none";
	document.getElementById("step2").style.display = "";
	document.getElementById("step3").style.display = "none";
}

function stepInputSysinfo()
{
	document.getElementById("step1").style.display = "";
	document.getElementById("step2").style.display = "none";
	document.getElementById("step3").style.display = "none";
}

function stepUploadLogo()
{
	var SoftwareName = document.getElementById("SoftwareName");
	var SoftwareVersion = document.getElementById("SoftwareVersion");
	var SoftwareVendor = document.getElementById("SoftwareVendor");
	if( SoftwareName.value == "" || SoftwareName.value == SoftwareName.title )
	{
		skit_alert(SoftwareName.title);
		SoftwareName.focus();
		return;
	}
	var regexp1 = "^([A-Za-z])|([a-zA-Z0-9])|([a-zA-Z0-9])|([a-zA-Z0-9_])+$";
    var m1 = SoftwareName.value.match(new RegExp(regexp1));
    if( !m1 )
    {
		SoftwareName.focus();
		$("#SoftwareName").tooltip("show");
        return;
    }
    
	if( SoftwareName.value.length<2 || SoftwareName.value.length>20 )
	{
		SoftwareName.focus();
		$("#SoftwareName").tooltip("show");
	    return;
	}
	
	var regexp2 = "^[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{1,2}(\.[0-9]{1,2})?$";
    var m2 = SoftwareVersion.value.match(new RegExp(regexp2));
    if( !m2 )
    {
		SoftwareVersion.focus();
		$("#SoftwareVersion").tooltip("show");
        return;
    }

    var regexp3 = "^[a-zA-Z0-9_\u4e00-\u9fa5]+$";
    var m3 = SoftwareVendor.value.match(new RegExp(regexp3));
    if( !m3 )
    {
		SoftwareVendor.focus();
		$("#SoftwareVendor").tooltip("show");
        return;
    }
    
    if( SoftwareVendor.value.length<2 || SoftwareVendor.value.length>20 )
	{
		$("#SoftwareVendor").tooltip("show");
		SoftwareVendor.focus();
	    return;
	}
	
	document.getElementById("step1").style.display = "none";
	document.getElementById("step2").style.display = "none";
	document.getElementById("step3").style.display = "";
}

function signin()
{
	document.forms[0].submit();
}

var brandicons = <ww:property value='messageCode'/>;
function changeicon()
{
	var i = Math.round(brandicons.length*Math.random());
	document.getElementById("icon-login").className = brandicons[i];
	window.setTimeout("changeicon()",1000);
}
changeicon();
var alt = "<ww:property value='responseException'/>";
if( alt )
{
	skit_alert(alt, "错误提示");
}
</script>
</body></html>