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

public class CalculateAverage {
        
        public static void main(String[] args) throws IOException {
                
                String path = "data/CLEF-IP-2010/PAC_test/topics/";
                String queryid = "PAC-825";
                String queryfile = "PAC-825_EP-1267369-A2.xml";
                
                /*String queryid = "PAC-544";
                String queryfile = "PAC-544_EP-1405720-A1.xml"; */
                
                String outputfilename =  queryid + "_avgfreq_testthendelete.txt"; 
//        		String outputfile = "C:/Users/Mona/workspace/PatentSearch/output/CommonTerms/" + outputfilename;
        		String outputfile = "./output/AverageFrequency/" + outputfilename;

        		/*------------- Write in outputfile. ----------------*/
        		FileOutputStream out = new FileOutputStream(outputfile);
        		PrintStream ps = new PrintStream(out);		
        		/*----------------------------------------------------*/
        		
                String indexDir = "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";
                String field = /* PatentDocument.Classification */PatentDocument.Description;

                QueryGneration query = new QueryGneration(path + queryfile, 0, 1, 0, 0, 0, 0, true, true);
                Map<String, Integer> terms = query.getSectionTerms(/*"title"*//*"abstract"*/"description"/*"claims"*/);

                EvaluateResults er = new EvaluateResults();
                ArrayList<String> tps = er.evaluatePatents(queryid, "TP");
                ArrayList<String> fps = er.evaluatePatents(queryid, "FP");
                ArrayList<String> fns = er.evaluatePatents(queryid, "FN");

                int n_tps = tps.size();
                int n_fps = fps.size();
                int n_fns = fns.size();
                
                CollectionReader reader = new CollectionReader(indexDir);       
                
//              System.out.println(terms.size());
//        int count = 0;
        for(Entry<String, Integer> t : terms.entrySet()){
                int avg_tps = 0;
//                count++;
                System.out.print(/*count + " " + */t.getKey() + "\t" + t.getValue()+ "\t");
                ps.print(/*count + " " + */t.getKey() + "\t" + t.getValue()+ "\t");
                
                for (String tp : tps) { 
                	avg_tps = avg_tps + reader.getTFreq(field, t.getKey(), tp);
                }
                
                float avgtps = (float)avg_tps/n_tps ;
                System.out.print(avgtps + "\t");
                ps.print(avgtps + "\t");
                
//              int k =0;
                int avg_fps = 0;
                for (String fp : fps) { 
//                      k++;
//                      if(k<=n_tps){
//                              System.out.println(reader.getTFreq(field, t.getKey(), fp) + "\t");
                        avg_fps = avg_fps + reader.getTFreq(field, t.getKey(), fp);
//                      }
                        
                }
                float avgfps = (float)avg_fps/n_fps;
                float diff = avgtps - avgfps;
                System.out.println( avgfps + "\t" + diff);
                ps.println( avgfps + "\t" + diff);
//              System.out.println((float)avg_fps/n_tps);
                
        }
        
         for (String tp : tps) { 
                                  
                          System.out.print("[" + tp + "], "); 
                          
                                        
                                
//                              int b = reader.getTFreq(field, term, filename);
//                              System.out.println(b);
                          }     
        
        }       

}