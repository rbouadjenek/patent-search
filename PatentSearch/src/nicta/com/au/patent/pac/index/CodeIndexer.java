/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.index;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.document.PatentsStopWords;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author rbouadjenek
 */
public class CodeIndexer {

    private final IndexWriter writer;
    private final Analyzer analyzer;
    private final TextFilesFilter filter = new TextFilesFilter();
    private final Set<String> codes = new HashSet<>();

    public CodeIndexer(String indexDir) throws IOException {
        File indexDirFile = new File(indexDir);
        analyzer = new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ENGLISH_STOP_WORDS_SET);
//        analyzer = new StandardAnalyzer(Version.LUCENE_48);
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        conf.setUseCompoundFile(false);
//        conf.setCodec(new SimpleTextCodec());
        writer = new IndexWriter(FSDirectory.open(indexDirFile), conf);
    }

    private void close() throws IOException {
        writer.close();
    }

    public void index(String dataDir) throws Exception {
        File f = new File(dataDir);
        File[] listFiles = f.listFiles();
        for (File listFile : listFiles) {
            if (listFile.isDirectory()) {
                index(listFile.toString());
            } else {
                if (!listFile.isHidden() && listFile.exists() && listFile.canRead() && filter.accept(listFile)) {
                    indexFile(listFile);
                }
            }
        }
    }

    private static class TextFilesFilter implements FileFilter {

        @Override
        public boolean accept(File path) {
            return path.getName().toLowerCase().endsWith(".xml");
        }
    }

    public int getNumberofDocs() {
        return writer.numDocs();
    }

    private void indexFile(File f) throws Exception {
        System.out.println(writer.numDocs() + 1 + "- Indexing " + f.getCanonicalPath());
        DomReader reader = new DomReader(f);
        List<Document> docs = reader.getDocuments();
        for (Document doc : docs) {
            if (!codes.contains(doc.get(PatentDocument.Classification))) {
                writer.addDocument(doc);
                codes.add(doc.get(PatentDocument.Classification));
            } 
        }
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        String dataDir;
        String indexDir;
        if (args.length == 0) {
            dataDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/classCodes/xml/";
            indexDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/codeIndex/";
        } else {
            dataDir = args[0];
            indexDir = args[1];
        }
        long start = System.currentTimeMillis();
        CodeIndexer indexer = new CodeIndexer(indexDir);
        try {
            indexer.index(dataDir);
            long end = System.currentTimeMillis();
            long millis = (end - start);
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("Indexed " + indexer.getNumberofDocs() + " files took " + Functions.getTimer(millis) + ".");
            System.out.println("-------------------------------------------------------------------------");
        } finally {
            indexer.close();
        }
    }

}
