/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.failureanalysis.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.Claim;
import nicta.com.au.patent.document.Claims;
import nicta.com.au.patent.document.ClassificationIpcr;
import nicta.com.au.patent.document.InventionTitle;
import nicta.com.au.patent.document.P;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.document.PatentsStopWords;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

/**
 *
 * @author rbouadjenek
 */
public class QueryGneration {

    private final PatentDocument pt;
    private final String[] queries = new String[7];
    private final Set<String> fullClassCodes = new HashSet<>();
    private PerFieldAnalyzerWrapper analyzer;
    private final int titleTreshold = 1;
    private final int abstractTreshold = 1;
    private final int descriptionTreshold = 1;
    private final int claimsTreshold = 1;
    public static final String all = "all";
    private static final String[] fields = {PatentDocument.Classification, PatentDocument.Title, PatentDocument.Abstract, PatentDocument.Description, "descriptionP5", PatentDocument.Claims, "claims1"};
    private final Map<String, Float> boosts;
    private final boolean filter;
    private final boolean stopWords;
    private final Map<String, Map<String, Integer>> vocabulary = new HashMap<>();
    private final Map<String, Integer> fieldsSize = new HashMap<>();

    public QueryGneration(String queryFileName, float titleBoost, float abstractBoost, float descriptionBoost, float descriptionP5Boost, float claimsBoost, float claims1Boost, boolean filter, boolean stopWords) throws IOException {
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
//        analyze();
    }

    public QueryGneration(PatentDocument queryPatent, Map<String, Float> boosts, boolean filter, boolean stopWords) throws IOException {
        this.pt = queryPatent;
        this.filter = filter;
        this.stopWords = stopWords;
        this.boosts = new HashMap<>();
        this.boosts.put(PatentDocument.Classification, boosts.get(PatentDocument.Classification));
        this.boosts.put(PatentDocument.Title, boosts.get(PatentDocument.Title));
        this.boosts.put(PatentDocument.Abstract, boosts.get(PatentDocument.Abstract));
        this.boosts.put(PatentDocument.Description, boosts.get(PatentDocument.Description));
        this.boosts.put("descriptionP5", boosts.get("descriptionP5"));
        this.boosts.put(PatentDocument.Claims, boosts.get(PatentDocument.Claims));
        this.boosts.put("claims1", boosts.get("claims1"));
//        getSectionTerms();
    }

    public Map<String, Integer> getSectionTerms(String section) throws IOException {
        String title = "";
        String ipc = "";
        String abstrac = "";
        String description = "";
        String descriptionP5 = "";
        String claims = "";
        String claims1 = "";

        //********************************************************************
        // leveraging Title
        //********************************************************************
        for (InventionTitle inventionTitle : pt.getTechnicalData().getInventionTitle()) {
            if (inventionTitle.getLang().toLowerCase().equals("en")) {
                title = inventionTitle.getContent();
            }
        }
        //********************************************************************
        // leveraging IPC Codes
        //********************************************************************
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
        //********************************************************************
        // leveraging Abstract
        //********************************************************************
        if(pt.getAbstrac().getLang() != null){
        	if (pt.getAbstrac().getLang().toLowerCase().equals("en")) {
        		abstrac = pt.getAbstrac().getContent();
        	}
        }
        //********************************************************************
        // leveraging Description
        //********************************************************************
        if (pt.getDescription() != null) {
        	if (pt.getDescription().getLang().toLowerCase().equals("en")) {
        		for (P p : pt.getDescription().getP()) {
        			if (Integer.parseInt(p.getNum()) == 1 || Integer.parseInt(p.getNum()) == 2
        					|| Integer.parseInt(p.getNum()) == 3 || Integer.parseInt(p.getNum()) == 4
        					|| Integer.parseInt(p.getNum()) == 5) { // Leveraging first 5 paragraphes
        				descriptionP5 += p.getContent() + " ";
        			}
        			description += p.getContent() + " ";
        		}
        	}
        }
        //********************************************************************
        // leveraging Claims
        //********************************************************************
        for (Claims cs : pt.getClaims()) {
            if (cs.getLang().toLowerCase().equals("en")) {
                for (Claim claim : cs.getClaim()) {
                    if (Integer.parseInt(claim.getNum()) == 1) {// Leveraging Claims 1
                        claims1 += claim.getClaimText() + " ";
                    }
                    claims += claim.getClaimText() + " ";
                }
            }
        }
        //********************************************************************
        this.queries[0] = ipc;
        this.queries[1] = title;
        this.queries[2] = abstrac;
        this.queries[3] = description;
        this.queries[4] = descriptionP5;
        this.queries[5] = claims;
        this.queries[6] = claims1;
        Map<String, Analyzer> analyzerPerField = new HashMap<>();
        if (stopWords) {
            /*analyzerPerField.put(PatentDocument.Title, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.TITLE_ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Abstract, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ABSTRACT_ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Description, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.DESCRIPTION_ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Claims, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.CLAIMS_ENGLISH_STOP_WORDS_SET));*/
            
            analyzerPerField.put(PatentDocument.Title, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.UNIFIED_PATENT__ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Abstract, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.UNIFIED_PATENT__ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Description, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.UNIFIED_PATENT__ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Claims, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.UNIFIED_PATENT__ENGLISH_STOP_WORDS_SET));
        } else {
            analyzerPerField.put(PatentDocument.Title, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Abstract, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Description, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Claims, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ENGLISH_STOP_WORDS_SET));
        }
        analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_48), analyzerPerField);

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
        /*if (oneNumber) {
            String qText = queries[j];
            String q = transformation(analyzer.tokenStream(fields[j], qText), titleTreshold, PatentDocument.Title);
//            System.err.println(q);
            queries[1] = q;
            boosts.put(PatentDocument.Title, (float) 1.0);

            q = transformation(analyzer.tokenStream(fields[j], qText), abstractTreshold, PatentDocument.Abstract);
//            System.err.println(q);
            queries[2] = q;
            boosts.put(PatentDocument.Abstract, (float) 1.0);
            q = transformation(analyzer.tokenStream(fields[j], qText), descriptionTreshold, PatentDocument.Description);
//            System.err.println(q);
            queries[3] = q;
            boosts.put(PatentDocument.Description, (float) 1.0);
            queries[4] = "";
            q = transformation(analyzer.tokenStream(fields[j], qText), claimsTreshold, PatentDocument.Claims);
//            System.err.println(q);
            queries[5] = q;
            boosts.put(PatentDocument.Claims, (float) 1.0);
            queries[6] = "";
        } else {*/
            String[] qText = queries;
            String q = transformation(analyzer.tokenStream(PatentDocument.Title, qText[1]), titleTreshold, PatentDocument.Title);
            Map<String, Integer> t = getTerms(analyzer.tokenStream(PatentDocument.Title, qText[1]), titleTreshold, PatentDocument.Title);
//            System.err.println(q);
            queries[1] = q;
            q = transformation(analyzer.tokenStream(PatentDocument.Abstract, qText[2]), abstractTreshold, PatentDocument.Abstract);
            Map<String, Integer> a = getTerms(analyzer.tokenStream(PatentDocument.Abstract, qText[2]), abstractTreshold, PatentDocument.Abstract);
//            System.err.println(q);
            queries[2] = q;
            q = transformation(analyzer.tokenStream(PatentDocument.Description, qText[3]), descriptionTreshold, PatentDocument.Description);
            Map<String, Integer> d = getTerms(analyzer.tokenStream(PatentDocument.Description, qText[3]), descriptionTreshold, PatentDocument.Description);
//            System.err.println(q);
            queries[3] = q;
            q = transformation(analyzer.tokenStream(PatentDocument.Description, qText[4]), descriptionTreshold, null);
            queries[4] = q;
            q = transformation(analyzer.tokenStream(PatentDocument.Claims, qText[5]), claimsTreshold, PatentDocument.Claims);
            Map<String, Integer> c = getTerms(analyzer.tokenStream(PatentDocument.Claims, qText[5]), claimsTreshold, PatentDocument.Claims);
//            System.err.println(q);
            queries[5] = q;
            q = transformation(analyzer.tokenStream(PatentDocument.Claims, qText[6]), claimsTreshold, null);
            queries[6] = q;
            
            if(section.equals("title")){
//                System.err.println(q);
                return t;}
            else{ 
            	if(section.equals("abstract")){
            		return a;
            	}
            	else{
            		if(section.equals("description")){
            			return d;
            		}
            		else{
            			return c;
            		}
            	}
                	
                }
//            return t;
//        }
    }

    private  Map<String, Integer> getTerms(TokenStream ts, int treshold, String field) throws IOException {
        Map<String, Integer> m = new HashMap<>();
        Map<String, Integer> qterm_freq = new HashMap<>();
        
        String q = "";
        CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
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
        ts.close();
//        return q;
        q = "";
//        int count = 0;
        for (String k : m.keySet()) {
            if (m.get(k) >= treshold) {
                if (!Functions.isNumeric(k)) {
                    q += k + "^" + m.get(k) + " ";
                    qterm_freq.put(k, m.get(k));
//                    count++;
//                    System.out.println(count + " " + k + " " + m.get(k));
                }
            }
        }
//        System.out.println("-------------------");
        if (field != null) {
            vocabulary.put(field, m);
        }
        fieldsSize.put(field, s);
//        return q;
        return qterm_freq;
    }
    
    private String transformation(TokenStream ts, int treshold, String field) throws IOException {
        Map<String, Integer> m = new HashMap<>();
        String q = "";
        CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
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
        ts.close();
//        return q;
        q = "";
        for (String k : m.keySet()) {
            if (m.get(k) >= treshold) {
                if (!Functions.isNumeric(k)) {
                    q += k + "^" + m.get(k) + " ";
//                    System.out.println(k);
                }
            }
        }
        if (field != null) {
            vocabulary.put(field, m);
        }
        fieldsSize.put(field, s);
        return q;
    }

    public Query parse() throws ParseException {
        if (queries.length != fields.length) {
            throw new IllegalArgumentException("queries.length != fields.length");
        }
        BooleanQuery bQuery = new BooleanQuery();
        BooleanQuery bQueryFields = new BooleanQuery();
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        for (int i = 1; i < fields.length; i++) {
            if (queries[i] != null && !queries[i].equals("") && boosts.get(fields[i]) != 0) {
                QueryParser qp;
                if (i == 4 || i == 6) {
                    qp = new QueryParser(Version.LUCENE_48, fields[i - 1], new StandardAnalyzer(Version.LUCENE_48));
                } else {
                    qp = new QueryParser(Version.LUCENE_48, fields[i], new StandardAnalyzer(Version.LUCENE_48));
                }
                Query q = qp.parse(queries[i]);
                q.setBoost(boosts.get(fields[i]));
                if (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0) {
                    bQueryFields.add(q, BooleanClause.Occur.SHOULD);
                }
            }
        }
        if (filter) {
            Query q = new QueryParser(Version.LUCENE_48, fields[0], new StandardAnalyzer(Version.LUCENE_48)).parse(queries[0]);
            q.setBoost(boosts.get(fields[0]));
            bQuery.add(q, BooleanClause.Occur.MUST);
        }
        bQuery.add(bQueryFields, BooleanClause.Occur.MUST);
        return bQuery;
    }

    public String[] getQueries() {
        return queries;
    }

    public static String[] getFields() {
        return fields;
    }

    public boolean isFilter() {
        return filter;
    }

    public Map<String, Float> getBoosts() {
        return boosts;
    }

    public int getFreqInTitle(String term) {
        if (vocabulary.get(PatentDocument.Title).containsKey(term)) {
            return vocabulary.get(PatentDocument.Title).get(term);
        } else {
            return 0;
        }
    }

    public int getFreqInAbstract(String term) {
        if (vocabulary.get(PatentDocument.Abstract).containsKey(term)) {
            return vocabulary.get(PatentDocument.Abstract).get(term);
        } else {
            return 0;
        }
    }

    public int getFreqInDescription(String term) {
        if (vocabulary.get(PatentDocument.Description).containsKey(term)) {
            return vocabulary.get(PatentDocument.Description).get(term);
        } else {
            return 0;
        }
    }

    public int getFreqInClaims(String term) {
        if (vocabulary.get(PatentDocument.Claims).containsKey(term)) {
            return vocabulary.get(PatentDocument.Claims).get(term);
        } else {
            return 0;
        }
    }

    public int getTitleSize() {
        return fieldsSize.get(PatentDocument.Title);
    }

    public int getAbstractSize() {
        return fieldsSize.get(PatentDocument.Abstract);
    }

    public int getDescriptionSize() {
        return fieldsSize.get(PatentDocument.Description);
    }

    public int getClaimsSize() {
        return fieldsSize.get(PatentDocument.Claims);
    }

    public Set<String> getFullClassCodes() {
        return fullClassCodes;
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
    
    public String getAbstract () throws IOException {
		String abs = "";		

		//********************************************************************
        // leveraging Abstract
        //********************************************************************
        if(pt.getAbstrac().getLang() != null){
        	if (pt.getAbstrac().getLang().toLowerCase().equals("en")) {
        		abs = pt.getAbstrac().getContent();
        	}
        }
		
		return abs;
	}
    
    public String getDescLangOrMising() throws IOException {

    	String status = null;
    	if(pt.getDescription() != null) {
    		if (pt.getDescription().getLang().toLowerCase().equals("en")){
    			status = "en";
    		}else {
    			status = "non-en";
    		}
    	}else { 
    		status = "missing";
    	}

    	return status;
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
//			System.out.println(p1 + "\t" + p1.charAt(0));
			
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
	
	public String getIpc1stElement() throws IOException {
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
			codes.add(p1.substring(0, 1));
			/*System.out.println(p1 + "\t" + p1.substring(0,1));*/
			
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
	
	public String getIpc1stTwoElement() throws IOException {
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
			codes.add(p1.substring(0, 3));
			/*System.out.println(p1 + "\t" + p1.substring(0,1));*/
			
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

	public String getmainIpc() throws IOException {
		String ipc = "";

		// ********************************************************************
		// leveraging IPC Codes
		// ********************************************************************
		Set<String> codes = new HashSet<>();
		List<ClassificationIpcr> x = pt.getTechnicalData().getClassificationIpcr();
//		if(x.get(0).getContent() != null){
		/*System.out.println(x.get(0).getContent());*/
		StringTokenizer st = new StringTokenizer(pt.getTechnicalData().getClassificationIpcr().get(0).getContent());
		String p1 = st.nextToken();
//		System.out.println(p1);		
		
		return p1;
//		}
		/*else{
			return "No ipc code";
					}*/
	}
	
	public List<String> getIpclist() throws IOException {
		String ipc = "";

		// ********************************************************************
		// leveraging IPC Codes
		// ********************************************************************
		Set<String> codes = new HashSet<>();
		List<ClassificationIpcr> x = pt.getTechnicalData().getClassificationIpcr();
		for (ClassificationIpcr ipcCode : pt.getTechnicalData()
				.getClassificationIpcr()) {
//			System.out.println(ipcCode.getContent());
			StringTokenizer st = new StringTokenizer(ipcCode.getContent());
			
			String p1 = st.nextToken();
//			System.out.println(p1);
			String p2 = st.nextToken();
//			System.out.println(p2);
			
/*------------- hashset contains no duplicate elements. ---------------*/
			codes.add(p1);			

		}
		 List<String> list = new ArrayList<String>(codes);
//		    System.out.println(list);
//		System.out.println(codes);
		/*for (String code : codes) {
			if (!ipc.contains(code)) {				
				ipc += code + " ";
				
			}
		}*/
//		System.out.println(ipc);
		return list;
	}

    
    public static void main(String[] args) throws ParseException, IOException {//PAC_1913_EP-1691238-A2.xml
        // TODO code application logic here
    	String path = "data/CLEF-IP-2010/PAC_test/topics/" /*"/media/mona/MyProfesion/EP/000000/96/81/18/"*/;
    	String queryfile = /*"PAC-1134_EP-1783182-A1.xml"*/"PAC-1644_EP-1369232-A1.xml"/*"PAC-1064_EP-1426187-A2.xml"*//*"PAC-1216_EP-1749865-A1.xml"*//*"PAC-825_EP-1267369-A2.xml"*/ /*"UN-EP-0968118.xml"*/;
		
    	QueryGneration query = new QueryGneration(path + queryfile, 0, 0, 1, 0, 0, 0, true, true);
    	/*Map<String, Integer> terms = query.getSectionTerms("title""abstract""description""claims");   
    	
    	int count = 0;
    	for(Entry<String, Integer> t : terms.entrySet()){
    		count++;
    		System.out.println(count + " " + t.getKey() + " " + t.getValue());
    	}
    	
    	System.out.println(query.parse());*/
    	
    	System.out.println("----------------------------------------------------------------------------------------------");
		System.out.println("-----------------------------Testing getTitle() & getIpc() methods----------------------------");
		System.out.println("----------------------------------------------------------------------------------------------");
		System.out.println(query.getTitle()); 
		System.out.println(query.getIpc());
		System.out.println(query.getIpc1stElement());
		System.out.println(query.getIpc1stTwoElement()); 
					
		List<String> ipclists = query.getIpclist();
		
		/*System.out.println("----------------------------");
		System.out.println(ipclists.get(0));
		System.out.println("----------------------------");*/
		
		/*for (String ipc : query.getIpclist()){
			System.out.println(ipc);			
		}*/
		
		
		System.out.println("Main IPC COde: " + query.getmainIpc());
		System.out.println(query.getmainIpc().substring(0, 1));
		System.out.println(query.getmainIpc().substring(1,3));
		System.out.println(query.getDescLangOrMising());

    }

}
