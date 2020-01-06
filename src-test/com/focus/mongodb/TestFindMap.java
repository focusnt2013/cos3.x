package com.focus.mongodb;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;

public class TestFindMap {

	public static void main(String[] args)
	{
		MongoClient mongo = null;
		try
		{
			MongoDatabase db = null;
			List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
			MongoCredential credential = MongoCredential.createScramSha1Credential("ljqread", "admin", "ljqread20170703".toCharArray());
			credentialsList.add(credential);
			ServerAddress serverAddress = new ServerAddress("122.115.40.75", 30011); 
			mongo = new MongoClient(serverAddress, credentialsList);
			db = mongo.getDatabase("baike");
			MongoCollection<Document> col = db.getCollection("works_measure");
			Document where = new Document("uniqueId", "b4b844486ca98ecc5d7bced18cf792e2");
			where.put("date", "2017-08-03");
			FindIterable<Document> find = col.find(where);
			MongoCursor<String>  distinct = col.distinct("source_from", String.class).iterator(); 
	        while( distinct.hasNext() )
	        {
	        	System.err.println(distinct.next());
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
