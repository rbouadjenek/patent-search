/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.search;

import com.hrstc.lucene.queryexpansion.PatentRocchioQueryExpansion;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.queryexpansion.PatentMMRQueryExpansion;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.DistributionLL;
import org.apache.lucene.search.similarities.IBSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LambdaDF;
import org.apache.lucene.search.similarities.Normalization;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author rbouadjenek
 */
public class PACSearcher {

    private final IndexSearcher is;
    private final int topK;

    private static Similarity getSimilarity(String similarity) {
        if (similarity.toLowerCase().startsWith("bm25ro")) {
            System.err.println("BM25Rocchio");
            return new BM25Rocchio();
        } else if (similarity.toLowerCase().startsWith("tfidf")) {
//            System.err.println("DefaultSimilarity");
            return new DefaultSimilarity();
        } else if (similarity.toLowerCase().startsWith("lmdir")) {
//            System.err.println("LMDirichletSimilarity");
            return new LMDirichletSimilarity();
        } else if (similarity.toLowerCase().startsWith("lmj")) {
//            System.err.println("LMJelinekMercerSimilarity");
            return new LMJelinekMercerSimilarity(1);
        } else if (similarity.toLowerCase().startsWith("ibs")) {
//            System.err.println("IBSimilarity");
            return new IBSimilarity(new DistributionLL(), new LambdaDF(), new Normalization.NoNormalization());
        } else if (similarity.toLowerCase().startsWith("bm25ro")) {
//            System.err.println("BM25Similarity");
            return new BM25Similarity();
        } else {
//            System.err.println("BM25Similarity");
            return new BM25Similarity();
        }
    }

    public PACSearcher(String indexDir, String similarity, int topK) throws IOException {
        Directory dir = FSDirectory.open(new File(indexDir));
        is = new IndexSearcher(DirectoryReader.open(dir));
        is.setSimilarity(getSimilarity(similarity));
        this.topK = topK;
    }

    public TopDocs search(String patent, float titleBoost, float abstractBoost, float descriptionBoost, float descriptionP5Boost, float claimsBoost, float claims1Boost, boolean filter, boolean stopWords) throws Exception {
        Map boosts = new HashMap<>();
        boosts.put(PatentDocument.Classification, new Float(0));
        boosts.put(PatentDocument.Title, new Float(titleBoost));
        boosts.put(PatentDocument.Abstract, new Float(abstractBoost));
        boosts.put(PatentDocument.Description, new Float(descriptionBoost));
        boosts.put("descriptionP5", new Float(descriptionP5Boost));
        boosts.put(PatentDocument.Claims, new Float(claimsBoost));
        boosts.put("claims1", new Float(claims1Boost));
        return search(new PatentDocument(patent), boosts, filter, stopWords);
    }

    public TopDocs search(PatentDocument patent, Map<String, Float> boosts, boolean filter, boolean stopWords) throws Exception {
        Query query = new PatentQuery(patent, boosts, filter, stopWords).parse();
//        System.err.println(query);
        return is.search(query, topK);
    }

    public TopDocs search(Query query) throws Exception {
        return is.search(query, topK);
    }

    public IndexSearcher getIndexSearch() {
        return is;
    }

    public void printSearch(String patent, float titleBoost, float abstractBoost, float descriptionBoost, float descriptionP5Boost, float claimsBoost, float claims1Boost, boolean filter, boolean stopWords) throws Exception {
        Map<String, Float> boosts = new HashMap<>();
        boosts.put(PatentDocument.Classification, new Float(0));
        boosts.put(PatentDocument.Title, new Float(titleBoost));
        boosts.put(PatentDocument.Abstract, new Float(abstractBoost));
        boosts.put(PatentDocument.Description, new Float(descriptionBoost));
        boosts.put("descriptionP5", new Float(descriptionP5Boost));
        boosts.put(PatentDocument.Claims, new Float(claimsBoost));
        boosts.put("claims1", new Float(claims1Boost));
        long start = System.currentTimeMillis();
        Query query = new PatentQuery(new PatentDocument(patent), boosts, filter, stopWords).parse();
        System.err.println(query);
//        PatentMMRQueryExpansion mmrqe = new PatentMMRQueryExpansion(is, 5, 0, 2, (float) 1, (float) 0.5);
//        query = mmrqe.expandQuery(new PatentQuery(new PatentDocument(patent), boosts, filter, stopWords));

        PatentRocchioQueryExpansion pqe = new PatentRocchioQueryExpansion(is, 5, 0, 2, 1, (float) 0.5, (float) 0.7, 0);
        query = pqe.expandQuery(new PatentQuery(new PatentDocument(patent), boosts, filter, stopWords));
        System.err.println(query);
        TopDocs hits = this.search(query);
        long end = System.currentTimeMillis();
        System.err.println("Found " + hits.totalHits
                + " document(s) has matched query. Processed in " + Functions.getTimer(end - start) + ".");

        int i = 0;
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            i++;
//            System.out.println(new PatentQuery(patent, titleBoost, abstractBoost, descriptionBoost, descriptionP5Boost, claimsBoost, claims1Boost, filter, stopWords).parse().toString());
            Explanation explanation = is.explain(new PatentQuery(patent, titleBoost, abstractBoost, descriptionBoost, descriptionP5Boost, claimsBoost, claims1Boost, filter, stopWords).parse(), scoreDoc.doc);
            Document doc = is.doc(scoreDoc.doc);

            System.out.println("PAC-375 Q0 " + doc.get(PatentDocument.FileName).substring(3) + " " + i + " " + scoreDoc.score + " STANDARD" + "\t" + doc.get(PatentDocument.Classification));
//            System.out.println(explanation.toString());

        }
    }

    public Query rewrite(String queryid, PatentDocument patent, RewriteQuery rq, Map<String, Float> boosts, boolean filter, boolean stopWords) throws ParseException, IOException {
        PatentQuery query = new PatentQuery(patent, boosts, filter, stopWords);
        return rq.rewrite(queryid, query);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String indexDir;
        String q;
        if (args.length == 0) {

            indexDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/index/";
            q = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/query/PAC-132_EP-1550834-A1.xml";
            indexDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/indexWithoutSW-Vec-CLEF-IP2010/";
            q = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/topics/PAC-1001_EP-1233512-A2.xml";
        } else {
            indexDir = args[0];
            q = args[1];
        }
        try {
            PACSearcher searcher = new PACSearcher(indexDir, "tfidf", 10);
            searcher.printSearch(q, 0, 1, 0, 0, 0, 0, true, true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
