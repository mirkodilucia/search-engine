package it.unipi.dii.aide.mircv.application.data;


import java.util.ArrayList;
import java.util.List;

/**
 * The basic FinalDocument, formed by an identifier (pid) and the text payload.
 */
public class FinalDocument {

    /**
     * the identifier of the document
     */
    private String pid;

    /**
     * Array with all the processed terms
     */
    private ArrayList<String> tokens;

    public FinalDocument() {}

    /**
     * Creates a new FinalDocument with the given identifier and payload
     * @param pid the document's identifier
     * @param text the document's payload
     */
    public FinalDocument(String pid, String[] text) {
        this.pid = pid;
        this.tokens = new ArrayList<>(List.of(text));
    }

    /**
     * gets the identifier
     * @return the identifier of the document
     */
    public String getPid() {
        return pid;
    }

    /**
     * sets the pid
     * @param pid the identifier of the document
     */
    public void setPid(String pid) {
        this.pid = pid;
    }

    /**
     * gets the document's text
     * @return the text payload of the document
     */
    public ArrayList<String> getTokens() {
        return tokens;
    }

    /**
     * sets the document's text
     * @param text the document's payload
     */
    public void setTokens(String[] text) {
        this.tokens = new ArrayList<>(List.of(text));
    }

    /**
     * Formats the document as a '[pid] \t [text] \n' string
     * @return the formatted string
     */
    public String toString() {
        return pid + '\t' + String.join(",", tokens) + '\n';
    }
}