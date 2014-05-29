/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicta.com.au.patent.pac.index;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import nicta.com.au.main.Functions;
import nicta.com.au.patent.document.Claim;
import nicta.com.au.patent.document.Claims;
import nicta.com.au.patent.document.ClassificationIpcr;
import nicta.com.au.patent.document.InventionTitle;
import nicta.com.au.patent.document.P;
import nicta.com.au.patent.document.PatentDocument;
import nicta.com.au.patent.document.PatentsStopWords;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author rbouadjenek
 */
public class PACIndexer {

    private final IndexWriter writer;
    private final Analyzer analyzer;
    private final TextFilesFilter filter = new TextFilesFilter();
    private final PerFieldAnalyzerWrapper aWrapper;

    public PACIndexer(String indexDir) throws IOException {
        File indexDirFile = new File(indexDir);

        Map<String, Analyzer> analyzerPerField = new HashMap<>();
        analyzerPerField.put(PatentDocument.Title, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.TITLE_ENGLISH_STOP_WORDS_SET));
        analyzerPerField.put(PatentDocument.Abstract, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ABSTRACT_ENGLISH_STOP_WORDS_SET));
        analyzerPerField.put(PatentDocument.Description, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.DESCRIPTION_ENGLISH_STOP_WORDS_SET));
        analyzerPerField.put(PatentDocument.Claims, new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.CLAIMS_ENGLISH_STOP_WORDS_SET));
        aWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_48), analyzerPerField);

        analyzer = new EnglishAnalyzer(Version.LUCENE_48, PatentsStopWords.ENGLISH_STOP_WORDS_SET);
//        analyzer = new StandardAnalyzer(Version.LUCENE_48);
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_48, aWrapper);
        conf.setUseCompoundFile(false);
//        conf.setCodec(new SimpleTextCodec());
        writer = new IndexWriter(FSDirectory.open(indexDirFile), conf);
    }

    private void close() throws IOException {
        writer.close();
    }

    public int index(String dataDir) throws Exception {
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
        return writer.numDocs();
    }

    private static class TextFilesFilter implements FileFilter {

        @Override
        public boolean accept(File path) {
            return path.getName().toLowerCase().endsWith(".xml") && path.getName().startsWith("UN");
        }
    }

    public int getNumberofDocs() {
        return writer.numDocs();
    }

    private Document getDocument(PatentDocument patent) throws IOException {
        Document doc = new Document();
        doc.add(new StringField(PatentDocument.FileName, patent.getUcid(), Field.Store.YES));// Index Filename

        for (InventionTitle title : patent.getTechnicalData().getInventionTitle()) {// Index Title**********
            if (title.getLang().toLowerCase().equals("en")) {
//                doc.add(new Field("title", title.getContent(), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES));
                doc.add(new VecTextField(PatentDocument.Title, new StringReader(title.getContent()), Store.NO));
            }
        }
        //**********************************************************************************
        String codes = "";
        for (ClassificationIpcr ipc : patent.getTechnicalData().getClassificationIpcr()) {// Index IPC****************
            String code = new StringTokenizer(ipc.getContent()).nextToken();
            if (!codes.contains(code)) {
                codes += code + " ";
            }
        }
//        doc.add(new TextField(PatentDocument.Classification, new StringReader(codes)));
        doc.add(new TextField(PatentDocument.Classification, codes, Store.YES));
        //*******************************************************************************
        if (patent.getAbstrac() != null && patent.getAbstrac().getLang() != null
                && patent.getAbstrac().getLang().toLowerCase().equals("en")) {// Index Abstract
            doc.add(new VecTextField(PatentDocument.Abstract, new StringReader(patent.getAbstrac().getContent()), Store.NO));
        }
        //*****************************************************************

        if (patent.getDescription() != null && patent.getDescription().getLang().toLowerCase().equals("en")) {// Index Description
            String description = "";
            for (P p : patent.getDescription().getP()) {
                description += p.getContent() + " ";
            }
            doc.add(new VecTextField(PatentDocument.Description, new StringReader(description), Store.NO));
        }
        //*********************************************************

        for (Claims claims : patent.getClaims()) {// Index Claims
            if (claims.getLang().toLowerCase().equals("en")) {
                String claimsText = "";
                for (Claim claim : claims.getClaim()) {
                    claimsText += claim.getClaimText() + " ";
                }
                doc.add(new VecTextField(PatentDocument.Claims, new StringReader(claimsText), Store.NO));
            }
        }
        //******************************************************
        return doc;
    }

    private void indexFile(File f) throws Exception {
        System.out.println(writer.numDocs() + 1 + "- Indexing " + f.getCanonicalPath());
        PatentDocument patent = new PatentDocument(f);
        writer.addDocument(this.getDocument(patent));
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
            dataDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/docs/";
            indexDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/index/";
        } else {
            dataDir = args[0];
            indexDir = args[1];
        }
        long start = System.currentTimeMillis();
        PACIndexer indexer = new PACIndexer(indexDir);
        int numIndexed;
        try {
            numIndexed = indexer.index(dataDir);
            long end = System.currentTimeMillis();
            System.out.println("-------------------------------------------------------------------------");
            long millis = (end - start);
            System.out.println("Indexing " + numIndexed + " files took " + Functions.getTimer(millis) + ".");
            System.out.println("Indexed " + indexer.getNumberofDocs() + " files in English.");
            System.out.println("-------------------------------------------------------------------------");
        } finally {
            indexer.close();
        }
    }

}
