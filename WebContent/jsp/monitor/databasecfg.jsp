<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<SCRIPT type="text/javascript">
var memoryTree = "monitorcfg!databases.open";
var memoryServer = "monitorcfg!databases.server";
var titleOpen = "JDBC数据库监控配置";
var hrefOpen = "monitorcfg!database.action";
</SCRIPT>
<html>
<%@ include file="navigate.inc"%>
</html>