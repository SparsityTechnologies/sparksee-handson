package com.sparsity.sparksee.tutorial;

import com.sparsity.sparksee.script.ScriptParser;

/**
 * Exercise 3.
 * <p>
 * Create a complete database using script loaders.
 *
 * @see ScriptParser
 */
public class Loader {

    public static void main(String[] args) throws Exception {
        ScriptParser sp = new ScriptParser();

	long start = System.currentTimeMillis();
        //
        // Create schema
        //
        sp.parse("./data/database/scripts/schema.des", true, "");
        //
        // Load CSV files
        //
        sp.parse("./data/database/scripts/load.des", true, "");
	long end = System.currentTimeMillis();
	System.out.println("Creating time: "+(end-start)/1000D/60D+"m.");
    }
}
