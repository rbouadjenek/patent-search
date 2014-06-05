/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.terms.impact;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;
import nicta.com.au.patent.pac.search.PatentQuery;
import nicta.com.au.patent.pac.search.PACSearcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
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
public class TermsImpact {

    final protected PACSearcher searcher;
    final protected TopicsInMemory topics;
    private final Map<String, Float> boosts;
    private final boolean filter;
    private final boolean stopWords;

    public TermsImpact(String indexDir, String topicFile, int topK, String similarity, float titleBoost, float abstractBoost, float descriptionBoost, float descriptionP5Boost, float claimsBoost, float claims1Boost, boolean filter, boolean stopWords) throws IOException {
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
        System.out.print("# indexDir: " + indexDir);
        System.out.print("# topicfile: " + topicFile);
        System.out.print("# topK: " + topK);
        System.out.print("# Similarity: " + similarity);
        System.out.print("# titleBoost: " + titleBoost);
        System.out.print("# abstractBoost: " + abstractBoost);
        System.out.print("# descriptionBoost: " + descriptionBoost);
        System.out.print("# descriptionP5Boost: " + descriptionP5Boost);
        System.out.print("# claimsBoost: " + claimsBoost);
        System.out.print("# claims1Boost: " + claims1Boost);
        System.out.print("# Filter: " + filter);
        System.out.println("# Stop Words: " + stopWords);
    }

    public void iterateOverQueryTerms() throws ParseException, Exception {
        long start = System.currentTimeMillis();
        int l = 0;
        for (Map.Entry<String, PatentDocument> e : topics.getTopics().entrySet()) {
            l++;
            String queryid = e.getKey();
            PatentDocument pt = e.getValue();
            System.err.print(l + "- " + queryid + " -> " + pt.getUcid() + ": ");
            long start2 = System.currentTimeMillis();
            PatentQuery query = new PatentQuery(pt, boosts, filter, stopWords);

            TopDocs hitsAll = searcher.search(query.parse());;

            int j = 0;
            if (hitsAll.totalHits == 0) {
                System.out.println(queryid + " allTerms " + " Q0 XXXXXXXXXX 1 0.0 STANDARD");
            }
            for (ScoreDoc scoreDoc : hitsAll.scoreDocs) {
                j++;
                Document doc = searcher.getIndexSearch().doc(scoreDoc.doc);
                System.out.println(queryid + " allTerms " + " Q0 " + doc.get(PatentDocument.FileName).substring(3) + " " + j + " " + scoreDoc.score + " STANDARD");
            }

            BooleanQuery bQuery = (BooleanQuery) query.parse();
            if (bQuery.getClauses().length != 2 || !(bQuery.getClauses()[1].getQuery() instanceof BooleanQuery)) {
                continue;
            }
            if (((BooleanQuery) bQuery.getClauses()[1].getQuery()).getClauses().length == 0
                    || !(((BooleanQuery) bQuery.getClauses()[1].getQuery()).getClauses()[0].getQuery() instanceof BooleanQuery)) {
                continue;
            }

            BooleanQuery bQuery2 = (BooleanQuery) ((BooleanQuery) bQuery.getClauses()[1].getQuery()).getClauses()[0].getQuery();
            for (int i = 0; i < bQuery2.clauses().size(); i++) {
                BooleanQuery bQueryFinal = new BooleanQuery();
                BooleanQuery bQueryFinal2 = new BooleanQuery();
                BooleanQuery bQuery3 = bQuery2.clone();
                BooleanClause bClause = bQuery3.clauses().remove(i);
                //*************
//            System.out.println(bQuery3);
                for (int k = 1; k < PatentQuery.getFields().length; k++) {
                    if (query.getQueries()[k] != null && !query.getQueries()[k].equals("") && (k != 4 || k != 6) && query.getBoosts().get(PatentQuery.getFields()[k]) != 0) {

                        BooleanQuery bq = ((BooleanQuery) bQuery3).clone();
                        BooleanQuery bq2 = new BooleanQuery();
                        for (BooleanClause bc : bq.clauses()) {
                            TermQuery tq = (TermQuery) bc.getQuery();
                            Term term = new Term(PatentQuery.getFields()[k], tq.getTerm().text());
                            TermQuery tq2 = new TermQuery(term);
                            tq2.setBoost(tq.getBoost());
                            bq2.add(tq2, BooleanClause.Occur.SHOULD);
                        }
//                    System.out.println(bq2);
                        bQueryFinal2.add(bq2, BooleanClause.Occur.SHOULD);

                    }

                }
                //*******************************
                bQueryFinal.add((Query) bQuery.getClauses()[0].getQuery(), BooleanClause.Occur.MUST);
                bQueryFinal.add(bQueryFinal2, BooleanClause.Occur.MUST);

                TopDocs hits = searcher.search(bQueryFinal);
                j = 0;
                //***************************
                // Get features
                //***************************      
                TermQuery term = (TermQuery) bClause.getQuery();
                double tf = bClause.getQuery().getBoost();// Term frequency
                int totalTF = searcher.getIndexSearch().getIndexReader().docFreq(term.getTerm());
                int docs = searcher.getIndexSearch().getIndexReader().getDocCount(term.getTerm().field());
                double idf = Math.log10((double) docs / (totalTF + 1));// Inverse document frequency
                int tLength = term.getTerm().text().length();// Term length
                int qSize = bQuery2.getClauses().length; // Query size
                //***************************

                if (hits.totalHits == 0) {
                    System.out.println(queryid + " " + bClause + " " + " Q0 XXXXXXXXXX 1 0.0 STANDARD");
                }

                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    j++;

                    Document doc = searcher.getIndexSearch().doc(scoreDoc.doc);
                    System.out.println(queryid + " " + bClause + " Q0 " + doc.get(PatentDocument.FileName).substring(3) + " " + j + " " + scoreDoc.score + " STANDARD");
                }
            }
            long end2 = System.currentTimeMillis();
            System.err.println(bQuery2.clauses().size() + " terms processed in " + Functions.getTimer(end2 - start2) + ".");
        }
        long end = System.currentTimeMillis();
        long millis = (end - start);
        System.err.println("#Global Execution time: " + Functions.getTimer(millis) + ".");
    }

    /**
     * @param args the command line arguments
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
//            indexDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/index/";
//            topicFile = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/clef-ip-2010_PACTopics/PAC_topics.txt";
//            topK = 10;
//            sim = "bm25";
//            titleBoost = 1;
//            abstractBoost = 1;
//            descriptionBoost = 1;
//            claimsBoost = 1;
//            filter = true;
//            stopWords = true;
            System.out.println("ERROR: incorrect parameters!");
            System.out.println("Usage: java -jar Lucene-4.4.0.jar  [-indexDir] [-topicFile] [-topK] [-similarity] [-titleBoost] [-abstractBoost] [-descriptionBoost] [-descriptionP5Boost] [-claimsBoost] [-claims1Boost] [-filter] [-stopWords]");

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
            try {
                TermsImpact ti = new TermsImpact(indexDir, topicFile, topK, sim, titleBoost, abstractBoost, descriptionBoost, descriptionP5Boost, claimsBoost, claims1Boost, filter, stopWords);
                ti.iterateOverQueryTerms();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }
}
