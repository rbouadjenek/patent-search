package com.hrstc.lucene.queryexpansion;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nicta.com.au.patent.pac.index.TermFreqVector;
import nicta.com.au.patent.pac.search.PatentQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;

/**
 * Implements Rocchio's pseudo feedback RocchioQueryExpansion algorithm
 * <p>
 * Query Expansion - Adding search termClaimsDescriptionAbstractTitles to a
 * user's search. Query expansion is the process of a search engine adding
 * search termClaimsDescriptionAbstractTitles to a user's weighted search. The
 * intent is to improve precision and/or recall. The additional
 * termClaimsDescriptionAbstractTitles may be taken from a thesaurus. For
 * example a search for "car" may be expanded to: car cars auto autos automobile
 * automobiles [foldoc.org].
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
public final class RocchioQueryExpansion {
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
    public static final String ROCCHIO_GAMMA_FLD = "rocchio.gamma";

    private final Map<String, Float> parameters;
    private final IndexReader ir;
    private final String sourceField;
    private final int Nbr_Terms;
    private final Map<TermFreqVector, String> docsTermVectorReldocs;
    private final Map<TermFreqVector, String> docsTermVectorIrreldocs;

    /**
     * Creates a new instance of QueryExpansion
     *
     * @param hits
     * @param ir
     * @param parameters
     * @param source
     * @param Nbr_Docs
     * @param Nbr_Terms
     * @throws java.io.IOException
     */
    public RocchioQueryExpansion(TopDocs hits, IndexReader ir, Map<String, Float> parameters, int source, int Nbr_Docs, int Nbr_Terms) throws IOException {
        this.ir = ir;
        this.parameters = parameters;
        if (source != 7) {
            this.sourceField = PatentQuery.getFields()[source];
        } else {
            this.sourceField = PatentQuery.all;
        }

        this.Nbr_Terms = Nbr_Terms;

        // Create combine documents termTitle vectors - sum ( rel termTitle vectors )
        //Get terms from relevant documents
        docsTermVectorReldocs = getDocsTerms(hits, 0, Nbr_Docs);
        //Get terms from irrelevant documents
        docsTermVectorIrreldocs = getDocsTerms(hits, hits.totalHits - Nbr_Docs, hits.totalHits);

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
        float alpha = parameters.get(RocchioQueryExpansion.ROCCHIO_ALPHA_FLD);
        float beta = parameters.get(RocchioQueryExpansion.ROCCHIO_BETA_FLD);
        float gamma = parameters.get(RocchioQueryExpansion.ROCCHIO_GAMMA_FLD);
        float decay = parameters.get(RocchioQueryExpansion.DECAY_FLD);

        // Adjust termTitle features of the docs with alpha * query; and beta; and assign weights/boost to terms (tf*idf)
        Query expandedQuery = adjust(query, currentField, alpha, beta, gamma, decay, Nbr_Terms);

        return expandedQuery;
    }

    /**
     * Adjust termClaimsDescriptionAbstractTitle features of the docs with alpha
     * * query; and beta; and assign weights/boost to
     * termClaimsDescriptionAbstractTitles (tf*idf).
     *
     * @param query
     * @param currentField
     * @param alpha
     * @param beta - factor of the equation
     * @param gamma
     * @param decay
     * @param maxExpandedQueryTerms - maximum number of
     * termClaimsDescriptionAbstractTitles in expanded query
     *
     * @return expandedQuery with boost factors adjusted using Rocchio's
     * algorithm
     *
     * @throws IOException
     * @throws ParseException
     */
    public Query adjust(Query query, String currentField, float alpha, float beta, float gamma, float decay, int maxExpandedQueryTerms)
            throws IOException, ParseException {
        Query expandedQuery;
        // setBoost of docs terms
        Map<String, TermQuery> relevantDocsTerms = setBoost(docsTermVectorReldocs, currentField, beta, decay);
        Map<String, TermQuery> irrrelevantDocsTerms = setBoost(docsTermVectorIrreldocs, currentField, gamma, decay);
//        Map<String, TermQuery> relevantDocsTerms = new HashMap<>();
//        Map<String, TermQuery> irrrelevantDocsTerms = new HashMap<>();
        // setBoost of query terms
        // Get queryTerms from the query

        // combine weights according to expansion formula
        List<TermQuery> expandedQueryTerms = combine(new HashMap<String, TermQuery>(), relevantDocsTerms, irrrelevantDocsTerms);
        // Sort by boost=weight
        Comparator comparator = new QueryBoostComparator();
        Collections.sort(expandedQueryTerms, comparator);
        relevantDocsTerms.clear();
        int termCount = Math.min(expandedQueryTerms.size(), maxExpandedQueryTerms);
        for (int i = 0; i < termCount; i++) {
            TermQuery tq = expandedQueryTerms.get(i);
            relevantDocsTerms.put(tq.getTerm().text(), tq);
        }
        TermFreqVector queryTermsVector = new TermFreqVector(query);
        Map<String, TermQuery> queryTerms;

        queryTerms = setBoost(queryTermsVector, currentField, alpha);

//        List<TermQuery> queryTermsList=new ArrayList(queryTerms.values());        
//        Collections.sort(queryTermsList, comparator);
//        queryTerms.clear();
//        for(TermQuery tq:queryTermsList){
//            queryTerms.put(tq.getTerm().text(), tq);
//        }
        expandedQueryTerms = combine(queryTerms, relevantDocsTerms, new HashMap<String, TermQuery>());
        Collections.sort(expandedQueryTerms, comparator);
        // Create Expanded Query
        expandedQuery = mergeQueries(expandedQueryTerms, Integer.MAX_VALUE);

        return expandedQuery;
    }

    /**
     * Merges <code>termClaimsDescriptionAbstractTitleQueries</code> into a
     * single query. In the future this method should probably be in
     * <code>Query</code> class. This is akward way of doing it; but only merge
     * queries method that is available is mergeBooleanQueries; so actually have
     * to make a string termClaimsDescriptionAbstractTitle1^boost1,
     * termClaimsDescriptionAbstractTitle2^boost and then parse it into a query
     *
     * @param termQueries - to merge
     * @param maxTerms
     *
     * @return query created from termClaimsDescriptionAbstractTitleQueries
     * including boost parameters
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public Query mergeQueries(List<TermQuery> termQueries, int maxTerms)
            throws ParseException {
        BooleanQuery query = new BooleanQuery();
        // Select only the maxTerms number of terms
        int termCount = Math.min(termQueries.size(), maxTerms);
        for (int i = 0; i < termCount; i++) {
            TermQuery termQuery = termQueries.get(i);
            query.add(termQuery, BooleanClause.Occur.SHOULD);
        }
        return query;
    }

    /**
     * Extracts termClaimsDescriptionAbstractTitles of the documents; Adds them
     * to vector in the same order
     *
     * @param hits
     * @param i
     * @param j
     *
     * @return relevantDocsTerms docs must be in order
     * @throws java.io.IOException
     */
    public Map<TermFreqVector, String> getDocsTerms(TopDocs hits, int i, int j)
            throws IOException {
        Map<TermFreqVector, String> docsTerms = new HashMap<>();
        // Process each of the documents
        while (i < j && i < hits.totalHits && i >= 0) {
            ScoreDoc scoreDoc = hits.scoreDocs[i];
            if (sourceField.equals(PatentQuery.all)) {
                Terms termTitle = ir.getTermVector(scoreDoc.doc, PatentQuery.getFields()[1]);
                TermFreqVector docTermsTitle = new TermFreqVector(termTitle);
                docsTerms.put(docTermsTitle, PatentQuery.getFields()[1]);

                Terms termAbstract = ir.getTermVector(scoreDoc.doc, PatentQuery.getFields()[2]);
                TermFreqVector docTermsAbstract = new TermFreqVector(termAbstract);
                docsTerms.put(docTermsAbstract, PatentQuery.getFields()[2]);

                Terms termDescription = ir.getTermVector(scoreDoc.doc, PatentQuery.getFields()[3]);
                TermFreqVector docTermsDescription = new TermFreqVector(termDescription);
                docsTerms.put(docTermsDescription, PatentQuery.getFields()[3]);

                Terms termClaims = ir.getTermVector(scoreDoc.doc, PatentQuery.getFields()[5]);
                TermFreqVector docTermsClaims = new TermFreqVector(termClaims);
                docsTerms.put(docTermsClaims, PatentQuery.getFields()[5]);

            } else {
                Terms term = ir.getTermVector(scoreDoc.doc, sourceField);                 //get termvector for document
                // Create termVector and add it to vector
                TermFreqVector docTerms = new TermFreqVector(term);
                docsTerms.put(docTerms, sourceField);

            }
            i++;
        }
        return docsTerms;
    }

    /**
     * Sets boost of termClaimsDescriptionAbstractTitles. boost = weight =
     * factor(tf*idf)
     *
     * @param termVector
     * @param currentField
     * @param factor
     * @return
     * @throws java.io.IOException
     */
    public Map<String, TermQuery> setBoost(TermFreqVector termVector, String currentField, float factor)
            throws IOException {
        Map<TermFreqVector, String> v = new HashMap<>();
        v.put(termVector, currentField);
        return setBoost(v, currentField, factor, 0);
    }

    /**
     * Sets boost of termClaimsDescriptionAbstractTitles. boost = weight =
     * factor(tf*idf)
     *
     * @param vecsTerms
     * @param currentField
     * @param factor - adjustment factor ( ex. alpha or beta )
     * @param decayFactor
     * @return
     * @throws java.io.IOException
     */
    public Map<String, TermQuery> setBoost(Map<TermFreqVector, String> vecsTerms, String currentField, float factor, float decayFactor)
            throws IOException {
        Map<String, TermQuery> terms = new HashMap<>();
        // setBoost for each of the terms of each of the docs
        int i = 0;
        float norm = (float) 1 / vecsTerms.size();
//        System.out.println("--------------------------");
        for (Map.Entry<TermFreqVector, String> e : vecsTerms.entrySet()) {
            // Increase decay
            String field = e.getValue();
            TermFreqVector docTerms = e.getKey();
            float decay = decayFactor * i;
            // Populate terms: with TermQuries and set boost
            for (String termTxt : docTerms.getTerms()) {
                // Create Term
                Term term = new Term(currentField, termTxt);
                // Calculate weight
                float tf = docTerms.getFreq(termTxt);
//                float idf = ir.docFreq(termTitle);
                int docs;
                float idf;
                if (sourceField.equals(PatentQuery.all)) {
                    docs = ir.getDocCount(field);
                    idf = (float) Math.log10((double) docs / (ir.docFreq(new Term(field, termTxt)) + 1));
                } else {
                    docs = ir.getDocCount(sourceField);
                    idf = (float) Math.log10((double) docs / (ir.docFreq(new Term(sourceField, termTxt)) + 1));
                }
                float weight = tf * idf;

//                System.out.println(term.text() + " -> tf= " + tf + " idf= " + idf + " tfidf= " + weight);
                // Adjust weight by decay factor
                weight = weight - (weight * decay);
                // Create TermQuery and add it to the collection
                TermQuery termQuery = new TermQuery(term);
                // Calculate and set boost
                float boost;
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
     * @param irrelevantDocsTerms
     * @return
     */
    public List<TermQuery> combine(Map<String, TermQuery> queryTerms, Map<String, TermQuery> relevantDocsTerms,
            Map<String, TermQuery> irrelevantDocsTerms) {
        // Add Terms of the relevant documents
        for (Map.Entry<String, TermQuery> e : queryTerms.entrySet()) {
            if (relevantDocsTerms.containsKey(e.getKey())) {
                TermQuery tq = relevantDocsTerms.get(e.getKey());
                tq.setBoost(tq.getBoost() + e.getValue().getBoost());
            } else {
                relevantDocsTerms.put(e.getKey(), e.getValue());
            }
        }
        // Substract terms of irrelevant documents
        for (Map.Entry<String, TermQuery> e : irrelevantDocsTerms.entrySet()) {
            if (relevantDocsTerms.containsKey(e.getKey())) {
                TermQuery tq = relevantDocsTerms.get(e.getKey());
                tq.setBoost(tq.getBoost() - e.getValue().getBoost());
            } else {
                TermQuery tq = e.getValue();
                tq.setBoost(-tq.getBoost());
                relevantDocsTerms.put(e.getKey(), tq);
            }
        }
        return new ArrayList<>(relevantDocsTerms.values());
    }

    public Map<String, Float> getRocchioVector(String currentField) throws IOException {
        Map<String, Float> out = new HashMap<>();
        float beta = parameters.get(RocchioQueryExpansion.ROCCHIO_BETA_FLD);
        float gamma = parameters.get(RocchioQueryExpansion.ROCCHIO_GAMMA_FLD);
        float decay = parameters.get(RocchioQueryExpansion.DECAY_FLD);
        Map<String, TermQuery> relevantDocsTerms = setBoost(docsTermVectorReldocs, currentField, beta, decay);
        Map<String, TermQuery> irrrelevantDocsTerms = setBoost(docsTermVectorIrreldocs, currentField, gamma, decay);
        List<TermQuery> expandedQueryTerms = combine(new HashMap<String, TermQuery>(), relevantDocsTerms, new HashMap<String, TermQuery>());
        for (TermQuery tq : expandedQueryTerms) {
            out.put(tq.getTerm().text(), tq.getBoost());

        }
        return out;
    }
}
