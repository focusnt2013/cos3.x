package com.focus.cos.web.common;

import java.util.List;

import com.googlecode.jslint4java.Issue;
import com.googlecode.jslint4java.JSLint;
import com.googlecode.jslint4java.JSLintBuilder;
import com.googlecode.jslint4java.JSLintResult;
import com.googlecode.jslint4java.Option;

public class JSChecker 
{
	private static void print(int t, String text, StringBuffer sb)
	{
		if( t >= 0 ) sb.append("\r\n");
		while( t-- > 0 ){
			sb.append("\t");
		}
		sb.append(text);
	}
	/**
	 * 
	 * @param javascript
	 * @return 检查结果
	 */
	public static String execute(String javascript, int t)
	{
		javascript = javascript.trim();
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
		JSLintResult result = js.lint("javascript", javascript);
		List<Issue> issues = result.getIssues();
		StringBuffer sb = new StringBuffer();
		print(t, "检查JavaScript脚本(length:"+javascript.length()+", 以下行数是脚本相对行数)", sb);
        int last = -1;
        String lastLine = "";
        if (issues != null && issues.size() > 0) {  
            for (Issue issue : issues) 
            {
            	if( issue.getReason() != null && issue.getReason().endsWith("is better written in dot notation.") ) continue;
            	if( issue.getReason() != null && issue.getReason().equals("'jQuery' was used before it was defined.") ) continue;
            	int l = issue.getLine();
            	if( last != l ){
            		print(t, String.format("行%1$s: %2$s", l, issue.getEvidence().trim()), sb);
            		lastLine = "";
            	}
            	if( lastLine.equals(issue.getEvidence()) ) continue;
            	print(t+1, String.format("[%1$s] %2$s", getSeverity(issue.getReason()), issue.getReason()), sb);
            	last = l;
            	lastLine = issue.getEvidence();
            }  
        } 
        return sb.toString();
	}
	
	private static String getSeverity(String text)
	{
//		if( "Don't declare variables in a loop".equals(text) ) return "war";
		if( "Expected ';' and instead saw '}'.".equals(text) ) return "err";
		if( "Unexpected ';'.".equals(text) ) return "err";
		return "war";
	}
}
