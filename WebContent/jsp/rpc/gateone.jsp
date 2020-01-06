<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ page import="org.json.JSONObject"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  
JSONObject account = (JSONObject)session.getAttribute("account");
int userid = account.getInt("id");
%>
<html>
<head>
<link href="skit/css/bootstrap.min.css" rel="stylesheet"/>
<link href="skit/css/font-awesome.min.css" rel="stylesheet"/>
<script src="skit/crypto/core.js"></script>
<script src="skit/crypto/hmac.js"></script>
<script src="skit/crypto/sha1.js"></script>
<style type='text/css'>
<ww:property value='pageModel' escape='false'/>
</style>
<SCRIPT TYPE="text/javascript">
<ww:property value='javascript' escape='false'/>
var userid = <%=userid%>;
</SCRIPT>
</head>
<body>
    <div id="gateone_container" style="position: relative; width: 100%; height: 100%;">
        <div id="gateone"></div>
    </div>

<div style='position:absolute;top:28px;left:28px;cursor:pointer' id='helper'>
    <ww:if test='sysadmin'>
    <button type="button" class="btn btn-info btn-circle" onclick="openHelper()" title='打开用户手册' style='cursor:pointer'>
   	<i class="fa fa-question-circle-o"></i></button>
	<ww:if test='embedded'>
   	<br/><br/>
    <button type="button" class="btn btn-success btn-circle" onclick="openDeveloper()" title='打开开发手册' style='cursor:pointer'>
   	<i class="fa fa-language"></i></button>
   	<br/><br/>
	<button type="button" class="btn btn-warning btn-circle" onclick="clearScreen()" title='清除屏幕'>
   	<i class="fa fa-terminal"></i></button>
	<br/><br/>
	<button type="button" class="btn btn-danger btn-circle" onclick="closeTerminal()" title='关闭'>
	<i class="fa fa-sign-out"></i></button>
	</ww:if>
    </ww:if>
</div>
</body>
<script type="text/javascript">
function openHelper(){
	var href = "http://liftoff.github.io/GateOne/Applications/terminal/userguide.html";
	if( window.top.openView ) {
		window.top.openView("GateOne 官方用户手册", href);
	}
	else{
		window.open(href, "_blank","resizable=no,toolbar=no,location=no,directories=no,menubar=no,scrollbars=no,status=yes");
	}
}
function openDeveloper(){
	var href = "http://liftoff.github.io/GateOne/Developer/index.html";
	if( window.top.openView ) {
		window.top.openView("GateOne 官方开发手册", href);
	}
	else{
		window.open(href, "_blank","resizable=no,toolbar=no,location=no,directories=no,menubar=no,scrollbars=no,status=yes");
	}
}

var currentTermNum;
function clearScreen(){
	if( currentTermNum && embedded ){
	    GateOne.Terminal.clearScreen(currentTermNum);
	}
}

function closeTerminal(){
	if( currentTermNum && embedded ){
	    GateOne.Terminal.closeTerminal(currentTermNum);
		if( window.parent.removeTab ){
			window.parent.removeTab();
		}
	}
}

/*得到终端的最后一行数据*/
function getTerminalLine(term){
	var terminal = GateOne.Terminal.terminals[term];
	var i, line = "";
	for(i = terminal.screen.length - 1; i >= 0; i--){
		var line = terminal.screen[i];
		line = line.trim();
		if( line ){
			break;
		}
	}
	return line;
}

/*回调*/
function callbackTerminal(term){
	var line = getTerminalLine(term);
	if( line == "Host/IP or ssh:// URL [localhost]: <span id=\"term"+term+"cursor\" class=\"✈cursor\"> </span>" ){
   		GateOne.Terminal.sendString(GateOne.prefs.autoConnectURL+"\n", term);
   		window.setTimeout(function () {
           	callbackTerminal(term);
           }, 500);//500毫秒后看看终端返回
	}
	else if( line.indexOf("s password: <span id=\"term"+term+"cursor\" class=\"✈cursor\"> </span>") != -1 ) {
		//如果返回的数据是提示输入密码就发送密码
    	var p = "<ww:property value='jdbcUserpswd'/>";
       	if( p && window.top.decodeEncryption ){
        	GateOne.Terminal.sendString(window.top.decodeEncryption(p)+"\r", term);	
       	}
       	else{
       		GateOne.Terminal.sendString("\r", term);	
       	}
	}
	else {
		if( line == "<span id=\"term"+term+"cursor\" class=\"✈cursor\"> </span>" ){
            window.setTimeout(function () {
            	callbackTerminal(term);//处于光标等待状态，要继续等
            }, 3000);
		}
	}
}
//在嵌入式模式下打开终端
function openTerminal(){
	try{
		//top.showObject(GateOne);
		//top.showObject(GateOne.Base);
		//.newTerminal(null, null, container);
		var existingContainer = GateOne.Utils.getNode('#'+GateOne.prefs.prefix+'container');
		var container = GateOne.Utils.createElement('div', {
			'id': 'container',
			'class': 'terminal',
			'style': {'height': '100%', 'width': '100%'}
     	});
        var gateone = GateOne.Utils.getNode('#gateone');
		if (existingContainer) {
            container = existingContainer;
        } else {
            gateone.appendChild(container);
        }
		var term;
		if( "<ww:property value='db'/>" ){
			term = userid*100000+<ww:property value='db'/>;
		}
		//alert("GateOne.prefs.autoConnectURL("+term+"): "+GateOne.prefs.autoConnectURL);
        currentTermNum = GateOne.Terminal.newTerminal(term, null, container);
        window.setTimeout(function () {
        	callbackTerminal(currentTermNum);
	        /*var terminal = GateOne.Terminal.terminals[currentTermNum];
			//top.showJson(GateOne.Terminal.terminals[currentTermNum]);
        	var p = "<ww:property value='jdbcUserpswd'/>";
	        if( terminal && terminal.screen && terminal.screen.length > 3 ){
	        	if( terminal.screen[3] == "Host/IP or ssh:// URL [localhost]: <span id=\"term"+term+"cursor\" class=\"✈cursor\"> </span>" ){
		        	GateOne.Terminal.sendString(GateOne.prefs.autoConnectURL+"\n", currentTermNum);
		        	if( p && window.top.decodeEncryption ){
			            window.setTimeout(function () {
	    		        	GateOne.Terminal.sendString(window.top.decodeEncryption(p)+"\n", currentTermNum);		            	
			            }, 500);
		        	}
	        	}
	        	else{
	        		try{
						var len = terminal.screen.length;
						for(var i = len -1 ; i >= 0; i--){
							var line = terminal.screen[i];
							line = line.trim();
							if( line ){
								if( line.indexOf("s password: <span id=\"term"+term+"cursor\" class=\"✈cursor\"> </span>") != -1 ){
						        	if( p && window.top.decodeEncryption ){
							            window.setTimeout(function () {
					    		        	GateOne.Terminal.sendString(window.top.decodeEncryption(p)+"\r", term);		            	
							            }, 500);
						        	}
					        	}
								break;
							}
						}
	        		}
	        		catch(e)
	        		{
	        			alert(e);
	        		}
	        	}
	        }*/
        }, 500);
        //GateOne.Terminal.terminals[currentTermNum]['sshConnectString'] = "gt@192.168.80.131:22";
	}
	catch(e){
		alert(e);
	}
}
//go.Visual.displayMessage(gettext(\"An SSL certificate must be accepted by your browser to continue.  Please click <a href='\"+acceptURL+\"' target='_blank'>here</a> to be redirected.\"));
function tipsAcceptSSL(acceptURL){
	var realurl = "https://<ww:property value='ip'/>:10443";
	var tips = "";
	if( acceptURL.indexOf(realurl) == -1 ){
		tips = "<br/><br/><span style='color:gray;font-size:10px;'>请确保您设置的GateOne访问地址或域名映射本机GateOne的WEB地址:"+realurl+"</span>";
	}
	window.top.skit_alert("您需要在新的浏览器窗口中接受后【GateOne】提供的SSL安全证书，堡垒机服务才可以使用。<br/><br/>"+
		"打开接收安全证书的页面["+acceptURL+"]进行设置点击【<a href='"+acceptURL+"' target='_blank'>这里</a>】。<br/>"+
		"进入【高级】，浏览器提示【该证书因为其自签名而不被信任。】，请为地址添加【安全例外】<br/><br/>"+
		"完成设置后请点击确定"+tips,
		"SSL安全证书配置提示", function(yes){
		if( window.parent.removeTab ){
			window.parent.removeTab();
		}
	});
}
var sshUrl = "<ww:property value='sshUrl'/>";
var embedded = <ww:property value='embedded'/>;
window.onload = function() {
	try
	{
		var auth = {
		   'api_key': "<ww:property value='apiKey'/>",
		   'upn': "<ww:property value='upn'/>",
		   'timestamp': <ww:property value='timestamp'/>,
		   'signature': "<ww:property value='signature'/>",
		   'signature_method': 'HMAC-SHA1',
		   'api_version': '1.0'
		};
	    // Initialize Gate One:
		var cfg = {
	      	url: "https://<ww:property value='urlOrigin'/>"
	      	,auth: auth
	      	,embedded: embedded
	      	,showTitle: false
	      	,fontSize: '90%'
	      	,style: { 'background-color': '#303641', 'box-shadow': '0 0 40px blueViolet'}
	    };
		GateOne.init(cfg, function(){
			//An SSL certificate must be accepted by your browser to continue. Please click here to be redirected.
			if( embedded ){
				//top.showJson(GateOne.prefs);
				//var url = "ssh://<ww:property value='sshUser'/>@<ww:property value='sshTarget'/>:<ww:property value='sshPort'/>";
				GateOne.prefs.autoConnectURL = sshUrl;
				window.setTimeout(openTerminal, 1000);
			}
			else{
				if( sshUrl ){
					//var url = "ssh://"+sshUser+"@localhost:<ww:property value='sshPort'/>";
					GateOne.prefs.autoConnectURL = sshUrl;
				}
				else{
					GateOne.prefs.autoConnectURL = "localhost";
				}
				//alert("GateOne.prefs.autoConnectURL: "+GateOne.prefs.autoConnectURL);
			}
		});
		
		var	windowWidth = window.innerWidth || document.documentElement.clientWidth || window.document.body.clientWidth; 
		var	windowHeight = window.innerHeight || document.documentElement.clientHeight || window.document.body.clientHeight;
		var helper = document.getElementById( 'helper' );
		helper.style.top = windowHeight - helper.clientHeight - 16;
		helper.style.left = windowWidth - helper.clientWidth - 15;
	}
	catch(e)
	{
		alert(e);
	}
}
</script>
</html>