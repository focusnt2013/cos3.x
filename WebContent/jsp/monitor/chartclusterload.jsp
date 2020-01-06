<%@page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
</head>
<body>
<div id="tabL" style="position:relative;display:none">
	<ul id='ulTab'>
	  	<ww:iterator value="listData" status="loop">
		<li title='<ww:property value="name"/>'>
			<a href="#i-<ww:property value="id"/>"><ww:property value="ip"/>
			<ww:if test='expired'><i class='skit_fa_icon_red fa fa-warning'></i></ww:if><ww:else><i class='skit_fa_icon fa fa-info-circle'></i></ww:else>
			</a>
		</li>
  		</ww:iterator>
	</ul>
  	<ww:iterator value="listData" status="loop">
	<iframe src='monitorload!serverchart.action?id=<ww:property value="id"/>' 
		name='i-<ww:property value="id"/>'
		id='i-<ww:property value="id"/>'
		style='width:100%;border:0px;'></iframe>
	</ww:iterator>
</div>
</body>
<SCRIPT type="text/javascript">
var ifrid = "i-<ww:property value='id'/>";
function resizeWindow()
{
	var div = document.getElementById( ifrid );
	var h = document.getElementById("ulTab").clientHeight;
	if( h == 0 ) h = 34;
	div.style.height = windowHeight - h - 14;
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<SCRIPT type="text/javascript">
$('#tabL').tabs({
    select: function(event, ui) {
		ifrid = ui.panel.id;
		if( resizeWindow ) resizeWindow();
    }
});
document.getElementById( 'tabL' ).style.display = "";
</SCRIPT>
</html>