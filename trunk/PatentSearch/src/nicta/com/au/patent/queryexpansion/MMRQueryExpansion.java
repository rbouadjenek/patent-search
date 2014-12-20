package nicta.com.au.patent.queryexpansion;

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
 * Implements Rocchio's pseudo feedback MMRQueryExpansion algorithm
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
public final class MMRQueryExpansion {
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
    public final float MMRQE_ALPHA;

    private final IndexReader ir;
    private final int Nbr_Docs;
    private final int Nbr_Terms;
    private final PRFMatrix prfMatrix;
    private final ArrayRealVector mappedQuery;
    private final String sourceField;
    private final Map<String, Float> rocchioVector;

    /**
     * Creates a new instance of QueryExpansion
     *
     * @param hits
     * @param ir
     * @param rocchioVector
     * @param MMRQE_ALPHA
     * @param MMRQE_LAMBDA
     * @param sourceField
     * @param Nbr_Docs
     * @param Nbr_Terms
     * @throws java.io.IOException
     */
    public MMRQueryExpansion(TopDocs hits, IndexReader ir, Map<String, Float> rocchioVector, float MMRQE_ALPHA, float MMRQE_LAMBDA, String sourceField, int Nbr_Docs, int Nbr_Terms) throws IOException {
        this.ir = ir;
        this.rocchioVector = rocchioVector;
        this.MMRQE_LAMBDA = MMRQE_LAMBDA;
        this.MMRQE_ALPHA = MMRQE_ALPHA;
        this.Nbr_Docs = Nbr_Docs;
        this.Nbr_Terms = Nbr_Terms;
        this.sourceField = sourceField;
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
    public Query expandQuery(Query query, String currentField) throws IOException, ParseException {

        // Create combine documents term vectors - sum ( rel term vectors )
        Map<String, TermQuery> expandedTerms = mmrqeAlgorithm(prfMatrix, mappedQuery, currentField);

        // setBoost of query terms
        // Get queryTerms from the query
        TermFreqVector queryTermsVector = new TermFreqVector(query);
        Map<String, TermQuery> queryTerms = setBoost(queryTermsVector, currentField);

        List<TermQuery> expandedQueryTerms = combine(queryTerms, expandedTerms);

        return generate(expandedQueryTerms);
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

    public Map<String, TermQuery> mmrqeAlgorithm(PRFMatrix prfMatrix, ArrayRealVector mappedQuery, String currentField)
            throws IOException {
        LinkedHashMap<String, TermQuery> terms = new LinkedHashMap<>();
        // mmrqeAlgorithm for each of the terms of each of the docs
        while (terms.size() < Nbr_Terms && terms.size() < prfMatrix.getColumnDimension()) {
            List<TermQuery> l = new ArrayList<>();
            for (String t1 : prfMatrix.getTerm_entries().keySet()) {
                if (!terms.containsKey(t1)) {
                    double sim1 = mappedQuery.getCosine(prfMatrix.getColumnVector(t1));
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
//                    System.out.println(sim1);
                    termQuery.setBoost(sim);
                    l.add(termQuery);
                }
//                System.out.println("------------------------");
            }
            Comparator comparator = new QueryBoostComparator();
            Collections.sort(l, comparator);
            TermQuery term = l.get(0);
//            System.out.println("------Containt of l -----------");
//            for (TermQuery t : l) {
//                System.out.println(t.getTerm().text() + "\t" + t.getBoost());
//            }
//            System.out.println("-----------------");
            
            
            if (rocchioVector != null) {
                term.setBoost((1 - MMRQE_ALPHA) * rocchioVector.get(term.getTerm().text()));
            } else {
                term.setBoost((1 - MMRQE_ALPHA) * 1);
            }

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
//        System.out.println("-----------------");
        for (Map.Entry<String, TermQuery> e : terms.entrySet()) {
            System.out.print(e.getKey() + ", ");
        }
        return terms;
    }

    /**
     * Sets boost of terms. boost = weight = factor(tf*idf)
     *
     * @param termVector
     * @param currentField
     * @return
     * @throws java.io.IOException
     */
    public Map<String, TermQuery> setBoost(TermFreqVector termVector, String currentField)
            throws IOException {
        Map<String, TermQuery> terms = new HashMap<>();
        // Increase decay
        // Populate terms: with TermQuries and set boost
        for (String termTxt : termVector.getTerms()) {
            // Create Term
            Term term = new Term(currentField, termTxt);
            // Calculate weight
            float tf = termVector.getFreq(termTxt);
//                float idf = ir.docFreq(term);
            int docs = ir.getDocCount(sourceField);
            float idf = (float) Math.log10((double) docs / (ir.docFreq(new Term(sourceField, termTxt)) + 1));
            float weight = tf * idf;
            // Create TermQuery and add it to the collection
            TermQuery termQuery = new TermQuery(term);
            // Calculate and set boost
            float boost = weight;

//            System.out.println(term.text() + " -> tf= " + tf + " idf= " + idf + " tfidf= " + weight);
            if (boost != 0) {
                termQuery.setBoost(MMRQE_ALPHA * tf);
                terms.put(termTxt, termQuery);

            }
        }
        return terms;
    }

    /**
     * combine weights according to expansion formula
     *
     * @param queryTerms
     * @param expandedTerms
     * @return
     */
    public List<TermQuery> combine(Map<String, TermQuery> queryTerms, Map<String, TermQuery> expandedTerms) {
        // Add Terms of the relevant documents
        for (Map.Entry<String, TermQuery> e : queryTerms.entrySet()) {
            if (expandedTerms.containsKey(e.getKey())) {
                TermQuery tq = expandedTerms.get(e.getKey());
                tq.setBoost(tq.getBoost() + e.getValue().getBoost());
            } else {
                expandedTerms.put(e.getKey(), e.getValue());
            }
        }
        List l = new ArrayList<>(expandedTerms.values());
        Comparator comparator = new QueryBoostComparator();
        Collections.sort(l, comparator);
        return l;
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
}
