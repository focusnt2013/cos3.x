<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/ztree/css/zTreeStyle/zTreeStyle.css" rel="stylesheet"/>
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<link href="skin/defone/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
<style type='text/css'>
</style>
<script src="<%=Kit.URL_PATH(request)%>skit/js/jsrsasign-rsa-min.js"></script>
<%=Kit.getDwrJsTag(request,"interface/DiggConfigMgr.js")%>
<%=Kit.getDwrJsTag(request,"interface/DiggMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<SCRIPT type="text/javascript">
function debugProgress()
{
	DiggMgr.getDiggProgress({
		callback:function(response){
			if( response.succeed )
			{
				if( response.result == 200 )
				{
					if( debugExport ){
						window.top.skit_alert(response.message, "下载成功提示");
					}
					setDebugProgress(100, response.message);
					window.setTimeout("closeDebugProgress()",1000);
				}
				else
				{
					setDebugProgress(response.result, response.message);
					window.setTimeout("debugProgress()",500);
				}
			}
			else
			{
				skit_alert(response.message, "错误提示");
			}
		},
		timeout:10000,
		errorHandler:function(err) {skit_alert("获取接口调用进度出现异常："+err)}
	});
}

function stopDebugExport()
{
	DiggMgr.stopExport({
		callback:function(response){
			if( response.succeed )
			{//表示正在执行升级
				skit_alert(response.message);
			}
			else
			{
				skit_alert(response.message, "错误提示");
			}
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("出现异常："+err)}
	});	
}

//设置调用的进度
function setDebugProgress(result, message)
{
	var ifr = window.frames["iProgress"];
	if( ifr && ifr.setProgress )
	{
		ifr.setProgress(result, message);
	}
}

function setSecurityScript()
{
	var ifr = window.frames["iSecurityJs"];
	if( ifr && ifr.setValue && securityjs )
	{
		var js = securityjs;
		js = js.replace("&gt;", ">");
		js = js.replace("&lt;", "<");;
		js = js.replace("&gt;", ">");
		js = js.replace("&lt;", "<");
		ifr.setValue(js);
	}
	ifr = window.frames["iSecurityJava"];
	if( ifr && ifr.setValue && securityjs )
	{
		ifr.setValue(securityjava);
	}
	ifr = window.frames["iSecurityPython"];
	if( ifr && ifr.setValue && securitypython )
	{
		ifr.setValue(securitypython);
	}
}
//设置调用的进度
function closeDebugProgress()
{
	document.getElementById("iDebugResponse").style.display = "";
	document.getElementById("iProgress").style.display = "none";
}

var debugExport = false;
function doDebugExport(){
	document.getElementById("iDebugResponse").style.display = "none";
	document.getElementById("iProgress").style.display = "";
	document.getElementById("iProgress").src = "helper!progress.action";
	window.setTimeout("debugProgress()",1500);
	$("#tabDebug").tabs('select', '#tab-debug-response');
	debugExport = true;
	var method = document.getElementById("diggmethod").value;
	doMakeUrl();
	document.getElementById("filetype").value = "xls";
	if( method == "POST" ){
		var nodes = myZtree.getSelectedNodes();
		var template = nodes[0];
		var pq_filter = document.getElementById("pq_filter").value;
		document.getElementById("pq_filter").value = m_pq_filter;
		document.getElementById("gridxmlid").value = template.id;
		
		document.forms["formDebug"].action = "digg!apiexportdebug.action";
		document.forms["formDebug"].target = "iDebugResponse";
		document.forms["formDebug"].submit();
		document.getElementById("pq_filter").value = pq_filter;
	}
	else{
		var diggurl = document.getElementById("diggurl_get_export").value;
		diggurl = diggurl.replace("\diggexport", "\digg!apiexportdebug.action");
		document.getElementById("iDebugResponse").src = diggurl;
	}
	newTimestamp();
	newNonce();
	skit_confirm("是否停止测试下周excel数据接口",function(yes){
		if( yes ){
			stopDebugExport();
		}
	});
}

function doDebug(){
	debugExport = false;
	document.getElementById("iDebugResponse").style.display = "none";
	document.getElementById("iProgress").style.display = "";
	document.getElementById("iProgress").src = "helper!progress.action";
	window.setTimeout("debugProgress()",1500);
	$("#tabDebug").tabs('select', '#tab-debug-response');

	var method = document.getElementById("diggmethod").value;
	doMakeUrl();
	if( method == "POST" ){
		var nodes = myZtree.getSelectedNodes();
		var template = nodes[0];
		var pq_filter = document.getElementById("pq_filter").value;
		document.getElementById("pq_filter").value = m_pq_filter;
		document.getElementById("gridxmlid").value = template.id;
		
		
		document.forms["formDebug"].action = "digg!apidebug.action";
		document.forms["formDebug"].target = "iDebugResponse";
		document.forms["formDebug"].submit();
		document.getElementById("pq_filter").value = pq_filter;
	}
	else{
		var diggurl = document.getElementById("diggurl_get").value;
		diggurl = diggurl.replace("\diggapi", "\digg!apidebug.action");
		document.getElementById("iDebugResponse").src = diggurl;
	}
	newTimestamp();
	newNonce();
}

var securityjs = "";
var securityjava = "";
var securitypython = "";
var m_pq_filter = "";
function doMakeUrl(){
	var nodes = myZtree.getSelectedNodes();
	var template = nodes[0];
	var diggurl_get = "<%=Kit.URL_PATH(request)%>diggapi?gridxml="+template.id;
	var diggurl_post = diggurl_get;
	var diggurl_get_export = "<%=Kit.URL_PATH(request)%>diggexport?gridxml="+template.id;
	var diggurl_post_export = diggurl_get_export;
	var url = "";
	var url_token = "";
	try{
		var extra_param = document.getElementById("extra_param").value;
		if( extra_param ){
			url += "&"+extra_param;
		}
		var pq_curpage = document.getElementById("pq_curpage").value;
		url += "&pq_curpage="+pq_curpage;
		var pq_rpp = document.getElementById("pq_rpp").value;
		url += "&pq_rpp="+pq_rpp;
		var pq_filter = document.getElementById("pq_filter").value;
		if( pq_filter ){
			var json = JSON.parse(pq_filter);
			pq_filter = "";
			if( json.data.length > 0 ){
				for(var i = 0; i < json.data.length; i++ ){
					var e = json.data[i];
					if( !e.value ){
						json.data = removeArray(json.data,i);
						i -= 1;
					}
					else if( typeof e.value == "object" && e.value.length == 0 ) {
						json.data = removeArray(json.data,i);
						i -= 1;
					}
				}
				if( json.data.length > 0 ){
					pq_filter = JSON.stringify(json, null, 0);
				}
			}
			url += "&pq_filter="+pq_filter;
			m_pq_filter = pq_filter;
		}
		var pq_sort = document.getElementById("pq_sort").value;
		if( pq_sort ){
			pq_sort = JSON.stringify(JSON.parse(pq_sort));
			url += "&pq_sort="+pq_sort;
		}
		if( document.getElementById("divToken").style.display == "" ){
		    var ts = document.getElementById("ts").value;
		    var nonce = document.getElementById("nonce").value;
		    var token = document.getElementById("token").value;
		    var privatekey = document.getElementById("privatekey").value;
			var rsa = new RSAKey();
			var signstr = token+ts+nonce;
			rsa.readPrivateKeyFromPEMString(privatekey);
			var signature = linebrk(rsa.sign(signstr, "sha1"), 64);
			signature = signature.replace("\r", "");
			signature = signature.replace("\n", "");
			url_token += "&ts="+ts;
			url_token += "&nonce="+nonce;
			url_token += "&signature="+signature;
			privatekey = privatekey.replace(/[\n]/g, '"+\n"');
			privatekey = privatekey.substring(0, privatekey.lastIndexOf("+"));
			document.getElementById("signature").value = signature;
			var js = '//执行以下脚本进行数字签名';
			js += '\r\n//引用JS库&lt;script src="jsrsasign-rsa-min.js"&gt;&lt;/script&gt;';
			js += '\r\nvar token = "'+token+'";//访问令牌，客户端自己知道用于签名，不用提交到后台';
			js += '\r\nvar ts = "'+document.getElementById("ts").value+'";//时间戳字符串，可以是数字长整型的也可以是yyyyMMddHHmmssSSS格式';
			js += '\r\nvar nonce = "'+document.getElementById("nonce").value+'";//用户负责产生的随机数，一天内不能重复\r\n//从【我的系统接口开发安全管理】提取签名密钥';
			js += '\r\nvar privatekey = "'+privatekey+';';
			js += '\r\nvar rsa = new RSAKey();';
			js += '\r\nvar signstr = token+ts+nonce;';
			js += '\r\nrsa.readPrivateKeyFromPEMString(privatekey);';
			js += '\r\nvar signature = linebrk(rsa.sign(signstr, "sha1"), 64);';
			js += '\r\n//生成的签名: \r\n'+signature;
			js += '\r\n//#########使用生成的签名拼接成URL执行HTTP调用得到返回的接口数据';
			js += '\r\n//'+url;
			js += '\r\n//';
			securityjs = js;
			
			var java = '//JAVA编程实现签名';
			java += '\r\n//引用库bcpkix-jdk15on-158.jar';
			java += '\r\n//引用库bcprov-jdk15on-1.54.jar';
			java += '\r\n//import org.bouncycastle.openssl.PEMKeyPair;';
			java += '\r\n//import org.bouncycastle.openssl.PEMParser;';
			java += '\r\n... ...';
			java += '\r\n';
			java += '\r\nString privatekey = "'+privatekey+';';
			java += '\r\n//用PEM解析器加载私钥字符串';
			java += '\r\nPEMParser parser = new PEMParser(new StringReader(privatekey));';
			java += '\r\n//从解析器读取PEM密钥对';
			java += '\r\nPEMKeyPair pari = (PEMKeyPair)parser.readObject();';
			java += '\r\nparser.close();//关闭解析器';
			java += '\r\n//从密钥对中得到私钥码流';
			java += '\r\nbyte[] pk = pari.getPrivateKeyInfo().getEncoded();';

			java += '\r\n\r\n//准备签名';
			java += '\r\nString token = "'+token+'";';
			java += '\r\nString ts = "'+document.getElementById("ts").value+'";//时间戳字符串，可以是数字长整型的也可以是yyyyMMddHHmmssSSS格式';
			java += '\r\nString nonce = "'+document.getElementById("nonce").value+'";//用户负责产生的随机数，一天内不能重复\r\n//从【我的系统接口开发安全管理】提取签名密钥';
			java += '\r\nString signatureString = token+ts+nonce;//用Token时间戳随机数构建签名字符串';
			
			java += '\r\n//用私钥对签名字符串进行签名';
			java += '\r\nPKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(pk);';
			java += '\r\nKeyFactory keyFactory = KeyFactory.getInstance("RSA");';
			java += '\r\nPrivateKey privateKey2 = keyFactory.generatePrivate(pkcs8EncodedKeySpec);';
			java += '\r\nSignature signature = Signature.getInstance("SHA1WithRSA");';
			java += '\r\nsignature.initSign(privateKey2);';
			java += '\r\nsignature.update(signatureString.getBytes());';
			java += '\r\nbyte[] signautreResult = signature.sign();';
			java += '\r\nStringBuilder signautreResultString = new StringBuilder();//将签名结果转换成字符串';
			java += '\r\nfor (byte b : signautreResult) {';
			java += '\r\n\tString hexString = Integer.toHexString(0x00FF & b);';
			java += '\r\n\tsignautreResultString.append(hexString.length() == 1 ? "0" + hexString : hexString);';
			java += '\r\n}';
			_url = "<%=Kit.URL_PATH(request)%>diggapi?gridxml="+template.id;
			java += '\r\nString url = "'+_url+'";';
			java += '\r\nurl += "&pq_curpage=1";//请求第一页';
			java += '\r\nurl += "&pq_rpp=10";//一页取10条记录';
			java += '\r\nurl += "&ts="+ts;';
			java += '\r\nurl += "&nonce="+nonce;';
			java += '\r\nurl += "&signature="+signautreResultString;';
			if( !pq_filter ){
				pq_filter = "{}";
			}
			java += '\r\nJSONObject pq_filter = '+pq_filter+';//如果为空表示没有设置任何条件';
			var pq_sort = document.getElementById("pq_sort").value;
			if( !pq_sort ){
				pq_sort = "[]";
			}
			java += '\r\nJSONObject pq_sort = '+pq_sort+';//如果为空表示没有设置排序参数';
			java += '\r\nDocument doc = HttpUtils.post(url, null, ("pq_filter="+pq_filter.toString()+"&pq_sort="+pq_sort.toString()).getBytes("UTF-8"));';
			java += '\r\nString json = doc.text();';
			securityjava = java;
			var python = '//Python实现签名';
			python += '\r\nimport base64';
			python += '\r\nimport codecs';
			python += '\r\nimport binascii';
			python += '\r\nimport rsa';
			python += '\r\n';
			python += "\r\nprivate_key_data = '''";
			python += privatekey;
			python += "\r\n'''";
			python += '\r\n';
			python += '\r\ndef rsa_sign_sha1(sign_data):';
			python += '\r\nprivate_key = rsa.PrivateKey.load_pkcs1(private_key_data)';
			python += '\r\n# 私钥签名';
			python += "\r\nsignature = rsa.sign(sign_data.encode(), private_key, 'SHA-1')";
			python += '\r\nreturn binascii.b2a_hex(signature).decode()';
			python += '\r\n';
			python += "\r\nif __name__ == '__main__':";
			python += '\r\n';
			python += '\r\n#访问令牌，客户端自己知道用于签名，不用提交到后台';
			python += '\r\ntoken = "'+token+'"';
			python += '\r\n#时间戳字符串，可以是数字长整型的也可以是yyyyMMddHHmmssSSS格式';
			python += '\r\nts = "'+ts+'"';
			python += '\r\n#用户负责产生的随机数，一天内不能重复';
			python += '\r\nnonce = "'+nonce+'"';
			python += '\r\nsignstr = token+ts+nonce';
			python += '\r\nsign = rsa_sign_sha1(signstr)';
			securitypython = python;
			setTimeout("setSecurityScript()", 1000);
		}
	}
	catch(e){
		alert("调用函数[doMakeUrl]出现异常"+e);
	}
	document.getElementById("diggurl_get").value = diggurl_get+url+url_token;
	document.getElementById("diggurl_post").value = diggurl_post+url_token;
	document.getElementById("diggurl_get_export").value = diggurl_get_export+"&filetype=xls"+url+url_token;
	document.getElementById("diggurl_post_export").value = diggurl_post_export+"&filetype=xls"+url_token;
}
function removeArray(array, i){
	delete array[i];
	var n = [];
	for(var j=0;j < array.length;j++){
		var e1 = array[j];
		if( e1 ){
			n.push(e1);
		}
	}
	return n;
}
function selectMethod(m, skip){
	document.getElementById("diggmethod").value = m;
	setUserActionMemory("diggcfg!api.method."+sysid, m);
	if( !skip ){
		doMakeUrl();
	}
}

function setTemplateApiToken(account, token, privatekey){
	var apitoken = {};
	apitoken.account = account;
	apitoken.token = token;
	apitoken.privatekey = privatekey;
	setTemplateApi(apitoken);
}
function setTemplateApiRemark(){
	var apiremark = document.getElementById("apiremark").value;
	if( apiremark == "" ){
		return;
	}
	var nodes = myZtree.getSelectedNodes();
	var template = nodes[0];
	if( template.api && template.api.remark == apiremark ){
		return;
	}
	setTemplateApi();
}
//设置模板的API接口
function setTemplateApi(apitoken, status){
	try{
		status = status?status:0;
		var nodes = myZtree.getSelectedNodes();
		var template = nodes[0];
		var token = apitoken?apitoken:template.apitoken;
		var tips = "您要申请开通该模板的数据接口吗？"+(token?("接口访问配置了安全令牌【"+token.account+"】"):"");
		var security = "";
		if( apitoken ){
			template.apitoken = apitoken;
			security = apitoken.account;
		}
		else{
			if( token ){
				security = token.account;
			}
		}
		if( template.api ){
			if( status == 1 ){
				tips = "您要开通该模板的数据接口吗？接口访问配置了安全令牌【"+security+"】";
			}
			else if( status == 2 ){
				tips = "您要关闭该模板的数据接口吗？";
			}
			else {
				tips = "您要修改该模板的数据接口配置参数吗？";
			}
			status = status?status:template.api.status;
		}
		var apiremark = document.getElementById("apiremark").value;
		if( apiremark == "" ){
	    	document.getElementById( "apiremark" ).focus();
			$("#apiremark").tooltip("show");
			return;
		}
		skit_confirm(tips, function(yes){
			if( yes ) {
				DiggConfigMgr.setTemplateApi(template.id, apiremark, security, status, {
					callback:function(rsp) {
						skit_hiddenLoading();
						try
						{
							if( rsp.succeed ) {
								skit_alert(rsp.message);
								var json = rsp.result;
								template.api = JSON.parse(json);
								if( apitoken ){
									document.getElementById("privatekey").value = apitoken.privatekey;
									document.getElementById("token").value = apitoken.token;
									document.getElementById("divToken").style.display = "";
									doMakeUrl();
									$("#tabL").tabs('select', "#tabL-debug");
								}
								else{
									template.apitoken = template.api.settoken;
									open(template);
								}
								if( status == 1 ){
									template.icon = "images/icons/selected.png";
								}
								else if( status == 2 ){
									template.icon = "images/icons/abort.png";
								}
								myZtree.updateNode(template);
							}
							else{
								skit_error(rsp.message);
							}
						}
						catch(e)
						{
							skit_alert("配置模板的API接口参数操作出现异常"+e);
						}
					},
					timeout:930000,
					errorHandler:function(message) {skit_hiddenLoading(); skit_alert(message); }
				});
			}
			else{
				autoSwitch = true;
				$('#divSwitchApi').bootstrapSwitch('setState', status==1?false:true);
				autoSwitch = false;
				if( apitoken ){
					document.getElementById("tab-security").src = "digg!query.action?gridxml=/grid/local/diggapisecurity.xml";
					if( apitoken ){
						delete template.apitoken;
						if( template.api ){
							if( template.api.token ){
								template.apitoken = template.api.token;
							}
							if( template.api.settoken ){
								template.apitoken = template.api.settoken;
							}
						}
					}
					open(template);
				}
			}
		});
	}
	catch(e){
		alert("调用函数[setTemplateApi]出现异常"+e);
	}
}
function newTimestamp(){
	DiggConfigMgr.getTs({
		callback:function(ts){
			document.getElementById("ts").value = ts;
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("获得时间戳失败："+err)}
	});
}
function newNonce(){
	DiggConfigMgr.getNonce({
		callback:function(nonce){
			document.getElementById("nonce").value = nonce;
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("获得随机数失败："+err)}
	});
}
</SCRIPT>
</head>
<body style='overflow-y:hidden;padding-top:0px;padding-left:0px;'>
<TABLE style='width:100%'><TR class='unline'>
	<TD width='250' valign='top' id='tdTree'>
	<div class="panel panel-default" style='border: 1px solid #9f9f9f'>
		<div class="panel-heading">
			<span id='spanTitle'><i class='skit_fa_btn fa fa-universal-access'></i>模板接口开发管理</span>
	    </div>
		<div class="panel-body" style='padding: 0px;' id='divTree'>
			<ul id='myZtree' class='ztree'></ul>
		</div>
	</div>
	</TD>
	<TD style='padding-left:3px;' valign='top'>
		<div id='divDashboard'>
			<div class="well profile" id='divDashboardProfile' 
				style='margin-left:6px;margin-top:6px;margin-bottom:6px;padding-bottom:6px;'>
			    <div class="col-sm-12">
			        <div class="col-xs-12 col-sm-10" style='padding-left:0px;'>
			        	<h3 style='margin-top:0px;'>元数据模板接口开发管理</h3>
			            <p><i class="fa fa-info-circle fa-fw text-muted"></i>
			               <span>通过配置开通GRID-DIGG元数据模板的数据接口，对第三方应用提供数据服务。</span></p>
			        </div>
			        <div class="col-xs-12 col-sm-2 text-center">
			            <figure>
			             	<h2 style='margin-top:0px;' id='h2ApiStatus'>0/0</h2>
			                <figcaption class="ratings" style='margin-top:0px;'>
			                    <p>接口开通情况</p>
			                </figcaption>
			            </figure>
			        </div>
			    </div>
			</div>		
	 		<iframe name='iTempaltes' id='iTempaltes' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:3px;margin-bottom:3px;'></iframe>
		</div>
		<div id="tabL" style="position:relative;display:none;">
			<ul>
				<li><a href="#tabL-config"><i class='fa fa-cogs'></i> 接口配置</a></li>
				<li><a href="#tabL-debug"><i class='fa fa-bug'></i> 接口调测</a></li>
				<li><a href="#tabL-fields"><i class='fa fa-table'></i> 接口字段</a></li>
				<li><a href="#tabL-xml"><i class='fa fa-code'></i> 模板脚本</a></li>
				<li><a href="#tabL-version"><i class='fa fa-paw'></i> 模板版本</a></li>
				<li><a href="#tabL-history"><i class='fa fa-history'></i> 调用情况</a></li>
			</ul>
			<table style='width:100%' id='tabL-debug'><tr>
				<td valign='top' style='width:384px;padding-left:6px;padding-right:5px;'>
					<form id='formDebug' name='formDebug' style='margin-bottom:0px;'>
						<input type='hidden' name='filename' id='filename' value='测试导出'>
						<input type='hidden' name='filetype' id='filetype' value='xls'>
						<input type='hidden' name='signature' id='signature'>
						<div class="panel panel-default" style='margin-bottom:5px;'>
							<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-flask'></i> 接口参数</div>
							<div class="panel-body" style='padding-top:5px;padding-bottom:5px;padding-left:10px;padding-right:10px;'>
								<div class="form-group" style='margin-bottom: 10px;'>
					     			<div class="input-group" style='width:128px;'>
										<span class="input-group-addon input-group-addon-font">调用方式
											<i class='fa fa-file-code-o' style='margin-left:6px'></i></span>
										<input class="form-control form-control-font" type="text" id='diggmethod'
											value='POST' style="width:64px;font-size:12px;background:#ffe4b5;">
			        					<div class="input-group-btn">
			                                <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false"
			                                	style='height:34px;'><span class="caret"></span></button>
			                                <ul class="dropdown-menu pull-left" style='width:60px;cursor:pointer'>
			                                    <li><a onclick='selectMethod("POST");'>POST</a></li>
			                                    <li><a onclick='selectMethod("GET");'>GET</a></li>
			                                </ul>
			                            </div>
									</div>
								</div>
								<div class="form-group" style='margin-bottom: 10px;'>
					     			<div class="input-group">
										<span class="input-group-addon input-group-addon-font">数据类型
											<i class='fa fa-file-code-o' style='margin-left:6px'></i></span>
										<select class="form-control form-control-font" style='font-size:12px' id='datatype'>
											<option value='json'>application/json</option>
										</select>
									</div>
								</div>
								<div class="form-group" style='margin-bottom: 10px;'>
									<div class="input-group">
										<span class="input-group-addon input-group-addon-font">分页页面
											<i class='fa fa-file-code-o' style='margin-left:6px'></i></span>
										<input class="form-control form-control-font" type="text" id='pq_curpage' name='pq_curpage'
											value='1' style="font-size:12px;text-align:center;">
										<span class="input-group-addon input-group-addon-font" style='border-left: 1px solid #eee;border-right: 1px solid #eee;'
											>分页条数<span class='fa fa-list'></span></span>
										<select class="form-control form-control-font" style='width:96px;font-size:12px' id='pq_rpp' name='pq_rpp'>
											<option value='10'>10条/页</option>
											<option value='20'>20条/页</option>
											<option value='50'>50条/页</option>
											<option value='100'>100条/页</option>
											<option value='0'>不分页</option>
										</select>
									</div>
								</div>
							</div>
						</div>
						<div class="panel panel-default" style='margin-bottom:5px;display:none;' id='divToken'>
							<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-signing'></i> 安全参数</div>
							<div class="panel-body" style='padding-top:5px;padding-bottom:5px;padding-left:10px;padding-right:10px;'>
								<div class="form-group" style='margin-bottom: 10px;'>
									<div class="input-group" style='width:256px;'>
										<span class="input-group-addon input-group-addon-font">访问令牌
											<i class='fa fa-user-secret' style='margin-left:6px'></i></span>
										<input class="form-control form-control-font" type="text" id='token' name='token'
											value='' style="font-size:12px;background:#ffe4b5;width:144px;">
										<span class="input-group-addon input-group-addon-font">token</span>
									</div>
								</div>
								<div class="form-group" style='margin-bottom: 10px;'>
									<div class="input-group" style='width:256px;'>
										<span class="input-group-addon input-group-addon-font">时间戳
											<i class='fa fa-clock-o' style='margin-left:17px'></i></span>
										<input class="form-control form-control-font" type="text" id='ts' name='ts'
											value='<ww:property value="ts"/>' style="font-size:12px;width:144px;">
										<span class="input-group-addon input-group-addon-font">ts</span>
			        					<div class="input-group-btn">
			                                <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false"
			                                	style='height:34px;' onclick='newTimestamp()'><span class="caret"></span></button>
			                                <ul class="dropdown-menu pull-left" style='width:60px;cursor:pointer'>
			                                    <li><a>生成时间戳</a></li>
			                                </ul>
			                            </div>
									</div>
								</div>
								<div class="form-group" style='margin-bottom: 10px;'>
									<div class="input-group" style='width:288px;'>
										<span class="input-group-addon input-group-addon-font">随机数
											<i class='fa fa-random' style='margin-left:16px'></i></span>
										<input class="form-control form-control-font" type="text" id='nonce' name='nonce'
											value='<ww:property value="nonce"/>' style="font-size:12px;width:144px;">
										<span class="input-group-addon input-group-addon-font">nonce</span>
										<div class="input-group-btn">
			                                <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false"
			                                	style='height:34px;' onclick='newNonce()'><span class="caret"></span></button>
			                                <ul class="dropdown-menu pull-left" style='width:60px;cursor:pointer'>
			                                    <li><a>生成随机数</a></li>
			                                </ul>
			                            </div>
									</div>
								</div>
								<div class="form-group" style='margin-bottom: 10px;'>
									<textarea class="form-control form-control-font" rows="6"
										id="privatekey" style='color:#ffe4b5;background-color:#000;font-size:10px;'
									    data-title='没有读取到安全令牌的私钥数据'
										data-toggle="tooltip" 
										data-placement="bottom"
										data-trigger='manual'
										onkeydown='$("#privatekey").tooltip("hide")'
					                	placeholder="没有读取到安全令牌的私钥数据"></textarea>
								</div>
							</div>
						</div>
						<div class="panel panel-default" style='margin-bottom:5px;'>
							<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-filter'></i> URL扩展参数</div>
							<div class="panel-body" style='padding-top:5px;padding-bottom:5px;padding-left:10px;padding-right:10px;'>
								<textarea class="form-control form-control-font" rows="3"
									id="extra_param" style='color:#fff;background-color:#000;font-size:12px;'
				                	placeholder="请输入URL扩展参数用于接口传参"></textarea>
							</div>
						</div>
						<div class="panel panel-default" style='margin-bottom:5px;'>
							<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-filter'></i> 条件参数
		                        <div class="checkbox checkbox-info checkbox-inline pull-right">
		                        	<input type="checkbox" class="layout_switch" onclick="switchPqfilter(this)"
		                        		checked='checked' id='switch_pq_filter'><label for="switch_pq_filter"></label>
		                        </div>
							</div>
							<div class="panel-body" style='padding-top:5px;padding-bottom:5px;padding-left:10px;padding-right:10px;'>
								<textarea class="form-control form-control-font" rows="8"
									name='pq_filter' id="pq_filter" style='color:#fff;background-color:#000;font-size:12px;'
								    data-title='请按照条件过滤的JSON格式要求填写正确的过滤配置'
									data-toggle="tooltip" 
									data-placement="bottom"
									data-trigger='manual'
									onkeydown='$("#pq_filter").tooltip("hide")'
				                	placeholder="单元格没有配置过滤选项"></textarea>
							</div>
						</div>
						<div class="panel panel-default" style='margin-bottom:5px;'>
							<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-sort-alpha-asc'></i> 排序参数
		                        <div class="checkbox checkbox-info checkbox-inline pull-right">
		                        	<input type="checkbox" class="layout_switch" onclick="switchPqsort(this)"
		                        		checked='checked' id='switch_pq_sort'><label for="switch_pq_sort"></label>
		                        </div>
							</div>
							<div class="panel-body" style='padding-top:5px;padding-bottom:5px;padding-left:10px;padding-right:10px;'>
								<textarea class="form-control form-control-font" rows="2" name='pq_sort'
									id="pq_sort" style='color:#fff;background-color:#000;font-size:12px;'
								    data-title='请按照排序参数的JSON格式要求填写正确的排序配置'
									data-toggle="tooltip" 
									data-placement="bottom"
									data-trigger='manual'
									onkeydown='$("#pq_sort").tooltip("hide")'
				                	placeholder="单元格没有配置排序选项"></textarea>
							</div>
						</div>
						<input type='hidden' name='gridxmlid' id='gridxmlid' value=''>
					</form>
					<table style='width:100%;'><tr><td>
					<tr><td align='center' style='padding-top:5px;'>
						<button type="button" class="btn btn-info" onclick='doMakeUrl()' style='font-size:12px;'
						><span class="fa fa-external-link"></span> 生成接口URL </button>
						<button type="button" class="btn btn-danger" onclick='doDebug()' style='font-size:12px;'
						><span class="fa fa-send"></span> 测试一下 </button>
						<button type="button" class="btn btn-success" onclick='doDebugExport()' style='font-size:12px;'
						><span class="fa fa-file"></span> 测试导出文件 </button>
						</td></tr>
					</table>
				</td>
				<td valign='top' style='padding-right:6px;'>
				<div id="tabDebug" style="position:relative;border:1px solid #cfcfcf;">
				<ul>
					<li><a href="#tab-debug-request"><i class='fa fa-cloud-upload'></i> 请求方法</a></li>
					<li><a href="#tab-debug-response"><i class='fa fa-cloud-download'></i> 数据响应</a></li>
					<li><a href="#tab-debug-log"><i class='fa fa-eye'></i> 调测报告</a></li>
					<li id='li-tab-debug-security-js'><a href="#tab-debug-security-js"><i class='fa fa-user-secret'></i> JS签名方法</a></li>
					<li id='li-tab-debug-security-java'><a href="#tab-debug-security-java"><i class='fa fa-user-secret'></i> JAVA签名方法</a></li>
					<li id='li-tab-debug-security-python'><a href="#tab-debug-security-python"><i class='fa fa-user-secret'></i> Python签名方法</a></li>
				</ul>
				<div id='tab-debug-request'>
					<div class="panel panel-default">
						<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-info'></i> GET方法调用接口URL</div>
						<div class="panel-body" style='padding-bottom:0px;'>
							<div class="form-group">
								<textarea class="form-control" rows="3"	id="diggurl_get" style='background:#000;color:#fff;font-size:12px;'></textarea>
							</div>
						</div>
					</div>
					<div class="panel panel-default">
						<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-info'></i> POST方法调用接口URL</div>
						<div class="panel-body" style='padding-bottom:0px;'>
							<div class="form-group">
								<textarea class="form-control" rows="3"	id="diggurl_post" style='background:#000;color:#fff;font-size:12px;'></textarea>
							</div>
						</div>
					</div>
					<div class="panel panel-default">
						<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-info'></i> GET方法调用导出接口URL 导出文件类型根据filetype控制</div>
						<div class="panel-body" style='padding-bottom:0px;'>
							<div class="form-group">
								<textarea class="form-control" rows="3"	id="diggurl_get_export" style='background:#000;color:#fff;font-size:12px;'></textarea>
							</div>
						</div>
					</div>
					<div class="panel panel-default">
						<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='skit_fa_btn fa fa-info'></i> POST方法调用导出接口URL 导出文件类型根据filetype控制</div>
						<div class="panel-body" style='padding-bottom:0px;'>
							<div class="form-group">
								<textarea class="form-control" rows="3"	id="diggurl_post_export" style='background:#000;color:#fff;font-size:12px;'></textarea>
							</div>
						</div>
					</div>
				</div>
				<div id='tab-debug-response'>
					<iframe name='iProgress' id='iProgress' class='nonicescroll' style='display:none;border:0px solid #eee;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
					<iframe name='iDebugResponse' id='iDebugResponse' class='nonicescroll' style='display:none;border:0px solid #eee;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
				</div>
				<iframe name='iDebugLog' id='tab-debug-log' class='nonicescroll' style='border:0px solid #eee;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
				<iframe src='editor!js.action' id='tab-debug-security-js' name='iSecurityJs' class='nonicescroll' 
					style='border:0px solid #eee;width:100%;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
				<iframe src='editor!js.action' id='tab-debug-security-java' name='iSecurityJava' class='nonicescroll' 
					style='border:0px solid #eee;width:100%;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
				<iframe src='editor!js.action' id='tab-debug-security-python' name='iSecurityPython' class='nonicescroll' 
					style='border:0px solid #eee;width:100%;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
				</div>
				</td>
			</tr></table>
			<div id='tabL-config'>
				<div id='divTempalteProfile' class="well profile" 
					style='margin-left:6px;margin-top:6px;margin-bottom:6px;padding-bottom:6px;'>
				    <div class="col-sm-12">
				        <div class="col-xs-12 col-sm-10" style='padding-left:0px;'>
				        	<h3 id='h2DiggTitle' style='margin-top:0px;'></h3>
				            <p><i class="fa fa-info-circle fa-fw text-muted"></i>
				               <span id='spanDiggName'>该查询是用于什么目的，用到了那些数据源。</span></p>
				            <p><strong><i class="fa fa-user fa-fw text-muted"></i> 开发者:  </strong>
				               <span class="tags" id="spanDigger"></span>
				               <strong><i class="fa fa-clock-o fa-fw text-muted"></i> 最后修改时间:  </strong>
				               <span class="tags" id="spanTimestamp">2017-06-24 11:14</span>
				            </p>
				            <p><strong><i class="fa fa-link fa-fw text-muted"></i> 接口地址:  </strong>
					        	<span id='spanDiggId'></span>
					        	<a onclick="copyApiUrl()" style='cursor:pointer' title='复制接口地址'><i class='skit_fa_icon_red fa fa-clipboard'></i></a> 
				            </p>
				            <p><strong><i class="fa fa-file-excel-o fa-fw text-muted"></i> 导出地址:  </strong>
					        	<span id='spanExportId'></span>
					        	<a onclick="copyExportUrl()" style='cursor:pointer' title='复制导出地址'><i class='skit_fa_icon_red fa fa-clipboard'></i></a> 
				            </p>
				        </div>
				        <div class="col-xs-12 col-sm-2 text-center">
				            <figure>
				                <i class="skit_fa_icon_red fa fa-toggle-off" style='font-size:48px;' id='iApiStatus'></i>
				                <figcaption class="ratings" style='margin-top:0px;'>
				                    <p id='pApiStatus'>接口未开通</p>
						            <p><div class="switch"
						            	id="divSwitchApi"
						            	style='height:32px;font-size:12px;' 
						            	data-on="info"
						            	data-off="danger"
						            	data-on-label="<i class='fa fa-check-square-o'></i>开通"
						            	data-off-label="<i class='fa fa-minus-circle'></i>禁用"
						            	title='开启或关闭模板开放数据接口'
						            	><input type="checkbox" id='iSwitchApi' checked/>
									</div></p>
						            <p><button type="button" class="btn btn-primary" onclick='setTemplateApi()'
						            	id='btnApiApply'><span class="fa fa-toggle-on"></span> 申请开通接口 </button></p>
				                </figcaption>
				            </figure>
				        </div>
				    </div>
				</div>
				<table style='width:100%'><tr>
					<td valign='top' style='width:280px;padding-left:6px;padding-right:5px;'>
					<div class="panel panel-default">
						<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-cogs'></i> 接口配置</div>
						<div class="panel-body" style='padding-top:5px;padding-bottom:5px;padding-left:10px;padding-right:10px;'>
							<div class="form-group" style='margin-bottom: 10px;'>
				     			<div class="input-group" style='display:'>
									<span class="input-group-addon input-group-addon-font" id='spanMode'>安全控制<i class='fa fa-globe' style='margin-left:6px'></i></span>
									<select class="form-control form-control-font" style='font-size:12px' id='apisecurity'
										onchange='changeApiSecurity(this)'>
										<option value='none'>无密钥访问</option>
										<option value='token'>安全令牌访问</option>
									</select>
								</div>
							</div>
							<div class="form-group" style='margin-bottom: 10px;'>
								<textarea class="form-control form-control-font" rows="2"
									id="apiremark" style='color:#66cccc;background-color:#eee;font-size:12px;'
								    data-title='请备注该接口的使用场景描述，以便于接口管理与维护'
									data-toggle="tooltip" 
									data-placement="bottom"
									data-trigger='manual'
									onkeydown='$("#apiremark").tooltip("hide")'
									onblur='setTemplateApiRemark()'
				                	placeholder="申请开通接口请备注该接口的使用场景描述，以便于接口管理与维护"></textarea>
							</div>
							<iframe src='digg!query.action?gridxml=/grid/local/diggapicode.xml' id='iCode' class='nonicescroll' style='border:0px solid #eee;width:248px;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
						</div>
					</div>
					</td>
					<td valign='top' style='padding-right:6px;'>
					<div id="tabConfig" style="position:relative;border:1px solid #cfcfcf;">
					<ul>
						<li><a href="#tab-datastyle"><i class='fa fa-file-text'></i> 数据格式</a></li>
						<li id='liSecurity' style='display:none'><a href="#tab-security"><i class='fa fa-user-secret'></i> 安全管理</a></li>
					</ul>
					<iframe name='iDataStyle' id='tab-datastyle' class='nonicescroll' style='border:0px solid #eee;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
					<iframe name='iSecurity' src='digg!query.action?gridxml=/grid/local/diggapisecurity.xml' id='tab-security' class='nonicescroll' style='border:0px solid #eee;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
					</div>					
					</td>
				</tr></table>
			</div>
			<iframe name='iXml' id='tabL-xml' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:0px;margin-bottom:3px;'></iframe>
			<iframe name='iVersion' id='tabL-version' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:0px;margin-bottom:3px;'></iframe>
			<iframe name='iHistory' id='tabL-history' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:0px;margin-bottom:3px;'></iframe>
			<iframe name='iFields' id='tabL-fields' class='nonicescroll' style='border:0px solid #eee;margin-left:3px;margin-top:0px;margin-bottom:3px;'></iframe>
		</div>	
	</TD>
</TR></TABLE>
<form id='form0' name='form0'>
<input type='hidden' name='editorContent' id='content'>
<input type='hidden' name='gridxml' id='gridxml'>
<input type='hidden' name='gridtext' id='gridtext'>
<input type='hidden' name='id' id='id'>
<input type='hidden' name='sysid' id='sysid' value="<ww:property value='sysid'/>">
</form>
</body>
<script type="text/javascript">
var grant = <ww:property value='grant'/>;//管理程序的权限
var sysid = "<ww:property value='sysid'/>";
function resizeWindow()
{
	var div = document.getElementById( 'divTree' );
	div.style.width = 248;
	div.style.height = windowHeight - 32;
	var w = div.clientWidth;
	
	div = document.getElementById("divDashboardProfile");
	div.style.width = windowWidth - 260;
	
	div = document.getElementById("iTempaltes");
	div.style.width = windowWidth - 256;
	div.style.height = windowHeight - 112;

	div = document.getElementById("tabL");
	div.style.width = windowWidth - 252;
	div.style.height = windowHeight;

	w += 384;
	div = document.getElementById("tabDebug");
	div.style.width = windowWidth - w - 32;
	div.style.height = windowHeight - 36;

	div = document.getElementById("formDebug");
	div.style.height = windowHeight - 72;

	w = windowWidth - w - 40;
	div = document.getElementById("iDebugResponse");
	div.style.height = windowHeight - 72;
	div.style.width = w;
	
	div = document.getElementById("iProgress");
	div.style.height = 192;
	div.style.width = w;
	div.style.marginTop = (windowHeight - 256)/2;

	div = document.getElementById("tab-debug-log");
	div.style.height = windowHeight - 72;
	div.style.width = w;

	div = document.getElementById("tab-debug-security-js");
	div.style.height = windowHeight - 72;
	div.style.width = w;

	div = document.getElementById("tab-debug-security-java");
	div.style.height = windowHeight - 72;
	div.style.width = w;
	
	div = document.getElementById("divTempalteProfile");
	div.style.width = windowWidth - 272;
	var h = div.clientHeight;

	div = document.getElementById("iCode");
	div.style.width = 272;
	div.style.height = windowHeight - h - 208;
	
	div = document.getElementById("tab-datastyle");
	div.style.width = windowWidth - 568;
	div.style.height = windowHeight - h - 84;

	div = document.getElementById("tab-security");
	div.style.width = windowWidth - 568;
	div.style.height = windowHeight - h - 84;
	
	div = document.getElementById("tabL-xml");
	div.style.width = windowWidth - 260;
	div.style.height = windowHeight - 32;

	div = document.getElementById("tabL-version");
	div.style.width = windowWidth - 260;
	div.style.height = windowHeight - 32;
	
	div = document.getElementById("tabL-history");
	div.style.width = windowWidth - 260;
	div.style.height = windowHeight - 32;
	
	div = document.getElementById("tabL-fields");
	div.style.width = windowWidth - 260;
	div.style.height = windowHeight - 32;
	
	h = windowHeight - 72;
	div = document.getElementById("tab-debug-request");
	div.style.height = h;
	
	div = document.getElementById("tab-debug-security-python");
	div.style.height = h;
	div.style.width = w - 10;
}
function setSecurityToken(account){
	var ifr = window.frames["iSecurity"];
	if( ifr && ifr.setApiToken ){
		ifr.setApiToken(account);
	}
}
//管理模式下打开节点
function open(node)
{
	setUserActionMemory("diggcfg!api."+sysid, node.id);
	if( "dir" == node.type )
	{
		document.getElementById( 'h2ApiStatus' ).innerHTML = node.countopen+"/"+node.count;
		document.getElementById("divDashboard").style.display = "";
		document.getElementById("tabL").style.display = "none";
		document.getElementById("id").value = node.id;
		document.forms["form0"].method = "POST";
		document.forms["form0"].action = "diggcfg!queryapi.action";
		document.forms["form0"].target = "iTempaltes";
		document.forms["form0"].submit();
	}
	else{
		document.getElementById("divDashboard").style.display = "none";
		document.getElementById("tabL").style.display = "";
		document.getElementById( 'spanDigger' ).innerHTML = node.developer;
		document.getElementById( 'h2DiggTitle' ).innerHTML = node.title+" <span class='tags' onclick='viewVersions()' title='点击显示版本详情' "+
			"style='font-size:11px;'>v"+node.version+"</span>";
		document.getElementById( 'spanDiggName' ).innerHTML = node.remark;
		document.getElementById( 'spanTimestamp' ).innerHTML = node.createtime;
		document.getElementById( 'spanDiggId' ).innerHTML = "<%=Kit.URL_PATH(request)%>diggapi?gridxml="+node.id;
		document.getElementById( 'spanExportId' ).innerHTML = "<%=Kit.URL_PATH(request)%>diggexport?gridxml="+node.id;
		
		if( node.pq_filter ){
			var textarea = document.getElementById( 'pq_filter' );
			textarea.value = JSON.stringify(node.pq_filter, null, 4 );
			var rows = node.pq_filter.data?node.pq_filter.data.length:0;
			if( rows > 0 ){
				rows *= 6;
				rows += 5;
				textarea.rows = rows;
			}
		}
		if( node.pq_sort ){
			var textarea = document.getElementById( 'pq_sort' );
			textarea.value = JSON.stringify(node.pq_sort, null, 4 );
			var rows = node.pq_sort.length;
			if( rows > 0 ){
				rows *= 4;
				rows += 1;
				textarea.rows = rows;
			}
		}
		var apitoken = node.apitoken;
		if( apitoken ){
			document.getElementById("privatekey").value = apitoken.privatekey;
			document.getElementById("token").value = apitoken.token;
			document.getElementById("divToken").style.display = "";
			setTimeout("setSecurityToken('"+apitoken.account+"');", 1000);
			document.getElementById("li-tab-debug-security-js").style.display = "";
			document.getElementById("li-tab-debug-security-java").style.display = "";
			document.getElementById("li-tab-debug-security-python").style.display = "";
		}
		else{
			document.getElementById("privatekey").value = "";
			document.getElementById("token").value = "";
			document.getElementById("divToken").style.display = "none";
			document.getElementById("li-tab-debug-security-js").style.display = "none";
			document.getElementById("li-tab-debug-security-java").style.display = "none";
			document.getElementById("li-tab-debug-security-python").style.display = "none";
		}
		doMakeUrl();
		if( node.api ){
			autoSwitch = true;
			switch(node.api.status){
			case 0:
				document.getElementById("btnApiApply").style.display = "none";
				document.getElementById("divSwitchApi").style.display = grant?"":"none";
				$('#divSwitchApi').bootstrapSwitch('setState', false);
				document.getElementById( 'iApiStatus' ).className = "skit_fa_icon_blue fa fa-toggle-off";
				document.getElementById( 'pApiStatus' ).innerHTML = node.api.tips?node.api.tips:"接口待开通";
				break;
			case 1:
				document.getElementById("btnApiApply").style.display = "none";
				document.getElementById("divSwitchApi").style.display = grant?"":"none";
				$('#divSwitchApi').bootstrapSwitch('setState', true);
				document.getElementById( 'iApiStatus' ).className = "fa fa-toggle-on";
				document.getElementById( 'pApiStatus' ).innerHTML = node.api.tips?node.api.tips:"接口已开通";
				break;
			case 2:
				document.getElementById("btnApiApply").style.display = "none";
				document.getElementById("divSwitchApi").style.display = "";
				$('#divSwitchApi').bootstrapSwitch('setState', false);
				document.getElementById( 'iApiStatus' ).className = "skit_fa_icon_red fa fa-toggle-off";
				document.getElementById( 'pApiStatus' ).innerHTML = node.api.tips?node.api.tips:"接口已关闭";
				break;
			}
			autoSwitch = false;
			if( node.api.token || node.api.settoken ){
				document.getElementById( 'apisecurity' ).options[1].selected = true;
			}
			else{
				document.getElementById( 'apisecurity' ).options[0].selected = true;
			}
			changeApiSecurity(document.getElementById( 'apisecurity' ));
			document.getElementById( 'apiremark' ).value = node.api.remark;
		}
		else{
			document.getElementById("btnApiApply").style.display = "";
			document.getElementById("divSwitchApi").style.display = "none";
			document.getElementById( 'iApiStatus' ).className = "skit_fa_icon_gray fa fa-toggle-off";
			document.getElementById( 'pApiStatus' ).innerHTML = "未初始化";
			document.getElementById( 'apisecurity' ).options[0].selected = true;
			changeApiSecurity(document.getElementById( 'apisecurity' ));
			document.getElementById( 'apiremark' ).value = "";
		}
		document.getElementById("gridxml").value = node.id;
		document.forms["form0"].method = "POST";
		document.forms["form0"].action = "diggcfg!datastyle.action";
		document.forms["form0"].target = "iDataStyle";
		document.forms["form0"].submit();

		document.getElementById("id").value = node.id;
		document.forms["form0"].action = "diggcfg!version.action";
		document.forms["form0"].target = "iVersion";
		document.forms["form0"].submit();
		
		DiggConfigMgr.getTemplateXml(node.id, {
			callback:function(rsp) {
				if( rsp.succeed ) {
					document.getElementById("content").value = rsp.result;
					document.forms["form0"].action = "editor!xml.action";
					document.forms["form0"].method = "POST";
					document.forms["form0"].target = "iXml";
					document.forms["form0"].submit();
					
					document.getElementById("tabL-history").src = "digg!query.action?gridxml=/grid/local/diggapiusage.xml&template="+node.id;
					document.getElementById("tabL-fields").src = "diggcfg!previewgrid.action?gridxml="+node.id;
				}
				else skit_error(rsp.message);
			},
			timeout:30000,
			errorHandler:function(err) {skit_hiddenLoading(); alert(err); }
		});
	}
	resizeWindow();
}

function changeApiSecurity(sel)
{
	if( sel.value == 'token' ){
		try{
			document.getElementById("liSecurity").style.display = "";
			$("#tabConfig").tabs('select', '#tab-security');
		}
		catch(e){
		}
	}
	else{
		try{
			document.getElementById("liSecurity").style.display = "none";
			$("#tabConfig").tabs('select', '#tab-datastyle');
		}
		catch(e){
		}
	}
}
</script>
<link href="skit/css/bootstrap.switch.css" rel="stylesheet">
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<script src="skit/js/jquery.md5.js"></script>
<script src="skit/js/bootstrap-tour.min.js"></script>
<script src="skit/js/jquery.inputmask.bundle.min.js"></script>
<script src="skit/js/bootstrap.switch.js"></script>
<style type='text/css'>
body {
}
.btn.btn-outline.btn-primary:hover {
    background-color: #fff;
    border: 1px solid #455a64;
    color: #455a64;
}
.btn.btn-primary:hover {
    background-color: #5a7582;
}
.btn.btn-primary {
    background-color: #455a64;
    border-color: #455a64;
}
.btn-primary:hover {
    color: #fff;
    background-color: #286090;
    border-color: #204d74;
}
.panel.panel-primary .panel-heading {
    background-color: #455a64;
    border-bottom: 2px solid #1b2428;
    color: #fff;
}
.panel .panel-heading {
    font-size: 12px;
    font-family: "微软雅黑",sans-serif;
    padding: 5px 10px;
	border-bottom: 1px solid transparent;
	border-top-left-radius: 3px;
	border-top-right-radius: 3px;
}
.panel-title {
    font-size: 12px;
    font-family: "微软雅黑",sans-serif;
    display:block;
    width:310px;
    word-break:keep-all;
    white-space:nowrap;
    overflow:hidden;
    text-overflow:ellipsis;
}
.panel .panel-menu {
    float: right;
    right: 30px;
    top: 8px;
    font-weight: 100;
}
.profile span.tags {
    background: <%=(String)session.getAttribute("System-Theme")%>;
    border-radius: 2px;
    color: #fff;
    font-weight: 700;
    padding: 2px 4px;
}
.input-group-addon {
    padding: 6px 12px;
    font-size: 12px;
    font-weight: 400;
    line-height: 1;
    color: #555;
    text-align: center;
    background-color: #eee;
    border: 1px solid #ccc;
        border-right-width: 1px;
        border-right-style: solid;
        border-right-color: rgb(204, 204, 204);
    border-radius: 4px;
        border-top-right-radius: 4px;
        border-bottom-right-radius: 4px;
}
.xml_edit
{
	width: 100%;
	font-size: 12px;
	font-family:微软雅黑,Roboto,sans-serif;
	margin:0 0 1px;
	color: #ffffff;
	background-color: #373737;
}
.ui-tabs .ui-tabs-nav {
    margin: 0;
    padding-top: 2px;
    padding-right: 1px;
    padding-bottom: 0px;
    padding-left: 1px;
}
.ui-tabs .ui-tabs-nav li a {
    float: left;
    padding-top: 3px;
    padding-right: 5px;
    padding-bottom: 3px;
    padding-left: 5px;
    text-decoration: none;
    font-size: 8pt;
}
.ui-tabs .ui-tabs-panel {
    display: block;
    border-width: 0;
    padding-top: 3px;
    padding-right: 1px;
    padding-bottom: 1px;
    padding-left: 1px;
    background: none;
}
</style>
<SCRIPT type="text/javascript">
$("#tabL").tabs({
    select: function(event, ui) {
    	setUserActionMemory("diggcfg!api1."+sysid, ui.panel.id);
    }
});
$("#tabConfig").tabs({
    select: function(event, ui) {
    }
});
$("#tabDebug").tabs({
    select: function(event, ui) {
    	if(ui.panel.id=="tab-debug-log"){
    		document.forms["formDebug"].action = "digg!apidebuglog.action";
			document.getElementById("filetype").value = debugExport?"export.log":"digg-api.log";
    		document.forms["formDebug"].target = "iDebugLog";
    		document.forms["formDebug"].submit();
    	}
    }
});
$( '#divTree' ).niceScroll({
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

$( '#formDebug' ).niceScroll({
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

$( '#tab-debug-request' ).niceScroll({
	cursorcolor: '<%=(String)session.getAttribute("System-Theme")%>',
	railalign: 'right',
	cursorborder: "none",
	horizrailenabled: true, 
	zindex: 2002,
	left: '245px',
	cursoropacitymax: 0.6,
	cursorborderradius: "0px",
	spacebarenabled: false 
});

var autoSwitch = false;
$('#divSwitchApi').on('switch-change', function (e, data) {
	if( autoSwitch )
	{
		autoSwitch = false;
		return;
	}
	autoSwitch = false;
	var iSwitchApi = document.getElementById("iSwitchApi");
	setTemplateApi(null, iSwitchApi.checked?1:2);
});	

function setPqFilter(textarea, rows)
{
	var id = textarea.id;
	var a = 0;
	var val = textarea.value;
	var rows = 0;
	val = val.replace(/[\n]/g, ' '); //去掉回车换行
	var args = val.split(" ");
	val = "";
	if( rows > 0 ){
		textarea.rows = rows;
	}
}
//########################################################################
	var setting = {
		//editable: false,
		//fontCss : {color:"red"},
		check: {
			enable: false
		},
		callback: {
			onRightClick: onRightClick,
			onClick: onClick
		},
		view: {
			addDiyDom: addDiyDom
		},
		data: {
			key: {
				name: "cname",
				title: "id"
			}
		}
	};
	
	function onRightClick(event, treeId, treeNode) 
	{
		if (!treeNode && event.target.tagName.toLowerCase() != "button" && $(event.target).parents("a").length == 0) {
			myZtree.cancelSelectedNode();
			showRMenu("root", event.clientX, event.clientY);
			//alert("0:"+document.body.clientTop);
		} else if (treeNode && !treeNode.noR) {
			myZtree.selectNode(treeNode);
			showRMenu("node", event.clientX, event.clientY);
		}
	}

	function onClick(event, treeId, treeNode)
	{
		open(treeNode);
	}
	
	function addDiyDom(treeId, treeNode)
	{
	}
	
	$(document).ready(function(){
		try
		{
			var zNodes = <ww:property value="jsonData" escape="false"/>;
			myZtree = $.fn.zTree.init($("#myZtree"), setting, zNodes);
			myZtreeMenu = $("#rMenu");
			var id = getUserActionMemory("diggcfg!api."+sysid);
			var node;
			if( id )
			{
				node = myZtree.getNodeByParam("id", id);
			}
			if( !node ){
				node = myZtree.getNodeByParam("id", "/cos/config/modules/"+sysid+"/digg");
			}
			myZtree.selectNode(node);
			myZtree.expandNode(node, true);
			open(node);
		}
		catch(e)
		{
			skit_alert("初始化模板导航树异常"+e.message+", 行数"+e.lineNumber);
		}

		try{
			var id = getUserActionMemory("diggcfg!api1."+sysid);
			if( id ){
				$("#tabL").tabs('select', '#'+id);
			}

			var method = getUserActionMemory("diggcfg!api.method."+sysid);
			if( method ){
				selectMethod(method, true);
			}
		}
		catch(e){
		}
	});
//#########################################################################
</SCRIPT>
</html>