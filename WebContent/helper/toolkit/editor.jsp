<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="helper/toolkit/editor.css" rel="stylesheet" />
<link href="skit/css/bootstrap.min.css" rel="stylesheet"/>
<link href="skit/css/font-awesome.min.css" rel="stylesheet"/>
<style type="text/css">
.panel.panel-primary .panel-heading {
    background-color: #455a64;
    border-bottom: 2px solid #1b2428;
    color: #fff;
}
.panel .panel-heading {
    font-size: 12px;
    font-family: "微软雅黑",sans-serif;
    padding: 5px 10px;
	border-bottom: 1px solid transparent;
	border-top-left-radius: 3px;
	border-top-right-radius: 3px;
}
.panel-title {
    font-size: 12px;
    font-family: "微软雅黑",sans-serif;
    display:block;
    width:310px;
    word-break:keep-all;
    white-space:nowrap;
    overflow:hidden;
    text-overflow:ellipsis;
}
.panel .panel-menu {
    float: right;
    right: 30px;
    top: 8px;
    font-weight: 100;
}
</style>
<script type="text/javascript">
var editorContent = "<ww:property value='editorContent'/>";
var editorType = "<ww:property value='editorType'/>";
var encoding = "<ww:property value='encoding'/>";
if( encoding && parent.setTextEncoding ) {
	parent.setTextEncoding(encoding);
}
</script>
<!-- InstanceBeginEditable name="head" -->
<script type="text/javascript" src="helper/toolkit/jquery-1.10.2.min.js"></script>
<script type="text/javascript" src="helper/toolkit/jquery.zclip.min.js"></script>
<script type="text/javascript" src="helper/toolkit/vkbeautify.js"></script>
<script type="text/javascript" src="helper/toolkit/ace.js" charset="utf-8"></script>
<script type="text/javascript">
/* (c) 2013 JAPISoft - Alexandre Brillant */
var jsMode = editorType=="js";
var jsonMode = editorType=="json";
var xmlMode = editorType=="xml";
var sqlMode = editorType=="sql";
var cssMode = editorType=="css";
( function() {
	var editor = null;
	String.prototype.trim = function() {
		return this.replace(/^\s+/g,'').replace(/\s+$/g,'');
	};
	$( function() {
		var aceMode = ( typeof ace == "object" );
		if ( aceMode ) {
			if ( document.getElementById( "editor" ) ) {
				editor = ace.edit("editor");
//				editor.getKeyboardHandler().addEventListener("keydown", function(e){
//					//var ev = window.event||e;
//					if( e.ctrlKey ){
//						alert(e.keyCode);
//					}
//				}, true);
				editor.setTheme("ace/theme/eclipse");
				editorContent = unicode2Chr(editorContent);
				if ( jsMode ) {
					editor.getSession().setMode("ace/mode/javascript");
				} else
				if ( jsonMode ) {
					editor.getSession().setMode("ace/mode/json");
					if( editorContent )
					{
		                var myObject = JSON.parse( editorContent );
		                editorContent = JSON.stringify( myObject, null, 4 ) ;
					}
				} else
				if ( xmlMode ) {
					editor.getSession().setMode("ace/mode/xml" );
					if( editorContent )
					{
						editorContent = vkbeautify.xml( editorContent );
					}
				} else
				if ( sqlMode ) {
					editor.getSession().setMode("ace/mode/sql" );
				} else
				if ( cssMode ) {
					editor.getSession().setMode("ace/mode/css" );
					if( editorContent )
					{
						editorContent = vkbeautify.css( editorContent );
					}
				}
				window.getValue = function() {
					return editor.getValue();	
				};
				window.setValue = function( pText ) {
					var cursor = editor.selection.getCursor();
					editor.setValue( pText );
					if( cursor && cursor.row && cursor.column ){
						editor.moveCursorTo(cursor.row, cursor.column);
					}
				};
				window.getSelection = function() {
					return editor.getSelection();
				};
				window.scrollToBottom = function(){
					var len = editor.session.getLength();
					editor.moveCursorTo(len,0);
				};
				window.scrollToTop = function(){
					editor.moveCursorTo(0,0);
				};
				editor.setValue( editorContent );
				editor.focus();
			}
		}
		
		$( "#copy" ).zclip( {
		   path:'helper/editor/ZeroClipboard.swf',
		   copy: function() { 
		   
			// Simple line field
			if ( $( ".result" ).length > 0 ) {
				return $( ".result" ).val();	
			}
		   
		    if ( aceMode ) {
				return getValue();	
			}
			// Multiple lines field
		   	return $( "textarea" ).val(); 
		   }
		} );
		
	} );
} )();

function formatMe() {
	var content = getValue();
	formatIt(content);
}
function formatIt(content) {
	if( content ){
		try {
			if ( jsonMode ) {
                var myObject = JSON.parse( content );
				setValue( JSON.stringify( myObject, null, 4 ) );
			} else
			if ( xmlMode ) {
				content = vkbeautify.xml( content );
				setValue( content );
			} else
			if ( cssMode ) {
				content = vkbeautify.css( content );
				setValue( content );
			} else
			if( sqlMode ){
				content = vkbeautify.sql( content );
				setValue( content );
			} else
			if( jsMode ){
				content = vkbeautify.js( content );
				setValue( content );
			} else {
				var tips = "未知文件类型不能格式化";
				if( top && top.skit_alert ){
					top.skit_alert( tips );
				}
				else{
					alert( tips );
				}
			}
		} catch( err ) {
			var tips = "您的数据无效请检查格式是否正确";
			if( top && top.skit_alert ){
				top.skit_alert( tips );
			}
			else{
				alert( tips );
			}
		}
	}
}
function formatJavascript()
{
	var clipboard = "";
	openscriptset(clipboard);
}
</script>
<!-- InstanceEndEditable -->
</head>
<body>
<!-- InstanceBeginEditable name="content" -->
<div id="editor" style="width:100%;height:100%;z-index: "></div>
<div style='position:absolute;top:28px;left:28px;cursor:pointer;z-index:999999' id='toolbar'>
   <button type="button" class="btn btn-success btn-circle" id="btnScrollBottom" onclick="scrollToBottom()" title='滚动到底部'>
   	<i class="fa fa-angle-double-down"></i></button><br/><br/>
   <button type="button" class="btn btn-success btn-circle" id="btnScrollTop" onclick="scrollToTop()" title='滚动到顶部'>
   	<i class="fa fa-angle-double-up"></i></button><br/><br/>
   <button type="button" class="btn btn-info btn-circle" onclick="formatMe()" title='格式化'>
   	<i class="fa fa-text-width fa-spin"></i></button>
   	<ww:if test='!"sql".equals(editorType)'>
   	<br/><br/>
   <button type="button" class="btn btn-success btn-circle" onclick="colors()" title='颜色表'>
   	<i class="fa fa-th"></i></button><br/><br/>
   <button type="button" class="btn btn-success btn-circle" onclick="icons()" title='图标'>
   	<i class="fa fa-info"></i></button><br/><br/>
   <button type="button" class="btn btn-circle" id='copy' title='复制'>
   	<i class="fa fa-clipboard"></i></button><br/><br/>
   <button type="button" class="btn btn-circle" id='javascript' onclick="formatJavascript()" title='将选中的字符串按照javascript标准格式化'
   	style='display:none;line-height:1.3;'>
   	<i class="fa fa-font"></i></button>
   	</ww:if>
</div>
<form action="">
<input name='ww' id='ww' type='hidden'/>
<input name='content' id='content' type='hidden'/>
</form>
</body>
<script type="text/javascript">
var	windowWidth = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
var	windowHeight = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
var toolbar = document.getElementById( 'toolbar' );
if( windowWidth ){
	toolbar.style.top = windowHeight - toolbar.clientHeight - 32;
	toolbar.style.left = windowWidth - toolbar.clientWidth - 15;
}
else{
	toolbar.style.display = "none";
}
if( xmlMode ){
	var javascript = document.getElementById( 'javascript' );
	javascript.style.display = "";
}

function showObject(obj)
{
	if( obj )
	{
		var alt = "";
		for(var key in obj )
			alt += "\r\n\t\t"+key+"="+obj[key];
		alert("展示对象内容:"+alt);
	}
}
function openscriptset(content)
{
	if( document.getElementById("iJavascript") )
	{
		return;
	}
	var fileico = "<i class='fa fa-font'></i>";
	var div = document.createElement("div");
	div.id = "div-javascript";
	div.className = "panel panel-primary";
	div.style.marginTop = 3;
	div.style.marginLeft = 3;
	div.style.position = "absolute";
	div.style.width = windowWidth - 24;
	var div1 = document.createElement("div");
	div1.className = "panel-heading";
	div1.style.height = 28;
	div.appendChild(div1);
	var div10 = document.createElement("div");
	div10.style.width = windowWidth - 216;
	div10.style.float = "left";
	div10.style.fontSize = "12px";
	div1.appendChild(div10);
	var span10 = document.createElement("span");
	span10.className = "panel-title";
	span10.innerHTML = fileico+" 请将XML文件中的JAVASCRIPT文字拷贝到编辑页面进行编辑";
	span10.id = "span-";
	span10.style.width = windowWidth - 216;
	div10.appendChild(span10);
	var div11 = document.createElement("div");
	div11.className = "panel-menu";
	div11.innerHTML = 
		"<button type='button' onclick='closescriptset()' data-action='close' class='btn btn-warning btn-action btn-xs' style='margin-left:10px'><i class='fa fa-times'></i></button>";
	div1.appendChild(div11);
	var div2 = document.createElement("div");
	div2.className = "panel-body";
	div2.id = "panel-body";
	div2.innerHTML = "<iframe name='iJavascript' id='iJavascript' class='nonicescroll' style='width:100%;border:0px solid red;'></iframe>";
	div.appendChild(div2);
	document.forms[0].appendChild(div);
	div.style.left = 10;
	div.style.top = 10;
	div.style.zIndex = 10000;
	document.getElementById("iJavascript").style.height = windowHeight - 108;
	
	document.getElementById( 'ww' ).value = windowWidth;
	document.getElementById( 'content' ).value = content;
	document.forms[0].action = "editor!javascript.action";
	document.forms[0].method = "POST";
	document.forms[0].target = "iJavascript";
	document.forms[0].submit();
}
function closescriptset()
{
	try
	{
		var panel = document.getElementById("div-javascript");
		document.forms[0].removeChild(panel);
	}
	catch(e)
	{
		alert("关闭窗口出现异常"+e.message+", 行数"+e.lineNumber);
	}
}
function formatscriptset(){
    var ifr = window.frames["iJavascript"];
    if( ifr && ifr.beautifyScript )
    {
    	ifr.beautifyScript();
    }
}
/**
 * unicode编码转中文
 */
function unicode2Chr(str) 
{
	if (str)
	{
		var st, t, i
		st = '';
		for (i = 1; i <= str.length/4; i++)
		{
			t = str.slice(4*i-4, 4*i-2);
			t = str.slice(4*i-2, 4*i).concat(t);
			st = st.concat('%u').concat(t);
		}
		st = unescape(st);
		return st;
	}
	else
		return "";
}
function colors()
{
	top.skit_frame("helper!colors.action", "颜色表", 320, 680);
}
function icons()
{
	top.skit_frame("https://api.jqueryui.com/resources/icons-list.html", "颜色表", 640, 960);
}

$(document).ready(function(){
	try
	{
	}
	catch(e)
	{
	}
});
</script>
</html>