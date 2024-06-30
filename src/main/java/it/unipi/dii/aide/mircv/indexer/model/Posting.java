package it.unipi.dii.aide.mircv.indexer.model;

public class Posting {

    private int documentId;
    private int frequency;

    public Posting() {
        // Default constructor with no parameters
    }

    /**
     * Constructor that initializes the posting with document ID and term frequency.
     *
     * @param docId     The document ID of the posting.
     * @param frequency The term frequency of the posting.
     */
    public Posting(int docId, int frequency) {
        this.documentId = docId;
        this.frequency = frequency;
    }

    public int getDocumentId() {
        return documentId;
    }
    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public int getFrequency() {
        return frequency;
    }
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "Posting{" +
                "documentId=" + documentId +
                ", frequency=" + frequency +
                '}';

    }

}
