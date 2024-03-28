package it.unipi.dii.aide.mircv.application.data;

/**
 * Represents a posting in a posting list.
 */
public class Posting {

    /**
     * The document ID of the posting.
     */
    private int docId;

    /**
     * The term frequency of the posting.
     */
    private int frequency;

    /**
     * Default constructor.
     */
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
        this.docId = docId;
        this.frequency = frequency;
    }

    /**
     * Gets the document ID of the posting.
     *
     * @return The document ID.
     */
    public int getDocId() {
        return docId;
    }

    /**
     * Sets the document ID of the posting.
     *
     * @param docId The document ID to set.
     */
    public void setDocId(int docId) {
        this.docId = docId;
    }

    /**
     * Gets the term frequency of the posting.
     *
     * @return The term frequency.
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Sets the term frequency of the posting.
     *
     * @param frequency The term frequency to set.
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     * Returns a string representation of the posting.
     *
     * @return A string representation containing document ID and term frequency.
     */
    @Override
    public String toString() {
        return "Posting{" +
                "docId=" + docId +
                ", frequency=" + frequency +
                '}';
    }
}
