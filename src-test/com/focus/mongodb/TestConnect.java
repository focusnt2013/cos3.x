package com.focus.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class TestConnect {

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
			ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>();
			BasicDBObject match = new BasicDBObject();
			match.put("score", new BasicDBObject("$ne", null));
			list.add(new BasicDBObject("$match", match));
			BasicDBObject group = new BasicDBObject();
//			group.put("_id", new BasicDBObject("score", "$score") );
			group.put("_id", new BasicDBObject("subject_id", "$subject_id") );
			group.put("crawl_time", new BasicDBObject("$max", "$crawl_time"));
			group.put("score", new BasicDBObject("$max", "$score"));
//			group.put("count", new BasicDBObject("$sum", 1));
//			group.put("proportion_one_star", new BasicDBObject("$max", "$proportion_one_star"));
			list.add(new BasicDBObject("$group", group));
			list.add(new BasicDBObject("$sort", new BasicDBObject("score", -1)));
			list.add(new BasicDBObject("$skip", 0));
			list.add(new BasicDBObject("$limit", 10));
			System.err.println(list.toString());
			MongoCursor<Document> cursor = col.aggregate(list).iterator();
//			System.out.println(col.count(new BasicDBObject("work_id","201708021556302062")));
//			MongoCursor<Document> cursor = col.find(new BasicDBObject("work_id","201708021556302062")).iterator();
			int i = 0;
			while(cursor.hasNext())
			{
				i += 1;
				Document obj = cursor.next();
				System.err.println(i+":"+obj.toJson());
			}
			System.out.print(i);
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
