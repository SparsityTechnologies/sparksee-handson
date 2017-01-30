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
	createAdmiresEdges(graph, s);

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


private static void createAdmiresEdges(Graph graph, Session sess)
{
	//  
	// TODO: Create admire edges from likes to users
	//
	
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
	// TODO: Initialize the pagerank attribute of each node to 1.0
	//
	Value v = new Value();
	v.setDouble(1.0);
	Objects users = graph.select(tUser);
	
	

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
			double accum = 0.0;
			//
			// TODO: Compute the pagerank contribution for each of the user  
			// (remember to consider directions)
			//
			
			
			//
			// TODO: Compute and store the new pagerank for the user
			//
			
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
	// TODO: Retrieve values ordered in an descendent way
	// (the first will be the highest Value).
	//
	

        //
	// TODO: Print, for each value, up to k users with that value.
	// Proceed with the next value unless k users have already been print
	//
	
}
}
