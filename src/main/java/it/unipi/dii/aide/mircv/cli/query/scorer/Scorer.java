package it.unipi.dii.aide.mircv.cli.query.scorer;

import it.unipi.dii.aide.mircv.cli.query.enums.Mode;
import it.unipi.dii.aide.mircv.cli.query.enums.ScoreFunction;
import it.unipi.dii.aide.mircv.config.model.Config;
import it.unipi.dii.aide.mircv.document.DocumentIndexState;
import it.unipi.dii.aide.mircv.document.table.DocumentIndexTable;
import it.unipi.dii.aide.mircv.indexer.model.Posting;

public class Scorer {

    protected Config config;

    protected final ScoreFunction SCORE_FUNCTION;
    protected final Mode MODE;

    private static final double k1 = 1.5;
    private static final double b = 0.75;

    public Scorer(Config config, Mode mode, ScoreFunction scoringFunction) {
        this.config = config;
        this.SCORE_FUNCTION = scoringFunction;
        this.MODE = mode;
    }

    /**
     * score the posting using the specified scoring function
     *
     * @param posting         the posting on which the scoring is performed
     * @param idf             the idf of the term related to the posting
     * @return the score for the posting
     */
    protected double scoreDocument(Config config, Posting posting, double idf) {
        if (SCORE_FUNCTION == ScoreFunction.BM25) return computeBM25(config, posting, idf);
        if (SCORE_FUNCTION == ScoreFunction.TFIDF) return computeTFIDF(posting, idf);
        return 0;
    }

    /**
     * computes the BM25 scoring function
     *
     * @param posting the posting to score
     * @param idf     the idf to use
     * @return the score computed
     */
    private double computeBM25(Config config, Posting posting, double idf) {
        double tf = (1 + Math.log10(posting.getFrequency()));

        int docLen = DocumentIndexTable.with(config).getDocumentLength(posting.getDocumentId());

        long documentLength = DocumentIndexState.getTotalDocumentLen();
        long collectionSize =  DocumentIndexState.getCollectionSize();

        double avgDocLen = (double)  documentLength / collectionSize;

        return idf * tf / (tf + k1 * (1 - b + b * docLen / avgDocLen));
    }

    /**
     * computes the TFIDF scoring function
     *
     * @param posting the posting to score
     * @param idf     the idf to use
     * @return the score computed
     */
    private static double computeTFIDF(Posting posting, double idf) {
        return idf * (1 + Math.log10(posting.getFrequency()));
    }
}
