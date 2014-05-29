/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.evaluation;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 *
 * @author rbouadjenek
 */
public class CreateAntiQrels {

    private final QrelsInMemory qrels;
    private final File results;
    private final int topIrrelevant;

    public CreateAntiQrels(String qrelsFilename, String results, int topIrrelevant) {
        this.qrels = new QrelsInMemory(qrelsFilename);
        this.results = new File(results);
        this.topIrrelevant = topIrrelevant;
    }

    public void createAntiQrels() {
        try {
            FileInputStream fstream = new FileInputStream(results);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);

            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                String currentQuery = "";
                int pos = 0;
                while ((str = br.readLine()) != null) {
                    if (str.trim().startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(str);
                    String queryid = st.nextToken();
                    st.nextToken();
                    String docid = st.nextToken();
                    String rank = st.nextToken();
                    String score = st.nextToken();
                    if (!currentQuery.equals(queryid)) {
                        currentQuery = queryid;
                        pos = 0;
                    }
                    if (currentQuery.equals(queryid) && pos < topIrrelevant && !qrels.isPatentRelevant(queryid, docid)) {
                        System.out.println(queryid + " 0 " + docid + " " + 0);
                        pos++;
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        CreateAntiQrels antiQrels = new CreateAntiQrels("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/PAC_test_rels.txt", "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Evaluations/results-CLEF-IP-2010/qTitle-sClaims-5-0.txt", 10);
        antiQrels.createAntiQrels();
    }

}
