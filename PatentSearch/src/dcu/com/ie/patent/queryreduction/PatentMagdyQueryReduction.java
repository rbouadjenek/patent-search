/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dcu.com.ie.patent.queryreduction;

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
import nicta.com.au.patent.queryreduction.*;
import com.hrstc.lucene.queryexpansion.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
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

public class PatentMagdyQueryReduction extends PatentQueryExpansion {

    private final IndexSearcher searcher;
    private final int Nbr_Docs;
    private final int Nbr_Terms;
    private final int source;
    private final String model;

    public PatentMagdyQueryReduction(String indexDir, String model, int Nbr_Docs, int Nbr_Terms, int source) throws IOException, Exception {
        Directory dir = FSDirectory.open(new File(indexDir));
        searcher = new IndexSearcher(DirectoryReader.open(dir));
        this.model = model;
        this.Nbr_Docs = Nbr_Docs;
        this.Nbr_Terms = Nbr_Terms;
        this.source = source;
        if (source <= 0 || source == 4 || source == 6 || source > 7) {
            throw new Exception("Invalid source of expansion!");
        }
    }

    public PatentMagdyQueryReduction(IndexSearcher searcher, String model, int Nbr_Docs, int Nbr_Terms, int source) throws IOException, Exception {
        this.searcher = searcher;
        this.model = model;
        this.Nbr_Docs = Nbr_Docs;
        this.Nbr_Terms = Nbr_Terms;
        this.source = source;
        if (source <= 0 || source == 4 || source == 6 || source > 7) {
            throw new Exception("Invalid source of expansion!");
        }
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
        IndexReader ir = searcher.getIndexReader();
        BooleanQuery bQuery = new BooleanQuery();
        BooleanQuery bQueryFieldsExpanded = new BooleanQuery();
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        //*****************************************************************
        //**************** Compute the PRF for field (i)******************* 
        //*****************************************************************
        TotalHitCountCollector collector = new TotalHitCountCollector();
        searcher.search(query.parse(), collector);
        TopDocs hits = searcher.search(query.parse(), Math.max(1, collector.getTotalHits())); // Compute PRF set

//                System.err.println(hits.totalHits + " total matching documents for field " + query.getFields()[i] + ".");
        Query expandedQuery = null;
        MagdyQueryReduction qe = new MagdyQueryReduction(hits, ir, PatentQuery.getFields()[source], Nbr_Docs, Nbr_Terms);

        for (int i = 1; i < PatentQuery.getFields().length; i++) {
            if (query.getQueries()[i] != null && !query.getQueries()[i].equals("") && (i != 4 || i != 6) && query.getBoosts().get(PatentQuery.getFields()[i]) != 0) {
                QueryParser qp = new QueryParser(Version.LUCENE_48, PatentQuery.getFields()[i],
                        new StandardAnalyzer(Version.LUCENE_48));
//                BooleanQuery bQueryFields = new BooleanQuery();// Contain a field to make the PRF field by field
                Query q = qp.parse(query.getQueries()[i]);
//                if (query.isFilter()) {
//                    Query filter = new QueryParser(Version.LUCENE_48, PatentQuery.getFields()[0],
//                            new StandardAnalyzer(Version.LUCENE_48)).parse(query.getQueries()[0]);
//                    bQueryFields.add(filter, BooleanClause.Occur.MUST);
//                }
//                if (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0) {
//                    bQueryFields.add(q, BooleanClause.Occur.MUST);
//                }
                if (expandedQuery == null) {
                    expandedQuery = qe.expandQuery(q, PatentQuery.getFields()[i]);
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
//        TopDocs hits = searcher.search(bQuery, 100);
//                System.err.println(hits.totalHits + " total matching documents.");
        return bQuery;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("MAGDY RD");
        PatentQuery query;
        String indexDir;
        if (args.length != 0) {
            indexDir = args[0];
            query = new PatentQuery(args[1], 1, 0, 0, 0, 0, 0, true, true);
            System.err.println(query.parse());
        } else {
            indexDir = "/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/Dev/indexWithoutSW-Vec-CLEF-IP2010/";
            query = new PatentQuery("/Volumes/Macintosh HD/Users/rbouadjenek/Documents/Patent-Project/CLEF-IP 2010/PAC_test/topics/PAC-1001_EP-1233512-A2.xml", 1, 0, 0, 0, 0, 0, true, true);
            System.err.println(query.parse());
        }
        PatentMagdyQueryReduction mmrqe = new PatentMagdyQueryReduction(indexDir, "bm25", 5,1, 2);
        System.err.println(mmrqe.expandQuery(query));
    }
}
