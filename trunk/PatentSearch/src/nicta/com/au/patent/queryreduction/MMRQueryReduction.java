package nicta.com.au.patent.queryreduction;

import alcatel.lucent.bell.labs.matrix.ArrayRealVector;
import alcatel.lucent.bell.labs.matrix.PRFMatrix;
import com.hrstc.lucene.queryexpansion.QueryBoostComparator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nicta.com.au.patent.pac.index.TermFreqVector;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;

/**
 * Implements Rocchio's pseudo feedback MMRQueryReduction algorithm
 * <p>
 * Query Expansion - Adding search terms to a user's search. Query expansion is
 * the process of a search engine adding search terms to a user's weighted
 * search. The intent is to improve precision and/or recall. The additional
 * terms may be taken from a thesaurus. For example a search for "car" may be
 * expanded to: car cars auto autos automobile automobiles [foldoc.org].</p>
 *
 * To see options that could be configured through the properties file
 *
 * @see Constants Section
 * <p>
 * Created on February 23, 2012, 5:29 AM
 * </p>
 * <p>
 * TODO: Yahoo started providing API to query www; could be nice to add yahoo
 * implement1at1ion as well
 * </p>
 * <p>
 * @author Reda Bouadjenek
 * </p>
 */
public final class MMRQueryReduction {
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
     * Params
     */
    public final float MMRQE_LAMBDA;

    private final IndexReader ir;
    private final int Nbr_Docs;
    private final int Nbr_Terms;
    private final PRFMatrix prfMatrix;
    private final ArrayRealVector mappedQuery;

    /**
     * Creates a new instance of QueryExpansion
     *
     * @param hits
     * @param ir
     * @param MMRQE_LAMBDA
     * @param sourceField
     * @param Nbr_Docs
     * @param Nbr_Terms
     * @throws java.io.IOException
     */
    public MMRQueryReduction(TopDocs hits, IndexReader ir, float MMRQE_LAMBDA, String sourceField, int Nbr_Docs, int Nbr_Terms) throws IOException {
        this.ir = ir;
        this.MMRQE_LAMBDA = MMRQE_LAMBDA;
        this.Nbr_Docs = Nbr_Docs;
        this.Nbr_Terms = Nbr_Terms;
        //Get terms from relevant documents
        Map<Integer, TermFreqVector> docsTermVectorReldocs = getDocsTerms(hits, sourceField);
        prfMatrix = new PRFMatrix(docsTermVectorReldocs, ir, sourceField, "tfidf");

//        prfMatrix.printMatrix();
//        prfMatrix.printSimilarities();
        mappedQuery = mapQuery(hits);
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
    public Query reduceQuery(Query query, String currentField) throws IOException, ParseException {

        // Create combine documents term vectors - sum ( rel term vectors )
        TermFreqVector queryTermsVector = new TermFreqVector(query);
        List<TermQuery> reductedQueryTerms = mmrqrAlgorithm(prfMatrix, mappedQuery, queryTermsVector, currentField);

        // setBoost of query terms
        // Get queryTerms from the query        
        return generate(reductedQueryTerms);
    }

    /**
     * Extracts terms of the documents; Adds them to vector in the same order
     *
     * @param hits
     * @param field
     *
     * @return relevantDocsTerms docs must be in order
     * @throws java.io.IOException
     */
    public Map<Integer, TermFreqVector> getDocsTerms(TopDocs hits, String field)
            throws IOException {
        Map<Integer, TermFreqVector> docsTerms = new LinkedHashMap<>();
        // Process each of the documents
        int i = 0;
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            if (i == Nbr_Docs) {
                break;
            }
            Terms term = ir.getTermVector(scoreDoc.doc, field);                 //get termvector for document
            // Create termVector and add it to vector
            TermFreqVector docTerms = new TermFreqVector(term);
            docsTerms.put(scoreDoc.doc, docTerms);
            i++;
        }
        return docsTerms;
    }

    public List<TermQuery> mmrqrAlgorithm(PRFMatrix prfMatrix, ArrayRealVector mappedQuery, TermFreqVector queryTermsVector, String currentField)
            throws IOException {
        Map<String, Float> qTerms = new HashMap<>();
        for (String termTxt : queryTermsVector.getTerms()) {
            float tf = queryTermsVector.getFreq(termTxt);
            qTerms.put(termTxt, tf);
        }

        Map<String, TermQuery> terms = new HashMap<>();
        // mmrqrAlgorithm for each of the terms of each of the docs
        while (terms.size() < queryTermsVector.size() - Nbr_Terms) {
            List<TermQuery> l = new ArrayList<>();
            for (String t1 : queryTermsVector.getTerms()) {
                if (!terms.containsKey(t1)) {
                    double sim1 = 0;
                    ArrayRealVector vec = prfMatrix.getColumnVector(t1);
                    if (vec != null) {
                        sim1 = mappedQuery.getCosine(vec);
                    }
//                    double sim1 = Double.NEGATIVE_INFINITY;
//                    for (String t2 : prfMatrix.getTerm_entries().keySet()) {
//                        double v = prfMatrix.getSimilarity(t1, t2);
//                        if (v != 0 && !t1.equals(t2)) {
//                            if (sim1 < 0) {
//                                sim1 = 0;
//                            }
//                            sim1 += prfMatrix.getSimilarity(t1, t2);
//                        }
//                    }

//                    sim1 /= prfMatrix.getTerm_entries().keySet().size();
                    double sim2 = 0;
                    for (String t2 : terms.keySet()) {
                        double sim3 = prfMatrix.getSimilarity(t1, t2);
                        if (sim2 < sim3) {
                            sim2 = sim3;
                        }
                    }
                    Term term = new Term(currentField, t1);
                    TermQuery termQuery = new TermQuery(term);
                    float sim = MMRQE_LAMBDA * (float) sim1 - (1 - MMRQE_LAMBDA) * (float) sim2;
                    termQuery.setBoost(sim);
                    l.add(termQuery);
                }

            }
            Comparator comparator = new QueryBoostComparator();
            Collections.sort(l, comparator);
            TermQuery term = l.get(0);

//            System.out.println("------Containt of l -----------");
//            for (TermQuery t : l) {
//                System.out.println(t.getTerm().text() + "\t" + t.getBoost());
//            }
//            System.out.println("-----------------");
            term.setBoost(qTerms.get(term.getTerm().text()));
            terms.put(term.getTerm().text(), term);
//            System.out.println("v= " + v);
//            System.out.println("-----------------");
//            for (TermQuery qt : l) {
//                System.out.println(qt.getTerm().text() + "\t" + qt.getBoost());
//            }
//            System.out.println("******************");
//            System.out.println(term.getTerm().text() + "\t" + term.getBoost());
//            System.out.println("******************");
        }
        for (String t1 : queryTermsVector.getTerms()) {
            if (!terms.containsKey(t1)) {
                System.err.print(t1 + ", ");
            }
        }
        System.err.println("MMRQR");
//        System.out.println("===========================");
//        for (Map.Entry<String, TermQuery> e : terms.entrySet()) {
//            System.out.println(e.getKey() + "\t" + e.getValue().getBoost());
//        }
        return new ArrayList(terms.values());
    }

    /**
     * Merges <code>termQueries</code> into a single query. In the future this
     * method should probably be in <code>Query</code> class. This is akward way
     * of doing it; but only merge queries method that is available is
     * mergeBooleanQueries; so actually have to make a string term1^boost,
     * term2^boost and then parse it into a query
     *
     * @param termQueries - to merge
     *
     * @return query created from termQueries including boost parameters
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public Query generate(List<TermQuery> termQueries)
            throws ParseException {
        BooleanQuery query = new BooleanQuery();
        // Select only the maxTerms number of terms
        for (TermQuery termQuery : termQueries) {
            query.add(termQuery, BooleanClause.Occur.SHOULD);
        }
        return query;
    }

    public ArrayRealVector mapQuery(TopDocs hits) {
        final Map<Integer, Double> out = new HashMap<>();
        int i = 0;
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            if (i == Nbr_Docs) {
                break;
            }
            out.put(i, (double) scoreDoc.score);
            i++;
//            String d = Integer.toString(scoreDoc.doc);
//            while (d.length() < 7) {
//                d += " ";
//            }
//            System.out.println(d + "|" + scoreDoc.score + "|");
        }
        return new ArrayRealVector(out);
    }
}
