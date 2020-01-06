package com.googlecode.jslint4java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.focus.cos.web.common.JSChecker;
import com.focus.util.IOHelper;

public class JSLintTest {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		File file = new File("test/JSLintTest/javascript.js");
//		List<String> list = IOHelper.readLines(file, "UTF-8");
		String javascript = new String(IOHelper.readAsByteArray(file), "UTF-8");
		System.out.println(JSChecker.execute(javascript, 0));
	}

}
