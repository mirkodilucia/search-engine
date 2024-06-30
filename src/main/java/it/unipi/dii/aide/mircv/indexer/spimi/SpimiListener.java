package it.unipi.dii.aide.mircv.indexer.spimi;

public interface SpimiListener {
    public void updateDocumentLength(int length);
    public void onSpimiFinished();
    public void incrementDocumentId();
}
