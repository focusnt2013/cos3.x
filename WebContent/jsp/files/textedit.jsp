<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<meta http-equiv="Pragma" content="no-cache" /> 
<meta http-equiv="Cache-Control" content="no-cache" /> 
<meta http-equiv="Expires" content="0" /> 
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<style type='text/css'>
.text
{
	width: 100%;
	font-size: 12px;
	font-family:微软雅黑,Roboto,sans-serif;
	margin:0 0 1px;
	color: #ffffff;
	background-color: #000;
	border: 0px;
}
</style>
</head>
<body>
<form>
<input type='hidden' name='encoding' id='encoding' value="<ww:property value='encoding'/>">
<input type='hidden' name='filetype' id='filetype' value="<ww:property value='filetype'/>">
<input type='hidden' name='path' id='path' value='<ww:property value='path'/>'>
<input type='hidden' name='rootdir' id='rootdir' value='<ww:property value='rootdir'/>'>
<input type='hidden' name='ip' id='ip' value="<ww:property value='ip'/>">
<input type='hidden' name='port' id='port' value="<ww:property value='port'/>">
<input type='hidden' name='ww' id='ww' value="<ww:property value='ww'/>">
<textarea id='editorContent' name='editorContent' class='text' ><ww:property value="localData"/></textarea>
<iframe name='iEditor' id='iEditor' class='nonicescroll' style='display:none;width:100%;border:0px;'></iframe>
</form>
</body>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
var path = "<ww:property value='path'/>";
var editor = document.getElementById( 'editorContent' );
var encoding = document.getElementById( 'encoding' ).value;
if( encoding && parent.setTextEncoding ) {
	parent.setTextEncoding(encoding);
}
var a = "";
a = path.lastIndexOf(".xml")>0?"xml":a;
a = a?a:(path.lastIndexOf(".json")>0?"json":a);
a = a?a:(path.lastIndexOf(".js")>0?"javascript":a);
a = a?a:(path.lastIndexOf(".css")>0?"css":a);
a = a?a:(path.lastIndexOf(".inc")>0?"xml":a);
a = a?a:(path.lastIndexOf(".txt")>0?"text":a);
a = a?a:"text";
if( a ){
	editor.style.display = "none";
	editor = document.getElementById( 'iEditor' );
	editor.style.display = "";
	document.forms[0].method = "POST";
	document.forms[0].action = "editor!"+a+".action";
	document.forms[0].target = "iEditor";
	document.forms[0].submit();
}
function resizeWindow()
{
	editor.style.height = windowHeight;
}
function getContent(){
    return editor.value;
}
function save(encoding)
{
	if( a )
	{
	    var ifr = window.frames["iEditor"];
	    if( ifr && ifr.getValue )
	    {
			document.getElementById( 'editorContent' ).value = ifr.getValue();
	    }
	}
	document.getElementById( 'encoding' ).value = encoding;
	document.forms[0].action = "files!textsave.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "";
	document.forms[0].submit();
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<script src="skit/js/auto-line-number.js"></script>
<SCRIPT type="text/javascript">
if( editor.id == "textedit" ){
	$("#editorContent").setTextareaCount({
		width: "30px",
		bgColor: "#373737",
		color: "#FFF",
		display: "inline-block",
	});
}
</SCRIPT>
</html>