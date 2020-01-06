package com.focus.cos.api;

import java.util.ArrayList;
import java.util.List;

public class Sysrole {
	/*角色ID*/
	private int id;
	/*角色名称*/
	private String name;
	/*角色ID*/
	private List<Sysrole> children = new ArrayList<Sysrole>();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Sysrole> getChildren() {
		return children;
	}
	
	
}
