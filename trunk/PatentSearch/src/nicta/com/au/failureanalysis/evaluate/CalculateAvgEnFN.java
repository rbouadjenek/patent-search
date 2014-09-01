package nicta.com.au.failureanalysis.evaluate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import nicta.com.au.failureanalysis.query.QueryGneration;
import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;

public class CalculateAvgEnFN {

	public static void main(String[] args) throws IOException {

		String path = "data/CLEF-IP-2010/PAC_test/topics/";
		String queryid = "PAC-1379"/*"PAC-1087"*//*"PAC-1035"*//*"PAC-825"*/;
		String queryfile = "PAC-1379_EP-1304229-A2.xml"/*"PAC-1087_EP-1666549-A1.xml"*//*"PAC-1035_EP-1378908-A2.xml"*/ /*"PAC-825_EP-1267369-A2.xml"*/;

		/*String queryid = "PAC-544";
    String queryfile = "PAC-544_EP-1405720-A1.xml"; */

		String outputfilename =  queryid + "TEST_avgfreq_testthendelete.txt"; 
		//	String outputfile = "C:/Users/Mona/workspace/PatentSearch/output/CommonTerms/" + outputfilename;
		String outputfile = "./output/AvgEnFNFreq/" + outputfilename;

		/*------------- Write in outputfile. ----------------*/
		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);		
		/*----------------------------------------------------*/

		String indexDir = "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";
		String field = /* PatentDocument.Classification */PatentDocument.Description;

		QueryGneration query = new QueryGneration(path + queryfile, 0, 1, 0, 0, 0, 0, true, true);
		Map<String, Integer> terms = query.getSectionTerms(/*"title"*//*"abstract"*/"description"/*"claims"*/);

		/*EvaluateResults er = new EvaluateResults();
		ArrayList<String> fns = er.evaluatePatents(queryid, "FN");*/

		AnalyseFNs afn = new AnalyseFNs();
		ArrayList<String> enfns = afn.getEnglishFNs(queryid);
		int n_enfns = enfns.size();

		CollectionReader reader = new CollectionReader(indexDir); 

		for(Entry<String, Integer> t : terms.entrySet()){
			int avg_enfns = 0;
			//            count++;
			System.out.print(/*count + " " + */t.getKey() + "\t" + t.getValue()+ "\t");
			ps.print(/*count + " " + */t.getKey() + "\t" + t.getValue()+ "\t");

			for (String enfn : enfns) { 
				avg_enfns = avg_enfns + reader.getTFreq(field, t.getKey(), enfn);
				
				System.out.print(reader.getTFreq(field, t.getKey(), enfn) + "\t");
				ps.print(reader.getTFreq(field, t.getKey(), enfn) + "\t");
			}

			float avgenfns = (float)avg_enfns/n_enfns ;
			System.out.println(avgenfns + "\t");
			ps.println(avgenfns + "\t");

			/*//          int k =0;
            int avg_fps = 0;
            for (String fp : fps) { 
//                  k++;
//                  if(k<=n_tps){
//                          System.out.println(reader.getTFreq(field, t.getKey(), fp) + "\t");
                    avg_fps = avg_fps + reader.getTFreq(field, t.getKey(), fp);
//                  }

            }
            float avgfps = (float)avg_fps/n_fps;
            float diff = avgtps - avgfps;
            System.out.println( avgfps + "\t" + diff);
            ps.println( avgfps + "\t" + diff);
//          System.out.println((float)avg_fps/n_tps);
			 */            
		}

	}

}
