<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<SCRIPT type="text/javascript">
var memoryTree = "files!navigate.open";
var memoryServer = "files!navigate.server";
var titleOpen = "集群文件管理器";
var hrefOpen = "files!open.action";
</SCRIPT>
<html>
<%@ include file="../monitor/navigate.inc"%>
</html>