package com.focus.util;

import java.util.zip.ZipFile;

public class ZipTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			ZipFile zip = new ZipFile("d:/Sys.zip");
			zip.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
