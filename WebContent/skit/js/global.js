	/**
 	 *	当前日期时间
 	 */
 	var nowDate = new Date();
 	
 	/**
 	 *	日期时间的下限
 	 */
	var lowLimDate = new Date('1970/1/1');
	
	 /**
	 * short for getElement
	 */
	function $(arg)
	{
		return document.getElementById(arg) || document.getElementsByName(arg)[0] ;
	}

 	/**
	 * 功能:检查并实时显示textarea的输入字数
	 * 参数:textarea: 要显示字数的文本域
	 *     maxLength: 文本域最大长度
	 *     input:文本域字符显示区
	 * 示例:
	 *     <textarea name="txt" rows="5" clos="70" onPropertyChange="checkLength(this,1024,'input')"></textarea>
	 *	   最大长度为<b>1024</b>个字符。已输入<span id="input"><b>0</b></span>个字符。
	 */
	function checkLength(textarea, maxLength, input)
	{
		var show = document.getElementById(input) || document.getElementsByName(input)[0];
		var value = textarea.value.trim();
		if( value.len() > maxLength)
		{
			show.innerHTML = "<font color=red><b>"+ value.len() + "</b></font>";
		}
		else
		{
			show.innerHTML = "<b>"+ value.len() + "</b>";
		}
	}
	
	function checkLengthCN(textarea, maxLength, input)
	{
		var show = document.getElementById(input) || document.getElementsByName(input)[0];
		var value = textarea.value.trim();
		if( value.length > maxLength)
		{
			show.innerHTML = "<font color=red><b>"+ value.length + "</b></font>";
		}
		else
		{
			show.innerHTML = "<b>"+ value.length + "</b>";
		}
	}
 
	/**
	 * 功能: 日期
	 */
	function isNotDate(fName, field)
	{
		var m, year, month, day;
		m = field.value.trim().match(new RegExp("^((\\d{4})|(\\d{2}))([-./])(\\d{1,2})\\4(\\d{1,2})$"));
		if(m == null ) 
	    {
	        alert("日期：格式错误，合法格式：yyyy-mm-dd!");
	        field.focus();
	        return true;
	    }
		day = m[6];
		month = m[5]*1;
		year =  (m[2].length == 4) ? m[2] : GetFullYear(parseInt(m[3], 10));
		if(!parseInt(month))
	    {
	        alert("日期：格式错误，合法格式：yyyy-mm-dd!");
	        field.focus();
	        return true;
	    }
		month = month==0 ?12:month;
		var date = new Date(year, month-1, day);
	    if (typeof(date) == "object" && year == date.getFullYear() && month == (date.getMonth()+1) && day == date.getDate())
	    {
	        return false;
	    }
	    else
	    {
	        alert("日期：格式错误，合法格式：yyyy-mm-dd!");
	        field.focus();
	        return true;
	    }
	
	    function GetFullYear(y)
	    {
	        return ((y<30 ? "20" : "19") + y)|0;
	    }
	}
	
	/**
	 * 检查是否输入整数
	 */
	function isInteger(value)
	{
		return /^(-|\+)?\d+$/.test(value);
	}

	/**
	 * 检查是否输入数字
	 */
	function isNumber(value)
	{
		return !isNaN(value);
	}
	
	function isMobileNumber(value)
	{
		var pattern=/^(13+\d{9})|(15+\d{9})|(18+\d{9})$/;
		return pattern.test(value);
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

	/**
	 * unicode编码转中文
	 */
	function unicode2Chr(str) 
	{
		if ('' != str)
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
			return(st);
		}
		else
			return('');
	}

	/**
	 * 取字符串长度,一个中文两个字节长
	 */
	String.prototype.len=function()
	{
		return this.replace(/[^\x00-\xff]/g,"**").length;
	}

	/**
	 * 字符串trim
	 */
	String.prototype.trim=function()
	{
	 	return this.replace(/^\s+|\s+$/g, "");
	}

	/**
	 * 设置Html标签（select、input(text)）的值

	 * @param selectName
	 * @param selectValue
	 */
	function setValue(setKey,setValue)
	{
		var objSetKey = document.getElementsByName(setKey)[0] || document.getElementById(setKey);
		if(objSetKey)
		{
			objSetKey.value = setValue;
		}
		else
		{
			alert('不存在name或id为'+setKey+'的标签！');
		}
	}

	/**
	 * 用于Submit中等待响应的全局变量
	 */
	BakupBodyFocus=null;
	BackupWaitBodyContext=null;
	CanCancelWait=true;
	var lockDoc = document;
	
	/**
	 * Submit中等待响应显示信息
	 */
	function jscomLockScreenToWait(msg)
	{
		if (BakupBodyFocus==null && BackupWaitBodyContext==null)
		{
			BakupBodyFocus = lockDoc.body.onfocus+'';
			BackupWaitBodyContext = lockDoc.body.oncontextmenu+'';
			lockDoc.body.onfocus = jscomLockScreenToWait;
			lockDoc.body.oncontextmenu = jscomCancelClick;
		}
		var div = lockDoc.all["divLockWaiting"];
		if (div+''=="undefined")
		{
			div = lockDoc.createElement("DIV");
			div.setAttribute("id","divLockWaiting");
			div.className = "WaitBox";
			//div.title="点击可以取消";
			div.style.padding = 10;
			div.style.paddingLeft = 30;
			div.style.paddingRight = 30;
			div.innerHTML = msg;
			lockDoc.body.appendChild(div);
		}
		var x =(lockDoc.body.clientWidth-div.offsetWidth)/2;
		var y = (lockDoc.body.clientHeight-div.offsetHeight)/2;
		div.style.pixelLeft = x;
		div.style.pixelTop = y;
		div.style.visibility = "visible";
		//div.onclick = jscomCancelClick; // 暂时屏蔽用户可取消屏幕锁定
	
		div.setCapture();
	}
	
	/**
	 * 取消Submit中等待响应的显示信息
	 */
	function jscomUnlockScreenWait()
	{
		var div = lockDoc.all["divLockWaiting"];
		if (div+''=="undefined")
			return;
		div.style.visibility = "hidden";
		lockDoc.body.onfocus = BakupBodyFocus;
		lockDoc.body.oncontextmenu = BackupWaitBodyContext;
		BakupBodyFocus = null;
		BackupWaitBodyContext = null;
		lockDoc.releaseCapture();
	}
	
	/**
	 * 取消Submit中等待响应的显示信息
	 */
	function jscomCancelClick()
	{
		var elem = event.srcElement;
		if (CanCancelWait==true && elem.className=='WaitBox'){
			jscomUnlockScreenWait();
			event.cancelBubble = false;
			return false;
		}
		return false;
	}
	
	
	/**
	 * 是否Email
	 */
	function isEmail(value)
	{
	   var objRe = /^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/ ;
		if(value != "" && objRe.test(value))
		{		
			return true;
		}else
		{
		    return false;
		}
	}
	
	//合法的电信号码
	String.prototype.isValidMobile = function()
	{
		var reg = /^(133|153|189){1}([0-9]){8}$/;
		return reg.test(this);
	}
	
	/**
	 * 验证版本号是否合法
	 */
	 String.prototype.isValidVersion = function()
	{
		//非0开头，可以以0结尾
	 	var tver = /^(([1-9])([0-9])*(.([0-9])+)*)$/;
	
		//0开头，以0结尾没意义，不能以0结尾
		var tver2 = /^(([0]).)+(([0-9])+.)*([1-9])+$/;
	
	  	var version = this;
		if(version.length==0)return true;
	  	return tver.test(version) || tver2.test(version);
	}
	
	/**
	 * check if the valid seciton NO
	 * @author nixin
	 */
	String.prototype.isValidSecNo = function()
	{
	 	var tno = /^([1-9])([0-9])*(.([0-9])+)*$/;
	  	var secNo = this;
	  	return tno.test(secNo);
	}
	
	/**
	 *show a window if it's existing
	 */
	 function showWindow(win)
	 {
	 	var ret = false;
	 	try
	 	{
	 		//fox IE
	 		win.focus();
	 		
	 		//for firefox
	 		if( win.document ) ret = true;
		}
		catch(e)
		{}
		
		return ret;
	 }
	 
	 /**
	  * 空白页地址:返回日期控件中的初始化iframe地址,add by lizan 2007.12.06
	  */
	  function getBlankUrl()
	  {
	  	return "jsp/blank.html";
	  }
	  
	  /**
	  * 写Cookie,注意name不能为其他name的前缀
	  */
	  function writeCookie(name, data)
	  {
	  	var cookieStr = name + "="+ data;
  		document.cookie = cookieStr;
	  } 

	 /**
	  *用cookie的name读取cookie
	 */
	 function readCookie(cookieName)
	 {
   		var searchName = cookieName + "=";
   		var cookies = document.cookie;
   		if(!cookies)
   		{
   			//no cookies or cookies is disabled
   			return "";
   		}
   		var start = cookies.indexOf(cookieName);
   		if (start == -1)
   		{ // cookie not found
     		return "";
     	}
   		start += searchName.length //start of the cookie data
   		var end = cookies.indexOf(";", start);
   		if (end == -1)
   		{
     		end = cookies.length;
     	}
   		return cookies.substring(start, end);
	}
	
	/**
	* show ModelWindow
	*/
	function showModalWindow(s_url,w,h)
	{
	   return window.showModalDialog(s_url,window,'dialogWidth=' + w + ';dialogHeight='+ h+';center:yes;help:no;resizable:no;scroll:no;status:no;');
	}
	
	/**
	* getWinLeft
	*/
	function getWinLeft(width)
	{
		var left = (screen.width - width)/2;
		if(left < 0) left = 0;
		return left;
	}

	/**
	* getWinTop
	*/
	function getWinTop(height)
	{
		var top = (screen.availHeight - height)/2;
		if(top < 0) top = 0;
		return top;
	}
	/**
	* 已打开窗口对象
	*/
	var newWindowObj = null;
	
	/**
	* newWindow
	*/
	function newWindow(strHref, Width,Height)
	{
		if (newWindowObj && !newWindowObj.closed)
		{
			newWindowObj.close();
		}
		newWindowObj = window.open(strHref ,"","height=" + Height + ", width=" + Width + ", top="+getWinTop(Height)+", left="+getWinLeft(Width)+", toolbar=no, menubar=no, scrollbars=yes, resizable=yes, location=no, status=no");
	}
	
	/**
	* newMaxWindow
	*/
	function newMaxWindow(strHref)
	{
		if (newWindowObj && !newWindowObj.closed)
			newWindowObj.close();
		var Height=screen.availHeight;
		var Width=screen.availWidth;
		newWindowObj = window.open(strHref ,"","height=" + Height + ", width=" + Width + ", top="+getWinTop(Height)+", left="+getWinLeft(Width)+", toolbar=yes, menubar=yes, scrollbars=yes, resizable=yes, location=no, status=yes");

		try
		{
			newWindowObj.moveTo(-4,-4);
			newWindowObj.resizeTo(screen.availWidth+8,screen.availHeight+8);
			newWindowObj.focus();
		}
		catch(e){}
		
	}
	
	/**
 	* 将复选框的全选或全不选
 	*/	
	function checkAll(checkall,subchecked){ 
	  	var OnOffItem = document.getElementsByName(checkall)[0];
	  	if (OnOffItem!=null){ 
	    	var OnOff = OnOffItem.checked;
	    	var theItem = document.getElementsByName(subchecked); 
	    	if (theItem!=null){ 
	      		if (typeof(theItem.value)=='undefined'){ 
	        		for (var i=0;i<theItem.length; i++){ 
	          			var SubItem = theItem[i];
	          			if(SubItem.disabled==false)
	          			{ 
	          				SubItem.checked = OnOff; 
	          			}
	        		} 
	      		}else{ 
	      			if(theItem.disabled==false)
	      			{
	        			theItem.checked = OnOff; 
	        		}
	      		} 
	    	} 
	  	} 
	} 
	
	/**
	 * 将复选框的全选或全不选
	 */	
	function checkOne(checkall,subchecked){ 
	  	var OnOffItem = document.getElementsByName(checkall)[0];
	  	if (OnOffItem!=null){ 
	    	var i_checked = 0;
	    	var theItem = document.getElementsByName(subchecked); 
	    	if (theItem!=null){
	      		if (typeof(theItem.value)=='undefined'){ 
	        		for (var i=0;i < theItem.length; i++){ 
	          			var SubItem = theItem[i]; 
	          			if(SubItem.checked){
	          				i_checked++;
	          			}
	        		} 
	        		if(i_checked==theItem.length){
	        			 OnOffItem.checked = true;
	        		}else{
	        			OnOffItem.checked = false;
	        		}
	      		}else{ 
	      			var o = theItem.checked;
	        		OnOffItem.checked = o; 
	      		} 
	    	} 
	  	} 
	}
	
	/*
	 *切换隐藏/显示“查询条件表单”
	 */
	function switchQuery()
	{
	    var normalQuery = document.getElementById('normalQuery');
	    var advQuery = document.getElementById('advQuery');
	    var queryBtn = document.getElementById('queryBtn');
	
	    if (advQuery.style.display == '')
	    {
	        normalQuery.style.display = '';
	        advQuery.style.display = 'none';
	        queryBtn.innerText = '高级查询';
	    }
	    else
	    {
	        normalQuery.style.display = 'none';
	        advQuery.style.display = '';
	        queryBtn.innerText = '常用查询';
	    }
	}
	
	/**
	 * 设置radio的值
	 * @param selectName
	 * @param selectValue
	 */
	function setRadioValue(setKey,setValue)
	{
		var objSetKey = document.getElementsByName(setKey) || document.getElementById(setKey);
		for(var i = 0 ; i < objSetKey.length ; i++)
		{
			if(objSetKey[i].value == setValue)
			{
				objSetKey[i].checked = true;
			}
		}
	}
	
	/**
	 * 取得所选预案id列,ex: 1, 3, 10
	*/
	function getCheckSelectedIds(checkName)
	{
		var ids = ""; 
		var cbg = document.all[checkName];
		if( !cbg )
		{
			return ids;
		}
		if(cbg.length)
		{ 
			//数组,有多于一个预案

			for(var i=0; i<cbg.length; i++)
			{
				if(cbg[i].checked )
				{
					ids += "," + cbg[i].value;
				}				
			}
		}
		else
		{
			if(cbg.checked)
			{
				ids +=  "," + cbg.value;
			}
		}
		if( ids!="" )
		{
			ids = ids.substring(1);
		}
		return ids;
	}
	
	function refurbishTop()
	{
		if(opener)
		{
		    if(typeof(window.opener.document)=='unknown'||typeof(window.opener.document) == 'undefined')
		    {
		        //父窗口已关闭
		    }
		    else
		    {
		        //父窗口没有关闭
		        opener.location.href = opener.location.href.replace("#", "");
		    }
		}
		else if(dialogArguments)
		{
			if(typeof(window.dialogArguments.document)=='unknown'||typeof(window.dialogArguments.document) == 'undefined')
		    {
		        //父窗口已关闭
		    }
		    else
		    {
		        //父窗口没有关闭
		        dialogArguments.location.href = dialogArguments.location.href.replace("#", "");
		    }
		}
		else
		{
			//父窗口已关闭
		}
	}

	/**
	* 重定向window
	*/
	function mvTo(objWin,width,height)
	{
		var top = getWinTop(height);
		var left = getWinLeft(width);
		objWin.moveTo(top,left);
		objWin.resizeTo(width,height);
	}
	
    function getPointerX(event) 
    {    
    		return event.pageX || (event.clientX + (document.documentElement.scrollLeft || document.body.scrollLeft));  
   	};  
    function getPointerY(event) 
    {    
    		return event.pageY || (event.clientY +  (document.documentElement.scrollTop || document.body.scrollTop));  
   	};
   	
   	function getEvent()
	{
    	var   i   =   0;
    	if(document.all) return  window.event;
    	func = getEvent.caller;
    	while(func != null)
    	{
        	var   arg0 = func.arguments[0];
        	if(arg0)
        	{
            	if(arg0.constructor == MouseEvent)
            	{
                	return   arg0;
            	}
        	}
        	func = func.caller;
   	 	}
    	return null;
	}
	
	//获取元素的纵坐标
	function skit_getTop(e)
	{
		var offset = e.offsetTop;
		if( e.offsetParent !== null )
		{
			offset += skit_getTop(e.offsetParent);
		}
		return offset;
	}
	//获取元素的横坐标
	function skit_getLeft(e)
	{
		var offset = e.offsetLeft;
		if(e.offsetParent!==null) 
		{
			offset += skit_getLeft(e.offsetParent);
		}
		return offset;
	} 
	
	function skit_mousePosition(ev)
	{
		if(ev.pageX || ev.pageY)
		{
			return {x:ev.pageX, y:ev.pageY};
		}
		return {
			x:ev.clientX + document.body.scrollLeft - document.body.clientLeft,
			y:ev.clientY + document.body.scrollTop - document.body.clientTop
		};
	}
	
	function px2Number(stylePx)
	{
		return Number(stylePx.substring(0, stylePx.length - 2));	
	}
	
//JS操作cookies方法!
//写cookies
function setCookie(name,value)
{
    var Days = 30;
    var exp = new Date();
    exp.setTime(exp.getTime() + Days*24*60*60*1000);
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString();

    
    var strsec = getsec(time);
    var exp = new Date();
    exp.setTime(exp.getTime() + strsec*1);
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString();
}

//读取cookies
function getCookie(name)
{
    var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");
 
    if(arr=document.cookie.match(reg))
 
        return (arr[2]);
    else
        return null;
}

//删除cookies
function delCookie(name)
{
    var exp = new Date();
    exp.setTime(exp.getTime() - 1);
    var cval=getCookie(name);
    if(cval!=null)
        document.cookie= name + "="+cval+";expires="+exp.toGMTString();
}
//如果需要设定自定义过期时间
//那么把上面的setCookie　函数换成下面两个函数就ok;
//程序代码
function setCookie(name,value,time)
{
    var strsec = getsec(time);
    var exp = new Date();
    exp.setTime(exp.getTime() + strsec*1);
    document.cookie = name + "="+ escape (value) + ";expires=" + exp.toGMTString();
}
function getsec(str)
{
   var str1=str.substring(1,str.length)*1;
   var str2=str.substring(0,1);
   if (str2=="s")
   {
        return str1*1000;
   }
   else if (str2=="h")
   {
       return str1*60*60*1000;
   }
   else if (str2=="d")
   {
       return str1*24*60*60*1000;
   }
}

function removeAllChild(tag)
{
    var div = document.getElementById(tag);
    while(div.lastChild) //当div下还存在末尾节点时 循环继续
    {div.removeChild(div.lastChild) }
}

function isChina(s)
{
}
/**
*验证单IP格式是否合法
*/
function checkIP(ipValue,ipType){
	var bdsip = /^((?:(?:25[0-5]|2[0-4]\d|[1]\d\d|[1-9]\d|\d)\.){3}(?:25[0-5]|2[0-4]\d|[1]\d\d|[1-9]\d|\d))$/;
	var fa = true;		
	var ipTrim = ipValue.replace(/^\s+|\s+$/g,"");
	if(ipTrim == '' && ipTrim == null){
		skit_alert(ipType+'IP不能为空！');
		fa = false;
		return fa;
	}
	if(!bdsip.test(ipValue)){
		skit_alert('请输入正确的'+ipType+'IP格式！');
		fa = false;
		return fa;
	}
	return fa;
}
/**
*验证IP网段格式是否合法
*/
function checkSegment(ipValue,ipType){
	var bdsip = /^((?:(?:25[0-5]|2[0-4]\d|[1]\d\d|[1-9]\d|\d)\.){3}(?:\*))$/;
	var fa = true;		
	var ipTrim = ipValue.replace(/^\s+|\s+$/g,"");
	if(ipTrim == '' && ipTrim == null){
		skit_alert(ipType+'IP不能为空！');
		fa = false;
		return fa;
	}
	if(!bdsip.test(ipValue)){
		skit_alert('请输入正确的'+ipType+'IP格式！');
		fa = false;
		return fa;
	}
	return fa;
}
/**
*验证IP格式是否为合法的单IP或IP网段
*/
function checkIPorSegment(ipValue,ipType){
	var bdsip = /^((?:(?:25[0-5]|2[0-4]\d|[1]\d\d|[1-9]\d|\d)\.){3}(?:25[0-5]|2[0-4]\d|[1]\d\d|[1-9]\d|\d|\*))$/;
	var fa = true;		
	var ipTrim = ipValue.replace(/^\s+|\s+$/g,"");
	if(ipTrim == '' && ipTrim == null){
		skit_alert(ipType+'IP不能为空！');
		fa = false;
		return fa;
	}
	if(!bdsip.test(ipValue)){
		skit_alert('请输入正确的'+ipType+'IP格式！');
		fa = false;
		return fa;
	}
	return fa;
}
/**
*验证IP段格式是否合法
*/
function checkPart(ipFirst,ipSecond){
	var fa = true;
	var start = ipFirst.split('.');
	var end = ipSecond.split('.');
	if( start[2] == end[2]){
		if(parseInt(start[3]) > parseInt(end[3])){
			skit_alert(ipFirst+'-'+ipSecond+'起始IP大于结束IP');
			fa = false;
			return fa;
		}
	}else{
		skit_alert(ipFirst+'-'+ipSecond+'不在同一个网段');
		fa = false;
		return fa;
	}
	return fa;
}