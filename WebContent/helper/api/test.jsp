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
function doDebug(){

	var ifr = window.frames["iDebugRqeust"];
	if( ifr && ifr.getValue )
	{
		if( apitype == "sysemail" ){
			skit_confirm("是否确定对附件URLL进行编码？编码能避免解析异常错误，取消则不编码。", function(yes){
				var json = jQuery.parseJSON(ifr.getValue());
				if( yes ) {
					json.attachment = encodeURIComponent(json.attachment);
					alert(json.attachment);
				}
				document.getElementById("data").value = JSON.stringify(json);
				document.forms["formDebug"].method = "POST";
				document.forms["formDebug"].action = "security!"+apitype+".action";
				document.forms["formDebug"].target = "iDebugResponse";
				document.forms["formDebug"].submit();
				$("#tabDebug").tabs('select', '#tab-debug-response');
			});		
		}
		else{
			document.getElementById("data").value = ifr.getValue();
			document.forms["formDebug"].method = "POST";
			document.forms["formDebug"].action = "security!"+apitype+".action";
			document.forms["formDebug"].target = "iDebugResponse";
			document.forms["formDebug"].submit();
			$("#tabDebug").tabs('select', '#tab-debug-response');
		}
	}
	else{
		skit_alert("没有正确打开请求设置");
	}
}
var apitype = "<ww:property value='datatype'/>";
var account = "<ww:property value='account'/>";
var securityjs = "";
var securityjava = "";
var securitypython = "";
var initialized = false;
function doMakeUrl(){
	var apiurl = "<%=Kit.URL_PATH(request)%>";
	apiurl += apitype+"/";
	apiurl += account+"/";
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
		apiurl += signature;
		
		document.getElementById("apiurl").value = apiurl;
		document.getElementById("signature").value = signature;
		document.getElementById("account").value = account;
		
		privatekey = privatekey.replace(/[\n]/g, '\\r\\n"+\n"');
		privatekey = privatekey.substring(0, privatekey.lastIndexOf("+"));
		//document.getElementById("signature").value = signature;
		
		var js = '//执行以下脚本进行数字签名';
		js += '\r\n//引用JS库&lt;script src="jsrsasign-rsa-min.js"&gt;&lt;/script&gt;';
		js += '\r\nvar token = "'+token+'";//访问令牌，客户端自己知道用于签名，不用提交到后台';
		js += '\r\nvar ts = "'+ts+'";//时间戳字符串，可以是数字长整型的也可以是yyyyMMddHHmmssSSS格式';
		js += '\r\nvar nonce = "'+nonce+'";//用户负责产生的随机数，一天内不能重复\r\n//从【我的系统接口开发安全管理】提取签名密钥';
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
		java += '\r\nString account = "'+account+'";//访问令牌账号的名称';
		java += '\r\n//访问令牌，客户端自己知道用于签名，不用提交到后台，配置在Zookeeper的/cos/config/security/[账号名]';
		java += '\r\nString token = "'+token+'";//';
		java += '\r\nString ts = "'+ts+'";//时间戳字符串，可以是数字长整型的也可以是yyyyMMddHHmmssSSS格式';
		java += '\r\nString nonce = "'+nonce+'";//用户负责产生的随机数，一天内不能重复\r\n//从【我的系统接口开发安全管理】提取签名密钥';
		java += '\r\nString signatureString = token+timestamp+nonce;//用Token时间戳随机数构建签名字符串';
		java += '\r\nbyte[] signautreResult = RsaKeyTools.sign(signatureString, pk);//用私钥对签名字符串进行签名';
		java += '\r\nString signature = RsaKeyTools.bytes2String(signautreResult);//将签名结果转换成字符串';
		var url = "<%=Kit.URL_PATH(request)%>"+apitype;
		java += '\r\nString url = "'+url+'/"+account+"/"+ts+"/"+nonce+"/"+signature+"/";';
		java += '\r\nString data = "'+JSON.stringify( _action)+'";//提交的数据';
		java += '\r\nDocument doc = HttpUtils.post(url, null, ("data="+data).getBytes("UTF-8"));';
		java += '\r\nStorng result = doc.getElementsByTag("body").get(0).text();';
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
		if( initialized ){
			setDebugRequest();
		}
		else{
			window.setTimeout(setDebugRequest, 2000);
			initialized = true;
		}
	}
	catch(e){
		alert("调用函数[doMakeUrl]出现异常"+e);
	}
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
function newTimestamp(){
	SecurityMgr.getTs({
		callback:function(ts){
			document.getElementById("ts").value = ts;
			doMakeUrl();
			$("#tabDebug").tabs('select', '#tab-debug-security-java');
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("获得时间戳失败："+err)}
	});
}
function newNonce(){
	SecurityMgr.getNonce({
		callback:function(nonce){
			document.getElementById("nonce").value = nonce;
			doMakeUrl();
			$("#tabDebug").tabs('select', '#tab-debug-security-java');
		},
		timeout:10000,
		errorHandler:function(err) {window.top.skit_alert("获得随机数失败："+err)}
	});
}

var defaultAction;
var defaultActionName;
var _action = {};
var _help = {};
//选择接口操作
function selectAction(li, a){
	if( li ){
		defaultAction = a;
		defaultActionName = li.innerHTML;
	}
	a = defaultAction;
	document.getElementById("actionName").value = defaultActionName;
	try{
		var action = {};
		var help = {};
		if( apitype == "sysuser" ){
			if( "addUser" == a ){
				action.action = "addUser";
				action.account = "admin";
				action.password = "123456";
				action.realname = "张三";
				action.email = "zhangsan@focusnt.com";
				action.role = 1;
				action.sex = 3;
	
				help.action = "操作标识";
				help.account = "用户的账号名称，系统内必须唯一";
				help.password = "密码";
				help.realname = "用户的真实姓名";
				help.email = "用户的邮箱，必填格式正确";
				help.role = "角色ID(角色配置在Zookeeper的/cos/config/role)，1表示系统管理员组";
				help.sex = "1表示男, 2表示女, 3表示未知";
			}
			else if( "listUser" == a ){
				action.action = "listUser";
				action.role = -1;
				action.sex = -1;
				action.status = -1;
	
				help.action = "操作标识";
				help.role = "角色ID（可在角色管理查看），-1-1表示取所有角色的用户";
				help.sex = "-1表示取所有性别用户, 1表示男, 2表示女, 3表示未知";
				help.status = "-1表示取所有用户, 1表示有效, 表示暂停";
			}
			else if( "getUser" == a ){
				action.action = "getUser";
				action.account = "admin";
				help.action = "操作标识，根据账号名查询";
				help.account = "用于登录账号名称，例如admin";
			}
			else if( "getRole" == a ){
				action.action = "getRole";
				action.account = "admin";
				help.action = "操作标识，根据账号名查询指定用户的角色信息";
				help.account = "用于登录账号名称，例如admin";
			}
		}
		else if( apitype == "syslog" ){
			action.type = 1;
			action.severity = 2;
			action.text = "这是一条测试系统日志接口的消息";
			action.category = "系统安全接口";
			action.context = "";
			action.contextlink = "";
			action.account = "admin";

			help.type = "日志类型: 操作日志(1), 运行日志(2), 用户日志(3), 安全日志(4)";
			help.severity = "日志级别: INFO(1), DEBUG(2), ERROR(3)";
			help.text = "日志描述，最多不超过1000字";
			help.category = "日志类型，用户可以自定义";
			help.context = "日志上下文，可以填写详细的日志描述，例如异常信息";
			help.contextlink = "日志上下文链接，填写一个链接地址，关联该日志";
			help.account = "如果是日志类型是操作日志，填写操作用户的账号";
		}
		else if( apitype == "sysemail" ){
			//String to,String cc, String subject,String content,String attachment, String sysid
			action.action = a;
			action.to = "author@focusnt.com";

			help.action = "发送不同类型的邮件: 富文本(html)、快照(链接镜像)、多图(提供图片链接)、普通(文字)";
			help.to = "接收邮件的邮箱地址，多个地址以分好分割";
			help.subject = "邮件标题";
			
			if( "sendHtml" == a ){
				action.subject = "这是一条富文本邮件";
				action.content = "<html><head></head><body><img src='cid:http://www.baidu.com/img/bdlogo.gif'><br/>欢迎使用COS邮件功能</body></html>";
				help.content = "base64.encode(邮件内容是HTML格式的富文本内容)";
			}
			else if( "sendSnapshot" == a ){
				action.subject = "这是一条快照邮件";
				action.content = "http://fontawesome.dashgame.com/";
				help.content = "提供快照的URL地址，系统会自动去下载该链接的内容，发送给用户";
			}
			else if( "sendImages" == a ){
				action.subject = "这是一条多图邮件";
				action.content = "<%=Kit.URL_PATH(request)%>images/cmp/17.png;<%=Kit.URL_PATH(request)%>images/cmp/27.png;<%=Kit.URL_PATH(request)%>images/cmp/37.png";
				help.content = "以分好分割的多个图片链接，用户收到邮件会看到正文中的多图";
			}
			else{
				action.subject = "这是一条普通邮件";
				action.content = "邮件文字";
				help.content = "邮件内容是文本格式";
			}
			
			action.attachment = "<%=Kit.URL_PATH(request)%>images/cmp/7.png, 图一.png;<%=Kit.URL_PATH(request)%>images/cmp/10.png, 图二.png";
			action.subsys = "";
			
			help.attachment = "邮件附件，分号分割多个附件，逗号分割路径和标题。支持绝对路径，但是需要确保发送邮件的服务器存储路径。注意‘, ’逗号后面有一个空格。为了确保附件URL能正确解析，请对URL文件进行编码encodeURIComponent";
			help.subsys = "子系统，如果你希望以某个子系统的身份发送填写，例如Sys(默认子系统)";
		}
		else if( apitype == "sysnotify" ){
			action.account = "admin";
			action.title = "这是一条测试系统通知接口的消息";
			action.filter = "系统接口";
			action.context = "通知消息的内容";
			action.contextlink = "http://www.baidu.com/";
			action.action = "查看";
			action.actionlink = "http://www.sina.com/";

			help.account = "接收通知的用户账号";
			help.title = "通知的标题文字，最多不超过100字";
			help.fitler = "通知类型，用户可以自定义";
			help.context = "通知的上下文内容";
			help.contextlink = "通知上下文链接，在通知页面视图会嵌入该链接";
			help.action = "通知视图出现按钮的名称";
			help.actionlink = "按钮对应的视图页面链接";
		}
		else if( apitype == "sysalarm" ){

			if( "sendAlarm" == a ){
				action.action = "sendAlarm";
				action.category = "设备告警";
				action.severity = "严重";
				action.type = "A2001";
				action.subsys = "Sys";
				action.dn = "10.10.10.100";
				action.title = "服务器10.10.10.100CPU负载极高超过95%";
				action.text = "服务器CPU负载从时间2018-03-06 02:57:43以来持续超过95%，当前是100%";
				action.cause = "伺服器正在运行某程序";

				help.action = "操作类型";
				help.category = "告警类别: B(业务告警),Q(服务质量告警),S(系统告警),E(环境告警),D(设备告警);";
				help.severity = "告警级别: BLACK(致命),RED(严重),ORANGE(重要),YELLOW(次要),BLUE(警告);";
				help.type = "告警类型，用户自定义各种告警的唯一标识";
				help.subsys = "产生告警的子系统的标识";
				help.dn = "产生告警的物理网元IP地址";
				help.title = "告警标题";
				help.text = "告警描述";
				help.cause = "告警原因";
			}
			else if( "closeAlarm" == a ){
				action.action = "closeAlarm";
				action.type = "A2001";
				action.subsys = "Sys";
				action.remark = "故障已经解决告警可结束";

				help.action = "操作类型";
				help.type = "告警类型，用户自定义各种告警的唯一标识";
				help.subsys = "产生告警的子系统的标识";
				help.remark = "备注告警关闭的原因";
			}
			else if( "queryAlarm" == a ){
				action.action = "queryAlarm";
				action.category = "设备告警";
				action.severity = "严重";
				action.subsys = "Sys";
				action.dn = "";
				action.range = -1;

				help.action = "操作类型";
				help.category = "告警类型: B(业务告警),Q(服务质量告警),S(系统告警),E(环境告警),D(设备告警);";
				help.severity = "告警级别: BLACK(致命),RED(严重),ORANGE(重要),YELLOW(次要),BLUE(警告);";
				help.subsys = "产生告警的子系统的标识，允许为空";
				help.dn = "产生告警的物理网元IP地址，允许为空";
				help.range = "0是未关闭告警，1是已关闭告警，-1所有告警";
			}
		}
		else if ( apitype == "programpublish" ){
			action.ip = _json.ip;
			action.port =_json.port;
			action.id = "mysqldb";
			help.ip = "发布程序到伺服器的IP地址";
			help.port = "发布程序到伺服器的主控端口";
			help.id = "程序应用的唯一标识";

			if( "addProgram" == a || "setProgram" == a ){
				action.oper = "addProgram" == a ? 0 : 1;
				action.operuser = "#第三方程序xxx";
				action.operlog = "第三方程序通过接口修改程序参数";
				action.name = "系统数据库服务";
				action.description = "最流行的关系型数据库管理系统之一，在 WEB应用方面，MySQL是最好的 RDBMS应用软件。";
				action.version = "addProgram" == a ? "5.7.29.0" : "5.7.29.1";

				help.oper = "操作: 0新增程序；1修改程序；删除程序";
				help.operuser = "由接口调用者填写谁调用了该接口";
				help.operlog = "描述接口调用操作";
				help.name = "程序名称";
				help.description = "程序描述";
				help.version = "程序版本号";

				action.maintenance = {};
				action.maintenance.manager = "admin";
				action.maintenance.programmer = "系统管理员";
				action.maintenance.email = "admin@focusnt.com";
				action.maintenance.remark = "";

				help.maintenance = {};
				help.maintenance.manager = "程序管理员账号";
				help.maintenance.programmer = "程序管理员名字";
				help.maintenance.email = "程序管理员邮箱";
				help.maintenance.remark = "程序维护说明";
				
				action.control = {};
				action.control.delayed = 0;
				action.control.restartup = 0;
				action.control.mode = 0;
				action.control.dependence = "COSPortal";
				action.control.logfile = "/mysql/data/*.err";
				action.control.pidfile = "/mysql/data/*.pid";
				action.control.cfgfile = "/mysql/my.ini";

				help.control = {};
				help.control.delayed = "主控启动后延迟多少秒启动";
				help.control.restartup = "程序停止后多少秒重启";
				help.control.mode = "工作模式 0主控启动 1前台启动 2单步调试 3后台守护 4后台守护前台启动";
				help.control.dependence = "程序依赖的程序，用于定义程序依赖关系";
				help.control.logfile = "日志路径以及日志文件的类型";
				help.control.pidfile = "进程PID文件的路径以及文件类型";
				help.control.cfgfile = "程序配置文件路径以及文件或类型";

				action.startup = {};
				action.startup.command = [];
				action.startup.command[0] = "mysqld";
				help.startup = {};
				help.startup.command = [];
				help.startup.command[0] = "启动命令，必须确保程序可运行";
				
				action.shutdown = {};
				action.shutdown.command = [];
				action.shutdown.command[0] = "mysqladmin";
				action.shutdown.command[1] = "-uroot";
				action.shutdown.command[2] = "-p123456";
				action.shutdown.command[3] = "shutdown";

				help.shutdown = {};
				help.shutdown.command = [];
				help.shutdown.command[0] = "停止命令，必须确保程序可运行";
				help.shutdown.command[1] = "停止参数1";
				help.shutdown.command[2] = "停止参数2";
				help.shutdown.command[3] = "停止参数3";
			}
			else if( "delProgram" == a ){
				action.oper = 2;
				action.operuser = "#第三方程序xxx";
				action.operlog = "第三方程序通过接口删除程序";
				help.oper = "操作: 0新增程序；1修改程序；删除程序";
				help.operuser = "由接口调用者填写谁调用了该接口";
				help.operlog = "描述接口调用操作";
			}
		}
		else if( apitype == "configmonitor" ){
			if( "addServer" == a ){
				action.action = "addServer";
				action.ip = "192.168.0.100";
				action.port = 10000;
				action.parent = 0;
				
				help.action = "操作标识";
				help.ip = "添加的伺服器IP地址";
				help.port = '添加的伺服器的主控端口';
				help.parent = '添加的伺服器的父节点id,默认0表示缺省集群';
			}
			else if( "addCluster" == a ){
				action.action = "addCluster";
				action.name = "伺服器模块集群名称";
				action.parent = -7;
				
				help.action = "操作标识";
				help.name = "监控导航的目录名称";
				help.parent = '伺服器集群父节点,-7表示根节点下';
			}
			else if( "delServer" == a ){
				action.action = "delServer";
				action.id = 100;
				
				help.action = "操作标识";
				help.id = "删除伺服器模块的id";
			}
			else if( "delCluster" == a ){
				action.action = "delCluster";
				action.id = 100;
				action.delServer = true;
				
				help.action = "操作标识";
				help.id = "删除伺服器集群的id";
				help.delServer = "是否删除集群目录下的伺服器配置，true表示是，false表示否集群下的伺服器将移动到缺省集群"
			}
			else if( "renameCluster" == a ){
				action.action = "renameCluster";
				action.id = 100;
				action.name = "新集群名称";
				
				help.action = "操作标识";
				help.id = "要修改名称的集群ID";
				help.name = "修改后集群名称";
			}
		}
		else if( apitype == "reportmonitor" ){
			action.servers = [];
			var server = {};
			server.ip = "192.168.0.100";
			server.port = 10000;
			server.system_uptime = _json.system_uptime;
			server.descript = _json.descript;
			server.host_name = _json.host_name;
			server.os_name = _json.os_name;
			server.os_version = _json.os_version;
			server.server_no = _json.server_no;
			//监控数据
			server.cpu = _json.cpu;
			//关于内存
			server.mem = _json.mem;
			//关于网络
			server.net = _json.net;
			//关于IO
			server.io = _json.io;
			//关于磁盘
			server.disk = _json.disk;
			
			server.host_info = _json.host_info;
			server.ip_info = _json.ip_info;
			server.modules = _json.modules;
			action.servers.push(server);
			action.remark = "这是真实的监控数据结构例子";
			
			help.servers = [];
			server = {};
			server.ip = "伺服器IP地址";
			server.port = "伺服器主控短裤";
			server.system_uptime = "伺服器启动时间";
			server.descript = "被监控的伺服模块简称";
			server.host_name = "被监控的伺服模块的主机名称";
			server.os_name = "操作系统名称";
			server.os_version = "操作系统版本";
			server.server_no = "伺服器唯一标识";
			//监控数据
			server.cpu = {};
			server.cpu.usage = "CPU使用百分比情况: 0.0000格式";
			//关于内存
			server.mem = {};
			server.mem.usage = "内存使用百分比情况: 0.0000格式";
			server.mem.used = "已使用内存";
			server.mem.cached = "缓存大小";
			server.mem.swap = "交换区大小";
			//关于网络
			server.net = {};
			server.net.input = "网络输入实时流量(秒)";
			server.net.output = "网络输出实时流量(秒)";
			//关于IO
			server.io = {};
			server.io.write = "磁盘写实时数据量(秒)";
			server.io.read = "磁盘读实时数据量(秒)";
			//关于磁盘
			server.disk = {};
			server.disk.usage = "磁盘使用百分比情况: 0.0000格式";
			server.disk.used = "已使用磁盘空间";
			server.disk.storages_info = "描述存储的整体情况";
			
			server.host_info = "被监控的伺服模块完整的主机描述";
			server.ip_info = "ifconfig指令输出结果";
			
			server.modules = [];
			var module = {};
			module.id = "程序的唯一标识ID";
			module.name = "运行程序名称";
			module.startup_time = nowtime();
			module.version = "程序版本号";
			module.log_path = "程序日志路径，可以为空";
			module.remark = "程序的描述";
			module.programer = "维护该程序的人员姓名";
			module.programer_email = "维护该程序的人员的邮箱地址";
			module.run_info = "程序运行描述";
			module.state = "程序运行描述: "+
		    "STATE_INIT = 0;//待机"+
		    "STATE_STARTUP = 1;//启动"+
		    "STATE_SHUTDOWN_WAIT = 2;//等待关闭"+
		    "STATE_SHUTDOWN = 3;//关闭"+
		    "STATE_SUSPEND_WAIT = 5;//暂停"+
		    "STATE_SUSPEND = 6;//暂停"+
		    "STATE_CLOSE = 7;//关闭";
			module.mem_usage = "程序当前内存占用(单位K)/程序最高占用(单位K)";
			module.run_time = "程序CPU占用时间单元(毫秒)";
			server.modules.push(module);
			help.servers.push(server);
			help.remark = "备注说明上传服务器的监控数据的情况";
		}
		_action = action;
		_help = help;
		doMakeUrl();
	}
	catch(e){
		alert(e);
	}
	$("#tabDebug").tabs('select', '#tab-debug-request');
}

function setDebugRequest(){
	var ifr = window.frames["iDebugRqeust"];
	if( ifr && ifr.setValue )
	{
		ifr.setValue(JSON.stringify( _action, null, 4 ));
	}
	ifr = window.frames["iDebugRqeustHelp"];
	if( ifr && ifr.setValue )
	{
		ifr.setValue(JSON.stringify( _help, null, 4 ));
	}

	ifr = window.frames["iSecurityJs"];
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
</SCRIPT>
</head>
<body style='overflow-y:hidden;padding-top:0px;padding-left:0px;'>
<table style='width:100%' id='tabL-debug'><tr>
	<td colspan='2'>
	<div class="form-group" style='margin-bottom: 5px;'>
  		<div class="input-group">
			<span class="input-group-addon input-group-addon-font">接口地址
				<i class='fa fa-terminal' style='margin-left:6px'></i></span>
			<input class="form-control form-control-font" type="text" id='apiurl'
				value='' style="font-size:12px;background:#ffe4b5;">
			<div class="input-group-btn">
	            <button type="button" class="btn btn-danger" style='font-size:12px;height:34px;'
	            	onclick='doDebug()'><span class="fa fa-send"></span>测试一下</button>
            </div>
		</div>
	</div>
	</td></tr>
	<tr><td valign='top' style='width:350px;padding-left:0px;padding-right:5px;'>
		<form id='formDebug' name='formDebug' style='margin-bottom:0px;'>
			<input type='hidden' name='data' id='data'>
			<input type='hidden' name='signature' id='signature'>
			<div class="form-group" style='margin-bottom: 5px;'>
     			<div class="input-group">
					<span class="input-group-addon input-group-addon-font">操作类型
						<i class='fa fa-file-code-o' style='margin-left:6px'></i></span>
					<input class="form-control form-control-font" type="text" id='actionName'
						value='' style="font-size:12px;background:#ffe4b5;">
      					<div class="input-group-btn">
                              <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false"
                              	style='height:34px;'><span class="caret"></span></button>
                              <ul class="dropdown-menu pull-left" style='font-size:12px;width:60px;cursor:pointer'>
							<ww:iterator value="listData" status="loop">
                                  <li><a onclick='selectAction(this, "<ww:property value='value'/>");'><ww:property value='name'/></a></li>
							</ww:iterator>
                              </ul>
                          </div>
				</div>
			</div>
			<div class="panel panel-default" style='margin-bottom:5px;'>
				<div class="panel-heading" style='font-size:12px;padding:5px 15px'><i class='fa fa-signing'></i> 安全参数</div>
				<div class="panel-body" style='padding-top:5px;padding-bottom:5px;padding-left:10px;padding-right:10px;'>
					<div class="form-group" style='margin-bottom: 10px;'>
						<div class="input-group" style='width:256px;'>
							<span class="input-group-addon input-group-addon-font">安全账户
								<i class='fa fa-user-secret' style='margin-left:6px'></i></span>
							<input class="form-control form-control-font" type="text" id='account' name='account'
								value='<ww:property value="token"/>' style="font-size:12px;background:#ffe4b5;width:144px;">
						</div>
					</div>
					<div class="form-group" style='margin-bottom: 10px;'>
						<div class="input-group" style='width:256px;'>
							<span class="input-group-addon input-group-addon-font">访问令牌
								<i class='fa fa-ticket' style='margin-left:6px'></i></span>
							<input class="form-control form-control-font" type="text" id='token'
								value='<ww:property value="token"/>' style="font-size:12px;background:#ffe4b5;width:144px;">
						</div>
					</div>					
					<div class="form-group" style='margin-bottom: 10px;'>
						<div class="input-group" style='width:256px;'>
							<span class="input-group-addon input-group-addon-font">时间戳
								<i class='fa fa-clock-o' style='margin-left:17px'></i></span>
							<input class="form-control form-control-font" type="text" id='ts' name='ts'
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
							<input class="form-control form-control-font" type="text" id='nonce' name='nonce'
								value='<ww:property value="nonce"/>' style="font-size:12px;width:144px;">
        					<div class="input-group-btn">
                                <button type="button" class="btn btn-info" style='font-size:12px;height:34px;' 
                                	onclick='newNonce()'><span class="fa fa-random"></span>随机</button>
                            </div>
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
		</form>
	</td>
	<td valign='top' style='padding-right:0px;'>
	<div id="tabDebug" style="position:relative;border:1px solid #cfcfcf;">
	<ul>
		<li><a href="#tab-debug-request"><i class='fa fa-cloud-upload'></i> 请求数据</a></li>
		<li><a href="#tab-debug-response"><i class='fa fa-cloud-download'></i> 响应数据</a></li>
		<li id='li-tab-debug-security-js'><a href="#tab-debug-security-js"><i class='fa fa-user-secret'></i> JS签名方法</a></li>
		<li id='li-tab-debug-security-java'><a href="#tab-debug-security-java"><i class='fa fa-user-secret'></i> JAVA签名方法</a></li>
		<li id='li-tab-debug-security-python'><a href="#tab-debug-security-python"><i class='fa fa-user-secret'></i> Python签名方法</a></li>
	</ul>

	<div id='tab-debug-request'>
	<table width='100%' border='0'><tr>
	<td width='50%' valign='top'><iframe name='iDebugRqeust' id='iDebugRqeust' src='editor!json.action' class='nonicescroll' style='border:0px solid #eee;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe></td>
	<td width='50%' valign='top'><iframe name='iDebugRqeustHelp' id='iDebugRqeustHelp' src='editor!json.action' class='nonicescroll' style='border:0px solid #eee;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe></td>
	</tr></table>
	</div>
	<iframe name='iDebugResponse' id='tab-debug-response' class='nonicescroll' src='editor!json.action'
		style='border:0px solid #eee;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
	<iframe src='editor!js.action' id='tab-debug-security-js' name='iSecurityJs' class='nonicescroll' 
		style='border:0px solid #eee;width:100%;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
	<iframe src='editor!js.action' id='tab-debug-security-java' name='iSecurityJava' class='nonicescroll' 
		style='border:0px solid #eee;width:100%;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
	<iframe src='editor!js.action' id='tab-debug-security-python' name='iSecurityPython' class='nonicescroll' 
		style='border:0px solid #eee;width:100%;margin-left:1px;margin-top:1px;margin-bottom:1px;'></iframe>
	</div>
	</td>
</tr></table>
</body>
<script type="text/javascript">
function resizeWindow()
{
	var div, w, h;
	div = document.getElementById("tabL-debug");
	h = div.rows[0].clientHeight;
	w = windowWidth - 350;

	div = document.getElementById("privatekey");
	div.style.height = windowHeight - h - 308;
	
	div = document.getElementById("tabDebug");
	div.style.width = w;
	//div.style.height = windowHeight - 36;

	h = windowHeight - h - 39;
	var _w = w - 8;
	div = document.getElementById("iDebugRqeust");
	div.style.width = _w/2;
	div.style.height = h;
	div = document.getElementById("iDebugRqeustHelp");
	div.style.width = _w/2;
	div.style.height = h;

	div = document.getElementById("tab-debug-response");
	div.style.height = h;
	div.style.width = w - 10;
	
	div = document.getElementById("tab-debug-security-js");
	div.style.height = h;
	div.style.width = w - 10;

	div = document.getElementById("tab-debug-security-java");
	div.style.height = h;
	div.style.width = w - 10;
	
	div = document.getElementById("tab-debug-security-python");
	div.style.height = h;
	div.style.width = w - 10;
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
$("#tabDebug").tabs({
    select: function(event, ui) {
    }
});
<ww:iterator value="listData" status="loop">
if( !defaultAction ) {
	defaultAction = "<ww:property value='value'/>";
	defaultActionName = "<ww:property value='name'/>";
}
</ww:iterator>
var _json = {};
try{
	_json = <ww:property value='jsonData' escape='false'/>;
	selectAction();
}
catch(e){
	alert(e);
}
</SCRIPT>
</html>