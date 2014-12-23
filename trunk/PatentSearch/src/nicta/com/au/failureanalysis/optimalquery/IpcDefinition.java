package nicta.com.au.failureanalysis.optimalquery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nicta.com.au.main.Functions;
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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.hrstc.lucene.queryexpansion.GenerateClassCodesQuery;

/**
 * @author mona
 *
 */
public class IpcDefinition {
	private static PerFieldAnalyzerWrapper analyzer;

	public ArrayList<String> GetIpcDefWords(String queryfile) throws IOException, ParseException{
		Directory dir = FSDirectory.open(new File("data/INDEX/codeIndex/"));

		Map<String, Analyzer> analyzerPerField = new HashMap<>();
		analyzerPerField.put(PatentDocument.Title, new EnglishAnalyzer(Version.LUCENE_4_10_2, PatentsStopWords.UNIFIED_PATENT__ENGLISH_STOP_WORDS_SET));
		analyzerPerField.put(PatentDocument.Abstract, new EnglishAnalyzer(Version.LUCENE_4_10_2, PatentsStopWords.UNIFIED_PATENT__ENGLISH_STOP_WORDS_SET));
		analyzerPerField.put(PatentDocument.Description, new EnglishAnalyzer(Version.LUCENE_4_10_2, PatentsStopWords.UNIFIED_PATENT__ENGLISH_STOP_WORDS_SET));
		analyzerPerField.put(PatentDocument.Claims, new EnglishAnalyzer(Version.LUCENE_4_10_2, PatentsStopWords.UNIFIED_PATENT__ENGLISH_STOP_WORDS_SET));
		analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_4_10_2), analyzerPerField);

		IndexSearcher is = new IndexSearcher(DirectoryReader.open(dir));

		PatentQuery query = new PatentQuery("./data/CLEF-IP-2010/PAC_test/topics/" + queryfile, 1, 0, 0, 0, 0, 0, true, true);

		long start = System.currentTimeMillis();
		TopDocs hits = is.search(GenerateClassCodesQuery.generateQuery(query.getFullClassCodes()), 20);
		long end = System.currentTimeMillis();
		/*System.err.println("Query: " + GenerateClassCodesQuery.generateQuery(query.getFullClassCodes()));
		System.err.println("Found " + hits.totalHits
				+ " document(s) has matched query. Processed in " + Functions.getTimer(end - start) + ".");*/
		int i = 0;
		ArrayList<String> ipcdeflists = new ArrayList<>();

		 String regex = "(.)*(\\d)(.)*";   //remove words that contain digits.   
		 Pattern pattern = Pattern.compile(regex);

		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			i++;
			Document doc = is.doc(scoreDoc.doc);
			String code = doc.get(PatentDocument.Classification);
			String title = doc.get(PatentDocument.Title);
			int spaceIndex = title.indexOf("/");
			//          String title1= null;
			if (spaceIndex != -1)
			{
				title = title.substring(0, spaceIndex);
			}else{title = title;}

			if(code.length()>11){
//				System.out.println(i+"- "+ code +"\t"+ title);
				TokenStream ts = analyzer.tokenStream(PatentDocument.Title, title);
				String q = "";
				CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
				ts.reset();

				while (ts.incrementToken()) {
					String term = charTermAttribute.toString().replace(":", "\\:");
					q += term + " ";

					Matcher matcher = pattern.matcher(term);
					boolean isMatched = matcher.matches();

					if (!ipcdeflists.contains(term) && !(isMatched)) {
						ipcdeflists.add(term);
					} 
				}
				ts.close();
//				System.err.println(q);           
//				System.out.println(ipcdeflists);
			}
			//          System.out.println(code.length());
		}
//		System.out.println(ipcdeflists.size() + " " + ipcdeflists);

		return ipcdeflists;		
	}

	public static void main(String[] args) throws IOException, ParseException {
		String queryfile = "PAC-1041_EP-1285768-A2.xml";
		
		IpcDefinition def = new IpcDefinition();
		ArrayList<String> ipcdefs = def.GetIpcDefWords(queryfile);
		
		ArrayList<String> finalwords = new ArrayList<>();
		finalwords.addAll(ipcdefs); 
		System.out.println(ipcdefs.size()+ " " + ipcdefs);		 
	}

}

