package it.unipi.dii.aide.mircv.application.indexer;

import it.unipi.dii.aide.mircv.application.config.Config;
import it.unipi.dii.aide.mircv.application.data.BlockDescriptor;
import it.unipi.dii.aide.mircv.application.data.Posting;
import it.unipi.dii.aide.mircv.application.data.PostingList;
import it.unipi.dii.aide.mircv.application.data.VocabularyEntry;


import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MergerLoader {

    public FileChannel[] documentsIdChannels;
    public FileChannel[] frequencyChannels;

    private static Config config;

    public MergerLoader(Config configuration, FileChannel[] documentsIdChannels, FileChannel[] frequencyChannels) {
        config = configuration;
        this.documentsIdChannels = documentsIdChannels;
        this.frequencyChannels = frequencyChannels;
    }

        private MappedByteBuffer loadDocumentsIdChannels(VocabularyEntry term, int index) throws IOException {
        return documentsIdChannels[index].map(
                FileChannel.MapMode.READ_ONLY,
                term.getDocIdOffset(),
                term.getDocIdSize()
        );
    }

        private MappedByteBuffer loadFrequencyChannels(VocabularyEntry term, int index) throws IOException {
        return frequencyChannels[index].map(
                FileChannel.MapMode.READ_ONLY,
                term.getFrequencyOffset(),
                term.getFrequencySize()
        );
    }

    public PostingList loadList(VocabularyEntry term, int index) throws IOException {
        PostingList newList;

        MappedByteBuffer docBuffer = loadDocumentsIdChannels(term, index);
        MappedByteBuffer freqBuffer = loadFrequencyChannels(term, index);

        newList = new PostingList(config, term.getTerm());

        for (int i = 0; i < term.getDocumentFrequency(); i++) {
            Posting posting = new Posting(docBuffer.getInt(), freqBuffer.getInt());
            newList.getPostings().add(posting);
        }

        return newList;
    }

    public void writeCompressedPostingListsToDisk(
            Posting currPosting,
            FileChannel docidChan,
            FileChannel frequencyChan,
            FileChannel descriptorChan,
            byte[] compressedDocs,
            byte[] compressedFreqs,
            BlockDescriptor blockDescriptor,
            long docsMemOffset,
            long freqsMemOffset,
            int postingsInBlock
    ) throws IOException {
        // Instantiation of MappedByteBuffer for integer list of docids and for integer list of freqs
        MappedByteBuffer docsBuffer = docidChan.map(FileChannel.MapMode.READ_WRITE, docsMemOffset, compressedDocs.length);
        MappedByteBuffer freqsBuffer = frequencyChan.map(FileChannel.MapMode.READ_WRITE, freqsMemOffset, compressedFreqs.length);

        // Write compressed posting lists to disk
        docsBuffer.put(compressedDocs);
        freqsBuffer.put(compressedFreqs);

        // Update the size of the block
        blockDescriptor.setDocidSize(compressedDocs.length);
        blockDescriptor.setFreqSize(compressedFreqs.length);

        // Update the max docid of the block
        blockDescriptor.setMaxDocid(currPosting.getDocId());

        // Update the number of postings in the block
        blockDescriptor.setNumPostings(postingsInBlock);

        // Write the block descriptor on disk
        blockDescriptor.saveDescriptorOnDisk(descriptorChan);
    }

    public void pushDocumentIdChannel(int i, FileChannel fileChannel) {
        documentsIdChannels[i] = fileChannel;
    }

    public void pushFrequencyChannel(int i, FileChannel fileChannel) {
        frequencyChannels[i] = fileChannel;
    }

    public void cleanup() {
        try {
            for (FileChannel fileChannel : documentsIdChannels) {
                fileChannel.close();
            }

            for (FileChannel fileChannel : frequencyChannels) {
                fileChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
