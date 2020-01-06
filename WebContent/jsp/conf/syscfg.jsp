<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>

<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<meta http-equiv="Pragma" content="no-cache" /> 
<meta http-equiv="Cache-Control" content="no-cache" /> 
<meta http-equiv="Expires" content="0" /> 
<link type="text/css" href="skit/css/cosinput.css" rel="stylesheet">
<style type='text/css'>
#style_switcher ul.colors {
    margin: 0;
    padding-left: 0px;
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
</style>
<%=Kit.getDwrJsTag(request,"interface/SysCfgMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body>
<div class="panel panel-default" style='margin:10px'>
	<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-cogs'></i> <ww:text name="label.ema.config.sysparam"/></div>
 	<div class="panel-body" style='padding: 0px;'>
	  <table>
		<tr>
			<td width='200'>系统名称</td>
			<td>
				<input type='text' title='系统名称'
					   class='skit_input_text' autocomplete='off' 
					   name='SysName'
				       value='<ww:property value="profile.getString('SysName')"/>'
					   onfocus="return onModifyFocus(this);"
					   onblur='return onModify(this);'
					   onkeyup="return onModifyKeyup(this);">
			</td>
		</tr>
		<tr>
			<td>系统说明</td>
			<td>
				<input type='text' title='系统说明'
					   class='skit_input_text' autocomplete='off' 
					   name='SysDescr'
				       value='<ww:property value="profile.getString('SysDescr')"/>'
					   onfocus="return onModifyFocus(this);"
					   onblur='return onModify(this);'
					   onkeyup="return onModifyKeyup(this);">
			</td>
		</tr>
		<tr>
			<td>系统管理员名字</td>
			<td>
				<input type='text'
					   class='skit_input_text' title='系统管理员姓名' autocomplete='off' 
					   name='SysContactName'
				       value='<ww:property value="profile.getString('SysContactName')"/>'
					   onfocus="return onModifyFocus(this);"
					   onblur='return onModify(this);'
					   onkeyup="return onModifyKeyup(this);">
			</td>
		</tr>
		<tr>
			<td>系统管理员联系方式</td>
			<td>
				<input type='text'
					   class='skit_input_text' title='系统管理员联系方式' autocomplete='off' 
					   name='SysContact'
				       value='<ww:property value="profile.getString('SysContact')"/>'
					   onfocus="return onModifyFocus(this);"
					   onblur='return onModify(this);'
					   onkeyup="return onModifyKeyup(this);">
			</td>
		</tr>
		<tr title='系统LOGO显示在系统主界面框架左上角，用来标识业务系统的类别。系统图标的规格是width不超过200，height不超过50像素。系统图标的格式必须是PNG，背景透视。'>
			<td>系统LOGO</td>
			<td>
				<input type='file' name='uploadfile' id='SysLogo'>
			</td>
		</tr>
		<tr>
			<td>系统色系样式</td>
			<td>
              <div id='style_switcher'
              		data-title='请选择界面色系样式'
					data-toggle="tooltip" 
					data-placement="right"
					data-trigger='hover'>
             	  	<input type='hidden' name='Theme' id='themeStyle' value='default'>
            	  <ul class="clearfix colors">
                    <li class="switch-style <ww:if test='datatype.equals("default")'>style_active</ww:if>" data-toggle="tooltip" data-container="body" data-placement="top" title="" data-bg-color="#18bc9c" data-link-color="#ffffff" data-border-color="#18bc9c" data-style="default" style="background-color: #18bc9c;" data-original-title="Default"></li>
                    <li class="switch-style <ww:if test='datatype.equals("blue")'>style_active</ww:if>" data-toggle="tooltip" data-container="body" data-placement="top" title="" data-bg-color="#23bab5" data-link-color="#ffffff" data-border-color="#23bab5" data-style="blue" style="background-color: #23bab5;" data-original-title="Blue"></li>
                    <li class="switch-style <ww:if test='datatype.equals("honey_flower")'>style_active</ww:if>" data-toggle="tooltip" data-container="body" data-placement="top" title="" data-bg-color="#674172" data-link-color="#ffffff" data-border-color="#674172" data-style="honey_flower" style="background-color: #674172;" data-original-title="Honey Flower"></li>
                    <li class="switch-style <ww:if test='datatype.equals("razzmatazz")'>style_active</ww:if>" data-toggle="tooltip" data-container="body" data-placement="top" title="" data-bg-color="#DB0A5B" data-link-color="#ffffff" data-border-color="#DB0A5B" data-style="razzmatazz" style="background-color: #DB0A5B;" data-original-title="Razzmatazz"></li>
                    <li class="switch-style <ww:if test='datatype.equals("ming")'>style_active</ww:if>" data-toggle="tooltip" data-container="body" data-placement="top" title="" data-bg-color="#336E7B" data-link-color="#ffffff" data-border-color="#336E7B" data-style="ming" style="background-color: #336E7B;" data-original-title="Ming"></li>
                    <li class="switch-style <ww:if test='datatype.equals("yellow")'>style_active</ww:if>" data-toggle="tooltip" data-container="body" data-placement="top" title="" data-bg-color="#ffd800" data-link-color="#ffffff" data-border-color="#336E7B" data-style="yellow" style="background-color: #ffd800;" data-original-title="Yellow"></li>
				</ul>
              </div>
			</td>
		</tr>
		<tr>
			<td>授权码</td>
			<td><ww:property value="profile.getString('SysObjectID')"/></td>
		</tr>
	  </table>
	</div>
</div>

<div class="panel panel-default" style='margin:10px'>
	<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-user-secret'></i> <ww:text name="系统安全配置"/></div>
	<div class="panel-body" style='padding: 0px;'>
		<table>
			<tr>
				<td width='200'>首次登录强制重置密码</td>
				<td>
					<input type='checkbox' title='首次登录强制重置密码'
						   class='skit_input_checkbox' 
						   <ww:if test='profile.has("AuthFirstReset")&&profile.getString("AuthFirstReset").equals("true")'>checked</ww:if>
						   name='AuthFirstReset'
						   onclick="onModifyCheckbox(this);">
				</td>
			</tr>
			<tr>
				<td width='200'>用户设置密码强度</td>
				<td>
					<input type='checkbox' title='密码不包含用户名'
						   class='skit_input_checkbox' 
						   <ww:if test='profile.has("AuthUnincludeUsername")&&profile.getString("AuthUnincludeUsername").equals("true")'>checked</ww:if>
						   name='AuthUnincludeUsername'
						   onclick="onModifyCheckbox(this);">
					 密码不包含用户名
					<input type='checkbox' title='用户密码包含字符、数字和下划线,以及 $%&等特殊字符'
						   class='skit_input_checkbox' 
						   <ww:if test='profile.has("AuthPasswordRegexp")&&profile.getString("AuthPasswordRegexp").equals("true")'>checked</ww:if>
						   name='AuthPasswordRegexp'
						   onclick="onModifyCheckbox(this);">
					 用户密码包含字符、数字和下划线,以及 ^%&',;=?$/"等特殊字符
				</td>
			</tr>
			<tr>
				<td width='200'>用户设置密码长度</td>
				<td>
					<input type='text'
						   class='skit_input_text' title='用户设置密码强度' autocomplete='off' 
						   name='AuthPasswordLength'
						   <ww:if test='profile.has("AuthPasswordLength")'>value='<ww:property value="profile.getString('AuthPasswordLength')"/>'</ww:if>
						   <ww:else>value='6'</ww:else>
						   onfocus="return onModifyFocus(this);"
						   onblur='return onModify(this);'
						   onkeyup="return onModifyKeyup(this);"
						   style='text-align:center;width:64'>位
				</td>
			</tr>
			<tr>
				<td width='200'>登录失败重试次数</td>
				<td>
					<input type='text'
						   class='skit_input_text' title='登录失败重试次数' autocomplete='off' 
						   name='AuthFailRepeat'
						   <ww:if test='profile.has("AuthFailRepeat")'>value='<ww:property value="profile.getString('AuthFailRepeat')"/>'</ww:if>
						   <ww:else>value='5'</ww:else>
						   onfocus="return onModifyFocus(this);"
						   onblur='return onModify(this);'
						   onkeyup="return onModifyKeyup(this);"
						   style='text-align:center;width:64'>次
				</td>
			</tr>
			<tr>
				<td>登录错误锁定时间</td>
				<td>
					<input type='text'
						   class='skit_input_text' title='登录错误锁定时间' autocomplete='off' 
						   name='AuthFailLock'
						   <ww:if test='profile.has("AuthFailLock")'>value='<ww:property value="profile.getString('AuthFailLock')"/>'</ww:if>
						   <ww:else>value='10'</ww:else>
						   onfocus="return onModifyFocus(this);"
						   onblur='return onModify(this);'
						   onkeyup="return onModifyKeyup(this);"
						   style='text-align:center;width:64'>分钟
				</td>
			</tr>
			<tr>
				<td>强制重置密码时间</td>
				<td>
					<input type='text'
						   class='skit_input_text' title='强制重置密码时间' autocomplete='off' 
						   name='AuthResetPeriod'
						   <ww:if test='profile.has("AuthResetPeriod")'>value='<ww:property value="profile.getString('AuthResetPeriod')"/>'</ww:if>
						   <ww:else>value=''</ww:else>
						   onfocus="return onModifyFocus(this);"
						   onblur='return onModify(this);'
						   onkeyup="return onModifyKeyup(this);"
						   placeholder=''
						   style='text-align:center;width:64'>天  <span style='color:red'>(不设置强制重置密码时间只提醒用户重置密码不强制关闭)</span>
				</td>
			</tr>
		</table>
	</div>
</div>

<div class="panel panel-default" style='margin:10px'>
	<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-envelope'></i> <ww:text name="系统邮箱配置"/></div>
	<div class="panel-body" style='padding: 0px;'>
		<table>
			<tr>
				<td width='200'>发送系统邮件的SMTP服务器</td>
				<td>
					<input type='text'
						   class='skit_input_text' title='发送系统邮件的SMTP服务器' autocomplete='off' 
						   name=SMTP
					       value='<ww:property value="profile.getString('SMTP')"/>'
						   onfocus="return onModifyFocus(this);"
						   onblur='return onModify(this);'
						   onkeyup="return onModifyKeyup(this);">
				</td>
			</tr>
			<tr>
				<td>系统邮箱POP3帐号</td>
				<td>
					<input type='text'
						   class='skit_input_text' title='系统邮箱POP3帐号' autocomplete='off' 
						   name='POP3Username'
					       value='<ww:property value="profile.getString('POP3Username')"/>'
						   onfocus="return onModifyFocus(this);"
						   onblur='return onModify(this);'
						   onkeyup="return onModifyKeyup(this);">
				</td>
			</tr>
			<tr>
				<td>系统邮箱POP3密码</td>
				<td>
					<input type='password'
						   class='skit_input_text' title='系统邮箱POP3密码' autocomplete='off' 
						   name='POP3Password'
					       value='<ww:property value="profile.getString('POP3Password')"/>'
						   onfocus="return onModifyFocus(this);"
						   onblur='return onModify(this);'
						   onkeyup="return onModifyKeyup(this);">
				</td>
			</tr>
			<tr>
				<td>系统邮箱标题</td>
				<td>
					<input type='text'
						   class='skit_input_text' title='系统邮箱标题' autocomplete='off' 
						   name='SysMailName'
					       value='<ww:property value="profile.getString('SysMailName')"/>'
						   onfocus="return onModifyFocus(this);"
						   onblur='return onModify(this);'
						   onkeyup="return onModifyKeyup(this);">
				</td>
			</tr>
		</table>
	</div>
</div>

<div class="panel panel-default" style='margin:10px'>
	<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-check-square-o'></i> <ww:text name="其它参数"/></div>
	<div class="panel-body" style='padding: 0px;'>
		<table>
			<tr>
				<td width='200'>演示用系统消息通知开关</td>
				<td>
					<input type='checkbox' title='演示用系统消息通知开关'
						   class='skit_input_checkbox' <ww:if test='profile.getString("DemoNotifies").equals("true")'>checked</ww:if>
						   name='DemoNotifies'
						   onclick="onModifyCheckbox(this);">
				</td>
			</tr>
			<tr>
				<td>禁用系统消息通知开关</td>
				<td>
					<input type='checkbox' title='禁用系统消息通知开关'
						   class='skit_input_checkbox' <ww:if test='profile.getString("DisableNotifies").equals("true")'>checked</ww:if>
						   name='DisableNotifies'
						   onclick="onModifyCheckbox(this);">
					 禁止系统消息通知打开，关闭我的工作入口
				</td>
			</tr>
			<tr>
				<td>主界面框架系统HTTPS开关</td>
				<td>
					<input type='checkbox' title='主界面框架系统HTTPS开关'
						   class='skit_input_checkbox' <ww:if test='profile.getString("EnableHttps").equals("true")'>checked</ww:if>
						   name='EnableHttps'
						   onclick="onModifyCheckbox(this);">
				   	当前主界面框架系统前端访问使用HTTPS/SSL的时候激活该配置
				</td>
			</tr>
			<tr>
				<td>系统日志保存天数</td>
				<td>
					<input type='text'
						   class='skit_input_text' title='保' autocomplete='off' 
						   name='SyslogDays'
						   <ww:if test='profile.has("SyslogDays")'>value='<ww:property value="profile.getString('SyslogDays')"/>'</ww:if>
						   <ww:else>value='30'</ww:else>
						   onfocus="return onModifyFocus(this);"
						   onblur='return onModify(this);'
						   onkeyup="return onModifyKeyup(this);"
						   style='text-align:center;width:64'>天
				</td>
			</tr>
			<tr>
				<td>系统邮件保存天数</td>
				<td>
					<input type='text'
						   class='skit_input_text' title='保' autocomplete='off' 
						   name='SysemailDays'
						   <ww:if test='profile.has("SysemailDays")'>value='<ww:property value="profile.getString('SysemailDays')"/>'</ww:if>
						   <ww:else>value='30'</ww:else>
						   onfocus="return onModifyFocus(this);"
						   onblur='return onModify(this);'
						   onkeyup="return onModifyKeyup(this);"
						   style='text-align:center;width:64'>天
				</td>
			</tr>
		</table>
	</div>
</div>
<div class="panel panel-default" style='margin:10px'>
	<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-columns'></i> <ww:text name="label.ema.config.skin"/></div>
	<div class="panel-body" style='padding: 0px;'>
	  	<table>
			<tr>
				<td width='200'><ww:text name="label.ema.config.skin_type"/></td>
				<td width='120'>
    				<div class="form-group" style='margin:3px'>
        				<div class="input-group">
							<input class="form-control"
								style='font-size:12px;background: #fffae6;'
								value="<ww:property value="skin.name"/>" type="text" id='skinType'>
        					<div class="input-group-btn">
        						<button type="button" class="btn btn-info dropdown-toggle"
        							data-toggle="dropdown"
									aria-expanded="false"
									style='display: inline-block;height:34px;'><span class="caret"></span></button>
                                <ul class="dropdown-menu pull-right">
                                    <li><a>defone</a></li>
                                </ul>	
        					</div>
						</div>
					</div>
				</td>
				<td>
					<span id='skinPreview' style='width:100%;height:21px;background:<ww:property value="skin.color"/>;'></span>
				</td>
			</tr>
			<tr>
				<td><ww:text name="label.ema.config.skin_auth"/></td>
				<td colspan='2' id='skinAuth'><ww:property value='skin.author'/></td>
			</tr>
			<tr>
				<td><ww:text name="label.ema.config.skin_version"/></td>
				<td colspan='2' id='skinVersion'><ww:property value='skin.version'/></td>
			</tr>
		</table>	
	</div>
</div>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<style type='text/css'>
body {overflow-y:auto;overflow-x:hidden; margin-top:0px; margin-left:0px; margin-bottom:0px; margin-right:0px }
</style>
<link rel="stylesheet" href="skit/css/bootstrap.min.css" />
<link rel="stylesheet" href="skit/css/font-awesome.min.css" />
<link rel="stylesheet" href="skit/css/fileinput.min.css" />
<style>
.file-drop-zone-title
{
	background-color: #000;
	color: #aaa;
	font-size: 40px;
	padding: 32px 10px;
}
.file-preview-frame img {
	background-color: #000;
}
</style>
<script src="skit/js/fileinput.js"></script>
<script src="skit/js/fileinput_locale_zh.js"></script>
<script src="skin/defone/js/bootstrap.min.js"></script>
<script type="text/javascript">
var focusValue = "";
function onModifyFocus()
{
	var args = onModifyFocus.arguments;
	focusValue = args[0].value;
}
function onModifyKeyup()
{
	var args = onModifyKeyup.arguments;
	var e = event.srcElement;
    var k = event.keyCode;
    if( k == 27 )
    {
    	args[0].parentNode.focus();
    }
}

function onModifyCheckbox(chk)
{
	SysCfgMgr.modifyProperty(chk.name, chk.checked?"true":"false", chk.title,{
		callback:function(ret) {
			skit_alert(ret);
		},
		timeout:30000,
		errorHandler:function(message) { skit_alert(""+message+""); }
	});
	focusValue = "";
}

function onModify()
{
	var args = onModify.arguments;
    if(args[0].value != focusValue)
    {
    	setData(args[0].name, args[0].value, args[0].title);
		focusValue = "";
	}
}
function setData()
{
	var args = setData.arguments;
	SysCfgMgr.modifyProperty(args[0], args[1], args[2],
	{
		callback:function(ret) {
			if( "Theme" == args[0] )
			{
				skit_alert(" ");

				skit_confirm("你色系样式配置完成，是否马上重新刷新？", function(yes){
					if( yes )
					{
						parent.window.location.reload();
					}
				});
				return;
			}
			skit_alert(ret);
		},
		timeout:30000,
		errorHandler:function(message) { skit_alert("<ww:text name='label.ema.config.exception'/>"+message+"<ww:text name='label.ema.config.exception.end'/>"); }
	});
}

var v = 0;
function showUploadLogo(){
	try
	{
		v += 1;
		var html = "<img src='<%=Kit.URL_IMAGEPATH(request)%>logo.png?v="+v+"' id='previewLogo'>";
		$("#SysLogo").fileinput({
			language: 'zh', //设置语言
			uploadUrl: 'syscfg!uploadLogo.action', //上传的地址
			allowedFileExtensions: ['png'],//接收的文件后缀
			showUpload: true, //是否显示上传按钮
			showCaption: false,//是否显示标题
			showClose: false,
			showPreview: true,
			browseClass: "btn btn-success", //按钮样式     
			previewSettings: { image: {width: "250px", height: "50px"}},
			previewFileIcon: '<i class="fa fa-file"></i>', 
			dropZoneEnabled: true,//是否显示拖拽区域
			dropZoneTitle: html+
				"<br/><span style='font-size:16px;color:#fff;'>系统LOGO显示在系统主界面框架左上角，用来标识业务系统的类别。系统图标的规格是width不超过245像素，height不超过44像素。系统图标的格式必须是PNG，背景透视。</span>",
			minImageWidth: 40, //图片的最小宽度
			minImageHeight: 40,//图片的最小高度
			maxImageWidth: 245,//图片的最大宽度
			maxImageHeight: 45,//图片的最大高度
			maxFileCount: 1, //表示允许同时上传的最大文件个数
			enctype: 'multipart/form-data',
			validateInitialCount:true,
			msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
					
		});
		//$( '.file-preview' ).hide();
		$("#SysLogo").on("fileloaded", function (data, previewId, index) {
			//document.getElementById("divPreviewLogo").style.display = "none";
			//$( '.file-preview' ).show();
		});
		//导入文件上传完成之后的事件
		$("#SysLogo").on("fileuploaded", function (event, data, previewId, index) {
			$("#SysLogo").fileinput("destroy");
			window.setTimeout(function(){
				showUploadLogo();
			}, 500);
			//document.getElementById("divPreviewLogo").style.display = "";
			//$( '.file-preview' ).hide();
			//document.getElementById("previewLogo").src = data.response["url"];
		});
	}
	catch (e)
	{
		alert(e);
	}
}
showUploadLogo();

function previewSkin()
{
	var args = previewSkin.arguments;
	inputSkinType.value = args[0];
	document.getElementById("skinPreview").style.background = args[4];
	document.getElementById("skinAuth").innerText = args[2];
	document.getElementById("skinVersion").innerText = args[3];
}
//调用顶层框架portal.jsp的改变皮肤的方法更换皮肤
function activeSkin()
{
	if( inputSkinType.value == "" )
	{
		skit_alert("<ww:text name='label.ema.config.skin.active_tip1'/>");
		return;
	}
	SysCfgMgr.changeSkin(inputSkinType.value,{
		callback:function() {
			//parent.window.location = "login!portal.action";
			parent.window.location.reload();
		},
		timeout:10000,
		errorHandler:function(message) { skit_alert(message); }
	});	
}

$('.switch-style').on('click', function (event) {
    $('#style_switcher li').removeClass('style_active');
    $(this).addClass('style_active');
    var themeStyle = $( this ).attr( 'data-style' );
    setData("Theme", themeStyle, "系统色系样式");
    return false;
});
</script>
</html>