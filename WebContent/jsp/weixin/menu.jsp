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
.pre {
	font-size:11px;
	font-size:9pt;	
	word-break:normal;
	white-space:pre-wrap;
	background:#fff;pre
	color:#000;
	margin-left: 10px;
	padding-top: 5px;
	padding-bottom:5px;
	padding-left:10px;
	padding-right:10px;
	border:1px dashed #e6e6e6; 
	background: #fffdf2;
}
.json_edit
{
	width: 100%;
	font-size: 12px;
	font-family:微软雅黑,Roboto,sans-serif;
	margin:0 0 1px;
	color: #ffffff;
	background-color: #373737;
}
</style>
<script type="text/javascript">
function doSave()
{
	try
	{
		jQuery.parseJSON(document.getElementById("menujson").value);
		document.forms[0].action="weixin!setmenu.action";
		document.forms[0].submit();
	}
	catch(e)
	{
		top.skit_alert("配置的微信公众号自定义菜单格式错误 "+e);
	}
}
function showExample()
{
	var json = document.getElementById("exampleMenuJson").value;
	top.skit_message(json, "微信公众号自定义菜单配置示例", 320);
}
</script>
</head>
<body>
<form>
<input type='hidden' name='sysid' id='sysid' value="<ww:property value='sysid'/>">
<input type='hidden' name='id' id='id' value="<ww:property value='id'/>">
<table width="100%" border="0">
  <tr>
	<td valign='top' width='50%'>
<div class='ui-tabs .ui-tabs-panel' style='padding:10px 10px'>
	<div class="panel panel-default" style='margin-bottom:0px;'>
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><a><i class='skit_fa_btn fa fa-info-circle'></i></a> 配置微信公众号的菜单</div>
		<div class="panel-body" style='padding-top:10px;padding-bottom:10px;'>
			<textarea type='text' id='menujson' name='menuJson'
			placeholder='请参考右侧微信公众号自定义菜单配置说明进行设置'
			class='json_edit' ><ww:property value="menuJson"/></textarea>
			<textarea type='text' id=exampleMenuJson style='display:none'><ww:property value="exampleMenuJson"/></textarea>
		</div>
	</div>
</div>
		
<div>
	<button type="button" class="btn btn-outline btn-success btn-block"
		style='width:100px;float:left;margin-left:10px;padding-right:10px;margin-top:0px;font-size:12px;' onclick='doSave();'><i class="fa fa-save"></i> 保存</button>

	<button type="button" class="btn btn-outline btn-info btn-block"
		style='width:100px;float:left;margin-left:10px;margin-top:0px;font-size:12px;' onclick='showExample();'><i class="fa fa-group"></i> 示例</button>

	<button type="button" class="btn btn-outline btn-block"
		style='width:100px;float:left;margin-left:10px;margin-top:0px;font-size:12px;' onclick='top.closeView();'><i class="fa fa-close"></i> 取消</button>
</div>	
	</td>
	<td valign='top' >
<div class='ui-tabs .ui-tabs-panel' style='padding:10px 10px'>
	<div class="panel panel-default" style='margin-bottom:0px;'>
		<div class="panel-heading" style='font-size:12px;padding:5px 15px'><a><i class='skit_fa_btn fa fa-info-circle'></i></a> 微信官方帮助指导您配置公众号菜单</div>
		<div class="panel-body" style='padding-top:10px;padding-bottom:10px;padding-left:0px;padding-right:10px;'>

	<div id='weixinwiki' class='pre nicescroll'>
自定义菜单能够帮助公众号丰富界面，让用户更好更快地理解公众号的功能。开启自定义菜单后，公众号界面如图所示：
<img src='http://mmbiz.qpic.cn/mmbiz/PiajxSqBRaEIVJ6bW5EhIpH4kiagbwE6INiclCPicLoIj42zqTUE2UWoWTn47WekOgAzb9DlRaZSSySbmaG4ibHBNtA/0?wx_fmt=jpeg'>
请注意：

1、自定义菜单最多包括3个一级菜单，每个一级菜单最多包含5个二级菜单。
2、一级菜单最多4个汉字，二级菜单最多7个汉字，多出来的部分将会以“...”代替。
3、创建自定义菜单后，菜单的刷新策略是，在用户进入公众号会话页或公众号profile页时，如果发现上一次拉取菜单的请求在5分钟以前，就会拉取一下菜单，如果菜单有更新，就会刷新客户端的菜单。测试时可以尝试取消关注公众账号后再次关注，则可以看到创建后的效果。

自定义菜单接口可实现多种类型按钮，如下：

1、click：点击推事件用户点击click类型按钮后，微信服务器会通过消息接口推送消息类型为event的结构给开发者（参考消息接口指南），并且带上按钮中开发者填写的key值，开发者可以通过自定义的key值与用户进行交互；
2、view：跳转URL用户点击view类型按钮后，微信客户端将会打开开发者在按钮中填写的网页URL，可与网页授权获取用户基本信息接口结合，获得用户基本信息。
3、scancode_push：扫码推事件用户点击按钮后，微信客户端将调起扫一扫工具，完成扫码操作后显示扫描结果（如果是URL，将进入URL），且会将扫码的结果传给开发者，开发者可以下发消息。
4、scancode_waitmsg：扫码推事件且弹出“消息接收中”提示框用户点击按钮后，微信客户端将调起扫一扫工具，完成扫码操作后，将扫码的结果传给开发者，同时收起扫一扫工具，然后弹出“消息接收中”提示框，随后可能会收到开发者下发的消息。
5、pic_sysphoto：弹出系统拍照发图用户点击按钮后，微信客户端将调起系统相机，完成拍照操作后，会将拍摄的相片发送给开发者，并推送事件给开发者，同时收起系统相机，随后可能会收到开发者下发的消息。
6、pic_photo_or_album：弹出拍照或者相册发图用户点击按钮后，微信客户端将弹出选择器供用户选择“拍照”或者“从手机相册选择”。用户选择后即走其他两种流程。
7、pic_weixin：弹出微信相册发图器用户点击按钮后，微信客户端将调起微信相册，完成选择操作后，将选择的相片发送给开发者的服务器，并推送事件给开发者，同时收起相册，随后可能会收到开发者下发的消息。
8、location_select：弹出地理位置选择器用户点击按钮后，微信客户端将调起地理位置选择工具，完成选择操作后，将选择的地理位置发送给开发者的服务器，同时收起位置选择工具，随后可能会收到开发者下发的消息。
9、media_id：下发消息（除文本消息）用户点击media_id类型按钮后，微信服务器会将开发者填写的永久素材id对应的素材下发给用户，永久素材类型可以是图片、音频、视频、图文消息。请注意：永久素材id必须是在“素材管理/新增永久素材”接口上传后获得的合法id。
10、view_limited：跳转图文消息URL用户点击view_limited类型按钮后，微信客户端将打开开发者在按钮中填写的永久素材id对应的图文消息URL，永久素材类型只支持图文消息。请注意：永久素材id必须是在“素材管理/新增永久素材”接口上传后获得的合法id。

请注意，3到8的所有事件，仅支持微信iPhone5.4.1以上版本，和Android5.4以上版本的微信用户，旧版本微信用户点击后将没有回应，开发者也不能正常接收到事件推送。9和10，是专门给第三方平台旗下未微信认证（具体而言，是资质认证未通过）的订阅号准备的事件类型，它们是没有事件推送的，能力相对受限，其他类型的公众号不必使用。

接口调用请求说明

http请求方式：POST（请使用https协议） https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN

click和view的请求示例

 {
     "button":[
     {	
          "type":"click",
          "name":"今日歌曲",
          "key":"V1001_TODAY_MUSIC"
      },
      {
           "name":"菜单",
           "sub_button":[
           {	
               "type":"view",
               "name":"搜索",
               "url":"http://www.soso.com/"
            },
            {
               "type":"view",
               "name":"视频",
               "url":"http://v.qq.com/"
            },
            {
               "type":"click",
               "name":"赞一下我们",
               "key":"V1001_GOOD"
            }]
       }]
 }

其他新增按钮类型的请求示例

{
	"button": [
        {
            "name": "扫码", 
            "sub_button": [
                {
                    "type": "scancode_waitmsg", 
                    "name": "扫码带提示", 
                    "key": "rselfmenu_0_0", 
                    "sub_button": [ ]
                }, 
                {
                    "type": "scancode_push", 
                    "name": "扫码推事件", 
                    "key": "rselfmenu_0_1", 
                    "sub_button": [ ]
                }
            ]
        }, 
        {
            "name": "发图", 
            "sub_button": [
                {
                    "type": "pic_sysphoto", 
                    "name": "系统拍照发图", 
                    "key": "rselfmenu_1_0", 
                   "sub_button": [ ]
                 }, 
                {
                    "type": "pic_photo_or_album", 
                    "name": "拍照或者相册发图", 
                    "key": "rselfmenu_1_1", 
                    "sub_button": [ ]
                }, 
                {
                    "type": "pic_weixin", 
                    "name": "微信相册发图", 
                    "key": "rselfmenu_1_2", 
                    "sub_button": [ ]
                }
            ]
        }, 
        {
            "name": "发送位置", 
            "type": "location_select", 
            "key": "rselfmenu_2_0"
        },
        {
           "type": "media_id", 
           "name": "图片", 
           "media_id": "MEDIA_ID1"
        }, 
        {
           "type": "view_limited", 
           "name": "图文消息", 
           "media_id": "MEDIA_ID2"
        }
    ]
}

参数说明
参数	是否必须	说明
button	是	一级菜单数组，个数应为1~3个
sub_button	否	二级菜单数组，个数应为1~5个
type	是	菜单的响应动作类型
name	是	菜单标题，不超过16个字节，子菜单不超过60个字节
key	click等点击类型必须	菜单KEY值，用于消息接口推送，不超过128字节
url	view类型必须	网页链接，用户点击菜单可打开链接，不超过1024字节
media_id	media_id类型和view_limited类型必须	调用新增永久素材接口返回的合法media_id
					</div>	
		</div>
	</div>
</div>	
	
	</td>
  </tr>
</table>
</form>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<script src="skit/js/auto-line-number.js"></script>
<SCRIPT LANGUAGE="JavaScript">
/*实现窗口对齐*/
function resizeWindow()
{
	var	windowWidth = window.document.body.clientWidth || window.innerWidth || document.documentElement.clientWidth; 
	var	windowHeight = window.document.body.clientHeight || window.innerHeight || document.documentElement.clientHeight;
	var div;
	var h = 0;
	//发布内容列表位置自适应
	div = document.getElementById( 'menujson' );
	div.style.height = windowHeight - 108;

	div = document.getElementById( 'weixinwiki' );
	div.style.height = windowHeight - 64;
	div.style.width = windowWidth/2 - 30;

	var skitViewDiv = $( '.nicescroll' );
	if( skitViewDiv )
		skitViewDiv.niceScroll({
			cursorcolor: '#eee',
			railalign: 'right',
			cursorborder: "none", 
			horizrailenabled: false, 
			zindex: 2001, 
			left: '0px', 
			cursoropacitymax: 0.6, 
			cursorborderradius: "0px", 
			spacebarenabled: false });
}
resizeWindow();
$("#menujson").setTextareaCount({
	width: "30px",
	bgColor: "#000",
	color: "#FFF",
	display: "inline-block",
});
</SCRIPT>
</html>