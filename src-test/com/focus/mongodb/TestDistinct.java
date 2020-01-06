package com.focus.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class TestDistinct {

	static StringBuffer sb = new StringBuffer("Map:");
	static int aa = 0;
	public static void main(String[] args)
	{
		MongoClient mongo = null;
		try
		{
			MongoDatabase db = null;
			List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
			MongoCredential credential = MongoCredential.createScramSha1Credential("spider", "admin", "spdrkLygaj".toCharArray());
			credentialsList.add(credential);
			ServerAddress serverAddress = new ServerAddress("122.115.40.75", 30011); 
			mongo = new MongoClient(serverAddress, credentialsList);
			db = mongo.getDatabase("spider_data_douban");
			MongoCollection<Document> col = db.getCollection("movie");
			Document match = new Document();
			String _id = "year";
			DistinctIterable<String> result = null;
			if( !match.isEmpty() ){
				result = col.distinct(_id, String.class).filter(match);
			}
			else{
				result = col.distinct(_id, String.class);
			}
			MongoCursor<?> cursor = result.iterator();
			while(cursor.hasNext())
			{
				Object obj = cursor.next();
				System.err.println(obj.toString());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if( mongo != null ){
				mongo.close();
			}
		}
	}
}
