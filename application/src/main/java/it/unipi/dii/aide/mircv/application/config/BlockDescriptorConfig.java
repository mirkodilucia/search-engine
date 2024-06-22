package it.unipi.dii.aide.mircv.application.config;



//Related to the paths of the block descriptor file
public class BlockDescriptorConfig
{
    private String blockDescriptorsPath; //File
    private boolean compressionEnabled;

    public BlockDescriptorConfig(String blockDescriptorsPath, boolean compressionEnabled) {
        this.blockDescriptorsPath = blockDescriptorsPath;
        this.compressionEnabled = compressionEnabled;
    }

    public String getBlockDescriptorsPath() {
        return blockDescriptorsPath;
    }

    public void setBlockDescriptorsPath(String blockDescriptorsPath) {
        this.blockDescriptorsPath = blockDescriptorsPath;
    }

    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
    }
}
