package com.hrstc.lucene.queryexpansion;

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
public final class ClassCodeBasedQueryExpansion {
    // CONSTANTS

    /**
     * how much importance of document decays as doc rank gets higher. decay =
     * decay * rank 0 - no decay
     */
    public static final String DECAY_FLD = "QE.decay";
    /**
     * Number of documents to use
     */
    public static final String DOC_NUM_FLD = "QE.doc.num";

    /**
     * Rocchio Params
     */
    public static final String ROCCHIO_ALPHA_FLD = "rocchio.alpha";
    public static final String ROCCHIO_BETA_FLD = "rocchio.beta";

    private final Map<String, Float> parameters;
    private final IndexReader ir;
//    private final String currentField;

    private final List<TermFreqVector> docsTermVectorReldocs;

    private final int Nbr_Terms;

    /**
     * Creates a new instance of QueryExpansion
     *
     * @param hits
     * @param ir
     * @param parameters
     * @param Nbr_Terms
     * @throws java.io.IOException
     */
    public ClassCodeBasedQueryExpansion(TopDocs hits, IndexReader ir, Map<String, Float> parameters, int Nbr_Terms) throws IOException {
        this.ir = ir;
        this.parameters = parameters;
        docsTermVectorReldocs = getDocsTerms(hits);
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
        // Load Necessary Values from Properties
        float alpha = parameters.get(ClassCodeBasedQueryExpansion.ROCCHIO_ALPHA_FLD);
        float beta = parameters.get(ClassCodeBasedQueryExpansion.ROCCHIO_BETA_FLD);
        float decay = parameters.get(ClassCodeBasedQueryExpansion.DECAY_FLD);

        // Create combine documents term vectors - sum ( rel term vectors )
        //Get terms from relevant documents
        //Get terms from irrelevant documents
        // Adjust term features of the docs with alpha * query; and beta; and assign weights/boost to terms (tf*idf)
        Query expandedQuery = adjust(docsTermVectorReldocs, currentField, query, alpha, beta, decay, Nbr_Terms);

        return expandedQuery;
    }

    /**
     * Adjust term features of the docs with alpha * query; and beta; and assign
     * weights/boost to terms (tf*idf).
     *
     * @param docsTermsVectorRelevant of the terms of t
     * @param currentField
     * @param query
     * @param alpha - factor of the equation
     * @param beta - factor of the equation
     * @param decay
     * @param maxExpandedQueryTerms - maximum number of terms in expanded query
     *
     * @return expandedQuery with boost factors adjusted using Rocchio's
     * algorithm
     *
     * @throws IOException
     * @throws ParseException
     */
    public Query adjust(List<TermFreqVector> docsTermsVectorRelevant, String currentField,
            Query query, float alpha, float beta, float decay, int maxExpandedQueryTerms)
            throws IOException, ParseException {
        Query expandedQuery;

        // setBoost of docs terms
        Map<String, TermQuery> relevantDocsTerms = setBoost(docsTermsVectorRelevant, currentField, beta, decay);

//        Map<String, TermQuery> relevantDocsTerms=new HashMap<>();
        Comparator comparator = new QueryBoostComparator();

        List<TermQuery> expandedQueryTerms = new ArrayList<>(relevantDocsTerms.values());
        Collections.sort(expandedQueryTerms, comparator);
        relevantDocsTerms.clear();
        int termCount = Math.min(expandedQueryTerms.size(), maxExpandedQueryTerms);
        for (int i = 0; i < termCount; i++) {
            TermQuery tq = expandedQueryTerms.get(i);
            relevantDocsTerms.put(tq.getTerm().text(), tq);
        }

        // setBoost of query terms
        // Get queryTerms from the query
        TermFreqVector queryTermsVector = new TermFreqVector(query);

        Map<String, TermQuery> queryTerms = setBoost(queryTermsVector, currentField, alpha);
        // combine weights according to expansion formula
        expandedQueryTerms = combine(queryTerms, relevantDocsTerms);
        // Sort by boost=weight

        Collections.sort(expandedQueryTerms, comparator);

        // Create Expanded Query
        expandedQuery = mergeQueries(expandedQueryTerms);

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
     * @return relevantDocsTerms docs must be in order
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
     * @param termVector
     * @param currentField
     * @param factor
     * @return
     * @throws java.io.IOException
     */
    public Map<String, TermQuery> setBoost(TermFreqVector termVector, String currentField, float factor)
            throws IOException {
        List<TermFreqVector> v = new ArrayList<>();
        v.add(termVector);
        return setBoost(v, currentField, factor, 0);
    }

    /**
     * Sets boost of terms. boost = weight = factor(tf*idf)
     *
     *
     * @param vecsTerms
     * @param currentField
     * @param factor - adjustment factor ( ex. alpha or beta )
     * @param decayFactor
     * @return
     * @throws java.io.IOException
     */
    public Map<String, TermQuery> setBoost(List<TermFreqVector> vecsTerms, String currentField, float factor, float decayFactor)
            throws IOException {
        Map<String, TermQuery> terms = new HashMap<>();
        // setBoost for each of the terms of each of the docs
        int i = 0;
        float norm = (float) 1 / vecsTerms.size();

        for (TermFreqVector docTerms : vecsTerms) {
            // Increase decay
            float decay = decayFactor * i;
            // Populate terms: with TermQuries and set boost
            for (String termTxt : docTerms.getTerms()) {
                // Create Term
                Term term = new Term(currentField, termTxt);
                // Calculate weight
                float tf = docTerms.getFreq(termTxt);
//                float idf = ir.docFreq(term);
                int docs = ir.getDocCount(PatentDocument.Title);
                float idf = (float) Math.log10((double) docs / (ir.docFreq(new Term(PatentDocument.Title, termTxt)) + 1));
                float weight = tf * idf;
                // Adjust weight by decay factor
                weight = weight - (weight * decay);
                // Create TermQuery and add it to the collection
                TermQuery termQuery = new TermQuery(term);
                // Calculate and set boost
                float boost = factor * weight;

                if (vecsTerms.size() == 1) {
                    boost = factor * tf;
                } else {
                    boost = factor;
                }

                if (boost != 0) {
                    termQuery.setBoost(boost * norm);
                    if (terms.containsKey(termTxt)) {
                        TermQuery tq = terms.get(termTxt);
                        tq.setBoost(tq.getBoost() + termQuery.getBoost());
                    } else {
                        terms.put(termTxt, termQuery);
                    }
                }
            }
            i++;
        }
        return terms;
    }

    /**
     * combine weights according to expansion formula
     *
     * @param queryTerms
     * @param relevantDocsTerms
     * @return
     */
    public List<TermQuery> combine(Map<String, TermQuery> queryTerms, Map<String, TermQuery> relevantDocsTerms) {
        // Add Terms of the relevant documents
        for (Map.Entry<String, TermQuery> e : queryTerms.entrySet()) {
            if (relevantDocsTerms.containsKey(e.getKey())) {
                TermQuery tq = relevantDocsTerms.get(e.getKey());
                tq.setBoost(tq.getBoost() + e.getValue().getBoost());
            } else {
                relevantDocsTerms.put(e.getKey(), e.getValue());
            }
        }
        return new ArrayList<>(relevantDocsTerms.values());
    }
}
