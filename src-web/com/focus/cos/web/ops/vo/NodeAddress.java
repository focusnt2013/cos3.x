package com.focus.cos.web.ops.vo;

import com.focus.cos.web.common.Kit;


/**
 * 节点索引地址
 * @author pingxl
 *
 */
public class NodeAddress implements Comparable<Object>{

	//地址
	private String address;
	//目录名称
	private String name;
	//值
	private byte[] data;
	//深度
	private int depth;
	//是否叶子节点
	private boolean isLeaf;
	//子节点个数
	private int subLeafCount;
	//节点是否存在
	private boolean isExist;

	public boolean isExist() {
		return isExist;
	}
	public void setExist(boolean isExist) {
		this.isExist = isExist;
	}
	public String getValueLenthStr() {
		return data!=null?Kit.bytesScale(data.length):"-";
	}
	public long getValueLenth() {
		return data!=null?data.length:-1;
	}
	public boolean isLeaf() {
		return isLeaf;
	}
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getValue() {
		return data==null?"N/A":"";
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public int compareTo(Object o) {
		if(o instanceof NodeAddress){
			NodeAddress address = (NodeAddress)o;
			return address.getName().compareTo(this.name);
		}else
			return 0;
	}
	public int getSubLeafCount() {
		return subLeafCount;
	}
	public void setSubLeafCount(int subLeafCount) {
		this.subLeafCount = subLeafCount;
	}
	
}
