function resetflow()
{
	document.forms[0].action = 'monitorload!resetflow.action';
	document.forms[0].target = "downloadframe";
	document.forms[0].method = "POST";
	document.forms[0].submit();
}