<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response); %>
<html>
<head>
<meta http-equiv="Pragma" content="no-cache" /> 
<meta http-equiv="Cache-Control" content="no-cache" /> 
<meta http-equiv="Expires" content="0" /> 
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<link type="text/css" href="skit/css/costable.css?v=3" rel="stylesheet">
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
function doMatch()
{
	try
	{
		var myregexp = document.getElementById("myregexp").value;
		var dataval = document.getElementById("dataval").value;
		var m = dataval.match(new RegExp(myregexp));
		if(m == null ) 
	    {
	        skit_alert("'"+dataval+"'不匹配正则表达式"+myregexp);
	    }
		else
		{
	        skit_alert("'"+dataval+"'匹配正则表达式"+myregexp);
		}
	}
	catch(e)
	{
        skit_alert("'"+dataval+"'匹配正则表达式'"+myregexp+"'出现异常"+e);
	}
}
</script>
</head>
<body>
<table>
  <tr>
	<td valign='top'>
		<table>
		  <tr><td>
			  <table>
				<tr>
					<td width='25%'>
			 			 <table>
						   <tr><td valign='top'><textarea type='text' id='myregexp' placeholder='请参考右侧正则表达式模板输入' class='json_edit' ></textarea></td></tr>
						   <tr><td valign='top'><textarea type='text' id='dataval' placeholder='请输入您要匹配校验的字符格式' class='json_edit' ></textarea></td></tr>
						 </table>
					</td>
					<td width='25%'><div id='regexpexample' class='pre nicescroll'>
<ww:property value='regexpexample'/>
					</div></td>
					<td width='50%'><iframe src='http://www.cnblogs.com/mq0036/p/6013225.html' id='regexpwiki' class='pre nicescroll'></iframe></td>
				</tr>
			  </table>
		  </td></tr>
		  <tr><td align='center'>
		<div style='width:280px; border:0px solid red;'>
			<button type="button" class="btn btn-outline btn-primary btn-block btn-xs"
				style='width:128px;float:left;padding-right:10px;' onclick='doMatch();'><i class="fa fa-code"></i> 匹配 </button>
			<button type="button" class="btn btn-outline btn-danger btn-block btn-xs"
				style='width:128px;padding-left:10px;float:right;margin-top:0px;' onclick='top.closeView();'><i class="fa fa-sign-out"></i> 取消</button></div>
		  </td></tr>		  
		</table>
	</td>
  </tr>
</table>
</body>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
	var	windowWidth = window.document.body.clientWidth || window.innerWidth || document.documentElement.clientWidth; 
	var	windowHeight = window.document.body.clientHeight || window.innerHeight || document.documentElement.clientHeight;
	var div;
	var h = 0;
	//发布内容列表位置自适应
	div = document.getElementById( 'myregexp' );
	div.style.height = 40;
	div.style.height = windowHeight/2 - 28;
	div = document.getElementById( 'dataval' );
	div.style.height = 40;
	div.style.height = windowHeight/2 - 28;

	div = document.getElementById( 'regexpwiki' );
	div.style.height = 40;
	div.style.height = windowHeight - 36;
	div.style.width = windowWidth/2 - 30;

	div = document.getElementById( 'regexpexample' );
	div.style.height = 40;
	div.style.height = windowHeight - 36;
	div.style.width = windowWidth/4 - 30;

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
</SCRIPT>
</html>