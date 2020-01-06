<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<style type='text/css'>
<jsp:include page='<%=Kit.getSkinCssPath("skit_view.css", "../../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_table.css", "../../")%>'/>
<jsp:include page='<%=Kit.getSkinCssPath("skit_button.css", "../../")%>'/>
</style>
<%=Kit.getJSTag(request, "skit_button.js")%>
<%=Kit.getDwrJsTag(request,"interface/OmtMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body>
<Table width='100%' cellspacing='0' cellpadding='0' id='tableFeview'>
  <TR style='height:3px'><TD/></TR>
  <TR><TD>
	<table width='100%' cellspacing=1 cellpadding=0 class='skit_table_frame' id='tbFrame1'>
		<tr><td>
		<table width='100%' cellspacing=0 cellpadding=0><tr>
			<td width='120'><select name='host' id='host' class='skit_select_option' onchange='visitPath(this);'>
			<ww:iterator value="listData" status="loop">
			<option value='<ww:property value="toString()"/>'
			><ww:property value="toString()"/></option>
			</ww:iterator>
			</select></td>
			<td><input type='text' id='indication' name='indication' class='skit_input_text'
			onkeydown="return ssh(this);"
			onclick="skit_open_select(this,'divMemory')"
			onkeyup="return skit_keyup_select(this, 'divMemory', '');"
			></td></tr></table>
		</td></tr>
		<tr><td>
		<div id='ack' valign='top' class='skit_view_div' style='border:solid #7F9DB9 1px;background-color: #000000;color:#FFFFFF;font-size: 11pt;'>
		</div></td></tr>
	</table>
	</TD>
  </TR>
</Table>
</body>
<script type="text/javascript" LANGUAGE="JavaScript">
function resizeWindow()
{
	var feview = window.document.body;
	var tableFeview = document.getElementById( "tableFeview" );
	var tbFrame1 = document.getElementById( "tbFrame1" ); 
	var div;
	var h = 0;
	var h1 = 0;
	var h2 = 0;
	
	h += tableFeview.rows[0].clientHeight;
	h = feview.clientHeight - h;
	//发布参数设置面板位置自适应
	div = document.getElementById( 'ack' );
	div.style.height = 50;
	h2 = tbFrame1.clientHeight;
	h2 -= div.clientHeight;
	h2 = h - h2;
	div.style.height = h2 - 2;
}
resizeWindow();

function ssh()
{
    var e = event.srcElement;
    var k = event.keyCode;
    if( k == 13 )
    {
    	execute();
    }
}
var mapCommands = new Object();
var memoryCommands = new Array();
function execute()
{
	var host = document.getElementById("host").value;
	host = "";
	var indication = document.getElementById("indication").value;
	OmtMgr.findJars(host,indication, {
		callback:function(response) {
			var ack = document.getElementById("ack");
			if( response && response.length > 0 )
			{
				var html = "";
				for( var i in response )
				{
					var line = response[i];
					if( i == 0 )
						html += "<span style='color:red;font-weight:bold'>"+line+"</span>";
					else
						html += line;
					html += "<br/>";
				}
				html += "<span style='color:red;font-weight:bold'>完成搜索。</span>";;
				document.getElementById("ack").innerHTML = html;
			}
			else
			{
				document.getElementById("ack").innerHTML = "<span style='color:red;font-weight:bold'>没有找到类"+indication+"所在的jar包。</span>";;
			}
		},
		timeout:120000,
		errorHandler:function(message) {skit_alert(message);}
	});
}
</script>
<%@ include file="../../skit/inc/skit_cos.inc"%>
</html>