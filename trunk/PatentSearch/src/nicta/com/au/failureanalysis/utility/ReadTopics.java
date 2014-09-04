package nicta.com.au.failureanalysis.utility;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.pac.evaluation.TopicsInMemory;
import nicta.com.au.patent.pac.evaluation.QrelsInMemory;
//import nicta.com.au.patent.pac.evaluation.TopicsInMemory;

public class ReadTopics {
	
	protected File file;
    protected LinkedHashMap<String, PatentDocument> topics = new LinkedHashMap<>();

    public ReadTopics(File Topics) throws IOException {
        this.file = Topics;
        this.loadQrelsFile();
    }

    public ReadTopics(String file) throws IOException {
        this.file = new File(file);

        this.loadQrelsFile();
    }

    public Map<String, PatentDocument> getTopics() {
        return topics;
    }

    protected void loadQrelsFile() {
        FileInputStream fstream;
        try {
            fstream = new FileInputStream(this.file);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                String topicid = "";
                PatentDocument patentTopic;
                while ((str = br.readLine()) != null) {
                    str = str.trim();
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
                    if (str.startsWith("<num>")) {
                        str = str.replace("<num>", "");
                        str = str.replace("</num>", "");
                        topicid = str;
                    } else if (str.startsWith("<file>") && !topicid.equals("")) {
                        str = str.replace("<file>", "");
                        str = str.replace("</file>", "");
                        patentTopic = new PatentDocument(file.getParent() + "/" + str);
                        if (patentTopic.getLang() != null) {
                            if (patentTopic.getLang().toLowerCase().equals("en")) {
                                this.topics.put(topicid, patentTopic);
                            }
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param args the1 command line1 argume1nts
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
//            TopicsInMemory topics = new TopicsInMemory("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_training/topics/PAC_topics.xml");
        	ReadTopics topics = new ReadTopics("data/CLEF-IP-2010/PAC_test/topics/PAC_topics.xml");
        	Map<String, PatentDocument> x = topics.getTopics();
        	for(Map.Entry<String, PatentDocument> topic : topics.getTopics().entrySet()){
        		System.out.println(topic.getKey() + "_" + topic.getValue().getUcid());
        	}
        		
        	System.out.println(topics.getTopics().size());
            QrelsInMemory qrels = new QrelsInMemory("data/qrel/PAC_test_rels.txt");
                     
            /*for (String queryid : topics.getTopics().keySet()) {
            	for (Map.Entry<String, Integer> e : qrels.getRelevantPatents(queryid).entrySet()) {
            		String docid = e.getKey();
            		int rel = e.getValue();
            		System.out.println(queryid + " 0 " + docid + " " + rel);
            	}

            }*/

        } catch (IOException ex) {
            Logger.getLogger(TopicsInMemory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
