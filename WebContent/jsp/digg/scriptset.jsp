<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
    <link rel=stylesheet href="jsp/digg/scriptset.css">
    <script src="jsp/digg/jsformat.js"></script>
    <script src="jsp/digg/scriptset.js"></script>
<style>
.CodeMirror{position:absolute;left:20px;top:20px;font-size:88%;background:#1d1e1c;font-family:monaco,monospace;height:auto;width:<ww:property value='ww'/>px;}    
</style>
<SCRIPT type="text/javascript">
function beautifyScript()
{
	var txt = editor.getValue();
	txt = js_beautify(txt,4,'',0);
	return editor.setValue(txt);
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

function getScriptset()
{
	return parent.getScriptset();
}

function checkModify()
{
	var script = editor.getValue();
	//alert("checkModify:"+_script);
	if( _script && _script != script )
	{
		parent.changeScriptset(script);
		_script = script;
	}
	window.setTimeout("checkModify()",3000);
}
</SCRIPT>
</head>
<body onload="main()" style='overflow-x: hidden'>
<div class="whiteboard" id='divWhitboard'>
    <div class="toolbar">
      <button data-type="toggle" data-target="configure,output">Configure</button>
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
document.getElementById("divWhitboard").style.left = ww - 360;
document.getElementById("divWhitboard").style.zIndex = 13412;
window.setTimeout("getScriptset()",1000);
window.setTimeout("checkModify()",3000);
</script>
<script id="text-intro" type="text/jshint">
</script>
</body>
</html>