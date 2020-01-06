package com.focus.util;

import java.util.ArrayList;

public abstract class Listener implements Runnable {
	protected Thread thread;
	protected boolean isRunning;
	protected boolean isBusy = true;
	protected long calculagraph;
	protected String remark;
	private static ArrayList<Object> threadQuene = new ArrayList<Object>();

	public Listener() {
		this.isRunning = false;
	}

	public static void printThreadStatus() {
		for (int i = 0; i < threadQuene.size(); i++) {
			String str = ((Listener) threadQuene.get(i)).status();
			if (str != null)
				System.out.println(str);
		}
	}

	public void start() {
		this.thread = new Thread(this);
		this.isRunning = true;
		this.thread.start();
		synchronized (threadQuene) {
			threadQuene.add(this);
		}
	}

	public void halt() {
		if ((this.thread != null) && (this.isRunning)) {
			Thread remover = this.thread;
			remover.interrupt();
			this.thread = null;
			this.isRunning = false;
			synchronized (threadQuene) {
				threadQuene.remove(this);
			}
		}
	}

	public void stop() {
		this.isRunning = false;
		synchronized (threadQuene) {
			threadQuene.remove(this);
		}
	}

	public String status() {
		if (this.remark == null)
			return null;
		return this.remark
				+ ":"
				+ (this.isRunning ? "空闲" : this.isBusy ? "忙["
						+ this.calculagraph + "]" : "线程已关闭");
	}
}