/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.main;

import com.hrstc.lucene.queryexpansion.GenerateClassCodesQuery;
import static com.hrstc.lucene.queryexpansion.GenerateClassCodesQuery.generateQuery;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.search.PatentQuery;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author rbouadjenek
 */
public class Test {

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
//        Directory dir = FSDirectory.open(new File("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/codeIndex/"));
        Directory dir = FSDirectory.open(new File("data/INDEX/codeIndex/"));
        IndexSearcher is = new IndexSearcher(DirectoryReader.open(dir));
//        PatentQuery query = new PatentQuery("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/topics/PAC-1028_EP-1652736-A1.xml", 1, 0, 0, 0, 0, 0, true, true);
        PatentQuery query = new PatentQuery("./data/CLEF-IP-2010/PAC_test/topics/PAC-1028_EP-1652736-A1.xml", 1, 0, 0, 0, 0, 0, true, true);
        long start = System.currentTimeMillis();
        TopDocs hits = is.search(GenerateClassCodesQuery.generateQuery(query.getFullClassCodes()), 10);
        long end = System.currentTimeMillis();
        System.err.println("Query: " + GenerateClassCodesQuery.generateQuery(query.getFullClassCodes()));
        System.err.println("Found " + hits.totalHits
                + " document(s) has matched query. Processed in " + Functions.getTimer(end - start) + ".");
        int i = 0;
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            i++;
            Document doc = is.doc(scoreDoc.doc);
            System.out.println(i+"- "+doc.get(PatentDocument.Classification)+"\t"+doc.get(PatentDocument.Title));

        }
    }
}
