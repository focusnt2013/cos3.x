package com.focus.weixin;

import java.lang.Thread.UncaughtExceptionHandler;

public class CallbackUncaughtExceptionHandler implements UncaughtExceptionHandler {
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		System.out.println("Found the exception of uncaught: "+e.getMessage()+"\r\n\tfrom "+t.getId()+"("+t.getName()+")");
	}
}
