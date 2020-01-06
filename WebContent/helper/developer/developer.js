var myreferrer = document.referrer;
var urlprefix = "";
//alert("document.referrer="+document.referrer);
if(myreferrer && myreferrer != "" )
{
	var i = myreferrer.indexOf("jsp");
	if( i == -1 ) i = myreferrer.indexOf("developer.htm");
	if( i == -1 ) i = myreferrer.indexOf("main");
	if( i != -1 )
	{
		urlprefix = myreferrer.substring(0, i);
	}
}
//alert(urlprefix);