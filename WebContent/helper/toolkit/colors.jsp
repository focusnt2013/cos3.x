<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response); %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="skit/css/bootstrap.min.css" rel="stylesheet"/>
<link href="skit/css/font-awesome.min.css" rel="stylesheet"/>
<style type="text/css">
<ww:property value='pq_cellcls' escape='false'/>
</style>
</head>
<body>
<table>
<ww:property value='html' escape='false'/>
</table>
</body>
<script type="text/javascript">
function a(e){
	if( parent && parent.setColor ){
		parent.setColor(e.innerHTML, e.title);
	}
}
</script>
</html>