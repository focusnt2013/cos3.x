<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<link href="skin/defone/css/simplemodal.css" rel="stylesheet">
<style>
.simple-modal .simple-modal-header h1 {
    margin: 0;
    color: #404040;
    font-size: 12px;
    font-weight: bold;
    font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    line-height: 20px;
}
.simple-modal a.close, .simple-modal a.previous-image, .simple-modal a.next-image {
    position: absolute;
    top: 10px;
    color: #999;
    font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    font-size: 22px;
    font-weight: normal;
    line-height: 10px;
    text-decoration: none;
}
.skit_mask {
   visibility: hidden;
   background-color: #92A3B2;
   left: 0px;
   position: absolute;
   top: 0px;
   background-image: none;
   BORDER-TOP: 1px solid #405B40;
   BORDER-LEFT: 1px solid #405B40;
   BORDER-RIGHT: 1px solid #405B40;
   BORDER-BOTTOM: 1px solid #405B40;
   /* older safari/Chrome browsers */  
   -webkit-opacity: 0.5;  
   /* Netscape and Older than Firefox 0.9 */  
   -moz-opacity: 0.5;  
   /* Safari 1.x (pre WebKit!) 老式khtml内核的Safari浏览器*/  
   -khtml-opacity: 0.5;  
   /* IE9 + etc...modern browsers */  
   opacity: .5;  
   /* IE 4-9 */  
   filter:alpha(opacity=50);  
   /*This works in IE 8 & 9 too*/  
   -ms-filter:"progid:DXImageTransform.Microsoft.Alpha(Opacity=50)";  
   /*IE4-IE9*/  
   filter:progid:DXImageTransform.Microsoft.Alpha(Opacity=50);  
}

.dropdown-submenu {
	position: relative;
}

.dropdown-submenu > .dropdown-menu {
	top: 0;
	left: 100%;
	margin-top: -6px;
	margin-left: -1px;
	-webkit-border-radius: 0 6px 6px 6px;
	-moz-border-radius: 0 6px 6px;
	border-radius: 0 6px 6px 6px;
}

.dropdown-submenu:hover > .dropdown-menu {
	display: block;
}

.dropdown-submenu > a:after {
	display: block;
	content: " ";
	float: right;
	width: 0;
	height: 0;
	border-color: transparent;
	border-style: solid;
	border-width: 5px 0 5px 5px;
	border-left-color: #ccc;
	margin-top: 5px;
	margin-right: -10px;
}

.dropdown-submenu:hover > a:after {
	border-left-color: #fff;
}

.dropdown-submenu.pull-left {
	float: none;
}

.dropdown-submenu.pull-left > .dropdown-menu {
	left: -100%;
	margin-left: 10px;
	-webkit-border-radius: 6px 0 6px 6px;
	-moz-border-radius: 6px 0 6px 6px;
	border-radius: 6px 0 6px 6px;
}
</style>
<div id='divDialogMask' class='skit_mask' style='cursor:pointer;' title='点击关闭弹窗'></div>
<form>
<textarea name='editorContent' id='iEditor' style='display:none'></textarea>
</form>
<div style='z-index:1000;position:absolute;top:54px;left:70px;cursor:pointer;display:none' id='divDebugObjectScript'>
    <div class="btn-group btn-block">
		<button type="button" class="btn btn-outline btn-default btn-xs" style='width:80%;height:26px;font-size:12px;'
			>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="fa fa-eye"></span>元数据模板预览调测&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</button>
	    <button type="button" class="btn btn-outline btn-default btn-xs dropdown-toggle" style='height:26px'
	    	data-toggle="dropdown" aria-expanded="false">
	      <span class="fa fa-caret-down"></span>
	    </button>
	    <ul class="dropdown-menu multi-level" role="menu" id='ulTest'>
	    	<li class="dropdown-submenu" id='liMenuDebugSubgrid' style='display:none'>
	    		<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-share-alt fa-fw'></i>查看与编辑关联表对象与脚本</a>
    			<ul id='uiMenuDebugSubgrid' class="dropdown-menu">
    			</ul>
	    	</li>
			<li class="divider" id='liDividerMenuDebugInnerGrid' style='display:none'></li>
	    	<li><a title='' onclick='showJs(0,this)'><i class='skit_fa_icon fa fa-edit fa-fw'></i>
	    		编辑全局用户自定义脚本</a>
	    	<li><a title='' onclick='showJs(1,this)'><i class='skit_fa_icon fa fa-edit fa-fw'></i>
	    		编辑主表数据预处理脚本</a></li>
	    	<li id='liMenuDebugRender' class="dropdown-submenu">
	    		<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑主表单元格渲染器脚本</a>
    			<ul class="dropdown-menu" id='ulMenuDebugRender'>
    			</ul>
	    	</li>
	    	<li class="divider"></li>
	    	<li><a title='' onclick='viewObject(0,this)'><i class='skit_fa_icon fa fa-info fa-fw'></i>
	    		查看主表配置对象</a></li>
	    	<li><a title='' onclick='viewObject(1,this)'><i class='skit_fa_icon fa fa-code fa-fw'></i>
	    		查看主表数据对象</a></li>
	    	<li><a title='' onclick='viewObject(2,this)'><i class='skit_fa_icon fa fa-th fa-fw'></i>
	    		查看主表单元格配置对象</a></li>
	    	<li><a title='' onclick='viewObject(3,this)'><i class='skit_fa_icon fa fa-database fa-fw'></i>
	    		查看主表数据模型对象</a></li>
	    	<li><a title='主表查询通过该对象数据进行过滤选项预置' onclick='viewObject(4,this)'><i class='skit_fa_icon fa fa-list fa-fw'></i>
	    		查看条件过滤选项映射表</a></li>
	    	<li><a title='主表单元格显示数据某些字段通过该配置转义' onclick='viewObject(5,this)'><i class='skit_fa_icon fa fa-retweet fa-fw'></i>
	    		查看主表数据字段转义映射表</a></li>
	    	<li><a title='让单元格按照配置进行颜色转换' onclick='viewObject(6,this)'><i class='skit_fa_icon fa fa-paint-brush fa-fw'></i>
	    		查看主表单元格样式配置</a></li>
	    </ul>
    </div>
</div>
<script src="skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="skin/defone/js/simple-modal.js"></script>
<!-- Model dialog -->
<SCRIPT type="text/javascript">
var currentSimpleModal;
function openDialogMask(SM)
{
	var divDialogMask = document.getElementById("divDialogMask");
	divDialogMask.style.visibility = "visible";
	divDialogMask.style.width = _windowWidth;
	divDialogMask.style.height = _windowHeight;
	if(SM){
		divDialogMask.addEvent("click", function(e){
			closeDialogMask();
        }.bind( this ));
		currentSimpleModal = SM;
	}
}
function closeDialogMask()
{
	var divDialogMask = document.getElementById("divDialogMask");
	divDialogMask.style.visibility = "hidden";
	if( currentSimpleModal ){
		currentSimpleModal.hide();
	}
	currentSimpleModal = null;
    if( callback_alert )
    {
        callback_alert();
        callback_alert = null;
    }
    else
    {
        skit_alert_close();
    }
}
var callback_select;
function skit_select()
{
    var args = skit_select.arguments;
    var title = args[0];
    var options = args[1];
    callback_select = args[2];
    
    var SM = new SimpleModal();
    SM.addButton("确定", "btn primary", function(){
        if( callback_select )
        {
        	var val = "";
            var _options = document.getElementById('dlgSelectValue').options;
            for( var i = 0; i < _options.length; i++ ){
            	if( _options[i].selected ){
            		val = _options[i].value;
            		break;
            	} 
            }
        	callback_select(true, val);
        	callback_select = null;
        }
        this.hide();
        closeDialogMask();
    });
    SM.addButton("取消", "btn", function(){
        if( callback_input )
        {
            callback_input(false);
            callback_input = null;
        }
        this.hide();
		closeDialogMask();
    });
    openDialogMask(SM);
    SM.show({
        "model":"modal",
        "title":title,
        "closeback": function(){
      	  closeDialogMask();
        },
        "contents":"<div class='form-group'><div class='input-group' style='width:100%'><select class='form-control' id='dlgSelectValue'></select></div></div>"
    });
    var h = _windowHeight;
    var w = _windowWidth;
    var div = document.getElementById('simple-modal');
    var top = h/2 - div.scrollHeight/2;
    SM.options.offsetTop = top;
    SM._display();
    var _optoins = document.getElementById('dlgSelectValue').options;
    for( var i = 0; i < options.length; i++ ){
    	_optoins.add(new Option(options[i].text,options[i].value)); 
    }
}
//公告的输入框
var callback_input;
function skit_input()
{
    var args = skit_input.arguments;
    var title = args[0];
    var content = args[1];
    callback_input = args[2];
    var placeholder = args.length>3?args[3]:"";
    
    var SM = new SimpleModal();
    SM.addButton("确定", "btn primary", function(){
        if( callback_input )
        {
            callback_input(true, document.getElementById('dlgInputValue').value);
            callback_input = null;
        }
        this.hide();
        closeDialogMask();
    });
    SM.addButton("取消", "btn", function(){
        if( callback_input )
        {
            callback_input(false);
            callback_input = null;
        }
        this.hide();
		closeDialogMask();
    });
    openDialogMask(SM);
    SM.show({
        "model":"modal",
        "title":title,
        "closeback": function(){
      	  closeDialogMask();
        },
        "contents":"<input type='text' id='dlgInputValue' class='skit_common_input' placeholder='"+placeholder+"' />"
    });
    var h = _windowHeight;
    var w = _windowWidth;
    var div = document.getElementById('simple-modal');
    var top = h/2 - div.scrollHeight/2;
    SM.options.offsetTop = top;
    SM._display();
    document.getElementById('dlgInputValue').value = content;
}
var callback_text;
function skit_text()
{
    var args = skit_text.arguments;
    var title = args[0];
    var content = args[1];
    callback_text = args[2];
    var placeholder = args.length>3?args[3]:"";
    
    var SM = new SimpleModal();
    openDialogMask(SM);
    SM.addButton("确定", "btn primary", function(){
        if( callback_text )
        {
            callback_text(true, document.getElementById('dlgTextValue').value);
            callback_text = null;
        }
        this.hide();
  	  	closeDialogMask();
    });
    SM.addButton("取消", "btn", function(){
        if( callback_text )
        {
            callback_text(false);
            callback_text = null;
        }
        this.hide();
  	  	closeDialogMask();
    });
    SM.show({
        "model":"modal",
        "title":title,
        "closeback": function(){
      	  closeDialogMask();
        },
        "contents":"<textarea type='text' id='dlgTextValue' placeholder='"+placeholder+"' class='skit_common_textarea' ></textarea>"
    });
    var h = _windowHeight;
    var w = _windowWidth;
    var div = document.getElementById('simple-modal');
    var top = h/2 - div.scrollHeight/2;
    SM.options.offsetTop = top;
    SM._display();
    if( content ) document.getElementById('dlgTextValue').value = content;
}

var callback_confirm;
function skit_confirm()
{
    var args = skit_confirm.arguments;
    var i;
    for(i = 0; i < args.length; i++){
    	if( typeof args[i] == 'function' ){
    		break;
    	}
    }
    var callback_confirm = args[i];
    var contents = args[i-1];
    var title = i-2==0?args[i-2]:"确认操作";
    var ok_name = i+1<args.length?args[i+1]:"我确定";
    var cancel_name = i+2<args.length?args[i+2]:"取消";

    var SM = new SimpleModal({'btn_ok' : ok_name,'btn_cancel' : cancel_name });
    openDialogMask(SM);
    SM.show({
        "title": title,
        "model":"confirm",
        "callback": function(){
            if( callback_confirm )
            {
                callback_confirm(true);
                callback_confirm = null;
            }
			closeDialogMask();
        },
        "cancelback": function(){
            if( callback_confirm )
            {
                callback_confirm(false);
                callback_confirm = null;
            }
      	  	closeDialogMask();
        },
        "closeback": function(){
      		closeDialogMask();
        },
        "contents": contents
    });
    var h = _windowHeight;
    var w = _windowWidth;
    var div = document.getElementById('simple-modal');
    var top = h/2 - div.scrollHeight/2;
    SM.options.offsetTop = top;
    SM._display();
}
/*显示提示对话框*/
function skit_message()
{
    var args = skit_message.arguments;
    var content = args.length>0?args[0]:"";
    var title = args.length>1?args[1]:"信息";
    title = title.length> 15 ? (title.substring(0, 32)+"...") : title;
    var height = args.length>2?Number(args[2]):200;
    var width = args.length>3?Number(args[3]):480;
    callback_confirm = args.length>4?args[4]:null;
    var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消","width":width,"hideFooter":callback_confirm?false:true});
    openDialogMask(SM);
    width -= 32;
    if( content.indexOf("\n") != -1 )
        content = "<pre id='skitMsgDlg' style='width:"+width+"px;height:"+height+"px'>"+content+"</pre>";
    else
        content = "<div id='skitMsgDlg' style='height:"+height+"px;width:"+width+"px;border:0px solid red;'>"+content+"</div>"
    
    SM.show({
      "title":title,
      "callback": function(){
        if( callback_confirm )
        {
            callback_confirm(true);
            callback_confirm = null;
        }
  	  	closeDialogMask();
      },
      "cancelback": function(){
    	  closeDialogMask();
      },
      "closeback": function(){
    	  closeDialogMask();
      },
      "contents": content
    });
    
    var h = _windowHeight;
    var w = _windowWidth;
    var div = document.getElementById('simple-modal');
    var top = h/2 - div.scrollHeight/2;
    SM.options.offsetTop = top;
    SM.options.width = width;
    $( '#skitMsgDlg' ).niceScroll({cursorcolor: '#fff',railalign: 'right', cursorborder: "none", horizrailenabled: false, zindex: 2001, left: '245px', cursoropacitymax: 0.6, cursorborderradius: "0px", spacebarenabled: false });
}

/*显示提示对话框*/
function skit_frame(url, title, height, width, nicescroll, ok_callback, ok_name, cancel_callback, cancel_name)
{
    /*var args = skit_frame.arguments;
    var url = args.length>0?args[0]:"";
    var title = args.length>1?args[1]:"信息";
    //title = title.length> 15 ? (title.substring(0, 32)+"...") : title;
    var height = args.length>2?args[2]:200;
    var width = args.length>3?args[3]:480;
    var ok_callback = args.length>4?args[4]:null;
    var nicescroll = args.length>5?args[5]:false;
    nicescroll = !nicescroll?callback==null:nicescroll;*/
    width = width?width:_windowWidth - 64;
    height = height?height:_windowHeight - 64;
    if(!ok_name) ok_name = "确定";
    if(!cancel_name) cancel_name = "取消";
    var hideFooter = ok_callback?false:true;
    var cfg = {"btn_ok":ok_name,"btn_cancel":cancel_name,"width":width,"hideFooter":hideFooter};
    var SM = new SimpleModal(cfg);
    //alert(width+"x"+height);
    width -= 32;
    openDialogMask(SM);
    content = "<iframe id='skitDlgFrame' name='skitDlgFrame' src='"+url+"' style='border:0px solid #eee;height:"+height+"px;width:"+width+"px;'></iframe>"
    SM.show({
	  "model": ok_callback?"":"modal",
      "title":title,
      "callback": function(){
          if( ok_callback )
          {
        	  if( !ok_callback() ){
	       		  return false;
        	  }
          }
          closeDialogMask();
          return true;
      },
      "cancelback": function(){
          if( cancel_callback )
          {
        	  cancel_callback();
          }
          closeDialogMask();
      },
      "closeback": function(){
    	  closeDialogMask();
      },
      "contents": content
    });
    var h = _windowHeight;
    var div = document.getElementById('simple-modal');
    var top = h/2 - div.scrollHeight/2;
    SM.options.offsetTop = top;
    SM.options.width = width;
    SM._display();
    if( nicescroll )
    	$( '#skitDlgFrame' ).niceScroll({cursorcolor: '#fff',railalign: 'right', cursorborder: "none", horizrailenabled: false, zindex: 2001, left: '245px', cursoropacitymax: 0.6, cursorborderradius: "0px", spacebarenabled: false });
}

function editJson(data, title, w, h, callback){
	var json = JSON.stringify(data);
	document.getElementById( 'iEditor' ).value = json;
	document.forms[0].method = "POST";
	document.forms[0].action = "editor!json.action";
	skit_frame("", title, h, w, false, function(){
		try{
		    var ifr = window.frames["skitDlgFrame"];
		    if( ifr && ifr.getValue )
		    {
		    	var editjson = ifr.getValue();
				jQuery.parseJSON(editjson);
				callback(editjson);
		    }
		    else{
		    	skit_alert("JSON编辑器未打开", "错误提示");
		    }
		}
		catch(e){
	    	skit_alert("编写的JSON脚本可能格式错误:"+e, "错误提示");
		}
	    return true;
	});
	document.forms[0].target = "skitDlgFrame";
	document.forms[0].submit();
}

function showText(txt, title, w, h){
	if( typeof txt == "object" ){
		txt = JSON.stringify(txt);
	}
	title = title?title:"显示文本数据";
	w = w?w:_windowWidth - 256;
	h = h?h:_windowHeight - 256;
	document.getElementById( 'iEditor' ).value = txt;
	document.forms[0].method = "POST";
	document.forms[0].action = "editor!text.action";
	skit_frame("", title, h, w, false);
	document.forms[0].target = "skitDlgFrame";
	document.forms[0].submit();
}

function showJson(data, title, w, h){
	var json = data;
	if( typeof data == "object" ){
		json = JSON.stringify(data);
	}
	title = title?title:"显示JSON对象结构";
	w = w?w:_windowWidth - 256;
	h = h?h:_windowHeight - 256;
	document.getElementById( 'iEditor' ).value = json;
	document.forms[0].method = "POST";
	document.forms[0].action = "editor!json.action";
	skit_frame("", title, h, w, false);
	document.forms[0].target = "skitDlgFrame";
	document.forms[0].submit();
}

function showJavascript(js, title, w, h){
	document.getElementById( 'iEditor' ).value = js;
	document.forms[0].method = "POST";
	document.forms[0].action = "editor!javascript.action?ww="+(w-32);
	skit_frame("", title, h, w, false);
	document.forms[0].target = "skitDlgFrame";
	document.forms[0].submit();
}
/**
 * 编辑JS脚本，
 */
function editJavascript(js, title, w, h, savecallback, continuecallback){
	document.getElementById( 'iEditor' ).value = js;
	document.forms[0].method = "POST";
	document.forms[0].action = "editor!javascript.action?ww="+(w-32);
	if(continuecallback){
		skit_frame("", title, h, w, true, function(){
		    var ifr = window.frames["skitDlgFrame"];
		    if( ifr && ifr.getScript ){
		    	var editjs = ifr.getScript();
		    	savecallback(editjs);
		    }
		    else{
		    	skit_alert("JAVASCRIPT编辑器未打开", "错误提示");
		    }
		    return true;
		}, "确定", function(){
			continuecallback();
		}, "继续运行脚本");
	}
	else{

		skit_frame("", title, h, w, true, function(){
		    var ifr = window.frames["skitDlgFrame"];
		    if( ifr && ifr.getScript ){
		    	var editjs = ifr.getScript();
		    	savecallback(editjs);
		    }
		    else{
		    	skit_alert("JAVASCRIPT编辑器未打开", "错误提示");
		    }
		    return true;
		}, "确定");
	}
	document.forms[0].target = "skitDlgFrame";
	document.forms[0].submit();
}

/**
 * 添加用户
 */
function addSysuser(title, parameters, savecallback){
	document.forms[0].method = "POST";
	document.forms[0].action = "user!preset.action?"+parameters;
	skit_frame("", title, _windowHeight-192, 640, true, function(){
	    var ifr = window.frames["skitDlgFrame"];
	    if( ifr && ifr.setUser ){
			ifr.setUser(function(tips, succeed, account, name, ts, json){
				if( succeed ){
					savecallback(tips, succeed, account, name);
		            closeDialogMask();
				}
				else{
					alert(tips);
				}
			});
			return false;
	    }
	    else{
	    	alert("新增用户未打开");
	    }
	    return true;
	}, "保存用户配置", function(){
	}, "取消");
	document.forms[0].target = "skitDlgFrame";
	document.forms[0].submit();
}

function showObject(obj)
{
	showJson(obj, "显示对象数据", 1024, 480);
}
/*显示提示对话框*/
var callback_alert;
function skit_alert()
{
    var args = skit_alert.arguments;
    var message = args[0];
    var title = args.length>1?args[1]:"提示";
    callback_alert = args.length>2?args[2]:null;
    var SM = new SimpleModal({"btn_ok":"确定","btn_cancel":"取消"});
    openDialogMask(SM);
    SM.show({
        "title":title,
        "callback": function(){
            closeDialogMask();
        },
        "closeback": function(){
      	  closeDialogMask();
        },
        "contents": "<p>"+message+"</p>"
    });
    
    var h = _windowHeight;
    var w = _windowWidth;
    var div = document.getElementById('simple-modal');
    var top = h/2 - div.scrollHeight/2;
    SM.options.offsetTop = top;
    SM._display();    
}

function skit_error()
{
	var args = skit_error.arguments;
	if( args.length == 2 ){
		skit_alert(args[0], "错误提示", args[1]);
	}
	else{
		skit_alert(args[0], "错误提示");
	}
}

function skit_progress(title, callback_abort)
{
    var SM = new SimpleModal({"btn_ok":"中止","btn_cancel":"取消","width":640});
    SM.show({
        "model":"confirm",
        "callback": function(){
            if( callback_abort )
            {
            	callback_abort(true);
            }
        },
        "cancelback": function(){
        },
        "title": title,
        "contents": "<iframe src='helper!progress.action' name='iProgress' id='iProgress' class='nonicescroll' style='width:600px;height:200px;border:0px;'></iframe>"
    });
    var h = _windowHeight;
    var w = _windowWidth;
    var div = document.getElementById('simple-modal');
    var top = h/2 - div.scrollHeight/2;
    SM.options.offsetTop = top;
    SM._display();
}

function skit_alert_close()
{
    if( curMenuItemId )
    {
        var ifr = window.frames[curMenuItemId];
        if( ifr && ifr.window && ifr.window.skit_close_alert )
        {
            ifr.window.skit_close_alert();
        }
    }
}

function skit_showMask(timeout)
{
    var onOverlayClickCallback = null;
    if( timeout == -1 )
    {
        onOverlayClickCallback = skit_hiddenMask;
        timeout = 999999;
    }
    else if( timeout ){} else
    {
        timeout = 7000;
    }
    $.blockUI({
         message: '<h2>...</h2>',
         css: {
             border: 'none', 
            padding: '15px', 
            background: 'none',
         },
         overlayCSS: { backgroundColor: '#FFF' },
         onOverlayClick: onOverlayClickCallback,
         timeout: timeout
       });    
}

function skit_hiddenMask()
{
    $.unblockUI();
}

function skit_showLoading(to)
{
    if( to ){}else{
        to = 1000;
    }
    $.blockUI({
         message: '<h2><i class="fa fa-spinner fa-spin"></i></h2>',
         css: {
             border: 'none', 
          padding: '15px', 
          background: 'none',
         },
         overlayCSS: { backgroundColor: '#FFF' },
         timeout: to 
    });
}

function skit_hiddenLoading()
{
    $.unblockUI();
}

//设置模版调试器uiMenuDebugSubgrid
function addSubgridDebugMenu(key, label, subgridobj)
{
	try{
		var id = "liSubgridDebugMenu"+key;
		if(document.getElementById(id)){
			return;
		}
		document.getElementById("liMenuDebugSubgrid").style.display = "";
		document.getElementById("liDividerMenuDebugInnerGrid").style.display = "";
		var uiMenuDebugSubgrid = document.getElementById("uiMenuDebugSubgrid");
		var colModel = subgridobj.colModel;
		var li = document.createElement("li");
		li.id = id;
		li.className = "dropdown-submenu";
		var html = "<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-external-link fa-fw'></i>"+label+"</a>"+
			"<ul class='dropdown-menu'>"+
	    	"<li><a title='' onclick='showJs(11,this, \""+key+"\", \""+label+"\")'><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑数据预处理脚本</a></li>";
		var rhtml = "";
	   	if( colModel )
	   	{
	   		var r, i;
	   		//将模板的单元格渲染器添加到菜单
	   		for(i = 0; i < colModel.length; i++){
	   			var col = colModel[i];
 				r = col.render;
 				if( r ){
 					var title = label+"["+col.title+"]";
 					rhtml += "<li><a title='' onclick='showJs(12, this, \""+key+"\", "+i+");'><i class='skit_fa_icon fa fa-magic fa-fw'></i>"+title+"</a></li>";
 				}
	   		}
	   	}
	   	if( rhtml ){
	   		html += "<li class='dropdown-submenu'>"+
	   			"<a title='' href='javascript:;'><i class='skit_fa_icon fa fa-edit fa-fw'></i>编辑单元格渲染器脚本</a>"+
				"<ul class='dropdown-menu'>"+rhtml+"</ul></li>";
	   	}
	   	html += "<li class='divider'></li>";
	   	html += "<li><a title='' onclick='viewObject(10, this, \""+key+"\", \""+label+"\")'><i class='skit_fa_icon fa fa-info fa-fw'></i>查看配置对象</a></li>";
	   	html += "<li><a title='' onclick='viewObject(11, this, \""+key+"\", \""+label+"\")'><i class='skit_fa_icon fa fa-code fa-fw'></i>查看数据对象</a></li>";
	   	html += "</ul>";
		li.innerHTML = html;
		uiMenuDebugSubgrid.appendChild(li);
	}
	catch(e){
		alert(e);
	}
}

function addRenderDebugMenu(title, i, j)
{
	var id = "liRenderDebugMenu"+i+"_"+j;
	if( document.getElementById(id) ){
		return;
	}
	document.getElementById("liMenuDebugSubgrid").style.display = "none";
	document.getElementById("liDividerMenuDebugInnerGrid").style.display = "none";
	document.getElementById("uiMenuDebugSubgrid").innerHTML = "";
	var ulMenuDebugRender = document.getElementById("ulMenuDebugRender");
	ulMenuDebugRender.innerHTML = "";
	var li = document.createElement("li");
	li.id = id;
	li.innerHTML = "<a onclick='showJs(2,this, "+i+","+j+");'><i class='skit_fa_icon fa fa-magic fa-fw'></i>"+title+"</a>";
	ulMenuDebugRender.appendChild(li);
}

function viewObject(type, li, i, j){
	var ifr = window.frames[curMenuItemId];
	if( ifr && ifr.viewObject )
	{
		ifr.viewObject(type, li.innerHTML, i, j);
	}
}

function showJs(type, li, i, j){
	var ifr = window.frames[curMenuItemId];
	if( ifr && ifr.editJavascript )
	{
		ifr.editJavascript(type, li.innerHTML, i, j);
	}
}

function getEncryptData(publickey, data)
{
	var encrypt = new JSEncrypt();
    encrypt.setPublicKey(publickey);
    return encrypt.encrypt(data);
}
/**
 * unicode编码转中文
 */
function decodeEncryption(str) 
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
//-->
</SCRIPT>
