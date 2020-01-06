package com.focus.cos.web.login.vo;

import java.util.ArrayList;
import java.util.List;

public class Permission{

	private String viewId;
	private List<PermissionAction> actions = new ArrayList<PermissionAction>();
	
	
	public String getViewId() {
		return viewId;
	}
	public void setViewId(String viewId) {
		this.viewId = viewId;
	}
	public List<PermissionAction> getActions() {
		return actions;
	}
	
}
