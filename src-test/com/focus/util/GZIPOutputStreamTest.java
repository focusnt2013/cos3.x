package com.focus.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPOutputStreamTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
        	File file = new File("COS重要流程.xlsx");
        	byte[] payload = IOHelper.readAsByteArray(file);
			GZIPOutputStream gos = new GZIPOutputStream(out);
			gos.write(payload, 0, payload.length);
			gos.flush();
			gos.finish();
			gos.close();
			byte[] getload = out.toByteArray();
			System.err.println(payload.length+":"+getload.length);
			
			GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(getload));
			payload = new byte[1024*1024];
    		int size = payload.length;
    		int off = 0;
    		int len = 0;
			while( (len = gis.read(payload, off, size)) != -1  )
    		{
				size -= len;
				off += len;
    		}
			System.out.println(off);
		} 
        catch (Exception e) 
        {
			e.printStackTrace();
		}
	}

}
