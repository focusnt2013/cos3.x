package com.focus.util;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public abstract class QueneListener extends Listener {
	public LinkedList<Object> queueObj;
	public long _duration_busy = 0L;
	public long _duration_global = 0L;
	public int busy_count = 0;
	public double speed = 0.0D;
	public double busy = 0.0D;
	public static final int COUNT_PACKET_PENDING = 200;

	public QueneListener() {
		this._duration_global = System.currentTimeMillis();
		this.queueObj = new LinkedList<Object>();
	}

	public synchronized void post(Object object) {
		this.queueObj.addLast(object);
		notify();
	}

	public final synchronized Object peek() {
		try {
			return this.queueObj.poll();
		} catch (NoSuchElementException e) {
		}

		return null;
	}

	public void capability() {
		this._duration_global = (System.currentTimeMillis() - this._duration_global);

		this.speed = (200000.0D / this._duration_busy);

		this.busy = (this._duration_busy * 100L / this._duration_global);
		this.busy_count = 0;
		this._duration_busy = 0L;
		this._duration_global = System.currentTimeMillis();
	}
}
