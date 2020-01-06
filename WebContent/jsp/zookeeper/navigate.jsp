<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<SCRIPT type="text/javascript">
var memoryTree = "zookeeper!navigate.open";
var memoryServer = "zookeeper!navigate.server";
var titleOpen = "分布式协调服务(Zookeepe)";
var hrefOpen = "zookeeper!open.action";
</SCRIPT>
<html>
<%@ include file="/jsp/monitor/navigate.inc"%>
</html>