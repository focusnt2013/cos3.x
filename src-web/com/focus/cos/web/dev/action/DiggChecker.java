package com.focus.cos.web.dev.action;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;

/**
 * 数据钻取日志
 * @author focus
 *
 */
public class DiggChecker {
	protected StringBuffer sblog = new StringBuffer(); 
	private int teststep = 0;
	private boolean avaliable;
	
	public DiggChecker(boolean avaliable)
	{
		this.avaliable = avaliable;
	}

	public boolean isAvaliable() {
		return avaliable;
	}
	
	/**
	 * 将数据直接写入日志
	 * @param o
	 */
	protected void print(Object o)
	{
		sblog.append("\r\n\r\n<span style='color:#0099cc'>------------------------------------------------------------------------------------------------------------------------------------------------------------------------</span>");
		sblog.append(o.toString());
		sblog.append("\r\n<span style='color:#0099cc'>------------------------------------------------------------------------------------------------------------------------------------------------------------------------</span>\r\n");
	}
	/**
	 * 逐条打印的方式写入
	 * @param t
	 * @param text
	 * @param args
	 */
	protected void print(int t, String text, Object... args)
	{
		if(!avaliable) return;
		if( text == null || text.isEmpty() ) return;
		final DecimalFormat df = new java.text.DecimalFormat("00");
		for(int i = 0; i < args.length; i++)
		{
			args[i] = "<span style='color:#33ffff'>"+args[i]+"</span>";
		}
		text = args.length>0?String.format(text, args):text;
		if( t == 0 ) sblog.append("\r\n<span style='color:#0099cc'>------------------------------------------------------------------------------------------------------------------------------------------------------------------------</span>");
		if( t >= 0 ) sblog.append("\r\n["+df.format(teststep++)+"] ");
		while( t-- > 0 ){
			sblog.append("    ");
		}
		sblog.append(text);
	}
	protected void error(int t, String text, Object... args)
	{
		if(!avaliable) return;
		if( text == null || text.isEmpty() ) return;
		final DecimalFormat df = new java.text.DecimalFormat("00");
		for(int i = 0; i < args.length; i++)
		{
			args[i] = "<span style='color:red'>"+args[i]+"</span>";
		}
		text = String.format(text, args);
		if( t == 0 ) sblog.append("\r\n<span style='color:#0099cc'>------------------------------------------------------------------------------------------------------------------------------------------------------------------------</span>");
		if( t >= 0 ) sblog.append("\r\n["+df.format(teststep++)+"] ");
		while( t-- > 0 ){
			sblog.append("    ");
		}
		sblog.append(text);
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
}
