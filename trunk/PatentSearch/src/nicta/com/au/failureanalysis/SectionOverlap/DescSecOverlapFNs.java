package nicta.com.au.failureanalysis.SectionOverlap;

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
public class DescSecOverlapFNs {
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
	public void FNsDesSecOverlap(CollectionReader reader) throws IOException {
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/SecOverlap/desc-sec-overlp.txt";

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

			/*float sum =0;
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
			int claimssize;*/

			
			if(n_enfns != 0){
				System.out.println("-----------------------------------------------------------------------------------------------------------------");
				System.out.println(queryid);
				System.out.println("-----------------------------------------------------------------------------------------------------------------");
				//				System.out.println("FN patent ID" + "\t" + "overlap" + "\t" + "|Q|" + "\t" + "|D|" + "\t" + "D-olap" + "\t" + "|Q U D|" + "\t" + "olap/|Q|" + "\t" + "olap/|Q U D|");
				/*System.out.println("FN patent ID" + "\t" + "(qD,dT)" + "\t" + "(qD,dA)" + "\t" + "(qD,dD)" + "\t" + "(qD,dC)" + "\t" + "|Q|" + "\t" + "|D U T|" + "\t" + "|D U A|" + "\t" + "|D U D|" + "\t" + "|D U C|"+ "\t" + "|sum/Q|" + " \t" + "sum|O(docSecs)|");*/
//				System.out.println("-----------------------------------------------------------------------------------------------------------------");
				for (String doc : enfns) { 
					int titlexists = 0;
					int absexists = 0;
					int descexists = 0;
					int claimsexists = 0;
					int notexists = 0;
					
					int titlesize;
					int abssize;
					int descsize;
					int claimssize; 
					/*querytitleoverlap = 0;
					queryabsoverlap = 0;
					querydescoverlap = 0;
					queryclaimsoverlap = 0;
					SectionsSumOverlap = 0;*/
					HashSet<String> titleterms = reader.getDocTerms("UN-"+doc, titlefield);
					HashSet<String> absterms = reader.getDocTerms("UN-"+doc, absfield);
					HashSet<String> descterms = reader.getDocTerms("UN-"+doc, descfield);
					HashSet<String> claimsterms = reader.getDocTerms("UN-"+doc, claimsfield);
					
					if(titleterms!=null){titlesize = titleterms.size();}else{titlesize=-1;}
					if(absterms!=null){abssize = absterms.size();}else{abssize = -1;}
					if(descterms!=null){descsize = descterms.size();}else{descsize = -1;}
					if(claimsterms!=null){claimssize = claimsterms.size();}else{claimssize = -1;}

					if(descterms!=null){
//						System.out.println(descterms);
						if(titleterms!=null){
							for(String t : titleterms){

								if(descterms.contains(t)){
									titlexists++;
								}
								//							System.out.println(t + "\t"+ descterms.contains(t));
							}}else{titlexists = 2;}
						
						if(absterms!=null){
							for(String a : absterms){
								if(descterms.contains(a)){
									absexists++;
								}
								//			System.out.println(a + "\t"+ descterms.contains(a));
							}}else{absexists = 2;}
						
						if(claimsterms!=null){
							for(String c : claimsterms){
								if(descterms.contains(c)){
									claimsexists++;
								}
								//							System.out.println(a + "\t"+ descterms.contains(a));
							}}else{claimsexists = 2;}
								
//						System.out.println((titlexists + "\t" + absexists + "\t" + claimsexists));
						System.out.println(doc+ "\t"+(float)titlexists/titlesize + "\t" + (float)absexists/abssize + "\t" + (float)claimsexists/claimssize);
						ps.println(doc+ "\t"+(float)titlexists/titlesize + "\t" + (float)absexists/abssize + "\t" + (float)claimsexists/claimssize);
						/*System.out.println("----------------------------------------------------");*/
						
						}else{
							System.out.println(doc + "\t"+"no desc");
							ps.println(doc + "\t"+"no desc");
							/*System.out.println("-----------------------------------------------------");*/
						}
					
					
					
					
					
					/*
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

					}*/

					/*SectionsSumOverlap = querytitleoverlap + queryabsoverlap 
							+ querydescoverlap + queryclaimsoverlap;

					titleminusoverlap = titlesize - querytitleoverlap;
					absminusoverlap = abssize - queryabsoverlap;
					descminusoverlap = descsize - querydescoverlap;
					claimsminusoverlap = claimssize - queryclaimsoverlap;

					titleunion = querysize + titleminusoverlap;
					absunion = querysize + absminusoverlap;
					descunion = querysize + descminusoverlap;
					claimsunion = querysize + claimsminusoverlap;

					overlapratio = (float)SectionsSumOverlap/querysize;*/

					/*System.out.println(querytitleoverlap + "\t"+ titleunion + "\t"+ (float)querytitleoverlap/titleunion); 
					System.out.println(queryabsoverlap + "\t"+ absunion+ "\t"+ (float)queryabsoverlap/absunion); 
					System.out.println(querydescoverlap + "\t"+ descunion+ "\t"+ (float)querydescoverlap/descunion); 
					System.out.println(queryclaimsoverlap + "\t"+ claimsunion+ "\t"+(float)queryclaimsoverlap/claimsunion); */

					/*uoverlapratio = ((float)querytitleoverlap/titleunion
							+(float)queryabsoverlap/absunion+(float)querydescoverlap/descunion
							+(float)queryclaimsoverlap/claimsunion);

					sum = sum + overlapratio;
					usum = usum + uoverlapratio;*/


					/*System.out.println(doc + "\t" + querytitleoverlap + "\t" + 
					queryabsoverlap + "\t" + querydescoverlap + "\t" + queryclaimsoverlap + "\t" + 
					querysize + "\t" +titleunion + "\t" +absunion + "\t" +descunion + "\t" + 
					claimsunion+ "\t" + overlapratio + "\t" + uoverlapratio);*/
				}

				/*avg = (float)sum/n_enfns;
				uavg = (float)usum/n_enfns;*/
				/*System.out.println("-----------------------------------------------------------------------------------------------------------------");
				System.out.println("Average Term Overlap: " +  avg);
				System.out.println("Union Average Term Overlap: " +  uavg);
				System.out.println();
				ps.println(queryid + "\t" + avg + "\t" + uavg);
*/

			}else{
				/*System.out.println(queryid+"\t" + "No FN for this query");
				ps.println(queryid+"\t"+ "No FN for this query");*/

			}			
		}			
	}

	
	public static void main(String[] args) throws IOException {

		CollectionReader reader = new CollectionReader(indexDir);
		DescSecOverlapFNs to1 = new DescSecOverlapFNs();
		//		to1.FNsOverlapSeperateSections(reader);
		//		to1.TPsOverlapSeperateSections(reader);		
		to1.FNsDesSecOverlap(reader);
	}

}
