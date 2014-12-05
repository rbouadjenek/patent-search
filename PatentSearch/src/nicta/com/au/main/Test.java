/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.main;

import com.hrstc.lucene.queryexpansion.GenerateClassCodesQuery;

import static com.hrstc.lucene.queryexpansion.GenerateClassCodesQuery.generateQuery;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.document.PatentsStopWords;
import nicta.com.au.patent.pac.search.PatentQuery;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author rbouadjenek
 */
public class Test {
	private static PerFieldAnalyzerWrapper analyzer;
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
//        Directory dir = FSDirectory.open(new File("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/codeIndex/"));
        Directory dir = FSDirectory.open(new File("data/INDEX/codeIndex/"));
        Map<String, Analyzer> analyzerPerField = new HashMap<>();
        analyzerPerField.put(PatentDocument.Title, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.UNIFIED_PATENT__ENGLISH_STOP_WORDS_SET));
        analyzerPerField.put(PatentDocument.Abstract, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.UNIFIED_PATENT__ENGLISH_STOP_WORDS_SET));
        analyzerPerField.put(PatentDocument.Description, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.UNIFIED_PATENT__ENGLISH_STOP_WORDS_SET));
        analyzerPerField.put(PatentDocument.Claims, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.UNIFIED_PATENT__ENGLISH_STOP_WORDS_SET));
        analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_48), analyzerPerField);
        
        IndexSearcher is = new IndexSearcher(DirectoryReader.open(dir));
//        PatentQuery query = new PatentQuery("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/topics/PAC-1028_EP-1652736-A1.xml", 1, 0, 0, 0, 0, 0, true, true);
        PatentQuery query = new PatentQuery("./data/CLEF-IP-2010/PAC_test/topics/PAC-1041_EP-1285768-A2.xml", 1, 0, 0, 0, 0, 0, true, true);
//        PatentQuery query = new PatentQuery("/media/mona/MyProfesion/EP/000000/96/81/18/UN-EP-0968118.xml", 1, 0, 0, 0, 0, 0, true, true);
        long start = System.currentTimeMillis();
        TopDocs hits = is.search(GenerateClassCodesQuery.generateQuery(query.getFullClassCodes()), 20);
        long end = System.currentTimeMillis();
        System.err.println("Query: " + GenerateClassCodesQuery.generateQuery(query.getFullClassCodes()));
        System.err.println("Found " + hits.totalHits
                + " document(s) has matched query. Processed in " + Functions.getTimer(end - start) + ".");
        int i = 0;
        ArrayList<String> m = new ArrayList<>();
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            i++;
            Document doc = is.doc(scoreDoc.doc);
            String code = doc.get(PatentDocument.Classification);
            String title = doc.get(PatentDocument.Title);
            int spaceIndex = title.indexOf("/");
//            String title1= null;
            if (spaceIndex != -1)
            {
                title = title.substring(0, spaceIndex);
            }else{title = title;}
//            System.out.println(title1);
                        
//            System.out.println(i+"- "+doc.get(PatentDocument.Classification)+"\t"+doc.get(PatentDocument.Title));
            if(code.length()>11){System.out.println(i+"- "+ code +"\t"+ title);
            TokenStream ts = analyzer.tokenStream(PatentDocument.Title, title);
            String q = "";
            CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
            ts.reset();

            while (ts.incrementToken()) {
                String term = charTermAttribute.toString().replace(":", "\\:");
                q += term + " ";
                if (!m.contains(term)) {
                    m.add(term);
                } 

            }
            ts.close();
            System.err.println(q);           
            System.out.println(m);
            }
//            System.out.println(code.length());
        }
        System.out.println(m.size() + " " + m);
    }
}

