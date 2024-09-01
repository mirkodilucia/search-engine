# Search-engine project :page_with_curl:

The project aim to develop a search engine able to index and process a minimum dataset of 8.8 million documents, in particular the dataset that can be found [here](https://microsoft.github.io/msmarco/TREC-Deep-Learning-2020)
under the "Passage ranking dataset" section, first link. This project is related to the TREC evaluation for the MSMARCO dataset.

This project was developed for the course "Multimedia Information Retrieval" of the MsC Artifical Intelligence and Data Engineering at University of Pisa for the 2022/2023 year course.

The project was developed by Chianese Andrea and Di Lucia Mirko, in pair programming.

For further details see the documentation [here](./documentation/Project_documentation.pdf)

### Project architecture

For testing and logic separation purposes the overall project was constructed based on the subsequent principal part, more details in the documentation:

- *Baseline library*
- *Indexer (Spimi and Merger)*
- *CLI*
- *Performance*

#### Baseline library

This part refers to all the basic functions and modules used by the main principal core functionalities, containing functions such as the ones used for compression, for preprocessing or the basic structures like the vocabulary 
or the scoring strategies and functions or again the flags for example.
All this functions are located near the main modules that uses them the most, for logic separation purposes. 

#### Indexer (Spimi and Merger)
This is one of the core parts, componing our project, it's the module that operate the Spimi and Merger algorithm.
This module can be compiled using the subsequent flags:

- *--debug* : permits to produce text files used for debugging purposes.
- *--compressed-reading*:  allows the reading of the compressed version of the initial dataset, in "tar.gz" format.
- *--stem* : enables the stemming and stopword removal of the documents in the dataset.
- *--compression* : activate the compression of the docids and frequencies.
- *--maxscore* : switch the scoring strategy used from DAAT to MaxScore.

In addition, the project is completely configurable just by changing the parameter in the config.xml file and the config class, this permits further customizations from the user.

If no flags where to be specified, or no changes applied to the config, the query processing will work with standard parameters such as: no debug, no compressed reading (reading directly the collection.tsv file of the dataset no stemming and stopword removal, no compression on docids and frequencies and the use of the DAAT as scoring strategy.

This flags can be found in the FlagManager class.

### CLI
This is the module that provides the user interface for querying the search engine and permits the user also to specify the searching mode of operation, conjunctive or disjunctive and also choose between the TFIDF and BM25 scoring functions,
in particular:

- query format : "terms to be searched" [-c "enables conjunctive mode" || -d "for disjunctive mode"]
  (example of a query: "life -c")
- selection of the scoring function: "tfidf" for the TFIDF or "bm25" for the BM25 one.

The interface will start by asking the user the query terms and the mode, after the user will be asked to specify the scoring function.

The results will be the top K mos relevant documents in discendant order based on the score from the scoring function.

This module also containt the "QueryParser" and "QueryHandler" modules.

#### Performance
This part contains the tests used for assessing performance of the overall system, for the queries and cache test to understand the impact of the use of the the caching.

For the query performance test, the results obtained are written in a text file, formatted in a suitable manner to be submitted for the TREC evaluation (trec_eval).

Format example: "1 Q0 pid1    1 2.73 runid1"



