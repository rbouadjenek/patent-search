/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hrstc.lucene.queryexpansion;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlsearcheredgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Lucene" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Lucene", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
import java.io.File;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import java.util.HashMap;
import java.util.Map;
import nicta.com.au.patent.pac.search.PatentQuery;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class PatentClassCodeBasedQueryExpansion extends PatentQueryExpansion {

    private final Map<String, Float> parameters;
    private final IndexSearcher classCodesSearcher;
    private final int Nbr_Terms;

    public PatentClassCodeBasedQueryExpansion(String classCodesIndexDir, int Nbr_Terms,
            float alpha, float beta, float decay) throws IOException, Exception {
        Directory classCodesDir = FSDirectory.open(new File(classCodesIndexDir));
        classCodesSearcher = new IndexSearcher(DirectoryReader.open(classCodesDir));
        parameters = new HashMap<>();
        parameters.put(RocchioQueryExpansion.ROCCHIO_ALPHA_FLD, alpha);
        parameters.put(RocchioQueryExpansion.ROCCHIO_BETA_FLD, beta);
        parameters.put(RocchioQueryExpansion.DECAY_FLD, decay);
        this.Nbr_Terms = Nbr_Terms;

    }

    public PatentClassCodeBasedQueryExpansion(IndexSearcher searcher, IndexSearcher classCodesSearcher, int Nbr_Terms,
            float alpha, float beta, float decay) throws IOException, Exception {
        this.classCodesSearcher = classCodesSearcher;
        parameters = new HashMap<>();
        parameters.put(RocchioQueryExpansion.ROCCHIO_ALPHA_FLD, alpha);
        parameters.put(RocchioQueryExpansion.ROCCHIO_BETA_FLD, beta);
        parameters.put(RocchioQueryExpansion.DECAY_FLD, decay);
        this.Nbr_Terms = Nbr_Terms;
    }

    /**
     * Performs Rocchio's query expansion with pseudo feedback for each fields
     * separatlly qm = alpha * query + ( beta / relevanDocsCount ) * Sum ( rel
     * docs vector )
     *
     * @param query
     *
     * @return expandedQuery
     *
     * @throws IOException
     * @throws ParseException
     */
    @Override
    public Query expandQuery(PatentQuery query) throws ParseException, IOException {
        BooleanQuery bQuery = new BooleanQuery();
        BooleanQuery bQueryFieldsExpanded = new BooleanQuery();
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        //********************************************************************
        //**************** Get the sec of definition codes ******************* 
        //********************************************************************
        TotalHitCountCollector collector = new TotalHitCountCollector();
        Query codesQuery = GenerateClassCodesQuery.generateQuery(query.getFullClassCodes());
//        System.err.println(codesQuery);
        classCodesSearcher.search(codesQuery, collector);
        IndexReader ir = classCodesSearcher.getIndexReader();
        TopDocs hits = classCodesSearcher.search(codesQuery, Math.max(1, collector.getTotalHits())); // Compute PRF set
//                System.err.println("Found " + hits.totalHits
//                        + " document(s)  that matched query '"
//                        + codesQuery + "':");
//                for (ScoreDoc scoreDoc : hits.scoreDocs) {
//                    System.out.println("----------");
//                    Document doc = classCodesSearcher.doc(scoreDoc.doc);
//                    System.out.println(scoreDoc.score + "\t" + doc.get(PatentDocument.Classification) + "\t" + doc.get(PatentDocument.Title));// + "\t" + doc.get("type") + "\t" + doc.get("num") + "\t" + doc.get("lang"));
////            System.out.println(explanation.toString());
//                }
//                System.out.println("*************************************");
        Query expandedQuery = null;
        ClassCodeBasedQueryExpansion queryExpansion = new ClassCodeBasedQueryExpansion(hits,ir, parameters,Nbr_Terms);
        for (int i = 1; i < PatentQuery.getFields().length; i++) {
            if (query.getQueries()[i] != null && !query.getQueries()[i].equals("") && (i != 4 || i != 6) && query.getBoosts().get(PatentQuery.getFields()[i]) != 0) {
                QueryParser qp = new QueryParser(Version.LUCENE_48, PatentQuery.getFields()[i],
                        new StandardAnalyzer(Version.LUCENE_48));
                BooleanQuery bQueryFields = new BooleanQuery();// Contain a field to make the PRF field by field
                Query q = qp.parse(query.getQueries()[i]);
                if (query.isFilter()) {
                    Query filter = new QueryParser(Version.LUCENE_48, PatentQuery.getFields()[0],
                            new StandardAnalyzer(Version.LUCENE_48)).parse(query.getQueries()[0]);
                    bQueryFields.add(filter, BooleanClause.Occur.MUST);
                }
                if (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0) {
                    bQueryFields.add(q, BooleanClause.Occur.MUST);
                }

//                System.err.println(hits.totalHits + " total matching documents for field " + query.getFields()[i] + ".");
                if (expandedQuery == null) {
                    expandedQuery = queryExpansion.expandQuery(q, PatentQuery.getFields()[i]);
                } else {
                    BooleanQuery bq = ((BooleanQuery) expandedQuery).clone();
                    BooleanQuery bq2 = new BooleanQuery();
                    for (BooleanClause bc : bq.clauses()) {
                        TermQuery tq = (TermQuery) bc.getQuery();
                        Term term = new Term(PatentQuery.getFields()[i], tq.getTerm().text());
                        TermQuery tq2 = new TermQuery(term);
                        tq2.setBoost(tq.getBoost());
                        bq2.add(tq2, BooleanClause.Occur.SHOULD);
                    }
                    expandedQuery = bq2;
                }
                bQueryFieldsExpanded.add(expandedQuery, BooleanClause.Occur.SHOULD);// Compute the new expanded query based on PRF set
//                System.err.println("Expanded Query: " + expandedQuery);
//                hits = searcher.search(expandedQuery, 100);
//                System.err.println(hits.totalHits + " total matching documents"+ query.getFields()[i] + ".");
            }
        }
        if (query.isFilter()) {
            Query q = new QueryParser(Version.LUCENE_48, PatentQuery.getFields()[0],
                    new StandardAnalyzer(Version.LUCENE_48)).parse(query.getQueries()[0]);
            q.setBoost(query.getBoosts().get(PatentQuery.getFields()[0]));
            bQuery.add(q, BooleanClause.Occur.MUST);
        }
        bQuery.add(bQueryFieldsExpanded, BooleanClause.Occur.MUST);
//        hits = searcher.search(bQuery, 100);
//                System.err.println(hits.totalHits + " total matching documents.");
        return bQuery;
    }

    public static void main(String[] args) throws Exception {
        PatentQuery query;
        String classCodesIndexDir;
        if (args.length != 0) {
            classCodesIndexDir = args[0];
            query = new PatentQuery(args[1], 0, 0, 0, 0, 0, 0, true, true);
            System.err.println(query.parse());
        } else {
            classCodesIndexDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/codeIndex/";
            query = new PatentQuery("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/topics/PAC-1001_EP-1233512-A2.xml", 0, 1, 0, 0, 0, 0, true, true);
            System.err.println(query.parse());
        }
        PatentClassCodeBasedQueryExpansion pqe = new PatentClassCodeBasedQueryExpansion(classCodesIndexDir, 0, (float) 1, (float) 0.5, (float) 0);
        System.err.println(pqe.expandQuery(query));
    }
}
