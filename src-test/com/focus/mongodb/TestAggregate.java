package com.focus.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.json.JSONArray;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class TestAggregate {

	static StringBuffer sb = new StringBuffer("Map:");
	static int aa = 0;


	public static void main(String[] args) {
		MongoClient mongo = null;
		try {
			MongoDatabase db = null;
			List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
			MongoCredential credential = MongoCredential
					.createScramSha1Credential("spider", "admin",
							"spdrkLygaj".toCharArray());
			credentialsList.add(credential);
			ServerAddress serverAddress = new ServerAddress("122.115.40.75",
					30011);
			mongo = new MongoClient(serverAddress, credentialsList);
			db = mongo.getDatabase("baike");
			MongoCollection<Document> col = db.getCollection("works_measure");
			ArrayList<BasicDBObject> list = new ArrayList<BasicDBObject>();
			BasicDBObject match = new BasicDBObject();
			match.put("date", "2017-08-04");
			match.put("uniqueId", "461cb49a7b832729c1de74fcbc715470");
			list.add(new BasicDBObject("$match", match));
			BasicDBObject group = new BasicDBObject();
			group.put("_id", new BasicDBObject("source_from", "$source_from"));
			group.put("count_measure", new BasicDBObject("$sum", 1));
			list.add(new BasicDBObject("$group", group));
			System.err.println((new JSONArray(list.toString()).toString(4)));
			MongoCursor<Document> cursor = col.aggregate(list).iterator();
			while (cursor.hasNext()) {
				Object obj = cursor.next();
				System.err.println(obj.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (mongo != null) {
				mongo.close();
			}
		}
	}
}
