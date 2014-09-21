package nicta.com.au.failureanalysis.TermOverlap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.evaluate.AnalyseFNs;
import nicta.com.au.failureanalysis.evaluate.EvaluateResults;
import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

/**
 * @author mona
 * @param reader
 * @throws IOException 
 */
public class TermOverlapConfig2 {
	static String querypath = "data/CLEF-IP-2010/PAC_test/topics/";	
	static String indexDir = "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";
	static String queryfield = PatentDocument.Description;
	static String titlefield = PatentDocument.Title;
	static String absfield = PatentDocument.Abstract;
	static String descfield = PatentDocument.Description;
	static String claimsfield = PatentDocument.Claims;
	
	/**
	 * @param reader
	 * @throws IOException
	 * FN patents
	 *  Config(2-a): [O(qD,docT)+O(qD,docA)+O(qD,docD)+O(qD, docC)]/|qD|
	 *  Config(2-b): [overlap(queryDesc,docTitle)/|qDesc U docTitle| + 
	 * overlap(queryDesc,docAbs)/|qDesc U docAbs| +
	 * overlap(queryDesc,docDesc)/|qDesc U docDesc| +
	 * overlap(queryDesc, docClaims)/|qDesc U docClaims|]
	 */
	public void FNsOverlapSeperateSections(CollectionReader reader) throws IOException {
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/TermOverlap/termoverlp-FNs-config(2):seperatesections-test.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
//		EvaluateResults er = new EvaluateResults();
		AnalyseFNs afn = new AnalyseFNs();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			//			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int n_enfns = enfns.size();

			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(queryfield);
			int querysize = qterms.size();

			float sum =0;
			float usum =0;
			float avg = 0;
			float uavg = 0;
			float overlapratio = 0;
			float uoverlapratio = 0;
//			int querydocintersection;
			int querytitleoverlap;
			int queryabsoverlap;
			int querydescoverlap;
			int queryclaimsoverlap;
			int SectionsSumOverlap;
//			int union;
			int titleunion;
			int absunion;
			int descunion;
			int claimsunion;
//			int dminusoverlap = 0;
			int titleminusoverlap;
			int absminusoverlap;
			int descminusoverlap;
			int claimsminusoverlap;
			
			int titlesize;
			int abssize;
			int descsize;
			int claimssize;

			if(n_enfns != 0){

				System.out.println(queryid);
				System.out.println("-----------------------------------------------------------------------------------------------------------------");
				//				System.out.println("FN patent ID" + "\t" + "overlap" + "\t" + "|Q|" + "\t" + "|D|" + "\t" + "D-olap" + "\t" + "|Q U D|" + "\t" + "olap/|Q|" + "\t" + "olap/|Q U D|");
				System.out.println("FN patent ID" + "\t" + "(qD,dT)" + "\t" + "(qD,dA)" + "\t" + "(qD,dD)" + "\t" + "(qD,dC)" + "\t" + "|Q|" + "\t" + "|D U T|" + "\t" + "|D U A|" + "\t" + "|D U D|" + "\t" + "|D U C|"+ "\t" + "|sum/Q|" + " \t" + "sum|O(docSecs)|");
				System.out.println("-----------------------------------------------------------------------------------------------------------------");
				for (String doc : enfns) { 
//					querydocintersection = 0;
					querytitleoverlap = 0;
					queryabsoverlap = 0;
					querydescoverlap = 0;
					queryclaimsoverlap = 0;
					SectionsSumOverlap = 0;
					HashSet<String> titleterms = reader.getDocTerms("UN-"+doc, titlefield);
					HashSet<String> absterms = reader.getDocTerms("UN-"+doc, absfield);
					HashSet<String> descterms = reader.getDocTerms("UN-"+doc, descfield);
					HashSet<String> claimsterms = reader.getDocTerms("UN-"+doc, claimsfield);
					if(titleterms!=null){titlesize = titleterms.size();}else{titlesize=0;}
					if(absterms!=null){abssize = absterms.size();}else{abssize = 0;}
					if(descterms!=null){descsize = descterms.size();}else{descsize = 0;}
					if(claimsterms!=null){claimssize = claimsterms.size();}else{claimssize = 0;}
					
					for(Entry<String, Integer> t : qterms.entrySet()){
						if(titleterms!=null){boolean titleexists = titleterms.contains(t.getKey());
						if(titleexists){
							querytitleoverlap++;}}		
						
						if(absterms!=null){boolean absexists = absterms.contains(t.getKey());
						if(absexists){
							queryabsoverlap++;	}}
						
						if(descterms!=null){boolean descexists = descterms.contains(t.getKey());
						if(descexists){
							querydescoverlap++;	}}
						
						if(claimsterms!=null){boolean claimsexists = claimsterms.contains(t.getKey());
						if(claimsexists){
							queryclaimsoverlap++;	}}			

					}

					SectionsSumOverlap = querytitleoverlap + queryabsoverlap 
							+ querydescoverlap + queryclaimsoverlap;

					titleminusoverlap = titlesize - querytitleoverlap;
					absminusoverlap = abssize - queryabsoverlap;
					descminusoverlap = descsize - querydescoverlap;
					claimsminusoverlap = claimssize - queryclaimsoverlap;

					titleunion = querysize + titleminusoverlap;
					absunion = querysize + absminusoverlap;
					descunion = querysize + descminusoverlap;
					claimsunion = querysize + claimsminusoverlap;

					overlapratio = (float)SectionsSumOverlap/querysize;
					
					/*System.out.println(querytitleoverlap + "\t"+ titleunion + "\t"+ (float)querytitleoverlap/titleunion); 
					System.out.println(queryabsoverlap + "\t"+ absunion+ "\t"+ (float)queryabsoverlap/absunion); 
					System.out.println(querydescoverlap + "\t"+ descunion+ "\t"+ (float)querydescoverlap/descunion); 
					System.out.println(queryclaimsoverlap + "\t"+ claimsunion+ "\t"+(float)queryclaimsoverlap/claimsunion); */
					
					uoverlapratio = ((float)querytitleoverlap/titleunion
							+(float)queryabsoverlap/absunion+(float)querydescoverlap/descunion
							+(float)queryclaimsoverlap/claimsunion);
					
					sum = sum + overlapratio;
					usum = usum + uoverlapratio;


					System.out.println(doc + "\t" + querytitleoverlap + "\t" + queryabsoverlap + "\t" + querydescoverlap + "\t" + queryclaimsoverlap + "\t" + querysize + "\t" +titleunion + "\t" +absunion + "\t" +descunion + "\t" + claimsunion+ "\t" + overlapratio + "\t" + uoverlapratio);
				}

				avg = (float)sum/n_enfns;
				uavg = (float)usum/n_enfns;
				System.out.println("-----------------------------------------------------------------------------------------------------------------");
				System.out.println("Average Term Overlap: " +  avg);
				System.out.println("Union Average Term Overlap: " +  uavg);
				System.out.println();
				ps.println(queryid + "\t" + avg + "\t" + uavg);


			}else{
				System.out.println(queryid+"\t" + "No FN for this query");
				ps.println(queryid+"\t"+ "No FN for this query");

			}			
		}			
	}
	
		
	/**
	 * @param reader
	 * @throws IOException
	 * TP patents
	 *  Config(2-a): [O(qD,docT)+O(qD,docA)+O(qD,docD)+O(qD, docC)]/|qD|
	 *  Config(2-b): [overlap(queryDesc,docTitle)/|qDesc U docTitle| + 
	 * overlap(queryDesc,docAbs)/|qDesc U docAbs| +
	 * overlap(queryDesc,docDesc)/|qDesc U docDesc| +
	 * overlap(queryDesc, docClaims)/|qDesc U docClaims|]
	 */
	public void TPsOverlapSeperateSections(CollectionReader reader) throws IOException {
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/TermOverlap/termoverlp-TPs-config(2):seperatesections-test.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*-------------------------------------------------------------------------------*/
		EvaluateResults er = new EvaluateResults();
//		AnalyseFNs afn = new AnalyseFNs();

		TopicsInMemory topics = new TopicsInMemory("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
		for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){

			String queryid = topic.getKey();
			String queryfile = topic.getKey() + "_" + topic.getValue().getUcid() + ".xml";

			ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
//			ArrayList<String> enfns = afn.getEnglishFNs(queryid);
			int n_tps = tps.size();

			QueryGneration query = new QueryGneration(querypath + queryfile, 0, 1, 0, 0, 0, 0, true, true);
			Map<String, Integer> qterms = query.getSectionTerms(queryfield);
			int querysize = qterms.size();

			float sum =0;
			float usum =0;
			float avg = 0;
			float uavg = 0;
			float overlapratio = 0;
			float uoverlapratio = 0;
//			int querydocintersection;
			int querytitleoverlap;
			int queryabsoverlap;
			int querydescoverlap;
			int queryclaimsoverlap;
			int SectionsSumOverlap;
//			int union;
			int titleunion;
			int absunion;
			int descunion;
			int claimsunion;
//			int dminusoverlap = 0;
			int titleminusoverlap;
			int absminusoverlap;
			int descminusoverlap;
			int claimsminusoverlap;
			
			int titlesize;
			int abssize;
			int descsize;
			int claimssize;

			if(n_tps != 0){

				System.out.println(queryid);
				System.out.println("---------------------------------------------------------------------------------------------------------");
				//				System.out.println("FN patent ID" + "\t" + "overlap" + "\t" + "|Q|" + "\t" + "|D|" + "\t" + "D-olap" + "\t" + "|Q U D|" + "\t" + "olap/|Q|" + "\t" + "olap/|Q U D|");
				System.out.println("TP patent ID" + "\t" + "(qD,dT)" + "\t" + "(qD,dA)" + "\t" + "(qD,dD)" + "\t" + "(qD,dC)" + "\t" + "|Q|" + "\t" + "|D U T|" + "\t" + "|D U A|" + "\t" + "|D U D|" + "\t" + "|D U C|"+ "\t" + "|sum/Q|" + " \t" + "sum|O(docSecs)|");
				System.out.println("---------------------------------------------------------------------------------------------------------");
				for (String doc : tps) { 
//					querydocintersection = 0;
					querytitleoverlap = 0;
					queryabsoverlap = 0;
					querydescoverlap = 0;
					queryclaimsoverlap = 0;
					SectionsSumOverlap = 0;
					HashSet<String> titleterms = reader.getDocTerms("UN-"+doc, titlefield);
					HashSet<String> absterms = reader.getDocTerms("UN-"+doc, absfield);
					HashSet<String> descterms = reader.getDocTerms("UN-"+doc, descfield);
					HashSet<String> claimsterms = reader.getDocTerms("UN-"+doc, claimsfield);
					if(titleterms!=null){titlesize = titleterms.size();}else{titlesize=0;}
					if(absterms!=null){abssize = absterms.size();}else{abssize = 0;}
					if(descterms!=null){descsize = descterms.size();}else{descsize = 0;}
					if(claimsterms!=null){claimssize = claimsterms.size();}else{claimssize = 0;}
					
					for(Entry<String, Integer> t : qterms.entrySet()){
						if(titleterms!=null){boolean titleexists = titleterms.contains(t.getKey());
						if(titleexists){
							querytitleoverlap++;}}		
						
						if(absterms!=null){boolean absexists = absterms.contains(t.getKey());
						if(absexists){
							queryabsoverlap++;	}}
						
						if(descterms!=null){boolean descexists = descterms.contains(t.getKey());
						if(descexists){
							querydescoverlap++;	}}
						
						if(claimsterms!=null){boolean claimsexists = claimsterms.contains(t.getKey());
						if(claimsexists){
							queryclaimsoverlap++;	}}			

					}

					SectionsSumOverlap = querytitleoverlap + queryabsoverlap 
							+ querydescoverlap + queryclaimsoverlap;

					titleminusoverlap = titlesize - querytitleoverlap;
					absminusoverlap = abssize - queryabsoverlap;
					descminusoverlap = descsize - querydescoverlap;
					claimsminusoverlap = claimssize - queryclaimsoverlap;

					titleunion = querysize + titleminusoverlap;
					absunion = querysize + absminusoverlap;
					descunion = querysize + descminusoverlap;
					claimsunion = querysize + claimsminusoverlap;

					overlapratio = (float)SectionsSumOverlap/querysize;
					
					/*System.out.println(querytitleoverlap + "\t"+ titleunion + "\t"+ (float)querytitleoverlap/titleunion); 
					System.out.println(queryabsoverlap + "\t"+ absunion+ "\t"+ (float)queryabsoverlap/absunion); 
					System.out.println(querydescoverlap + "\t"+ descunion+ "\t"+ (float)querydescoverlap/descunion); 
					System.out.println(queryclaimsoverlap + "\t"+ claimsunion+ "\t"+(float)queryclaimsoverlap/claimsunion); */
					
					uoverlapratio = ((float)querytitleoverlap/titleunion
							+(float)queryabsoverlap/absunion+(float)querydescoverlap/descunion
							+(float)queryclaimsoverlap/claimsunion);
					
					sum = sum + overlapratio;
					usum = usum + uoverlapratio;


					System.out.println(doc + "\t" + querytitleoverlap + "\t" + queryabsoverlap + "\t" + querydescoverlap + "\t" + queryclaimsoverlap + "\t" + querysize + "\t" +titleunion + "\t" +absunion + "\t" +descunion + "\t" + claimsunion + "\t" + overlapratio + "\t" + uoverlapratio);
				}

				avg = (float)sum/n_tps;
				uavg = (float)usum/n_tps;
				System.out.println("-----------------------------------------------------------------------------------------");
				System.out.println("Average Term Overlap: " +  avg);
				System.out.println("Union Average Term Overlap: " +  uavg);
				System.out.println();
				ps.println(queryid + "\t" + avg + "\t" + uavg);


			}else{
				System.out.println(queryid+"\t" + "No TP for this query");
				ps.println(queryid+"\t"+ "No TP for this query");

			}			
		}			
	}
	public static void main(String[] args) throws IOException {

		CollectionReader reader = new CollectionReader(indexDir);
		TermOverlapConfig2 to1 = new TermOverlapConfig2();
		to1.FNsOverlapSeperateSections(reader);
//		to1.TPsOverlapSeperateSections(reader);		
	}

}
