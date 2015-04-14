NICTA Patent Prior Art Search
April 2015
================================

This projet aims to develop an IR platform for Patent Prior Art Search Project. 
This implementation is developed on the top of the Lucene IR search engine. 

Contributed by:
================================
- Reda Bouadjenek (rbouadjenek@gmail.com)
- Scott Sanner (scott.sanner@oregonstate.edu)
- Mona Golestan (Mona.GolestanFar@nicta.com.au)
- Gabriela Ferraro (gabriela.ferraro@nicta.com.au)


************************************************************
DISCLAIMER:

NICTA is not liable to you or anyone else if interference with or damage
to your computer systems occurs in connection with use of this software. 

************************************************************

LICENSE
================================

Copyright (c) 2015, National ICT Australia
All rights reserved.

This software is under the MOZILLA PUBLIC LICENSE (MPL).
Please see the license file LICENSE.txt

The contents (data/documentation/artwork) is under the Creative Commons 3.0 BY-SA 
More information about "Creative Commons" license can be found at
http://creativecommons.org/licenses/by-sa/3.0/

QUICK START
================================

1. INDEXING
================================



To use this software you need to first index a collection of patents. You can use the CLEF-IP datasets 
proposed here: http://www.ifs.tuwien.ac.at/~clef-ip/.

In this patent collection, each patent consists of multiple versions of documents in XML format
labelled as A1, A2 … B1, and B2. The “A” letter refers to different versions of patent
applications. The number with the letter refers to the version; thus A1 refers to the initial version
of a patent application submitted to the patent office. The “B” versions refer to granted patents.
Each of these versions contains some updates to the text, citations, and claims of the previous
one. We advise to merge different versions of a single patent into one single document as 
suggested by the track organizers. The content of each section in the
merged document has to be taken from the latest available version, since some of the final versions
consisted of only the sections which had their content updated rather than all the patent sections.

This merge operation is performed by the class: nicta.com.au.patent.document.CreateUnifiedDocuments

-> Usage: java -cp PatentSearch.jar nicta.com.au.patent.document.CreateUnifiedDocuments [collection]

Once the merged files are created, for indexing use: nicta.com.au.patent.pac.index.PACIndexer

-> Usage: java -cp PatentSearch.jar nicta.com.au.patent.pac.index.PACIndexer [options]
			[options] have to be defined in the following order:
			[-dataDir]: directory of fthe collection
			[-indexDir]: directory of the index

In the implementation, each section of a patent (title, abstract,
claims, and description) is indexed in a separate field. However,
when a query is processed, all indexed fields are targeted, since
this generally offers best retrieval performance. This implementation 
index whith the default settings for stemming and stop-word removal. 
It also remove patent-specific stop-words which are given in : nicta.com.au.patent.document.PatentsStopWords

Finally, we provide here the two indexes we used in our paper: 

A Study of Query Reformulation for Patent Prior Art Search with Partial Patent Applications. 
Mohamed Reda Bouadjenek, Scott Sanner, Gabriela Ferraro. In ICAIL '15 Proceedings of the 
15th ACM ICAIL International Conference on Artificial Intelligence and Law, 2015.


CLEF-IP 2010 : https://mega.co.nz/#!2YZDECTb!mOiuU99YbFM7nsYXRjxxqC9DTykC4tS14ZGqJWNNr3U
CLEF-IP 2011 : https://mega.co.nz/#!WU4n0brZ!FaUHboFuvSqYR8FOp1rF8CJJIjpVbUUHGtbVhn1KFzY


2. SEARCH
===============================

To execute all the topics (queries) provided in the CLEF-IP collections (see the previous links), 
use: nicta.com.au.patent.pac.evaluation.ExecuteTopics

-> Usage: Usage: java -cp PatentSearch.jar nicta.com.au.patent.pac.evaluation.ExecuteTopics [options]
			[options] have to be defined in the following order:
			[-indexDir]: directory of the index
			[-topicFile]: topic file
			[-topK]: topK document retrieved for each topic
			[-similarity]: Similarity used (DefaultSimilarity, BM25Similarity, LMDirichletSimilarity, LMJelinekMercerSimilarity, IBSimilarity)
			[-titleBoost]: value for boosting the title 
			[-abstractBoost]: value for boosting the abstract 
			[-descriptionBoost]: value for boosting the description 
			[-descriptionP5Boost]: value for boosting the first five paragraphs of the description 
			[-claimsBoost]: value for boosting the claims 
			[-claims1Boost]: value for boosting the first claims 
			[-filter]: boolean to indicate whether or not we should use the IPC filer 
			[-stopWords]: boolean to indicate whether or not we should use the patent specific stop words 
			[-rewrite]: boolean to indicate whether or not we should rewrite the query according to a terms impact file
			[-termsImpactFilename]: term impact file
			[-expansion]: boolean to indicate whether or not we should expand the query
			[-expansion]: what expansion algorithm to use? Rocchio based on PRF or on Classification codes, or using MMRQE algorithm
			[-nbrDocs]: if Rocchio based on PRF, then give number of document used for the PRF set
			[-indexClassDir]: if Rocchio based on Classification codes, then give the directory of the classification index
			[-nbrtermsTitle]: number of term used for the expansion
			[-source]: source of the expansion title(1),abstract(2),description(3),or claims(5)
			[-alpha]: value of alpha to set the weight of the original query terms
			[-beta]: value of beta to set the weight of the expanded query terms for relevant documents
			[-gamma]: value of gamma to set the weight of the expanded query terms for irrelevant documents
			[-decay]: weight of the terms in top k documents


The results file has the format: query_id, iter, docno, rank, sim, run_id  delimited by spaces.
Query id is the query number.  The iter constant, 0, is required but ignored.  
The Document numbers are string values like EP-0734121.  The Similarity (sim) is a float value.
Rank is an integer from 0 to 1000.  Runid is a string which gets printed out with the output.  
An example of a line from the results file:

PAC-1001 Q0 EP-0734121 1 1314.7852 STANDARD
PAC-1001 Q0 EP-1152529 2 1123.9963 STANDARD
PAC-1001 Q0 EP-1126605 3 1109.3835 STANDARD


3. EVALUATION
===============================


We advise the usage of two tools to evaluate the obtained results using the standard, NIST evaluation procedures.

-> TRECEVAL: http://faculty.washington.edu/levow/courses/ling573_SPR2011/hw/trec_eval_desc.htm

-> PRESEval: http://alt.qcri.org/~wmagdy/Scripts/PRESeval.htm

Note: qrels files which contain the query relevance assessment in TREC format are also provided in the links bellow.



CONTACT
================================
For questions about this distribution, please contact:
- Reda Bouadjenek (rbouadjenek@gmail.com)
- Scott Sanner (scott.sanner@oregonstate.edu)
- Mona Golestan (Mona.GolestanFar@nicta.com.au)
- Gabriela Ferraro (gabriela.ferraro@nicta.com.au)


PACKAGE OUTLINE
================================


CITATION
====================
The user must acknowledge the use of this tool in publications by citing the following papers:

- A Study of Query Reformulation for Patent Prior Art Search with Partial Patent Applications. 
Mohamed Reda Bouadjenek, Scott Sanner, Gabriela Ferraro. In ICAIL '15 Proceedings of the 
15th ACM ICAIL International Conference on Artificial Intelligence and Law, 2015.











