package com.focus.cos.control;

import com.focus.util.Log;

public class DirStateOueryTest {

	public static void main(String[] args) {

        //启动日志管理器
        Log.getInstance().setSubroot( "DirStateOuery" );
        Log.getInstance().setDebug( true );
        Log.getInstance().setLogable( true );
        Log.getInstance().start();
		DirStateQuery test = new DirStateQuery(){
			@Override
			public void notify(DirState state) {
				DirState state1 = get("/maven/test");
				System.err.println(state1);
				state1 = get("/maven/test/test/src/main/java/focusnt");
				System.err.println(state1);
			}
		};
		test.createScan("/maven");
		test.createScan("/maven");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		test.close();
	}
}

