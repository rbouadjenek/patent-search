/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
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
import java.util.StringTokenizer;

/**
 *
 * @author rbouadjenek
 */
public final class DistributionInMemory {

    private final Map<String, Double> distributionInMemory = new HashMap<>();
    private final String distributionFilename;

    public DistributionInMemory(String distributionFilename) {
        this.distributionFilename = distributionFilename;
        this.loadDistributionFile();
    }

    public void loadDistributionFile() {
        try {
            FileInputStream fstream = new FileInputStream(distributionFilename);
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
                    int num = Integer.parseInt(st.nextToken());
                    String queryid = st.nextToken();
                    double avP = Double.parseDouble(st.nextToken());
                    distributionInMemory.put(queryid, avP);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Double> getDistributionInMemory() {
        return distributionInMemory;
    }
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        DistributionInMemory dist = new DistributionInMemory("/Volumes/Macintosh HD/Users/rbouadjenek/Google Drive/NICTA-LENS Project/results/data-CLEF-IP_2010/distAbstract.txt");
        System.out.println(dist.distributionInMemory.get("PAC-1001")-0.117594736);
    }

}
