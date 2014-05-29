/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.main;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import nicta.com.au.patent.document.PatentDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author rbouadjenek
 */
public class Searcher {

    public static void search(String indexDir, String q)
            throws Exception {
        Directory dir = FSDirectory.open(new File(indexDir));
        IndexSearcher is = new IndexSearcher(DirectoryReader.open(dir));

        is.setSimilarity(new BM25Similarity());
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);

        QueryParser parser = new QueryParser(Version.LUCENE_48, null, analyzer);
        Query query = parser.parse(q);

        BooleanQuery bq=new BooleanQuery();
        PhraseQuery pq1 = new PhraseQuery();
         pq1.add(new Term(PatentDocument.Classification, "G"));
         bq.add(pq1,BooleanClause.Occur.SHOULD);
         
         
         PhraseQuery pq2 = new PhraseQuery();
         pq2.add(new Term(PatentDocument.Classification, "G01"));
         bq.add(pq2,BooleanClause.Occur.SHOULD);
         
         
//        pq.add(new Term(PatentDocument.Classification, "02"));
//        pq.setSlop(0);

        BooleanQuery qry = new BooleanQuery();
        Query qt =new TermQuery(new Term(PatentDocument.Classification, "\"A01B1H02\""));

        
       
        System.out.println(bq.toString());

        long start = System.currentTimeMillis();
        TopDocs hits = is.search(bq, 10);
        long end = System.currentTimeMillis();
        System.err.println("Found " + hits.totalHits
                + " document(s) (in " + (end - start)
                + " milliseconds) that matched query '"
                + bq + "':");
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            System.out.println("----------");
            Document doc = is.doc(scoreDoc.doc);
            System.out.println(scoreDoc.score + "\t" + doc.get(PatentDocument.Classification) + "\t" + doc.get(PatentDocument.Title));// + "\t" + doc.get("type") + "\t" + doc.get("num") + "\t" + doc.get("lang"));
//            System.out.println(explanation.toString());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        args = new String[2];
        args[0] = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/codeIndex2/";
//        args[0] = "/Volumes/Macintosh SSD/Users/index/";
        args[1] = "G01";
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java "
                    + Searcher.class.getName()
                    + " <index dir> <query>");
        }
        String indexDir = args[0];
        String q = args[1];
        try {
            search(indexDir, q);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
