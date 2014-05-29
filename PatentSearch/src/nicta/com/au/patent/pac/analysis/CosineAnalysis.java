/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template doc, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.analysis;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.QrelsInMemory;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author rbouadjenek
 */
public class CosineAnalysis {

    private final File queriesFilename;
    private final TopicsInMemory topics;
    private final QrelsInMemory qrels;
    private final String dataDir;
    final private IndexReader ir;
    private double[] average;
    private final boolean specificStopWords;

    public CosineAnalysis(String queriesFilename, String topicsFilename, String qrelsFilename, String dataDir, String indexDir, boolean specificStopWords) throws IOException {
        this.queriesFilename = new File(queriesFilename);
        this.topics = new TopicsInMemory(topicsFilename);
        this.qrels = new QrelsInMemory(qrelsFilename);
        this.dataDir = dataDir;
        Directory dir = FSDirectory.open(new File(indexDir));
        ir = DirectoryReader.open(dir);
        this.specificStopWords = specificStopWords;
        System.out.println("*******************");
        System.out.println("Cosine");
        System.out.println("File name: " + this.queriesFilename.getName());
        System.out.println("qrels File name: " + new File(qrelsFilename).getName());
        System.out.println("Specific Stop Words: " + specificStopWords);
        System.out.println("*******************");
    }

    public void analyze() throws Exception {
        average = new double[5];
        average[0] = 0;
        average[1] = 0;
        average[2] = 0;
        average[3] = 0;
        average[4] = 0;
        try {
            FileInputStream fstream = new FileInputStream(queriesFilename);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                int i = 0;
                while ((str = br.readLine()) != null) {
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
                    i++;
                    StringTokenizer st = new StringTokenizer(str);
                    String queryNum = st.nextToken();
                    String queryId = st.nextToken();
                    double[] average2 = this.queryCosine(queryId, topics.getTopics().get(queryId));
                    average[0] += average2[0];
                    average[1] += average2[1];
                    average[2] += average2[2];
                    average[3] += average2[3];
                    average[4] += average2[4];
                }
                average[0] /= i;
                average[1] /= i;
                average[2] /= i;
                average[3] /= i;
                average[4] /= i;
            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public double[] queryCosine(String queryId, PatentDocument query) throws IOException, Exception {
        double[] average2 = new double[5];
        average2[0] = 0;
        average2[1] = 0;
        average2[2] = 0;
        average2[3] = 0;
        average2[4] = 0;
        for (String p : qrels.getRelevantPatents(queryId).keySet()) {
            String doc;
            if (p.startsWith("EP")) {
                doc = dataDir + "EP/00000" + p.substring(3, 4) + "/" + p.substring(4, 6) + "/" + p.substring(6, 8) + "/" + p.substring(8, 10) + "/UN-" + p + ".xml";
            } else {
                doc = dataDir + "WO/00" + p.substring(3, 7) + "/" + p.substring(7, 9) + "/" + p.substring(9, 11) + "/" + p.substring(11, 13) + "/UN-" + p + ".xml";
            }
            FieldsCosineSimilarities sim = new FieldsCosineSimilarities(query, doc, ir, specificStopWords);
            sim.computeCosineSimilarities();
            average2[0] += sim.getFieldsCosineSimilarity()[0];
            average2[1] += sim.getFieldsCosineSimilarity()[1];
            average2[2] += sim.getFieldsCosineSimilarity()[2];
            average2[3] += sim.getFieldsCosineSimilarity()[3];
            average2[4] += sim.getFieldsCosineSimilarity()[4];

        }
        average2[0] /= qrels.getRelevantPatents(queryId).size();
        average2[1] /= qrels.getRelevantPatents(queryId).size();
        average2[2] /= qrels.getRelevantPatents(queryId).size();
        average2[3] /= qrels.getRelevantPatents(queryId).size();
        average2[4] /= qrels.getRelevantPatents(queryId).size();
        return average2;
    }

    public void printResults() {
        System.out.println("Title: " + average[0]);
        System.out.println("Classification: " + average[1]);
        System.out.println("Abstract: " + average[2]);
        System.out.println("Description: " + average[3]);
        System.out.println("Claims: " + average[4]);
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, Exception {
        // TODO code application logic here

        String queriesFilename;
        String topicsFilename;
        String qrels;
        String dataDir;
        String indexDir;
        boolean specificStopWords;
        if (args.length == 0) {
            queriesFilename = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/analysis/top.txt";
            topicsFilename = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/clef-ip-2010_PACTopics/PAC_topics.txt";
            qrels = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Evaluations/CLEF-IP2010-Results/PAC_qrels_21_EN_NEW.txt";
            dataDir = "/Volumes/TOSHIBA EXT/CLEF-IP/";
            indexDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/index/";
            specificStopWords = true;
        } else {
            queriesFilename = args[0];
            topicsFilename = args[1];
            qrels = args[2];
            dataDir = args[3];
            indexDir = args[4];
            specificStopWords = Boolean.valueOf(args[5]);
        }
        CosineAnalysis j = new CosineAnalysis(queriesFilename, topicsFilename, qrels, dataDir, indexDir, specificStopWords);
        j.analyze();
        j.printResults();
    }

}
