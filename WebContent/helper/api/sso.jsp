<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<link type="text/css" href="skit/css/bootstrap-tour.min.css" rel="stylesheet">
<link href="skin/defone/css/awesome-bootstrap-checkbox.css" rel="stylesheet">
<style type='text/css'>
</style>
<%=Kit.getDwrJsTag(request,"interface/SecurityMgr.js")%>
<%=Kit.getDwrJsTag(request,"engine.js")%>
<%=Kit.getDwrJsTag(request,"util.js")%>
<script src="<%=Kit.URL_PATH(request)%>skit/js/jsrsasign-rsa-min.js"></script>
<SCRIPT type="text/javascript">
var securityjava = "";
function newTimestamp(){
	SecurityMgr.getTs({
		callback:function(ts){
			document.getElementById("ts").value = ts;
			makeSsoUrl();
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("获得时间戳失败："+err)}
	});
}
function newNonce(){
	SecurityMgr.getNonce({
		callback:function(nonce){
			document.getElementById("nonce").value = nonce;
			makeSsoUrl();
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("获得随机数失败："+err)}
	});
}
function doMakeUrl(encryptSsoAccount){
	var apiurl = "<%=Kit.URL_PATH(request)%>";
	apiurl += "sso/";
	if( encryptSsoAccount ){
		document.getElementById("encryptSsoAccount").value = encryptSsoAccount; 
	}
	else{
		encryptSsoAccount = "接口安全账号<空格>rsAEncrypt(登录账号)";
	}
	apiurl += encryptSsoAccount+"/";
	try{
	    var ts = document.getElementById("ts").value;
		apiurl += ts+"/";
	    var nonce = document.getElementById("nonce").value;
		apiurl += nonce+"/";
	    var token = document.getElementById("token").value;
	    var privatekey = document.getElementById("privatekey").value;
		var rsa = new RSAKey();
		var signstr = token+ts+nonce;
		rsa.readPrivateKeyFromPEMString(privatekey);
		var signature = linebrk(rsa.sign(signstr, "sha1"), 64);
		signature = signature.replace("\r", "");
		signature = signature.replace("\n", "");
		apiurl += signature+"/";
		var sso_token = document.getElementById("sso_token").value;
		apiurl += sso_token;
		
		privatekey = privatekey.replace(/[\n]/g, '\\r\\n"+\n"');
		privatekey = privatekey.substring(0, privatekey.lastIndexOf("+"));
		document.getElementById("apiurl").value = apiurl;
		var cosAccount = document.getElementById("cosAccount").value;
		var apiAccount = document.getElementById("apiAccount").value;
		var java = '//JAVA编程实现签名';
		java += '\r\n//引用库bcpkix-jdk15on-158.jar';
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
		java += '\r\n//访问令牌账号的名称，不同接口类型账号不一样，配置在Zookeeper的/cos/config/security/[账号名]';
		java += '\r\nString apiAccount = "'+apiAccount+'";';
		java += '\r\nString cosAccount = "'+cosAccount+'"//RSA私钥加密('+cosAccount+')";';
		java += '\r\nRSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pk));';
		java += '\r\nCipher cipher = Cipher.getInstance("RSA");';
		java += '\r\ncipher.init(Cipher.ENCRYPT_MODE, priKey);';
		java += '\r\ncosAccount = RsaKeyTools.bytes2String(cipher.doFinal(cosAccount.getBytes("UTF-8")));';
		java += '\r\naccount = apiAccount+"_"+cosAccount';
		java += '\r\nsso_token = ""+sso_token+""';
		java += '\r\n//访问令牌，客户端自己知道用于签名，不用提交到后台，配置在Zookeeper的/cos/config/security/[账号名]';
		java += '\r\nString token = "'+token+'";//';
		java += '\r\nString ts = "'+ts+'";//时间戳字符串，可以是数字长整型的也可以是yyyyMMddHHmmssSSS格式';
		java += '\r\nString nonce = "'+nonce+'";//用户负责产生的随机数，一天内不能重复\r\n//从【我的系统接口开发安全管理】提取签名密钥';
		java += '\r\nString signatureString = token+timestamp+nonce;//用Token时间戳随机数构建签名字符串';
		java += '\r\nbyte[] signautreResult = RsaKeyTools.sign(signatureString, pk);//用私钥对签名字符串进行签名';
		java += '\r\nString signature = RsaKeyTools.bytes2String(signautreResult);//将签名结果转换成字符串';
		var url = "<%=Kit.URL_PATH(request)%>sso";
		java += '\r\nString url = "'+url+'/"+account+"/"+ts+"/"+nonce+"/"+signature+"/"+sso_token;';
		securityjava = java;
		window.setTimeout(function(){
			var ifr = window.frames["iSecurityJava"];
			if( ifr && ifr.setValue && securityjava )	{
				ifr.setValue(securityjava);
			}
		}, 500);
	}
	catch(e){
		alert("调用函数[doMakeUrl]出现异常"+e);
	}
}
function makeSsoUrl(){
	var privatekey = document.getElementById("privatekey").value;
	var apiAccount = document.getElementById("apiAccount").value;
	var cosAccount = document.getElementById("cosAccount").value;
	SecurityMgr.encryptSsoAccount(privatekey, cosAccount, apiAccount,{
		callback:function(rsp){
			if( rsp.succeed ){
				doMakeUrl(rsp.result);
			}
			else{
				skit_alert(result.message);
			}
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("账号安全加密："+err)}
	});
}
function doTest(){
	var sso_token = document.getElementById("sso_token").value;
	if( !sso_token ){
		skit_alert("请提供单点登录用户的TOEKN。", "错误提示");
		return;
	}
	makeSsoUrl();
	skit_alert("请复制单点登录地址的URL，打开其它浏览器，做地址栏粘贴。");
}
</SCRIPT>
</head>
<body style='overflow-y:hidden;padding-top:0px;padding-left:0px;'>
<table style='width:100%' id='tabL-debug'><tr>
	<td colspan='2'>
	<div class="form-group" style='margin-bottom: 5px;'>
  		<div class="input-group">
			<span class="input-group-addon input-group-addon-font">单点登录账户<i class='fa fa-terminal' style='margin-left:6px'></i></span>
			<input class="form-control form-control-font" type="text" id='cosAccount' value='<ww:property value="ssoAccount"/>' style="font-size:12px;background:#ffe4b5;">
			<span class="input-group-addon input-group-addon-font">输入本系统相关账号进行单点登录测试</span>
			<input class="form-control form-control-font" type="text" id='encryptSsoAccount' readonly>
			<span class="input-group-addon input-group-addon-font">安全账号+'_'+RSA加密(私钥, 单点登录账号)</span>
		</div>
	</div>
	<div class="form-group" style='margin-bottom: 5px;'>
  		<div class="input-group">
			<span class="input-group-addon input-group-addon-font">单点登录地址<i class='fa fa-terminal' style='margin-left:6px'></i></span>
			<input class="form-control form-control-font" type="text" id='apiurl' value='' style="font-size:12px;" readonly>
			<div class="input-group-btn">
	            <button type="button" class="btn btn-danger" style='font-size:12px;height:34px;' onclick='doTest()'><span class="fa fa-send"></span>测试一下</button>
            </div>
		</div>
	</div>
	</td></tr>
	<tr><td valign='top' style='width:576px;padding-left:0px;padding-right:5px;'>
		<div class="panel panel-default" style='margin-bottom:5px;'>
			<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-signing'></i> 安全参数</div>
			<div class="panel-body" style='padding-top:5px;padding-bottom:5px;padding-left:10px;padding-right:10px;'>
				<div class="form-group" style='margin-bottom: 10px;'>
					<div class="input-group" style='width:256px;'>
						<span class="input-group-addon input-group-addon-font">安全账户
							<i class='fa fa-user-secret' style='margin-left:6px'></i></span>
						<input class="form-control form-control-font" type="text" id='apiAccount' readonly
							value='<ww:property value="account"/>' style="font-size:12px;width:144px;">
					</div>
				</div>
				<div class="form-group" style='margin-bottom: 10px;'>
					<div class="input-group" style='width:256px;'>
						<span class="input-group-addon input-group-addon-font">访问令牌
							<i class='fa fa-ticket' style='margin-left:6px'></i></span>
						<input class="form-control form-control-font" type="text" id='token' readonly
							value='<ww:property value="token"/>' style="font-size:12px;width:144px;">
					</div>
				</div>
				<div class="form-group" style='margin-bottom: 10px;'>
					<div class="input-group" style='width:256px;'>
						<span class="input-group-addon input-group-addon-font">时间戳
							<i class='fa fa-clock-o' style='margin-left:17px'></i></span>
						<input class="form-control form-control-font" type="text" id='ts' name='ts' readonly
							value='<ww:property value="timestamp"/>' style="font-size:12px;width:144px;">
       					<div class="input-group-btn">
                               <button type="button" class="btn btn-info" style='font-size:12px;height:34px;' 
                               	onclick='newTimestamp()'><span class="fa fa-random"></span>随机</button>
                           </div>
					</div>
				</div>
				<div class="form-group" style='margin-bottom: 10px;'>
					<div class="input-group" style='width:288px;'>
						<span class="input-group-addon input-group-addon-font">随机数
							<i class='fa fa-random' style='margin-left:16px'></i></span>
						<input class="form-control form-control-font" type="text" id='nonce' name='nonce' readonly
							value='<ww:property value="nonce"/>' style="font-size:12px;width:144px;">
       					<div class="input-group-btn">
                               <button type="button" class="btn btn-info" style='font-size:12px;height:34px;' 
                               	onclick='newNonce()'><span class="fa fa-random"></span>随机</button>
                           </div>
					</div>
				</div>
				<div class="form-group" style='margin-bottom: 10px;'>
					<div class="input-group" style='width:256px;'>
						<span class="input-group-addon input-group-addon-font">用户令牌值
							<i class='fa fa-clock-o' style='margin-left:17px'></i></span>
						<input class="form-control form-control-font" type="text" id='sso_token'
							value='<ww:property value="sso_token"/>' style="font-size:12px;width:144px;background:#ffe4b5;">
						<span class="input-group-addon input-group-addon-font">单点登录用户存储做用户扩展属性字段中的token</i></span>
					</div>
				</div>
			</div>
		</div>
		<div class="panel panel-default" style='margin-bottom:5px;'>
			<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-signing'></i> 安全密钥(用于接口调用签名)</div>
			<div class="panel-body" style='padding-top:5px;padding-bottom:5px;padding-left:10px;padding-right:10px;'>
				<div class="form-group" style='margin-bottom: 10px;'>
					<textarea class="form-control form-control-font" rows="6"
						id="privatekey" style='color:#ffe4b5;background-color:#000;font-size:10px;'
					    data-title='没有加载到安全令牌的私钥数据'
						data-toggle="tooltip" 
						data-placement="bottom"
						data-trigger='manual'
						onkeydown='$("#privatekey").tooltip("hide")'
	                	placeholder="没有加载到安全令牌的私钥数据"><ww:property value="privatekey"/></textarea>
				</div>
			</div>
		</div>
	</td>
	<td valign='top' style='padding-right:0px;'>
		<div class="panel panel-default" style='margin-bottom:5px;'>
			<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-code'></i> JAVA签名方法</div>
			<div class="panel-body" style='padding-top:5px;padding-bottom:5px;padding-left:10px;padding-right:10px;'>
				<iframe src='editor!js.action' id='show-security-java' name='iSecurityJava' class='nonicescroll' 
					style='border:0px solid #eee;width:64px;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
			</div>
		</div>
	</div>
	</td>
</tr></table>
</body>
<script type="text/javascript">
function resizeWindow()
{
	var div, w, h;
	div = document.getElementById("privatekey");
	div.style.height = windowHeight - 435;
	
	div = document.getElementById("show-security-java");
	div.style.height = windowHeight - 120;
	div.style.width = windowWidth - 600;
}
</script>
<%@ include file="../../skit/inc/skit_bootstrap.inc"%>
<%@ include file="../../skit/inc/skit_cos.inc"%>
<%@ include file="../../skit/inc/skit_ztree.inc"%>
<script src="skit/js/jquery.md5.js"></script>
<script src="skit/js/bootstrap-tour.min.js"></script>
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
</style>
<SCRIPT type="text/javascript">
var _json = {};
try{
	_json = <ww:property value='jsonData' escape='false'/>;
	makeSsoUrl();
}
catch(e){
	alert(e);
}
</SCRIPT>
</html>