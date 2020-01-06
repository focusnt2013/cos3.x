<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<SCRIPT TYPE="text/javascript">
var reallocation = "<%=Kit.URL_PATH(request)%>";
//检查下载
function getDiggProgress()
{
	DiggMgr.getDiggProgress({
		callback:function(response){
			if( response.succeed )
			{
				if( response.result == 200 )
				{
					dlg_progress.hide();
					$("#"+grid_type).pqGrid( "hideLoading" );
					if(document.referrer && dodebug ){
						window.top.skit_confirm(response.message+"<br/>您是否查看DIGG报告",function(yes){
							if( yes ){
								var gridxml = document.getElementById("gridxml").value;
								window.top.removeView("diggcfg!debuglog.action?id=digg");
								window.top.openView("查看元数据模板数据驱动引擎调试日志: "+gridxml, "diggcfg!debuglog.action?id=digg")
							}
						});
					}
				}
				else
				{
					setProgress(response.result, response.message);
					if( dlg_progress != null ) window.setTimeout(getDiggProgress, 500);
				}
			}
			else
			{
				$("#"+grid_type).pqGrid( "hideLoading" );
				dlg_progress.hide();
				window.top.skit_alert(response.message, "查询错误提示");
			}
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("获取查询进度出现异常："+err)}
	});
}

//检查下载
function getExportProgress()
{
	DiggMgr.getExportProgress({
		callback:function(response){
			if( response.succeed )
			{
				if( response.result == 200 )
				{
					dlg_progress.hide();
					window.top.skit_alert(response.message, "下载成功提示");
					$("#"+grid_type).pqGrid( "hideLoading" );
					if( parent.showDebugexport ){
						parent.showDebugexport(true);
					}
				}
				else
				{
					setProgress(response.result, response.message);
					if( dlg_progress != null ) window.setTimeout("getExportProgress()",500);
				}
			}
			else
			{
				$("#"+grid_type).pqGrid( "hideLoading" );
				dlg_progress.hide();
				window.top.skit_alert(response.message, "下载错误提示");
				if( parent.showDebugexport ){
					parent.showDebugexport(true);
				}
			}
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("获取下载进度出现异常："+err)}
	});	
}

function stopExport()
{
	dlg_progress.hide();
	DiggMgr.stopExport({
		callback:function(response){
			if( response.succeed )
			{//表示正在执行升级
				window.top.skit_alert(response.message);
			}
			else
			{
				window.top.skit_alert(response.message, "错误提示");
			}
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("出现异常："+err)}
	});	
}

function setProgress(result, message)
{
	var ifr = window.frames["iProgress"];
	if( ifr && ifr.setProgress )
	{
		ifr.setProgress(result, message);
	}
}
var dlg_progress;
function startProgress(){
	if( remoteDataReceived ){
		return;
	}
	grid_progress("<i class='fa fa-digg'></i>进度");
	$grid.pqGrid("showLoading");
	window.setTimeout(function(){
		getDiggProgress();
	}, 500);
}
function grid_progress(title, callback_abort)
{
	try
	{
		if( callback_abort )
		{
			dlg_progress = new SimpleModal({"btn_ok":"中止","width":640});
			dlg_progress.show({
		        "model":"confirm",
		        "callback": function(){
		            if( callback_abort )
		            {
		            	callback_abort(true);
		            	dlg_progress = null;
		            }
		        },
		        "cancelback": function(){
		        },
		        "title": title,
		        "contents": "<iframe src='helper!progress.action' name='iProgress' id='iProgress' class='nonicescroll' style='width:600px;height:160px;border:0px;'></iframe>"
		    });
		}
		else{
			var ph = 192;
			var href = window.location.href;
			if( href.indexOf("diggcfg!debug.action") == -1){
				ph = 160;
			}
			//alert(document.referrer);
			dlg_progress = new SimpleModal({"hideFooter":true,"width":640});
			dlg_progress.show({
		        "model":"modal",
		        "cancelback": function(){
		        },
		        "title": title,
		        "contents": "<iframe src='helper!progress.action' name='iProgress' id='iProgress' class='nonicescroll' style='width:600px;height:"+ph+"px;border:0px;'></iframe>"
		    });
		}
	    var div = document.getElementById('simple-modal');
	    var top = h/2 - div.scrollHeight/2;
	    dlg_progress.options.offsetTop = top;
	    dlg_progress._display();
	}
	catch(e)
	{
		$grid.pqGrid("hideLoading");
		alert(e);
	}
}
<ww:if test='dodebug'>
function viewObject(type, title, detailKey, detailLabel)
{
	switch(type){
	case 0:
    	top.showJson(gridobj, title, w, h-128);
		break;
	case 1:
    	if( dataLocal ){
        	top.showJson(dataLocal, title+"[本地]", w, h-128);
    	}
    	else if( dataRemote ){
        	top.showJson(dataRemote, title+"[远程]", w, h-128);
    	}
    	else{
    		top.skit_alert(title+"不存在");
    	}
		break;
	case 2:
    	top.showJson(colModel, title, w, h-128);
		break;
	case 3:
    	top.showJson(dataModel, title, w, h-128);
		break;
	case 4:
		if(fieldOptions){
			top.showJson(fieldOptions, title, w, h-128);
		}
		break;
	case 5:
		if(valueLabels){
			top.showJson(valueLabels, title, w, h-128);
		}
		break;
	case 6:
		top.showJson({"pq_rowcls":filterRowcls, "pq_cellcls":filterCellcls}, title, w, h-128);
		break;
	case 10:
    	top.showJson(detailGrids[detailKey], title+"["+detailLabel+"]", w, h-128);
		break;
	case 11:
    	if( dataLocalPlus ){
        	top.showJson(dataLocalPlus[detailKey], title+"["+detailLabel+"]", w, h-128);
    	}
    	else{
    		top.skit_alert(title+"查看关联表数据对象不存在");
    	}
		break;
	}
}

function editJavascript(type, title, i, j)
{
	var js = "";
	switch(type){
	case 0://
		js = document.getElementById( 'globalscript' ).innerHTML;
		top.editJavascript(js, title, w, h-128, function(editjs){
			if( parent && parent.setJavascriptDatamodel ){
				parent.setJavascriptDatamodel(editjs);
			}
    	});
		break;
	case 3://
		js = document.getElementById( 'beforeGridView' ).innerHTML;
		top.editJavascript(js, title, w, h-128, function(editjs){
			if( parent && parent.setJavascriptBeforeGridView ){
				i = editjs.indexOf("/*用户自定义表格初始化前回调脚本区域below*/");
				j = editjs.indexOf("/*用户自定义表格初始化前回调脚本区域above*/");
				if( i == -1 || j == -1 ){
    				setTimeout("top.skit_alert('请在代码区/*用户自定义表格初始化前回调脚本区域below*/与/*用户自定义表格初始化前回调脚本区域above*/之间修改脚本');", 500);
					return;
				}
				i += "/*用户自定义表格显示前回调脚本区域below*/".length+1;
				parent.setJavascriptBeforeGridView(editjs.substring(i, j));
			}
    	});
		break;
	case 1:
		if( dataModel.location == "remote" ){
			js = ""+dataModel.getData;
		}
		else{
			if(gridobj.beforeTableView){
				js = ""+gridobj.beforeTableView;
			}
		}
		if( js ){
        	top.showJavascript(js, title, w, h-128, function(editjs){
        		//得到的编辑后的返回脚本
        	});
		}
		else{
    		top.skit_alert("本模板不能"+title);
		}
		break;
	case 2://编辑渲染器脚本
		if( j >= 0 ){
			js = colModel[i].colModel[j].render+"";
		}
		else{
			js = colModel[i].render+"";
		}
    	top.showJavascript(js, "编辑单元格渲染器脚本: "+title, w, h-128, function(editjs){
    		//得到的编辑后的返回脚本
    	});
	case 4://编辑器類型脚本
		if( j >= 0 ){初始化
			js = colModel[i].colModel[j].editor.type+"";
		}
		else{
			js = colModel[i].editor.type+"";
		}
    	top.showJavascript(js, "编辑单元格自定义编辑器脚本: "+title, w, h-128, function(editjs){
    		//得到的编辑后的返回脚本
    	});
		break;
	case 5://编辑器初始化脚本
		if( j >= 0 ){初始化
			js = colModel[i].colModel[j].editor.init+"";
		}
		else{
			js = colModel[i].editor.init+"";
		}
    	top.showJavascript(js, "编辑单元格编辑器初始化脚本: "+title, w, h-128, function(editjs){
    		//得到的编辑后的返回脚本
    	});
    	break;
	case 6://编辑可否判斷腳本	
		if( j >= 0 ){初始化
			js = colModel[i].colModel[j].editable+"";
		}
		else{
			js = colModel[i].editable+"";
		}
    	top.showJavascript(js, "编辑单元格是否可编辑判断脚本: "+title, w, h-128, function(editjs){
    		//得到的编辑后的返回脚本
    	});
		break;
	case 11://查看详情视图的处理函数
		var detail = detailGrids[i];
		if( detail.dataModel.location == "remote" ){
			var js = ""+detail.dataModel.getData;
	    	top.showJavascript(js, title, w, h-128, function(editjs){
	    	});
		}
		break;
	case 12://编辑关联表的渲染器脚本
		var detail = detailGrids[i];
		js = detail.colMode[j].render+"";
    	top.showJavascript(js, title, w, h-128, function(editjs){
    	});
		break;
	}
}
</ww:if>
function checkEmail(ui, nullable)
{
    try
    {
        var value = ui.value.trim();
        if( nullable && value == "" ) return true; 
		if( value.indexOf("<br>") != -1 ){
			value = value.replace("<br>", "");
			ui.value = value;
		}
        var regexp = "^([\.a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+";
        var m = value.match(new RegExp(regexp));
        if( m )
        {
	        return true;
        }
        if(!ui.msg) ui.msg = "请输入正确的邮箱地址";
    	return false;
    }
    catch(e)
    {
    	ui.msg = "检查邮箱地址出现异常"+e;
    	return false;
    }
}

function checkUrl(ui, nullable)
{
    try
    {
        var value = ui.value.trim();
        if( nullable && value == "" ) return true; 
		if( value.indexOf("<br>") != -1 ){
			value = value.replace("<br>", "");
			ui.value = value;
		}
        var regexp = "^(http|https|ftp)\://([a-zA-Z0-9\.\-]+(\:[a-zA-Z0-9\.&amp;%\$\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\-]+\.)*[a-zA-Z0-9\-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(\:[0-9]+)*(/($|[a-zA-Z0-9\.\,\?\!\'\\\+&amp;%\$#\=~_\-]+))*$";
        var m = value.match(new RegExp(regexp));
        if( m )
        {
	        return true;
        }
        if(!ui.msg) ui.msg = "请输入正确的URL网址";
    	return false;
    }
    catch(e)
    {
    	ui.msg = "检查URL网址出现异常"+e;
    	return false;
    }
}

function checkVersion(ui, nullable)
{
    try
    {
        var value = ui.value.trim();
        if( nullable && value == "" ) return true; 
		if( value.indexOf("<br>") != -1 ){
			value = value.replace("<br>", "");
			ui.value = value;
		}
        var regexp1 = "^(\\d{1,2})\\.(\\d{1,2})\\.(\\d{1,2})\\.(\\d{1,2})$";
        var m1 = value.match(new RegExp(regexp1));
        if( m1 )
        {
	        return true;
        }
        ui.msg = "版本号按照当前1.0.0.0格式输入";
    	return false;
    }
    catch(e)
    {
    	ui.msg = "检查版本号输入出现异常"+e;
    	return false;
    }
}

function checkHost(ui, nullable)
{
    try
    {
        var value = ui.value.trim();
        if( nullable && value == "" ) return true; 
		if( value.indexOf("<br>") != -1 ){
			value = value.replace("<br>", "");
			ui.value = value;
		}
        var regexp1 = "^[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\.?$";
        var regexp2 = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";
        var m1 = value.match(new RegExp(regexp1));
        var m2 = value.match(new RegExp(regexp2));
        if( m1 || m2 )
        {
	        return true;
        }
        if(!ui.msg) ui.msg = "请输入IP地址或域名";
    	return false;
    }
    catch(e)
    {
    	ui.msg = "检查主机IP或地址出现异常"+e;
    	return false;
    }
}

function checkSubject(ui, nullable)
{
    try
    {
        var value = ui.value.trim();
        if( nullable && value == "" ) return true;
		if( value.indexOf("<br>") != -1 ){
			value = value.replace("<br>", "");
			ui.value = value;
		}
        value = value.replace("<br>", "");
        var regexp1 = "^[a-zA-Z0-9_\u4e00-\u9fa5]+$";
        var m1 = value.match(new RegExp(regexp1));
        if( m1 )
        {
	        return true;
        }
        ui.msg = "只能含有汉字、数字、字母、下划线不能以下划线开头和结尾";
    	return false;
    }
    catch(e)
    {
    	ui.msg = "检查名称出现异常"+e;
    	return false;
    }
}

function checkNumber(ui, nullable, maxnum)
{
    var value = ui.value;
    if( nullable && value == "" ) return true; 
    try
    {
    	if( value == "" )
    	{
    		return false;
    	}
		if( value.indexOf("<br>") != -1 ){
			value = value.replace("<br>", "");
			ui.value = value;
		}
        var num = Number(value);
        if( isNaN(num) )
        {
        	return false;
        }
        if( maxnum && num > maxnum )
        {
        	return false;
        }
        return true;
    }
    catch(e)
    {
    	ui.msg = "检查输入数字出现异常"+e;
    	return false;
    }
}

function checkPort(ui, nullable)
{
    var value = ui.value;
    if( nullable && value == "" ) return true; 
    try
    {
    	value = value.replace(/[\r\n]/g,"");
        value = value.replace("<br>", "");
        ui.value = value;
        var port = Number(value);
        if( !port || port < 0 || port > 65535 )
        {
        	ui.msg = "端口("+value+")请输入0到65535的整数";
        	return false;
        }
        return true;
    }
    catch(e)
    {
    	ui.msg = "端口("+value+")请输入0到65535的整数";
    	return false;
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

function alertObject(obj)
{
	if( obj )
	{
		var alt = "";
		for(var key in obj )
			alt += "\r\n\t\t"+key+"="+obj[key];
		alert("展示对象内容:"+alt);
	}
	else{
		alert("对象为空");
	}
}


function showObject(obj, title, width, height)
{
	if( obj )
	{
		showjson_data = obj;
		showjson_title = title;
        window.setTimeout(function(){
        	showJson(width, height);
        }, 300);
	}
}

function previewObject(obj)
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

var showjson_title;
var showjson_data;
function showJson(width, height){
	width = width?width:w;
	height = height?height:(h-128);
	window.top.showJson(showjson_data, showjson_title?showjson_title:"查看数据对象", width, height);
}

function showNumeric(type, val){
	if( "length" == type ){
		return showDatalength(val);
	}
	else if( "size" == type ){
		return showDatasize(val);
	}
	else if( "rmb" == type ){
		if( val == 0 || val == "0" ){
			return "￥0";
		}
		var size = showDatasize(val);
		if( size == "" ){
			return "";
		}
		return "￥"+size;
	}
	else if( "doller" == type ){
		return "$"+showDatasize(val);
	}
}

function showDatalength(str)
{
	if( str )
	{
		var size = str;
		try
		{
			if( typeof str == "string" )
			{
				size = parseFloat(str)
			}
		}
		catch(e)
		{
		}
		var ret;
	    if( size < 1024*1024 )
	    {
	    	ret = (size/1024).toFixed(2)+ "K";
	    }
	    else if( size < 1024*1024*1024 )
	    {
	    	ret = (size/(1024*1024)).toFixed(2)+ "M";
	    }
	    else if( size < Ts )
	    {
	    	ret = (size/(1024*1024*1024)).toFixed(2)+ "G";
	    }
	    else
	    {
	    	ret = (size/(1024*1024*1024*1024)).toFixed(2)+ "T";
	    }
	    return ret;
	}
	return "";
}

function showDatasize(val)
{
	if( val )
	{
		try
		{
			if( typeof val == "string" )
			{
				val = parseFloat(val)
			}
		}
		catch(e)
		{
		}
		var ret = ""+val;
	    if( val > 10000 ) {
	    	if( val < 10000*10000 ){
		    	if( val%10000 == 0 )
		    	{
		    		ret = (val/10000)+ "万";
		    	}
		    	else
		    	{
		    		val = (val/10000);
		    		if( val >= 1000 ) ret = val.toFixed(1)+ "万";
		    		else if( val >= 100 ) ret = val.toFixed(2)+ "万";
		    		else ret = val.toFixed(3)+ "万";
		    	}
		    }
		    else{
		    	if( val%(10000*10000) == 0 )
		    	{

		    		ret = (val/(10000*10000))+ "亿";
		    	}
		    	else
		    	{
			    	val = (val/(10000*10000));
		    		if( val >= 1000 ) ret = val.toFixed(1)+ "亿";
		    		else if( val >= 100 ) ret = val.toFixed(2)+ "亿";
		    		else ret = val.toFixed(3)+ "亿";
		    	}
		    }
	    }
	    return ret;
	}
	return "";
}

function html2text(obj, indx) {
	var content = obj[indx];
	if( content ){
		var old = content;
		content = content.replace(/<[^>]+>/g, ""); //去掉所有的html标记
		content = content.replace(/↵/g, "");     //去掉所有的↵符号
		content = content.replace(/[\r\n]/g, "") //去掉回车换行
		if( content != old ){
			content = content.replace(/\s*/g, "");  //去掉空格
		}
		obj[indx] = content;
	}
}
function showSnapshot()
{
	abortResize = false;
	resizeWindow();
	window.top.skit_confirm("你在主界面框架打开该网页？选择否将在新的浏览器标签页打开。",function(yes){
   		var referer = chr2Unicode(window.location.href);
		if( yes ){
			window.top.openView("网页快照", "helper!openSnapshot.action?snapshot="+referer);
		}
		else
		{
			document.getElementById("snapshot").value = referer;
			document.forms[0].action = "helper!openSnapshot.action";
			document.forms[0].method = "GET";
			document.forms[0].target = "_blank";
			document.forms[0].submit();
		}
	});
}
//时间戳汉室
function timestampstring()
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
	    return ""+year+month+day+hours+minutes+seconds;
	}
	catch(e)
	{
		alert("计算时间出错"+e);
		return datestr;
	}
}

//提示信息后2秒刷新数据和视图
function refreshDataAndView(alt){
	top.skit_alert(alt);
	$grid.pqGrid( "refreshDataAndView" );
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
function skit_date(a)
{
	try
	{
		var date = new Date();
		if( a ){
			var time = date.getTime();
			time += a*24*60*60*1000;
			date = new Date(time);
		}
	    var year = date.getFullYear();
	    var month = date.getMonth() + 1;
	    var day = date.getDate();
	    var hours = date.getHours();
	    var minutes = date.getMinutes();
	    var seconds = date.getSeconds();
	    month = month < 10 ? "0" + month : month;
	    day = day < 10 ? "0" + day : day;
	    return year+"-"+month+"-"+day;
	}
	catch(e)
	{
		alert("计算时间出错"+e);
		return datestr;
	}
}
function yesterday(datestr)
{
	try
	{
		var date = new Date();
		if( datestr ){
			var time = Date.parse(datestr);
			time -= 24*60*60*1000;
			date = new Date(time);
		}
		else{
			date.setTime(date.getTime()-24*60*60*1000);
		}
	    var year = date.getFullYear();
	    var month = date.getMonth() + 1;
	    var day = date.getDate();
	    month = month < 10 ? "0" + month : month;
	    day = day < 10 ? "0" + day : day;
	    return year+"-"+month+"-"+day;
	}
	catch(e)
	{
		alert("计算日期["+datestr+"]的昨天出错"+e);
		return datestr;
	}
}
//上传文件
//var lastFileinputTimestamp = 0;
function preupload(uploadTitle, uploadUrl, uploadTypes, uploadExtraDataCallback, fileuploadedCallback, fileloadedCallback, scaleImage)
{
	try
	{
		var divUploadfile = document.getElementById( 'divUploadfile' );
		var spanUploadtitle = document.getElementById( 'spanUploadtitle' );
		spanUploadtitle.innerHTML = uploadTitle;
		divUploadfile.style.display = "";
		if( scaleImage ){
			$("#uploadfile").fileinput({
					language: 'zh', //设置语言
					uploadUrl: uploadUrl, //上传的地址
					allowedFileExtensions: uploadTypes,//接收的文件后缀
					showUpload: true, //是否显示上传按钮
					showCaption: false,//是否显示标题
					showClose: false,
					showPreview: true,
					browseClass: "btn btn-primary", //按钮样式     
					previewSettings: { image: {width: "auto", height: "128px"} },
					//dropZoneEnabled: false,//是否显示拖拽区域
					minImageWidth: scaleImage.minImageWidth?scaleImage.minImageWidth:0, //图片的最小宽度
					minImageHeight: scaleImage.minImageHeight?scaleImage.minImageHeight:0,//图片的最小高度
					maxImageWidth: scaleImage.maxImageWidth?scaleImage.maxImageWidth:0,//图片的最大宽度
					maxImageHeight: scaleImage.maxImageHeight?scaleImage.maxImageHeight:0,//图片的最大高度
					maxFileSize: scaleImage.maxFileSize?scaleImage.maxFileSize:0,//单位为kb，如果为0表示不限制文件大小
					//minFileCount: 0,
					maxFileCount: 1, //表示允许同时上传的最大文件个数
					enctype: 'multipart/form-data',
					validateInitialCount:true,
					previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
					msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
		            uploadExtraData: uploadExtraDataCallback
			});
		}
		else{
			$("#uploadfile").fileinput({
					language: 'zh', //设置语言
					uploadUrl: uploadUrl, //上传的地址
					allowedFileExtensions: uploadTypes,//接收的文件后缀
					showUpload: true, //是否显示上传按钮
					showCaption: false,//是否显示标题
					showClose: false,
					showPreview: true,
					browseClass: "btn btn-primary", //按钮样式     
					previewSettings: { image: {width: "auto", height: "128px"} },
					//dropZoneEnabled: false,//是否显示拖拽区域
					//minFileCount: 0,
					maxFileCount: 1, //表示允许同时上传的最大文件个数
					enctype: 'multipart/form-data',
					validateInitialCount:true,
					previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
					msgFilesTooMany: "选择上传的文件数量({n}) 超过允许的最大数值{m}！",
		            uploadExtraData: function(previewId, index) {
						//额外参数的关键点
		                var obj = uploadExtraDataCallback();
		                obj.filename = document.getElementById( 'uploadfilename' ).value;
		                var i = obj.filename.lastIndexOf(".");
		                obj.filetype = obj.filename.substring(i+1);
		                return obj;
		            }
			});
		}
		divUploadfile.style.top = 64;
		divUploadfile.style.left = w/2 - divUploadfile.clientWidth/2;
		//$( '.file-preview' ).hide();
		if( !fileloadedCallback ){
			fileloadedCallback = function (data, info, index) {
				document.getElementById( 'uploadfilename' ).value = info.name;
				document.getElementById( 'spanUploadtitle' ).innerHTML = uploadTitle+": "+info.name;
			};
		}
		$("#uploadfile").on("fileloaded", fileloadedCallback);
		//导入文件上传完成之后的事件
		$("#uploadfile").on("fileuploaded", function (event, data, previewId, index) {
			//var ts = (new Date()).getTime();
			//var duration = ts - lastFileinputTimestamp;
			//&& duration > 1000 
			if( fileuploadedCallback ){
				fileuploadedCallback(data.response);//时间间隔超过一秒的才能调用
				fileuploadedCallback = null;
			}
//			else{
//				alert("fileuploadedCallback=null, previewId="+previewId+", index="+index);
//			}
			$("#uploadfile").fileinput("destroy");
			document.getElementById( 'divUploadfile' ).style.display = "none";
			//lastFileinputTimestamp = ts;
		});
		openDialogMask();
	}
	catch (e)
	{
		alert(e);
	}
}

function closeuploadfile()
{
	document.getElementById( 'divUploadfile' ).style.display = "none";
	$("#uploadfile").fileinput("destroy");
}

function uploadFile(rowIndx, dataIndx, fdfs_addr, prefix_url, filetypes){
	if( isEditing($grid) ){
		var rowData = $grid.pqGrid("getRowData", {rowIndx: rowIndx});
	    var url = "files!upfdfs.action";
		preupload("上传文件", url, filetypes?filetypes:["png","jpg"], function(){//填写上传参数的回调地址
			var obj = {};
			obj["fastdfs.tracker_servers"] = fdfs_addr;//"10.10.10.12:22122";
			obj.prefixurl = prefix_url;//"http://fdfs.banquanjia.com.cn/";
			return obj;
		}, function (data) {
			//data是服务的定义的返回json对象，可以做处理
			if(data.errcode){
			    top.skit_alert(data.errmsg);
			}else{
			    $grid.pqGrid( "quitEditMode" );
			    var row = {};
			    row[""+dataIndx] = data.url;
		        $grid.pqGrid( "updateRow", { rowIndx: rowIndx, row: row });
	            $grid.pqGrid( "refreshCell", { rowIndx: rowIndx, dataIndx: dataIndx} );
	            $grid.pqGrid( "refreshCell", { rowIndx: rowIndx, dataIndx: dataIndx+"_"} );
			}
		});
	}
	else{
		top.skit_alert("编辑模式下才能够上传文件");
	}
}

function uploadImage(rowIndx, dataIndx, fdfs_addr, prefix_url, filetypes){
	if( isEditing($grid) ){
		var rowData = $grid.pqGrid("getRowData", {rowIndx: rowIndx});
	    var url = "files!upfdfs.action";
		preupload("上传图片", url, filetypes?filetypes:["png","jpg","jpeg","gif"], function(){//填写上传参数的回调地址
			var obj = {};
			obj["fastdfs.tracker_servers"] = fdfs_addr;//"10.10.10.12:22122";
			obj.prefixurl = prefix_url;//"http://fdfs.banquanjia.com.cn/";
			return obj;
		}, function (data) {
			//data是服务的定义的返回json对象，可以做处理
			if(data.errcode){
			    top.skit_alert(data.errmsg);
			}else{
			    $grid.pqGrid( "quitEditMode" );
			    var row = {};
			    row[""+dataIndx] = data.url;
		        $grid.pqGrid( "updateRow", { rowIndx: rowIndx, row: row });
	            $grid.pqGrid( "refreshCell", { rowIndx: rowIndx, dataIndx: dataIndx} );
	            $grid.pqGrid( "refreshCell", { rowIndx: rowIndx, dataIndx: dataIndx+"_"} );
			}
		});
	}
	else{
		top.skit_alert("编辑模式下才能够上传图片");
	}
}

function previewImage(src, name, width){
	if( !width ) width= 360;
    top.skit_message("<img src='"+src+"' width='"+width+"'/>", name, 480, width+30);
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
/*打开新增cos系统用户的函数*/
var addsysuserCallback;
function openAddCosSysuserDialog(title, callback, cancelCallback){
	addsysuserCallback = callback;//将回调函数赋值给COS的回调
	return openDialogFrame("", title, 640, h-128, false, null, null, function(){
		document.forms[0].removeChild(document.getElementById("data"));
		closeDialog();
	});
}

/*显示提示对话框*/
function openTextDialog(title, text, width, height, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name)
{
	var id = openDialogFrame("editor!text.action",title, width, height, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name);
	window.setTimeout(function () {
	    var ifr = window.frames["_DiggDialog"];
	    if( ifr && ifr.setValue )
	    {
	    	ifr.setValue(text);
	    }
	    else{
	    	top.skit_alert("文本编辑器打开超时，请重新尝试");
	    }
	}, 2000);
	return id;
}
function setDialogText(text)
{
    var ifr = window.frames["_DiggDialog"];
    if( ifr && ifr.setValue )
    {
    	ifr.setValue(text);
    }
}

function openReloadDialog(title, url, width, height, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name)
{
	return openDialogFrame(url,title, width, height, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name, true);
}

/*显示提示对话框*/
function openDialog(title, url, width, height, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name)
{
	return openDialogFrame(url,title, width, height, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name, true);
}

function openSimpleDialog(title, url, width, height)
{
	return openDialogFrame(url, title, width, height, null, null, null, null, null, false);
}

var dialogSimpleModal;
var _needRefresh;
function closeDialog(needReload){
	if( needReload ){
	    closeDialogMask();
	}
	else{
		closeDialogMask(document.getElementById('divDialogMask'));
	}
    $("#"+grid_type).pqGrid( "hideLoading" );
}
function closeSimpleDialog(){
	_needRefresh = false;
	closeDialog();
}

function openDialogFrame(url, title, width, height, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name, needRefresh)
{
	if( !width || width > (w-32) ){
		width = w-2;
	}
	if( !height || height > (h-64) ){
		height = h - 64;
	}
    if(!ok_name) ok_name = "确定";
    if(!cancel_name) cancel_name = "取消";
    var hideFooter = ok_name&&ok_callback?false:true;
    var cfg = {"btn_ok":ok_name,"btn_cancel":cancel_name,"width":width,"hideFooter":hideFooter};
    if( dialogSimpleModal ){
    	dialogSimpleModal.hide();
    	dialogSimpleModal = null;
    }
    dialogSimpleModal = new SimpleModal(cfg);
    //alert(width+"x"+height);
    width -= 32;
	$("#"+grid_type).pqGrid( "showLoading" );
	var overflow = "";
	if(nicescroll==2){
		overflow = "overflow-y:scroll;"
	}
    content = "<iframe id='_DiggDialog' name='_DiggDialog' src='"+url+"' height='100%' "+
    	"style='border:0px solid #eee;height:"+height+"px;width:"+width+"px;"+(overflow)+"'></iframe>"
    dialogSimpleModal.show({
	  "model": ok_name&&ok_callback?"":"modal",
      "title":title,
      "callback": function(){
          if( ok_callback )
          {
        	  if( !ok_callback() ){
	       		  return false;
        	  }
          }
          $("#"+grid_type).pqGrid( "hideLoading" );
          return true;
      },
      "cancelback": function(){
          if( cancel_callback )
          {
        	  cancel_callback();
          }
          $("#"+grid_type).pqGrid( "hideLoading" );
      },
      "closeback": function(){
          $("#"+grid_type).pqGrid( "hideLoading" );
          closeDialogMask();
          if( needRefresh ){
        	  $("#"+grid_type).pqGrid( "refreshDataAndView" );
          }
      },
      "contents": content
    });
    _needRefresh = needRefresh;
    var div = document.getElementById('simple-modal');
    var top = h/2 - div.scrollHeight/2;
    dialogSimpleModal.options.offsetTop = top;
    dialogSimpleModal.options.width = width;
    dialogSimpleModal._display();
    openDialogMask();
    if( nicescroll || nicescroll == 1 )
    {
    	$( '#_DiggDialog' ).niceScroll({cursorcolor: '#fff',railalign: 'right', cursorborder: "none", horizrailenabled: false, zindex: 2001, left: '245px', cursoropacitymax: 0.6, cursorborderradius: "0px", spacebarenabled: false });
    }
    return "_DiggDialog";
}

function openDialogMask()
{
	var divDialogMask = document.getElementById("divDialogMask");
	divDialogMask.style.visibility = "visible";
	divDialogMask.style.width = w;
	divDialogMask.style.height = h;
}

function closeDialogMask(divDialogMask)
{
	var needReload = divDialogMask?false:true;
	divDialogMask = divDialogMask?divDialogMask:document.getElementById("divDialogMask");
	divDialogMask.style.visibility = "hidden";
	if( dialogSimpleModal ){
		dialogSimpleModal.hide();
        $("#"+grid_type).pqGrid( "hideLoading" );
        if( needReload ){
        	if( "grid_edit" == grid_type ){
        		window.location.reload();
        	}
        	else{
        		$("#"+grid_type).pqGrid( "refreshDataAndView" );
        	}
        }
	}
	dialogSimpleModal = null;
	closeuploadfile();
}
//得到随机码
var nonces = ["<ww:property value='nonce'/>"];
function getNonce(){
	DiggMgr.getNonce({
		callback:function(nonce){
			nonces.push(nonce);
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("获得随机数失败："+err)}
	});
	var nonce = nonces.pop();
	return nonce;
}
function showRowDataField(rowIndx, title, dataIndx){
	var rowData = $grid.pqGrid("getRowData", {rowIndx: rowIndx});
	top.skit_alert(rowData[dataIndx], title);
}

//重载父类方法
function skit_alert()
{
	var args = skit_alert.arguments;
	if( window.top != window && top && window.top.skit_alert )
	{
		if( args.length == 2 )
			window.top.skit_alert(args[0], "提示", function(){
				args[1].focus();
			});
		else
			window.top.skit_alert(args[0], "提示");
	}
	else{
		alert(args[0]);
	}
}
</SCRIPT>

<ww:if test="responseConfirm!=null">
<SCRIPT TYPE="text/javascript">
if( top && top.skit_alert )
top.skit_confirm('<ww:property value="responseMessage"/>', function(yes){
	if( yes )
	{
		top.openView('<ww:property value="viewTitle"/>', '<ww:property value="responseConfirm"/>');
	}
});
</SCRIPT>
</ww:if>
<ww:elseif test="responseMessage!=null">
<SCRIPT TYPE="text/javascript">
if( top && top.skit_alert )
top.skit_alert('<ww:property value="responseMessage"/>', "后台提示");
</SCRIPT>
</ww:elseif>
<ww:if test="responseException!=null">
<SCRIPT TYPE="text/javascript">
if( top && top.skit_alert )
top.skit_alert('<ww:property value="responseException" escape="false"/>',"异常提示");
</SCRIPT>
</ww:if>