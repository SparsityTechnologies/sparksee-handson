package com.sparsity.sparksee.tutorial;

import com.sparsity.sparksee.gdb.AttributeKind;
import com.sparsity.sparksee.gdb.Condition;
import com.sparsity.sparksee.gdb.DataType;
import com.sparsity.sparksee.gdb.Database;
import com.sparsity.sparksee.gdb.Sparksee;
import com.sparsity.sparksee.gdb.SparkseeConfig;
import com.sparsity.sparksee.gdb.EdgesDirection;
import com.sparsity.sparksee.gdb.ExportType;
import com.sparsity.sparksee.gdb.Graph;
import com.sparsity.sparksee.gdb.Objects;
import com.sparsity.sparksee.gdb.ObjectsIterator;
import com.sparsity.sparksee.gdb.Session;
import com.sparsity.sparksee.gdb.Value;
import com.sparsity.sparksee.gdb.Values;
import com.sparsity.sparksee.gdb.ValuesIterator;
import com.sparsity.sparksee.script.ScriptParser;
import com.sparsity.sparksee.tutorial.utils.CommunityExport;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;


/**
 * Exercise 7.
 * <p>
 * Finding the communities of the knows subgraph.
 * 
 * @see Graph#newAttribute(int, java.lang.String, com.sparsity.sparksee.gdb.DataType, com.sparsity.sparksee.gdb.AttributeKind)
 */
public class Exercise2 {

    public static void main(String[] args) throws Exception {

        ScriptParser sp = new ScriptParser();

	long start = System.currentTimeMillis();
        //
        // Create schema
        //
        sp.parse("./data/database_small/scripts/schema.des", true, "");
        //
        // Load CSV files
        //
        sp.parse("./data/database_small/scripts/load.des", true, "");
	long end = System.currentTimeMillis();
	System.out.println("Creating time: "+(end-start)/1000D/60D+"m.");

        SparkseeConfig cfg = new SparkseeConfig();
        Sparksee sparksee = new Sparksee(cfg);
        Database db = sparksee.open("./data/graph_small.gdb", false);
        Session s = db.newSession();
        Graph graph = s.getGraph();
       
        //
        // Compute the communities of the knows graph
        //
        computeCommunities(s, graph);
           
        s.close();
        db.close();
        sparksee.close();
    }
    
    

    /**
     * Compute the communities of the knows subgraph.
     * <p>
     * 
     * Computes the communities of the knows subgraph by means of the label propagation algorithm.
     * Assign to each node a "label" indicating the community it belongs to. Use the CommunityExport
     * exporter provided to visualize the communities. Remember to remove
     * the attribute used to store the label before exiting the function.
     *
     * 
     * @param graph Graph.
     * 
     * 
     * @see Objects
     * @see ObjectsIterator
     * @see Graph#degree(long, int, com.sparsity.sparksee.gdb.EdgesDirection)
     */
    public static void computeCommunities(Session s, Graph graph) throws IOException {
        
        System.out.println("*********Communities *********");
        int tUser = graph.findType("User");
        int tnickname = graph.findAttribute(tUser, "nickname");
        int tknows = graph.findType("knows"); 
        int tlabel = graph.newAttribute(tUser, "label", DataType.Integer, AttributeKind.Basic);
        int i = 0;
        System.out.println("Initializing labels");
        //
        // Initialize the labels of each user
        //
        Objects users = graph.select(tUser);
        ObjectsIterator oit = users.iterator();
        while(oit.hasNext()){
            long oid = oit.next();
            Value v = new Value();
            v.setInteger(i++);
            graph.setAttribute(oid, tlabel, v);
        }
        oit.close();
        
        System.out.println("Spreading labels");
        //
        // While not converges, assign to each user the most common label of all of
        // its neighbors
        //
        boolean converged = false;
        while(!converged){
            converged = true;
            oit = users.iterator();
            while(oit.hasNext()){
                long oid = oit.next();
                HashMap<Integer, Integer> map = new HashMap<Integer,Integer>();
                
                //
                // Fill the map containing the number of occurrences of each label of the user knows 
                //
                Objects neighbors = graph.neighbors(oid,tknows,EdgesDirection.Outgoing);
                ObjectsIterator nit = neighbors.iterator();
                while(nit.hasNext()){
                    Value v = graph.getAttribute(nit.next(), tlabel);
                    Integer label = v.getInteger();
                    Integer currentValue = map.containsKey(label) ? map.get(label) : 0;
                    map.put(label, currentValue+1);
                }
                nit.close();
                neighbors.close();
                // 
                // Follow the map entries decreasingly first by occurrences, and then by label
                //
                if(map.size() > 0) {
                    List<Entry<Integer,Integer>> list = new LinkedList(map.entrySet());
                    Collections.sort(list, new Comparator< Entry<Integer,Integer> > () {
                        public int compare(Entry<Integer,Integer> r1, Entry<Integer,Integer> r2) {
                        Integer res = r1.getValue() - r2.getValue() != 0 ? r1.getValue() - r2.getValue() : r1.getKey() - r2.getKey();
                        return res*-1;
                       }
                    });
                                    
                    //
                    // Set the most frequent label if it is different than the current one
                    //
                    Entry<Integer,Integer> newLabel = list.iterator().next();
                    Value v = graph.getAttribute(oid,tlabel);
                    if(v.getInteger() != newLabel.getKey()){
                        v.setInteger(newLabel.getKey());
                        graph.setAttribute(oid, tlabel, v);
                        converged=false;
                    }
                }
            }
            oit.close();
        }
        users.close();
        
        System.out.println("Tagging small communities");
        //
        // Tag those communities smaller than 2 with a label of -1, for 
        // visualization purposes
        //
        Values vals = graph.getValues(tlabel);
        System.out.println("Number of communities: "+vals.count());
        ValuesIterator vit = vals.iterator();
        Objects toTag= s.newObjects();
        while(vit.hasNext()) {
            Objects community = graph.select(tlabel,Condition.Equal,vit.next());
            if(community.size() <= 2){
                ObjectsIterator comIt = community.iterator();
                while(comIt.hasNext()){
                    toTag.add(comIt.next());
                }
                comIt.close();
            }
            community.close();
        }
        vit.close();
        vals.close();
        ObjectsIterator toTagIt = toTag.iterator();
        /*while(toTagIt.hasNext()){
            Value v = new Value();
            v.setInteger(-1);
            graph.setAttribute(toTagIt.next(),tlabel,v);
        }*/
        toTag.close();
        
        //
        // Use the CommunityExport class to export the communities
        //
        graph.export("./data/communities.graphml", ExportType.YGraphML, new CommunityExport());
        
        //
        // Remove the label attribute
        //
        graph.removeAttribute(tlabel);
        System.out.println("*************************************");
   }
   
}
