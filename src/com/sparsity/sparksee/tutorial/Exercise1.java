package com.sparsity.sparksee.tutorial;

import com.sparsity.sparksee.gdb.AttributeKind;
import com.sparsity.sparksee.gdb.Condition;
import com.sparsity.sparksee.gdb.DataType;
import com.sparsity.sparksee.gdb.Database;
import com.sparsity.sparksee.gdb.Sparksee;
import com.sparsity.sparksee.gdb.SparkseeConfig;
import com.sparsity.sparksee.gdb.EdgesDirection;
import com.sparsity.sparksee.gdb.Graph;
import com.sparsity.sparksee.gdb.Objects;
import com.sparsity.sparksee.gdb.ObjectsIterator;
import com.sparsity.sparksee.gdb.Order;
import com.sparsity.sparksee.gdb.Session;
import com.sparsity.sparksee.gdb.Value;
import com.sparsity.sparksee.gdb.Values;
import com.sparsity.sparksee.gdb.ValuesIterator;

/**
 * Exercise 5.
 * <p>
 * Compute the most popular users.
 *
 * @see Graph#newAttribute(int, java.lang.String,
 * com.sparsity.sparksee.gdb.DataType, com.sparsity.sparksee.gdb.AttributeKind)
 */
public class Exercise1
{

public static void main(String[] args) throws Exception
{
	SparkseeConfig cfg = new SparkseeConfig();
	Sparksee sparksee = new Sparksee(cfg);
	Database db = sparksee.open("./data/graph.gdb", false);
	Session s = db.newSession();
	Graph graph = s.getGraph();
        //
	// Create the new attribute
	//
	int tUser = graph.findType("User");
	int newAttr = graph.newAttribute(tUser, "rank", DataType.Double, AttributeKind.Basic);

	// Extend the graph with "admires" edges
	createAdmiresEdges2(graph, s);

        //
	// Compute and store attribute value for each User
	//
	computePageRank(graph, newAttr);
        //
	// Get the most popular users and print out some attribute for each
	// of them.
	//
	getTheMostPopularUsers(graph, newAttr, 10);

        //
	// Remove the attribute
	//
	graph.removeAttribute(newAttr);

	s.close();
	db.close();
	sparksee.close();
}

private static void createAdmiresEdges(Graph graph)
{

	System.out.println("Creating admires edges");
	int tAdmires = graph.newEdgeType("admires", true, true);
	int tUser = graph.findType("User");
	int tLikes = graph.findType("likes");
	int tWrites = graph.findType("creates");
	Objects users = graph.select(tUser);
	ObjectsIterator oit = users.iterator();
	int counter = 0;
	while (oit.hasNext())
	{
		long id = oit.next();
		Objects likes = graph.neighbors(id, tLikes, EdgesDirection.Outgoing);
		ObjectsIterator lit = likes.iterator();
		while (lit.hasNext())
		{
			long messageId = lit.next();
			Objects creator = graph.neighbors(messageId, tWrites, EdgesDirection.Ingoing);
			long admiredId = creator.any();
			creator.close();
			if (graph.findEdge(tAdmires, id, admiredId) == Objects.InvalidOID)
			{
				graph.newEdge(tAdmires, id, admiredId);
			}
		}
		lit.close();
		likes.close();
		counter++;
		if (counter % 100 == 0)
		{
			System.out.println("Number of users processed: " + counter + " out of " + users.count());

		}
	}
	oit.close();
	users.close();
}

private static void createAdmiresEdges2(Graph graph, Session sess)
{

	System.out.println("Creating admires edges");
	int tAdmires = graph.newEdgeType("admires", true, true);
	int tUser = graph.findType("User");
	int tLikes = graph.findType("likes");
	int tWrites = graph.findType("creates");
	Objects users = graph.select(tUser);
	ObjectsIterator oit = users.iterator();
	int threshold = 10;
	int counter = 0;
	while (oit.hasNext())
	{
		long id = oit.next();
		Objects candidates = sess.newObjects();
		Objects likes = graph.neighbors(id, tLikes, EdgesDirection.Outgoing);
		ObjectsIterator lit = likes.iterator();
		while (lit.hasNext())
		{
			long messageId = lit.next();
			Objects creator = graph.neighbors(messageId, tWrites, EdgesDirection.Ingoing);
			long admiredId = creator.any();
			candidates.add(admiredId);
			creator.close();
		}
		lit.close();

		ObjectsIterator candidatesIt = candidates.iterator();
		while (candidatesIt.hasNext())
		{
			long cid = candidatesIt.next();
			Objects messages = graph.neighbors(cid, tWrites, EdgesDirection.Outgoing);
			Objects intersection = Objects.combineIntersection(likes, messages);
			if (intersection.size() > threshold && graph.findEdge(tAdmires, id, cid) == Objects.InvalidOID)
			{
				graph.newEdge(tAdmires, id, cid);
			}
			intersection.close();
			messages.close();
		}
		candidatesIt.close();
		likes.close();

		counter++;
		if (counter % 1000 == 0)
		{
			System.out.println("Number of users processed: " + counter + " out of " + users.count());

		}
		candidates.close();
	}
	oit.close();
	users.close();
}

/**
 * Compute and store the page rank for each User.
 *
 * @param graph Graph.
 * @param attr User's attribute to store computed values.
 *
 * @see Objects
 * @see ObjectsIterator
 * @see Graph#degree(long, int, com.sparsity.sparksee.gdb.EdgesDirection)
 */
public static void computePageRank(Graph graph, int attr)
{

	System.out.println("Computing PageRank");
	int tUser = graph.findType("User");
	int tAdmires = graph.findType("admires");

        //
	// Initialize the pagerank attribute of each node to 1.0
	//
	Value v = new Value();
	v.setDouble(1.0);
	Objects users = graph.select(tUser);
	ObjectsIterator oit = users.iterator();
	while (oit.hasNext())
	{
		graph.setAttribute(oit.next(), attr, v);
	}
	oit.close();

        //
	// Perform 10 pagerank iterations
	//
	for (int i = 0; i < 10; ++i)
	{
		System.out.println("Starting PageRank iteration " + i);
		oit = users.iterator();
		while (oit.hasNext())
		{
			long oid = oit.next();
                //
			// Compute the pagerank contribution for each of the user  
			// (remember to consider directions)
			//
			double accum = 0.0;
			Objects neighbors = graph.neighbors(oid, tAdmires, EdgesDirection.Ingoing);
			ObjectsIterator nit = neighbors.iterator();
			while (nit.hasNext())
			{
				long neighbor = nit.next();
				long outDegree = graph.degree(neighbor, tAdmires, EdgesDirection.Outgoing);
				graph.getAttribute(neighbor, attr, v);
				if (outDegree > 0)
				{
					accum += v.getDouble() / outDegree;
				}
			}
			nit.close();
			neighbors.close();
                //
			// Compute and store the new pagerank for the user
			//
			v.setDouble((1 - 0.85) + 0.85 * accum);
			graph.setAttribute(oid, attr, v);
		}
		oit.close();
	}
	users.close();
}

/**
 * Prints the names for the top-k most popular Users along with their page rank.
 * <p>
 * Most popular Users are those which have the higher pagerank values This
 * values was previously stored into the given attribute.
 *
 * @param graph Graph.
 * @param attr User's attribute which stores the pagerank.
 * @param k The number of elements to be print.
 * @return Collection of OIDs for the most popular Users.
 *
 * @see Values
 * @see ValuesIterator
 */
public static void getTheMostPopularUsers(Graph graph, int attr, int k)
{
        //
	// Retrieve values ordered in an descendent way
	// (the first will be the highest Value).
	//
	int tUser = graph.findType("User");
	int aNick = graph.findAttribute(tUser, "nickname");
	Values values = graph.getValues(attr);
	ValuesIterator vit = values.iterator(Order.Descendent);

        //
	// Print, for each value, up to k users with that value.
	// Proceed with the next value unless k users have already been print
	//
	int printed = 0;
	while (vit.hasNext() && printed < k)
	{
		Value v = vit.next();
		Objects object = graph.select(attr, Condition.Equal, v);
		ObjectsIterator oit = object.iterator();
		while (oit.hasNext() && printed < k)
		{
			long oid = oit.next();
			Value nickName = graph.getAttribute(oid, aNick);
			System.out.println(nickName.getString() + " " + v.getDouble());
			printed++;
		}
		oit.close();
		object.close();
	}
	vit.close();
	values.close();
}
}
