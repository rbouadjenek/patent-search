/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.terms.impact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;
import nicta.com.au.patent.pac.index.TermFreqVector;
import nicta.com.au.patent.pac.search.PatentQuery;
import nicta.com.au.patent.pac.search.PACSearcher;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

/**
 *
 * @author rbouadjenek
 */
public class FeaturesSelection {

    final protected PACSearcher searcher;
    final protected TopicsInMemory topics;
    private final Map<String, Float> boosts;
    private final boolean filter;
    private final boolean stopWords;

    public FeaturesSelection(String indexDir, String topicFile, int topK, String similarity, float titleBoost, float abstractBoost, float descriptionBoost, float descriptionP5Boost, float claimsBoost, float claims1Boost, boolean filter, boolean stopWords) throws IOException {
        boosts = new HashMap<>();
        boosts.put(PatentDocument.Classification, new Float(0));
        boosts.put(PatentDocument.Title, new Float(titleBoost));
        boosts.put(PatentDocument.Abstract, new Float(abstractBoost));
        boosts.put(PatentDocument.Description, new Float(descriptionBoost));
        boosts.put("descriptionP5", new Float(descriptionP5Boost));
        boosts.put(PatentDocument.Claims, new Float(claimsBoost));
        boosts.put("claims1", new Float(claims1Boost));
        searcher = new PACSearcher(indexDir, similarity, topK);
        topics = new TopicsInMemory(topicFile);
        this.filter = filter;
        this.stopWords = stopWords;
    }

    public static double Log2(double number) {
        return Math.log(number) / Math.log(2);
    }

    public List<TermFreqVector> getDocsTerms(TopDocs hits, String field)
            throws IOException {
        List<TermFreqVector> docsTerms = new ArrayList<>();
        // Process each of the documents
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Terms term = searcher.getIndexSearch().getIndexReader().getTermVector(scoreDoc.doc, field); //get termvector for document
            // Create termVector and add it to vector
            TermFreqVector docTerms = new TermFreqVector(term);
            docsTerms.add(docTerms);
        }
        return docsTerms;
    }

    public void iterateOverQueryTerms() throws ParseException, Exception {
        long start = System.currentTimeMillis();
        int l = 0;
//        System.out.println("queryid\tterm\ttf\tln_tf\tidf\ttfidf\ttLength\tratioTerm\t"
//                + "nbrUniqTerms\tqSize\tscq\tisInTitle\tisInAbstract\tisInDescription\tisInClaims");

        System.out.println("queryid\tremovedBooleanClause\ttf\tln_tf\tidf\ttfidf\ttLength\tratioTerm\tnbrUniqTerms\tqSize\tscq\tSCS\tictf\tQC\tclarity\tfreqInTitle\tratioInTitle\tfreqDescription\tratioInDescription\tfreqClaims\tratioInClaims");
        for (Map.Entry<String, PatentDocument> e : topics.getTopics().entrySet()) {
            l++;
            String queryid = e.getKey();
            PatentDocument pt = e.getValue();
//            System.err.print(l + "- " + queryid + " -> " + pt.getUcid() + ": ");
            long start2 = System.currentTimeMillis();
            PatentQuery query = new PatentQuery(pt, boosts, filter, stopWords);
            BooleanQuery bQuery = (BooleanQuery) query.parse();
            if (bQuery.getClauses().length != 2 || !(bQuery.getClauses()[1].getQuery() instanceof BooleanQuery)
                    || ((BooleanQuery) bQuery.getClauses()[1].getQuery()).getClauses().length == 0
                    || !(((BooleanQuery) bQuery.getClauses()[1].getQuery()).getClauses()[0].getQuery() instanceof BooleanQuery)) {
                continue;
            }
            BooleanQuery bQuery2 = (BooleanQuery) ((BooleanQuery) bQuery.getClauses()[1].getQuery()).getClauses()[0].getQuery();
            for (int i = 0; i < bQuery2.clauses().size(); i++) {
                BooleanQuery bQueryFinal = new BooleanQuery();
                BooleanQuery bQuery3 = bQuery2.clone();
                BooleanClause removedBooleanClause = bQuery3.clauses().remove(i);
                bQueryFinal.add((Query) bQuery.getClauses()[0].getQuery(), BooleanClause.Occur.MUST);
                bQueryFinal.add(bQuery3, BooleanClause.Occur.MUST);
                //***************************
                // Get features
                //*************************** 
                IndexReader ir = searcher.getIndexSearch().getIndexReader();
                TermQuery term = (TermQuery) removedBooleanClause.getQuery();
                double tf = removedBooleanClause.getQuery().getBoost();// Term frequency
                double ln_tf = Math.log(1 + tf);// Get log of the term frequency
                int totalTF = ir.docFreq(term.getTerm());
                int docs = ir.getDocCount(term.getTerm().field());
                double idf = 0;
                if (totalTF != 0) {
                    idf = Math.log10((double) docs / (totalTF));// Inverse document frequency
                }
                double tfidf = ln_tf * idf;// Compute the TFIDF
                int tLength = term.getTerm().text().length();// Term length
                int qSize = 0;
                if (term.getTerm().field().endsWith(PatentDocument.Title)) {
                    qSize = query.getTitleSize(); // Query size
                } else if (term.getTerm().field().endsWith(PatentDocument.Abstract)) {
                    qSize = query.getAbstractSize(); // Query size
                } else if (term.getTerm().field().endsWith(PatentDocument.Description)) {
                    qSize = query.getDescriptionSize(); // Query size
                } else if (term.getTerm().field().endsWith(PatentDocument.Claims)) {
                    qSize = query.getClaimsSize(); // Query size
                }
                double ratioTerm = (double) tf / qSize;
                int nbrUniqTerms = bQuery2.getClauses().length;
                long totalTermFreq = ir.totalTermFreq(term.getTerm());
                double ln_totalTermFreq = Math.log(1 + totalTermFreq);
                double scq = ln_totalTermFreq * idf;
                double freqInTitle = query.getFreqInTitle(term.getTerm().text());
                double ratioInTitle = (double) freqInTitle / query.getTitleSize();
                double freqAbstract = query.getFreqInAbstract(term.getTerm().text());
                double ratioInAbstract = (double) freqAbstract / query.getAbstractSize();
                double freqDescription = query.getFreqInDescription(term.getTerm().text());
                double ratioInDescription = (double) freqDescription / query.getDescriptionSize();
                double freqClaims = query.getFreqInClaims(term.getTerm().text());
                double ratioInClaims = (double) freqClaims / query.getClaimsSize();
                double Pcoll = (double) totalTermFreq / ir.getSumTotalTermFreq(term.getTerm().field());
                double SCS = 0;
                double ictf = 0;
                List<TermFreqVector> docsTermVector = getDocsTerms(searcher.search(term), term.getTerm().field());
                double a1 = 0;
                for (TermFreqVector vec : docsTermVector) {
                    a1 += Math.sqrt((double) vec.getFreq(term.getTerm().text()) / vec.numberOfTerms());
                }
                double clarity = 0;
                if (totalTermFreq != 0) {
                    SCS = ratioTerm * Log2(ratioTerm / Pcoll);// Simplified Clarity Score
                    ictf = Math.log10((double) docs / (totalTermFreq));// Inverse Collection Term Frequency
                    clarity = a1 * Log2(a1 / Pcoll);
                }
                double QC = totalTF / (double) docs;// QueryScope

//***************************
                System.out.println(queryid + "\t" + removedBooleanClause + "\t"
                        + tf + "\t"
                        + ln_tf + "\t"
                        + idf + "\t"
                        + tfidf + "\t"
                        + tLength + "\t"
                        + ratioTerm + "\t"
                        + nbrUniqTerms + "\t"
                        + qSize + "\t"
                        + scq + "\t"
                        + SCS + "\t"
                        + ictf + "\t"
                        + QC + "\t"
                        + clarity + "\t"
                        + freqInTitle + "\t"
                        + ratioInTitle + "\t"
                        + freqDescription + "\t"
                        + ratioInDescription + "\t"
                        + freqClaims + "\t"
                        + ratioInClaims
                );
            }
            long end2 = System.currentTimeMillis();
//            System.err.println(bQuery2.clauses().size() + " terms processed in " + Functions.getTimer(end2 - start2) + ".");
        }
        long end = System.currentTimeMillis();
        long millis = (end - start);
        System.err.println("#Global Execution time: " + Functions.getTimer(millis) + ".");
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {
        // TODO code application logic here
        String indexDir;
        String topicFile;
        int topK;
        String sim;
        float titleBoost;
        float abstractBoost;
        float descriptionBoost;
        float descriptionP5Boost;
        float claimsBoost;
        float claims1Boost;
        boolean filter;
        boolean stopWords;
        if (args.length != 12) {
            indexDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/indexWithoutSW-Vec-CLEF-IP2010/";
            topicFile = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/topics/PAC_topics.xml";
            topK = 10;
            sim = "bm25";
            titleBoost = 0;
            abstractBoost = 1;
            descriptionBoost = 0;
            descriptionP5Boost = 0;
            claimsBoost = 0;
            claims1Boost = 0;
            filter = true;
            stopWords = true;
//            System.out.println("ERROR: incorrect parameters!");
//            System.out.println("Usage: java -jar Lucene-4.4.0.jar  [-indexDir] [-topicFile] [-topK] [-similarity] [-titleBoost] [-abstractBoost] [-descriptionBoost] [-descriptionP5Boost] [-claimsBoost] [-claims1Boost] [-filter] [-stopWords]");
        } else {
            indexDir = args[0];
            topicFile = args[1];
            topK = Integer.parseInt(args[2]);
            sim = args[3];
            titleBoost = Float.parseFloat(args[4]);
            abstractBoost = Float.parseFloat(args[5]);
            descriptionBoost = Float.parseFloat(args[6]);
            descriptionP5Boost = Float.parseFloat(args[7]);
            claimsBoost = Float.parseFloat(args[8]);
            claims1Boost = Float.parseFloat(args[9]);
            filter = Boolean.valueOf(args[10]);
            stopWords = Boolean.valueOf(args[11]);
        }
        try {
            FeaturesSelection ti = new FeaturesSelection(indexDir, topicFile, topK, sim, titleBoost,
                    abstractBoost, descriptionBoost, descriptionP5Boost, claimsBoost, claims1Boost, filter, stopWords);
            ti.iterateOverQueryTerms();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
