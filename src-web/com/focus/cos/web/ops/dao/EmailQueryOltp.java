package com.focus.cos.web.ops.dao;

import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.digg.OLTP;
import com.focus.util.Tools;

public class EmailQueryOltp extends OLTP {

	@Override
	public void handle(JSONObject row, JSONObject digg)
	{
		JSONArray attachments = new JSONArray();
		if( row.has("ATTACHMENTS") )
		{
			String[] as = Tools.split(row.getString("ATTACHMENTS"), ";");
			for( String attachment : as)
			{
				String args[] = Tools.split(attachment, ", ");
				if( args.length == 0 || args[0].isEmpty() ) continue;
				String path = args[0];
				String attachmentFilename = null;
				if( args.length == 1 )
				{
					int j = path.lastIndexOf('/');
					if( j == -1 ) continue;
					attachmentFilename = attachment.substring(j);
					int i = attachmentFilename.lastIndexOf('.');
					if( i == -1 ) attachmentFilename = "未知文件";
				}
				else
				{
					attachmentFilename = args[1];
				}
				JSONObject a = new JSONObject();
				a.put("name", attachmentFilename);
				a.put("path", path);
				attachments.put(a);
			}
		}
		row.put("attachments", attachments);
		row.put("previewurl", "email!preview.action?eid="+row.getLong("EID"));
	}
}
