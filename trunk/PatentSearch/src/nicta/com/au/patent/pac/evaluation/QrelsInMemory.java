/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.evaluation;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author rbouadjenek
 */
public final class QrelsInMemory {

    /**
     * Each element in the array contains the docids of the relevant documents
     * with respect to a query.
     */
    public Map<String, Map<String, Integer>> qrelsPerQuery = new HashMap<>();
    /**
     * The qrels file.
     */
    protected String qrelsFilename;

    /**
     * A constructor that creates an instance of the class and loads in memory
     * the relevance assessments from the given file.
     *
     * @param qrelsFilename String The full path of the qrels file to load.
     */
    public QrelsInMemory(String qrelsFilename) {
        this.qrelsFilename = qrelsFilename;
        this.loadQrelsFile();
    }

    /**
     * Get ids of the queries that appear in the pool.
     *
     * @return The ids of queries in the pool.
     */
    public Set<String> getQueryids() {
        return qrelsPerQuery.keySet();
    }

    /**
     * Returns the total number of queries contained in the loaded relevance
     * assessments.
     *
     * @return int The number of unique queries in the loaded relevance
     * assessments.
     */
    public int getNumberOfQueries() {
        return this.qrelsPerQuery.size();
    }

    /**
     * Get the pooled relevant documents for the given query.
     *
     * @param queryid The id of the given query.
     * @return A hashset containing the docnos of the pooled relevant documents
     * for the given query.
     */
    public Map<String, Integer> getRelevantPatents(String queryid) {
        return qrelsPerQuery.get(queryid);
    }

    /**
     * Returns the numbe of relevant documents for a given query.
     *
     * @param queryid String The identifier of a query.
     * @return int The number of relevant documents for the given query.
     */
    public int getNumberOfRelevantPatent(String queryid) {
        return getRelevantPatents(queryid).size();
    }

    /**
     * Check if the given document is relevant for a given query.
     *
     * @param queryid String a query identifier.
     * @param docid String a document identifier.
     * @return boolean true if the given document is relevant for the given
     * query, or otherwise false.
     */
    public boolean isPatentRelevant(String queryid, String docid) {
        return qrelsPerQuery.get(queryid).containsKey(docid);
    }

    /**
     * Load in memory the relevance assessment files that are specified in the
     * array fqrels.
     */
    protected void loadQrelsFile() {
        try {
            FileInputStream fstream = new FileInputStream(qrelsFilename);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                while ((str = br.readLine()) != null) {
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(str);
                    String queryid = st.nextToken();
                    String v = st.nextToken();
                    String docid = st.nextToken();
                    String relevance = st.nextToken();
                    Map<String, Integer> patent;
                    if (qrelsPerQuery.containsKey(queryid)) {
                        patent = qrelsPerQuery.get(queryid);
                        if (patent.containsKey(docid)) {
                            int val = patent.get(docid);
                            if (val < Integer.parseInt(relevance)) {
                                patent.put(docid, Integer.parseInt(relevance));
                            }
                        } else {
                            patent.put(docid, Integer.parseInt(relevance));
                        }

                    } else {
                        patent = new HashMap<>();
                        patent.put(docid, Integer.parseInt(relevance));
                    }
                    qrelsPerQuery.put(queryid, patent);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        QrelsInMemory qrels = new QrelsInMemory("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2011/PAC_test/PAC_test_rels.txt");
        System.out.println(qrels.qrelsPerQuery.size());
        System.out.println(qrels.getNumberOfRelevantPatent("PAC-1001"));

    }
}
