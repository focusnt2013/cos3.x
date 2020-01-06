<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
	<link href="skit/css/bootstrap.min.css" rel="stylesheet"/>
	<link href="skit/css/font-awesome.min.css" rel="stylesheet"/>
    <link rel=stylesheet href="helper/toolkit/javascript.css">
    <script src="helper/toolkit/jsformat.js" type="text/javascript" ></script>
    <script src="helper/toolkit/javascript.js" type="text/javascript" ></script>
	<script src="helper/toolkit/jquery.zclip.min.js" type="text/javascript" ></script>	
<style>
.CodeMirror{
	position:absolute;
	left:20px;top:20px;
	font-size:88%;
	background:#1d1e1c;
	font-family:monaco,monospace;
	height:auto;
	width:<ww:property value='ww'/>px;}    
</style>
<SCRIPT type="text/javascript">
function beautifyScript()
{
	var txt = editor.getValue();
	txt = js_beautify(txt,4,'',0);
	return editor.setValue(txt);
}
function getValue()
{
	return editor.getValue();
}
function getScript()
{
	return editor.getValue();
}
var _script;
function setScript(script)
{
	editor.setValue(script);
	_script = script;
}
</SCRIPT>
</head>
<body onload="main()" style='overflow-x: hidden;'>
<div class="whiteboard" id='divWhitboard'>
    <div class="toolbar">
       <button data-type="toggle" data-target="configure,output"><i class="fa fa-cogs"></i> 配置</button>
	   <button type="button" onclick="beautifyScript()" style='color:#fff;margin-left:16px;'
	   	onmouseover='this.style.color="#aaa";' onmouseout='this.style.color="#fff";'><i class="fa fa-text-width"></i> 格式化</button> 
	   <button type="button" id='copy' style='color:#fff;margin-left:16px;'
	   	onmouseover='this.style.color="#aaa";' onmouseout='this.style.color="#fff";'><i class="fa fa-copy"></i> 复制</button>
    </div>

	<div id="configure">
	  <div>
	    <h4>Report</h4>
	    <button data-type="pref" id="complex">Cyclomatic complexity</button>
	    <button data-type="pref" id="unused">Unused variables</button>
	    <button data-type="pref" id="undef">Undefined variables</button>
	  </div>
	
	  <div>
	    <h4>Warn</h4>
	    <button data-type="pref" id="eqnull">About == null</button>
	    <button data-type="pref" id="debug">About debugging code</button>
	    <button data-type="pref" id="forin">About unsafe for..in</button>
	    <button data-type="pref" id="noarg">About arguments.caller and .callee</button>
	    <button data-type="pref" id="boss">About assignments if/for/...</button>
	    <button data-type="pref" id="loopfunc">About functions inside loops</button>
	    <button data-type="pref" id="evil">About eval</button>
	    <button data-type="pref" id="laxbreak">About unsafe line breaks</button>
	    <button data-type="pref" id="bitwise">About potential typos in logical operators</button>
	    <button data-type="pref" id="strict">When code is not in strict mode</button>
	    <button data-type="pref" id="nonew">When new is used for side-effects</button>
	  </div>
	
	  <div>
	    <h4>Assume</h4>
	    <button data-type="pref" id="browser">Browser</button>
	    <button data-type="pref" id="node">NodeJS</button>
	    <button data-type="pref" id="jquery">jQuery</button>
	    <button data-type="pref" id="devel">Development (console, etc.)</button>
	    <button data-type="pref" id="esnext">New JavaScript features (ES6)</button>
	    <button data-type="pref" id="moz">Mozilla JavaScript extensions</button>
	    <button data-type="pref" id="es3">Older environments (ES3)</button>
	  </div>
	</div>

	<div id="output">
	  <div data-type="metrics" class="report" style="display:none">
	    <h4>Metrics</h4>
	    <div></div>
	  </div>
	  <table data-type="errors" class="report"></table>
	  <table data-type="undef" class="report"></table>
	  <table data-type="unused" class="report"></table>
	</div>
</div>
<script>
var ww = <ww:property value='ww'/>;
var	windowWidth = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
var	windowHeight = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
if( ww == 0 )
{
	ww = windowWidth - 50;
}
document.getElementById("divWhitboard").style.left = ww - 360;
document.getElementById("divWhitboard").style.zIndex = 13412;

function showDefaultConent()
{
	var editorContent = "<ww:property value='editorContent'/>";
	if( editorContent ){
		editorContent = unicode2Chr(editorContent);
		editorContent = js_beautify(editorContent, 4,'',0);
		editor.setValue(editorContent);
	}
}
window.setTimeout("showDefaultConent()",1000);
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
</script>
<script id="text-intro" type="text/jshint">
      /**
       * 这是一个JAVASCRIPT脚本编辑器
       * @create 2017-10-25
       * @desc 你可以将您的JAVA脚本拷贝到这里进行脚本语法教研,或者进行格式化让脚本可读性更强.
       */
</script>
</body>
</html>