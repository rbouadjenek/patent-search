/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.index;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author rbouadjenek
 */
public class TermFreqVector {

    private final Map<String, Integer> TermFreqVector;

    public TermFreqVector(Query query) throws IOException {
        TermFreqVector = new HashMap<>();
        if (query instanceof BooleanQuery) {
            for (BooleanClause bc : ((BooleanQuery) query).clauses()) {
                TermQuery qt = (TermQuery) bc.getQuery();
                TermFreqVector.put(qt.getTerm().text(), (int) qt.getBoost());
            }
        } else if (query instanceof TermQuery) {
            TermQuery qt = (TermQuery) query;
            TermFreqVector.put(qt.getTerm().text(), (int) qt.getBoost());
        }
    }

    public TermFreqVector(Terms terms) throws IOException {
        TermFreqVector = new HashMap<>();
        if (terms != null && terms.size() > 0) {
            TermsEnum termsEnum = terms.iterator(null); // access the terms for this field
            BytesRef term;
//            System.out.println("--------");
            while ((term = termsEnum.next()) != null) {// explore the terms for this field
                DocsAndPositionsEnum docsPosEnum = termsEnum.docsAndPositions(null, null);
                docsPosEnum.nextDoc();
                TermFreqVector.put(term.utf8ToString(), docsPosEnum.freq());
//                System.out.print(term.utf8ToString() + " " + docsPosEnum.freq() + " positions: "); //get the term frequency in the document
                for (int j = 0; j < docsPosEnum.freq(); j++) {
//                    System.out.print(docsPosEnum.nextPosition() + " ");
                }
//                System.out.println("");
//                System.out.print(term.utf8ToString()+" ");
            }
//            System.out.println("");
//            System.out.println("----------");
        }
    }

    public int size() {
        return TermFreqVector.size();
    }

    public Integer getFreq(String term) {
        return TermFreqVector.get(term);
    }

    public Set<String> getTerms() {
        return TermFreqVector.keySet();
    }

    public Collection<Integer> termFreqs() {
        return TermFreqVector.values();
    }

    public int numberOfTerms() {
        int sum = 0;
        for (int i : TermFreqVector.values()) {
            sum += i;
        }
        return sum;
    }

}
