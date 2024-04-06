package it.unipi.dii.aide.mircv.application.indexer.spimi;

public interface SpimiListener {

    public void updateDocumentId(String pid);
    public void updateDocumentLength(int length);
    public void onSpimiFinished();
}
