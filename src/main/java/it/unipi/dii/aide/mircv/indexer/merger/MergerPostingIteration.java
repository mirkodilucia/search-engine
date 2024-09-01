package it.unipi.dii.aide.mircv.indexer.merger;

import it.unipi.dii.aide.mircv.indexer.model.Posting;
import it.unipi.dii.aide.mircv.indexer.model.BlockDescriptor;

import java.nio.channels.FileChannel;
import java.util.Iterator;

public class MergerPostingIteration {
    public Iterator<Posting> postingListIterator;
    public BlockDescriptor blockDescriptor;
    public int maxNumPostings;

    public int nPostingsToBeWritten;

    public MergerPostingIteration(Iterator<Posting> postingListIterator, BlockDescriptor blockDescriptor, int nPostingsToBeWritten, int maxNumPostings) {
        this.postingListIterator = postingListIterator;
        this.blockDescriptor = blockDescriptor;
        this.maxNumPostings = maxNumPostings;
        this.nPostingsToBeWritten = nPostingsToBeWritten;
    }

    public MergerFileChannel.CompressionResult writeBlock(FileChannel descriptorChannel) {
        return blockDescriptor.writeBlock(descriptorChannel);
    }

    public MergerFileChannel.CompressionResult writeCompressedBlock(FileChannel descriptorChannel) {
        return blockDescriptor.writeBlock(descriptorChannel);
    }

}
