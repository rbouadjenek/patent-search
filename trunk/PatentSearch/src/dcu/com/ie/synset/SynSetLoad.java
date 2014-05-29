/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template synSetFile, choose Tools | Templates
 * and open the template in the editor.
 */
package dcu.com.ie.synset;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author rbouadjenek
 */
public final class SynSetLoad {

    class MyEntry<String, Double> implements Map.Entry<String, Double> {

        private final String key;
        private Double value;

        public MyEntry(String key, Double value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Double getValue() {
            return value;
        }

        @Override
        public Double setValue(Double value) {
            Double old = this.value;
            this.value = value;
            return old;
        }
    }

    Map<String, List<Map.Entry<String, Double>>> synSet = new HashMap<>();
    protected File synSetFile;

    public SynSetLoad(File file) {
        this.synSetFile = file;
        this.loadQrelsFile();
    }

    public SynSetLoad(String file) {
        this.synSetFile = new File(file);
        this.loadQrelsFile();
    }

    protected void loadQrelsFile() {
        FileInputStream fstream;
        try {
            fstream = new FileInputStream(this.synSetFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                List<Map.Entry<String, Double>> l = new ArrayList<>();
                while ((str = br.readLine()) != null) {
                    str = str.trim();
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(str);
                    String term = st.nextToken();
                    if (term.contains(":")) {
                        l = new ArrayList<>();
                        synSet.put(term.replace(":", ""), l);
                    } else {
                        Double d = Double.valueOf(st.nextToken());
                        MyEntry e = new MyEntry(term, d);
                        l.add(e);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Map<String, List<Map.Entry<String, Double>>> getSynSet() {
        return synSet;
    }

    public List<Map.Entry<String, Double>> getSynSeyList(String term, int Nbr_Terms) {
        if (synSet.containsKey(term)) {
            Comparator comparator = new SynSetTermComparator();
            List<Map.Entry<String, Double>> l = synSet.get(term);
            Collections.sort(l, comparator);
            if (Nbr_Terms < l.size()) {
                return l.subList(0, Nbr_Terms);
            } else {
                return l;
            }
        } else {
            MyEntry e = new MyEntry(term, 1.0);
            List<Map.Entry<String, Double>> l = new ArrayList<>();
            l.add(e);
            return new ArrayList<>();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        // TODO code application logic here
        SynSetLoad synset = new SynSetLoad("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/SynSet/SynSet.pruned.txt");
        int max=0;
        for (Map.Entry<String, List<Map.Entry<String, Double>>> e1 : synset.getSynSet().entrySet()) {
            System.out.println(e1.getKey()+" -> "+e1.getValue().size());
            if(max<e1.getValue().size()) max=e1.getValue().size();
            for (Map.Entry<String, Double> e2 : e1.getValue()) {
                System.out.println("\t" + e2.getKey() + " -> " + e2.getValue());
            }
        }
//        System.out.println("max= "+max);

//            System.out.println(topics.getTopics().size());
    }

}
