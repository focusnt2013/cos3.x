<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response); %>
<html>
<head>
<meta charset="UTF-8"/>
<!--JQUERY UI files-->
<link href="<%=Kit.URL_PATH(request)%>skit/bootstrap/css/bootstrap.min.css" rel="stylesheet"/>
<link href="<%=Kit.URL_PATH(request)%>skit/css/font-awesome.min.css" rel="stylesheet"/>
<link href="<%=Kit.URL_PATH(request)%>skit/css/awesome-bootstrap-checkbox.css" rel="stylesheet"/>
<link href="<%=Kit.URL_PATH(request)%>skit/krajee/css/fileinput.css?v=1" rel="stylesheet"/>
<link href="<%=Kit.URL_PATH(request)%>skit/krajee/css/jquery-ui.css" rel="stylesheet"/>
<link href="<%=Kit.URL_PATH(request)%>skit/krajee/css/jquery-confirm.min.css" rel="stylesheet"/>
<link href="<%=Kit.URL_PATH(request)%>skin/defone/css/simplemodal.css" rel="stylesheet"/>
<link href="<%=Kit.URL_PATH(request)%>skit/css/jquery.datetimepicker.min.css" rel="stylesheet"/>

<script src="<%=Kit.URL_PATH(request)%>skit/krajee/js/jquery-3.3.1.min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/krajee/js/bootstrap.bundle.min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/krajee/js/fileinput.js?v=50"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/krajee/js/locales/zh.js?v=0"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/krajee/js/themes/fas/theme.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/krajee/js/themes/explorer-fas/theme.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/krajee/js/jquery-confirm.min.js?v=0"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/krajee/js/sortable.min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skin/defone/js/mootools-core-1.3.1.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skin/defone/js/simple-modal.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/grid/globalscript.js?v=1912241327"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/js/jsrsasign-rsa-min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/js/jquery.inputmask.min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/js/jquery.datetimepicker.full.js"></script>
<%=Kit.getDwrJsTag(request,"interface/FormMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<%@ include file="pqgrid-skit.inc"%>
<style type='text/css'>
body {
	overflow-x: hidden;
	overflow-y: auto;
	background: <ww:property value='bgcolor'/>
	font:14px/26px '微软雅黑','宋体',Arail;
	padding-left:20px;
	padding-right:20px;
}
.file-preview-object{
	display: flex;
	align-items: center;
	justify-content: center;
}
.form-check{
	display: inline-block;
	height:34px;
}
.form-uncheck{
	color:#a0a0a0;
	display:inline-block;
	height:34px;
}
.form-checkbox{
	border-right:0px;
	border-left:0px;
	padding-bottom: 0px;
	background-color:#fffff0;
}

.form-input{
	background-color:#fffff0;
}
.form-textarea{
	background-color:#fffff0;
	font-size:12px;
	letter-spacing: 0px;
}
.tooltip-inner{
    background-color: #fa6b7d;
}
.tooltip.bottom .tooltip-arrow{
    border-bottom-color: #fa6b7d;
}
.li-checkbox{
	float: left;
	width: 128px;
	height: 30px;
}
</style>
</head>
<body>
<div id='divDialogMask' onclick='closeDialogMask(this)' class='skit_mask' style='cursor:pointer;z-index: 999;' title='点击关闭弹窗'>
	<img src="images/icons/loading.gif" style="position:fixed;left:50%;top:50%;">
</div>
<script type="text/javascript">
var showBottom = <ww:property value='showBottom'/>;
var hasToolbar = <ww:property value='hasToolbar'/>;
var formData = {}, valueLabels = {};
var _formData;
var	w = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
var	h = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
var	screenWidth = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
var	screenHeight = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
var deleteConfirm = function(resolve, reject) {
    $.confirm({
    	title: "确认",
        content: "您确认提交请求删除该文件吗？",
        type: 'green',
        buttons: {   
            ok: {
                text: '确认',
                btnClass: 'btn-primary text-white',
                keys: ['enter'],
                action: function(){
                    resolve();
                }
            },
            cancel: {
                text: '取消',
            }
        }
    });
};

function callbackBeforeView(){
	try{
	    <ww:property value="beforeGridView" escape="false"/>
	}
	catch(e){
   		if( top && top.skit_alert) top.skit_alert("执行表格初始化回调出现异常"+e.message+", 行数"+e.lineNumber);
   		else alert("执行表格初始化回调出现异常"+e.message+", 行数"+e.lineNumber);
	}
	var _title;
	try
	{
		for(_title in openFileFunctions){
			var func = openFileFunctions[_title];
			func();
		}
	}
	catch(e){
		var err = "初始化文件上传模块【"+_title+"】脚本出现异常"+e.message+", 行数"+e.lineNumber;
		alert(err);
	}
	debugstr += debugtime("OK");
}

//得到随机码
var nonces = ["<ww:property value='nonce'/>"];
function getNonce(){
	FormMgr.getNonce({
		callback:function(nonce){
			nonces.push(nonce);
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("获得随机数失败："+err)}
	});
	var nonce = nonces.pop();
	return nonce;
}

function debugtime(step){
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
    return "\r\n\t["+hours+":"+minutes+":"+seconds+"] "+step;
}
var debugstr = debugtime("Begin");
var openFileFunctions = {};
var onBlurFunctions = {};
var onFocusFunctions = {};
openDialogMask();
</script>
<ww:if test='hasUeditor'>
<script src="<%=Kit.URL_PATH(request)%>skit/ueditor/ueditor.config.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/ueditor/ueditor.all.js?v=1910241143"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/ueditor/lang/zh-cn/zh-cn.js"></script>
</ww:if>
<ww:iterator value="forms" status="loop">
<ww:if test='title!=null'>
<div class="panel panel-default" id="panel_<ww:property value='title'/>">
	<div class="panel-heading" style='text-align:left;'>
		<span><ww:property value='title'/></span>
		<span style='padding-left:10px;color:#808080;font-size:11px;'><ww:property value='label'/></span>
	</div>
	<div class="panel-body">
		<%@ include file="inc/forms.inc"%>
		<ww:if test='src!=null'>
		<iframe src="<ww:property value='src'/>" id="i<ww:property value='title'/>" name='i<ww:property value='title'/>'
			style="width:100%;height:<ww:property value='height'/>px;border:0px solid #eee;"></iframe>
		</ww:if>
	</div>
</div>
</ww:if>
<ww:else>
<%@ include file="inc/forms.inc"%>
</ww:else>
</ww:iterator>
<script type="text/javascript">
debugstr += debugtime("Finish render forms and begin toolbar");
</script>
<div class="form-group" style='text-align:center'>
<ww:iterator value="toolbars" status="loop">
	<button id='btn_<ww:property value='label'/>' onclick="toolbar_<ww:property value='#loop.index'/>();formData=_formData?_formData:formData;" class="btn btn-<ww:property value='tooblarSize'/> btn-<ww:property value='btn'/> btn-action"><i class="fa <ww:property value='icon'/>"></i><ww:property value='label'/></button>
</ww:iterator>
</div>
</body>
<script src="<%=Kit.URL_PATH(request)%>skit/js/jquery-ui.min.js"></script>
<script src="<%=Kit.URL_PATH(request)%>skit/bootstrap/js/bootstrap.min.js"></script>
<SCRIPT TYPE="text/javascript">
debugstr += debugtime("Finish toolbar and begin javascript");
var grid_type = "grid_form";
function onfocusTrigger(i){
	$("#"+i.id).tooltip("hide");
	var onfocus = onFocusFunctions[i.id];
	if( onfocus ){
		onfocus(i);
	}
}
function onblurTrigger(i){
	if( !initialized ){
		top.skit_alert("页面初始化未完成，请稍后再试");
		return;
	}
	var onblur = onBlurFunctions[i.id];
	if( onblur ){
		onblur(i);
	}
	var b = document.getElementById("btnCheck-"+i.name);
	if( b ){
		var val = i.value.trim();
		var result = val?true:false;
		var o = dataColumns[i.name];
		try{
			if( o && o.validations ){
				for( var j = 0; j < o.validations.length; j++ ){
					v = o.validations[j];
					if( v.type == "subject" ){
				        if( !checkSubject(i, o.nullable) ) {
				        	result = false;break;
				        }
					}
					else if( v.type == "email" ){
				        if( !checkEmail(i, o.nullable) ) {
				        	result = false;break;
				        }
					}
					else if( v.type == "url" ){
				        if( !checkUrl(i, o.nullable) ) {
				        	result = false;break;
				        }
					}
					else if( v.type == "version" ){
				        if( !checkVersion(i, o.nullable) ) {
				        	result = false;break;
				        }
					}
					else if( v.type == "port" ){
				        if( !checkPort(i, o.nullable) ) {
				        	result = false;break;
				        }
					}
					else if( v.type == "host" || v.type == "ip" || v.type == "domain" ){
				        if( !checkHost(i, o.nullable) ) {
				        	result = false;break;
				        }
					}
					else if( v.type == "number" ){
				        if( !checkNumber(i, o.nullable, v.value?v.value:0) ) {
				        	result = false;break;
				        }
					}
					else if( v.type == "minLen" ){
						if( val.length < Number(v.value) ){
							if( !v.msg ) {
								i.msg = v.msg;
							}
							else{
								i.msg = o.title+"输入字符不能小于"+v.value+"个";
							}
				        	result = false;break;
						}
					}
					else if( v.type == "maxLen" ){
						if( val.length > Number(v.value) ){
							if( v.msg ) {
								i.msg = v.msg;
							}
							else{
								i.msg = o.title+"输入字符不能大于"+v.value+"个";
							}
				        	result = false;break;
						}
					}
					else if( v.type == "regexp" ){
				        if( !val.match(new RegExp(v.value)) ) {
							if( v.msg ) {
								i.msg = v.msg;
							}
							else{
								i.msg = o.title+"输入格式不正确";
							}
				        	result = false;break;
				        }
					}
				}
			}
			if( i.msg && !result ){
				$("#"+i.id).tooltip("show");
			}
		}
		catch(e){
			alert(e);
			result = false;
		}
		b.fontSize = "12px";
		b.className = result?"btn btn-success form-check":"btn btn-danger form-check";
		//b.innerHTML = result?"<i class='fa fa-check-square-o' style='font-size:12px;'></i>":"<i class='fa fa-edit' style='font-size:12px;'></i>";
	}
	return b;
}

function selectTrigger(i){
	var b = document.getElementById("btnCheck-"+i.name);
	if( b ){
		b.fontSize = "12px";
		b.className = "btn btn-success form-check";
		//b.innerHTML = "<i class='fa fa-check-square-o' style='font-size:12px;'></i>";
	}
	return b;
}

function checkboxTrigger(i){
	var b = document.getElementById("btnCheck-"+i.name);
	if( b ){
		var o = dataColumns[i.name];
		var hascheck = false;
		for(var j = 0; j < o.options.length; j++ ){
			var e = document.getElementById(o.options[j].label);
			if( e.checked ){
				hascheck = true;
				break;
			}
		}
		b.fontSize = "12px";
		b.className = hascheck?"btn btn-success form-check":"btn btn-danger form-check";
	}
	return b;
}

function radioTrigger(i){
	if( !i.checked ){
		i.checked = true;
		return;
	}
	var o = dataColumns[i.name];
	for(var j = 0; j < o.options.length; j++ ){
		var e = document.getElementById(o.options[j].label);
		if( i != e ){
			e.checked = false;
		}
	}
	var b = document.getElementById("btnCheck-"+i.name);
	if( b ){
		b.fontSize = "12px";
		b.className = "btn btn-success form-check";
		//b.innerHTML = "<i class='fa fa-check-square-o' style='font-size:12px;'></i>";
	}
	return b;
}

var gridxml = "<ww:property value='gridxml'/>";
var recIndx = "<ww:property value='id'/>";
function doSave(){
	try{
		var data = getFormData();
		var formJson = JSON.stringify(data);
		var columnsJson = JSON.stringify(dataColumns);
		openDialogMask();
		FormMgr.save(formData[recIndx]?1:0, gridxml, formJson, columnsJson,{
			callback:function(response){
				closeDialogMask();
				if( response.succeed ){
					callbackSave();
				}
				else{
					window.top.skit_alert(response.message, "错误提示");
				}
			},
			timeout:331000,
			errorHandler:function(err) {closeDialogMask();window.top.skit_alert("出现异常："+err)}
		});
	}
	catch(e){
		alert("执行保存出现异常"+e.message+", 行数"+e.lineNumber);
	}
}
//构建表单
function buildForm(){
	var o, v, e, val, i, j,k, b, ue;
	for(i=0; i < titleColumns.length; i++){
		o = titleColumns[i];
		val = formData[o.dataIndx];
		val = getDataValue(formData, o);

		if( o.type == "ueditor" ){
			ue = UE.getEditor(o.dataIndx);
			if( val ){
				ue.ready(function(){
					this.setContent(formData[this.containerId]);
				});
			}
			continue;
		}
		
		if( val != 0 && (val == "" || val == null) ){
			//alert(o.dataIndx+"("+o.type+"):"+val);
			if( o.type == "select" ){
				e = document.getElementById(o.dataIndx);
				if( e ){
					selectTrigger(e);
				}
			}
			continue;
		}
		if( o.type == "radio" ){
			//alert(o.dataIndx+": "+val);
			for( j = 0; j < o.options.length; j++ ){
				e = document.getElementById(o.options[j].label);
				if( e ){
					//alert(e.id+":"+e.value);
					if( e.value == val+"" ){
						e.checked = true;
						radioTrigger(e);
						break;
					}
				}
			}
		}
		else if( o.type == "checkbox" ){
			var args = val.split(",");
			for( j = 0; j < o.options.length; j++ ){
				e = document.getElementById(o.options[j].label);
				if( e ){
					for( k = 0; k < args.length; k++ ){
						if( e.value == args[k] ){
							e.checked = true;
							checkboxTrigger(e);
							break;
						}
					}
				}
			}
		}
		else if( o.type == "file" ){
			//id = "file-"+id;
		}
		else if( o.type == "select" ){
			e = document.getElementById(o.dataIndx);
			if( e ){
				for( j = 0; j < e.options.length; j++ ){
					if( e.options[j].value == val+"" ){
						e.options[j].selected = true;
						selectTrigger(e);
						break;
					}
				}
			}
			else{
				alert("Not found select "+o.dataIndx);
			}
		}
		else{
			e = document.getElementById(o.dataIndx);
			if( e ){
				e.value = val;
				if( o.type == 'password' )
				{
					e.value = "******";
				}
				onblurTrigger(e);
			}
		}
	}
}

function callbackBeforeCommit(){
	var i, j, e, id, o, v, val, key, ue, data = {};
	for(i=0; i < titleColumns.length; i++){
		o = titleColumns[i];
		id = o.dataIndx;
		if( o.type == "file" ){
			if( formData[id] ){
				data[id] = formData[id];
			}
			continue;
		}
		
		if( o.type == "ueditor" ){
			ue = UE.getEditor(id);
			data[id] = ue.getContent();
			continue;
		}

		if( o.type == "hidden" && formData[id] ){
			data[id] = formData[id];
			continue;
		}

		e = document.getElementById(id);
		if( e ){
	        val = e.value.trim();
			if( val && o.validations ){
				for( j = 0; j < o.validations.length; j++ ){
					v = o.validations[j];
					e.msg = v.msg;
					if( v.type == "subject" ){
				        if( !checkSubject(e, o.nullable) ) {
							skit_alert(e.msg, e);
							return null;
				        }
					}
					else if( v.type == "email" ){
				        if( !checkEmail(e, o.nullable) ) {
							skit_alert(e.msg, e);
							return null;
				        }
					}
					else if( v.type == "url" ){
				        if( !checkUrl(e, o.nullable) ) {
							skit_alert(e.msg, e);
							return null;
				        }
					}
					else if( v.type == "version" ){
				        if( !checkVersion(e, o.nullable) ) {
							skit_alert(e.msg, e);
							return null;
				        }
					}
					else if( v.type == "port" ){
				        if( !checkPort(e, o.nullable) ) {
							skit_alert(e.msg, e);
							return null;
				        }
					}
					else if( v.type == "host" || v.type == "ip" || v.type == "domain" ){
				        if( !checkHost(e, o.nullable) ) {
							skit_alert(e.msg, e);
							return null;
				        }
					}
					else if( v.type == "number" ){
				        if( !checkNumber(e, o.nullable, v.value?v.value:0) ) {
							skit_alert(e.msg, e);
							return null;
				        }
					}
					else if( v.type == "minLen" ){
						if( val.length < Number(v.value) ){
							if( v.msg ) {
								skit_alert(v.msg, e);
							}
							else{
								skit_alert(o.title+"输入字符不能小于"+v.value+"个", e);
							}
							return null;
						}
					}
					else if( v.type == "maxLen" ){
						if( val.length > Number(v.value) ){
							if( v.msg ) {
								skit_alert(v.msg, e);
							}
							else{
								skit_alert(o.title+"输入字符不能大于"+v.value+"个", e);
							}
							return null;
						}
					}
					else if( v.type == "regexp" ){
				        if( !val.match(new RegExp(v.value)) ) {
							if( v.msg ) {
								skit_alert(v.msg, e);
							}
							else{
								skit_alert(o.title+"输入格式不正确", e);
							}
							return null;
				        }
					}
				}
			}
			if( !o.nullable && o.type != "hidden" && !o.readonly ){
				onblurTrigger(e);
				if( val == "" ){
					skit_alert(o.title+"不允许为空", e);
					return null;
				}
			}
			data[o.dataIndx] = val;
		}
		else{//对于那种radio、checkbox情况
			if( o.type == "radio" ){
				for( j = 0; j < o.options.length; j++ ){
					e = document.getElementById(o.options[j].label);
					if( e.checked ){
						data[o.dataIndx] = o.options[j].value;
						break;
					}
				}
				var b = document.getElementById("btnCheck-"+o.dataIndx);
				if( b ){
					var result = j<o.options.length;
					b.fontSize = "12px";
					b.className = result?"btn btn-success form-check":"btn btn-danger form-check";
					//b.innerHTML = result?"<i class='fa fa-check-square-o' style='font-size:12px;'></i>":"<i class='fa fa-edit' style='font-size:12px;'></i>";
					if( !result ){
						skit_alert("请选择【"+o.title+"】", e);
						return null;
					}
				}
			}
			else if( o.type == "checkbox" ){
				v = "";
				for( j = 0; j < o.options.length; j++ ){
					e = document.getElementById(o.options[j].label);
					if( e.checked ){
						if( v ){
							v += ",";
						}
						v += o.options[j].value;
					}
				}
				data[o.dataIndx] = v;
				var b = document.getElementById("btnCheck-"+o.dataIndx);
				if( b ){
					b.fontSize = "12px";
					b.className = v?"btn btn-success form-check":"btn btn-danger form-check";
					//b.innerHTML = result?"<i class='fa fa-check-square-o' style='font-size:12px;'></i>":"<i class='fa fa-edit' style='font-size:12px;'></i>";
					if( !v ){
						skit_alert("请选择【"+o.title+"】", e);
						return null;
					}
				}
			}
		}
	}
	return data;
}

function getFormData(){
	return formData;
}

debugstr += debugtime("Begin init toolbar functions");
<ww:iterator value="toolbars" status="loop">
function toolbar_<ww:property value='#loop.index'/>(){
	_formData = formData;
	try
	{
   		var type = "<ww:property value='label'/>";
   		if( "commmit" == "<ww:property value='type'/>" || "commit" == "<ww:property value='type'/>" ){
   			if( !initialized ){
   				top.skit_alert("页面初始化未完成，请稍后再试");
   				return;
   			}
   			formData = callbackBeforeCommit();
			if( formData != null ){
				<ww:property value='javascript' escape='false'/>
				if( _formData[recIndx] ){
					formData[recIndx] = _formData[recIndx];
				}
				if( "<ww:property value='confirm'/>" ){
					top.skit_confirm("表单提交确认", "您是否确认提交当前表单", function(yes){
						if( yes ){
							doSave();
						}
					});
				}
				else{
					doSave();
				}
   			}
   		}
   		else{
   	   		<ww:property value='javascript' escape='false'/>
   		}
	}
	catch(e){
		var err = "执行工具栏按钮【<ww:property value='label'/>】脚本出现异常"+e.message+", 行数"+e.lineNumber;
		if( top && top.skit_alert) top.skit_alert(err);
		else alert(err);
	}
}
</ww:iterator>
<ww:if test='localData!=null&&!localData.equals("")'>
formData = "<ww:property value='localData'/>";
formData = unicode2Chr(formData);
formData = jQuery.parseJSON(formData);
</ww:if>
var titleColumns = <ww:property value="titleColumns" escape="false"/>;
var dataColumns = <ww:property value="dataColumns" escape="false"/>;
<ww:if test='filterModel.labels!=null&&!filterModel.labels.equals("")'>
valueLabels = <ww:property value="filterModel.labels" escape="false"/>;
</ww:if>

function callbackSave() {
	if( parent.closeDialog ){
	    parent.closeDialog(true);
	}
}
</SCRIPT>
<SCRIPT TYPE="text/javascript" id='globalscript'>
<ww:property value='javascript' escape='false'/>
var initialized = false;
$(document).ready(function(){
	initialized = true;
	try{
		if( parent.showDebuglog ){
    		parent.showDebuglog(true);
    	}
		buildForm();
		closeDialogMask();
	}
	catch(e){
		alert(e);		
	}
});
callbackBeforeView();
</SCRIPT>
</html>