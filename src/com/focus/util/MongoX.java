package com.focus.util;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

public class MongoX
{
	private static String Address = "";
	private static int Port = 0;
	private static String Username = "";
	private static String Password = "";
	private static String Database = "";
	private static MongoClient Mongo = null;
	private static long Timestamp = 0;
	/**
	 * 得到数据库连接
	 * @param database
	 * @param tablename
	 * @return
	 * @throws Exception
	 */
	public static MongoCollection<Document> getDBCollection(String tablename) throws Exception
	{
		try
		{
			MongoDatabase db = null;
			if( Mongo != null )
			{
				if( Tools.MILLI_OF_MINUTE > System.currentTimeMillis() - Timestamp )
				{
					try
					{
						db = Mongo.getDatabase(Database);
						if( db == null )
						{
							Mongo.close();
							Mongo = null;
						}
					}
					catch(Exception e)
					{
						Mongo.close();
						Mongo = null;
					}
				}
			}
			if( Mongo == null )
			{
				List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
				MongoCredential credential = MongoCredential.createScramSha1Credential(Username, Database, Password.toCharArray());
				credentialsList.add(credential);
				ServerAddress serverAddress = new ServerAddress(Address, Port); 
				Mongo = new MongoClient(serverAddress, credentialsList);
				// 当程序被关闭的时候钩子会被回调
				Runtime.getRuntime().addShutdownHook(new Thread()
				{
					public void run()
					{
						if( Mongo != null )Mongo.close();
					}
				});
				Timestamp = System.currentTimeMillis();
			}
			if( db == null )
			{
				db = Mongo.getDatabase(Database);
			}
//			else if( !Mongo.getConnector().isOpen() )
//			{
////				System.out.println("Found mongo disconnect("+mongo.getConnector().getAddress()+").");
//				Mongo.close();
//				Mongo = new MongoClient(Address, Port);
//				Map.clear();
//			}
	        return db.getCollection(tablename);
		}
		catch(Exception e)
		{
			if( Mongo != null ) Mongo.close();
			throw e;
		}
	}

	/**
	 * 局部更新某个字段
	 * @param collection
	 * @param where
	 * @param set
	 */
	public static UpdateResult update(MongoCollection<Document> collection, BasicDBObject where, String field, DBObject set)
	{
		BasicDBObject updateSetValue = new BasicDBObject("$set",new BasicDBObject(field, set));
		return collection.updateMany(where, updateSetValue);
	}
	
	public static UpdateResult update(MongoCollection<Document> collection, BasicDBObject where, Document set)
	{
		BasicDBObject updateSetValue = new BasicDBObject("$set",set);
		return collection.updateMany(where, updateSetValue);
	}

	public static UpdateResult update(String table, BasicDBObject where, Document set) throws Exception
	{
		MongoCollection<Document> collection = getDBCollection(table);
		BasicDBObject updateSetValue = new BasicDBObject("$set",set);
		return collection.updateMany(where, updateSetValue);
	}
	
	/**
	 * 插入一条记录
	 * @param databse
	 * @param table
	 * @param set
	 * @return
	 * @throws Exception
	 */
	public static void insert(String table, Document set) throws Exception
	{
		MongoCollection<Document> coll = getDBCollection(table);
		coll.insertOne(set);
	}

	public static void insert(MongoCollection<Document> coll, Document set) throws Exception
	{
		coll.insertOne(set);
	}

	public static Document findOne(String table, BasicDBObject where) throws Exception
	{
		MongoCollection<Document> coll = getDBCollection(table);
		FindIterable<Document> find = coll.find(where);
		MongoCursor<Document> cursor = find.iterator();
		if(cursor.hasNext())
		{
			return cursor.next();
		}
		return null;
	}

	public static Document findOne(MongoCollection<Document> coll, BasicDBObject where) throws Exception
	{
		FindIterable<Document> find = coll.find(where);
		MongoCursor<Document> cursor = find.iterator();
		if(cursor.hasNext())
		{
			return cursor.next();
		}
		return null;
	}

	public static Document findById(String table, String _id) throws Exception
	{
		MongoCollection<Document> coll = getDBCollection(table);
		FindIterable<Document> find = coll.find(new BasicDBObject("_id", new ObjectId(_id)));
		MongoCursor<Document> cursor = find.iterator();
		if(cursor.hasNext())
		{
			return cursor.next();
		}
		return null;
	}
	
	
	public static boolean exist(MongoCollection<Document> coll, BasicDBObject where) throws Exception
	{
		return coll.count(where)>0;
	}
	
	public static boolean exist(String table, BasicDBObject where) throws Exception
	{
		MongoCollection<Document> coll = getDBCollection(table);
		return coll.count(where)>0;
	}
	/**
	 * 计数器加1
	 * @param collection
	 * @param key
	 * @param value
	 * @param field
	 * @throws Exception
	 */
	public static long count(MongoCollection<Document> collection, String key, Object value ,String field) throws Exception
	{
		Document cond = new Document();
		cond.put(key, value);
		if( collection.count(cond) == 0 )
		{
			collection.insertOne(cond);
		}
		BasicDBObject update = new BasicDBObject("$inc", new BasicDBObject(field, 1));
		Document result = collection.findOneAndUpdate(cond, update);
		Object obj = result.get(field);
		Long count = 0L;
		if( obj != null ) count = Long.valueOf(obj.toString());
		return count;
	}

	/**
	 * 计数器加1
	 * @param collection
	 * @param key
	 * @param value
	 * @param field
	 * @throws Exception
	 */
	public static long sum(MongoCollection<Document> collection, BasicDBObject where, String field, int value) throws Exception
	{
		Document result = collection.findOneAndUpdate(where, new BasicDBObject("$inc", new BasicDBObject(field, value)));
		if( result == null ) return 0;
		Object obj = result.get(field);
		Long count = 0L;
		if( obj != null ) count = Long.valueOf(obj.toString());
		return count;
	}
	public static Document sum(MongoCollection<Document> collection, BasicDBObject where, BasicDBObject set) throws Exception
	{
		return collection.findOneAndUpdate(where, new BasicDBObject("$inc", set));
	}
	
	public static void setAddress(String address) {
		Address = address;
	}

	public static void setPort(int port) {
		Port = port;
	}

	public static void setUsername(String username) {
		Username = username;
	}

	public static void setPassword(String password) {
		Password = password;
	}
	
	public static void setDatabase(String database) {
		Database = database;
	}
}
