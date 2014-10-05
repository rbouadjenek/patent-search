package nicta.com.au.failureanalysis.query.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import nicta.com.au.failureanalysis.search.CollectionReader;
import nicta.com.au.failureanalysis.search.CollectionSearcher;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.search.BM25Rocchio;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.DistributionLL;
import org.apache.lucene.search.similarities.IBSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LambdaDF;
import org.apache.lucene.search.similarities.Normalization;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class TestExpalin {
	static String indexDir =  "data/DocPLusQueryINDEX"/*"data/INDEX/indexWithoutSW-Vec-CLEF-IP2010"*/;

	private static IndexReader ir;
	
	private final IndexSearcher is;
	private final int topK;

	private static Similarity getSimilarity(String similarity) {
		if (similarity.toLowerCase().startsWith("bm25ro")) {
			System.err.println("BM25Rocchio");
			return new BM25Rocchio();
		} else if (similarity.toLowerCase().startsWith("tfidf")) {
			// System.err.println("DefaultSimilarity");
			return new DefaultSimilarity();
		} else if (similarity.toLowerCase().startsWith("lmdir")) {
			// System.err.println("LMDirichletSimilarity");
			return new LMDirichletSimilarity();
		} else if (similarity.toLowerCase().startsWith("lmj")) {
			// System.err.println("LMJelinekMercerSimilarity");
			return new LMJelinekMercerSimilarity(1);
		} else if (similarity.toLowerCase().startsWith("ibs")) {
			// System.err.println("IBSimilarity");
			return new IBSimilarity(new DistributionLL(), new LambdaDF(),
					new Normalization.NoNormalization());
		} else if (similarity.toLowerCase().startsWith("bm25ro")) {
			// System.err.println("BM25Similarity");
			return new BM25Similarity();
		} else {
			// System.err.println("BM25Similarity");
			return new BM25Similarity();
		}
	}

	public IndexSearcher getIndexSearch() {
		return is;
	}

	public TestExpalin(String indexDir, String similarity, int topK)
			throws IOException {
		Directory dir = FSDirectory.open(new File(indexDir));
		is = new IndexSearcher(DirectoryReader.open(dir));
		is.setSimilarity(getSimilarity(similarity));
		this.topK = topK;
		
		ir = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
	}

	public void termScoreInDoc(String field, String term, String filename) throws IOException {
		TermQuery query = new TermQuery(new Term(field, term));
//		Term t1 = new Term(PatentDocument.Title, term);
//		Term t2 = new Term(PatentDocument.Abstract, term);
		Term t3 = new Term(PatentDocument.Description, term);
//		Term t4 = new Term(PatentDocument.Claims, term);
		TopDocs topdocs = is.search(query, topK);
		/*System.err.println("'" + term + "'"
				+ " appeared in " + topdocs.totalHits
				+ " documents:");*/
		
//		System.out.println(ir.docFreq(t3));
		
		CollectionReader reader = new CollectionReader(indexDir);
		int docid = reader.getDocId("UN-"+filename, PatentDocument.FileName);
		Explanation explanationtest = is.explain(query, docid);
		System.out.println("Score for '"+term+ "' : "+explanationtest.getValue());
		System.out.println("Document ID : " +docid);
		
/*
		int docid = reader.getDocId("UN-EP-0663270", field);
		System.out.println(docid);*/
		/*DocsEnum de = MultiFields.getTermDocsEnum(ir, MultiFields.getLiveDocs(ir), field, new BytesRef("UN-" + filename));

		while ((de.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
			int docid = de.docID();
			System.out.println(docid);	}		*/
			
			
				
		for (ScoreDoc scoreDoc : topdocs.scoreDocs) {
			Document doc = is.doc(scoreDoc.doc);
			//			System.out.println(scoreDoc.doc + " " + doc.get(PatentDocument.FileName).substring(3) + " " + scoreDoc.score);
			if(doc.get(PatentDocument.FileName).contains(filename)){
				// .substring(3).equals(filename)
				//				termfreq = de.freq();
				System.out.println(doc.get(PatentDocument.FileName) + "\t" + term + "\t" + scoreDoc.score + "\t" + scoreDoc.doc);
				 Explanation explanation = is.explain(query, scoreDoc.doc);
				 
				 float a = explanation.getValue();
				 System.out.println(a);
				 System.out.println(explanation.toString());
			}
			//			doc.get(PatentDocument.FileName).substring(3);
		}		
	}

	/**
	 * @param field
	 * @param term
	 * @return documents that contain "term"
	 * @throws IOException
	 */
	public HashSet<String>/*ArrayList<String>*/ singleTermSearch(String field, String term)
			throws IOException {

		ArrayList<String> returneddocuments = new ArrayList<>();
		HashSet<String> h = new HashSet<>();

		TermQuery query = new TermQuery(new Term(field, term));

		TopDocs topdocs = is.search(query, topK);
		System.err.println("'" + term + "'"
				+ " appeared in " + topdocs.totalHits
				+ " documents:");

		for (ScoreDoc scoreDoc : topdocs.scoreDocs) {
			Document doc = is.doc(scoreDoc.doc);
			//			System.out.println(scoreDoc.doc + " " + doc.get(PatentDocument.FileName).substring(3) + " " + scoreDoc.score);
			returneddocuments.add(doc.get(PatentDocument.FileName).substring(3));
			h.add(doc.get(PatentDocument.FileName).substring(3));
		}
		//		return returneddocuments;
		return h;
	}
	
	public static void main(String[] args) throws IOException {

		String term = "film"/*"print"*//*"G03F"*//*"C04B"*/ /*"G06F"*//*"b32b" *//* "h01l"*/ /*"methyl" */ /*"mona"*/ /*"resin"*/;
		String field = /*PatentDocument.Classification*/ PatentDocument.Description;
		String filename = /*"EP-0663270"*/"EP-0415270";
		String docName = "UN-EP-0663270";

		TestExpalin searcher = new TestExpalin(indexDir, /*"tfidf"*/"lmdir"/*"bm25ro"*/, 100000);
				
//		System.out.println(ir.maxDoc());
		
		/*---------------------Testing SingleTermSEarch method---------------------*/
		searcher.termScoreInDoc(field, term, filename);
		searcher.termScoreInDoc(field, "print", filename);
		/*-------------------------------------------------------------------------*/

		DefaultSimilarity ds	=  new DefaultSimilarity();
//		System.out.println(ds.idf(27, 1331157));
		/*---------------------Testing SingleTermSEarch method---------------------*/

		HashSet<String> returned_docs = searcher.singleTermSearch(field , term.toLowerCase()); 

		int n = 0;
		/*for (String d : returned_docs) {
				n++;
				System.out.println("[" + n + "] " + d);
			}*/
	}

}
