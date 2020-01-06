<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response); %>
<html>
<head>
<style type='text/css'>
<%@ include file="../../../skit/css/jquery-ui.css"%>
<%@ include file="../../../skit/grid/pqgrid.min.css"%>
<%@ include file="../../../skit/grid/pqselect.min.css"%>
<%@ include file="../../../skit/grid/themes/office/pqgrid.css"%>
<%@ include file="../../../skit/grid/pqgrid-skit.css"%>
body {overflow-y:auto; overflow-x:auto; margin-top:3px; margin-left:0px; margin-bottom:0px; margin-right:0px }
</style>
</head>
<body>
<ww:property value='%{snapshot}' escape='false'/>
</body>
</html>