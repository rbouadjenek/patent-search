/*
 * QueryBoostComparator.java
 *
 * Created on February 23, 2005, 5:28 PM
 *
 * @author Neil O. Rouben
 */
package com.hrstc.lucene.queryexpansion;

import java.util.*;

import org.apache.lucene.search.*;

public class QueryBoostComparator implements Comparator {

 
    /**
     * Compares queries based on their boost Since want to be sorted in
     * decending order; comparison will be reversed
     * @param obj1
     * @param obj2
     */
    @Override
    public int compare(Object obj1, Object obj2) {
        Query q1 = (Query) obj1;
        Query q2 = (Query) obj2;
        return Float.compare(q2.getBoost(), q1.getBoost());
    }

}
