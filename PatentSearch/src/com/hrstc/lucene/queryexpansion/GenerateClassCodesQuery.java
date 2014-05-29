/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hrstc.lucene.queryexpansion;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nicta.com.au.patent.document.PatentDocument;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;

/**
 *
 * @author rbouadjenek
 */
public class GenerateClassCodesQuery {

    public static Pattern pattern = Pattern.compile("([A-Z])(\\d{2})([A-Z])(\\d+)/(\\d+)");

    public static Query generateQuery(Set<String> codes) {
        BooleanQuery bquery = new BooleanQuery();
        for (String code : codes) {
            // in case you would like to ignore case sensitivity,
            // you could use this statement:
            // Pattern pattern = Pattern.compile("\\s+", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(code);
            // check all occurance
            while (matcher.find()) {
                PhraseQuery pq = new PhraseQuery();
                PhraseQuery section = new PhraseQuery();
                section.add(new Term(PatentDocument.Classification, matcher.group(1)));
                bquery.add(section, BooleanClause.Occur.SHOULD);

                PhraseQuery classification = new PhraseQuery();
                classification.add(new Term(PatentDocument.Classification, matcher.group(1) + matcher.group(2)));
                bquery.add(classification, BooleanClause.Occur.SHOULD);

                PhraseQuery subclass = new PhraseQuery();
                subclass.add(new Term(PatentDocument.Classification, matcher.group(1) + matcher.group(2) + matcher.group(3)));
                bquery.add(subclass, BooleanClause.Occur.SHOULD);

                PhraseQuery mainGroup = new PhraseQuery();
                mainGroup.add(new Term(PatentDocument.Classification, matcher.group(1) + matcher.group(2) + matcher.group(3) + matcher.group(4) + "NICTA00"));
                bquery.add(mainGroup, BooleanClause.Occur.SHOULD);

                PhraseQuery subGroup = new PhraseQuery();
                subGroup.add(new Term(PatentDocument.Classification, matcher.group(1) + matcher.group(2) + matcher.group(3) + matcher.group(4) + "NICTA" + matcher.group(5)));
                bquery.add(subGroup, BooleanClause.Occur.SHOULD);

            }
        }

        return bquery;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

}
