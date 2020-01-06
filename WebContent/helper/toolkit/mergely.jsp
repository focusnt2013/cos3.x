<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html lang="en">
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
	<link href="<%=Kit.URL_PATH(request)%>skit/css/font-awesome.min.css" rel="stylesheet"/>
	<!-- Requiresr jQuery -->
	<script src="skit/mergely/jquery.min.js" type="text/javascript"></script>
	<!-- Requires CodeMirror -->
	<script type="text/javascript" src="skit/mergely/codemirror.js"></script>
	<link type="text/css" rel="stylesheet" href="skit/mergely/codemirror.css" />
	<!-- Requires Mergely -->
	<script type="text/javascript" src="skit/mergely/mergely.js"></script>
	<link type="text/css" rel="stylesheet" href="skit/mergely/mergely.css" />
	<script type="text/javascript">
	var compareLeft = "<ww:property value='compareLeft'/>";
	var compareRight = "<ww:property value='compareRight'/>";
    $(document).ready(function () {
    	var editor = $('#compare');
		editor.mergely({
			width: windowWidth - 16,
			height: windowHeight - 38, // containing div must be given a height
			cmsettings: { readOnly: false },
		});
		editor.mergely('lhs', unicode2Chr(compareLeft));
		editor.mergely('rhs', unicode2Chr(compareRight));
	});
	</script>
</head>
<body>
	<table  style="width: 100%;"><tr>
		<td style="width: 50%; font-size:12px;color:gray;"><ww:property value='compareLeftTitle' escape='false'/></td>
		<td style="width: 50%; font-size:12px; padding-left: 20px; color:gray;"><ww:property value='compareRightTitle' escape='false'/></td>
	</tr></table>
	<div id="mergely-resizer">
		<div id="compare">
		</div>
	</div>
</body>
<script type="text/javascript">
function getRight(){
	var txt = $('#compare').mergely('get', "rhs");
	return txt;
}
function getLeft(){
	var txt = $('#compare').mergely('get', "lhs");
	return txt;
}
var	windowWidth = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
var	windowHeight = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
/**
 * unicode编码转中文
 */
function unicode2Chr(str){
	if (str)
	{
		var st, t, i
		st = '';
		for (i = 1; i <= str.length/4; i++){
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
</html>
