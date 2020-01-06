<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link href="skin/defone/css/bootstrap.css" rel="stylesheet">
<link href="skin/defone/css/font-awesome.min.css" rel="stylesheet">
<link href="skin/defone/css/themify-icons.css" rel="stylesheet">
<link href="skin/defone/css/ionicons.min.css" rel="stylesheet">
<link type="text/css" href="skit/css/bootstrap.css" rel="stylesheet">
<style type='text/css'>
.msgr-name {
	font-size:14px;
	font-weight:bold;
	width:258px;
	font-family:微软雅黑,Roboto,sans-serif;
	white-space:nowrap;
	overflow:hidden;
	text-overflow:ellipsis;
	word-break:keep-all;
	display:block;
	padding-left:3px;
}
.msgr-summary {
	font-size:11px;
	width:258px;
	color:#a0a0a0;
	font-family:微软雅黑,Roboto,sans-serif;
	white-space:nowrap;
	overflow:hidden;
	text-overflow:ellipsis;
	word-break:keep-all;
	display:block;
	padding-left:3px;
}
.msgr-time {
	font-size: 10px;
	color: #a0a0a0;
	font-weight: 400;
}
.skit_table_cell
{
	padding-top: 13px;
	padding-bottom: 13px;
	height:23px;
    BORDER-BOTTOM: 1px solid #e6e6e6;
    BORDER-LEFT: 0px solid #FFF;
	word-spacing: 2px;
	font-size:12px;
	padding-left: 3px;
	padding-right: 3px;
}
.notify_title {
	font-size:20px;
	font-weight:bold;
	font-family:微软雅黑,Roboto,sans-serif;
	padding-top: 10px;
	padding-left:10px;
}
.notify_time {
	font-size:11px;
	width:268px;
	color:#a0a0a0;
	font-family:微软雅黑,Roboto,sans-serif;
	padding-left:10px;
}
.notify_tips {
	font-size:9pt;
	color:#a0a0a0;
	font-family:微软雅黑,Roboto,sans-serif;
	margin-left:10px;
	margin-right:10px;
	border:1px dashed #e6e6e6;
	padding-top:5px;
	padding-left:10px;
	padding-bottom:5px;
}

.notify_context {
	font-size:11px;
	word-break:normal;
	white-space:pre;
	font-size:9pt;
	color:#fff;
	background:#000;
	margin-left: 10px;
	padding-top: 5px;
	padding-bottom:5px;
	padding-left:10px;
	padding-right:10px;
	border:1px dashed #e6e6e6; 
    tab-size:4;
    -moz-tab-size:4;  
    -o-tab-size:4; 
}

.notify_context_img {
	border:1px dashed #e6e6e6; 
	width: 256px;
	padding-left:0px;
	margin-right:10px;
}

.badge.badge-notification {
    position:absolute;
    top: 9px;
    left: 7px;
    z-index: 1000;
    padding: 6px 6px;
    font-size: 14px;
    width:28px;
    height:26px;
}

.badge.badge-info {
    background-color: #f9bf3b;
}
.badge {
    -webkit-border-radius: 3px;
    -moz-border-radius: 3px;
    border-radius: 3px;
    font-size: 11px;
    font-family: Lato,sans-serif;
    font-weight: 300;
}
.fadeIn {
    -webkit-animation-name: fadeIn;
    -moz-animation-name: fadeIn;
    -o-animation-name: fadeIn;
    animation-name: fadeIn;
}
.animated {
    -webkit-animation-duration: 1s;
    -moz-animation-duration: 1s;
    -o-animation-duration: 1s;
    animation-duration: 1s;
    -webkit-animation-fill-mode: both;
    -moz-animation-fill-mode: both;
    -o-animation-fill-mode: both;
    animation-fill-mode: both;
}
.btn.btn-outline.btn-primary:hover {
    background-color: #fff;
    border: 1px solid #455a64;
    color: #455a64;
}.btn.btn-primary:hover {
    background-color: #fff;
}
.btn.btn-primary {
    background-color: #455a64;
    border-color: #455a64;
}
.btn {
    -webkit-border-radius: 20px;
    -moz-border-radius: 20px;
    border-radius: 20px;
    border: none;
    outline: 0 !important;
}
.btn, a {
    -webkit-transition: all .25s ease-out;
    -moz-transition: all .25s ease-out;
    -o-transition: all .25s ease-out;
    transition: all .25s ease-out;
}

.alert {
    position:absolute;
    color:#b51a29;
    top: 3px;
    left: 3px;
    z-index: 10000;
    padding: 15px;
	margin-bottom: 0px;
	width:327px;
	border: 1px solid transparent;
	-webkit-border-radius: 0;
	-moz-border-radius: 0;
	border-radius: 0;
	opacity:0.8;
}
.skit_table_row_selected
{
	background-color: #e3f1f4
}
</style>
<%=Kit.getJSTag(request, "skit_table.js")%>
<%=Kit.getJSTag(request, "skit_button.js")%>
<%=Kit.getDwrJsTag(request,"interface/DescktopMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<SCRIPT type="text/javascript">
var state = -1;
var state_li;
var filter = "";
var filter_li;
function setFilter(li, f)
{
	if( filter_li != li )
	{
		if( filter_li ) filter_li.className = "";
		if( filter_li ) filter_li.style.backgroundColor = "";
		filter_li = li;
		if( f != "" ) filter_li.className = "active";
		if( f != "" ) filter_li.style.backgroundColor = "#86c0eb";
		filter = f;
		clearNotifies();
		handleNotifies(true);
	}
}

function setState(li, s)
{
	if( state_li != li )
	{
		if( state_li ) state_li.className = "";
		if( state_li ) state_li.style.backgroundColor = "";
		state_li = li;
		if( s != -1 ) state_li.className = "#86c0eb";
		if( s != "" ) state_li.style.backgroundColor = "#86c0eb";
		state = s;
		clearNotifies();
		handleNotifies(true);
	}
}

function clearNotifies()
{
	var table = document.getElementById("tableFilters");
	var length = table.rows.length;
	for(var i = 0; i < length; i+=1 )
	{
		table.deleteRow(0);
	}
}

function handleNotifies(order)
{
	if( dataAppending )
	{
		top.skit_alert("正在加载数据，不能执行重复操作。");
		return;
	}
	dataAppending = true;
	var timestamp = 0;
	var table = document.getElementById( 'tableFilters' );
	if( order )
	{
		if( table.rows.length > 0 ) timestamp = table.rows[0].cells[2].innerHTML;
		var tr = table.insertRow(0);
		tr.id = "trSpinner";
		var td = tr.insertCell(0);
		td.className = "skit_table_cell_end";
		td.colSpan = 2;
		td.align = "center";
		td.style = "color:#86c0eb;font-size:14px;padding-top:10px;padding-bottom:10px;";
		td.innerHTML = "<i class='fa fa-spinner fa-spin'></i><br/>加载数据中("+timestamp+")……";
		divFilters.scrollTop = 0+"px";
	}
	else
	{
		if( table.rows.length > 0 ) timestamp = table.rows[table.rows.length-1].cells[2].innerHTML;
		var tr = table.insertRow(table.rows.length);
		tr.id = "trSpinner";
		var td = tr.insertCell(0);
		td.className = "skit_table_cell_end";
		td.colSpan = 2;
		td.align = "center";
		td.style = "color:#86c0eb;font-size:14px;padding-top:10px;padding-bottom:10px;";
		td.innerHTML = "<i class='fa fa-spinner fa-spin'></i><br/>加载数据中("+timestamp+")……";
		divFilters.scrollTop = divFilters.scrollHeight;	
	}
	var keywords = document.getElementById("keywords").value;
	//alert(timestamp+", "+ order+", "+state+", ‘"+filter+"’, ‘"+keywords+"’");
	DescktopMgr.getNotifies(timestamp, order, state, filter, keywords, {
		callback:function(response){
			if(!response)
			{
				top.skit_alert("获取数据失败");
				return;
			}
			try
			{
				var notifies = response.notifies;
				var table = document.getElementById("tableFilters");
				var spinner = document.getElementById("trSpinner");
				if( spinner )
				{
					table.deleteRow(spinner.rowIndex);
				}
				showHandleTips("共获取到"+notifies.length+"条系统消息。", "alert alert-success");
				var j = order?0:table.rows.length;
				var autoView = table.rows.length==0&&notifies.length>0;
				var firstTr;
				for(var i = 0; i < notifies.length; i+=1 )
				{
					var n = notifies[i];
    				var tr = document.getElementById(n.nid);
    				if( tr )
    				{
    					table.deleteRow(tr.rowIndex);
    				}
                    createMessage(n, table, j++);
				}
				document.getElementById("badge-notify").innerHTML = response.notifyTips;
				if( autoView ){
					viewNotify(table.rows[0].id);
				}
				else if( firstTr ){
					//alert("firstTr="+firstTr.scrollTop)
					firstTr.style.backgroundColor = "#fffdf2";
					divFilters.scrollTop = divFilters.scrollTop + 64;
					firstTr.style.borderTop = "2px solid #b51a29";
				}
				var skitViewDiv = $( '.skit_view_div' );
				if( skitViewDiv )
					skitViewDiv.niceScroll({
						cursorcolor: '#444',
						railalign: 'right',
						cursorborder: "none", 
						horizrailenabled: false, 
						zindex: 2001, 
						left: '0px', 
						cursoropacitymax: 0.6, 
						cursorborderradius: "0px", 
						spacebarenabled: false });
				//1)cursorcolor - 设置滚动条颜色，默认值是“＃000000”
				//2)cursoropacitymin - 滚动条透明度最小值
				//3)cursoropacitymax - 滚动条透明度最大值
				//4)cursorwidth - 滚动条的宽度像素，默认为5（你可以写“5PX”）
				//5)cursorborder - CSS定义边框，默认为“1px solid #FFF”
				//6)cursorborderradius - 滚动条的边框圆角
				//7)ZIndex的 - 改变滚动条的DIV的z-index值，默认值是9999
				//8)scrollspeed - 滚动速度，默认值是60
				//9)mousescrollstep - 滚动鼠标滚轮的速度，默认值是40（像素）
				//10)touchbehavior - 让滚动条能拖动滚动触摸设备默认为false
				//11)hwacceleration - 使用硬件加速滚动支持时，默认为true
				//12)boxzoom - 使变焦框的内容，默认为false
				//13)dblclickzoom - （仅当boxzoom = TRUE）变焦启动时，双点击框，默认为true
				//14)gesturezoom - boxzoom = true并使用触摸设备）变焦（仅当激活时，间距/盒，默认为true
				//15)grabcursorenabled“抢”图标，显示div的touchbehavior = true时，默认值是true
				//16)autohidemode，如何隐藏滚动条的作品，真正的默认/“光标”=只光标隐藏/ FALSE =不隐藏的背景下，改变铁路背景的CSS，默认值为“”
				//17)iframeautoresize中，AUTORESIZE iframe上的load事件（默认：true）
				//18)cursorminheight，设置最低滚动条高度（默认值：20）
				//19)preservenativescrolling，您可以用鼠标滚动本地滚动的区域，鼓泡鼠标滚轮事件（默认：true）
				//20)railoffset，您可以添加抵消顶部/左轨位置（默认：false）
				//21)bouncescroll，使滚动反弹结束时的内容移动（仅硬件ACCELL）（默认：FALSE）
				//22)spacebarenabled，允许使用空格键滚动（默认：true）
				//23)railpadding，设置间距（默认：顶：0，右：0，左：0，底部：0}）
				//24)disableoutline，Chrome浏览器，禁用纲要（橙色hightlight）时，选择一个div nicescroll（默认：true）
				//alert("hieght："+document.getElementById( 'divFilters' ).scrollHeight);
			}
			catch(e)
			{
				showHandleTips("刷新数据出现异常"+e, "alert alert-danger");
			}
			dataAppending = false;
			setNotifyTips(response.notifyTips);
			//window.setTimeout("handleNotifies()",30000);//30秒执行一次系统消息检查
		},
		timeout:30000,
		errorHandler:function(message) {
			dataAppending = false;
			showHandleTips("获取数据出现异常"+message, "alert alert-danger");
		}
	});	
}

function setNotifyTips(count)
{
	if( top && top.setNotifyTips )
	{
		top.setNotifyTips(count);
	}
}

function showHandleTips(tips, classname)
{
	document.getElementById("handleTips").className = classname;
	document.getElementById("handleTips").style.display = "";
	document.getElementById("handleTips").innerHTML = tips;
	window.setTimeout("hiddenHandleTips()",2000);
}

function hiddenHandleTips()
{
	document.getElementById("handleTips").style.display = "none";
}

function createMessage(n, table, j)
{
	var tr = table.insertRow(j++);
	tr.onmouseover = new Function("this.style.backgroundColor='#e3f1f4';");
	tr.onmouseout = new Function("this.style.backgroundColor='';");
	tr.id = n.nid;
	var icon = "<i style='font-size:10px;color:#fbc728;' class='fa fa-envelope'></i>";
	var fontColor = "color:#86c0eb";
	if( n.read )
	{
		icon = "<i style='font-size:10px;color:#a0a0a0;' class='fa fa-envelope-o'></i>";
		fontColor = "color:#c5c5c5";
	}
	var td = tr.insertCell(0);
	td.className = "skit_table_cell";
	td.innerHTML = "<span class='msgr-name' style='"+fontColor+"'>"+n.title+"</span><span class='msgr-summary'>"+n.filter+"</span>";
	
	td = tr.insertCell(1);
	td.align = "center";
	td.className = "skit_table_cell";
	td.style.width = "60px";
	td.innerHTML = "<span class='msgr-time' title='"+n.time+"'>"+n.prettytime+"</span><br/>"+icon;
	
	td = tr.insertCell(2);
	td.style.display = "none";
	td.innerHTML = n.timestamp;
	return tr;
}

var curNotify;
function viewNotify(id, trClicked)
{
	DescktopMgr.setSysnotifyState(id, 1,{
		callback:function(result){
			//top.showJson(result);
			if( result )
			{
				try
				{
					var tr = document.getElementById(id);
					if( !tr )
					{
						var table = document.getElementById("tableFilters");
						tr = createMessage(result, table, 0);
					}
					tr.className = "skit_table_row_selected";
					//if( trNotify ) trNotify.className = "";
					trNotify = tr; 
					document.getElementById("notifyFilter").innerHTML = "<i class='skit_fa_btn fa fa-envelope'></i> "+result.filter;
					document.getElementById("notifyTitle").innerHTML = result.title;
					document.getElementById("notifyTime").innerHTML = "<i class='fa fa-clock-o fa-fw' style='font-size:10px;'></i>"+result.time;
					document.getElementById("notifyTips").innerHTML = "<ww:property value='account'/>，你好。这是一条系统消息，它可能是工单系统发来提醒你做业务处理的通知。请按照该消息的提示完成相关的业务操作。";
					var icon = "<i style='font-size:10px;color:#a0a0a0;' class='fa fa-envelope-o'></i>";
					tr.cells[0].innerHTML = "<span class='msgr-name' style='color:#a0a0a0'>"+result.title+"</span><span class='msgr-summary'>"+result.filter+"</span>";
					tr.cells[1].innerHTML = "<span class='msgr-time' title='"+result.time+"'>"+result.prettytime+"</span><br/>"+icon;
					var notifyContext = document.getElementById( 'notifyContext' );
					//alert(document.getElementById( 'trNotifyContext' ));
					if( result.context ){
						notifyContext.innerHTML = result.context;
						document.getElementById( 'trNotifyContext' ).style.display = "";
					}
					else{
						document.getElementById( 'trNotifyContext' ).style.display = "none";
					}
					var notifyContextlink = document.getElementById( 'notifyContextlink' );
					var	windowWidth = window.document.body.clientWidth || window.innerWidth || document.documentElement.clientWidth; 
					//var	windowHeight = window.document.body.clientHeight || window.innerHeight || document.documentElement.clientHeight;
					if( result.contextimg )
					{
						document.getElementById("notifyContextImg").parentNode.style.display = "";
						document.getElementById("notifyContextImg").src = result.contextimg;
						document.getElementById( 'notifyContext' ).style.width = windowWidth - 333 - 276 - 10;
					}
					else
					{
						document.getElementById("notifyContextImg").parentNode.style.display = "none";
						document.getElementById( 'notifyContext' ).style.width = windowWidth - 333 - 25;
					}
					var btnAction = document.getElementById( 'btnAction' );
					if( result.actionlink )
					{
						btnAction.style.display = "";
						btnAction.title = result.actionlink;
						btnAction.innerHTML = result.action;
					}
					else
					{
						btnAction.style.display = "none";
					}
					if( result.contextlink )
					{
						notifyContextlink.src = result.contextlink;
						notifyContextlink.parentNode.style.display = "";
						var w = document.getElementById( 'divContext' ).clientWidth - 24;
						var h = document.getElementById( 'divContent' ).clientHeight;
						h -= document.getElementById( 'notifyTitle' ).clientHeight;
						h -= document.getElementById( 'notifyTime' ).clientHeight;
						h -= document.getElementById( 'notifyTips' ).clientHeight;
						h -= document.getElementById( 'tbFrame2' ).rows[0].clientHeight;
						h -= document.getElementById( 'tbFrame2' ).rows[2].clientHeight;
						h -= 60;
						notifyContextlink.style.height = h;
						notifyContextlink.style.width = w;
					}
					else
					{
						notifyContextlink.parentNode.style.display = "none";
					}
					
					if( result.same )
					{
						notifyContext.style.display = "none";
					}
					else
					{
						notifyContext.style.display = "";
					}
					if( top && top.closeNotify ) top.closeNotify(result.nid);
					if( !trClicked ){
						var top1 = skit_getTop(tr) - tr.clientHeight;
						divFilters.scrollTop = top1;
					}
					curNotify = result;
				}
				catch(e)
				{
					skit_alert("查看系统消息通知异常"+e.message+", 行数"+e.lineNumber);
				}
			}
		},
		timeout:10000,
		errorHandler:function(message) {
			alert(message);
		}
	});
}
var trNotify;
function selectNotifies(table)
{
	selectRowEx(table, function(tr){
		viewNotify(tr.id, tr);
	});
}

function openAction(btn)
{
	if( btn.title.indexOf("#feedback") != -1 )
	{
		var to = null;
		var tips = '向【系统管理员组】'+curNotify.action;
		if( btn.title.indexOf("?to=") != -1 )
		{
			to = btn.title.substring("#feedback?to=".length); 
			tips = "向用户【"+to+"】"+curNotify.action;
		}
		top.skit_text(tips,'',function(yes, val){
			if( yes ){
				top.sendSystemNotify(to, curNotify.filter,
					"<ww:property value='account'/>: "+val,
					"<ww:property value='account'/>向您发送了一条系统消息:\r\n"+val);
			}
		}, "请输入您的问题，不允许为空。");		
	}
	else
	{
		top.openView(btn.innerHTML, btn.title);
	}
}
</SCRIPT>
</head>
<body style='margin-top:3px;margin-left:1px;margin-right:1px;margin-bottom:1px;'>
<span class="badge badge-notification badge-info animated fadeIn" id="badge-notify" style='top:6px;border-radius:5px;'>0</span>
<div id="handleTips" style='display:none;'></div>
<TABLE style='width:100%'>
<TR class='unline'><TD width='330' valign='top'>
        <div class="panel panel-default">
   			<div class="panel-body" style='padding: 0px;'>
	        	<div class="input-group">
	                <input id='keywords' class="form-control"
						style='padding-left:36px;height:30px;' placeholder="输入关键字搜索" type="text">
	                <div class="input-group-btn">
	                    <button type="button" class="btn btn-info dropdown-toggle"
	                    	style="height:30px"
	                    	data-toggle="dropdown"
	                    	aria-expanded="false">过滤<span class="caret"></span></button>
	                    <ul class="dropdown-menu pull-right">
							<ww:iterator value="listData" status="loop">
	                        <li onclick='setFilter(this, "<ww:property value='name'/>");'><a href="#"><ww:property value='name'/></a></li>
	                		</ww:iterator>
	                        <li style='background-color:#fffdf2' onclick='setFilter(this, "");'><a href="#" style='font-weight:bold'><i class='skit_fa_icon fa fa-bookmark-o'></i> 全部</a></li>
	                        <li class="divider"></li>
	                        <li id='liUnread' onclick='setState(this, 0);'><a href="#"><i class='skit_fa_icon_yellow fa fa-envelope'></i> 未读</a></li>
	                        <li id='liRead' onclick='setState(this, 1);'><a href="#" onclick='setState(this, 1);'><i class='skit_fa_icon fa fa-envelope-o' style='color:#ccc'></i> 已读</a></li>
	                        <li id='liAllread' onclick='setState(this, 2);' style='background-color:#fffdf2'><a href="#" style='font-weight:bold'><i class='skit_fa_icon fa fa-laptop'></i> 全部</a></li>
	                    </ul>
	                </div>
	            </div>
                <div class="skit_view_div" id='divFilters'>
                	<table id='tableFilters' style='width:100%;cursor:pointer;' onClick='selectNotifies(this)'>
                    </table>
                </div>
   			</div>
   		</div>
	</TD>
	<TD style='padding-left:3px;' valign='top'>
        <div class="panel panel-default" id='divContext'>
   			<div class="panel-heading" style='font-size:12px;padding:5px 15px'><span id='notifyFilter'></span></div>
   			<div class="panel-body" style='padding: 3px;'>
                <div id='divContent' class='skit_view_div'>
	                <p class='notify_title' id='notifyTitle'></p>
	                <p class='notify_time' id='notifyTime'></p>
	                <p class='notify_tips' id='notifyTips'></p>
                	<table style='width:100%'>
                	<tr><td valign='top' style='padding-bottom:10px;'>
	                		<table style='width:100%' id='tbFrame2'>
	                		<tr id='trNotifyContext'><td><div class='notify_context' id='notifyContext'></div></td></tr>
	                		<tr><td style='padding-left:10px;padding-top:10px;'>
	                			<iframe id='notifyContextlink' name='notifyContextlink' class='nonicescroll'
	                				style='border:0px solid red;'></iframe></td></tr>
	                		<tr><td style='padding-left:10px;padding-top:10px;'><button type="button" class="btn btn-primary btn-outline" onclick='openAction(this)' id='btnAction' style='display:none;'></button></td></tr>
							</table>
                		</td>
                		<td width='276' valign='top' align='right'><img class='notify_context_img' id='notifyContextImg'></td></tr>
                    </table>
                </div>
   			</div>
   		</div>
	</TD>
</TR>
</TABLE>
<div style='position:absolute;top:28px;left:28px;' id='toolbar'>
   <button type="button" class="btn btn-warning btn-circle" onclick="top.reloadView();" title='重加载新的系统消息'
   	style='opacity:0.5;'><i class="fa fa-repeat fa-spin"></i></button><br/><br/>
   <button type="button" class="btn btn-success btn-circle" onclick="scroll2(this);" title='滚动到底部'
   	style='opacity:0.5;'><i class='fa fa-angle-double-down'></i></button>
</div>
</body>
<SCRIPT type="text/javascript">
/*实现窗口对齐*/
function resizeWindow()
{
//	var	windowWidth = window.document.body.clientWidth || window.innerWidth || document.documentElement.clientWidth; 
//	var	windowHeight = window.document.body.clientHeight || window.innerHeight || document.documentElement.clientHeight;
	var div;
	//发布内容列表位置自适应
	div = document.getElementById( 'divFilters' );
	div.style.height = windowHeight - 40;

	div = document.getElementById( 'divContent' );
	div.style.height = windowHeight - 48;
	//alert("resizeWindow:"+windowWidth+"x"+windowHeight);

	var toolbar = document.getElementById( 'toolbar' );
	toolbar.style.top = windowHeight - toolbar.clientHeight - 10;
	toolbar.style.left = 3;

	div = document.getElementById( 'notifyContext' );
	div.style.width = windowWidth - 333 - 276 - 10;

	var notifyContextlink = document.getElementById( 'notifyContextlink' );
	if( !notifyContextlink.parentNode.style.display )
	{
		var w = div.clientWidth;
		var h = document.getElementById( 'divContent' ).clientHeight;
		h -= document.getElementById( 'notifyTitle' ).clientHeight;
		h -= document.getElementById( 'notifyTime' ).clientHeight;
		h -= document.getElementById( 'notifyTips' ).clientHeight;
		h -= document.getElementById( 'tbFrame2' ).rows[0].clientHeight;
		h -= document.getElementById( 'tbFrame2' ).rows[2].clientHeight;
		h -= 60;
		notifyContextlink.style.height = h;
		notifyContextlink.style.width = w;
	}
	else
	{
		notifyContextlink.parentNode.style.display = "none";
	}
}

function openNicescroll()
{
	var iframe = $("#notifyContextlink");
	if( iframe )
	{
		$( '#notifyContextlink' ).niceScroll({
			cursorcolor: "#e3f1f4",
			railalign: 'right',
			cursorborder: "none", 
			horizrailenabled: false, 
			zindex: 2001, 
			left: '0px', 
			cursoropacitymax: 0.6, 
			cursorborderradius: "0px", 
			spacebarenabled: false });
	}
}
</SCRIPT>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<script src="skin/defone/js/bootstrap.js"></script>
<script src="skin/defone/js/jquery.nicescroll.min.js"></script>
<SCRIPT type="text/javascript">
function scroll2(btn)
{
	if( btn.innerHTML.indexOf("up") != -1 )
	{
		divFilters.scrollTop = 0+"px";
		btn.innerHTML = "<i class='fa fa-angle-double-down'></i>";
		btn.title = "滚动到顶部";
	}
	else
	{
		divFilters.scrollTop = divFilters.scrollHeight;	
		btn.title = "滚动到底部";
		btn.innerHTML = "<i class='fa fa-angle-double-up'></i>";
	}
}
var dataAppending = false;
var scrollPosition = -1;
var divFilters = document.getElementById( 'divFilters' );
$( '#divFilters' ).on('scroll', function(){
	if( dataAppending ) return;
	//alert(divFilters.scrollTop+"/"+divFilters.scrollHeight);
	var _scroll = divFilters.scrollTop;
	if( _scroll == 0 && scrollPosition != _scroll )
	{
		scrollPosition = 0;
		handleNotifies(true);
		return;
	}
	var _top = divFilters.scrollHeight - _scroll;
	//alert(_top +"/"+ divFilters.clientHeight);
	if( _top <= divFilters.clientHeight && scrollPosition != _scroll )
	{
		handleNotifies(false);
	}
	scrollPosition = _scroll;
});
handleNotifies(true);
document.onkeydown=function(event) 
{ 
	var e = event ? event :(window.event ? window.event : null); 
	if(e.keyCode==13){
		clearNotifies();
		handleNotifies(true);
	} 
}
</SCRIPT>
</html>