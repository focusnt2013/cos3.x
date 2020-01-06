<%@page language="java" pageEncoding="UTF-8"%>
<%@ page import="com.focus.cos.web.common.Kit"%>
<%@ taglib uri="/webwork" prefix="ww" %>
<%Kit.noCache(request,response);  %>
<html>
<head>
<style type='text/css'>
body {
	overflow-x:hidden;  
    background: #f1f1f1;  
    font:14px/26px '微软雅黑','宋体',Arail;  
}
.maintable {  
    width:640px;  
    left:50%;  
    margin-left:-320px;
    background: #f1f1f1;  
    font:14px/26px '微软雅黑','宋体',Arail;  
    margin:0 auto;
    color:#555; 
}
pre	{
    width:590px;  
	margin: 1px 0 0 0;
	padding: 7px;
	border: 0;
	border: 1px dotted #fef8de;
	background: #fffdf2;
	font-size: 8pt;
    color:#fd561e; 
    white-space: pre-wrap;       
	white-space: -moz-pre-wrap;  
	white-space: -pre-wrap;      
	white-space: -o-pre-wrap;    
	word-wrap: break-word;    
}
</style>
</head>
<body>
    <table cellpadding="0" cellspacing="0" border="0" class="maintable">
        <tr style="background-color:#fff;">
            <td style="padding:30px 38px;">
                <div>
                <table width='100%' cellpadding="0" cellspacing="0" border="0"  style="background:#000000;">
                <tr><td style='padding-left:5px; padding-top:5px;' colspan='2'><h2 style="font-size:16px; color:#fff; font-weight:600;margin:0 0 5px 0;"><ww:property value="alarm.module"/> 告警</h2></td></tr>
                <tr><td style='padding-left:5px; font-size:16px; color: #fd561e; padding-bottom:5px;' colspan='2'><ww:property value="alarm.alarmTitle"/></td></tr>
                <tr><td style='padding-left:5px;color: #bebbb6' width='100'>告警类型：</td><td style='color: #fff'><ww:property value="alarm.orgtype"/></td></tr>
                <tr><td style='padding-left:5px;color: #bebbb6'>告警级别：</td><td><span style='background-color:<ww:property value="alarm.severityColor"/>;
                	font-weight:bold;color:<ww:property value="alarm.severityFontColor"/>;border:1px solid #fff;padding-left:10px;
                	padding-right:10px;text-align:center;'><ww:property value="alarm.orgseverity"/></span></td></tr>
                <tr><td style='padding-left:5px;color: #bebbb6'>告警网元：</td><td style='color: #fff'><ww:property value="alarm.dn"/></td></tr>
                <tr><td style='padding-left:5px;color: #bebbb6'>告警时间：</td><td style='color: #fff'><ww:property value="alarm.time"/></td></tr>
                <tr><td style='padding-left:5px;color: #bebbb6'>告警原因：</td><td style='color: #fff'><ww:property value="alarm.probablecause"/></td></tr>
                <tr><td style='padding-left:5px;color: #bebbb6'>程序负责人：</td><td style='color: #fff'><ww:property value="account"/></td></tr>
                <tr><td style='padding-left:5px;color: #bebbb6; padding-bottom:5px;  padding-right:5px;' colspan='2'>告警详情：
                    <pre>
<ww:property value="alarm.alarmText"/>
                    </pre>
               		</td>
               	</tr>
               	<ww:if test='alarm.contextUrl!=null'>
                <tr><td colspan='2' align='center' style='color: #fff'>
                <iframe src='<ww:property value="alarm.contextUrl"/>' scrolling='no' frameborder='0' style='width:580px; height:220px'></iframe>
                </td></tr>
                </ww:if>
                </table>
                </div>
            </td>
        </tr>
    </table>
</body>
</html>