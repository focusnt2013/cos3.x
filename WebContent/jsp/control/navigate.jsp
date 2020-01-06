<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<SCRIPT type="text/javascript">
var memoryTree = "control!navigate.open";
var memoryServer = "control!navigate.server";
var titleOpen = "集群程序管理器";
var hrefOpen = "control!open.action";
</SCRIPT>
<html>
<%@ include file="/jsp/monitor/navigate.inc"%>
</html>