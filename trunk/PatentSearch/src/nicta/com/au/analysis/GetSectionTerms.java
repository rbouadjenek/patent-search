package nicta.com.au.analysis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.Claim;
import nicta.com.au.patent.document.Claims;
import nicta.com.au.patent.document.ClassificationIpcr;
import nicta.com.au.patent.document.InventionTitle;
import nicta.com.au.patent.document.P;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.document.PatentsStopWords;
import nicta.com.au.patent.pac.search.PatentQuery;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.Version;

/**
 * @author 
 * 
 */
public class GetSectionTerms {

	private final PatentDocument pt;
	private final String[] queries = new String[7];
	private final Set<String> fullClassCodes = new HashSet<>();
	private PerFieldAnalyzerWrapper analyzer;
	private final int titleTreshold = 1;
	private final int abstractTreshold = 1;
	private final int descriptionTreshold = 1;
	private final int claimsTreshold = 1;
	public static final String all = "all";
	private static final String[] fields = { PatentDocument.Classification,
			PatentDocument.Title, PatentDocument.Abstract,
			PatentDocument.Description, "descriptionP5", PatentDocument.Claims,
			"claims1" };
	private final Map<String, Float> boosts;
	private final boolean filter;
	private final boolean stopWords;
	private final Map<String, Map<String, Integer>> vocabulary = new HashMap<>();
	private final Map<String, Integer> fieldsSize = new HashMap<>();

	public GetSectionTerms(String queryFileName, float titleBoost,
			float abstractBoost, float descriptionBoost,
			float descriptionP5Boost, float claimsBoost, float claims1Boost,
			boolean filter, boolean stopWords) throws IOException {
		this.pt = new PatentDocument(queryFileName);
		boosts = new HashMap<>();
		boosts.put(PatentDocument.Classification, new Float(0));
		boosts.put(PatentDocument.Title, new Float(titleBoost));
		boosts.put(PatentDocument.Abstract, new Float(abstractBoost));
		boosts.put(PatentDocument.Description, new Float(descriptionBoost));
		boosts.put("descriptionP5", new Float(descriptionP5Boost));
		boosts.put(PatentDocument.Claims, new Float(claimsBoost));
		boosts.put("claims1", new Float(claims1Boost));
		this.filter = filter;
		this.stopWords = stopWords;
//		analyze();
	}

	Map<String, Integer> analyze() throws IOException {
		String title = "";
		String ipc = "";
		String abstrac = "";
		String description = "";
		String descriptionP5 = "";
		String claims = "";
		String claims1 = "";

		// ********************************************************************
		// leveraging Title
		// ********************************************************************
		for (InventionTitle inventionTitle : pt.getTechnicalData()
				.getInventionTitle()) {
			if (inventionTitle.getLang().toLowerCase().equals("en")) {
				title = inventionTitle.getContent();
//				System.out.println(title);
			}
		}
		// ********************************************************************
		// leveraging IPC Codes
		// ********************************************************************
		Set<String> codes = new HashSet<>();
		for (ClassificationIpcr ipcCode : pt.getTechnicalData().getClassificationIpcr()) {
			StringTokenizer st = new StringTokenizer(ipcCode.getContent());
			String p1 = st.nextToken();
			String p2 = st.nextToken();
			codes.add(p1);
			fullClassCodes.add(p1 + p2);
		}
		for (String code : codes) {
			if (!ipc.contains(code)) {
				ipc += code + " ";
			}
		}
		// ********************************************************************
		// leveraging Abstract
		// ********************************************************************
		if(pt.getAbstrac().getLang() != null){
				
		if (pt.getAbstrac().getLang().toLowerCase().equals("en")) {
			abstrac = pt.getAbstrac().getContent();
		}/*else {abstrac = null;} */
		}
		// ********************************************************************
		// leveraging Description
		// ********************************************************************
		
		if (pt.getDescription() != null) {
			if (pt.getDescription().getLang().toLowerCase().equals("en")) {
				for (P p : pt.getDescription().getP()) {
					if (Integer.parseInt(p.getNum()) == 1
							|| Integer.parseInt(p.getNum()) == 2
							|| Integer.parseInt(p.getNum()) == 3
							|| Integer.parseInt(p.getNum()) == 4
							|| Integer.parseInt(p.getNum()) == 5) { // Leveraging
																	// first 5
																	// paragraphes
						descriptionP5 += p.getContent() + " ";
					}
					description += p.getContent() + " ";
				}
			}
		}
		// ********************************************************************
		// leveraging Claims
		// ********************************************************************
		for (Claims cs : pt.getClaims()) {
			if (cs.getLang().toLowerCase().equals("en")) {
				for (Claim claim : cs.getClaim()) {
					if (Integer.parseInt(claim.getNum()) == 1) {// Leveraging
																// Claims 1
						claims1 += claim.getClaimText() + " ";
					}
					claims += claim.getClaimText() + " ";
				}
			}
		}
		// ********************************************************************
		this.queries[0] = ipc;
		this.queries[1] = title;
		this.queries[2] = abstrac;
		this.queries[3] = description;
		this.queries[4] = descriptionP5;
		this.queries[5] = claims;
		this.queries[6] = claims1;
		Map<String, Analyzer> analyzerPerField = new HashMap<>();
		if (stopWords) {
			analyzerPerField.put(PatentDocument.Title, new EnglishAnalyzer(
					Version.LUCENE_45,
					PatentsStopWords.TITLE_ENGLISH_STOP_WORDS_SET));
			analyzerPerField.put(PatentDocument.Abstract, new EnglishAnalyzer(
					Version.LUCENE_45,
					PatentsStopWords.ABSTRACT_ENGLISH_STOP_WORDS_SET));
			analyzerPerField
					.put(PatentDocument.Description,
							new EnglishAnalyzer(
									Version.LUCENE_45,
									PatentsStopWords.DESCRIPTION_ENGLISH_STOP_WORDS_SET));
			analyzerPerField.put(PatentDocument.Claims, new EnglishAnalyzer(
					Version.LUCENE_45,
					PatentsStopWords.CLAIMS_ENGLISH_STOP_WORDS_SET));
		} else {
			analyzerPerField
					.put(PatentDocument.Title, new EnglishAnalyzer(
							Version.LUCENE_45,
							PatentsStopWords.ENGLISH_STOP_WORDS_SET));
			analyzerPerField
					.put(PatentDocument.Abstract, new EnglishAnalyzer(
							Version.LUCENE_45,
							PatentsStopWords.ENGLISH_STOP_WORDS_SET));
			analyzerPerField.put(PatentDocument.Description,
					new EnglishAnalyzer(Version.LUCENE_45,
							PatentsStopWords.ENGLISH_STOP_WORDS_SET));
			analyzerPerField
					.put(PatentDocument.Claims, new EnglishAnalyzer(
							Version.LUCENE_45,
							PatentsStopWords.ENGLISH_STOP_WORDS_SET));
		}
		analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(
				Version.LUCENE_45), analyzerPerField);

		boolean oneNumber = false;
		int j = -1;
		for (int i = 1; i < fields.length; i++) {
			float v = boosts.get(fields[i]);
			if (oneNumber == false && v > 0) {
				oneNumber = true;
				j = i;
			} else if (oneNumber && v > 0) {
				oneNumber = false;
				j = -1;
				break;
			}
		}
//		if (oneNumber) {
			String qText = queries[3];
			Map<String, Integer> descriptionmap = transformation(
					analyzer.tokenStream(fields[3], qText),
					descriptionTreshold, PatentDocument.Description);
			/*for (String k : descriptionmap.keySet()) {
				if (!Functions.isNumeric(k)) {
//	                q += k + "^" + m.get(k) + " ";
	                System.out.println(k);
	            }
			}
*/
			boosts.put(PatentDocument.Description, (float) 1.0);

//		} 
		return descriptionmap;		
	}
	

	private Map<String, Integer> transformation(TokenStream ts, int treshold, String field)
			throws IOException {
		Map<String, Integer> m = new HashMap<>();
		String q = "";
		CharTermAttribute charTermAttribute = ts
				.addAttribute(CharTermAttribute.class);
		ts.reset();
		int s = 0;
		while (ts.incrementToken()) {
			String term = charTermAttribute.toString().replace(":", "\\:");
			q += term + " ";
			if (m.containsKey(term)) {
				m.put(term, m.get(term) + 1);
			} else {
				m.put(term, 1);
			}
			s++;
		}
		// return q;
		/*q = "";
		for (String k : m.keySet()) {
			if (m.get(k) >= treshold) {
				if (!Functions.isNumeric(k)) {
					q += k + "^" + m.get(k) + " ";
					// System.out.println(k);
				}
			}
		}*/
		if (field != null) {
			vocabulary.put(field, m);
		}
		fieldsSize.put(field, s);
		return m;
	}
	
	
	public String getTitle() throws IOException {
		String title = "";		

		// ********************************************************************
		// leveraging Title
		// ********************************************************************
		for (InventionTitle inventionTitle : pt.getTechnicalData()
				.getInventionTitle()) {
			if (inventionTitle.getLang().toLowerCase().equals("en")) {
				title = inventionTitle.getContent();
				
//				System.out.println(title);
			}
		}
		
		return title;
	}

	public String getIpc() throws IOException {
		String ipc = "";

		// ********************************************************************
		// leveraging IPC Codes
		// ********************************************************************
		Set<String> codes = new HashSet<>();
		for (ClassificationIpcr ipcCode : pt.getTechnicalData()
				.getClassificationIpcr()) {
//			System.out.println(ipcCode.getContent());
			StringTokenizer st = new StringTokenizer(ipcCode.getContent());
			
			String p1 = st.nextToken();
//			System.out.println(p1);
			String p2 = st.nextToken();
/*------------- hashset contains no duplicate elements. ---------------*/
			codes.add(p1);
			
			fullClassCodes.add(p1 + p2);
		}
		
//		System.out.println(codes);
		for (String code : codes) {
			if (!ipc.contains(code)) {				
				ipc += code + " ";
				
			}
		}
//		System.out.println(ipc);
		return ipc;
	}

	public Set<String> getIpclist() throws IOException {
		String ipc = "";

		// ********************************************************************
		// leveraging IPC Codes
		// ********************************************************************
		Set<String> codes = new HashSet<>();
		for (ClassificationIpcr ipcCode : pt.getTechnicalData()
				.getClassificationIpcr()) {
//			System.out.println(ipcCode.getContent());
			StringTokenizer st = new StringTokenizer(ipcCode.getContent());
			
			String p1 = st.nextToken();
//			System.out.println(p1);
			String p2 = st.nextToken();
			
/*------------- hashset contains no duplicate elements. ---------------*/
			codes.add(p1);			

		}
		
//		System.out.println(codes);
		/*for (String code : codes) {
			if (!ipc.contains(code)) {				
				ipc += code + " ";
				
			}
		}*/
//		System.out.println(ipc);
		return codes;
	}
	public static void main(String[] args) throws ParseException, IOException {
		
//		FileOutputStream out = new FileOutputStream("C:/Users/Mona/workspace/PatentSearch/output/compare.txt");
//		FileOutputStream out = new FileOutputStream("output/compare.txt");
//		PrintStream ps = new PrintStream(out);

		
		GetSectionTerms query = new GetSectionTerms("data/CLEF-IP-2010/PAC_test/topics/PAC-544_EP-1405720-A1.xml",0, 0, 1, 0, 0, 0, true, true);
//		GetSectionTerms query = new GetSectionTerms("data/CLEF-IP-2010/PAC_test/topics/PAC-1001_EP-1233512-A2.xml",0, 0, 1, 0, 0, 0, true, true);

//		GetSectionTerms reldocument = new GetSectionTerms("data/EP/UN-EP-1152529.xml",0, 0, 1, 0, 0, 0, true, true);

		/*---------------- Please uncomment between two paths when you ---------------------*/
		
		GetSectionTerms reldocument = new GetSectionTerms("/media/mona/MyProfesion/EP/000001/15/25/29/UN-EP-1152529.xml",0, 0, 1, 0, 0, 0, true, true);
//		GetSectionTerms reldocument = new GetSectionTerms("F:/EP/000001/15/25/29/UN-EP-1152529.xml",0, 0, 1, 0, 0, 0, true, true);
		
//		GetSectionTerms query = new GetSectionTerms("C:/Users/Mona/workspace/PatentSearch/data/EP/RelDocsPAC-848/PAC-848_EP-1344810-A1.xml",0, 0, 1, 0, 0, 0, true, true);
//		GetSectionTerms reldocument = new GetSectionTerms("C:/Users/Mona/workspace/PatentSearch/data/EP/RelDocsPAC-848/89/UN-EP-0575189.xml",0, 0, 1, 0, 0, 0, true, true);
		
		
//		GetSectionTerms doc = new GetSectionTerms("/media/mona/MyProfesion/EP/000000/75/57/77/UN-EP-0755777.xml",0, 0, 1, 0, 0, 0, true, true); //Des='De' 
		
		//TODO: Please uncomment two following lines to see the error for patents with missing abstract.  
//		GetSectionTerms doc = new GetSectionTerms("/media/mona/MyProfesion/EP/000000/19/17/71/UN-EP-0191771.xml",0, 0, 1, 0, 0, 0, true, true);
//		Map<String, Integer> maps = doc.analyze();
	
		GetSectionTerms doc = new GetSectionTerms("/media/mona/MyProfesion/EP/000000/18/81/15/UN-EP-0188115.xml",0, 0, 1, 0, 0, 0, true, true);
		Map<String, Integer> maps = doc.analyze();
	
		for (Entry<String, Integer> entry  : maps.entrySet()) {			
		    System.out.println(entry.getKey() + " - " + entry.getValue());}
		
				
		Map<String, Integer> query_map = query.analyze();
		Map<String, Integer> reldoc_map = reldocument.analyze();
		
		int count = 0;
		for (Entry<String, Integer> q : query_map.entrySet()){

			if (!Functions.isNumeric(q.getKey())) {
				
//				int n = Collections.frequency(reldoc_map.keySet(), q.getKey());
				if(reldoc_map.get(q.getKey()) != null){
					count++;
				int doc_freq = reldoc_map.get(q.getKey());
				

				System.out.println( count + "\t" + q.getKey() + "\t" + doc_freq);
//				ps.println(count + "\t" + q.getKey() + "\t" + doc_freq);
				}

//                System.out.println(k);
            }
		}
//		System.out.println(query.parse());
		
		
		System.out.println("----------------------------------------------------------------------------------------------");
		System.out.println("-----------------------------Testing getTitle() & getIpc() methods----------------------------");
		System.out.println("----------------------------------------------------------------------------------------------");
		System.out.println(query.getTitle()); 
		System.out.println(query.getIpc()); 
					
		query.getIpclist();		
		for (String ipc : query.getIpclist()){
			System.out.println(ipc);			
		}
		

	}

}
