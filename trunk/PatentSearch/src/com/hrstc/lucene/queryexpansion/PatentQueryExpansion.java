/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hrstc.lucene.queryexpansion;

import java.io.IOException;
import nicta.com.au.patent.pac.search.PatentQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

/**
 *
 * @author rbouadjenek
 */
public abstract class PatentQueryExpansion {

    public abstract Query expandQuery(PatentQuery query) throws ParseException, IOException;
}
