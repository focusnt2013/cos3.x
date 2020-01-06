<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link href="images/favicon.ico" type="image/x-icon" rel="shortcut icon" />
<style type='text/css'>
body {overflow:hidden; margin-top:0px; margin-left:0px; margin-bottom:0px; margin-right:0px }
<jsp:include page='<%=Kit.getSkinCssPath("skit_frame.css", "../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_button.css", "../")%>'/>
.skit_btn390_w {
	font-size: 14pt;
	font-weight: bold;
	border:0; 
	width: 390px;
	height: 40px;
	text-decoration: none;
	text-align:center;
	background: url();
	cursor:pointer;
	background-image: url(images/button/skit_btn390_w.png);
}
.skit_btn390_r {
	font-size: 14pt;
	font-weight: bold;
	border:0; 
	width: 390px;
	height: 40px;
	text-decoration: none;
	text-align:center;
	background: url();
	cursor:pointer;
	background-image: url(images/button/skit_btn390_r.png); 
	color:#FFFFFF;
}

.skit_input300	{
	font-size: 14pt;
	background-image: url(images/button/skit_input300.png);
	width:304px;
	height:43px;
	padding-left:7px;
	padding-top:10px;
	padding-bottom:3px;
	padding-right:7px;
	color: gray;
}
.skit_dialog_cell
{
	font-size: 12pt;
	height:28px;
	word-spacing: 2px;
	padding-left: 3px;
	padding-right: 3px;
	color: white;
}
</style>
<%=Kit.getJSTag(request, "skit_button.js")%>
<script type="text/javascript">
</script>
</head>
<body><form action='register!trial.action' method='post'><table id='frameview' border='0'><tr><td>&nbsp;</td></tr></table></form></body>
<%@ include file="../skit/inc/skit_cos.inc"%>
<SCRIPT LANGUAGE="JavaScript">
var frameview = document.getElementById('frameview');
frameview.style.width = document.body.clientWidth;
frameview.style.height = document.body.clientHeight;
var row;
var cell;
var panel = document.createElement("table");
panel.width = "100%";
panel.cellspacing = 0;
panel.cellpadding = 0;
row = panel.insertRow(panel.rows.length);	
cell = row.insertCell(row.cells.length);
cell.className = "skit_dialog_cell";
cell.width = 390;
cell.innerHTML = "<table border='0'><tr>"+
	"<td style='background-image: url(images/n1/2.png);width:128px;height:128px'></td>"+
	"<td style='background-image: url(images/n1/8.png);width:128px;height:128px'></td>"+
    "</tr></table>";
cell.align = "center";

row = panel.insertRow(panel.rows.length);	
cell = row.insertCell(row.cells.length);
cell.className = "skit_dialog_cell";
cell.innerHTML = "请尽快联系您的代理商获取激活码<br/>或者拨打电话<span style='color:#FFF000;'>400-820-7575</span>联系客服";

row = panel.insertRow(panel.rows.length);	
cell = row.insertCell(row.cells.length);
cell.className = "skit_dialog_cell";
cell.align = "center";
cell.innerHTML = "<input type='text' name='activeCode' class='skit_input300' value='请输入激活码'>";

row = panel.insertRow(panel.rows.length);	
cell = row.insertCell(row.cells.length);
cell.className = "skit_dialog_cell";
cell.align = "center";
cell.innerHTML = "<button type='button' class='skit_btn390_r' onclick='active()'>激活</button>";

row = panel.insertRow(panel.rows.length);	
cell = row.insertCell(row.cells.length);
cell.className = "skit_dialog_cell";
cell.align = "center";
cell.innerHTML = "<button type='button' class='skit_btn390_w' onclick='login()'>先不激活继续试用</button>";

skit_text("您的试用期还有<span style='color:red'>28</span>天到期", panel, null);
function login()
{
	document.forms[0].submit();
}
</SCRIPT>
</html>