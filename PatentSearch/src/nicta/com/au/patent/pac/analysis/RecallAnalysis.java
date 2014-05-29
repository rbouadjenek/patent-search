package nicta.com.au.patent.pac.analysis;

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
import nicta.com.au.patent.pac.evaluation.QrelsInMemory;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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
public class RecallAnalysis {

    private final TopicsInMemory topics;
    private final QrelsInMemory qrels;
    private final String dataDir;
    private final boolean filter;

    public RecallAnalysis(String topicsFilename, String qrelsFilename, String dataDir, boolean filter) throws IOException {
        this.topics = new TopicsInMemory(topicsFilename);
        this.qrels = new QrelsInMemory(qrelsFilename);
        this.dataDir = dataDir;
        this.filter = filter;
    }

    public void analyze(String field) throws IOException {
        int i = 0;
        for (Map.Entry<String, PatentDocument> e : topics.getTopics().entrySet()) {
            i++;
            double rate = 0;
            String queryId = e.getKey();
            PatentDocument p1 = e.getValue();
            System.err.println("Query: " + queryId);
            for (String pId : qrels.getRelevantPatents(queryId).keySet()) {
                String doc;
                if (pId.startsWith("EP")) {
                    System.err.println("Relevant doc: " + pId);
                    doc = dataDir + "EP/00000" + pId.substring(3, 4) + "/" + pId.substring(4, 6) + "/" + pId.substring(6, 8) + "/" + pId.substring(8, 10) + "/UN-" + pId + ".xml";
                } else {
                    System.err.println("Relevant doc: " + pId);
                    doc = dataDir + "WO/00" + pId.substring(3, 7) + "/" + pId.substring(7, 9) + "/" + pId.substring(9, 11) + "/" + pId.substring(11, 13) + "/UN-" + pId + ".xml";
                }
                PatentDocument p2 = new PatentDocument(doc);
                if (field.equals(PatentDocument.Title)) {
                    System.err.println(PatentDocument.Title);
                    Set<String> v1 = parseTitle(p1);
                    Set<String> v2 = parseTitle(p2);
                    if (sharing(v1, v2)) {
                        rate++;
                    }
                } else if (field.equals(PatentDocument.Classification)) {
                    System.err.println(PatentDocument.Classification);
                    Set<String> v1 = parseClassification(p1);
                    Set<String> v2 = parseClassification(p2);
                    if (sharing(v1, v2)) {
                        rate++;
                    }
                } else if (field.equals(PatentDocument.Abstract)) {
                    System.err.println(PatentDocument.Abstract);
                    Set<String> v1 = parseAbstract(p1);
                    Set<String> v2 = parseAbstract(p2);
                    if (sharing(v1, v2)) {
                        rate++;
                    }
                } else if (field.equals(PatentDocument.Description)) {
                    System.err.println(PatentDocument.Description);
                    Set<String> v1 = parseDescription(p1);
                    Set<String> v2 = parseDescription(p2);
                    if (sharing(v1, v2)) {
                        rate++;
                    }
                } else if (field.equals(PatentDocument.Claims)) {
                    System.err.println(PatentDocument.Claims);
                    Set<String> v1 = parseClaims(p1);
                    Set<String> v2 = parseClaims(p2);
                    if (sharing(v1, v2)) {
                        rate++;
                    }
                } else if (field.equals("descriptionclaims")) {
                    System.err.println("descriptionclaims");
                    if (sharing(parseClaims(p1), parseClaims(p2)) || sharing(parseDescription(p1), parseDescription(p2))) {
                        rate++;
                    }
                } else if (field.equals("all")) {
                    System.err.println("all");
                    if (sharing(parseTitle(p1), parseTitle(p2))) {
                        rate++;
                    } else if (sharing(parseAbstract(p1), parseAbstract(p2))) {
                        rate++;
                    } else if (sharing(parseClaims(p1), parseClaims(p2))) {
                        rate++;
                    } else if (sharing(parseDescription(p1), parseDescription(p2))) {
                        rate++;
                    }
                }

            }
            System.out.println(e.getKey() + "\t" + p1.getUcid() + "\t" + rate / qrels.getRelevantPatents(queryId).size());
        }
    }

    public final Set<String> parseClassification(PatentDocument pt) throws IOException {
        Set<String> out = new HashSet<>();
        for (ClassificationIpcr ipcCode : pt.getTechnicalData().getClassificationIpcr()) {
            StringTokenizer st = new StringTokenizer(ipcCode.getContent());
            out.add(st.nextToken());
        }
        return out;
    }

    public final Set<String> parseTitle(PatentDocument pt) throws IOException {
        String title = "";
        Set<String> out = new HashSet<>();
        for (InventionTitle inventionTitle : pt.getTechnicalData().getInventionTitle()) {
            if (inventionTitle.getLang().toLowerCase().equals("en")) {
                title = inventionTitle.getContent();
            }
        }
        Analyzer analyzer;
        if (filter) {
            analyzer = new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.TITLE_ENGLISH_STOP_WORDS_SET);
        } else {
            analyzer = new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.ENGLISH_STOP_WORDS_SET);
        }
        return transformation(analyzer.tokenStream(PatentDocument.Title, title));
    }

    public final Set<String> parseAbstract(PatentDocument pt) throws IOException {
        String abstrac = "";
        if (pt.getAbstrac().getLang() != null && pt.getAbstrac().getLang().toLowerCase().equals("en")) {
            abstrac = pt.getAbstrac().getContent();
        }
        Analyzer analyzer;
        if (filter) {
            analyzer = new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.ABSTRACT_ENGLISH_STOP_WORDS_SET);
        } else {
            analyzer = new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.ENGLISH_STOP_WORDS_SET);
        }
        return transformation(analyzer.tokenStream(PatentDocument.Abstract, abstrac));
    }

    public final Set<String> parseDescription(PatentDocument pt) throws IOException {
        String description = "";
        if (pt.getDescription() != null && pt.getDescription().getLang().toLowerCase().equals("en")) {
            for (P p : pt.getDescription().getP()) {
                description += p.getContent() + " ";
            }
        }
        Analyzer analyzer;
        if (filter) {
            analyzer = new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.DESCRIPTION_ENGLISH_STOP_WORDS_SET);
        } else {
            analyzer = new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.ENGLISH_STOP_WORDS_SET);
        }
        return transformation(analyzer.tokenStream(PatentDocument.Description, description));
    }

    public final Set<String> parseClaims(PatentDocument pt) throws IOException {
        String claims = "";
        for (Claims cs : pt.getClaims()) {
            if (cs.getLang().toLowerCase().equals("en")) {
                for (Claim claim : cs.getClaim()) {
                    claims += claim.getClaimText() + " ";
                }
            }
        }
        Analyzer analyzer;
        if (filter) {
            analyzer = new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.CLAIMS_ENGLISH_STOP_WORDS_SET);
        } else {
            analyzer = new EnglishAnalyzer(Version.LUCENE_44, PatentsStopWords.ENGLISH_STOP_WORDS_SET);
        }
        return transformation(analyzer.tokenStream(PatentDocument.Claims, claims));
    }

    private Set<String> transformation(TokenStream ts) throws IOException {
        Set<String> out = new HashSet<>();
        CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            String term = charTermAttribute.toString();
            out.add(term);
        }
        return out;
    }

    public boolean sharing(Set<String> v1, Set<String> v2) {
        for (String key : v1) {
            if (v2.contains(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param args the cooutoutand line arguoutents
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String topicsFilename;
        String qrels;
        String dataDir;
        String field;
        boolean specificStopWords;
        if (args.length == 0) {
            topicsFilename = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2011/PAC_topics.txt";
            qrels = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2011/PAC_topics/clef-ip-2011-PAC_qrels_New.txt";
            dataDir = "/Volumes/TOSHIBA EXT/CLEF-IP/";
            field = "title";
            specificStopWords = true;
        } else {
            topicsFilename = args[0];
            qrels = args[1];
            dataDir = args[2];
            field = args[3];
            specificStopWords = Boolean.valueOf(args[4]);
        }
        try {
            RecallAnalysis sim = new RecallAnalysis(topicsFilename, qrels, dataDir, specificStopWords);
            sim.analyze(field);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
