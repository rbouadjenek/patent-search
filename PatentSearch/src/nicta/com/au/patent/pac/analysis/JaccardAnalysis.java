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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author rbouadjenek
 */
public class JaccardAnalysis {

    private final IndexSearcher is;
    private final File queriesFilename;
    private final TopicsInMemory topics;
    private final QrelsInMemory qrels;
    private double[] average;
    private final boolean specificStopWords;

    public JaccardAnalysis(String indexDir, String queriesFilename, String topicsFilename, String qrelsFilename, boolean specificStopWords) throws IOException {
        Directory dir = FSDirectory.open(new File(indexDir));
        is = new IndexSearcher(DirectoryReader.open(dir));
        this.queriesFilename = new File(queriesFilename);
        this.topics = new TopicsInMemory(topicsFilename);
        this.qrels = new QrelsInMemory(qrelsFilename);
        this.specificStopWords = specificStopWords;
        System.out.println("*******************");
        System.out.println("Jaccard");
        System.out.println("Type: " + qrelsFilename);
        System.out.println("File name: " + this.queriesFilename.getName());
        System.out.println("Specific Stop Words: " + specificStopWords);
        System.out.println("*******************");
    }

    public void analyze() {
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
                    double[] average2 = this.queryJaccard(queryId, topics.getTopics().get(queryId));
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

    public double[] queryJaccard(String queryId, PatentDocument query) throws IOException {
        double[] average2 = new double[5];
        average2[0] = 0;
        average2[1] = 0;
        average2[2] = 0;
        average2[3] = 0;
        average2[4] = 0;
        for (String p : qrels.getRelevantPatents(queryId).keySet()) {
            String doc = "UN-" + p;

            FieldsJaccardSimilarities sim = new FieldsJaccardSimilarities(is, query, doc, specificStopWords);
            sim.computeJaccardSimilarities();
            average2[0] += sim.getFieldsJaccardSimilarity()[0];
            average2[1] += sim.getFieldsJaccardSimilarity()[1];
            average2[2] += sim.getFieldsJaccardSimilarity()[2];
            average2[3] += sim.getFieldsJaccardSimilarity()[3];
            average2[4] += sim.getFieldsJaccardSimilarity()[4];

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
    public static void main(String[] args) throws IOException {
        // TODO code application logic here

        String queriesFilename;
        String topicsFilename;
        String qrels;
        String indexDir;
        boolean specificStopWords;
        if (args.length == 0) {
            indexDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/indexWithoutSW-Vec-CLEF-IP2010-2.0/";
            queriesFilename = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/analysis/bottomDescription-CLEF-IP-2010.txt";
            topicsFilename = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/topics/PAC_topics.xml";
            qrels = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/PAC-Description_Anti_qrels_EN.txt";
            specificStopWords = true;
        } else {
            indexDir = args[0];
            queriesFilename = args[1];
            topicsFilename = args[2];
            qrels = args[3];
            specificStopWords = Boolean.valueOf(args[4]);
        }
        JaccardAnalysis j = new JaccardAnalysis(indexDir, queriesFilename, topicsFilename, qrels, specificStopWords);
        j.analyze();
        j.printResults();
    }

}
