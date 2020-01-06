package com.focus.control;

import java.io.Serializable;

public class Storage implements Serializable {

	private static final long serialVersionUID = -1825100027708038040L;

	private String address;
	private double totalSpace;
	private double ableSpace;
	private double percent;
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public double getTotalSpace() {
		return totalSpace;
	}
	public void setTotalSpace(double totalSpace) {
		this.totalSpace = totalSpace;
	}
	public double getAbleSpace() {
		return ableSpace;
	}
	public void setAbleSpace(double ableSpace) {
		this.ableSpace = ableSpace;
	}
	public double getPercent() {
		return percent;
	}
	public void setPercent(double percent) {
		this.percent = percent;
	}
	
}
