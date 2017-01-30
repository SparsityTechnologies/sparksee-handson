package com.sparsity.sparksee.tutorial;

import com.sparsity.sparksee.algorithms.SinglePairShortestPathBFS;
import com.sparsity.sparksee.algorithms.TraversalBFS;
import com.sparsity.sparksee.gdb.Database;
import com.sparsity.sparksee.gdb.Sparksee;
import com.sparsity.sparksee.gdb.SparkseeConfig;
import com.sparsity.sparksee.gdb.EdgesDirection;
import com.sparsity.sparksee.gdb.Graph;
import com.sparsity.sparksee.gdb.Objects;
import com.sparsity.sparksee.gdb.ObjectsIterator;
import com.sparsity.sparksee.gdb.Session;
import com.sparsity.sparksee.gdb.Type;
import com.sparsity.sparksee.gdb.Value;
import java.io.FileNotFoundException;

/**
 * Exercise 4.
 * <p>
 * Learn to do basic queries.
 *
 * @see Graph
 */
public class Walkthrough
{

public static void main(String[] args) throws FileNotFoundException, Throwable
{
	SparkseeConfig cfg = new SparkseeConfig();
	Sparksee sparksee = new Sparksee(cfg);
	Database db = sparksee.open("./data/graph.gdb", false);
	Session s = db.newSession();
        //
	// Compute the messages from user
	//
	messagesFromUser(s, "Mirza Kalich Ali");
	
        //
	// Compute the friends of user
	//
	knownUsers(s, "Mirza Kalich Ali");
	
        //
	// Compute messages which share two hashtags
	//
	commonHashtags(s, "Mohandas_Karamchand_Gandhi", "Kanye_West");
	
        //
	// Compute the distance between two users
	//
	distanceBetweenUsers(s, "Claudio Pinto", "Anatoly Shevchenko");

	s.close();
	db.close();
	sparksee.close();
}

/**
 * Get messages from a given User.
 * <p>
 * Procedure:
 * <ul>
 * <li>Find the OID of the User.</li>
 * <li>Retrieve its Messages (its neighbours).</li>
 * </ul>
 *
 * @param sess User session.
 * @param nick Nickname of the User.
 *
 * @see Graph#findType(java.lang.String)
 * @see Graph#findAttribute(int, java.lang.String)
 * @see Graph#getObjectType(long)
 * @see Graph#select(int, com.sparsity.sparksee.gdb.Condition,
 * com.sparsity.sparksee.gdb.Value)
 * @see Graph#neighbors(long, int, com.sparsity.sparksee.gdb.EdgesDirection)
 * @see Objects
 * @see ObjectsIterator
 */
public static void messagesFromUser(Session sess, String nick)
{
	Graph g = sess.getGraph();
	Value v = new Value();
        //
	// Find out the OID of the User with the given nickname.
	//
	int tUser = g.findType("User");
	if (tUser == Type.InvalidType)
		System.out.println("\"User\" invalid type");
	int aNick = g.findAttribute(tUser, "nickname");
	v.setString(nick);
	long oidUser = g.findObject(aNick, v);
        //
	// Retrieve Messages for the found User's OID and print out
	// some attributes.
	//
	int tMessages = g.findType("creates");
	Objects objs = g.neighbors(oidUser, tMessages, EdgesDirection.Outgoing);
	ObjectsIterator it = objs.iterator();
	int tMessage = g.findType("Message");
	int aBody = g.findAttribute(tMessage, "body");
	System.out.println("messagesFromUser:");
	while (it.hasNext())
	{
		long oidMessage = it.next();
		g.getAttribute(oidMessage, aBody, v);
		System.out.println(v.getString());
	}
	it.close();
	objs.close();
}

/**
 * Get friends from a given User.
 * <p>
 * Procedure:
 * <ul>
 * <li>Find the OID of the User.</li>
 * <li>Retrieve its friends (its neighbours).</li>
 * </ul>
 *
 * @param sess User session.
 * @param nick Nickname of the User.
 *
 * @see Graph#findType(java.lang.String)
 * @see Graph#findAttribute(int, java.lang.String)
 * @see Graph#getObjectType(long)
 * @see Graph#select(int, com.sparsity.sparksee.gdb.Condition,
 * com.sparsity.sparksee.gdb.Value)
 * @see Graph#neighbors(long, int, com.sparsity.sparksee.gdb.EdgesDirection)
 * @see Objects
 * @see ObjectsIterator
 */
public static void knownUsers(Session sess, String nick)
{
	Graph g = sess.getGraph();
	Value v = new Value();
        //
	// Find out the OID of the User with the given nickname.
	//
	int tUser = g.findType("User");
	int aNick = g.findAttribute(tUser, "nickname");
	v.setString(nick);
	long oidUser = g.findObject(aNick, v);
        //
	// Retrieve known users for the found User's OID and print out
	// some attributes.
	//
	int tknows = g.findType("knows");
	Objects objs = g.neighbors(oidUser, tknows, EdgesDirection.Ingoing);
	ObjectsIterator it = objs.iterator();
	int aNickname = g.findAttribute(tUser, "nickname");
	System.out.println("Knows :");
	while (it.hasNext())
	{
		long oidFollower = it.next();
		g.getAttribute(oidFollower, aNickname, v);
		System.out.println(v.getString());
	}
	it.close();
	objs.close();
}

/**
 * Get common Messages for the given Hashtags.
 * <p>
 * Procedure:
 * <ul>
 * <li>Get Messages for each Hastag.</li>
 * <li>Intersect retrieved collections.</li>
 * <ul>
 *
 * @param sess User session.
 * @param ht1 Hashtag.
 * @param ht2 Hashtag.
 *
 * @see Graph#findType(java.lang.String)
 * @see Graph#findAttribute(int, java.lang.String)
 * @see Graph#findObject(int, com.sparsity.sparksee.gdb.Value)
 * @see Graph#neighbors(long, int, com.sparsity.sparksee.gdb.EdgesDirection)
 * @see Objects
 */
public static void commonHashtags(Session sess, String ht1, String ht2)
{
	Graph g = sess.getGraph();
	Value v = new Value();
        //
	// Find out the OID of the Hashtags with the given hastag's texts.
	//
	int tTag = g.findType("Hashtag");
	int aName = g.findAttribute(tTag, "name");
	v.setString(ht1);
	long oid1 = g.findObject(aName, v);
	v.setString(ht2);
	long oid2 = g.findObject(aName, v);

        //
	// Retrieve Messages with both hashtags and intersect the
	// retrieved collection of Messages.
	//
	int ttags = g.findType("has");
	Objects objs1 = g.neighbors(oid1, ttags, EdgesDirection.Ingoing);
	Objects objs2 = g.neighbors(oid2, ttags, EdgesDirection.Ingoing);
	objs1.intersection(objs2);
	ObjectsIterator it = objs1.iterator();
	int tMessage = g.findType("Message");
	int aBody = g.findAttribute(tMessage, "body");
	System.out.println("common Hashtags:");
	while (it.hasNext())
	{
		long oidMessage = it.next();
		g.getAttribute(oidMessage, aBody, v);
		System.out.println(v.getString());
	}
	it.close();
	objs1.close();
	objs2.close();
}

/**
 * Get the distance between two given Users.
 * <p>
 * Procedure:
 * <ul>
 * <li>Find the OID for each User.</li>
 * <li>Compute the distance ({@link TraversalBFS}).</li>
 * <ul>
 *
 * @param sess User session.
 * @param n1 Nickname of a User.
 * @param n2 Nickname of a User.
 *
 * @see Graph#findType(java.lang.String)
 * @see Graph#findAttribute(int, java.lang.String)
 * @see Graph#findObj(long, edu.upc.dama.sparksee.core.Value)
 * @see Objects
 * @see Objects
 * @see Graph#explode(long, int, com.sparsity.sparksee.gdb.EdgesDirection)
 */
public static void distanceBetweenUsers(Session sess, String n1, String n2) throws Throwable
{
	Graph g = sess.getGraph();

	int userNodeType = g.findType("User");
	int knowsEdgeType = g.findType("knows");
	int nicknameAttribute = g.findAttribute(userNodeType, "nickname");

        //
	// Obtain the oid's of the two nodes
	//
	long begin = g.findObject(nicknameAttribute, (new Value()).setString(n1));
	long end = g.findObject(nicknameAttribute, (new Value()).setString(n2));
        //
	// Use the SinglePairShortestPathBFS API to find the shortest path between them.
	//
	SinglePairShortestPathBFS bfs = new SinglePairShortestPathBFS(sess, begin, end);
	bfs.addEdgeType(knowsEdgeType, EdgesDirection.Any);
	bfs.addNodeType(userNodeType);
	bfs.run();
	double length = bfs.getCost();
	System.out.println("Length : " + length);
}
}
