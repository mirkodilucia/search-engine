package it.unipi.dii.aide.mircv.document.preprocess;

import java.util.ArrayList;
import java.util.List;

public class FinalDocument {

    private final String docId;
    private final ArrayList<String> tokens;

    public FinalDocument(String docId, String[] tokens) {
        this.docId = docId;
        this.tokens = new ArrayList<>(List.of(tokens));
    }

    public String getDocId() {
        return docId;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public String toString() {
        return docId + '\t' + String.join(",", tokens);
    }

    public boolean isEmpty() {
        return tokens.isEmpty();
    }
}
