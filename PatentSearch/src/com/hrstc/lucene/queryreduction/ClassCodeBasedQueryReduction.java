package com.hrstc.lucene.queryreduction;

import com.hrstc.lucene.queryexpansion.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.index.TermFreqVector;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;

/**
 * Implements Rocchio's pseudo feedback RocchioQueryExpansion algorithm
 * <p>
 * Query Expansion - Adding search terms to a user's search. Query expansion is
 * the process of a search engine adding search terms to a user's weighted
 * search. The intent is to improve precision and/or recall. The additional
 * terms may be taken from a thesaurus. For example a search for "car" may be
 * expanded to: car cars auto autos automobile automobiles [foldoc.org].
 *
 * To see options that could be configured through the properties file @see
 * Constants Section
 * <p>
 * Created on February 23, 2012, 5:29 AM
 * <p>
 * TODO: Yahoo started providing API to query www; could be nice to add yahoo
 * implementation as well
 * <p>
 * @author Neil O. Rouben
 */
public final class ClassCodeBasedQueryReduction {
    // CONSTANTS

    /**
     * Number of documents to use
     */
    public static final String DOC_NUM_FLD = "QE.doc.num";

    private final IndexReader ir;
//    private final String currentField;

    private final List<TermFreqVector> classCodeTermsVector;

    private final int Nbr_Terms;

    /**
     * Creates a new instance of QueryExpansion
     *
     * @param hits
     * @param ir
     * @param Nbr_Terms
     * @throws java.io.IOException
     */
    public ClassCodeBasedQueryReduction(TopDocs hits, IndexReader ir, int Nbr_Terms) throws IOException {
        this.ir = ir;
        classCodeTermsVector = getDocsTerms(hits);
        this.Nbr_Terms = Nbr_Terms;
    }

    /**
     * Performs Rocchio's query expansion with pseudo feedback qm = alpha *
     * query + ( beta / relevanDocsCount ) * Sum ( rel docs vector )
     *
     * @param query
     * @param currentField
     *
     * @return expandedQuery
     *
     * @throws IOException
     * @throws ParseException
     */
    public Query expandQuery(Query query, String currentField) throws IOException, ParseException {

        // Create combine documents term vectors - sum ( rel term vectors )
        //Get terms from relevant documents
        //Get terms from irrelevant documents
        // Adjust term features of the docs with alpha * query; and beta; and assign weights/boost to terms (tf*idf)
        Query expandedQuery = adjust(classCodeTermsVector, currentField, query);

        return expandedQuery;
    }

    /**
     * Adjust term features of the docs with alpha * query; and beta; and assign
     * weights/boost to terms (tf*idf).
     *
     * @param classCodeTermsVector of the terms of t
     * @param currentField
     * @param query
     *
     * @return expandedQuery with boost factors adjusted using Rocchio's
     * algorithm
     *
     * @throws IOException
     * @throws ParseException
     */
    public Query adjust(List<TermFreqVector> classCodeTermsVector, String currentField,
            Query query)
            throws IOException, ParseException {
        Query expandedQuery;
        // setBoost of query terms
        // Get queryTerms from the query
        TermFreqVector queryTermsVector = new TermFreqVector(query);

        Map<String, Float> qTerms = new HashMap<>();
        for (String termTxt : queryTermsVector.getTerms()) {
            float tf = queryTermsVector.getFreq(termTxt);
            qTerms.put(termTxt, tf);
        }
        List<TermFreqVector> v = new ArrayList<>();
        v.add(queryTermsVector);
        Map<String, TermQuery> queryTerms = setBoost(v, currentField);
        // setBoost of docs terms
        Map<String, TermQuery> classCodeTerms = setBoost(classCodeTermsVector, currentField);

        //----------------------
        float sum1 = 0;
        float sum2 = 0;
        for (Map.Entry<String, TermQuery> e : queryTerms.entrySet()) {
            TermQuery tq = e.getValue();
            sum1 += tq.getBoost();
        }

        for (Map.Entry<String, TermQuery> e : classCodeTerms.entrySet()) {
            TermQuery tq = e.getValue();
            sum2 += tq.getBoost();
        }

        for (Map.Entry<String, TermQuery> e : queryTerms.entrySet()) {
            String term = e.getKey();
            TermQuery tq = e.getValue();
            float tf = tq.getBoost() / sum1;
//            System.out.println("term: " + tq.getTerm().text());
//            System.out.println("tf= " + tq.getBoost() + "/" + sum1);
            float idf = (float) Math.log10((double) (sum2+2) / 1);
            
            if (classCodeTerms.containsKey(term)) {
                TermQuery tq2 = classCodeTerms.get(term);
                idf = (float) Math.log10((double) (sum2 + 2) / (tq2.getBoost() + 1));
//                System.out.println("idf= " + sum2 + "/" + tq2.getBoost() + "= " + idf);
            }else{
//                System.out.println("idf= "+idf);
            }
            tq.setBoost(tf * idf);
//            System.out.println("tfidf=" + tf * idf);
//            System.out.println("---------");
        }

        //------------------
//        Map<String, TermQuery> classCodeTerms=new HashMap<>();
        Comparator comparator = new QueryBoostComparator();

        List<TermQuery> queryTermsList = new ArrayList<>(queryTerms.values());
        Collections.sort(queryTermsList, comparator);
        queryTerms.clear();

        int querySize = queryTermsList.size() - Nbr_Terms;
        if (querySize < 0) {
            querySize = 0;
        }
//        System.out.println("*************");
        for (int i = 0; i < querySize; i++) {
            TermQuery tq = queryTermsList.get(i);
//            System.out.println("term: " + tq.getTerm().text());
//            System.out.println("tfdf= " + tq.getBoost());
//            System.out.println("----------");
            tq.setBoost(qTerms.get(tq.getTerm().text()));
            queryTerms.put(tq.getTerm().text(), tq);
        }

        expandedQuery = mergeQueries(new ArrayList<>(queryTerms.values()));

        return expandedQuery;
    }

    /**
     * Merges <code>termQueries</code> into a single query. In the future this
     * method should probably be in <code>Query</code> class. This is akward way
     * of doing it; but only merge queries method that is available is
     * mergeBooleanQueries; so actually have to make a string term1^boost1,
     * term2^boost and then parse it into a query
     *
     * @param termQueries - to merge
     *
     * @return query created from termQueries including boost parameters
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public Query mergeQueries(List<TermQuery> termQueries)
            throws ParseException {
        BooleanQuery query = new BooleanQuery();
        // Select only the maxTerms number of terms
        for (TermQuery termQuery : termQueries) {
            query.add(termQuery, BooleanClause.Occur.SHOULD);
        }
        return query;
    }

    /**
     * Extracts terms of the documents; Adds them to vector in the same order
     *
     * @param hits
     *
     * @return classCodeTerms docs must be in order
     * @throws java.io.IOException
     */
    public List<TermFreqVector> getDocsTerms(TopDocs hits)
            throws IOException {
        List<TermFreqVector> docsTerms = new ArrayList<>();
        // Process each of the documents
        for (int i = 0; i < hits.totalHits; i++) {
            ScoreDoc scoreDoc = hits.scoreDocs[i];
            Terms term = ir.getTermVector(scoreDoc.doc, PatentDocument.Title);                 //get termvector for document
            // Create termVector and add it to vector
            TermFreqVector docTerms = new TermFreqVector(term);
            docsTerms.add(docTerms);
        }
        return docsTerms;
    }

    /**
     * Sets boost of terms. boost = weight = factor(tf*idf)
     *
     *
     * @param vecsTerms
     * @param currentField
     * @return
     * @throws java.io.IOException
     */
    public Map<String, TermQuery> setBoost(List<TermFreqVector> vecsTerms, String currentField)
            throws IOException {
        Map<String, TermQuery> terms = new HashMap<>();
        // setBoost for each of the terms of each of the docs

        for (TermFreqVector docTerms : vecsTerms) {
            // Populate terms: with TermQuries and set boost
            for (String termTxt : docTerms.getTerms()) {
                // Create Term
                Term term = new Term(currentField, termTxt);
                // Calculate weight
                float tf = docTerms.getFreq(termTxt);

                // Create TermQuery and add it to the collection
                TermQuery termQuery = new TermQuery(term);
                // Calculate and set boost
                float boost = tf;

                if (boost != 0) {
                    termQuery.setBoost(boost);
                    if (terms.containsKey(termTxt)) {
                        TermQuery tq = terms.get(termTxt);
                        tq.setBoost(tq.getBoost() + termQuery.getBoost());
                    } else {
                        terms.put(termTxt, termQuery);
                    }
                }
            }
        }
        return terms;
    }

}
