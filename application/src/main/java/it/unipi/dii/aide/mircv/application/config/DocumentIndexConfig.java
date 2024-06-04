package it.unipi.dii.aide.mircv.application.config;

public class DocumentIndexConfig
{
    private final String documentIndexPath; //File

    public DocumentIndexConfig(String documentIndexPath) {
        this.documentIndexPath = documentIndexPath;
    }

    public String getDocumentIndexPath() {
        return documentIndexPath;
    }
}
