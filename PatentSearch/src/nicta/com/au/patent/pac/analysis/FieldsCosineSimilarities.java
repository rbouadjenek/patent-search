package nicta.com.au.patent.pac.analysis;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
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
public class FieldsCosineSimilarities {

    private final Map<String, Double>[] pt1FieldsVectors;
    private final Map<String, Double>[] pt2FieldsVectors;
    private final boolean specificStopWords;
    final private IndexReader ir;
    private final double[] fieldsCosineSimilarity = new double[5];

    public FieldsCosineSimilarities(String PatentApplication, String GrantedPatend, String indexDir, boolean specificStopWords) throws IOException, Exception {
        this.specificStopWords = specificStopWords;
        Directory dir = FSDirectory.open(new File(indexDir));
        ir = DirectoryReader.open(dir);
        PatentDocument pt1 = new PatentDocument(PatentApplication);
        PatentDocument pt2 = new PatentDocument(GrantedPatend);
        pt1FieldsVectors = this.parse(pt1);
        pt2FieldsVectors = this.parse(pt2);

    }

    public FieldsCosineSimilarities(PatentDocument PatentApplication, String GrantedPatend, IndexReader ir, boolean specificStopWords) throws IOException, Exception {
        this.specificStopWords = specificStopWords;
        this.ir = ir;
        PatentDocument pt1 = PatentApplication;
        PatentDocument pt2 = new PatentDocument(GrantedPatend);
        pt1FieldsVectors = this.parse(pt1);
        pt2FieldsVectors = this.parse(pt2);

    }

    public final Map<String, Double>[] parse(PatentDocument pt) throws IOException, Exception {
        Map<String, Double>[] out = new Map[5];
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
        Map<String, Double> m1 = new HashMap<>();
        for (ClassificationIpcr ipcCode : pt.getTechnicalData().getClassificationIpcr()) {
            StringTokenizer st = new StringTokenizer(ipcCode.getContent());
            m1.put(st.nextToken(), 1.0);
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
            analyzerPerField.put(PatentDocument.Title, new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.TITLE_ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Abstract, new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.ABSTRACT_ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Description, new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.DESCRIPTION_ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Claims, new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.CLAIMS_ENGLISH_STOP_WORDS_SET));
        } else {
            analyzerPerField.put(PatentDocument.Title, new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Abstract, new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Description, new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.ENGLISH_STOP_WORDS_SET));
            analyzerPerField.put(PatentDocument.Claims, new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.ENGLISH_STOP_WORDS_SET));

        }

        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_44), analyzerPerField);
        Map<String, Double> m0 = getVector(analyzer.tokenStream(PatentDocument.Title, ptFields[0]), PatentDocument.Title);
        Map<String, Double> m2 = getVector(analyzer.tokenStream(PatentDocument.Abstract, ptFields[2]), PatentDocument.Abstract);
        Map<String, Double> m3 = getVector(analyzer.tokenStream(PatentDocument.Description, ptFields[3]), PatentDocument.Description);
        Map<String, Double> m4 = getVector(analyzer.tokenStream(PatentDocument.Claims, ptFields[4]), PatentDocument.Claims);
        out[0] = m0;
        out[1] = m1;
        out[2] = m2;
        out[3] = m3;
        out[4] = m4;
        return out;
    }

    private Map<String, Double> getVector(TokenStream ts, String field) throws IOException, Exception {
        Map<String, Double> m = new HashMap<>();
        CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        int i = 0;
        while (ts.incrementToken()) {
            i++;
            String term = charTermAttribute.toString();
            if (m.containsKey(term)) {
                m.put(term, m.get(term) + 1);
            } else {
                m.put(term, 1.0);
            }
        }
        for (String key : m.keySet()) {
            Term t = new Term(field, key);
            int totalTF = ir.docFreq(t);
            int docs = ir.getDocCount("claims");
            double idf = Math.log10((double) docs / (totalTF + 1));
            m.put(key, (m.get(key) / i) * idf);
        }

        return m;
    }

    public double getIdf(String term) {

        return 0;
    }

    public void computeCosineSimilarities() {
        fieldsCosineSimilarity[0] = this.cosine(pt1FieldsVectors[0], pt2FieldsVectors[0]);
        fieldsCosineSimilarity[1] = this.cosine(pt1FieldsVectors[1], pt2FieldsVectors[1]);
        fieldsCosineSimilarity[2] = this.cosine(pt1FieldsVectors[2], pt2FieldsVectors[2]);
        fieldsCosineSimilarity[3] = this.cosine(pt1FieldsVectors[3], pt2FieldsVectors[3]);
        fieldsCosineSimilarity[4] = this.cosine(pt1FieldsVectors[4], pt2FieldsVectors[4]);
    }

    public double cosine(Map<String, Double> v1, Map<String, Double> v2) {
        if (v1.isEmpty() || v2.isEmpty()) {
            return 0;
        }
        double sim = 0;
        for (String term : v1.keySet()) {
            if (v2.containsKey(term)) {
                sim = sim + v2.get(term) * v1.get(term);
            }
        }
        double r = 0;
        for (Double d : v2.values()) {
            r = r + Math.pow(d, 2);
        }
        r = Math.sqrt(r);
        double q = 0;
        for (Double d : v1.values()) {
            q = q + Math.pow(d, 2);
        }
        q = Math.sqrt(q);
        if (q == 0 || r == 0) {
            return 0;
        }
        sim = sim / (q * r);
        return sim;
    }

    public double[] getFieldsCosineSimilarity() {
        return fieldsCosineSimilarity;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String q;
        String doc;
        String indexDir;
        if (args.length == 0) {
            q = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/query/PAC-132_EP-1550834-A1.xml";
            doc = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/docs/UN-EP-1070700.xml";
            indexDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/index/";
        } else {
            q = args[0];
            doc = args[1];
            indexDir = args[2];
        }
        try {
            FieldsCosineSimilarities sim = new FieldsCosineSimilarities(q, doc, indexDir, true);
            sim.computeCosineSimilarities();
            System.out.println("Title: " + sim.getFieldsCosineSimilarity()[0]);
            System.out.println("Classification: " + sim.getFieldsCosineSimilarity()[1]);
            System.out.println("Abstract: " + sim.getFieldsCosineSimilarity()[2]);
            System.out.println("Description: " + sim.getFieldsCosineSimilarity()[3]);
            System.out.println("Claims: " + sim.getFieldsCosineSimilarity()[4]);

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            Logger.getLogger(FieldsCosineSimilarities.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
