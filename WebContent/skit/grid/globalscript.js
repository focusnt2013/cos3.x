function buildPreview(o){
	var type = "";
	var url = o.downloadUrl;
	var i = url.lastIndexOf(".");
	if( i != -1 ){
		var type = url.substring(i+1);
		if( type.match(/(doc|docx|xls|xlsx|ppt|pptx)$/i) ){
			o.type = "office";
		}
		else if( type.match(/(pdf)$/i) ){
			o.type = "pdf";
		}
		else if( type.match(/(htm|html)$/i) ){
			o.type = "html";
		}
		else if( type.match(/(txt|ini|csv|java|php|js|css)$/i) ){
			o.type = "text";
		}
		else if( type.match(/(pem|p12)$/i) ){
			o.type = "gdocs";
			o.downloadUrl = false;
		}
		else if( type.match(/(avi|mpg|mkv|mov|mp4|3gp|webm|wmv)$/i) ){
			o.type = "video";
		}
		else if( type.match(/(mp3|wav)$/i) ){
			o.type = "audio";
		}
		else if( type.match(/(jpg|png|gif|bmp|jpeg|webp)$/i) ){
			o.type = "image";
		}
	}
	return type?("."+type):"";
}

function viewObject(type, title, detailKey, detailLabel)
{
	switch(type){
	case 0:
    	top.showJson(gridobj, title, w, h-128);
		break;
	case 1:
		top.showJson(formData, title+"[表单]", w, h-128);
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
	}
}

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
/*显示提示对话框*/
function openReloadDialog(title, url, width, height, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name)
{
	return openDialogFrame(url,title, width, height, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name, true);
}

/*显示提示对话框*/
function openDialog(title, url, width, height, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name)
{
	return openDialogFrame(url,title, width, height, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name, false);
}

var dialogSimpleModal;
function closeDialog()
{
	var div = null;
	if( needReload ){
	    div = document.getElementById('divDialogMask');
	}
    closeDialogMask(div);
}
var _needReload;
function openDialogFrame(url, title, width, height, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name, needReload)
{
	_needReload = needReload;
	if( !width || width > (w-32) ){
		width = screenWidth-2;
	}
	if( !height || height > (h-64) ){
		height = screenHeight - 96;
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
          return true;
      },
      "cancelback": function(){
          if( cancel_callback )
          {
        	  cancel_callback();
          }
      },
      "closeback": function(){
          closeDialogMask();
      },
      "contents": content
    });
    var div = document.getElementById('simple-modal');
    var top = screenHeight/2 - div.scrollHeight/2;
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
	divDialogMask.style.width = screenWidth;
	//alert(window.document.body.scrollHeight);
	divDialogMask.style.height = window.document.body.scrollHeight;
}

function closeDialogMask(divDialogMask)
{
	var needReload = divDialogMask?false:true;
	divDialogMask = divDialogMask?divDialogMask:document.getElementById("divDialogMask");
	divDialogMask.style.visibility = "hidden";
	if( dialogSimpleModal ){
		dialogSimpleModal.hide();
		if( needReload ){
			window.location.reload();
		}
	}
	dialogSimpleModal = null;
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


var tempString;//临时字符串
function getObjectValue(data, args, i)
{
	if( data )
	{
		if( i + 1 == args.length )
		{
			return data[args[i]];
		}
		return getObjectValue(data[args[i++]], args, i);
	}
	return null;
}

function getDataValue(row, col)
{
	var val = row[col.dataIndx];
	if( val || val == 0 || col.dataType == 'bool' || col.dataType == 'boolean' ) 
	{
		if( typeof val == "string" ){
			return trimString(row[col.dataIndx]);
		}
		return val;
	}
	else
	{
		tempString = col.dataIndx;
		if( tempString && typeof tempString == "string" )
		{
			var args = tempString.split(".");
			if( args && args.length > 1 )
			{
				val = getObjectValue(row, args, 0);
	    		if( val || typeof val != "string" ){
	    			return val;
	    		}
			}
			else{
				val = row[col.dataIndx];
	    		if( val || typeof val != "string" ){
	    			return val;
	    		}
			}
		}
	}
	return "";
}

function getObjectData(row, dataIndx)
{
	var args = dataIndx.split(".");
	if( args )
	{
		return getObjectValue(row, args, 0);
	}
	return null;
}


function trimString(val)
{
	var str = ""+val;
	str = str.trim();
	while(str.length>0&&str.charAt(0)=="　")
	{
		str = str.substring(1);
	}
	return str;
}
