/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nicta.com.au.patent.pac.search;

import org.apache.lucene.search.similarities.BM25Similarity;

/**
 *
 * @author rbouadjenek
 */
public class BM25Rocchio extends BM25Similarity {
    
    

    @Override
    protected float idf(long docFreq, long numDocs) {
        return 1; //To change body of generated methods, choose Tools | Templates.
    }
    
}
