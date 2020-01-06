<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
	<title><ww:property value='systemName'/></title>
    <link href="images/logo1.png" rel="icon" type="image/png">
    <link href="skin/defone/css/bootstrap.min.css" rel="stylesheet">
    <link href="skin/defone/css/font-awesome.css" rel="stylesheet">
</head>

<body>
	<div class="container-fluid">
	    <div class="row">
	        <div class="col-lg-6" style="width:100%">
	            <div class="panel panel-default">
	                <div class="panel-heading">
	                    演示所有的图标
	                </div>
	                <!-- /.panel-heading -->
	                <div class="panel-body">
					    <ul>
					    	<ww:iterator value="listData" status="loop">
					        <li style="float:left;width:192px;font-size:11pt">
					        	<i class="fa <ww:property value="toString()"/> animated flip"></i><ww:property value="toString()"/></li>
					        </ww:iterator>
					    </ul>
	                </div>
	                <!-- /.panel-body -->
	            </div>
	            <!-- /.panel -->
	        </div>
	        <!-- /.col-lg-6 -->
	    </div>
	    <!-- /.row -->
	</div>
    <!-- /#wrapper -->
<script type="text/javascript">
</script>
    </body>
</html>