package com.focus.cos.web.action;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.googlecode.jslint4java.Issue;
import com.googlecode.jslint4java.JSLint;
import com.googlecode.jslint4java.JSLintBuilder;
import com.googlecode.jslint4java.JSLintResult;
import com.googlecode.jslint4java.Option;

/**
 * 模版工作模式
 * @author focus
 *
 */
public class TemplateChecker {
	public final static int WORK = 0;
	public final static int TEST = 1;
	public final static int DEBUG = 2;
	public final static int DEMO = 3;
	/*工作模式0 正常;1 测试;2 调测 3 样式预览*/
	private int mode;
	protected StringBuffer sblog = new StringBuffer(); 
	protected int testerr;//记录错误个数
	protected int testwar;//记录错误个数
	private int teststep = 0;
	protected JSONArray dynamicParameter = new JSONArray();
	private HashMap<String, String> values;
	
	public TemplateChecker(int mode,HashMap<String, String> values)
	{
		this.mode = mode;
		this.values = values;
	}

	/**
	 * 参数处理
	 * @param text
	 * @param request
	 * @param data
	 * @param skip
	 * @param nodeName
	 * @param nodeAttr
	 * @return
	 * @throws Exception
	 */
	public String parameter(String text, HttpServletRequest request)
			throws Exception
	{
		return parameter(text, request, null, null, null, true);
	}
	/**
	 * 控制取参数是否强校验
	 * @param text
	 * @param request
	 * @param skip
	 * @return
	 * @throws Exception
	 */
	public String parameter(String text, HttpServletRequest request, boolean skip)
			throws Exception
	{
		return parameter(text, request, null, null, null, skip, false);
	}

	public String parameter(String text, HttpServletRequest request, JSONObject data)
			throws Exception
	{
		return parameter(text, request, data, null, null, true, false);
	}
	
	public String parameter(String text, HttpServletRequest request, String nodeName, String nodeAttr)
		throws Exception
	{
		return parameter(text, request, null, nodeName, nodeAttr, false, false);
	}

	public String parameter(String text, HttpServletRequest request, String nodeName, String nodeAttr, boolean skip)
		throws Exception
	{
		return parameter(text, request, null, nodeName, nodeAttr, skip, false);
	}

	public String javascript(String text, HttpServletRequest request)
		throws Exception
	{
		return this.parameter(text, request, null, "javascript", "cdata", false, true);
	}

	public String javascript(String text, HttpServletRequest request, String nodeName)
		throws Exception
	{
		return this.parameter(text, request, null, nodeName, "cdata", false, true);
	}
	
	public String parameter(String text, HttpServletRequest request, JSONObject data, String nodeName, String nodeAttr, boolean skip)
		throws Exception
	{
		return parameter(text, request, null, nodeName, nodeAttr, skip, false);
	}
	public String parameter(String text, HttpServletRequest request, JSONObject data, String nodeName, String nodeAttr, boolean skip, boolean nullit)
		throws Exception
	{
        StringBuffer sbMatch = null;
        StringBuffer result = new StringBuffer();
        int len = text.length();
        for( int i = 0; i < len; i++ )
        {
            char c = text.charAt( i );
            if( c == '%' )
            {
                if( sbMatch == null )
                {
                    sbMatch = new StringBuffer();
                }
                else if( sbMatch.length() > 32 )
                {
                	result.append( "%" );
                	result.append(sbMatch.toString());
                	result.append( "%" );
                    sbMatch = null;
                }
                else
                {
                    String property = null;
                    if( data != null ){
                    	if( data.has(sbMatch.toString()) ){
                    		property = data.get(sbMatch.toString()).toString();
                    	}
                    	else{
                    		return text;
                    	}
                    }
                    if( values != null ){
                    	if( values.containsKey(sbMatch.toString()) ){
                    		property = values.get(sbMatch.toString());
                    	}
                    }
                    if( (property == null ) ){
                    	property = request.getParameter(sbMatch.toString());
                    }
                    if( property == null ){
                    	property = System.getProperty(sbMatch.toString(), skip?null:"");
                    }
                    if( property == null )
                    {
                		if( this.getMode() > TemplateChecker.WORK || skip ){
                			/*JSONObject e = new JSONObject();
                			e.put("node", nodeName);
                			e.put("attr", nodeAttr);
                			e.put("regex", sbMatch.toString());
                			dynamicParameter.put(e);*/
                			if( !nullit ){
                				result.append( "%" );
                				result.append(sbMatch.toString());
                				result.append( "%" );
                			}
                			sbMatch = null;
                		}
                		else
                		{
	                		StringBuffer sb = new StringBuffer();
	                		sb.append("未能从HTTP-Request或System.properties获得配置的参数值[%"+sbMatch+"%]:\r\n"+text+", the parameters of below ");
	                		Iterator<Map.Entry<String, String[]>> iterator = request.getParameterMap().entrySet().iterator();
	                		while(iterator.hasNext())
	                		{
	                			Map.Entry<String, String[]> e = iterator.next();
	                			sb.append("\r\n\t");
	                			sb.append(e.getKey());
	                			sb.append("=");
	                			for(String value : e.getValue())
	                				sb.append(value+"\t");
	                		}
                			throw new Exception("未能从HTTP-Request获得配置的参数值[%"+sbMatch+"%]:\r\n"+text+"\r\n"+sb.toString());
                		}
                    }
                    else
                    {
                    	if( !skip && nodeName != null ){
                    		JSONObject e = new JSONObject();
                    		e.put("node", nodeName);
                    		e.put("attr", nodeAttr);
                    		e.put("regex", sbMatch.toString());
                    		e.put("value", property);
                    		setDynamicParameter(e);
                    	}
                    	result.append( property );
	                    sbMatch = null;
                    }
                }
            }
            else if( sbMatch == null )
            {
            	result.append( c );
            }
            else if( sbMatch != null )
            {
                sbMatch.append( c );
            }
        }
        if( sbMatch != null )
        {
        	result.append( "%" );
        	result.append(sbMatch.toString());
        }
        return result.toString();
	}
	
	protected void test(int t, String text, Object... args)
	{
		if( text == null || text.isEmpty() ) return;
		final DecimalFormat df = new java.text.DecimalFormat("00");
		for(int i = 0; i < args.length; i++)
		{
			String color = "#33ffff";
			if( "严重错误".equals(args[i]) ){
				color = "#DB0A5B";
			}
			else if( "警告提醒".equals(args[i]) ){
				color = "#ffd800";
			}
			args[i] = "<span style='color:"+color+"'>"+args[i]+"</span>";
		}
		text = String.format(text, args);
		if( t == 0 ) sblog.append("\r\n<span style='color:#0099cc'>------------------------------------------------------------------------------------------------------------------------------------------------------------------------</span>");
		if( t >= 0 ) sblog.append("\r\n["+df.format(teststep++)+"] ");
		while( t-- > 0 ){
			sblog.append("    ");
		}
		sblog.append(text);
	}
	
	protected boolean hasDynamicParameter()
	{
		return dynamicParameter.length()>0;
	}
	
	protected String printDynamicParameter(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < dynamicParameter.length(); i++)
		{
			JSONObject e =  dynamicParameter.getJSONObject(i);
			sb.append("\r\n\t["+(i+1)+"]节点"+e.getString("node")+"属性"+e.getString("attr")+"需要参数%"+e.getString("regex")+"%.");
		}
		return sb.toString();
	}
	
	protected void setDynamicParameter(JSONObject e){
		this.dynamicParameter.put(e);
	}
	
	/**
	 * 写异常数据到检查日志
	 * @param e
	 */
	public void write(Exception e){
		ByteArrayOutputStream out1 = new ByteArrayOutputStream(1024);
		PrintStream ps = new PrintStream(out1);
		e.printStackTrace(ps);
		this.sblog.append(out1.toString());
	}
	
	public void err()
	{
		this.testerr += 1;
	}

	public int errcount()
	{
		return this.testerr;
	}

	public void war()
	{
		this.testwar += 1;
	}

	public int warcount()
	{
		return this.testwar;
	}
	/**
	 * 执行检查
	 * @param javascript
	 * @param t
	 */
	protected void js(String javascript, int t){
		js(null, javascript, t);
	}
	protected void js(String globalscript, String javascript, int t){
		sblog.append(jscheck(javascript, t, globalscript));
	}
	/**
	 * 
	 * @param javascript
	 * @return 检查结果
	 */
	private String jscheck(String javascript, int t, String globalscript)
	{
		if( mode == 0 ) return null;
		javascript = javascript.trim();
		if( javascript.startsWith("function(") )
		{
			javascript = "function a("+javascript.substring("function(".length());
		}
		else if( javascript.startsWith("function (") )
		{
			javascript = "function a("+javascript.substring("function (".length());
		}
		JSLintBuilder builder = new JSLintBuilder();
		JSLint js = builder.fromDefault();
		js.addOption(Option.VARS);
		js.addOption(Option.TODO);
		js.addOption(Option.FORIN);
		js.addOption(Option.WHITE);
		js.addOption(Option.EVIL);
		js.addOption(Option.EQEQ);
		js.addOption(Option.CONTINUE);
        js.addOption(Option.SLOPPY);  
        js.addOption(Option.WHITE);  
        js.addOption(Option.EQEQ);  
        js.addOption(Option.NOMEN);  
        js.addOption(Option.CONTINUE);  
        js.addOption(Option.REGEXP);  
        js.addOption(Option.VARS);  
        js.addOption(Option.PLUSPLUS);
        if( globalscript != null && !globalscript.isEmpty() ){
        	javascript = globalscript+"\r\n"+javascript;
        }
		JSLintResult result = js.lint("javascript", javascript);
		List<Issue> issues = result.getIssues();
		StringBuffer sb = new StringBuffer();
		print(t, "检查JavaScript脚本(length:"+javascript.length()+", 以下行数是脚本相对行数)", sb);
        int last = -1;
        String lastLine = "";
        int count_err = 0;
        int count_war = 0;
        if (issues != null && issues.size() > 0) {  
            for (Issue issue : issues) 
            {
            	if( issue.getReason() == null )
            		continue;

            	if( issue.getReason().endsWith("Unexpected 'return'.") ) continue;
            	if( issue.getReason().endsWith("skit_alert' was used before it was defined.") ) continue;
            	if( issue.getReason().endsWith("valueLabels' was used before it was defined.") ) continue;
            	if( issue.getReason().endsWith("formData' was used before it was defined.") ) continue;
            	if( issue.getReason().endsWith("nowtime' was used before it was defined.") ) continue;
            	if( issue.getReason().endsWith("getNonce' was used before it was defined.") ) continue;
            	if( issue.getReason().endsWith("parent' was used before it was defined.") ) continue;
            	if( issue.getReason().endsWith("dataTemp' was used before it was defined.") ) continue;
            	if( issue.getReason().endsWith("rowIndx' was used before it was defined.") ) continue;
            	if( issue.getReason().endsWith("window' was used before it was defined.") ) continue;
            	if( issue.getReason().endsWith("top' was used before it was defined.") ) continue;
            	if( issue.getReason().endsWith("is better written in dot notation.") ) continue;
            	if( issue.getReason().equals("'preupload' was used before it was defined.") ) continue;
            	if( issue.getReason().equals("'dataRemote' was used before it was defined.") ) continue;
            	if( issue.getReason().equals("'jQuery' was used before it was defined.") ) continue;
            	if( issue.getReason().equals("'document' was used before it was defined.") ) continue;
            	if( issue.getReason().equals("'remoteData' was used before it was defined.") ) continue;
            	if( issue.getReason().equals("'callbackRowData' was used before it was defined.") ) continue;
            	if( issue.getReason().equals("'dataLocalPlus' was used before it was defined.") ) continue;
            	if( issue.getReason().equals("'rowData' was used before it was defined.") ) continue;
            	if( issue.getReason().equals("'alert' was used before it was defined.") ) continue;
            	if( issue.getReason().equals("'addRow' was used before it was defined.") ) continue;
            	if( issue.getReason().equals("'$grid' was used before it was defined.") ) continue;
            	if( issue.getReason().equals("addRow($grid);") ) continue;
            	if( issue.getReason().equals("Unused 'previewId'.") ) continue;
            	if( issue.getReason().equals("Unused 'event'.") ) continue;
            	if( issue.getReason().equals("Unused 'index'.") ) continue;
            	if( issue.getReason().equals("''openPopupDialog' was used before it was defined.") ) continue;
            	if( issue.getReason().startsWith("preupload") ) continue;
            	int l = issue.getLine();
            	if( last != l ){
            		print(t, String.format("行%1$s: %2$s", l, (issue.getEvidence()!=null?issue.getEvidence().trim():"null")), sb);
            		lastLine = "";
            	}
            	if( lastLine.equals(issue.getEvidence()) ) continue;
            	String severity = getSeverity(issue.getReason());
            	if( severity.equals("提醒") ){
            		count_war += 1;
            		testwar += 1;
            	}
            	else{
            		count_err += 1;
            		testerr += 1;
            	}
            	print(t, String.format("[%1$s] %2$s", severity, issue.getReason()), sb);
            	last = l;
            	lastLine = issue.getEvidence();
            }  
        } 
    	print(t, String.format("发现影响执行的JS错误<span style='color:red'>%s</span>个，发现语法可优化的提示<span style='color:#0099cc'>%s</span>个.", count_err, count_war), sb);
        return sb.toString();
	}

	private static void print(int t, String text, StringBuffer sb)
	{
		if( t >= 0 ) sb.append("\r\n");
		while( t-- > 0 ){
			sb.append("\t");
		}
		if( text.startsWith("[提醒]") ){
			sb.append("<span style='background:yellow;color:#0099cc'>");
		}
		else if( text.startsWith("[错误]") ){
			sb.append("<span style='background:red'>");
		}
		else
		{
			sb.append("<span style='background:#cfcfcf'>");
		}
		sb.append(text);
		sb.append("</span>");
	}
	
	private String getSeverity(String text)
	{//
//		if( "Don't declare variables in a loop".equals(text) ) return "war";
		if( "Expected '{' and instead saw 'for'.".equals(text) ) return "错误";
		if( "Expected '(end)' and instead saw '}'.".equals(text) ) return "错误";
		if( text.startsWith("Expected ';' and instead saw") ) return "错误";
		if( "Unexpected ';'.".equals(text) ) return "错误";
		if( "Unclosed string.".equals(text) ) return "错误";
		if( "Stopping. (100% scanned).".equals(text) ) return "错误";
		if( "Move 'var' declarations to the top of the function.".equals(text) ) return "错误";
		return "提醒";
	}
	public String toString(){
		return this.sblog.toString();
	}

	public byte[] payload(){
		try {
			return this.sblog.toString().getBytes("UTF-8");
		} catch (Exception e) {
		}
		return "没有检查结果".getBytes();
	}
	
	public int getMode(){
		return mode;
	}
}
