package nicta.com.au.patent.pac.analysis;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import nicta.com.au.patent.document.Claim;
import nicta.com.au.patent.document.Claims;
import nicta.com.au.patent.document.ClassificationIpcr;
import nicta.com.au.patent.document.InventionTitle;
import nicta.com.au.patent.document.P;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.document.PatentsStopWords;
import nicta.com.au.patent.pac.index.TermFreqVector;
import nicta.com.au.patent.pac.search.PatentQuery;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rbouadjenek
 */
public final class FieldsJaccardSimilarities {

    private final Map<String, Integer>[] ptAppicationFieldsVectors;
    private final Map<String, Integer> pt2FieldsVectors = new HashMap<>();
    private final boolean specificStopWords;
    private final IndexSearcher is;
    private final double[] fieldsJaccardSimilarity = new double[5];

    public FieldsJaccardSimilarities(String indexDir, String PatentApplication, String GrantedPatent, boolean specificStopWords) throws IOException, ParseException {
        Directory dir = FSDirectory.open(new File(indexDir));
        is = new IndexSearcher(DirectoryReader.open(dir));
        this.specificStopWords = specificStopWords;
        PatentDocument pt1 = new PatentDocument(PatentApplication);
        ptAppicationFieldsVectors = this.parse(pt1);
        initialize(GrantedPatent);
    }

    public FieldsJaccardSimilarities(IndexSearcher is, PatentDocument PatentApplication, String GrantedPatent, boolean specificStopWords) throws IOException {
        this.is = is;
        this.specificStopWords = specificStopWords;
        PatentDocument pt1 = PatentApplication;
        ptAppicationFieldsVectors = this.parse(pt1);
        initialize(GrantedPatent);
    }

    public void initialize(String GrantedPatent) throws IOException {
        Query query = new TermQuery(new Term(PatentDocument.FileName, GrantedPatent));
        TopDocs hits = is.search(query, 1);
        Set<TermFreqVector> docsTerms = new HashSet<>();
        for (ScoreDoc scoreDoc : hits.scoreDocs) {

//            Document doc = is.doc(scoreDoc.doc);
//            System.out.println(doc.get(PatentDocument.FileName).substring(3)+"\t" + scoreDoc.score);
            Terms termTitle = is.getIndexReader().getTermVector(scoreDoc.doc, PatentQuery.getFields()[1]);
            TermFreqVector docTermsTitle = new TermFreqVector(termTitle);
            docsTerms.add(docTermsTitle);

            Terms termAbstract = is.getIndexReader().getTermVector(scoreDoc.doc, PatentQuery.getFields()[2]);
            TermFreqVector docTermsAbstract = new TermFreqVector(termAbstract);
            docsTerms.add(docTermsAbstract);

            Terms termDescription = is.getIndexReader().getTermVector(scoreDoc.doc, PatentQuery.getFields()[3]);
            TermFreqVector docTermsDescription = new TermFreqVector(termDescription);
            docsTerms.add(docTermsDescription);

            Terms termClaims = is.getIndexReader().getTermVector(scoreDoc.doc, PatentQuery.getFields()[5]);
            TermFreqVector docTermsClaims = new TermFreqVector(termClaims);
            docsTerms.add(docTermsClaims);
        }
        for (TermFreqVector tfv : docsTerms) {
            for (String term : tfv.getTerms()) {
                if (!pt2FieldsVectors.containsKey(term)) {
                    pt2FieldsVectors.put(term, tfv.getFreq(term));
                } else {
                    int v = pt2FieldsVectors.get(term);
                    pt2FieldsVectors.put(term, tfv.getFreq(term) + v);
                }
            }
        }
    }

    public final Map<String, Integer>[] parse(PatentDocument pt) throws IOException {
        Map<String, Integer>[] out = new Map[5];
        String[] ptFields = new String[5];
        String title = "";
        String ipc = "";
        String abstrac = "";
        String description = "";
        String claims = "";
        for (InventionTitle inventionTitle : pt.getTechnicalData().getInventionTitle()) {
            if (inventionTitle.getLang().toLowerCase().equals("en")) {
                title = inventionTitle.getContent();
            }
        }
        Map<String, Integer> m1 = new HashMap<>();
        for (ClassificationIpcr ipcCode : pt.getTechnicalData().getClassificationIpcr()) {
            StringTokenizer st = new StringTokenizer(ipcCode.getContent());
            m1.put(st.nextToken(), 1);
        }

        if (pt.getAbstrac().getLang() != null && pt.getAbstrac().getLang().toLowerCase().equals("en")) {
            abstrac = pt.getAbstrac().getContent();
        }
        if (pt.getDescription() != null && pt.getDescription().getLang().toLowerCase().equals("en")) {
            for (P p : pt.getDescription().getP()) {
                description += p.getContent() + " ";
            }
        }
        for (Claims cs : pt.getClaims()) {
            if (cs.getLang().toLowerCase().equals("en")) {
                for (Claim claim : cs.getClaim()) {
                    claims += claim.getClaimText() + " ";
                }
            }
        }
        ptFields[0] = title;
        ptFields[1] = ipc;
        ptFields[2] = abstrac;
        ptFields[3] = description;
        ptFields[4] = claims;
        Map<String, Analyzer> analyzerPerField = new HashMap<>();
        if (specificStopWords == true) {
            analyzerPerField.put(PatentDocument.Title, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.TITLE_ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Abstract, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ABSTRACT_ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Description, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.DESCRIPTION_ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Claims, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.CLAIMS_ENGLISH_STOP_WORDS_SET));
        } else {
            analyzerPerField.put(PatentDocument.Title, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Abstract, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Description, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Claims, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ENGLISH_STOP_WORDS_SET));

        }
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_48), analyzerPerField);
        Map<String, Integer> m0 = transformation(analyzer.tokenStream(PatentDocument.Title, ptFields[0]));
        Map<String, Integer> m2 = transformation(analyzer.tokenStream(PatentDocument.Abstract, ptFields[2]));
        Map<String, Integer> m3 = transformation(analyzer.tokenStream(PatentDocument.Description, ptFields[3]));
        Map<String, Integer> m4 = transformation(analyzer.tokenStream(PatentDocument.Claims, ptFields[4]));
        out[0] = m0;
        out[1] = m1;
        out[2] = m2;
        out[3] = m3;
        out[4] = m4;
        return out;
    }

    private Map<String, Integer> transformation(TokenStream ts) throws IOException {
        Map<String, Integer> m = new HashMap<>();
        CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            String term = charTermAttribute.toString();
            if (m.containsKey(term)) {
                m.put(term, m.get(term) + 1);
            } else {
                m.put(term, 1);
            }
        }
        return m;
    }

    public void computeJaccardSimilarities() {
        fieldsJaccardSimilarity[0] = this.jaccard(ptAppicationFieldsVectors[0], pt2FieldsVectors);
        fieldsJaccardSimilarity[1] = this.jaccard(ptAppicationFieldsVectors[1], pt2FieldsVectors);
        fieldsJaccardSimilarity[2] = this.jaccard(ptAppicationFieldsVectors[2], pt2FieldsVectors);
        fieldsJaccardSimilarity[3] = this.jaccard(ptAppicationFieldsVectors[3], pt2FieldsVectors);
        fieldsJaccardSimilarity[4] = this.jaccard(ptAppicationFieldsVectors[4], pt2FieldsVectors);
    }

    public double jaccard(Map<String, Integer> v1, Map<String, Integer> v2) {
        if (v1.isEmpty() || v2.isEmpty()) {
            return 0;
        }
        double val1 = 0, val2 = 0;
        for (String key : v1.keySet()) {
            if (v2.containsKey(key)) {
                val1 += v1.get(key) + v2.get(key);
            }
            val2 += v1.get(key);
        }
        for (String key : v2.keySet()) {
            val2 += v2.get(key);
        }
//        System.out.println("val1/val2= " + val1 + "/" + val2 + "= " + val1 / val2);
        return val1 / val2;
    }

    public double[] getFieldsJaccardSimilarity() {
        return fieldsJaccardSimilarity;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ParseException {
        // TODO code application logic here
        String q = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/topics/PAC-1269_EP-1525886-A1.xml";
        String doc = "UN-EP-1215817";
        String indexDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/indexWithoutSW-Vec-CLEF-IP2010-2.0/";

//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/rel/UN-EP-0595715.xml";//rel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/rel/UN-EP-0595716.xml";//rel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/rel/UN-EP-0620006.xml";//rel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/rel/UN-EP-0688566.xml";//rel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/rel/UN-EP-0688567.xml";//rel
//             doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/rel/UN-EP-0688568.xml";//rel
//              doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/rel/UN-EP-0711561.xml";//rel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/irrel/UN-EP-0212870.xml";//irrel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/irrel/UN-EP-0306236.xml";//irrel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/irrel/UN-EP-0313380.xml";//irrel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/irrel/UN-EP-0369741.xml";//irrel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/irrel/UN-EP-0494996.xml";//irrel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/irrel/UN-EP-0533799.xml";//irrel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/irrel/UN-EP-0764047.xml";//irrel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/irrel/UN-EP-0803513.xml";//irrel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/irrel/UN-EP-0914862.xml";//irrel
//            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Desktop/irrel/UN-EP-1227782.xml";//irrel
        try {
            FieldsJaccardSimilarities sim = new FieldsJaccardSimilarities(indexDir, q, doc, true);
            sim.computeJaccardSimilarities();
            System.out.println("Title: " + sim.getFieldsJaccardSimilarity()[0]);
            System.out.println("Classification: " + sim.getFieldsJaccardSimilarity()[1]);
            System.out.println("Abstract: " + sim.getFieldsJaccardSimilarity()[2]);
            System.out.println("Description: " + sim.getFieldsJaccardSimilarity()[3]);
            System.out.println("Claims: " + sim.getFieldsJaccardSimilarity()[4]);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
