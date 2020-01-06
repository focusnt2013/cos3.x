package com.focus.cos.web.ops.action;

import com.focus.util.Tools;

public class FilesActionTest {

	public static void main(String[] args) {
		byte[] v = new byte[4];
		v[0] = 3;
		v[1] = 17;
		v[2] = 5;
		v[3] = 16;
		System.out.println(Tools.bytesToInt(v));
		v[0] = 3;
		v[1] = 17;
		v[2] = 4;
		v[3] = 16;
		System.out.println(Tools.bytesToInt(v));
	}

}
