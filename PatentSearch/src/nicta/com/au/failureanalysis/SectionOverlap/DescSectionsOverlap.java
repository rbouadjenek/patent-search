package nicta.com.au.failureanalysis.SectionOverlap;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.patent.document.P;
import nicta.com.au.patent.document.PatentDocument;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;

public class DescSectionsOverlap {
	
	private static IndexReader ir;
	static String indexDir =  "data/INDEX/indexWithoutSW-Vec-CLEF-IP2010";
	//	private final int topK;

	public DescSectionsOverlap(String indexDir/*, String similarity, int topK*/)
			throws IOException {
		ir = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
	}
	
	public HashSet<String> LoopOverIndexedDocs(/*CollectionReader reader*/) throws IOException{
		
		HashSet<String> indexedpataents= new HashSet<>();
		Bits liveDocs = MultiFields.getLiveDocs(ir);
		System.out.println(ir.maxDoc());
		for (int i=0; i<ir.maxDoc(); i++) {
		    if (liveDocs != null && !liveDocs.get(i))
		        continue;

		    Document doc = ir.document(i);
//		    System.out.println(doc.get(PatentDocument.FileName).substring(3));
		    indexedpataents.add(doc.get(PatentDocument.FileName));
		    }
		return indexedpataents;
	}
	
	public static void main(String[] args) throws IOException {

		String docName = "UN-EP-0802230"; 
		String titlefield = PatentDocument.Title;
		String absfield = PatentDocument.Abstract;
		String descfield = PatentDocument.Description;
		String claimsfield = PatentDocument.Claims;

		DescSectionsOverlap dsoverlap = new DescSectionsOverlap(indexDir);	
		//		HashSet<String> patents = dsoverlap.LoopOverIndexedDocs();

		int titlexists = 0;
		int absexists = 0;
		int descexists = 0;
		int claimsexists = 0;
		int notexists = 0;
		CollectionReader reader = new CollectionReader(indexDir);
		System.out.println(reader.getDocTerms(docName, titlefield));
		HashSet<String> titleterms = reader.getDocTerms(docName, titlefield);
		HashSet<String> absterms = reader.getDocTerms(docName, absfield);
		HashSet<String> descterms = reader.getDocTerms(docName, descfield);
		HashSet<String> claimsterms = reader.getDocTerms(docName, claimsfield);
		
		int titlesize = titleterms.size();
		int abssize = absterms.size();
		int descsize = descterms.size();
		int claimssize = claimsterms.size();
		
		System.out.println(descterms);
		for(String t : titleterms){
			if(descterms.contains(t)){
				titlexists++;
			}
//			System.out.println(t + "\t"+ descterms.contains(t));
		}
		for(String a : absterms){
			if(descterms.contains(a)){
				absexists++;
			}
//			System.out.println(a + "\t"+ descterms.contains(a));
		}
		for(String c : claimsterms){
			if(descterms.contains(c)){
				claimsexists++;
			}
//			System.out.println(a + "\t"+ descterms.contains(a));
		}
		System.out.println((float)titlexists/titlesize + "\t" + (float)absexists/abssize + "\t" + (float)claimsexists/claimssize);

		/*System.out.println(patents.size());*/
		/*for (String p:patents){System.out.println(p);}*/
	}
}
