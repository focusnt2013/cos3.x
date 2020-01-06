<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%> 
<link href="<%=Kit.URL_PATH(request)%>skit/css/bootstrap.min.css" rel="stylesheet">
<link href="<%=Kit.URL_PATH(request)%>skit/css/font-awesome.css" rel="stylesheet">
<link href="<%=Kit.URL_PATH(request)%>skit/css/jquery-ui.css?v=0" rel="stylesheet">
<script src="<%=Kit.URL_PATH(request)%>skit/js/jquery-2.1.4.min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/js/jquery-ui.min.js?v=0"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/js/jquery.nicescroll.min.js"></script>
<style type='text/css'>
body {overflow-y:hidden;overflow-x:hidden; margin-top:0px; margin-left:0px; margin-bottom:0px; margin-right:0px }
.ui-widget {
    font-family: Verdana,Arial,sans-serif;
    font-size: 12px;
}
.checkbox, .radio {
    position: relative;
    display: block;
    margin-top: 0px;
    margin-bottom: 0px;
	border-left:0px;
	width:0px;
}
.input-group-addon-x {
	border-top: 1px solid #ccc;
	border-bottom: 1px solid #ccc;
    border-right: 1px solid #ccc;
	border-left: 1px solid #eee;
}
.simple-modal .simple-modal-header h1 {
    margin: 0;
    color: #404040;
    font-size: 14px;
    font-weight: bold;
    font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    line-height: 36px;
}
</style>
<SCRIPT type="text/javascript">
function removeBlank(str){
    var reg = /\n(\n)*( )*(\n)*\n/g;
    return str.replace(reg,"\n");
}

function timeout_alert(tips, title){
	if(!title) title = "提示";
	window.setTimeout("skit_alert('"+tips+"','"+title+"');", 300);
}
//重载父类方法
function skit_alert()
{
	var args = skit_alert.arguments;
	if( window.top != window && top && window.top.skit_alert )
	{
		if( args.length == 3 )
			window.top.skit_alert(args[0], args[1], args[2]);
		else if( args.length == 2 )
			window.top.skit_alert(args[0], args[1]);
		else
			window.top.skit_alert(args[0]);
	}
	else{
		alert(args[0]);
	}
}

function skit_error()
{
	var args = skit_error.arguments;
	if( window.top != window && top && window.top.skit_alert )
	{
		if( args.length == 2 )
			window.top.skit_alert(args[0], "错误提示", args[1]);
		else
			window.top.skit_alert(args[0], "错误提示");
	}
}

function skit_errtip()
{
    var args = skit_errtip.arguments;
    if( window.top != window && top && window.top.skit_alert )
	{
		window.top.skit_alert(args[0], "错误输入提示", function(){
			if( args.length > 1 )
			{
				args[1].focus();
			}
		});
	}
}

function skit_input()
{
	var args = skit_input.arguments;
	if( window.top != window && window.top && top.skit_input )
	{
		window.top.skit_input(args[0], args[1], args[2]);
	}
}
function skit_confirm()
{
	var args = skit_confirm.arguments;
	if( window.top != window && window.top && window.top.skit_confirm )
	{
		if( args.length == 2 )
		{
			window.top.skit_confirm(args[0], args[1]);
		}
		else if( args.length == 3 )
		{
			window.top.skit_confirm(args[0], args[1], args[2]);
		}
		else
		{
			window.top.skit_confirm(args[0]);
		}
	}
}
function skit_message()
{
	var args = skit_message.arguments;
	if( window.top != window && window.top && window.top.skit_message )
	{
		if( args.length == 5 )
			window.top.skit_message(args[0],args[1],args[2],args[3],args[4]);
		else if( args.length == 4 )
			window.top.skit_message(args[0],args[1],args[2],args[3]);
		else if( args.length == 3 )
			window.top.skit_message(args[0],args[1],args[2]);
		else if( args.length == 2 )
			window.top.skit_message(args[0],args[1]);
		else if( args.length == 1 )
			window.top.skit_message(args[0]);
	}
}
function skit_alert_hidden()
{
	if( window.top != window && window.top && window.top.skit_alert_hidden )
	{
		window.top.skit_alert_hidden();
	}
}
function skit_showMask()
{
	if( window.top != window && window.top && window.top.skit_showMask )
	{
		window.top.skit_showMask();
	}
}
function skit_hiddenMask()
{
	if( window.top != window && window.top && window.top.skit_hiddenMask )
	{
		window.top.skit_hiddenMask();
	}
}
function skit_showLoading(ts)
{
	if( window.top != window && window.top && window.top.skit_showLoading )
	{
		window.top.skit_showLoading(ts);
	}
}
function skit_hiddenLoading()
{
	if( window.top != window && window.top && window.top.skit_hiddenLoading )
	{
		window.top.skit_hiddenLoading();
	}
}
function skit_alert_keydown_clos()
{
	if( window.top != window && window.top && window.top.skit_alert_keydown_clos )
	{
		window.top.skit_alert_keydown_clos();
	}
}
function previewMms()
{
	var args = previewMms.arguments;
	if( window.top != window && window.top && window.top.previewMms )
	{
		window.top.previewMms(args[0]);
	}
}
function closePreviewMms()
{
	if( window.top != window && window.top && window.top.closePreviewMms )
	{
		window.top.closePreviewMms();
	}
}
function previewContent()
{
	var args = previewContent.arguments;
	if( window.top != window && window.top && window.top.previewContent )
	{
		
		if( args.length > 1 )
		{
			window.top.previewContent(args[0],args[1]);
		}
		else
		{
			window.top.previewContent(args[0]);
		}
	}
}
function closePreview()
{
	if( window.top != window && window.top && window.top.closePreview )
	{
		window.top.closePreview();
	}
}
function closeView()
{
	if( window.top != window && window.top && window.top.closeView )
	{
		var args = closeView.arguments;
		if( args.length > 1 )
		{
			window.top.closeView(args[0], args[1]);
		}
		else
		{
			window.top.closeView();
		}
	}
}

function reopenView()
{
	if( window.top != window && window.top && window.top.reopenView )
	{
		window.top.reopenView();
	}
}

function openView()
{
	var args = openView.arguments;
	if( window.top != window && window.top && window.top.openView )
	{
		if( args.length > 2)
		{
			return window.top.openView(args[0], args[1], args[2]);
		}
		else
		{
			return window.top.openView(args[0], args[1]);
		}
	}
	else
	{
		window.open(args[1],"_blank","left=0,top=0,width="+window.screen.availWidth+"px,height="+window.screen.availHeight+"px,resizable=no,toolbar=no,location=no,directories=no,menubar=no,scrollbars=no,status=yes");
		//window.open(args[1],"_blank","left=0,top=0,width="+window.document.body.clientWidth+"px,height="+window.screen.body.clientHeight+"px,resizable=no,toolbar=no,location=no,directories=no,menubar=no,scrollbars=no,status=yes");
	}
	return null;
}
function showPopview()
{
	var args = showPopview.arguments;
	if( window.top != window && window.top && window.top.showPopview )
	{
		if( args.length > 4 )
		{
			window.top.showPopview(args[0], args[1], args[2], args[3], args[4], args[5]);
		}
		else if( args.length > 2 )
		{
			window.top.showPopview(args[0], args[1], args[2], args[3]);
		}
		else
		{
			window.top.showPopview(args[0], args[1]);
		}
	}
}
function closePopview()
{
	if( window.top != window && window.top && window.top.closePopview )
	{
		window.top.closePopview();
	}
}
function setUserActionMemory(key, value)
{
//alert("setUserActionMemory:"+key+","+value);
	if( window.top != window && window.top && window.top.setUserActionMemory )
	{
		window.top.setUserActionMemory(key, value);
	}
}
//得到用户行为记忆参数
function getUserActionMemory(key, defval)
{
	if( window.top != window && window.top && top.getUserActionMemory )
	{
		return window.top.getUserActionMemory(key, defval);
	}
	return null;
}
//构建权限
function skit_buildPermissions()
{
	if( window.top != window && window.top && window.top.getPermissions )
	{
		var args = window.top.getPermissions();//从上层框架得到权限列表组
		if(args)
		{
			for( var i = 0; i < args.length; i++ )
			{
				var id = args[i][0];
				var disabled = args[i][1];
				var btn = document.getElementById(id);
				if( btn )
				{
					//alert(id+":"+disabled+", btn="+btn);
					if(!disabled)
					{
						btn.onclick = new Function("skit_alert('对不起，您没有该按钮操作权限。');");
					}
				}
			}
		}
	}
}
function skit_inner_alert()
{
    var args = skit_inner_alert.arguments;
	var title = args[0];
	var message = args[1];
    if( window.top && window.top.skit_alert )
    {
    	window.top.skit_alert(message, title);
		return;
	}
}
///////////////////////////////////////////////////////////////////////////////
<ww:if test="responseException!=null">
skit_inner_alert("异常提示", "<ww:property value='%{responseException}' escape='false'/>");
</ww:if>
<ww:if test="responseMessage!=null">
skit_inner_alert("后台提示", "<ww:property value='responseMessage'/>");
</ww:if>
document.title = "<ww:property value='viewTitle'/>";
if( window.top != window && window.top && window.top.setViewTitle )
{
	<ww:if test="viewTitle!=null">
	window.top.setViewTitle("<ww:property value='viewTitle'/>");
	</ww:if>
}

skit_hiddenLoading();
skit_buildPermissions();//装载权限
var	windowWidth = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
var	windowHeight = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
window.onresize=function(){
	windowWidth = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
	windowHeight = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
	//alert("onresize:"+windowWidth+","+windowHeight+"; h="+window.screen.height);
	if( window.resizeWindow ){
		resizeWindow();
	}
}
if( window.resizeWindow ){
	resizeWindow();
}
//使用nice滚动条组件
var color = '#cccccc';
var table = $( '.skit_table_head' );
if( table )
{
	var td = table.first();
	color = td.css('background-color');
}
if( color ){}else
{
	color = "#cccccc";
}
var skitViewDiv = $( '.skit_view_div' );
if( skitViewDiv )
	skitViewDiv.niceScroll({
		cursorcolor: color,
		railalign: 'right',
		cursorborder: "none", 
		horizrailenabled: false, 
		zindex: 2001, 
		left: '0px', 
		cursoropacitymax: 0.6, 
		cursorborderradius: "0px", 
		spacebarenabled: false });
var iframes = $("iframe");
if( iframes )
{
	for(var i = 0; i < iframes.length; i++ )
	{
		var iframe = iframes[i];
		if( iframe.style.display == "" && iframe.className != "nonicescroll" )
		{
			$("#"+iframe.id).niceScroll({
				cursorcolor: color,
				railalign: 'right',
				cursorborder: "none", 
				horizrailenabled: true, 
				zindex: 2001, 
				left: '0px', 
				cursoropacitymax: 0.6, 
				cursorborderradius: "0px", 
				spacebarenabled: false });
		}
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

/**
 * 中文转unicode编码
 */
function chr2Unicode(str)
{
	if ('' != str) 
	{
		var st, t, i;
		st = '';
		for (i = 1; i <= str.length; i++)
		{
			t = str.charCodeAt(i - 1).toString(16);
			if (t.length < 4)
			while(t.length <4)
				t = '0'.concat(t);
			t = t.slice(2, 4).concat(t.slice(0, 2))
			st = st.concat(t);
		}
		return(st.toUpperCase());
	}
	else
	{
		return('');
	}
}

function showObject(obj)
{
	if( obj )
	{
		var alt = "显示对象详情";
		for(var key in obj ){
			var subobj = obj[key];
			var type = typeof subobj;
			if( type == 'object' ){
				alt += "\r\n【"+key+"】::";
				for(var key1 in subobj ){
					alt += "\r\n\t\t"+key1+"="+subobj[key1];
				}
			}
			else{
				alt += "\r\n【"+key+"("+type+")】="+subobj;
			}
			
		}
		alert(alt);
	}
}

$(function () { 
	$("[data-toggle='tooltip']").tooltip(); 
});

//#############################################################################
var memorySortData = new Object();
function sortAmount(tbid, icoid, index)
{
	try
	{
		var memorySortIcon = memorySortData[tbid];
		if( !memorySortIcon ){
			memorySortIcon = new Object();
			memorySortData[tbid] = memorySortIcon;
		}
		for( var key in memorySortIcon )
		{
			if( key != icoid ) memorySortIcon[key].className = "fa skit_fa_icon fa-sort";
		}
		var sortIcon = document.getElementById(icoid);
		memorySortIcon[icoid] = sortIcon;
		var order = sortIcon.className.indexOf("fa-sort-amount-desc") != -1;
		var table = document.getElementById(tbid);
		quicksort(table.rows, 1, table.rows.length-2, order, function(pivot, val, order){
	    	if( order )
	    	{
	    		if( Number(val) < Number(pivot) )
		        {
	    			return true;
		        }
	    	}
	        else
		    {
	    		if( Number(val) > Number(pivot) )
		        {
	    			return true;
		        }
		    }
	    	return false;
		}, index);
		if( order )	sortIcon.className = "fa skit_fa_icon fa-sort-amount-asc";
		else sortIcon.className = "fa skit_fa_icon fa-sort-amount-desc";
	}
	catch(e)
	{
		skit_alert("数字排序出现异常"+e);
	}
}

function sortAlpha(tbid, icoid, index)
{
	try
	{
		var memorySortIcon = memorySortData[tbid];
		if( !memorySortIcon ){
			memorySortIcon = new Object();
			memorySortData[tbid] = memorySortIcon;
		}
		for( var key in memorySortIcon )
		{
			if( key != icoid ) memorySortIcon[key].className = "fa skit_fa_icon fa-sort";
		}
		var sortIcon = document.getElementById(icoid);
		memorySortIcon[icoid] = sortIcon;
		var order = sortIcon.className.indexOf("fa-sort-alpha-desc") != -1;
		var table = document.getElementById(tbid);
		quicksort(table.rows, 1, table.rows.length-2, order, function(pivot, val, order){
			try
			{
		    	if( order )
		    	{
		    		if( val < pivot )
			        {
		    			return true;
			        }
		    	}
		        else
			    {
		    		if( val > pivot )
			        {
		    			return true;
			        }
			    }
			}
			catch(e)
			{
				skit_alert("比较出现异常"+e);
			}
	    	return false;
		}, index);
		if( order )	sortIcon.className = "fa skit_fa_icon fa-sort-alpha-asc";
		else sortIcon.className = "fa skit_fa_icon fa-sort-alpha-desc";
	}
	catch(e)
	{
		skit_alert("字母排序出现异常"+e);
	}
}
//表格排序
function quicksort( rows, low, high, order, compare, dataIdx )
{
    var pivot;
    if( low < high )
    {
        pivot = partition( rows, low, high, order, compare, dataIdx );
        quicksort( rows, low, pivot - 1, order, compare, dataIdx );
        quicksort( rows, pivot + 1, high, order, compare, dataIdx );
    }
}

function partition( rows, low, high, order, compare, dataIdx )
{
    var pivot, p_pos, i;
    p_pos = low;
    pivot = rows[p_pos].cells[dataIdx].title;
//alert("partition(low="+low+",high="+high+") "+pivot);
    for( i = low + 1; i <= high; i++ )
    {
    	if( compare(pivot, rows[i].cells[dataIdx].title, order) )
        {
            p_pos++;
            swap( rows, p_pos, i );
        }
    }
    swap( rows, low, p_pos );
    return p_pos;
}
/**
 * 交换排序的位置
 */
 function swap( rows, i, j )
{
    var tmp_i = rows[i].innerHTML;
    var id_i = rows[i].id;
    var tmp_j = rows[j].innerHTML;
    var id_j = rows[j].id;
    rows[i].id = id_j;
    rows[i].innerHTML = tmp_j;
    rows[j].id = id_i;
    rows[j].innerHTML = tmp_i;
}

 function nowtime()
 {
 	try
 	{
 		var date = new Date();
 	    var year = date.getFullYear();
 	    var month = date.getMonth() + 1;
 	    var day = date.getDate();
 	    var hours = date.getHours();
 	    var minutes = date.getMinutes();
 	    var seconds = date.getSeconds();
 	    month = month < 10 ? "0" + month : month;
 	    day = day < 10 ? "0" + day : day;
 	    hours = hours < 10 ? "0" + hours : hours;
 	    minutes = minutes < 10 ? "0" + minutes : minutes;
 	    seconds = seconds < 10 ? "0" + seconds : seconds;
 	    return year+"-"+month+"-"+day+" "+hours+":"+minutes+":"+seconds;
 	}
 	catch(e)
 	{
 		alert("计算时间出错"+e);
 		return datestr;
 	}
 }
//#############################################################################
</SCRIPT>