<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<style type='text/css'>
</style>
<script src="skit/crypto/core.js"></script>
<script src="skit/crypto/hmac.js"></script>
<script src="skit/crypto/sha1.js"></script>
<link rel=stylesheet href="https://124.204.50.154:10443/static/gateone.css">
<script type="text/javascript" src="https://124.204.50.154:10443/static/gateone.js"></script>
</head>
<body>
    <div id="gateone_container" style="position: relative; width: 100%; height: 100%;">
        <div id="gateone"></div>
    </div>
</body>
<script type="text/javascript">
window.onload = function() {
	var key = "YTk4NjkzMTU5ZDIwNDdkZDlkOTMwMzA1M2E4NWI0OWU0O";
	var date = new Date();
	var un = "wbq";
	var timestamp = date.getTime();
	var signature = "";
	try
	{
		signature = CryptoJS.HmacSHA1(key+un+timestamp, "ZWJiNTc4YjVkYTdlNDRiY2JkZDNjMzUyZmE2MGIzNTg0N").toString(CryptoJS.enc.Hex);
	}
	catch(e)
	{
		alert(e);
	}
	//alert(signature);
	var auth = {
	   'api_key': key,
	   'upn': un,
	   'timestamp': timestamp,
	   'signature': signature,
	   'signature_method': 'HMAC-SHA1',
	   'api_version': '1.0'
	};
    // Initialize Gate One:
    var cfg = {
      	url: 'https://124.204.50.154:10443',
      	auth: auth,
      	embedded: false
    };
	
	GateOne.init(cfg, function(){
	    // Introducing the superSandbox()!  Use it to wrap any code that you don't want to load until dependencies are met.
	    // In this example we won't call newTerminal() until GateOne.Terminal and GateOne.Terminal.Input are loaded.
	    GateOne.Base.superSandbox("NewExternalTerm", ["GateOne.Terminal", "GateOne.Terminal.Input"], function(window, undefined) {
	        "use strict";
	        try{
		        var existingContainer = GateOne.Utils.getNode('#'+GateOne.prefs.prefix+'container');
		        var container = GateOne.Utils.createElement('div', {'id': 'container', 'class': 'terminal', 'style': {'height': '100%', 'width': '100%'}});
		   		var gateone = GateOne.Utils.getNode('#gateone');
		   		// Don't actually submit the form
			    if (!existingContainer) {
			        gateone.appendChild(container);
			    } else {
			    	container = existingContainer;
			    }
		   		// Create the new terminal
		    	var termNum = GateOne.Terminal.newTerminal(null, null, container); 
				//alert("展示对象("+termNum+"):"+GateOne.Terminal.newTerminal);
	        }
	        catch(e)
	        {
	        	alert(e);
	        }
	    });
	});
}
</script>
</html>