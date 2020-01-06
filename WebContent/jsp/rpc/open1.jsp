<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<link type="text/css" href="skit/css/costable.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getJSTag(request, "skit_button.js")%>
<%=Kit.getDwrJsTag(request,"interface/RpcMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
</head>
<body>
<table>
	<tr class='unline'><td style='padding-top: 3px;padding-bottom: 3px;'>
	<div class="input-group custom-search-form">
		<div class="input-group-btn">
            <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false"
            	style='display: inline-block;height:28px;' id='btnAddr'><span class="caret"></span></button>
		    <ul class="dropdown-menu pull-left" style='width:300px' id='ulExecuteMemory'>
		        <li id='liDefault'><a onclick='selectExecute(this)' style='font-size:12px;'>清空指令输入</a></li>
		    </ul>
		</div>
		<span class="input-group-addon" style='font-size:12px;border-right:0px solid red;'><i class="fa fa-terminal"></i></span>
	    <input class="form-control" placeholder="请输入向远端伺服器执行的指令"
	    	 onkeyup='return go(this);' autocomplete='on'
	    	 id='indication' name='indication' type="text"
			 style='background-color: #000000;color:#FFFFFF;font-size: 12px;height:28px;padding-top:3px;padding-bottom:3px;'>
	    <span class="input-group-btn">
	        <button class="btn btn-inverse" type="button" onclick='execute()'>
	            <i class="fa fa-play-circle"></i>
	        </button>
	    </span>
	</div>
	<tr><td>
	<div id='ack' style='border:1px solid #7F9DB9;background-color: #000000;color:#FFFFFF;font-size: 11pt;'>
	<table>
	<tr class='unline'><td id='ack0' style='background-color: #000000;color:#FFFFFF;font-size: 11pt;'></td></tr>
	<tr class='unline'><td id='ack1' style='background-color: #000000;color:#FFFFFF;font-size: 11pt;'></td></tr>
	</table>
	</div></td></tr>
</table>
</body>
<script type="text/javascript">
function resizeWindow()
{
	var div = document.getElementById( 'ack' );
	div.style.height = windowHeight - 38;
}
</script>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<script type="text/javascript">
$( '#ack' ).niceScroll({
	cursorcolor: '<%=(String)session.getAttribute("System-Theme")%>',
	railalign: 'right',
	cursorborder: "none",
	horizrailenabled: true, 
	zindex: 2001,
	left: '245px',
	cursoropacitymax: 0.6,
	cursorborderradius: "0px",
	spacebarenabled: false 
});

var ip = "<ww:property value='ip'/>";
var port = <ww:property value='port'/>;

try
{
	var str = getUserActionMemory("rpc!open.execute-<ww:property value='id'/>");
	var ul = document.getElementById( 'ulExecuteMemory' );
	if( str && str != "" )
	{
		var executeMemory = jQuery.parseJSON(str);
		var liDefault = document.getElementById( 'liDefault' );
		for(var i = 0; i< executeMemory.length; i++ )
		{
			var li = document.createElement("li");
			li.innerHTML = "<a onclick='selectExecute(this)' style='font-size:12px;' class='liaExecute'>"+executeMemory[i]+"</a>";
			ul.insertBefore(li, liDefault);
		}
	}
	ul.style.width = windowWidth - 50;
}
catch(e)
{
}

document.onkeydown=function(event) 
{ 
	var e = event ? event :(window.event ? window.event : null); 
	if(e.keyCode==13){ 
		//执行的方法 
        execute();
	} 
}

function selectExecute(a)
{
	var indication = a.innerHTML;
	if( indication == "清空指令输入" ){
		document.getElementById("indication").value = "";
		return;
	}
	document.getElementById("indication").value = indication;
	execute();
}

function saveExecuteMemory(indication)
{
	var args = $(".liaExecute");
	var ul = document.getElementById( 'ulExecuteMemory' );
	for(var i = 0; i< args.length; i++ )
	{
		var indication = args[i].innerHTML;
		if( indication == indication ) return;
	}
	if( args.length >= 10 )
	{
		var liDefault = document.getElementById( 'liDefault' );
		var lastChild = liDefault.previousSibling;//ul.childNodes[ul.childNodes.length-2];
		//alert("lastChild="+(lastChild?lastChild.innerHTML:"null"));
		ul.removeChild(lastChild);
	}
	
	var li = document.createElement("li");li.innerHTML = "<a onclick='selectExecute(this)' style='font-size:12px;' class='liaExecute'>"+dir+"</a>";
	ul.insertBefore(li, ul.firstChild);
	
	args = $(".liaExecute");
	var executeMemory = new Array();
	for(var i = 0; i< args.length; i++ )
	{
		executeMemory.push(args[i].innerHTML);
	}
	var value = JSON.stringify(executeMemory);
	//alert("saveExecuteMemory:"+value);
	setUserActionMemory("rpc!open.execute-<ww:property value='id'/>", value);
}

var mapCommands = new Object();
var memoryCommands = new Array();
var viewLock = false;//当正在执行指令的时候锁定屏幕
function execute()
{
	var indication = document.getElementById("indication").value;
	if( indication == "clear" )
	{
		var ack = document.getElementById("ack");
		ack.innerHTML = "";
		return;
	}
	viewLock = true;
	RpcMgr.sendSsh(ip,port,indication, {
		callback:function(result) {
			if( result != "" )
			{
				skit_alert(result)
			}
			else
			{
				if( mapCommands[indication] == "1" || indication == "" )
				{
				}
				else
				{
					mapCommands[indication] = "1";
					memoryCommands[memoryCommands.length] = [indication,indication];
					skit_new_select(memoryCommands, "divMemory", document.getElementById("indication").clientWidth);
				}
				saveExecuteMemory(indication);
				document.getElementById("indication").value = "";
			}
			viewLock = false;
		},
		timeout:120000,
		errorHandler:function(message) {skit_alert(message);viewLock=false;}
	});
}
//刷屏
var offset = 0;//刷屏的数据偏移量
function refresh()
{
	if( viewLock )
	{
		window.setTimeout("refresh()",1000);
		return;
	}
	//parent.setViewTitle(offset);
	var indication = document.getElementById("indication").value;
	RpcMgr.getSshResponse(ip, port, offset, {
		callback:function(response) {
			var ack = document.getElementById("ack");
			if( response && response.offset > offset )
			{
				var html = document.getElementById("ack0").innerHTML;
				for( var i in response.messages )
				{
					var line = response.messages[i];
					if( line.indexOf("@<ww:property value='host'/>") > 0 )
					{
						line = "<span style='color:red;font-weight:bold'>"+line+"</span>";
					}
					html += line;
					html += "<br/>";
				}
				document.getElementById("ack0").innerHTML = html;
				if( response.esc )
				{
					document.getElementById("ack1").innerHTML = "<span style='color:red;font-weight:bold'>"+response.user+"@"+response.host+"# _"+"</span><br/><br/>"; 
				}
				else if( response.lastLine )
				{
					document.getElementById("ack1").innerHTML = response.lastLine+"<br/><br/>";
				}
				else
				{
					document.getElementById("ack1").innerHTML = "&nbsp;";
				}
				//执行一次指令后，将滚动条跳到最后
				ack.scrollTop = ack.scrollHeight;
				offset = response.offset;
			}
			window.setTimeout("refresh()",1000);
		},
		timeout:30000,
		errorHandler:function(message) {alert(message);window.setTimeout("refresh()",3000);}
	});
}
refresh();
</script>
</html>