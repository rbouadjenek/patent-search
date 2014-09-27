package nicta.com.au.failureanalysis.SectionOverlap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;

import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.PatentDocument;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;

public class DescSecOverlapDisplay {
	private static IndexReader ir;
	static String indexDir =  "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";
	//	private final int topK;

	public DescSecOverlapDisplay(String indexDir/*, String similarity, int topK*/)
			throws IOException {
		ir = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
	}
	
	public HashSet<String> LoopOverIndexedDocs() throws IOException{
		/*--------------------------- Write in output file. ------------------------*/
		String outputfile = "./output/SecOverlap/secoverlp.txt";

		FileOutputStream out = new FileOutputStream(outputfile);
		PrintStream ps = new PrintStream(out);
		/*--------------------------------------------------------------------------*/
		
		String titlefield = PatentDocument.Title;
		String absfield = PatentDocument.Abstract;
		String descfield = PatentDocument.Description;
		String claimsfield = PatentDocument.Claims;
		
		
//	-------------------
//	-------------------
		DescSectionsOverlap dsoverlap = new DescSectionsOverlap(indexDir);	
		//		HashSet<String> patents = dsoverlap.LoopOverIndexedDocs();
		
				
		String docName;
		HashSet<String> indexedpataents= new HashSet<>();
		Bits liveDocs = MultiFields.getLiveDocs(ir);
		System.out.println(ir.maxDoc());
		System.out.println("----------------------");
		for (int i=0; i<ir.maxDoc(); i++) {
		    if (liveDocs != null && !liveDocs.get(i))
		        continue;

		    Document doc = ir.document(i);
		    docName = doc.get(PatentDocument.FileName);
//		    dsoverlap.CalculateSecOverlap(docName);
		    
		    int titlexists = 0;
			int absexists = 0;
			int descexists = 0;
			int claimsexists = 0;
			int notexists = 0;
			
			int titlesize;
			int abssize;
			int descsize;
			int claimssize; 
			CollectionReader reader = new CollectionReader(indexDir);
		    
		    HashSet<String> titleterms = reader.getDocTerms(docName, titlefield);
			HashSet<String> absterms = reader.getDocTerms(docName, absfield);
			HashSet<String> descterms = reader.getDocTerms(docName, descfield);
			HashSet<String> claimsterms = reader.getDocTerms(docName, claimsfield);
			
			
			if(titleterms!=null){titlesize = titleterms.size();}else{titlesize=-1;}
			if(absterms!=null){abssize = absterms.size();}else{abssize=-1;}
			if(descterms!=null){descsize = descterms.size();}else{descsize=-1;}
			if(claimsterms!=null){claimssize = claimsterms.size();}else{claimssize=-1;}
			
			System.out.println(titlesize +"\t"+ abssize + "\t" + descsize + "\t" + claimssize);
			if(descterms!=null){
//			System.out.println(descterms);
				if(titleterms!=null){
			for(String t : titleterms){
				
				if(descterms.contains(t)){
					titlexists++;
				}
//				System.out.println(t + "\t"+ descterms.contains(t));
			}}else{absexists = 2;}
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
//				System.out.println(a + "\t"+ descterms.contains(a));
			}}else{claimsexists = 2;}
					
			System.out.println((titlexists + "\t" + absexists + "\t" + claimsexists));
			System.out.println(docName+ "\t"+(float)titlexists/titlesize + "\t" + (float)absexists/abssize + "\t" + (float)claimsexists/claimssize);
			ps.println(docName+ "\t"+(float)titlexists/titlesize + "\t" + (float)absexists/abssize + "\t" + (float)claimsexists/claimssize);
			System.out.println("----------------------------------------------------");
			
			}else{System.out.println(docName + "\t"+"no desc");
			ps.println(docName + "\t"+"no desc");
			System.out.println("-----------------------------------------------------");}
		    
		    
		    
//		    System.out.println(doc.get(PatentDocument.FileName).substring(3));
		    indexedpataents.add(docName);
		    }
		return indexedpataents;
	}
	
	public void CalculateSecOverlap(String docName) throws IOException{
		
		
//		System.out.println(reader.getDocTerms(docName, titlefield));
		
		/*System.out.println(patents.size());*/
		/*for (String p:patents){System.out.println(p);}*/
		
		
	}
	
	public static void main(String[] args) throws IOException {

		String docName = "UN-EP-0802230"; 
		

		DescSecOverlapDisplay dsoverlap = new DescSecOverlapDisplay(indexDir);	
		HashSet<String> patents = dsoverlap.LoopOverIndexedDocs();
//		dsoverlap.CalculateSecOverlap(docName);
				
	}

}
