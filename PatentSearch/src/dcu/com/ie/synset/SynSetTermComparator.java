/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dcu.com.ie.synset;

import java.util.Comparator;
import java.util.Map;
import org.apache.lucene.search.Query;

/**
 *
 * @author rbouadjenek
 */
public class SynSetTermComparator implements Comparator {
    /**
     * Compares queries based on their boost Since want to be sorted in
     * decending order; comparison will be reversed
     * @param obj1
     * @param obj2
     */
    @Override
    public int compare(Object obj1, Object obj2) {
        Map.Entry<String, Double> q1 = (Map.Entry<String, Double>) obj1;
        Map.Entry<String, Double> q2 = (Map.Entry<String, Double>) obj2;
        return Double.compare(q2.getValue(), q1.getValue());
    }
}
